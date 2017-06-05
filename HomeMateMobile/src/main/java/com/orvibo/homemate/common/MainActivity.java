package com.orvibo.homemate.common;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.infopush.IInfopushPresenter;
import com.orvibo.homemate.common.infopush.InfopushPresenter;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.MinaSocket;
import com.orvibo.homemate.core.OrviboThreadPool;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.load.LoadManage;
import com.orvibo.homemate.core.load.MultiLoad;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.InfoPushType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoadDataType;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.MainActionType;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.device.hub.HubEvent;
import com.orvibo.homemate.device.hub.HubUpdateActivity;
import com.orvibo.homemate.device.searchdevice.SearchDevice;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.event.gateway.HubUpgradeEvent;
import com.orvibo.homemate.event.login.Login365Event;
import com.orvibo.homemate.event.main.LoadDataEvent;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.model.UploadLocation;
import com.orvibo.homemate.model.gateway.HubConstant;
import com.orvibo.homemate.model.location.LocationServiceUtil;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.service.LocationService;
import com.orvibo.homemate.service.ViCenterService;
import com.orvibo.homemate.sharedPreferences.FindNewVersion;
import com.orvibo.homemate.sharedPreferences.LocationCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.SmartSceneFragment;
import com.orvibo.homemate.update.OrviboUpdateAgent;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.user.ViHomePersonalFragment;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.MainFourTabView;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.smartgateway.app.R;
import com.umeng.update.UmengUpdateAgent;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * @author huangqiyao
 * @date 2015/1/4
 */
