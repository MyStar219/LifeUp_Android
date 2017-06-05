package com.orvibo.homemate.device;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.PhoneUtil;

/**
 * Created by allen on 2016/4/15.
 */
public class CocoDeviceOfflineTipsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coco_device_offline_tips);
        WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setTextSize(WebSettings.TextSize.LARGEST);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
       // webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //第一个方法设置webview推荐使用的窗口，设置为true。
        webSettings.setUseWideViewPort(true);
        //第二个方法是设置webview加载的页面的模式，也设置为true。
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        String url;
        if (PhoneUtil.isCN(this)) {
            //http://192.168.2.20/html/index.html
           // url = "http://192.168.2.20/html/next0.html";
            url = "http://homemate.orvibo.com/html/index.html";
        } else {
            //url = "http://192.168.2.20/html-en/next2.html";
            url = "http://homemate.orvibo.com/html-en/index.html";
        }
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                if (url.contains("status=complete")) {
                    finish();
                }
                return true;
            }
        });
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }
}
