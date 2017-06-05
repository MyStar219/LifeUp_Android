package com.orvibo.homemate.device.ap;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.ap.ApUtil;
import com.orvibo.homemate.ap.ApWifiHelper;
import com.orvibo.homemate.ap.EntityDevice;
import com.orvibo.homemate.ap.EntityWifi;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.NetChangeHelper;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.device.manage.DeviceSettingActivity;
import com.orvibo.homemate.model.DeviceBind;
import com.orvibo.homemate.model.DeviceUnbind;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.sharedPreferences.WifiCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.tencent.stat.StatService;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by Allen on 2015/8/14.
 */
public class ApBindActivity extends BaseActivity implements NetChangeHelper.OnNetChangedListener {
    //public class ApBindActivity extends BaseActivity implements NetChangeReceiver.NetChangedListener {
    private static final String TAG = ApBindActivity.class.getName();
    private ImageView deviceSearchImageView;
    private TextView countDownTextView;
    private DialogFragmentTwoButton dialogFragmentTwoButton;
    //    private NetChangeReceiver netChangeReceiver;
    private NetChangeHelper netChangeHelper;
    private AnimationDrawable anim;
    private String oldSSID, oldPassword;
    private EntityDevice entityDevice;
    private EntityWifi entityWifi;
    private boolean isRequesting = false;
    private boolean isUnbindSuccess = false;
    private boolean isBindSuccess = false;
    //    private boolean isLoadSuccess = false;
    private static final int COUNTDOWN = 1;
    private static final int CONNECT = 2;
    private int countdownTime = 60;
    private int connectTime = 20;
    private boolean isPaused = false;
    private boolean goWifiSetting = false;
    private int typeId;
    private int deviceType;
    private TextView tipsTextView;
    private GifDrawable gifFromRaw;
    private String mProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ap_bind_activity);
        typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);
        mProductName = getIntent().getStringExtra(IntentKey.PRODUCTNAME);
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        init();
        if (typeId != 0) {
                tipsTextView.setText(String.format(getString(R.string.ap_bind_device_tips), getString(typeId)));
        }
        if(mProductName!=null){
            tipsTextView.setText(String.format(getString(R.string.ap_bind_device_tips), mProductName));
        }
        switch (typeId) {
            case R.string.device_add_coco:
            case R.string.device_add_yidong:
            case R.string.device_add_s20c:
            case R.string.device_add_socket_feidiao:
                deviceType = DeviceType.COCO;
                break;
            case R.string.device_add_oujia:
            case R.string.device_add_aoke_liangyi:
                deviceType = DeviceType.CLOTHE_SHORSE;
                break;
        }
    }

    private void init() {
        deviceSearchImageView = (ImageView) findViewById(R.id.deviceSearchImageView);
        if(typeId==R.string.device_add_xiaofang_tv){
            deviceSearchImageView.setBackgroundResource(R.drawable.pic_bg_allone_connect);
        }
        try {
            gifFromRaw = new GifDrawable(getResources(), R.raw.ap_bind_anim);
            gifFromRaw.setSpeed(1);
            deviceSearchImageView.setImageDrawable(gifFromRaw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        countDownTextView = (TextView) findViewById(R.id.countDownTextView);
        tipsTextView = (TextView) findViewById(R.id.tipsTextView);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);
//        netChangeReceiver = new NetChangeReceiver(this);
//        netChangeReceiver.register(this);
        netChangeHelper = NetChangeHelper.getInstance(mAppContext);
        netChangeHelper.doCheck(this);
        Intent intent = getIntent();
        oldSSID = intent.getStringExtra(ApConstant.OLD_SSID);
        oldPassword = intent.getStringExtra(ApConstant.PASSWORD);
        int oldNetworkId = intent.getIntExtra(ApConstant.OLD_NETWORK_ID, -1);
        entityDevice = (EntityDevice) intent.getSerializableExtra(ApConstant.ENTITY_DEVICE);
        entityWifi = (EntityWifi) intent.getSerializableExtra(ApConstant.ENTITY_WIFI);

        handler.sendEmptyMessage(COUNTDOWN);
        handler.sendEmptyMessage(CONNECT);
        ApWifiHelper apWifiHelper = new ApWifiHelper(mAppContext);
//        ApWifiHelper apWifiHelper = new ApWifiHelper(ApBindActivity.this);
        if (oldNetworkId != -1) {//连接配置之前连接过的wifi
            apWifiHelper.connectWifi(oldNetworkId);
        } else {//配置之前连接的是设备ap，配置后连接配置选择的wifi
            String ssid = entityWifi.getSsid();
            String type = apWifiHelper.getSecurityType(entityWifi);
            apWifiHelper.connectWifi(ssid, oldPassword, type);
        }
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_Cancel), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cancelButton: {
                onBackPressed();
                break;
            }
        }
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
        if (dialogFragmentTwoButton != null && dialogFragmentTwoButton.isVisible()) {
            try {
                dialogFragmentTwoButton.dismiss();
            } catch (Exception e) {

            }
        }
        if (goWifiSetting) {
            goWifiSetting = false;
            startAnim();
            connectTime = 20;
            countdownTime = 60;
            handler.sendEmptyMessage(COUNTDOWN);
            handler.sendEmptyMessage(CONNECT);
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COUNTDOWN:
                    Log.d(TAG, "countdownTime:" + countdownTime);
                    if (countdownTime-- > 0) {
                        handler.sendEmptyMessageDelayed(COUNTDOWN, 1000);
                        countDownTextView.setText(countdownTime + getString(R.string.time_second));
                    } else {
                        stopAnim();
                        timeout();
                    }
                    break;
                case CONNECT:
                    if (connectTime-- > 0) {
                        handler.sendEmptyMessageDelayed(CONNECT, 1000);
                    } else {
                        onNetChanged();
                        if (!isRequesting && !isPaused) {
                            dialogFragmentTwoButton.setTitle(getString(R.string.ap_config_reconnect_wifi) + ApUtil.addDoubleQuotes(oldSSID));
                            dialogFragmentTwoButton.setRightButtonText(getString(R.string.ap_config_reconnect_go));
                            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                                @Override
                                public void onLeftButtonClick(View view) {
                                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_PopViewCancel), null);
                                }

                                @Override
                                public void onRightButtonClick(View view) {
                                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_ToConnect), null);
                                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(intent);
                                    goWifiSetting = true;
                                    stopAnim();
                                    handler.removeCallbacksAndMessages(null);
                                }
                            });
                            dialogFragmentTwoButton.show(getFragmentManager(), "");
                        }
                    }
                    break;
            }
        }
    };
    //    private Load load;
