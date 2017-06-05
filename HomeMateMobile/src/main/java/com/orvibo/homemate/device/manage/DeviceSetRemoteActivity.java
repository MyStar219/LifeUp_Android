package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.bind.BaseSelectKeyNoActivity;

import java.util.HashMap;

/**
 * 遥控器入网：<p>
 * 1.长按2+和设置<p>
 * 2.按0561<p>
 * 3.重新上电<p>
 *
 * Created by Allen on 2015/4/23.
 * 注： + - 设置按键不能绑定，遥控器一个按键不能绑定多个动作
 */
public class DeviceSetRemoteActivity extends BaseSelectKeyNoActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_remote);
        initKeyViews();
//        initBound();

        setTitle(getString(R.string.device_set_title));
    }

    private void initKeyViews() {
        TextView remote_on_tv = (TextView) findViewById(R.id.remote_on_tv);
        TextView remote_off_tv = (TextView) findViewById(R.id.remote_off_tv);
        TextView remote_at_home_tv = (TextView) findViewById(R.id.remote_at_home_tv);
        TextView remote_leave_home_tv = (TextView) findViewById(R.id.remote_leave_home_tv);
        TextView remote_sleep_tv = (TextView) findViewById(R.id.remote_sleep_tv);
        TextView remote_fun_tv = (TextView) findViewById(R.id.remote_fun_tv);
        TextView remote_tv1 = (TextView) findViewById(R.id.remote_tv1);
        TextView remote_tv2 = (TextView) findViewById(R.id.remote_tv2);
        TextView remote_tv3 = (TextView) findViewById(R.id.remote_tv3);
        TextView remote_tv4 = (TextView) findViewById(R.id.remote_tv4);
        TextView remote_tv5 = (TextView) findViewById(R.id.remote_tv5);
        TextView remote_tv6 = (TextView) findViewById(R.id.remote_tv6);
        TextView remote_tv7 = (TextView) findViewById(R.id.remote_tv7);
        TextView remote_tv8 = (TextView) findViewById(R.id.remote_tv8);
        TextView remote_tv9 = (TextView) findViewById(R.id.remote_tv9);
        TextView remote_tv0 = (TextView) findViewById(R.id.remote_tv0);
        TextView remote_tv1_plus = (TextView) findViewById(R.id.remote_tv1_plus);
        TextView remote_tv2_plus = (TextView) findViewById(R.id.remote_tv2_plus);
        TextView remote_lock = (TextView) findViewById(R.id.remote_lock);
        TextView remote_plus = (TextView) findViewById(R.id.remote_plus);
        TextView remote_minus = (TextView) findViewById(R.id.remote_minus);
        TextView remote_set = (TextView) findViewById(R.id.remote_set);
        mAllKeyViews = new HashMap<Integer, View>();
        mAllKeyViews.put(1, remote_on_tv);
        mAllKeyViews.put(4, remote_off_tv);
        mAllKeyViews.put(2, remote_at_home_tv);
        mAllKeyViews.put(3, remote_leave_home_tv);
        mAllKeyViews.put(21, remote_sleep_tv);
        mAllKeyViews.put(23, remote_fun_tv);
        mAllKeyViews.put(5, remote_tv1);
        mAllKeyViews.put(6, remote_tv2);
        mAllKeyViews.put(7, remote_tv3);
        mAllKeyViews.put(8, remote_tv4);
        mAllKeyViews.put(9, remote_tv5);
        mAllKeyViews.put(10, remote_tv6);
        mAllKeyViews.put(11, remote_tv7);
        mAllKeyViews.put(12, remote_tv8);
        mAllKeyViews.put(14, remote_tv9);
        mAllKeyViews.put(15, remote_tv0);
        mAllKeyViews.put(13, remote_tv1_plus);
        mAllKeyViews.put(16, remote_tv2_plus);
        mAllKeyViews.put(17, remote_lock);
        mAllKeyViews.put(18, remote_plus);
        mAllKeyViews.put(19, remote_minus);
        mAllKeyViews.put(20, remote_set);
    }
}
