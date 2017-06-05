package com.orvibo.homemate.device.smartlock;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.SelectRepeatView;


/**
 * 门锁定时反锁提醒
 * Created by snown on 2016/4/25.
 */
public class LockTimingActivity extends BaseActivity implements View.OnClickListener,
        SelectRepeatView.OnSelectWeekListener {
    private static final String TAG = LockTimingActivity.class
            .getSimpleName();
    private TimePicker timePicker;
    private SelectRepeatView selectRepeatView;

    private String deviceId;
    private int selectedWeekInt, oldWeek;
    private int hour;
    private int minute;

    private Device device;
    private MessagePushDao messagePushDao;
    private ImageView imageLockCheck, imageLockedCheck;
    private LinearLayout itemLock;
    private MessagePush lockMessage, lockedMessage;
    private SensorTimerPush sensorTimerPush;
    private boolean isTimeSave = false;//是否为修改定时时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_timing);
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        messagePushDao = new MessagePushDao();
        deviceId = device.getDeviceId();
        init();
    }

    private void init() {
        initView();
        initLockData();
        initLockedData();
        initSensorTimerPush();
    }


    private void initView() {
        this.imageLockedCheck = (ImageView) findViewById(R.id.imageLockedCheck);
        this.selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        this.timePicker = (TimePicker) findViewById(R.id.timePicker);
        this.itemLock = (LinearLayout) findViewById(R.id.itemLock);
        itemLock.setOnClickListener(this);
        this.imageLockCheck = (ImageView) findViewById(R.id.imageLockCheck);
        imageLockCheck.setOnClickListener(this);
        imageLockedCheck.setOnClickListener(this);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        boolean is24HourFormat = DateFormat.is24HourFormat(mAppContext);
        timePicker.setIs24HourView(is24HourFormat);
        selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        selectRepeatView.setOnSelectWeekListener(this);

    }

    /**
     * 初始化定时反锁数据
     */
    private void initLockData() {
        lockMessage = messagePushDao.getMessagePushByType(deviceId, MessagePushType.DEVICE_LOCK_REMIND);
        if (lockMessage == null) {
            lockMessage = new MessagePush();
            lockMessage.setIsPush(MessagePushStatus.ON);
            lockMessage.setWeek(255);
            lockMessage.setStartTime("21:00:00");
        }
        selectedWeekInt = lockMessage.getWeek();
        oldWeek = selectedWeekInt;
        selectRepeatView.refresh(selectedWeekInt, true);
        String[] times = lockMessage.getStartTime().split(":");
        hour = Integer.valueOf(times[0]);
        minute = Integer.valueOf(times[1]);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);

        if (lockMessage.getIsPush() == MessagePushStatus.ON) {
            imageLockCheck.setImageLevel(1);
            selectRepeatView.setStatus(true);
            timePicker.setEnabled(true);
            imageLockedCheck.setEnabled(true);
        } else {
            imageLockCheck.setImageLevel(0);
            selectRepeatView.setStatus(false);
            timePicker.setEnabled(false);
            imageLockedCheck.setEnabled(false);
        }
    }

    /**
     * 初始化已反锁不提醒数据
     */
    private void initLockedData() {
        lockedMessage = messagePushDao.getMessagePushByType(deviceId, MessagePushType.DEVICE_LOCKED_REMIND);
        if (lockedMessage == null) {
            lockedMessage = new MessagePush();
            lockedMessage.setIsPush(MessagePushStatus.ON);
        }
        if (lockedMessage.getIsPush() == MessagePushStatus.OFF) {
            imageLockedCheck.setImageLevel(1);
        } else {
            imageLockedCheck.setImageLevel(0);
        }
    }


    private void initSensorTimerPush() {
        sensorTimerPush = new SensorTimerPush(mAppContext) {
            @Override
            public void onSensorTimerPushResult(int result, int type) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    //设置成功后重新加载界面
                    if (isTimeSave)
                        finish();
                    else {
                        if (type == MessagePushType.DEVICE_LOCK_REMIND) {
                            initLockData();
                        } else {
                            initLockedData();
                        }
                    }
                }
            }

            @Override
            public void onAllSensorSetTimerPushResult(int result) {

            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageLockedCheck:
                setPush(MessagePushType.DEVICE_LOCKED_REMIND);
                break;
            case R.id.imageLockCheck:
                setPush(MessagePushType.DEVICE_LOCK_REMIND);
                break;
        }
    }

    /**
     * 设置消息推送开关
     *
     * @param messageType
     */
    private void setPush(int messageType) {
        showDialog();
        isTimeSave = false;
        String startTime = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute() + ":00";
        if (messageType == MessagePushType.DEVICE_LOCKED_REMIND) {
            if (lockedMessage.getIsPush() == MessagePushStatus.ON)
                sensorTimerPush.startSetDeviceTimerPush(deviceId, MessagePushStatus.OFF, startTime, messageType);
            else
                sensorTimerPush.startSetDeviceTimerPush(deviceId, MessagePushStatus.ON, startTime, messageType);
        } else {
            if (lockMessage.getIsPush() == MessagePushStatus.ON)
                sensorTimerPush.startSetDeviceTimerPush(deviceId, MessagePushStatus.OFF, startTime, messageType);
            else
                sensorTimerPush.startSetDeviceTimerPush(deviceId, MessagePushStatus.ON, startTime, messageType);
        }
    }


    @Override
    public void onBackPressed() {
        cancel();
    }


    public void leftTitleClick(View view) {
        cancel();
    }

    private void cancel() {
        if (isTimeChange()) {
            showDialog();
            String startTime = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute() + ":00";
            sensorTimerPush.startTimerPush(MessagePushType.DEVICE_LOCK_REMIND, deviceId, 0, MessagePushStatus.ON, startTime, "00:00:00", selectedWeekInt);
            isTimeSave = true;
        } else {
            finish();
        }
    }

    /**
     * 时间是否有变化
     *
     * @return
     */
    private boolean isTimeChange() {
        if (lockMessage != null && lockMessage.getIsPush() == MessagePushStatus.ON) {
            if (timePicker.getCurrentHour() != hour || timePicker.getCurrentMinute() != minute || selectedWeekInt != oldWeek)
                return true;
        }
        return false;
    }


    @Override
    public void onSelectWeek(int selectWeek) {
        selectedWeekInt = selectWeek;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
