package com.orvibo.homemate.device.timing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.util.WeekUtil;

import org.apache.mina.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Allen on 2015/4/3.
 * Modified by smagret on 2015/04/14
 */
public class DeviceTimingRepeatActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = DeviceTimingRepeatActivity.class
            .getSimpleName();

    private ImageView back_iv;
    private RelativeLayout rlMonday, rlTuesday, rlWednesday, rlThursday, rlFriday, rlSaturday, rlSunday;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private List<CheckBox> weekCheckBoxes = new ArrayList<CheckBox>();
    private Set<Integer> weekSet = new ConcurrentHashSet<Integer>();
    private ArrayList<Integer> weeks = new ArrayList<Integer>();
    private int selectedWeekInt;
    private String selectedWeekString = "";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_repeat);

        Intent intent = getIntent();
        selectedWeekInt = intent.getIntExtra("selectedWeekInt", 0);
        init();

    }

    private void init() {
        initView();
        initListener();
        initData();
    }

    private void initView() {
        back_iv = (ImageView) findViewById(R.id.back_iv);
        rlMonday = (RelativeLayout) findViewById(R.id.rlMonday);
        rlTuesday = (RelativeLayout) findViewById(R.id.rlTuesday);
        rlWednesday = (RelativeLayout) findViewById(R.id.rlWednesday);
        rlThursday = (RelativeLayout) findViewById(R.id.rlThursday);
        rlFriday = (RelativeLayout) findViewById(R.id.rlFriday);
        rlSaturday = (RelativeLayout) findViewById(R.id.rlSaturday);
        rlSunday = (RelativeLayout) findViewById(R.id.rlSunday);
        cbMonday = (CheckBox) findViewById(R.id.cbMonday);
        cbTuesday = (CheckBox) findViewById(R.id.cbTuesday);
        cbWednesday = (CheckBox) findViewById(R.id.cbWednesday);
        cbThursday = (CheckBox) findViewById(R.id.cbThursday);
        cbFriday = (CheckBox) findViewById(R.id.cbFriday);
        cbSaturday = (CheckBox) findViewById(R.id.cbSaturday);
        cbSunday = (CheckBox) findViewById(R.id.cbSunday);

        weekCheckBoxes.add(cbMonday);
        weekCheckBoxes.add(cbTuesday);
        weekCheckBoxes.add(cbWednesday);
        weekCheckBoxes.add(cbThursday);
        weekCheckBoxes.add(cbFriday);
        weekCheckBoxes.add(cbSaturday);
        weekCheckBoxes.add(cbSunday);
    }

    private void initListener() {
        back_iv.setOnClickListener(this);
        rlMonday.setOnClickListener(this);
        rlTuesday.setOnClickListener(this);
        rlWednesday.setOnClickListener(this);
        rlThursday.setOnClickListener(this);
        rlFriday.setOnClickListener(this);
        rlSaturday.setOnClickListener(this);
        rlSunday.setOnClickListener(this);
    }

    private void initData() {

        WeekUtil.initWeekCheckBoxes(mContext,selectedWeekInt,weekCheckBoxes);
    }

    @Override
    public void onBackPressed() {
        selectedWeekInt = WeekUtil.getSelectedWeekInt(getSelectedWeek());
        selectedWeekString = WeekUtil.getWeeks(mContext, selectedWeekInt);
//       selectedWeekString = WeekUtil.getSelectedWeekString(context, selectedWeekInt);
        Intent data = new Intent();
        data.putExtra("selectedWeekInt", selectedWeekInt);
        data.putExtra("selectedWeekString", selectedWeekString);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_iv:
                onBackPressed();
                break;
            case R.id.rlMonday:
                cbMonday.performClick();
                break;
            case R.id.rlTuesday:
                cbTuesday.performClick();
                break;
            case R.id.rlWednesday:
                cbWednesday.performClick();
                break;
            case R.id.rlThursday:
                cbThursday.performClick();
                break;
            case R.id.rlFriday:
                cbFriday.performClick();
                break;
            case R.id.rlSaturday:
                cbSaturday.performClick();
                break;
            case R.id.rlSunday:
                cbSunday.performClick();
                break;
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
