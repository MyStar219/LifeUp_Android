package com.orvibo.homemate.device.action;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.CurtainControlOldView;
import com.orvibo.homemate.view.custom.CurtainRollerView;

/**
 * 卷帘、幕布、百叶窗选择动作
 * 其中百叶窗在切换动作时CurtainDropdownView 效果不会变化
 * Created by smagret on 2015/04/18
 * Modify  by wuliquan on 2016/06/22
 */
public class ActionCurtainRollerOldActivity extends BaseActionActivity implements CurtainControlOldView.OnStatusChangedListener {
    private static final String TAG = ActionCurtainRollerOldActivity.class
            .getSimpleName();
    private CurtainRollerView     curtainDropdownView;
    private CurtainControlOldView curtainControlOldView;
    private LinearLayout curtainWindowShadesBottom;
    private TextView pageUpTextView,pageDownTextView;
    private int                   deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_dropdown_old);
        init();
    }

    private void init() {
        initView();
        initListener();
        initData();
    }

    private void initView() {
        curtainDropdownView = (CurtainRollerView) findViewById(R.id.curtainDropdownView);
        curtainDropdownView.setTouchable(false);
        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
        curtainWindowShadesBottom = (LinearLayout)findViewById(R.id.curtainWindowShadesBottom);
        pageUpTextView=(TextView)findViewById(R.id.pageUpTextView);
        pageDownTextView=(TextView)findViewById(R.id.pageDownTextView);
        pageUpTextView.setOnClickListener(this);
        pageDownTextView.setOnClickListener(this);
//        if (bindType == BindType.NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(true);
//        } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(false);
//        }
        setBindBar(NavigationType.greenType);
    }

    private void initListener() {
        curtainControlOldView.setOnStatusChangedListener(this);
    }

    private void initData() {
        if (device != null) {
            deviceType = device.getDeviceType();
            if(deviceType==DeviceType.WINDOW_SHADES){
                curtainDropdownView.setProgress(0);
                curtainWindowShadesBottom.setVisibility(View.VISIBLE);
            }else{
                curtainWindowShadesBottom.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId()==R.id.pageUpTextView){
            curtainControlOldView.initStatus();
            pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_press, 0, 0);
            pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
            command = DeviceOrder.CURTAIN_PAGE_UP;
            value1 = 0;
        }else if(v.getId()==R.id.pageDownTextView){
            curtainControlOldView.initStatus();
            pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
            pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_press, 0, 0);
            command = DeviceOrder.CURTAIN_PAGE_DOWN;
            value1 = 0;
        }
    }

    @Override
    protected void onSelectedAction(Action action) {
        value1 = action.getValue1();
        setStatus(value1);
        setPageTextStatus(action);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        value1 = deviceStatus.getValue1();
        setStatus(value1);
        setPageTextStatus(deviceStatus);
    }

    @Override
    protected void onDefaultAction(Action defaultAction) {
        super.onDefaultAction(defaultAction);
        value1 = defaultAction.getValue1();
        setStatus(value1);
        setPageTextStatus(defaultAction);
    }
    private void setPageTextStatus(Action action){
        if(deviceType==DeviceType.WINDOW_SHADES){
            if(action.getCommand().equals(DeviceOrder.CURTAIN_PAGE_UP)){
                curtainControlOldView.initStatus();
                pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_press, 0, 0);
                pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
            }else if(action.getCommand().equals(DeviceOrder.CURTAIN_PAGE_DOWN)){
                curtainControlOldView.initStatus();
                pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
                pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_press, 0, 0);
            }
        }
    }
    private void setStatus(int value1) {
        LogUtil.d(TAG, "setStatus()-value1:" + value1);
        if(deviceType==DeviceType.WINDOW_SHADES) {
            pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
            pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
        }
        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            command = DeviceOrder.CLOSE;
            if (deviceType == DeviceType.SCREEN) {
                curtainControlOldView.open();
            } else {
                curtainControlOldView.close();
            }
            if(deviceType!=DeviceType.WINDOW_SHADES)
            curtainDropdownView.setProgress(0);

        } else if (value1 == DeviceStatusConstant.CURTAIN_ON) {
            command = DeviceOrder.OPEN;
            if (deviceType == DeviceType.SCREEN) {
                curtainControlOldView.close();
            } else {
                curtainControlOldView.open();
            }
            if(deviceType!=DeviceType.WINDOW_SHADES)
            curtainDropdownView.setProgress(100);
        } else if (value1 == DeviceStatusConstant.CURTAIN_STOP) {
//            if (bindType == BindType.NEED_TOGGLE) {
            command = DeviceOrder.STOP;
            curtainControlOldView.stop();
            if(deviceType!=DeviceType.WINDOW_SHADES)
            curtainDropdownView.setProgress(50);
            //keyName = getResources().getString(R.string.action_stop);
//            } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//                //产品设计问题到导致情景联动普通窗帘没有停，所以停默认显示开
//                command = DeviceOrder.OPEN;
//                curtainControlOldView.open();
//                curtainDropdownView.setProgress(100);
//                keyName = getResources().getString(R.string.action_on);
//            }
        }
    }

    @Override
    public void onCurtainOpen() {
        if (deviceType == DeviceType.SCREEN) {
            curtainDropdownView.setProgress(0);
            command = DeviceOrder.CLOSE;
            value1 = 0;
        }
        else {
            if(deviceType==DeviceType.WINDOW_SHADES){
                pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
                pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
            }else{
                curtainDropdownView.setProgress(100);
            }

            command = DeviceOrder.OPEN;
            value1 = 100;
        }
        curtainControlOldView.open();
    }

    @Override
    public void onCurtainClose() {
        if (deviceType == DeviceType.SCREEN) {
            curtainDropdownView.setProgress(100);
            command = DeviceOrder.OPEN;
            value1 = 100;
        }
        else {
            if(deviceType==DeviceType.WINDOW_SHADES){
                pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
                pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
            }else {
                curtainDropdownView.setProgress(0);
            }
            command = DeviceOrder.CLOSE;
            value1 = 0;
        }
        curtainControlOldView.close();
    }

    @Override
    public void onCurtainStop() {
        curtainControlOldView.stop();
        if(deviceType==DeviceType.WINDOW_SHADES){
            pageUpTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_up_normal, 0, 0);
            pageDownTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_blinds_down_normal, 0, 0);
        }else{
            curtainDropdownView.setProgress(50);
        }
        command = DeviceOrder.STOP;
        value1 = 50;
    }
}
