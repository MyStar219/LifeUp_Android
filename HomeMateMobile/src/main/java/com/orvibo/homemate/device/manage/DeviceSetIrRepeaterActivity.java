package com.orvibo.homemate.device.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.RemoteCount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.AddDevice;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.HashMap;

/**
 * Created by Allen on 2015/4/23.
 * Modified by smagret on 2015/05/05
 */
public class DeviceSetIrRepeaterActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = DeviceSetIrRepeaterActivity.class.getSimpleName();
    private Button btnMinusTV, btnPlusTV, btnMinusTVBox, btnPlusTVBox, btnMinusConditioner, btnPlusConditioner, btnMinusSelfDefine, btnPlusSelfDefine;
    private TextView tvNumberTV, tvNumberTVBox, tvNumberConditioner, tvNumberSelfDefine;
    private Button btnOK;
    private int numberTV, numberTVBox, numberConditioner, numberSelfDefine;
    private int needAddNumberTV, needAddNumberTVBox, needAddNumberConditioner, needAddNumberSelfDefine;
    private int oldNumberTV;
    private int oldNumberTVBox;
    private int oldNumberConditioner;
    private int oldNumberSelfDefine;
    private String tvName;
    private String tvBoxName;
    private String conditionerName;
    private String selfDefineName;
    private RemoteCount remoteCount;
    private RemoteCount oldRemoteCount;
    private Device device;
    private AddDeviceControl addDeviceControl;
    private HashMap<String, Integer> irDeviceCount = new HashMap<String, Integer>();
    private ToastPopup toastPopup;
    /**
     * 添加设备返回结果个数
     */
    private int returnCount = 0;

    /**
     * 添加设备个数
     */
    private int totalCount = 0;

    /**
     * 同类型遥控器最多添加5个
     */
    private int MAXCOUNT = 5;


    private String roomId, deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_ir_repeater);
        findViews();
        init();
        initData();
    }

    private void findViews() {
        btnMinusTV = (Button) findViewById(R.id.btnMinusTV);
        btnPlusTV = (Button) findViewById(R.id.btnPlusTV);
        btnMinusTVBox = (Button) findViewById(R.id.btnMinusTVBox);
        btnPlusTVBox = (Button) findViewById(R.id.btnPlusTVBox);
        btnMinusConditioner = (Button) findViewById(R.id.btnMinusConditioner);
        btnPlusConditioner = (Button) findViewById(R.id.btnPlusConditioner);
        btnMinusSelfDefine = (Button) findViewById(R.id.btnMinusSelfDefine);
        btnPlusSelfDefine = (Button) findViewById(R.id.btnPlusSelfDefine);
        tvNumberTV = (TextView) findViewById(R.id.tvNumberTV);
        tvNumberTVBox = (TextView) findViewById(R.id.tvNumberTVBox);
        tvNumberConditioner = (TextView) findViewById(R.id.tvNumberConditioner);
        tvNumberSelfDefine = (TextView) findViewById(R.id.tvNumberSelfDefine);
        btnOK = (Button) findViewById(R.id.btnOK);
    }

    private void init() {
        btnMinusTV.setOnClickListener(this);
        btnPlusTV.setOnClickListener(this);
        btnMinusTVBox.setOnClickListener(this);
        btnPlusTVBox.setOnClickListener(this);
        btnMinusConditioner.setOnClickListener(this);
        btnPlusConditioner.setOnClickListener(this);
        btnMinusSelfDefine.setOnClickListener(this);
        btnPlusSelfDefine.setOnClickListener(this);
        btnOK.setOnClickListener(this);
    }

    private void initData() {
        toastPopup = new ToastPopup();
        addDeviceControl = new AddDeviceControl(mAppContext);
        remoteCount = (RemoteCount) getIntent().getSerializableExtra("remoteCount");
        oldRemoteCount = (RemoteCount) getIntent().getSerializableExtra("oldRemoteCount");
        device = (Device) getIntent().getSerializableExtra("device");
        if (remoteCount != null) {
            numberTV = remoteCount.getTvCount();
            numberTVBox = remoteCount.getStbCount();
            numberConditioner = remoteCount.getAcCount();
            numberSelfDefine = remoteCount.getSelfDefineCount();

            tvNumberTV.setText(numberTV + "");
            tvNumberTVBox.setText(numberTVBox + "");
            tvNumberConditioner.setText(numberConditioner + "");
            tvNumberSelfDefine.setText(numberSelfDefine + "");
            if (numberTV == 0) {
                btnMinusTV.setBackgroundResource(R.drawable.bg_less_pressed);
                btnMinusTV.setClickable(false);
            }
            if (numberTVBox == 0) {
                btnMinusTVBox.setClickable(false);
                btnMinusTVBox.setBackgroundResource(R.drawable.bg_less_pressed);
            }
            if (numberConditioner == 0) {
                btnMinusConditioner.setClickable(false);
                btnMinusConditioner.setBackgroundResource(R.drawable.bg_less_pressed);
            }
            if (numberSelfDefine == 0) {
                btnMinusSelfDefine.setClickable(false);
                btnMinusSelfDefine.setBackgroundResource(R.drawable.bg_less_pressed);
            }
        } else {
            remoteCount = new RemoteCount();
        }
        if (oldRemoteCount != null) {
            oldNumberTV = oldRemoteCount.getTvCount();
            oldNumberTVBox = oldRemoteCount.getStbCount();
            oldNumberConditioner = oldRemoteCount.getAcCount();
            oldNumberSelfDefine = oldRemoteCount.getSelfDefineCount();
        }

        if (device != null) {
            roomId = device.getRoomId();
            deviceId = device.getDeviceId();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnMinusTV: {
                ivMinus(DeviceType.TV);
                break;
            }
            case R.id.btnPlusTV: {
                ivPlus(DeviceType.TV);
                break;
            }
            case R.id.btnMinusTVBox: {
                ivMinus(DeviceType.STB);
                break;
            }
            case R.id.btnPlusTVBox: {
                ivPlus(DeviceType.STB);
                break;
            }
            case R.id.btnMinusConditioner: {
                ivMinus(DeviceType.AC);
                break;
            }
            case R.id.btnPlusConditioner: {
                ivPlus(DeviceType.AC);
                break;
            }
            case R.id.btnMinusSelfDefine: {
                ivMinus(DeviceType.SELF_DEFINE_IR);
                break;
            }
            case R.id.btnPlusSelfDefine: {
                ivPlus(DeviceType.SELF_DEFINE_IR);
                break;
            }
            case R.id.btnOK: {
                addDevice();
                break;
            }
        }
    }

    private void ivMinus(int type) {
        if (type == DeviceType.TV) {
            if (numberTV > oldNumberTV) {
                tvNumberTV.setText(--numberTV + "");
                if (numberTV < MAXCOUNT && numberTV > 0) {
                    btnPlusTV.setClickable(true);
                    btnPlusTV.setBackgroundResource(R.drawable.bg_add_widget);
                }else if (numberTV == 0) {
                    btnMinusTV.setBackgroundResource(R.drawable.bg_less_pressed);
                    btnMinusTV.setClickable(false);
                }
            } else {
                showMinusErrorToast();
                btnMinusTV.setBackgroundResource(R.drawable.bg_less_pressed);
                btnMinusTV.setClickable(false);
            }
        } else if (type == DeviceType.STB) {
            if (numberTVBox > oldNumberTVBox) {
                tvNumberTVBox.setText(--numberTVBox + "");
                if (numberTVBox < MAXCOUNT&&numberTVBox > 0) {
                    btnPlusTVBox.setClickable(true);
                    btnPlusTVBox.setBackgroundResource(R.drawable.bg_add_widget);
                }else if (numberTVBox == 0) {
                    btnMinusTVBox.setClickable(false);
                    btnMinusTVBox.setBackgroundResource(R.drawable.bg_less_pressed);
                }
            } else {
                showMinusErrorToast();
                btnMinusTVBox.setClickable(false);
                btnMinusTVBox.setBackgroundResource(R.drawable.bg_less_pressed);
            }
        } else if (type == DeviceType.AC) {
            if (numberConditioner > oldNumberConditioner) {
                tvNumberConditioner.setText(--numberConditioner + "");
                if (numberConditioner < MAXCOUNT&&numberConditioner > 0) {
                    btnPlusConditioner.setClickable(true);
                    btnPlusConditioner.setBackgroundResource(R.drawable.bg_add_widget);
                }else if (numberConditioner == 0) {
                    btnMinusConditioner.setClickable(false);
                    btnMinusConditioner.setBackgroundResource(R.drawable.bg_less_pressed);
                }
            } else {
                showMinusErrorToast();
                btnMinusConditioner.setClickable(false);
                btnMinusConditioner.setBackgroundResource(R.drawable.bg_less_pressed);
            }
        } else if (type == DeviceType.SELF_DEFINE_IR) {
            if (numberSelfDefine > oldNumberSelfDefine) {
                tvNumberSelfDefine.setText(--numberSelfDefine + "");
                if (numberSelfDefine < MAXCOUNT&&numberSelfDefine > 0) {
                    btnPlusSelfDefine.setClickable(true);
                    btnPlusSelfDefine.setBackgroundResource(R.drawable.bg_add_widget);
                }else if (numberSelfDefine == 0) {
                    btnMinusSelfDefine.setClickable(false);
                    btnMinusSelfDefine.setBackgroundResource(R.drawable.bg_less_pressed);
                }
            } else {
                showMinusErrorToast();
                btnMinusSelfDefine.setClickable(false);
                btnMinusSelfDefine.setBackgroundResource(R.drawable.bg_less_pressed);
            }
        }
    }

    private void ivPlus(int type) {
        if (type == DeviceType.TV) {
            if (numberTV < MAXCOUNT) {
                tvNumberTV.setText(++numberTV + "");
                if (numberTV > oldNumberTV) {
                    btnMinusTV.setClickable(true);
                    btnMinusTV.setBackgroundResource(R.drawable.bg_less_widget);
                }
            } else {
                showPlusErrorToast();
                btnPlusTV.setClickable(false);
                btnPlusTV.setBackgroundResource(R.drawable.bg_add_pressed);
            }
        } else if (type == DeviceType.STB) {
            if (numberTVBox < MAXCOUNT) {
                tvNumberTVBox.setText(++numberTVBox + "");
                if (numberTVBox > oldNumberTVBox) {
                    btnMinusTVBox.setClickable(true);
                    btnMinusTVBox.setBackgroundResource(R.drawable.bg_less_widget);
                }
            } else {
                showPlusErrorToast();
                btnPlusTVBox.setClickable(false);
                btnPlusTVBox.setBackgroundResource(R.drawable.bg_add_pressed);
            }
        } else if (type == DeviceType.AC) {
            if (numberConditioner < MAXCOUNT) {
                tvNumberConditioner.setText(++numberConditioner + "");
                if (numberConditioner > oldNumberConditioner) {
                    btnMinusConditioner.setClickable(true);
                    btnMinusConditioner.setBackgroundResource(R.drawable.bg_less_widget);
                }
            } else {
                showPlusErrorToast();
                btnPlusConditioner.setClickable(false);
                btnPlusConditioner.setBackgroundResource(R.drawable.bg_add_pressed);
            }
        } else if (type == DeviceType.SELF_DEFINE_IR) {
            if (numberSelfDefine < MAXCOUNT) {
                tvNumberSelfDefine.setText(++numberSelfDefine + "");
                if (numberSelfDefine > oldNumberSelfDefine) {
                    btnMinusSelfDefine.setClickable(true);
                    btnMinusSelfDefine.setBackgroundResource(R.drawable.bg_less_widget);
                }
            } else {
                showPlusErrorToast();
                btnPlusSelfDefine.setClickable(false);
                btnPlusSelfDefine.setBackgroundResource(R.drawable.bg_add_pressed);
            }
        }
    }

    private void addDevice() {
        needAddNumberTV = numberTV - oldNumberTV;
        needAddNumberTVBox = numberTVBox - oldNumberTVBox;
        needAddNumberConditioner = numberConditioner - oldNumberConditioner;
        needAddNumberSelfDefine = numberSelfDefine - oldNumberSelfDefine;
        if (needAddNumberTV > 0
                || needAddNumberTVBox > 0
                || needAddNumberConditioner > 0
                || needAddNumberSelfDefine > 0) {
            showDialogNow();
        }
        if (needAddNumberTV > 0) {
            addDeviceControl.startAddDevice(currentMainUid, userName, getTvName(), DeviceType.TV, roomId, "", deviceId);
        } else {
            if (needAddNumberTVBox > 0) {
                addDeviceControl.startAddDevice(currentMainUid, userName, getTvBoxName(), DeviceType.STB, roomId, "", deviceId);
            } else {
                if (needAddNumberConditioner > 0) {
                    addDeviceControl.startAddDevice(currentMainUid, userName, getConditionerName(), DeviceType.AC, roomId, "", deviceId);
                } else {
                    if (needAddNumberSelfDefine > 0) {
                        addDeviceControl.startAddDevice(currentMainUid, userName, getSelfDefineName(), DeviceType.SELF_DEFINE_IR, roomId, "", deviceId);
                    } else {
                        dismissDialog();
                        remoteCount.setTvCount(numberTV);
                        remoteCount.setStbCount(numberTVBox);
                        remoteCount.setAcCount(numberConditioner);
                        remoteCount.setSelfDefineCount(numberSelfDefine);
                        LogUtil.d(TAG, "RemoteCount" + remoteCount);

                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("remoteCount", remoteCount);
                        intent.putExtras(bundle);
                        DeviceSetIrRepeaterActivity.this.setResult(RESULT_OK, intent);
                        DeviceSetIrRepeaterActivity.this.finish();
                    }
                }
            }
        }
    }

    private void addNextDevice(int deviceType) {


//        if (deviceType == DeviceType.TV) {
//            needAddNumberTV--;
//        } else if (deviceType == DeviceType.STB) {
//            needAddNumberTVBox--;
//        } else if (deviceType == DeviceType.AC) {
//            needAddNumberConditioner--;
//        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
//            needAddNumberSelfDefine--;
//        }

        if (needAddNumberTV > 0) {
            addDeviceControl.startAddDevice(currentMainUid, userName, getTvName(), DeviceType.TV, roomId, "", deviceId);
        } else {
            if (needAddNumberTVBox > 0) {
                addDeviceControl.startAddDevice(currentMainUid, userName, getTvBoxName(), DeviceType.STB, roomId, "", deviceId);
            } else {
                if (needAddNumberConditioner > 0) {
                    addDeviceControl.startAddDevice(currentMainUid, userName, getConditionerName(), DeviceType.AC, roomId, "", deviceId);
                } else {
                    if (needAddNumberSelfDefine > 0) {
                        addDeviceControl.startAddDevice(currentMainUid, userName, getSelfDefineName(), DeviceType.SELF_DEFINE_IR, roomId, "", deviceId);
                    } else {
                        dismissDialog();
                        remoteCount.setTvCount(numberTV);
                        remoteCount.setStbCount(numberTVBox);
                        remoteCount.setAcCount(numberConditioner);
                        remoteCount.setSelfDefineCount(numberSelfDefine);
                        LogUtil.d(TAG, "RemoteCount" + remoteCount);

                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("remoteCount", remoteCount);
                        intent.putExtras(bundle);
                        DeviceSetIrRepeaterActivity.this.setResult(RESULT_OK, intent);
                        DeviceSetIrRepeaterActivity.this.finish();
                    }
                }
            }
        }
    }

    private class AddDeviceControl extends AddDevice {

        @Override
        public void onAddDeviceResult(String uid, int serial, int result, int deviceType) {
            reSetIrDeviceNumber();
            if (result == ErrorCode.SUCCESS) {
                addNextDevice(deviceType);
            } else {
                dismissDialog();
//                tvNumberTV.setText(numberTV - needAddNumberTV + "");
//                tvNumberTVBox.setText(numberTVBox - needAddNumberTVBox + "");
//                tvNumberConditioner.setText(numberConditioner - needAddNumberConditioner + "");
//                tvNumberSelfDefine.setText(numberSelfDefine - needAddNumberSelfDefine + "");
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }

        public AddDeviceControl(Context context) {
            super(context);
        }
    }

    private void reSetIrDeviceNumber(){
        String extAddr = device.getExtAddr();
        DeviceDao deviceDao = new DeviceDao();
        oldNumberTV = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.TV);
        oldNumberTVBox = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.STB);
        oldNumberConditioner = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.AC);
        oldNumberSelfDefine = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.SELF_DEFINE_IR);

        needAddNumberTV = numberTV - oldNumberTV;
        needAddNumberTVBox = numberTVBox - oldNumberTVBox;
        needAddNumberConditioner = numberConditioner - oldNumberConditioner;
        needAddNumberSelfDefine = numberSelfDefine - oldNumberSelfDefine;
    }

    private void showMinusErrorToast() {
        toastPopup.showPopup(DeviceSetIrRepeaterActivity.this, getResources()
                .getString(R.string.minus_ir_device_error_tips), getResources()
                .getString(R.string.know), null);
    }

    private void showPlusErrorToast() {
        toastPopup.showPopup(DeviceSetIrRepeaterActivity.this, getResources()
                .getString(R.string.plus_ir_device_error_tips), getResources()
                .getString(R.string.know), null);
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    public String getTvName() {
        String tvTemp = getResources().getString(R.string.tv_name);
        return tvName = String.format(tvTemp, numberTV - needAddNumberTV + 1);
    }

    public String getTvBoxName() {
        String tvBoxTemp = getResources().getString(R.string.tvBox_name);
        return tvBoxName = String.format(tvBoxTemp, numberTVBox - needAddNumberTVBox + 1);
    }

    public String getConditionerName() {
        String conditionerTemp = getResources().getString(R.string.conditioner_name);
        return conditionerName = String.format(conditionerTemp, numberConditioner - needAddNumberConditioner + 1);
    }

    public String getSelfDefineName() {
        String selfDefineTemp = getResources().getString(R.string.selfDefine_name);
        return selfDefineName = String.format(selfDefineTemp, numberSelfDefine - needAddNumberSelfDefine + 1);
    }
}
