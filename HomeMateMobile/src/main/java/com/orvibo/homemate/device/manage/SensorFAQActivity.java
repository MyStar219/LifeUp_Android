package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.UrlManager;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;

/**
 * Created by wuliquan on 2015/07/25
 */
public class SensorFAQActivity extends BaseActivity {
    private Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_faq);
        device = (Device) getIntent().getSerializableExtra("device");
        WebView webView = (WebView) findViewById(R.id.wv);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        String url = "";

        if(device!=null) {
            if (PhoneUtil.isCN(mAppContext)) {
                switch (device.getModel()) {
                    case ModelID.HEIMAN_HS1CA:
                        url = UrlManager.HS1CA_URL_CN;
                        break;
                    case ModelID.HEIMAN_HS1CG:
                        url = UrlManager.HS1CG_URL_CN;
                        break;
                    case ModelID.HEIMAN_HS1EB:
                        url = UrlManager.HS1EB_URL_CN;
                        break;
                    case ModelID.HEIMAN_HS1HT:
                        url = UrlManager.HS1HT_URL_CN;
                        break;
                    case ModelID.HEIMAN_HS1SA:
                        url = UrlManager.HS1SA_URL_CN;
                        break;
                    case ModelID.HEIMAN_HS1WL:
                        url = UrlManager.HS1WL_URL_CN;
                        break;
                }
            } else {
                switch (device.getModel()) {
                    case ModelID.HEIMAN_HS1CA:
                        url = UrlManager.HS1CA_URL_EN;
                        break;
                    case ModelID.HEIMAN_HS1CG:
                        url = UrlManager.HS1CG_URL_EN;
                        break;
                    case ModelID.HEIMAN_HS1EB:
                        url = UrlManager.HS1EB_URL_EN;
                        break;
                    case ModelID.HEIMAN_HS1HT:
                        url = UrlManager.HS1HT_URL_EN;
                        break;
                    case ModelID.HEIMAN_HS1SA:
                        url = UrlManager.HS1SA_URL_EN;
                        break;
                    case ModelID.HEIMAN_HS1WL:
                        url = UrlManager.HS1WL_URL_EN;
                        break;
                }
            }
        }
        if(!StringUtil.isEmpty(url)) {
            webView.loadUrl(url);
        }
    }
}
