package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Message;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.device.manage.edit.SensorDeviceEditActivity;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.messagepush.SensorStatusRecordActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.DeviceCache;
import com.orvibo.homemate.sharedPreferences.TipsCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.MyCountdownTextView;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.TipsLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增传感器状态界面
 * created by wenchao
 */
public class NewSensorStatusActivity extends BaseControlActivity implements NavigationCocoBar.OnRightClickListener, OOReport.OnDeviceOOReportListener, MyCountdownTextView.OnCountdownFinishedListener {
    private static final String TAG = NewSensorStatusActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private MyCountdownTextView statusTextView;
    private View sensorRemainingBatteryLayout, sensorRemainingBatteryView;
    private TextView sensorStateRecord, sensorRemainingBattery, remainingBattery;
    private ImageView statusImageView, icon_mute, pic_skeleton;
    private TipsLayout tipsLayout;
    private LinearLayout sensorStateLayout;
    private MessageDao messageDao;
    private List<Message> messages = new ArrayList<>();
    private String userId;
    private RelativeLayout rl_content_ll;
    private DeviceStatus deviceStatus;
    private ControlDevice controlDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_sensor_status_activity);
        messageDao = new MessageDao();
        OOReport.getInstance(mAppContext).registerOOReport(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG,"onResume()");
        refresh();
    }

    private void refresh() {
        userId = UserCache.getCurrentUserId(mContext);
        messages = messageDao.selMessagesByUserIdAndDeviceId(userId, deviceId);
        if (device != null) {
            navigationBar.setCenterText(device.getDeviceName());
            if (device.getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                sensorRemainingBatteryView.setVisibility(View.GONE);
                sensorRemainingBatteryLayout.setVisibility(View.GONE);
            }
            deviceStatus = mDeviceStatusDao.selDeviceStatus(device.getUid(), deviceId);
            if (deviceStatus != null) {
//                int onlineState = deviceStatus.getOnline();
//                refreshOnOff(onlineState);
//                if (onlineState == OnlineStatus.ONLINE) {
                refreshStatus(deviceStatus.getValue1());
                refreshRemainingBattery(deviceStatus.getValue4());
//                }
            } else {
                LogUtil.e(TAG, "onResume()-Can't obtain " + deviceId + " device's deviceStatus at " + device.getUid());
            }
        }
        refreshStateRecord();
    }

    private void initControlDevice() {
        controlDevice = new ControlDevice(mAppContext) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                if (isFinishingOrDestroyed()) {
                    return;
                }
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.sensor_silent_success);
                }
            }
        };
    }

