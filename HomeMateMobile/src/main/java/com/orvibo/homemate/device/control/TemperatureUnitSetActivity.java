package com.orvibo.homemate.device.control;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.event.TmperatureUnitEvent;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import de.greenrobot.event.EventBus;

public class TemperatureUnitSetActivity extends BaseActivity {

    private com.orvibo.homemate.view.custom.NavigationGreenBar nbTitle;
    private android.widget.ImageView unitCheck1;
    private android.widget.RelativeLayout unit1;
    private android.widget.ImageView unitCheck2;
    private android.widget.RelativeLayout unit2;
    private String unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_unit_set);
        this.unit2 = (RelativeLayout) findViewById(R.id.unit2);
        this.unitCheck2 = (ImageView) findViewById(R.id.unitCheck2);
        this.unit1 = (RelativeLayout) findViewById(R.id.unit1);
        this.unitCheck1 = (ImageView) findViewById(R.id.unitCheck1);
        this.nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        unit = CommonCache.getTemperatureUnit();
        if (CommonCache.getTemperatureUnit().equalsIgnoreCase(getString(R.string.conditioner_temperature_unit))) {
            saveUnit(1);
        } else {
            saveUnit(2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unit1:
                saveUnit(1);
                break;
            case R.id.unit2:
                saveUnit(2);
                break;
        }
    }

    /**
     * 点击保存温度单位
     *
     * @param positon
     */
    public void saveUnit(int positon) {
        if (positon == 1) {
            unitCheck1.setVisibility(View.VISIBLE);
            unitCheck2.setVisibility(View.GONE);
            CommonCache.setTemperatureUnit(getString(R.string.conditioner_temperature_unit));
        } else {
            unitCheck1.setVisibility(View.GONE);
            unitCheck2.setVisibility(View.VISIBLE);
            CommonCache.setTemperatureUnit(getString(R.string.conditioner_temperature_unit1));
        }
    }

    @Override
    protected void onDestroy() {
        if (!unit.equalsIgnoreCase(CommonCache.getTemperatureUnit())) {
            EventBus.getDefault().post(new TmperatureUnitEvent(CommonCache.getTemperatureUnit()));
        }
        super.onDestroy();
    }
}
