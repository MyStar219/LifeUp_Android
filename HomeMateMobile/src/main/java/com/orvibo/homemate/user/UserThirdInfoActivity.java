package com.orvibo.homemate.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.ThirdAccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.model.ThirdBind;
import com.orvibo.homemate.model.ThirdUnbind;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.Serializable;

public class UserThirdInfoActivity extends BaseActivity implements UserThirdAuth.OnAuthListener {

    private ImageView           mBindIconImageView;
    private TextView            mBindInfoTextView;
    private Button              mBindChangeButton;
    private NavigationCocoBar   mNavigationCocoBar;
    private int registerType;
    private ThirdAccountDao thirdAccountDao;
    private ThirdAccount thirdAccount;
    private UserThirdAuth userThirdAuth;
    private ThirdBind thirdBind;
    private ThirdUnbind thirdUnbind;
    private UMShareAPI shareAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        registerType = intent.getIntExtra(Constant.REGISTER_TYPE, RegisterType.WEIXIN_USER);
        Serializable serializable = intent.getSerializableExtra(Constant.THIRD_ACCOUNT);
        if (serializable != null) {
            thirdAccount = (ThirdAccount) serializable;
        }
        thirdAccountDao = new ThirdAccountDao();
        setContentView(R.layout.activity_user_bind_info);
        initView();
        shareAPI = UMShareAPI.get(mAppContext);
        userThirdAuth = new UserThirdAuth(this, shareAPI);
        userThirdAuth.setOnAuthListener(this);
        initThirdBind();
        initThirdUnbind();
        refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareAPI.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        mNavigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        mBindIconImageView = (ImageView) findViewById(R.id.bindIconImageView);
        mBindInfoTextView = (TextView) findViewById(R.id.bindInfoTextView);
        mBindChangeButton = (Button) findViewById(R.id.bindChangeButton);
        mBindChangeButton.setOnClickListener(this);

    }

    private void refresh() {
        if (registerType == RegisterType.WEIXIN_USER) {
            mNavigationCocoBar.setCenterText(getString(R.string.auth_login_wechat_account));
            if (thirdAccount != null) {
                mBindIconImageView.setImageResource(R.drawable.bg_wechat_unbinging);
                String info = String.format(getString(R.string.auth_bind_info), getString(R.string.auth_login_wechat2)) + thirdAccount.getThirdUserName();
                mBindInfoTextView.setText(info);
                mBindChangeButton.setText(R.string.auth_unbind);
            } else {
                mBindIconImageView.setImageResource(R.drawable.bg_wechat_binging);
                String tips = String.format(getString(R.string.auth_bind_tips), getString(R.string.auth_login_wechat2));
                mBindInfoTextView.setText(tips);
                mBindChangeButton.setText(String.format(getString(R.string.auth_bind_target), getString(R.string.auth_login_wechat2)));
            }
        } else if (registerType == RegisterType.QQ_USER) {
            mNavigationCocoBar.setCenterText(getString(R.string.auth_login_qq_account));
            if (thirdAccount != null) {
                mBindIconImageView.setImageResource(R.drawable.bg_qq_unbinging);
                String info = String.format(getString(R.string.auth_bind_info), getString(R.string.auth_login_qq2)) + thirdAccount.getThirdUserName();
                mBindInfoTextView.setText(info);
                mBindChangeButton.setText(R.string.auth_unbind);
            } else {
                mBindIconImageView.setImageResource(R.drawable.bg_qq_binging);
                String tips = String.format(getString(R.string.auth_bind_tips), getString(R.string.auth_login_qq2));
                mBindInfoTextView.setText(tips);
                mBindChangeButton.setText(String.format(getString(R.string.auth_bind_target), getString(R.string.auth_login_qq2)));
            }
        } else if (registerType == RegisterType.SINA_USER) {
            mNavigationCocoBar.setCenterText(getString(R.string.auth_login_sina_account));
            if (thirdAccount != null) {
                mBindIconImageView.setImageResource(R.drawable.bg_microblog_unbinging);
                String info = String.format(getString(R.string.auth_bind_info), getString(R.string.auth_login_sina2)) + thirdAccount.getThirdUserName();
                mBindInfoTextView.setText(info);
                mBindChangeButton.setText(R.string.auth_unbind);
            } else {
                mBindIconImageView.setImageResource(R.drawable.btn_bg_microblog_binging);
                String tips = String.format(getString(R.string.auth_bind_tips), getString(R.string.auth_login_sina2));
                mBindInfoTextView.setText(tips);
                mBindChangeButton.setText(String.format(getString(R.string.auth_bind_target), getString(R.string.auth_login_sina2)));
            }
        }
    }

    private void initThirdBind() {
        thirdBind = new ThirdBind(mAppContext) {
            @Override
            public void onBindResult(int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    //绑定成功之后，由于thirdAccount信息不完整，需要从数据库中查询出来
                    thirdAccount = thirdAccountDao.selThirdAccount(thirdAccount.getThirdId());
                    refresh();
                } else {
                    thirdAccount = null;
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initThirdUnbind() {
        thirdUnbind = new ThirdUnbind() {
            @Override
            public void onUnbindResult(int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    thirdAccount = null;
                    refresh();
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.auth_unbind_success));
                    dialogFragmentOneButton.setContent(getString(R.string.auth_unbind_success_content));
                    dialogFragmentOneButton.show(getFragmentManager(),"");
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (registerType == RegisterType.WEIXIN_USER) {
            if (thirdAccount == null) {
                if (AppTool.isWechatInstalled(mAppContext)){
                    userThirdAuth.authLogin(SHARE_MEDIA.WEIXIN);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_wechat);
                }
            } else {
                unbind();
            }
        } else if (registerType == RegisterType.QQ_USER) {
            if (thirdAccount == null) {
                if (AppTool.isQQInstalled(mAppContext)){
                    userThirdAuth.authLogin(SHARE_MEDIA.QQ);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_qq);
                }
            } else {
                unbind();
            }
        } else if (registerType == RegisterType.SINA_USER) {
            if (thirdAccount == null) {
                if (AppTool.isSinaInstalled(mAppContext)){
                    userThirdAuth.authLogin(SHARE_MEDIA.SINA);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_sina);
                }
            } else {
                unbind();
            }
        }
    }

    private void unbind() {
        Account account = new AccountDao().selAccountByUserId(userId);
        if(thirdAccountDao.getBindCount(userId) > 1 || !TextUtils.isEmpty(account.getPhone()) || !TextUtils.isEmpty(account.getEmail())){
            thirdUnbind.startUnbind(thirdAccount.getThirdAccountId());
        } else {
            showCannotUnbindDialog();
        }
    }

    private void showCannotUnbindDialog() {
        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
        dialogFragmentOneButton.setTitle(getString(R.string.auth_cannot_unbind));
        dialogFragmentOneButton.setContent(getString(R.string.auth_cannot_unbind_content));
        dialogFragmentOneButton.show(getFragmentManager(),"");
    }

    @Override
    public void onComplete(ThirdAccount thirdAccount) {
        showDialog(null, getString(R.string.binding_progress));
        this.thirdAccount = thirdAccount;//第三方认证通过之后获取部分信息封装到thirdAccount中
        thirdBind.startBind(userId, thirdAccount.getThirdUserName(), thirdAccount.getThirdId(),thirdAccount.getToken(), registerType,thirdAccount.getFile());
    }

    @Override
    public void onError() {

    }

    @Override
    public void onCancel() {

    }
}
