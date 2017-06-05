package com.orvibo.homemate.common.launch;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnLogin365Listener;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.common.adapter.ViewPagerAdapter;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.RegisterType;
import com.orvibo.homemate.model.ThirdRegister;
import com.orvibo.homemate.model.ThirdVerify;
import com.orvibo.homemate.model.login.LoginX;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.sharedPreferences.AppCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.user.UserThirdAuth;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Allen on 2015/7/11.
 */
public class GuideActivity extends BaseActivity implements
//        View.OnClickListener,
        UserThirdAuth.OnAuthListener, OnLogin365Listener {
    private static final String TAG = GuideActivity.class.getName();
    private ViewPager guideViewPager;
    private ImageView authIcon;
    private TextView authName, otherLogin;
    private TypedArray guideImg, guideIndexImg;
    private String[] guideTitle, guideContent;
    private boolean isLogin = false;
    private int registerType = RegisterType.REGISTER_USER;

    private ThirdVerify thirdVerify;
    private UserThirdAuth userThirdAuth;
    private ThirdRegister thirdRegister;
    private ThirdAccount thirdAccount;
    //    private Login365 mLogin365;
    private UMShareAPI shareAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_activity);
        guideViewPager = (ViewPager) findViewById(R.id.guideViewPager);

        authIcon = (ImageView) findViewById(R.id.authIcon);
        authName = (TextView) findViewById(R.id.authName);
        otherLogin = (TextView) findViewById(R.id.otherLogin);
        otherLogin.setOnClickListener(this);
        guideImg = getResources().obtainTypedArray(R.array.guide_img);
        guideTitle = getResources().getStringArray(R.array.guide_title);
        guideContent = getResources().getStringArray(R.array.guide_content);
        guideIndexImg = getResources().obtainTypedArray(R.array.guide_index_img);
        findViewById(R.id.getStart).setOnClickListener(this);
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < guideImg.length(); i++) {
            View view = View.inflate(this, R.layout.guide_item, null);
            //   ImageView guideImageView = (ImageView) view.findViewById(R.id.guideImageView);
            LinearLayout mBg_guide = (LinearLayout) view.findViewById(R.id.bg_guide);
            TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            TextView contentTextView = (TextView) view.findViewById(R.id.contentTextView);
            Drawable drawable = guideImg.getDrawable(i);
            mBg_guide.setBackgroundDrawable(drawable);
            String title = guideTitle[i];
            titleTextView.setText(title);
            String content = guideContent[i];
            contentTextView.setText(content);
            if (!PhoneUtil.isCN(mAppContext)) {
                titleTextView.setTextSize(getResources().getDimension(R.dimen.text_big) / getResources().getDisplayMetrics().scaledDensity);
            }
            ImageView indexImageView = (ImageView) view.findViewById(R.id.indexImageView);
            Drawable indexDrawable = guideIndexImg.getDrawable(i);
            indexImageView.setImageDrawable(indexDrawable);
            views.add(view);
        }
        guideViewPager.setAdapter(new ViewPagerAdapter(views));
        setAuth();
        initThirdVerify();
        initThirdRegister();
        AppCache.saveAppVersion(mAppContext);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareAPI.onActivityResult(requestCode, resultCode, data);
    }

    private void setAuth() {
        shareAPI = UMShareAPI.get(mAppContext);
        userThirdAuth = new UserThirdAuth(this, shareAPI);
        userThirdAuth.setOnAuthListener(this);
        isLogin = getIntent().getBooleanExtra(Constant.IS_LOGIN, false);
        if (!isOverseasVersion) {
            if (isLogin) {
                authIcon.setVisibility(View.GONE);
                authName.setText(R.string.open_now);
                otherLogin.setVisibility(View.INVISIBLE);
            } else if (AppTool.isWechatInstalled(mAppContext)) {
                registerType = RegisterType.WEIXIN_USER;
                authIcon.setImageResource(R.drawable.pic_guide_wechat_normal);
                authName.setText(R.string.auth_login_wechat);
            } else if (AppTool.isQQInstalled(mAppContext)) {
                registerType = RegisterType.QQ_USER;
                authIcon.setImageResource(R.drawable.pic_guide_qq_normal);
                authName.setText(R.string.auth_login_qq);
            } else if (AppTool.isSinaInstalled(mAppContext)) {
                registerType = RegisterType.SINA_USER;
                authIcon.setImageResource(R.drawable.pic_guide_sina_normal);
                authName.setText(R.string.auth_login_sina);
            } else {
                authIcon.setVisibility(View.GONE);
                authName.setText(R.string.login_now);
                otherLogin.setVisibility(View.INVISIBLE);
            }
        } else {
            authIcon.setVisibility(View.GONE);
            authName.setText(R.string.open_now);
            otherLogin.setVisibility(View.INVISIBLE);
        }
    }

    private void initThirdRegister() {
        thirdRegister = new ThirdRegister(mContext) {
            @Override
            public void onRegisterResult(int result) {
                dismissDialog();
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
                    doLoginInfo(userId, password);
                } else {
                    dismissDialog();
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void doLoginInfo(String userId, String password) {
        LoginX.getInstance(mAppContext).setOnLogin365Listener(this);
        LoginX.getInstance(mAppContext).setNeedSaveLastLoginUserName(false);
        LoginX.getInstance(mAppContext).login(userId, password);
    }

    /**
     * @param gateways          当前用户下所拥有的所有vicenter，不保证登录成功或读表成功。
     * @param cocos             服务器返回的coco，不保证能读表成功。
     * @param result            如果成功，说明读取数据成功，否则失败。
     * @param serverLoginResult 登录服务器接口成功，不保证读取coco、vicenter数据成功。
     * @hide
     */
    @Override
    public final void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
        result(result);
    }

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getStart:
                if (isLogin) {
                    toMainActivity();
                } else if (registerType == RegisterType.WEIXIN_USER && !isOverseasVersion) {
                    //点击授权，并且获得用户信息
                    userThirdAuth.authLogin(SHARE_MEDIA.WEIXIN);
                } else if (registerType == RegisterType.QQ_USER && !isOverseasVersion) {
                    userThirdAuth.authLogin(SHARE_MEDIA.QQ);
                } else if (registerType == RegisterType.SINA_USER && !isOverseasVersion) {
                    userThirdAuth.authLogin(SHARE_MEDIA.SINA);
                } else {
                    toLoginActivity();
                }
                break;
            case R.id.otherLogin:
                toLoginActivity();
                break;
        }
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, Constant.GuideActivity);
        intent.putExtra("launch", true);
        startActivity(intent);
        finish();
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentKey.START_APP, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onComplete(ThirdAccount thirdAccount) {
        LogUtil.d(TAG, "onComplete-thirdAccount:" + thirdAccount);
        this.thirdAccount = thirdAccount;
        String thirdId = thirdAccount.getThirdId();
        if (!TextUtils.isEmpty(thirdId)) {
            showDialogNow(null, getString(R.string.authing));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginX.getInstance(mAppContext).removeListener(this);
    }
}
