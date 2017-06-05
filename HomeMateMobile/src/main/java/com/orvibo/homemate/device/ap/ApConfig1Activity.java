package com.orvibo.homemate.device.ap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.ap.ApScanAndConnect;
import com.orvibo.homemate.ap.ApWifiHelper;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.QRCode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.reconnect.Reconnect;
import com.orvibo.homemate.core.reconnect.ReconnectAction;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.RequestState;
import com.orvibo.homemate.data.RequestType;
import com.orvibo.homemate.model.base.RequestConfig;
import com.orvibo.homemate.model.base.RequestTarget;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;
import com.orvibo.homemate.view.custom.TimingCountdownTabView;
import com.tencent.stat.StatService;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.FailReason;
import com.videogo.universalimageloader.core.assist.ImageScaleType;
import com.videogo.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Locale;

/**
 * Created by Allen on 2015/8/7.
 */
public class ApConfig1Activity extends BaseActivity implements ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = ApConfig1Activity.class.getName();

    private AnimationDrawable anim;
    private ApScanAndConnect apScanAndConnect;

    private String oldSSID;
    private int oldNetworkId = -1;
    private boolean isPaused = false;
    private NavigationCocoBar navigationCocoBar;
    //    private String defaultSSid = ApConstant.AP_DEFAULT_SSID;
    private String defaultSSid = ApConstant.AP_OTHER_DEFAULT_SSID;
    private String tryConnectSSid;
    private int typeId;
    private QRCode mQrCode;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private String mProductName;
    private TimingCountdownTabView mTimingCountdownTabView;
    private ImageView mBlueGrayImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoad();
        mQrCode = (QRCode) getIntent().getSerializableExtra("QRCode");
        setContentView(R.layout.ap_config1_activity);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        mBlueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        TextView tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        //没有textId的wifi设备
        if (mQrCode != null) {
            DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(mQrCode.getQrCodeId(), getLanguage());
            if (deviceLanguage != null) {
                //  String productName = deviceLanguage.getProductName();
                String stepInfo = deviceLanguage.getStepInfo();
                //  COCO智能插线板@http://firmware.orvibo.com/pic/bg_coco_2.png@接通电源，长按开关6秒，直到WiFi指示灯闪烁
                String[] stepInfos = stepInfo.split("@");
                mProductName = stepInfos[0];
                String picUrl = null;
                String tips = null;
                if (stepInfos.length > 1) {
                    picUrl = stepInfos[1];
                }
                if (stepInfos.length > 2) {
                    tips = stepInfos[2];
                }
                //    tipTextView1.setText(getString(R.string.device_add_s20c_text));
                if (tips != null) {
                    tipTextView1.setText(tips);
                }

                navigationCocoBar.setCenterText(getString(R.string.add) + mProductName);

                //  mImageLoader.displayImage(mQrCode.getPicUrl(), blueGrayImageView, mOptions, new ImageLoadingListener() {
                mImageLoader.displayImage(picUrl, mBlueGrayImageView, mOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        mBlueGrayImageView.setVisibility(View.GONE);
                        showDialogNow(null, getString(R.string.loading0));
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        dismissDialog();
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        mBlueGrayImageView.setVisibility(View.VISIBLE);
                        dismissDialog();
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        dismissDialog();
                    }
                });
            }
            //有textId的wifi设备
        } else {

            typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);

            navigationCocoBar.setCenterText(getString(R.string.add) + getString(typeId));

            //   navigationCocoBar.setCenterText(getString(R.string.add) + getString(typeId));
            switch (typeId) {
                case R.string.device_add_coco:
                    defaultSSid = ApConstant.AP_DEFAULT_SSID;
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
                case R.string.device_add_liangba:
                    tipTextView1.setText(getString(R.string.liangba_clotheshorse_add_tip));
                    mBlueGrayImageView.setImageResource(R.drawable.bg_aoke_liangba);
                    break;
                case R.string.device_add_oujia:
                case R.string.device_add_mairunclothes:
                    initRemoteSelect();
                    tipTextView1.setText(getString(R.string.device_clotheshorse_add_tip_oujia));
                    // mBlueGrayImageView.setImageResource(R.drawable.bg_zicheng_c2);
                    break;
                case R.string.device_add_aoke_liangyi:
                    tipTextView1.setText(getString(R.string.device_clotheshorse_add_aoke_tip));
                    mBlueGrayImageView.setImageResource(R.drawable.bg_aoke_liangyi);
                    break;
                case R.string.device_add_s20c:
                    //  case R.string.device_add_socket_cn:
                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
                    mBlueGrayImageView.setImageResource(R.drawable.bg_s20c1_anim);
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
                case R.string.device_add_s31_socket:
                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
                    mBlueGrayImageView.setImageResource(R.drawable.bg_s31_anim);
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
                //海外
                case R.string.device_add_socket_s25:
                case R.string.device_add_socket_us:
                case R.string.device_add_socket_eu:
                case R.string.device_add_socket_uk:
                case R.string.device_add_socket_au:
                case R.string.device_add_socket_cn:
                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
                    mBlueGrayImageView.setImageResource(R.drawable.icon_bg_socket_s25_anim);
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
//                case R.string.device_add_socket_us:
//                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
//                    blueGrayImageView.setImageResource(R.drawable.bg_s20c1_anim_us);
//                    anim = (AnimationDrawable) blueGrayImageView.getDrawable();
//                    anim.start();
//                    break;
//                case R.string.device_add_socket_eu:
//                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
//                    blueGrayImageView.setImageResource(R.drawable.bg_s20c1_anim_eu);
//                    anim = (AnimationDrawable) blueGrayImageView.getDrawable();
//                    anim.start();
//                    break;
//                case R.string.device_add_socket_uk:
//                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
//                    blueGrayImageView.setImageResource(R.drawable.bg_s20c1_anim_uk);
//                    anim = (AnimationDrawable) blueGrayImageView.getDrawable();
//                    anim.start();
//                    break;
//                case R.string.device_add_socket_au:
//                    tipTextView1.setText(getString(R.string.device_add_s20c_text));
//                    blueGrayImageView.setImageResource(R.drawable.bg_s20c1_anim_au);
//                    anim = (AnimationDrawable) blueGrayImageView.getDrawable();
//                    anim.start();
//                    break;
                case R.string.device_add_yidong:
                    //海外
                case R.string.device_add_socket_yd:
                    tipTextView1.setText(getString(R.string.device_add_yidong_text));
                    if (!("zh".equals(language))) {
                        mBlueGrayImageView.setImageResource(R.drawable.icon_bg_socket_yd_anim);
                    } else {
                        mBlueGrayImageView.setImageResource(R.drawable.icon_bg_yidong_anim);
                    }
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
                case R.string.device_add_feidiao_lincoln:
                    tipTextView1.setText(getString(R.string.device_add_feidiao_text));
                    mBlueGrayImageView.setImageResource(R.drawable.icon_bg_feidiao_anim);
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
                case R.string.device_add_feidiao_xiaoe:
                    tipTextView1.setText(getString(R.string.device_add_feidiao_xiaoe_text));
                    mBlueGrayImageView.setImageResource(R.drawable.bg_feidiaoxiaoe);
                    break;
                case R.string.device_add_xiaofang_tv:
                    tipTextView1.setText(getString(R.string.device_add_xiaofang_tv_text));
                    mBlueGrayImageView.setImageResource(R.drawable.icon_bg_xiaofang_tv_anim);
                    anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
                    anim.start();
                    break;
            }
        }
        initApScanAndConnect();

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
    }

    /**
     * version 1.8 紫程修改了方案
     */
    private void initRemoteSelect() {
        mTimingCountdownTabView = (TimingCountdownTabView) findViewById(R.id.remote_select);
        int defautPosition = TimingCountdownTabView.TIMING_POSITION;
        mTimingCountdownTabView.setOnTabSelectedListener(new TimingCountdownTabView.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case TimingCountdownTabView.TIMING_POSITION:
                        mBlueGrayImageView.setImageResource(R.drawable.bg_zicheng_c2);
//                        mBlueGrayImageView.setImageResource(R.drawable.bg_oujia_anim);
//                        anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
//                        anim.start();
                        break;
                    case TimingCountdownTabView.COUNTDOWN_POSITION:
                        mBlueGrayImageView.setImageResource(R.drawable.bg_zicheng_f2);
//                        mBlueGrayImageView.setImageResource(R.drawable.bg_oujia_f_anim);
//                        anim = (AnimationDrawable) mBlueGrayImageView.getDrawable();
//                        anim.start();
                        break;
                }
            }
        });
        mTimingCountdownTabView.setRemoteView(true);
        mTimingCountdownTabView.setSelectedPosition(defautPosition);
        mTimingCountdownTabView.setVisibility(View.VISIBLE);
        mTimingCountdownTabView.initName(getString(R.string.device_clotheshorse_add_oujia_c), getString(R.string.device_clotheshorse_add_oujia_f));

    }

    private void initImageLoad() {
        initImage();
        initDisplayImageOptions();
    }

    private void initImage() {
        mImageLoader = ImageLoader.getInstance();
    }

    private void initDisplayImageOptions() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.launch_logo) // resource or drawable
                .showImageForEmptyUri(R.drawable.launch_logo) // resource or drawable
                .showImageOnFail(R.drawable.launch_logo) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .delayBeforeLoading(1000)
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .build();

    }

    private void initApScanAndConnect() {
        apScanAndConnect = new ApScanAndConnect(mAppContext, defaultSSid) {
            @Override
            public void onStartScan() {
                Log.d(TAG, "onStartScan()");
            }

            @Override
            public void onScanEmpty() {
                Log.d(TAG, "onScanEmpty()");
                toApConfig1TipsActivity();
            }

            @Override
            public void onStartConnect(String ssid) {
                tryConnectSSid = ssid;
                Log.d(TAG, "onStartConnect()-ssid:" + ssid);
            }

            @Override
            public void onConnected(String ssid, String ip) {
                Log.d(TAG, "onConnected()");
                LogUtil.e(TAG, "ssid=" + ssid + ",ip=" + ip + ",old_ssid=" + oldSSID + ",oldNetworkId=" + oldNetworkId);
//                dismissDialog();
                Intent intent = new Intent(ApConfig1Activity.this, ApConfig2Activity.class);
                intent.putExtra(ApConstant.IP, ip);
                intent.putExtra(ApConstant.OLD_SSID, oldSSID);
                intent.putExtra(ApConstant.OLD_NETWORK_ID, oldNetworkId);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
                //在数据库里新增加的设备,并且本地没有对应的typeId（资源）
                intent.putExtra("QRCode", mQrCode);
                intent.putExtra(IntentKey.PRODUCTNAME, mProductName);
                startActivity(intent);
                finish();
            }

            @Override
            public void onTimeout() {
                Log.w(TAG, "onTimeout()");
                if (isFinishingOrDestroyed()) {
                    LogUtil.w(TAG, "onTimeout()-Activity is Finishing or has been destroyed.");
                    return;
                }
                dismissDialog();
                if (!TextUtils.isEmpty(tryConnectSSid)) {
                    toApConfig1TipsActivity();
                } else if (!isPaused) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_GreenFlashes_FindDeviceFail), null);
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.ap_connect_timeout));
                    if (R.string.device_add_coco == typeId) {
                        dialogFragmentOneButton.setContent(getString(R.string.ap_connect_timeout_content));
                    } else {
                        dialogFragmentOneButton.setContent(getString(R.string.ap_connect_s20_timeout_content));
                    }
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                }
            }
        };
    }

    private void toApConfig1TipsActivity() {
        Intent intent = new Intent(ApConfig1Activity.this, ApConfig1TipsActivity.class);
        intent.putExtra(ApConstant.OLD_SSID, oldSSID);
        intent.putExtra(ApConstant.OLD_NETWORK_ID, oldNetworkId);
        intent.putExtra(IntentKey.DEVICE_ADD_TYPE, typeId);
        intent.putExtra(IntentKey.PRODUCTNAME, mProductName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_GreenFlashes_theNextBtn), null);
                boolean isNetworkAvailable = NetUtil.isNetworkEnable(mAppContext);
                if (!isNetworkAvailable) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_GreenFlashes_NoNetwork), null);
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.net_not_connect));
                    dialogFragmentOneButton.setContent(getString(R.string.net_not_connect_content));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                    return;
                }
                boolean canStart = false;
                ApWifiHelper apWifiHelper = new ApWifiHelper(mAppContext);
                //获得当前连接wifi的信息的对象
                WifiInfo wifiInfo = apWifiHelper.getWifiInfo();
                if (wifiInfo != null) {
                    String ssid = apWifiHelper.getSSID();//当前连接到wifi的名称
//                    String serverIp = apWifiHelper.getDhcpIp();
                    //先保存原来的SSID和NetworkID，配置完毕再重新连接
                    if (ssid != null) { //&& serverIp != null) {
                        //判断是否已经连接上Ap（设备分享出来的ap）
                        if (apWifiHelper.isAPConnected(defaultSSid)) {
                            canStart = true;
                        } else if (UserManage.getInstance(mAppContext).isLogined()) {
                            canStart = true;
                            oldSSID = ssid;
                            oldNetworkId = wifiInfo.getNetworkId();
                            Log.d(TAG, "oldSSID:" + oldSSID + " oldNetworkId:" + oldNetworkId);
                        } else {
                            Reconnect reconnect = Reconnect.getInstance();
                            ReconnectAction reconnectAction = new ReconnectAction();
                            reconnectAction.setForceReconnect(true);
                            reconnectAction.setUid(Reconnect.SERVER);
                            reconnectAction.setReconnectType(ReconnectAction.RECONNECT_LOGIN);

                            RequestConfig requestConfig = new RequestConfig();
                            requestConfig.state = RequestState.SERVER;
                            requestConfig.type = RequestType.NORMAL;
                            requestConfig.target = RequestTarget.SERVER;
                            reconnectAction.setRequestConfig(requestConfig);
                            reconnect.reconnect(reconnectAction);
                        }
                    }
                }
                if (canStart) {
                    apScanAndConnect.start();
                    showDialogNow(this, getString(R.string.ap_config_searching));
                } else {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_GreenFlashes_ConnectServerFail), null);
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.ap_not_connect_server));
                    dialogFragmentOneButton.setContent(getString(R.string.ap_not_connect_server_content));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
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
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_GreenFlashes_Back), null);
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        if (anim != null)
            anim.stop();
        anim = null;
        apScanAndConnect.cancel();
        super.onDestroy();
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

//    @Override
//    public void showDialogNow(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
//        super.showDialogNow(onCancelClickListener, content);
//    }

    @Override
    public void onCancelClick(View view) {
        apScanAndConnect.cancel();
    }
}
