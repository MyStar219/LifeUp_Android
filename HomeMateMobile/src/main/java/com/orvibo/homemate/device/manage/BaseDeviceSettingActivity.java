package com.orvibo.homemate.device.manage;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.light.RgbwLightSettingFragment;
import com.orvibo.homemate.device.manage.edit.DeviceInfoActivity;
import com.orvibo.homemate.device.manage.edit.DeviceNameActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.InputUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetSelectRoomPopup;

import java.util.List;


/**
 * Created by snown on 2016/7/25.
 *
 * @描述: 设备管理基础类，里面包含5项：设备名称，选择房间，不确定设备在哪，设备信息，删除设备
 */
public class BaseDeviceSettingActivity extends BaseActivity {


    private NavigationGreenBar nbTitle;
    private TextView deviceNameTitle;//设备名称标题
    private TextView deviceNameText;//设备名称内容
    private LinearLayout deviceNameLayout;//设备名称view
    private TextView selectRoom;//设置房间点击
    private LinearLayout selectRoomLayout;//设置房间的view
    private TextView unknownDevice;//不知道设备在哪
    private FrameLayout fragmentContent;//设置处不同设备内容填充
    private TextView deviceInfo;//设备信息
    private Button deleteBtn;//删除设备

    private final int REQUEST_CODE_EDIT_REMOTE = 2;
    private final int REQUEST_CODE_SET_DEVICE_NAME = 3;

    public static final int RESULT_DELETE = 0;

    private Device mDevice;

    private RoomDao mRoomDao;

    private ModifyDevice mModifyDevice;
    private DeleteDevice mDeleteDevice;

    private ConfirmAndCancelPopup mRoomNotSetPopup;
    private DeviceSetSelectRoomPopup mSetRoomPopup;
    private ConfirmAndCancelPopup mFindPopup;

    private ControlDevice mControlDevice;

