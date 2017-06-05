package com.orvibo.homemate.device.timing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.load.loadserver.LoadServer;
import com.orvibo.homemate.core.load.loadserver.OnLoadServerListener;
import com.orvibo.homemate.dao.CountdownDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.CountdownConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.AddCountdown;
import com.orvibo.homemate.model.DeleteCountdown;
import com.orvibo.homemate.model.ModifyCountdown;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.ActionView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.NavigationTextBar;
import com.orvibo.homemate.view.custom.SelectRepeatView;
import com.orvibo.homemate.view.custom.SwitchView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;


/**
 * 添加、修改定时。添加定时-如果服务器返回定时已存在则进行读表
 * Created by Allen on 2015/4/1.
 * Modified by smagret on 2015/04/13
 */
public class DeviceCountdownCreateActivity extends BaseActivity implements View.OnClickListener
        , OnLoadServerListener
        ,
        TimePicker.OnTimeChangedListener, SwitchView.OnSwitchCheckedListener {
    private static final String TAG = DeviceCountdownCreateActivity.class
            .getSimpleName();
    private final int WHAT_LOAD_RESULT = 1;
    private TimePicker timePicker;
    private ActionView av_bindaction;
    private LinearLayout llAction;
    private TextView tvDelete;
    //    private TextView textViewAction;
    private SavaPopup savaPopup;
    private NavigationGreenBar nb_title;
    //    private View colorView;
    private SelectRepeatView selectRepeatView;

    private String uid;
    private Device device;
    private String deviceId;

    private String countdownAction;
    private String order;
    private String name = "";
    private int value1;
    private int value2;
    private int value3;
    private int value4;

    private int time;
    private int startTime;
    private int lastMinute = 0;
    private int freq;
    private int pluseNum;
    private String pluseData;

    private Action action;

    private AddCountdownControl addCountdownControl;
    private ModifyCountdownControl modifyCountdownControl;
    private DeleteCountdownControl deleteCountdownControl;

    private Countdown oldCountdown;
    private String countdownId;
    private CountdownDao mCountdownDao;

    private int FLAG = -1;//
    private int MODIFY_COUNTDOWN = 0;//
    private int ADD_COUNTDOWN = 1;//
    private int deviceType;
    private boolean isRequesting = false;
    private volatile boolean isLoaded;
    private DeviceStatusDao mDeviceStatusDao;
    private LinearLayout mLlActionSwitch;
    private SwitchView switchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_create);
        init();
    }

    private void init() {
        initView();
        initData();
        initListener();
        initSwitchStatus();
    }

    private void initView() {
        av_bindaction = (ActionView) findViewById(R.id.av_bindaction);
        av_bindaction.setActionTextColor(getResources().getColor(R.color.gray), true);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        //boolean is24HourFormat = DateFormat.is24HourFormat(mAppContext);
        timePicker.setIs24HourView(true);
        llAction = (LinearLayout) findViewById(R.id.llAction);
        av_bindaction = (ActionView) findViewById(R.id.av_bindaction);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        // textViewAction = (TextView) findViewById(R.id.textViewAction);
        nb_title = (NavigationGreenBar) findViewById(R.id.nbTitle);
        // colorView = findViewById(R.id.colorView);
        savaPopup = new SavaPopup();
        selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        selectRepeatView.setVisibility(View.GONE);
        //
        mLlActionSwitch = (LinearLayout) findViewById(R.id.llActionSwitch);
        switchView = (SwitchView) findViewById(R.id.switchView);
        switchView.setSwitchParm(DeviceCountdownCreateActivity.this);
        switchView.setOnSwitchCheckedListener(this);

    }

    private void initData() {
        mDeviceStatusDao = new DeviceStatusDao();
        mCountdownDao = new CountdownDao();
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        action = (Action) intent.getSerializableExtra(IntentKey.ACTION);
        deviceId = device.getDeviceId();
        uid = device.getUid();
        deviceType = device.getDeviceType();
        if (deviceType == DeviceType.COCO
                || deviceType == DeviceType.S20) {
            llAction.setVisibility(View.GONE);
            mLlActionSwitch.setVisibility(View.VISIBLE);
            switchView.setOpenCheckBoxText(getString(R.string.action_open));
            switchView.setCloseCheckBoxText(getString(R.string.action_close));
        } else {
            llAction.setVisibility(View.VISIBLE);
            mLlActionSwitch.setVisibility(View.GONE);
        }
        oldCountdown = (Countdown) intent.getSerializableExtra("countdown");
        if (oldCountdown != null) {
            FLAG = MODIFY_COUNTDOWN;
            value1 = oldCountdown.getValue1();
            value2 = oldCountdown.getValue2();
            value3 = oldCountdown.getValue3();
            value4 = oldCountdown.getValue4();
            order = oldCountdown.getCommand();
            name = action.getName();
            freq = action.getFreq();
            pluseNum = action.getPluseNum();
            pluseData = action.getPluseData();
            countdownId = oldCountdown.getCountdownId();
            tvDelete.setVisibility(View.VISIBLE);

            timePicker.setCurrentHour(DateUtil.getCountdownHour(oldCountdown.getTime()));
            timePicker.setCurrentMinute(DateUtil.getCountdownMinute(oldCountdown.getTime()));
            lastMinute = DateUtil.getCountdownMinute(oldCountdown.getTime());
            countdownId = oldCountdown.getCountdownId();
            countdownAction = DeviceTool.getActionName(mAppContext, oldCountdown);
            if (!TextUtils.isEmpty(name)) {
                countdownAction = name;
            }
            Action action = Countdown.getAction(oldCountdown);
            action.setName(countdownAction);
            av_bindaction.setAction(action);
//            if (oldCountdown.getCommand().equals(DeviceOrder.COLOR_CONTROL)) {
//                int[] rgb = ColorUtil.hsl2DeviceRgb(oldCountdown.getValue4(), oldCountdown.getValue3(), oldCountdown.getValue2());
//                colorView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
//                colorView.setVisibility(View.VISIBLE);
//                textViewAction.setText(getResources().getString(R.string.action_color_text));
//            } else {
//                colorView.setVisibility(View.GONE);
//                textViewAction.setText(countdownAction);
//            }
            nb_title.setText(getResources().getString(R.string.device_countdown_modify));
        } else {
            FLAG = ADD_COUNTDOWN;
            tvDelete.setVisibility(View.GONE);
            nb_title.setText(getResources().getString(R.string.countdown_add));
            lastMinute = 1;
            timePicker.setCurrentHour(0);
            timePicker.setCurrentMinute(1);
//            av_bindaction.setEmptyView(Constant.INVALID_NUM);
            av_bindaction.setAction(action);
        }

        addCountdownControl = new AddCountdownControl(mAppContext);
        modifyCountdownControl = new ModifyCountdownControl(mAppContext);
        deleteCountdownControl = new DeleteCountdownControl(mAppContext);
    }

    /**
     * 点击item时，代表定时已经存在
     */
    private void initSwitchStatus() {
        int status = 0;
        if (deviceType == DeviceType.COCO || deviceType == DeviceType.S20) {
            if (action != null) {
                value1 = action.getValue1();
                order = action.getCommand();
            } else {
                DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, deviceId);
                //默认值不是设备当前值
                if (deviceStatus != null) {
                    Action action = BindTool.getDefaultAction(device, deviceStatus, BindActionType.COUNTDOWN);
                    if (action != null) {
                        order = action.getCommand();
                        value1 = action.getValue1();
                    } else {
                        order = DeviceOrder.ON;
                        value1 = DeviceStatusConstant.ON;
                    }
                } else {
                    order = DeviceOrder.ON;
                    value1 = DeviceStatusConstant.ON;
                }
            }
            status = value1;
            switchView.refresh(status, true);
        }
    }

    private void initListener() {
        llAction.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        timePicker.setOnTimeChangedListener(this);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_LOAD_RESULT) {
                int result = msg.arg1;
                if (result == ErrorCode.SUCCESS) {
                    Countdown countdown = mCountdownDao.selCountdown(deviceId, time, startTime, CountdownConstant.COUNTDOWN_EFFECT);
                    LogUtil.d(TAG, "handleMessage()-deviceId:" + deviceId + ",time:" + time + ",startTime:" + startTime + "\ncountdown:" + countdown);
                    if (countdown == null) {
                        addCountdown();
                        if (!countdownExist()) {
                            isRequesting = true;
                            addCountdownControl.startAddCountdown(uid, userName, deviceId, order, value1, value2, value3, value4,
                                    time, startTime, name, freq, pluseNum, pluseData);
                        } else {
                            dismissDialog();
                            ToastUtil.showToast(
                                    ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
                                    ToastType.NULL, ToastType.SHORT);
                        }
                    } else {
                        dismissDialog();
                        if (isEqualAction(countdown)) {
                            ToastUtil.showToast(
                                    getResources().getString(R.string.device_countdown_add_success),
                                    ToastType.NULL, ToastType.SHORT);
                            if (savaPopup != null) {
                                savaPopup.dismiss();
                            }
                            finish();
                        } else {
                            ToastUtil.toastError(ErrorCode.TIMING_EXIST);
                        }
                    }
                } else if (!ToastUtil.toastCommonError(result)) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.FAIL);
                }
            }
        }
    };

    @Override
    public void onLoadServerFinish(List<String> tableNames, int result) {
        if (result == ErrorCode.SUCCESS && device != null && !TextUtils.isEmpty(device.getUid()) && device.getUid().equals(uid)) {
            //添加倒计时->返回倒计时已存在->读表->读表成功重新添加倒计时
            Message msg = mHandler.obtainMessage(WHAT_LOAD_RESULT);
            msg.arg1 = ErrorCode.SUCCESS;
            mHandler.sendMessage(msg);
        }
        LoadServer.getInstance(mAppContext).removeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ActivityTool.GET_ACTION:
                if (resultCode == RESULT_OK && data != null) {
                    action = (Action) data.getSerializableExtra(IntentKey.ACTION);
                    countdownAction = action.getKeyName();
                    order = action.getCommand();
                    value1 = action.getValue1();
                    value2 = action.getValue2();
                    value3 = action.getValue3();
                    value4 = action.getValue4();
                    name = action.getName();
                    freq = action.getFreq();
                    pluseNum = action.getPluseNum();
                    pluseData = action.getPluseData();
                    if (!TextUtils.isEmpty(name)) {
                        countdownAction = name;
                    }
                    action.setName(countdownAction);
                    av_bindaction.setAction(action);
//                    if (action.getCommand().equals(DeviceOrder.COLOR_CONTROL)) {
//                        int[] rgb = ColorUtil.hsl2DeviceRgb(value4, value3, value2);
//                        colorView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
//                        colorView.setVisibility(View.VISIBLE);
//                        textViewAction.setText(getResources().getString(R.string.action_color_text));
//                    } else if (action.getCommand().equals(DeviceOrder.OPEN) && value1 == 50) {
//                        textViewAction.setText(getResources().getString(R.string.scene_action_on));
//                        value1 = 100;
//                    } else {
//                        colorView.setVisibility(View.GONE);
//                        textViewAction.setText(countdownAction);
//                    }

//                    textViewAction.setText(countdownAction);
                    LogUtil.d(TAG, "onActivityResult() - order = " + order
                            + " value1 = " + value1 + " value2 = " + value2 + " value3 = " + value3 + " value4 = " + value4);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAction: {
                ActivityTool.toSelectActionActivity(this,
                        ActivityTool.GET_ACTION, BindActionType.COUNTDOWN, device, action);
                break;
            }
            case R.id.tvDelete: {
                deleteCountdown();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private void getTime() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        startTime = DateUtil.getUTCTime();
        time = hour * 60 + minute;
    }


    private void addCountdown() {
        if (!isRequesting) {
            if (!countdownExist()) {
                isRequesting = true;
                showDialog();
                addCountdownControl.startAddCountdown(uid, userName, deviceId, order, value1, value2, value3, value4,
                        time, startTime, name, freq, pluseNum, pluseData);
            } else {
                dismissDialog();
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }

    private void modifyCountdown() {
        if (!isRequesting) {
            getTime();
            if (!countdownExist()) {
                modifyCountdownControl.startModifyCountdown(uid, userName, countdownId, order, value1, value2,
                        value3, value4, time, startTime, name, freq, pluseNum, pluseData);
                showDialog();
                isRequesting = true;
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, ErrorCode.COUNTDOWN_EXIST),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }

    private void deleteCountdown() {
        if (!isRequesting) {
            isRequesting = true;
            showDialog();
            deleteCountdownControl.startDeleteCountdown(uid, userName, countdownId);
        }
    }

    public void rightTitleClick(View view) {
        getTime();
        if (!isActionSet()) {
            ToastUtil.showToast(
                    R.string.device_timing_action_not_set_error,
                    ToastType.NULL, ToastType.SHORT);
            return;
        }

        if (FLAG == MODIFY_COUNTDOWN) {
            modifyCountdown();
        } else if (FLAG == ADD_COUNTDOWN) {
            isLoaded = false;
            addCountdown();
        }
    }

    public void leftTitleClick(View view) {
        cancel();
    }

    private void cancel() {
        if (!isActionSet()) {
            finish();
            return;
        }
        if (!isEqualOldCountdown()) {
            savaPopup.showPopup(DeviceCountdownCreateActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            finish();
            return;
        }
    }

    private boolean isEqualOldCountdown() {
        boolean isEqualOldCountdown = false;
        getTime();
        if (oldCountdown != null) {
            if ((order.equals(oldCountdown.getCommand())
                    && (value1 == oldCountdown.getValue1())
                    && (value2 == oldCountdown.getValue2())
                    && (value3 == oldCountdown.getValue3())
                    && (value4 == oldCountdown.getValue4())
                    && (time == oldCountdown.getTime()))) {
                isEqualOldCountdown = true;
            }
        }
        return isEqualOldCountdown;
    }

    /**
     * @param countdown
     * @return true 动作一样
     */
    private boolean isEqualAction(Countdown countdown) {
        boolean isEqualAction = false;
        if (countdown != null) {
            if ((order != null && order.equals(countdown.getCommand())
                    && (value1 == countdown.getValue1())
                    && (value2 == countdown.getValue2())
                    && (value3 == countdown.getValue3())
                    && (value4 == countdown.getValue4())
            )) {
                isEqualAction = true;
            }
        }
        return isEqualAction;
    }

    private boolean countdownExist() {
        List<Countdown> countdowns = new CountdownDao().selCountdownsByDevice(device.getUid(), device.getDeviceId());
        for (Countdown t : countdowns) {
            if (!t.getCountdownId().equals(countdownId)
                    && t.getTime() == time && t.getIsPause() == CountdownConstant.COUNTDOWN_EFFECT
                    && t.getStartTime() == DateUtil.getUTCTime()) {
                return true;
            }
        }
        return false;
    }

    private boolean isActionSet() {
        boolean isActionSet = false;

        if (order == null || order.equals("")) {
            isActionSet = false;
        } else {
            isActionSet = true;
        }
        return isActionSet;
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
        if (hourOfDay == 0 && minute == 0) {
            if (lastMinute == 1) {
                timePicker.setCurrentMinute(59);
                lastMinute = 59;
            } else if (lastMinute == 59) {
                timePicker.setCurrentMinute(1);
                lastMinute = 1;
            } else {
                timePicker.setCurrentMinute(1);
            }
        }
        if (minute != 0) {
            lastMinute = minute;
        }
    }

    @Override
    public void onSwitchOpened() {
        switch (device.getDeviceType()) {
            case DeviceType.COCO:
            case DeviceType.S20:
                order = DeviceOrder.ON;
                value1 = DeviceStatusConstant.ON;
                break;
        }

    }


    @Override
    public void onSwitchClosed() {
        switch (device.getDeviceType()) {
            case DeviceType.COCO:
            case DeviceType.S20:
                order = DeviceOrder.OFF;
                value1 = DeviceStatusConstant.OFF;
                break;
        }

    }


    private class SavaPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            if (FLAG == MODIFY_COUNTDOWN) {
                modifyCountdown();
            } else if (FLAG == ADD_COUNTDOWN) {
                isLoaded = false;
                addCountdown();
            }
        }

        public void cancel() {
            dismiss();
            finish();
        }
    }

    private class AddCountdownControl extends AddCountdown {

        public AddCountdownControl(Context context) {
            super(context);
        }

        @Override
        public void onAddCountdownResult(String uid, int serial, int result, String countdownId) {
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                dismissDialog();
                ToastUtil.showToast(
                        getResources().getString(R.string.device_countdown_add_success),
                        ToastType.NULL, ToastType.SHORT);
                if (savaPopup != null) {
                    savaPopup.dismiss();
                }
                finish();
            } else {
                if (!isLoaded && result == ErrorCode.TIMING_EXIST) {
                    MyLogger.kLog().d(deviceId + " has exist same time countdown,load device last data.");
                    isLoaded = true;
                    LoadServer.getInstance(mAppContext).setOnLoadServerListener(DeviceCountdownCreateActivity.this);
                    LoadServer.getInstance(mAppContext).loadServer();
                } else {
                    dismissDialog();
                    ToastUtil.showToast(
                            ErrorMessage.getError(mAppContext, result),
                            ToastType.ERROR, ToastType.SHORT);
                }
            }
        }
    }

    private class ModifyCountdownControl extends ModifyCountdown {

        public ModifyCountdownControl(Context context) {
            super(context);
        }

        @Override
        public void onModifyCountdownResult(String uid, int serial, int result) {
            dismissDialog();
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(
                        getResources().getString(R.string.device_countdown_modify_success),
                        ToastType.NULL, ToastType.SHORT);
                if (savaPopup != null) {
                    savaPopup.dismiss();
                }
                finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class DeleteCountdownControl extends DeleteCountdown {


        public DeleteCountdownControl(Context context) {
            super(context);
        }

        @Override
        public void onDeleteCountdownResult(String uid, int serial, int result) {
            dismissDialog();
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(
                        getResources().getString(R.string.device_countdown_delete_success),
                        ToastType.NULL, ToastType.SHORT);
                finish();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (savaPopup != null && savaPopup.isShowing()) {
            savaPopup.dismiss();
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        LoadServer.getInstance(mAppContext).removeListener(this);
        super.onDestroy();
    }

}
