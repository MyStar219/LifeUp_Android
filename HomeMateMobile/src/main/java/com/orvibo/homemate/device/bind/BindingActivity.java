//package com.orvibo.homemate;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.orvibo.homemate.core.Bind;
//import com.orvibo.homemate.core.Bind.OnBindListener;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.data.IntentKey;
//import com.orvibo.homemate.data.LoginType;
//import com.orvibo.homemate.model.login.Logout;
//import com.orvibo.homemate.sharedPreferences.BindGatewayCache;
//import com.orvibo.homemate.ui.AddDevicePopup;
//import com.orvibo.homemate.ui.BindPopup;
//import com.orvibo.homemate.util.AppTool;
//import com.orvibo.homemate.util.LogUtil;
//
//import java.util.List;
//
///**
// * @author Smagret
// * @data 2015/01/28
// * @deprecated 已不再使用旧的绑定交互。
// * 绑定gateway
// */
//public class BindingActivity extends BaseActivity implements OnClickListener {
//    private static final String TAG = BindingActivity.class.getSimpleName();
//
//    private Bind mBind;
//
//    // ui
//    private AddDevicePopup mAddDevicePopup;
//
//    private Button binding_btn;
//    private TextView mTitle_tv;
//    private TextView mBindStatus_tv;
//    private TextView mBindTip_tv;
//    private List<String> mUids = null;
//    private BindPopup mBindPopup;
//    private final int UPDATE_UI = 1;
//
//    private int bindStatus = Constant.INVALID_NUM;
//
//    /**
//     * true 设置界面添加新主机
//     */
//    private boolean isFromSetting;
//
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_UI: {
//                    mBindPopup.dismiss();
//                    if (mUids != null && mUids.size() > 0) {
//                        showBindSuccessView(mUids.size());
//
//                        //绑定成功，管理员登录
//                        if (BindGatewayCache.isFromLogin(mContext)) {
//                            startActivity(new Intent(mContext, MainActivity.class));
//                            LogUtil.d(TAG, "handleMessage()-from login");
//                            Intent intent = new Intent(mContext, AdminLoginActivity.class);
//                            intent.putExtra(IntentKey.SEARCH_DEVICE, true);
//                            startActivity(intent);
//                            finish();
//                        }
//                    } else {
//                        showBindFailView();
//                        //ToastUtil.toastError( msg.arg1);
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_binding);
//        LogUtil.d(TAG, "onCreate()");
//
//        isFromSetting = getIntent().getBooleanExtra(IntentKey.FROM_ADD_GATEWAY, false);
//        initView();
//        initListener();
//        mBind = new Bind(mContext);
//        mBind.setOnBindListener(new OnBindListener() {
//            @Override
//            public void onBind(List<String> uids, int result) {
//                LogUtil.d(TAG, "onBind()-uids-" + uids + ",result:" + result + ",thread:" + Thread.currentThread());
//                bindStatus = result;
//                mUids = uids;
//                Message msg = mHandler.obtainMessage();
//                msg.what = UPDATE_UI;
//                msg.arg1 = result;
//                mHandler.sendMessage(msg);
////                mHandler.sendEmptyMessageDelayed(UPDATE_UI, 0);
//            }
//        });
//        boolean isFromRegister = BindGatewayCache.isFromRegister(mContext);
//        boolean isFromLogin = BindGatewayCache.isFromLogin(mContext);
//        boolean isFromAddNewGateway = BindGatewayCache.isFromAddNewGateway(mContext);
//        LogUtil.d(TAG, "onCreate()-isFromRegister:" + isFromRegister + ",isFromLogin:" + isFromLogin + ",isFromAddNewGateway:" + isFromAddNewGateway);
//    }
//
//    private void initView() {
//        mTitle_tv = (TextView) findViewById(R.id.title_tv);
//        binding_btn = (Button) findViewById(R.id.binding_btn);
//        mBindStatus_tv = (TextView) findViewById(R.id.bindStatus_tv);
//        mBindTip_tv = (TextView) findViewById(R.id.bindTip_tv);
//        showBindView();
//
//        mBindPopup = new BindPopup(mContext);
//    }
//
//    private void initListener() {
//        binding_btn.setOnClickListener(this);
//    }
//
//    private void setBindTitle(int id) {
//        mTitle_tv.setText(id);
//    }
//
//    private void setBindButtonText(int textId) {
//        binding_btn.setText(textId);
//    }
//
//    private void showBindView() {
//        mBindStatus_tv.setVisibility(View.INVISIBLE);
//        mBindTip_tv.setText(R.string.binging_tips);
//        setBindTitle(R.string.binging_host);
//        setBindButtonText(R.string.binging_host);
//    }
//
//    private void showBindSuccessView(int successCount) {
//        mBindStatus_tv.setVisibility(View.VISIBLE);
//        mBindStatus_tv.setCompoundDrawables(
//                getResources().getDrawable(R.drawable.icon_right_normal), null,
//                null, null);
//        mBindStatus_tv.setText(String.format(
//                getString(R.string.binging_success_toast), successCount));
//        mBindTip_tv.setText(String.format(
//                getString(R.string.binging_success_tip), successCount));
//        setBindTitle(R.string.binging_success);
//        setBindButtonText(R.string.search);
//    }
//
//    private void showBindFailView() {
//        mBindStatus_tv.setVisibility(View.VISIBLE);
//        mBindStatus_tv.setCompoundDrawables(
//                getResources().getDrawable(R.drawable.icon_error_normal), null,
//                null, null);
//        mBindStatus_tv.setText(R.string.binding_fail_tips1);
//        mBindTip_tv.setText(R.string.binding_fail_tips2);
//        setBindTitle(R.string.binging_fail);
//        setBindButtonText(R.string.binging_rebind);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.binding_btn:
//                if (bindStatus == ErrorCode.SUCCESS) {
//                    if (BindGatewayCache.isFromLogin(mContext)) {
//                        startActivity(new Intent(mContext, MainActivity.class));
//                        Intent intent = new Intent(mContext, AdminLoginActivity.class);
//                        intent.putExtra(IntentKey.SEARCH_DEVICE, true);
//                        startActivity(intent);
//                        finish();
//                    } else if (BindGatewayCache.isFromRegister(mContext)) {
//                        //从注册进入到绑定界面
//                        mAddDevicePopup = new AddDevicePopup(mContext) {
//                            @Override
//                            protected void onSetDevices() {
//                                exitManage();
//                                startActivity(new Intent(mContext, MainActivity.class));
//                                super.onSetDevices();
//                                finish();
//                            }
//
//                            @Override
//                            protected void onToAddFailView() {
//                                exitManage();
//                                startActivity(new Intent(mContext, MainActivity.class));
//                                super.onToAddFailView();
//                                finish();
//                            }
//                        };
//                        mAddDevicePopup.showToast(true);
//                    } else {
//                        //添加新主机，直接跳转到设备管理界面，并进行搜索设备操作
//                        Intent intent = new Intent(mContext, DeviceManageActivity.class);
//                        intent.putExtra(IntentKey.SEARCH_DEVICE, true);
//                        startActivity(intent);
//                        finish();
//                    }
//                } else {
//                    showBindView();
//                    mBindPopup.showToast();
//                    mBind.bind();
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    /**
//     * 退出管理员
//     */
//    private void exitManage() {
//        if (mUids != null && !mUids.isEmpty()) {
//            Logout logout = new Logout(mContext) {
//                @Override
//                protected void onLogout(String uid, int serial, int result) {
//                    LogUtil.d(TAG, "onLogout()-uid:" + uid + ",result:" + result);
//                }
//            };
//
//            for (String uid : mUids) {
//                logout.logoutVicenter(uid, LoginType.ADMIN_LOGIN);
//            }
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mBindPopup != null && mBindPopup.isShowing()) {
//            mBindPopup.dismiss();
//        } else {
//            if (!isFromSetting) {
//                AppTool.exitApp(mContext);
//            }
//            super.onBackPressed();
//        }
//    }
//}
