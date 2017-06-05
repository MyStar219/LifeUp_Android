package com.orvibo.homemate.user;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.ModifyPassword;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.password.PasswordForgotActivity;
import com.orvibo.homemate.util.MD5;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/5/28.
 */
public class UserPasswordChangeActivity extends BaseActivity implements EditTextWithCompound.OnInputListener, View.OnClickListener, NavigationCocoBar.OnLeftClickListener, DialogFragmentOneButton.OnButtonClickListener {
    private NavigationCocoBar navigationBar;
    private EditTextWithCompound oldPwdEditText, newPwdEditText;
    private ImageView eyeOldPwdImageView, eyeNewPwdImageView;
    private TextView tip1ErrorTextView, tip2ErrorTextView;
    private Button finishButton, forgotButton;
    private String newPassword;

    private ModifyPassword modifyPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_change);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        oldPwdEditText = (EditTextWithCompound) findViewById(R.id.oldPwdEditText);
        oldPwdEditText.isNeedFilter(false);
        oldPwdEditText.setNeedRestrict(false);
//        oldPwdEditText.setPwdEyeEnable(true);
        oldPwdEditText.setOnInputListener(this);
//        oldPwdEditText.setMinLength(6);
        oldPwdEditText.setMaxLength(16);
        newPwdEditText = (EditTextWithCompound) findViewById(R.id.newPwdEditText);
        newPwdEditText.isNeedFilter(false);
        newPwdEditText.setNeedRestrict(false);
//        newPwdEditText.setPwdEyeEnable(true);
        newPwdEditText.setOnInputListener(this);
        newPwdEditText.setMinLength(6);
        newPwdEditText.setMaxLength(16);
        eyeOldPwdImageView = (ImageView) findViewById(R.id.eyeOldPwdImageView);
        eyeOldPwdImageView.setOnClickListener(this);
        eyeNewPwdImageView = (ImageView) findViewById(R.id.eyeNewPwdImageView);
        eyeNewPwdImageView.setOnClickListener(this);
        tip1ErrorTextView = (TextView) findViewById(R.id.tip1ErrorTextView);
        tip2ErrorTextView = (TextView) findViewById(R.id.tip2ErrorTextView);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        finishButton.setEnabled(false);
        forgotButton = (Button) findViewById(R.id.forgotButton);
        forgotButton.setOnClickListener(this);
        modifyPassword = new ModifyPassword(this) {
            @Override
            public void onModifyPasswordResult(int serial, int result) {
                super.onModifyPasswordResult(serial, result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    DialogFragmentOneButton dialogFragment = new DialogFragmentOneButton();
                    dialogFragment.setTitle(getString(R.string.user_password_change_success));
                    dialogFragment.setContent(getString(R.string.user_password_change_success_tip));
                    dialogFragment.setOnButtonClickListener(UserPasswordChangeActivity.this);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    dialogFragment.show(transaction, getClass().getName());
                } else if (result == ErrorCode.FAIL) {
                    ToastUtil.showToast(getString(R.string.user_password_change_fail));
                } else if (result == ErrorCode.CHANGE_PASSWORD_ERROR) {
                    ToastUtil.showToast(getString(R.string.user_password_change_error));
                    tip1ErrorTextView.setVisibility(View.VISIBLE);
                    oldPwdEditText.setUnlawful();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ModifyPsd_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eyeOldPwdImageView: {
                //默认是明文，眼睛是关闭状态
                int selectionStart = oldPwdEditText.getSelectionStart();
                if (oldPwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    oldPwdEditText.setTransformationMethod(null);
                    eyeOldPwdImageView.setImageResource(R.drawable.password_hide);
                } else {

                    oldPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeOldPwdImageView.setImageResource(R.drawable.password_show);
                }
                if (selectionStart > 0) {
                    try {
                        oldPwdEditText.setSelection(selectionStart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.eyeNewPwdImageView: {
                //默认是明文，眼睛是关闭状态
                int selectionStart = newPwdEditText.getSelectionStart();
                if (newPwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    newPwdEditText.setTransformationMethod(null);
                    eyeNewPwdImageView.setImageResource(R.drawable.password_hide);
                } else {

                    newPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeNewPwdImageView.setImageResource(R.drawable.password_show);
                }
                if (selectionStart > 0) {
                    try {
                        newPwdEditText.setSelection(selectionStart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.finishButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ModifyPsd_Finish), null);
                String oldPwd = oldPwdEditText.getText().toString();
                newPassword = newPwdEditText.getText().toString();
                showDialog(null, getString(R.string.user_password_changing));
                modifyPassword.startModifyPassword(UserCache.getCurrentMainUid(this), UserCache.getCurrentUserName(this), MD5.encryptMD5(oldPwd), MD5.encryptMD5(newPassword));
                break;
            }
            case R.id.forgotButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ModifyPsd_ForgotPsd), null);
                Intent intent = new Intent(this, PasswordForgotActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onRightful() {
        if (oldPwdEditText.isRightful() && newPwdEditText.isRightful()) {
            finishButton.setEnabled(true);
        } else {
            finishButton.setEnabled(false);
        }
        tip1ErrorTextView.setVisibility(View.GONE);
        if (newPwdEditText.isRightful()) {
            tip2ErrorTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUnlawful() {
        finishButton.setEnabled(false);
        tip1ErrorTextView.setVisibility(View.GONE);
        if (!newPwdEditText.isRightful()) {
            tip2ErrorTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClearText() {
        finishButton.setEnabled(false);
        tip1ErrorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onButtonClick(View view) {
        // 退出的话需要发送退出登录请求
        new Logout(mContext).logout(userName, currentMainUid);
        UserManage.getInstance(mContext.getApplicationContext()).exitAccount(UserCache.getCurrentUserId(mContext));
//      EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.UserPasswordChangeActivity);
//        intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
        intent.putExtra(Constant.ACCOUNT, UserCache.getCurrentUserName(this));
        intent.putExtra(Constant.PWD, newPassword);
        startActivityForResult(intent, 0);
    }

}
