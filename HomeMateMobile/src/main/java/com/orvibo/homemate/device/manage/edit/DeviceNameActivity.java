package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.model.ModifyHomeName;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/5/28.
 */
public class DeviceNameActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener {
    private NavigationCocoBar navigationBar;
    private EditTextWithCompound deviceNameEditText;
    private Button saveButton;
    private Device device;
    private ModifyDevice modifyDevice;
    private DoorUserData doorUserData;

    //    private AdminLogin adminLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_nickname);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        navigationBar.setCenterText(getString(R.string.device_name_change_hint));
        deviceNameEditText = (EditTextWithCompound) findViewById(R.id.userNicknameEditText);
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        doorUserData = (DoorUserData) getIntent().getSerializableExtra(IntentKey.DOOR_USER_DATA);
//        if (ProductManage.getInstance().isCOCO(device)) {
//            deviceNameEditText.setNeedRestrict(false);
//        }

        if (ProductManage.getInstance().isWifiDeviceByModel(device.getModel())) {
            deviceNameEditText.setNeedRestrict(false);
        }
        String deviceName = device.getDeviceName();
        deviceNameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        if (doorUserData != null) {
            //门锁用户名称还是16位
            deviceNameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH_COMMON);
            deviceNameEditText.setText(TextUtils.isEmpty(doorUserData.getName()) ? "" : doorUserData.getName());
            navigationBar.setCenterText(getString(R.string.smart_lock_member_set));
        } else {
            deviceNameEditText.setText(deviceName);
            deviceNameEditText.setHint(R.string.device_set_input_devicename_hint);
            if (!StringUtil.isEmpty(deviceName)) {
                String tempName = deviceNameEditText.getText().toString().trim();
                deviceNameEditText.setSelection(tempName.length());
            }
        }


        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        modifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                super.onModifyDeviceResult(uid, serial, result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    Intent intent = new Intent();
                    intent.putExtra(Constant.DEVICE, device);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (result == ErrorCode.TIMEOUT_MD) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
                } else {
                    if (result == ErrorCode.NO_ADMIN_PERMISSIONS) {
                        ToastUtil.showToast(getString(R.string.NO_ADMIN_PERMISSIONS));
                    } else if (!ToastUtil.toastCommonError(result)) {
                        ToastUtil.showToast(getString(R.string.device_name_change_fail));
                    }
                }
            }
        };
//        adminLogin = new AdminLogin(this) {
//            @Override
//            protected void onLogin(int result) {
//                if (result == ErrorCode.SUCCESS) {
//                    modifyHomeName.startModifyHomeName(userName, currentMainUid, device.getDeviceName());
//                } else {
//                    ToastUtil.toastError(DeviceNameActivity.this, result);
//                    dismissDialog();
//                }
//            }
//        };
    }

    private ModifyHomeName modifyHomeName = new ModifyHomeName(this) {
        @Override
        public void onModifyHomeNameResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra(Constant.DEVICE, device);
                setResult(RESULT_OK, intent);
                finish();
            } else if (result == ErrorCode.TIMEOUT_MD) {
                ToastUtil.showToast(getString(R.string.TIMEOUT));
            } else {
                ToastUtil.showToast(getString(R.string.device_name_change_fail));
            }
//            logout();
        }
    };

    @Override
    public void onLeftClick(View v) {
//        if (device.getDeviceType() == DeviceType.VICENTER) {//退出管理员登录
//            logout();
//        }
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (!device.getDeviceName().equals(deviceNameEditText.getText().toString())) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceName_Back), null);
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.save_content));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.unsave));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.save));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    finish();
                }

                @Override
                public void onRightButtonClick(View view) {
                    save();
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    //    private void logout() {
//        new Logout(DeviceNameActivity.this).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingCOCO_DeviceName_Save), null);
                save();
                break;
            }
        }
    }

    private void save() {
        // 判断网络是否连接
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        if (StringUtil.isEmpty(deviceNameEditText.getText().toString())) {
            if (doorUserData != null) {
                ToastUtil.showToast(getString(R.string.common_enter) + getString(R.string.lock_member_set_name));
            } else
                ToastUtil.showToast(R.string.device_setting_name_input, Toast.LENGTH_SHORT);
            return;
        }
//        if (StringUtil.isHasSpecialChar(deviceNameEditText.getText().toString())
//                && !ProductManage.getInstance().isWifiDeviceByModel(device.getModel())) {
//            ToastUtil.showToast(R.string.SPECIAL_CHAR_ERROR);
//            return;
//        }
        if (doorUserData != null) {
            showDialog();
            DeviceApi.authSetName(userName, currentMainUid, doorUserData.getDoorUserId(), deviceNameEditText.getText().toString(), new BaseResultListener() {
                @Override
                public void onResultReturn(BaseEvent baseEvent) {
                    dismissDialog();
                    if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                        Intent intent = new Intent();
                        intent.putExtra(Constant.DEVICE, device);
                        doorUserData.setName(deviceNameEditText.getText().toString());
                        intent.putExtra(IntentKey.DOOR_USER_DATA, doorUserData);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if (baseEvent.getResult() == ErrorCode.TIMEOUT_MD) {
                        ToastUtil.showToast(getString(R.string.TIMEOUT));
                    } else {
                        ToastUtil.showToast(getString(R.string.FAIL));
                    }
                }
            });
        } else {
            showDialog(null, getString(R.string.device_name_changing));
            device.setDeviceName(deviceNameEditText.getText().toString());
            if (ProductManage.getInstance().isVicenter300ByDeviceType(device.getDeviceType())) {
                modifyHomeName.startModifyHomeName(userName, currentMainUid, device.getDeviceName());
            } else {
                modifyDevice.modify(device.getUid(), UserCache.getCurrentUserName(mAppContext), device.getDeviceName(), device.getDeviceType(), device.getRoomId(), device.getIrDeviceId(), device.getDeviceId());
            }
        }
    }

}
