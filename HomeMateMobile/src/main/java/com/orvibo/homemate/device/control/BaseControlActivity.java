package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.OrviboThreadPool;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.manage.BaseDeviceSettingActivity;
import com.orvibo.homemate.device.manage.PercentCurtainSetDeviceActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.device.timing.DeviceTimingListActivity;
import com.orvibo.homemate.device.timing.ModeTimingListActivity;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.NavigationWhiteBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备控制基类
 *
 * @author huangqiyao
 * @date 2015/1/4
 */
public class BaseControlActivity extends BaseActivity implements OnPropertyReportListener {
    private static final String TAG = BaseControlActivity.class.getSimpleName();
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

    /**
     * 设备控制类
     */
    protected ControlDevice controlDevice;
    protected String uid;
    protected Device device;
    protected String deviceId;
    protected String deviceName;
    private DeviceDao mDeviceDao;
    protected DeviceStatusDao mDeviceStatusDao;
    protected PopupWindow popupWindow;

    private FrequentlyModeDao frequentlyModeDao;
    private List<FrequentlyMode> frequentlyModes = new ArrayList<FrequentlyMode>();

    /**
     * true正在触摸控制，当正在控制时不出来控制失败结果。
     */
    protected volatile boolean isMoving;
    protected boolean isOnOffBtn = false;//是否点击的开关按钮

    /**
     * 设备上报了属性报告。如果多次控制设备，其中一些操作失败但又上报属性报告，不会提示控制失败。
     */
    protected volatile boolean isReportProperty;

    protected Acpanel mAcpanel;

