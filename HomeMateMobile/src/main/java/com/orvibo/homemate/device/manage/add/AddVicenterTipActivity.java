package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginType;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * 添加vihome
 * Created by Allen on 2015/8/7.
 */
public class AddVicenterTipActivity extends BaseActivity {
    private static final String TAG = AddVicenterTipActivity.class.getName();
    public static final int CODE_EXIT_ADD_DEVICE = 2;
    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    //    private AdminLogin adminLogin;
//    private Login365 mLogin365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vicenter);
        init();
//        initAdminLogin();
    }

    private void init() {
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        String add = getString(R.string.add);
        navigationCocoBar.setCenterText(add + getString(R.string.device_add_host));
        navigationCocoBar.setOnLeftClickListener(new NavigationCocoBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View v) {
                // ViHomeApplication.getInstance().setIsManage(false);
//                finish();
                exit();
            }
        });
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
    }

//    private void initAdminLogin() {
//        adminLogin = new AdminLogin(mAppContext) {
//            @Override
//            protected void onLogin(int result) {
//                dismissDialog();
//                if (result == ErrorCode.SUCCESS) {
//                    ViHomeApplication.getInstance().setIsManage(true);
//                    Intent intent = new Intent(mContext, AddVicenterActivity.class);
//                    intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
//                    startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
//                } else if (result == ErrorCode.USER_PASSWORD_ERROR) {
//                    if (!UserManage.getInstance(mAppContext).isLoginSuccess()) {
//                        AppTool.notifyMainToGoToLoginActivity();
//                    } else {
//                        ToastUtil.toastError(result);
//                    }
//                } else {
//                    String curUid = UserCache.getCurrentMainUid(mAppContext);
//                    if (StringUtil.isEmpty(curUid) && result == ErrorCode.GATEWAY_NOT_BINDED) {
//                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
//                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_BIND);
//                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
//                    } else if (result == ErrorCode.MNDS_NOT_FOUND_GATEWAY && isResumed) {
//                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                        dialogFragmentOneButton.setTitle(getString(R.string.FAIL));
//                        dialogFragmentOneButton.setContent(getString(R.string.binding_vicenter_fail));
//                        dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
//                        dialogFragmentOneButton.show(getFragmentManager(), "");
//                    } else {
//                        ToastUtil.toastError(result);
//                    }
//                }
//            }
//        };
//    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton: {
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                } else if (!NetUtil.isWifi(mAppContext)) {
                    ToastUtil.toastError(ErrorCode.WIFI_DISCONNECT);
                } else {
                    currentMainUid = UserCache.getCurrentMainUid(mAppContext);
                    String md5Password = UserCache.getMd5Password(mAppContext, userName);
                    if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(md5Password) || StringUtil.isEmpty(currentMainUid)) {
                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_BIND);
                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
                    } else {
                        //去掉管理员登录 by huangqiyao at 2016/3/22
//                        showDialogNow();
                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
//                        adminLogin.login(userName, md5Password, true);
                        //本地判断该账号已经绑定主机，但当其他app此时把主机删除时app会弹框提示数据同步。
//                        if (mLogin365 == null) {
//                            mLogin365 = new Login365(mAppContext);
//                        }
//                        mLogin365.setOnLogin365Listener(new OnLogin365Listener() {
//                            @Override
//                            public void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
//
//                            }
//                        });
//                        mLogin365.login(userName, md5Password);
                    }
                }
                break;
            }
        }
    }

    private void exit() {
        // vihome need to exit admin login.
        currentMainUid = UserCache.getCurrentMainUid(mAppContext);
        if (!StringUtil.isEmpty(currentMainUid) && ViHomeApplication.getInstance().isManage()) {
            new Logout(mAppContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
        }
        ViHomeApplication.getInstance().setIsManage(false);
//        if (adminLogin != null) {
//            adminLogin.cancel();
//        }
        setResult(ResultCode.FINISH);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-resultCode:" + resultCode);
        if (resultCode == ResultCode.FINISH) {
            setResult(ResultCode.FINISH);
            finish();
        } else if (resultCode == CODE_EXIT_ADD_DEVICE) {
            setResult(ResultCode.FINISH);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }
}