public class MainActivity extends BaseActivity implements
        MainFourTabView.OnMainTabSelectedListener,
        DialogFragmentTwoButton.OnTwoButtonClickListener,
        ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DeviceFragment mDeviceFragment;
    // private SecurityFragment mSecurityFragment;
    private SmartSceneFragment mSmartSceneFragment;
    private ViHomePersonalFragment mViHomePersonalFragment;
    private MainFourTabView mainFourTabView;

    /**
     * 处理消息推送相关类
     */
    private IInfopushPresenter mIInfopushPresenter;

    private UploadLocation uploadLocation;
    private MessageDao mMessageDao = new MessageDao();
    private OrviboUpdateAgent mOrviboUpdateAgent;
    private OrviboCheckUpdateListener mOrviboCheckUpdateListener;
    private HashMap<String, String> updateInfos;
    /**
     * 搜索主机和wifi设备
     */
    private SearchDevice mSearchDevice;
    /**
     * true：已经检查过是否需要添加的wifi设备或者主机，false还没有检查过是否需要添加设备。
     */
    public static volatile boolean isCheckedUnbindDevice = false;
    /**
     * true首页处于可见状态(如果有弹Activity但不是全屏也属于可见)
     */
    private boolean isMainVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isMainVisible = true;
        FragmentManager fm = getFragmentManager();
        mDeviceFragment = (DeviceFragment) fm.findFragmentById(R.id.device_fragment);
        //  mSecurityFragment = (SecurityFragment) fm.findFragmentById(R.id.security_fragment);
        mSmartSceneFragment = (SmartSceneFragment) fm.findFragmentById(R.id.smartscene_fragment);
        mViHomePersonalFragment = (ViHomePersonalFragment) fm.findFragmentById(R.id.me_fragment);

        mIInfopushPresenter = new InfopushPresenter(this);
        mIInfopushPresenter.onRegister();
        initBottomTab(BottomTabType.FOUR_BOTTOM_TAB);
        boolean checkAppUpdate = true;
        Intent intent = getIntent();
        //从哪个类跳转到主页
        String toMainSource = null;
        if (intent != null && intent.hasExtra(IntentKey.TO_MAIN_SOURCE)) {
            toMainSource = intent.getStringExtra(IntentKey.TO_MAIN_SOURCE);
        }
        LogUtil.d(TAG, "onCreate()-toMainSource:" + toMainSource);

        //有网情况下如果登录操作正在执行，显示进度。如果从登录页面跳转到主页，说明已经读表已经结束(或者读取重要表已经结束)，不需要再显示圈圈
        if (NetUtil.isNetworkEnable(mAppContext) && !StringUtil.isEmpty(userName) && ViCenterService.isLogining && !LoginActivity.class.getSimpleName().equals(toMainSource)) {
            //1.10版本不再显示小人跑的进度 showDialogNow();
            if (mDeviceFragment != null) {
                mDeviceFragment.showDeviceProgress();
            }
            checkAppUpdate = false;//需要等到登录结束后才检查app升级
        } else if (UserManage.getInstance(mAppContext).isLogined()) {
            //修复了接收到邀请，但不弹界面问题
            currentMainUid = UserCache.getCurrentMainUid(mAppContext);
            toRequestLocation();
            checkConnect();
            checkSearchUnbindDevice();
        } else if (!ViCenterService.isLogining && UserCache.getLoginStatus(mAppContext, userName) == LoginStatus.USERNAME_OR_PASSWORD_WRONG) {
            LogUtil.w(TAG, "onCreate()-Username or password wrong,to loginactivity");
            onEventMainThread(new MainEvent(true));
        } else {
            checkConnect();
        }
        //   registerEvent(MainActivity.this);
        if (!EventBus.getDefault().isRegistered(MainActivity.this)) {
            EventBus.getDefault().registerSticky(MainActivity.this);
        }
        // 如果正在登录，则延时4s再检查app更新；否则立即检查app升级
        int delayCheckAppUpdateTime = 0;
        if (!checkAppUpdate) {
            delayCheckAppUpdateTime = 4 * 1000;
        }
        mainFourTabView.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAppUpdate();
            }
        }, delayCheckAppUpdateTime);

        // toRequestLocation();

        //状态栏沉浸式设置
        // initSystemBar(MainActivity.this);

        //   MainEvent mainEvent = EventBus.getDefault().getStickyEvent(MainEvent.class);
        // MyLogger.kLog().e("mainEvent:" + mainEvent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG, "onNewIntent()");
        //退出账号，listener都被移除，再进入此页面时需要重新设置listener
        mIInfopushPresenter.onRegister();
        if (intent.hasExtra(IntentKey.EVENT_LOGIN_RESULT)) {
            try {
                Login365Event login365Event = (Login365Event) intent.getSerializableExtra(IntentKey.EVENT_LOGIN_RESULT);
                processLogin(login365Event, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intent.hasExtra(IntentKey.EVENT_LOAD_IMPORTANT_DATA_RESULT)) {
            LogUtil.i(TAG, "onNewIntent()-loadImportantData");
            try {
                synchronized (this) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    hideAllFragment(transaction);
                    // showFragment(transaction, mDeviceFragment, false);
                    if (mDeviceFragment != null) {
                        transaction.show(mDeviceFragment);
                        mDeviceFragment.onRefresh();
                    }
                    transaction.commitAllowingStateLoss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainFourTabView.setSelectedPosition(MainFourTabView.DEVICE_POSITION, false);
        }
    }

    private void checkConnect() {
        if (!NetUtil.isNetworkEnable(mAppContext)) {
            ToastUtil.toastCommonError(ErrorCode.NET_DISCONNECT);
        } else {
            if (!MinaSocket.isServerConnected() && !StringUtil.isEmpty(userName)) {
                LogUtil.e(TAG, "checkConnect()-Can't connect server");
                ToastUtil.toastCommonError(ErrorCode.NET_DISCONNECT);
            }
        }
    }

    /**
     * 检查app是否需要升级
     */
    private void checkAppUpdate() {
        // initUmengUpdate();
        initOrviboUpdate();
    }

    /**
     * orvibo 版本升级
     */
    private void initOrviboUpdate() {
//        mDownloadAgent = DownloadAgent.getIstance(this);
        mOrviboUpdateAgent = OrviboUpdateAgent.getInstance(mAppContext);
        mOrviboCheckUpdateListener = new OrviboCheckUpdateListener();
        mOrviboUpdateAgent.setOrviboCheckUpdateListener(mOrviboCheckUpdateListener);
        //网络可用时调用
        if (NetUtil.isNetworkEnable(this)) {
            mOrviboUpdateAgent.update();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume()-Main visible");
        isMainVisible = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ViHomeApplication.getInstance().setIsManage(false);
        refreshPersonalIconHint();

        if (!TextUtils.isEmpty(currentMainUid) && mSearchDevice != null) {
            MyLogger.kLog().w("已经添加主机，关闭添加主机的对话框");
            mSearchDevice.dismissDialog();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLogger.kLog().d();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMainVisible = false;
        MyLogger.kLog().d();
    }

    /**
     * true首页处于可见状态，可能部分被挡住
     *
     * @return
     */
    public boolean isMainVisible() {
        return isMainVisible;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment != null) {
            if (mDeviceFragment == null && fragment instanceof DeviceFragment) {
                mDeviceFragment = (DeviceFragment) fragment;
            } else if (mSmartSceneFragment == null && fragment instanceof SmartSceneFragment) {
                mSmartSceneFragment = (SmartSceneFragment) fragment;
            }
//            else if (mSecurityFragment == null && fragment instanceof SecurityFragment) {
//                mSecurityFragment = (SecurityFragment) fragment;
//            }
            else if (mViHomePersonalFragment == null && fragment instanceof ViHomePersonalFragment) {
                mViHomePersonalFragment = (ViHomePersonalFragment) fragment;
            }
        }
        super.onAttachFragment(fragment);
    }


    public final void onEventMainThread(MainEvent event) {
        if (isFinishingOrDestroyed()) {
            LogUtil.w(TAG, "onEventMainThread()-MainEvent:Activity isFinishingOrDestroyed");
            return;
        }
        final int bottomTabType = event.getBottomTabType();
        final boolean showFirstTab = event.getShowFirstTab();
        LogUtil.d(TAG, "onEventMainThread()-event:" + event);
        userName = UserCache.getCurrentUserName(mAppContext);
        final int mainActionType = event.getMainActionType();

        //退出app
        if (mainActionType == MainActionType.EXIT_APP) {
            exit();
            finish();
            return;
        }
        if (event.isToLoginActivity()) {
            dismissDialog();
            boolean isLoginActivityRunning = LoginActivity.isRunning;
            LogUtil.d(TAG, "onEventMainThread()-isLoginActivityRunning:" + isLoginActivityRunning);
            if (!isLoginActivityRunning) {
                UserManage.getInstance(mAppContext).exitAccount(UserCache.getCurrentUserId(mAppContext));
                ActivityTool.toLoginActivity(mContext, userName, Constant.MAIN_ACTIVITY, ErrorCode.USER_PASSWORD_ERROR);
                InfoPushManager.getInstance(mAppContext).cancelAllNotification(UserCache.getCurrentUserId(mAppContext));//清除定时通知栏信息
                InfoPushManager.getInstance(mAppContext).setLogined(false);
                //   InfoPushManager.getInstance(mAppContext).unregisters();
                mIInfopushPresenter.onUnRegister();
            }
            refreshFragment(BottomTabType.FOUR_BOTTOM_TAB, true, true);
        } else if (event.getLogin365Event() != null) {
            //登录结果
            Login365Event login365Event = event.getLogin365Event();
            processLogin(login365Event, true);
            //检查未绑定设备
            checkSearchUnbindDevice();
        } else if (mainActionType == MainActionType.LOAD_DATA) {
            //通知拉取数据
            LoadDataEvent loadDataEvent = event.getLoadDataEvent();
            if (loadDataEvent != null && loadDataEvent.getUid() != null) {
                if (ActivityTool.canShowActivity(this)) {
                    Intent intent = new Intent(this, LoadDataActivity.class);
                    intent.putExtra(IntentKey.UID, loadDataEvent.getUid());
                    intent.putExtra(IntentKey.GOTOMAIN, loadDataEvent.isToMain());
                    intent.putExtra(IntentKey.LOAD_DATA_TYPE, loadDataEvent.getLoadType());
                    startActivity(intent);
                } else {
                    LogUtil.w(TAG, "onEventMainThread()-App not on foreground,login at background.");
                    //app在后台，执行读表操作，不再显示读表界面
                    LoadUtil.noticeAutoLogin(mAppContext);
                }
            }
        } else if (mainActionType == MainActionType.HUB_UPGRADE) {
            //主机升级
            HubUpgradeEvent hubUpgradeEvent = event.getHubUpgradeEvent();
            LogUtil.d(TAG, "onEventMainThread()-" + hubUpgradeEvent);
            if (hubUpgradeEvent != null && hubUpgradeEvent.isSuccess() && AppTool.isAppOnForeground(mAppContext)) {
                if (hubUpgradeEvent.getUpgradeStatus() == HubConstant.Upgrade.UPGRADING) {
                    Intent hubUpgradeIntent = new Intent(this, HubUpdateActivity.class);
                    hubUpgradeIntent.putExtra(IntentKey.HUB_UPGRADE_EVENT, hubUpgradeEvent);
                    startActivity(hubUpgradeIntent);
                } else {
                    EventBus.getDefault().post(new HubEvent(null, InfoPushType.HUB_UPDATE, HubConstant.Upgrade.FINISH));
                }
                //设置切换动画
//                overridePendingTransition(R.anim.top_to_bottom_in_slow, R.anim.top_to_bottom_out_slow);
            }
        }
//        else if (mainActionType == MainActionType.LOCATION) {
//            processLocationResult(event.getLocationResultEvent());
//        }
        else if (!TextUtils.isEmpty(event.getLoadImportantDataUid())) {
            //读取完重要的数据后的回调
            String loadUid = event.getLoadImportantDataUid();
            LogUtil.i(TAG, "onEventMainThread()-loadUid:" + loadUid);
            refreshCurrentFragment();
            dismissDialog();
        } else {
            if (showFirstTab) {
                mainFourTabView.setSelectedPosition(MainFourTabView.DEVICE_POSITION, true);
            } else {
                refreshFragment(bottomTabType, showFirstTab, true);
            }
        }
    }

    private void processLogin(Login365Event event, boolean refreshDeviceList) {
        LogUtil.i(TAG, "porcessLogin()-Login365Event:" + event + ",refreshDeviceList:" + refreshDeviceList);
        if (!isFinishingOrDestroyed()) {
            int result = event.getResult();
            int serverLoginResult = event.getServerLoginResult();
//            dismissDialog();
            if (mDeviceFragment != null) {
                mDeviceFragment.stopDeviceProgress();
            }
            if ((result == ErrorCode.USER_PASSWORD_ERROR && serverLoginResult != ErrorCode.SUCCESS) || serverLoginResult == ErrorCode.USER_PASSWORD_ERROR) {
                EventBus.getDefault().post(new MainEvent(true));
            } else if (!UserManage.getInstance(mAppContext).isLogined()) {
                EventBus.getDefault().post(new MainEvent(true));
            } else {
                currentMainUid = UserCache.getCurrentMainUid(mAppContext);
                int bottomType = (currentMainUid != null && !StringUtil.isEmpty(currentMainUid)) ? BottomTabType.FOUR_BOTTOM_TAB : BottomTabType.FOUR_BOTTOM_TAB;
                mainFourTabView.setSelectedPosition(MainFourTabView.DEVICE_POSITION, true);
                // refreshFragment(bottomType, false, refreshDeviceList);
                if (serverLoginResult == ErrorCode.SUCCESS) {
                    // toRequestLocation();
                    toRequestLocation();
                }
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.toastCommonError(ErrorCode.NET_DISCONNECT);
                } else {
                    int loginStatus = UserCache.getLoginStatus(mAppContext, userName);
                    if (serverLoginResult != ErrorCode.SUCCESS
                            && !MinaSocket.isServerConnected()
                            && !StringUtil.isEmpty(userName)
                            && loginStatus != LoginStatus.SUCCESS) {
                        ToastUtil.toastCommonError(ErrorCode.ERROR_CONNECT_SERVER_FAIL);
                    } else if (result != ErrorCode.SUCCESS && serverLoginResult != ErrorCode.SUCCESS) {
                        //添加登陆提示
                        ToastUtil.toastError(ErrorCode.SOCKET_EXCEPTION);
                    }
                }
            }
        }
    }

    /**
     * @param bottomTabType {@link com.orvibo.homemate.data.BottomTabType}
     */
    private void initBottomTab(int bottomTabType) {
        mainFourTabView = (MainFourTabView) findViewById(R.id.mainFourTabView);
        mainFourTabView.setOnMainTabSelectedListener(this);
        if (bottomTabType == BottomTabType.FOUR_BOTTOM_TAB) {
            resetFourBottomTab();
        }
    }

    private void resetFourBottomTab() {
        synchronized (this) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            mainFourTabView.reset();
//            mainFourTabView.setSelectedPosition(MainFourTabView.DEVICE_POSITION, false);
        }
    }

    @Override
    public void onMainFourTabSelected(int position) {
        LogUtil.d(TAG, "onMainFourTabSelected()-position:" + position + ",thread:" + Thread.currentThread());
        if (isFinishingOrDestroyed()) {
            return;
        }
        synchronized (this) {
            try {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                hideAllFragment(transaction);
                switch (position) {
                    case MainFourTabView.DEVICE_POSITION:
                        if (mDeviceFragment == null) {
                            mDeviceFragment = new DeviceFragment();
                        }
                        showFragment(transaction, mDeviceFragment, true);
                        break;
                    case MainFourTabView.SCENE_POSITION:
                        if (mSmartSceneFragment == null) {
                            mSmartSceneFragment = new SmartSceneFragment();
                        }
                        showFragment(transaction, mSmartSceneFragment, true);
                        break;
//                    case MainFourTabView.SECURITY_POSITION:
//                        if (mSecurityFragment == null) {
//                            mSecurityFragment = new SecurityFragment();
//                        }
//                        showFragment(transaction, mSecurityFragment, true);
//                        break;
                    case MainFourTabView.PERSONAL_POSITION:
                        if (mViHomePersonalFragment == null) {
                            mViHomePersonalFragment = new ViHomePersonalFragment();
                        }
                        showFragment(transaction, mViHomePersonalFragment, true);
                        refreshPersonalIconHint();
                        break;
                }
                transaction.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshPersonalIconHint() {
        int infoPushCount = mMessageDao.selUnreadCount(UserCache.getCurrentUserId(ViHomeApplication.getAppContext()));
        if (infoPushCount > 0) {
            mainFourTabView.showPersonalIconHint();
        } else {
            mainFourTabView.hidePersonalIconHint();
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (isFinishingOrDestroyed()) {
            MyLogger.kLog().e("isFinishingOrDestroyed");
            return;
        }
        //true首页处于可见状态(如果有弹Activity但不是全屏也属于可见)
        //修复在more tab上有弹框(人体传感器消息)
        if (isMainVisible()) {
            //新版本读表不再回调多次，只会回调2次
//            if (Login365.isLoginging()) {
//                //防止刷新频繁
//                LogUtil.e(TAG, "onRefresh()-Login is doing...");
//                return;
//            }
            //读完单个表不刷新界面
            final int loadDataType = event.loadDataType;
            if (loadDataType == LoadDataType.LOAD_TABLE_DATA) {
                if (isFragmentVisible(mViHomePersonalFragment)) {
                    refreshCurrentFragment();
                } else {
                    LogUtil.d(TAG, "onRefresh()-Don't refresh table data.");
                }
            } else {
                refreshCurrentFragment();
            }
        } else {
//            if (isFragmentVisible(mSecurityFragment)) {
//                mSecurityFragment.onRefresh();
//            }
            LogUtil.w(TAG, "onRefresh()-Index is hide,don't refresh.");
        }
        //刷新个人中心消息红点界面
        refreshPersonalIconHint();
    }

    /**
     * 刷新当前可见的fragment
     */
    private void refreshCurrentFragment() {
        if (isFragmentVisible(mDeviceFragment)) {
            mDeviceFragment.onRefresh();
        } else if (isFragmentVisible(mSmartSceneFragment)) {
            mSmartSceneFragment.onRefresh();
        }
//        else if (isFragmentVisible(mSecurityFragment)) {
//            mSecurityFragment.onRefresh();
//        }
        else if (isFragmentVisible(mViHomePersonalFragment)) {
            mViHomePersonalFragment.onRefresh();
        }
    }

    private boolean isFragmentVisible(BaseFragment fragment) {
        return fragment != null && fragment.isVisible();
    }

    private void hideAllFragment(FragmentTransaction transaction) {
        if (mDeviceFragment != null) {
//        if (mDeviceFragment != null && mDeviceFragment.isFragmentVisible()) {
            transaction.hide(mDeviceFragment);
        }
        if (mSmartSceneFragment != null) {
//        if (mSmartSceneFragment != null && mSmartSceneFragment.isFragmentVisible()) {
            transaction.hide(mSmartSceneFragment);
        }
//        if (mSecurityFragment != null) {
//            transaction.hide(mSecurityFragment);
//        }
        if (mViHomePersonalFragment != null) {
//        if (mViHomePersonalFragment != null && mViHomePersonalFragment.isFragmentVisible()) {
            transaction.hide(mViHomePersonalFragment);
        }
    }

    /**
     * @param ft
     * @param baseFragment
     * @param onlyShow     true只是显示fragment，不会通知回调BaseFragment#onVisible()接口。
     */
    private void showFragment(FragmentTransaction ft, BaseFragment baseFragment, boolean onlyShow) {
        try {
            boolean isAdded = baseFragment.isAdded();
            LogUtil.d(TAG, "showFragment()-baseFragment:" + baseFragment + ",isAdded:" + isAdded + ",onlyShow:" + onlyShow);
//            if (!isAdded) {
//                ft.add(R.id.container, baseFragment);
//            } else {
            ft.show(baseFragment);
//                if (!onlyShow) {
            baseFragment.onVisible();
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Double click to exit
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Conf.COLLECTION_LOG) {
                LogcatHelper.getInstance(mAppContext).stop();
            }
            MultiLoad.getInstance(mAppContext).clearListeners();
            exit();
            if (AppTool.isStopService(mAppContext)) {
                System.exit(0);
            } else {
                //低内存重启service
                if (AppTool.isLowMemory(mAppContext)) {
                    stopService(new Intent(this, ViCenterService.class));
                    AppTool.reStartVicenterService(mAppContext);
                } else {
                    //  AppTool.goDesktop(this);
                }
            }


        }
        return super.onKeyDown(keyCode, event);
    }

    private void displayBriefMemory() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        LogUtil.i(TAG, "displayBriefMemory()-系统剩余内存:" + (info.availMem >> 10) + "k");
        LogUtil.i(TAG, "displayBriefMemory()-系统是否处于低内存运行：" + info.lowMemory);
        LogUtil.i(TAG, "displayBriefMemory()-当系统剩余内存低于" + info.threshold + "时就看成低内存运行");

    }

    @Override
    protected void onDestroy() {
        LogUtil.w(TAG, "onDestroy()");
//        InfoPushManager.getInstance(mAppContext).unregisterWarningListener(this);
//        InfoPushManager.getInstance(mAppContext).unregisterPushInviteFamilyListener(this);
//        InfoPushManager.getInstance(mAppContext).unregisterPushInviteFamilyResultListener(this);
        if (EventBus.getDefault().isRegistered(MainActivity.this)) {
            EventBus.getDefault().unregister(MainActivity.this);
        }

        stopService(new Intent(this, LocationService.class));
//        if (mLocationCity != null) {
//            mLocationCity.stop();
//            mLocationCity.releaseMemory();
//            mLocationCity = null;
//        }
        mOrviboCheckUpdateListener = null;
        super.onDestroy();
    }

    private void exit() {
        LogUtil.e(TAG, "exit()-finish activity");
        UmengUpdateAgent.setUpdateListener(null);
        if (AppTool.isStopService(mAppContext)) {
            AppTool.stopService(mAppContext);
            InfoPushManager.getInstance(mAppContext).cancelAllNotification(UserCache.getCurrentUserId(mAppContext));//清除定时通知栏信息
            InfoPushManager.getInstance(mAppContext).setLogined(false);
        }

        LoadManage.getInstance().clearAllLoadedCount();
        mIInfopushPresenter.onUnRegister();
    }

    /**
     * @param bottomType
     * @param showFirstTab
     * @param refreshDeviceList true刷新设备列表
     */
    private void refreshFragment(int bottomType, boolean showFirstTab, boolean refreshDeviceList) {
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "refreshFragment()-showFirstTab:" + showFirstTab);
        }
        if (refreshDeviceList) {
            refreshFragment(mDeviceFragment);
        }
        refreshFragment(mSmartSceneFragment);
        refreshFragment(mViHomePersonalFragment);
        refreshPersonalIconHint();
    }

    private void refreshFragment(BaseFragment fragment) {
        if (fragment != null) {
            boolean isAdded = fragment.isAdded();
            LogUtil.d(TAG, "refreshFragment()-fragment:" + fragment + ",isAdded:" + isAdded);
            if (isAdded) {
                if (fragment.isVisible()) {
                    fragment.onRefresh();
                } else {
                    LogUtil.d(TAG, "refreshFragment()-fragment:" + fragment + " is hide");
                }
            }
        }
    }

    private void showNoLocationPermissionPopup() {
        View view = mainFourTabView;
        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        NoLocationPermissionPopup noLocationPermissionPopup = new NoLocationPermissionPopup();
                        noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                                getResources().getString(R.string.location_no_permission_tips),
                                getResources().getString(R.string.to_set),
                                getResources().getString(R.string.cancel));
                    }
                }
            });
        }
    }

    private void showLocationFailPopup() {
        View view = mainFourTabView;
        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        LocationFailPopup locationFailPopup = new LocationFailPopup();
                        locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
                                getResources().getString(R.string.location_fail_tips),
                                getResources().getString(R.string.know), null);
                    }
                }
            });
        }
    }

    private void locationPosition() {

        startService(new Intent(this, LocationService.class));
    }

    /**
     * 处理定位结果
     *
     * @param event
     */
    public final void onEventMainThread(LocationResultEvent event) {
        LogUtil.d(TAG, "processLocationResult()-event:" + event);
        if (isFinishingOrDestroyed()) {
            return;
        }
        String country = event.getCountry();
        String state = event.getState();
        String city = event.getCity();
        double latitude = event.getLatitude();
        double longitude = event.getLongitude();
        int result = event.getResult();

        if (result == 0) {
            String latitudeString = String.valueOf(latitude);
            String longitudeString = String.valueOf(longitude);
            initUploadLoaction();

            uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(mContext),
                    longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());

        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            //ToastUtil.toastCommonError(result);
            if (LocationCache.getTimeLag(mContext, userId)) {
                if (!LocationCache.getUploadFlag(mContext, userId)) {
                    showNoLocationPermissionPopup();
                }
            }
        } else {
            //ToastUtil.showToast(getResources().getString(R.string.location_error));
            if (LocationCache.getTimeLag(mContext, userId)) {
                if (!LocationCache.getUploadFlag(mContext, userId)) {
                    showLocationFailPopup();
                }
            }


        }

    }

    private class NoLocationPermissionPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            LocationServiceUtil.gotoLocServiceSettings(mContext);
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private class LocationFailPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

