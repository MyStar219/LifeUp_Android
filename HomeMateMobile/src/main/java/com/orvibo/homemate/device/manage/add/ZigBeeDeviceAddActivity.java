package com.orvibo.homemate.device.manage.add;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.QRCode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginType;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.FailReason;
import com.videogo.universalimageloader.core.assist.ImageScaleType;
import com.videogo.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Locale;

/**
 * 二维码扫描结果跳转添加设备界面
 * zigbee设备添加界面
 * Created by snown on 2015/11/6.
 */
public class ZigBeeDeviceAddActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = ZigBeeDeviceAddActivity.class.getName();
    //    public static final int CODE_EXIT_ADD_DEVICE = 2;
    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    private ImageView blueGrayImageView;
    private TextView tipTextView1, tipTextView2;
    private int productNameId;
    //    private AdminLogin          adminLogin;
    private QRCode mQRCode;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        mQRCode = (QRCode) getIntent().getSerializableExtra("QRCode");
        productNameId = getIntent().getIntExtra(Constant.CONFIG_TITLE, 0);
        initImageLoad();
        init();
//        initAdminLogin();
    }

    private void init() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView2 = (TextView) findViewById(R.id.tipTextView2);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        tipTextView2.setOnClickListener(this);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        //通过二维码扫描进来（并且该设备是一个新的类型）
        if (mQRCode != null) {
            DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(mQRCode.getQrCodeId(), getLanguage());
            if (deviceLanguage != null) {
                //  String productName = deviceLanguage.getProductName();
                String stepInfo = deviceLanguage.getStepInfo();
                //  COCO智能插线板@http://firmware.orvibo.com/pic/bg_coco_2.png@接通电源，长按开关6秒，直到WiFi指示灯闪烁
                String[] stepInfos = stepInfo.split("@");
                String productName = stepInfos[0];
                String picUrl = stepInfos[1];
                String tips = stepInfos[2];
                //    tipTextView1.setText(getString(R.string.device_add_s20c_text));
                if (tips != null) {
                    tipTextView1.setText(tips);
                }
                if (productName != null) {
                    navigationCocoBar.setCenterText(getString(R.string.add) + productName);
                }
                //  mImageLoader.displayImage(mQrCode.getPicUrl(), blueGrayImageView, mOptions, new ImageLoadingListener() {
                mImageLoader.displayImage(picUrl, blueGrayImageView, mOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        blueGrayImageView.setVisibility(View.GONE);
                        showDialogNow(null, getString(R.string.loading0));
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        dismissDialog();
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        blueGrayImageView.setVisibility(View.VISIBLE);
                        dismissDialog();
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        dismissDialog();
                    }
                });
            }
        }
        //通过设备列表进来
        if (productNameId == R.string.device_add_smoke_sensor || productNameId == R.string.device_add_co_sensor
                || productNameId == R.string.device_add_flooding_detector
                || productNameId == R.string.device_add_temperature_and_humidity_probe || productNameId == R.string.device_add_emergency_button) {
            tipTextView2.setVisibility(View.VISIBLE);
            tipTextView2.setText(R.string.device_add_tipTextView2_);
        } else if (productNameId == R.string.device_add_human_body_sensor || productNameId == R.string.device_add_magnetometer) {
            tipTextView2.setVisibility(View.VISIBLE);
        }
        //美标开关
        else if (productNameId == R.string.device_add_zigbee_smart_switch || productNameId == R.string.device_add_zigbee_smart_outlet || productNameId == R.string.device_add_zigbee_scene_switch || productNameId == R.string.device_add_zigbee_dimmer_switch) {
            tipTextView2.setVisibility(View.VISIBLE);
            tipTextView2.setText(R.string.device_add_tipTextView2_smart_switch);
            nextButton.setText(R.string.device_add_zigbee_tip_next);
        }
        tipTextView1.setText(getString(getStringByNameId(productNameId)));
        blueGrayImageView.setImageResource(getResourceIdByNameId(productNameId));
        Drawable drawable = blueGrayImageView.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).start();
        }

        String add = getString(R.string.add);
        navigationCocoBar.setCenterText(add + getString(productNameId));

        navigationCocoBar.setOnLeftClickListener(new NavigationCocoBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View v) {
//                exit();
                currentMainUid = UserCache.getCurrentMainUid(mContext);
                if (!StringUtil.isEmpty(currentMainUid) && ViHomeApplication.getInstance().isManage()) {
                    new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
                }
                ViHomeApplication.getInstance().setIsManage(false);
                finish();
            }
        });

    }


    private int getResourceIdByNameId(int nameId) {
        int resId = R.drawable.bg_add_switch;
        switch (nameId) {
            case R.string.device_add_switch:
                resId = R.drawable.bg_add_switch;
                break;
            case R.string.device_add_other_socket:
            case R.string.device_add_socket:
                resId = R.drawable.bg_add_socket;
                break;
            case R.string.device_add_iintelligent_door_lock:
                resId = R.drawable.bg_add_iintelligent_door_lock;
                break;
            case R.string.device_add_human_body_sensor:
                resId = R.drawable.bg_motion_sensor_flash;
                break;
            case R.string.device_add_magnetometer:
                resId = R.drawable.bg_door_window_sensor_flash;
                break;
            case R.string.device_add_curtain_motor:
                resId = R.drawable.bg_add_curtain_motor;
                break;
            case R.string.device_add_yingshi_camera:
                resId = R.drawable.bg_add_camera;
                break;
            case R.string.device_add_p2p_camera:
                resId = R.drawable.bg_add_camera;
                break;
            case R.string.device_add_remote_control:
            case R.string.device_add_zigbee_remote_control:
                resId = R.drawable.bg_add_remote_control;
                break;
            case R.string.device_add_control_box:
                resId = R.drawable.bg_add_control_box;
                break;
            case R.string.device_add_zigbee:
                resId = R.drawable.bg_add_zigbee;
                break;
            case R.string.device_add_light:
                resId = R.drawable.bg_add_light;
                break;
            case R.string.device_add_smoke_sensor:
                resId = R.drawable.bg_smoke_sensor_quick_flash;
                break;
            case R.string.device_add_flammable_gas_sensor:
                resId = R.drawable.pic_combustible_electrify;
                break;
            case R.string.device_add_co_sensor:
                resId = R.drawable.bg_co_sensor_quick_flash;
                break;
            case R.string.device_add_flooding_detector:
                resId = R.drawable.bg_flooding_sensor_quick_flash;
                break;
            case R.string.device_add_temperature_and_humidity_probe:
                resId = R.drawable.bg_temperature_sensor_quick_flash;
                break;
            case R.string.device_add_emergency_button:
                resId = R.drawable.bg_sos_sensor_quick_flash;
                break;
            //以下四个只在外语环境下出现
            case R.string.device_add_zigbee_smart_switch:
                resId = R.drawable.bg_smart_switch;
                break;
            case R.string.device_add_zigbee_smart_outlet:
                resId = R.drawable.bg_smart_outletg;
                break;
            case R.string.device_add_zigbee_scene_switch:
                resId = R.drawable.bg_scene_switch;
                break;
            case R.string.device_add_zigbee_dimmer_switch:
                resId = R.drawable.bg_dimmer_switch;
                break;
        }
        return resId;
    }

    private int getStringByNameId(int nameId) {
        int textId = R.string.device_add_switch_text;
        switch (nameId) {
            case R.string.device_add_switch:
                textId = R.string.device_add_switch_text;
                break;
            case R.string.device_add_other_socket:
            case R.string.device_add_socket:
                textId = R.string.device_add_socket_text;
                break;
            case R.string.device_add_iintelligent_door_lock:
                textId = R.string.device_add_iintelligent_door_lock_text;
                break;
            case R.string.device_add_human_body_sensor:
                textId = R.string.device_add_human_body_sensor_text;
                break;
            case R.string.device_add_magnetometer:
                textId = R.string.device_add_magnetometer_text;
                break;
            case R.string.device_add_curtain_motor:
                textId = R.string.device_add_curtain_motor_text;
                break;
            case R.string.device_add_yingshi_camera:
                textId = R.string.device_add_camera_text;
                break;
            case R.string.device_add_p2p_camera:
                textId = R.string.device_add_camera_text;
                break;
            case R.string.device_add_remote_control:
            case R.string.device_add_zigbee_remote_control:
                textId = R.string.device_add_remote_control_text;
                break;
            case R.string.device_add_control_box:
                textId = R.string.device_add_control_box_text;
                break;
            case R.string.device_add_zigbee:
                textId = R.string.device_add_zigbee_text;
                break;
            case R.string.device_add_s20c:
                textId = R.string.device_add_s20c_text;
                break;
            case R.string.device_add_yidong:
                textId = R.string.device_add_yidong_text;
                break;
            case R.string.device_add_light:
                textId = R.string.device_add_switch_text;
                break;
            case R.string.device_add_smoke_sensor:
            case R.string.device_add_co_sensor:
            case R.string.device_add_flooding_detector:
            case R.string.device_add_temperature_and_humidity_probe:
            case R.string.device_add_emergency_button:
                textId = R.string.device_add_sensor_text;
                break;
            case R.string.device_add_flammable_gas_sensor:
                textId = R.string.device_add_flammable_gas_sensor_text;
                break;

            //以下四个只在外语环境下出现
            case R.string.device_add_zigbee_smart_switch:
            case R.string.device_add_zigbee_smart_outlet:
            case R.string.device_add_zigbee_scene_switch:
            case R.string.device_add_zigbee_dimmer_switch:
                textId = R.string.device_add_zigbee_smart_switch_tips;
                break;
        }
        return textId;
    }

