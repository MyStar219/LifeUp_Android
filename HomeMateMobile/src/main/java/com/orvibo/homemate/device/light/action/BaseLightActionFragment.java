package com.orvibo.homemate.device.light.action;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.RGBColorView;
import com.orvibo.homemate.view.custom.RoundImageView;

/**
 * Created by snown on 2016/6/23.
 *
 * @描述: 定时，绑定相关灯光基类
 */
public class BaseLightActionFragment extends BaseActionFragment {
    private static final String TAG = "BaseLightActionFragment";
    protected Button onOffBtn;//开关状态按钮
    protected View alphaBg;//蒙版
    protected NavigationGreenBar navigationGreenBar;
    private TextView openRb;
    private TextView closeRb;
    private TextView toggleRb;
    private View toggleView;
    private View noToggleView;
    protected SeekBar seekBarLight;
    protected SeekBar mColor_seekBar;
    protected RGBColorView colorView;

    protected RoundImageView customColorImageView1;
    protected RoundImageView customColorImageView2;
    protected RoundImageView customColorImageView3;
    protected RoundImageView customColorImageView4;
    protected RoundImageView customColorImageView5;

    protected void initView(View view) {
        onOffBtn = (Button) view.findViewById(R.id.onOffBtn);
        onOffBtn.setVisibility(View.GONE);
        alphaBg = view.findViewById(R.id.alphaBg);
        navigationGreenBar = (NavigationGreenBar) view.findViewById(R.id.nbTitle);
        toggleView = view.findViewById(R.id.toggleView);
        noToggleView = view.findViewById(R.id.noToggleView);
        if (BindActionType.isSupportToggle(bindActionType)) {
            //有翻转功能。开、关、翻转
            toggleView.setVisibility(View.VISIBLE);
            noToggleView.setVisibility(View.GONE);
            openRb = (TextView) view.findViewById(R.id.rbOpen);
            closeRb = (TextView) view.findViewById(R.id.rbClose);
            toggleRb = (TextView) view.findViewById(R.id.rbToggle);
            toggleRb.setOnClickListener(this);
        } else {
            //开、关
            noToggleView.setVisibility(View.VISIBLE);
            toggleView.setVisibility(View.GONE);
            openRb = (TextView) view.findViewById(R.id.rbOpen1);
            closeRb = (TextView) view.findViewById(R.id.rbClose1);
        }
        if (navigationGreenBar != null) {
            navigationGreenBar.showWhiteStyle();
            navigationGreenBar.setRightImageViewVisibility(View.GONE);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
    }

    /**
     * 初始化开关和相关自定义view
     */
    protected void initSwithStatus(boolean isCanClick) {
        LogUtil.d(TAG, "initSwithStatus()-isCanClick:" + isCanClick + ",command:" + command);
        seekBarLight.setEnabled(isCanClick);
        if (mColor_seekBar != null)
            mColor_seekBar.setEnabled(isCanClick);
        if (colorView != null)
            colorView.setEnabled(isCanClick);
        if (isCanClick) {
            alphaBg.setVisibility(View.GONE);
        } else {
            alphaBg.setVisibility(View.VISIBLE);

        }
        if (customColorImageView1 != null)
            customColorImageView1.setEnabled(isCanClick);
        if (customColorImageView2 != null)
            customColorImageView2.setEnabled(isCanClick);
        if (customColorImageView3 != null)
            customColorImageView3.setEnabled(isCanClick);
        if (customColorImageView4 != null)
            customColorImageView4.setEnabled(isCanClick);
        if (customColorImageView5 != null)
            customColorImageView5.setEnabled(isCanClick);

        if (command != null) {
            switch (command) {
                case DeviceOrder.ON:
                case DeviceOrder.COLOR_CONTROL:
                case DeviceOrder.COLOR_TEMPERATURE:
                case DeviceOrder.MOVE_TO_LEVEL:
                    openRb.setSelected(true);
                    closeRb.setSelected(false);
                    if (toggleRb != null)
                        toggleRb.setSelected(false);
                    break;
                case DeviceOrder.OFF:
                    openRb.setSelected(false);
                    closeRb.setSelected(true);
                    if (toggleRb != null)
                        toggleRb.setSelected(false);
                    break;
                case DeviceOrder.TOGGLE:
                    openRb.setSelected(false);
                    closeRb.setSelected(false);
                    if (toggleRb != null)
                        toggleRb.setSelected(true);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        changeView(v.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume()-command:" + command);
        initViewGroup();
    }

    /**
     * 开关，翻转界面
     */
    protected void initViewGroup() {
        if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.TOGGLE) && toggleRb != null) {
            toggleRb.setPressed(true);
            initSwithStatus(false);
        } else if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.OFF)) {
            closeRb.setSelected(true);
            initSwithStatus(false);
        } else {
            openRb.setSelected(true);
            initSwithStatus(true);
        }
        openRb.setOnClickListener(this);
        closeRb.setOnClickListener(this);
    }

    /**
     * 灯的开关控制界面更新
     */
    private void changeView(int radioButtonId) {
        LogUtil.d(TAG, "changeView()-radioButtonId:" + radioButtonId);
        //更新文本内容，以符合选中项
        if (radioButtonId == R.id.rbOpen || radioButtonId == R.id.rbOpen1) {
            value1 = DeviceStatusConstant.ON;
            //rgb 色温灯 调光灯没有ON，只有对应如下的order by keeu
            final int deviceType = device.getDeviceType();
            if (deviceType == DeviceType.RGB) {
                command = DeviceOrder.COLOR_CONTROL;
                if (value2 < Constant.COLOR_LIGHT_MIN) {
                    LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than " + Constant.COLOR_LIGHT_MIN + ".Set it to " + value2);
                    value2 = Constant.DIMMER_MAX / 2 + 1;
                }
            } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
                command = DeviceOrder.COLOR_TEMPERATURE;
                if (value2 < Constant.COLOR_LIGHT_MIN) {
                    LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than " + Constant.COLOR_LIGHT_MIN + ".Set it to " + value2);
                    value2 = Constant.DIMMER_MAX / 2 + 1;
                }
            } else if (deviceType == DeviceType.DIMMER) {
                command = DeviceOrder.MOVE_TO_LEVEL;
                if (value2 < Constant.DIMMING_LIGHT_MIN) {
                    LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than " + Constant.COLOR_LIGHT_MIN + ".Set it to " + value2);
                    value2 = Constant.DIMMER_MAX / 2 + 1;
                }
            } else {
                command = DeviceOrder.ON;
                if (value2 <= 0) {
                    value2 = 1;
                    LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than 0.Set it to 1.");
                }
            }
            initSwithStatus(true);
            //通知刷新页面
            action.setValue1(value1);
            action.setCommand(command);
            action.setValue2(value2);
            action.setValue3(value3);
            action.setValue4(value4);
            onSelectedAction(action);
        } else if (radioButtonId == R.id.rbClose || radioButtonId == R.id.rbClose1) {
            command = DeviceOrder.OFF;
            value1 = DeviceStatusConstant.OFF;
            initSwithStatus(false);
            // value2 = value4 = 0;
        } else if (radioButtonId == R.id.rbToggle) {
            command = DeviceOrder.TOGGLE;
            initSwithStatus(false);
            // value1 = value2 = value4 = 0;
        }
    }

}
