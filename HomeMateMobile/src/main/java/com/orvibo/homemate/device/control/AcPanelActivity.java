package com.orvibo.homemate.device.control;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.ACPanelModelAndWindConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.ByteUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.ACPanelCircularSeekBar;
import com.orvibo.homemate.view.custom.ACPanelCircularSeekBar.OnChangeTemperatureListener;
import com.orvibo.homemate.view.custom.ACPanelCircularSeekBar.OnTouchStateListener;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 空调面板 ，wifi空调
 * Created by baoqi on 2016/3/15.
 */
public class AcPanelActivity extends BaseControlActivity implements OnChangeTemperatureListener,
        IrButton.OnControlResultListener, OnTouchStateListener {
    private String TAG = getClass().getSimpleName();
    private IrButton mBtnOpen;
    private IrButton mBtnShutdown;
    private ACPanelCircularSeekBar mCircularSeekBar;
    private IrButton mTvTemperature;
    private TextView mTemperratureTips;
    private ImageView mIvModel;
    private ImageView mIvSpeed;
    private IrButton mBtnCold;
    private IrButton mBtnHot;
    private IrButton mBtnDehumidifier;
    private IrButton mBtnAuto;
    private IrButton mBtnLow;
    private IrButton mBtnMiddle;
    private IrButton mBtnHigh;
    private IrButton mBtnAutoWind;
    private List<IrButton> mIrButtons = new ArrayList<>();
    private final int DEFAULT_TEMPERATURE = 250;
    private TextView mTvSetTemperature;
    private LinearLayout mCurrentStateLL;
    private GradientDrawable mGradientDrawable;
    private int defaut_model = ACPanelModelAndWindConstant.MODEL_AUTO;
    private int defaut_windLevel = ACPanelModelAndWindConstant.WIND_AUTO;
    private int defaut_onoff = ACPanelModelAndWindConstant.AC_CLOSE;
    private int defaut_lock = ACPanelModelAndWindConstant.AC_UNLOCK;
    private int current_model = defaut_model;
    private int current_windLevel = defaut_windLevel;
    private int current_onoff = defaut_onoff;
    private int current_Temperature = DEFAULT_TEMPERATURE;
    private int current_setTemperature = DEFAULT_TEMPERATURE;
    private int current_Lock = defaut_lock;
    private boolean isClose = false;
    private boolean isLock = false;
    private boolean isHs = false;//
    private boolean isSilence = false;//静音开关的状态
    private int lastSetTemperature = 250;
    private DeviceStatusDao mDeviceStatusDao;
    private DeviceStatus mDeviceStatus;
    private ImageView mCircularSeekBar_bg;
    private final static int WHAT_REFRESH = 0;
    private final static int WHAT_SETTEMPERATURE = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_REFRESH:
                    refresh(current_model, current_windLevel, current_onoff, current_Lock);
                    break;
                case WHAT_SETTEMPERATURE:
                    if (!isSetTemperatureing) {
                        mTemperratureTips.setText(R.string.ac_panel_current_temperature);
                        mCurrentStateLL.setVisibility(View.VISIBLE);
                        mTvSetTemperature.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };
    /**
     * 设备关闭标记
     * 这个标记是为了区分直接控制（true），联动控制（false）
     */
    private boolean closeFlag = true;
    private boolean isSetTemperatureing = false;
    /**
     * 设备离线，点击了电源、锁开关会导致其他的开关无法发送命令
     */
    private boolean isOffLine = false;
    private LinearLayout mModeAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_panel);
        initView();
        initData();
        initEvent();
        initAcPanel();
        initAcPanelStatus();

    }

    private void initAcPanelStatus() {
        mDeviceStatusDao = new DeviceStatusDao();
        mDeviceStatus = mDeviceStatusDao.selDeviceStatus(currentMainUid, device);

        if (mDeviceStatus != null&&device!=null) {
            mAcpanel.setValue1(mDeviceStatus.getValue1());
            mAcpanel.setValue2(mDeviceStatus.getValue2());
            mAcpanel.setValue3(mDeviceStatus.getValue3());
            mAcpanel.setValue4(mDeviceStatus.getValue4());
            current_model = mAcpanel.getModel();
            current_onoff = mAcpanel.getOnoff();
            current_windLevel = mAcpanel.getWindLevel();
            current_Lock = mAcpanel.getLock();
            current_setTemperature = mAcpanel.getSetTemperature(device.getDeviceType());
            if (current_onoff == ACPanelModelAndWindConstant.AC_CLOSE) {
                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));
            } else {
                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
            }
            mBtnOpen.refresh();
            if (current_Lock == ACPanelModelAndWindConstant.AC_LOCK) {
                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
            } else {
                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));
            }
            mBtnShutdown.refresh();
            refresh(current_model, current_windLevel, current_onoff, current_Lock);
        }

    }

    private void initAcPanel() {
        mAcpanel = new Acpanel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            if (DeviceType.AC_PANEL == device.getDeviceType()) {
                mCircularSeekBar.setAsACPanel();
            } else {
                mCircularSeekBar.setAsWifiACPanel();
            }
        }
        setControlDeviceBar(NavigationType.greenType, deviceName);
        refresh(current_model, current_windLevel, current_onoff, current_Lock);
    }

    private void initData() {
        //初始化circularSeekBar
        if (device.getDeviceType() == DeviceType.AC_PANEL) {
            mCircularSeekBar.setAsACPanel();
        } else {
            mCircularSeekBar.setAsWifiACPanel();
        }

        //  mCircularSeekBar.setTemperature(current_setTemperature * 10);//初始化一下进度,显示上一次设置的温度
        mCircularSeekBar.setKeyName(current_Temperature + getString(R.string.conditioner_temperature_unit));


        //初始化tvTemperture
        mTvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        mTvTemperature.setKeyName(current_Temperature + getString(R.string.conditioner_temperature_unit));
        mTvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));

        //初始化GradientDrawable.用于tvSetTemperature的背景
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.setShape(GradientDrawable.OVAL);
        mGradientDrawable.setColor(getResources().getColor(R.color.green));
        //初始化tvSetTemperature
        mTvSetTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        mTvSetTemperature.setBackgroundDrawable(mGradientDrawable);

        //初始化按键状态
        mCircularSeekBar.initStatus(AcPanelActivity.this, uid, userName, deviceId);
        for (IrButton irButton : mIrButtons) {
            //空调面板不需要学习，直接设置为学习状态
            irButton.setLearnedStatus(true);
            irButton.initStatus(AcPanelActivity.this, uid, userName, deviceId);
        }
    }

    private void initView() {
        final NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
        mModeAuto = (LinearLayout) findViewById(R.id.mode_auto);
        mCircularSeekBar_bg = (ImageView) findViewById(R.id.circularSeekBar_bg);
        mBtnOpen = (IrButton) findViewById(R.id.btnOpen);
        mBtnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        mCircularSeekBar = (ACPanelCircularSeekBar) findViewById(R.id.circularSeekBar);
        //（在拖动圆盘的过程中显示设定的温度）
        mTvSetTemperature = (TextView) findViewById(R.id.tvSetTemperature);
        mCurrentStateLL = (LinearLayout) findViewById(R.id.currentStateLL);
        //（1.在不拖动圆盘的情况下显示value2中的环境温度）
        mTvTemperature = (IrButton) findViewById(R.id.tvTemperature);
        mTemperratureTips = (TextView) findViewById(R.id.temperatureTips);
        mIvModel = (ImageView) findViewById(R.id.ivModel);
        mIvSpeed = (ImageView) findViewById(R.id.ivSpeed);
        mBtnCold = (IrButton) findViewById(R.id.btnCold);
        mBtnHot = (IrButton) findViewById(R.id.btnHot);
        mBtnDehumidifier = (IrButton) findViewById(R.id.btnDehumidifier);
        mBtnAuto = (IrButton) findViewById(R.id.btnAuto);
        mBtnLow = (IrButton) findViewById(R.id.btnLow);
        mBtnMiddle = (IrButton) findViewById(R.id.btnMiddle);
        mBtnHigh = (IrButton) findViewById(R.id.btnHigh);
        mBtnAutoWind = (IrButton) findViewById(R.id.btnAutoWind);
        mIrButtons.add(mBtnOpen);
        mIrButtons.add(mBtnShutdown);
        mIrButtons.add(mBtnCold);
        mIrButtons.add(mBtnHot);
        mIrButtons.add(mBtnDehumidifier);
        mIrButtons.add(mBtnAuto);
        mIrButtons.add(mBtnLow);
        mIrButtons.add(mBtnMiddle);
        mIrButtons.add(mBtnHigh);
        mIrButtons.add(mBtnAutoWind);
        mIrButtons.add(mTvTemperature);
        if (device != null) {
            if (device.getDeviceType() == DeviceType.AC) {
                mBtnShutdown.setVisibility(View.GONE);
                //16~31
                mCircularSeekBar_bg.setBackgroundResource(R.drawable.cw_controller_scale_on);
            } else if (device.getDeviceType() == DeviceType.AC_PANEL) {
                mBtnShutdown.setVisibility(View.VISIBLE);
                //10~30
                mCircularSeekBar_bg.setBackgroundResource(R.drawable.ac_controller_scale_on_yl);
                //艺林的空调面板模式（自动）不可用
//                mBtnAuto.setEnabled(false);
//                mBtnAuto.setCloseStatus(true);
//                mIrButtons.remove(mBtnAuto);
                mModeAuto.setVisibility(View.GONE);
            }
        }

    }

    private void initEvent() {
        mCircularSeekBar.setOnChangeTemperatureListener(this);
        mCircularSeekBar.setOnTouchStateListener(this);
        for (final IrButton irButton : mIrButtons) {
            if (irButton.equals(mTvTemperature)) {
                continue;
            }
            irButton.setOnControlResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //新建一個acpanel用於記錄点击控制后的状态
                    Acpanel acPanel = new Acpanel();
                    //把当前界面的状态值赋给acpanel
                    if (mAcpanel != null) {
                        acPanel.setValue1(mAcpanel.getValue1());
                        acPanel.setValue2(mAcpanel.getValue2());
                        acPanel.setValue3(mAcpanel.getValue3());
                        acPanel.setValue4(mAcpanel.getValue4());
                    }
                    //点击时查询设备最新的状态，如果离线，toast提示
                    if (mDeviceStatusDao != null) {
//                        //离线状态下返回的状态为null
//                        mDeviceStatus = mDeviceStatusDao.selLatestDeviceStatuse(currentMainUid, device.getDeviceType());
//                        if (mDeviceStatus != null) {
//                            isOffLine = mDeviceStatus.getOnline() == 0 ? true : false;
//                            if (isOffLine) {
//                                ToastUtil.showToast(getString(R.string.device_offline));
//                                return;
//                            }
//                        }
                        boolean isOnline = mDeviceStatusDao.isOnline(uid, deviceId);
                        if (!isOnline) {

                            ToastUtil.showToast(getString(R.string.OFFLINE_DEVICE));
                            return;
                        }
                    }
                    if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE && !v.equals(mBtnOpen)) {
                        // ToastUtil.showToast(getString(R.string.conditioner_power_close));
                        return;
                    }
                    if (mAcpanel.getLock() == ACPanelModelAndWindConstant.AC_LOCK && !v.equals(mBtnShutdown)) {
                        // ToastUtil.showToast(getString(R.string.conditioner_lock));
                        return;
                    }

                    switch (v.getId()) {
                        case R.id.btnOpen:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
                                    irButton.setOrder(DeviceOrder.ON);
                                } else {
                                    irButton.setOrder(DeviceOrder.OFF);
                                }
                            }

                            if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
                                //改变value的值并且传到主机
                                acPanel.setOnoff(ACPanelModelAndWindConstant.AC_OPEN);
                                //点击之前就要先更换背景
                                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
                                //   mBtnOpen.refresh();
                            } else {
                                acPanel.setOnoff(ACPanelModelAndWindConstant.AC_CLOSE);
                                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));
                                //  mBtnOpen.refresh();
                            }

                            break;
                        case R.id.btnShutdown:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_LOCK_SETTING);
                            }

                            if (mAcpanel.getLock() == ACPanelModelAndWindConstant.AC_LOCK) {
                                acPanel.setLock(ACPanelModelAndWindConstant.AC_UNLOCK);
                                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));
                                //mBtnShutdown.refresh();
                            } else {
                                acPanel.setLock(ACPanelModelAndWindConstant.AC_LOCK);
                                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
                                // mBtnShutdown.refresh();
                            }
                            break;
                        case R.id.btnCold:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_MODE_SETTING);
                            }

                            acPanel.setModel(ACPanelModelAndWindConstant.MODEL_COOL);
                            break;
                        case R.id.btnHot:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_MODE_SETTING);
                            }

                            acPanel.setModel(ACPanelModelAndWindConstant.MODEL_HOT);
                            break;
                        case R.id.btnDehumidifier:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_MODE_SETTING);
                            }
                            acPanel.setModel(ACPanelModelAndWindConstant.MODEL_SEND_WIND);
                            break;
                        case R.id.btnAuto:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_MODE_SETTING);
                            }

                            acPanel.setModel(ACPanelModelAndWindConstant.MODEL_AUTO);
                            break;
                        case R.id.btnLow:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_WIND_SETTING);
                            }

                            acPanel.setWindLevel(ACPanelModelAndWindConstant.WIND_LOW1);
                            break;
                        case R.id.btnMiddle:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_WIND_SETTING);
                            }

                            acPanel.setWindLevel(ACPanelModelAndWindConstant.WIND_MEDIUM3);
                            break;
                        case R.id.btnHigh:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_WIND_SETTING);
                            }

                            acPanel.setWindLevel(ACPanelModelAndWindConstant.WIND_STRONG5);
                            break;
                        case R.id.btnAutoWind:
                            if (device.getDeviceType() == DeviceType.AC) {
                                irButton.setOrder(DeviceOrder.AC_CTRL);
                            } else {
                                irButton.setOrder(DeviceOrder.AC_WIND_SETTING);
                            }

                            acPanel.setWindLevel(ACPanelModelAndWindConstant.WIND_AUTO);
                            break;
                    }
                    // 点击后需要把新的value1,value2,value3,value4传递过去
                    irButton.setACPanel(acPanel);
                    irButton.onClick();
                    closeFlag = true;
                }
            });
        }

    }

    /* 1.电源开关关闭时其他所有按钮置灰，圆盘置灰，显示最后的室温，取不到室温用25度；设定温度显示最后数值
        ，取不到数据放在25度位置上。电源关闭时，点击其他任意灰色按钮或滑块，toast提示“设备电源未开启”
       2.锁定时其他所有按钮置灰，当前温度数值不置灰。点击其他任意灰色按钮或滑块，toast提示“按键已锁定
       3.设置温度范围：16-28℃，精度为0.1度，拨动温度球时中央显示"设置温度"，3秒无操作后显示回当前温度
        （拖动圆球，设定温度实时变化，精确到0.1度，抬手停留在当前位置，
         但发送命令精度为0.5。如设定24.3，发送24.5；设定24.2发送24；设定24.5，发送24.5）
       4.模式和风力，点击后切换小icon的显示，并根据属性报告切换icon
       5.定时倒计时，沿用COCO逻辑，只可设置开关动作和周期*/
    @Override
    public void onChangeTemperature(ACPanelCircularSeekBar view, int temperature, int color) {
        //拖动过程中，温度变化的精度是0.1度
        mTvTemperature.setTextColor(color);
        if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
            mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_gray));
        }
