package com.orvibo.homemate.device.ys;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orvibo.homemate.ap.ApWifiHelper;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.WifiCache;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

/**
 * Created by allen on 2015/11/12.
 * update by yuwei 2016/8/16
 */
public class YsAdd4Activity extends BaseActivity implements EditTextWithCompound.OnInputListener {
    private NavigationCocoBar navigationCocoBar;
    private TextView wifiNameTextView, tv_unsupport_5G_device;
    private EditTextWithCompound wifiPwdEditText;
    private ImageView pwdShowHideImageView;
    private Button configStartButton;
    private String ssid, password;

    private int typeid;
    //添加单个小欧摄像头时传输的设备ID
    private String danaleAddDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_config2_activity);
        typeid = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, 0);
        findViews();
        init();
    }

    private void findViews() {
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        tv_unsupport_5G_device = (TextView) findViewById(R.id.tv_unsupport_5G_device);
        if (typeid == R.string.xiao_ou_camera) {
            navigationCocoBar.setCenterText(getString(R.string.add) + getString(typeid));
            tv_unsupport_5G_device.setVisibility(View.VISIBLE);
        } else
            navigationCocoBar.setCenterText(getString(R.string.add_ys_device_title));
        wifiNameTextView = (TextView) findViewById(R.id.wifiNameTextView);
        wifiPwdEditText = (EditTextWithCompound) findViewById(R.id.wifiPwdEditText);
        pwdShowHideImageView = (ImageView) findViewById(R.id.pwdShowHideImageView);
        configStartButton = (Button) findViewById(R.id.configStartButton);
    }

    private void init() {
        wifiNameTextView.setCompoundDrawables(null, null, null, null);
        wifiPwdEditText.setOnInputListener(this);
        wifiPwdEditText.setRightfulBackgroundDrawable(null);
        wifiPwdEditText.isNeedFilter(false);
        wifiPwdEditText.setNeedRestrict(false);
        pwdShowHideImageView.setOnClickListener(this);
        configStartButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWifiConnect()){
            ApWifiHelper apWifiHelper = new ApWifiHelper(mAppContext);
            ssid = apWifiHelper.getSSID();
            wifiNameTextView.setText(ssid);
            if (apWifiHelper.currentWifiSecurityIsNone()) {
                wifiPwdEditText.setHint(R.string.ap_config_wifi_password_disable_hint);
                wifiPwdEditText.setText("");
                wifiPwdEditText.setEnabled(false);
                pwdShowHideImageView.setVisibility(View.GONE);
            } else {
                wifiPwdEditText.setEnabled(true);
                wifiPwdEditText.setHint(R.string.ap_config_wifi_password_hint);
                String password = WifiCache.getPassword(this, ssid);
                if (!TextUtils.isEmpty(password)) {
                    wifiPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    wifiPwdEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    wifiPwdEditText.setLongClickable(false);
                    pwdShowHideImageView.setVisibility(View.GONE);
                    wifiPwdEditText.setIntactText(password);
                } else {
                    wifiPwdEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    wifiPwdEditText.setLongClickable(true);
                    pwdShowHideImageView.setVisibility(View.VISIBLE);
                    wifiPwdEditText.setText("");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.configStartButton:
                ssid = wifiNameTextView.getText().toString();
                password = wifiPwdEditText.getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtil.showToast(R.string.ap_config_ssid_empty);
                } else if (wifiPwdEditText.isEnabled() && password.length() < 8) {
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.ap_config_check_psw_length));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else {
                    if (typeid == R.string.xiao_ou_camera) {
//                        sendWifiForDanaleCamera(ssid, password);
                    } else {
                        Intent intent = getIntent();
                        intent.setClass(this, YsAdd5Activity.class);
                        intent.putExtra(IntentKey.YS_WIFI_SSID, ssid);
                        intent.putExtra(IntentKey.YS_WIFI_PASSOWRD, password);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
            case R.id.pwdShowHideImageView: {
                int selectionStart = wifiPwdEditText.getSelectionStart();
                if (wifiPwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    wifiPwdEditText.setTransformationMethod(null);
                    pwdShowHideImageView.setImageResource(R.drawable.password_hide);
                } else {
                    wifiPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwdShowHideImageView.setImageResource(R.drawable.password_show);
                }
                wifiPwdEditText.setSelection(selectionStart);
                break;
            }
        }
    }

    @Override
    public void onRightful() {
        pwdShowHideImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUnlawful() {
        pwdShowHideImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClearText() {

    }

    private boolean isWifiConnect(){
        if (!NetUtil.isWifi(mAppContext)){
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.please_connect_wifi));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.ap_config_reconnect_go));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_PopViewCancel), null);
                    finish();
                }

                @Override
                public void onRightButtonClick(View view) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_ToConnect), null);
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
            return false;
        }else {
            return true;
        }
    }
}
