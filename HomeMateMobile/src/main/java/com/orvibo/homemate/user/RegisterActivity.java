package com.orvibo.homemate.user;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.model.GetSmsCode;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

/**
 * Created by Allen on 2015/6/3.
 */
public class RegisterActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener, View.OnClickListener, EditTextWithCompound.OnInputListener, ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = "RegisterActivity";
    private EditTextWithCompound userPhoneEditText;
    private TextView userPhoneTipTextView, loginTextView, registerNextTip;
    private Button nextButton, emailRegisterButton;
    private GetSmsCodeControl getSmsCodeControl;
    private String userPhone;
    private static final int GET_SMS_CODE = 0;
    private static final int REGISTER_EMAIL = 1;
    private SpannableString spanableInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        userPhoneEditText = (EditTextWithCompound) findViewById(R.id.userPhoneEmailEditText);
        userPhoneEditText.setType(EditTextWithCompound.TYPE_PHONE);
        userPhoneEditText.setMaxLength(11);
        userPhoneEditText.setOnInputListener(this);
        userPhoneTipTextView = (TextView) findViewById(R.id.userPhoneTipTextView);
        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(this);
        registerNextTip = (TextView) findViewById(R.id.registerNextTip);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        emailRegisterButton = (Button) findViewById(R.id.emailRegisterButton);
        emailRegisterButton.setOnClickListener(this);
        getSmsCodeControl = new GetSmsCodeControl(mAppContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String title = getResources().getString(R.string.register_next_tip);
        if (PhoneUtil.isCN(mContext)) {
            registerNextTip.setText(getClickableSpan(title, 18, title.length()));
        } else {
            registerNextTip.setText(getClickableSpan(title, 49, title.length()));
        }
        registerNextTip.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString getClickableSpan(final String title, int start, int end) {
        View.OnClickListener userAgreementClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Agreement), null);
                if (RegisterActivity.this.getString(R.string.register_exist).equals(title)) {
                    toLogin();
                } else {
                    Intent intent = new Intent(RegisterActivity.this, UserAgreementActivity.class);
                    startActivity(intent);
                }
            }
        };

        spanableInfo = new SpannableString(title);
        try {
            spanableInfo.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            spanableInfo.setSpan(new Clickable(userAgreementClickListener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            spanableInfo.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return spanableInfo;
    }
    private void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.RegisterEmailActivity);
//                intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
        intent.putExtra(Constant.ACCOUNT,userPhone );
        startActivity(intent);
        finish();
    }

    class Clickable extends ClickableSpan implements View.OnClickListener {
        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener l) {
            mListener = l;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginTextView: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Login), null);
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constant.LOGIN_ENTRY, Constant.RegisterActivity);
//                intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
                intent.putExtra(Constant.ACCOUNT, userPhone);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.nextButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phome_Next), null);
                if (NetUtil.isNetworkEnable(mAppContext)) {
                    showDialog(this, getString(R.string.user_identify_getting_code));
                    //showProgressDialog(this, getString(R.string.user_identify_getting_code));
                    userPhone = userPhoneEditText.getText().toString();
                    getSmsCodeControl.startGetSmsCode(userPhone, GetSmsType.REGISTER);
                } else {
                    ToastUtil.showToast(R.string.net_not_connect);
                }
                break;
            }
            case R.id.emailRegisterButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Email), null);
                Intent intent = new Intent(this, RegisterEmailActivity.class);
                startActivity(intent);
                finish();
                break;
            }
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
        if (userPhoneEditText.length() == 11) {
            userPhoneTipTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            userPhoneTipTextView.setTextColor(getResources().getColor(R.color.red));
            userPhoneTipTextView.setText(R.string.user_phone_format_error);
            userPhoneEditText.setUnlawful();
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClearText() {
        setUserPhoneTipTextView(false);
    }

    @Override
    public void onCancelClick(View view) {
        getSmsCodeControl.stopRequest();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_SMS_CODE) {
                Intent intent = new Intent(this, RegisterPasswordSetActivity.class);
                intent.putExtra(Constant.USER_CONTACT, userPhone);
                startActivity(intent);
                finish();
            }
        }
    }

    private class GetSmsCodeControl extends GetSmsCode {
        public GetSmsCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onGetSmsCodeResult(final int result) {
            LogUtil.d(TAG, "result:" + result + ",thread:" + Thread.currentThread());
            if (!isFinishingOrDestroyed()) {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        if (result == ErrorCode.SUCCESS) {
                            goIdentify();//去验证手机验证码界面
                        } else if (result == ErrorCode.FAIL) {
                            ToastUtil.showToast(R.string.user_identify_send_fail);
                        } else if (result == ErrorCode.USER_REGISTER_ERROR) {
                            setUserPhoneTipTextView(true);
                            userPhoneEditText.setUnlawful();
                        } else if (result == ErrorCode.TIMEOUT_RK) {
                            ToastUtil.showToast(R.string.TIMEOUT);
                        } else {
                            ToastUtil.toastError(result);
                        }
                    }
                });
            }
        }

        @Override
        protected void stopRequest() {
            super.stopRequest();
        }
    }

    private void goIdentify() {
        Intent intent = new Intent(this, UserPhoneIdentifyActivity.class);
        intent.putExtra(Constant.USER_CONTACT, userPhone);
        intent.putExtra(Constant.GET_SMS_TYPE, GetSmsType.REGISTER);
        startActivityForResult(intent, GET_SMS_CODE);
    }

    private void setUserPhoneTipTextView(boolean isError) {
        if (isError) {
            userPhoneTipTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            userPhoneTipTextView.setText(R.string.register_exist);
            userPhoneTipTextView.setTextColor(getResources().getColor(R.color.red));
            loginTextView.setVisibility(View.GONE);
            String title_error = getResources().getString(R.string.register_exist);
            if (PhoneUtil.isCN(mContext)) {
                userPhoneTipTextView.setText(getClickableSpan(title_error, 12, title_error.length()));
            } else {
                userPhoneTipTextView.setText(getClickableSpan(title_error, 52, title_error.length()));
            }
            registerNextTip.setMovementMethod(LinkMovementMethod.getInstance());
            userPhoneTipTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            userPhoneTipTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tip_icon, 0, 0, 0);
            userPhoneTipTextView.setText(R.string.user_phone_only_china);
            userPhoneTipTextView.setTextColor(getResources().getColor(R.color.gray));
            loginTextView.setVisibility(View.GONE);
        }
    }
}
