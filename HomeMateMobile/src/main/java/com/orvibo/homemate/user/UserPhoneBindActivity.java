package com.orvibo.homemate.user;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.data.UserBindType;
import com.orvibo.homemate.model.GetSmsCode;
import com.orvibo.homemate.model.UserBind;
import com.orvibo.homemate.model.login.LoginConfig;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.password.PasswordSetActivity;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.tencent.stat.StatService;

import java.util.List;


/**
 * Created by Allen on 2015/5/28.
 */
public class UserPhoneBindActivity extends BaseActivity implements View.OnClickListener, DialogFragmentOneButton.OnButtonClickListener, EditTextWithCompound.OnInputListener, ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = UserPhoneBindActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView tip1TextView, tip2TextView, tip3TextView;
    private EditTextWithCompound userPhoneEditText;
    private Button nextButton;
    private GetSmsCodeControl getSmsCodeControl;
    private Account account;
    private String userPhone;
    private UserBind userBind;
    private int userBindType = UserBindType.CHANGE_PHONE;
    private int getSmsType = GetSmsType.CHANGE_PHONE;
    private final int REQUEST_IDENTIFY = 1;
    private final int REQUEST_SET_PASSWORD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bind_change);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        tip1TextView = (TextView) findViewById(R.id.tip1TextView);
        tip2TextView = (TextView) findViewById(R.id.tip2TextView);
        userBindType = getIntent().getIntExtra(Constant.USER_BIND_TYPE, userBindType);
        if (userBindType == UserBindType.BIND_PHONE) {
            navigationBar.setCenterText(getString(R.string.user_phone_bind));
            tip1TextView.setText(R.string.user_phone_bind_tip);
            tip2TextView.setVisibility(View.GONE);
        }
        getSmsType = getIntent().getIntExtra(Constant.GET_SMS_TYPE, getSmsType);
        tip3TextView = (TextView) findViewById(R.id.tip3TextView);
        userPhoneEditText = (EditTextWithCompound) findViewById(R.id.userPhoneEmailEditText);
        userPhoneEditText.setType(EditTextWithCompound.TYPE_PHONE);
        userPhoneEditText.setMaxLength(11);
        userPhoneEditText.setOnInputListener(this);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        getSmsCodeControl = new GetSmsCodeControl(mAppContext);
        userBind = new UserBind(mAppContext) {
            @Override
            public void onUserBindResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    setUser();
                    DialogFragmentOneButton dialogFragment = new DialogFragmentOneButton();
                    String title = getString(R.string.user_phone_change_success);
                    String content = getString(R.string.user_phone_change_success_tip);
                    if (userBindType == UserBindType.BIND_PHONE) {
                        title = getString(R.string.user_phone_bind_success);
                        content = getString(R.string.user_phone_bind_success_tip);
                    }
                    dialogFragment.setTitle(title);
                    dialogFragment.setContent(content);
                    dialogFragment.setOnButtonClickListener(UserPhoneBindActivity.this);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    dialogFragment.show(transaction, getClass().getName());
                } else if (result == ErrorCode.FAIL) {
                    ToastUtil.showToast(R.string.user_phone_bind_fail);
                } else if (result == ErrorCode.BIND_PHONE_FAIL) {
                    ToastUtil.showToast(R.string.user_phone_bind_fail_exist);
                } else if (result == ErrorCode.TIMEOUT_UB) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void setUser() {
        String curUserId = UserCache.getCurrentUserId(mContext);
        String curUserName = UserCache.getCurrentUserName(mContext);
        String md5Password = UserCache.getMd5Password(mContext, curUserName);
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.needSaveLastLoginUserName = true;
        loginConfig.startApp = false;
        //手机注册用户修改绑定手机
        if (!StringUtil.isEmpty(curUserName)
                && !curUserName.equals(userPhone)
                && account != null
                && account.getRegisterType() == RegisterType.REGISTER_USER
                && userBindType == UserBindType.CHANGE_PHONE) {

            UserCache.removeUser(mContext, curUserName);
            UserCache.setLoginStatus(mContext, curUserName, LoginStatus.NO_USER);

            List<String> mainUids = UserCache.getMainUids(mContext, curUserName);
            UserCache.saveMainUids(mContext, userPhone, mainUids);
            UserCache.saveUser(mContext, userPhone, md5Password, false);
            UserCache.saveUserId(mContext, curUserId, userPhone);
            UserCache.setLoginStatus(mContext, userPhone, LoginStatus.SUCCESS);
        }
        LoadUtil.noticeLogin(mAppContext, userPhone, md5Password, loginConfig);
        new AccountDao().updPhoneByUserId(curUserId, userPhone);
        if (account != null) {
        }
        account.setPhone(userPhone);

    }

    private void refresh() {
        account = new AccountDao().selMainAccountdByUserName(UserCache.getCurrentUserName(mAppContext));
        MyLogger.kLog().d(account + ",tip2TextView:" + tip2TextView);
        if (account != null) {
            tip2TextView.setText(String.format(getString(R.string.user_phone_change_current), StringUtil.hidePhoneMiddleNumber(account.getPhone())));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextButton: {
                if (getSmsType == GetSmsType.BIND_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_Next), null);
                } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewPhone_Next), null);
                }
                showDialog(this, getString(R.string.user_identify_getting_code));
                userPhone = userPhoneEditText.getText().toString();
                getSmsCodeControl.startGetSmsCode(userPhone, getSmsType);
                break;
            }
        }
    }

    @Override
    public void leftTitleClick(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (getSmsType == GetSmsType.BIND_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_Back), null);
        } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewPhone_Back), null);
        }
        super.onBackPressed();
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
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            tip3TextView.setText(R.string.user_phone_format_error);
            tip3TextView.setTextColor(getResources().getColor(R.color.red));
            userPhoneEditText.setUnlawful();
        }
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
            if (account != null) {
                if (requestCode == REQUEST_IDENTIFY && account.getRegisterType() != RegisterType.REGISTER_USER && TextUtils.isEmpty(account.getEmail()) && TextUtils.isEmpty(account.getPhone())) {
                    Intent intent = new Intent(mContext, PasswordSetActivity.class);
                    intent.putExtra(Constant.USER_CONTACT, account.getUserId());
                    intent.putExtra(Constant.PWD, UserCache.getMd5Password(mContext, account.getUserId()));
                    startActivityForResult(intent, REQUEST_SET_PASSWORD);
                } else {
                    String tip = getString(R.string.user_phone_changing);
                    if (userBindType == UserBindType.BIND_PHONE) {
                        tip = getString(R.string.user_phone_binding);
                    }
                    showDialog(null, tip);
                    userBind.startUserBind(userBindType, userPhone, account.getEmail());
                }
            }
        }
    }

    @Override
    public void onButtonClick(View view) {
        if (getSmsType == GetSmsType.BIND_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedPhone_VerificationCode_Roger), null);
        } else if (getSmsType == GetSmsType.CHANGE_PHONE) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_NewVerificationCode_Roger), null);
        }
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
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
                goIdentify();
            } else if (result == ErrorCode.FAIL) {
                ToastUtil.showToast(getString(R.string.user_identify_send_fail));
            } else if (result == ErrorCode.BIND_PHONE_FAIL) {
                setUserPhoneTipTextView(true);
                userPhoneEditText.setUnlawful();
            } else if (result == ErrorCode.TIMEOUT_GSC) {
                ToastUtil.showToast(getString(R.string.TIMEOUT));
            } else {
                ToastUtil.toastError(result);
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
        intent.putExtra(Constant.GET_SMS_TYPE, getSmsType);
        startActivityForResult(intent, REQUEST_IDENTIFY);
    }

    private void setUserPhoneTipTextView(boolean isError) {
        if (isError) {
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            tip3TextView.setText(R.string.user_phone_has_bind_tip);
            tip3TextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tip_icon, 0, 0, 0);
            tip3TextView.setText(R.string.user_phone_only_china);
            tip3TextView.setTextColor(getResources().getColor(R.color.gray));
        }
    }
}
