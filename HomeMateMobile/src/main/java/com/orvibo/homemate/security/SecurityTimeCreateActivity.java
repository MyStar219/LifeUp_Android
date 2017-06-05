package com.orvibo.homemate.security;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.load.MultiLoad;
import com.orvibo.homemate.core.load.OnMultiLoadListener;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.AddTimer;
import com.orvibo.homemate.model.DeleteTimer;
import com.orvibo.homemate.model.ModifyTimer;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.NavigationTextBar;
import com.orvibo.homemate.view.custom.SelectRepeatView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wuliquan on 2016/7/21.
 */
public class SecurityTimeCreateActivity extends BaseActivity implements OnMultiLoadListener, SelectRepeatView.OnSelectWeekListener {
    private final int WHAT_LOAD_RESULT = 1;
    private LinearLayout llActionSwitch;
    private TimePicker timePicker;
    private LinearLayout llAction;
    private TextView tvDelete;
    private TextView textViewAction;
    private NavigationTextBar nb_title;
    private View colorView;
    private View dialog_view;
    private SavaPopup savaPopup;
    private LinearLayout color_ll;
    private SelectRepeatView selectRepeatView;

    private int FLAG = -1;//
    private int MODIFY_TIMER = 0;//
    private int ADD_TIMER = 1;//

    private boolean isRequesting = false;
    private volatile boolean isLoaded;

    private String mainUid;
    private TimingDao mTimingDao;
    private DeviceDao mDeviceDao;
    private SecurityDao mSecurityDao;

    private MultiLoad mMultiLoad;
    private AddTimerControl addTimerControl;
    private ModifyTimerControl modifyTimerControl;
    private DeleteTimerControl deleteTimerControl;

    private Timing timing;
    private Timing oldTiming;
    private int selectedWeekInt;

