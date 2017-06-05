package com.orvibo.homemate.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/03/28
 */
public class RenameRoomNameActivity extends BaseActivity {

    private static final String TAG = RenameRoomNameActivity.class
            .getSimpleName();
    private EditTextWithCompound roomName_et;
    private Room room;
    private SavaPopup savaPopup;
    private ToastPopup toastPopup;

    // private String userName;
    // private String uid;
    private String roomName;
    private String newRoomName;

    // private int roomId;
    // private int floorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename_room_name);
        init();
        initView();
        initListener();
    }

    private void init() {
        mContext = RenameRoomNameActivity.this;
        Intent intent = getIntent();
        room = (Room) intent.getSerializableExtra("room");
        if (room != null) {
            roomName = room.getRoomName();
        } else {
            finish();
        }

        initView();
        initListener();
    }

    private void initView() {
        roomName_et = (EditTextWithCompound) findViewById(R.id.roomName_et);
        roomName_et.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        roomName_et.setNeedRestrict(false);
        if (!StringUtil.isEmpty(roomName)) {
            roomName = roomName_et.getText().toString();
            roomName_et.setSelection(roomName.length());
        }
        savaPopup = new SavaPopup();
        toastPopup = new ToastPopup();
    }

    private void initListener() {

    }

    public void rightTitleClick(View v) {
        reName();
    }

    public void leftTitleClick(View v) {
        newRoomName = roomName_et.getText().toString();
        if (!StringUtil.isEmpty(newRoomName) && !roomName.equals(newRoomName)) {
            savaPopup.showPopup(RenameRoomNameActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            this.finish();
        }
    }

    private void reName() {
        newRoomName = roomName_et.getText().toString();
        if (StringUtil.isEmpty(newRoomName)) {
            toastPopup.showPopup(RenameRoomNameActivity.this, getResources()
                    .getString(R.string.room_name_empty), getResources()
                    .getString(R.string.know), null);
            return;
        }

        if (roomName.equals(newRoomName)) {
            toastPopup.showPopup(RenameRoomNameActivity.this, getResources()
                    .getString(R.string.message_not_modified), getResources()
                    .getString(R.string.know), null);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("newRoomName", newRoomName);
        RenameRoomNameActivity.this.setResult(RESULT_OK, intent);
        RenameRoomNameActivity.this.finish();
    }

    public void save(View v) {
        reName();
    }

    private class SavaPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            newRoomName = roomName_et.getText().toString();
            Intent intent = new Intent();
            intent.putExtra("newRoomName", newRoomName);
            RenameRoomNameActivity.this.setResult(RESULT_OK, intent);
            RenameRoomNameActivity.this.finish();
        }

        public void cancel() {
            dismiss();
            finish();
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

    @Override
    public void onBackPressed() {
        newRoomName = roomName_et.getText().toString();
        if (!StringUtil.isEmpty(newRoomName) && !roomName.equals(newRoomName)) {
            savaPopup.showPopup(RenameRoomNameActivity.this, getResources()
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
        super.onDestroy();
    }

}
