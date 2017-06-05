package com.orvibo.homemate.device.manage.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.irlearn.DeviceSetSelfRemoteIrLearnActivity;
import com.orvibo.homemate.model.AddIrKey;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

/**
 * Created by Allen on 2015/4/24.
 * Modified by smagret on 2015/05/09
 */
public class DeviceSetSelfRemoteButtonNameActivity extends BaseActivity {
    private Button btnSave;
    private EditTextWithCompound etRemoteName;
    private String keyName;
    private Device device;
    private AddIrKeyControl addIrKeyControl;
    private String uid;
    private String deviceId;
    private ToastPopup toastPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_self_remote_button_name);
        device = (Device) getIntent().getSerializableExtra("device");
        uid = device.getUid();
        deviceId = device.getDeviceId();
        findViews();
        init();

    }

    private void findViews() {
        btnSave = (Button) findViewById(R.id.btnSave);
        etRemoteName = (EditTextWithCompound) findViewById(R.id.etRemoteName);
        etRemoteName.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH_COMMON);
        toastPopup = new ToastPopup();
    }

    private void init() {
        addIrKeyControl = new AddIrKeyControl(mAppContext);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                keyName = etRemoteName.getText().toString();

                if (StringUtil.isEmpty(keyName)) {
                    toastPopup.showPopup(DeviceSetSelfRemoteButtonNameActivity.this, getResources()
                            .getString(R.string.ir_key_name_empty), getResources()
                            .getString(R.string.know), null);
                } else if (StringUtil.isHasSpecialChar(keyName)) {
                    ToastUtil.showToast(R.string.SPECIAL_CHAR_ERROR);
                } else {
                    showDialogNow();
                    addIrKeyControl.startAddIrKey(uid, userName, deviceId, keyName, 0, null,0,0);
                }
                break;
        }

    }

    private class AddIrKeyControl extends AddIrKey {

        public AddIrKeyControl(Context context) {
            super(context);
        }

        @Override
        public void onAddIrKeyResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent(DeviceSetSelfRemoteButtonNameActivity.this, DeviceSetSelfRemoteIrLearnActivity.class);
                intent.putExtra("keyName", keyName);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                DeviceSetSelfRemoteButtonNameActivity.this.finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }

        }
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

}