//    /**
//     *
//     */
//    private class RequestLocation extends ConfirmAndCancelPopup {
//        @Override
//        public void confirm() {
//            super.confirm();
//            locationPosition();
//            dismiss();
//
//        }
//
//        @Override
//        public void cancel() {
//            super.cancel();
//            dismiss();
//        }
//    }

    /**
     * 请求定位
     */
    private void toRequestLocation() {
        // showRequestLocationPopu();
        locationPosition();

    }

//    /**
//     * 如果没有位置信息则弹框提示允许获得位置信息
//     */
//    public void showRequestLocationPopu() {
//        // 判断是否是否有地位信息(有上传信息标志也行)
//        if (PhoneUtil.isCN(this) || JudgeLocationUtil.isLocation(UserCache.getCurrentUserId(this)) || LocationCache.getUploadFlag(mContext, userId)) {
//            return;
//        } else {
//            RequestLocation requestLocation = new RequestLocation();
//            String content = getString(R.string.allow_location_title_tips1) + getString(R.string.app_name) + getString(R.string.allow_location_title_tips2);
//            requestLocation.showPopup(this, content, getString(R.string.allow_location_content_tips), getString(R.string.allow_yes), getString(R.string.allow_no));
//        }
//    }

    private void initUploadLoaction() {
        if (uploadLocation == null) {
            uploadLocation = new UploadLocation() {
                @Override
                public void onUploadLoactionResult(int errorCode, String errorMessage) {
                    LogUtil.d(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                    if (errorCode == 0) {
                        LocationCache.saveUploadFlag(mContext, true, userId);
                    }
                }
            };
        }
    }

    @Override
    public void onCancelClick(View view) {
    }

    /**
     * orvibo 检查更新接口 version 1.9
     */
    private class OrviboCheckUpdateListener implements OrviboUpdateAgent.OrviboCheckUpdateListener {

        @Override
        public void onUpdateReturned(boolean isCanUpdate, HashMap<String, String> responseInfos) {
            LogUtil.d(TAG, "updateInfos=" + responseInfos);
            if (isCanUpdate) {
                FindNewVersion.setIsNewVersion(getApplicationContext(), true);
                if (responseInfos != null && responseInfos.size() > 0) {
                    updateInfos = responseInfos;
                    //  showDownloadDialog(responseInfos);
                    mOrviboUpdateAgent.showOrviboUpdateTipsDialog(MainActivity.this, updateInfos);
                    //  mDownloadAgent.showOrviboUpdateTipsDialog(MainActivity.this, updateInfos);
                }
            } else {
                FindNewVersion.setIsNewVersion(getApplicationContext(), false);
            }
        }
    }

    /**
     * 检查未绑定设备
     */
    private void checkSearchUnbindDevice() {
        MyLogger.kLog().d();
        if (isCheckedUnbindDevice) {
            LogUtil.w(TAG, "checkSearchUnbindDevice()-Has been check unbind devices.");
            return;
        }
        isCheckedUnbindDevice = true;

        if (mSearchDevice == null) {
            mSearchDevice = new SearchDevice(MainActivity.this);
        }
        OrviboThreadPool.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                currentMainUid = UserCache.getCurrentMainUid(mAppContext);
                userId = UserCache.getCurrentUserId(mAppContext);
                if (TextUtils.isEmpty(userId)) {
                    LogUtil.e(TAG, "run()-userId is null.");
                    return;
                }
                //没有主机->搜索主机
                if (TextUtils.isEmpty(currentMainUid)) {
                    //搜索主机，true搜索主机结束后再搜索wifi设备
                    mSearchDevice.searchHub(true);
                } else {
                    //只搜索wifi设备
                    mSearchDevice.searchWifiDevice();
                }
            }
        });
    }


}