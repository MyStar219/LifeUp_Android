package com.orvibo.homemate.device.manage;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.RemoteList;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.IrApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.bo.RemoteCount;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.data.NetType;
import com.orvibo.homemate.device.allone2.RemoteControlActivity;
import com.orvibo.homemate.device.allone2.irlearn.RemoteLearnActivity;
import com.orvibo.homemate.device.bind.SelectDeviceTypeActivity;
import com.orvibo.homemate.device.control.TemperatureUnitSetActivity;
import com.orvibo.homemate.device.manage.edit.DeviceInfoActivity;
import com.orvibo.homemate.device.manage.edit.DeviceNameActivity;
import com.orvibo.homemate.device.smartlock.LockMemberListActivity;
import com.orvibo.homemate.device.smartlock.LockTimingActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.LevelDelayTime;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.model.OffDelayTime;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.model.TimerPush;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.smartscene.manager.DeviceSetSceneButtonActivity;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.InputUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
//import com.orvibo.homemate.view.custom.DialogFragmentThreeButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetSelectRoomPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * 设置设备名称、房间。
 */
public class SetDeviceActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener
//        DialogFragmentThreeButton.OnThreeButtonClickListener
{
    private static final String TAG = SetDeviceActivity.class.getSimpleName();
    private Device mDevice;

    /**
     * 只保存name,roomId,deviceType
     */
    private Device mOldDevice;
    private RoomDao mRoomDao;

    private ModifyDevice mModifyDevice;
    private DeleteDevice mDeleteDevice;
    private RemoteCount mRemoteCount;
    private RemoteCount mOldRemoteCount;
    private ControlDevice mControlDevice;
    private LevelDelayTime mLevelDelayTime;
    private OffDelayTime mOffDelayTime;

    //view
    private View deviceName;
    private TextView deviceNameTitleTextView, tip4_tv;
    private TextView deviceNameTextView;
    private LinearLayout add_remote_ll;
    private LinearLayout select_deviceType_ll;
    private LinearLayout selectLevelDalayTime;
    private LinearLayout selectOffDalayTime;
    private ImageView arrow_type_iv;
    private LinearLayout select_room_ll;
    private TextView mDeviceType_tv;
    private TextView mRoom_tv;
    private TextView mAddRemote_tv;
    private TextView learn_ir_tv;
    private TextView deviceInfoTextView;
    private NavigationGreenBar nb_title;
    private TextView unknown_device_tv;
    private Button deleteButton;
    private TextView type_tips_tv;
    private TextView levelDalayTimeTextView;
    private TextView offDalayTimeTextView;

    private ConfirmAndCancelPopup mFindPopup;
    private ConfirmAndCancelPopup mRoomNotSetPopup;
    private ConfirmAndCancelPopup mConfirmSavePopup;
    private ConfirmAndCancelPopup mModifyIrDeviceRoomPopup;
    private DeviceSetSelectRoomPopup mSetRoomPopup;

    private String floorRoomName;
    private final int REQUEST_CODE_SET_DEVICE_TYPE = 0;
    private final int REQUEST_CODE_ADD_REMOTE = 1;
    private final int REQUEST_CODE_EDIT_REMOTE = 2;
    private final int REQUEST_CODE_SET_DEVICE_NAME = 3;
    public static final int RESULT_DELETE = 0;
    public static final int RESULT_MODIFY = 1;
    private boolean firstEditDevice = false;
    private MessagePush messagePush;

    /**
     * 对于多路设备，之前的逻辑是删除一路，其他基路一起删掉，同时主机给设备发送离网指令，设备收到指令后就处于清网状态
     * 现在逻辑是，可以删除其中的一路，其他两路不删除。
     * true:删除所有设备，false：删除该设备
     */
    private boolean isDeleteAllDevice = true;

    /**
     * 如果这个设备是多路设备，是否可以删除其中的一路或者几路
     * 以下特殊情况需要考虑：
     * 对于删除红外转发器，其创建的子设备需要一同删除
     * 删除小方也要删除所有的子设备
     * 创维RGBW灯是不允许删除其中的一路
     */
    private boolean canDeleteSingleDevice = false;

    private int levelDalayTime;
    private int offDalayTime;

    private TextView deviceFaqTextView;
    private ImageView faq_line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set);
        nb_title = (NavigationGreenBar) findViewById(R.id.nbTitle);
        nb_title.setText(getResources().getString(R.string.device_set_title));

        mDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        firstEditDevice = getIntent().getBooleanExtra(IntentKey.FIRST_EDIT_DEVICE, false);
