package com.orvibo.homemate.device.action;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allen on 2015/4/17.
 */
public class ActionLightActivity extends BaseActionActivity implements IrButton.OnCheckedResultListener {
    private IrButton irBtnSwitchOn, irBtnSwitchOff;
    private List<IrButton> irNoButtons = new ArrayList<IrButton>();
    private RelativeLayout rlAction;

    private NavigationGreenBar navigationGreenBar;
    private ImageView statusImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        setBindBar(NavigationType.greenType);
        initView();
        initData();
        initListener();
    }

    private void initView() {
//        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        irBtnSwitchOn = (IrButton) findViewById(R.id.btnSwitchOn);
        irBtnSwitchOff = (IrButton) findViewById(R.id.btnSwitchOff);
        irNoButtons.add(irBtnSwitchOn);
        irNoButtons.add(irBtnSwitchOff);
        rlAction = (RelativeLayout) findViewById(R.id.rlAction);
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        statusImageView = (ImageView) findViewById(R.id.statusImageView);
    }


    private void initListener() {
        for (final IrButton irButton : irNoButtons) {
            irButton.setOnCheckedResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irButton.onChecked();
                    if (irButton.getOrder().equals("on")) {
                        switchOn();
                    } else {
                        switchOff();
                    }
                }
            });
        }
    }

    private void initData() {
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ActionLightActivity.this, uid, userName, deviceId);
        }
    }

    private void switchOn() {
//        ivSwitch.setImageResource(R.drawable.switch_on_anim);
//        ((AnimationDrawable) ivSwitch.getDrawable()).start();
        irBtnSwitchOn.setVisibility(View.GONE);
        irBtnSwitchOff.setVisibility(View.VISIBLE);
        value1 = DeviceStatusConstant.ON;
//        navigationGreenBar.setBarColor(getResources().getColor(R.color.green));
//        navigationGreenBar.setBackViewWhite();
//        navigationGreenBar.setMiddleTextColor(getResources().getColor(R.color.white));
//        navigationGreenBar.setRightTextColor(getResources().getColor(R.color.white));
        statusImageView.setImageResource(R.drawable.socket_open_background);
        rlAction.setBackgroundResource(R.color.green);
    }

    private void switchOff() {
//        ivSwitch.setImageResource(R.drawable.switch_off_anim);
//        ((AnimationDrawable) ivSwitch.getDrawable()).start();
        irBtnSwitchOn.setVisibility(View.VISIBLE);
        irBtnSwitchOff.setVisibility(View.GONE);
        value1 = DeviceStatusConstant.OFF;
//        navigationGreenBar.setBarColor(getResources().getColor(R.color.common_background));
//        navigationGreenBar.setBackViewGreen();
//        navigationGreenBar.setMiddleTextColor(getResources().getColor(R.color.black));
//        navigationGreenBar.setRightTextColor(getResources().getColor(R.color.green));
        statusImageView.setImageResource(R.drawable.socket_close_background);
        rlAction.setBackgroundResource(R.color.common_background);
    }

    @Override
    public void onCheckedResult(String keyName, String order, boolean is_learned) {
        this.command = order;
        this.keyName = keyName;
    }

    /**
     * 已被选中的状态
     *
     * @param action
     */
    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        setAction(action);
        if (BindActionType.isSupportToggle(bindActionType)) {
            rlAction.setVisibility(View.GONE);
        } else {
            rlAction.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设备当前真实的状态
     *
     * @param deviceStatus
     * @see #onSelectedAction(Action)
     * @deprecated
     */
    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        setAction(deviceStatus);
    }

    /**
     * 设备默认的状态
     *
     * @param defaultAction
     * @see #onSelectedAction(Action)
     * @deprecated
     */
    @Override
    protected void onDefaultAction(Action defaultAction) {
        setAction(defaultAction);
    }

    private void setAction(Action action) {
        command = action.getCommand();
        keyName = action.getKeyName();
        value1 = action.getValue1();
        if (command != null && command.equals(DeviceOrder.ON)) {
            command = DeviceOrder.ON;
            switchOn();
        } else if (command != null && command.equals(DeviceOrder.OFF)) {
            command = DeviceOrder.OFF;
            switchOff();
        } else if (command != null && command.equals(DeviceOrder.TOGGLE)) {
            command = DeviceOrder.TOGGLE;
        }
    }

}
