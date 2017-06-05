package com.orvibo.homemate.device.timing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.load.MultiLoad;
import com.orvibo.homemate.core.load.OnMultiLoadListener;
import com.orvibo.homemate.core.load.loadserver.LoadServer;
import com.orvibo.homemate.core.load.loadserver.OnLoadServerListener;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.ACPanelModelAndWindConstant;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.AddTimer;
import com.orvibo.homemate.model.DeleteTimer;
import com.orvibo.homemate.model.ModifyTimer;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.ActionView;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.NavigationTextBar;
import com.orvibo.homemate.view.custom.SelectRepeatView;
import com.orvibo.homemate.view.custom.SwitchView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 添加、修改定时。添加定时-如果服务器返回定时已存在则进行读表
 * Created by Allen on 2015/4/1.
 * Modified by smagret on 2015/04/13
 */
public class DeviceTimingCreateActivity extends BaseActivity implements View.OnClickListener,
        OnLoadServerListener,
        SelectRepeatView.OnSelectWeekListener, SwitchView.OnSwitchCheckedListener
//        ,SwitchView.OnSwitchCheckedListener
        , OnMultiLoadListener {
    private static final String TAG = DeviceTimingCreateActivity.class
            .getSimpleName();
    private final int WHAT_LOAD_RESULT = 1;
    private TimePicker timePicker;
    private LinearLayout llAction;
    private TextView alloneTimingTips;
    private TextView tvDelete;
    //    private TextView textViewAction;
    private SavaPopup savaPopup;
    private NavigationGreenBar nb_title;
    //定时动作
    private ActionView av_bindaction;
    //    private View colorView;
//    private LinearLayout color_ll;
    private SelectRepeatView selectRepeatView;

    private String uid;
    private String deviceId;
    private String timingActionName;
    private String order;
    private String name = "";
    private String timingId;
    private int selectedWeekInt;
    private int value1;
    private int value2;
    private int value3;
    private int value4;
    private int hour;
    private int minute;
    private int second;
    private int FLAG = -1;//
    private int MODIFY_TIMER = 0;//
    private int ADD_TIMER = 1;//
    private int deviceType;

    /**
     * 防止短时间多次操作
     */
    private boolean isRequesting = false;

    /**
     * true 接收到定时已存在错误码，app读表，这时app需要处理读表结果。false 不处理读表结果
     */
    private volatile boolean isLoaded;

    private Action action;
    private Device device;
    private MultiLoad mMultiLoad;
    private AddTimerControl addTimerControl;
    private ModifyTimerControl modifyTimerControl;
    private DeleteTimerControl deleteTimerControl;
    private Timing oldTiming;
    private TimingDao mTimingDao;
    private LinearLayout llActionSwitch;
    private SwitchView switchView;
    private Acpanel mAcPanel;
    private DeviceStatusDao mDeviceStatusDao;
    private int freq;
    private int pluseNum;
    private String pluseData;

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
        //获得acpanel对象
        //mAcPanel = (Acpanel) intent.getSerializableExtra(IntentKey.ACPANEL);
        initAcPanel();
        initSwitchStatus();

        mMultiLoad = MultiLoad.getInstance(mAppContext);
    }

    private void initAcPanel() {
        if (device.getDeviceType() == DeviceType.AC_PANEL ||
                (device.getDeviceType() == DeviceType.AC && device.getAppDeviceId() == AppDeviceId.AC_WIIF && !ProductManage.isAlloneSunDevice(device))) {
            mAcPanel = new Acpanel();
            //添加新的定时时,在数据库中查询最新的状态
            if (oldTiming == null) {
                DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(currentMainUid, device);
                if (deviceStatus != null) {
                    mAcPanel.setValue1(deviceStatus.getValue1());
                    mAcPanel.setValue2(deviceStatus.getValue2());
                    mAcPanel.setValue3(deviceStatus.getValue3());
                    mAcPanel.setValue4(deviceStatus.getValue4());
                }
            }
        }
    }

    private void initView() {
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        boolean is24HourFormat = DateFormat.is24HourFormat(mAppContext);
        timePicker.setIs24HourView(is24HourFormat);
        llAction = (LinearLayout) findViewById(R.id.llAction);
        alloneTimingTips = (TextView) findViewById(R.id.alloneTimingTips);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        av_bindaction = (ActionView) findViewById(R.id.av_bindaction);
        av_bindaction.setActionTextColor(getResources().getColor(R.color.gray), true);
        nb_title = (NavigationGreenBar) findViewById(R.id.nbTitle);
        // colorView = findViewById(R.id.colorView);
        // color_ll = (LinearLayout) findViewById(R.id.color_ll);
        savaPopup = new SavaPopup();
        selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        selectRepeatView.setOnSelectWeekListener(this);
        llActionSwitch = (LinearLayout) findViewById(R.id.llActionSwitch);
        //
        switchView = (SwitchView) findViewById(R.id.switchView);
        switchView.setSwitchParm(DeviceTimingCreateActivity.this);
        switchView.setOnSwitchCheckedListener(this);

    }

    private void initData() {
        mDeviceStatusDao = new DeviceStatusDao();
        mTimingDao = new TimingDao();
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        action = (Action) intent.getSerializableExtra(IntentKey.ACTION);
        if (action == null) {
            action = BindTool.getDefaultAction(device, mDeviceStatusDao.selDeviceStatus(device.getDeviceId()), BindActionType.TIMING);
        }

        deviceId = device.getDeviceId();
        deviceType = device.getDeviceType();
        uid = device.getUid();
        initActionView();
        if (deviceType == DeviceType.AC_PANEL
                || deviceType == DeviceType.COCO
                || deviceType == DeviceType.S20
                || (deviceType == DeviceType.AC && device.getAppDeviceId() == AppDeviceId.AC_WIIF && !ProductManage.isAlloneSunDevice(device))) {
            llAction.setVisibility(View.GONE);
            llActionSwitch.setVisibility(View.VISIBLE);
            switchView.setOpenCheckBoxText(getString(R.string.action_open));
            switchView.setCloseCheckBoxText(getString(R.string.action_close));
        } else {
            llAction.setVisibility(View.VISIBLE);
            llActionSwitch.setVisibility(View.GONE);
        }
        oldTiming = (Timing) intent.getSerializableExtra("timing");
        if (oldTiming != null) {
            FLAG = MODIFY_TIMER;
            value1 = oldTiming.getValue1();
            value2 = oldTiming.getValue2();
            value3 = oldTiming.getValue3();
            value4 = oldTiming.getValue4();
            order = oldTiming.getCommand();
            name = action.getName();
            freq = action.getFreq();
            pluseNum = action.getPluseNum();
            pluseData = action.getPluseData();
            timingId = oldTiming.getTimingId();
            tvDelete.setVisibility(View.VISIBLE);

            timePicker.setCurrentHour(oldTiming.getHour());
            timePicker.setCurrentMinute(oldTiming.getMinute());
            timingId = oldTiming.getTimingId();
            selectedWeekInt = oldTiming.getWeek();
            timingActionName = DeviceTool.getActionName(mAppContext, oldTiming);
            if (!TextUtils.isEmpty(name)) {
                timingActionName = name;
            }
            Action action = Timing.getAction(oldTiming);
            action.setName(timingActionName);
            av_bindaction.setAction(action);
            nb_title.setText(getResources().getString(R.string.device_timing_modify));
        } else {
            FLAG = ADD_TIMER;
            tvDelete.setVisibility(View.GONE);
            nb_title.setText(getResources().getString(R.string.device_timing_add));
            timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            //av_bindaction.setEmptyView(Constant.INVALID_NUM);
            av_bindaction.setAction(action);
        }

        selectRepeatView.refresh(selectedWeekInt, true);
        addTimerControl = new AddTimerControl(mAppContext);
        modifyTimerControl = new ModifyTimerControl(mAppContext);
        deleteTimerControl = new DeleteTimerControl(mAppContext);


    }

    /**
     * 点击item时，代表定时已经存在
     */
    private void initSwitchStatus() {
        int status = 0;
        if (device.getDeviceType() == DeviceType.AC_PANEL || (device.getDeviceType() == DeviceType.AC && device.getAppDeviceId() == AppDeviceId.AC_WIIF && !ProductManage.isAlloneSunDevice(device))) {
            if (mAcPanel != null && oldTiming != null) {
                mAcPanel.setValue1(oldTiming.getValue1());
                int onoff = mAcPanel.getOnoff();
                if (onoff == ACPanelModelAndWindConstant.AC_CLOSE) {
                    status = 1;
                } else {
                    status = 0;
                }
            }
            switchView.refresh(status, true);
        } else if (deviceType == DeviceType.COCO || deviceType == DeviceType.S20) {
            if (action != null) {
                value1 = action.getValue1();
                order = action.getCommand();
            } else {
                DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, deviceId);
                //默认值不是设备当前值
                if (deviceStatus != null) {
                    //int value1 = deviceStatus.getValue1();
                    Action action = BindTool.getDefaultAction(device, deviceStatus, BindActionType.TIMING);
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
    }

//    private void initLoad() {
//        mLoad = new Load(mAppContext);
//        mLoad.setOnLoadListener(this);
//    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_LOAD_RESULT) {
                int result = msg.arg1;
                if (result == ErrorCode.SUCCESS) {
                    Timing timing = mTimingDao.selTiming(deviceId, hour, minute, second, selectedWeekInt, TimingConstant.TIMEING_EFFECT);
                    LogUtil.d(TAG, "handleMessage()-deviceId:" + deviceId + ",hour:" + hour + ",minute:" + minute + ",second:" + second + ",week:" + selectedWeekInt + "\ntiming:" + timing);
                    if (timing == null) {
                        addTimer();
//                        if (!timingExist()) {
//                            isRequesting = true;
//                            addTimerControl.startAddTimer(uid, userName, deviceId, order, value1, value2, value3, value4,
//                                    hour, minute, second, selectedWeekInt, name, freq, pluseNum, pluseData);
//                        } else {
//                            dismissDialog();
//                            ToastUtil.showToast(
//                                    ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
//                                    ToastType.NULL, ToastType.SHORT);
//                        }
                    } else {
                        dismissDialog();
                        if (isEqualAction(timing)) {
                            ToastUtil.showToast(
                                    getResources().getString(R.string.device_timing_add_success),
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

//    @Override
//    protected void onRefresh(ViewEvent event) {
//        super.onRefresh(event);
//        if (device != null && !TextUtils.isEmpty(device.getUid()) && device.getUid().equals(event.uid)) {
//            Message msg = mHandler.obtainMessage(WHAT_LOAD_RESULT);
//            msg.arg1 = ErrorCode.SUCCESS;
//            mHandler.sendMessage(msg);
//        }
//    }

    @Override
    public void onLoadServerFinish(List<String> tableNames, int result) {
        if (!isLoaded) {
            //本页面没有请求读表，不处理读表结果
            LogUtil.e(TAG, "onLoadServerFinish()-tableNames:" + tableNames + ",result:" + result);
            return;
        }
        if (device != null && !TextUtils.isEmpty(device.getUid()) && device.getUid().equals(uid)) {
            mMultiLoad.removeOnMultiLoadListener(this);
            //if (result == ErrorCode.SUCCESS) {
            //添加定时->返回定时已存在->读表->读表成功重新添加定时
            Message msg = mHandler.obtainMessage(WHAT_LOAD_RESULT);
            msg.arg1 = result;
            mHandler.sendMessage(msg);
            // }
        }
    }

    @Override
    public void onMultiLoadFinish(String uid, int result, boolean noneUpdate) {
        if (!isLoaded) {
            //本页面没有请求读表，不处理读表结果
            LogUtil.e(TAG, "onMultiLoadFinish()-uid:" + uid + ",result:" + result + ",noneUpdate:" + noneUpdate);
            return;
        }
        if (device != null && !TextUtils.isEmpty(device.getUid()) && device.getUid().equals(uid)) {
            mMultiLoad.removeOnMultiLoadListener(this);
            //if (result == ErrorCode.SUCCESS) {
            //添加定时->返回定时已存在->读表->读表成功重新添加定时
            Message msg = mHandler.obtainMessage(WHAT_LOAD_RESULT);
            msg.arg1 = result;
            mHandler.sendMessage(msg);
            // }
        }
    }

    @Override
    public void onImportantTablesLoadFinish(String uid) {

    }

    @Override
    public void onTableLoadFinish(String uid, String tableName) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ActivityTool.GET_ACTION:
                if (resultCode == RESULT_OK && data != null) {
                    action = (Action) data.getSerializableExtra(IntentKey.ACTION);
                    initActionView();
                }
                break;
        }
    }

    private void initActionView() {
        if (action != null) {
            timingActionName = DeviceTool.getActionName(mContext, action);
             if (!TextUtils.isEmpty(action.getDeviceId()))
                deviceId = action.getDeviceId();
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
                timingActionName = name;
            }
            action.setName(timingActionName);
            av_bindaction.setAction(action);
            LogUtil.d(TAG, "initActionView() - order = " + order
                    + " value1 = " + value1 + " value2 = " + value2 + " value3 = " + value3 + " value4 = " + value4);
            if (deviceType == DeviceType.FAN && name.equals(getString(R.string.allone_electric_source))) {
                alloneTimingTips.setVisibility(View.VISIBLE);
            } else {
                alloneTimingTips.setVisibility(View.GONE );
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAction: {
                ActivityTool.toSelectActionActivity(this,
                        ActivityTool.GET_ACTION, BindActionType.TIMING, device, action);
                break;
            }
            case R.id.tvDelete: {
                deleteTimer();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private void getTime() {
        hour = timePicker.getCurrentHour();
        minute = timePicker.getCurrentMinute();
    }

    private void addTimer() {
        if (!isRequesting) {
//            if (!timingExist()) {
                isRequesting = true;
                // mMultiLoad.setOnMultiLoadListener(DeviceTimingCreateActivity.this);
                showDialog();
                addTimerControl.startAddTimer(uid, userName, deviceId, order, value1, value2, value3, value4,
                        hour, minute, second, selectedWeekInt, name, freq, pluseNum, pluseData);
//            } else {
//                dismissDialog();
//                ToastUtil.showToast(
//                        ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
//                        ToastType.NULL, ToastType.SHORT);
//            }
        }
    }

    private void modifyTimer() {
        if (!isRequesting) {
            getTime();
//            if (!timingExist()) {
                //    mMultiLoad.setOnMultiLoadListener(DeviceTimingCreateActivity.this);
                modifyTimerControl.startModifyTimer(uid, userName, deviceId, timingId, order, value1, value2,
                        value3, value4, hour, minute, second, selectedWeekInt, name, freq, pluseNum, pluseData);
                showDialog();
                isRequesting = true;
//            } else {
//                ToastUtil.showToast(
//                        ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
//                        ToastType.NULL, ToastType.SHORT);
//            }
        }
    }

    private void deleteTimer() {
        if (!isRequesting) {
            isRequesting = true;
            showDialog();
            //  mMultiLoad.setOnMultiLoadListener(DeviceTimingCreateActivity.this);
            deleteTimerControl.startDeleteTimer(uid, userName, timingId);
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

        if (FLAG == MODIFY_TIMER) {
            modifyTimer();
        } else if (FLAG == ADD_TIMER) {
            isLoaded = false;
            addTimer();
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
        if (!isEqualOldTimer()) {
            savaPopup.showPopup(DeviceTimingCreateActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            finish();
            return;
        }
    }

    private boolean isEqualOldTimer() {
        boolean isEqualOldTiming = false;
        getTime();
        if (oldTiming != null) {
            if ((order.equals(oldTiming.getCommand())
                    && (value1 == oldTiming.getValue1())
                    && (value2 == oldTiming.getValue2())
                    && (value3 == oldTiming.getValue3())
                    && (value4 == oldTiming.getValue4())
                    && (hour == oldTiming.getHour())
                    && (minute == oldTiming.getMinute())
                    && (second == oldTiming.getSecond())
                    && (selectedWeekInt == oldTiming.getWeek()))) {
                isEqualOldTiming = true;
            }
        }
        return isEqualOldTiming;
    }

    /**
     * @param timing
     * @return true 动作一样
     */
    private boolean isEqualAction(Timing timing) {
        boolean isEqualAction = false;
        if (timing != null) {
            if ((order != null && order.equals(timing.getCommand())
                    && (value1 == timing.getValue1())
                    && (value2 == timing.getValue2())
                    && (value3 == timing.getValue3())
                    && (value4 == timing.getValue4())
            )) {
                isEqualAction = true;
            }
        }
        return isEqualAction;
    }

//    private boolean timingExist() {
//        List<Timing> timings = new TimingDao().selTimingsByDevice(device.getUid(), device.getDeviceId());
//        for (Timing t : timings) {
//            int week = t.getWeek();
//            if (week == 0) {
//                week = getTodayWeek();
//            }
//            int selectedWeekInt = this.selectedWeekInt;
//            if (selectedWeekInt == 0) {
//                selectedWeekInt = getTodayWeek();
//            }
//            if (!t.getTimingId().equals(timingId) && t.getHour() == hour && t.getMinute() == minute
//                    && WeekUtil.hasEqualDayOfWeek(week, selectedWeekInt) && t.getDeviceId().equalsIgnoreCase(deviceId)) {
//                return true;
//            }
//        }
//        return false;
//    }

    private int getTodayWeek() {
        int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (week == 1) {
            week = 7;
        } else {
            week = week - 1;
        }
        List<Integer> weeks = new ArrayList<Integer>();
        weeks.add(week);
        week = WeekUtil.getSelectedWeekInt(weeks);
        return week;
    }

    private boolean isActionSet() {
        boolean isActionSet = false;

        isActionSet = !(order == null || order.equals(""));
        return isActionSet;
    }

    @Override
    public void onSelectWeek(int selectWeek) {
        selectedWeekInt = selectWeek;
    }


    @Override
    public void onSwitchOpened() {
        switch (device.getDeviceType()) {
            /**
             * wifi 空调
             */
            case DeviceType.AC:
                if (device.getAppDeviceId() == AppDeviceId.AC_WIIF && !ProductManage.isAlloneSunDevice(device)) {
                    order = DeviceOrder.AC_CTRL;
                    if (mAcPanel != null) {
                        mAcPanel.setOnoff(ACPanelModelAndWindConstant.AC_OPEN);
                        setValueFromAcPanel();
                    }
                }
                break;
            /**
             * 空调面板
             */
            case DeviceType.AC_PANEL:
                order = DeviceOrder.ON;
                if (mAcPanel != null) {
                    mAcPanel.setOnoff(ACPanelModelAndWindConstant.AC_OPEN);
                    setValueFromAcPanel();
                }
                break;
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
            /**
             * wifi 空调
             */
            case DeviceType.AC:
                if (device.getAppDeviceId() == AppDeviceId.AC_WIIF && !ProductManage.isAlloneSunDevice(device)) {
                    order = DeviceOrder.AC_CTRL;
                    if (mAcPanel != null) {
                        mAcPanel.setOnoff(ACPanelModelAndWindConstant.AC_CLOSE);
                        setValueFromAcPanel();
                    }
                }
                break;
            /**
             * 空调面板
             */
            case DeviceType.AC_PANEL:
                order = DeviceOrder.OFF;
                if (mAcPanel != null) {
                    mAcPanel.setOnoff(ACPanelModelAndWindConstant.AC_CLOSE);
                    setValueFromAcPanel();
                }
                break;
            case DeviceType.COCO:
            case DeviceType.S20:
                order = DeviceOrder.OFF;
                value1 = DeviceStatusConstant.OFF;
                break;
        }

    }

    private void setValueFromAcPanel() {
        if (mAcPanel != null) {
            if (oldTiming == null) {
                value1 = mAcPanel.getValue1();
                value2 = mAcPanel.getValue2();
                value3 = mAcPanel.getValue3();
                value4 = mAcPanel.getValue4();
            } else {
                //编辑旧的定时时，其他的空调设置不变，
                value1 = mAcPanel.getValue1();
            }
        }
    }

    private class SavaPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            if (FLAG == MODIFY_TIMER) {
                modifyTimer();
            } else if (FLAG == ADD_TIMER) {
                isLoaded = false;
                addTimer();
            }
        }

        public void cancel() {
            dismiss();
            finish();
        }
    }

    private class AddTimerControl extends AddTimer {

        public AddTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onAddTimerResult(String uid, int serial, int result, String timingId) {
            isRequesting = false;
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(
                        getResources().getString(R.string.device_timing_add_success),
                        ToastType.NULL, ToastType.SHORT);
                if (savaPopup != null) {
                    savaPopup.dismiss();
                }
                finish();
            } else {
                if (!isLoaded && result == ErrorCode.TIMING_EXIST) {
                    //定时已存在
                    MyLogger.kLog().d(deviceId + " has exist same time timing,load device last data.");
                    isLoaded = true;
//                    if (mLoad == null) {
//                        initLoad();
//                    }
//                    mLoad.load(uid);
                    if (ProductManage.getInstance().isVicenter300(uid)) {
                        mMultiLoad.setOnMultiLoadListener(DeviceTimingCreateActivity.this);
                        mMultiLoad.load(uid);
                    } else {
                        LoadServer.getInstance(mAppContext).setOnLoadServerListener(DeviceTimingCreateActivity.this);
                        LoadServer.getInstance(mAppContext).loadServer();
                    }
//                    LoadUtil.noticeLoadHubData(uid, Constant.INVALID_NUM);
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.TIMING_EXIST));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else if (result == ErrorCode.MODE_EXIST) {
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.MODE_EXIST));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else {
                    ToastUtil.showToast(ErrorMessage.getError(mAppContext, result), ToastType.ERROR, ToastType.SHORT);
                }
            }
        }
    }

    private class ModifyTimerControl extends ModifyTimer {

        public ModifyTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onModifyTimerResult(String uid, int serial, int result) {
            dismissDialog();
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(
                        getResources().getString(R.string.device_timing_modify_success),
                        ToastType.NULL, ToastType.SHORT);
                if (savaPopup != null) {
                    savaPopup.dismiss();
                }
                finish();
            } else if (!isLoaded && result == ErrorCode.TIMING_EXIST) {
                MyLogger.kLog().d(deviceId + " has exist same time timing,load device last data.");
                isLoaded = true;
                if (ProductManage.getInstance().isVicenter300(uid)) {
                    mMultiLoad.setOnMultiLoadListener(DeviceTimingCreateActivity.this);
                    mMultiLoad.load(uid);
                } else {
                    LoadServer.getInstance(mAppContext).setOnLoadServerListener(DeviceTimingCreateActivity.this);
                    LoadServer.getInstance(mAppContext).loadServer();
                }
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.TIMING_EXIST));
                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else if (result == ErrorCode.MODE_EXIST) {
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.MODE_EXIST));
                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class DeleteTimerControl extends DeleteTimer {


        public DeleteTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onDeleteTimerResult(String uid, int serial, int result) {
            dismissDialog();
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(
                        getResources().getString(R.string.device_timing_delete_success),
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
//        if (mLoad != null) {
//            mLoad.removeListener(this);
//            mLoad.cancelLoad();
//        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        LoadServer.getInstance(mAppContext).removeListener(this);
        super.onDestroy();
    }

}
