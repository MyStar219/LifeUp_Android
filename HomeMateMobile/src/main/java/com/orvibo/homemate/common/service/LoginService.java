//package com.orvibo.homemate.common.service;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//
//import com.orvibo.homemate.data.BottomTabType;
//import com.orvibo.homemate.data.ClientType;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.IntentKey;
//import com.orvibo.homemate.event.login.LaunchEvent;
//import com.orvibo.homemate.event.login.LoginServiceEvent;
//import com.orvibo.homemate.util.LogUtil;
//
//import de.greenrobot.event.EventBus;
//
///**
// * 登录service
// * Created by huangqiyao on 2015/5/26.
// */
//public class LoginService extends Service {
//    public static final int ACTION_GET_ENTER_TYPE = 1;
//    private static final String TAG = LoginService.class.getSimpleName();
//    private Context mContext;
//    private OnLoginListener mOnLoginListener;
//    private Login2 mLogin;
//
//
//    /**
//     * 登录结果
//     */
//    private volatile int loginResult = Constant.INVALID_NUM;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        LogUtil.d(TAG, "onCreate()");
//        mContext = this;
//        initLogin();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        LogUtil.d(TAG, "onStartCommand()-intent:" + intent);
//        if (intent != null) {
//            final boolean login = intent.getBooleanExtra(IntentKey.LOGIN, false);
//            LogUtil.d(TAG, "onStartCommand()-login:" + login);
//            if (login) {
//                autoLogin();
//            }
//        }
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new LoginBinder();
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        cancelLogin();
//        return super.onUnbind(intent);
//    }
//
//    public void onEventMainThread(LoginServiceEvent event) {
//        if (event.getState() == ACTION_GET_ENTER_TYPE) {
//            int bottomTabType = BottomTabType.TWO_BOTTOM_TAB;
//            if (mLogin != null) {
//                bottomTabType = mLogin.getBottomTabType();
//            }
//            EventBus.getDefault().post(new LaunchEvent(bottomTabType));
//        }
//    }
//
//    public class LoginBinder extends Binder {
//        /**
//         * 获取当前Service的实例
//         *
//         * @return
//         */
//        public LoginService getService() {
//            return LoginService.this;
//        }
//    }
//
//    private void initLogin() {
//        mLogin = new Login2(mContext, ClientType.PHONE) {
//
//            @Override
//            public void onLoginResult(int result) {
//                LogUtil.d(TAG, "onLoginResult()-result:" + result);
//                loginResult = result;
//                callback(result);
//            }
//        };
//    }
//
//    /**
//     * @return 0登录成功，-1未登录结束
//     */
//    public int getLoginResult() {
//        return loginResult;
//    }
//
//    /**
//     * @param userName 用户名
//     * @param password 明文密码
//     */
//    public void login(String userName, String password) {
//        mLogin.login(userName, password);
//    }
//
//    public void autoLogin() {
//        loginResult = Constant.INVALID_NUM;
//        mLogin.autoLogin(ClientType.PHONE);
//    }
//
//    public void cancelLogin() {
//        mLogin.cancelLogin();
//    }
//
//    private void callback(int result) {
//        if (mOnLoginListener != null) {
//            mOnLoginListener.onLoginResulg(result);
//        }
//    }
//
//    public void setOnLoginListener(OnLoginListener onLoginListener) {
//        mOnLoginListener = onLoginListener;
//    }
//
//    public interface OnLoginListener {
//        void onLoginResulg(int result);
//    }
//
//    @Override
//    public void onDestroy() {
//        EventBus.getDefault().unregister(this);
//        super.onDestroy();
//    }
//}
