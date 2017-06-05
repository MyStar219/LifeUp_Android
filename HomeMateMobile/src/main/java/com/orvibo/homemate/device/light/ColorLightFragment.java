package com.orvibo.homemate.device.light;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.OrviboTime;
import com.orvibo.homemate.util.ColorUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.ColorTempertureView;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 色温灯控制fragment
 */
public class ColorLightFragment extends BaseLightFragment {
    private static final String TAG = ColorLightFragment.class.getSimpleName();
    private ColorTempertureView circleColorView;

    private SeekBar mColor_seekBar;
    private SeekBar mAlpha_seekBar;

    /**
     * color temperature：色温控制，此时value3填写色温值，范围位154～370；
     */
    private volatile int mColor;
    /**
     * color temperature：色温控制，此时value2填写亮度值；
     */
    private volatile int mAlpha;
    private volatile int mColorTemperture;

    private static final int mColorSeekBarMax = Constant.COLOR_TEMP_LIGHT_MAX - Constant.COLOR_TEMP_LIGHT_MIN;


    @Override
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
        initStatus();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        circleColorView = (ColorTempertureView) view.findViewById(R.id.circleColorView);
        mColor_seekBar = (SeekBar) view.findViewById(R.id.color_seekBar);
        mColor_seekBar.setMax(mColorSeekBarMax);
        mAlpha_seekBar = (SeekBar) view.findViewById(R.id.seekBarLight);
        mAlpha_seekBar.setMax(Constant.DIMMER_MAX);
        // final int minColor = Constant.COLOR_TEMP_LIGHT_MIN;
        SeekBarListener seekBarListener = new SeekBarListener();
        mColor_seekBar.setOnSeekBarChangeListener(seekBarListener);
        mAlpha_seekBar.setOnSeekBarChangeListener(seekBarListener);
    }

    private void initStatus() {
        DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, deviceId);
        // int value1 = DeviceStatusConstant.OFF;
        //value2只用于界面显示
        //int value2 = 0;
        if (deviceStatus != null) {
            int value1 = deviceStatus.getValue1();
            initSwithStatus(value1);
            mColor = deviceStatus.getValue3();
            mAlpha = deviceStatus.getValue2();
            if (mAlpha < Constant.COLOR_LIGHT_MIN) {
                mAlpha = Constant.COLOR_LIGHT_MIN;
            }
//            if (value1 == DeviceStatusConstant.ON && mColor <= 0) {
//                //on
//                mColor = Constant.COLOR_TEMP_LIGHT_MAX;
//                mAlpha = Constant.DIMMER_MAX;
//            } else if (value1 == DeviceStatusConstant.OFF) {
//                //off
//                mColor = Constant.COLOR_TEMP_LIGHT_MIN;
//                mAlpha = 0;
//            }
        } else {
            mColor = Constant.COLOR_TEMP_LIGHT_MIN;
            mAlpha = Constant.COLOR_LIGHT_MIN;
        }
        mColorTemperture = colorToColorTemperture(mColor);
        LogUtil.d(TAG, "initStatus()-deviceStatus:" + deviceStatus);
        setSeekBarStatus(mAlpha, mColor);
        setColor(mAlpha);
    }

    /**
     * 初始化开关和相关自定义view
     *
     * @param value1
     */
    @Override
    public void initSwithStatus(int value1) {
        super.initSwithStatus(value1);
        if (value1 == DeviceStatusConstant.ON) {
            mColor_seekBar.setEnabled(true);
            mAlpha_seekBar.setEnabled(true);
        } else {
            mColor_seekBar.setEnabled(false);
            mAlpha_seekBar.setEnabled(false);
        }
    }

    /**
     * 设置颜色界面
     *
     * @param value2 亮度
     */
    private void setColor(int value2) {
        double[] rgb = ColorUtil.colorTemperatureToRGB(mColorTemperture);
        int red = (int) rgb[0];
        int green = (int) rgb[1];
        int blue = (int) rgb[2];
//        int realColor = Color.argb(mAlpha, red, green, blue);
        circleColorView.setARGB(value2, red, green, blue);
        LogUtil.i(TAG, "setRgb()-mColorTemperture:" + mColorTemperture
                + ",alpha:" + mAlpha
                + ",red:" + red
                + ",green:" + green
                + ",blue:" + blue
                + ",value2:" + value2);

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
        mAlpha_seekBar.setProgress(alpha);
        mColor_seekBar.setProgress(color - Constant.COLOR_TEMP_LIGHT_MIN);
    }

    private int colorToColorTemperture(int color) {
        int max = Constant.COLOR_TEMPERATURE_MAX;
        return max - (max - Constant.COLOR_TEMPERATURE_MIN) * (color - Constant.COLOR_TEMP_LIGHT_MIN) / (Constant.COLOR_TEMP_LIGHT_MAX - Constant.COLOR_TEMP_LIGHT_MIN);
    }

    /**
     * 色温控制
     *
     * @param noPorcess true不处理控制结果和属性报告
     */
    private void control(boolean noPorcess) {
        Message msg = mHandler.obtainMessage(WHAT_CONTROL);
        msg.arg1 = mAlpha;
        msg.arg2 = mColor;
        sendControlMessage(msg, noPorcess);
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        LogUtil.d(TAG, "isCheckTimeout()-uid:" + uid + ",deviceId:" + deviceId + ",result:" + result);
        if (isProcessControlResult() && result != ErrorCode.TIMEOUT && result != ErrorCode.TIMEOUT_CD) {
            if (result != ErrorCode.SUCCESS) {
                //控制失败，还原到当前数据库的值
                initStatus();
            }
            return super.onControlResult(uid, deviceId, result);
        }
        return false;
    }

    @Override
    protected void onPropertyReport(String deviceId, int value1, int value2, int value3, int value4, int statusType) {
        super.onPropertyReport(deviceId, value1, value2, value3, value4, statusType);
        if (!isMoving) {
            initSwithStatus(value1);
        }
        if (!isNoProcessProperty()) {
            LogUtil.i(TAG, "onPropertyReport()-Recive the propertyReport,value2:" + value2 + ",value3:" + value3);
            mAlpha = value2;
            mColor = value3;
            mColorTemperture = colorToColorTemperture(mColor);
            setSeekBarStatus(mAlpha, mColor);
            setColor(value2);
        } else {
            LogUtil.w(TAG, "onPropertyReport()-moving color||alpha or in " + Constant.TIME_REFRESH_DEVICE_STATUS + ",not process the propertyReport.");
        }
    }

    @Override
    protected void onRefreshStatus(DeviceStatus deviceStatus) {
        super.onRefreshStatus(deviceStatus);
        if (deviceStatus == null) {
            return;
        }
        mAlpha = deviceStatus.getValue2();
        mColor = deviceStatus.getValue3();
        mColorTemperture = colorToColorTemperture(mColor);
        setSeekBarStatus(mAlpha, mColor);
        setColor(mAlpha);
    }

    @Override
    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_CONTROL:
                int alpha = msg.arg1;
                int color = msg.arg2;
                if (alpha < Constant.DIMMING_LIGHT_MIN) {
                    alpha = Constant.DIMMING_LIGHT_MIN;
                }
                boolean noProcess = msg.getData().getBoolean(IntentKey.NO_PROCESS);
                LogUtil.d(TAG, "handleMessage()-what:" + msg.what + ",color:" + color + ",alpha:" + alpha + ",noProcess:" + noProcess);
                controlDevice.colorTempLight(uid, deviceId, DeviceStatusConstant.ON, alpha, color, noProcess);
                break;
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private static final int MIN_PROGRESS = 3;
        private static final int MIN_TIME = OrviboTime.CONTROL_INTERVAL_TIME;//ms
        private final int minColor = Constant.COLOR_TEMP_LIGHT_MIN;
        private int mCurrentColor;
        private int mStartColor;

        private long mColorCurrentTime;
        private long mColorStartTime;

        private int mCurrentAlpha;
        private int mStartAlpha;

        private long mAlphaCurrentTime;
        private long mAlphaStartTime;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setValue(seekBar);
            //LogUtil.d(TAG, "onProgressChanged(color)-color:" + mColor + ",progress:" + progress);
            setColor(mAlpha);
            if (fromUser) {
                final boolean isColor = seekBar == mColor_seekBar;
                if (isColor) {
                    mCurrentColor = progress;
                    mColorCurrentTime = System.currentTimeMillis();
                } else {
                    mCurrentAlpha = progress;
                    mAlphaCurrentTime = System.currentTimeMillis();
                }
                if (isCanControl(isColor)) {
                    //control
                    if (isColor) {
                        mStartColor = mCurrentColor;
                        mColorStartTime = mColorCurrentTime;
                    } else {
                        mStartAlpha = mCurrentAlpha;
                        mAlphaStartTime = mAlphaCurrentTime;
                    }
                    control(true);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoving = true;
            isReportProperty = false;
            mStartColor = seekBar.getProgress();
            mColorStartTime = System.currentTimeMillis();
            cancelDelayRefreshMessage();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(WHAT_CONTROL);
            LogUtil.i(TAG, "onStopTrackingTouch()-stop move,going to control the color temperture light.");
            isMoving = false;
            int progress = seekBar.getProgress();
            if (progress < 0) {
                progress = Constant.DIMMING_LIGHT_MIN;
                seekBar.setProgress(progress);
            }
            //松开手需要控制色温灯
            setValue(seekBar);
            LogUtil.e(TAG, "onStopTrackingTouch()-color:" + mColor
                    + ",alpha:" + mAlpha
                    + ",colorTemperture:" + mColorTemperture);
            //final boolean isColor = seekBar == mColor_seekBar;
            control(false);
            lastControl();
        }

        /**
         * 间隔时间>={@link #MIN_TIME}
         * 幅度值>={@link #MIN_PROGRESS}
         *
         * @param isColor treu颜色控制
         * @return
         */
        private boolean isCanControl(boolean isColor) {
            if (isColor) {
//                LogUtil.d(TAG, "isCancontrol()-mStartColor:" + mStartColor
//                        + ",mCurrentColor:" + mCurrentColor
//                        + ",mColorStartTime:" + mColorStartTime
//                        + ",mColorCurrentTime:" + mColorCurrentTime);
                return Math.abs(mCurrentColor - mStartColor) >= MIN_PROGRESS && (mColorCurrentTime - mColorStartTime) >= MIN_TIME;
            } else {
//                LogUtil.d(TAG, "isCancontrol()-mStartAlpha:" + mStartAlpha
//                        + ",mCurrentAlpha:" + mCurrentAlpha
//                        + ",mAlphaStartTime:" + mAlphaStartTime
//                        + ",mAlphaCurrentTime:" + mAlphaCurrentTime);
                return Math.abs(mCurrentAlpha - mStartAlpha) >= MIN_PROGRESS && (mAlphaCurrentTime - mAlphaStartTime) >= MIN_TIME;
            }
        }

        private void setValue(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (seekBar == mColor_seekBar) {
                mColor = getColorValue(progress);
//                mColor = (int) (minColor + mColorSeekBarMax * (1 - 1f * progress / mColorSeekBarMax));
                mColorTemperture = colorToColorTemperture(mColor);
            } else {
                mAlpha = progress;
            }
        }
    }

    private int getColorValue(int process) {
        return Constant.COLOR_TEMP_LIGHT_MIN + process;
    }

}
