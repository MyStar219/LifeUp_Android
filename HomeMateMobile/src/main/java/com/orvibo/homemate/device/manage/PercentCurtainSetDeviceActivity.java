package com.orvibo.homemate.device.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.FrequentlyModeId;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.edit.DeviceInfoActivity;
import com.orvibo.homemate.device.manage.edit.DeviceNameActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.InputUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetSelectRoomPopup;

public class PercentCurtainSetDeviceActivity extends BaseActivity {
    private View deviceName;
    private TextView deviceNameTextView;
    private TextView deviceInfoTextView;
    private LinearLayout modelId_one_ll, modelId_two_ll, modelId_three_ll, modelId_four_ll;
    private LinearLayout select_room_ll;
    private TextView modelId_one_name, modelId_one_value, modelId_two_name, modelId_two_value, modelId_three_name, modelId_three_value, modelId_four_name, modelId_four_value;
    private TextView mRoom_tv;
    private Button deleteButton;
    private String floorRoomName;

    private FrequentlyMode frequentlyMode_one;
    private FrequentlyMode frequentlyMode_two;
    private FrequentlyMode frequentlyMode_three;
    private FrequentlyMode frequentlyMode_four;
    private FrequentlyModeDao frequentlyModeDao;

    private Device mDevice;
    private RoomDao mRoomDao;
    private ConfirmAndCancelPopup mRoomNotSetPopup;
    private DeviceSetSelectRoomPopup mSetRoomPopup;

