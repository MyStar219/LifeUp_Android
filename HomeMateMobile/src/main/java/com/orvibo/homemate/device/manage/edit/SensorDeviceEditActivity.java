package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.device.manage.SensorFAQActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.MessagePushUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetSelectRoomPopup;
import com.tencent.stat.StatService;


public class SensorDeviceEditActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener, DialogFragmentOneButton.OnButtonClickListener {

    private NavigationCocoBar navigationBar;
    private TextView deviceNameTextView, positionTextView, messageSettingTimeTextView, messageSettingRepeatTextView, roomTextView, deviceInfoTextView;
    private ImageView deviceInfoImageView;
    private TextView deviceFaqTextView;
    private ImageView faq_line;
    private View deviceName, position, messageSetting, room;
    private Button deleteButton;
    private Device device;
    private RoomDao mRoomDao;
    //true没有创建房间
    private boolean isRoomEmpty = false;
    private ConfirmAndCancelPopup mRoomNotSetPopup;
    private DeviceSetSelectRoomPopup mSetRoomPopup;
    private ModifyDevice modifyDevice;
    private String floorRoomName;
    private boolean firstEditDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_device_edit_activity);
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(Constant.DEVICE);
        firstEditDevice = intent.getBooleanExtra(IntentKey.FIRST_EDIT_DEVICE, false);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        navigationBar.setCenterText(getString(R.string.setting));
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(device.getDeviceName());
        deviceName = findViewById(R.id.deviceName);
        deviceName.setOnClickListener(this);
        position = findViewById(R.id.position);
        position.setOnClickListener(this);
        positionTextView = (TextView) findViewById(R.id.positionTextView);
        messageSetting = findViewById(R.id.messageSetting);
        messageSetting.setVisibility(View.GONE);
        messageSetting.setOnClickListener(this);
        roomTextView = (TextView) findViewById(R.id.roomTextView);
        room = findViewById(R.id.room);
        room.setOnClickListener(this);
        deviceInfoImageView = (ImageView) findViewById(R.id.deviceInfoImageView);
        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        messageSettingTimeTextView = (TextView) findViewById(R.id.messageSettingTimeTextView);
        messageSettingRepeatTextView = (TextView) findViewById(R.id.messageSettingRepeatTextView);

        deviceFaqTextView =(TextView) findViewById(R.id.deviceFaqTextView);
        faq_line          =(ImageView) findViewById(R.id.faq_line);

        if(ProductManage.getInstance().isHeimanSensor(device)){
            deviceFaqTextView.setVisibility(View.VISIBLE);
            faq_line.setVisibility(View.VISIBLE);
            deviceFaqTextView.setOnClickListener(this);
        }else{
            deviceFaqTextView.setVisibility(View.GONE);
            faq_line.setVisibility(View.GONE);
        }

        if (firstEditDevice) {
            deviceInfoTextView.setVisibility(View.GONE);
            deviceInfoImageView.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
        mRoomDao = new RoomDao();
        initModifyDevice();
    }

    private void initModifyDevice() {
        modifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                super.onModifyDeviceResult(uid, serial, result);
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    roomTextView.setText(floorRoomName);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
//        device.setDeviceType(DeviceType.MAGNETIC_DRAWER);
        int deviceType = device.getDeviceType();
        switch (deviceType) {
            case DeviceType.INFRARED_SENSOR:
                position.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_item_height));
                params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.margin_x4), 0, 0);
                messageSetting.setLayoutParams(params);
                break;
            case DeviceType.MAGNETIC:
                positionTextView.setText(R.string.device_position_door);
                break;
            case DeviceType.MAGNETIC_DRAWER:
                positionTextView.setText(R.string.device_position_drawer);
                break;
            case DeviceType.MAGNETIC_WINDOW:
                positionTextView.setText(R.string.device_position_window);
                break;
            case DeviceType.MAGNETIC_OTHER:
                positionTextView.setText(R.string.device_position_other);
                break;
            case DeviceType.FLAMMABLE_GAS:
            case DeviceType.SMOKE_SENSOR:
            case DeviceType.WATER_SENSOR:
            case DeviceType.CO_SENSOR:
            case DeviceType.SOS_SENSOR:
                position.setVisibility(View.GONE);
                messageSetting.setVisibility(View.GONE);
                break;
        }
        setMessageSetting();

        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                room.setVisibility(View.GONE);
            }
        }

        isRoomEmpty = isRoomNotSet();
        String floorRoomName = new RoomDao().selFloorNameAndRoomName(device.getUid(), device.getRoomId());
        if (isRoomEmpty || StringUtil.isEmpty(floorRoomName)) {
            roomTextView.setText(R.string.device_set_room_empty);
        } else {
            roomTextView.setText(floorRoomName);
        }
    }

    private void setMessageSetting() {
        String deviceId = device.getDeviceId();
        MessagePushDao messagePushDao = new MessagePushDao();
        MessagePush messagePush = messagePushDao.selMessagePushByDeviceId(deviceId);
        if (messagePush == null) {
            messagePush = new MessagePush();
            messagePush.setTaskId(deviceId);
            messagePush.setIsPush(MessagePushStatus.ON);
            messagePush.setType(MessagePushType.SINGLE_SENSOR_TYPE);
            messagePush.setStartTime("00:00:00");
            messagePush.setEndTime("00:00:00");
            messagePush.setWeek(255);
        }
        MessagePush allMessagePush = messagePushDao.selAllSetMessagePushByType(UserCache.getCurrentUserId(mAppContext), MessagePushType.ALL_SENSOR_TYPE);
        if (allMessagePush != null && allMessagePush.getIsPush() == MessagePushStatus.OFF) {
            messagePush.setIsPush(MessagePushStatus.OFF);
        }
        if (messagePush.getIsPush() == MessagePushStatus.OFF) {
            messageSettingRepeatTextView.setText(mContext.getString(R.string.device_timing_action_shutdown));
            messageSettingTimeTextView.setVisibility(View.GONE);
        } else if(messagePush.getStartTime().equals(messagePush.getEndTime())){
            messageSettingTimeTextView.setVisibility(View.GONE);
            messageSettingRepeatTextView.setText(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
        } else {
            messageSettingTimeTextView.setVisibility(View.VISIBLE);
            messageSettingTimeTextView.setText(MessagePushUtil.getTimeInterval(mContext, messagePush));
            messageSettingRepeatTextView.setText(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
            messageSettingRepeatTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingCOCO_Back), null);
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DEVICE, device);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.deviceName: {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceName), null);
                Intent intent = new Intent(this, DeviceNameActivity.class);
                intent.putExtra(Constant.DEVICE, device);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.position: {
                Intent intent = new Intent(this, SensorDevicePositionActivity.class);
                intent.putExtra(Constant.DEVICE, device);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.messageSetting: {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_AlertSettings), null);
                Intent intent = new Intent(this, SensorMessageSettingActivity.class);
                intent.putExtra(Constant.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.room: {
                if (isRoomEmpty) {
                    showRoomNotSetPopup();
                } else {
                    showSelectRoomPopup();
                }
                break;
            }
            case R.id.deviceInfoTextView: {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceInfo), null);
                Intent intent = new Intent(this, DeviceInfoActivity.class);
