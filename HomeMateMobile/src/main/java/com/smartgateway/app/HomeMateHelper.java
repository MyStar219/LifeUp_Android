package com.smartgateway.app;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orvibo.homemate.ap.ApUtil;
import com.orvibo.homemate.api.UserApi;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.MinaSocket;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.load.LoadManage;
import com.orvibo.homemate.core.load.loadtable.LoadTableCache;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.core.reconnect.Reconnect;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.HostManager;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.smartlock.LockRecordActivity;
import com.orvibo.homemate.model.login.LoginConfig;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UpdateTimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MD5;
import com.smartgateway.app.data.model.Provider;
import com.smartgateway.app.data.model.User.UserUtil;
import com.tencent.stat.StatConfig;
import com.videogo.openapi.EZOpenSDK;
import com.yolanda.nohttp.Logger;

import java.util.List;

public class HomeMateHelper {

    public void init(Context context) {
        // init from LoginActivity
        MinaSocket.resetServerHost();
        HostManager.resetCurrentServerHost();
        ViHomeApplication.getInstance().setIsManage(false);

        //软键盘弹出时不遮挡输入框
        //修复了切换账号后会登陆上次账号问题
        Reconnect.getInstance().cancel();

        UserCache.removeCurrentUserId(context);
        UserCache.removeCurrentMainUid(context);
        UserCache.removeCurrentUserName(context);

        if (EZOpenSDK.getInstance() != null) {
            EZOpenSDK.getInstance().setAccessToken(null);
        }

        TimeCache.clear(context);
        MainActivity.isCheckedUnbindDevice = false;//设置为没有检查过未绑定设备
    }

    public void login(Context context) {
        Provider provider = UserUtil.getProviderInfo(context, "HomeMate");

        if (provider == null) {
            Logger.e("provider is null, can't login to HM");
            return;
        }

        // checking for necessary for log out before login
        String userName = UserCache.getCurrentUserName(context);
        if (userName != null) {
            signOut(context);
        }

        init(context);

        String username = provider.getUsername();
        String password = provider.getPassword();

        UserCache.savePassword(context, username, password);
        HostManager.resetCurrentServerHost();
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.needSaveLastLoginUserName = true;
        loginConfig.startApp = false;
        String md5Password = MD5.encryptMD5(password);

        LoadUtil.noticeLogin(context, username, md5Password, loginConfig);
    }

    public void signOut(Context context) {
        String currentMainUid = UserCache.getCurrentMainUid(context);
        String userName = UserCache.getCurrentUserName(context);

        if (userName == null) {
            return;
        }

        new Logout(context).logout(userName, currentMainUid);
        AppTool.setHeartbeart(context, false);
        InfoPushManager.getInstance(context).cancelAllNotification(UserCache.getCurrentUserId(context));
        InfoPushManager.getInstance(context).setLogined(false);
        InfoPushManager.getInstance(context).unregisters();

        String userId = null;
        AccountDao mAccountDao = new AccountDao();
        Account account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(context));
        if (account == null && UserManage.getInstance(context).isLogined() && MinaSocket.isServerConnected()) {
            LogUtil.d("SignOut", "init()-Could not found " + userName + "'s account info,start to reload user info.");
            UpdateTimeCache.resetUpdateTime(context, userId);
        }
        if (account != null) {
            userId = account.getUserId();
        }
        UserManage.getInstance(context).exitAccount(userId);
        LoadManage.getInstance().clearAllLoadedCount();
    }

    // init from launch activity
    public void startInit() {
        Context mAppContext;
        mAppContext = ViHomeProApp.getAppContext();
//        if (Conf.COLLECTION_LOG) {
//            LogcatHelper.getInstance(mAppContext).start();
//        }
        String source = mAppContext.getResources().getString(R.string.oemSource);
        String ap_other_default_ssid = mAppContext.getResources().getString(R.string.ap_other_default_ssid);
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
        TimeCache.clear(mAppContext);
        ViHomeApplication.getInstance().setIsActive(true);
        MainActivity.isCheckedUnbindDevice = false;//设置为没有检查过未绑定设备
    }

    @Nullable
    public Device getDoorLock(Context context) {
        DeviceDao dao = new DeviceDao();

        String mainUid = UserCache.getCurrentMainUid(context);

        if (TextUtils.isEmpty(mainUid)) {
            Logger.d("main uid is null");
            return null;
        }

        List<Device> devices = dao.selAllRoomControlDevices(mainUid);

        if (devices == null || devices.isEmpty()) {
            Logger.d("devices is null or empty");
            return null;
        }

        for (Device device : devices) {
                if (device != null && device.getDeviceType() == DeviceType.LOCK) {
                    if (ProductManage.isSmartLock(device)) {
                        Logger.d("smart lock is found");
                        return device;
                    }
                }
        }

        Logger.d("smart lock is absent");
        return null;
    }

    public void openLockRecord(Context context, Device device) {
        Intent intent = new Intent(context, LockRecordActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        context.startActivity(intent);
    }
}
