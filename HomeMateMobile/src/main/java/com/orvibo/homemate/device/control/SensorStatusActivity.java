package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.StatusRecord;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.device.manage.edit.SensorDeviceEditActivity;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.messagepush.SensorStatusRecordActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.sharedPreferences.DeviceCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SensorStatusActivity extends BaseControlActivity implements NavigationCocoBar.OnRightClickListener, OOReport.OnDeviceOOReportListener {
    private static final String TAG = SensorStatusActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView sensorStateRecord, sensorRemainingBattery, remainingBattery, statusTextView;
    private ImageView statusImageView;
    private LinearLayout sensorStateLayout;
    //   private MessageDao messageDao;
    //  private List<Message> messages = new ArrayList<>();
    private String userId;
    private LinearLayout rl_content_ll;
    private DeviceStatus deviceStatus;
    //  private StatusRecordDao mStatusRecordDao;
    private List<StatusRecord> mStatusRecordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_status_activity);
        //messageDao = new MessageDao();
        //  mStatusRecordDao =  StatusRecordDao.getInstance();
        OOReport.getInstance(mAppContext).registerOOReport(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Serializable serializable = intent.getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();

        }
        refresh();
    }

    private void refresh() {
        userId = UserCache.getCurrentUserId(mContext);
        //  messages = messageDao.selMessagesByUserIdAndDeviceId(userId, deviceId);
        //   mStatusRecordList = mStatusRecordDao.selStatusRecordsByDeviceId(deviceId);
        if (device != null) {
            navigationBar.setCenterText(device.getDeviceName());
            deviceStatus = mDeviceStatusDao.selDeviceStatus(device.getUid(), deviceId);
            if (deviceStatus == null) {
                deviceStatus = new DeviceStatus();
                deviceStatus.setValue4(100);
                deviceStatus.setOnline(OnlineStatus.ONLINE);
            }
            refreshStatus(deviceStatus.getValue1());
            refreshRemainingBattery(deviceStatus.getValue4());
            refreshOnOff(deviceStatus.getOnline());
        }
        //  refreshStateRecord();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    private void refreshOnOff(int ooStatus) {//更新背景，在线人体红外信息展示人体红外信息展示
//        if (ooStatus == OnlineStatus.ONLINE) {
        navigationBar.setBarColor(getResources().getColor(R.color.green));
        rl_content_ll.setBackgroundColor(getResources().getColor(R.color.green));
        if (device.getDeviceType() == DeviceType.INFRARED_SENSOR) {
            if (deviceStatus != null && deviceStatus.getValue1() == DeviceStatusConstant.OFF) {
                statusImageView.setImageResource(R.drawable.pic_scene_infrared_someone);
                long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
                String record = time + " " + mContext.getString(R.string.infrared_sensor_alarm);
                statusTextView.setText(record);
            } else {
                statusImageView.setImageResource(R.drawable.pic_scene_infrared_normal);
                statusTextView.setText("");
            }
        }
//        } else {
//            navigationBar.setBarColor(getResources().getColor(R.color.gray_white));
//            rl_content_ll.setBackgroundColor(getResources().getColor(R.color.gray_white));
//            if (device.getDeviceType() == DeviceType.INFRARED_SENSOR) {
//                statusImageView.setImageResource(R.drawable.pic_scene_infrared_normal);
//            }
//            statusTextView.setText(R.string.sensor_device_offline);
//        }
    }

    private void refreshStatus(int value1) {//门窗开关状态
//        if (device.getDeviceType() == DeviceType.INFRARED_SENSOR) {
//            if (value1 == 0) {
//                statusTextView.setText(R.string.infrared_sensor_normal);
//                statusImageView.setImageResource(R.drawable.pic_scene_infrared_normal);
//            } else {
//                statusTextView.setText(R.string.infrared_sensor_alarm);
//                statusImageView.setImageResource(R.drawable.pic_scene_infrared_someone);
//            }
//        } else {
        long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
        updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
        String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
        if (device.getDeviceType() == DeviceType.MAGNETIC) {
            if (value1 == 0) {
                statusTextView.setText(time + " " + getString(R.string.magnetic_off));
                statusImageView.setImageResource(R.drawable.pic_door_close);
            } else {
                statusTextView.setText(time + " " + getString(R.string.magnetic_on));
                statusImageView.setImageResource(R.drawable.pic_door_open);
            }
        } else if (device.getDeviceType() == DeviceType.MAGNETIC_WINDOW) {
            if (value1 == 0) {
                statusTextView.setText(time + " " + getString(R.string.magnetic_off));
                statusImageView.setImageResource(R.drawable.pic_window_close);
            } else {
                statusTextView.setText(time + " " + getString(R.string.magnetic_on));
                statusImageView.setImageResource(R.drawable.pic_window_open);
            }
        } else if (device.getDeviceType() == DeviceType.MAGNETIC_DRAWER) {
            if (value1 == 0) {
                statusTextView.setText(time + " " + getString(R.string.magnetic_off));
                statusImageView.setImageResource(R.drawable.pic_drawer_close);
            } else {
                statusTextView.setText(time + " " + getString(R.string.magnetic_on));
                statusImageView.setImageResource(R.drawable.pic_drawer_open);
            }
        } else if (device.getDeviceType() == DeviceType.MAGNETIC_OTHER) {
            if (value1 == 0) {
                statusTextView.setText(time + " " + getString(R.string.magnetic_off));
                statusImageView.setImageResource(R.drawable.pic_other_close);
            } else {
                statusTextView.setText(time + " " + getString(R.string.magnetic_on));
                statusImageView.setImageResource(R.drawable.pic_other_open);
            }
        }
//        }
    }

    private void refreshRemainingBattery(int percent) {
        sensorRemainingBattery.setText(String.valueOf(percent) + "%");
        if (percent <= 10) {
            sensorRemainingBattery.setTextColor(getResources().getColor(R.color.scene_red));
            remainingBattery.setTextColor(getResources().getColor(R.color.scene_red));
        } else {
            sensorRemainingBattery.setTextColor(getResources().getColor(R.color.black));
            remainingBattery.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void refreshStateRecord() {
        sensorStateRecord.setText(String.valueOf(mStatusRecordList.size()));
    }

    /**
     * 初始化控件、参数
     */
    private void initView() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        sensorStateRecord = (TextView) findViewById(R.id.sensorStateRecord);
//        sensorStateRecord.setVisibility(View.GONE);
        sensorRemainingBattery = (TextView) findViewById(R.id.sensorRemainingBattery);
        remainingBattery = (TextView) findViewById(R.id.remainingBattery);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusImageView = (ImageView) findViewById(R.id.statusImageView);
        sensorStateLayout = (LinearLayout) findViewById(R.id.sensorStateLayout);
        rl_content_ll = (LinearLayout) findViewById(R.id.rl_content_ll);
        sensorStateLayout.setOnClickListener(this);
        navigationBar.setOnRightClickListener(this);
    }

    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport deviceId: " + deviceId + " statsType: " + statsType + " value1: " + value1 + " value2: " + value2 + " value3: " + value3 + " value4: " + value4 + " alarmType" + alarmType);

        refreshStatus(value1);
        refreshRemainingBattery(value4);
    }

    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent(this, SensorDeviceEditActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            logout();
//        }
//    }

    @Override
    public void onClick(View v) {
        //  Intent intent = new Intent(SensorStatusActivity.this, SensorMessageActivity.class);
        Intent intent = new Intent(SensorStatusActivity.this, SensorStatusRecordActivity.class);

        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

//    private void adminLogin() {
//        if (adminLogin == null) {
//            adminLogin = new AdminLogin(ViHomeApplication.getAppContext()) {
//                @Override
//                protected void onLogin(int result) {
//                    Intent intent = new Intent(SensorStatusActivity.this, SensorMessageActivity.class);
//                    intent.putExtra(IntentKey.DEVICE, device);
//                    startActivity(intent);
//                }
//            };
//        }
//        String userName = UserCache.getCurrentUserName(ViHomeApplication.getAppContext());
//        String md5Password = UserCache.getMd5Password(ViHomeApplication.getAppContext(), userName);
//        if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(md5Password)) {
//            ToastUtil.showToast(R.string.login_tip);
//            return;
//        }
//        adminLogin.login(userName, md5Password, true);
//    }

//    private void logout() {
//        LogUtil.d(TAG, "logout()");
//        new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
//    }

    @Override
    protected void onDestroy() {
        OOReport.getInstance(mAppContext).removeOOReport(this);
        super.onDestroy();
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (!StringUtil.isEmpty(deviceId) && device != null && deviceId.equals(device.getDeviceId())) {
            refreshOnOff(online);
        }
    }
}