//    private Load.OnLoadListener onLoadListener;
    private DeviceBind deviceBind;

    private void startBind() {
//        load = new Load(mAppContext);
//        final Load load = Load.getInstance(this);
//        onLoadListener = new Load.OnLoadListener() {
//            @Override
//            public void onLoadFinish(final String uid, int result) {
//                LogUtil.d(TAG, "onLoadFinish- uid: " + uid + "result: " + result);
//                if (uid.equals(entityDevice.getMac()) && !isLoadSuccess) {
//                    if (result == ErrorCode.SUCCESS) {
//                        isLoadSuccess = true;
//                        UserCache.setLoginStatus(mAppContext, UserCache.getCurrentUserName(mAppContext), LoginStatus.SUCCESS);
//
//                        Device device = new DeviceDao().selAllDevices(entityDevice.getMac()).get(0);
//                        LogUtil.d(TAG, "onLoadFinish- device: " + device);
//                        Intent intent = new Intent(ApBindActivity.this, DeviceSettingActivity.class);
//                        intent.putExtra(Constant.DEVICE, device);
//                        intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
//                        startActivity(intent);
//                        finish();
//                    } else if (countdownTime > 0) {
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                load.cancelLoad();
//                                load.load(uid, 0);
//                            }
//                        }, 2000);
//                    }
//                }
//            }
//        };
//        load.setOnLoadListener(onLoadListener);

        deviceBind = new DeviceBind() {
            @Override
            public void onBindResult(final String uid, int serial, int result) {
                Log.d(TAG, "onBindResult result:" + result);
                if (!isBindSuccess && !isFinishingOrDestroyed()) {
                    if (result == ErrorCode.SUCCESS) {
                        WifiCache.savePassword(mAppContext, oldSSID, oldPassword);
                        isBindSuccess = true;
                        UserCache.setLoginStatus(mAppContext, UserCache.getCurrentUserName(mAppContext), LoginStatus.SUCCESS);
                        Device device = new DeviceDao().selAllDevices(entityDevice.getMac()).get(0);
                        LogUtil.d(TAG, "onBindResult- device: " + device);
                        Intent intent = new Intent(ApBindActivity.this, DeviceSettingActivity.class);
                        intent.putExtra(Constant.DEVICE, device);
                        intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
                        startActivity(intent);
                        finish();
                    } else if (countdownTime > 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bind(mAppContext, uid, deviceType + "");
                            }
                        }, 2000);
                    }
                }
            }
        };

        DeviceUnbind deviceUnbind = new DeviceUnbind() {
            @Override
            public void onUnbindResult(final String uid, int serial, int result) {
                Log.d(TAG, "onUnbindResult result:" + result);
                if (!isUnbindSuccess && !isFinishingOrDestroyed()) {
                    if (result == ErrorCode.SUCCESS) {
                        isUnbindSuccess = true;
                        deviceBind.bind(mAppContext, uid, deviceType + "");
                        UserCache.setLoginStatus(mAppContext, uid, LoginStatus.SUCCESS);
                        UserCache.setLoginStatus(mAppContext, UserCache.getCurrentUserName(mAppContext), LoginStatus.SUCCESS);
                    } else if (countdownTime > 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                unBind(mAppContext, uid);
                            }
                        }, 2000);
                    }
                }
            }
        };
        //TODO java.lang.NullPointerException
        if (entityDevice != null) {
            deviceUnbind.unBind(mAppContext, entityDevice.getMac());
        } else {
            LogUtil.e(TAG, "startBind()-entityDevice is null.");
        }
    }

    private void timeout() {
        if (!isPaused) {
            Log.d(TAG, "timeout");
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            if (isUnbindSuccess /**&& !isLoadSuccess*/) {
                WifiCache.savePassword(mAppContext, entityWifi.getSsid(), "");
                dialogFragmentOneButton.setTitle(getString(R.string.ap_bind_connect_fail_title));
                dialogFragmentOneButton.setContent(getString(R.string.ap_bind_connect_fail_content));
                dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(View view) {
                        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_Confirm_PsdError), null);
                        finish();
                    }
                });
            } else {
                dialogFragmentOneButton.setTitle(getString(R.string.ap_not_connect_server));
                dialogFragmentOneButton.setContent(getString(R.string.ap_not_connect_server_content));
                dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(View view) {
                        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_Confirm_ConnectError), null);
                        finish();
                    }
                });
            }
            dialogFragmentOneButton.show(getFragmentManager(), "");
        } else {
            if (isUnbindSuccess) {
                tipsTextView.setText(R.string.ap_bind_connect_fail_title);
                countDownTextView.setText(R.string.ap_bind_connect_fail_content);
            } else {
                tipsTextView.setText(R.string.ap_not_connect_server);
                countDownTextView.setText(R.string.ap_not_connect_server_content);
            }

        }
    }

    /**
     * 开始执行扫描动画
     */
    private void startAnim() {
        if (deviceSearchImageView != null && gifFromRaw != null) {
            gifFromRaw.start();
        }
    }


    /**
     * 清除扫描动画
     */
    private void stopAnim() {
        if (deviceSearchImageView != null && gifFromRaw != null) {
            gifFromRaw.stop();
        }
    }

    @Override
    public void onNetChanged() {
        if (isFinishingOrDestroyed()) {
            return;
        }
        ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = con.getActiveNetworkInfo();
        LogUtil.d(TAG, "onNetChanged()-networkInfo:" + networkInfo + ",isRequesting:" + isRequesting);
        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED && !isRequesting) {
            isRequesting = true;
            // startBind();
            try {
                if (dialogFragmentTwoButton != null && !dialogFragmentTwoButton.isHidden()) {
                    dialogFragmentTwoButton.dismissAllowingStateLoss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFinishingOrDestroyed()) {
                        return;
                    }
                    startBind();
                }
            }, 3 * 1000);//等待ViCenter重连成功
        }
    }

//    @Override
//    public void onNetChanged(NetworkInfo networkInfo) {
//        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED && !isRequesting) {
//            Log.d(TAG, "onNetChanged()-networkInfo:" + networkInfo);
//            isRequesting = true;
////            handler.postDelayed(new Runnable() {
////                @Override
////                public void run() {
//            startBind();
////                }
////            }, 10 * 1000);
//        }
//    }

    @Override
    protected void onDestroy() {
        if (gifFromRaw != null) {
            gifFromRaw = null;
        }
        countdownTime = 0;
        handler.removeCallbacksAndMessages(null);
        //   netChangeReceiver.unregister();
        if (netChangeHelper != null) {
            netChangeHelper.cancelCheck(this);
        }
//        if (load != null) {
//            load.cancelLoad();
//            load.removeListener(onLoadListener);
//            load = null;
//            onLoadListener = null;
//        }
        deviceBind = null;
        super.onDestroy();
        System.gc();
    }
}
