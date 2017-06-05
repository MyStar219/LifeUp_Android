package com.orvibo.homemate.common;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.BaseDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;
import com.orvibo.homemate.view.dialog.ToastDialog;

public class BaseFragment extends Fragment implements OnClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener {
    public static final String TAG = BaseFragment.class.getSimpleName();
    protected Activity context;
    protected Context mAppContext;
    protected volatile String mainUid;
    private ProgressDialogFragment progressDialogFragment;
    private final int SHOW_DIALOG = 1;
    private volatile boolean isResumed = false;
    protected Device device;
    public String userName;
    protected String userId;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_DIALOG:
//                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    if (isResumed) {
                        try {
                            progressDialogFragment.show(getFragmentManager(), getClass().getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        mAppContext = ViHomeProApp.getContext();
        mainUid = UserCache.getCurrentMainUid(context);
        userName = UserCache.getCurrentUserName(mAppContext);
        userId = UserCache.getCurrentUserId(mAppContext);
        progressDialogFragment = new ProgressDialogFragment();

    }

    public void setData(Bundle bundle) {
        if (bundle != null) {
            device = (Device) bundle.getSerializable(Constant.DEVICE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    protected boolean isFragmentVisible() {
        return isResumed && isVisible();
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
        BaseDao.initDB(mAppContext);
        mainUid = UserCache.getCurrentMainUid(context);
        userName = UserCache.getCurrentUserName(mAppContext);
    }

    public void showDialogNow(ProgressDialogFragment.OnCancelClickListener onCancelClickListener) {
        dismissDialog();
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setOnCancelClickListener(onCancelClickListener);
        progressDialogFragment.setContent(getString(R.string.loading));
        if (handler.hasMessages(SHOW_DIALOG)) {
            handler.removeMessages(SHOW_DIALOG);
        }
        handler.sendEmptyMessage(SHOW_DIALOG);
    }

    public void showDialog(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        dismissDialog();
        if (handler.hasMessages(SHOW_DIALOG)) {
            handler.removeMessages(SHOW_DIALOG);
        }
        handler.sendEmptyMessageDelayed(SHOW_DIALOG, 1000);
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setOnCancelClickListener(onCancelClickListener);
        progressDialogFragment.setContent(content);
    }

    public void showDialog() {
        showDialog(null, getString(R.string.loading));
    }

    //TODO 有什么用
    public void showEmptyDialog() {
        dismissDialog();
        if (handler.hasMessages(SHOW_DIALOG)) {
            handler.removeMessages(SHOW_DIALOG);
        }
        handler.sendEmptyMessageDelayed(SHOW_DIALOG, 1000);
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setOnCancelClickListener(null);
//        progressDialogFragment.setContentGone();
    }

    public void dismissDialog() {
        if (handler.hasMessages(SHOW_DIALOG)) {
            handler.removeMessages(SHOW_DIALOG);
        }
        if (progressDialogFragment != null && !progressDialogFragment.isHidden()) {
            try {
                progressDialogFragment.dismiss();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void onRefresh() {

    }

    /**
     * 如果第一次add fragment，不会调用此接口。
     */
    public void onVisible() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToastDialog.cancel();
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
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
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.CocoPersonalFragment);
        startActivity(intent);
    }

    /**
     * 刷新进度
     */
    private SwipeRefreshLayout mRefreshLayout;

    /**
     * 设置进度颜色
     */
    protected void initProgress(SwipeRefreshLayout refreshLayout) {
//        if (refreshLayout != null) {
        refreshLayout.setColorScheme(R.color.process_color1,
                R.color.process_color2,
                R.color.process_color3,
                R.color.process_color4);
//        }
        mRefreshLayout = refreshLayout;
    }

    /**
     * 显示进度
     */
    protected void showProgress() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(true);
            mRefreshLayout.setColorScheme(R.color.process_color1,
                    R.color.process_color2,
                    R.color.process_color3,
                    R.color.process_color4);
        } else {
            LogUtil.e(TAG, "showProgress()-progress layout is null.");
        }
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
