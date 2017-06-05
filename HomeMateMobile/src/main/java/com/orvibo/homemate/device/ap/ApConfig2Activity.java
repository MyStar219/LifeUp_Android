package com.orvibo.homemate.device.ap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConfig;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.ap.EntityDevice;
import com.orvibo.homemate.ap.EntitySetWifiResult;
import com.orvibo.homemate.ap.EntityWifi;
import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.QRCode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.adapter.ApWifiAdapter;
import com.orvibo.homemate.sharedPreferences.DeviceModelIdCache;
import com.orvibo.homemate.sharedPreferences.WifiCache;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 输入wifi密码进行配置界面
 * Created by Allen on 2015/8/7.
 */
public class ApConfig2Activity extends BaseActivity implements EditTextWithCompound.OnInputListener {
    private static final String TAG = ApConfig2Activity.class.getName();
    private View contentView;
    private TextView wifiNameTextView;
    private EditTextWithCompound wifiPwdEditText;
    private ImageView pwdShowHideImageView;
    private PopupWindow wifiPopupWindow;
    private ApWifiAdapter apWifiAdapter;

    private ApConfig apConfig;
    private String oldSSID, oldPassword;
    private int oldNetworkId;
    private EntityDevice entityDevice;
    private EntityWifi entityWifi;
    private static final int SEARCH = 3;
    private static final int SET_WIFI = 4;
    private static final int DISMISS_DIALOG = 5;
    private boolean foundOldSSID = false;
    private boolean checkedWifi = false;
    private int searchTimes = 3;
    private static final int SEARCH_TIME = 2 * 1000;
    int typeId;
    private QRCode mQrCode;
    private String mProductName;
    //标志用户主动取消配置
    private boolean isUserCancle = false;
    private String deviceModelID;
    private NavigationCocoBar mNavigationCocoBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = View.inflate(this, R.layout.ap_config2_activity, null);
        mQrCode = (QRCode) getIntent().getSerializableExtra("QRCode");
        setContentView(contentView);
        mProductName = getIntent().getStringExtra(IntentKey.PRODUCTNAME);
        typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);

        mNavigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//没有textId的wifi设备
        if (mProductName != null) {
            mNavigationCocoBar.setCenterText(getString(R.string.add) + mProductName);
        } else {
            //typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);
            if (typeId != 0) {
                mNavigationCocoBar.setCenterText(getString(R.string.add) + getString(typeId));
            }
            switch (typeId) {
                case R.string.device_add_coco:
                    mNavigationCocoBar.setCenterText(getString(R.string.ap_config_title));
                    break;

            }
        }
        initApConfig();
        init();

    }

    private void init() {
        wifiNameTextView = (TextView) findViewById(R.id.wifiNameTextView);
        wifiNameTextView.setHint(R.string.ap_config_searching);
        wifiNameTextView.setOnClickListener(this);
        handler.sendEmptyMessageDelayed(SEARCH, SEARCH_TIME);
        wifiPwdEditText = (EditTextWithCompound) findViewById(R.id.wifiPwdEditText);
        wifiPwdEditText.setRightfulBackgroundDrawable(null);
        wifiPwdEditText.setOnInputListener(this);
        wifiPwdEditText.isNeedFilter(false);
        wifiPwdEditText.setNeedRestrict(false);
        pwdShowHideImageView = (ImageView) findViewById(R.id.pwdShowHideImageView);
        pwdShowHideImageView.setOnClickListener(this);
        Button configStartButton = (Button) findViewById(R.id.configStartButton);
        configStartButton.setOnClickListener(this);
        apWifiAdapter = new ApWifiAdapter();

        Intent intent = getIntent();
        String ip = intent.getStringExtra(ApConstant.IP);
        oldSSID = intent.getStringExtra(ApConstant.OLD_SSID);
        oldNetworkId = intent.getIntExtra(ApConstant.OLD_NETWORK_ID, -1);
        apConfig.getDevice(ip);
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH:
                    Object object;
                    if (!checkedWifi && !foundOldSSID && (object = apWifiAdapter.getItem(0)) != null && object instanceof EntityWifi) {
                        checkWifi((EntityWifi) object);
                    } else if (--searchTimes > 0) {
                        handler.sendEmptyMessageDelayed(SEARCH, SEARCH_TIME);
                    } else {
                        wifiNameTextView.setHint(R.string.ap_config_check_wifi_hint);
                    }
                    break;
                case SET_WIFI:
                    dismissDialog();
                    if (!isFinishingOrDestroyed()) {
                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                        dialogFragmentOneButton.setTitle(getString(R.string.ap_config_send_fail));
                        dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(View view) {
                                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_SendPwdFail), null);
                                finish();
                            }
                        });
                        dialogFragmentOneButton.show(getFragmentManager(), "");
                    }
                    break;
                case DISMISS_DIALOG:
                    dismissDialog();
                    break;
            }
        }
    };

    private void initApConfig() {
        apConfig = new ApConfig() {
            @Override
            public void onConnectFail() {
//                ToastUtil.showToast( R.string.ap_config_connect_fail);
                Log.w(TAG, "onConnectFail()");
            }

            @Override
            public void onSendFail() {
//                ToastUtil.showToast( R.string.ap_config_send_fail);
                Log.w(TAG, "onSendFail()");
            }


            @Override
            protected void onGetDevice(EntityDevice entityDevice) {
                ApConfig2Activity.this.entityDevice = entityDevice;
                if (entityDevice != null) {
                    deviceModelID = entityDevice.getModelId();
                    DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(deviceModelID);
                    if (deviceDesc != null) {
                        String language = getResources().getConfiguration().locale.getLanguage();
                        DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
                        if (deviceLanguage != null) {
                            mProductName = deviceLanguage.getProductName();
                            if (mProductName != null) {
                                mProductName = deviceLanguage.getProductName();
                                mNavigationCocoBar.setCenterText(getString(R.string.add) + mProductName);
                            }
                        }
                    }
                }

                if (!StringUtil.isEmpty(entityDevice.getMac()) && !StringUtil.isEmpty(entityDevice.getModelId())) {
                    DeviceModelIdCache.saveDeviceModelId(ViHomeApplication.getAppContext(), entityDevice.getMac(), entityDevice.getModelId());
                }
            }

            @Override
            protected void onScanWifi(EntityWifi entityWifi) {
                dismissDialog();
                String ssid = entityWifi.getSsid();
                if (!TextUtils.isEmpty(ssid)) {
                    apWifiAdapter.addEntityWifi(entityWifi, new ArrayList<String>() {{
                        add(ApConstant.AP_OTHER_DEFAULT_SSID);
                        add(ApConstant.AP_DEFAULT_SSID);
                    }});
                    if (ssid.equals(oldSSID)) {
                        foundOldSSID = true;
                        checkWifi(entityWifi);
                    }
                }
            }

            @Override
            protected void onSetWifi(EntitySetWifiResult entitySetWifiResult) {
                //用来判断是否是用户主动取消，如果是用户主动取消，则不做处理直接返回
                if (isUserCancle) {
                    return;
                }
                handler.removeMessages(SET_WIFI);
                dismissDialog();
                if (entitySetWifiResult.getResult() == ApConstant.RESULT_SUCCESS) {
                    Intent intent = new Intent(ApConfig2Activity.this, ApBindActivity.class);
                    intent.putExtra(ApConstant.OLD_SSID, oldSSID);
                    intent.putExtra(ApConstant.OLD_NETWORK_ID, oldNetworkId);
                    intent.putExtra(ApConstant.PASSWORD, oldPassword);
                    intent.putExtra(ApConstant.ENTITY_WIFI, entityWifi);
                    intent.putExtra(ApConstant.ENTITY_DEVICE, entityDevice);
                    intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
                    intent.putExtra(IntentKey.PRODUCTNAME, mProductName);
                    startActivity(intent);
                    finish();
                } else {
//                    ToastUtil.showToast(R.string.ap_config_fail);
                    apConfig.scanWifi();
                }
            }

            @Override
            public void onTimeout() {
                ToastUtil.showToast(R.string.ap_config_timeout);
                Log.w(TAG, "onTimeout()");
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.wifiNameTextView: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_SelectWiFi), null);
                showWifiList();
                break;
            }
            case R.id.configStartButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_StartAdd), null);
                String ssid = wifiNameTextView.getText().toString();
                String password = wifiPwdEditText.getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtil.showToast(R.string.ap_config_ssid_empty);
                } else if (wifiPwdEditText.isEnabled() && password.length() < 8) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_CheckPwdLength), null);
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.ap_config_check_psw_length));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else {
                    oldSSID = ssid;
                    oldPassword = password;
                    String modelId = null;
                    if (entityDevice != null)
                        modelId = entityDevice.getModelId();
                    if (TextUtils.isEmpty(modelId) || modelId.equals("7f831d28984a456698dce9372964caf3")) {
                        ssid = ssid.replace("+", "\\\\2B");
                        ssid = ssid.replace(",", "\\\\2C");
                        ssid = ssid.replace("=", "\\\\3D");
                        password = password.replace("+", "\\\\2B");
                        password = password.replace(",", "\\\\2C");
                        password = password.replace("=", "\\\\3D");
                    }
                    showDialog();
                    apConfig.setWifi(ssid, password);
                    handler.sendEmptyMessageDelayed(SET_WIFI, 3000);
                }
                break;
            }
            case R.id.pwdShowHideImageView: {
                int selectionStart = wifiPwdEditText.getSelectionStart();
                if (wifiPwdEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    wifiPwdEditText.setTransformationMethod(null);
                    pwdShowHideImageView.setImageResource(R.drawable.password_hide);
                } else {
                    wifiPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwdShowHideImageView.setImageResource(R.drawable.password_show);
                }
                if (selectionStart > 0) {
                    wifiPwdEditText.setSelection(selectionStart);
                }
                break;
            }
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_Cancel), null);
        //   if (!("zh".equals(language))) {
        if (contentView != null) {
            contentView.post(new Runnable() {
                @Override
                public void run() {
                    new ConfirmAndCancelPopup() {
                        @Override
                        public void confirm() {
                            /**
                             *  取消设置，故意发送错误密码，使设备退出配置状态，用户下次配置需要，重新复位操作
                             */
                            isUserCancle = true;
                            String modelId = null;
                            String ssid = wifiNameTextView.getText().toString();
                            if (entityDevice != null)
                                modelId = entityDevice.getModelId();
                            if (TextUtils.isEmpty(modelId) || modelId.equals("7f831d28984a456698dce9372964caf3")) {
                                ssid = ssid.replace("+", "\\\\2B");
                                ssid = ssid.replace(",", "\\\\2C");
                                ssid = ssid.replace("=", "\\\\3D");
                            }
                            apConfig.setWifi(ssid, "********");
                            super.confirm();
                            dismiss();
                            finish();
                        }

                        @Override
                        public void cancel() {

                            dismiss();
                            super.cancel();

                        }
                    }.showPopup(ApConfig2Activity.this, R.string.ap_config_cancel, R.string.ap_config_cancel_no, R.string.ap_config_cancel_yes);
                }
            });
        }
