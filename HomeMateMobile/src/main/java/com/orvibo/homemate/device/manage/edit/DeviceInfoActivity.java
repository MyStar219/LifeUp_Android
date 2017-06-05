package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.bo.GatewayServer;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CameraInfoDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.dao.GatewayServerDao;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.control.CurtainWindowShadesActivity;
import com.orvibo.homemate.sharedPreferences.RGBCache;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.AutoAjustSizeTextView;
import com.orvibo.homemate.view.custom.ClickCountImageView;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.util.List;


/**
 * Created by Allen on 2015/5/28.
 */
public class DeviceInfoActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener, ClickCountImageView.OnSpecificClickListener {
    private final String TAG = DeviceInfoActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private ClickCountImageView iconImageView;
    private TextView companyNameTextView, productNameTextView;
    private LinearLayout info1LinearLayout, info2LinearLayout, info3LinearLayout, info4LinearLayout, info5LinearLayout;
    private ImageView info1LineImageView, info2LineImageView, info3LineImageView, info4LineImageView, info5LineImageView;
    private TextView info1TitleTextView, info2TitleTextView, info2TextView, info3TitleTextView, info4TitleTextView, info5TitleTextView, info3TextView, info4TextView, info5TextView;
    private AutoAjustSizeTextView info1TextView;
    private Gateway gateway;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info_activity);
        init();
    }

    private void init() {
        gateway = (Gateway) getIntent().getSerializableExtra(Constant.GATEWAY);
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        LogUtil.d(TAG, "init()-gateway:" + gateway);
        LogUtil.d(TAG, "init()-device:" + device);
        if (device == null) {
            finish();
            return;
        }
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        iconImageView = (ClickCountImageView) findViewById(R.id.iconImageView);
        iconImageView.setOnClickListener(this);
        iconImageView.setOnSpecificClickListener(this);
        iconImageView.setClickCount(10);
        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
        productNameTextView = (TextView) findViewById(R.id.productNameTextView);
        info1LineImageView = (ImageView) findViewById(R.id.info1LineImageView);
        info2LineImageView = (ImageView) findViewById(R.id.info2LineImageView);
        info2LinearLayout = (LinearLayout) findViewById(R.id.info2LinearLayout);
        info1LinearLayout = (LinearLayout) findViewById(R.id.info1LinearLayout);
        info3LineImageView = (ImageView) findViewById(R.id.info3LineImageView);
        info4LineImageView = (ImageView) findViewById(R.id.info4LineImageView);
        info5LineImageView = (ImageView) findViewById(R.id.info5LineImageView);
        info3LinearLayout = (LinearLayout) findViewById(R.id.info3LinearLayout);
        info4LinearLayout = (LinearLayout) findViewById(R.id.info4LinearLayout);
        info5LinearLayout = (LinearLayout) findViewById(R.id.info5LinearLayout);
        info1TitleTextView = (TextView) findViewById(R.id.info1TitleTextView);
        info2TitleTextView = (TextView) findViewById(R.id.info2TitleTextView);
        info3TitleTextView = (TextView) findViewById(R.id.info3TitleTextView);
        info4TitleTextView = (TextView) findViewById(R.id.info4TitleTextView);
        info5TitleTextView = (TextView) findViewById(R.id.info5TitleTextView);
        info1TextView = (AutoAjustSizeTextView) findViewById(R.id.info1TextView);
        info2TextView = (TextView) findViewById(R.id.info2TextView);
        info3TextView = (TextView) findViewById(R.id.info3TextView);
        info4TextView = (TextView) findViewById(R.id.info4TextView);
        info5TextView = (TextView) findViewById(R.id.info5TextView);
        int deviceType = device.getDeviceType();
        String model = device.getModel();
        if (TextUtils.isEmpty(model) && gateway != null) {
            model = gateway.getModel();
            if (TextUtils.isEmpty(model)) {
                GatewayServer gatewayServer = new GatewayServerDao().selGatewayServer(gateway.getUid());
                if (gatewayServer != null) {
                    model = gatewayServer.getModel();
                }
            }
        }
        if (!TextUtils.isEmpty(model) && DeviceUtil.isNew6SensorDevice(deviceType) && model.contains("V")) {//安防报警器
            model = model.substring(0, model.lastIndexOf("V") + 1);
        } else if (TextUtils.isEmpty(model) && device.getDeviceType() == DeviceType.CAMERA && device.getExtAddr().startsWith("VIEW")) {//p2p摄像机
            model = "bcec7653260b4e98b0e653e0a79d83db";
        } else if (!TextUtils.isEmpty(model) && model.contains("E10")) {
            model = "7f831d28984a456698dce9372964caf3";
        }

        DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(model);

        if (deviceDesc == null) {
            deviceDesc = new DeviceDescDao().selDeviceDesc(device.getAppDeviceId());
        }

        if (deviceDesc == null) {
            return;
        }
        ImageLoader.getInstance().displayImage(deviceDesc.getPicUrl(), iconImageView, ViHomeApplication.getImageOptions());

        DeviceLanguageDao deviceLanguageDao = new DeviceLanguageDao();
        String language = getResources().getConfiguration().locale.getLanguage();
        DeviceLanguage deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
        if (deviceLanguage == null && language.contains("-")) {
            deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), language.substring(0, language.indexOf("-") - 1));
        }
        if (deviceLanguage == null) {
            deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), "zh");
        }
        if (deviceLanguage != null) {
            companyNameTextView.setText(deviceLanguage.getManufacturer());
            productNameTextView.setText(deviceLanguage.getProductName());
        }
        info1TitleTextView.setText(R.string.device_model);
        if (!TextUtils.isEmpty(deviceDesc.getProductModel())) {
            if (deviceDesc.getProductModel().equals("Null")) {
                info1LinearLayout.setVisibility(View.GONE);
                info1LineImageView.setVisibility(View.GONE);
            } else {
                info1TextView.setText(deviceDesc.getProductModel());
            }
        } else {
            info1TextView.setText(deviceDesc.getModel());
        }
        info2TitleTextView.setText(R.string.device_mac);
        info2TextView.setText(device.getExtAddr());
        boolean isWifiDevice = ProductManage.getInstance().isWifiDevice(device);

        if (deviceType == DeviceType.BACK_MUSIC) {
            String uid = device.getUid();
            if (!TextUtils.isEmpty(uid)) {
                String[] models1 = uid.split("\\|");
                if (models1.length > 1) {
                    String s = models1[1];
                    if (!TextUtils.isEmpty(s)) {
                        String[] models2 = s.split("-");
                        if (models2.length > 1) {
                            if (!TextUtils.isEmpty(models2[1])) {
                                model = models2[1];
                            }
                        }
                    }
                }
            }
            info1LinearLayout.setVisibility(View.VISIBLE);
            info1LineImageView.setVisibility(View.VISIBLE);
            info1TextView.setText(model);
            info2LinearLayout.setVisibility(View.GONE);
            info2LineImageView.setVisibility(View.GONE);
            info3LinearLayout.setVisibility(View.GONE);
            info3LineImageView.setVisibility(View.GONE);
        } else if (device.getDeviceType() == DeviceType.CAMERA) {
            CameraInfoDao cameraInfoDao = new CameraInfoDao();
            CameraInfo cameraInfo = cameraInfoDao.selCameraInfoByUid(device.getUid());
            if (cameraInfo == null)
                cameraInfo = cameraInfoDao.selCameraInfoByUid(device.getExtAddr());
            if (cameraInfo != null && cameraInfo.getType() == CameraType.CAMERA_530) {
                //P2P网络摄像机，只显示DID(原来为mac)
//                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.icon_bg_camera, iconImageView, ViHomeApplication.getCircleImageOptions());
//                productNameTextView.setText(R.string.device_type_P2P_CAMERA);
                info2TitleTextView.setText(R.string.device_camera_did);
                info1LineImageView.setVisibility(View.GONE);
                info1LinearLayout.setVisibility(View.GONE);
            } else {
                info2TitleTextView.setText(R.string.device_serial);
                info2TextView.setText(device.getUid());
            }
            info3LinearLayout.setVisibility(View.GONE);
            info3LineImageView.setVisibility(View.GONE);
//            if (cameraInfo != null && cameraInfo.getType() == CameraType.DANALE_CAMERA) {
//                loadDanaleDeviceInfo();
//            }
        } else if (ProductManage.getInstance().isVicenter300ByDeviceType(device.getDeviceType())//300主机、小主机、COCO
                || isWifiDevice) {
            if (!isWifiDevice) {
                View ZigbeeDeviceCount = findViewById(R.id.ZigbeeDeviceCount);
                ZigbeeDeviceCount.setVisibility(View.VISIBLE);
                ImageView ZigbeeDeviceCountImageView = (ImageView) findViewById(R.id.ZigbeeDeviceCountImageView);
                ZigbeeDeviceCountImageView.setVisibility(View.VISIBLE);
                TextView ZigbeeDeviceCountTextView = (TextView) findViewById(R.id.ZigbeeDeviceCountTextView);
                ZigbeeDeviceCountTextView.setText(onLineOffLineCount());
                TextView moreTextView = (TextView) findViewById(R.id.moreTextView);
                moreTextView.setVisibility(View.VISIBLE);
                moreTextView.setOnClickListener(this);
            }
            View centerModel = findViewById(R.id.centerModel);
            centerModel.setVisibility(View.VISIBLE);
            ImageView centerModelImageView = (ImageView) findViewById(R.id.centerModelImageView);
            centerModelImageView.setVisibility(View.VISIBLE);
            TextView centerModelTextView = (TextView) findViewById(R.id.centerModelTextView);
            if (!TextUtils.isEmpty(deviceDesc.getProductModel())) {
                if (deviceDesc.getProductModel().equals("Null")) {
                    centerModel.setVisibility(View.GONE);
                    centerModelImageView.setVisibility(View.GONE);
                } else {
                    centerModelTextView.setText(deviceDesc.getProductModel());
                }
            } else {
                centerModelTextView.setText(deviceDesc.getModel());
            }
            info1LinearLayout.setVisibility(View.VISIBLE);
            info1LineImageView.setVisibility(View.VISIBLE);
            info1LinearLayout.setVisibility(View.VISIBLE);
            info1LineImageView.setVisibility(View.VISIBLE);
            info1TitleTextView.setText(R.string.device_mac);
            String uid = device.getUid();
            try {
                if (!TextUtils.isEmpty(uid)) {
                    info1TextView.setText(toMac(uid.replaceAll("202020202020", "")));
                } else {
                    info1TextView.setText(uid + "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            info2TitleTextView.setText(R.string.device_ip);
            info3TitleTextView.setText(R.string.device_firmware);
            if (gateway == null) {
                gateway = new GatewayDao().selGatewayByUid(device.getUid());
            }
            if (gateway != null) {
                info2TextView.setText(gateway.getLocalStaticIP() + "");
                String ver = gateway.getSoftwareVersion();
                //添加协调器版本号
                String cVer = gateway.getCoordinatorVersion();
                if (!StringUtil.isEmpty(cVer)) {
                    String[] temps = cVer.split("_");
                    if (temps != null && temps.length > 1) {
                        cVer = temps[1];
                    }
                    if (!StringUtil.isEmpty(cVer)) {
                        if (cVer.contains("V")) {
                            cVer = cVer.replace("V", "");
                        } else if (cVer.contains("v")) {
                            cVer = cVer.replace("v", "");
                        }
                    }
                    ver += "_" + cVer;
                }
                info3TextView.setText(ver);
            }
            if (isWifiDevice) {
                info2LinearLayout.setVisibility(View.GONE);
                info2LineImageView.setVisibility(View.GONE);
            }
            //门窗磁传感器、红外传感器
        } else if (DeviceUtil.isSensorDevice(device.getDeviceType()) || DeviceTool.isShowLower(deviceDesc.getModel())
                || ProductManage.isBLLock(device) || device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR || device.getDeviceType() == DeviceType.HUMIDITY_SENSOR) {
            info3TitleTextView.setText(R.string.device_remain_power);
            DeviceStatus deviceStatus = new DeviceStatusDao().selDeviceStatus(device.getUid(), device.getDeviceId());
            if (deviceStatus != null) {
                info3TextView.setText(deviceStatus.getValue4() + "%");
            } else {
                info3TextView.setText("- -");
            }
        } else {
            info3LinearLayout.setVisibility(View.GONE);
            info3LineImageView.setVisibility(View.GONE);
        }
    }

    private String onLineOffLineCount() {
        int totalCount = 0;
        int onLineCount = 0;
        DeviceDao mDeviceDao = new DeviceDao();
        DeviceStatusDao mDeviceStatusDao = new DeviceStatusDao();
        List<Device> devices = mDeviceDao.selVicenterDevices(currentMainUid);
        totalCount = devices.size();
        for (Device device : devices) {
            String uid = device.getUid();
            String deviceId = device.getDeviceId();
            DeviceStatus deviceStatus;
            if (DeviceUtil.isIrDevice(uid, deviceId)) {
                deviceStatus = mDeviceStatusDao.selIrDeviceStatus(uid, device.getExtAddr());
            } else {
                deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
            }
            if (deviceStatus != null && deviceStatus.getOnline() == 1) {
                onLineCount++;
            }
        }
        if (Conf.VER_ALL_DEVICE_ONLINE) {
            return "" + totalCount;
        }
        return onLineCount + "/" + totalCount;
    }

    private String toMac(String mac) {
        return mac.substring(0, 2).toUpperCase() + ":" + mac.substring(2, 4).toUpperCase() + ":" + mac.substring(4, 6).toUpperCase() + ":" + mac.substring(6, 8).toUpperCase() + ":" + mac.substring(8, 10).toUpperCase() + ":" + mac.substring(10, 12).toUpperCase();
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceInfo_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.moreTextView:
                Intent intent = new Intent(this, DeviceMoreActivity.class);
                intent.putExtra(Constant.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.iconImageView:
                if (device != null
                        && (device.getDeviceType() == DeviceType.WINDOW_SHADES
                        || device.getDeviceType() == DeviceType.RGB)) {
                    iconImageView.onClick();
                }
        }
    }


    @Override
    public void onSpecificClicked() {


        if (device != null
                && device.getDeviceType() == DeviceType.WINDOW_SHADES) {
            //进入百叶窗限位设置
            Intent intent = new Intent(this, CurtainWindowShadesActivity.class);
            intent.putExtra(Constant.DEVICE, device);
            intent.putExtra(IntentKey.LIMIT_SET, true);
            startActivity(intent);
        } else if (device != null
                && device.getDeviceType() == DeviceType.RGB) {
            //进入RGB灯（可以详细显示RGB数值）
            Boolean isShow = RGBCache.getRgbIsShow(mContext, userId);
            if (isShow) {
                RGBCache.setRgbIsShow(mContext, userId, false);
                ToastUtil.showToast(R.string.rgb_parameter_hide);
            } else {
                RGBCache.setRgbIsShow(mContext, userId, true);
                ToastUtil.showToast(R.string.rgb_parameter_show);
            }
        }
    }
}
