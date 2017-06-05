package com.orvibo.homemate.device.light;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.RGBData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.StatusType;
import com.orvibo.homemate.sharedPreferences.RGBCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.ColorView;
import com.orvibo.homemate.view.custom.RGBColorView;
import com.orvibo.homemate.view.custom.RoundImageView;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: RGB灯控制fragment
 */
public class RgbLightFragment extends BaseLightFragment implements ColorView.OnColorChangedListener, RGBColorView.OnColorChangedListener, View.OnLongClickListener {
    private static final String TAG = RgbLightFragment.class.getSimpleName();
    private RGBColorView colorView;

    private DeviceStatusDao deviceStatusDao;
    private DeviceStatus deviceStatus;
    private SeekBarListener seekBarListener;
    private SeekBar seekBarLight;
    private TextView redTextView;
    private TextView greenTextView;
    private TextView blueTextView;
    private TextView currentColorTextView;
    private RoundImageView customColorImageView1;
    private RoundImageView customColorImageView2;
    private RoundImageView customColorImageView3;
    private RoundImageView customColorImageView4;
    private RoundImageView customColorImageView5;
    private LinearLayout customColorLinearLayout;
    private RelativeLayout digitColorRelativeLayout;
    private SeekBar seekBarWhiteLight;

    /**
     * color control：RGB调色，此时value2填写亮度值；
     */
    private int level2;
    /**
     * color control：RGB调色，此时value3填写饱和度值；
     */
    private int saturation3;
    /**
     * color control：RGB调色，此时value4填写色度值；
     */
    private int hue4;

    /**
     * 显示RGB数值 true：显示；false：不显示
     */
    private boolean IS_RGB_SHOW = false;

    private RGBData mRGBData;
    private int[] hsl = new int[4];