//        floorRoomName = getIntent().getStringExtra("floorRoomName");
        LogUtil.d(TAG, "onCreate()-device:" + mDevice + ",floorRoomName:" + floorRoomName);
        mRoomDao = new RoomDao();

        if (mDevice == null && savedInstanceState.getSerializable(IntentKey.DEVICE) != null) {
            mDevice = (Device) savedInstanceState.getSerializable(IntentKey.DEVICE);
        }
        //  initSystemBar(SetDeviceActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDevice == null) {
            finish();
        } else {
            //修复了不显示楼层房间问题
            if (!StringUtil.isEmpty(mDevice.getRoomId())) {
                floorRoomName = mRoomDao.selFloorNameAndRoomName(mDevice.getUid(), mDevice.getRoomId());
            }
            findView();
            initView();
        }
        if (mDevice.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
            mDeviceType_tv.setText(CommonCache.getTemperatureUnit());
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mDevice != null) {
            outState.putSerializable(IntentKey.DEVICE, mDevice);
        }
        super.onSaveInstanceState(outState);
    }

    private void findView() {
        deviceName = findViewById(R.id.deviceName);
        deviceNameTitleTextView = (TextView) findViewById(R.id.deviceNameTitleTextView);
        tip4_tv = (TextView) findViewById(R.id.tip4_tv);
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        mDeviceType_tv = (TextView) findViewById(R.id.type_tv);
        mRoom_tv = (TextView) findViewById(R.id.room_tv);
        mAddRemote_tv = (TextView) findViewById(R.id.add_remote_tv);
        learn_ir_tv = (TextView) findViewById(R.id.learn_ir_tv);
        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        unknown_device_tv = (TextView) findViewById(R.id.unknown_device_tv);
        if (mDevice.getDeviceType() == DeviceType.REMOTE || mDevice.getDeviceType() == DeviceType.LOCK || ProductManage.getInstance().isWifiDevice(mDevice)) {
            unknown_device_tv.setVisibility(View.GONE);
        }
        add_remote_ll = (LinearLayout) findViewById(R.id.add_remote_ll);
        select_deviceType_ll = (LinearLayout) findViewById(R.id.select_deviceType_ll);
        selectLevelDalayTime = (LinearLayout) findViewById(R.id.selectLevelDalayTime);
        selectOffDalayTime = (LinearLayout) findViewById(R.id.selectOffDalayTime);
        arrow_type_iv = (ImageView) findViewById(R.id.arrow_type_iv);
        select_room_ll = (LinearLayout) findViewById(R.id.select_room_ll);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        type_tips_tv = (TextView) findViewById(R.id.type_tips_tv);
        levelDalayTimeTextView = (TextView) findViewById(R.id.levelDalayTimeTextView);
        offDalayTimeTextView = (TextView) findViewById(R.id.offDalayTimeTextView);

        selectLevelDalayTime.setOnClickListener(this);
        selectOffDalayTime.setOnClickListener(this);
        select_room_ll.setOnClickListener(this);
        mAddRemote_tv.setOnClickListener(this);
        learn_ir_tv.setOnClickListener(this);
        deviceInfoTextView.setOnClickListener(this);
        unknown_device_tv.setOnClickListener(this);
        boolean isRoomEmpty = isRoomNotSet();
        if (isRoomEmpty || StringUtil.isEmpty(floorRoomName)) {
            mRoom_tv.setText(R.string.device_set_room_empty);
        } else {
            mRoom_tv.setText(floorRoomName);
        }

        deviceFaqTextView = (TextView) findViewById(R.id.deviceFaqTextView);
        faq_line = (ImageView) findViewById(R.id.faq_line);

        if (ProductManage.isHeimanSensor(mDevice)) {
            deviceFaqTextView.setVisibility(View.VISIBLE);
            faq_line.setVisibility(View.VISIBLE);
            deviceFaqTextView.setOnClickListener(this);
        } else {
            deviceFaqTextView.setVisibility(View.GONE);
            faq_line.setVisibility(View.GONE);
        }

        if (firstEditDevice) {
            deviceInfoTextView.setVisibility(View.GONE);
            findViewById(R.id.deviceInfoImageView).setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    private void initView() {
        deviceName.setOnClickListener(this);
        String name = mDevice.getDeviceName();
        if (!StringUtil.isEmpty(name)) {
            name = name.trim();
            deviceNameTextView.setText(name);
        }
        copyDevice();
        setDeviceType();
        initModifyDevice();
    }

    /**
     * 设置设备类型
     * <p/>
     * 注意：如果设备类型设置为34、35的话，用户不能再自己去修改设备的图标。
     * 如果设备类型是8的话可以在设备类型为3幕布、8对开窗帘、39卷闸门、40花洒、41推窗器、42卷帘（无百分比）之间互相切换。
     */
    private void setDeviceType() {
        final int deviceType = mDevice.getDeviceType();
//        Drawable icon = mContext.getResources().getDrawable(DeviceTool.getDeviceIconResId(mDevice,true));
        //窗帘控制器才有选中更详细的设备类型
        Drawable arrow = null;
        final int appDeviceId = mDevice.getAppDeviceId();
        if (appDeviceId == AppDeviceId.CURTAIN_CONTROL_BOX
                || appDeviceId == AppDeviceId.CURTAIN_CONTROLLER || deviceType == DeviceType.REMOTE || appDeviceId == AppDeviceId.REMOTE
                || deviceType == DeviceType.SCENE_KEYPAD || appDeviceId == AppDeviceId.SCENE_KEYPAD) {
            if (!(deviceType == DeviceType.CURTAIN_PERCENT
                    || deviceType == DeviceType.ROLLER_SHADES_PERCENT
                    || deviceType == DeviceType.WINDOW_SHADES
//                    || deviceType == DeviceType.WINDOW_SHADES_PERCENT
            )) {
                select_deviceType_ll.setOnClickListener(this);
            } else {
                select_deviceType_ll.setVisibility(View.GONE);
            }
            arrow = mContext.getResources().getDrawable(R.drawable.bg_right_arrow);
        } else {
            select_deviceType_ll.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_item_height));
            params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.margin_x4), 0, 0);
            select_room_ll.setLayoutParams(params);
        }
        //按原比例显示
