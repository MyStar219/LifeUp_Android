package com.orvibo.homemate.device.timing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.bo.TimingGroup;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TimingType;
import com.orvibo.homemate.model.AddTimingGroup;
import com.orvibo.homemate.model.DeleteTimingGroup;
import com.orvibo.homemate.model.ModifyTimingGroup;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationTextBar;
import com.orvibo.homemate.view.custom.SelectRepeatView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.DeviceSetTimePopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by allen on 2016/7/9.
 */
public class ModeTimingCreateActivity extends BaseActivity implements SelectRepeatView.OnSelectWeekListener{
    private NavigationTextBar nbTitle;
    private EditTextWithCompound etModeName;
    private View llBeginTime, llEndTime;
    private TextView tvBeginTime, tvEndTime;
    private SelectRepeatView selectRepeatView;
    private View tvDelete;
    private DeviceSetTimePopup deviceSetTimePopup;
    private SavePopup savePopup;
    private boolean isChooseStart = true;
    private boolean isEditMode = false;

    private TimingDao timingDao;
    private TimingGroup timingGroup, oldTimingGroup;
    private Timing beginTiming, endTiming, oldBeginTiming, oldEndTiming;
    private Device device;
    private AddTimingGroup addTimingGroup;
    private ModifyTimingGroup modifyTimingGroup;
    private DeleteTimingGroup deleteTimingGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_mode_timing_create);

        init();
    }

    private void init() {
        nbTitle = (NavigationTextBar) findViewById(R.id.nbTitle);
        etModeName = (EditTextWithCompound) findViewById(R.id.etModeName);
        etModeName.setNeedRestrict(false);
        etModeName.setRightfulBackgroundDrawable(null);
        etModeName.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        llBeginTime = findViewById(R.id.llBeginTime);
        llBeginTime.setOnClickListener(this);
        llEndTime = findViewById(R.id.llEndTime);
        llEndTime.setOnClickListener(this);
        tvBeginTime = (TextView) findViewById(R.id.tvBeginTime);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        selectRepeatView = (SelectRepeatView) findViewById(R.id.selectRepeatView);
        selectRepeatView.setOnSelectWeekListener(this);
        tvDelete = findViewById(R.id.tvDelete);
        tvDelete.setOnClickListener(this);
        timingDao = new TimingDao();
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.TIMING_GROUP);
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        if (serializable == null) {
            isEditMode = false;
            timingGroup = new TimingGroup();
            timingGroup.setTimingGroupId("");
            timingGroup.setUid(device.getUid());
            timingGroup.setDeviceId(device.getDeviceId());
            nbTitle.setMiddleText(getString(R.string.mode_add_title));
            beginTiming = new Timing();
            beginTiming.setUid(device.getUid());
            beginTiming.setDeviceId(device.getDeviceId());
            beginTiming.setShowIndex(1);
            beginTiming.setCommand(DeviceOrder.ON);
            Calendar calendar = Calendar.getInstance();
            beginTiming.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            beginTiming.setMinute(calendar.get(Calendar.MINUTE));
            beginTiming.setTimingType(TimingType.MODE);
            endTiming = new Timing();
            endTiming.setUid(device.getUid());
            endTiming.setDeviceId(device.getDeviceId());
            endTiming.setShowIndex(2);
            endTiming.setCommand(DeviceOrder.OFF);
            endTiming.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            endTiming.setMinute(calendar.get(Calendar.MINUTE));
            endTiming.setTimingType(TimingType.MODE);
            tvDelete.setVisibility(View.GONE);
            etModeName.setText("");
        } else {
            isEditMode = true;
            timingGroup = (TimingGroup) serializable;
            nbTitle.setMiddleText(getString(R.string.mode_edit_title));
            etModeName.setText(timingGroup.getName());
            etModeName.setSelection(timingGroup.getName().length());
            List<Timing> timings = timingDao.selTimingsByTimingGroupId(device.getUid(), timingGroup.getTimingGroupId());
            beginTiming = timings.get(0);
            endTiming = timings.get(1);
            tvDelete.setVisibility(View.VISIBLE);
        }
        setOld();

        tvBeginTime.setText(TimeUtil.getTime(mAppContext, beginTiming.getHour(), beginTiming.getMinute()));
        tvEndTime.setText(TimeUtil.getTime(mAppContext, endTiming.getHour(), endTiming.getMinute()));
        selectRepeatView.refresh(beginTiming.getWeek(), true);
        initDeviceSetTimePopup();
        initAddTimingGroup();
        initModifyTimingGroup();
        initDeleteTimingGroup();
    }

    private void setOld() {
        oldTimingGroup = new TimingGroup();
        oldTimingGroup.setName(timingGroup.getName());
        oldBeginTiming = new Timing();
        oldBeginTiming.setHour(beginTiming.getHour());
        oldBeginTiming.setMinute(beginTiming.getMinute());
        oldBeginTiming.setWeek(beginTiming.getWeek());
        oldEndTiming = new Timing();
        oldEndTiming.setHour(endTiming.getHour());
        oldEndTiming.setMinute(endTiming.getMinute());
        oldEndTiming.setWeek(endTiming.getWeek());
    }

    private void initAddTimingGroup() {
        addTimingGroup = new AddTimingGroup(mContext) {
            @Override
            public void onAddTimingGroupResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.mode_add_success);
                    finish();
                } else if(result == ErrorCode.WIFI_TIMING_COUNT_MAX_ERROR) {
                    ToastUtil.showToast(R.string.mode_add_max);
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initModifyTimingGroup() {
        modifyTimingGroup = new ModifyTimingGroup(mContext) {
            @Override
            public void onModifyTimingGroupResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.mode_modify_success);
                    finish();
                } else if (result == ErrorCode.TIMING_EXIST) {
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.TIMING_EXIST));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initDeleteTimingGroup() {
        deleteTimingGroup = new DeleteTimingGroup(mContext) {
            @Override
            public void onDeleteTimingGroupResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.mode_delete_success);
                    finish();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initDeviceSetTimePopup() {
        deviceSetTimePopup = new DeviceSetTimePopup(mContext) {
            @Override
            public void onSetTime(int hour, int minute) {
                if (isChooseStart) {
                    beginTiming.setHour(hour);
                    beginTiming.setMinute(minute);
                    tvBeginTime.setText(TimeUtil.getTime(mAppContext, hour, minute));
                } else {
                    endTiming.setHour(hour);
                    endTiming.setMinute(minute);
                    tvEndTime.setText(TimeUtil.getTime(mAppContext, hour, minute));
                }
            }
        };
    }

    @Override
    public void rightTitleClick(View v) {
        String modeName = etModeName.getText().toString();
        if (TextUtils.isEmpty(modeName)) {
            ToastUtil.showToast(R.string.mode_name_empty);
            return;
        }
        if (tvBeginTime.getText().equals(tvEndTime.getText())) {
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.mode_begin_end_time_equals));
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.show(getFragmentManager(), "");
            return;
        }
        timingGroup.setName(modeName);
        List<Timing> timingList = new ArrayList<>();
        timingList.add(beginTiming);
        timingList.add(endTiming);
        showDialog();
        if (isEditMode) {
            modifyTimingGroup.startModifyTimingGroup(timingGroup, timingList);
        } else {
            addTimingGroup.startAddTimingGroup(timingGroup, timingList);
        }
    }

    private boolean hasChanged() {
        if(!oldTimingGroup.getName().equals(etModeName.getText().toString())) {
            return true;
        }
        if (oldBeginTiming.getHour() != beginTiming.getHour()|| oldBeginTiming.getMinute() != beginTiming.getMinute() ||
                oldBeginTiming.getWeek() != beginTiming.getWeek() || oldEndTiming.getHour() != endTiming.getHour() ||
                oldEndTiming.getMinute() != endTiming.getMinute() || oldEndTiming.getWeek() != endTiming.getWeek()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (hasChanged()) {
            if(savePopup == null) {
                savePopup = new SavePopup();
            }
            savePopup.showPopup(mContext, getResources()
                    .getString(R.string.mode_save_confirm), getResources()
                    .getString(R.string.save), getResources()
                    .getString(R.string.not_save));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.llBeginTime:
                isChooseStart = true;
                deviceSetTimePopup.show(getString(R.string.mode_begin_time_choose), beginTiming.getHour(), beginTiming.getMinute());
                break;
            case R.id.llEndTime:
                isChooseStart = false;
                deviceSetTimePopup.show(getString(R.string.mode_end_time_choose), endTiming.getHour(), endTiming.getMinute());
                break;
            case R.id.tvDelete:
                showDialog();
                deleteTimingGroup.startDeleteTimingGroup(timingGroup.getTimingGroupId());
                break;
        }
    }

    private class SavePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
            rightTitleClick(null);
        }

        public void cancel() {
            dismiss();
            finish();
        }
    }

    @Override
    public void onSelectWeek(int selectWeek) {
        beginTiming.setWeek(selectWeek);
        endTiming.setWeek(selectWeek);
    }
}
