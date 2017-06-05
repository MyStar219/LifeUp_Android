package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * Created by allen on 2016/3/22.
 */
public class DeviceAddGasFailTryActivity extends BaseActivity{
    private NavigationCocoBar navigationBar;
    private ImageView deviceImageView;
    private TextView tipTextView1, tipTextView2;
    private Button nextButton;
    public static final String CONDITION = "condition";
    public static final int NOT_ADD = 1;
    public static final int SOMEONE_ADD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        init();
    }

    @Override
    public void leftTitleClick(View v) {
        setResult(RESULT_CANCELED);
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        deviceImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        Intent intent = getIntent();
        int condition = intent.getIntExtra(CONDITION, NOT_ADD);
        if (condition == NOT_ADD) {
            navigationBar.setCenterText(getString(R.string.device_add_flammable_gas_sensor_fail_tips2));
            deviceImageView.setImageResource(R.drawable.pic_combustible_unconnected);
            tipTextView1.setText(R.string.device_add_flammable_gas_sensor_fail_tips4);
            nextButton.setText(R.string.retry_once);
        } else {
            navigationBar.setCenterText(getString(R.string.device_add_flammable_gas_sensor_fail_tips3));
            deviceImageView.setImageResource(R.drawable.pic_combustible_light_on);
            tipTextView1.setText(R.string.device_add_flammable_gas_sensor_fail_tips5);
            nextButton.setText(R.string.device_add_flammable_gas_sensor_fail_retry);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton:
                setResult(AddVicenterActivity.RESULT_RETRY);
                finish();
                break;
        }
    }
}
