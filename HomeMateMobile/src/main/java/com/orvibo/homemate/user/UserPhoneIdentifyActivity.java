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
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.model.CheckSmsCode;
import com.orvibo.homemate.model.GetSmsCode;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

/**
 * Created by Allen on 2015/5/28.
 */
public class UserPhoneIdentifyActivity extends BaseActivity
        implements EditTextWithCompound.OnInputListener,
        View.OnClickListener,
        NavigationCocoBar.OnLeftClickListener,
        ProgressDialogFragment.OnCancelClickListener {
    private NavigationCocoBar navigationBar;
    private TextView userContactTextView, countdownTextView, errorTextView;
    private EditTextWithCompound smsCodeEditText;
    private Button finishButton;
    private Handler handler;
    private GetSmsCodeControl getSmsCodeControl;
    private CheckSmsCodeControl checkSmsCodeControl;
    private String userContact;
    private int getSmsType = GetSmsType.REGISTER;
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
        smsCodeEditText = (EditTextWithCompound) findViewById(R.id.codeEditText);
        smsCodeEditText.setOnInputListener(this);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        Intent intent = getIntent();
        userContact = intent.getStringExtra(Constant.USER_CONTACT);
        getSmsType = intent.getIntExtra(Constant.GET_SMS_TYPE, getSmsType);
        isAutoCountdown = intent.getBooleanExtra(IS_AUTO_COUNTDOWN, isAutoCountdown);
        handler = new MyHandler();
        if (isAutoCountdown) {
            handler.sendEmptyMessage(0);
            String userContactF = String.format(getString(R.string.user_identify_contact), StringUtil.hidePhoneMiddleNumber(userContact));
            userContactTextView.setText(userContactF);
        } else {
            countdownTextView.setText(R.string.user_send);
            String userContactF = String.format(getString(R.string.user_phone_change_current), StringUtil.hidePhoneMiddleNumber(userContact));
            userContactTextView.setText(userContactF);
        }

        getSmsCodeControl = new GetSmsCodeControl(mAppContext);
        checkSmsCodeControl = new CheckSmsCodeControl(mAppContext);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (getSmsType == GetSmsType.REGISTER) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_PhoneVerificationCode_Back), null);
        } else if (getSmsType == GetSmsType.MODIFY_PASSWORD) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_VerificationCode_Back), null);
        } else if (getSmsType == GetSmsType.BIND_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_VerificationCode_Back), null);
        } else if (getSmsType == GetSmsType.BEFORE_CHANGE_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_VerificationCode_Back), null);
        } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewVerificationCode_Back), null);
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.countdownTextView: {
                countdownTextView.setEnabled(false);
                if (getSmsType == GetSmsType.REGISTER) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_PhoneVerificationCode_Resend), null);
                } else if (getSmsType == GetSmsType.MODIFY_PASSWORD) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_Resend), null);
                } else if (getSmsType == GetSmsType.BIND_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_Resend), null);
                } else if (getSmsType == GetSmsType.BEFORE_CHANGE_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_VerificationCode_Resend), null);
                } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewVerificaitonCode_Resend), null);
                }
                showDialog(this, getString(R.string.user_identify_getting_code));
                getSmsCodeControl.startGetSmsCode(userContact, getSmsType);
                break;
            }
            case R.id.finishButton: {
                if (getSmsType == GetSmsType.REGISTER) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_PhoneVerificationCode_Next), null);
                } else if (getSmsType == GetSmsType.MODIFY_PASSWORD) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_VerificationCode_Next), null);
                } else if (getSmsType == GetSmsType.BIND_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_VerificationCode_Finish), null);
                } else if (getSmsType == GetSmsType.BEFORE_CHANGE_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_VerificaionCode_checking), null);
                } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewVerificationCode_Finish), null);
                }
                showDialog(this, getString(R.string.user_identify_checking));
                code = smsCodeEditText.getText().toString();
                checkSmsCodeControl.startCheckSmsCode(userContact, code);
                break;
            }

        }
    }

    @Override
    public void onCancelClick(View view) {
        getSmsCodeControl.stopRequest();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isFinishingOrDestroyed()) {
                return;
            }
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

    private class GetSmsCodeControl extends GetSmsCode {
        public GetSmsCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onGetSmsCodeResult(int result) {
            dismissDialog();
            Log.d(getClass().getName(), "result:" + result);
            if (result == ErrorCode.SUCCESS) {
                countdownTime = COUNTDOWN_BEGIN_TIME;
                handler.sendEmptyMessage(0);
            } else if (result == ErrorCode.FAIL) {
                ToastUtil.showToast(R.string.user_identify_send_fail);
            } else if (result == ErrorCode.USER_REGISTER_ERROR) {
                ToastUtil.showToast(R.string.user_identify_register_fail);
            } else if (result == ErrorCode.BIND_PHONE_FAIL) {
                ToastUtil.showToast(R.string.user_identify_bind_fail);
            } else if (result == ErrorCode.TIMEOUT_GSC) {
                ToastUtil.showToast(R.string.timeout);
            } else {
                ToastUtil.toastError(result);
            }
        }

        @Override
        protected void stopRequest() {
            super.stopRequest();
        }
    }

    private class CheckSmsCodeControl extends CheckSmsCode {

        public CheckSmsCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onCheckSmsCodeResult(int result, String phoneNumber) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                setResult(RESULT_OK);
                finish();

            } else if (result == ErrorCode.FAIL) {
                ToastUtil.showToast(R.string.user_identify_check_fail);
            } else if (result == ErrorCode.PHONE_CODE_NOT_MATCH) {
                ToastUtil.showToast(R.string.user_identify_check_phone_code_not_match);
            } else if (result == ErrorCode.AUTH_CODE_EXPIRED) {
                ToastUtil.showToast(R.string.user_identify_check_code_expired);
            } else if (result == ErrorCode.AUTH_CODE_ERROR) {
//                ToastUtil.showToast(R.string.user_identify_check_code_error);
                smsCodeEditText.setUnlawful();
                errorTextView.setVisibility(View.VISIBLE);
            } else if (result == ErrorCode.TIMEOUT_CSC) {
                ToastUtil.showToast(R.string.timeout);
            } else {
                ToastUtil.toastError(result);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (getSmsCodeControl != null) {
            getSmsCodeControl.cancel();
        }

        if (checkSmsCodeControl != null) {
            checkSmsCodeControl.cancel();
        }
    }
}
