package com.orvibo.homemate.device.manage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.DoorLockRecordData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.DoorLockRecordDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.device.BaseDevicesAdapter;
import com.orvibo.homemate.device.smartlock.LockRecordActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.sharedPreferences.DeviceCache;
import com.orvibo.homemate.sharedPreferences.GatewayOnlineCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.password.PasswordForgotActivity;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LockUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DeviceCustomView;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.MyCountdownTextView;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.dialog.ToastDialog;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.PasswordErrorPopup;
import com.orvibo.homemate.view.popup.UnlockPopup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allen on 2016/1/4.
 */
public class ClassificationActivity extends BaseActivity implements OnPropertyReportListener, OOReport.OnDeviceOOReportListener {
    private static final String TAG = ClassificationActivity.class.getSimpleName();
    private ListView classificationListView;
    private NavigationCocoBar navigationCocoBar;
    private OpenlockPopup openlockPopup;
    private InputErrorPopup inputErrorPopup;
    private ModifyPopup modifyPopup;
    private List<Device> devices;
    private ClassificationAdapter mDevicesAdapter;
    private int deviceType;
    private ControlDevice mControlDevice;
    private MessagePushDao messagePushDao;
    private DeviceDao mDeviceDao;
    private RoomDao mRoomDao;
    private DeviceStatusDao deviceStatusDao;
    private SensorTimerPush sensorTimerPush;
    private Animation warningAnimation;
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<>();
    private FloorDao mFloorDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICES);
        if (serializable != null) {
            devices = (List<Device>) serializable;
            if (!devices.isEmpty()) {
                deviceType = devices.get(0).getDeviceType();
            }
            setContentView(R.layout.activity_classification);
            init();
        } else {
            finish();
        }
    }

    private void init() {
        classificationListView = (ListView) findViewById(R.id.classificationListView);
        List<DeviceStatus> deviceStatuses = new DeviceStatusDao().selDevicesStatuses(UserCache.getCurrentMainUid(mAppContext), devices);
        mDevicesAdapter = new ClassificationAdapter(this, devices, deviceStatuses, this);
        classificationListView.setAdapter(mDevicesAdapter);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationCocoBar);
        if (deviceType == DeviceType.MAGNETIC || deviceType == DeviceType.MAGNETIC_DRAWER || deviceType == DeviceType.MAGNETIC_WINDOW || deviceType == DeviceType.MAGNETIC_OTHER) {
            navigationCocoBar.setCenterText(getString(R.string.device_magnetic_sensor_name));
        } else {
            navigationCocoBar.setCenterText(getString(DeviceTool.getDeviceTypeNameResId(deviceType)));
        }
        openlockPopup = new OpenlockPopup();
        inputErrorPopup = new InputErrorPopup();
        modifyPopup = new ModifyPopup();
        messagePushDao = new MessagePushDao();
        mDeviceDao = new DeviceDao();
        mRoomDao = new RoomDao();
        mFloorDao = new FloorDao();
        deviceStatusDao = new DeviceStatusDao();
        warningAnimation = AnimationUtils.loadAnimation(mContext, R.anim.security_alarm);
        initControl();
        initSensorTimerPush();
        initRooms(devices);
        PropertyReport.getInstance(mAppContext).registerPropertyReport(this);
        OOReport.getInstance(mAppContext).registerOOReport(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        DeviceDao deviceDao = new DeviceDao();
        if (deviceType == DeviceType.MAGNETIC || deviceType == DeviceType.MAGNETIC_DRAWER || deviceType == DeviceType.MAGNETIC_WINDOW || deviceType == DeviceType.MAGNETIC_OTHER) {//协议写的不好，同一个设备有多种分类而没有进行二级分类，没办法只能写成这样了
            devices = deviceDao.selDevicesByDeviceType(UserCache.getCurrentMainUid(mContext), DeviceType.MAGNETIC);
            devices.addAll(deviceDao.selDevicesByDeviceType(UserCache.getCurrentMainUid(mContext), DeviceType.MAGNETIC_DRAWER));
            devices.addAll(deviceDao.selDevicesByDeviceType(UserCache.getCurrentMainUid(mContext), DeviceType.MAGNETIC_WINDOW));
            devices.addAll(deviceDao.selDevicesByDeviceType(UserCache.getCurrentMainUid(mContext), DeviceType.MAGNETIC_OTHER));
        } else {
            devices = deviceDao.selDevicesByDeviceType(UserCache.getCurrentMainUid(mContext), deviceType);
        }
        initRooms(devices);
        mDevicesAdapter.notifyDataSetChanged();
        if (devices.isEmpty()) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initControl() {
        // 控制回调
        mControlDevice = new ControlDevice(mAppContext) {

            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                LogUtil.d(TAG, "onControlDeviceResult()-uid:" + uid + ",deviceId:" + deviceId + ",result:" + result);
                dismissDialog();
                Device device = mDeviceDao.selDevice(uid, deviceId);
                int deviceType = 0;
                if (device != null) {
                    deviceType = device.getDeviceType();
                }
                if (deviceType == DeviceType.LOCK) {
                    if (openlockPopup != null && openlockPopup.isShowing()) {
                        openlockPopup.dismiss();
                        if (result != ErrorCode.SUCCESS) {
                            ToastUtil.toastError(result);
                        }
                    }
                } else {
                    if (result == ErrorCode.OFFLINE_GATEWAY) {
                        //coco离线
                        ToastUtil.toastError(result);
                        if (ProductManage.getInstance().isWifiDevice(device) && mDevicesAdapter != null) {
                            mDevicesAdapter.refreshOnline(uid, deviceId, OnlineStatus.ONLINE);
                        }
                    } else if (result != ErrorCode.SUCCESS) {
                        ToastUtil.toastError(result);
                    }
                }
            }
        };
    }

    private void initSensorTimerPush() {
        sensorTimerPush = new SensorTimerPush(mAppContext) {
            @Override
            public void onSensorTimerPushResult(int result, int type) {
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    mDevicesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onAllSensorSetTimerPushResult(int result) {

            }
        };
    }

    // 设备属性报告
    @Override
    public void onPropertyReport(String uid, String deviceId, int deviceType,
                                 int appDeviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.i(TAG, "onPropertyReport()-uid:" + uid + ",deviceId:" + deviceId + ",value1:" + value1 + ",value2:" + value2 + ",value3:" + value3 + ",value4:" + value4);
        if (deviceType == DeviceType.LOCK) {
            Device device = new DeviceDao().selDevice(deviceId);
            if (!ProductManage.isSmartLock(device)) {
                if (value1 == DeviceStatusConstant.ON) {
                    ToastDialog.show(mContext, getResources().getString(R.string.lock_opened));
                } else if (value1 == DeviceStatusConstant.OFF) {
                    ToastDialog.show(mContext, getResources().getString(R.string.lock_closed));
                }
            }
        }

        if (mDevicesAdapter != null) {
            mDevicesAdapter.refreshOnline(uid, deviceId, OnlineStatus.ONLINE);
            mDevicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (mDevicesAdapter != null) {
            mDevicesAdapter.refreshOnline(uid, deviceId, online);
            mDevicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unlock_tv:
                lockControl(v);
                break;
            case R.id.message_tv:
                onOffSensorMsg(v);
                break;
            case R.id.itemView:
                Device device = (Device) v.getTag(R.id.tag_device);
                if (deviceType == DeviceType.LOCK) {
                    if (ProductManage.isSmartLock(device)) {
                        Intent intent = new Intent(mContext, LockRecordActivity.class);
                        intent.putExtra(IntentKey.DEVICE, device);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, SetDeviceActivity.class);
                        intent.putExtra(IntentKey.DEVICE, device);
                        startActivity(intent);
                    }
                } else {
                    String actionActivityName = DeviceTool.getControlActivityNameByDeviceType(device);
                    if (!StringUtil.isEmpty(actionActivityName)) {
                        Intent intent = new Intent();
                        intent.setClassName(this, actionActivityName);
                        intent.putExtra(IntentKey.DEVICE, device);
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    private void onOffSensorMsg(View view) {
        MessagePush messagePush = (MessagePush) view.getTag(R.id.tag_message_push);
        Device device = (Device) view.getTag(R.id.tag_device);
        if (messagePush == null) {
            messagePush = new MessagePush();
            messagePush.setTaskId(device.getDeviceId());
            messagePush.setIsPush(MessagePushStatus.ON);
            messagePush.setType(MessagePushType.SINGLE_SENSOR_TYPE);
            messagePush.setStartTime("00:00:00");
            messagePush.setEndTime("00:00:00");
            messagePush.setWeek(255);
        }
        if (messagePush.getIsPush() == MessagePushStatus.ON) {
            messagePush.setIsPush(MessagePushStatus.OFF);
        } else {
            messagePush.setIsPush(MessagePushStatus.ON);
        }
        sensorTimerPush.startSetDeviceTimerPush(device.getDeviceId(), messagePush.getIsPush(), messagePush.getStartTime(), messagePush.getEndTime(), messagePush.getWeek());

    }

    private void lockControl(View v) {
        Account account = new AccountDao().selAccountByUserId(userId);
        if (TextUtils.isEmpty(account.getEmail()) && TextUtils.isEmpty(account.getPhone())) {
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.warm_tips));
            dialogFragmentOneButton.setContent(getString(R.string.lock_phone_email_empty));
            dialogFragmentOneButton.show(getFragmentManager(), "");
        } else {
//        int curStatus = Integer.valueOf(v.getContentDescription() + "");
            final Device device = (Device) v.getTag(R.id.tag_device);
//        LogUtil.d(TAG, "deviceControl()-device:" + device + ",status:" + curStatus);
            final String uid = device.getUid();
            final String deviceId = device.getDeviceId();
            if (v.getId() == R.id.lock_tv) {
                //off
//            showDeviceControlProgressDialog();
                mControlDevice.lock(uid, deviceId);
            } else {
                openlockPopup.showPopup(mContext, mControlDevice, uid, deviceId);
            }
        }
    }

    private class OpenlockPopup extends UnlockPopup {

        @Override
        public void forgetPassword() {
            if (!isFinishingOrDestroyed()) {
                dismiss();
                modifyPopup.showPopup(mContext, getResources()
                        .getString(R.string.lock_modify_password_tips), getResources()
                        .getString(R.string.lock_continue_modify_password), getResources()
                        .getString(R.string.lock_quit_modify_password));
            }
        }

        @Override
        public void showInputErrorPopup(String title, String yes) {
            dismiss();
            inputErrorPopup.showPopup(ClassificationActivity.this, title, yes);
        }
    }

    private class InputErrorPopup extends PasswordErrorPopup {

        public void confirm() {
            dismiss();
        }

        @Override
        public void forgetPassword() {
            dismiss();
            modifyPopup.showPopup(mContext, getResources()
                    .getString(R.string.lock_modify_password_tips), getResources()
                    .getString(R.string.lock_continue_modify_password), getResources()
                    .getString(R.string.lock_quit_modify_password));
        }
    }

    private class ModifyPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            Intent intent = new Intent(mAppContext, PasswordForgotActivity.class);
            startActivity(intent);
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private class ClassificationAdapter extends BaseDevicesAdapter implements MyCountdownTextView.OnCountdownFinishedListener {
        private final Resources mRes;
        private View.OnClickListener mClickListener;
        private int SMART_LOCK = 0;
        private int NORMAL_LOCK = 1;
        private int SENSOR = 2;

        public ClassificationAdapter(Activity context, List<Device> devices, List<DeviceStatus> deviceStatuses, View.OnClickListener clickListener) {
            super(context, devices, deviceStatuses);
            mRes = context.getResources();
            mClickListener = clickListener;
        }

        @Override
        public int getItemViewType(int position) {
            Device device = devices.get(position);
            if (deviceType == DeviceType.LOCK) {//锁
                if (ProductManage.isSmartLock(device)) {//智能门锁
                    return SMART_LOCK;
                } else {//旧门锁
                    return NORMAL_LOCK;
                }
            } else {//传感器
                return SENSOR;
            }
        }

        @Override
        public int getViewTypeCount() {
            return SENSOR + 1;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = devices.get(position);
            int itemType = getItemViewType(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                if (itemType == SMART_LOCK) {
                    convertView = View.inflate(mAppContext, R.layout.device_item_arrow_security, null);
                    viewHolder.lockRecordTextView = (TextView) convertView.findViewById(R.id.lockRecordTextView);
                } else if (itemType == NORMAL_LOCK) {
                    convertView = View.inflate(mAppContext, R.layout.device_item_lock, null);
                    viewHolder.unlock_tv = (TextView) convertView.findViewById(R.id.unlock_tv);
                } else {
                    convertView = View.inflate(mAppContext, R.layout.device_item_sensor, null);
                    viewHolder.message_tv = (TextView) convertView.findViewById(R.id.message_tv);
                    viewHolder.warning = (ImageView) convertView.findViewById(R.id.warningImageView);
                    viewHolder.warningTextView = (MyCountdownTextView) convertView.findViewById(R.id.warningTextView);
                }
                viewHolder.deviceCustomView = (DeviceCustomView) convertView.findViewById(R.id.deviceCustomView);
                viewHolder.itemView = convertView.findViewById(R.id.itemView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            convertView.setBackgroundResource(R.drawable.item_selector);
            viewHolder.deviceCustomView.setTextColor(getResources().getColor(R.color.black), getResources().getColor(R.color.gray));
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(device.getUid(), device.getDeviceId());
            if (itemType == NORMAL_LOCK) {
                viewHolder.unlock_tv.setOnClickListener(mClickListener);
                viewHolder.unlock_tv.setTag(R.id.tag_device, device);
            } else if (itemType == SMART_LOCK) {
                DoorLockRecordData doorLockRecordData = DoorLockRecordDao.getInstance().getRecentRecord(device.getDeviceId());
                String[] doorLockRecordDataRecent = getDoorLockRecordAndDeviceName(doorLockRecordData);
                if (!TextUtils.isEmpty(doorLockRecordDataRecent[1])) {
                    viewHolder.lockRecordTextView.setText(doorLockRecordDataRecent[0]);
                } else {
                    viewHolder.lockRecordTextView.setText("");
                }
            } else {
                boolean hasWarning = false;
                long countDownTime = 0;
                if (deviceStatus != null) {
                    if (ProductManage.getInstance().canWarning(device)) {//可报警设备
                        long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                        updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                        long sysTime = System.currentTimeMillis();
                        countDownTime = 3 * 60 * 1000L + updateTime - sysTime;
                        if (countDownTime > 3 * 60 * 1000L) {
                            countDownTime = 3 * 60 * 1000L;
                        }
                        //设备在线并且处于报警状态，紧急按钮
                        if (deviceStatus.getOnline() == OnlineStatus.ONLINE && deviceStatus.getValue1() == DeviceStatusConstant.ALARM && (deviceType != DeviceType.SOS_SENSOR || countDownTime > 0)) {
                            LogUtil.d(TAG, "countDownTime:" + countDownTime);
                            hasWarning = true;
                        }
                    }
                }
                if (hasWarning) {
                    viewHolder.warningTextView.stopCountdown();
                    viewHolder.warningTextView.setTextColor(getResources().getColor(R.color.white));
                    viewHolder.deviceCustomView.setTextColor(getResources().getColor(R.color.white), getResources().getColor(R.color.white));
                    convertView.setBackgroundResource(R.color.warning_red);
                    viewHolder.warning.setVisibility(View.VISIBLE);
                    viewHolder.warning.startAnimation(warningAnimation);
                    String waring;
                    if (deviceType == DeviceType.WATER_SENSOR) {
                        waring = mContext.getString(R.string.sensor_leakage);
                    } else {
                        waring = mContext.getString(R.string.sensor_alarming);
                    }
                    if (deviceType == DeviceType.SOS_SENSOR && countDownTime > 1000) {
                        viewHolder.warningTextView.startCountdown(waring + " %s", (int) (countDownTime / 1000));
                        viewHolder.warningTextView.registerCountdownFinishedListener(this);
                    } else {
                        viewHolder.warningTextView.setText(waring);
                    }
                } else {
                    viewHolder.warning.setVisibility(View.GONE);
                    viewHolder.warning.clearAnimation();
                    viewHolder.warningTextView.setTextColor(getResources().getColor(R.color.gray));
                    viewHolder.message_tv.setOnClickListener(mClickListener);
                    viewHolder.message_tv.setTag(R.id.tag_device, device);
                    String record = "";
                    if (deviceStatus != null && deviceType == DeviceType.TEMPERATURE_SENSOR) {
                        if (!CommonCache.isCelsius()) {
                            record = mContext.getString(R.string.sensor_temperature) + MathUtil.geFahrenheitData(MathUtil.getRoundData(deviceStatus.getValue1())) + CommonCache.getTemperatureUnit();
                        } else {
                            record = mContext.getString(R.string.sensor_temperature) + MathUtil.getRoundData(deviceStatus.getValue1()) + CommonCache.getTemperatureUnit();
                        }
                    } else if (deviceStatus != null && deviceType == DeviceType.HUMIDITY_SENSOR) {
                        record = mContext.getString(R.string.sensor_humidity) + MathUtil.getRoundData(deviceStatus.getValue2()) + "%";
                    }  else if(ProductManage.getInstance().canWarning(device)){
                        record = getString(R.string.sensor_normal);
                    } else if (deviceType == DeviceType.INFRARED_SENSOR) {
                        if (deviceStatus != null && deviceStatus.getValue1() == DeviceStatusConstant.OFF) {
                            long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                            updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                            String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
                            record = time + " " + mContext.getString(R.string.infrared_sensor_alarm);
                            viewHolder.warningTextView.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.warningTextView.setVisibility(View.GONE);
                        }
                    } else if (deviceStatus != null && DeviceUtil.isMagnetic(deviceType)) {
                        long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                        updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                        String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
                        if (deviceStatus.getValue1() == DeviceStatusConstant.OFF) {
                            record = time + " " + mContext.getString(R.string.magnetic_on);
                        } else {
                            record = time + " " + mContext.getString(R.string.magnetic_off);
                        }
                        viewHolder.warningTextView.setVisibility(View.VISIBLE);
                    }
                    viewHolder.warningTextView.setText(record);
                }
            }
            boolean isOnline = isOnline(device);
            Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
            viewHolder.deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
            viewHolder.deviceCustomView.setDeviceInfo(device.getDeviceName(), getRoom(device.getUid(), device.getDeviceId()));
            viewHolder.itemView.setOnClickListener(mClickListener);
            convertView.setTag(R.id.tag_device, device);
            viewHolder.itemView.setTag(R.id.tag_device, device);
            return convertView;
        }

        @Override
        public void onCountdownFinished() {
            notifyDataSetChanged();
        }

        private boolean isOnline(Device device) {
            boolean isOnline = true;
            final String deviceId = device.getDeviceId();
            boolean isWifiDevice = ProductManage.getInstance().isWifiDevice(device);
            synchronized (this) {
                try {
                    if (isWifiDevice) {
                        isOnline = isOnline(deviceId);
                    } else if (ProductManage.getInstance().isVicenter300ByDeviceType(device.getDeviceType())) {
                        String currentMainUid = UserCache.getCurrentMainUid(ViHomeApplication.getAppContext());
                        isOnline = GatewayOnlineCache.isOnline(mContext, currentMainUid);
                    } else if (!deviceId.equals(Constant.NULL_DATA)) {
                        isOnline = isOnline(deviceId);
                    }
                } catch (NullPointerException e) {
                }
            }
            return isOnline;
        }

        class ViewHolder {
            private TextView unlock_tv;
            private ImageView warning;
            private MyCountdownTextView warningTextView;
            private TextView message_tv, lockRecordTextView;
            private DeviceCustomView deviceCustomView;
            private View itemView;
        }

        private String[] getDoorLockRecordAndDeviceName(DoorLockRecordData doorLockRecordData) {
            String record = "";
            String deviceName = "";
            if (doorLockRecordData != null) {
                Device device = mDeviceDao.selDevice(doorLockRecordData.getDeviceId());
                if (device != null) {
                    deviceName = device.getDeviceName();
                }
                String time = TimeUtil.secondToDateString((int) (doorLockRecordData.getCreateTime() / 1000));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(time).append(" ");
                DoorUserData doorUserData = doorLockRecordData.getDoorUser();
                //门锁开锁显示钥匙开门
                if (doorLockRecordData.getType() == 5) {
                    stringBuilder.append(LockUtil.getLockType(doorLockRecordData.getType())).append(" ").append(mContext.getString(R.string.action_unlock_door));
                } else if (doorUserData != null)
                    stringBuilder.append(TextUtils.isEmpty(doorUserData.getName()) ? mContext.getString(R.string.user)+" "+doorUserData.getAuthorizedId() : doorUserData.getName()).append(" ").append(mContext.getString(R.string.action_unlock_door));
                else if (doorLockRecordData.getType() >= 6)
                    stringBuilder.append(LockUtil.getLockType(doorLockRecordData.getType()));
                else
                    stringBuilder.append(mContext.getString(R.string.lock_type_expired)).append(" ").append(mContext.getString(R.string.action_unlock_door));
                record = stringBuilder.toString();
            }
            return new String[]{record, deviceName};
        }
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
        LogUtil.d(TAG, "initRooms()-mFloorNameAndRoomNames:" + mFloorNameAndRoomNames);
    }

    /**
     * 没有楼层和房间时不需要显示：未设定区域和房间
     *
     * @param uid
     * @param deviceId
     * @return
     */
    private String getRoom(String uid, String deviceId) {
        final String key = uid + "_" + deviceId;
        String name = null;
        List<Floor> mFloors = mFloorDao.selAllFloors(uid);
        List<Room> rooms = mRoomDao.selAllRooms(uid);
        if (!(mFloors.isEmpty() && rooms.isEmpty())) {
            name = ViHomeProApp.getContext().getString(R.string.device_manage_room_not_set);
        }
        if (mFloorNameAndRoomNames.containsKey(key)) {
            name = mFloorNameAndRoomNames.get(key);
        }
        return name;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OOReport.getInstance(mAppContext).removeOOReport(this);
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        if (mControlDevice != null) {
            mControlDevice.stopControl();
        }
        if (openlockPopup != null && openlockPopup.isShowing()) {
            openlockPopup.dismiss();
        }

        if (inputErrorPopup != null && inputErrorPopup.isShowing()) {
            inputErrorPopup.dismiss();
        }
        if (inputErrorPopup != null && inputErrorPopup.isShowing()) {
            inputErrorPopup.dismiss();
        }
        ToastDialog.cancel();
    }
}
