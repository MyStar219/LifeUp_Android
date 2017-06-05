package com.orvibo.homemate.user;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.orvibo.homemate.data.GetEmailType;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.data.UserBindType;
import com.orvibo.homemate.model.GetEmailCode;
import com.orvibo.homemate.model.UserBind;
import com.orvibo.homemate.model.login.LoginConfig;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.password.PasswordSetActivity;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
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
public class UserEmailBindActivity extends BaseActivity implements View.OnClickListener, DialogFragmentOneButton.OnButtonClickListener, EditTextWithCompound.OnInputListener, ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = UserEmailBindActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView tip1TextView, tip2TextView, tip3TextView;
    private EditTextWithCompound userEmailEditText;
    private Button nextButton;
    private GetEmailCodeControl getEmailCodeControl;
    private Account account;
    private String userEmail;
    private UserBind userBind;
    private int userBindType = UserBindType.CHANGE_EMAIL;
    private int getEmailType = GetEmailType.CHANGE_EMAIL;
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
        if (userBindType == UserBindType.BIND_EMAIL) {
            navigationBar.setCenterText(getString(R.string.user_email_bind));
            tip1TextView.setText(R.string.user_email_bind_tip);
            tip2TextView.setVisibility(View.GONE);
        } else {
            navigationBar.setCenterText(getString(R.string.user_email_change));
            tip1TextView.setText(R.string.user_email_change_tip);
        }
        getEmailType = getIntent().getIntExtra(Constant.GET_SMS_TYPE, getEmailType);
        tip3TextView = (TextView) findViewById(R.id.tip3TextView);
        tip3TextView.setText(R.string.user_email_has_bind_tip);
        tip3TextView.setVisibility(View.INVISIBLE);
        userEmailEditText = (EditTextWithCompound) findViewById(R.id.userPhoneEmailEditText);
        userEmailEditText.setHint(R.string.user_email);
        userEmailEditText.setType(EditTextWithCompound.TYPE_EMAIL);
        userEmailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        userEmailEditText.setOnInputListener(this);
        userEmailEditText.setNeedRestrict(false);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        getEmailCodeControl = new GetEmailCodeControl(this);
        userBind = new UserBind(mAppContext) {
            @Override
            public void onUserBindResult(int serial, int result) {
                dismissDialog();
                stopProgress();
                if (result == ErrorCode.SUCCESS) {
                    setUser();
                    DialogFragmentOneButton dialogFragment = new DialogFragmentOneButton();
                    String title = getString(R.string.user_email_change_success);
                    String content = getString(R.string.user_email_change_success_tip);
                    if (userBindType == UserBindType.BIND_EMAIL) {
                        title = getString(R.string.user_email_bind_success);
                        content = getString(R.string.user_email_bind_success_tip);
                    }
                    dialogFragment.setTitle(title);
                    dialogFragment.setContent(content);
                    dialogFragment.setOnButtonClickListener(UserEmailBindActivity.this);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    dialogFragment.show(transaction, getClass().getName());
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
        if (!StringUtil.isEmpty(curUserName) && !curUserName.equals(userEmail) && account.getRegisterType() == RegisterType.REGISTER_USER && userBindType == UserBindType.CHANGE_EMAIL) {

            UserCache.removeUser(mContext, curUserName);
            UserCache.setLoginStatus(mContext, curUserName, LoginStatus.NO_USER);

            List<String> mainUids = UserCache.getMainUids(mContext, curUserName);
            UserCache.saveMainUids(mContext, userEmail, mainUids);
            UserCache.saveUser(mContext, userEmail, md5Password, false);
            UserCache.saveUserId(mContext, curUserId, userEmail);
            UserCache.setLoginStatus(mContext, userEmail, LoginStatus.SUCCESS);
        }
        LoadUtil.noticeLogin(mAppContext, userEmail, md5Password, loginConfig);
        new AccountDao().updEmailByUserId(curUserId, userEmail);
        if (account != null) {
            account.setEmail(userEmail);
        } else {
            MyLogger.kLog().e(account);
        }
    }

    private void refresh() {
        account = new AccountDao().selMainAccountdByUserName(UserCache.getCurrentUserName(this));
        if (account != null) {
            tip2TextView.setText(String.format(getString(R.string.user_email_change_current), StringUtil.hideEmailMiddleWord(account.getEmail())));
        } else {
            MyLogger.kLog().e(account);
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (getEmailType == GetEmailType.BIND_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Back), null);
        } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewEmail_Back), null);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextButton: {
                if (getEmailType == GetEmailType.BIND_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Next), null);
                } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewEmail_Next), null);
                }
                showDialog(this, getString(R.string.user_identify_getting_code));
                showProgress();
                userEmail = userEmailEditText.getText().toString();
                getEmailCodeControl.startGetEmailCode(userEmail, getEmailType);
                break;
            }
        }
    }

    @Override
    public void onRightful() {
        nextButton.setEnabled(true);
        setUserEmailTipTextView(false);
    }

    @Override
    public void onUnlawful() {
        nextButton.setEnabled(false);
        setUserEmailTipTextView(false);
    }

    @Override
    public void onClearText() {
        setUserEmailTipTextView(false);
    }

    @Override
    public void onCancelClick(View view) {
        getEmailCodeControl.stopRequest();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IDENTIFY && account.getRegisterType() != RegisterType.REGISTER_USER && TextUtils.isEmpty(account.getEmail()) && TextUtils.isEmpty(account.getPhone())) {
                Intent intent = new Intent(mContext, PasswordSetActivity.class);
                intent.putExtra(Constant.USER_CONTACT, account.getUserId());
                intent.putExtra(Constant.PWD, UserCache.getMd5Password(mContext, account.getUserId()));
                startActivityForResult(intent, REQUEST_SET_PASSWORD);
            } else {
                String tip = getString(R.string.user_email_changing);
                if (userBindType == UserBindType.BIND_EMAIL) {
                    tip = getString(R.string.user_email_binding);
                }
                if (account != null) {
                    userBind.startUserBind(userBindType, account.getPhone(), userEmail);
                    showDialog(null, tip);
                } else {
                    LogUtil.e(TAG, "onActivityResult: account is null");
                }
            }
        }
    }

    @Override
    public void onButtonClick(View view) {
        if (getEmailType == GetEmailType.BIND_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_BindedEmail_Verificaiton_Roger), null);
        } else if (getEmailType == GetEmailType.CHANGE_EMAIL) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_NewVerificationCode_Roger), null);
        }
        Intent intent = new Intent(this, UserActivity.class);
        account.setEmail(userEmail);
        intent.putExtra(Constant.ACCOUNT, account);
        startActivity(intent);
    }


    private class GetEmailCodeControl extends GetEmailCode {
        public GetEmailCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onGetEmailCodeResult(int result) {
            dismissDialog();
            stopProgress();
            Log.d(getClass().getName(), "result:" + result);
            if (result == ErrorCode.SUCCESS) {
                goIdentify();
            } else if (result == ErrorCode.BIND_EMAIL_FAIL) {
                setUserEmailTipTextView(true);
                userEmailEditText.setUnlawful();
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
        Intent intent = new Intent(this, UserEmailIdentifyActivity.class);
        intent.putExtra(Constant.USER_CONTACT, userEmail);
        intent.putExtra(Constant.GET_EMAIL_TYPE, GetEmailType.BIND_EMAIL);
        startActivityForResult(intent, REQUEST_IDENTIFY);
    }

    private void setUserEmailTipTextView(boolean isError) {
        if (isError) {
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_icon, 0, 0, 0);
            tip3TextView.setText(R.string.user_email_has_bind_tip);
            tip3TextView.setTextColor(getResources().getColor(R.color.red));
            tip3TextView.setVisibility(View.VISIBLE);
        } else {
            tip3TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tip_icon, 0, 0, 0);
            tip3TextView.setVisibility(View.INVISIBLE);
        }
    }
}