    private TextView out_tv,inhome_tv,disarm_tv,dismiss_tv;
    private View contentView;
    private PopupWindow selectActionAlert;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_LOAD_RESULT) {
                int result = msg.arg1;
                oldTiming = timing;
                if (result == ErrorCode.SUCCESS) {
                    Timing timing = mTimingDao.selTiming(oldTiming.getDeviceId(), oldTiming.getHour(), oldTiming.getMinute(), oldTiming.getSecond(),
                            selectedWeekInt, TimingConstant.TIMEING_EFFECT);
                    if (timing == null) {
                        addTimer();
                        if (!timingExist(timing)) {
                            isRequesting = true;
                            addTimerControl.startAddTiming(timing.getUid(), userName,timing);
                        } else {
                            dismissDialog();
                            ToastUtil.showToast(
                                    ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
                                    ToastType.NULL, ToastType.SHORT);
                        }
                    } else {
                        dismissDialog();
                        if (oldTiming.getCommand().equals(timing.getCommand())) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_create);
        oldTiming = (Timing) getIntent().getSerializableExtra("timing");
        mainUid = UserCache.getCurrentMainUid(SecurityTimeCreateActivity.this);
        initView();


        mTimingDao = new TimingDao();
        mDeviceDao = new DeviceDao();
        mSecurityDao = SecurityDao.getInstance();
        addTimerControl = new AddTimerControl(mAppContext);
        modifyTimerControl = new ModifyTimerControl(mAppContext);
        deleteTimerControl = new DeleteTimerControl(mAppContext);
    }

    private void initView() {
        dialog_view = this.getLayoutInflater().inflate(R.layout.dialog_select_security_type, null);
        contentView = findViewById(R.id.contentView);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        boolean is24HourFormat = DateFormat.is24HourFormat(mAppContext);
        timePicker.setIs24HourView(is24HourFormat);
        llAction = (LinearLayout) findViewById(R.id.llAction);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        textViewAction = (TextView) findViewById(R.id.textViewAction);
        nb_title = (NavigationTextBar) findViewById(R.id.nbTitle);
        nb_title.setTitleBackgroundColor(getResources().getColor(R.color.green));
        colorView = findViewById(R.id.colorView);
        color_ll = (LinearLayout) findViewById(R.id.color_ll);
        savaPopup = new SavaPopup();
        llActionSwitch = (LinearLayout)findViewById(R.id.llActionSwitch);
        llActionSwitch.setVisibility(View.GONE);
        selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        selectRepeatView.setOnSelectWeekListener(this);
        llActionSwitch = (LinearLayout) findViewById(R.id.llActionSwitch);
        llActionSwitch.setVisibility(View.GONE);

        out_tv = (TextView)dialog_view.findViewById(R.id.out_tv);
        inhome_tv = (TextView)dialog_view.findViewById(R.id.inhome_tv);
        disarm_tv = (TextView)dialog_view.findViewById(R.id.disarm_tv);
        dismiss_tv = (TextView)dialog_view.findViewById(R.id.dismiss_tv);

        llAction.setOnClickListener(this);

        if(oldTiming !=null){
            FLAG = MODIFY_TIMER;
            nb_title.setMiddleText(getString(R.string.device_timing_modify));
            timing = new Timing();
            timing.setUid(oldTiming.getUid());
            timing.setTimingId(oldTiming.getTimingId());
            timing.setCommand(oldTiming.getCommand());
            timing.setHour(oldTiming.getHour());
            timing.setMinute(oldTiming.getMinute());
            timing.setWeek(oldTiming.getWeek());

            timePicker.setCurrentHour(oldTiming.getHour());
            timePicker.setCurrentMinute(oldTiming.getMinute());
            selectedWeekInt = oldTiming.getWeek();
            textViewAction.setText(getOrderByCommand(timing.getCommand()));
            tvDelete.setVisibility(View.VISIBLE);
            tvDelete.setOnClickListener(this);
        }else{
            nb_title.setMiddleText(getString(R.string.device_timing_add));
            timing = new Timing();
            timing.setUid(mainUid);
            FLAG = ADD_TIMER;
            tvDelete.setVisibility(View.GONE);

        }
        selectRepeatView.refresh(selectedWeekInt, true);

    }

    private String getOrderByCommand(String command){
        if(command.equals("inside security")){
            return getResources().getString(R.string.inhome_security);
        }
        else if(command.equals("outside security")){
            return getResources().getString(R.string.outhome_security);
        }
        else if (command.equals("cancel security")){
            return getResources().getString(R.string.cancel_security);

        }
        return "";
    }
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.tvDelete:
               deleteTimer();
               break;
           case R.id.llAction:
               if(!isCanDefence()){
                   showTipDialog();
                   return;
               }
               if (selectActionAlert == null) {
                   if(dialog_view == null) {
                       dialog_view = this.getLayoutInflater().inflate(R.layout.dialog_select_security_type, null);
                   }
                   dialog_view.findViewById(R.id.out_tv).setOnClickListener(this);
                   dialog_view.findViewById(R.id.inhome_tv).setOnClickListener(this);
                   dialog_view.findViewById(R.id.disarm_tv).setOnClickListener(this);
                   dialog_view.findViewById(R.id.dismiss_tv).setOnClickListener(this);
                   selectActionAlert = new PopupWindow(dialog_view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                   PopupWindowUtil.initPopup(selectActionAlert, getResources().getDrawable(R.color.popup_bg), 2);
               }
               setSelectAction(timing.getCommand());
               selectActionAlert.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
               break;
           case R.id.dismiss_tv:
               selectActionAlert.dismiss();
               break;
           case R.id.inhome_tv:
               Security atHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.AT_HOME);
               timing.setDeviceId(atHomeSecurity.getSecurityId());
               timing.setCommand("inside security");
               textViewAction.setText(getOrderByCommand(timing.getCommand()));
               setSelectAction("inside security");
               break;
           case R.id.out_tv:
               Security leaveHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.LEAVE_HOME);
               timing.setDeviceId(leaveHomeSecurity.getSecurityId());
               timing.setCommand("outside security");
               textViewAction.setText(getOrderByCommand(timing.getCommand()));
               setSelectAction("outside security");
               break;
           case R.id.disarm_tv:
               timing.setDeviceId("");
               timing.setCommand("cancel security");
               textViewAction.setText(getOrderByCommand(timing.getCommand()));
               setSelectAction("cancel security");
               break;
       }
    }

    /**
     * 设置
     * @param action
     */
    private void setSelectAction(String action){
        if(action.equals("inside security")){
            inhome_tv.setTextColor(getResources().getColor(R.color.green));
            out_tv.setTextColor(getResources().getColor(R.color.black));
            disarm_tv.setTextColor(getResources().getColor(R.color.black));
        }
        else if(action.equals("outside security")){
            inhome_tv.setTextColor(getResources().getColor(R.color.black));
            out_tv.setTextColor(getResources().getColor(R.color.green));
            disarm_tv.setTextColor(getResources().getColor(R.color.black));
        }
        else if(action.equals("cancel security")){
            inhome_tv.setTextColor(getResources().getColor(R.color.black));
            out_tv.setTextColor(getResources().getColor(R.color.black));
            disarm_tv.setTextColor(getResources().getColor(R.color.green));
        }
        selectActionAlert.dismiss();
    }
    public void showTipDialog() {
        final Dialog dialog = new AlertDialog.Builder(SecurityTimeCreateActivity.this).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = -100;
        dialog.show();
        window.setContentView(R.layout.dialog_no_device_one_button);
        window.findViewById(R.id.gotItButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    @Override
    public void onMultiLoadFinish(String uid, int result, boolean noneUpdate) {
        if (result == ErrorCode.SUCCESS &&timing!=null&&timing.getUid().equals(uid)) {
            //添加定时->返回定时已存在->读表->读表成功重新添加定时
            Message msg = mHandler.obtainMessage(WHAT_LOAD_RESULT);
            msg.arg1 = ErrorCode.SUCCESS;
            mHandler.sendMessage(msg);
        }

        mMultiLoad.removeOnMultiLoadListener(this);
    }

    @Override
    public void onImportantTablesLoadFinish(String uid) {

    }

    @Override
    public void onTableLoadFinish(String uid, String tableName) {

    }

    @Override
    public void onSelectWeek(int selectWeek) {
        selectedWeekInt = selectWeek;
    }

    /**
     * 是否可以布防
     * @return
     */
    private boolean isCanDefence() {
        String currentMainUid = UserCache.getCurrentMainUid(SecurityTimeCreateActivity.this);
        boolean hasLock = mDeviceDao.hasDevice(currentMainUid, DeviceType.LOCK);
        boolean hasDoorContact = mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC)
                || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_WINDOW)
                || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_DRAWER)
                || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_OTHER);
        boolean hasInfraredSensor = mDeviceDao.hasDevice(currentMainUid, DeviceType.INFRARED_SENSOR);



        if(!hasLock&&!hasDoorContact&&!hasInfraredSensor){
            return false;
        }else{
            return true;
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

    /**
     * 添加定时
     */
    private void addTimer() {
        if (!isRequesting) {
            timing.setWeek(selectedWeekInt);
            if (!timingExist(timing)) {
                isRequesting = true;
                showDialog();

                addTimerControl.startAddTiming(timing.getUid(), userName,timing);
            } else {
                dismissDialog();
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }

    /**
     * 修改定时
     */
    private void modifyTimer() {
        if (!isRequesting) {
            getTime();
            timing.setWeek(selectedWeekInt);
            if (!timingExist(timing)) {
                modifyTimerControl.startModifyTimer(timing.getUid(), userName,timing.getDeviceId(), timing.getTimingId(),timing.getCommand(),
                        timing.getValue1(),timing.getValue2(),timing.getValue3(),timing.getValue4(),timing.getHour(),timing.getMinute(),
                        timing.getSecond(),timing.getWeek(),timing.getName(),timing.getFreq(),timing.getPluseNum(),timing.getPluseData());
                showDialog();
                isRequesting = true;
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mAppContext, ErrorCode.TIMING_EXIST),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }

    /**
     * 删除定时
     */
    private void deleteTimer() {
        if (!isRequesting) {
            isRequesting = true;
            showDialog();
            deleteTimerControl.startDeleteTimer(timing.getUid(), userName, timing.getTimingId());
        }
    }

    /**
     * 判断该定时是否存在
     * @param timing
     * @return
     */
    private boolean timingExist(Timing timing) {
        List<Timing> timings = mTimingDao.selTimingByCommand(mainUid,timing.getCommand());
        for (Timing t : timings) {
            int week = t.getWeek();
            if (week == 0) {
                week = getTodayWeek();
            }
            int selectedWeekInt = this.selectedWeekInt;
            if (selectedWeekInt == 0) {
                selectedWeekInt = getTodayWeek();
            }
            if (!t.getTimingId().equals(timing.getTimingId()) && t.getHour() == timing.getHour() && t.getMinute() == timing.getMinute()
                    && WeekUtil.hasEqualDayOfWeek(week, selectedWeekInt)) {
                return true;
            }
        }
        return false;
    }

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

    private void getTime() {
        timing.setHour(timePicker.getCurrentHour());
        timing.setMinute(timePicker.getCurrentMinute());
    }


    private class AddTimerControl extends AddTimer {

        public AddTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onAddTimerResult(String uid, int serial, int result, String timingId) {
            isRequesting = false;
            if (result == ErrorCode.SUCCESS) {
                dismissDialog();
                ToastUtil.showToast(
                        getResources().getString(R.string.device_timing_add_success),
                        ToastType.NULL, ToastType.SHORT);
                if (savaPopup != null) {
                    savaPopup.dismiss();
                }
                finish();
            } else {
                if (!isLoaded && result == ErrorCode.TIMING_EXIST) {
                    MyLogger.kLog().d( " has exist same time timing,load device last data.");
                    isLoaded = true;
                    mMultiLoad.setOnMultiLoadListener(SecurityTimeCreateActivity.this);
                    mMultiLoad.load(uid);
                }else if(result ==ErrorCode.TIMING_COUNT_MAX_ERROR){
                    dismissDialog();
                    ToastUtil.showToast(
                           getString(R.string.out_timer),
                            ToastType.ERROR, ToastType.SHORT);
                }
                 else {
                    dismissDialog();
                    ToastUtil.showToast(
                            ErrorMessage.getError(mAppContext, result),
                            ToastType.ERROR, ToastType.SHORT);
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
    public void onBackPressed() {
        cancel();
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
            savaPopup.showPopup(SecurityTimeCreateActivity.this, getResources()
                    .getString(R.string.save_modify_or_not), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            finish();
            return;
        }
    }

    private boolean isActionSet() {
        boolean isActionSet = false;

        if (timing.getCommand() == null || timing.getCommand().equals("")) {
            isActionSet = false;
        } else {
            isActionSet = true;
        }
        return isActionSet;
    }


    private boolean isEqualOldTimer() {
        boolean isEqualOldTiming = false;
        getTime();
        if (oldTiming != null) {
            if ((timing.getCommand().equals(oldTiming.getCommand())
                    && (timing.getHour() == oldTiming.getHour())
                    && (timing.getMinute() == oldTiming.getMinute())
                    && (selectedWeekInt == oldTiming.getWeek()))) {
                isEqualOldTiming = true;
            }
        }
        return isEqualOldTiming;
    }




}
