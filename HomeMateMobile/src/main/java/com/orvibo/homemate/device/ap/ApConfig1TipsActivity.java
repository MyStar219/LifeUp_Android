package com.orvibo.homemate.device.ap;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.ap.ApWifiHelper;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;

import java.util.Locale;

/**
 * Created by Allen on 2015/8/26.
 */
public class ApConfig1TipsActivity extends BaseActivity {
    private static final String TAG = ApConfig1TipsActivity.class.getName();
    private String oldSSID;
    private int oldNetworkId = -1;
    private String defaultSSid = ApConstant.AP_OTHER_DEFAULT_SSID;
    int typeId;
    private TextView tipTextView1;
    private ImageView imageView;
    private String productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_config1_tips_activity);
        typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);
        productName = getIntent().getStringExtra(IntentKey.PRODUCTNAME);
        init();
        NavigationCocoBar navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        imageView = (ImageView) findViewById(R.id.ImageView);
        if (productName != null) {
            navigationCocoBar.setCenterText(getString(R.string.add) + productName);
        } else {
            navigationCocoBar.setCenterText(getString(R.string.add) + getString(typeId));
        }
        if (typeId == R.string.device_add_coco) {
            defaultSSid = ApConstant.AP_DEFAULT_SSID;
            navigationCocoBar.setCenterText(getString(R.string.ap_config_title));
        } else {
            tipTextView1.setText(getString(R.string.ap_config_tips_wifi1));
            Locale locale = getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            if (language.endsWith("zh")) {
            imageView.setImageResource(R.drawable.bg_wifi);}
            else{
                imageView.setImageResource(R.drawable.bg_wifi_en);
            }
        }
        if (productName != null) {
            navigationCocoBar.setCenterText(getString(R.string.add) + productName);
        } else {
            navigationCocoBar.setCenterText(getString(R.string.add) + getString(typeId));
        }
    }

    private void init() {
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        Intent intent = getIntent();
        oldSSID = intent.getStringExtra(ApConstant.OLD_SSID);
        oldNetworkId = intent.getIntExtra(ApConstant.OLD_NETWORK_ID, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApWifiHelper apWifiHelper = new ApWifiHelper(this);
//        String ssid = apWifiHelper.getSSID();
        String serverIp = apWifiHelper.getDhcpIp();
        //先保存原来的SSID和NetworkID，配置完毕再重新连接
        if (apWifiHelper.isAPConnected(defaultSSid)) {
            Intent intent = new Intent(ApConfig1TipsActivity.this, ApConfig2Activity.class);
            intent.putExtra(ApConstant.IP, serverIp);
            intent.putExtra(ApConstant.OLD_SSID, oldSSID);
            intent.putExtra(ApConstant.OLD_NETWORK_ID, oldNetworkId);
            intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
            intent.putExtra(IntentKey.PRODUCTNAME, productName);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_ConnectionGuide_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_ConnectionGuide_ToConnect), null);
                try {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
