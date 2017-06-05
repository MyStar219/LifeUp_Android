package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CameraInfoDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.device.HopeMusic.HopeMusicHelper;
import com.orvibo.homemate.device.manage.CocoFAQActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.TimerPush;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;

import de.greenrobot.event.EventBus;

/**
 * NOTE:所有zigbee设备不要调用此activity，因为删除操作会解绑主机。如果一定要用此activity，请在删除设备代码添加兼容，让删除设备之发给网关。
 */
public class DeviceEditActivity extends BaseActivity implements View.OnClickListener, Handler.Callback, NavigationCocoBar.OnLeftClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener, DialogFragmentOneButton.OnButtonClickListener {
    private final String TAG = DeviceEditActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView deviceNameTextView, deviceInfoTextView;
    private LinearLayout messageSettingTextView;
    private View messageSettingView;
    private View deviceName;
    private Button deleteButton;
    private Device device;
    private boolean isDeleteYS = false;
    private Handler handler;
    private ImageView ctrl_iv;
    private MessagePushDao messagePushDao;
    private TimerPush timerPush;
    private MessagePush messagePush;
    private int isPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_edit_activity);
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        init();
        initData();
        initTimerPush();
        LogUtil.d(TAG, "onCreate()-device:" + device);
        // initSystemBar(DeviceEditActivity.this);
    }

    private void initData() {
        messagePushDao = new MessagePushDao();
        refresh();
    }

    public void initTimerPush() {
        timerPush = new TimerPush(mContext) {
            @Override
            public void onTimerPushResult(int result) {
                LogUtil.d(TAG, "onTimerPushResult() - result：" + result);
                stopProgress();
                if (result == 0) {
                    refresh();
                }
            }

            @Override
            public void onAllSetTimerPushResult(int result) {

            }
        };
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        navigationBar.setCenterText(getString(R.string.setting));
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(device.getDeviceName());
        deviceName = findViewById(R.id.deviceName);
        deviceName.setOnClickListener(this);
        messageSettingTextView = (LinearLayout) findViewById(R.id.messageSettingTextView);
        messageSettingView = findViewById(R.id.messageSettingView);
        ctrl_iv = (ImageView) findViewById(R.id.ctrl_iv);
        // messageSettingTextView.setOnClickListener(this);
        TextView faq_tv = (TextView) findViewById(R.id.faq_tv);
        faq_tv.setOnClickListener(this);
        View faq_view = findViewById(R.id.faq_view);
        faq_tv.setVisibility(View.GONE);
        faq_view.setVisibility(View.GONE);
        if (device.getDeviceType() == DeviceType.COCO || device.getDeviceType() == DeviceType.S20) {
            messageSettingTextView.setVisibility(View.VISIBLE);
            messageSettingView.setVisibility(View.VISIBLE);
            String model = device.getModel();
            if (ProductManage.getInstance().isCOCO(device) && ("7f831d28984a456698dce9372964caf3".equals(model)
                    || "2a9131f335684966a86c54ca784520d7".equals(model) || model.startsWith("E10"))) {
                faq_tv.setVisibility(View.VISIBLE);
                faq_view.setVisibility(View.VISIBLE);
            }
        }
        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.deleteButton);
//        if (device.getDeviceType() == DeviceType.VICENTER) {
//            deleteButton.setText(R.string.vicenter_delete_text);
//        } else {
//            deleteButton.setText(R.string.device_delete_unbind);
//        }
        deleteButton.setOnClickListener(this);
        handler = new Handler(this);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingCOCO_Back), null);
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DEVICE, device);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceName: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceName), null);
                Intent intent = new Intent(this, DeviceNameActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.messageSettingTextView: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_AlertSettings), null);
                Intent intent = new Intent(this, DeviceTimingInfoPushSetActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.deviceInfoTextView: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceInfo), null);
                Intent intent = new Intent(this, DeviceInfoActivity.class);
                if (device != null) {
                    intent.putExtra(Constant.GATEWAY, new GatewayDao().selGatewayByUid(device.getUid()));
                }
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.deleteButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_Delete), null);
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                if (device.getDeviceType() == DeviceType.VICENTER || device.getDeviceType() == DeviceType.MINIHUB) {
                    dialogFragmentTwoButton.setTitle(getString(R.string.vicenter_delete_title));
                    dialogFragmentTwoButton.setContent(getString(R.string.vicenter_delete_content));
                } else {
                    dialogFragmentTwoButton.setTitle(getString(R.string.device_set_delete_content));
                }
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
                dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
            }
            case R.id.faq_tv:
                Intent intent = new Intent(this, CocoFAQActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            device = (Device) data.getSerializableExtra(IntentKey.DEVICE);
            deviceNameTextView.setText(device.getDeviceName());
//            navigationBar.setCenterText(getString(R.string.setting) + device.getDeviceName());
        }
    }

