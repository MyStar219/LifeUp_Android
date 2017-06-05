package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Create By smagret 2015/04/20
 */
public class Curtain2HalvesView extends LinearLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private OnProgressChangedListener onProgressChangedListener;
    private OnActionClickListener     onActionClickListener;
    private int                       startProgress;//初始进度

    private Curtain2HalvesInsideView curtain2HalvesInsideView;
    private SeekBar                  seekBar;
    private View                     mode_ll;
    private boolean              IS_ACTION_CONTROL = false;
    private List<FrequentlyMode> frequentlyModes   = new ArrayList<FrequentlyMode>();
    private TextView frequentlyModeOne, frequentlyModeTwo, frequentlyModeThree, frequentlyModeFour, action_on, action_stop, action_off;
//    private HashMap<Integer, TextView> textViewHashMap = new HashMap<>();

    public Curtain2HalvesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(View.inflate(context, R.layout.curtain_2halves_view, null));
        initView();
        init();
    }

    private void initView() {
        curtain2HalvesInsideView = (Curtain2HalvesInsideView) findViewById(R.id.curtain2HalvesInsideView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        mode_ll = findViewById(R.id.mode_ll);
        frequentlyModeOne = (TextView) findViewById(R.id.frequentlyModeOne);
        frequentlyModeOne.setOnClickListener(this);
        frequentlyModeTwo = (TextView) findViewById(R.id.frequentlyModeTwo);
        frequentlyModeTwo.setOnClickListener(this);
        frequentlyModeThree = (TextView) findViewById(R.id.frequentlyModeThree);
        frequentlyModeThree.setOnClickListener(this);
        frequentlyModeFour = (TextView) findViewById(R.id.frequentlyModeFour);
        frequentlyModeFour.setOnClickListener(this);
        action_on = (TextView) findViewById(R.id.action_on);
        action_on.setOnClickListener(this);
        action_stop = (TextView) findViewById(R.id.action_stop);
        action_stop.setOnClickListener(this);
        action_off = (TextView) findViewById(R.id.action_off);
        action_off.setOnClickListener(this);
//        textViewHashMap.put(frequentlyModes.get(0).getValue1(), frequentlyModeOne);
//        textViewHashMap.put(frequentlyModes.get(0).getValue2(), frequentlyModeTwo);
//        textViewHashMap.put(frequentlyModes.get(0).getValue3(), frequentlyModeThree);
//        textViewHashMap.put(frequentlyModes.get(0).getValue4(), frequentlyModeFour);
//        textViewHashMap.put(DeviceStatusConstant.CURTAIN_ON, action_on);
//        textViewHashMap.put(DeviceStatusConstant.CURTAIN_STOP, action_stop);
//        textViewHashMap.put(DeviceStatusConstant.CURTAIN_OFF, action_off);
    }

    public void setSeekBarVisible() {
        seekBar.setVisibility(View.VISIBLE);
        mode_ll.setVisibility(View.GONE);
    }

    public void setSeekBarGone() {
        seekBar.setVisibility(View.GONE);
        mode_ll.setVisibility(View.VISIBLE);
    }

//    public void setStopButtonEnable() {
//        action_stop.setEnabled(true);
//        action_stop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop, 0, 0);
//    }
//
//    public void setStopButtonDisable() {
//        action_stop.setEnabled(false);
//        action_stop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop_disabled, 0, 0);
//    }

    public void setStopButtonVisible() {
        action_stop.setVisibility(View.VISIBLE);
    }

    public void setStopButtonGone() {
        action_stop.setVisibility(View.GONE);
    }

    public void setOpenButtonEnable() {
        action_on.setEnabled(true);
        action_on.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
    }

    public void setOpenButtonDisable() {
        action_on.setEnabled(false);
        action_on.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open_disabled, 0, 0);
    }

    public void setCloseButtonEnable() {
        action_off.setEnabled(true);
        action_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close, 0, 0);
    }

    public void setCloseButtonDisable() {
        action_off.setEnabled(false);
        action_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_disabled, 0, 0);
    }

//    public void setButtonChecked(Action action) {
//        if (action == null) {
//            return;
//        }
//        String order = action.getCommand();
//        int value1 = action.getValue1();
//
//        if (order.equals(DeviceOrder.OPEN)) {
//            if (value1 == DeviceStatusConstant.CURTAIN_ON) {
//
//
//            } else if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
//
//            } else {
//
//            }
//
//        } else if (order.equals(DeviceOrder.STOP)) {
//        }
//    }

    public void setFrequentlyModes(List<FrequentlyMode> frequentlyModes) {
        this.frequentlyModes = frequentlyModes;
        frequentlyModeOne.setText(frequentlyModes.get(0).getName());
        frequentlyModeTwo.setText(frequentlyModes.get(1).getName());
        frequentlyModeThree.setText(frequentlyModes.get(2).getName());
        frequentlyModeFour.setText(frequentlyModes.get(3).getName());
    }

    private void init() {
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setActionControl() {
        IS_ACTION_CONTROL = true;
    }


    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
        if (IS_ACTION_CONTROL) {
            curtain2HalvesInsideView.setProgress(progress * 100 / seekBar.getMax());
        }
        onProgressChangedListener.onProgressChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        onProgressChangedListener.onProgressFinish(seekBar.getProgress());
    }

    /**
     * 根据进度百分比，绘制窗帘布
     *
     * @param progress 进度百分比0-100
     */
    public void setProgress(int progress) {
        if (progress >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
            curtain2HalvesInsideView.setProgress(DeviceStatusConstant.CURTAIN_ON);
            seekBar.setProgress(DeviceStatusConstant.CURTAIN_ON);
        } else if (progress <= DeviceStatusConstant.CURTAIN_STATUS_OFF) {
            curtain2HalvesInsideView.setProgress(DeviceStatusConstant.CURTAIN_OFF);
            seekBar.setProgress(DeviceStatusConstant.CURTAIN_OFF);
        } else {
            curtain2HalvesInsideView.setProgress(progress * 100 / seekBar.getMax());
            seekBar.setProgress(progress * 100 / seekBar.getMax());
        }
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        onProgressChangedListener = listener;
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.action_on:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, DeviceStatusConstant.CURTAIN_ON);
                break;
            case R.id.action_stop:
                onActionClickListener.onActionClick(DeviceOrder.STOP, DeviceStatusConstant.CURTAIN_STOP);
                break;
            case R.id.action_off:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, DeviceStatusConstant.CURTAIN_OFF);
                break;
            case R.id.frequentlyModeOne:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, frequentlyModes.get(0).getValue1());
                break;
            case R.id.frequentlyModeTwo:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, frequentlyModes.get(1).getValue1());
                break;
            case R.id.frequentlyModeThree:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, frequentlyModes.get(2).getValue1());
                break;
            case R.id.frequentlyModeFour:
                onActionClickListener.onActionClick(DeviceOrder.OPEN, frequentlyModes.get(3).getValue1());
                break;
        }
    }


    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);

        void onProgressFinish(int progress);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        onActionClickListener = listener;
    }

    public interface OnActionClickListener {
        void onActionClick(String order, int progress);
    }
}
