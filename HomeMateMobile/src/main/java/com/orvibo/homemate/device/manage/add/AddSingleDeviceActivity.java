package com.orvibo.homemate.device.manage.add;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.BaseActivity;
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

/**
 * 二维码扫描结果跳转添加设备界面
 * Created by snown on 2015/11/6.
 */
public class AddSingleDeviceActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = AddSingleDeviceActivity.class.getName();
    //    public static final int CODE_EXIT_ADD_DEVICE = 2;
    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    private ImageView blueGrayImageView;
    private TextView tipTextView1, tipTextView2;
    private int productNameId;
//    private AdminLogin adminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        productNameId = getIntent().getIntExtra(Constant.CONFIG_TITLE, 0);
        init();
//        initAdminLogin();
    }

    private void init() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        blueGrayImageView.setImageResource(getResourceIdByNameId(productNameId));
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView2 = (TextView) findViewById(R.id.tipTextView2);
        tipTextView2.setOnClickListener(this);
        if (productNameId == R.string.device_add_human_body_sensor || productNameId == R.string.device_add_magnetometer) {
            tipTextView2.setVisibility(View.VISIBLE);
        }
        tipTextView1.setText(getString(getStringByNameId(productNameId)));
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
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
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
    }


    private int getResourceIdByNameId(int nameId) {
        int resId = R.drawable.bg_add_switch;
        switch (nameId) {
            case R.string.device_add_switch:
                resId = R.drawable.bg_add_switch;
                break;
            case R.string.device_add_socket:
                resId = R.drawable.bg_add_socket;
                break;
            case R.string.device_add_iintelligent_door_lock:
                resId = R.drawable.bg_add_iintelligent_door_lock;
                break;
            case R.string.device_add_human_body_sensor:
                resId = R.drawable.bg_human_body_infrared;
                break;
            case R.string.device_add_magnetometer:
                resId = R.drawable.bg_magnetometer;
                break;
            case R.string.device_add_curtain_motor:
                resId = R.drawable.bg_add_curtain_motor;
                break;
            case R.string.device_add_camera:
                resId = R.drawable.bg_add_camera;
                break;
            case R.string.device_add_remote_control:
                resId = R.drawable.bg_add_remote_control;
                break;
            case R.string.device_add_control_box:
                resId = R.drawable.bg_add_control_box;
                break;
            case R.string.device_add_zigbee:
                resId = R.drawable.bg_add_zigbee;
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
            case R.string.device_add_camera:
                textId = R.string.device_add_camera_text;
                break;
            case R.string.device_add_remote_control:
                textId = R.string.device_add_remote_control_text;
                break;
            case R.string.device_add_control_box:
                textId = R.string.device_add_control_box_text;
                break;
            case R.string.device_add_zigbee:
                textId = R.string.device_add_zigbee_text;
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
//                    } else if(productNameId == R.string.device_add_flammable_gas_sensor) {
//                        postion = 7;
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
                if (!NetUtil.isWifi(mContext)) {
                    ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
                } else {
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
                        // showDialogNow();
                        Intent intent = new Intent(mContext, AddVicenterActivity.class);
                        intent.putExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, AddVicenterActivity.ACTION_TYPE_SEARCH_DEVICE);
                        int postion = 0;
                        if (productNameId == R.string.device_add_human_body_sensor || productNameId == R.string.device_add_magnetometer) {
                            postion = 5;
                        } else if (productNameId == R.string.device_add_smoke_sensor || productNameId == R.string.device_add_co_sensor
                                || productNameId == R.string.device_add_flooding_detector || productNameId == R.string.device_add_temperature_and_humidity_probe || productNameId == R.string.device_add_emergency_button) {
                            postion = 6;
                        } else if(productNameId == R.string.device_add_flammable_gas_sensor) {
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
        } else {
            window.setContentView(R.layout.add_magnetometer_dialog);
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
}