    private int seekBarProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_rgb_light, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
        initStatus();
        initFiveColorView();
    }

    @Override
    public void onResume() {
        super.onResume();
//        setControlDeviceBar(NavigationType.greenType, deviceName);
        IS_RGB_SHOW = RGBCache.getRgbIsShow(mContext, UserCache.getCurrentUserId(mContext));
        if (IS_RGB_SHOW) {
            digitColorRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            digitColorRelativeLayout.setVisibility(View.GONE);
        }
    }

    protected void initView(View view) {
        super.initView(view);
        colorView = (RGBColorView) view.findViewById(R.id.colorView);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
        redTextView = (TextView) view.findViewById(R.id.redTextView);
        greenTextView = (TextView) view.findViewById(R.id.greenTextView);
        blueTextView = (TextView) view.findViewById(R.id.blueTextView);
        currentColorTextView = (TextView) view.findViewById(R.id.currentColorTextView);
        customColorImageView1 = (RoundImageView) view.findViewById(R.id.customColorImageView1);
        customColorImageView2 = (RoundImageView) view.findViewById(R.id.customColorImageViewView2);
        customColorImageView3 = (RoundImageView) view.findViewById(R.id.customColorImageView3);
        customColorImageView4 = (RoundImageView) view.findViewById(R.id.customColorImageView4);
        customColorImageView5 = (RoundImageView) view.findViewById(R.id.customColorImageView5);
        customColorLinearLayout = (LinearLayout) view.findViewById(R.id.customColorLinearLayout);
        digitColorRelativeLayout = (RelativeLayout) view.findViewById(R.id.digitColorRelativeLayout);
        customColorLinearLayout.setVisibility(View.VISIBLE);
    }

    private void initListener() {
        seekBarLight.setOnSeekBarChangeListener(seekBarListener);
        seekBarLight.setOnSeekBarChangeListener(seekBarListener);
        colorView.setOnColorChangedListener(this);
        customColorImageView1.setOnClickListener(this);
        customColorImageView2.setOnClickListener(this);
        customColorImageView3.setOnClickListener(this);
        customColorImageView4.setOnClickListener(this);
        customColorImageView5.setOnClickListener(this);
        customColorImageView1.setOnLongClickListener(this);
        customColorImageView2.setOnLongClickListener(this);
        customColorImageView3.setOnLongClickListener(this);
        customColorImageView4.setOnLongClickListener(this);
        customColorImageView5.setOnLongClickListener(this);
    }

    private void initData() {
        seekBarListener = new SeekBarListener();
        deviceStatusDao = new DeviceStatusDao();
        mRGBData = new RGBData();
    }

    /**
     * 初始化开关和相关自定义view
     *
     * @param value1
     */
    @Override
    protected void initSwithStatus(int value1) {
        super.initSwithStatus(value1);
        boolean isEnable = value1 == 0;
        colorView.setEnabled(isEnable);
        seekBarLight.setEnabled(isEnable);
        customColorLinearLayout.setEnabled(isEnable);
        customColorImageView1.setEnabled(isEnable);
        customColorImageView2.setEnabled(isEnable);
        customColorImageView3.setEnabled(isEnable);
        customColorImageView4.setEnabled(isEnable);
        customColorImageView5.setEnabled(isEnable);
    }

    /**
     * Init dimming light status
     */
    private void initStatus() {
        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        if (deviceStatus != null) {
            initSwithStatus(deviceStatus.getValue1());
            hsl[0] = deviceStatus.getValue4();
            hsl[1] = deviceStatus.getValue3();
            hsl[2] = deviceStatus.getValue2();
            mRGBData.setHsl(hsl);
            seekBarLight.setProgress(mRGBData.getBrightness());
            seekBarProgress = mRGBData.getBrightness();

            colorView.post(new Runnable() {
                @Override
                public void run() {
                    int colorViewWH = colorView.getMeasuredWidth();
                    colorView.locationAim(mRGBData.getRgbWithBrightnessByHsl(), colorViewWH);
                    colorView.invalidate();
                }
            });
//第二种方法
//            ViewTreeObserver vto = colorView.getViewTreeObserver();
//            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @SuppressWarnings("deprecation")
//                @Override
//                public void onGlobalLayout() {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        colorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    } else {
//                        colorView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    }
//
//                    int colorViewWH = colorView.getMeasuredWidth();
//                    colorView.locationAim(mRGBData.getRgbWithBrightnessByHsl(),colorViewWH);
//                    colorView.invalidate();
//                }
//            });
        }
    }

    private void initFiveColorView() {
        RGBData rgbData1 = getFiveColorRGBData(1);
        if (rgbData1 != null) {
            customColorImageView1.setBackgroundColor(Color.rgb(rgbData1.getRgbWithBrightnessByHsl()[0]
                    , rgbData1.getRgbWithBrightnessByHsl()[1], rgbData1.getRgbWithBrightnessByHsl()[2]));
        }
        RGBData rgbData2 = getFiveColorRGBData(2);
        if (rgbData2 != null) {
            customColorImageView2.setBackgroundColor(Color.rgb(rgbData2.getRgbWithBrightnessByHsl()[0]
                    , rgbData2.getRgbWithBrightnessByHsl()[1], rgbData2.getRgbWithBrightnessByHsl()[2]));
        }
        RGBData rgbData3 = getFiveColorRGBData(3);
        if (rgbData3 != null) {
            customColorImageView3.setBackgroundColor(Color.rgb(rgbData3.getRgbWithBrightnessByHsl()[0]
                    , rgbData3.getRgbWithBrightnessByHsl()[1], rgbData3.getRgbWithBrightnessByHsl()[2]));
        }
        RGBData rgbData4 = getFiveColorRGBData(4);
        if (rgbData4 != null) {
            customColorImageView4.setBackgroundColor(Color.rgb(rgbData4.getRgbWithBrightnessByHsl()[0]
                    , rgbData4.getRgbWithBrightnessByHsl()[1], rgbData4.getRgbWithBrightnessByHsl()[2]));
        }
        RGBData rgbData5 = getFiveColorRGBData(5);
        if (rgbData5 != null) {
            customColorImageView5.setBackgroundColor(Color.rgb(rgbData5.getRgbWithBrightnessByHsl()[0]
                    , rgbData5.getRgbWithBrightnessByHsl()[1], rgbData5.getRgbWithBrightnessByHsl()[2]));
        }
    }

    private RGBData getFiveColorRGBData(int position) {
        String color = RGBCache.getRGBFiveColor(mContext, deviceId, position);
        int[] hsl = new int[4];
        RGBData rgbData = new RGBData();
        if (!StringUtil.isEmpty(color)) {
            String[] values = color.split("_");
            hsl[0] = Integer.valueOf(values[0]);
            hsl[1] = Integer.valueOf(values[1]);
            hsl[2] = Integer.valueOf(values[2]);
            rgbData.setHsl(hsl);
        } else {
            return null;
        }
        return rgbData;
    }

    @Override
    public void onStartColorChanged() {
        isMoving = true;
        isReportProperty = false;
    }

    @Override
    public void onColorChanged(int color) {
        LogUtil.d(TAG, "onColorChanged()-color:" + color);
        isMoving = true;
        colorControl(color, true);
    }

    @Override
    public void onColorShow(int color) {
        mRGBData.setRgb(color);
        refreshDigitColorView();
    }

    @Override
    public void onColorAim(int color) {
        colorControl(color, false);
        refreshDigitColorView();
        sendDelayRefreshStatusMessage();
        isMoving = false;
    }

    @Override
    protected void onRefreshStatus(DeviceStatus deviceStatus) {
        super.onRefreshStatus(deviceStatus);
        //延时message没有被移除，时间到后刷新页面
        initStatus();
    }

    private void refreshDigitColorView() {
        redTextView.setText(mRGBData.getRgbWithBrightness()[0] + "");
        greenTextView.setText(mRGBData.getRgbWithBrightness()[1] + "");
        blueTextView.setText(mRGBData.getRgbWithBrightness()[2] + "");
        currentColorTextView.setBackgroundColor(Color.rgb(mRGBData.getRgbWithBrightness()[0], mRGBData.getRgbWithBrightness()[1], mRGBData.getRgbWithBrightness()[2]));
    }

    private void refreshDigitColorViewByProgress(int progress) {
        redTextView.setText(mRGBData.getRgbWithBrightness(progress)[0] + "");
        greenTextView.setText(mRGBData.getRgbWithBrightness(progress)[1] + "");
        blueTextView.setText(mRGBData.getRgbWithBrightness(progress)[2] + "");
        currentColorTextView.setBackgroundColor(Color.rgb(mRGBData.getRgbWithBrightness(progress)[0]
                , mRGBData.getRgbWithBrightness(progress)[1], mRGBData.getRgbWithBrightness(progress)[2]));
    }

    private void colorControl(int color, boolean noProcess) {
        mRGBData.setRgb(color);
        level2 = mRGBData.getHsl()[2];//亮度
        saturation3 = mRGBData.getHsl()[1];//饱和度
        hue4 = mRGBData.getHsl()[0];//色度
        controlDevice.rgbLight(uid, deviceId, level2, saturation3, hue4, noProcess);
    }

    @Override
    protected void onPropertyReport(String deviceId, int value1, int value2, int value3, int value4, int statusType) {
        super.onPropertyReport(deviceId, value1, value2, value3, value4, statusType);
        if (!isMoving) {
            initSwithStatus(value1);
        }
        if (!isNoProcessProperty()) {
            LogUtil.d(TAG, "onPropertyReport()-set status.");
            initStatus();
            if (statusType == StatusType.RGB_STATUS) {
                hsl[0] = value4;
                hsl[1] = value3;
                hsl[2] = value2;
                mRGBData.setHsl(hsl);
                colorView.locationAim(mRGBData.getRgbByHsl());
                colorView.invalidate();
            }
        } else {
            if (isMoving) {
                LogUtil.w(TAG, "onPropertyReport()-moving light level,not process the propertyReport.");
            } else {
                LogUtil.w(TAG, "onPropertyReport()-Stop in " + Constant.TIME_REFRESH_DEVICE_STATUS + "ms,not process the propertyReport.");
            }
        }
    }

    private void sendControlMessage(int progress, boolean noProcess) {
        LogUtil.e(TAG, "sendControlMessage()-progress:" + progress + ",noProcess:" + noProcess);
        Message msg = mHandler.obtainMessage(WHAT_CONTROL);
        msg.arg1 = progress;
        sendControlMessage(msg, noProcess);
    }

    @Override
    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_CONTROL:
                int progress = msg.arg1;
                boolean noProcess = msg.getData().getBoolean(IntentKey.NO_PROCESS);
                LogUtil.e(TAG, "handleMessage()-progress:" + progress + ",noProcess:" + noProcess);
                controlLight(progress, noProcess);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.customColorImageView1:
                longClickFiveColor(1, customColorImageView1);
                break;
            case R.id.customColorImageViewView2:
                longClickFiveColor(2, customColorImageView2);
                break;
            case R.id.customColorImageView3:
                longClickFiveColor(3, customColorImageView3);
                break;
            case R.id.customColorImageView4:
                longClickFiveColor(4, customColorImageView4);
                break;
            case R.id.customColorImageView5:
                longClickFiveColor(5, customColorImageView5);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.customColorImageView1:
                clickFiveColor(1, customColorImageView1);
                break;
            case R.id.customColorImageViewView2:
                clickFiveColor(2, customColorImageView2);
                break;
            case R.id.customColorImageView3:
                clickFiveColor(3, customColorImageView3);
                break;
            case R.id.customColorImageView4:
                clickFiveColor(4, customColorImageView4);
                break;
            case R.id.customColorImageView5:
                clickFiveColor(5, customColorImageView5);
                break;
        }
    }

    private void longClickFiveColor(int position, View view) {
        RGBCache.setRGBFiveColor(mContext, deviceId, position
                , mRGBData.getHsl()[0] + "_" + mRGBData.getHsl()[1] + "_" + mRGBData.getHsl()[2]);

        view.setBackgroundColor(Color.rgb(mRGBData.getRgb()[0]
                , mRGBData.getRgb()[1], mRGBData.getRgb()[2]));
    }

    private void clickFiveColor(int position, View view) {
        String color = RGBCache.getRGBFiveColor(mContext, deviceId, position);
        if (StringUtil.isEmpty(color)) {
            //没有保存过颜色，保存颜色
            RGBCache.setRGBFiveColor(mContext, deviceId, position
                    , mRGBData.getHsl()[0] + "_" + mRGBData.getHsl()[1] + "_" + mRGBData.getHsl()[2]);
        } else {
            //更新色盘颜色，并控制
            String[] values = color.split("_");
            hsl[0] = Integer.valueOf(values[0]);
            hsl[1] = Integer.valueOf(values[1]);
            hsl[2] = Integer.valueOf(values[2]);
            mRGBData.setHsl(hsl);
            mRGBData.setBrightness(seekBarProgress < Constant.RGB_LIGHT_MIN ? Constant.RGB_LIGHT_MIN : seekBarProgress);

            refreshDigitColorView();

            colorView.post(new Runnable() {
                @Override
                public void run() {
                    int colorViewWH = colorView.getMeasuredWidth();
                    colorView.locationAim(mRGBData.getRgbWithBrightnessByHsl(), colorViewWH);
                    colorView.invalidate();
                }
            });

            level2 = mRGBData.getHsl()[2];//亮度
            saturation3 = mRGBData.getHsl()[1];//饱和度
            hue4 = mRGBData.getHsl()[0];//色度
            //noProcess要设置为false，
            controlDevice.rgbLight(uid, deviceId, level2, saturation3, hue4, false);
            isReportProperty = false;
//            //滑块定位到当前控制的色块位置
//            colorView.locationAim(mRGBData.getRgbByHsl());
//            colorView.invalidate();
            //不立即处理属性报告，逻辑跟滑动色块一样，手松开后延迟指定时间再处理
            sendDelayRefreshStatusMessage();
        }

        view.setBackgroundColor(Color.rgb(mRGBData.getRgb()[0]
                , mRGBData.getRgb()[1], mRGBData.getRgb()[2]));
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private static final int MIN_PROGRESS = 3;
        private static final int MIN_TIME = 200;//ms
        private int mStartProgress;
        private int mCurrentProgress;

        private long mProgressStartTime;
        private long mProgressCurrentTime;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mCurrentProgress = progress;
                mProgressCurrentTime = System.currentTimeMillis();
                refreshDigitColorViewByProgress(progress);
                if (isCanControl()) {
                    mStartProgress = mCurrentProgress;
                    mProgressStartTime = mProgressCurrentTime;
                    sendControlMessage(progress, true);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoving = true;

            mStartProgress = seekBar.getProgress();
            mProgressStartTime = System.currentTimeMillis();
            isReportProperty = false;
            cancelDelayRefreshMessage();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMoving = false;
            seekBarProgress = seekBar.getProgress();
            sendControlMessage(seekBarProgress, false);
            sendDelayRefreshStatusMessage();
        }

        /**
         * @return true满足控制条件。间隔时间>{@link #MIN_TIME},滑动距离>{@link #MIN_PROGRESS}
         */
        private boolean isCanControl() {
            return Math.abs(mCurrentProgress - mStartProgress) >= MIN_PROGRESS && (mProgressCurrentTime - mProgressStartTime) >= MIN_TIME;
        }
    }

    /**
     * 调整RGB灯亮度
     *
     * @param progress
     * @param noProcess true Don't process the property report.
     */
    private void controlLight(int progress, boolean noProcess) {
        mRGBData.setBrightness(progress < Constant.RGB_LIGHT_MIN ? Constant.RGB_LIGHT_MIN : progress);
        level2 = mRGBData.getHsl()[2];//亮度
        saturation3 = mRGBData.getHsl()[1];//饱和度
        hue4 = mRGBData.getHsl()[0];//色度
        controlDevice.rgbLight(uid, deviceId,
                level2, saturation3, hue4, noProcess);
    }

    @Override
    public boolean onControlResult(String uid, String deviceId, int result) {
        if (isProcessControlResult()) {
            //超时不做任何处理，页面也不刷新。解决多个请求，有部分失败但滑块不断变化问题。放在initStatus()接口的前面
            if (result == ErrorCode.TIMEOUT) {
                return false;
            }

            if (result != ErrorCode.SUCCESS) {
                initStatus();
            }
            return super.onControlResult(uid, deviceId, result);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (colorView != null) {
            colorView.release();
        }
        super.onDestroy();
    }
}
