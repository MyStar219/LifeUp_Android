package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.DeviceSetFiveKeySceneActivity;
import com.orvibo.homemate.device.manage.DeviceSetSevenKeySceneActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.manager.DeviceSetSceneButtonActivity;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetSelectRoomPopup;
import com.tencent.stat.StatService;


/**
 * 情景面板编辑类
 *
 * @author huangqiyao
 * @date 2016/1/4 modify
 */
public class SceneDeviceEditActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener, DialogFragmentOneButton.OnButtonClickListener {
    private NavigationCocoBar navigationBar;
    private TextView deviceNameTextView, buttonSettingTextView, roomTextView, deviceInfoTextView, unknown_device_tv;
    private ImageView deviceInfoImageView;
    private View deviceName, room;
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
    private ControlDevice mControlDevice;
    private ConfirmAndCancelPopup mFindPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scene_device_edit_activity);
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        firstEditDevice = intent.getBooleanExtra(IntentKey.FIRST_EDIT_DEVICE, false);
        init();
       // initSystemBar(SceneDeviceEditActivity.this);
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        navigationBar.setCenterText(getString(R.string.setting));
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(device.getDeviceName() + "");
        deviceName = findViewById(R.id.deviceName);
        deviceName.setOnClickListener(this);
        buttonSettingTextView = (TextView) findViewById(R.id.buttonSettingTextView);
        buttonSettingTextView.setOnClickListener(this);
        roomTextView = (TextView) findViewById(R.id.roomTextView);
        room = findViewById(R.id.room);
        room.setOnClickListener(this);
        deviceInfoImageView = (ImageView) findViewById(R.id.deviceInfoImageView);
        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setOnClickListener(this);
        unknown_device_tv = (TextView) findViewById(R.id.unknown_device_tv);
        unknown_device_tv.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        if (firstEditDevice) {
            deviceInfoTextView.setVisibility(View.GONE);
            deviceInfoImageView.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
        mRoomDao = new RoomDao();

        initRoomShow();
        initModifyDevice();
    }

    /**
     * 查询主机中设备个数，如果小于10个设备就不显示楼层房间
     */
    private void initRoomShow(){
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                room.setVisibility(View.GONE);
            }
        }
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
        isRoomEmpty = isRoomNotSet();
        String floorRoomName = new RoomDao().selFloorNameAndRoomName(device.getUid(), device.getRoomId());
        if (isRoomEmpty || StringUtil.isEmpty(floorRoomName)) {
            roomTextView.setText(R.string.device_set_room_empty);
        } else {
            roomTextView.setText(floorRoomName);
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
            case R.id.buttonSettingTextView: {
                Intent intent;
                if (device.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
                    intent = new Intent(this, DeviceSetFiveKeySceneActivity.class);
                } else if (device.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
                    intent = new Intent(this, DeviceSetSevenKeySceneActivity.class);
                } else {
                    intent = new Intent(this, DeviceSetSceneButtonActivity.class);
                }
                intent.putExtra(IntentKey.DEVICE, device);
                startActivityForResult(intent, 0);
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
            case R.id.unknown_device_tv: {
                // find device
                showFindDevicePopup();
                break;
            }
            case R.id.deviceInfoTextView: {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceInfo), null);
                Intent intent = new Intent(this, DeviceInfoActivity.class);
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

    private void showFindDevicePopup() {
        //不确定设备在哪接口
        if (mControlDevice == null) {
            mControlDevice = new ControlDevice(mAppContext) {
                @Override
                public void onControlDeviceResult(String uid, String deviceId, int result) {

                }
            };
        }
        mControlDevice.identify(device.getUid(), device.getDeviceId());
        if (mFindPopup == null) {
            mFindPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                }
            };
        }
        mFindPopup.showPopup(mContext, getString(R.string.device_set_find_content), getString(R.string.device_set_find_btn), null);
    }

    DeleteDevice mDeleteDevice = new DeleteDevice(SceneDeviceEditActivity.this) {
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
        // EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
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

    @Override
    protected void onDestroy() {
        if (mControlDevice != null) {
            mControlDevice.stopControl();
            mControlDevice = null;
        }
        super.onDestroy();
    }
}
