package com.orvibo.homemate.smartscene.manager;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.TimingCountdownTabView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

/**
 * @author Smagret
 * @date 2015/12/08
 */
public class SecurityManagerActivity extends BaseActivity implements TimingCountdownTabView.OnTabSelectedListener
        , SecurityManagerFragment.OnLinkageManagerListener {
    private static final String TAG = SecurityManagerActivity.class.getSimpleName();

    private ArmingManagerFragment mArmingManagerFragment;
    private DisarmingManagerFragment mDisarmingManagerFragment;
    private TimingCountdownTabView topTimingCountdownTabView;
    private RelativeLayout bar_rl;
    /**
     * 返回提示是否保存界面
     */
    private ConfirmAndCancelPopup mConfirmSavePopup;
    private Security security;
    private int currentPosition = 0;
    private int tabSelectedPosition = 0;
    private int latestType;
    /**
     * 点击tab，切换布撤防时，要判断当前布撤防是否被修改，如果修改了要先保存才能切换
     */
    private boolean tabClickChange = false;

    /**
     * 点击返回时，要判断当前布撤防是否被修改，如果修改了要先保存才能返回
     */
    private boolean backClickChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_manager_container);
        bar_rl = (RelativeLayout) findViewById(R.id.bar_rl);
        Intent intent = getIntent();
        security = (Security) intent.getSerializableExtra(IntentKey.SECURITY);
        if (security == null) {
            finish();
        }
        latestType = intent.getIntExtra("latestType", 0);
        initTopTab();
    }

    /**
     */
    private void initTopTab() {
        topTimingCountdownTabView = (TimingCountdownTabView) findViewById(R.id.topTimingCountdownTabView);
        topTimingCountdownTabView.setOnTabSelectedListener(this);
        if (latestType == TimingCountdownTabView.TIMING_POSITION) {
            topTimingCountdownTabView.setSelectedPosition(TimingCountdownTabView.TIMING_POSITION);
        } else {
            topTimingCountdownTabView.setSelectedPosition(TimingCountdownTabView.COUNTDOWN_POSITION);
        }
        topTimingCountdownTabView.initName(getResources().getString(R.string.security_arm), getResources().getString(R.string.security_disarm));
        topTimingCountdownTabView.setArmView(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bar_rl.setBackgroundColor(getResources().getColor(R.color.green));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment != null) {
            if (mArmingManagerFragment == null && fragment instanceof ArmingManagerFragment) {
                mArmingManagerFragment = (ArmingManagerFragment) fragment;
            } else if (mDisarmingManagerFragment == null && fragment instanceof DisarmingManagerFragment) {
                mDisarmingManagerFragment = (DisarmingManagerFragment) fragment;
            }
        }
        super.onAttachFragment(fragment);
    }

    @Override
    public void onTabSelected(int position) {
        LogUtil.d(TAG, "onTabSelected()-position:" + position);
        if (isFinishingOrDestroyed()) {
            return;
        }

        if (currentPosition != position && isChanged()) {
            tabClickChange = true;
            showConfirmSavePopup();
        } else {
            tabChange(position);
        }
        tabSelectedPosition = position;
    }


    private void tabChange(int position) {
        if (topTimingCountdownTabView != null) {
            topTimingCountdownTabView.setTabSelectedView(position);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // hideAllFragment(transaction);
        switch (position) {
            case TimingCountdownTabView.TIMING_POSITION:
                hideFragment(transaction, mDisarmingManagerFragment);
                if (mArmingManagerFragment == null) {
                    mArmingManagerFragment = new ArmingManagerFragment();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(IntentKey.SECURITY, security);
                    mBundle.putInt(IntentKey.ARM_TYPE, ArmType.ARMING);
                    //通过setArguments给fragment传递数据
                    mArmingManagerFragment.setArguments(mBundle);
                    mArmingManagerFragment.registerFinishListener(this);
                }
                currentPosition = TimingCountdownTabView.TIMING_POSITION;
                showFragment(transaction, mArmingManagerFragment);
                break;
            case TimingCountdownTabView.COUNTDOWN_POSITION:
                hideFragment(transaction, mArmingManagerFragment);
                if (mDisarmingManagerFragment == null) {
                    mDisarmingManagerFragment = new DisarmingManagerFragment();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(IntentKey.SECURITY, security);
                    mBundle.putInt(IntentKey.ARM_TYPE, ArmType.DISARMING);
                    //通过setArguments给fragment传递数据
                    mDisarmingManagerFragment.setArguments(mBundle);
                    mDisarmingManagerFragment.registerFinishListener(this);
                }
                currentPosition = TimingCountdownTabView.COUNTDOWN_POSITION;
                showFragment(transaction, mDisarmingManagerFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragment(FragmentTransaction transaction, BaseFragment fragment) {
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private synchronized void showFragment(FragmentTransaction ft, BaseFragment baseFragment) {
        boolean isAdded = baseFragment.isAdded();
        LogUtil.d(TAG, "showFragment()-baseFragment:" + baseFragment + ",isAdded:" + isAdded);
        if (!isAdded) {
            ft.add(R.id.container, baseFragment);
        } else {
            ft.show(baseFragment);
        }
        baseFragment.onVisible();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (mArmingManagerFragment != null) {
            mArmingManagerFragment.onRefresh();
        }
        if (mDisarmingManagerFragment != null) {
            mDisarmingManagerFragment.onRefresh();
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.w(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void leftTitleClick(View v) {
        if (isChanged()) {
            backClickChange = true;
            showConfirmSavePopup();
        } else {
            super.leftTitleClick(v);
        }
    }

    @Override
    public void rightTitleClick(View view) {
        save();
    }

    /**
     * 提示是否保存修改界面
     */
    private void showConfirmSavePopup() {
        if (mConfirmSavePopup == null) {
            mConfirmSavePopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    save();
                }

                @Override
                public void cancel() {
                    super.cancel();
                    reset();
                    if (!tabClickChange) {
                        finish();
                    } else {
                        tabChange(tabSelectedPosition);
                        tabClickChange = false;
                    }
                }
            };
        }
        mConfirmSavePopup.showPopup(mContext, R.string.save_content, R.string.save, R.string.unsave);
    }

    /**
     * @return true已经修改了联动
     */
    private boolean isChanged() {
        boolean isArmingChanged = false;
        boolean isDisarmingChanged = false;
        if (mArmingManagerFragment != null) {
            isArmingChanged = mArmingManagerFragment.isArmChanged();
        }
        if (mDisarmingManagerFragment != null) {
            isDisarmingChanged = mDisarmingManagerFragment.isDisArmChanged();
        }

        return isArmingChanged || isDisarmingChanged;
    }

    @Override
    public void onBackPressed() {
        if (!isDialogShowing()
                && isChanged()
                && (mConfirmSavePopup == null || (mConfirmSavePopup != null && !mConfirmSavePopup.isShowing()))) {
            backClickChange = true;
            showConfirmSavePopup();
        } else {
            super.onBackPressed();
        }
    }

    private void save() {
        if (currentPosition == TimingCountdownTabView.TIMING_POSITION) {
            if (mArmingManagerFragment != null) {
                mArmingManagerFragment.save();
            }
        } else if (currentPosition == TimingCountdownTabView.COUNTDOWN_POSITION) {
            if (mDisarmingManagerFragment != null) {
                mDisarmingManagerFragment.save();
            }
        }
    }

    private void reset() {
        if (currentPosition == TimingCountdownTabView.TIMING_POSITION) {
            if (mArmingManagerFragment != null) {
                mArmingManagerFragment.reset();
            }
        } else if (currentPosition == TimingCountdownTabView.COUNTDOWN_POSITION) {
            if (mDisarmingManagerFragment != null) {
                mDisarmingManagerFragment.reset();
            }
        }
    }

    @Override
    public void onLinkageFinish() {
        //finish();

        if (!tabClickChange && backClickChange) {
            finish();
        } else {
            tabChange(tabSelectedPosition);
            tabClickChange = false;
        }
    }
}