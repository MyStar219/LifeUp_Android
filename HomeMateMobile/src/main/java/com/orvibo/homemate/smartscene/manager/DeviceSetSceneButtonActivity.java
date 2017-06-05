package com.orvibo.homemate.smartscene.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.bind.BaseSelectKeyNoActivity;
import com.orvibo.homemate.data.IntentKey;

import java.util.HashMap;

/**
 * 情景面板绑定界面
 * Created by Allen on 2015/4/23.
 *
 * @intent {@link IntentKey#DEVICE}情景面板对象
 */
public class DeviceSetSceneButtonActivity extends BaseSelectKeyNoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_scene);
        initKeyViews();
//        initBound();

        setTitle(getString(R.string.device_set_panel_title));
    }

    private void initKeyViews() {
        View panel_v1 =  findViewById(R.id.panel_v1);
        View panel_v2 =  findViewById(R.id.panel_v2);
        View panel_v3 =  findViewById(R.id.panel_v3);
        TextView panel_tv1 = (TextView) findViewById(R.id.panel_tv1);
        TextView panel_tv2 = (TextView) findViewById(R.id.panel_tv2);
        TextView panel_tv3 = (TextView) findViewById(R.id.panel_tv3);
        TextView panel_tv4 = (TextView) findViewById(R.id.panel_tv4);
        TextView panel_tv5 = (TextView) findViewById(R.id.panel_tv5);
        TextView panel_tv6 = (TextView) findViewById(R.id.panel_tv6);
        View layoutView1 =  findViewById(R.id.layoutView1);
        View layoutView2 =  findViewById(R.id.layoutView2);
        View layoutView3 =  findViewById(R.id.layoutView3);
        View colorView1 =  findViewById(R.id.colorView1);
        View colorView2 =  findViewById(R.id.colorView2);
        View colorView3 =  findViewById(R.id.colorView3);
        mAllKeyViews = new HashMap<Integer, View>();
        mAllKeyTextViews1 = new HashMap<Integer, TextView>();
        mAllKeyTextViews2 = new HashMap<Integer, TextView>();
        mAllKeyLayoutViews = new HashMap<Integer, View>();
        mAllKeyColorViews = new HashMap<Integer, View>();
        mAllKeyViews.put(1, panel_v1);
        mAllKeyViews.put(2, panel_v2);
        mAllKeyViews.put(3, panel_v3);
        mAllKeyTextViews1.put(1,panel_tv1);
        mAllKeyTextViews1.put(2,panel_tv3);
        mAllKeyTextViews1.put(3,panel_tv5);
        mAllKeyTextViews2.put(1,panel_tv2);
        mAllKeyTextViews2.put(2,panel_tv4);
        mAllKeyTextViews2.put(3,panel_tv6);
        mAllKeyLayoutViews.put(1,layoutView1);
        mAllKeyLayoutViews.put(2,layoutView2);
        mAllKeyLayoutViews.put(3,layoutView3);
        mAllKeyColorViews.put(1,colorView1);
        mAllKeyColorViews.put(2,colorView2);
        mAllKeyColorViews.put(3,colorView3);
    }
}