//        DecimalFormat decimalFormat = new DecimalFormat(".#");
//        float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
//        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
//        setTemperature(temperatureText);
        setTemperature(mTvSetTemperature, temperature, false);
        //  mCircularSeekBar.setKeyName(temperatureText);
        //  mCircularSeekBar.setOrder(getCommand(temperature));
        mGradientDrawable.setColor(color);
        mTvSetTemperature.setBackgroundDrawable(mGradientDrawable);

    }

    private void setTemperature(TextView view, int temperature, boolean isSet) {
        // DecimalFormat decimalFormat = new DecimalFormat("##.#");
        // float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
        DecimalFormat decimalFormat = new DecimalFormat("##");
        // float t = Float.parseFloat(decimalFormat.format(temperature / 10));
        int t = Integer.parseInt(decimalFormat.format(temperature / 10));
        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
        SpannableStringBuilder builder = new SpannableStringBuilder(temperatureText);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_huge));
        //  builder.setSpan(absoluteSizeSpan, 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//设置（小数点和第一位小数）字体大小
        mCircularSeekBar.setKeyName(temperatureText);
        //mTvTemperature.setText(builder);
        // mTvSetTemperature.setText(builder);
        if (isSet) {
            if (device != null && device.getDeviceType() == DeviceType.AC_PANEL) {
                mAcpanel.setTemperature((temperature / 10) - 10);
            }else{
                mAcpanel.setTemperature((temperature / 10) - 16);
            }
        }
        view.setText(builder);
    }

    private void setTemperature(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_huge));
        builder.setSpan(absoluteSizeSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTemperature.setText(builder);
        mTvSetTemperature.setText(builder);
    }

    @Override
    public void onResultTemperature(ACPanelCircularSeekBar view, int temperature, int color) {

        //设置结束后，温度的精度为0.5
        //拨动温度球时中央显示"设置温度"，3秒无操作后显示回当前温度
        mTvTemperature.setTextColor(color);
        //  mTemperratureTips.setText(getString(R.string.ac_panel_current_temperature));
        setTemperature(mTvTemperature, temperature, true);
        mTvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        //  mTvTemperature.setOrder(getCommand(temperature));
        mCircularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        // mCircularSeekBar.setOrder(getCommand(temperature));
        //温度设置完后，mTvTemperature（IrButton）发送命令

        if (device.getDeviceType() == DeviceType.AC) {
            mTvTemperature.setOrder(DeviceOrder.AC_CTRL);
        } else {
            mTvTemperature.setOrder(DeviceOrder.AC_TEMPERATURE_SETTING);
        }
        mTvTemperature.setACPanel(mAcpanel);
        mTvTemperature.onClick();
        closeFlag = true;
        //  ToastUtil.showToast("over");
        // refresh(current_model, current_windLevel, current_onoff, current_Lock);
    }

    private String getCommand(int temperature) {
        return "3110" + temperature;
    }


    @Override
    public void onControlResult(int result) {
        LogUtil.d(TAG, "controlResult:" + result);
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            // refreshStatus(currentFlag);

        } else if (result == ErrorCode.OFFLINE_DEVICE) {
            //只有电源键能成功返回
            isOffLine = true;
        }
    }

    @Override
    public void onTouchState(int motionEvent) {
        if (motionEvent == MotionEvent.ACTION_DOWN) {
            isSetTemperatureing = true;
            mTemperratureTips.setText(R.string.ac_panel_setting_temperature);
            mCurrentStateLL.setVisibility(View.GONE);
            mTvSetTemperature.setVisibility(View.VISIBLE);
        } else if (motionEvent == MotionEvent.ACTION_UP) {
            isSetTemperatureing = false;

//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isSetTemperatureing) {
//                        mTemperratureTips.setText(R.string.ac_panel_current_temperature);
//                        mCurrentStateLL.setVisibility(View.VISIBLE);
//                        mTvSetTemperature.setVisibility(View.GONE);
//                    }
//                }
//            }, 3000);
            mHandler.removeMessages(WHAT_SETTEMPERATURE);
            mHandler.sendEmptyMessageDelayed(WHAT_SETTEMPERATURE, 3000);

        }
    }

    /**
     * @param value1 开关机模式风挡设置信息
     * @param value2 静音和温度设定信息
     * @param value3 扫风设定信息
     * @param value4 灯光、健康、有无随身感，温度显示方式设定信息
     */
    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (device != null && device.getDeviceId().equals(deviceId)) {
            // 因为固件的设置温度范围在（10~30），当固件的设置的范围超过这个范围，主机那边没有处理，返回的数据时不对的，暂时这样处理
            mAcpanel.setValue1(value1);
            if (!(ByteUtil.formValue2getTemperature(value2) == 255)) {
                mAcpanel.setValue2(value2);
            }
            mAcpanel.setValue3(value3);
            mAcpanel.setValue4(value4);

            isOffLine = false;
            current_onoff = mAcpanel.getOnoff();
            //联动输出设置了关闭动作时，以下当前值不去获取
            if (!(current_onoff == ACPanelModelAndWindConstant.AC_CLOSE && !closeFlag)) {
                current_model = mAcpanel.getModel();
                current_windLevel = mAcpanel.getWindLevel();
                current_Lock = mAcpanel.getLock();
            }

            // refresh(current_model, current_windLevel, current_onoff, current_Lock);
            //bug 9369 :(原因：一次控制，有两次相同的属性报告回来，两次属性报告之间有延时，造成连续的点击控制会有多次跳转问题)


            // mHandler.post(new MyRefreshRunnable());
            mHandler.removeMessages(WHAT_REFRESH);
            mHandler.sendEmptyMessageDelayed(WHAT_REFRESH, 0);

        }
    }

    private class MyRefreshRunnable implements Runnable {
        @Override
        public void run() {
            refresh(current_model, current_windLevel, current_onoff, current_Lock);
        }
    }

    private void refresh(int model, int windLevel, int onoff, int lock) {


        //刷新模式
        int resId_model = R.drawable.pic_auto_off;
        if (model == ACPanelModelAndWindConstant.MODEL_COOL) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_cooling_on : R.drawable.pic_cooling_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_HOT) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_heating_on : R.drawable.pic_heating_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_AUTO) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_auto_on : R.drawable.pic_auto_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_SEND_WIND) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_exhaust_on : R.drawable.pic_exhaust_off);
        }
        //刷新风级
        int resId_wind = R.drawable.pic_auto_wind_off;
        if (windLevel == ACPanelModelAndWindConstant.WIND_AUTO) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_auto_wind_on : R.drawable.pic_auto_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_LOW1) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_weak_wind_on : R.drawable.pic_weak_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM2 || windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM3 || windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM4) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_medium_wind_on : R.drawable.pic_medium_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_STRONG5) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_strong_wind_on : R.drawable.pic_strong_wind_off);
        }
        //设置mTvTemperature显示环境温度
        setTemperature(mTvTemperature, mAcpanel.getTemperature() * 10, false);
        //mTvTemperature.setText(mAcpanel.getTemperature()*10);
        //圆盘设置最后设定的温度
        if (!(current_onoff == ACPanelModelAndWindConstant.AC_CLOSE && !closeFlag)) {
            if (device != null) {
                int setTemperature = mAcpanel.getSetTemperature(device.getDeviceType());
//            if(device.getDeviceType()==DeviceType.AC_PANEL){
//                setTemperature=setTemperature-16;
//            }
                mCircularSeekBar.setTemperature(setTemperature * 10);
            }
        }
        //电源开关关闭时其他所有按钮置灰，圆盘置灰，显示最后的室温，取不到室温用25度；设定温度显示最后数值，取不到数据放在25度位置上。
        // 电源关闭时，点击其他任意灰色按钮或滑块，toast提示“设备电源未开启”
        if (onoff == ACPanelModelAndWindConstant.AC_CLOSE || lock == ACPanelModelAndWindConstant.AC_LOCK) {
            for (IrButton irButton : mIrButtons) {
                if (onoff == ACPanelModelAndWindConstant.AC_CLOSE) {
                    if (irButton.equals(mBtnOpen)) {
                        //电源关闭时其他所有按钮置灰，圆盘置灰
                        mCircularSeekBar.setInActivateStatus(true);
                        mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_gray));
                        mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));
                        mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.icon_lock_disable));
                        continue;
                    }
                    irButton.setCloseStatus(true);
                }
                if (lock == ACPanelModelAndWindConstant.AC_LOCK) {
                    if (irButton.equals(mBtnShutdown)) {
                        mCircularSeekBar.setLockStatus(true);
                        mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
                        mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.icon_on_off_disable));
                        continue;
                    }
                    irButton.setCloseStatus(true);
                }
            }

        } else if (onoff == ACPanelModelAndWindConstant.AC_OPEN && lock == ACPanelModelAndWindConstant.AC_UNLOCK) {
            //  mCircularSeekBar.setTemperature((mAcpanel.getSetTemperature()) * 10);
            mCircularSeekBar.setInActivateStatus(false);
            mCircularSeekBar.setLockStatus(false);
            mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_green));
            for (IrButton irButton : mIrButtons) {
                irButton.setCloseStatus(false);
            }
            mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
            mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));

        }
        mBtnOpen.refresh();
        mBtnShutdown.refresh();
        closeFlag = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
