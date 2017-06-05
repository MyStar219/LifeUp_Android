package com.orvibo.homemate.device.control.infrareddevice;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.data.DeviceControlConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

/**
 * Created by Kuee on 2016/2/23.
 */
public class ChuangweiConditionerActivity extends BaseControlActivity {
    private static final String TAG = "ChuangweiConditionerActivity";
    private TextView tv_status;
    private DeviceStatus deviceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditioner_chuangwei);
        findViewById(R.id.btn_temp).setOnClickListener(this);

        tv_status = (TextView) findViewById(R.id.tv_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceStatus = mDeviceStatusDao.selDeviceStatus(deviceId);
        setACStatus(deviceStatus);
    }

    private int getTempByValue2(int value2) {
        String tStr = Integer.toBinaryString(value2);
        String tempStr = getB(tStr).substring(3);
        return Integer.parseInt(tempStr, 2);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_temp: {
                if (deviceStatus == null) {
                    ToastUtil.showToast("获取不到deviceStatus");
                } else {
                    int value2 = deviceStatus.getValue2();
                    int temp = getTempByValue2(value2);
                    if (temp >= 16) {
                        temp = 0;
                    } else {
                        temp += 1;
                    }

                    ToastUtil.showToast("控制[" + (temp + 16) + "]");

                    String value2Str = Integer.toBinaryString(temp);
                    value2Str = getB(value2Str);
                    String h = value2Str.substring(0, 3);

                    String s1 = Integer.toBinaryString(temp);
                    s1 = getB(s1);
                    String tempStr = s1.substring(3);
                    String realValue2Str = h + tempStr;
                    int realValue2 = Integer.parseInt(realValue2Str, 2);
                    LogUtil.d(TAG, "onClick()-value2:" + value2 + ",realValue2Str:" + realValue2Str + ",realValue2:" + realValue2 + ",value1:" + Integer.toBinaryString(deviceStatus.getValue1()));
                    controlDevice.startControlDevice(UserCache.getCurrentUserName(mAppContext),
                            currentMainUid,
                            deviceId,
                            DeviceOrder.AC_CTRL,
                            deviceStatus.getValue1(),
//                            deviceStatus.getValue2(),// 修改温度
                            realValue2,// 修改温度
                            deviceStatus.getValue3(),
                            deviceStatus.getValue4(),
                            0,
                            false,
                            DeviceControlConstant.QualityOfService.REQUEST_REPEATE,
                            DeviceControlConstant.DefaultResponse.RESPONSE);
                }
                break;
            }
        }
    }

    private String getB(String rB) {
        StringBuffer headStr = new StringBuffer();
        final int headlen = 8 - rB.length();
        for (int i = 0; i < headlen; i++) {
            headStr.append("0");
        }
        return headStr.append(rB).toString();
    }

    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        super.onPropertyReport(deviceId, statsType, value1, value2, value3, value4, alarmType, payloadData);
        deviceStatus = mDeviceStatusDao.selDeviceStatus(deviceId);
        setACStatus(deviceStatus);
    }

    private void setACStatus(DeviceStatus deviceStatus) {
        if (deviceStatus == null) {
            return;
        }
        int value2 = deviceStatus.getValue2();
        int temp = getTempByValue2(value2);
        tv_status.setText("当前温度：" + (temp + 16) + "°C");
    }
}
