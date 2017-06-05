package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.util.PhoneUtil;

/**
 * Created by huangqiyao on 2015/11/25.
 */
public class CocoFAQActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coco_faq);
        WebView webView = (WebView) findViewById(R.id.wv);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        String url;
        if (PhoneUtil.isCN(mAppContext)) {
            url = "http://www.orvibo.com/software/COCOFAQ_CN.html";
        } else {
            url = "http://www.orvibo.com/software/COCOFAQ_EN.html";
        }
        webView.loadUrl(url);
//      webView.loadUrl("file:///android_asset/COCOFAQ_cn.html");
    }
}
