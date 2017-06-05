package com.orvibo.homemate.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.NetChangeHelper;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.reconnect.Reconnect;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.HostManager;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MainActionType;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.event.login.Login365Event;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.model.ThirdRegister;
import com.orvibo.homemate.model.ThirdVerify;
import com.orvibo.homemate.model.login.Login365;
import com.orvibo.homemate.model.login.LoginConfig;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.service.InfoPushService;
import com.orvibo.homemate.service.ViCenterService;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.core.MinaSocket;
import com.orvibo.homemate.user.adapter.LoginAccountAdapter;
import com.orvibo.homemate.user.password.PasswordForgotActivity;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MD5;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.SdkCompat;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WifiUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.smartgateway.app.data.model.Provider;
import com.smartgateway.app.data.model.User.UserUtil;
import com.tencent.stat.StatService;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.videogo.openapi.EZOpenSDK;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 用户登录界面。登录逻辑点击登录->通知VicenterService去执行登录操作，登录结果通过eventBus回调。
 * Created by orvibo on 2015/5/23.
 */
public class LoginActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        UserThirdAuth.OnAuthListener,
//        OnLogin365Listener,
        NavigationCocoBar.OnLeftClickListener,
        ProgressDialogFragment.OnCancelClickListener,
        NetChangeHelper.OnNetChangedListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    /**
     * true正在处于LoginActivity界面，此时MainActivity不会再跳转到此界面
     */
    public static volatile boolean isRunning = false;
    private String userName;
    private String password;

    //view
    private EditTextWithCompound userName_et;
    private EditTextWithCompound password_et;
    private Button login_btn;

    private ImageView dropdownImageView;

    private TextView forget_password_tv;
    private TextView register_tv;

    private String loginEntry;

    private List<String> accounts;
    private PopupWindow popupWindow;
    private LoginAccountAdapter accountAdapter;
    private boolean nameInputFromUser = true;
//    private boolean pwdInputFromUser = true;

    private View loginAccountListView;
    private ImageView eyeNewPwdImageView;

    private NetChangeHelper mNetChangeHelper;


    private long startTime;
    private AccountDao mAccountDao;
    private LoginAccountAdapter accountAdapter_unfocus;
    private PopupWindow popupWindow_unfocuas;
    private boolean isFocusable;
    private ListView mAccountListView_unfocus;
    private ListView mMAccountListView_unfocus;
    private List<String> mContainsPhone;
    private TextView mWechat_login;
    private TextView mQQ_login;
    private TextView mSina_login;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mPopupWindowTop;

    //  private ImageView mShowPasswordImageView;
    //  private ImageView mHidePasswordImageView;
    private int registerType = RegisterType.REGISTER_USER;

    private UMShareAPI shareAPI;
    private ThirdVerify thirdVerify;
    private UserThirdAuth userThirdAuth;
    private ThirdRegister thirdRegister;
    private ThirdAccount thirdAccount;
    private Login365 mLogin365;
    private LinearLayout mThirdAuthTips;
    private LinearLayout mThirdAuthButtons;
    private boolean nameInputFormAccouts;

    /**
     * true登录操作已经结束，按手机返回按钮时如果已经登录结束则不取消登录。
     */
    private volatile boolean isLoginFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isRunning = true;
        MinaSocket.resetServerHost();
        HostManager.resetCurrentServerHost();
        ViHomeApplication.getInstance().setIsManage(false);
        super.onCreate(savedInstanceState);
        mAccountDao = new AccountDao();

        //软键盘弹出时不遮挡输入框
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        loginEntry = intent.getStringExtra(Constant.LOGIN_ENTRY);
        init();
        //修复了切换账号后会登陆上次账号问题
        Reconnect.getInstance().cancel();

        UserCache.removeCurrentUserId(mAppContext);
        UserCache.removeCurrentMainUid(mAppContext);
        UserCache.removeCurrentUserName(mAppContext);
        EZOpenSDK.getInstance().setAccessToken(null);//萤石摄像机sdk清除AccessToken
        String lastLoginUserName = UserCache.getLastLoginUserName(mContext);
        if (lastLoginUserName != null) {
            userName_et.setText(StringUtil.hideUserNameMiddleWord(lastLoginUserName));
            userName = lastLoginUserName;
            nameInputFromUser = false;
        }
        LogUtil.d(TAG, "onCreate()");
        mNetChangeHelper = NetChangeHelper.getInstance(mAppContext);
        TimeCache.clear(mAppContext);
        getScreenWidthAndHeight();
        MainActivity.isCheckedUnbindDevice = false;//设置为没有检查过未绑定设备
        shareAPI = UMShareAPI.get(mAppContext);
        userThirdAuth = new UserThirdAuth(this, shareAPI);
        userThirdAuth.setOnAuthListener(this);
        initThirdVerify();
        initThirdRegister();
    }

    /**
     * 获得屏幕的宽高
     */
    private void getScreenWidthAndHeight() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViHomeApplication.getInstance().setIsManage(false);
        Intent intent = getIntent();
        int errrorCode = intent.getIntExtra(IntentKey.ERROR_CODE, Constant.INVALID_NUM);
