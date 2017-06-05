package com.orvibo.homemate.device.smartlock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.gesturelock.GestureContentView;
import com.orvibo.homemate.view.gesturelock.GestureDrawline;

/**
 * 手势密码界面
 * Created by snown on 2016/03/29
 */
public class GestureActivity extends BaseActivity implements GestureDrawline.GestureCallBack {
    private TextView tipInfo, gestureManage;
    private int inputCount = 5;
    private GestureContentView gestureContentView;
    private AuthUnlockData authUnlockData;
    private Device device;
    private boolean hasGesture;//是否设置过手势密码
    private NavigationGreenBar navigationGreenBar;
    private boolean isChangePass = false;//是否是从修改密码按钮跳转过来
    private FrameLayout mGestureContainer;
    private boolean mIsFirstInput = true;
    private String mFirstPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_lock);
        isChangePass = getIntent().getBooleanExtra("isChangePass", false);
        initView();
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //点击返回按钮时回到门锁记录界面
        if (!isChangePass)
            ActivityJumpUtil.jumpActFinish(GestureActivity.this, LockRecordActivity.class);
        super.onBackPressed();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tipInfo = getViewById(R.id.tipInfo);
        gestureManage = getViewById(R.id.gestureManage);
        navigationGreenBar = getViewById(R.id.navigationGreenBar);
        authUnlockData = (AuthUnlockData) getIntent().getSerializableExtra(IntentKey.AUTH_UNLOCK_DATA);
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        hasGesture = SmartLockCache.hasGesture();
        mGestureContainer = (FrameLayout) findViewById(R.id.gesture_container);
        gestureContentView = new GestureContentView(mContext, hasGesture, SmartLockCache.getPwd(), this);
        gestureContentView.setParentView(mGestureContainer);
        if (!hasGesture) {
            tipInfo.setText(R.string.lock_gesture_tip);
            navigationGreenBar.setText(getString(R.string.lock_gesture_set));
            gestureManage.setVisibility(View.GONE);
        } else if (isChangePass) {
            tipInfo.setText(R.string.lock_gesture_change_tip1);
            navigationGreenBar.setText(getString(R.string.lock_gesture_change));
            gestureManage.setVisibility(View.GONE);
        } else {
            navigationGreenBar.setText(getString(R.string.lock_gesture_input));
        }

        gestureManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestureActivity.this, GestureManageActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                intent.putExtra(IntentKey.AUTH_UNLOCK_DATA, authUnlockData);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onGestureCodeInput(String inputCode) {
        if (!isInputPassValidate(inputCode)) {
            tipInfo.setTextColor(getResources().getColor(R.color.red));
            tipInfo.setText(R.string.lock_gesture_tip3);
            gestureContentView.clearDrawlineState(0L);
            return;
        }
        if (mIsFirstInput) {
            tipInfo.setTextColor(getResources().getColor(R.color.black));
            tipInfo.setText(R.string.lock_gesture_tip1);
            mFirstPassword = inputCode;
            gestureContentView.clearDrawlineState(0L);
        } else {
            if (inputCode.equals(mFirstPassword)) {
                //将密码设置在本地
                SmartLockCache.setPwd(inputCode);
                SmartLockCache.setGestureTime(System.currentTimeMillis());
                ToastUtil.showToast(R.string.device_setting_success);
                ActivityTool.lockStartJump(GestureActivity.this, authUnlockData, device);
            } else {
                tipInfo.setTextColor(getResources().getColor(R.color.red));
                tipInfo.setText(R.string.lock_gesture_tip2);
                // 保持绘制的线，1.5秒后清除
                gestureContentView.clearDrawlineState(1300L);
            }
        }
        mIsFirstInput = false;
    }

    @Override
    public void checkedSuccess() {
        if (isChangePass) {
            tipInfo.setTextColor(getResources().getColor(R.color.black));
            tipInfo.setText(R.string.lock_gesture_change_tip2);
            gestureContentView.clearDrawlineState(0L);
            gestureContentView = new GestureContentView(mContext, false, SmartLockCache.getPwd(), this);
            gestureContentView.setParentView(mGestureContainer);
        } else {
            SmartLockCache.setLockPassErrorCount(0);
            SmartLockCache.setGestureTime(System.currentTimeMillis());
            ActivityTool.lockStartJump(GestureActivity.this, authUnlockData, device);
        }
    }

    @Override
    public void checkedFail() {
        gestureContentView.clearDrawlineState(0L);
        tipInfo.setTextColor(getResources().getColor(R.color.red));
        if (hasGesture) {
            int errorCount = SmartLockCache.getLockPassErrorCount();
            errorCount++;
            SmartLockCache.setLockPassErrorCount(errorCount);
            if (errorCount >= 5) {
                SmartLockCache.clearPwd();
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.warm_tips));
                dialogFragmentOneButton.setContent(getString(R.string.dialog_content_gesture_invalid));
                dialogFragmentOneButton.setButtonText(getString(R.string.login_again));
                dialogFragmentOneButton.setCancelable(false);
                dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(View view) {
                        ActivityTool.toLoginActivity(mContext, userName, Constant.MAIN_ACTIVITY, Constant.INVALID_NUM);
                        finish();
                    }
                });
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else
                tipInfo.setText(String.format(getString(R.string.lock_gesture_tip4), inputCount - errorCount));
        } else {
            tipInfo.setText(R.string.lock_gesture_tip2);
        }
    }

    private boolean isInputPassValidate(String inputPassword) {
        return !(TextUtils.isEmpty(inputPassword) || inputPassword.length() < 4);
    }
}

