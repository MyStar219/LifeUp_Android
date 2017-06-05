package com.orvibo.homemate.device.irlearn;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.device.control.BaseIrActivity;
import com.orvibo.homemate.view.custom.CircularSeekBar;
import com.orvibo.homemate.view.custom.IrButton;

import java.util.ArrayList;
import java.util.List;

//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by smagret on 2015/04/11
 */
public class ConditionerIrLearnActivity extends BaseIrActivity implements CircularSeekBar.
        OnChangeTemperatureListener, IrButton.OnLearningResultListener {
    private IrButton btnOpen, btnShutdown;
    private CircularSeekBar circularSeekBar;
    private IrButton tvTemperature;
    private ImageView ivModel, ivSweep, ivSpeed;
    private IrButton btnCold, btnHot, btnDehumidifier, btnLow, btnMiddle, btnHigh, btnSweep, btnStopSweep;
    private List<IrButton> irNoButtons = new ArrayList<IrButton>();
    private final int DEFAULT_TEMPERATURE = 23;
    private DeviceIrDao mDeviceIrDao;
    private int current_tem;
    private int current_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditioner);
        findViews();
        init();
        initData();
        initListener();
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(mContext);
//        refWatcher.watch(ConditionerIrLearnActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // circularSeekBar.setTemperature(DEFAULT_TEMPERATURE);
    }

    private void findViews() {
        btnOpen = (IrButton) findViewById(R.id.btnOpen);
        btnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        circularSeekBar.setGravity(Gravity.NO_GRAVITY);
        circularSeekBar.setIsLeanMode(true);
        tvTemperature = (IrButton) findViewById(R.id.tvTemperature);
        ivModel = (ImageView) findViewById(R.id.ivModel);
        ivSweep = (ImageView) findViewById(R.id.ivSweep);
        ivSpeed = (ImageView) findViewById(R.id.ivSpeed);
        btnCold = (IrButton) findViewById(R.id.btnCold);
        btnHot = (IrButton) findViewById(R.id.btnHot);
        btnDehumidifier = (IrButton) findViewById(R.id.btnDehumidifier);
        btnLow = (IrButton) findViewById(R.id.btnLow);
        btnMiddle = (IrButton) findViewById(R.id.btnMiddle);
        btnHigh = (IrButton) findViewById(R.id.btnHigh);
        btnSweep = (IrButton) findViewById(R.id.btnSweep);
        btnStopSweep = (IrButton) findViewById(R.id.btnStopSweep);
        irNoButtons.add(btnOpen);
        irNoButtons.add(btnShutdown);
//        irNoButtons.add(circularSeekBar);
        irNoButtons.add(tvTemperature);
        irNoButtons.add(btnCold);
        irNoButtons.add(btnHot);
        irNoButtons.add(btnDehumidifier);
        irNoButtons.add(btnLow);
        irNoButtons.add(btnMiddle);
        irNoButtons.add(btnHigh);
        irNoButtons.add(btnSweep);
        irNoButtons.add(btnStopSweep);
    }

    private void init() {
        setIrBar();
        //默认温度的颜色
        tvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));
        mDeviceIrDao = new DeviceIrDao();
        circularSeekBar.setOnChangeTemperatureListener(this);
        circularSeekBar.setOnLearningResultListener(this);
        circularSeekBar.setTemperature(DEFAULT_TEMPERATURE);
        circularSeekBar.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(DEFAULT_TEMPERATURE));
        circularSeekBar.initStatus(ConditionerIrLearnActivity.this, uid, userName, deviceId);
        tvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        tvTemperature.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(DEFAULT_TEMPERATURE));

    }

    private void initData() {
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ConditionerIrLearnActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {

        for (final IrButton irButton : irNoButtons) {
            irButton.setOnLearningResultListener(this);
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    irButton.onlongClick();
                    return true;
                }
            });
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irButton.onClick();
                }
            });
        }
    }

    private String getCommand(int temp) {
        return "3110" + temp;
    }

    @Override
    public void onChangeTemperature(CircularSeekBar view, int temperature, int color) {
//        if (tvTemperature.isLearned()) {
//            tvTemperature.setTextColor(color);
//        }
        onChangeTemperature(temperature, color);

    }

    private void onChangeTemperature(int temperature, int color) {
        current_tem = temperature;
        current_color = color;
        DeviceIr deviceIr = mDeviceIrDao.selDeviceIr(currentMainUid, device.getDeviceId(), getCommand(temperature));
        if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0 && deviceIr.getCommand().equals(getCommand(temperature))) {
            tvTemperature.setTextColor(color);
        } else {
            tvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));
        }
        tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(temperature));
        tvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(temperature));
    }

    @Override
    public void onResultTemperature(CircularSeekBar view, int temperature, int color) {
        current_tem = temperature;
        current_color = color;
        tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setTextColor(color);
        tvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(temperature));
        circularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(temperature));
        tvTemperature.onClick();

    }

    @Override
    public void onLearningResult(int result) {
        if (result == ErrorCode.SUCCESS) {
            DeviceIr deviceIr = mDeviceIrDao.selDeviceIr(currentMainUid, device.getDeviceId(), getCommand(current_tem));
            if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0 && deviceIr.getCommand().equals(getCommand(current_tem))) {
                tvTemperature.setTextColor(current_color);
            }
        }
    }

//    @Override
//    public void onLearningResult(int result, int color, int temperature) {
//        DeviceIr deviceIr = mDeviceIrDao.selDeviceIr(currentMainUid, device.getDeviceId(), getCommand(temperature));
//        if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0 && deviceIr.getCommand().equals(getCommand(temperature))) {
//            tvTemperature.setTextColor(color);
//        }
//
//    }
//

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
