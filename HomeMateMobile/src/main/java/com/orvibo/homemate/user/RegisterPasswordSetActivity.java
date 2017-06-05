package com.orvibo.homemate.user;

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
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.model.account.RegisterX;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

import de.greenrobot.event.EventBus;

/**
 * Created by Allen on 2015/6/12.
 */
public class RegisterPasswordSetActivity extends BaseActivity implements View.OnClickListener,
        ProgressDialogFragment.OnCancelClickListener,
        NavigationCocoBar.OnLeftClickListener,
        DialogFragmentOneButton.OnButtonClickListener,
        EditTextWithCompound.OnInputListener {
    private static final String TAG = RegisterPasswordSetActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private EditTextWithCompound pwdEditText;
    private ImageView eyeImageView;
    private TextView errorTextView;
    private Button finishButton;

    private String userContact, pwd;

    private RegisterX mRegisterX;

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
        pwdEditText.isNeedFilter(false);
        pwdEditText.setNeedRestrict(false);
//        pwdEditText.setPwdEyeEnable(true);
        pwdEditText.setOnInputListener(this);
        pwdEditText.setMinLength(6);
        pwdEditText.setMaxLength(16);
        eyeImageView = (ImageView) findViewById(R.id.eyeImageView);
        eyeImageView.setOnClickListener(this);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        mRegisterX = new RegisterX(mContext) {
            @Override
            public void onRegister(int result) {
                if (isFinishingOrDestroyed()) {
                    return;
                }
                if (result == ErrorCode.SUCCESS) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Success), null);
                } else {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Fail), null);
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
                    dismissDialog();
                    ToastUtil.showToast(R.string.register_fail);
                } else if (result == ErrorCode.USER_REGISTER_ERROR) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.register_fail_exist);
                } else if (result == ErrorCode.TIMEOUT) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.TIMEOUT);
                } else {
                    dismissDialog();
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void toMainView() {
        EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(IntentKey.BOTTOME_TAB_TYPE, BottomTabType.TWO_BOTTOM_TAB);
        startActivity(intent);
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
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Psd_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eyeImageView: {
                int selectionStart = pwdEditText.getSelectionStart();
                if (pwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    pwdEditText.setTransformationMethod(null);
                    eyeImageView.setImageResource(R.drawable.password_hide);
                } else {
                    pwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeImageView.setImageResource(R.drawable.password_show);
                }
//                pwdEditText.setSelection(selectionStart);

                if (selectionStart > 0) {
                    try {
                        pwdEditText.setSelection(selectionStart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.finishButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Psd_Finish), null);
                pwd = pwdEditText.getText().toString();
                showDialog(this, getString(R.string.registering));
                mRegisterX.register(userContact, null, pwd);
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

    @Override
    public void onCancelClick(View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegisterX != null) {
            mRegisterX.cancel();
        }
    }
}
