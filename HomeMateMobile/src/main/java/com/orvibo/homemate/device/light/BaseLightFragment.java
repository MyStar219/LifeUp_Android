package com.orvibo.homemate.device.light;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.OrviboThreadPool;
import com.orvibo.homemate.dao.DeviceSettingDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 灯的基础控制fragment
 */
public class BaseLightFragment extends BaseFragment implements
        OnPropertyReportListener,
        View.OnClickListener {
    /**
     * true正在触摸控制，当正在控制时不出来控制失败结果。
     */
    protected volatile boolean isMoving;

    /**
     * 控制rgb、调幅类型的设备，每间隔30ms再控制
     */
    public static final int TIME_DELAY_CONTROL = 30;
    /**
     * 刷新设备状态
     */
    protected static final int WHAT_REFRESH_STATUS = 10;

    /**
     * 回调设备状态
     */
    protected static final int WHAT_CALLBACK_DEVICE_STATUS = 11;

    /**
     * 控制设备
     */
    protected static final int WHAT_CONTROL = 12;

    protected DeviceStatusDao mDeviceStatusDao = new DeviceStatusDao();

    protected String uid;

    protected String deviceId;

    protected Device device;

    protected ControlDevice controlDevice;

    protected Context mContext;

    /**
     * 控制有属性报告返回
     */
    protected volatile boolean isReportProperty;


    protected Button onOffBtn;//开关状态按钮
    protected View alphaBg;//蒙版
    private String rgbDeviceId;//rgbw灯中的rgb的deviceid
    private View toggleView;

    protected void initView(View view) {
        onOffBtn = (Button) view.findViewById(R.id.onOffBtn);
        onOffBtn.setOnClickListener(this);
        alphaBg = view.findViewById(R.id.alphaBg);
        toggleView = view.findViewById(R.id.toggleView);
        toggleView.setVisibility(View.GONE);
    }

    /**
     * 初始化开关和相关自定义view
     *
     * @param value1
     */
    protected void initSwithStatus(int value1) {
        if (value1 == 0) {
            onOffBtn.setTag(true);
            onOffBtn.setBackgroundResource(R.drawable.icon_switch_light_off_selector);
            alphaBg.setVisibility(View.GONE);
        } else {
            onOffBtn.setTag(false);
            onOffBtn.setBackgroundResource(R.drawable.icon_switch_light_on_selector);
            alphaBg.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        int delayTime = DeviceSettingDao.getInstance().getLightDelayTime(rgbDeviceId);
        switch (v.getId()) {
            case R.id.onOffBtn:
                showDialog();
                if ((boolean) onOffBtn.getTag()) {
                    controlDevice.off(uid, deviceId, DeviceStatusConstant.OFF, delayTime);
                } else {
                    controlDevice.on(uid, deviceId, delayTime);
                }
                break;
        }
    }


    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case WHAT_REFRESH_STATUS:
                    OrviboThreadPool.getInstance().submitTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, deviceId);
                            if (deviceStatus != null) {
                                Message msg = mHandler.obtainMessage(WHAT_CALLBACK_DEVICE_STATUS);
                                msg.obj = deviceStatus;
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
                    break;
                case WHAT_CALLBACK_DEVICE_STATUS:
                    if (!hasMessages(WHAT_REFRESH_STATUS)) {
                        DeviceStatus deviceStatus = (DeviceStatus) msg.obj;
                        onRefreshStatus(deviceStatus);
                    }
                    break;
            }
            onHandleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = ViHomeProApp.getContext();
        device = (Device) getArguments().getSerializable(IntentKey.DEVICE);
        rgbDeviceId = getArguments().getString(MultipleLightActivity.KEY_RGB_DEVICE_ID);
        if (device != null) {
            deviceId = device.getDeviceId();
            uid = device.getUid();
        }
        PropertyReport.getInstance(getActivity().getApplicationContext()).registerPropertyReport(
                this);
        initControl();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 刷新设备状态
     *
     * @param deviceStatus
     */
    protected void onRefreshStatus(DeviceStatus deviceStatus) {

    }

    /**
     * 延时刷新设备状态
     */
    protected void sendDelayRefreshStatusMessage() {
        cancelDelayRefreshMessage();
        mHandler.sendEmptyMessageDelayed(WHAT_CALLBACK_DEVICE_STATUS, Constant.TIME_REFRESH_DEVICE_STATUS);
    }

    /**
     * 取消延时刷新状态的消息
     */
    protected void cancelDelayRefreshMessage() {
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_CALLBACK_DEVICE_STATUS);
        }
    }

    private void initControl() {
        controlDevice = new ControlDevice(ViHomeProApp.getContext()) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                dismissDialog();
                if (onControlResult(uid, deviceId, result) && result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    /**
     * 设备控制结果
     *
     * @param uid
     * @param deviceId
     * @param result
     * @return true父类会处理控制失败结果
     */
    protected boolean onControlResult(String uid, String deviceId, int result) {
        dismissDialog();
        return true;
    }

    /**
     * 最后一次控制，可以理解为松开手时的控制
     */
    protected void lastControl() {
        mHandler.removeMessages(WHAT_REFRESH_STATUS);
        mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_STATUS, Constant.TIME_REFRESH_DEVICE_STATUS);
    }

    /**
     * @return true处理控制失败结果，否则不处理。
     */
    protected boolean isProcessControlResult() {
        return !isMoving && !isReportProperty;
    }


    /**
     * 通知控制
     *
     * @param msg
     * @param noProcess
     */
    protected void sendControlMessage(Message msg, boolean noProcess) {
        if (mHandler.hasMessages(WHAT_CONTROL)) {
            //如果消息队列已经有控制消息，则不再发送控制消息
            if (noProcess) {
                return;
            } else {
                mHandler.removeMessages(WHAT_CONTROL);
            }
        }
        Bundle bundle = msg.getData();
        bundle.putBoolean(IntentKey.NO_PROCESS, noProcess);
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, TIME_DELAY_CONTROL);//延时20ms，因为松开手时有可能会同一时间触发2次相同动作
        if (!noProcess) {
            sendDelayRefreshStatusMessage();
        }
    }

    /**
     * @return true不处理属性报告
     */
    protected boolean isNoProcessProperty() {
        return isMoving || mHandler.hasMessages(WHAT_CALLBACK_DEVICE_STATUS);
    }

    /**
     * 处理handler message
     *
     * @param msg
     */
    protected void onHandleMessage(Message msg) {

    }

    @Override
    public final void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        //不处理其他设备属性报告
        if (device != null && this.uid.equals(uid) && this.deviceId.equals(deviceId)) {
            LogUtil.i(TAG, "onPropertyReport()-uid:" + uid
                    + ",deviceId:" + deviceId
                    + ",deviceType:" + deviceType
                    + ",value1:" + value1
                    + ",value2:" + value2
                    + ",value3:" + value3
                    + ",value4:" + value4
                    + ",statusType:" + statusType
            );
            // dismissDialog();
            if (isAdded() || isResumed()) {
                isReportProperty = true;
                onPropertyReport(deviceId, value1, value2, value3, value4, statusType);
            } else {
                LogUtil.e(TAG, "onPropertyReport()-" + this + " fragment is removed.");
            }
        }
    }

    protected void onPropertyReport(String deviceId, int value1, int value2, int value3, int value4, int statusType) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        if (controlDevice != null) {
            controlDevice.stopControl();
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
