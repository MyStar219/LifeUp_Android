package com.orvibo.homemate.device.manage.add;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.QRCode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginIntent;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.UserBindType;
import com.orvibo.homemate.device.HopeMusic.HopeMusicHelper;
import com.orvibo.homemate.device.ap.ApConfig1Activity;
import com.orvibo.homemate.device.manage.DeviceSettingActivity;
import com.orvibo.homemate.device.ys.YsAdd1Activity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.user.UserPhoneBindActivity;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.FailReason;
import com.videogo.universalimageloader.core.assist.ImageScaleType;
import com.videogo.universalimageloader.core.listener.ImageLoadingListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 扫描结果设备展示界面
 * Created by snown on 2015/11/5.
 */
public class DeviceScannedResultActivity extends BaseActivity {
    private ImageView productImage;
    private TextView productNameTextView;
    private TextView companyNameTextView;
    private int qrCodeId;//二维码扫描设备对应的id
    private Button addDeivce;
    private String model;
    private String hopeQrCode;
    private boolean isSpecial;

    private int loginIntent = LoginIntent.ALL;
    private String loginEntryString;
    private NavigationGreenBar navigationGreenBar;
    private DeviceLanguageDao mDeviceLanguageDao;
    private QRCode mQRCode;
    private DeviceDescDao mDeviceDescDao;
    private DeviceDesc mDeviceDesc;
    private DeviceLanguage mDeviceLanguage;
    private String mType;
    private HopeMusicHelper hopeMusicHelper;
    private static HashMap<String, Integer> mTypeSwitch;
    private final static int WIFI_DEVICE = 0;
    private final static int ZIGBEE_DEVICE = 1;
    private final static int UNKNOW_DEVICE = 2;
    private int deviceType = 3;
    private ConfirmAndCancelPopup mUpdateVersionTip;

