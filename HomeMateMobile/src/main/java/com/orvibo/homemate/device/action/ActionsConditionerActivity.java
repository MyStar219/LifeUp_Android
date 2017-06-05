//package com.orvibo.homemate.device.action;
//
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.View;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Action;
//import com.orvibo.homemate.bo.DeviceIr;
//import com.orvibo.homemate.dao.DeviceIrDao;
//import com.orvibo.homemate.data.IntentKey;
//import com.orvibo.homemate.data.ToastType;
//import com.orvibo.homemate.util.ToastUtil;
//import com.orvibo.homemate.view.custom.CircularSeekBar;
//import com.orvibo.homemate.view.custom.IrButton;
//import com.orvibo.homemate.view.custom.NavigationGreenBar;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Allen on 2015/3/26.
// * modified by smagret on 2015/04/11
// */
//public class ActionsConditionerActivity extends BaseActionsActivity implements CircularSeekBar.
//        OnChangeTemperatureListener, IrButton.OnCheckedResultListener {
//    private IrButton btnOpen, btnShutdown;
//    private CircularSeekBar circularSeekBar;
//    private IrButton tvTemperature;
//    private IrButton btnCold, btnHot, btnDehumidifier, btnLow, btnMiddle, btnHigh, btnSweep, btnStopSweep;
//    private NavigationGreenBar navigationBar;
//
//    private DeviceIr deviceIr;
//    private DeviceIrDao deviceIrDao;
//    private List<IrButton> irNoButtons = new ArrayList<IrButton>();
//
//    private final int COLD_FLAG = 0;
//    private final int HOT_FLAG = 1;
//    private final int DEHUMIDIFIER_FLAG = 2;
//    private final int LOW_FLAG = 3;
//    private final int MIDDLE_FLAG = 4;
//    private final int HIGH_FLAG = 5;
//    private final int SWEEP_FLAG = 6;
//    private final int STOPSWEEP_FLAG = 7;
//    private int currentFlag = -1;
//    private final int DEFAULT_TEMPERATURE = 23;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_conditioner);
//
//        findViews();
//        init();
//        initData();
//        initListener();
//    }
//
//    private void findViews() {
//        btnOpen = (IrButton) findViewById(R.id.btnOpen);
//        btnShutdown = (IrButton) findViewById(R.id.btnShutdown);
//        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
//        circularSeekBar.setGravity(Gravity.NO_GRAVITY);
//        circularSeekBar.initStatus(ActionsConditionerActivity.this, uid, userName, deviceId);
//        tvTemperature = (IrButton) findViewById(R.id.tvTemperature);
//        btnCold = (IrButton) findViewById(R.id.btnCold);
//        btnHot = (IrButton) findViewById(R.id.btnHot);
//        btnDehumidifier = (IrButton) findViewById(R.id.btnDehumidifier);
//        btnLow = (IrButton) findViewById(R.id.btnLow);
//        btnMiddle = (IrButton) findViewById(R.id.btnMiddle);
//        btnHigh = (IrButton) findViewById(R.id.btnHigh);
//        btnSweep = (IrButton) findViewById(R.id.btnSweep);
//        btnStopSweep = (IrButton) findViewById(R.id.btnStopSweep);
//        irNoButtons.add(btnOpen);
//        irNoButtons.add(btnShutdown);
//        irNoButtons.add(tvTemperature);
//        irNoButtons.add(btnCold);
//        irNoButtons.add(btnHot);
//        irNoButtons.add(btnDehumidifier);
//        irNoButtons.add(btnLow);
//        irNoButtons.add(btnMiddle);
//        irNoButtons.add(btnHigh);
//        irNoButtons.add(btnSweep);
//        irNoButtons.add(btnStopSweep);
//        navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
//    }
//
//    private void init() {
//        circularSeekBar.setOnChangeTemperatureListener(this);
//        circularSeekBar.setTemperature(DEFAULT_TEMPERATURE);
//        tvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
//        tvTemperature.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
//        tvTemperature.setOrder(getCommand(DEFAULT_TEMPERATURE));
//        navigationBar.setRightText(getResources().getString(R.string.confirm));
//    }
//
//    private void initData() {
//        deviceIrDao = new DeviceIrDao();
//        for (final IrButton irButton : irNoButtons) {
//            irButton.initStatus(ActionsConditionerActivity.this, uid, userName, deviceId);
//        }
//    }
//
//    private void initListener() {
//        for (final IrButton irButton : irNoButtons) {
//            irButton.setOnCheckedResultListener(this);
//            irButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    for (final IrButton irButton2 : irNoButtons) {
//                        irButton2.onUnChecked();
//                    }
//                    irButton.onChecked();
//                    if (view.getTag() != null) {
//                        currentFlag = Integer.parseInt(view.getTag().toString());
//                    }
//                }
//            });
//        }
//    }
//
//    private String getCommand(int temp) {
//        return "3110" + temp;
//    }
//
//    @Override
//    public void onChangeTemperature(CircularSeekBar view, int temperature, int color) {
//        tvTemperature.setText(temperature + getString(R.string.conditioner_temperature_unit));
//        tvTemperature.setTextColor(color);
//    }
//
//    @Override
//    public void onResultTemperature(CircularSeekBar view, int temperature, int color) {
//        String command = getCommand(temperature);
//        String temperatureString = temperature + getString(R.string.conditioner_temperature_unit);
//        String keyName = temperatureString;
//        tvTemperature.setText(temperatureString);
//        tvTemperature.setTextColor(color);
//        tvTemperature.setKeyName(keyName);
//        tvTemperature.setOrder(command);
//        getIsLearned(command);
//        tvTemperature.onChecked();
//    }
//
//    private void refreshTemperature(String order) {
//        //    command = order;
//        int temperature = getTemperatureByOrder(order);
//        if (temperature > 15) {
//            String temperatureString = temperature + getString(R.string.conditioner_temperature_unit);
//            String keyName = temperatureString;
//            tvTemperature.setText(temperatureString);
//            tvTemperature.setKeyName(keyName);
//            tvTemperature.setOrder(order);
//            circularSeekBar.setTemperature(temperature);
//            getIsLearned(order);
//            tvTemperature.onChecked();
//        }
//    }
//
//    private int getTemperatureByOrder(String order) {
//        String temperatureString = order.substring(4, 6);
//        return Integer.parseInt(temperatureString);
//    }
//
//    //同一时间不能选择多个动作
//    @Override
//    public void onCheckedResult(String keyName, String command, boolean is_learned) {
//        if (is_learned) {
//            boolean isContainCommand = false;
//            if (actions != null && !actions.isEmpty()) {
//                for (Action action : actions) {
//                    if (command.equals(action.getCommand()) && action.getDelayTime() == 0) {
//                        isContainCommand = true;
//                        break;
//                    }
//                }
//            }
//            if (!isContainCommand) {
//                Action action = new Action(deviceId, command, 0, 0, 0, 0, keyName);
//                action.setUid(uid);
//                actions.add(action);
//            }
//        }
//    }
//
//    public void rightTitleClick(View view) {
//        // if (IS_LEARNED) {
//        Intent intent = new Intent();
//        //action = new Action(deviceId, command, 0, 0, 0, 0, keyName);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(IntentKey.ACTIONS, (Serializable) actions);
//        intent.putExtras(bundle);
//        setResult(RESULT_OK, intent);
//        this.finish();
////        } else {
////            String message = getResources().getString(R.string.select_learned_key_tips);
////            ToastUtil.showToast(mContext,
////                    message,
////                    ToastType.ERROR, ToastType.SHORT);
////        }
//    }
//
//    private void getIsLearned(String command) {
//        deviceIr = deviceIrDao.selDeviceIr(uid, deviceId, command);
//        if (deviceIr == null || deviceIr.getIr().length <= 0) {
//            String message = getResources().getString(R.string.ir_key_not_learned);
//            ToastUtil.showToast(
//                    message,
//                    ToastType.ERROR, ToastType.SHORT);
//        }
//    }
//
//    /**
//     * 已被选中的状态
//     *
//     * @param actions
//     */
//    @Override
//    protected void onSelectedActions(List<Action> actions) {
//        if (actions != null && !actions.isEmpty()) {
//            for (final IrButton irButton : irNoButtons) {
//                final String order = irButton.getOrder();
//                for (Action action : actions) {
//                    if (order.equals(action.getCommand())) {
//                        irButton.onChecked();
//                        refreshTemperature(order);
//                        break;
//                    }
//                }
//            }
//        }
//    }
//}
