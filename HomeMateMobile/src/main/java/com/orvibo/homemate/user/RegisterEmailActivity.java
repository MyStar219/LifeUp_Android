package com.orvibo.homemate.user;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.model.account.RegisterX;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

import de.greenrobot.event.EventBus;

/**
 * Created by Allen on 2015/6/3.
 */
public class RegisterEmailActivity extends BaseActivity implements
        DialogFragmentTwoButton.OnTwoButtonClickListener,
        NavigationCocoBar.OnLeftClickListener,
        View.OnClickListener,
        EditTextWithCompound.OnInputListener,
        ProgressDialogFragment.OnCancelClickListener {
    private EditTextWithCompound userEmailEditText, passwordEditText;
    private ImageView eyeImageView;
    private TextView tip1ErrorTextView, loginTextView, registerNextTip;
    private Button registerButton, phoneRegisterButton;
    private RegisterX mRegisterX;
    private String email, password;
    private SpannableString spanableInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);
        init();
    }

    private void init() {
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        userEmailEditText = (EditTextWithCompound) findViewById(R.id.userPhoneEmailEditText);
        userEmailEditText.setType(EditTextWithCompound.TYPE_EMAIL);
        userEmailEditText.setOnInputListener(this);
        userEmailEditText.setNeedRestrict(false);
        passwordEditText = (EditTextWithCompound) findViewById(R.id.passwordEditText);
//        passwordEditText.setPwdEyeEnable(true);
        passwordEditText.setMinLength(6);
        passwordEditText.setMaxLength(16);
        passwordEditText.setOnInputListener(this);
        passwordEditText.setNeedRestrict(false);
        eyeImageView = (ImageView) findViewById(R.id.eyeImageView);
        eyeImageView.setOnClickListener(this);
        tip1ErrorTextView = (TextView) findViewById(R.id.tip1ErrorTextView);
//        tip2ErrorTextView = (TextView) findViewById(R.id.tip2ErrorTextView);
        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(this);
        registerNextTip = (TextView) findViewById(R.id.registerNextTip);
        phoneRegisterButton = (Button) findViewById(R.id.phoneRegisterButton);
        phoneRegisterButton.setOnClickListener(this);
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        mRegisterX = new RegisterX(mContext) {
            @Override
            public void onRegister(int result) {
                if (isFinishingOrDestroyed()) {
                    return;
                }
                if (result == ErrorCode.SUCCESS) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Success), null);
                } else {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Fail), null);
                    ToastUtil.toastError(result);
                }
            }

            @Override
            public void onRegisterXFinish(int result) {
                if (isFinishingOrDestroyed()) {
                    return;
                }
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    toMainView();
                    finish();
                } else if (result == ErrorCode.FAIL) {
                    ToastUtil.showToast(R.string.register_fail);
                } else if (result == ErrorCode.USER_REGISTER_ERROR) {
                    ToastUtil.showToast(R.string.register_fail_exist);
                    tip1ErrorTextView.setVisibility(View.VISIBLE);
                   // loginTextView.setVisibility(View.VISIBLE);
                    userEmailEditText.setUnlawful();
                } else if (result == ErrorCode.TIMEOUT_RK) {
                    ToastUtil.showToast(R.string.TIMEOUT);
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        String title = getResources().getString(R.string.register_email_next_tip);
        String title_error = getResources().getString(R.string.register_exist);
        if (PhoneUtil.isCN(mContext)) {
            registerNextTip.setText(getClickableSpan(title, 19, title.length()));
            tip1ErrorTextView.setText(getClickableSpan(title_error, 12, title_error.length()));
        } else {
            registerNextTip.setText(getClickableSpan(title, 58, title.length()));
            tip1ErrorTextView.setText(getClickableSpan(title_error, 52, title_error.length()));
        }
        registerNextTip.setMovementMethod(LinkMovementMethod.getInstance());
        tip1ErrorTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private SpannableString getClickableSpan(final String title, int start, int end) {
        View.OnClickListener userAgreementClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Agreement), null);
                if (RegisterEmailActivity.this.getString(R.string.register_exist).equals(title)) {
                    toLogin();
                } else {
                    Intent intent = new Intent(RegisterEmailActivity.this, UserAgreementActivity.class);
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
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return spanableInfo;
    }

    private void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.RegisterEmailActivity);
//                intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
        intent.putExtra(Constant.ACCOUNT, email);
        startActivity(intent);
        finish();
    }

    private void toMainView() {
        EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        Intent intent = new Intent(mContext, MainActivity.class);
//        intent.putExtra(IntentKey.BOTTOME_TAB_TYPE, BottomTabType.TWO_BOTTOM_TAB);
        startActivity(intent);
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
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eyeImageView: {
                int selectionStart = passwordEditText.getSelectionStart();
                if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    passwordEditText.setTransformationMethod(null);
                    eyeImageView.setImageResource(R.drawable.password_hide);
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeImageView.setImageResource(R.drawable.password_show);
                }
//                passwordEditText.setSelection(selectionStart);
                if (selectionStart > 0) {
                    try {
                        passwordEditText.setSelection(selectionStart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.loginTextView: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Login), null);
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constant.LOGIN_ENTRY, Constant.RegisterEmailActivity);
//                intent.putExtra(IntentKey.LOGIN_INTENT, LoginIntent.SERVER);
                intent.putExtra(Constant.ACCOUNT, email);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.registerButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Register), null);
                email = userEmailEditText.getText().toString();
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.register_email_confirm));
                dialogFragmentTwoButton.setContent(String.format(getString(R.string.register_email_tip), email));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.register_email_fix));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.register_email_continue));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                password = passwordEditText.getText().toString();
                break;
            }
            case R.id.phoneRegisterButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Phone), null);
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }

    @Override
    public void onRightful() {
        if (userEmailEditText.isRightful() && passwordEditText.isRightful()) {
            registerButton.setEnabled(true);
        } else {
            registerButton.setEnabled(false);
        }
        tip1ErrorTextView.setVisibility(View.GONE);
        loginTextView.setVisibility(View.GONE);
//        tip2ErrorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onUnlawful() {
        registerButton.setEnabled(false);
        tip1ErrorTextView.setVisibility(View.GONE);
//        tip2ErrorTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClearText() {
        tip1ErrorTextView.setVisibility(View.GONE);
        loginTextView.setVisibility(View.GONE);
    }

    @Override
    public void onCancelClick(View view) {
    }

    @Override
    public void onLeftButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Fix), null);
    }

    @Override
    public void onRightButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Email_Continue), null);
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        showDialog(this, getString(R.string.registering));
        mRegisterX.register(null, email, password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegisterX != null) {
            mRegisterX.cancel();
        }
    }
}
