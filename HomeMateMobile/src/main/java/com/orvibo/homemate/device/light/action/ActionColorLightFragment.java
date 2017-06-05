package com.orvibo.homemate.device.light.action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.util.ColorUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.ColorTempertureView;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 色温灯绑定fragment
 */
public class ActionColorLightFragment extends BaseLightActionFragment {
    private static final String TAG = ActionColorLightFragment.class.getSimpleName();

    private ColorTempertureView circleColorView;


    /**
     *
     */
    private volatile int mColor;
    private volatile int mColorTemperture;
    private volatile int mAlpha;

    private static final int mColorSeekBarMax = Constant.COLOR_TEMP_LIGHT_MAX - Constant.COLOR_TEMP_LIGHT_MIN;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_color_temperature_light, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        circleColorView = (ColorTempertureView) view.findViewById(R.id.circleColorView);
        mColor_seekBar = (SeekBar) view.findViewById(R.id.color_seekBar);
        mColor_seekBar.setMax(mColorSeekBarMax);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
        seekBarLight.setMax(Constant.DIMMER_MAX);
        final int minColor = Constant.COLOR_TEMP_LIGHT_MIN;
        SeekBarListener seekBarListener = new SeekBarListener();
        mColor_seekBar.setOnSeekBarChangeListener(seekBarListener);
        seekBarLight.setOnSeekBarChangeListener(seekBarListener);
    }


    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        mColor = action.getValue3();
        mColorTemperture = colorToColorTemperture(mColor);
        mAlpha = action.getValue2();
        command = action.getCommand();
        value1 = action.getValue1();
        value2 = action.getValue2();
        value3 = action.getValue3();
        value4 = action.getValue4();
//        if (command.equals(DeviceOrder.ON)) {
//            value1 = DeviceStatusConstant.ON;
//            mAlpha = Constant.DIMMER_MAX;
////            if (mAlpha == 0) {
//            command = DeviceOrder.COLOR_TEMPERATURE;
////            }
//        } else if (command.equals(DeviceOrder.OFF)) {
//            value1 = DeviceStatusConstant.OFF;
//        } else if (command.equals(DeviceOrder.TOGGLE)) {
//
//        } else {
//            command = DeviceOrder.COLOR_TEMPERATURE;
//            value1 = DeviceStatusConstant.ON;
//            value2 = mAlpha;
//            value3 = mColor;
//        }
        setSeekBarStatus(mAlpha, mColor);
        setColor(value2);
//        setColor(value1 == DeviceStatusConstant.OFF ? 0 : value2);
//        setAction(action);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        setAction(deviceStatus);
    }

    @Override
    protected void onDefaultAction(Action action) {
        super.onDefaultAction(action);
        setAction(action);
    }

    private void setAction(Action action) {
        mColor = action.getValue3();
        mColorTemperture = colorToColorTemperture(mColor);
        mAlpha = action.getValue2();
        int tempValue1 = action.getValue1();
        if (mAlpha >= 0) {
            if (mAlpha < Constant.COLOR_TEMP_LIGHT_MIN) {
                mAlpha = Constant.COLOR_TEMP_LIGHT_MIN;
            }
            command = DeviceOrder.COLOR_TEMPERATURE;
            value1 = DeviceStatusConstant.ON;
            value2 = mAlpha;
            value3 = mColor;
        } else {
            command = DeviceOrder.OFF;
            value1 = DeviceStatusConstant.OFF;
        }
        if (tempValue1 == DeviceStatusConstant.OFF) {
            command = DeviceOrder.OFF;
            value1 = DeviceStatusConstant.OFF;
        }
        setSeekBarStatus(mAlpha, mColor);
        setColor(mAlpha);
//        setColor(tempValue1 == DeviceStatusConstant.OFF ? 0 : mAlpha);
    }

    /**
     * 色
     */
    private void setColor(int value2) {
        //154(黄) ~ 370(白)
        double[] rgb = ColorUtil.colorTemperatureToRGB(mColorTemperture);
        int red = (int) rgb[0];
        int green = (int) rgb[1];
        int blue = (int) rgb[2];
//        int realColor = Color.argb(mAlpha, red, green, blue);
        circleColorView.setARGB(value2, red, green, blue);
//        LogUtil.i(TAG, "setRgb()-mColorTemperture:" + mColorTemperture
//                + ",alpha:" + mAlpha
//                + ",red:" + red
//                + ",green:" + green
//                + ",blue:" + blue
//                + ",realColor:" + realColor);

    }

    /**
     * 设置seekBar值
     *
     * @param alpha
     * @param color
     */
    private void setSeekBarStatus(int alpha, int color) {
        LogUtil.i(TAG, "setSeekBarStatus()-亮度seekbar:" + alpha
                + ",色温seekbar:" + color);
        seekBarLight.setProgress(alpha);
        mColor_seekBar.setProgress(color - Constant.COLOR_TEMP_LIGHT_MIN);
    }

    private int colorToColorTemperture(int color) {
        int max = Constant.COLOR_TEMPERATURE_MAX;
        return max - (max - Constant.COLOR_TEMPERATURE_MIN) * (color - Constant.COLOR_TEMP_LIGHT_MIN) / (Constant.COLOR_TEMP_LIGHT_MAX - Constant.COLOR_TEMP_LIGHT_MIN);
//        return color * Constant.COLOR_TEMPERATURE_MAX / Constant.COLOR_TEMP_LIGHT_MAX;
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                value1 = DeviceStatusConstant.ON;
                command = DeviceOrder.COLOR_TEMPERATURE;
            }
            setValue(seekBar);
            //LogUtil.d(TAG, "onProgressChanged(color)-color:" + mColor + ",progress:" + progress);
            setColor(mAlpha);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        private void setValue(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (seekBar == mColor_seekBar) {
                mColor = getColorValue(progress);
                mColorTemperture = colorToColorTemperture(mColor);
                value3 = mColor;
            } else {
                mAlpha = progress == 0 ? Constant.COLOR_LIGHT_MIN : progress;
                value2 = mAlpha;
            }
        }
    }

    private int getColorValue(int process) {
        return Constant.COLOR_TEMP_LIGHT_MIN + process;
    }

}
