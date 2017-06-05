package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.popup.DeviceSetTimePopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Smagret on 2015/10/17.
 */
public class IntelligentSceneSelectTimeActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private TextView timeIntervalTextView;
    private LinearLayout timeIntervalLinearLayout, timeStartLinearLayout, timeEndLinearLayout, timeRepeatLinearLayout;
    private DeviceSetTimePopup deviceSetTimePopup;
    private TextView timeStartTextView, timeEndTextView, timeRepeatTextView;
    private CheckBox timeRepeatSunCheckBox, timeRepeatMonCheckBox, timeRepeatTuesCheckBox;
    private CheckBox timeRepeatWedCheckBox, timeRepeatThurCheckBox, timeRepeatFriCheckBox, timeRepeatSatCheckBox;
    private boolean isChooseStart = true;
    private int startHour, startMinute, endHour, endMinute;
    private List<CheckBox> weekCheckBoxes = new ArrayList<CheckBox>();
    private List<Integer> weeks = new ArrayList<Integer>();
    private List<LinkageCondition> linkageConditions;
    private Linkage linkage;
    private boolean isBackPressed = false;
    private int mWeek;
    private String mStartTime;
    private String mEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable serializable = getIntent().getSerializableExtra(Constant.LINKAGE_CONDITIONS);
        linkageConditions = (List<LinkageCondition>) serializable;
        setContentView(R.layout.intelligent_scene_select_time_activity);
        findViews();
        init();
    }

    private void findViews() {
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

        setStatus();
        initTimeAndWeek(linkageConditions);
        WeekUtil.initWeekCheckBoxes(mContext, mWeek, weekCheckBoxes);
        setTimeInterval(mStartTime, mEndTime);
        initDeviceSetTimePopup();
    }

    private void setStatus() {
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
    }

    /**
     * @param linkageConditions
     */
    private void initTimeAndWeek(List<LinkageCondition> linkageConditions) {
        LinkageCondition linkageCondition;
        int linkageType;
        int condition;
        int value;
        boolean hasTimeAndWeekCondition = false;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                condition = linkageCondition.getCondition();
                value = linkageCondition.getValue();
                if (linkageType == 1) {
                    hasTimeAndWeekCondition = true;
                    if (condition == 3) {
                        mStartTime = TimeUtil.getTime24(mContext, value / 3600, (value % 3600) / 60);
                    } else if (condition == 4) {
                        mEndTime = TimeUtil.getTime24(mContext, value / 3600, (value % 3600) / 60);
                    }
                } else if (linkageType == 2) {
                    hasTimeAndWeekCondition = true;
                    if (condition == 0) {
                        mWeek = value;
                    }
                }
                if (!hasTimeAndWeekCondition) {
                    mWeek = 255;
                }
            }
        } else {
            mStartTime = "";
            mEndTime = "";
            mWeek = 255;
        }
    }



    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        linkageConditions = IntelligentSceneTool.getTimeAndWeekConditions(linkageConditions, mStartTime, mEndTime, mWeek);
        Intent intent = new Intent();
        intent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
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

    private void setTimeInterval(String startTime, String endTime) {
        if (TextUtils.isEmpty(startTime)) {
            startTime = "00:00";
        }
        if (TextUtils.isEmpty(endTime)) {
            endTime = "00:00";
        }
        String[] startTimeSplit = startTime.split(":");
        String[] endTimeSplit = endTime.split(":");
        if (startTimeSplit.length > 1 && endTimeSplit.length > 1) {
            startHour = Integer.valueOf(startTimeSplit[0]);
            startMinute = Integer.valueOf(startTimeSplit[1]);
            endHour = Integer.valueOf(endTimeSplit[0]);
            endMinute = Integer.valueOf(endTimeSplit[1]);
            setTimeInterval();
        }
    }

    private void setTimeInterval() {
        String timeInterval;
        if (startHour > endHour || startHour == endHour && startMinute > endMinute) {
            timeInterval = TimeUtil.getTime(mContext, startHour, startMinute) + mContext.getString(R.string.time_interval_to) + mContext.getString(R.string.time_interval_tomorrow) + TimeUtil.getTime(mContext, endHour, endMinute);
        } else if (endHour == startHour && endMinute == startMinute) {
            timeInterval = mContext.getString(R.string.time_interval_all_day);
        } else {
            timeInterval = TimeUtil.getTime(mContext, startHour, startMinute) + mContext.getString(R.string.time_interval_to) + TimeUtil.getTime(mContext, endHour, endMinute);
        }
        timeStartTextView.setText(TimeUtil.getTime(mAppContext, startHour, startMinute));
        timeEndTextView.setText(TimeUtil.getTime(mAppContext, endHour, endMinute));
        timeIntervalTextView.setText(getString(R.string.intelligent_scene_condition_time2) + timeInterval);
        mStartTime = startHour + ":" + startMinute;
        mEndTime = endHour + ":" + endMinute;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
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
            mWeek = week;
            timeRepeatTextView.setText(getString(R.string.intelligent_scene_condition_week) + WeekUtil.getWeeks(mContext, week));
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
}
