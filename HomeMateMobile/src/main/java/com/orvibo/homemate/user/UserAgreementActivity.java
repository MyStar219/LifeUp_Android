package com.orvibo.homemate.user;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.util.PhoneUtil;
import com.tencent.stat.StatService;

/**
 * 用户协议
 * Created by huangqiyao on 2015/5/22.
 */
public class UserAgreementActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agreement);
        WebView userGuide_wv = (WebView) findViewById(R.id.userGuide_wv);
//        userGuide_wv.setBackgroundColor(0);
//        try {
//            userGuide_wv.getBackground().setAlpha(0);
//        } catch (Exception e) {
//        }
        // userGuide_wv.getSettings().setUseWideViewPort(true);
        // userGuide_wv.getSettings().setLoadWithOverviewMode(true);

        String  UserAgreementUrlCN = mAppContext.getResources().getString(R.string.userAgreementUrlCN);
        String  UserAgreementUrlEN = mAppContext.getResources().getString(R.string.userAgreementUrlEN);

        if (PhoneUtil.isCN(mContext)) {
            userGuide_wv.loadUrl(UserAgreementUrlCN);
        } else {
            userGuide_wv.loadUrl(UserAgreementUrlEN);
        }
        // Html.fromHtml(source);
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_Register_Phone_Agreement_Back), null);
        super.onBackPressed();
    }
}