    protected boolean useBaseSetting = false;//设置是否使用最新的设置基础类

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            if (what == WHAT_REFRESH_STATUS) {
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
            } else if (what == WHAT_CALLBACK_DEVICE_STATUS) {
                if (!hasMessages(WHAT_REFRESH_STATUS)) {
                    DeviceStatus deviceStatus = (DeviceStatus) msg.obj;
                    LogUtil.i(TAG, "handleMessage()-deviceStatus:" + deviceStatus);
                    onRefreshStatus(deviceStatus);
                } else {
                    LogUtil.w(TAG, "handleMessage()-Doing control,not callback deviceStatus.");
                }
            }
            onHandleMessage(msg);
        }
    };
    private int mWidth;
    private int mHeight;

    /**
     * 处理handler message
     *
     * @param msg
     */
    protected void onHandleMessage(Message msg) {
    }

    /**
     * 通知控制
     *
     * @param msg
     * @param noProcess
     */
    protected void sendControlMessage(Message msg, boolean noProcess) {
        LogUtil.e(TAG, "sendControlMessage()-noProcess:" + noProcess);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();

        }
        //Caused by: java.lang.NullPointerException:
        // Attempt to invoke virtual method 'java.io.Serializable android.os.Bundle.getSerializable(java.lang.String)' on a null object reference
        if (device == null && savedInstanceState != null && savedInstanceState.getSerializable(IntentKey.DEVICE) != null) {
            device = (Device) savedInstanceState.getSerializable(IntentKey.DEVICE);
        }
        initPopup();
        initControl();
        PropertyReport.getInstance(mContext).registerPropertyReport(
                this);
        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        frequentlyModeDao = new FrequentlyModeDao();
        //  initSystemBar(BaseControlActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceId != null) {
            //暂时屏蔽
            device = mDeviceDao.selDevice(deviceId);
            if (device != null) {
                deviceName = device.getDeviceName();
            } else {
                finish();
            }
        }
        //setControlDeviceBar(deviceName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (device != null) {
            outState.putSerializable(IntentKey.DEVICE, device);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.shareTextView: {
                popupWindow.dismiss();
                Intent intent = null;
//                if ((device != null && device.getDeviceType() == DeviceType.AC_PANEL) || (device != null && device.getDeviceType() == DeviceType.AC && device.getAppDeviceId() == AppDeviceId.AC_WIIF)) {
//                    intent = new Intent(this, TimingCountdownActivity.class);
//                    intent.putExtra(IntentKey.ACPANEL,mAcpanel);
//                } else {
//                    intent = new Intent(this, DeviceTimingListActivity.class);
//                }
                intent = new Intent(this, DeviceTimingListActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.settingsTextView: {
                popupWindow.dismiss();
                toSetDevice();
                break;
            }
            case R.id.timingModeTextView: {
                Intent intent = new Intent(this, ModeTimingListActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
        }
    }

    private void initPopup() {
        View view = View.inflate(this, R.layout.coco_device_popup, null);
        TextView settingsTextView = (TextView) view.findViewById(R.id.settingsTextView);
        settingsTextView.setOnClickListener(this);
        TextView shareTextView = (TextView) view.findViewById(R.id.shareTextView);
        shareTextView.setOnClickListener(this);
        View timingModeImageView = view.findViewById(R.id.timingModeImageView);
        TextView timingModeTextView = (TextView) view.findViewById(R.id.timingModeTextView);
        timingModeTextView.setOnClickListener(this);
        if (device != null) {
            if (ProductManage.getInstance().isAllonDevice(device) || !ProductManage.getInstance().isWifiDevice(device)) {
                //  settingsTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_time, 0, 0, 0);
                //  settingsTextView.setText(R.string.timing);
                settingsTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_settings, 0, 0, 0);
                settingsTextView.setText(R.string.setting);
                //  shareTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_settings, 0, 0, 0);
                // shareTextView.setText(R.string.setting);
                shareTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_time, 0, 0, 0);
                shareTextView.setText(R.string.timing);
            }
//            if (ProductManage.getInstance().isWifiOnOffDevice(device)) {
//                timingModeImageView.setVisibility(View.VISIBLE);
//                timingModeTextView.setVisibility(View.VISIBLE);
//            }
        }
        popupWindow = new PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        popupWindow = new PopupWindow();
//        popupWindow.setContentView(view);
//        view.measure(0, 0);
//        mWidth = view.getMeasuredWidth();
//        mHeight = view.getMeasuredHeight();
//

        PopupWindowUtil.initPopup(popupWindow, getResources().getDrawable(R.color.transparent), 1);
    }

    /**
     * 最后一次控制，可以理解为松开手时的控制
     */
    protected void lastControl() {
        mHandler.removeMessages(WHAT_REFRESH_STATUS);
        mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_STATUS, Constant.TIME_REFRESH_DEVICE_STATUS);
    }

    protected void setBar(int barType, String title, String rightText) {
        if (barType == NavigationType.greenType) {
            NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
            if (navigationGreenBar != null) {
                navigationGreenBar.setText(title);
                navigationGreenBar.setRightText(rightText);
            }
        } else if (barType == NavigationType.whiteType) {
            NavigationWhiteBar navigationWhiteBar = (NavigationWhiteBar) findViewById(R.id.nbTitle);
            if (navigationWhiteBar != null) {
                navigationWhiteBar.setText(title);
                navigationWhiteBar.setRightText(rightText);
            }
        }
    }

    protected void setControlDeviceBar(int barType, String title) {
        setBar(barType, title, getString(R.string.device_timing));
    }

    protected void setBindDeviceBar(int barType) {
        setBar(barType, getString(R.string.device_set_panel_title), getString(R.string.save));
    }

    private void initControl() {
        controlDevice = new ControlDevice(mAppContext) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                dismissDialog();
                if (onControlResult(uid, deviceId, result) && result != ErrorCode.SUCCESS) {
//                    if (!ToastUtil.toastCommonError(result)) {
//                        ToastUtil.showToast(R.string.ctrl_fail);
//                    }
//                    if (!ProductManage.getInstance().isS20(device)&&!ProductManage.getInstance().isCOCO(device)) {
                    ToastUtil.toastError(result);
//                    }
                }
            }
        };
    }

    @Override
    public final void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (device != null && this.uid.equals(uid) && this.deviceId.equals(deviceId)) {
            LogUtil.i(TAG, "onPropertyReport()-uid:" + uid
                    + ",deviceId:" + deviceId
                    + ",deviceType:" + deviceType
                    + ",appDeviceId:" + appDeviceId
                    + ",statsType:" + statsType
                    + ",value1:" + value1
                    + ",value2:" + value2
                    + ",value3:" + value3

                    + ",value4:" + value4
                    + ",alarmType:" + alarmType
            );
            // dismissDialog();
            if (!isFinishingOrDestroyed()) {
                isReportProperty = true;
                onPropertyReport(deviceId, statsType, value1, value2, value3, value4, alarmType, payloadData);
            }
        }
    }

    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {

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
        //dismissDialog();
        return true;
    }

    /**
     * @return true处理控制失败结果，否则不处理。
     */
    protected boolean isProcessControlResult() {
        return !isMoving && !isReportProperty;
    }

    /**
     * @return true不处理属性报告
     */
    protected boolean isNoProcessProperty() {
        return isMoving || mHandler.hasMessages(WHAT_CALLBACK_DEVICE_STATUS);
    }

    public void rightTitleClick(View view) {
        popupWindow.showAtLocation(view, Gravity.RIGHT | Gravity.TOP, 0, getResources().getDimensionPixelSize(R.dimen.coco_popup_margin));
//        Intent intent = new Intent(this, DeviceTimingListActivity.class);
//        intent.putExtra(IntentKey.DEVICE, device);
//        startActivity(intent);
    }

    protected void toSetDevice() {
        Intent intent = null;
        if (useBaseSetting) {
            intent = new Intent(this, BaseDeviceSettingActivity.class);
        } else if (device.getDeviceType() == DeviceType.CURTAIN_PERCENT || device.getDeviceType() == DeviceType.ROLLER_SHADES_PERCENT) {
            frequentlyModes = frequentlyModeDao.selFrequentlyModes(deviceId);
            if (frequentlyModes.size() != 0) {
                intent = new Intent(this, PercentCurtainSetDeviceActivity.class);
            } else {
                intent = new Intent(this, SetDeviceActivity.class);
            }
        } else {
            intent = new Intent(this, SetDeviceActivity.class);
        }
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

    @Override
    protected void onCancelDialog() {
        super.onCancelDialog();
        if (controlDevice != null) {
            controlDevice.stopControl();
        }
    }

    /**
     * 刷新设备状态
     *
     * @param deviceStatus
     */
    protected void onRefreshStatus(DeviceStatus deviceStatus) {

    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (device != null && isNeedRefreshView(event.uid, uid)) {
            Device tDevice = mDeviceDao.selDevice(device.getDeviceId());
            if (tDevice == null) {
                MyLogger.kLog().e("数据库找不到设备:" + device);
                finish();
            } else {
                device = tDevice;
                deviceName = device.getDeviceName();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // controlDevice.release();
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        if (controlDevice != null) {
            controlDevice.stopControl();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    public Device getDevice() {
        return device;
    }
}
