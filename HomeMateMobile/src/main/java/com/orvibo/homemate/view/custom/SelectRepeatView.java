package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.WeekUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smagret
 * @date 2016/1/26
 * 选择星期
 */
public class SelectRepeatView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = SelectRepeatView.class.getSimpleName();
    private LinearLayout timeRepeatLinearLayout;
    private TextView timeRepeatTextView;
    private CheckBox timeRepeatSunCheckBox, timeRepeatMonCheckBox, timeRepeatTuesCheckBox;
    private CheckBox timeRepeatWedCheckBox, timeRepeatThurCheckBox, timeRepeatFriCheckBox, timeRepeatSatCheckBox;
    private List<CheckBox> weekCheckBoxes = new ArrayList<CheckBox>();
    private List<Integer> weeks = new ArrayList<Integer>();
    private Context mContext;
    private OnSelectWeekListener onSelectWeekListener;

    public SelectRepeatView(Context context) {
        super(context);
        init(context);
    }

    public SelectRepeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectRepeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }


    public SelectRepeatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_select_repeat, this, true);
        findViews();
        init();
    }

    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    private void findViews() {
        timeRepeatLinearLayout = (LinearLayout) findViewById(R.id.timeRepeatLinearLayout);
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
    }

    public void refresh(int week, boolean checkable) {
        setCheckBoxBackground();
        setStatus(checkable);
        WeekUtil.initWeekCheckBoxes(mContext, week, weekCheckBoxes);
        timeRepeatTextView.setText(mContext.getResources().getString(R.string.time_repeat) + WeekUtil.getWeeks(mContext, week));
    }

    /**
     * 强制重绘背景
     */
    private void setCheckBoxBackground() {
        if (weekCheckBoxes != null) {
            int size = weekCheckBoxes.size();
            for (int i = 0; i < size; i++) {
                weekCheckBoxes.get(i).setBackgroundResource(R.drawable.time_repeat_bg);
            }
        }
    }

    public void setStatus(boolean checkable) {
        if (checkable) {
            timeRepeatLinearLayout.setBackgroundResource(R.color.white);
            timeRepeatTextView.setBackgroundResource(R.color.white);
            timeRepeatSunCheckBox.setEnabled(true);
            timeRepeatMonCheckBox.setEnabled(true);
            timeRepeatTuesCheckBox.setEnabled(true);
            timeRepeatWedCheckBox.setEnabled(true);
            timeRepeatThurCheckBox.setEnabled(true);
            timeRepeatFriCheckBox.setEnabled(true);
            timeRepeatSatCheckBox.setEnabled(true);
        } else {
            timeRepeatLinearLayout.setBackgroundResource(R.color.transparent);
            timeRepeatTextView.setBackgroundResource(R.color.transparent);
            timeRepeatSunCheckBox.setEnabled(false);
            timeRepeatMonCheckBox.setEnabled(false);
            timeRepeatTuesCheckBox.setEnabled(false);
            timeRepeatWedCheckBox.setEnabled(false);
            timeRepeatThurCheckBox.setEnabled(false);
            timeRepeatFriCheckBox.setEnabled(false);
            timeRepeatSatCheckBox.setEnabled(false);
        }
    }

    /**
     * add by yuwei
     * 大拿设置录像计划，不可以不选择重复，所以判断如果只选择了一天，那就不可再取消
     *
     * @param week
     */
    public void checkChangeRefresh(int week) {
        WeekUtil.weekCheckChangeNotAllNull(week, weekCheckBoxes);
    }

    public void setAllCheckBoxClickable(boolean isClickable) {
        timeRepeatSunCheckBox.setClickable(isClickable);
        timeRepeatMonCheckBox.setClickable(isClickable);
        timeRepeatTuesCheckBox.setClickable(isClickable);
        timeRepeatWedCheckBox.setClickable(isClickable);
        timeRepeatThurCheckBox.setClickable(isClickable);
        timeRepeatFriCheckBox.setClickable(isClickable);
        timeRepeatSatCheckBox.setClickable(isClickable);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //TODO 以后扩展
//        if (!timeRepeatSunCheckBox.isChecked() && !timeRepeatMonCheckBox.isChecked() && !timeRepeatTuesCheckBox.isChecked() && !timeRepeatWedCheckBox.isChecked()
//                && !timeRepeatThurCheckBox.isChecked() && !timeRepeatFriCheckBox.isChecked() && !timeRepeatSatCheckBox.isChecked()) {
//            buttonView.setChecked(true);
//        } else {
        int week = WeekUtil.getSelectedWeekInt(getSelectedWeek());
//            if (week == 0) {
//                week = 255;
//            }
        if (onSelectWeekListener != null) {
            onSelectWeekListener.onSelectWeek(week);
        }
        timeRepeatTextView.setText(mContext.getResources().getString(R.string.time_repeat) + WeekUtil.getWeeks(mContext, week));
        if (isChecked) {
            buttonView.setBackgroundColor(getResources().getColor(R.color.green));
            buttonView.setTextColor(getResources().getColor(R.color.white));
        } else {
            buttonView.setBackgroundColor(getResources().getColor(R.color.bg_white_gray));
            buttonView.setTextColor(getResources().getColor(R.color.gray));
        }
//        }
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

    public void setOnSelectWeekListener(OnSelectWeekListener onSelectWeekListener) {
        this.onSelectWeekListener = onSelectWeekListener;
    }

    public interface OnSelectWeekListener {
        void onSelectWeek(int selectWeek);
    }
}
