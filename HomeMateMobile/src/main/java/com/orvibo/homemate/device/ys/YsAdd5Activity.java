package com.orvibo.homemate.device.ys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hikvision.wifi.configuration.DeviceDiscoveryListener;
import com.hikvision.wifi.configuration.DeviceInfo;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.DeviceSettingActivity;
import com.orvibo.homemate.model.DeviceBind;
import com.orvibo.homemate.model.DeviceUnbind;
import com.orvibo.homemate.sharedPreferences.WifiCache;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.util.LogUtil;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by allen on 2015/11/12.
 */
public class YsAdd5Activity extends BaseActivity {
    private static final String TAG = YsAdd5Activity.class.getName();
    private ImageView deviceSearchImageView;
    private TextView tipsTextView, countDownTextView;
    private Button cancelButton;
    private String serial, code, model, ssid, password;
    private int cameraType = -1;
    private Handler handler;
    private static final int COUNTDOWN = 1;
    private static final int ADD_DEVICE = 3;
    private int countdownTime = 60;
    private boolean isWifiConnected = false;
    private boolean isPlatConnected = false;
    private boolean isPaused = false;
    private DeviceUnbind deviceUnbind;
    private DeviceBind deviceBind;
    private GifDrawable gifFromRaw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ap_bind_activity);
        findViews();
        init();
    }

    private void findViews() {
        deviceSearchImageView = (ImageView) findViewById(R.id.deviceSearchImageView);
        try {
            gifFromRaw = new GifDrawable(getResources(), R.raw.ap_bind_anim);
            gifFromRaw.setSpeed(1);
            deviceSearchImageView.setImageDrawable(gifFromRaw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tipsTextView = (TextView) findViewById(R.id.tipsTextView);
        countDownTextView = (TextView) findViewById(R.id.countDownTextView);
        cancelButton = (Button) findViewById(R.id.cancelButton);
    }

    private void init() {
        Intent intent = getIntent();
        serial = intent.getStringExtra(IntentKey.YS_DEVICE_SERIAL);
        code = intent.getStringExtra(IntentKey.YS_DEVICE_CODE);
        model = getIntent().getStringExtra(IntentKey.YS_DEVICE_MODEL);
        if (model.contains("C2C")) {
            cameraType = CameraType.YS_NO_YUNTAI;
        } else if (model.contains("CO6")){
            cameraType = CameraType.YS_YUNTAI;
        }
        ssid = intent.getStringExtra(IntentKey.YS_WIFI_SSID);
        password = intent.getStringExtra(IntentKey.YS_WIFI_PASSOWRD);
        tipsTextView.setText(getString(R.string.add_ys_device_tips5));
        cancelButton.setOnClickListener(this);
        handler = new Handler(new MyHandlerCallback());
        handler.sendEmptyMessage(COUNTDOWN);
        initDeviceBind();
        config();
        startAnim();
    }

    private void initDeviceBind() {
        deviceUnbind = new DeviceUnbind() {
            @Override
            public void onUnbindResult(final String uid, int serial, int result) {
                Log.d(TAG, "onUnbindResult result:" + result);
                    if (result == ErrorCode.SUCCESS) {
                        deviceBind.bindCamera(mAppContext, YsAdd5Activity.this.serial, userName, code, cameraType);
                    } else  {
                        addDeviceFail();
                    }

            }
        };
        deviceBind = new DeviceBind() {
            @Override
            public void onBindResult(String uid, int serial, int result) {
                super.onBindResult(uid, serial, result);
                Message message = handler.obtainMessage();
                message.what = ADD_DEVICE;
                message.obj = (result == com.orvibo.homemate.data.ErrorCode.SUCCESS);
                handler.sendMessage(message);
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdownTime = 0;
        stopAnim();
        EZOpenSDK.getInstance().stopConfigWiFi();
        if (gifFromRaw != null) {
            gifFromRaw = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void config() {
        new Thread() {
            @Override
            public void run() {
                EZOpenSDK.getInstance().startConfigWifi(mAppContext, ssid, password, new DeviceDiscoveryListener() {
                    @Override
                    public void onDeviceLost(DeviceInfo deviceInfo) {
                        Log.d(getClass().getName(), "onDeviceLost deviceInfo:" + deviceInfo);
                    }

                    @Override
                    public void onDeviceFound(DeviceInfo deviceInfo) {
                        Log.d(getClass().getName(), "onDeviceFound deviceInfo:" + deviceInfo);
                        if (deviceInfo == null || deviceInfo.getState() == null) {
                            LogUtil.debugLog(TAG, "接收到无效的bonjour信息 为空");
                            return;
                        }
                        // 设备序列号 相等 说明是我们要添加的设备 否则不是
                        if (serial != null && serial.equals(deviceInfo.getSerialNo())) {
//                            if ("WIFI".equals(deviceInfo.getState().name())) {
//                                if (isWifiConnected) {
//                                    return;
//                                }
//                                isWifiConnected = true;
//                                LogUtil.debugLog(TAG, "接收到设备连接上wifi信息 " + deviceInfo.toString());
//                            } else if ("PLAT".equals(deviceInfo.getState().name())) {
//                                if (isPlatConnected) {
//                                    return;
//                                }
//                                isPlatConnected = true;
//                                LogUtil.debugLog(TAG, "接收到设备连接上PLAT信息 " + deviceInfo.toString());
//                                add();
//                            }
                        }

                    }

                    @Override
                    public void onError(String s, int i) {
                        Log.d(getClass().getName(), "onError s:" + s);
                    }
                });
                getCameraInfo();
            }
        }.start();
    }

    private void getCameraInfo() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isPlatConnected && !isFinishingOrDestroyed()) {
            try {
                EZOpenSDK.getInstance().getCameraInfo(serial);
            } catch (BaseException e) {
                e.printStackTrace();
                if (e.getErrorCode() == EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_ONLINE_NOT_ADD) {
                    isPlatConnected = true;
                    add();
                } else {
                    getCameraInfo();
                }
            }
        }
    }

    private void add() {
        boolean isSuccess = false;
        try {
            isSuccess = EZOpenSDK.getInstance().addDevice(serial, code);
            if (isSuccess){
                deviceUnbind.unBind(mAppContext, serial);
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }
        Log.d(getClass().getName(), "isSuccess:" + isSuccess);
        if (!isSuccess) {
            Message message = handler.obtainMessage();
            message.what = ADD_DEVICE;
            message.obj = false;
            handler.sendMessage(message);
        }
    }

    /**
     * 开始执行扫描动画
     */
    private void startAnim() {
        if (deviceSearchImageView != null&&gifFromRaw!=null) {
            gifFromRaw.start();
        }
    }


    /**
     * 清除扫描动画
     */
    private void stopAnim() {
        if (deviceSearchImageView != null&&gifFromRaw!=null) {
            gifFromRaw.stop();
        }
    }

    private void addDeviceFail() {
//        WifiCache.savePassword(mAppContext, ssid, "");
        EZOpenSDK.getInstance().stopConfigWiFi();
        if (!isFinishingOrDestroyed()) {
            Log.d(TAG, "addDeviceFail");
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.setTitle(getString(R.string.add_device_fail));
            dialogFragmentOneButton.setContent(getString(R.string.add_ys_device_fail_tips));
            dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(View view) {
                    onBackPressed();
                }
            });
            dialogFragmentOneButton.show(getFragmentManager(), "");
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cancelButton:
                onBackPressed();
                break;
        }
    }

    class MyHandlerCallback implements android.os.Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case COUNTDOWN:
                    Log.d(TAG, "countdownTime:" + countdownTime);
                    if (countdownTime-- > 0) {
                        handler.sendEmptyMessageDelayed(COUNTDOWN, 1000);
                        countDownTextView.setText(countdownTime + getString(R.string.time_second));
                    } else {
                        stopAnim();
                        addDeviceFail();
                    }
                    break;
                case ADD_DEVICE:
                    boolean isSuccess = (boolean) message.obj;
                    if (isSuccess) {
                        WifiCache.savePassword(mAppContext, ssid, password);
                        Device device = new DeviceDao().selAllDevices(serial).get(0);
                        com.orvibo.homemate.util.LogUtil.d(TAG, "onLoadFinish- device: " + device);
                        Intent intent = new Intent(YsAdd5Activity.this, DeviceSettingActivity.class);
                        intent.putExtra(Constant.DEVICE, device);
                        startActivity(intent);
                        finish();
                    } else {
                        addDeviceFail();
                    }
                    break;
            }
            return true;
        }
    }
}