//                if (device != null) {
//                    intent.putExtra(Constant.GATEWAY, new GatewayDao().selGatewayByUid(device.getUid()));
//                }
                intent.putExtra(Constant.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.deleteButton: {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_Delete), null);
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                if (device.getDeviceType() == DeviceType.VICENTER) {
                    dialogFragmentTwoButton.setTitle(getString(R.string.vicenter_delete_title));
                    dialogFragmentTwoButton.setContent(getString(R.string.vicenter_delete_content));
                } else if (device.getDeviceType() == DeviceType.INFRARED_SENSOR
                        ||device.getDeviceType() == DeviceType.MAGNETIC
                        ||device.getDeviceType() == DeviceType.MAGNETIC_DRAWER
                        ||device.getDeviceType() == DeviceType.MAGNETIC_WINDOW
                        ||device.getDeviceType() == DeviceType.MAGNETIC_OTHER) {

                    LinkageConditionDao linkageConditionDao = new LinkageConditionDao();
                    boolean hasLinkageCondition = linkageConditionDao.hasLinkageCondition(device.getDeviceId());
                    if (hasLinkageCondition) {
                        dialogFragmentTwoButton.setTitle(getString(R.string.intelligent_scene_delete_security_device_tips));
                    }else{
                        dialogFragmentTwoButton.setTitle(getString(R.string.device_set_delete_content));
                    }
                } else {
                    dialogFragmentTwoButton.setTitle(getString(R.string.device_set_delete_content));
                }
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
                dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
            }
            case R.id.deviceFaqTextView:
                Intent intent = new Intent(this, SensorFAQActivity.class);
                intent.putExtra("device",device);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            device = (Device) data.getSerializableExtra(Constant.DEVICE);
            deviceNameTextView.setText(device.getDeviceName());
        }
    }

    /**
     * @return true没有房间
     */
    private boolean isRoomNotSet() {
        return device != null && !mRoomDao.hasRoom(device.getUid());
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
            mSetRoomPopup = new DeviceSetSelectRoomPopup(mContext, device.getUid()) {
                @Override
                public void onSelect(Floor selectedFloor, Room selectedRoom) {
                    if (selectedFloor != null && selectedRoom != null) {
                        floorRoomName = selectedFloor.getFloorName() + " " + selectedRoom.getRoomName();
                        device.setRoomId(selectedRoom.getRoomId());
                        showDialog();
                        modifyDevice.modify(device.getUid(), userName, device.getDeviceName(), device.getDeviceType(), selectedRoom.getRoomId(), device.getIrDeviceId(), device.getDeviceId());
                    }
                }
            };
        }
        mSetRoomPopup.show(device.getRoomId());
    }

    DeleteDevice mDeleteDevice = new DeleteDevice(SensorDeviceEditActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                finish();
            } else {
                ToastUtil.toastError(result);
            }
        }
    };

    private void toMainActivity() {
//        EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLeftButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_ConfirmDelete), null);
        // 判断网络是否连接
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        showDialog();
        mDeleteDevice.deleteZigbeeDevice(device.getUid(), UserCache.getCurrentUserName(this), device.getDeviceId(), device.getExtAddr(), device.getDeviceType());
    }

    @Override
    public void onRightButtonClick(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_CancelDelete), null);
    }

    @Override
    public void onButtonClick(View view) {
        toMainActivity();
    }
}
