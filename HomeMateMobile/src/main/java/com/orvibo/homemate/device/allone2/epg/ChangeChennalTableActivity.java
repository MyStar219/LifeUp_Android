package com.orvibo.homemate.device.allone2.epg;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;

/**
 * Created by yuwei on 2016/4/11.
 */
public class ChangeChennalTableActivity extends BaseAlloneControlActivity implements CompoundButton.OnCheckedChangeListener{

    private CheckBox cb_high_chennal,cb_pay_chennal,cb_high_definition_first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_chennal_table);

        initView();
    }

    private void initView(){
        cb_high_chennal = (CheckBox) findViewById(R.id.cb_high_chennal);
        cb_pay_chennal = (CheckBox) findViewById(R.id.cb_pay_chennal);
        cb_high_definition_first = (CheckBox) findViewById(R.id.cb_high_definition_first);

        cb_high_chennal.setOnCheckedChangeListener(this);
        cb_pay_chennal.setOnCheckedChangeListener(this);
        cb_high_definition_first.setOnCheckedChangeListener(this);

        cb_high_chennal.setChecked(AlloneStbCache.getNeedToGetHDProgram(deviceId));
        cb_pay_chennal.setChecked(AlloneStbCache.getIsPayProgram(deviceId));
        cb_high_definition_first.setChecked(AlloneStbCache.getIsProgramHDFirst(deviceId));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.cb_high_chennal:
                //是否筛选高清频道
                AlloneStbCache.setNeedToHDProgram(deviceId,isChecked);
                break;
            case R.id.cb_pay_chennal:
                //是否筛选付费频道
                AlloneStbCache.setIsPayProgram(deviceId,isChecked);
                break;
            case R.id.cb_high_definition_first:
                //是否高清优先
                AlloneStbCache.setIsProgramHDFirst(deviceId,isChecked);
                break;
        }
    }
}
