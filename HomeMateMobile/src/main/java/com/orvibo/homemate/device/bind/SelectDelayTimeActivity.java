package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.view.popup.SelectTimePopup;

/**
 * Created by Allen on 2015/4/17.
 */
public class SelectDelayTimeActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = SelectDelayTimeActivity.class.getSimpleName();

    private RadioButton
            zeroSecRadioButton,
            oneSecRadioButton,
            twoSecRadioButton,
            threeSecRadioButton,
            fourSecRadioButton,
            fiveSecRadioButton,
            sixSecRadioButton,
            sevenSecRadioButton,
            eightSecRadioButton,
            nineSecRadioButton,
            tenSecRadioButton,
            thirtySecRadioButton,
            customSecRadioButton;
    private RadioGroup selectTimeRadioGroup;
    //private DeviceSetTimePopup deviceSetTimePopup;
    private SelectTimePopup mSelectTimePopup;
    private int delayTime;
    private int hour, minute,second;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_delay_time);
        Intent intent = getIntent();
        delayTime = intent.getIntExtra(IntentKey.DELAY_TIME, 0);
        hour = delayTime / 3600;
        minute = (delayTime - hour * 3600) / 60;
        second = delayTime - hour * 3600 - minute * 60;
        init();
    }

    private void findViews() {
        selectTimeRadioGroup = (RadioGroup) findViewById(R.id.selectTimeRadioGroup);
        zeroSecRadioButton = (RadioButton) findViewById(R.id.zeroSecRadioButton);
        oneSecRadioButton = (RadioButton) findViewById(R.id.oneSecRadioButton);
        twoSecRadioButton = (RadioButton) findViewById(R.id.twoSecRadioButton);
        threeSecRadioButton = (RadioButton) findViewById(R.id.threeSecRadioButton);
        fourSecRadioButton = (RadioButton) findViewById(R.id.fourSecRadioButton);
        fiveSecRadioButton = (RadioButton) findViewById(R.id.fiveSecRadioButton);
        sixSecRadioButton = (RadioButton) findViewById(R.id.sixSecRadioButton);
        sevenSecRadioButton = (RadioButton) findViewById(R.id.sevenSecRadioButton);
        eightSecRadioButton = (RadioButton) findViewById(R.id.eightSecRadioButton);
        nineSecRadioButton = (RadioButton) findViewById(R.id.nineSecRadioButton);
        tenSecRadioButton = (RadioButton) findViewById(R.id.tenSecRadioButton);
        thirtySecRadioButton = (RadioButton) findViewById(R.id.thirtySecRadioButton);
        customSecRadioButton = (RadioButton) findViewById(R.id.customSecRadioButton);
        customSecRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deviceSetTimePopup.show(getString(R.string.bind_select_delay_time_title), hour, minute);
                mSelectTimePopup.show(getString(R.string.bind_select_delay_time_title),hour,minute,second);
            }
        });
    }

    private void init() {
        findViews();
        initDeviceSetTimePopup();
        initRadioGroup();
        selectTimeRadioGroup.setOnCheckedChangeListener(this);
    }

    private void initRadioGroup() {
        if (delayTime == 0) {
            zeroSecRadioButton.setChecked(true);
        }else if (delayTime == 1){
            oneSecRadioButton.setChecked(true);
        }else if (delayTime == 2){
            twoSecRadioButton.setChecked(true);
        }else if (delayTime == 3) {
            threeSecRadioButton.setChecked(true);
        }else if (delayTime == 4){
            fourSecRadioButton.setChecked(true);
        }else if (delayTime == 5) {
            fiveSecRadioButton.setChecked(true);
        }else if (delayTime == 6) {
            sixSecRadioButton.setChecked(true);
        }else if (delayTime == 7) {
            sevenSecRadioButton.setChecked(true);
        }else if (delayTime == 8) {
            eightSecRadioButton.setChecked(true);
        }else if (delayTime == 9) {
            nineSecRadioButton.setChecked(true);
        }else if (delayTime == 10) {
            tenSecRadioButton.setChecked(true);
        } else if (delayTime == 30) {
            thirtySecRadioButton.setChecked(true);
        } else {
            customSecRadioButton.setChecked(true);
            customSecRadioButton.setText(getTimeString(hour,minute,second));
        }
    }

    private void initDeviceSetTimePopup() {
/*        deviceSetTimePopup = new DeviceSetTimePopup(mContext,true) {
            @Override
            public void onSetTime(int hour, int minute) {
                customSecRadioButton.setText(getTimeString(hour,minute));
                delayTime = hour * 60 * 60 + minute * 60;
                returnResult(delayTime);
            }
        };*/
        mSelectTimePopup = new SelectTimePopup(mContext, new SelectTimePopup.OnTimeSelectListener() {
            @Override
            public void selectTime(int hour, int min, int sec) {
                customSecRadioButton.setText(getTimeString(hour,min,sec));
                delayTime = hour * 60 * 60 + min * 60 + sec;
                returnResult(delayTime);
            }
        });
    }

    private String getTimeString(int hour, int minute,int second) {
        String timeString = getResources().getString(R.string.custom_time) + " ";
        if (hour > 0) {
            timeString += hour + getResources().getString(R.string.time_hours);
        }

        if (minute > 0) {
            timeString += minute + getResources().getString(R.string.time_minutes);
        }

        if (second > 0){
            timeString += second + getResources().getString(R.string.time_second_mini);
        }
        return timeString;
    }

    public void leftTitleClick(View v) {
        returnResult(delayTime);
    }

    @Override
    public void onBackPressed() {
        returnResult(delayTime);
    }

    private void returnResult(int delayTime) {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.DELAY_TIME, delayTime);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == zeroSecRadioButton.getId()) {
            delayTime = 0;
            returnResult(delayTime);
        }else if (checkedId == oneSecRadioButton.getId()){
            delayTime = 1;
            returnResult(delayTime);
        }else if (checkedId == twoSecRadioButton.getId()){
            delayTime = 2;
            returnResult(delayTime);
        }else if (checkedId == threeSecRadioButton.getId()) {
            delayTime = 3;
            returnResult(delayTime);
        }else if (checkedId == fourSecRadioButton.getId()){
            delayTime = 4;
            returnResult(delayTime);
        }else if (checkedId == fiveSecRadioButton.getId()) {
            delayTime = 5;
            returnResult(delayTime);
        }else if (checkedId == sixSecRadioButton.getId()){
            delayTime = 6;
            returnResult(delayTime);
        }else if (checkedId == sevenSecRadioButton.getId()){
            delayTime = 7;
            returnResult(delayTime);
        }else if (checkedId == eightSecRadioButton.getId()){
            delayTime = 8;
            returnResult(delayTime);
        }else if (checkedId == nineSecRadioButton.getId()){
            delayTime = 9;
            returnResult(delayTime);
        }else if (checkedId == tenSecRadioButton.getId()) {
            delayTime = 10;
            returnResult(delayTime);
        } else if (checkedId == thirtySecRadioButton.getId()) {
            delayTime = 30;
            returnResult(delayTime);
        } else if (checkedId == customSecRadioButton.getId()) {
            //deviceSetTimePopup.show(getString(R.string.bind_select_delay_time_title), hour, minute);
            mSelectTimePopup.show(getString(R.string.bind_select_delay_time_title), hour, minute,second);
        }
    }
}
