package com.orvibo.homemate.common.launch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.orvibo.homemate.ap.ApUtil;
import com.orvibo.homemate.api.UserApi;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.load.loadtable.LoadTableCache;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.model.login.LoginConfig;
import com.orvibo.homemate.sharedPreferences.AppCache;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UpdateTimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.update.OrviboUpdateAgent;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.smartgateway.app.R;
import com.tencent.stat.StatConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 每次打开app启动界面(非安装后首次打开)。
 * Created by orvibo on 2015/5/23.
 */
public class LaunchActivity extends Activity {
    private static final String TAG = "LaunchActivity";
    private final int WHAT_ENTER = 1;
    private final int WHAT_TEST = 2;
    private Context mAppContext;
    private long mStartTime;
    private String source;
    private String ap_other_default_ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate()=====================Start App=====================");
        mStartTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        mAppContext = ViHomeProApp.getAppContext();
        if (Conf.COLLECTION_LOG) {
            LogcatHelper.getInstance(mAppContext).start();
        }
        source = mAppContext.getResources().getString(R.string.oemSource);
        ap_other_default_ssid = mAppContext.getResources().getString(R.string.ap_other_default_ssid);
        UserApi.init(source, Conf.SDK.COMMON);
        ApUtil.initSSID(ap_other_default_ssid);
        UpdateTimeCache.logAllUpdateTime(mAppContext);
        LoadTableCache.logAllTableUpdateTime(mAppContext);
        UserCache.logAllCache(mAppContext);
        String userName = UserCache.getCurrentUserName(mAppContext);
        String md5Password = UserCache.getMd5Password(mAppContext, userName);
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.needSaveLastLoginUserName = true;
        loginConfig.startApp = true;
        LoadUtil.noticeLogin(mAppContext, userName, md5Password, loginConfig);
        StatConfig.setDebugEnable(false);
        StatConfig.setAutoExceptionCaught(true);
//        setContentView(R.layout.activity_launch);
        TimeCache.clear(mAppContext);
        ViHomeApplication.getInstance().setIsActive(true);
        MainActivity.isCheckedUnbindDevice = false;//设置为没有检查过未绑定设备
//        // 调试多次登陆
//        mHandler.sendEmptyMessageDelayed(WHAT_TEST, 100);
//        mHandler.sendEmptyMessageDelayed(WHAT_TEST, 200);
//        mHandler.sendEmptyMessageDelayed(WHAT_TEST, 300);
//        if (!PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
//            ((ImageView)findViewById(R.id.tip_iv)).setImageResource(R.drawable.launch_slogan_en);
//        }
        //安装后首次打开app
//        if (AppTool.isFirstLaunchApp(mAppContext) || AppTool.isUpdatedApp(mAppContext)) {
//            Intent intent = new Intent(this, GuideActivity.class);
//            if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(md5Password)) {
//                intent.putExtra(Constant.IS_LOGIN, true);
//            }
//            startActivity(intent);
////            AppCache.saveAppVersion(mAppContext);
//            finish();
//            return;
//        }
        jugdeInstall();
        mHandler.removeMessages(WHAT_ENTER);
        if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(md5Password)) {
            mHandler.sendEmptyMessageDelayed(WHAT_ENTER, 500);
        } else {
            mHandler.sendEmptyMessageDelayed(WHAT_ENTER, Constant.LAUNCH_TIME);
        }

        if (Conf.DEBUG_DATA) {
            testAddDevices();
        }

    }

    private void testAddDevices() {
        DeviceDao deviceDao = new DeviceDao();
        Context context = getApplicationContext();
        String uid = UserCache.getCurrentMainUid(context);
        if (!TextUtils.isEmpty(uid)) {
            List<Device> devices = new ArrayList<>();
            devices.addAll(getDevices(uid, DeviceType.DIMMER, 1));
            devices.addAll(getDevices(uid, DeviceType.COLOR_TEMPERATURE_LAMP, 1));
            devices.addAll(getDevices(uid, DeviceType.RGB, 1));
            deviceDao.updateDevices(context, devices);
        }
    }

    private List<Device> getDevices(String uid, int deviceType, int deviceCount) {
        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < deviceCount; i++) {
            Device device = new Device();
            device.setUid(uid);
            device.setDeviceId(AppTool.getUUID());
            device.setExtAddr("extaddr-" + deviceType);
            device.setEndpoint(1);
            device.setDeviceName("device-" + deviceType);
            device.setDeviceType(deviceType);
            device.setDelFlag(DeleteFlag.NORMAL);
            devices.add(device);
        }
        return devices;
    }

    /**
     * 判断是否已经安装下载的apk
     */
    private void jugdeInstall() {
        OrviboUpdateAgent orviboUpdateAgent = OrviboUpdateAgent.getInstance(this);
        OrviboUpdateAgent.isInstalled(AppTool.getAppVersionCode(this));
    }

    private void enterMain(int viewType) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentKey.BOTTOME_TAB_TYPE, viewType);
        intent.putExtra(IntentKey.GATEWAY_UIDS, viewType);
        intent.putExtra(IntentKey.COCO_UIDS, viewType);
        intent.putExtra(IntentKey.LOGIN_RESULT, viewType);
        intent.putExtra(IntentKey.LOGIN_SERVER_RESULT, viewType);
        intent.putExtra(IntentKey.START_APP, true);
        startActivity(intent);
        LogUtil.d(TAG, "enterMain()-cost " + (System.currentTimeMillis() - mStartTime) + "ms");
        finish();
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.GuideActivity);
        intent.putExtra("launch", true);
        startActivity(intent);
        finish();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == WHAT_ENTER) {
                if (UserManage.getInstance(mAppContext).isLogined()) {
                    enterMain(BottomTabType.TWO_BOTTOM_TAB);
                } else {
                    toLoginActivity();
                }
            } else if (what == WHAT_TEST) {
//                // 调试e
//                String userName = UserCache.getCurrentUserName(mAppContext);
//                String md5Password = UserCache.getMd5Password(mAppContext, userName);
//                LoginConfig loginConfig = new LoginConfig();
//                loginConfig.needSaveLastLoginUserName = true;
//                loginConfig.startApp = true;
//                LoadUtil.noticeLogin(mAppContext, userName, md5Password, loginConfig);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.saveAppVersion(mAppContext);
    }

//    @Override
//    public void onBackPressed() {
//        exitLaunch();
//        super.onBackPressed();
//    }

//    private void exitLaunch() {
//        if (mHandler != null) {
//            mHandler.removeMessages(WHAT_ENTER);
//        }
//
//        if (AppTool.isStopService(mAppContext)) {
//            InfoPushManager.getInstance(mAppContext).cancelAllNotification(UserCache.getCurrentUserId(mAppContext));//清除定时通知栏信息
//            InfoPushManager.getInstance(mAppContext).setLogined(false);
//            AppTool.exitApp(mAppContext);
//            System.exit(0);
//        }
//    }


}
