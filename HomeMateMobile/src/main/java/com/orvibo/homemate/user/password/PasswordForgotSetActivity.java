package com.orvibo.homemate.user.password;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.ResetPassword;
import com.orvibo.homemate.sharedPreferences.LockCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.MD5;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/6/12.
 */
public class PasswordForgotSetActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener, DialogFragmentOneButton.OnButtonClickListener, EditTextWithCompound.OnInputListener {
    private NavigationCocoBar navigationBar;
    private EditTextWithCompound pwdEditText;
    private ImageView eyeImageView;
    private TextView errorTextView;
    private Button finishButton;

    private String userContact, pwd;
    private ResetPassword resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        userContact = intent.getStringExtra(Constant.USER_CONTACT);
        if (!TextUtils.isEmpty(userContact)) {
            setContentView(R.layout.activity_register_password_set);
            init();
        } else {
            onBackPressed();
        }
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        pwdEditText = (EditTextWithCompound) findViewById(R.id.passwordEditText);
//        pwdEditText.setPwdEyeEnable(true);
        pwdEditText.setOnInputListener(this);
        pwdEditText.setMinLength(6);
        pwdEditText.setMaxLength(16);
        pwdEditText.setNeedRestrict(false);
        eyeImageView = (ImageView) findViewById(R.id.eyeImageView);
        eyeImageView.setOnClickListener(this);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        resetPassword = new ResetPassword(mAppContext) {
            @Override
            public void onResetPasswordResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.user_password_set_success);
                    UserCache.savePassword(mAppContext, userName, pwd);//保存新密码
                    LockCache.saveUnLockTimes(mContext, userName, Constant.MAX_UNLOCKTIME);//重置门锁开锁次数
                    Intent intent = new Intent(PasswordForgotSetActivity.this, LoginActivity.class);
                    intent.putExtra(Constant.LOGIN_ENTRY, Constant.PasswordForgotSetActivity);
//                    intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
                    intent.putExtra(Constant.ACCOUNT, userContact);
                    intent.putExtra(Constant.PWD, pwd);
                    startActivityForResult(intent, 0);
                } else if (result == ErrorCode.FAIL) {
                    ToastUtil.showToast(R.string.user_password_set_fail);
                } else if (result == ErrorCode.RESET_PASSWORD_ERROR) {
                    ToastUtil.showToast(R.string.user_password_set_error);
                } else if (result == ErrorCode.TIMEOUT_MP) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
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
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_NewPsd_Bakc), null);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finishButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_NewPsd_Finish), null);
                showDialog(null, getString(R.string.user_password_setting));
                pwd = pwdEditText.getText().toString();
                resetPassword.startResetPassword(userContact, userContact, MD5.encryptMD5(pwd));
                break;
            }
            case R.id.eyeImageView: {
                int selectionStart = pwdEditText.getSelectionStart();
                if (pwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    pwdEditText.setTransformationMethod(null);
                    eyeImageView.setImageResource(R.drawable.password_hide);
                } else {
                    pwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeImageView.setImageResource(R.drawable.password_show);
                }
                if (selectionStart > 0) {
                    try {
                        pwdEditText.setSelection(selectionStart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onButtonClick(View view) {
        finish();
    }

    @Override
    public void onRightful() {
        errorTextView.setVisibility(View.INVISIBLE);
        finishButton.setEnabled(true);
    }

    @Override
    public void onUnlawful() {
        errorTextView.setVisibility(View.VISIBLE);
        finishButton.setEnabled(false);
    }

    @Override
    public void onClearText() {

    }
}