//        } else {
//            super.onBackPressed();
//        }
    }

    private void showWifiList() {
        if (wifiPopupWindow == null) {
            View view = View.inflate(this, R.layout.ap_wifi_list, null);
            NavigationCocoBar popupNavigationBar = (NavigationCocoBar) view.findViewById(R.id.navigationBar);
            popupNavigationBar.setOnLeftClickListener(new NavigationCocoBar.OnLeftClickListener() {
                @Override
                public void onLeftClick(View v) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_Back), null);
                    wifiPopupWindow.dismiss();
                }
            });
            popupNavigationBar.setOnRightClickListener(new NavigationCocoBar.OnRightClickListener() {
                @Override
                public void onRightClick(View v) {
                    showDialogNow();
                    handler.sendEmptyMessageDelayed(DISMISS_DIALOG, 3000);
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_WiFi_RefreshWiFi), null);
                    apConfig.scanWifi();
                }
            });
            ListView wifiListView = (ListView) view.findViewById(R.id.wifiListView);
            wifiListView.setAdapter(apWifiAdapter);
            wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    checkWifi(apWifiAdapter.getEntityWifiByPosition(position));
                }
            });
            wifiPopupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            PopupWindowUtil.initPopup(wifiPopupWindow, getResources().getDrawable(R.color.transparent), 1);
        }
        wifiPopupWindow.showAtLocation(contentView, Gravity.NO_GRAVITY, 0, 0);

    }

    private void checkWifi(EntityWifi entityWifi) {
        this.entityWifi = entityWifi;
        checkedWifi = true;
        String ssid = entityWifi.getSsid();
        apWifiAdapter.setCheckedSSID(ssid);
        wifiNameTextView.setText(ssid);

        String auth = entityWifi.getAuth();
        String enc = entityWifi.getEnc();
        if ((auth == null || auth.equals(ApConstant.AUTH_OPEN)) && (enc == null || enc.equals(ApConstant.ENC_NONE))) {
            wifiPwdEditText.setHint(R.string.ap_config_wifi_password_disable_hint);
            wifiPwdEditText.setText("");
            wifiPwdEditText.setEnabled(false);
            wifiPwdEditText.setDrawable();
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
                wifiPwdEditText.setDrawable();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        apConfig.cancelConfig();
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
        pwdShowHideImageView.setVisibility(View.VISIBLE);
        wifiPwdEditText.setTransformationMethod(null);
        pwdShowHideImageView.setImageResource(R.drawable.password_hide);
    }

    private String getLanguage() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return "zh";
        } else if (language.endsWith("en")) {
            return "en";
        } else if (language.endsWith("de")) {
            return "de";
        } else if (language.endsWith("fr")) {
            return "fr";
        }
        return "en";
    }
}
