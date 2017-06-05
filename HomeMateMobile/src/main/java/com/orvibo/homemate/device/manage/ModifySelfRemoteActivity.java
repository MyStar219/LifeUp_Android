package com.orvibo.homemate.device.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.DeleteIrKey;
import com.orvibo.homemate.model.ModifyIrKey;
import com.orvibo.homemate.room.adapter.RoomManagerFloorListAdapter;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/06/02
 */
public class ModifySelfRemoteActivity extends BaseActivity {
    private static final String TAG = ModifySelfRemoteActivity.class
            .getSimpleName();

    private EditTextWithCompound keyName_et;
    private String uid;
    private RoomManagerFloorListAdapter roomManagerFloorListAdapter;
    private SavePopup savaPopup;
    private DeletePopup deletePopup;
    private ToastPopup toastPopup;
    private DeviceIr deviceIr;
    private String keyName;
    private String newKeyName;
    private String deviceIrId;

    private ModifyIrKeyControl modifyIrKeyControl;
    private DeleteIrKeyControl deleteIrKeyControl;

    private final int RESULT_CODE_MODIFY_SELF_REMOTE = 4;
    private final int RESULT_CODE_DELETE_SELF_REMOTE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_self_remote);
        init();
        initView();
        initListener();
    }

    private void init() {
        savaPopup = new SavePopup();
        deletePopup = new DeletePopup();
        toastPopup = new ToastPopup();
        Intent intent = getIntent();
        deviceIr = (DeviceIr) intent.getSerializableExtra("deviceIr");
        keyName = deviceIr.getKeyName();
        deviceIrId = deviceIr.getDeviceIrId();
        uid = deviceIr.getUid();

        modifyIrKeyControl = new ModifyIrKeyControl(mAppContext);
        deleteIrKeyControl = new DeleteIrKeyControl(mAppContext);
    }

    private void initView() {
        keyName_et = (EditTextWithCompound) findViewById(R.id.keyName_et);
        keyName_et.setText(keyName);
        keyName_et.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH_COMMON);
        if (!StringUtil.isEmpty(keyName)) {
            keyName = keyName_et.getText().toString();
            keyName_et.setSelection(keyName.length());
        }
    }

    private void initListener() {

    }

    private class SavePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            modifyRoomName();
            dismiss();
        }

        public void cancel() {
            finish();
        }
    }

    private class DeletePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            deleteIrKeyControl.startDeleteIrKey(userName, uid, deviceIrId,null,0);
            showDialog();
            dismiss();
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

    public void save(View view) {
        modifyRoomName();
    }


    /**
     * 删除按键
     *
     * @param view
     */
    public void rightTitleClick(View view) {
        deletePopup.showPopup(ModifySelfRemoteActivity.this, getResources()
                .getString(R.string.delete_ir_key_tips), getResources()
                .getString(R.string.delete), getResources()
                .getString(R.string.cancel));
    }

    public void leftTitleClick(View v) {
        newKeyName = keyName_et.getText().toString();
        if (!StringUtil.isEmpty(newKeyName) && !keyName.equals(newKeyName)) {
            savaPopup.showPopup(ModifySelfRemoteActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            this.finish();
        }
    }

    private void modifyRoomName() {
        newKeyName = keyName_et.getText().toString();
        if (StringUtil.isHasSpecialChar(newKeyName)) {
            ToastUtil.showToast(R.string.SPECIAL_CHAR_ERROR);
            return;
        }
        if (StringUtil.isEmpty(newKeyName)) {
            toastPopup.showPopup(ModifySelfRemoteActivity.this, getResources()
                    .getString(R.string.key_name_empty), getResources()
                    .getString(R.string.know), null);
            return;
        }

        if (keyName.equals(newKeyName)) {
//            toastPopup.showPopup(ModifyRoomActivity.this, getResources()
//                    .getString(R.string.message_not_modified), getResources()
//                    .getString(R.string.know));
//            return;
        }
        showDialog();
        modifyIrKeyControl.startModifyIrKey(userName, uid, deviceIrId, null,newKeyName);
    }

    private class ModifyIrKeyControl extends ModifyIrKey {

        public ModifyIrKeyControl(Context context) {
            super(context);
        }

        @Override
        public void onModifyIrKeyResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                ModifySelfRemoteActivity.this.setResult(RESULT_CODE_MODIFY_SELF_REMOTE, intent);
                ModifySelfRemoteActivity.this.finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class DeleteIrKeyControl extends DeleteIrKey {

        public DeleteIrKeyControl(Context context) {
            super(context);
        }

        @Override
        public void onDeleteIrKeyResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                ModifySelfRemoteActivity.this.setResult(RESULT_CODE_DELETE_SELF_REMOTE, intent);
                ModifySelfRemoteActivity.this.finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        newKeyName = keyName_et.getText().toString();
        if (!StringUtil.isEmpty(newKeyName) && !keyName.equals(newKeyName)) {
            savaPopup.showPopup(ModifySelfRemoteActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (savaPopup != null && savaPopup.isShowing()) {
            savaPopup.dismiss();
        }

        if (toastPopup != null && toastPopup.isShowing()) {
            toastPopup.dismiss();
        }

        if (deletePopup != null && deletePopup.isShowing()) {
            deletePopup.dismiss();
        }
        super.onDestroy();
    }

}
