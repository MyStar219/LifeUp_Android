package com.orvibo.homemate.common;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.dao.BaseDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoadDataType;
import com.orvibo.homemate.data.LoginIntent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tencent.stat.StatService;

import de.greenrobot.event.EventBus;

/**
 * activity基类，实现通用简单功能
 *
 * @author huangqiyao
 * @date 2015/1/4
 */
public class BaseActivity extends FragmentActivity implements OnClickListener,
        DialogFragmentTwoButton.OnTwoButtonClickListener, InfoPushManager.OnLoadMessageListener {
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected Context mContext;
    public Context mAppContext;
    private ProgressDialogFragment progressDialogFragment;
    public String userName;
    public String currentMainUid;
    protected String userId;
    private final int SHOWDIALOG = 1;
    private FragmentManager fragmentManager;
    protected boolean isResumed = false;
    private boolean isDestroyed = false;
    protected String language;
    /**
     * true海外版本，不显示授权登录界面
     */
    protected boolean isOverseasVersion;
    private SystemBarTintManager mTintManager;
    //子类布局的容器
    private FrameLayout parentFrameLayout;

    public <K extends View> K getViewById(int id) {
        return (K) getWindow().findViewById(id);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOWDIALOG:
                    if (isResumed) {
                        progressDialogFragment.show(fragmentManager, getClass().getName());
                    }
                default:
                    break;
            }
        }
    };
    private BasePresenter mBasePresenter;

    protected String mIntentSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isResumed = true;
        isDestroyed = false;
        mContext = this;
        mAppContext = ViHomeProApp.getContext();
        userName = UserCache.getCurrentUserName(mAppContext);
        currentMainUid = UserCache.getCurrentMainUid(mAppContext);
        userId = UserCache.getCurrentUserId(mAppContext);
        fragmentManager = getFragmentManager();
        InfoPushManager.getInstance(mAppContext).registerLoadMessageListener(this);

        try {
            EventBus.getDefault().registerSticky(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        language = getResources().getConfiguration().locale.getLanguage();
        isOverseasVersion();
        mBasePresenter = new BasePresenter(this);
        LogUtil.d(getClass().getSimpleName(), "onCreate()");
    }

    private void isOverseasVersion() {

        if (!PhoneUtil.isCN(this)) {
            isOverseasVersion = true;
        }
    }


    @Override
    protected void onResume() {
        isResumed = true;
        StatService.onResume(mAppContext);
        super.onResume();
        LogUtil.d(getClass().getSimpleName(), "onResume()");
        BaseDao.initDB(mAppContext);
        userName = UserCache.getCurrentUserName(mContext);
        currentMainUid = UserCache.getCurrentMainUid(mContext);
        userId = UserCache.getCurrentUserId(mAppContext);
        if (!ViHomeApplication.getInstance().isActive()) {
            //app 从后台唤醒，进入前台
            ViHomeApplication.getInstance().setIsActive(true);
            LogUtil.d(TAG, "onResume()-HomeMate go to foreground.");
            //如果app在后台超过30min，切换到前台时进行读表
            if (AppTool.isLoginWhenAppToForeground()) {
                if (UserManage.getInstance(mAppContext).isLogined()) {
                    LogUtil.w(TAG, "onResume()-Start to reload all data.");
                    Intent intent = new Intent(this, LoadDataActivity.class);
                    intent.putExtra(IntentKey.UID, currentMainUid);
                    intent.putExtra(IntentKey.GOTOMAIN, false);
                    intent.putExtra(IntentKey.LOAD_DATA_TYPE, LoadDataType.LOAD_ALL);
                    startActivity(intent);
                } else {
                    //后台到前台，检查主机固件升级
                    mBasePresenter.onCheckHubUpgrade();
                }
            } else {
                //后台到前台，检查主机固件升级
                mBasePresenter.onCheckHubUpgrade();
            }
//            if (AppTool.isLoginWhenAppToForeground() && UserManage.getInstance(mAppContext).isLogined()) {
//                LogUtil.w(TAG, "onResume()-Start to reload all data.");
//                Intent intent = new Intent(this, LoadDataActivity.class);
//                intent.putExtra(IntentKey.UID, currentMainUid);
//                intent.putExtra(IntentKey.GOTOMAIN, false);
//                intent.putExtra(IntentKey.LOAD_DATA_TYPE, LoadDataType.LOAD_ALL);
//                startActivity(intent);
//            }
            TimeCache.clear(mAppContext);
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        StatService.onPause(mAppContext);
        super.onPause();
        LogUtil.d(getClass().getSimpleName(), "onPause()");
    }

    /**
     * 判断acitvity是否可见
     *
     * @return true当前activity可见
     */
    public boolean isVisible() {
        return isResumed;
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(getClass().getSimpleName(), "onStop()");
        if (!AppTool.isAppOnForeground(mAppContext)) {
            SmartLockCache.setGestureTime(0);
            //app 进入后台 　　
            ViHomeApplication.getInstance().setIsActive(false); //记录当前已经进入后台
            LogUtil.d(TAG, "onStop()-HomeMate go to background.");
            TimeCache.saveStartBackgroundTime(mAppContext);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        LogUtil.w(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtil.w(TAG, "onTrimMemory()-level:" + level);
        super.onTrimMemory(level);
    }

    @Override
    public Resources getResources() {
        try {
            Resources res = super.getResources();
            Configuration config = new Configuration();
            config.setToDefaults();//设置字体大小不随系统字体大小改变
            res.updateConfiguration(config, res.getDisplayMetrics());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return super.getResources();
        }
    }

    public boolean getIsResumed() {
        return isResumed;
    }

    protected void setTitle(String name) {
        TextView back_titlebar_tv = (TextView) findViewById(R.id.back_titlebar_tv);
        if (back_titlebar_tv != null) {
            if (!StringUtil.isEmpty(name)) {
                back_titlebar_tv.setText(name);
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 标题栏返回事件
     *
     * @param v
     */
    public void leftTitleClick(View v) {
        this.finish();
    }


    public void rightTitleClick(View v) {

    }

    public void leftToRightImgOnClick(View v) {
    }

    protected ProgressDialogFragment getDialog() {
        return progressDialogFragment;
    }

    /**
     * 关闭进度对话款
     */
    public void dismissDialog() {
        // LogUtil.d(TAG, "dismissDialog()");
        if (handler.hasMessages(SHOWDIALOG)) {
            handler.removeMessages(SHOWDIALOG);
        }
        if (progressDialogFragment != null) {
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.hide(progressDialogFragment);
//            progressDialogFragment.dismiss();
            //fragmentManager.beginTransaction().remove(progressDialogFragment);
            try {
                progressDialogFragment.dismissAllowingStateLoss();
//                progressDialogFragment.dismiss();
            } catch (Exception e) {
//                e.printStackTrace();
                LogUtil.w(TAG, "dismissDialog()-" + e.getMessage());
            }
        }
    }

    protected boolean isDialogShowing() {
        return progressDialogFragment != null && progressDialogFragment.isVisible();
    }

    /**
     * 延迟1S显示进度对话框
     */
    public void showDialog() {
        showDialog(null, getString(R.string.loading));
    }

    /**
     * 不延迟1S显示进度对话框
     */
    public void showDialogNow() {
        showDialogNow(null, getString(R.string.loading));
    }

    public void showDialogNow(ProgressDialogFragment.OnCancelClickListener onCancelClickListener) {
        showDialogNow(onCancelClickListener, getString(R.string.loading));
    }

    public void showDialog(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        dismissDialog();
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setOnCancelClickListener(onCancelClickListener);
        progressDialogFragment.setContent(content);
        if (handler.hasMessages(SHOWDIALOG)) {
            handler.removeMessages(SHOWDIALOG);
        }
        handler.sendEmptyMessageDelayed(SHOWDIALOG, 1000);
    }

    /**
     * 返回键点击无法消失dialog
     *
     * @param content
     */
    public void showDialogNoBack(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        dismissDialog();
        progressDialogFragment = ProgressDialogFragment.newInstance(false);
        progressDialogFragment.setOnCancelClickListener(null);
        progressDialogFragment.setContent(content);
        if (handler.hasMessages(SHOWDIALOG)) {
            handler.removeMessages(SHOWDIALOG);
        }
        handler.sendEmptyMessageDelayed(SHOWDIALOG, 1000);
    }

    public void showDialogNow(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        if (isResumed) {
            dismissDialog();
            progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.setOnCancelClickListener(onCancelClickListener);
            progressDialogFragment.setContent(content);
            progressDialogFragment.show(fragmentManager, getClass().getName());
        }
    }

    public void showDialogNow(String content) {
        showDialogNow(null, content);
    }

    /**
     * 取消进度对话框
     */
    protected void onCancelDialog() {

    }

    @Override
    protected void onDestroy() {
        LogUtil.d(getClass().getSimpleName(), "onDestroy()");
        if (handler.hasMessages(SHOWDIALOG)) {
            handler.removeMessages(SHOWDIALOG);
        }
        dismissDialog();

        InfoPushManager.getInstance(mAppContext).unregisterLoadMessageListener(this);
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
        isDestroyed = true;
    }

    /**
     * @return true activity正在被finish或者已经被销毁
     */
    public boolean isFinishingOrDestroyed() {
        return isFinishing() || isDestroyed;
    }

    /**
     * 获取当前是哪个activity
     *
     * @return
     */
    protected String getActivityTag() {
        return null;
    }

    public void showLoginDialog() {
        DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
        String title = getString(R.string.login_now_title);
        dialogFragment.setTitle(title);
        dialogFragment.setLeftButtonText(getString(R.string.cancel));
        dialogFragment.setRightButtonText(getString(R.string.login));
        dialogFragment.setOnTwoButtonClickListener(this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }

    @Override
    public void onLeftButtonClick(View view) {
    }

    @Override
    public void onRightButtonClick(View view) {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.ViHome);
        intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.ALL);
        startActivityForResult(intent, 0);
    }

    /**
     * 接口通知界面刷新数据
     *
     * @param event
     */
    public final void onEventMainThread(ViewEvent event) {
        LogUtil.d(TAG, "onEventMainThread()-event:" + event);
        onRefresh(event);
    }

    /**
     * @param eventUid ViewEvent携带的uid，如果为null也要刷新。
     * @param uid      当前界面使用的uid
     * @return true说明uid一样，可以刷新界面。如果
     */
    protected boolean isNeedRefreshView(String eventUid, String uid) {
        boolean refrehs = false;
        if (eventUid == null || eventUid.equals(uid)) {
            refrehs = true;
        }
        return refrehs;
    }

    /**
     * 当读取完网关数据后会回调此接口通知刷新界面。
     *
     * @param event
     */
    protected void onRefresh(ViewEvent event) {

    }

    /**
     * 注册EventBus监听事件
     *
     * @param o
     */
    protected void registerEvent(Object o) {
        try {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注销EventBus
     *
     * @param o
     */
    protected void unregisterEvent(Object o) {
        try {
            EventBus.getDefault().unregister(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadMessage(String uid, int result) {
        if (result == ErrorCode.SUCCESS) {
            ViewEvent viewEvent = new ViewEvent(LoadDataType.LOAD_TABLE_DATA, uid, result);
            onRefresh(viewEvent);
        }
    }

    /**
     * 刷新进度
     */
    private SwipeRefreshLayout mRefreshLayout;


    /**
     * 显示进度
     */
    protected void showProgress() {
        if (mRefreshLayout == null) {
            mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_progress);
            mRefreshLayout.setVisibility(View.VISIBLE);
            mRefreshLayout.setColorScheme(R.color.process_color1,
                    R.color.process_color2,
                    R.color.process_color3,
                    R.color.process_color4);
        }
        mRefreshLayout.setRefreshing(true);
    }

    /**
     * 停止进度
     */
    protected void stopProgress() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 进度正在执行
     *
     * @return
     */
    protected boolean isProgressGoing() {
        return mRefreshLayout != null && mRefreshLayout.isRefreshing();
    }


}