//    DeviceUnbind deviceUnbind = new DeviceUnbind() {
//        @Override
//        public void onUnbindResult(String uid, int serial, int result) {
//            dismissDialog();
//            if (result == ErrorCode.SUCCESS) {
////                if (device.getDeviceType() == DeviceType.VICENTER) {
////                    toMainActivity();
////                } else {
//                    ToastUtil.showToast(R.string.device_delete_success, Toast.LENGTH_SHORT);
//                    Intent intent = new Intent(DeviceEditActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
////                }
////            } else if (device.getDeviceType() == DeviceType.VICENTER && result == ErrorCode.OFFLINE_GATEWAY) {
////                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
////                dialogFragmentOneButton.setTitle(getString(R.string.delete_success));
////                dialogFragmentOneButton.setContent(getString(R.string.vicenter_delete_success_content));
////                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
////                dialogFragmentOneButton.setButtonTextColor(getResources().getColor(R.color.blue));
////                dialogFragmentOneButton.setOnButtonClickListener(DeviceEditActivity.this);
////                dialogFragmentOneButton.showToast(getFragmentManager(),"");
//            } else {
//                ToastUtil.showToast( R.string.device_delete_failure, Toast.LENGTH_SHORT);
//            }
//        }
//    };

    DeleteDevice mDeleteDevice = new DeleteDevice(DeviceEditActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, final int result) {
            if (result == ErrorCode.SUCCESS) {
                if (isDeleteYS) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                EZOpenSDK.getInstance().deleteDevice(device.getUid());
                            } catch (BaseException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(0);
                        }
                    }.start();
                } else {
                    dismissDialog();
                    ToastUtil.showToast(R.string.device_delete_success, Toast.LENGTH_SHORT);
                    toMainActivity();
                }
            } else if ((device.getDeviceType() == DeviceType.VICENTER || device.getDeviceType() == DeviceType.MINIHUB) && result == ErrorCode.OFFLINE_GATEWAY) {
//                GatewayTool.clearGateway(DeviceEditActivity.this, uid);
                dismissDialog();
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.delete_success));
                dialogFragmentOneButton.setContent(getString(R.string.vicenter_delete_success_content));
                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                dialogFragmentOneButton.setButtonTextColor(getResources().getColor(R.color.blue));
                dialogFragmentOneButton.setOnButtonClickListener(DeviceEditActivity.this);
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else {
                dismissDialog();
                ToastUtil.showToast(R.string.device_delete_failure, Toast.LENGTH_SHORT);
            }
        }
    };

    private void toMainActivity() {
        if (device.getDeviceType() == DeviceType.VICENTER || device.getDeviceType() == DeviceType.MINIHUB) {
            EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean handleMessage(Message msg) {
        dismissDialog();
        toMainActivity();
        return true;
    }

    @Override
    public void onLeftButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_ConfirmDelete), null);
        // 判断网络是否连接
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                List<GatewayServer> gatewayServers = new GatewayServerDao().selGatewayServers();
//                for (GatewayServer gatewayServer : gatewayServers) {
//                    if (gatewayServer.getModel().equals("CMR001")){
//                        mDeleteDevice.startDeleteDeviceToServer(gatewayServer.getUid(), UserCache.getCurrentUserName(mAppContext), null, device.getDeviceType());
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }.start();
        showDialog();
        final int deviceType = device.getDeviceType();
        if (deviceType == DeviceType.CAMERA) {
            CameraInfo cameraInfo = new CameraInfoDao().selCameraInfoByUid(device.getUid());
            if (cameraInfo != null && (cameraInfo.getType() == CameraType.YS_NO_YUNTAI || cameraInfo.getType() == CameraType.YS_YUNTAI)) {
                isDeleteYS = true;
            }
        } else if(deviceType == DeviceType.BACK_MUSIC) {
            //先从向往服务器删除
            HopeMusicHelper hopeMusicHelper = new HopeMusicHelper(mAppContext);
            hopeMusicHelper.deleteDevice(device.getIrDeviceId(), new HopeMusicHelper.DeleteHopeMusciListener() {
                @Override
                public void DeleteDeviceSuccess(String uid) {
                    //再从我们服务器删除
                    mDeleteDevice.deleteWifiDeviceOrGateway(device.getUid(), UserCache.getCurrentUserName(mAppContext));
                }

                @Override
                public void DeleteDeviceFail(String msg) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.device_delete_failure, Toast.LENGTH_SHORT);
                }
            });
            return;
        }
        //普通摄像机和门锁只能本地删除
        if (ProductManage.getInstance().isWifiDevice(device)
                || device != null && (deviceType == DeviceType.VICENTER || deviceType == DeviceType.MINIHUB)) {
//                || ProductManage.getInstance().isVicenter300(device.getUid(), GatewayCache.getGatewayModel(device.getUid()))) {
            mDeleteDevice.deleteWifiDeviceOrGateway(device.getUid(), UserCache.getCurrentUserName(this));//删除主机或coco时deviceId应传空，重连后根据此值是否为空判断是发给服务器还是主机
        } else {
            mDeleteDevice.deleteZigbeeDevice(device.getUid(), UserCache.getCurrentUserName(this), device.getDeviceId(), device.getExtAddr(), device.getDeviceType());
        }
    }

    @Override
    public void onRightButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_CancelDelete), null);
    }

    @Override
    public void onButtonClick(View view) {
        MessageDao messageDao = new MessageDao();
        String userId = UserCache.getCurrentUserId(ViHomeApplication.getAppContext());
        messageDao.delSensorMessagesByUserId(userId);
        toMainActivity();
    }

    private void refresh() {
        messagePush = messagePushDao.selMessagePushByDeviceId(device.getDeviceId());
        MessagePush allMessagePush = messagePushDao.selAllSetMessagePushByType(UserCache.getCurrentUserId(mAppContext), MessagePushType.All_TIMER_TYPE);
        if (allMessagePush != null && allMessagePush.getIsPush() == MessagePushStatus.OFF) {
            isPush = MessagePushStatus.OFF;
        } else if (messagePush == null) {
            isPush = MessagePushStatus.ON;
        } else {
            isPush = messagePush.getIsPush();
        }
        if (isPush == MessagePushStatus.ON) {
            ctrl_iv.setImageLevel(1);
        } else {
            ctrl_iv.setImageLevel(0);
        }

    }

    public void control(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_AlertSettings_Switch), null);
        showProgress();
        if (isPush == 0) {
            timerPush.startSetDeviceTimerPush(device.getDeviceId(), 1);
        } else if (isPush == 1) {
            timerPush.startSetDeviceTimerPush(device.getDeviceId(), 0);
        }
    }
}
