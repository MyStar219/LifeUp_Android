package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.BaseSelectKeyNoActivity;

import java.util.HashMap;

/**
 * 情景面板绑定界面
 * Created by Allen on 2015/4/23.
 *
 * @intent {@link IntentKey#DEVICE}情景面板对象
 */
public class DeviceSetSevenKeySceneActivity extends BaseSelectKeyNoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_seven_key_scene);
        initKeyViews();
//        initBound();

        setTitle(getString(R.string.device_set_panel_title));
    }

    private void initKeyViews() {
        View panel_v1 =  findViewById(R.id.panel_v1);
        View panel_v2 =  findViewById(R.id.panel_v2);
        View panel_v3 =  findViewById(R.id.panel_v3);
        View panel_v4 =  findViewById(R.id.panel_v4);
        View panel_v5 =  findViewById(R.id.panel_v5);
        View panel_v6 =  findViewById(R.id.panel_v6);
        View panel_v7 =  findViewById(R.id.panel_v7);
        TextView panel_tv1 = (TextView) findViewById(R.id.panel_tv1);
        TextView panel_tv2 = (TextView) findViewById(R.id.panel_tv2);
        TextView panel_tv3 = (TextView) findViewById(R.id.panel_tv3);
        TextView panel_tv4 = (TextView) findViewById(R.id.panel_tv4);
        TextView panel_tv5 = (TextView) findViewById(R.id.panel_tv5);
        TextView panel_tv6 = (TextView) findViewById(R.id.panel_tv6);
        TextView panel_tv7 = (TextView) findViewById(R.id.panel_tv7);
        TextView panel_tv8 = (TextView) findViewById(R.id.panel_tv8);
        TextView panel_tv9 = (TextView) findViewById(R.id.panel_tv9);
        TextView panel_tv10 = (TextView) findViewById(R.id.panel_tv10);
        TextView panel_tv11 = (TextView) findViewById(R.id.panel_tv11);
        TextView panel_tv12 = (TextView) findViewById(R.id.panel_tv12);
        TextView panel_tv13 = (TextView) findViewById(R.id.panel_tv13);
        TextView panel_tv14 = (TextView) findViewById(R.id.panel_tv14);
        View layoutView1 =  findViewById(R.id.layoutView1);
        View layoutView2 =  findViewById(R.id.layoutView2);
        View layoutView3 =  findViewById(R.id.layoutView3);
        View layoutView4 =  findViewById(R.id.layoutView4);
        View layoutView5 =  findViewById(R.id.layoutView5);
        View layoutView6 =  findViewById(R.id.layoutView6);
        View layoutView7 =  findViewById(R.id.layoutView7);
        View colorView1 =  findViewById(R.id.colorView1);
        View colorView2 =  findViewById(R.id.colorView2);
        View colorView3 =  findViewById(R.id.colorView3);
        View colorView4 =  findViewById(R.id.colorView4);
        View colorView5 =  findViewById(R.id.colorView5);
        View colorView6 =  findViewById(R.id.colorView6);
        View colorView7 =  findViewById(R.id.colorView7);
        mAllKeyViews = new HashMap<Integer, View>();
        mAllKeyTextViews1 = new HashMap<Integer, TextView>();
        mAllKeyTextViews2 = new HashMap<Integer, TextView>();
        mAllKeyLayoutViews = new HashMap<Integer, View>();
        mAllKeyColorViews = new HashMap<Integer, View>();
        mAllKeyViews.put(1, panel_v1);
        mAllKeyViews.put(2, panel_v2);
        mAllKeyViews.put(3, panel_v3);
        mAllKeyViews.put(4, panel_v4);
        mAllKeyViews.put(5, panel_v5);
        mAllKeyViews.put(6, panel_v6);
        mAllKeyViews.put(7, panel_v7);
        mAllKeyTextViews1.put(1, panel_tv1);
        mAllKeyTextViews1.put(2, panel_tv3);
        mAllKeyTextViews1.put(3, panel_tv5);
        mAllKeyTextViews1.put(4, panel_tv7);
        mAllKeyTextViews1.put(5, panel_tv9);
        mAllKeyTextViews1.put(6, panel_tv11);
        mAllKeyTextViews1.put(7, panel_tv13);
        mAllKeyTextViews2.put(1, panel_tv2);
        mAllKeyTextViews2.put(2, panel_tv4);
        mAllKeyTextViews2.put(3, panel_tv6);
        mAllKeyTextViews2.put(4, panel_tv8);
        mAllKeyTextViews2.put(5, panel_tv10);
        mAllKeyTextViews2.put(6, panel_tv12);
        mAllKeyTextViews2.put(7, panel_tv14);
        mAllKeyLayoutViews.put(1,layoutView1);
        mAllKeyLayoutViews.put(2,layoutView2);
        mAllKeyLayoutViews.put(3,layoutView3);
        mAllKeyLayoutViews.put(4,layoutView4);
        mAllKeyLayoutViews.put(5,layoutView5);
        mAllKeyLayoutViews.put(6,layoutView6);
        mAllKeyLayoutViews.put(7,layoutView7);
        mAllKeyColorViews.put(1,colorView1);
        mAllKeyColorViews.put(2,colorView2);
        mAllKeyColorViews.put(3,colorView3);
        mAllKeyColorViews.put(4,colorView4);
        mAllKeyColorViews.put(5,colorView5);
        mAllKeyColorViews.put(6,colorView6);
        mAllKeyColorViews.put(7,colorView7);
    }
}