//    private void initAdminLogin() {
//        adminLogin = new AdminLogin(mAppContext) {
//            @Override
//            protected void onLogin(int result) {
//                dismissDialog();
//                if (result == ErrorCode.SUCCESS) {
//                    ViHomeApplication.getInstance().setIsManage(true);
//                    Intent intent = new Intent(mContext, AddVicenterActivity.class);
//                    intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
//                    int postion = 0;
//                    if (productNameId == R.string.device_add_human_body_sensor || productNameId == R.string.device_add_magnetometer) {
//                        postion = 5;
//                    } else if (productNameId == R.string.device_add_smoke_sensor || productNameId == R.string.device_add_co_sensor
//                            || productNameId == R.string.device_add_flooding_detector || productNameId == R.string.device_add_temperature_and_humidity_probe || productNameId == R.string.device_add_emergency_button) {
//                        postion = 6;
//                    }
//                    intent.putExtra(IntentKey.VICENTER_ADD_ACTION_POSITION, postion);
//                    startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
//                } else {
//                    String curUid = UserCache.getCurrentMainUid(mContext);
//                    if (StringUtil.isEmpty(curUid) && result == ErrorCode.GATEWAY_NOT_BINDED) {
//                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
//                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_BIND);
//                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
//                    } else if (result == ErrorCode.MNDS_NOT_FOUND_GATEWAY && isResumed) {
//                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                        dialogFragmentOneButton.setTitle(getString(R.string.FAIL));
//                        dialogFragmentOneButton.setContent(getString(R.string.binding_vicenter_fail));
//                        dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
//                        dialogFragmentOneButton.show(getFragmentManager(), "");
//                    } else {
//                        ToastUtil.toastError(result);
//                    }
//                }
//            }
//        };
//    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton: {
                //判断是否是网络可用
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                    return;
                }
                //判断网络类型是否是wifi（仅限本地操作）
                if (!NetUtil.isWifi(mAppContext)) {
                    ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
                } else {
                    //如果用户没有主机也不能添加
                    currentMainUid = UserCache.getCurrentMainUid(mContext);
                    String md5Password = UserCache.getMd5Password(mContext, userName);
                    if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(md5Password) || StringUtil.isEmpty(currentMainUid)) {
                        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                        dialogFragmentTwoButton.setTitle(getString(R.string.device_add_zigbee_failed_title));
                        dialogFragmentTwoButton.setContent(getString(R.string.device_add_zigbee_failed_content));
                        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.green));
                        dialogFragmentTwoButton.setRightButtonText(getString(R.string.device_add_zigbee_failed));
                        dialogFragmentTwoButton.setRightTextColor(getResources().getColor(R.color.green));
                        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                        dialogFragmentTwoButton.show(getFragmentManager(), "");
                    } else {

                        //去掉管理员登录 by huangqiyao at 2016/3/22
//                        showDialogNow();
                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
                        int postion = 0;
                        if (productNameId == R.string.device_add_human_body_sensor || productNameId == R.string.device_add_magnetometer) {
                            postion = 5;
                        } else if (productNameId == R.string.device_add_smoke_sensor || productNameId == R.string.device_add_co_sensor
                                || productNameId == R.string.device_add_flooding_detector || productNameId == R.string.device_add_temperature_and_humidity_probe || productNameId == R.string.device_add_emergency_button) {
                            postion = 6;
                        } else if (productNameId == R.string.device_add_flammable_gas_sensor) {
                            postion = 7;
                        }
                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_POSITION, postion);
                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);


                        // adminLogin.login(userName, md5Password, true);
