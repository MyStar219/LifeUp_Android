package com.orvibo.homemate.device.timing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.smartgateway.app.R;

/**
 * Created by Allen on 2015/4/3.
 */
public class DeviceTimingAddActivity extends Activity implements View.OnClickListener {
    private static final String TAG = DeviceTimingAddActivity.class
            .getSimpleName();
    private ImageView back_iv;
    private RadioGroup rgAction;
    private RadioButton rb_open;
    private RadioButton rb_close;
    private String timingAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_action);

        findViews();
        init();
    }

    private void findViews() {
        back_iv = (ImageView) findViewById(R.id.back_iv);
        rgAction = (RadioGroup) findViewById(R.id.rgAction);
        rb_open = (RadioButton) findViewById(R.id.rb_open);
        rb_close = (RadioButton) findViewById(R.id.rb_close);
    }

    /**
     *
     */
    private void init() {
        Intent intent = getIntent();
        //TODO
//        device = (Device) intent.getSerializableExtra("device");
//        deviceId = device.getId();
//        uid = device.getUid();
        back_iv.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {

        timingAction = getTimingAction();
        Intent data = new Intent();
        data.putExtra("timingAction", timingAction);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    private String getTimingAction() {

        String temp = "";
        int index = rgAction.getCheckedRadioButtonId();
        switch (index) {
            case R.id.rb_open:
                temp = getResources().getString(R.string.device_timing_action_open);
                break;
            case R.id.rb_close:
                temp = getResources().getString(R.string.device_timing_action_shutdown);
                break;
        }
        return temp;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_iv:
                onBackPressed();
                break;
        }
    }
}
