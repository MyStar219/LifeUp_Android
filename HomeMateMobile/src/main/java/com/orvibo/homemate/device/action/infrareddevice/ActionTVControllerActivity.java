package com.orvibo.homemate.device.action.infrareddevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.action.BaseActionActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smagret on 2015/04/13
 */

public class ActionTVControllerActivity extends BaseActionActivity implements IrButton.OnCheckedResultListener {
    private static final String TAG = ActionTVControllerActivity.class
            .getSimpleName();
    private IrButton irButton0;
    private IrButton irButton1;
    private IrButton irButton2;
    private IrButton irButton3;
    private IrButton irButton4;
    private IrButton irButton5;
    private IrButton irButton6;
    private IrButton irButton7;
    private IrButton irButton8;
    private IrButton irButton9;
    private IrButton irButtonSilence;
    private IrButton irButtonPower;
    private IrButton irButtonMenu;
    private IrButton irButtonBack;
    private IrButton irButtonVolumeAdd;
    private IrButton irButtonVolumeMinus;
    private IrButton irButtonProgramAdd;
    private IrButton irButtonProgramMinus;
    private IrButton irButtonUp;
    private IrButton irButtonDown;
    private IrButton irButtonLeft;
    private IrButton irButtonRight;
    private IrButton irButtonOk;
    private List<IrButton> irNoButtons = new ArrayList<IrButton>();
    private NavigationGreenBar navigationBar;

    /**
     * 该按键是否被学习
     */
    private boolean IS_LEARNED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_controller);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        irButton0 = (IrButton) findViewById(R.id.irButton0);
        irButton1 = (IrButton) findViewById(R.id.irButton1);
        irButton2 = (IrButton) findViewById(R.id.irButton2);
        irButton3 = (IrButton) findViewById(R.id.irButton3);
        irButton4 = (IrButton) findViewById(R.id.irButton4);
        irButton5 = (IrButton) findViewById(R.id.irButton5);
        irButton6 = (IrButton) findViewById(R.id.irButton6);
        irButton7 = (IrButton) findViewById(R.id.irButton7);
        irButton8 = (IrButton) findViewById(R.id.irButton8);
        irButton9 = (IrButton) findViewById(R.id.irButton9);
        irButtonSilence = (IrButton) findViewById(R.id.irMuteSilence);
        irButtonPower = (IrButton) findViewById(R.id.irButtonPower);
        irButtonMenu = (IrButton) findViewById(R.id.irButtonMenu);
        irButtonBack = (IrButton) findViewById(R.id.irButtonBack);
        irButtonVolumeAdd = (IrButton) findViewById(R.id.irButtonVolumeAdd);
        irButtonVolumeMinus = (IrButton) findViewById(R.id.irButtonVolumeMinus);
        irButtonProgramAdd = (IrButton) findViewById(R.id.irButtonChannelAdd);
        irButtonProgramMinus = (IrButton) findViewById(R.id.irButtonChannelMinus);
        irButtonUp = (IrButton) findViewById(R.id.irButtonUp);
        irButtonDown = (IrButton) findViewById(R.id.irButtonDown);
        irButtonLeft = (IrButton) findViewById(R.id.irButtonLeft);
        irButtonRight = (IrButton) findViewById(R.id.irButtonRight);
        irButtonOk = (IrButton) findViewById(R.id.irButtonOk);

        irNoButtons.add(irButton0);
        irNoButtons.add(irButton1);
        irNoButtons.add(irButton2);
        irNoButtons.add(irButton3);
        irNoButtons.add(irButton4);
        irNoButtons.add(irButton5);
        irNoButtons.add(irButton6);
        irNoButtons.add(irButton7);
        irNoButtons.add(irButton8);
        irNoButtons.add(irButton9);
        irNoButtons.add(irButtonSilence);
        irNoButtons.add(irButtonPower);
        irNoButtons.add(irButtonMenu);
        irNoButtons.add(irButtonBack);
        irNoButtons.add(irButtonVolumeAdd);
        irNoButtons.add(irButtonVolumeMinus);
        irNoButtons.add(irButtonProgramAdd);
        irNoButtons.add(irButtonProgramMinus);
        irNoButtons.add(irButtonUp);
        irNoButtons.add(irButtonDown);
        irNoButtons.add(irButtonLeft);
        irNoButtons.add(irButtonRight);
        irNoButtons.add(irButtonOk);

        navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
    }

    private void initData() {
        navigationBar.setRightText(getResources().getString(R.string.confirm));
        navigationBar.setRightTextVisibility(View.GONE);

        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ActionTVControllerActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {
        for (final IrButton irButton : irNoButtons) {
            irButton.setOnCheckedResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (irButton.isLearned()) {
                    for (final IrButton irButton2 : irNoButtons) {
                        irButton2.onUnChecked();
                    }
                    irButton.onChecked();
//                    } else {
//                        String message = getResources().getString(R.string.ir_key_not_learned);
//                        ToastUtil.showToast(
//                                message,
//                                ToastType.ERROR, ToastType.SHORT);
//                    }
                }
            });
        }
    }

    public void rightTitleClick(View view) {
        if (IS_LEARNED) {
            Intent intent = new Intent();
            action = new Action(deviceId, command, 0, 0, 0, 0, keyName);
            Bundle bundle = new Bundle();
            bundle.putSerializable("action", action);
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

    /**
     * 已被选中的状态
     *
     * @param action
     */
    @Override
    protected void onSelectedAction(Action action) {
        LogUtil.d(TAG, "onSelectedAction()-action:" + action);
        String order = action.getCommand();
        for (final IrButton irButton : irNoButtons) {
            if (irButton.getOrder().equals(order)) {
                irButton.onChecked();
            }
        }
    }
}
