package com.orvibo.homemate.room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.ModifyRoom;
import com.orvibo.homemate.room.adapter.RoomManagerFloorListAdapter;
import com.orvibo.homemate.util.FloorAndRoomUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/03/28
 */
public class ModifyRoomActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ModifyRoomActivity.class
            .getSimpleName();

    private EditTextWithCompound roomName_et;
    private LinearLayout roomTypeLinearLayout;
    private ImageView roomTypeImageView;
    private String uid;
    private RoomManagerFloorListAdapter roomManagerFloorListAdapter;
    private SavePopup savaPopup;
    private Room room;
    private String roomName;
    private String newRoomName;
    private String roomId;
    private String floorId;
    private int roomType;
    private int newRoomType;
    private ModifyRoomControl modifyRoomControl;
    private final int SELECT_ROOM_TYPE = 1;

    private final int RESULT_CODE_MODIFY_ROOM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_room);
        init();
        initView();
        initListener();
    }

    private void init() {
        savaPopup = new SavePopup();
        Intent intent = getIntent();
        room = (Room) intent.getSerializableExtra("room");
        roomName = room.getRoomName();
        roomId = room.getRoomId();
        floorId = room.getFloorId();
        roomType = room.getRoomType();
        newRoomType = room.getRoomType();
        uid = room.getUid();

        modifyRoomControl = new ModifyRoomControl(mAppContext);
    }

    private void initView() {
        roomName_et = (EditTextWithCompound) findViewById(R.id.roomName_et);
        roomName_et.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        roomName_et.setNeedRestrict(false);
        roomName_et.setText(roomName);
        roomName_et.setRightful(getResources().getDrawable(R.color.transparent));
        if (!StringUtil.isEmpty(roomName)) {
            roomName = roomName_et.getText().toString();
            roomName_et.setSelection(roomName.length());
        }
        roomTypeLinearLayout = (LinearLayout) findViewById(R.id.roomTypeLinearLayout);
        roomTypeLinearLayout.setOnClickListener(this);
        roomTypeImageView = (ImageView) findViewById(R.id.roomTypeImageView);
        roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(roomType, true));
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
            dismiss();
            finish();
        }
    }

    public void save(View view) {
        modifyRoomName();
    }

    public void leftTitleClick(View v) {
        onBackPressed();
    }

    private void modifyRoomName() {
        newRoomName = roomName_et.getText().toString();
        if (StringUtil.isEmpty(newRoomName)) {
            ToastUtil.showToast(R.string.room_name_empty);
            return;
        }

        if (roomName.equals(newRoomName)&&roomType == newRoomType) {
            finish();
        }
        showDialogNow();
        modifyRoomControl.startModifyRoom(userName, uid, roomId, newRoomName, floorId, room.getRoomType());
    }

    private class ModifyRoomControl extends ModifyRoom {

        public ModifyRoomControl(Context context) {
            super(context);
        }

        @Override
        public void onModifyRoomResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra("newRoomName", newRoomName);
                intent.putExtra("newRoomType", newRoomType);
                setResult(RESULT_CODE_MODIFY_ROOM, intent);
                finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        newRoomName = roomName_et.getText().toString();
        if (!StringUtil.isEmpty(newRoomName) && !roomName.equals(newRoomName) || roomType != newRoomType) {
            savaPopup.showPopup(ModifyRoomActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.roomTypeLinearLayout:
                Intent intent = new Intent(mContext, SelectRoomTypeActivity.class);
                intent.putExtra("roomType", roomType);
                intent.putExtra("srcActivity", ModifyRoomActivity.class.getSimpleName());
                startActivityForResult(intent, SELECT_ROOM_TYPE);
        }

    }

    /**
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * <p/>
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case SELECT_ROOM_TYPE:
                if (data != null) {
                    newRoomType = data.getIntExtra("roomType", 0);
                    room.setRoomType(newRoomType);
                    roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(newRoomType, true));
                }
                break;
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
        super.onDestroy();
    }

}