    private String floorRoomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_device_set);
        mDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        mRoomDao = new RoomDao();
        if (mDevice == null && savedInstanceState.getSerializable(IntentKey.DEVICE) != null) {
            mDevice = (Device) savedInstanceState.getSerializable(IntentKey.DEVICE);
        }

        initView();
        setDeviceFragment();
    }

    private void initView() {
        this.deleteBtn = (Button) findViewById(R.id.deleteBtn);
        this.deviceInfo = (TextView) findViewById(R.id.deviceInfo);
        this.fragmentContent = (FrameLayout) findViewById(R.id.fragmentContent);
        this.unknownDevice = (TextView) findViewById(R.id.unknownDevice);
        this.selectRoomLayout = (LinearLayout) findViewById(R.id.selectRoomLayout);
        this.selectRoom = (TextView) findViewById(R.id.selectRoom);
        this.deviceNameLayout = (LinearLayout) findViewById(R.id.deviceNameLayout);
        this.deviceNameText = (TextView) findViewById(R.id.deviceName);
        this.deviceNameTitle = (TextView) findViewById(R.id.deviceNameTitle);
        this.nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        deviceNameLayout.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        selectRoomLayout.setOnClickListener(this);
        deviceInfo.setOnClickListener(this);
        unknownDevice.setOnClickListener(this);
    }


    private void initData() {
        nbTitle.setText(getResources().getString(R.string.device_set_title));
        deviceNameText.setText(mDevice.getDeviceName());
        selectRoomLayout.setVisibility(isShowRoom() ? View.VISIBLE : View.GONE);
        unknownDevice.setVisibility(isShowFindDevice() ? View.VISIBLE : View.GONE);
        if (!StringUtil.isEmpty(mDevice.getRoomId())) {
            floorRoomName = mRoomDao.selFloorNameAndRoomName(mDevice.getUid(), mDevice.getRoomId());
        }
        if (StringUtil.isEmpty(floorRoomName)) {
            selectRoom.setText(R.string.device_set_room_empty);
        } else {
            selectRoom.setText(floorRoomName);
        }
    }

    /**
     * 区分不同设备的设置项
     */
    private void setDeviceFragment() {
        Fragment fragment = null;

        switch (mDevice.getDeviceType()) {

        }
        //创维rgbw和单路灯都要有缓亮缓灭功能
        if (ProductManage.isSkyRGBW(mDevice) || ProductManage.isSkySingleLight(mDevice))
            fragment = new RgbwLightSettingFragment();
        if (fragment != null) {
            fragmentContent.setVisibility(View.VISIBLE);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKey.DEVICE, mDevice);
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().add(R.id.fragmentContent, fragment).commitAllowingStateLoss();
        } else {
            fragmentContent.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDevice == null) {
            finish();
        } else {
            initData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mDevice != null) {
            outState.putSerializable(IntentKey.DEVICE, mDevice);
        }
        super.onSaveInstanceState(outState);
    }


    /**
     * 设置房间是否显示
     *
     * @return
     */
    private boolean isShowRoom() {
        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (ProductManage.getInstance().isWifiDevice(mDevice))
            return false;
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceNameLayout: {
                Intent intent = new Intent(this, DeviceNameActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_NAME);
            }
            break;
            case R.id.deleteBtn:
                showDeleteDevicePopup();
                break;
            case R.id.selectRoomLayout:
                // select room
                InputUtil.closeInput(mContext);
                if (isRoomNotSet()) {
                    showRoomNotSetPopup();
                } else {
                    showSelectRoomPopup();
                }
                break;
            case R.id.deviceInfo: {
                Intent intent = new Intent(this, DeviceInfoActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivity(intent);
            }
            break;
            case R.id.unknownDevice:
                showFindDevicePopup();
                break;
        }
    }

    /**
     * @return true没有房间
     */
    private boolean isRoomNotSet() {
        return mDevice != null && !mRoomDao.hasRoom(mDevice.getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEVICE_NAME && resultCode == RESULT_OK) {
            mDevice = (Device) data.getSerializableExtra(Constant.DEVICE);
            if (mDevice != null) {
                String name = mDevice.getDeviceName();
                if (!StringUtil.isEmpty(name)) {
                    name = name.trim();
                    deviceNameText.setText(name);
                }
            }
        }
    }


    private void deleteDevice() {
        mDeleteDevice = new DeleteDevice(mAppContext) {
            @Override
            public void onDeleteDeviceResult(String uid, int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
//                    mDeletePopup.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(IntentKey.DEVICE, mDevice);
                    setResult(RESULT_DELETE, intent);
                    finish();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
        if (mDevice.getDeviceType() == DeviceType.IR_REPEATER || mDevice.getAppDeviceId() == AppDeviceId.IR_REPEATER) {
            mDeleteDevice.deleteZigbeeDevice(mDevice.getUid(), mDevice.getUserName(), mDevice.getDeviceId(), mDevice.getExtAddr(), mDevice.getDeviceType());
        } else {
            mDeleteDevice.deleteZigbeeDevice(mDevice.getUid(), mDevice.getUserName(), mDevice.getDeviceId(), mDevice.getExtAddr(), mDevice.getDeviceType());
        }
    }

    private void showDeleteDevicePopup() {
        String content = getString(R.string.device_set_delete_content);
        final int deviceType = mDevice.getDeviceType();
        if (!DeviceUtil.isIrDevice(mDevice)) {
            //非虚拟红外设备
            DeviceDao deviceDao = new DeviceDao();
            List<Device> devices = deviceDao.selIRDeviceByExtAddr(mDevice.getUid(), mDevice.getExtAddr());
            if (devices != null && devices.size() > 1) {
                String sourceContent = getString(R.string.device_set_delete_mul_device_content);
                content = String.format(sourceContent, devices.size(), getDeleteOtherDeviceNames(devices));
                if (deviceType == DeviceType.IR_REPEATER) {
                    //红外转发器
                    content = getString(R.string.device_set_delete_mul_ir_device_content);
                }
            }
            if (ProductManage.getInstance().isAlloneSunDevice(mDevice)) {
                content = getString(R.string.device_set_delete_content);
            } else if (deviceType == DeviceType.ALLONE) {
                content = getString(R.string.device_set_delete_allone_content);
            }
            if (deviceType == DeviceType.LOCK
                    || deviceType == DeviceType.MAGNETIC
                    || deviceType == DeviceType.MAGNETIC_WINDOW
                    || deviceType == DeviceType.MAGNETIC_DRAWER
                    || deviceType == DeviceType.MAGNETIC_OTHER
                    || deviceType == DeviceType.INFRARED_SENSOR) {
                LinkageConditionDao linkageConditionDao = new LinkageConditionDao();
                boolean hasLinkageCondition = linkageConditionDao.hasLinkageCondition(mDevice.getDeviceId());
                if (hasLinkageCondition) {
                    content = getString(R.string.intelligent_scene_delete_security_device_tips);
                }
            }
        }
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
        dialogFragmentTwoButton.setContent(content);
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    private String getDeleteOtherDeviceNames(List<Device> devices) {
        StringBuffer otherDeviceNames = new StringBuffer();
        final int count = devices.size();
        final String curDeviceId = mDevice.getDeviceId();
        for (int i = 0; i < count; i++) {
            Device device = devices.get(i);
            if (device.getDeviceId().equals(curDeviceId)) {
                continue;
            }
            if (otherDeviceNames.length() > 0) {
                otherDeviceNames.append(",");
            }
            otherDeviceNames.append("\"" + device.getDeviceName() + "\"");
        }
        return otherDeviceNames.toString();
    }

    @Override
    public void onLeftButtonClick(View view) {
        showDialog();
        deleteDevice();
    }

    @Override
    public void onRightButtonClick(View view) {

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
        initModifyDevice();
        //选择房间
        if (mSetRoomPopup == null) {
            mSetRoomPopup = new DeviceSetSelectRoomPopup(mContext, mDevice.getUid()) {
                @Override
                public void onSelect(Floor selectedFloor, Room selectedRoom) {
                    if (selectedFloor != null && selectedRoom != null) {
                        mDevice.setRoomId(selectedRoom.getRoomId());
                        showDialog();
                        mModifyDevice.modify(mDevice.getUid(), userName, mDevice.getDeviceName(),
                                mDevice.getDeviceType(), mDevice.getRoomId(), mDevice.getIrDeviceId(), mDevice.getDeviceId());

                    }
                }
            };
        }
        mSetRoomPopup.show(mDevice.getRoomId());
    }

    private void showFindDevicePopup() {
        //不确定设备在哪接口
        showDialog();
        if (mControlDevice == null) {
            mControlDevice = new ControlDevice(mAppContext) {
                @Override
                public void onControlDeviceResult(String uid, String deviceId, int result) {
                    if (isFinishingOrDestroyed()) {
                        return;
                    }
                    dismissDialog();
                    if (result == ErrorCode.SUCCESS) {
                        if (mFindPopup == null) {
                            mFindPopup = new ConfirmAndCancelPopup() {
                                @Override
                                public void confirm() {
                                    dismiss();
                                }
                            };
                        }
                        mFindPopup.showPopup(BaseDeviceSettingActivity.this, getString(R.string.device_set_find_content), getString(R.string.device_set_find_btn), null);
                    } else {
                        ToastUtil.toastError(result);
                    }
                }
            };
        }
        mControlDevice.identify(mDevice.getUid(), mDevice.getDeviceId());
    }

    /**
     * 是否显示查找设备
     *
     * @return
     */
    private boolean isShowFindDevice() {
        boolean findDevice = true;
        //匹配如下设备类型，如果匹配则不显示
        switch (mDevice.getDeviceType()) {
            case DeviceType.LOCK:
                findDevice = false;
                break;
        }
        return findDevice;
    }

    private void initModifyDevice() {
        mModifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    int deviceType = mDevice.getDeviceType();
                    if (deviceType == DeviceType.TV || (deviceType == DeviceType.AC && mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF)
                            || deviceType == DeviceType.STB || deviceType == DeviceType.SELF_DEFINE_IR) {
                        dismissDialog();
                        ActivityTool.toLearnIrActivity(BaseDeviceSettingActivity.this, REQUEST_CODE_EDIT_REMOTE, mDevice);
                    }
                    if (!StringUtil.isEmpty(mDevice.getRoomId())) {
                        floorRoomName = mRoomDao.selFloorNameAndRoomName(mDevice.getUid(), mDevice.getRoomId());
                    }
                    selectRoom.setText(floorRoomName);
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
}
