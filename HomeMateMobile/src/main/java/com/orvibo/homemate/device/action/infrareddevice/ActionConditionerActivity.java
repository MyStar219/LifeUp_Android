package com.orvibo.homemate.device.action.infrareddevice;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.device.action.BaseActionActivity;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.CircularSeekBar;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allen on 2015/3/26.
 * modified by smagret on 2015/04/11
 */
public class ActionConditionerActivity extends BaseActionActivity implements CircularSeekBar.
        OnChangeTemperatureListener, IrButton.OnCheckedResultListener {
    private IrButton btnOpen, btnShutdown;
    private CircularSeekBar circularSeekBar;
    private IrButton tvTemperature;
    private ImageView ivModel, ivSweep, ivSpeed;
    private IrButton btnCold, btnHot, btnDehumidifier, btnLow, btnMiddle, btnHigh, btnSweep, btnStopSweep;
    private NavigationGreenBar navigationBar;

    private DeviceIr deviceIr;
    private DeviceIrDao deviceIrDao;
    private List<IrButton> irNoButtons = new ArrayList<IrButton>();

    private final int DEFAULT_TEMPERATURE = 23;

    /**
     * 该按键是否被学习
     */
    private boolean IS_LEARNED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditioner);

        findViews();
        init();
        initData();
        initListener();
    }

    private void findViews() {
        btnOpen = (IrButton) findViewById(R.id.btnOpen);
        btnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        circularSeekBar.setGravity(Gravity.NO_GRAVITY);
        circularSeekBar.initStatus(ActionConditionerActivity.this, uid, userName, deviceId);
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
        navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
    }

    private void init() {
        circularSeekBar.setOnChangeTemperatureListener(this);
        circularSeekBar.setTemperature(DEFAULT_TEMPERATURE);
        tvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        tvTemperature.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(DEFAULT_TEMPERATURE));
        navigationBar.setRightText(getResources().getString(R.string.confirm));
        navigationBar.setRightTextVisibility(View.GONE);
    }

    private void initData() {
        deviceIrDao = new DeviceIrDao();
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ActionConditionerActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {
        for (final IrButton irButton : irNoButtons) {
            irButton.setOnCheckedResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (final IrButton irButton2 : irNoButtons) {
                        irButton2.onUnChecked();
                    }
                    irButton.onChecked();
                }
            });
        }
    }

    private String getCommand(int temp) {
        return "3110" + temp;
    }

    @Override
    public void onChangeTemperature(CircularSeekBar view, int temperature, int color) {
        tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setTextColor(color);
    }

    @Override
    public void onResultTemperature(CircularSeekBar view, int temperature, int color) {
        command = getCommand(temperature);
        String temperatureString = temperature + getString(R.string.conditioner_temperature_unit);
        keyName = temperatureString;
        tvTemperature.setText(temperatureString);
        tvTemperature.setTextColor(color);
        tvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(command);
        getIsLearned();
        tvTemperature.onChecked();
        if (!IS_LEARNED) {
            command = "";
            keyName = "";
        }
    }

    private void refreshTemperature(String order) {
        command = order;
        int temperature = getTemperatureByOrder(command);
        if (temperature > 15) {
            String temperatureString = temperature + getString(R.string.conditioner_temperature_unit);
            keyName = temperatureString;
            tvTemperature.setText(temperatureString);
            tvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
            tvTemperature.setOrder(command);
            circularSeekBar.setTemperature(temperature);
            getIsLearned();
            tvTemperature.onChecked();
        }
    }

    private int getTemperatureByOrder(String order) {
        String temperatureString = order.substring(4, 6);
        return Integer.parseInt(temperatureString);
    }

    @Override
    public void onCheckedResult(String keyName, String command, boolean is_learned) {
        IS_LEARNED = is_learned;
        if (is_learned) {
            this.command = command;
            this.keyName = keyName;
        } else {
            this.command = "";
            this.keyName = "";
        }
    }

    public void rightTitleClick(View view) {
        if (IS_LEARNED) {
            Intent intent = new Intent();
            action = new Action(deviceId, command, 0, 0, 0, 0, keyName);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKey.ACTION, action);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            this.finish();
        } else {
            String message = getResources().getString(R.string.select_learned_key_tips);
            ToastUtil.showToast(
                    message,
                    ToastType.ERROR, ToastType.SHORT);
        }
    }

    private void getIsLearned() {
        deviceIr = deviceIrDao.selDeviceIr(uid, deviceId, command);
        if (deviceIr != null && deviceIr.getIr().length > 0) {
            IS_LEARNED = true;
        } else {
            IS_LEARNED = false;
            String message = getResources().getString(R.string.ir_key_not_learned);
            ToastUtil.showToast(
                    message,
                    ToastType.ERROR, ToastType.SHORT);
        }
    }

    /**
     * 已被选中的状态
     *
     * @param action
     */
    @Override
    protected void onSelectedAction(Action action) {
        String order = action.getCommand();
        for (final IrButton irButton : irNoButtons) {
            if (irButton.getOrder().equals(order)) {
                irButton.onChecked();
            }
        }
        if (!TextUtils.isEmpty(order))
            refreshTemperature(order);
    }
}