//    private void refreshOnOff(int ooStatus) {
//        refreshStatusImageViewByOnOff(ooStatus);
//        if (ooStatus == OnlineStatus.ONLINE) {
//            navigationBar.setBarColor(getResources().getColor(R.color.green));
//            rl_content_ll.setBackgroundColor(getResources().getColor(R.color.green));
////            statusTextView.setText(R.string.sensor_normal);
//        } else {
//            navigationBar.setBarColor(getResources().getColor(R.color.gray_white));
//            rl_content_ll.setBackgroundColor(getResources().getColor(R.color.gray_white));
//            statusTextView.setText(R.string.sensor_device_offline);
//            icon_mute.setVisibility(View.GONE);
//            pic_skeleton.setVisibility(View.GONE);
//        }
//    }

    private void refreshStatus(int value1) {
        if (device.getDeviceType() == DeviceType.SOS_SENSOR) {
            long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
//            if (deviceStatus != null) {
//                updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
//            }
            long sysTime = System.currentTimeMillis();
            long countDownTime = 3 * 60 * 1000L + updateTime - sysTime;//updateTime的值会更新导致没有触发客户端也会报警
            if (countDownTime > 3 * 60 * 1000L) {
                countDownTime = 3 * 60 * 1000L;
            }
            LogUtil.d(TAG, "countDownTime:" + countDownTime+", updateTime="+updateTime);
            if (value1 == DeviceStatusConstant.ALARM && countDownTime > 1000) {
                statusTextView.stopCountdown();
                statusTextView.startCountdown(mContext.getString(R.string.sensor_alarming) + "(%s)", (int) (countDownTime / 1000));
                value1 = 1;
            } else {
                value1 = DeviceStatusConstant.NOT_ALARM;
            }
        }
        refreshStatusImageViewByValue1(value1);
        if (value1 == DeviceStatusConstant.NOT_ALARM) {
            navigationBar.setBarColor(getResources().getColor(R.color.green));
            rl_content_ll.setBackgroundColor(getResources().getColor(R.color.green));
            statusTextView.setText(R.string.sensor_normal);
            if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                icon_mute.setVisibility(View.GONE);
            }
            pic_skeleton.setVisibility(View.GONE);
        } else {
            tipsLayout.showTips();
            navigationBar.setBarColor(getResources().getColor(R.color.red));
            rl_content_ll.setBackgroundColor(getResources().getColor(R.color.red));
            if (device.getDeviceType() != DeviceType.SOS_SENSOR) {
                statusTextView.setText(R.string.sensor_alarming);
            }
            if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                icon_mute.setVisibility(View.VISIBLE);
            }
            pic_skeleton.setVisibility(View.VISIBLE);
        }
    }

    private void refreshStatusImageViewByValue1(int value1) {
        if (value1 == DeviceStatusConstant.NOT_ALARM) {
            if (device.getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                statusImageView.setImageResource(R.drawable.pic_combustible_normal);
            } else if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_smoke_normal);
            } else if (device.getDeviceType() == DeviceType.WATER_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_water_normal);
            } else if (device.getDeviceType() == DeviceType.CO_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_co_normal);
            } else {
                statusImageView.setImageResource(R.drawable.pic_sos_normal);
            }
        } else {
            if (device.getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                statusImageView.setImageResource(R.drawable.pic_combustible_warming);
            } else if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_smoke_warning);
            } else if (device.getDeviceType() == DeviceType.WATER_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_water_warming);
            } else if (device.getDeviceType() == DeviceType.CO_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_co_warning);
            } else {
                statusImageView.setImageResource(R.drawable.pic_sos_warming);
            }
        }
    }

    private void refreshStatusImageViewByOnOff(int ooStatus) {
        if (ooStatus == OnlineStatus.ONLINE) {
            if (device.getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                statusImageView.setImageResource(R.drawable.pic_combustible_normal);
            } else if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_smoke_normal);
            } else if (device.getDeviceType() == DeviceType.WATER_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_water_normal);
            } else if (device.getDeviceType() == DeviceType.CO_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_co_normal);
            } else {
                statusImageView.setImageResource(R.drawable.pic_sos_normal);
            }
        } else {
            if (device.getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                statusImageView.setImageResource(R.drawable.pic_combustible_offline);
            } else if (device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_smoke_offline);
            } else if (device.getDeviceType() == DeviceType.WATER_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_water_offline);
            } else if (device.getDeviceType() == DeviceType.CO_SENSOR) {
                statusImageView.setImageResource(R.drawable.pic_co_offline);
            } else {
                statusImageView.setImageResource(R.drawable.pic_sos_offline);
            }
        }
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
        sensorStateRecord.setText(String.valueOf(messages.size()));
    }

    /**
     * 初始化控件、参数
     */
    private void initView() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        sensorStateRecord = (TextView) findViewById(R.id.sensorStateRecord);
        sensorRemainingBattery = (TextView) findViewById(R.id.sensorRemainingBattery);
        remainingBattery = (TextView) findViewById(R.id.remainingBattery);
        statusTextView = (MyCountdownTextView) findViewById(R.id.statusTextView);
        statusTextView.registerCountdownFinishedListener(this);
        statusImageView = (ImageView) findViewById(R.id.statusImageView);
        tipsLayout = (TipsLayout) findViewById(R.id.tipsLayout);
        sensorRemainingBatteryLayout = findViewById(R.id.sensorRemainingBatteryLayout);
        sensorRemainingBatteryView = findViewById(R.id.sensorRemainingBatteryView);

        String language = PhoneUtil.getPhoneLanguage(mAppContext);
        String tips = TipsCache.getTips(language);
        List<String> tipsList = new ArrayList<>();
        if (!TextUtils.isEmpty(tips)) {
            try {
                JSONArray tipsJsonArray = new JSONArray(tips);
                for (int i = 0; i < tipsJsonArray.length(); i++) {
                    JSONObject tipsJsonObject = tipsJsonArray.getJSONObject(i);
                    int deviceType = tipsJsonObject.getInt("deviceType");
                    if (deviceType == device.getDeviceType()) {
                        String text = tipsJsonObject.getString("text").replace("\\n", "\n");
                        tipsList.add(text);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tipsLayout.setTipsList(tipsList);
        if (TipsCache.isShowGuide()) {
            tipsLayout.sleep();
        } else {
            TipsCache.hasShowGuide(true);
        }
        icon_mute = (ImageView) findViewById(R.id.icon_mute);
        icon_mute.setOnClickListener(this);
        pic_skeleton = (ImageView) findViewById(R.id.pic_skeleton);
        sensorStateLayout = (LinearLayout) findViewById(R.id.sensorStateLayout);
        rl_content_ll = (RelativeLayout) findViewById(R.id.rl_content_ll);
        rl_content_ll.setOnClickListener(this);
        sensorStateLayout.setOnClickListener(this);
        navigationBar.setOnRightClickListener(this);
    }

    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport deviceId: " + deviceId + " statsType: " + statsType + " value1: " + value1 + " value2: " + value2 + " value3: " + value3 + " value4: " + value4 + " alarmType" + alarmType);
        if (device != null && device.getDeviceId().equals(deviceId)) {
            refreshStatus(value1);
            refreshRemainingBattery(value4);
        }
    }

    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent(this, SensorDeviceEditActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sensorStateLayout:
                Intent intent = new Intent(NewSensorStatusActivity.this, SensorStatusRecordActivity.class);
                //  Intent intent = new Intent(NewSensorStatusActivity.this, SensorMessageActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.rl_content_ll:
                tipsLayout.sleep();
                break;
            case R.id.icon_mute:
                if (controlDevice == null) {
                    initControlDevice();
                }
                controlDevice.mute(UserCache.getCurrentMainUid(mAppContext), device.getDeviceId());
                break;
        }

    }

    @Override
    protected void onDestroy() {
        OOReport.getInstance(mAppContext).removeOOReport(this);
        super.onDestroy();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        LogUtil.d(TAG,"onRefresh()");
        refresh();
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (!StringUtil.isEmpty(deviceId) && device != null && deviceId.equals(device.getDeviceId())) {
//            refreshOnOff(online);
        }
    }

    @Override
    public void onCountdownFinished() {
        LogUtil.d(TAG,"onCountdownFinished()");
        refresh();
    }
}
