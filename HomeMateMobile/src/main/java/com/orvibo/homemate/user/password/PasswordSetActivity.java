package com.orvibo.homemate.user.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.ModifyPassword;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MD5;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;

/**
 * @author Allen
 * @data 2015/03/28
 */
public class PasswordSetActivity extends BaseActivity {
    private static final String TAG = PasswordSetActivity.class.getSimpleName();
    private EditTextWithCompound firstPassword_et;
    private ModifyPassword modifyPassword;
    private String oldPassword;
    private String userName;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_set);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        userName = intent.getStringExtra(Constant.USER_CONTACT);
        oldPassword = intent.getStringExtra(Constant.PWD);

        initView();
        initListener();
        modifyPassword = new ModifyPassword(mAppContext) {
            @Override
            public void onModifyPasswordResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    setResult(RESULT_OK);
                    //保存用户
                    UserCache.saveUser(mAppContext, userName , MD5.encryptMD5(password), false);
                    UserCache.savePassword(mAppContext, userName, password);
                    //启动心跳包
                    AppTool.setHeartbeart(mAppContext, true);
                    finish();
                } else {
                    ToastUtil.showToast(ErrorMessage.getError(mAppContext, result),ToastType.ERROR, ToastType.SHORT);
                }
            }
        };
    }

    private void initView() {
        firstPassword_et = (EditTextWithCompound) findViewById(R.id.firstPassword_et);
    }

    private void initListener() {

    }

    public void confirm(View v) {
        if (!NetUtil.isNetworkEnable(mAppContext)) {
            ToastUtil.toastError( ErrorCode.NET_DISCONNECT);
            return;
        }

        String firstPassword = firstPassword_et.getText().toString();
        if (StringUtil.isEmpty(oldPassword) && StringUtil.isEmpty(firstPassword) || StringUtil.stringLength(firstPassword) < 6) {
            ToastUtil.showToast( R.string.password_empty, ToastType.ERROR, Toast.LENGTH_LONG);
        } else if (StringUtil.isEmpty(firstPassword) || StringUtil.stringLength(firstPassword) > 16) {
            ToastUtil.showToast( R.string.password_max, ToastType.ERROR, Toast.LENGTH_LONG);
        } else {
            LogUtil.e(TAG, "confirm() - userName:" + userName);
            showDialog();
            password = firstPassword;
            modifyPassword.startModifyPassword(UserCache.getCurrentMainUid(mAppContext), userName, oldPassword, MD5.encryptMD5(firstPassword));
        }
    }

    @Override
    public void leftTitleClick(View v) {
        setResult(ResultCode.FINISH_REGISTER);
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        setResult(ResultCode.FINISH_REGISTER);
        super.onBackPressed();
    }
}
