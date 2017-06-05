package com.orvibo.homemate.device.light;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.OrviboTime;
import com.orvibo.homemate.util.LogUtil;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 调光灯控制fragment
 */
public class DimmingLightFragment extends BaseLightFragment {
    private static final String TAG = DimmingLightFragment.class
            .getSimpleName();
    private static final int WHAT_REFRESH_STATUS = 1;

    private ImageView ivLight;
    private SeekBar seekBarLight;
    private DeviceStatusDao deviceStatusDao;
    private DeviceStatus deviceStatus;
    private int currentLightProgress;
    private int value2;
    private int value1;

    @Override
    protected void onHandleMessage(Message msg) {
        int value2 = msg.arg1;
        switch (msg.what) {
            case WHAT_REFRESH_STATUS:
                seekBarLight.setProgress(value2);
            case WHAT_CONTROL:
                int progress = msg.arg1;
                boolean noProcess = msg.getData().getBoolean(IntentKey.NO_PROCESS);
                controlLight(progress, noProcess);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dimming_light, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
        initDate();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    protected void initView(View view) {
        super.initView(view);
        ivLight = (ImageView) view.findViewById(R.id.imageView5);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
    }

    private void initListener() {
        seekBarLight.setOnSeekBarChangeListener(new SeekBarListener());
    }

    private void initDate() {
        deviceStatusDao = new DeviceStatusDao();
        initStatus();
    }

    /**
     * Init dimming light status
     */
    private void initStatus() {
        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        if (deviceStatus != null) {
            value1 = deviceStatus.getValue1();
            initSwithStatus(value1);
            value2 = deviceStatus.getValue2();
            currentLightProgress = value2;
            seekBarLight.setProgress(currentLightProgress);
            ivLight.getBackground().setAlpha(Constant.DIMMER_MAX - currentLightProgress);
        }
    }

    /**
     * 初始化开关和相关自定义view
     *
     * @param value1
     */
    @Override
    protected void initSwithStatus(int value1) {
        super.initSwithStatus(value1);
        if (value1 == 0) {
            seekBarLight.setEnabled(true);
        } else {
            seekBarLight.setEnabled(false);
        }
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        if (isProcessControlResult() && result != ErrorCode.TIMEOUT && result != ErrorCode.TIMEOUT_CD) {
            if (result != ErrorCode.SUCCESS) {
                //控制失败，还原到当前数据库的值
                initStatus();
            }
            return super.onControlResult(uid, deviceId, result);
        }
        return false;
    }

    @Override
    protected void onPropertyReport(String deviceId, int value1, int value2, int value3, int value4, int statusType) {
        super.onPropertyReport(deviceId, value1, value2, value3, value4, statusType);
        if (!isMoving) {
            initSwithStatus(value1);
        }
        if (!isNoProcessProperty()) {
//            deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//            LogUtil.i(TAG, "OnPropertyReport()-deviceStatus" + deviceStatus);
            seekBarLight.setProgress(value2);
        } else {
            if (isMoving) {
                LogUtil.w(TAG, "onPropertyReport()-moving light level,not process the propertyReport.");
            } else {
                LogUtil.w(TAG, "onPropertyReport()-Stop in " + Constant.TIME_REFRESH_DEVICE_STATUS + "ms,not process the propertyReport.");
            }
        }
    }

    private void sendControlMessage(int progress, boolean noProcess) {
        Message msg = mHandler.obtainMessage(WHAT_CONTROL);
        msg.arg1 = progress;
        sendControlMessage(msg, noProcess);
    }

    /**
     * 调整dimming灯亮度
     *
     * @param progress
     * @param noProcess true Don't process the property report.
     */
    private void controlLight(int progress, boolean noProcess) {
        if (progress == 0) {
            progress = Constant.COLOR_LIGHT_MIN;
        }
        seekBarLight.setProgress(progress);
        value2 = progress;
        controlDevice.dimmingLight(uid, deviceId, value2, noProcess);
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private static final int MIN_PROGRESS = 3;
        private static final int MIN_TIME = OrviboTime.CONTROL_INTERVAL_TIME;//ms
        private int mStartProgress;
        private int mCurrentProgress;

        private long mProgressStartTime;
        private long mProgressCurrentTime;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ivLight.getBackground().setAlpha(Constant.DIMMER_MAX - progress);
            if (fromUser) {
                mCurrentProgress = progress;
                mProgressCurrentTime = System.currentTimeMillis();
                if (isCanControl()) {
                    mStartProgress = mCurrentProgress;
                    mProgressStartTime = mProgressCurrentTime;
                    sendControlMessage(progress, true);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoving = true;
            if (controlDevice != null) {
                controlDevice.stopControl();
            }
            mStartProgress = seekBar.getProgress();
            mProgressStartTime = System.currentTimeMillis();
            isReportProperty = false;
            cancelDelayRefreshMessage();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMoving = false;
            sendControlMessage(seekBar.getProgress(), false);
            sendDelayRefreshStatusMessage();

        }

        /**
         * @return true满足控制条件。间隔时间>{@link #MIN_TIME},滑动距离>{@link #MIN_PROGRESS}
         */
        private boolean isCanControl() {
            return Math.abs(mCurrentProgress - mStartProgress) >= MIN_PROGRESS && (mProgressCurrentTime - mProgressStartTime) >= MIN_TIME;
        }
    }
}