    static {
        initHashMap();
    }

    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initDisplayImageOptions();
        initImage();
        // initHashMap();
        // TypeSwitch.put("coco",R.string.device_add_coco);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("isSpecial", false)) {
            isSpecial = true;
            hopeQrCode = getIntent().getStringExtra("hopeQrCode");
            model = getIntent().getStringExtra("model");
            mDeviceDescDao = new DeviceDescDao();
        } else {
            isSpecial = false;
            mQRCode = (QRCode) getIntent().getSerializableExtra("QRCode");
        }
        init();

    }

    private static void initHashMap() {
        mTypeSwitch = new HashMap<String, Integer>();
        mTypeSwitch.put("coco", R.string.device_add_coco);
        mTypeSwitch.put("host", R.string.device_add_host);
        mTypeSwitch.put("motionsensor", R.string.device_add_human_body_sensor);
        mTypeSwitch.put("doorsensor", R.string.device_add_magnetometer);
        mTypeSwitch.put("socket", R.string.device_add_socket);
        mTypeSwitch.put("switch", R.string.device_add_switch);
        mTypeSwitch.put("controlbox", R.string.device_add_control_box);
        mTypeSwitch.put("remotecontrol", R.string.device_add_remote_control);
        mTypeSwitch.put("zigbeeallone", R.string.device_add_zigbee);
        mTypeSwitch.put("smartlock", R.string.device_add_iintelligent_door_lock);
        mTypeSwitch.put("curtainmotor", R.string.device_add_curtain_motor);
        mTypeSwitch.put("s20c", R.string.device_add_s20c);
        mTypeSwitch.put("aokeclothes", R.string.device_add_aoke_liangyi);
        mTypeSwitch.put("oujiaclothes", R.string.device_add_oujia);
        mTypeSwitch.put("liangbaclothes", R.string.device_add_liangba);
        mTypeSwitch.put("feidiaolincoln", R.string.device_add_socket_feidiao);
        mTypeSwitch.put("mairunclothes", R.string.device_add_mairunclothes);
        mTypeSwitch.put("yidongsocket", R.string.device_add_yidong);
        mTypeSwitch.put("winplus2", R.string.device_add_winplus1);
        mTypeSwitch.put("winplus1", R.string.device_add_winplus2);
        mTypeSwitch.put("light", R.string.device_add_light);
        mTypeSwitch.put("allone2", R.string.device_add_xiaofang_tv);
        mTypeSwitch.put("xiaoou", R.string.xiao_ou_camera);
        mTypeSwitch.put("motionsensor", R.string.device_add_human_body_sensor);
        mTypeSwitch.put("doorsensor", R.string.device_add_magnetometer);
        mTypeSwitch.put("hmburn", R.string.device_add_flammable_gas_sensor);
        mTypeSwitch.put("hmwater", R.string.device_add_flooding_detector);
        mTypeSwitch.put("hmtemp", R.string.device_add_temperature_and_humidity_probe);
        mTypeSwitch.put("hmsmoke", R.string.device_add_smoke_sensor);
        mTypeSwitch.put("hmco", R.string.device_add_co_sensor);
        mTypeSwitch.put("hmbutton", R.string.device_add_emergency_button);
    }


    private void init() {
        mUpdateVersionTip = new ConfirmAndCancelPopup() {
            @Override
            public void confirm() {
                super.confirm();
                dismiss();
                finish();
            }
        };
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        navigationGreenBar.setText(getString(R.string.qr_scanning_result));
        productImage = (ImageView) findViewById(R.id.productImageView);
        productNameTextView = (TextView) findViewById(R.id.productNameTextView);
        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
        addDeivce = (Button) findViewById(R.id.nextButton);
        String url = "";
        if (isSpecial) {
            DeviceDesc deviceDesc = mDeviceDescDao.selDeviceDesc(model);
            if (deviceDesc != null) {
                url = deviceDesc.getPicUrl();
            }
        } else {
            url = mQRCode.getPicUrl();
        }
        mImageLoader.displayImage(url, productImage, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                showDialogNow(null, getString(R.string.loading0));
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                dismissDialog();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                dismissDialog();
                productImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                dismissDialog();
            }
        });
        initDao();
        addDeivce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //如果是向往背景音乐
                if (isSpecial && !StringUtil.isEmpty(hopeQrCode)) {
                    if(showBindPhoneDialog()){
                        return;
                    }
                    String userId = UserCache.getCurrentUserId(mAppContext);
                    List<String> uids = new GatewayDao().selCurrentUserUids(userId);
                    if (uids.contains(hopeQrCode)) {
                        //此用户已经存在该设备
                        ToastUtil.showToast(R.string.add_ys_device_exist);
                    } else {
                        if (!NetUtil.isNetworkEnable(mAppContext)) {
                            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                        } else {
                            bindBackgroundMusic(hopeQrCode);
                        }
                    }
//                    DeviceDao deviceDao = new DeviceDao();
//                    Device device = deviceDao.selWifiDevice(hopeQrCode);
//                    if (device != null) {
//                        ToastUtil.showToast(R.string.add_ys_device_exist);
//                    } else {
//                        if (!NetUtil.isNetworkEnable(mAppContext)) {
//                            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
//                        } else {
//                            bindBackgroundMusic(hopeQrCode);
//                        }
//                    }
                } else {
                    mType = mQRCode.getType();
                    Integer textId = mTypeSwitch.get(mType);


                    String userName = UserCache.getCurrentUserName(ViHomeProApp.getContext());
                    int logoutStatus = UserCache.getLoginStatus(ViHomeProApp.getContext(), userName);
                    if (logoutStatus != LoginStatus.SUCCESS && logoutStatus != LoginStatus.FAIL) {
                        // if (textId == null || DeviceTool.isWifiDevice(textId)) {
                        if ((textId == null) && (deviceType == WIFI_DEVICE) || (textId != null) && DeviceTool.isWifiDevice(textId)) {
                            loginIntent = LoginIntent.SERVER;
                            loginEntryString = Constant.COCO;
                        } else {
                            loginIntent = LoginIntent.ALL;
                            loginEntryString = Constant.ViHome;
                        }
                        showLoginDialog();
                    } else {
                        if (textId == null) {
                            if (mType.endsWith("_0")) {
                                deviceType = WIFI_DEVICE;
                            } else if (mType.endsWith("_1")) {
                                deviceType = ZIGBEE_DEVICE;
                            } else {
                                deviceType = UNKNOW_DEVICE;
                            }


                        }
                        Intent intent;
                        if (deviceType == UNKNOW_DEVICE) {
                            //当前APP版本不支持添加该设备,请先升级到最新版本后再试
                            showUpdateVersionTipPopup();
                            return;
                        }
                        //wifi装置入口1
                        //  if (textId == null || DeviceTool.isWifiDevice(textId)) {//COCO，晾衣架，一栋智能插座，S20智能插座//R.string
                        if ((textId == null && deviceType == WIFI_DEVICE) || (textId != null && DeviceTool.isWifiDevice(textId))) {//COCO，晾衣架，一栋智能插座，S20智能插座//R.string
                            intent = new Intent(DeviceScannedResultActivity.this, ApConfig1Activity.class);
                            //  if (textId == null) {
                            //新添加的wifi设备
                            if (deviceType == WIFI_DEVICE) {
                                intent.putExtra("QRCode", mQRCode);
                            } else {
                                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, textId);
                            }
                        } else if (textId != null && textId == R.string.xiao_ou_camera) {
                            //扫描结果为小欧设备
                            intent = new Intent(DeviceScannedResultActivity.this, YsAdd1Activity.class);
                            intent.putExtra(Constant.CONFIG_TITLE, R.string.xiao_ou_camera);
                        } else if (textId != null && textId == R.string.device_add_host) {//ViHome Pro
                            //主机装置入口2
                            intent = new Intent(DeviceScannedResultActivity.this, AddVicenterTipActivity.class);
                        } else {
                            //zigbee装置入口3
                            intent = new Intent(DeviceScannedResultActivity.this, ZigBeeDeviceAddActivity.class);
                            //新添加的zigbee设备
                            if (deviceType == ZIGBEE_DEVICE) {

                                //  intent.putExtra(Constant.CONFIG_TITLE, textId);
                                intent.putExtra("QRCode", mQRCode);
                            } else {
                                intent.putExtra(Constant.CONFIG_TITLE, textId);
                            }
                        }
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
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
//        缩放类型mageScaleType:
//        EXACTLY:图像将完全按比例缩小的目标大小
//        EXACTLY_STRETCHED:图片会缩放到目标大小完全
//        IN_SAMPLE_INT:图像将被二次采样的整数倍
//        IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
//        NONE:图片不会调整
                .imageScaleType(ImageScaleType.EXACTLY) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .build();

    }

    private void initDao() {

        if (mQRCode != null && !isSpecial) {
            //创建操作设备信息语言包表的dao类
            mDeviceLanguageDao = new DeviceLanguageDao();
            mDeviceLanguage = mDeviceLanguageDao.selDeviceLanguage(mQRCode.getQrCodeId(), getLanguage());
            //   ToastUtil.showToast(mDeviceLanguage.toString());
            if (mDeviceLanguage != null) {
                productNameTextView.setText(mDeviceLanguage.getProductName());
                companyNameTextView.setText(mDeviceLanguage.getManufacturer());
            }
        } else if (isSpecial) {
            //创建操作设备信息语言包表的dao类
            mDeviceLanguageDao = new DeviceLanguageDao();

            DeviceDesc deviceDesc = mDeviceDescDao.selDeviceDesc(model);
            if(deviceDesc!=null) {
                mDeviceLanguage = mDeviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), getLanguage());
                if (mDeviceLanguage != null) {
                    productNameTextView.setText(mDeviceLanguage.getProductName());
                    companyNameTextView.setText(mDeviceLanguage.getManufacturer());
                }
            }
        }
    }


    //绑定向往背景音乐
    private void bindBackgroundMusic(final String qrcode) {
        if(hopeMusicHelper == null) {
            hopeMusicHelper = new HopeMusicHelper(DeviceScannedResultActivity.this);
        }
        hopeMusicHelper.setLoginHopeServerListener(new HopeMusicHelper.LoginHopeServerListener() {
            @Override
            public void loginSuccess(String token) {
                hopeMusicHelper.bindDevice(qrcode);
            }

            @Override
            public void loginFail(String string) {
                dismissDialog();
                if(string.equals(getString(R.string.add_hope_music_bind_phone_title))){
                    showBindPhoneDialog();
                }else {
                    ToastUtil.showToast(getString(R.string.binging_fail));
                }
            }
        });
        hopeMusicHelper.setAddHopeMusciListener(new HopeMusicHelper.AddHopeMusciListener() {
            @Override
            public void addDeviceSuccess(String uid) {
                dismissDialog();
                ToastUtil.showToast(R.string.music_bind_success);
                Device device = new DeviceDao().selAllDevices(uid).get(0);
                Intent intent = new Intent(DeviceScannedResultActivity.this, DeviceSettingActivity.class);
                intent.putExtra(Constant.DEVICE, device);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_back_music);
                startActivity(intent);
                finish();
            }

            @Override
            public void addDeviceFail() {
                dismissDialog();
                ToastUtil.showToast(getString(R.string.binging_fail));
            }
        });
        hopeMusicHelper.setBindHopeMusicListener(new HopeMusicHelper.BindHopeMusicListener() {
            @Override
            public void bindDeviceSuccess(String deviceId) {
                hopeMusicHelper.startBind(qrcode, deviceId);
            }

            @Override
            public void bindDeviceFail(String error) {
                dismissDialog();
                ToastUtil.showToast(error);
            }
        });
        showDialogNow();
        hopeMusicHelper.loginHopeServer();

    }

    /**
     * 显示绑定手机号码
     */
    private boolean showBindPhoneDialog(){
        if (!TextUtils.isEmpty(userName) && !StringUtil.isPhone(userName)) {
            Account account = new AccountDao().selMainAccountdByUserName(userName);
            if (account != null) {
                String phone = account.getPhone();
                if (phone == null || TextUtils.isEmpty(phone)) {
                    DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                    dialogFragmentTwoButton.setTitle(getString(R.string.add_hope_music_bind_phone_title));
                    dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                    dialogFragmentTwoButton.setRightButtonText(getString(R.string.add_ys_bind_phone));
                    dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                        @Override
                        public void onLeftButtonClick(View view) {

                        }

                        @Override
                        public void onRightButtonClick(View view) {
                            Intent intent = new Intent(mAppContext, UserPhoneBindActivity.class);
                            intent.putExtra(Constant.GET_SMS_TYPE, GetSmsType.BIND_PHONE);
                            intent.putExtra(Constant.USER_BIND_TYPE, UserBindType.BIND_PHONE);
                            startActivity(intent);
                        }
                    });
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    dialogFragmentTwoButton.show(transaction, "");
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    public void showLoginDialog() {
        DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
        String title = getString(R.string.login_now_title);
        dialogFragment.setTitle(title);
        dialogFragment.setLeftButtonText(getString(R.string.cancel));
        dialogFragment.setRightButtonText(getString(R.string.login));
        dialogFragment.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
            }

            @Override
            public void onRightButtonClick(View view) {
                Intent intent = new Intent(DeviceScannedResultActivity.this, LoginActivity.class);
                intent.putExtra(Constant.LOGIN_ENTRY, loginEntryString);
                intent.putExtra(IntentKey.LOGIN_INTENT, loginIntent);
                startActivity(intent);
            }
        });
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(hopeMusicHelper!=null){
            hopeMusicHelper.release();
        }
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

    public void showUpdateVersionTipPopup() {
        mUpdateVersionTip.showPopup(mContext, getResources().getString(R.string.warm_tips),
                getResources().getString(R.string.update_version_tips),
                getResources().getString(R.string.know), null);

    }
}
