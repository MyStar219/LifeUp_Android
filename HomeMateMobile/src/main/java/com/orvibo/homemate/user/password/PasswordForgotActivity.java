package com.orvibo.homemate.user.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetEmailType;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.model.GetEmailCode;
import com.orvibo.homemate.model.GetSmsCode;
import com.orvibo.homemate.user.UserEmailIdentifyActivity;
import com.orvibo.homemate.user.UserPhoneIdentifyActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/5/28.
 */
public class PasswordForgotActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener,ProgressDialogFragment.OnCancelClickListener, EditTextWithCompound.OnInputListener{
    private static final String TAG = PasswordForgotActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView tip1TextView, tip2TextView, tip3TextView;
    private EditTextWithCompound userPhoneEmailEditText;
    private Button nextButton;
    private GetSmsCodeControl getSmsCodeControl;
    private GetEmailCodeControl getEmailCodeControl;
    private String userPhoneEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bind_change);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        navigationBar.setCenterText(getString(R.string.login_password_forgot));
        tip1TextView = (TextView) findViewById(R.id.tip1TextView);
        tip2TextView = (TextView) findViewById(R.id.tip2TextView);
        tip3TextView = (TextView) findViewById(R.id.tip3TextView);
        tip1TextView.setVisibility(View.GONE);
        tip2TextView.setVisibility(View.INVISIBLE);
        tip3TextView.setVisibility(View.INVISIBLE);
        userPhoneEmailEditText = (EditTextWithCompound) findViewById(R.id.userPhoneEmailEditText);
        userPhoneEmailEditText.setType(EditTextWithCompound.TYPE_PHONE_EMAIL);
        userPhoneEmailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        userPhoneEmailEditText.setHint(R.string.login_account_hint);
        userPhoneEmailEditText.setOnInputListener(this);
        userPhoneEmailEditText.setNeedRestrict(false);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        getSmsCodeControl = new GetSmsCodeControl(mAppContext);
        getEmailCodeControl = new GetEmailCodeControl(mAppContext);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onCancelClick(View view) {
        getSmsCodeControl.stopRequest();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextButton: {
                nextButton.setEnabled(false);
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ForgotPsd_Next), null);
                userPhoneEmail = userPhoneEmailEditText.getText().toString();
                showDialog(this, getString(R.string.user_identify_getting_code));
                if (StringUtil.isPhone(userPhoneEmail)) {
                    getSmsCodeControl.startGetSmsCode(userPhoneEmail, GetSmsType.MODIFY_PASSWORD);
                } else {
                    getEmailCodeControl.startGetEmailCode(userPhoneEmail, GetEmailType.MODIFY_PASSWORD);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, PasswordForgotSetActivity.class);
            intent.putExtra(Constant.USER_CONTACT, userPhoneEmail);
            startActivity(intent);
            finish();
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private class GetSmsCodeControl extends GetSmsCode {
        public GetSmsCodeControl(Context context) {
            super(context);
        }
        @Override
        public void onGetSmsCodeResult(int result) {
            dismissDialog();
            LogUtil.d(TAG, "onGetSmsCodeResult()-result:" + result);
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent(PasswordForgotActivity.this, UserPhoneIdentifyActivity.class);
                intent.putExtra(Constant.USER_CONTACT, userPhoneEmail);
                intent.putExtra(Constant.GET_SMS_TYPE, GetSmsType.MODIFY_PASSWORD);
                startActivityForResult(intent, 0);
            } else if (result == ErrorCode.FAIL){
                ToastUtil.showToast(getString(R.string.user_identify_send_fail));
            } else if (result == ErrorCode.USER_NOT_EXIST_ERROR){
                userPhoneEmailEditText.setUnlawful();
                ToastUtil.showToast(getString(R.string.user_identify_not_exist));
            } else {
                ToastUtil.showToast(getString(R.string.TIMEOUT));
            }
        }

        @Override
        protected void stopRequest() {
            super.stopRequest();
        }
    }

    private class GetEmailCodeControl extends GetEmailCode {
        public GetEmailCodeControl(Context context) {
            super(context);
        }
        @Override
        public void onGetEmailCodeResult(int result) {
            dismissDialog();
            LogUtil.d(TAG, "onGetEmailCodeResult()-result:" + result);
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent(PasswordForgotActivity.this, UserEmailIdentifyActivity.class);
                intent.putExtra(Constant.USER_CONTACT, userPhoneEmail);
                intent.putExtra(Constant.GET_EMAIL_TYPE, GetEmailType.MODIFY_PASSWORD);
                startActivityForResult(intent, 0);
            } else if (result == ErrorCode.FAIL){
                ToastUtil.showToast(getString(R.string.user_identify_send_fail));
            } else if (result == ErrorCode.USER_NOT_EXIST_ERROR){
                userPhoneEmailEditText.setUnlawful();
                setUserPhoneTipTextView(true);
                ToastUtil.showToast(getString(R.string.user_identify_not_exist));
            } else if(result == ErrorCode.TIMEOUT_GEC){
                ToastUtil.showToast(getString(R.string.TIMEOUT));
            }
        }

        @Override
        protected void stopRequest() {
            super.stopRequest();
        }
    }

    @Override
    public void onRightful() {
        nextButton.setEnabled(true);
        setUserPhoneTipTextView(false);
    }

    @Override
    public void onUnlawful() {
        nextButton.setEnabled(false);
        setUserPhoneTipTextView(false);
    }

    @Override
    public void onClearText() {

    }

    private void setUserPhoneTipTextView(boolean isError){
        if (isError){
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            tip3TextView.setText(R.string.user_identify_not_exist);
            tip3TextView.setTextColor(getResources().getColor(R.color.red));
            tip3TextView.setVisibility(View.VISIBLE);
        } else {
            tip3TextView.setVisibility(View.INVISIBLE);
        }
    }
}
