package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ResultCode;

/**
 * Created by allen on 2016/3/22.
 */
public class DeviceAddGasFailActivity extends BaseActivity{
    private TextView button1, button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gas_device_fail);
        init();
    }

    private void init() {
        button1 = (TextView) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (TextView) findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    @Override
    public void leftTitleClick(View v) {
        setResult(ResultCode.FINISH);
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        setResult(ResultCode.FINISH);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.button1: {
                Intent intent = new Intent(this, DeviceAddGasFailTryActivity.class);
                intent.putExtra(DeviceAddGasFailTryActivity.CONDITION, DeviceAddGasFailTryActivity.NOT_ADD);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.button2: {
                Intent intent = new Intent(this, DeviceAddGasFailTryActivity.class);
                intent.putExtra(DeviceAddGasFailTryActivity.CONDITION, DeviceAddGasFailTryActivity.SOMEONE_ADD);
                startActivityForResult(intent, 0);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AddVicenterActivity.RESULT_RETRY) {
            setResult(AddVicenterActivity.RESULT_RETRY);
            finish();
        }
    }
}
