package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.bind.BaseSelectKeyNoActivity;

import java.util.HashMap;

/**
 * 极悦智能遥控器（随意贴）
 * Created by wenchao on 2016/1/4.
 *
 */
public class DeviceSetJiyueRemoteActivity extends BaseSelectKeyNoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_jiyue_remote);
        initKeyViews();
//        initBound();

        setTitle(getString(R.string.device_set_panel_title));
    }

    private void initKeyViews() {
        View panel_v1 =  findViewById(R.id.panel_v1);
        View panel_v2 =  findViewById(R.id.panel_v2);
        View panel_v3 =  findViewById(R.id.panel_v3);
        View panel_v4 =  findViewById(R.id.panel_v4);
        TextView panel_tv1 = (TextView) findViewById(R.id.panel_tv1);
        TextView panel_tv2 = (TextView) findViewById(R.id.panel_tv2);
        TextView panel_tv3 = (TextView) findViewById(R.id.panel_tv3);
        TextView panel_tv4 = (TextView) findViewById(R.id.panel_tv4);
        TextView panel_tv5 = (TextView) findViewById(R.id.panel_tv5);
        TextView panel_tv6 = (TextView) findViewById(R.id.panel_tv6);
        TextView panel_tv7 = (TextView) findViewById(R.id.panel_tv7);
        TextView panel_tv8 = (TextView) findViewById(R.id.panel_tv8);
        View layoutView1 =  findViewById(R.id.layoutView1);
        View layoutView2 =  findViewById(R.id.layoutView2);
        View layoutView3 =  findViewById(R.id.layoutView3);
        View layoutView4 =  findViewById(R.id.layoutView4);
        View colorView1 =  findViewById(R.id.colorView1);
        View colorView2 =  findViewById(R.id.colorView2);
        View colorView3 =  findViewById(R.id.colorView3);
        View colorView4 =  findViewById(R.id.colorView4);
        mAllKeyViews = new HashMap<Integer, View>();
        mAllKeyTextViews1 = new HashMap<Integer, TextView>();
        mAllKeyTextViews2 = new HashMap<Integer, TextView>();
        mAllKeyLayoutViews = new HashMap<Integer, View>();
        mAllKeyColorViews = new HashMap<Integer, View>();
        mAllKeyViews.put(3, panel_v1);
        mAllKeyViews.put(11, panel_v2);
        mAllKeyViews.put(7, panel_v3);
        mAllKeyViews.put(15, panel_v4);
        mAllKeyTextViews1.put(3, panel_tv1);
        mAllKeyTextViews1.put(11, panel_tv3);
        mAllKeyTextViews1.put(7, panel_tv5);
        mAllKeyTextViews1.put(15, panel_tv7);
        mAllKeyTextViews2.put(3, panel_tv2);
        mAllKeyTextViews2.put(11, panel_tv4);
        mAllKeyTextViews2.put(7, panel_tv6);
        mAllKeyTextViews2.put(15, panel_tv8);
        mAllKeyLayoutViews.put(3,layoutView1);
        mAllKeyLayoutViews.put(11,layoutView2);
        mAllKeyLayoutViews.put(7,layoutView3);
        mAllKeyLayoutViews.put(15,layoutView4);
        mAllKeyColorViews.put(3,colorView1);
        mAllKeyColorViews.put(11,colorView2);
        mAllKeyColorViews.put(7,colorView3);
        mAllKeyColorViews.put(15,colorView4);
    }
}
