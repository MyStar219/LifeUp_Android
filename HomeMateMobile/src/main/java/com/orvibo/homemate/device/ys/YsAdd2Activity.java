package com.orvibo.homemate.device.ys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.UserBindType;
import com.orvibo.homemate.device.manage.DeviceSettingActivity;
import com.orvibo.homemate.model.DeviceBind;
import com.orvibo.homemate.model.DeviceUnbind;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.core.MinaSocket;
import com.orvibo.homemate.user.UserPhoneBindActivity;
import com.orvibo.homemate.util.HttpTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;

import java.util.List;

/**
 * Created by allen on 2015/11/12.
 */
public class YsAdd2Activity extends BaseActivity {
    private static final String TAG = YsAdd2Activity.class.getName();
    private ImageView productImageView;
    private TextView productNameTextView, companyNameTextView, tipsTextView;
    private Button nextButton;
    private String phone;
    private String serial, code, model;
    private int cameraType = CameraType.YS_NO_YUNTAI;
    private Handler handler;
    private static final int GET_CAMERA_INFO = 1;
    private static final int REGISTER_TO_YS = 2;
    private static final int ADD_DEVICE = 3;
    private boolean available, isOnline, hasAddInYS;
    private int registerCount;
    private DeviceUnbind deviceUnbind;
    private DeviceBind deviceBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unbind_device);
        findViews();
        init();
    }

    private void findViews() {
        productImageView = (ImageView) findViewById(R.id.productImageView);
        productNameTextView = (TextView) findViewById(R.id.productNameTextView);
        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
        tipsTextView = (TextView) findViewById(R.id.tipsTextView);
        nextButton = (Button) findViewById(R.id.nextButton);
    }

    private void init() {
        String result = getIntent().getStringExtra(IntentKey.DEVICE);
        if (!TextUtils.isEmpty(result)) {
            String[] deviceInfo = result.split("\r");
            if (deviceInfo.length > 3) {
                serial = deviceInfo[1];
                code = deviceInfo[2];
                model = deviceInfo[3];
            }
        }
        if (serial.isEmpty() || code.isEmpty() || model.isEmpty()) {
            finish();
            return;
        }
//        serial = "474543271";
//        code = "MHPJRZ";
//        model = "CS-C2S-21WPFR";
//        serial = "551912726";
//        code = "QMMFNY";
//        model = "CS-C2C-31WFR";
//        serial = "527459718";
//        code = "HKJUVV";
//        model = "CS-CO6-21WFR";
        StringBuilder cameraInfoStr = new StringBuilder();
        cameraInfoStr.append(getString(R.string.ys_camera));
        String[] modelStrs = model.split("-");
        if (modelStrs.length > 1) {
            cameraInfoStr.append(modelStrs[1]);
            if (modelStrs[1].equals("C2C")) {
                productImageView.setImageResource(R.drawable.device_500_yingshi_c2c);
                cameraType = CameraType.YS_NO_YUNTAI;
            } else if (modelStrs[1].equals("CO6")) {
                productImageView.setImageResource(R.drawable.device_500_yingshi_c6);
                cameraType = CameraType.YS_YUNTAI;
            } else {
                productImageView.setVisibility(View.INVISIBLE);
            }
        }
        if (!TextUtils.isEmpty(serial)) {
            cameraInfoStr.append("(");
            cameraInfoStr.append(serial);
            cameraInfoStr.append(")");
        }
        productNameTextView.setText(cameraInfoStr.toString());
        companyNameTextView.setText(getString(R.string.ys_company));
        nextButton.setOnClickListener(this);
        handler = new Handler(new MyHandlerCallback());
        initDeviceBind();
        check();
    }

    private void initDeviceBind() {
        deviceUnbind = new DeviceUnbind() {
            @Override
            public void onUnbindResult(final String uid, int serial, int result) {
                Log.d(TAG, "onUnbindResult result:" + result);
//                if (result == com.orvibo.homemate.data.ErrorCode.SUCCESS) {
                deviceBind.bindCamera(mAppContext, YsAdd2Activity.this.serial, userName, code, cameraType);
//                } else  {
//                    available = false;
//                    check();
//                    ToastUtil.showToast(getString(R.string.add_device_fail));
//                }

            }
        };
        deviceBind = new DeviceBind() {
            @Override
            public void onBindResult(String uid, int serial, int result) {
                super.onBindResult(uid, serial, result);
                Message message = handler.obtainMessage();
                message.what = ADD_DEVICE;
                message.obj = (result == com.orvibo.homemate.data.ErrorCode.SUCCESS);
                handler.sendMessage(message);
            }
        };
    }

    private boolean check() {
        boolean isNetworkAvailable = NetUtil.isNetworkEnable(mAppContext);
        if (!isNetworkAvailable) {
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.net_not_connect));
            dialogFragmentOneButton.setContent(getString(R.string.net_not_connect_content));
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.show(getFragmentManager(), "");
            return false;
        }
        if (!MinaSocket.isServerConnected()) {
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.ap_not_connect_server));
            dialogFragmentOneButton.setContent(getString(R.string.ap_not_connect_server_content));
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.show(getFragmentManager(), "");
            return false;
        }

