package com.orvibo.homemate.device.control;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.data.StatusType;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.CurtainWindowShadesView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 瑞祥百叶窗
 *
 * @author Smagret
 * @date 2016/04/13
 */
public class CurtainWindowShadesActivity extends BaseControlActivity implements CurtainWindowShadesView.OnActionClickListener {
    private static final String TAG = CurtainWindowShadesActivity.class
            .getSimpleName();
    private CurtainWindowShadesView curtainWindowShadesView;
    private DeviceStatusDao         deviceStatusDao;
    private DeviceStatus            deviceStatus;
    private int                     currentProgress;
    private int                     oldProgress;
    private int                     progress;
    private final int REFRESH_STATUS = 1;
    private final int REFRESH_TIME   = 15;

    /**
     * 控制模式 true：限位设置；false：表示普通控制
     */
    private boolean IS_LIMIT_SET = false;


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
        setContentView(R.layout.activity_curtain_window_shades);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    private void init() {
        IS_LIMIT_SET = getIntent().getBooleanExtra(IntentKey.LIMIT_SET, false);
        initView();
        initListener();
        initDate();
    }

    private void initView() {
        curtainWindowShadesView = (CurtainWindowShadesView) findViewById(R.id.curtainWindowShadesView);
        if (IS_LIMIT_SET) {
            curtainWindowShadesView.setLimitSet();
        }
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            if (IS_LIMIT_SET) {
                navigationGreenBar.setRightImageViewVisibility(View.GONE);
            } else {
                navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            }
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
            navigationGreenBar.setText(deviceName);
        }
    }

    private void initListener() {
        curtainWindowShadesView.setOnActionClickListener(this);
    }

    private void initDate() {
        refreshStatus();
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
        curtainWindowShadesView.setProgress(currentProgress);
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            ToastUtil.toastError(ErrorCode.SUCCESS);
        } else {
            currentProgress = oldProgress;
            curtainWindowShadesView.setProgress(currentProgress);
        }
        return super.onControlResult(uid, deviceId, result);
    }

    @Override
    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {

        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        Log.d(TAG, "OnPropertyReport()-deviceStatus" + deviceStatus);

        if (statusType == StatusType.ON_OFF_STATUS) {
            curtainWindowShadesView.setProgress(value1);
            refreshEnable(value1);
        }
    }

    private void refreshEnable(int value1) {
        if ((value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON || value1 <= DeviceStatusConstant.CURTAIN_STATUS_OFF)
                && !IS_LIMIT_SET) {
            if (value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
                curtainWindowShadesView.setOpenButtonDisable();
                curtainWindowShadesView.setCloseButtonEnable();
            } else {
                curtainWindowShadesView.setCloseButtonDisable();
                curtainWindowShadesView.setOpenButtonEnable();

            }
        } else {
            curtainWindowShadesView.setOpenButtonEnable();
            curtainWindowShadesView.setCloseButtonEnable();
        }
    }

    @Override
    public void onActionClick(String order, int progress) {
        if (order.equals(DeviceOrder.STOP)) {
            showDialog();
            controlDevice.curtainStop(uid, deviceId, 0);
        } else if (order.equals(DeviceOrder.CURTAIN_PAGE_UP)) {
            showDialog();
            controlDevice.curtainPageup(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_PAGE_DOWN)) {
            showDialog();
            controlDevice.curtainPagedown(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_COARSE_TUNE_UPPER)) {
            showDialog();
            controlDevice.setCurtainCoarseTuneUpper(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_COARSE_TUNE_LOWER)) {
            showDialog();
            controlDevice.curtainCoarseTuneLower(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_STOP_TUNING)) {
            showDialog();
            controlDevice.curtainStopTuning(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_FINE_TUNE_UPPER)) {
            showDialog();
            controlDevice.setCurtainFineTuneUpper(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_FINE_TUNE_LOWER)) {
            showDialog();
            controlDevice.setCurtainFineTuneLower(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_UPPER_POSITION)) {
            showDialog();
            controlDevice.curtainUpperPosition(uid, deviceId);
        } else if (order.equals(DeviceOrder.CURTAIN_LOWER_POSITION)) {
            showDialog();
            controlDevice.curtainLowerPosition(uid, deviceId);
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