//        mDeviceType_tv.setCompoundDrawablesWithIntrinsicBounds(icon, null, arrow, null);
        //http://blog.csdn.net/wulianghuan/article/details/24421179需要设置rect
//        mDeviceType_tv.setCompoundDrawables(icon, null, arrow, null);
        //设备类型名
        mDeviceType_tv.setText(DeviceTool.getDeviceTypeNameResId(deviceType));
        if (deviceType == DeviceType.IR_REPEATER || appDeviceId == AppDeviceId.IR_REPEATER) {
            add_remote_ll.setVisibility(View.VISIBLE);
            setIrRepeater();
        } else if (deviceType == DeviceType.REMOTE || appDeviceId == AppDeviceId.REMOTE
                || deviceType == DeviceType.SCENE_KEYPAD || appDeviceId == AppDeviceId.SCENE_KEYPAD) {
            //遥控器、情景面板
            add_remote_ll.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
            select_deviceType_ll.setVisibility(View.VISIBLE);
            setRemoteView();
        } else if (deviceType == DeviceType.CAMERA) {
            unknown_device_tv.setVisibility(View.GONE);
            add_remote_ll.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
        } else {
            add_remote_ll.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
        }

        if (DeviceUtil.isIrDevice(mDevice)
                && !DeviceTool.isIrDevicelearned(mDevice) && mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF) {
            learn_ir_tv.setVisibility(View.VISIBLE);
            findViewById(R.id.learn_ir_iv).setVisibility(View.VISIBLE);
            select_deviceType_ll.setVisibility(View.GONE);
            select_room_ll.setVisibility(View.GONE);
            findViewById(R.id.device_room_line).setVisibility(View.GONE);
            unknown_device_tv.setVisibility(View.GONE);
            deviceInfoTextView.setVisibility(View.GONE);
            findViewById(R.id.deviceInfoImageView).setVisibility(View.GONE);
            nb_title.setText(getResources().getString(R.string.learn_ir));
            deviceNameTitleTextView.setText(getResources().getString(R.string.device_set_ir_remote_name_text));
            //deviceNameTextView.setHint(getResources().getString(R.string.device_set_set_remote_name_tips));
        } else if (DeviceUtil.isIrDevice(mDevice)
                && DeviceTool.isIrDevicelearned(mDevice) && mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF) {
            learn_ir_tv.setVisibility(View.VISIBLE);
            findViewById(R.id.learn_ir_iv).setVisibility(View.VISIBLE);
            select_deviceType_ll.setVisibility(View.GONE);
            select_room_ll.setVisibility(View.GONE);
            findViewById(R.id.device_room_line).setVisibility(View.GONE);
            unknown_device_tv.setVisibility(View.GONE);
           /* if (mDevice.getDeviceType() == DeviceType.AC_PANEL) {
                deviceInfoTextView.setVisibility(View.VISIBLE);
                learn_ir_tv.setVisibility(View.GONE);
                select_room_ll.setVisibility(View.VISIBLE);
            } else {
                select_room_ll.setVisibility(View.GONE);
                deviceInfoTextView.setVisibility(View.GONE);
                learn_ir_tv.setVisibility(View.VISIBLE);
              //  findViewById(R.id.learn_ir_iv).setVisibility(View.VISIBLE);
            }*/
            deviceInfoTextView.setVisibility(View.GONE);
            findViewById(R.id.deviceInfoImageView).setVisibility(View.GONE);
            deviceNameTitleTextView.setText(getResources().getString(R.string.device_set_ir_remote_name_text));
            deviceNameTextView.setHint(getResources().getString(R.string.device_set_set_remote_name_tips));
        } else if (deviceType == DeviceType.LOCK && ProductManage.isSmartLock(mDevice)) {//门锁设置
            unknown_device_tv.setVisibility(View.GONE);
            add_remote_ll.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
            select_deviceType_ll.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_item_height));
            params.setMargins(0, 0, 0, 0);
            select_room_ll.setLayoutParams(params);
            type_tips_tv.setText(getString(R.string.smart_lock_member_set));
            List<DoorUserData> doorUserDatas = DoorUserDao.getInstance().getDoorUserList(mDevice.getDeviceId());
            mDeviceType_tv.setText(doorUserDatas.size() + getString(R.string.common_unit));
            select_deviceType_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    List<DoorUserData> doorUserDatas = DoorUserDao.getInstance().getDoorUserList(mDevice.getDeviceId());
                    intent.putExtra(IntentKey.DOOR_USER_DATA, (Serializable) doorUserDatas);
                    intent.putExtra(IntentKey.DEVICE, mDevice);
                    ActivityJumpUtil.jumpAct(SetDeviceActivity.this, LockMemberListActivity.class, intent);
                }
            });
            if (ProductManage.isBLLock(mDevice)) {
                final MessagePushDao messagePushDao = new MessagePushDao();
                messagePush = messagePushDao.getMessagePushByType(mDevice.getDeviceId(), MessagePushType.DEVICE_LOCK_OPEN_REMIND);
                View itemArrow = findViewById(R.id.itemArrow);
                View itemCheck = findViewById(R.id.itemCheck);
                final ImageView imageCheck = (ImageView) findViewById(R.id.imageCheck);
                itemArrow.setVisibility(View.VISIBLE);
                itemCheck.setVisibility(View.VISIBLE);
                if (messagePush == null) {
                    messagePush = new MessagePush();
                    messagePush.setIsPush(MessagePushStatus.ON);
                }
                if (messagePush.getIsPush() == MessagePushStatus.ON) {
                    imageCheck.setImageLevel(1);
                } else {
                    imageCheck.setImageLevel(0);
                }
                itemArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SetDeviceActivity.this, LockTimingActivity.class);
                        intent.putExtra(IntentKey.DEVICE, mDevice);
                        startActivity(intent);
                    }
                });
                imageCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SensorTimerPush sensorTimerPush = new SensorTimerPush(mContext) {
                            @Override
                            public void onSensorTimerPushResult(int result, int type) {
                                if (result != ErrorCode.SUCCESS) {
                                    ToastUtil.toastError(result);
                                } else {
                                    messagePush = messagePushDao.getMessagePushByType(mDevice.getDeviceId(), MessagePushType.DEVICE_LOCK_OPEN_REMIND);
                                    if (messagePush == null) {
                                        messagePush = new MessagePush();
                                        messagePush.setIsPush(MessagePushStatus.ON);
                                    }
                                    if (messagePush.getIsPush() == MessagePushStatus.ON) {
                                        imageCheck.setImageLevel(1);
                                    } else {
                                        imageCheck.setImageLevel(0);
                                    }
                                }
                            }

                            @Override
                            public void onAllSensorSetTimerPushResult(int result) {

                            }
                        };
                        if (messagePush.getIsPush() == MessagePushStatus.ON)
                            sensorTimerPush.startSetDeviceTimerPush(mDevice.getDeviceId(), MessagePushStatus.OFF, null, MessagePushType.DEVICE_LOCK_OPEN_REMIND);
                        else
                            sensorTimerPush.startSetDeviceTimerPush(mDevice.getDeviceId(), MessagePushStatus.ON, null, MessagePushType.DEVICE_LOCK_OPEN_REMIND);
                    }
                });
            }
        } else if (deviceType == DeviceType.TEMPERATURE_SENSOR || deviceType == DeviceType.HUMIDITY_SENSOR) {
            unknown_device_tv.setVisibility(View.GONE);
            add_remote_ll.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
            if (deviceType == DeviceType.HUMIDITY_SENSOR)
                select_deviceType_ll.setVisibility(View.GONE);
            else
                select_deviceType_ll.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_item_height));
            params.setMargins(0, 0, 0, 0);
            select_room_ll.setLayoutParams(params);
            type_tips_tv.setText(getString(R.string.temperature_unit));
            mDeviceType_tv.setText(CommonCache.getTemperatureUnit());
            select_deviceType_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityJumpUtil.jumpAct(SetDeviceActivity.this, TemperatureUnitSetActivity.class);
                }
            });
        } else if (deviceType == DeviceType.AC_PANEL || (deviceType == DeviceType.AC && mDevice.getAppDeviceId() == AppDeviceId.AC_WIIF)) {
            learn_ir_tv.setVisibility(View.GONE);
            learn_ir_tv.setClickable(false);
            select_deviceType_ll.setVisibility(View.GONE);
            select_room_ll.setVisibility(View.VISIBLE);
            findViewById(R.id.device_room_line).setVisibility(View.VISIBLE);
            unknown_device_tv.setVisibility(View.GONE);
            deviceInfoTextView.setVisibility(View.VISIBLE);
            nb_title.setText(getResources().getString(R.string.setting));
//            deviceNameTitleTextView.setText(getResources().getString(R.string.device_set_ir_remote_name_text));
        } else if (DeviceUtil.isNewFiveSensorDevice(deviceType)) {
            unknown_device_tv.setVisibility(View.GONE);
        } else if (deviceType == DeviceType.ALLONE) {
            select_room_ll.setVisibility(View.GONE);
        }
        if (ProductManage.isAlloneSunDevice(mDevice)) {
            //有定时显示定时执行提醒
            if (AlloneUtil.hasTiming(deviceType) || (deviceType == DeviceType.STB && !ProductManage.isAlloneLearnDevice(mDevice))) {
                findViewById(R.id.messageSettingView).setVisibility(View.VISIBLE);
                if (deviceType == DeviceType.STB) {  //机顶盒有节目预约提醒
                    TextView remaindText = (TextView) findViewById(R.id.remaindText);
                    remaindText.setText(getString(R.string.porgram_reserve_remaid));
                }
                final ImageView infoPushSwitchImageView = (ImageView) findViewById(R.id.infoPushSwitchImageView);
                messagePush = new MessagePushDao().selMessagePushByDeviceId(mDevice.getDeviceId());
                if (messagePush == null) {
                    messagePush = new MessagePush();
                    messagePush.setTaskId(mDevice.getDeviceId());
                    messagePush.setIsPush(MessagePushStatus.ON);
                    messagePush.setStartTime("00:00:00");
                    messagePush.setEndTime("00:00:00");
                }
                if (deviceType == DeviceType.STB)
                    messagePush.setType(MessagePushType.DEVICE_ALLONE_STB_REMIND);
                else
                    messagePush.setType(MessagePushType.SINGLE_TIMER_TYPE);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    infoPushSwitchImageView.setImageLevel(0);
                } else {
                    infoPushSwitchImageView.setImageLevel(1);
                }
                infoPushSwitchImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                        TimerPush timerPush = new TimerPush(mContext) {
                            @Override
                            public void onTimerPushResult(int result) {
                                dismissDialog();
                                if (result == 0) {
                                    messagePush = new MessagePushDao().selMessagePushByDeviceId(mDevice.getDeviceId());
                                    if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                                        infoPushSwitchImageView.setImageLevel(0);
                                    } else {
                                        infoPushSwitchImageView.setImageLevel(1);
                                    }
                                } else {
                                    ToastUtil.toastError(result);
                                }
                            }

                            @Override
                            public void onAllSetTimerPushResult(int result) {

                            }
                        };
                        if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                            timerPush.startTimerPush(messagePush.getType(), messagePush.getTaskId(), MessagePushStatus.ON);
                        } else {
                            timerPush.startTimerPush(messagePush.getType(), messagePush.getTaskId(), MessagePushStatus.OFF);
                        }
                    }
                });
            }
            select_room_ll.setVisibility(View.GONE);
            unknown_device_tv.setVisibility(View.GONE);
            add_remote_ll.setVisibility(View.GONE);
            arrow_type_iv.setVisibility(View.GONE);
            findViewById(R.id.add_remote_line).setVisibility(View.GONE);
            select_deviceType_ll.setVisibility(View.VISIBLE);
            type_tips_tv.setText(getString(R.string.device_belong));
            final Device device = new DeviceDao().selBelongDevice(mDevice.getModel(), DeviceType.ALLONE, mDevice.getUid());
            mDeviceType_tv.setText(device.getDeviceName());
            if (!ProductManage.isAlloneLearnDevice(mDevice)) {
                deviceInfoTextView.setText(getString(R.string.remote_control_change));
                final KKIr kkIr = KKIrDao.getInstance().getPower(mDevice.getDeviceId(), true);
                if (kkIr != null) {
                    add_remote_ll.setVisibility(View.VISIBLE);
                    mAddRemote_tv.setVisibility(View.INVISIBLE);
                    tip4_tv.setText(R.string.remote_control_tv_delete);
                    add_remote_ll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                            dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
                            dialogFragmentTwoButton.setContent(getString(R.string.allone_learn_tip3));
                            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                            dialogFragmentTwoButton.setRightButtonText(getString(R.string.confirm));
                            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                                @Override
                                public void onLeftButtonClick(View view) {
                                    dialogFragmentTwoButton.dismiss();
                                }

                                @Override
                                public void onRightButtonClick(View view) {
                                    dialogFragmentTwoButton.dismiss();
                                    showDialog();
                                    IrApi.deleteIrKey(userName, mDevice.getUid(), null, kkIr.getKkIrId(), 0, new BaseResultListener() {
                                        @Override
                                        public void onResultReturn(BaseEvent baseEvent) {
                                            dismissDialog();
                                            if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                                                EventBus.getDefault().post(new PartViewEvent());
                                                add_remote_ll.setVisibility(View.GONE);
                                                ToastUtil.showToast(R.string.success);
                                            } else {
                                                ToastUtil.toastError(baseEvent.getResult());
                                            }
                                        }
                                    });
                                }
                            });
                            dialogFragmentTwoButton.show(getFragmentManager(), "");

                        }
                    });
                }
                deviceInfoTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                        final AlloneSaveData saveData = new AlloneSaveData();
                        saveData.setData(mDevice.getCompany());
                        KookongSDK.getAllRemoteIds(AlloneUtil.getKKDeviceType(deviceType), saveData.getBrandId(), saveData.getSpId(), saveData.getAreaId(), new IRequestResult<RemoteList>() {
                            @Override
                            public void onSuccess(String msg, RemoteList result) {
                                final List<Integer> remoteids = result.rids;
                                if (remoteids != null && remoteids.size() > 0) {
                                    KookongSDK.getIRDataById(AlloneDataUtil.getLoadIrData(remoteids, 0), new IRequestResult<IrDataList>() {

                                        @Override
                                        public void onSuccess(String msg, IrDataList result) {
                                            dismissDialog();
                                            List<IrData> irDatas = result.getIrDataList();
                                            Intent intent = new Intent(SetDeviceActivity.this, RemoteControlActivity.class);
                                            intent.putExtra(IntentKey.DEVICE, mDevice);
                                            intent.putExtra(IntentKey.ALL_ONE_DATA, (Serializable) irDatas);
                                            intent.putIntegerArrayListExtra(IntentKey.ALL_ONE_REMOTE_IDS, (ArrayList<Integer>) remoteids);
                                            intent.putExtra(IntentKey.ALL_ONE_SAVE_DATA, saveData);
                                            intent.putExtra(IntentKey.IS_CHANGE_REMOTE, true);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onFail(String msg) {
                                            dismissDialog();
                                            ToastUtil.showToast(R.string.allone_error_data_tip);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onFail(String msg) {
                                dismissDialog();
                                ToastUtil.showToast(R.string.allone_error_data_tip);
                            }
                        });
                    }
                });
            } else {
                deviceInfoTextView.setText(getString(R.string.remote_control_modify));
                deviceInfoTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SetDeviceActivity.this, RemoteLearnActivity.class);
                        intent.putExtra(IntentKey.DEVICE, mDevice);
                        intent.putExtra(IntentKey.IS_START_LEARN, true);
                        intent.putExtra(IntentKey.IS_CHANGE_REMOTE, true);
                        startActivity(intent);
                    }
                });
            }
        }
        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                select_room_ll.setVisibility(View.GONE);
            }
        }
    }

    private void setIrRepeater() {
        String extAddr = mDevice.getExtAddr();
        DeviceDao deviceDao = new DeviceDao();
        int tvCount = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.TV);
        int acCount = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.AC);
        int stbCount = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.STB);
        int selfDefineCount = deviceDao.selDeviceCountByExtAddrAndDeviceType(currentMainUid, extAddr, DeviceType.SELF_DEFINE_IR);
        int totalCount = tvCount + acCount + stbCount + selfDefineCount;

        mRemoteCount = new RemoteCount();
        mRemoteCount.setTvCount(tvCount);
        mRemoteCount.setStbCount(stbCount);
        mRemoteCount.setAcCount(acCount);
        mRemoteCount.setSelfDefineCount(selfDefineCount);

        copyRemoteCount();

        String tempRemoteCount = getResources().getString(R.string.device_set_add_remote_count);
        String remoteCount = String.format(tempRemoteCount, totalCount);
        if (totalCount == 0) {
            mAddRemote_tv.setText(getResources().getString(R.string.device_set_add_remote_default));
        } else {
            mAddRemote_tv.setText(remoteCount);
        }
    }

    private void setRemoteView() {
        type_tips_tv.setText(R.string.device_set_button);
//        mDeviceType_tv.setText(R.string.device_set_action_content);
        mDeviceType_tv.setText("");
    }

    /**
     * 提示是否保存修改界面
     */
    private void showConfirmSavePopup() {
        if (mConfirmSavePopup == null) {
            mConfirmSavePopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    showDialog();
                    save();
                }

                @Override
                public void cancel() {
                    super.cancel();
                    finish();
                }
            };
        }
        mConfirmSavePopup.showPopup(mContext, R.string.save_content, R.string.save, R.string.unsave);
    }

    @Override
    public void leftTitleClick(View v) {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DEVICE, mDevice);
        setResult(RESULT_OK, intent);
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DEVICE, mDevice);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.deviceFaqTextView) {
            Intent intent = new Intent(this, SensorFAQActivity.class);
            intent.putExtra("device", mDevice);
            startActivity(intent);
        } else if (vId == R.id.deviceName) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_DeviceName), null);
            Intent intent = new Intent(this, DeviceNameActivity.class);
            mDevice = new DeviceDao().selDevice(mDevice.getUid(), mDevice.getDeviceId());
            intent.putExtra(Constant.DEVICE, mDevice);
            startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_NAME);
        } else if (vId == R.id.select_deviceType_ll) {
            if (mDevice.getDeviceType() == DeviceType.REMOTE || mDevice.getAppDeviceId() == AppDeviceId.REMOTE) {
                // 遥控器
                Intent intent;
                if (!StringUtil.isEmpty(mDevice.getModel()) && ProductManage.isStickers(mDevice.getModel())) {
                    intent = new Intent(mContext, DeviceSetJiyueRemoteActivity.class);
                } else {
                    intent = new Intent(mContext, DeviceSetRemoteActivity.class);
                }
                intent.putExtra(IntentKey.DEVICE, mDevice);
                startActivity(intent);
            } else {
                // select device type
                Intent intent = new Intent(mContext, SelectDeviceTypeActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_TYPE);
            }
        } else if (vId == R.id.select_room_ll) {
            // select room
            InputUtil.closeInput(mContext);

            if (isRoomNotSet()) {
                showRoomNotSetPopup();
            } else {
                if (mDevice.getAppDeviceId() == AppDeviceId.IR_REPEATER
                        || mDevice.getDeviceType() == DeviceType.IR_REPEATER) {
                    showModifyIrDeviceRoomPopup();
                } else {
                    if (mDevice.getDeviceType() == DeviceType.TEMPERATURE_SENSOR || mDevice.getDeviceType() == DeviceType.HUMIDITY_SENSOR) {
                        showSensorRoomPopup();
                    } else
                        showSelectRoomPopup();
                }
            }
        } else if (vId == R.id.unknown_device_tv) {
            // find device
            showFindDevicePopup();
        } else if (vId == R.id.learn_ir_tv) {
            if (mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF) {
                ActivityTool.toLearnIrActivity(SetDeviceActivity.this, REQUEST_CODE_EDIT_REMOTE, mDevice);
            }
            this.finish();
        } else if (vId == R.id.add_remote_tv) {
            final int deviceType = mDevice.getDeviceType();
            final int appDeviceId = mDevice.getAppDeviceId();
            if (deviceType == DeviceType.IR_REPEATER || appDeviceId == AppDeviceId.IR_REPEATER) {
                // 添加虚拟红外设备
                Intent intent = new Intent(mContext, DeviceSetIrRepeaterActivity.class);
                if (mRemoteCount != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("remoteCount", mRemoteCount);
                    bundle.putSerializable("oldRemoteCount", mOldRemoteCount);
                    bundle.putSerializable(IntentKey.DEVICE, mDevice);
                    intent.putExtras(bundle);
                }
                startActivityForResult(intent, REQUEST_CODE_ADD_REMOTE);
            } else if (deviceType == DeviceType.SCENE_KEYPAD || appDeviceId == AppDeviceId.SCENE_KEYPAD) {
                //情景面板
//                Intent intent = new Intent(context, DeviceSetRemoteActivity.class);
                Intent intent = new Intent(mContext, DeviceSetSceneButtonActivity.class);
                intent.putExtra(IntentKey.DEVICE, mDevice);
                startActivity(intent);
            }

        } else if (vId == R.id.deviceInfoTextView) {
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            intent.putExtra(Constant.DEVICE, mDevice);
            startActivity(intent);
        } else if (vId == R.id.deleteButton) {
            showDeleteDevicePopup();
        }
    }

    private void save() {
        // save device
        final int net = NetUtil.judgeNetConnect(mContext);
        if (net == NetType.NET_ERROR) {
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            return;
        } else if (net == NetType.GPRS) {
            ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
            return;
        }
        String name = deviceNameTextView.getText().toString().trim();
        if (StringUtil.isEmpty(name)) {
            if (DeviceUtil.isIrDevice(mDevice.getUid(), mDevice.getDeviceId())) {
                ToastUtil.toastError(ErrorCode.DEVICE_NAME_IR_NULL);
            } else {
                ToastUtil.toastError(ErrorCode.DEVICE_NAME_NULL);
            }
            return;
        }

//        if (mDevice.getRoomId().isEmpty()) {
//            ToastUtil.toastError( ErrorCode.ROOM_NOT_SELECT);
//            dismissDialog();
//            return;
//        }
        if (mOldDevice != null) {
            if (mOldDevice.getDeviceName().equals(name)
                    && mOldDevice.getRoomId().equals(mDevice.getRoomId())
                    && mOldDevice.getDeviceType() == mDevice.getDeviceType()) {
//                    DeviceTool.isIrDevice(uid,deviceId)
                int deviceType = mDevice.getDeviceType();
                if (DeviceUtil.isIrDevice(mDevice.getUid(), mDevice.getDeviceId())
                        && !DeviceTool.isIrDevicelearned(mDevice)) {
                    if (StringUtil.isEmpty(name)) {
                        showDeviceNameNotSetPopup();
                    } else {
                        finish();
                        ActivityTool.toLearnIrActivity(SetDeviceActivity.this, REQUEST_CODE_EDIT_REMOTE, mDevice);
                        return;
                    }
                    dismissDialog();
                } else {
//                    Intent intent = new Intent();
//                    intent.putExtra(IntentKey.DEVICE, mDevice);
//                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }
            }
        }

        //设备名称ok
        mDevice.setDeviceName(name);
        //设备类型ok
        if (mDevice.getDeviceType() != Constant.INVALID_NUM) {
            //房间ok
            showDialog();
            mModifyDevice.modify(mDevice.getUid(), mDevice.getUserName(),
                    mDevice.getDeviceName(), mDevice.getDeviceType(),
                    mDevice.getRoomId(), mDevice.getIrDeviceId(),
                    mDevice.getDeviceId());
        }
    }

    private void initModifyDevice() {
        mModifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                LogUtil.d(TAG, "onModifyDeviceResult()-uid:" + uid + ",serial:" + serial + ",result:" + result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    int deviceType = mDevice.getDeviceType();
                    if (deviceType == DeviceType.TV || (deviceType == DeviceType.AC && mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF)
                            || deviceType == DeviceType.STB || deviceType == DeviceType.SELF_DEFINE_IR) {
                        dismissDialog();
                        ActivityTool.toLearnIrActivity(SetDeviceActivity.this, REQUEST_CODE_EDIT_REMOTE, mDevice);
                    }
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
                        mFindPopup.showPopup(SetDeviceActivity.this, getString(R.string.device_set_find_content), getString(R.string.device_set_find_btn), null);
                    } else {
                        ToastUtil.toastError(result);
                    }
                }
            };
        }
        mControlDevice.identify(mDevice.getUid(), mDevice.getDeviceId());
    }

    private void showDeviceNameNotSetPopup() {
        if (mFindPopup == null) {
            mFindPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                }
            };
        }
        mFindPopup.showPopup(mContext, getString(R.string.device_set_set_remote_name_tips), getString(R.string.device_set_find_btn), null);
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
                canDeleteSingleDevice = true;
                if (deviceType == DeviceType.IR_REPEATER) {
                    //红外转发器
                    content = getString(R.string.device_set_delete_mul_ir_device_content);
                    canDeleteSingleDevice = false;
                }

            }
            if (ProductManage.isAlloneSunDevice(mDevice)) {
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

//        if (canDeleteSingleDevice) {
//            DialogFragmentThreeButton dialogFragmentThreeButton = new DialogFragmentThreeButton();
//            dialogFragmentThreeButton.setTitle(getString(R.string.warm_tips));
//            dialogFragmentThreeButton.setContent(content);
//            dialogFragmentThreeButton.setUpButtonText(getString(R.string.device_single_delete));
//            dialogFragmentThreeButton.setMiddleButtonText(getString(R.string.device_all_delete));
//            dialogFragmentThreeButton.setMiddleTextColor(getResources().getColor(R.color.red));
//            dialogFragmentThreeButton.setDownButtonText(getString(R.string.cancel));
//            dialogFragmentThreeButton.setOnThreeButtonClickListener(this);
//            dialogFragmentThreeButton.show(getFragmentManager(), "");
//        } else {
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
            dialogFragmentTwoButton.setContent(content);
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
            dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
            dialogFragmentTwoButton.show(getFragmentManager(), "");
//        }
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

        if (isDeleteAllDevice) {
            mDeleteDevice.deleteZigbeeDevice(mDevice.getUid(), mDevice.getUserName(),
                    mDevice.getDeviceId(), mDevice.getExtAddr(), mDevice.getDeviceType());
        } else {
            mDeleteDevice.deleteZigbeeDevice(mDevice.getUid(), mDevice.getUserName(),
                    mDevice.getDeviceId(), "", mDevice.getDeviceType());
        }
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

    private void showModifyIrDeviceRoomPopup() {
        if (mModifyIrDeviceRoomPopup == null) {
            mModifyIrDeviceRoomPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    showSelectRoomPopup();
                    dismiss();
                }
            };
        }
        mModifyIrDeviceRoomPopup.showPopup(mContext, getString(R.string.device_set_modify_ir_repeater_room_tips), getString(R.string.lock_continue_modify_password), getString(R.string.cancel));
    }

    /**
     * 温湿度传感器修改房间提醒
     */
    private void showSensorRoomPopup() {
        if (mModifyIrDeviceRoomPopup == null) {
            mModifyIrDeviceRoomPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    showSelectRoomPopup();
                    dismiss();
                }
            };
        }
        DeviceDao deviceDao = new DeviceDao();
        List<Device> devices = deviceDao.selIRDeviceByExtAddr(mDevice.getUid(), mDevice.getExtAddr());
        String content = null;
        if (devices != null && devices.size() > 1) {
            content = String.format(getString(R.string.sensor_change_room_tip), getDeleteOtherDeviceNames(devices));
        }
        mModifyIrDeviceRoomPopup.showPopup(mContext, content, getString(R.string.lock_continue_modify_password), getString(R.string.cancel));
    }

    private void showSelectRoomPopup() {
        //选择房间
        if (mSetRoomPopup == null) {
            mSetRoomPopup = new DeviceSetSelectRoomPopup(mContext, mDevice.getUid()) {
                @Override
                public void onSelect(Floor selectedFloor, Room selectedRoom) {
                    LogUtil.d(TAG, "onSelect()-floor:" + selectedFloor + ",room:" + selectedRoom);
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

    /**
     * @return true没有房间
     */
    private boolean isRoomNotSet() {
        return mDevice != null && !mRoomDao.hasRoom(mDevice.getUid());
    }

    private void copyDevice() {
        mOldDevice = new Device();
        mOldDevice.setRoomId(mDevice.getRoomId());
        mOldDevice.setDeviceName(mDevice.getDeviceName());
        mOldDevice.setDeviceType(mDevice.getDeviceType());
    }

    private void copyRemoteCount() {
        mOldRemoteCount = new RemoteCount();
        mOldRemoteCount.setTvCount(mRemoteCount.getTvCount());
        mOldRemoteCount.setStbCount(mRemoteCount.getStbCount());
        mOldRemoteCount.setAcCount(mRemoteCount.getAcCount());
        mOldRemoteCount.setSelfDefineCount(mRemoteCount.getSelfDefineCount());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEVICE_TYPE) {
            //设置设备类型
            if (data != null) {
                mDevice = (Device) data.getSerializableExtra(Constant.DEVICE);
                if (mDevice != null) {
//                int deviceType = data.getIntExtra("deviceType", mDevice.getDeviceType());
//                mDevice.setDeviceType(deviceType);
                    setDeviceType();
                }
            }
        } else if (requestCode == REQUEST_CODE_ADD_REMOTE) {
            if (data != null) {
                mRemoteCount = (RemoteCount) data.getSerializableExtra("remoteCount");
                if (mRemoteCount != null && mAddRemote_tv != null) {
                    copyRemoteCount();
                    String tempRemoteCount = getResources().getString(R.string.device_set_add_remote_count);
                    int count = mRemoteCount.getAcCount() + mRemoteCount.getSelfDefineCount() + mRemoteCount.getStbCount() + mRemoteCount.getTvCount();
                    String remoteCount = String.format(tempRemoteCount, count);
                    if (count == 0) {
                        mAddRemote_tv.setText(getResources().getString(R.string.device_set_add_remote_default));
                    } else {
                        mAddRemote_tv.setText(remoteCount);
                    }
                    LogUtil.d(TAG, "RemoteCount" + mRemoteCount);
                }
            }
        } else if (requestCode == REQUEST_CODE_EDIT_REMOTE) {
            if (data != null) {

            }
        } else if (requestCode == REQUEST_CODE_SET_DEVICE_NAME && resultCode == RESULT_OK) {
            mDevice = (Device) data.getSerializableExtra(Constant.DEVICE);
            if (mDevice != null && deviceNameTextView != null) {
                String name = mDevice.getDeviceName();
                if (!StringUtil.isEmpty(name)) {
                    name = name.trim();
                    deviceNameTextView.setText(name);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mControlDevice != null) {
            mControlDevice.stopControl();
            mControlDevice = null;
        }
        super.onDestroy();
    }

    @Override
    public void onLeftButtonClick(View view) {
        showDialog();
        deleteDevice();
    }

    @Override
    public void onRightButtonClick(View view) {

    }

//    @Override
//    public void onUpButtonClick(View view) {
//        isDeleteAllDevice = false;
//        showDialog();
//        deleteDevice();
//    }
//
//    @Override
//    public void onMiddleButtonClick(View view) {
//        isDeleteAllDevice = true;
//        showDialog();
//        deleteDevice();
//    }
//
//    @Override
//    public void onDownButtonClick(View view) {
//
//    }
}
