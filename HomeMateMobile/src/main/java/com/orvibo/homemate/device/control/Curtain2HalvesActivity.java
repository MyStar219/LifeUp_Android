package com.orvibo.homemate.device.control;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.data.StatusType;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.Curtain2HalvesView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 对开窗帘支持百分比控制
 * Created by Allen on 2015/4/20.
 * modified by smagret on 2015/05/12
 * <p/>
 * 入网方法：连续按5次，第5次长按之后出现红灯蓝灯交替闪烁，共计10次，然后等一会就出现蓝灯闪烁就可以入网了，蓝灯闪烁5次就停止闪烁了。
 */
public class Curtain2HalvesActivity extends BaseControlActivity implements Curtain2HalvesView.OnProgressChangedListener, Curtain2HalvesView.OnActionClickListener {
    private static final String TAG = Curtain2HalvesActivity.class
            .getSimpleName();
    private Curtain2HalvesView curtain2HalvesView;
    private DeviceStatusDao deviceStatusDao;
    private DeviceStatus deviceStatus;
    private int currentProgress;
    private int oldProgress;
    private int progress;
    private final int REFRESH_STATUS = 1;
    private final int REFRESH_TIME = 15;
    private FrequentlyModeDao frequentlyModeDao;
    private List<FrequentlyMode> frequentlyModes = new ArrayList<FrequentlyMode>();
//    private TextView openTimesTextView;
//    private TextView closeTimesTextView;
//    private int      openTimes;
//    private int      closeTimes;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_STATUS:
                    Log.d(TAG, "handleMessage()-REFRESH_STATUS" + REFRESH_STATUS);
                    refreshStatus();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_2halves);
        init();
        frequentlyModeDao = new FrequentlyModeDao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
        frequentlyModes = frequentlyModeDao.selFrequentlyModes( deviceId);
        if (frequentlyModes.size() == 4) {
            curtain2HalvesView.setSeekBarGone();
            curtain2HalvesView.setFrequentlyModes(frequentlyModes);
        } else {
            curtain2HalvesView.setSeekBarVisible();
        }
    }

    private void init() {
        initView();
        initListener();
        initDate();
    }

    private void initView() {
        curtain2HalvesView = (Curtain2HalvesView) findViewById(R.id.curtain2HalvesView);
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
            navigationGreenBar.setText(deviceName);
        }

//        openTimesTextView = (TextView) findViewById(R.id.openTimesTextView);
//        closeTimesTextView = (TextView) findViewById(R.id.closeTimesTextView);
    }

    private void initListener() {
        curtain2HalvesView.setOnProgressChangedListener(this);
        curtain2HalvesView.setOnActionClickListener(this);
    }

    private void initDate() {
        refreshStatus();
//        openTimes = CurtainCache.getOpenTimes(mContext, deviceId);
//        closeTimes = CurtainCache.getCloseTimes(mContext, deviceId);
//        openTimesTextView.setText("Open:" + openTimes);
//        closeTimesTextView.setText("Close:" + closeTimes);
    }

    private void refreshStatus() {
        deviceStatusDao = new DeviceStatusDao();
        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        if (deviceStatus != null) {
            progress = deviceStatus.getValue1();
            currentProgress = progress;
            refreshEnable(progress);
        }
        oldProgress = currentProgress;
        curtain2HalvesView.setProgress(currentProgress);
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            ToastUtil.toastError(ErrorCode.SUCCESS);
//            curtainDropdownView.setProgress(currentProgress);
        } else {
            currentProgress = oldProgress;
            curtain2HalvesView.setProgress(currentProgress);
        }
        return super.onControlResult(uid, deviceId, result);
    }

    @Override
    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {

        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        Log.d(TAG, "OnPropertyReport()-deviceStatus" + deviceStatus);

        if (statusType == StatusType.ON_OFF_STATUS) {
            curtain2HalvesView.setProgress(value1);
            refreshEnable(value1);

//            if (value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
//                openTimes = openTimes + 1;
//                CurtainCache.setOpenTimes(mContext,openTimes,deviceId);
//                openTimesTextView.setText("Open:" + openTimes);
//            } else {
//                closeTimes = closeTimes+1;
//                CurtainCache.setCloseTimes(mContext,closeTimes,deviceId);
//                closeTimesTextView.setText("Close:" + closeTimes);
//            }
        }
    }

    private void refreshEnable(int value1) {
        if (value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON || value1 <= DeviceStatusConstant.CURTAIN_STATUS_OFF) {
//            curtain2HalvesView.setStopButtonDisable();
            if (value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON && value1 <= DeviceStatusConstant.CURTAIN_ON) {
                curtain2HalvesView.setOpenButtonDisable();
                curtain2HalvesView.setCloseButtonEnable();
            } else if (value1 == DeviceStatusConstant.CURTAIN_INIT) {
                ToastUtil.showToast(mContext.getResources().getString(R.string.curtain_init_tips));
            } else {
                curtain2HalvesView.setCloseButtonDisable();
                curtain2HalvesView.setOpenButtonEnable();
            }
        } else {
//            curtain2HalvesView.setStopButtonEnable();
            curtain2HalvesView.setOpenButtonEnable();
            curtain2HalvesView.setCloseButtonEnable();
        }
    }

    @Override
    public void onProgressChanged(int progress) {
        Log.d(getClass().getName(), "progress:" + progress);
    }

    @Override
    public void onProgressFinish(int progress) {
        Log.d(getClass().getName(), "progress:" + progress);
        if (currentProgress < progress) {
            controlDevice.curtainPercentClose(uid, deviceId, progress, 0);
            showDialog();
        } else if (currentProgress >= progress) {
            controlDevice.curtainPercentOpen(uid, deviceId, progress, 0);
            showDialog();
        }
        if (handler.hasMessages(REFRESH_STATUS)) {
            handler.removeMessages(REFRESH_STATUS);
            handler.sendEmptyMessageDelayed(REFRESH_STATUS, REFRESH_TIME);
        }
    }

    @Override
    public void onActionClick(String order, int progress) {
        if (order.equals(DeviceOrder.STOP)) {
            showDialog();
            controlDevice.curtainStop(uid, deviceId, 0);
        } else {
            showDialog();
            controlDevice.curtainPercentOpen(uid, deviceId, progress, 0);
        }
        if (handler.hasMessages(REFRESH_STATUS)) {
            handler.removeMessages(REFRESH_STATUS);
            handler.sendEmptyMessageDelayed(REFRESH_STATUS, REFRESH_TIME);
        }
    }

}
