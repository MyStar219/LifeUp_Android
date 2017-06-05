//package com.orvibo.homemate.device.timing;
//
//import android.app.AlertDialog;
//import android.app.FragmentTransaction;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.format.DateFormat;
//import android.text.format.Time;
//import android.view.View;
//import android.widget.Button;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.bo.Timing;
//import com.orvibo.homemate.common.BaseActivity;
//import com.orvibo.homemate.common.adapter.WheelTextAdapter;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.DeviceOrder;
//import com.orvibo.homemate.data.DeviceStatusConstant;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.data.TimingConstant;
//import com.orvibo.homemate.dao.DeviceStatusDao;
//import com.orvibo.homemate.dao.TimingDao;
//import com.orvibo.homemate.model.AddTimer;
//import com.orvibo.homemate.model.DeleteTimer;
//import com.orvibo.homemate.model.ModifyTimer;
//import com.orvibo.homemate.sharedPreferences.UserCache;
//import com.orvibo.homemate.util.BroadcastUtil;
//import com.orvibo.homemate.util.NetUtil;
//import com.orvibo.homemate.util.ToastUtil;
//import com.orvibo.homemate.util.WeekUtil;
//import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
//import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
//import com.orvibo.homemate.view.custom.NavigationCocoBar;
//import com.orvibo.homemate.view.custom.ProgressDialogFragment;
//import com.orvibo.homemate.view.custom.wheelview.TosGallery;
//import com.orvibo.homemate.view.custom.wheelview.WheelView;
//import com.tencent.stat.StatService;
//
//import java.util.List;
//
//public class TimingAddActivity extends BaseActivity implements View.OnClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener, ProgressDialogFragment.OnCancelClickListener {
//
//    private static final String TAG = TimingAddActivity.class.getName();
//    private final static int TIMING_MANAGE_SUCCESS = 0;
//    private final static int TIMING_MANAGE_FAILED = 1;
//    public static final String ME = TAG;
//    private WheelView mHourWheel;
//    private WheelView mMinuteWheel;
//    private WheelView mFormatWheel;
//    private Timing timing;
//    private Device device;
//    private TextView timing_repeat;
//    //    private TimingManage timingManage;
//    private RadioGroup radioGroup;
//    private RadioButton timingActionOn;
//    private RadioButton timingActionOff;
//    private String status;
//    private NavigationCocoBar navigationCocoBar;
//    private AlertDialog makeSureDialog;
//    private Button timingDelete;
//    private boolean isChanged;
//    private boolean is24HourFormat;
//    private int hourPosition;
//    private boolean hasTiming;
//    private AddTimer addTimer;
//    private ModifyTimer modifyTimer;
//    private DeleteTimer deleteTimer;
//    private boolean isRequesting = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.timing_add_activity);
//        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
//        timing = (Timing) getIntent().getSerializableExtra(Constant.TIMING);
//        is24HourFormat = DateFormat.is24HourFormat(mAppContext);
//        init();
//
//        setTiming();
//        if (is24HourFormat || timing.getHour() < 12) {
//            hourPosition = timing.getHour();
//        } else {
//            hourPosition = timing.getHour() - 12;
//        }
//        mHourWheel.setSelection(hourPosition);
//        mMinuteWheel.setSelection(timing.getMinute());
//        if (is24HourFormat) {
//            mFormatWheel.setVisibility(View.GONE);
//        } else {
//            mFormatWheel.setVisibility(View.VISIBLE);
//            mFormatWheel.setSelection(timing.getHour() < 12 ? 0 : 1);
//        }
//        BroadcastUtil.recBroadcast(receiver, mAppContext, ME);
////        timingManage = new TimingManage(TimingAddActivity.this, device.getUid());
////        timingManage.setOnTimingManageResult(this);
//    }
//
//    private void init() {
//        timingDelete = (Button) findViewById(R.id.timingDelete);
//        timingDelete.setOnClickListener(this);
//        mHourWheel = (WheelView) findViewById(R.id.wheel_hour);
//        mMinuteWheel = (WheelView) findViewById(R.id.wheel_minute);
//        mFormatWheel = (WheelView) findViewById(R.id.wheel_format);
//
//        mHourWheel.setOnEndFlingListener(mListener);
//        mMinuteWheel.setOnEndFlingListener(mListener);
//        mFormatWheel.setOnEndFlingListener(mListener);
//
//        mHourWheel.setScrollCycle(true);
//        mMinuteWheel.setScrollCycle(true);
//        mFormatWheel.setScrollCycle(false);
//
//        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        navigationCocoBar.setOnRightClickListener(new NavigationCocoBar.OnRightClickListener() {
//            @Override
//            public void onRightClick(View v) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_Save), null);
//                save();
//            }
//        });
//        navigationCocoBar.setOnLeftClickListener(new NavigationCocoBar.OnLeftClickListener() {
//            @Override
//            public void onLeftClick(View v) {
//                if (!isChanged) {
//                    onBackPressed();
//                } else {
//                    DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
//                    String title = getString(R.string.timing_save_title);
//                    dialogFragment.setTitle(title);
//                    dialogFragment.setOnTwoButtonClickListener(TimingAddActivity.this);
//                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                    dialogFragment.show(transaction, getClass().getName());
//                }
//            }
//        });
//
//        if (is24HourFormat) {
//            mHourWheel.setAdapter(new WheelTextAdapter(this,
//                    WheelTextAdapter.TYPE_HOUR));
//        } else {
//            mHourWheel.setAdapter(new WheelTextAdapter(this,
//                    WheelTextAdapter.TYPE_FMHOUR));
//        }
//        mMinuteWheel.setAdapter(new WheelTextAdapter(this,
//                WheelTextAdapter.TYPE_MINUTE));
//        mFormatWheel.setAdapter(new WheelTextAdapter(this,
//                WheelTextAdapter.TYPE_FORMAT));
//
//        final Time time = new Time();
//        time.setToNow();
//
//        timing_repeat = (TextView) findViewById(R.id.timing_repeat);
//        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
//        timingActionOn = (RadioButton) findViewById(R.id.timing_action_on);
//        timingActionOff = (RadioButton) findViewById(R.id.timing_action_off);
//
//        if (timing != null) {
//            status = timing.getCommand();
//            if (status.equals(DeviceOrder.ON)) {
//                radioGroup.check(timingActionOn.getId());
//            } else {
//                radioGroup.check(timingActionOff.getId());
//            }
//        } else {
//            int s = new DeviceStatusDao().selDeviceStatus(device.getUid(), device.getDeviceId()).getValue1();
//            if (s == DeviceStatusConstant.OFF) {
//                status = DeviceOrder.ON;
//                radioGroup.check(timingActionOn.getId());
//            } else {
//                status = DeviceOrder.OFF;
//                radioGroup.check(timingActionOff.getId());
//            }
//        }
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                isChanged = true;
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_Action), null);
//                if (checkedId == timingActionOn.getId()) {
//                    status = DeviceOrder.ON;
//                    timing.setValue1(DeviceStatusConstant.ON);
//                } else {
//                    timing.setValue1(DeviceStatusConstant.OFF);
//                    status = DeviceOrder.OFF;
//                }
//                timing.setCommand(status);
//            }
//        });
//        addTimer = new AddTimer(mAppContext) {
//            @Override
//            public void onAddTimerResult(String uid, int serial, int result,
//                                         String timingId) {
//                dismissDialog();
//                isRequesting = false;
//                if (result == ErrorCode.SUCCESS) {
//                    finish();
//                } else if (result == ErrorCode.FAIL) {
//                    ToastUtil.showToast(R.string.timing_add_fail);
//                } else if (result == ErrorCode.STORAGE_FULL_ERROR) {
//                    ToastUtil.showToast(R.string.timing_storage_full);
//                } else if (result == ErrorCode.TIMING_COUNT_MAX_ERROR) {
//                    ToastUtil.showToast(R.string.timing_count_full);
//                } else if (result == ErrorCode.TIMEOUT_AT) {
//                    ToastUtil.showToast(R.string.TIMEOUT);
//                } else if (result == ErrorCode.TIMING_EXIST) {
//                    ToastUtil.showToast(R.string.TIMING_EXIST);
//                }
//            }
//        };
//        modifyTimer = new ModifyTimer(mAppContext) {
//            @Override
//            public void onModifyTimerResult(String uid, int serial, int result) {
//                dismissDialog();
//                isRequesting = false;
//                if (result == ErrorCode.SUCCESS) {
//                    finish();
//                } else if (result == ErrorCode.TIMEOUT_MT) {
//                    ToastUtil.showToast(R.string.TIMEOUT);
//                } else if (result == ErrorCode.TIMING_EXIST) {
//                    ToastUtil.showToast(R.string.TIMING_EXIST);
//                } else {
//                    ToastUtil.showToast(R.string.timing_change_fail);
//                }
//            }
//        };
//        deleteTimer = new DeleteTimer(mAppContext) {
//            @Override
//            public void onDeleteTimerResult(String uid, int serial, int result) {
//                dismissDialog();
//                isRequesting = false;
//                if (result == ErrorCode.SUCCESS) {
//                    finish();
//                } else if (result == ErrorCode.FAIL) {
//                    ToastUtil.showToast(R.string.timing_delete_fail);
//                } else if (result == ErrorCode.TIMEOUT_DT) {
//                    ToastUtil.showToast(R.string.TIMEOUT);
//                } else {
//                    ToastUtil.toastError(result);
//                }
//            }
//        };
//    }
//
//    public void repeat(View v) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_Repeat), null);
//        Intent intent = new Intent(this, TimingRepeatActivity.class);
//        intent.putExtra("week", timing.getWeek());
//        this.startActivity(intent);
//    }
//
//    public void delete(View v) {
//        if (timing != null) {
////            timingManage.delete(timing.getTimingNo());
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.timingDelete: {
//                if (!isRequesting) {
//                    isRequesting = true;
//                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_Delete), null);
//                    showDialog(this, getString(R.string.timing_deleting));
//                    deleteTimer.startDeleteTimer(device.getUid(), timing.getUserName(), timing.getTimingId());
//                }
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void leftTitleClick(View v) {
//        onBackPressed();
//    }
//
//    @Override
//    public void onBackPressed() {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_Cancel), null);
//        super.onBackPressed();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        timing_repeat.setText(WeekUtil.getWeeks(mAppContext, timing.getWeek()));
//    }
//
//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context arg0, Intent arg1) {
//
//            if (arg1 != null && arg1.getIntExtra("event", -1) == 0) {
//                isChanged = true;
//                timing.setWeek(arg1.getIntExtra("week", 0));
//                return;
//            }
//        }
//    };
//
//    private void setTiming() {
//        if (timing == null) {
//            hasTiming = false;
//            Time t = new Time();
//            t.setToNow();
//            timing = new Timing();
//            timing.setWeek(0x00);
//            timing.setUid(device.getUid());
//            timing.setDeviceId(device.getDeviceId());
//            timing.setUserName(UserCache.getCurrentUserName(mAppContext));
//            timing.setHour(t.hour);
//            timing.setMinute(t.minute);
//            timing.setSecond(0);
//            timing.setIsPause(TimingConstant.TIMEING_EFFECT);
//            String cmd = String.valueOf(status);
//            timing.setCommand(cmd);
//            timingDelete.setVisibility(View.GONE);
//        } else {
//            hasTiming = true;
//            timingDelete.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private TosGallery.OnEndFlingListener mListener = new TosGallery.OnEndFlingListener() {
//        @Override
//        public void onEndFling(TosGallery v) {
//            isChanged = true;
//            int pos = v.getSelectedItemPosition();
//            if (v == mHourWheel || v == mFormatWheel) {
//                hourPosition = mHourWheel.getSelectedItemPosition();
//                timing.setHour(mFormatWheel.getSelectedItemPosition() * 12 + hourPosition);
//            } else if (v == mMinuteWheel) {
//                timing.setMinute(pos);
//            }
//        }
//    };
//
////    @Override
////    public void onTimingManageResult(String uid, int result) {
////        if (result == 0) {
////            mHandler.sendEmptyMessage(TIMING_MANAGE_SUCCESS);
////        } else {
////            mHandler.sendEmptyMessage(TIMING_MANAGE_FAILED);
////        }
////        finish();
////    }
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case TIMING_MANAGE_SUCCESS:
//                    ToastUtil.showToast(R.string.operation_success);
//                    break;
//                case TIMING_MANAGE_FAILED:
//                    ToastUtil.showToast(R.string.operation_failed);
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//
//    @Override
//    public void onLeftButtonClick(View view) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_PopViewDoNotSave), null);
//        TimingAddActivity.this.finish();
//    }
//
//    @Override
//    public void onRightButtonClick(View view) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_PopViewSave), null);
//        save();
//    }
//
//    private void save() {
//        if (!NetUtil.isNetworkEnable(mAppContext)) {
//            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
//            return;
//        }
//        if (!isRequesting) {
//            if (timingExist()) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOEditTimer_TimeConflict), null);
//                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                dialogFragmentOneButton.setTitle(getString(R.string.TIMING_EXIST));
//                dialogFragmentOneButton.setButtonText(getString(R.string.ok));
//                dialogFragmentOneButton.show(getFragmentManager(), "");
//            } else {
//                isRequesting = true;
//                if (hasTiming) {
//                    showDialog(this, getString(R.string.timing_changing));
//                    modifyTimer.startModifyTimer(timing.getUid(), UserCache.getCurrentUserName(mAppContext), timing.getTimingId(), timing.getCommand(), timing.getValue1(), timing.getValue2(), timing.getValue3(), timing.getValue4(), timing.getHour(), timing.getMinute(), timing.getSecond(), timing.getWeek());
//                } else {
//                    showDialog(this, getString(R.string.timing_adding));
//                    addTimer.startAddTimer(device.getUid(), UserCache.getCurrentUserName(mAppContext), device.getDeviceId(),
//                            timing.getCommand(), timing.getValue1(), timing.getValue2(), timing.getValue3(),
//                            timing.getValue4(), timing.getHour(), timing.getMinute(), timing.getSecond(), timing.getWeek());
//
//                }
//            }
//        }
//    }
//
//    private boolean timingExist() {
//        List<Timing> timings = new TimingDao().selTimingsByDevice(device.getUid(), device.getDeviceId());
//        for (Timing t : timings) {
//            if (!t.getTimingId().equals(timing.getTimingId())
//                    && t.getHour() == timing.getHour()
//                    && t.getMinute() == timing.getMinute()
//                    && t.getWeek() == timing.getWeek()
//                    && t.getIsPause() == timing.getIsPause()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void onCancelClick(View view) {
//        isRequesting = false;
//    }
//}
