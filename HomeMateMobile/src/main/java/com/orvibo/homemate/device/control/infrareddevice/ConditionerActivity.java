package com.orvibo.homemate.device.control.infrareddevice;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.sharedPreferences.ConditionCache;
import com.orvibo.homemate.view.custom.CircularSeekBar;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 控制空调
 * Created by Allen on 2015/3/26.
 * modified by smagret on 2015/04/11
 */
public class ConditionerActivity extends BaseIrControlActivity implements CircularSeekBar.
        OnChangeTemperatureListener, IrButton.OnControlResultListener {
    private IrButton btnOpen, btnShutdown;
    private CircularSeekBar circularSeekBar;
    private IrButton tvTemperature;
    private ImageView ivModel, ivSweep, ivSpeed;
    private IrButton btnCold, btnHot, btnDehumidifier, btnLow, btnMiddle, btnHigh, btnSweep, btnStopSweep;

    private final int COLD_FLAG = 0;
    private final int HOT_FLAG = 1;
    private final int DEHUMIDIFIER_FLAG = 2;
    private final int LOW_FLAG = 3;
    private final int MIDDLE_FLAG = 4;
    private final int HIGH_FLAG = 5;
    private final int SWEEP_FLAG = 6;
    private final int STOPSWEEP_FLAG = 7;
    private int currentFlag = -1;
    private int last_temperature = 1;

    private DeviceIrDao mDeviceIrDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditioner);
        findViews();
        init();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
        last_temperature = ConditionCache.getConditionerTemp(mContext, deviceId);
        circularSeekBar.setTemperature(last_temperature);
        onLeanedTemperatureOrOnChangeTemperture(last_temperature, getResources().getColor(R.color.green));
        // ToastUtil.showToast("=======");
    }

    private void findViews() {
        btnOpen = (IrButton) findViewById(R.id.btnOpen);
        btnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        circularSeekBar.setGravity(Gravity.NO_GRAVITY);
        circularSeekBar.initStatus(ConditionerActivity.this, uid, userName, deviceId);
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
        mDeviceIrDao = new DeviceIrDao();
        final NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
        last_temperature = ConditionCache.getConditionerTemp(mContext, deviceId);
        circularSeekBar.setOnChangeTemperatureListener(this);
        circularSeekBar.setTemperature(last_temperature);
        tvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        tvTemperature.setKeyName(last_temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(last_temperature));
        circularSeekBar.setKeyName(last_temperature + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(last_temperature));
    }

    private void initData() {
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ConditionerActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {

        for (final IrButton irButton : irNoButtons) {
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            irButton.setOnControlResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irButton.onClick();
                    if (view.getTag() != null) {
                        currentFlag = Integer.parseInt(view.getTag().toString());
                    }
                }
            });
        }

    }

    private void refreshStatus(int statusFlag) {
        if (statusFlag == COLD_FLAG) {
            ivModel.setBackgroundResource(R.drawable.conditioner_cold);
        } else if (statusFlag == HOT_FLAG) {
            ivModel.setBackgroundResource(R.drawable.pic_heating_on);
        } else if (statusFlag == SWEEP_FLAG) {
            ivSweep.setBackgroundResource(R.drawable.conditioner_sweep);
        } else if (statusFlag == STOPSWEEP_FLAG) {
            ivSweep.setBackgroundResource(R.drawable.conditioner_stop_sweep);
        } else if (statusFlag == LOW_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_weak_wind_on);
        } else if (statusFlag == MIDDLE_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_medium_wind_on);
        } else if (statusFlag == HIGH_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_strong_wind_on);
        }

    }

    private void refreshModel(int statusFlag) {
        if (statusFlag == COLD_FLAG) {
            ivModel.setBackgroundResource(R.drawable.conditioner_cold);
        } else if (statusFlag == HOT_FLAG) {
            ivModel.setBackgroundResource(R.drawable.pic_heating_on);
        }

    }

    private void refreshSweep(int statusFlag) {
        if (statusFlag == SWEEP_FLAG) {
            ivSweep.setBackgroundResource(R.drawable.conditioner_sweep);
        } else if (statusFlag == STOPSWEEP_FLAG) {
            ivSweep.setBackgroundResource(R.drawable.conditioner_stop_sweep);
        }
    }

    private void refreshWindSpeed(int statusFlag) {
        if (statusFlag == LOW_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_weak_wind_on);
        } else if (statusFlag == MIDDLE_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_medium_wind_on);
        } else if (statusFlag == HIGH_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_strong_wind_on);
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
        onLeanedTemperatureOrOnChangeTemperture(temperature, color);
    }

    /**
     * 学习结束和界面的初始化过来
     *
     * @param temperature
     * @param color
     */
    private void onLeanedTemperatureOrOnChangeTemperture(int temperature, int color) {
        if (device != null) {
            //初始化当前设置温度的的颜色，如果没有学习显示灰色，已经学习取圆盘上的颜色给该温度
            DeviceIr deviceIr = mDeviceIrDao.selDeviceIr(currentMainUid, device.getDeviceId(), getCommand(temperature));
            if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0 && deviceIr.getCommand().equals(getCommand(temperature))) {
                tvTemperature.setTextColor(color);
            } else {
                tvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));
            }
            tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
            circularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
            circularSeekBar.setOrder(getCommand(temperature));
        }
    }

    @Override
    public void onResultTemperature(CircularSeekBar view, int temperature, int color) {
        tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
        //  tvTemperature.setTextColor(color);
        DeviceIr deviceIr = mDeviceIrDao.selDeviceIr(currentMainUid, device.getDeviceId(), getCommand(temperature));
        if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0 && deviceIr.getCommand().equals(getCommand(temperature))) {
            tvTemperature.setTextColor(color);
        } else {
            tvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));
        }
        tvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(temperature));
        tvTemperature.onClick();
        circularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(temperature));
        ConditionCache.setConditionerTemp(mContext, temperature, deviceId);
//        circularSeekBar.onClick();
    }

    @Override
    public void onControlResult(int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            refreshStatus(currentFlag);
        }
    }
}
