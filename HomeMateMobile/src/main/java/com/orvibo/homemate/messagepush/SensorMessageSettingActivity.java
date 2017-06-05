package com.orvibo.homemate.messagepush;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.popup.DeviceSetTimePopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 2015/10/12.
 */
public class SensorMessageSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{
    private ImageView infoPushSwitchImageView;
    private TextView timeIntervalTextView;
    private LinearLayout timeIntervalLinearLayout, timeStartLinearLayout, timeEndLinearLayout, timeRepeatLinearLayout;
    private DeviceSetTimePopup deviceSetTimePopup;
    private TextView timeStartTextView, timeEndTextView,timeRepeatTextView;
    private CheckBox timeRepeatSunCheckBox, timeRepeatMonCheckBox, timeRepeatTuesCheckBox;
    private CheckBox timeRepeatWedCheckBox, timeRepeatThurCheckBox, timeRepeatFriCheckBox, timeRepeatSatCheckBox;
    private boolean isChooseStart = true;
    private int startHour, startMinute, endHour, endMinute;
    private List<CheckBox> weekCheckBoxes = new ArrayList<CheckBox>();
    private List<Integer> weeks = new ArrayList<Integer>();
    private Device device;
    private MessagePush messagePush;
    private SensorTimerPush sensorTimerPush;
    private boolean isBackPressed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra(Constant.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            setContentView(R.layout.sensor_message_setting_activity);
            findViews();
            init();
        } else {
            finish();
        }
    }

    private void findViews() {
        infoPushSwitchImageView = (ImageView) findViewById(R.id.infoPushSwitchImageView);
        timeIntervalTextView = (TextView) findViewById(R.id.timeIntervalTextView);
        timeIntervalLinearLayout = (LinearLayout) findViewById(R.id.timeIntervalLinearLayout);
        timeStartLinearLayout = (LinearLayout) findViewById(R.id.timeStartLinearLayout);
        timeEndLinearLayout = (LinearLayout) findViewById(R.id.timeEndLinearLayout);
        timeRepeatLinearLayout = (LinearLayout) findViewById(R.id.timeRepeatLinearLayout);
        timeStartTextView = (TextView) findViewById(R.id.timeStartTextView);
        timeEndTextView = (TextView) findViewById(R.id.timeEndTextView);
        timeRepeatTextView = (TextView) findViewById(R.id.timeRepeatTextView);
        timeRepeatSunCheckBox = (CheckBox) findViewById(R.id.timeRepeatSunCheckBox);
        timeRepeatMonCheckBox = (CheckBox) findViewById(R.id.timeRepeatMonCheckBox);
        timeRepeatTuesCheckBox = (CheckBox) findViewById(R.id.timeRepeatTuesCheckBox);
        timeRepeatWedCheckBox = (CheckBox) findViewById(R.id.timeRepeatWedCheckBox);
        timeRepeatThurCheckBox = (CheckBox) findViewById(R.id.timeRepeatThurCheckBox);
        timeRepeatFriCheckBox = (CheckBox) findViewById(R.id.timeRepeatFriCheckBox);
        timeRepeatSatCheckBox = (CheckBox) findViewById(R.id.timeRepeatSatCheckBox);
    }

    private void init() {
        infoPushSwitchImageView.setOnClickListener(this);
        timeRepeatSunCheckBox.setOnCheckedChangeListener(this);
        timeRepeatMonCheckBox.setOnCheckedChangeListener(this);
        timeRepeatTuesCheckBox.setOnCheckedChangeListener(this);
        timeRepeatWedCheckBox.setOnCheckedChangeListener(this);
        timeRepeatThurCheckBox.setOnCheckedChangeListener(this);
        timeRepeatFriCheckBox.setOnCheckedChangeListener(this);
        timeRepeatSatCheckBox.setOnCheckedChangeListener(this);
        weekCheckBoxes.add(timeRepeatMonCheckBox);
        weekCheckBoxes.add(timeRepeatTuesCheckBox);
        weekCheckBoxes.add(timeRepeatWedCheckBox);
        weekCheckBoxes.add(timeRepeatThurCheckBox);
        weekCheckBoxes.add(timeRepeatFriCheckBox);
        weekCheckBoxes.add(timeRepeatSatCheckBox);
        weekCheckBoxes.add(timeRepeatSunCheckBox);
        initSensorTimerPush();
        initDeviceSetTimePopup();
        refresh();
    }

    private void refresh() {
        String deviceId = device.getDeviceId();
        MessagePushDao messagePushDao = new MessagePushDao();
        messagePush = messagePushDao.selMessagePushByDeviceId(deviceId);
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
        if (allMessagePush != null && allMessagePush.getIsPush() == MessagePushStatus.OFF){
            messagePush.setIsPush(MessagePushStatus.OFF);
        }
        setStatus();
        WeekUtil.initWeekCheckBoxes(mAppContext, messagePush.getWeek(), weekCheckBoxes);
        setTimeInterval(messagePush);
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        sensorTimerPush.startSetDeviceTimerPush(device.getDeviceId(), messagePush.getIsPush(), messagePush.getStartTime(), messagePush.getEndTime(),messagePush.getWeek());
        showDialogNow();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    private void initSensorTimerPush() {
        sensorTimerPush = new SensorTimerPush(mAppContext) {
            @Override
            public void onSensorTimerPushResult(int result,int type) {
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                }
                refresh();
                if (isBackPressed) {
                    finish();
                }
            }

            @Override
            public void onAllSensorSetTimerPushResult(int result) {

            }
        };
    }

    private void initDeviceSetTimePopup() {
        deviceSetTimePopup = new DeviceSetTimePopup(mContext) {
            @Override
            public void onSetTime(int hour, int minute) {
                if (isChooseStart) {
                    startHour = hour;
                    startMinute = minute;
                } else {
                    endHour = hour;
                    endMinute = minute;
                }
                setTimeInterval();
            }
        };
    }

    private void setStatus() {
        if (messagePush.getIsPush() == MessagePushStatus.ON) {
            infoPushSwitchImageView.setImageLevel(1);
            timeStartLinearLayout.setOnClickListener(this);
            timeEndLinearLayout.setOnClickListener(this);
            timeIntervalLinearLayout.setBackgroundResource(R.color.white);
            timeRepeatLinearLayout.setBackgroundResource(R.color.white);
            timeIntervalTextView.setBackgroundResource(R.color.white);
            timeRepeatTextView.setBackgroundResource(R.color.white);
            timeStartTextView.setTextColor(getResources().getColor(R.color.green));
            timeEndTextView.setTextColor(getResources().getColor(R.color.green));
            timeRepeatSunCheckBox.setEnabled(true);
            timeRepeatMonCheckBox.setEnabled(true);
            timeRepeatTuesCheckBox.setEnabled(true);
            timeRepeatWedCheckBox.setEnabled(true);
            timeRepeatThurCheckBox.setEnabled(true);
            timeRepeatFriCheckBox.setEnabled(true);
            timeRepeatSatCheckBox.setEnabled(true);
        } else {
            infoPushSwitchImageView.setImageLevel(0);
            timeStartLinearLayout.setOnClickListener(null);
            timeEndLinearLayout.setOnClickListener(null);
            timeIntervalLinearLayout.setBackgroundResource(R.color.transparent);
            timeRepeatLinearLayout.setBackgroundResource(R.color.transparent);
            timeIntervalTextView.setBackgroundResource(R.color.transparent);
            timeRepeatTextView.setBackgroundResource(R.color.transparent);
            timeStartTextView.setTextColor(getResources().getColor(R.color.gray));
            timeEndTextView.setTextColor(getResources().getColor(R.color.gray));
            timeRepeatSunCheckBox.setEnabled(false);
            timeRepeatMonCheckBox.setEnabled(false);
            timeRepeatTuesCheckBox.setEnabled(false);
            timeRepeatWedCheckBox.setEnabled(false);
            timeRepeatThurCheckBox.setEnabled(false);
            timeRepeatFriCheckBox.setEnabled(false);
            timeRepeatSatCheckBox.setEnabled(false);
        }
    }
    private void setTimeInterval(MessagePush messagePush) {
        String startTime = messagePush.getStartTime();
        if (TextUtils.isEmpty(startTime)) {
            startTime = "00:00:00";
        }
        String endTime = messagePush.getEndTime();
        if (TextUtils.isEmpty(endTime)) {
            endTime = "00:00:00";
        }
        String[] startTimeSplit = startTime.split(":");
        String[] endTimeSplit = endTime.split(":");
        if (startTimeSplit.length > 1 && endTimeSplit.length > 1) {
            startHour = Integer.valueOf(startTimeSplit[0]);
            startMinute =  Integer.valueOf(startTimeSplit[1]);
            endHour = Integer.valueOf(endTimeSplit[0]);
            endMinute =  Integer.valueOf(endTimeSplit[1]);
            setTimeInterval();
        }
    }

    private void setTimeInterval() {
        String timeInterval;
        if (startHour > endHour || startHour == endHour && startMinute > endMinute) {
            timeInterval = TimeUtil.getTime(mAppContext, startHour, startMinute)+mContext.getString(R.string.time_interval_to)+mContext.getString(R.string.time_interval_tomorrow)+TimeUtil.getTime(mAppContext, endHour, endMinute);
        } else if(endHour == startHour && endMinute == startMinute) {
            timeInterval = mContext.getString(R.string.time_interval_all_day);
        } else {
            timeInterval = TimeUtil.getTime(mAppContext, startHour, startMinute)+mContext.getString(R.string.time_interval_to) +TimeUtil.getTime(mAppContext, endHour, endMinute);
        }
        timeStartTextView.setText(TimeUtil.getTime(mAppContext, startHour, startMinute));
        timeEndTextView.setText(TimeUtil.getTime(mAppContext, endHour, endMinute));
        timeIntervalTextView.setText(getString(R.string.time_interval) + timeInterval);
        messagePush.setStartTime(TimeUtil.getTime24(mAppContext, startHour, startMinute) + ":00");
        messagePush.setEndTime(TimeUtil.getTime24(mAppContext, endHour, endMinute) + ":00");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.infoPushSwitchImageView:{
                if (messagePush.getIsPush() == MessagePushStatus.ON) {
                    messagePush.setIsPush(MessagePushStatus.OFF);
                } else {
                    messagePush.setIsPush(MessagePushStatus.ON);
                }
                sensorTimerPush.startSetDeviceTimerPush(device.getDeviceId(), messagePush.getIsPush(), messagePush.getStartTime(), messagePush.getEndTime(),messagePush.getWeek());
                break;
            }
            case R.id.timeStartLinearLayout: {
                isChooseStart = true;
                deviceSetTimePopup.show(getString(R.string.time_start_choose), startHour, startMinute);
                break;
            }
            case R.id.timeEndLinearLayout: {
                isChooseStart = false;
                deviceSetTimePopup.show(getString(R.string.time_end_choose), endHour, endMinute);
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!timeRepeatSunCheckBox.isChecked() && !timeRepeatMonCheckBox.isChecked() && !timeRepeatTuesCheckBox.isChecked() && !timeRepeatWedCheckBox.isChecked()
                && !timeRepeatThurCheckBox.isChecked() && !timeRepeatFriCheckBox.isChecked() && !timeRepeatSatCheckBox.isChecked()) {
            buttonView.setChecked(true);
        } else {
            int week = WeekUtil.getSelectedWeekInt(getSelectedWeek());
            messagePush.setWeek(week);
            timeRepeatTextView.setText(getString(R.string.time_repeat) + WeekUtil.getWeeks(mAppContext, week));
            if (isChecked) {
                buttonView.setTextColor(getResources().getColor(R.color.white));
            } else {
                buttonView.setTextColor(getResources().getColor(R.color.gray));
            }
        }

    }

    private List<Integer> getSelectedWeek() {
        if (weeks != null && weeks.size() > 0) {
            weeks.clear();
        }
        for (int i = 0; i < weekCheckBoxes.size(); i++) {
            if (weekCheckBoxes.get(i).isChecked()) {
                weeks.add(i + 1);
            }
        }
        return weeks;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