//                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
//                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
//                        startActivityForResult(intent, CONTEXT_INCLUDE_CODE);
                    }
                }
                break;
            }
            case R.id.tipTextView2: {
                showTipDialog();
                break;
            }
        }
    }

    public void showTipDialog() {
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = -100;
        dialog.show();
        if (productNameId == R.string.device_add_human_body_sensor) {
            window.setContentView(R.layout.add_human_body_infrared_dialog);
            AnimationDrawable drawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
            drawable.start();
        } else if (productNameId == R.string.device_add_magnetometer) {
            window.setContentView(R.layout.add_magnetometer_dialog);
            AnimationDrawable drawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
            drawable.start();
        } else {
            window.setContentView(R.layout.add_sensor_dialog);
            if (productNameId == R.string.device_add_smoke_sensor) {
                ((ImageView) window.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_smoke_sensor);
            } else if (productNameId == R.string.device_add_co_sensor) {
                ((ImageView) window.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_co_sensor);
            } else if (productNameId == R.string.device_add_flooding_detector) {
                ((ImageView) window.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_flooding_sensor);
            } else if (productNameId == R.string.device_add_temperature_and_humidity_probe) {
                ((ImageView) window.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_temperature_sensor);
            } else if (productNameId == R.string.device_add_emergency_button) {
                ((ImageView) window.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_sos_sensor);
            }
        }
        /**
         * version  1.9 美标开关
         */
        switch (productNameId) {
            case R.string.device_add_zigbee_smart_switch:
                window.setContentView(R.layout.add_overside_smart_switch_dialog);
                ((ImageView) window.findViewById(R.id.tipImageView)).setImageResource(R.drawable.bg_smart_switch_guide);
                AnimationDrawable smartSwitchDrawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
                smartSwitchDrawable.start();
                break;
            case R.string.device_add_zigbee_smart_outlet:
                window.setContentView(R.layout.add_overside_smart_switch_dialog);
                ((ImageView) window.findViewById(R.id.tipImageView)).setImageResource(R.drawable.bg_smart_outlet_guide);
                AnimationDrawable smartOutletrDawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
                smartOutletrDawable.start();
                break;
            case R.string.device_add_zigbee_scene_switch:
                window.setContentView(R.layout.add_overside_smart_switch_dialog);
                ((ImageView) window.findViewById(R.id.tipImageView)).setImageResource(R.drawable.bg_scenes_switch_guide);
                AnimationDrawable sceneSwitchDrawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
                sceneSwitchDrawable.start();
                break;
            case R.string.device_add_zigbee_dimmer_switch:
                window.setContentView(R.layout.add_overside_smart_switch_dialog);
                ((ImageView) window.findViewById(R.id.tipImageView)).setImageResource(R.drawable.bg_dimmer_switch_guide);
                AnimationDrawable dimmerSwitchDrawable = (AnimationDrawable) ((ImageView) window.findViewById(R.id.tipImageView)).getDrawable();
                dimmerSwitchDrawable.start();
                break;
        }
        window.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void exit() {
        // vihome need to exit admin login.
        currentMainUid = UserCache.getCurrentMainUid(mContext);
        if (!StringUtil.isEmpty(currentMainUid) && ViHomeApplication.getInstance().isManage()) {
            new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
        }
        ViHomeApplication.getInstance().setIsManage(false);
        setResult(ResultCode.FINISH);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-resultCode:" + resultCode);
        if (resultCode == ResultCode.FINISH) {
            finish();
//        } else if (resultCode == CODE_EXIT_ADD_DEVICE) {
//            setResult(ResultCode.FINISH);
//            finish();
        }
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    @Override
    public void onLeftButtonClick(View view) {

    }

    @Override
    public void onRightButtonClick(View view) {
        Intent intent = new Intent(mContext, AddVicenterTipActivity.class);
        startActivityForResult(intent, 0);
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
                .imageScaleType(ImageScaleType.EXACTLY) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .build();

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