    private final int REQUEST_CODE_SET_DEVICE_NAME = 3;
    public static final int RESULT_DELETE = 0;
    private boolean firstEditDevice = false;
    private ModifyDevice mModifyDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percent_curtain_set_device);
        mDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        mRoomDao = new RoomDao();
        firstEditDevice = getIntent().getBooleanExtra(IntentKey.FIRST_EDIT_DEVICE, false);
        frequentlyModeDao = new FrequentlyModeDao();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFrequentlyMode();
        if (!StringUtil.isEmpty(mDevice.getRoomId())) {
            floorRoomName = mRoomDao.selFloorNameAndRoomName(mDevice.getUid(), mDevice.getRoomId());
        }
        boolean isRoomEmpty = isRoomNotSet();
        if (isRoomEmpty || StringUtil.isEmpty(floorRoomName)) {
            mRoom_tv.setText(R.string.device_set_room_empty);
        } else {
            mRoom_tv.setText(floorRoomName);
        }
        isShowFloorRoomSelect();
        initModifyDevice();
    }

    /**
     * 是否显示楼层房间选择的条目
     */
    public void isShowFloorRoomSelect() {
        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                select_room_ll.setVisibility(View.GONE);
            }
        }
    }

    private void initModifyDevice() {
        mModifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    if (!StringUtil.isEmpty(mDevice.getRoomId())) {
                        floorRoomName = mRoomDao.selFloorNameAndRoomName(mDevice.getUid(), mDevice.getRoomId());
                    }
                    mRoom_tv.setText(floorRoomName);
                } else {
                    Device device = new DeviceDao().selDevice(uid, mDevice.getDeviceId());
                    if (device != null) {
                        mDevice.setRoomId(device.getRoomId());
                    }
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void refreshFrequentlyMode() {
        frequentlyMode_one = frequentlyModeDao.selFrequentlyMode(mDevice.getUid(), mDevice.getDeviceId(), FrequentlyModeId.MODEL_ID_ONE);
        frequentlyMode_two = frequentlyModeDao.selFrequentlyMode(mDevice.getUid(), mDevice.getDeviceId(), FrequentlyModeId.MODEL_ID_TWO);
        frequentlyMode_three = frequentlyModeDao.selFrequentlyMode(mDevice.getUid(), mDevice.getDeviceId(), FrequentlyModeId.MODEL_ID_THREE);
        frequentlyMode_four = frequentlyModeDao.selFrequentlyMode(mDevice.getUid(), mDevice.getDeviceId(), FrequentlyModeId.MODEL_ID_FOUR);

        modelId_one_name.setText(frequentlyMode_one.getName());
        modelId_one_value.setText(valueToPercent(frequentlyMode_one.getValue1()));
        modelId_two_name.setText(frequentlyMode_two.getName());
        modelId_two_value.setText(valueToPercent(frequentlyMode_two.getValue1()));
        modelId_three_name.setText(frequentlyMode_three.getName());
        modelId_three_value.setText(valueToPercent(frequentlyMode_three.getValue1()));
        modelId_four_name.setText(frequentlyMode_four.getName());
        modelId_four_value.setText(valueToPercent(frequentlyMode_four.getValue1()));
    }

    private String valueToPercent(int value) {
        String percent = "";
        if (value == 0) {
            percent = getString(R.string.all_off);
        } else if (value == 100) {
            percent = getString(R.string.all_on);
        } else {
            percent = value + "%";
        }
        return percent;
    }

    private void initView() {
        mRoom_tv = (TextView) findViewById(R.id.room_tv);
        boolean isRoomEmpty = isRoomNotSet();
        if (isRoomEmpty || StringUtil.isEmpty(floorRoomName)) {
            mRoom_tv.setText(R.string.device_set_room_empty);
        } else {
            mRoom_tv.setText(floorRoomName);
        }
        deviceName = findViewById(R.id.deviceName);
        deviceName.setOnClickListener(this);
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        String name = mDevice.getDeviceName();
        if (!StringUtil.isEmpty(name)) {
            name = name.trim();
            deviceNameTextView.setText(name);
        }
        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setOnClickListener(this);

        modelId_one_ll = (LinearLayout) findViewById(R.id.modelId_one_ll);
        modelId_one_ll.setOnClickListener(this);
        modelId_two_ll = (LinearLayout) findViewById(R.id.modelId_two_ll);
        modelId_two_ll.setOnClickListener(this);
        modelId_three_ll = (LinearLayout) findViewById(R.id.modelId_three_ll);
        modelId_three_ll.setOnClickListener(this);
        modelId_four_ll = (LinearLayout) findViewById(R.id.modelId_four_ll);
        modelId_four_ll.setOnClickListener(this);
        select_room_ll = (LinearLayout) findViewById(R.id.select_room_ll);
        select_room_ll.setOnClickListener(this);

        modelId_one_name = (TextView) findViewById(R.id.modelId_one_name);
        modelId_one_value = (TextView) findViewById(R.id.modelId_one_value);
        modelId_two_name = (TextView) findViewById(R.id.modelId_two_name);
        modelId_two_value = (TextView) findViewById(R.id.modelId_two_value);
        modelId_three_name = (TextView) findViewById(R.id.modelId_three_name);
        modelId_three_value = (TextView) findViewById(R.id.modelId_three_value);
        modelId_four_name = (TextView) findViewById(R.id.modelId_four_name);
        modelId_four_value = (TextView) findViewById(R.id.modelId_four_value);

        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        if (firstEditDevice) {
            deviceInfoTextView.setVisibility(View.GONE);
            findViewById(R.id.deviceInfoTextView_line_top).setVisibility(View.GONE);
            findViewById(R.id.deviceInfoTextView_line_bottom).setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            findViewById(R.id.deleteButton_line_top).setVisibility(View.GONE);
            findViewById(R.id.deleteButton_line_bottom).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.deviceName) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceName), null);
            Intent intent = new Intent(this, DeviceNameActivity.class);
            intent.putExtra(Constant.DEVICE, mDevice);
            startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_NAME);
        } else if (vId == R.id.modelId_one_ll) {
            Intent intent = new Intent(mContext, PercentCurtainSetPercentActivity.class);
            intent.putExtra(IntentKey.DEVICE, mDevice);
            intent.putExtra(IntentKey.FREQUENTLY_MODE, frequentlyMode_one);
            startActivity(intent);
        } else if (vId == R.id.modelId_two_ll) {
            Intent intent = new Intent(mContext, PercentCurtainSetPercentActivity.class);
            intent.putExtra(IntentKey.DEVICE, mDevice);
            intent.putExtra(IntentKey.FREQUENTLY_MODE, frequentlyMode_two);
            startActivity(intent);
        } else if (vId == R.id.modelId_three_ll) {
            Intent intent = new Intent(mContext, PercentCurtainSetPercentActivity.class);
            intent.putExtra(IntentKey.DEVICE, mDevice);
            intent.putExtra(IntentKey.FREQUENTLY_MODE, frequentlyMode_three);
            startActivity(intent);
        } else if (vId == R.id.modelId_four_ll) {
            Intent intent = new Intent(mContext, PercentCurtainSetPercentActivity.class);
            intent.putExtra(IntentKey.DEVICE, mDevice);
            intent.putExtra(IntentKey.FREQUENTLY_MODE, frequentlyMode_four);
            startActivity(intent);
        } else if (vId == R.id.deviceInfoTextView) {
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            intent.putExtra(Constant.DEVICE, mDevice);
            startActivity(intent);
        } else if (vId == R.id.deleteButton) {
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.device_set_delete_content));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
            dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        } else if (vId == R.id.select_room_ll) {
            // select room
            InputUtil.closeInput(mContext);

            if (isRoomNotSet()) {
                showRoomNotSetPopup();
            } else {
                showSelectRoomPopup();
            }
        }
    }

    private boolean isRoomNotSet() {
        return mDevice != null && !mRoomDao.hasRoom(mDevice.getUid());
    }

    private void showRoomNotSetPopup() {
        if (mRoomNotSetPopup == null) {
            mRoomNotSetPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    // go to set room
                    startActivity(new Intent(mContext, SetFloorAndRoomActivity.class));
                    dismiss();
                }
            };
        }
        mRoomNotSetPopup.showPopup(mContext, getString(R.string.device_set_setroom_content), getString(R.string.device_set_setroom_btn), getString(R.string.cancel));
    }

    private void showSelectRoomPopup() {
        //选择房间
        if (mSetRoomPopup == null) {
            mSetRoomPopup = new DeviceSetSelectRoomPopup(mContext, mDevice.getUid()) {
                @Override
                public void onSelect(Floor selectedFloor, Room selectedRoom) {
                    if (selectedFloor != null && selectedRoom != null) {
                        //floorRoomName = selectedFloor.getFloorName() + " " + selectedRoom.getRoomName();
                        mDevice.setRoomId(selectedRoom.getRoomId());
                        showDialog();
                        mModifyDevice.modify(mDevice.getUid(), userName, mDevice.getDeviceName(), mDevice.getDeviceType(), mDevice.getRoomId(), mDevice.getIrDeviceId(), mDevice.getDeviceId());
                    }
                }
            };
        }
        mSetRoomPopup.show(mDevice.getRoomId());
    }

    @Override
    public void onLeftButtonClick(View view) {
        super.onLeftButtonClick(view);
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        showDialog();
        mDeleteDevice.deleteZigbeeDevice(mDevice.getUid(), UserCache.getCurrentUserName(this), mDevice.getDeviceId(), mDevice.getExtAddr(), mDevice.getDeviceType());
    }

    @Override
    public void onRightButtonClick(View view) {

    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingCOCO_Back), null);
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DEVICE, mDevice);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    DeleteDevice mDeleteDevice = new DeleteDevice(PercentCurtainSetDeviceActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra(IntentKey.DEVICE, mDevice);
                setResult(RESULT_DELETE, intent);
                finish();
            } else {
                ToastUtil.toastError(result);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEVICE_NAME && resultCode == RESULT_OK) {
            mDevice = (Device) data.getSerializableExtra(Constant.DEVICE);
            String name = mDevice.getDeviceName();
            if (!StringUtil.isEmpty(name)) {
                name = name.trim();
                deviceNameTextView.setText(name);
            }
        }
    }
}