//        String userName = UserCache.getCurrentUserName(this);
        int logoutStatus = UserCache.getLoginStatus(this, userName);
        if (logoutStatus != LoginStatus.SUCCESS && logoutStatus != LoginStatus.FAIL) {
            showLoginDialog();
            return false;
        }
        phone = userName;
        if (!StringUtil.isPhone(userName)) {
            Account account = new AccountDao().selMainAccountdByUserName(userName);
            if (account != null) {
                phone = account.getPhone();
                if (TextUtils.isEmpty(phone)) {
                    DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                    dialogFragmentTwoButton.setTitle(getString(R.string.add_ys_bind_phone_title));
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
                    dialogFragmentTwoButton.show(getFragmentManager(), "");
                    return false;
                }
            } else {
                MyLogger.kLog().e(account + ",userName:" + userName);
            }
        }
        nextButton.setVisibility(View.INVISIBLE);
        getCameraInfo();
        showDialogNow(null, getString(R.string.ys_get_device_info));
        return true;
    }

    private void getCameraInfo() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Account account = new AccountDao().selAccountByUserId(UserCache.getCurrentUserId(mContext));
                if (account != null && !TextUtils.isEmpty(account.getPhone())) {
                    phone = account.getPhone();
                    String accessToken = HttpTool.getAccessToken(phone);
                    if (!TextUtils.isEmpty(accessToken)) {
                        EZOpenSDK.getInstance().setAccessToken(accessToken);
                    } else {
                        String code = HttpTool.register(phone);
                        if (code != null && code.equals("200")) {
                            accessToken = HttpTool.getAccessToken(phone);
                            EZOpenSDK.getInstance().setAccessToken(accessToken);
                        }
                    }
                }
                Message message = handler.obtainMessage();
                message.what = GET_CAMERA_INFO;
                try {
                    message.obj = EZOpenSDK.getInstance().getCameraInfo(serial);
                } catch (BaseException e) {
                    e.printStackTrace();
                    message.arg1 = e.getErrorCode();
                }
                handler.sendMessage(message);
            }
        }.start();
    }

    private void registerAccount2YS() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String code = HttpTool.register(phone);
                if (code != null && code.equals("200")) {
                    String accessToken = HttpTool.getAccessToken(phone);
                    EZOpenSDK.getInstance().setAccessToken(accessToken);
                    getCameraInfo();
                } else {
                    handler.sendEmptyMessage(REGISTER_TO_YS);
                }

            }
        }.start();
    }

    private void addDevice() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean isSuccess = false;
                try {
                    isSuccess = EZOpenSDK.getInstance().addDevice(serial, code);
                    if (isSuccess) {
                        beginUnbind();
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = ADD_DEVICE;
                    message.arg1 = e.getErrorCode();
                    message.obj = false;
                    handler.sendMessage(message);
                }
                Log.d(getClass().getName(), "isSuccess:" + isSuccess);
            }
        }.start();
    }

    private void beginUnbind() {
        deviceUnbind.unBind(mAppContext, serial);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton:
                if (available) {
                    if (isOnline) {
                        showDialogNow(null, getString(R.string.add_ys_device));
                        if (hasAddInYS) {//已经在萤石平台上绑定了，直接在我们服务器上解绑再绑定
                            new Thread() {
                                @Override
                                public void run() {
                                    beginUnbind();
                                }
                            }.start();
                        } else {
                            addDevice();
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(IntentKey.YS_DEVICE_SERIAL, serial);
                        intent.putExtra(IntentKey.YS_DEVICE_CODE, code);
                        intent.putExtra(IntentKey.YS_DEVICE_MODEL, model);
                        intent.setClass(this, YsAdd3Activity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    check();
                }
                break;
        }
    }


    class MyHandlerCallback implements android.os.Handler.Callback {

        @Override
        public boolean handleMessage(final Message message) {
            dismissDialog();
            switch (message.what) {
                case GET_CAMERA_INFO:
                    if (message.obj != null) {
                        List<Device> devices = new DeviceDao().selAllDevices(serial);
                        if (devices.isEmpty()) {
                            available = true;
                            isOnline = true;
                            hasAddInYS = true;
                            tipsTextView.setVisibility(View.GONE);
                            nextButton.setVisibility(View.VISIBLE);
                        } else {
                            tipsTextView.setText(getString(R.string.add_ys_device_exist));
                            tipsTextView.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.INVISIBLE);
                        }

//                        new Thread() {
//                            @Override
//                            public void run() {
//                                super.run();
//                                try {
//                                    DeleteDevice deleteDevice = new DeleteDevice(mAppContext);
//                                    List<Device> devices = new DeviceDao().selAllDevices(serial);
//                                    if (!devices.isEmpty()) {
//                                        Device device = devices.get(0);
//                                        deleteDevice.startDeleteDeviceToServer(device.getUid(), userName, "", device.getDeviceType());
//                                    }
//                                    EZOpenSDK.getInstance().deleteDevice(serial);
//                                } catch (BaseException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
                    }
                    LogUtil.d(TAG, "GET_CAMERA_INFO:" + message.arg1);
                    switch (message.arg1) {
                        case EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_OFFLINE_BUT_ADD:
                        case EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_ONLINE_ADDED:
                            available = false;
                            tipsTextView.setText(getString(R.string.add_ys_device_added));
                            tipsTextView.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.INVISIBLE);
                            break;
                        case EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_ONLINE_NOT_ADD:
                            available = true;
                            isOnline = true;
                            tipsTextView.setVisibility(View.GONE);
                            nextButton.setVisibility(View.VISIBLE);
                            break;
                        case EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_OFFLINE_NOT_ADD:
                        case EZConstants.EZOpenSDKError.ERROR_WEB_DEVICE_NOTEXIT:
                            available = true;
                            isOnline = false;
                            tipsTextView.setVisibility(View.GONE);
                            nextButton.setVisibility(View.VISIBLE);
                            break;
                        case EZConstants.EZOpenSDKError.VIDEOGONETSDK_SESSION_ERROR + 100000:
                        case EZConstants.EZOpenSDKError.ERROR_WEB_PARAM_ERROR:
                            available = false;
                            if (registerCount++ < 1) {
                                registerAccount2YS();
                            }
                            break;
                        case EZConstants.EZOpenSDKError.ERROR_WEB_NETWORK_EXCEPTION:
                            available = false;
                            ToastUtil.showToast(R.string.network_canot_work);
                            break;
                    }
                    break;
                case REGISTER_TO_YS:
                    available = false;
                    tipsTextView.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                    ToastUtil.showToast(getString(R.string.ys_get_device_info_fail));
                    break;
                case ADD_DEVICE:
                    boolean isSuccess = (boolean) message.obj;
                    if (isSuccess) {
                        Device device = new DeviceDao().selAllDevices(serial).get(0);
                        com.orvibo.homemate.util.LogUtil.d(TAG, "onLoadFinish- device: " + device);
                        Intent intent = new Intent(YsAdd2Activity.this, DeviceSettingActivity.class);
                        intent.putExtra(Constant.DEVICE, device);
                        startActivity(intent);
                        finish();
                    } else {
                        available = false;
                        check();
                        if (message.arg1 == EZConstants.EZOpenSDKError.ERROR_WEB_DIVICE_SO_TIMEOUT) {
                            ToastUtil.showToast(getString(R.string.add_ys_device_network_error));
                        } else {
                            ToastUtil.showToast(getString(R.string.add_device_fail));
                        }
                    }
                    break;
            }
            return true;
        }
    }
}
