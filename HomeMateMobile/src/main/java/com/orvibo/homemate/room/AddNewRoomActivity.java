package com.orvibo.homemate.room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.AddRoom;
import com.orvibo.homemate.util.FloorAndRoomUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

/**
 * @author Smagret
 * @data 2015/03/28
 */
public class AddNewRoomActivity extends BaseActivity {

    private static final String TAG = AddNewRoomActivity.class.getSimpleName();
    private EditTextWithCompound roomName_et;
    private ImageView            roomTypeImageView;
    private LinearLayout         roomTypeLinearLayout;
    private Context              mContext;

    private ConfirmAndCancelPopup savaPopup;

    private AddRoomControl addRoomControl;
    private String         floorId;
    private String         srcActivity;
    private String         roomName = "";
    private int            roomType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_room);
        init();
        initView();
        initListener();
    }

    private void init() {
        mContext = AddNewRoomActivity.this;
        addRoomControl = new AddRoomControl(mAppContext);
        floorId = getIntent().getStringExtra("floorId");
        roomType = getIntent().getIntExtra("roomType", 0);
        srcActivity = getIntent().getStringExtra("srcActivity");
        roomName = getIntent().getStringExtra("roomName");
        initView();
        initListener();
    }

    private void initView() {
        roomName_et = (EditTextWithCompound) findViewById(R.id.roomName_et);
        roomName_et.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        roomName_et.setNeedRestrict(false);
        if (srcActivity != null && !srcActivity.equals(AddNewRoomActivity.class.getSimpleName())) {
            roomName = getResources().getString(FloorAndRoomUtil.getRoomNameByRoomType(roomType));
        }
        roomName_et.setText(roomName);
        roomName_et.setRightful(getResources().getDrawable(R.color.transparent));
        if (!StringUtil.isEmpty(roomName)) {
            roomName = roomName_et.getText().toString();
            roomName_et.setSelection(roomName.length());
        }
        roomName_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                roomName = editable.toString();
            }
        });
        roomTypeImageView = (ImageView) findViewById(R.id.roomTypeImageView);
        roomTypeLinearLayout = (LinearLayout) findViewById(R.id.roomTypeLinearLayout);
        roomTypeLinearLayout.setOnClickListener(this);
        roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(roomType, true));
        savaPopup = new ConfirmAndCancelPopup() {
            @Override
            public void confirm() {
                Intent intent = new Intent();
                intent.putExtra("roomType", roomType);
                intent.putExtra("floorId", floorId);
                intent.putExtra("roomName", roomName);
                intent.putExtra("srcActivity", AddNewRoomActivity.class.getSimpleName());
                setResult(RESULT_CANCELED, intent);
                dismiss();
                finish();
            }
        };
    }

    private void initListener() {

    }

    public void confirm(View v) {
        modifyRoomName();
    }

    public void leftTitleClick(View v) {
        cancel();
    }

    public void cancel() {
        savaPopup.showPopup(AddNewRoomActivity.this, getResources().getString(R.string.save_add_room_or_not), getResources().getString(R.string.quit),
                getResources().getString(R.string.continue_set));
    }

    private void modifyRoomName() {
        roomName = roomName_et.getText().toString();
        if (StringUtil.isEmpty(roomName)) {
            ToastUtil.showToast(R.string.room_name_empty);
        } else {
            addRoomControl.startAddRoom(userName, currentMainUid, roomName, floorId, roomType);
            showDialog();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.roomTypeLinearLayout:
                Intent intent = new Intent();
                intent.putExtra("srcActivity", AddNewRoomActivity.class.getSimpleName());
                intent.putExtra("roomType", roomType);
                intent.putExtra("floorId", floorId);
                intent.putExtra("roomName", roomName);
                setResult(RESULT_CANCELED, intent);
                finish();
        }

    }

    private class AddRoomControl extends AddRoom {

        public AddRoomControl(Context context) {
            super(context);
        }

        @Override
        public void onAddRoomResult(String uid, int serial, int result, String roomId) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra("roomId", roomId);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class SavePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            super.cancel();
            finish();
        }

        public void cancel() {
            dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        cancel();
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
