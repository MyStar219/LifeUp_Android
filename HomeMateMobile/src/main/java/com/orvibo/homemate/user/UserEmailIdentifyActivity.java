package com.orvibo.homemate.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetEmailType;
import com.orvibo.homemate.model.CheckEmailCode;
import com.orvibo.homemate.model.GetEmailCode;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

/**
 * 验证邮箱验证码
 * Created by Allen on 2015/5/28.
 */
public class UserEmailIdentifyActivity extends BaseActivity implements EditTextWithCompound.OnInputListener, View.OnClickListener, NavigationCocoBar.OnLeftClickListener, ProgressDialogFragment.OnCancelClickListener {
    private NavigationCocoBar navigationBar;
    private TextView userContactTextView, countdownTextView, errorTextView;
    private EditTextWithCompound codeEditText;
    private Button finishButton;
    private Handler handler;
    private GetEmailCodeControl getEmailCodeControl;
    private CheckEmailCodeControl checkEmailCodeControl;
    private String userContact;
    private int getEmailType = GetEmailType.REGISTER;
    private String code;
    private static final int COUNTDOWN_BEGIN_TIME = 60;
    private int countdownTime = COUNTDOWN_BEGIN_TIME;
    private boolean isAutoCountdown = true;
    public static final String IS_AUTO_COUNTDOWN = "is_auto_countdown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_identify);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        userContactTextView = (TextView) findViewById(R.id.userContactTextView);
        countdownTextView = (TextView) findViewById(R.id.countdownTextView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        countdownTextView.setOnClickListener(this);
        codeEditText = (EditTextWithCompound) findViewById(R.id.codeEditText);
        codeEditText.setOnInputListener(this);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        Intent intent = getIntent();
        userContact = intent.getStringExtra(Constant.USER_CONTACT);
        isAutoCountdown = intent.getBooleanExtra(IS_AUTO_COUNTDOWN, isAutoCountdown);
        handler = new MyHandler();
        if (isAutoCountdown) {
            handler.sendEmptyMessage(0);
            String userContactF = String.format(getString(R.string.user_identify_contact), StringUtil.hideEmailMiddleWord(userContact));
            userContactTextView.setText(userContactF);
        } else {
            countdownTextView.setText(R.string.user_send);
            String userContactF = String.format(getString(R.string.user_email_change_current), StringUtil.hideEmailMiddleWord(userContact));
            userContactTextView.setText(userContactF);
        }
        getEmailType = intent.getIntExtra(Constant.GET_EMAIL_TYPE, getEmailType);
        if (getEmailType == GetEmailType.REGISTER) {
            navigationBar.setCenterText(getString(R.string.register_title));
        }

        getEmailCodeControl = new GetEmailCodeControl(mAppContext);
        checkEmailCodeControl = new CheckEmailCodeControl(mAppContext);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (getEmailType == GetEmailType.MODIFY_PASSWORD) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_VerificationCode_Back), null);
        } else if (getEmailType == GetEmailType.BIND_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Verification_Back), null);
        } else if (getEmailType == GetEmailType.BEFORE_CHANGE_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_VerificationCode_Back), null);
        } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewVerificationCode_Back), null);
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.countdownTextView: {
                countdownTextView.setEnabled(false);
                if (getEmailType == GetEmailType.MODIFY_PASSWORD) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_Resend), null);
                } else if (getEmailType == GetEmailType.BIND_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Verification_Resend), null);
                } else if (getEmailType == GetEmailType.BEFORE_CHANGE_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_VerificationCode_Resend), null);
                } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewVerificationCode_Resend), null);
                }
                showDialog(this, getString(R.string.user_identify_getting_code));
                getEmailCodeControl.startGetEmailCode(userContact, getEmailType);
                break;
            }
            case R.id.finishButton: {
                if (getEmailType == GetEmailType.MODIFY_PASSWORD) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_VerificationCode_Next), null);
                } else if (getEmailType == GetEmailType.BIND_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Verification_Finish), null);
                } else if (getEmailType == GetEmailType.BEFORE_CHANGE_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_VerificationCode_checking), null);
                } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewVerificationCode_Finish), null);
                }
                showDialog(this, getString(R.string.user_identify_checking));
                code = codeEditText.getText().toString();
                checkEmailCodeControl.startCheckEmailCode(userContact, code);
                break;
            }

        }
    }

    @Override
    public void onCancelClick(View view) {
        getEmailCodeControl.stopRequest();
    }

    @Override
    public void onRightful() {
        errorTextView.setVisibility(View.INVISIBLE);
        finishButton.setEnabled(true);
    }

    @Override
    public void onUnlawful() {
        errorTextView.setVisibility(View.INVISIBLE);
        finishButton.setEnabled(false);
    }

    @Override
    public void onClearText() {

    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (--countdownTime <= 0) {
                countdownTextView.setText(R.string.user_identify_send);
                countdownTextView.setEnabled(true);
            } else {
                String countDown = String.format(getString(R.string.user_identify_countdown), countdownTime);
                countdownTextView.setText(countDown);
                countdownTextView.setEnabled(false);
                handler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    }

    private class GetEmailCodeControl extends GetEmailCode {
        public GetEmailCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onGetEmailCodeResult(int result) {
            dismissDialog();
            Log.d(getClass().getName(), "result:" + result);
            if (result == ErrorCode.SUCCESS) {
                countdownTime = COUNTDOWN_BEGIN_TIME;
                handler.sendEmptyMessage(0);
            } else if (result == ErrorCode.FAIL) {
                ToastUtil.showToast(getString(R.string.user_identify_send_fail));
            } else if (result == ErrorCode.USER_REGISTER_ERROR) {
                ToastUtil.showToast(getString(R.string.user_identify_register_fail));
            } else if (result == ErrorCode.BIND_EMAIL_FAIL) {
                ToastUtil.showToast(getString(R.string.user_identify_bind_fail));
            } else if (result == ErrorCode.TIMEOUT_GEC) {
                ToastUtil.showToast(getString(R.string.timeout));
            } else {
                ToastUtil.toastError(result);
            }
        }

        @Override
        protected void stopRequest() {
            super.stopRequest();
        }
    }

    private class CheckEmailCodeControl extends CheckEmailCode {

        public CheckEmailCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onCheckEmailCodeResult(int result, String emailNumber) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                setResult(RESULT_OK);
                finish();
            } else if (result == ErrorCode.FAIL) {
                ToastUtil.showToast(getString(R.string.user_identify_check_fail));
            } else if (result == ErrorCode.EMAIL_CODE_NOT_MATCH) {
                ToastUtil.showToast(getString(R.string.user_identify_check_email_code_not_match));
            } else if (result == ErrorCode.AUTH_CODE_EXPIRED) {
                ToastUtil.showToast(getString(R.string.user_identify_check_code_expired));
            } else if (result == ErrorCode.AUTH_CODE_ERROR) {
//                ToastUtil.showToast(getString(R.string.user_identify_check_code_error));
                codeEditText.setUnlawful();
                errorTextView.setVisibility(View.VISIBLE);
            } else if (result == ErrorCode.TIMEOUT_CEC) {
                ToastUtil.showToast(getString(R.string.timeout));
            } else {
                ToastUtil.toastError(result);
            }
        }
    }
}