//        LogUtil.d(TAG, "onResume()-errrorCode:" + errrorCode);
        if (errrorCode != Constant.INVALID_NUM) {
            if (errrorCode == ErrorCode.USER_PASSWORD_ERROR) {
                //  if (!isDialogShowing()) {
                // }
            }
            ToastUtil.toastError(errrorCode);
            dismissDialog();
        }
//        password_et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        showPassword(false);
//        password_et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        performAutoLogin();
    }

    private void performAutoLogin() {
        Provider provider = UserUtil.getProviderInfo(this, "HomeMate");
        if (provider != null) {
            userName_et.setText(provider.getUsername());
            password_et.setText(provider.getPassword());
            onClick(findViewById(R.id.login_btn));
        } else {
            onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareAPI.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {
        initView();
        initListener();
    }

    private void initView() {
        //*****************************************************************
        //找到第三方登录按钮
        //微信按钮
        mWechat_login = (TextView) findViewById(R.id.wechat_login);
        //qq按钮
        mQQ_login = (TextView) findViewById(R.id.qq_login);
        //新浪按钮
        mSina_login = (TextView) findViewById(R.id.sina_login);
        //*****************************************************************
        //  mShowPasswordImageView = (ImageView) findViewById(R.id.showPasswordImageView);
        // mHidePasswordImageView = (ImageView) findViewById(R.id.hidePasswordImageView);
        userName_et = (EditTextWithCompound) findViewById(R.id.userName_et);
        //显示为普通文本
        //userName_et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        userName_et.setType(EditTextWithCompound.TYPE_PHONE_EMAIL);
        password_et = (EditTextWithCompound) findViewById(R.id.password_et);
        password_et.isNeedFilter(false);
        userName_et.setNeedRestrict(false);
        password_et.setNeedRestrict(false);
        eyeNewPwdImageView = (ImageView) findViewById(R.id.eyeNewPwdImageView);
        eyeNewPwdImageView.setOnClickListener(this);

        mThirdAuthTips = (LinearLayout) findViewById(R.id.thirdAuthTips);
        mThirdAuthButtons = (LinearLayout) findViewById(R.id.thirdAuthButtons);
        hideThirdAuthViews();
        //password_et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        setListener();
//        password_et.setRightfulBackgroundDrawable(getResources().getDrawable(R.drawable.login_edittext_down));

        dropdownImageView = (ImageView) findViewById(R.id.dropdownImageView);
        dropdownImageView.setOnClickListener(this);

        login_btn = (Button) findViewById(R.id.login_btn);
        forget_password_tv = (TextView) findViewById(R.id.forget_password_tv);
        register_tv = (TextView) findViewById(R.id.register_tv);

        nameInputFromUser = false;
//        pwdInputFromUser = false;
//        userName = UserCache.getCurrentUserName(mContext);
//        if (!StringUtil.isEmpty(userName)) {
//            userName_et.setText(userName);
//            inputFromUser = false;
//            userName_et.clearFocus();
//            password_et.requestFocus();
//        }

//        userName = UserCache.getCurrentUserName(this);
//        if (!TextUtils.isEmpty(userName)) {
//            userName_et.setText(StringUtil.hideUserNameMiddleWord(userName));
//            userName_et.requestFocus();
//            userName_et.setSelection(userName_et.length());
//            password = UserCache.getPassword(this, userName);
//            password_et.setText(password);
//            inputFromUser = false;
//        }

        accounts = UserCache.getUserList(this);
        // Toast.makeText(this, accounts.toString(), Toast.LENGTH_LONG).show();
        LogUtil.d(TAG, " onCreate:***********************************" + accounts.toString());
        if (accounts == null || accounts.isEmpty()) {
            dropdownGone();
        }
    }

    /**
     * 海外版本需要隐藏第三方登录接口
     */
    private void hideThirdAuthViews() {
        if (isOverseasVersion) {
            mThirdAuthButtons.setVisibility(View.INVISIBLE);
            mThirdAuthTips.setVisibility(View.INVISIBLE);
        }
    }

    //
    private void setListener() {
        userName_et.setOnInputListener(new EditTextWithCompound.OnInputListener() {
            @Override
            public void onRightful() {
                LogUtil.d(TAG, "onRightful()");
                nameInputFromUser = true;
                nameInputFormAccouts = false;

            }

            @Override
            public void onUnlawful() {
                LogUtil.d(TAG, "onUnlawful()");
                nameInputFromUser = true;
                nameInputFormAccouts = false;
            }

            @Override
            public void onClearText() {

            }
        });
        //监听输入框的输入事件
        userName_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!nameInputFormAccouts) {
                    isFocusable = false;
                    String inputNum = userName_et.getText().toString().trim();
                    if (!inputNum.isEmpty()) {
                        mContainsPhone = new ArrayList<String>();
                     //   List<String> phoneList = mAccountDao.selAllAccountPhone();//数据的来源不是数据库而是记录文件
                        List<String> phoneList = UserCache.getUserList(LoginActivity.this);
                        if (phoneList != null && !phoneList.isEmpty()) {
                            for (String phone : phoneList) {
                                if (!phone.contains(inputNum)) {
                                    continue;
                                }
                                if (!mContainsPhone.contains(phone)) {
                                    mContainsPhone.add(phone);
                                }
                            }
                        }
                        if (popupWindow_unfocuas != null && popupWindow_unfocuas.isShowing()) {
                            if (mContainsPhone.isEmpty()) {
                                dropdownImageView.setVisibility(View.VISIBLE);
                                accountAdapter_unfocus.setData(mContainsPhone);
                                accountAdapter_unfocus.notifyDataSetChanged();
                                popupWindow_unfocuas.dismiss();
                                //bug 9360 为什么账号列表dismiss时会把 dropdownImageView show出来
                                accounts = UserCache.getUserList(LoginActivity.this);
                                if (accounts == null || accounts.isEmpty()) {
                                    dropdownGone();
                                }
                                return;
                            }
                            if (accountAdapter_unfocus != null) {
                                accountAdapter_unfocus.setData(mContainsPhone);
                                accountAdapter_unfocus.notifyDataSetChanged();
                            }
                        }
                        if (mContainsPhone != null && !mContainsPhone.isEmpty()) {
                            showOrHideWindowPopup(mContainsPhone);
                        }
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userName_et.getText().toString().trim().isEmpty()) {
                    if (popupWindow_unfocuas != null) {
                        popupWindow_unfocuas.dismiss();
                    }
                }
                if (popupWindow_unfocuas != null) {
                    popupWindow_unfocuas.setHeight(getPopupWindowHeight(accountAdapter_unfocus, mMAccountListView_unfocus));
                }

            }
        });

    }

    private void dropdownGone() {
        dropdownImageView.setVisibility(View.GONE);
        int padding = getResources().getDimensionPixelSize(R.dimen.padding_x4);
        userName_et.setPaddingLeft(padding);
        userName_et.setPaddingRight(padding);
    }

    private void initListener() {
        login_btn.setOnClickListener(this);
        forget_password_tv.setOnClickListener(this);
        register_tv.setOnClickListener(this);

        //设置第三方按钮的点击事件
        mWechat_login.setOnClickListener(this);
        mQQ_login.setOnClickListener(this);
        mSina_login.setOnClickListener(this);
    }

    @Override
    protected void onCancelDialog() {
        dismissDialog();
    }

    @Override
    public void onClick(View v) {
        LogUtil.d(TAG, "onClick()-" + v);
        switch (v.getId()) {
            case R.id.dropdownImageView: {
                try {
                    //修复7621 Monkey 随机测试, app crash
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isFocusable = true;
                showOrHideWindowPopup(accounts);
                break;
            }
            case R.id.forget_password_tv:
                //忘记密码
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Login_ForgotPsd), null);
                startActivity(new Intent(mContext, PasswordForgotActivity.class));
                break;
            case R.id.register_tv:
                //注册
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Login_Register), null);
                if (PhoneUtil.isCN(mAppContext)) {
                    startActivityForResult(new Intent(mContext, RegisterActivity.class), 0);
                } else {
                    startActivityForResult(new Intent(mContext, RegisterEmailActivity.class), 0);
                }
                break;
            case R.id.eyeNewPwdImageView: {
                showPassword(password_et.getTransformationMethod() instanceof PasswordTransformationMethod);
                break;
            }
            case R.id.login_btn:
                //登录
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Login_login), null);

                if (!NetUtil.isNetworkEnable(mContext)) {
                    ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                    return;
                }
                if (nameInputFromUser) {
                    userName = userName_et.getText().toString();
                }
                password = password_et.getText().toString();
                if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(password)) {
                    ToastUtil.showToast(getResources().getString(R.string.login_empty_error), ToastType.NULL, ToastType.SHORT);
                    return;
                }

                //如果userName为list列表中的值（包含*），则取其真实值
                String account = getContainUserName(userName);
                if (!StringUtil.isEmpty(account)) {
                    userName = account;
                }
                //账号不区分大小写，统一使用小写
                try {
                    //bug10494:如果这里抛了异常，大小写登录可能会缓存两个同一个账号
                    userName = userName.toLowerCase();//
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //LogUtil.d(TAG, "Login Password: " + password);
                UserCache.savePassword(mContext, userName, password);
                showDialogNow(this);
                // MinaSocket.resetServerHost();
                HostManager.resetCurrentServerHost();
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.needSaveLastLoginUserName = true;
                loginConfig.startApp = false;
                String md5Password = MD5.encryptMD5(password);
                login(userName, md5Password, loginConfig);
//                mNetChangeHelper.doCheck(this);
//                String md5Password = MD5.encryptMD5(password);
//                LogUtil.d(TAG, "onClick()-userName:" + userName + ",md5Password:" + md5Password);
//                unregisterEvent(LoginActivity.this);
//                try {
//                    EventBus.getDefault().register(LoginActivity.this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                LoadUtil.noticeLogin(mAppContext, userName, md5Password, false);
                break;
            case R.id.wechat_login:
                //weixin
                if (AppTool.isWechatInstalled(mAppContext)) {
                    registerType = RegisterType.WEIXIN_USER;
                    userThirdAuth.authLogin(SHARE_MEDIA.WEIXIN);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_wechat);
                }
                break;
            case R.id.qq_login:
                //qq
                if (AppTool.isQQInstalled(mAppContext)) {
                    registerType = RegisterType.QQ_USER;
                    userThirdAuth.authLogin(SHARE_MEDIA.QQ);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_qq);
                }
                break;
            case R.id.sina_login:
                //sina
                if (AppTool.isSinaInstalled(mAppContext)) {
                    registerType = RegisterType.SINA_USER;
                    userThirdAuth.authLogin(SHARE_MEDIA.SINA);
                } else {
                    ToastUtil.showToast(R.string.auth_login_not_install_sina);
                }
                break;
        }
    }

    private void login(String userName, String md5Password, LoginConfig config) {
        isLoginFinish = false;
        mNetChangeHelper.doCheck(this);
        LogUtil.d(TAG, "login()-userName:" + userName + ",md5Password:" + md5Password);
        unregisterEvent(LoginActivity.this);
        try {
            EventBus.getDefault().register(LoginActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoadUtil.noticeLogin(mAppContext, userName, md5Password, config);
    }

    private void showOrHideWindowPopup(List<String> accountPhones) {
        if (popupWindow == null || popupWindow_unfocuas == null) {
            loginAccountListView = View.inflate(this, R.layout.login_account_list, null);
            if (popupWindow == null && isFocusable) {
                ListView accountListView = (ListView) loginAccountListView.findViewById(R.id.accountListView);
                accountAdapter = new LoginAccountAdapter(this, accountPhones);
                accountAdapter.setOnDeleteListener(new OnAccountDeleteListener());
                accountListView.setAdapter(accountAdapter);
                accountListView.setOnItemClickListener(this);
                popupWindow = new PopupWindow(loginAccountListView, userName_et.getWidth(), getResources().getDimensionPixelSize(R.dimen.login_popup_height));
                PopupWindowUtil.initPopup(popupWindow, mContext.getResources().getDrawable(R.color.tran), 0, true);
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        dropdownImageView.clearAnimation();
                        if (dropdownImageView.getVisibility() == View.VISIBLE) {
                            dropdownImageView.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.rotate_back_180_anim));
                        }
                    }
                });
            } else if (popupWindow_unfocuas == null && !isFocusable) {
                mMAccountListView_unfocus = (ListView) loginAccountListView.findViewById(R.id.accountListView);
                accountAdapter_unfocus = new LoginAccountAdapter(this, accountPhones, false);
                accountAdapter_unfocus.setOnDeleteListener(new OnAccountDeleteListener());
                mMAccountListView_unfocus.setAdapter(accountAdapter_unfocus);
                // mMAccountListView_unfocus.setOnItemClickListener(this);
                popupWindow_unfocuas = new PopupWindow(loginAccountListView, userName_et.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow_unfocuas.setContentView(loginAccountListView);
                popupWindow_unfocuas.setWidth(userName_et.getWidth());
                PopupWindowUtil.initPopup(popupWindow_unfocuas, mContext.getResources().getDrawable(R.color.tran), 0, false);
                popupWindow_unfocuas.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                popupWindow_unfocuas.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
//                popupWindow_unfocuas.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                    @Override
//                    public void onDismiss() {
//                        dropdownImageView.clearAnimation();
//                        if (dropdownImageView.getVisibility() == View.VISIBLE) {
//                            dropdownImageView.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.rotate_back_180_anim));
//                        }
//                    }
//                });
            }

        }
        if (isFocusable) {
            if (accountAdapter != null) {
                accountAdapter.notifyDataSetChanged();
            }

            int[] location = new int[2];
            userName_et.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (popupWindow_unfocuas != null) {
                popupWindow_unfocuas.dismiss();
            }

            popupWindow.showAtLocation(loginAccountListView, Gravity.NO_GRAVITY, x, y
                    + userName_et.getHeight());
            dropdownImageView.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.rotate180_anim));
        } else {
            if (accountAdapter_unfocus != null) {
                //因为accountPhones在edittext每次输入变化时都会重新创建对象，所以adapter每次每次都要重新设置
                accountAdapter_unfocus.setData(accountPhones);
                accountAdapter_unfocus.notifyDataSetChanged();
            }

            int[] location = new int[2];
            userName_et.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            mPopupWindowTop = y + userName_et.getHeight();
            if (popupWindow != null) {
                popupWindow.dismiss();
            }

            //动态设置popupwindow的高度
            popupWindow_unfocuas.setHeight(getPopupWindowHeight(accountAdapter_unfocus, mMAccountListView_unfocus));
            int ph = popupWindow_unfocuas.getHeight();
            /*
                    *更新popupWindow的位置和大小，如果宽度width和高度height传的都是-1，那么只会更新popupWindow的位置
                    *同理可以说明可以单独的设置width和height来更新popupWindow的宽度“或”高度
                    * @param x popupWindow 左上角新的x坐标
                    * @param y popupWindow新的坐标
                    * @param width popupWindow的新的宽度，如果设置为-1的话就不会更新popupWindow的宽度
                    * @param height popWindow的新的高度，如果设置为-1的话就不会更新popupWindow的高度
                    * @param force reposition the window even if the specified position
                    * already seems to correspond to the LayoutParams 是否强制性更新，如果设置成true的话
                    * 就强制调用windowManager的updateViewLayout方法，设置成false，就会根据其余的四个参数来判断是否进行更新
                    * update(int x, int y, int width, int height, boolean force)
            */
            if (popupWindow_unfocuas.isShowing()) {
                popupWindow_unfocuas.update(x, y + userName_et.getHeight(), -1, getPopupWindowHeight(accountAdapter_unfocus, mMAccountListView_unfocus), false);
            } else {
                popupWindow_unfocuas.showAtLocation(loginAccountListView, Gravity.NO_GRAVITY, x, y
                        + userName_et.getHeight());
            }
        }


    }

    private void showPassword(boolean show) {
        int selectionStart = password_et.getSelectionStart();
        if (show) {
//        if (password_et.getTransformationMethod() instanceof PasswordTransformationMethod) {
            password_et.setTransformationMethod(null);
            eyeNewPwdImageView.setImageResource(R.drawable.password_hide);
            // mShowPasswordImageView.setVisibility(View.VISIBLE);
            // mHidePasswordImageView.setVisibility(View.GONE);
        } else {
            password_et.setTransformationMethod(PasswordTransformationMethod.getInstance());
            eyeNewPwdImageView.setImageResource(R.drawable.password_show);
            // mHidePasswordImageView.setVisibility(View.VISIBLE);
            // mShowPasswordImageView.setVisibility(View.GONE);
        }
        if (selectionStart > 0) {
            try {
                password_et.setSelection(selectionStart);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getContainUserName(String userName) {
        String contain = null;
        if (accounts != null && !accounts.isEmpty()) {
            for (String name : accounts) {
                if (name != null && StringUtil.hideUserNameMiddleWord(name).equals(userName)) {
                    LogUtil.d(TAG, "getContainUserName userName = " + userName);
                    contain = name;
                    break;
                }
            }
        }
        return contain;
    }

    public final void onEventMainThread(MainEvent event) {
        LogUtil.d(TAG, "onEventMainThread()-event:" + event);
        //撤销EventBus
        unregisterEvent(LoginActivity.this);
        if (event != null) {
            isLoginFinish = true;
            String loadImportantGatewayUid = event.getLoadImportantDataUid();
            if (!TextUtils.isEmpty(loadImportantGatewayUid)) {
                //读完网关重要数据
                //由于MainActivity也监听了同样的event，当Login365post这个event时LoginActivity和MainActivity都会接受到相同的event
                saveUserAccount();
                processLoadImportantData(loadImportantGatewayUid);
                // loadImportantGatewayUid为null时候没有保存用户数据
            } else if (event.getLogin365Event() != null) {
                //整个登录操作结束，回调此结果
                Login365Event login365Event = event.getLogin365Event();
                loginFinish(login365Event.getGateways(), login365Event.getCocos(), login365Event.getResult(), login365Event.getServerLoginResult());
            }
        }
    }

    /**
     * 登录成功后保存用户数据
     */
    private void saveUserAccount() {
        List<String> accounts = UserCache.getUserList(LoginActivity.this);
        if (!accounts.contains(userName) && registerType == RegisterType.REGISTER_USER) {
            //解决bug：10494，双重判断，如果已经缓存了就不再缓存（不论是大小写）
            for (String account : accounts) {
                if (account.equalsIgnoreCase(userName)) {
                    return;
                }
            }
            try {
                accounts.add(userName.toLowerCase());
                UserCache.setUserList(LoginActivity.this, accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        LogUtil.d(TAG, " saveUserAccount():" + UserCache.getUserList(LoginActivity.this).toString());
    }

    /**
     * 读完主机重要数据后的处理
     *
     * @param uid 主机uid
     */
    private void processLoadImportantData(String uid) {
        isRunning = false;
//        try {
//            EventBus.getDefault().post(new MainEvent(uid));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        cancelCheckNetChange();
        dismissDialog();
//        if (login != null) {
//            login.removeListener(this);
//        }
        Intent toMainIntent = new Intent(this, MainActivity.class);
        toMainIntent.putExtra(IntentKey.EVENT_LOAD_IMPORTANT_DATA_RESULT, uid);
        toMainIntent.putExtra(IntentKey.TO_MAIN_SOURCE, LoginActivity.class.getSimpleName());
        startActivity(toMainIntent);
        finish();
    }

    //    @Override
    public void loginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
        LogUtil.i(TAG, "onLoginFinish()-gateways:" + gateways + ",cocos:" + cocos + ",result:" + result + ",serverLoginResult:" + serverLoginResult);
        long castTime = System.currentTimeMillis() - startTime;
        LogUtil.i(TAG, "onLoginFinish()-cast " + castTime + "ms");
        dismissDialog();
        cancelCheckNetChange();
        String userId = UserCache.getCurrentUserId(mContext);
        if (!NetUtil.isNetworkEnable(mAppContext) || result == ErrorCode.NET_DISCONNECT) {
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            UserManage.getInstance(mAppContext).exitAccount(userId);
            return;
        }

        //int bottomType = BottomTabType.TWO_BOTTOM_TAB;
        if (serverLoginResult == ErrorCode.USER_PASSWORD_ERROR || result == ErrorCode.USER_PASSWORD_ERROR) {
            LogUtil.e(TAG, "onLoginFinish()-UserName or password error.");
            ToastUtil.toastError(ErrorCode.USER_PASSWORD_ERROR);
            UserManage.getInstance(mAppContext).exitAccount(userId);
            return;
        } else if (result == ErrorCode.SUCCESS || serverLoginResult == ErrorCode.SUCCESS) {
//            List<String> accounts = UserCache.getUserList(LoginActivity.this);
//            if (!accounts.contains(userName) && registerType == RegisterType.REGISTER_USER) {
//                accounts.add(userName);
//                UserCache.setUserList(LoginActivity.this, accounts);
//                LogUtil.d(TAG, " onLoginFinish:***********************************" + UserCache.getUserList(LoginActivity.this).toString());
//            }
            saveUserAccount();
        } else if (result == ErrorCode.USER_NOT_BIND_HUB_BY_APP) {
            SdkCompat.setLoginOk(mAppContext, userName, password);
        } else {
            String wifiSSID = WifiUtil.getWifiSSID(mContext);
            if (!StringUtil.isEmpty(wifiSSID) && wifiSSID.indexOf(ApConstant.AP_DEFAULT_SSID) >= 0 || wifiSSID.indexOf(ApConstant.AP_OTHER_DEFAULT_SSID) >= 0) {
                LogUtil.e(TAG, "onLoginFinish()-User connected coco's ap.");
                ToastUtil.showToast(R.string.CONNECT_COCO_AP);
            } else {
                ToastUtil.showToast(R.string.LOGIN_FAIL);
            }
            UserManage.getInstance(mAppContext).exitAccount(userId);
            return;
        }

        currentMainUid = UserCache.getCurrentMainUid(mContext);
        Intent toMainIntent = new Intent(this, MainActivity.class);
        Login365Event login365Event = new Login365Event(gateways, cocos, result, serverLoginResult);
        toMainIntent.putExtra(IntentKey.EVENT_LOGIN_RESULT, login365Event);
        startActivity(toMainIntent);
//        if (login != null) {
//            login.removeListener(this);
//        }
        finish();
    }

    @Override
    public void onCancelClick(View view) {
        LogUtil.w(TAG, "onCancelClick()");
//        if (login != null) {
//            login.cancel();
//        }
        if (!isLoginFinish) {
            LoadUtil.noticeCancelLogin(mAppContext);
            try {
                unregisterEvent(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cancelCheckNetChange();
            String userId = UserCache.getCurrentUserId(mAppContext);
            UserManage.getInstance(mAppContext).exitAccount(userId);
        } else {
            MyLogger.kLog().w("已经登录结束，不处理取消登录操作。");
        }

    }

    @Override
    public void onNetChanged() {
    }

    @Override
    public void onLeftClick(View v) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Login_Back), null);

        String userId = UserCache.getCurrentUserId(mContext);
        UserManage.getInstance(mAppContext).exitAccount(userId);

//        if (loginEntry != null && (loginEntry.equals(Constant.COCO) || loginEntry.equals(Constant.ViHome))) {
        finish();
//        } else {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed()");
        EventBus.getDefault().post(new MainEvent(MainActionType.EXIT_APP));
        //通知vicenterService取消登录
        LoadUtil.noticeCancelLogin(getApplicationContext());
        String userId = UserCache.getCurrentUserId(mAppContext);
        UserManage.getInstance(mAppContext).exitAccount(userId);
        cancelCheckNetChange();
        stopService(new Intent(this, ViCenterService.class));
        stopService(new Intent(this, InfoPushService.class));
        if (Conf.COLLECTION_LOG) {
            LogcatHelper.getInstance(mAppContext).stop();
        }
        super.onBackPressed();
        System.exit(0);
    }

    private void cancelCheckNetChange() {
        if (mNetChangeHelper != null) {
            mNetChangeHelper.cancelCheck(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        userName = accounts.get(position);
        LogUtil.d(TAG, "onItemClick()-accounts:" + accounts + ",userName:" + userName);
        userName_et.setText(StringUtil.hideUserNameMiddleWord(userName));
        userName_et.requestFocus();
        userName_et.setSelection(userName_et.length());
//        password = UserCache.getPassword(this, userName);
//        password_et.setIntactText(password);
        nameInputFromUser = false;
//        pwdInputFromUser = false;
        popupWindow.dismiss();
    }

    private class OnAccountDeleteListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            final String accountName = (String) v.getTag();
            String lastLoginUserName = UserCache.getLastLoginUserName(LoginActivity.this);
            accounts.remove(accountName);
//            new Thread() {
//                @Override
//                public void run() {
//                    // GatewayDao gatewayDao = new GatewayDao();
//                    List<String> mainUids = UserCache.getMainUids(LoginActivity.this, accountName);
//                    for (String uid : mainUids) {
//                        GatewayTool.clearGateway(mAppContext, uid);
//                        // gatewayDao.delGateway(mAppContext, uid);
//                        // UpdateTimeCache.resetUpdateTime(mAppContext, uid);
//                    }
//                    AccountDao accountDao = new AccountDao();
//                    Account account = accountDao.selAccountByUserName(accountName);
//                    if (account != null) {
//                        String userId = account.getUserId();
//                        if (!StringUtil.isEmpty(userId)) {
//                            List<String> uids = new GatewayServerDao().selUids(userId);
//                            if (uids != null && !uids.isEmpty()) {
//                                for (String uid : uids) {
//                                    GatewayTool.clearGateway(mAppContext, uid);
//                                }
//                            }
//                            UpdateTimeCache.resetUpdateTime(mAppContext, userId);
//                        }
//                        accountDao.delAccountByUserName(accountName);
//                    }
//                    UserCache.removeUser(mAppContext, accountName);
//                }
//            }.start();

//            if (accountAdapter_unfocus != null && !isFocusable) {
//                accountAdapter_unfocus.notifyDataSetChanged();
//            }
            if (!accounts.isEmpty()) {
//                if (account.equals(userName)) {
//                    userName = accounts.get(0);
//                    password = UserCache.getPassword(LoginActivity.this, userName);
//                }
                if (accountAdapter != null) {
                    accountAdapter.setData(accounts);
                    accountAdapter.notifyDataSetChanged();
                }
            } else {
//                userName = "";
//                password = "";
                dropdownGone();
                popupWindow.dismiss();
            }
//            userName_et.setText(StringUtil.hideUserNameMiddleWord(userName));
//            userName_et.requestFocus();
//            userName_et.setSelection(userName_et.length());
//            password_et.setText(password);
//            inputFromUser = false;
//            UserCache.saveUser(LoginActivity.this, userName, MD5.encryptMD5(password));
//            UserCache.savePassword(LoginActivity.this, userName, password);
            UserCache.setUserList(LoginActivity.this, accounts);
            if (accountName.equals(userName)) {
                if (userName.equals(lastLoginUserName)) {
                    UserCache.setLastLoginUserName(LoginActivity.this, null);
                }
                userName_et.setText("");
                popupWindow.setFocusable(true);
            }
        }
    }

    @Override
    protected void onStop() {
        isRunning = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * popupwindow中的item点击时调用
     *
     * @param v
     * @param position
     */
    public void onPopupWindowItemClick(View v, int position) {
        if (popupWindow_unfocuas != null) {
            popupWindow_unfocuas.dismiss();
        }
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        String phone = (String) v.getTag(R.id.accountTextView);
        nameInputFormAccouts = true;
        userName = phone;
        userName_et.setText(phone);
        userName_et.requestFocus();
        userName_et.setSelection(phone.length());
        //设定userName_et的内容不是软键盘输入
        nameInputFromUser = false;

    }

    /**
     * 动态计算popupwindow的高度
     *
     * @param adapter
     * @param listView
     * @return
     */
    public int getPopupWindowHeight(Adapter adapter, ListView listView) {
        int totalHeight = 0;
        for (int i = 0; i < mContainsPhone.size(); i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(0, 0);
            totalHeight += item.getMeasuredHeight();
        }
        //限定定最大值，避免popupwindow的高度超高最大值
        if (totalHeight > (mScreenHeight - mPopupWindowTop)) {
            totalHeight = mScreenHeight - mPopupWindowTop;
        }
        return totalHeight;
    }

    private void initThirdRegister() {
        thirdRegister = new ThirdRegister(mContext) {
            @Override
            public void onRegisterResult(int result) {
                result(result);
            }
        };
    }

    private void initThirdVerify() {
        thirdVerify = new ThirdVerify(mContext) {
            @Override
            public void onVerifyResult(int result, String userId, String password) {
                if (result == ErrorCode.USER_NO_AUTHORIZE) {
                    thirdRegister.startRegister(thirdAccount.getThirdUserName(), thirdAccount.getThirdId(), thirdAccount.getToken(), registerType, thirdAccount.getFile());
                } else if (result == ErrorCode.SUCCESS) {
//                    doLoginInfo(userId, password);
                    LoginConfig loginConfig = new LoginConfig();
                    loginConfig.needSaveLastLoginUserName = false;
                    loginConfig.startApp = false;
                    login(userId, password, loginConfig);
                } else {
                    dismissDialog();
                    ToastUtil.toastError(result);
                }
            }

        };
    }

//    private void doLoginInfo(String userId, String password) {
//        if (mLogin365 == null) {
//            mLogin365 = new Login365(mContext);
//            mLogin365.setOnLogin365Listener(this);
//        }
//        mLogin365.setNeedSaveLastLoginUserName(false);
//        mLogin365.login(userId, password);
//    }

//    /**
//     * @param gateways          当前用户下所拥有的所有vicenter，不保证登录成功或读表成功。
//     * @param cocos             服务器返回的coco，不保证能读表成功。
//     * @param result            如果成功，说明读取数据成功，否则失败。
//     * @param serverLoginResult 登录服务器接口成功，不保证读取coco、vicenter数据成功。
//     * @hide
//     */
//    @Override
//    public final void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
//        result(result);
//    }

    private void result(int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            ToastUtil.toastError(result);
        }
    }

    @Override
    public void onComplete(ThirdAccount thirdAccount) {
        LogUtil.d(TAG, "onComplete-thirdAccount:" + thirdAccount);
        this.thirdAccount = thirdAccount;
        String thirdId = thirdAccount.getThirdId();
        if (!TextUtils.isEmpty(thirdId)) {
            showDialogNow(this, getString(R.string.authing));
            thirdVerify.startVerify(thirdId, registerType);
        } else {
            ToastUtil.showToast(R.string.auth_fail);
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onCancel() {

    }
}
