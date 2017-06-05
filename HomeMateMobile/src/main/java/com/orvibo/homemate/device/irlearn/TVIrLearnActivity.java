package com.orvibo.homemate.device.irlearn;

import android.os.Bundle;
import android.view.View;

import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.device.control.BaseIrActivity;
import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.IrButton;
//import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smagret on 2015/04/13.
 */
public class TVIrLearnActivity extends BaseIrActivity {


    private static final String TAG = TVIrLearnActivity.class
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_controller);
        //init();
        initView();
        initData();
        initListener();
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(mContext);
//        refWatcher.watch(this);
    }

    private void initView() {
        setIrBar();
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
    }

    private void initData() {
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(TVIrLearnActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {

        for (final IrButton irButton : irNoButtons) {
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
}
