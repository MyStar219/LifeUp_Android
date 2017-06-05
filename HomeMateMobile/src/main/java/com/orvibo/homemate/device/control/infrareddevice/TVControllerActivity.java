package com.orvibo.homemate.device.control.infrareddevice;

import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Allen on 2015/4/7.
 * Modified by smagret on 2015/04/13
 */

public class TVControllerActivity extends BaseIrControlActivity {
    private static final String TAG = TVControllerActivity.class
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_controller);
        initView();
        initData();
        initListener();
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(mContext);
//        refWatcher.watch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
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
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
    }

    private void initData() {
        LogUtil.d(TAG, "initData()-uid:" + uid + ",userName:" + userName + ",deviceId:" + deviceId);
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(TVControllerActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {
        for (final IrButton irButton : irNoButtons) {
            //长按后会触发短按，返回true屏蔽短按事件
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
}
