package com.orvibo.homemate.device.light.action;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.RGBData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.sharedPreferences.RGBCache;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.RGBColorView;
import com.orvibo.homemate.view.custom.RoundImageView;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: RGB灯绑定fragment
 */
public class ActionRgbLightFragment extends BaseLightActionFragment implements RGBColorView.OnColorChangedListener {
    private static final String TAG = ActionRgbLightFragment.class.getSimpleName();
    private SeekBarListener seekBarListener;

    private RGBData mRGBData;
    private int[] hsl = new int[4];
    private int seekBarProgress;

    private LinearLayout customColorLinearLayout;


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
        initFiveColorView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void initView(View view) {
        super.initView(view);
        colorView = (RGBColorView) view.findViewById(R.id.colorView);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
        customColorImageView1 = (RoundImageView) view.findViewById(R.id.customColorImageView1);
        customColorImageView2 = (RoundImageView) view.findViewById(R.id.customColorImageViewView2);
        customColorImageView3 = (RoundImageView) view.findViewById(R.id.customColorImageView3);
        customColorImageView4 = (RoundImageView) view.findViewById(R.id.customColorImageView4);
        customColorImageView5 = (RoundImageView) view.findViewById(R.id.customColorImageView5);
        customColorLinearLayout = (LinearLayout) view.findViewById(R.id.customColorLinearLayout);
        customColorLinearLayout.setVisibility(View.VISIBLE);
    }

    private void initListener() {
        seekBarLight.setOnSeekBarChangeListener(seekBarListener);
        colorView.setOnColorChangedListener(this);

        customColorImageView1.setOnClickListener(this);
        customColorImageView2.setOnClickListener(this);
        customColorImageView3.setOnClickListener(this);
        customColorImageView4.setOnClickListener(this);
        customColorImageView5.setOnClickListener(this);
    }

    private void initData() {
        seekBarListener = new SeekBarListener();
        deviceStatusDao = new DeviceStatusDao();
        mRGBData = new RGBData();
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
        String color = RGBCache.getRGBFiveColor(mAppContext, deviceId, position);
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

    private void clickFiveColor(int position, View view) {
        String color = RGBCache.getRGBFiveColor(mAppContext, deviceId, position);
        if (!StringUtil.isEmpty(color)) {
            //更新色盘颜色
            String[] values = color.split("_");
            hsl[0] = Integer.valueOf(values[0]);
            hsl[1] = Integer.valueOf(values[1]);
            hsl[2] = Integer.valueOf(values[2]);
            mRGBData.setHsl(hsl);
            mRGBData.setBrightness(seekBarProgress < Constant.RGB_LIGHT_MIN ? Constant.RGB_LIGHT_MIN : seekBarProgress);

            colorView.post(new Runnable() {
                @Override
                public void run() {
                    int colorViewWH = colorView.getMeasuredWidth();
                    colorView.locationAim(mRGBData.getRgbWithBrightnessByHsl(), colorViewWH);
                    colorView.invalidate();
                }
            });

            value1 = DeviceStatusConstant.ON;
            value2 = mRGBData.getHsl()[2];//亮度
            value3 = mRGBData.getHsl()[1];//饱和度
            value4 = mRGBData.getHsl()[0];//色度
            command = DeviceOrder.COLOR_CONTROL;
        }
    }

    @Override
    public void onStartColorChanged() {

    }

    @Override
    public void onColorChanged(int color) {
        value1 = DeviceStatusConstant.ON;
        initData(color);
    }

    @Override
    public void onColorShow(int color) {

    }

    @Override
    public void onColorAim(int color) {
        initData(color);
    }

    /**
     * 硬件限制 色度：0-0xEF，饱和度：0-0xF0
     *
     * @param color
     */
    private void initData(int color) {
        mRGBData.setRgb(color);
        value2 = mRGBData.getHsl()[2];//亮度
        value3 = mRGBData.getHsl()[1];//饱和度
        value4 = mRGBData.getHsl()[0];//色度
        command = DeviceOrder.COLOR_CONTROL;
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                controlLight(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * 调整RGB灯亮度
     *
     * @param progress
     */
    private void controlLight(int progress) {
        //进度为0时，会关掉灯，设置个最小值
        mRGBData.setBrightness(progress < Constant.RGB_LIGHT_MIN ? Constant.RGB_LIGHT_MIN : progress);
        value2 = mRGBData.getHsl()[2];//亮度
        value3 = mRGBData.getHsl()[1];//饱和度
        value4 = mRGBData.getHsl()[0];//色度
        command = DeviceOrder.COLOR_CONTROL;
    }

    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        setAction(action);
    }

    @Override
    protected void onDefaultAction(Action action) {
        super.onDefaultAction(action);
        setAction(action);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        setAction(deviceStatus);
    }

    private void setAction(Action action) {
        MyLogger.kLog().d(action);
        if (action != null) {
            command = action.getCommand();
            value1 = action.getValue1();
            value2 = action.getValue2();
            value3 = action.getValue3();
            value4 = action.getValue4();

            hsl[0] = value4;
            hsl[1] = value3;
            hsl[2] = value2;
            mRGBData.setHsl(hsl);

            colorView.post(new Runnable() {
                @Override
                public void run() {
                    int colorViewWH = colorView.getMeasuredWidth();
                    colorView.locationAim(mRGBData.getRgbWithBrightnessByHsl(), colorViewWH);
                    colorView.invalidate();
                }
            });
            seekBarLight.setProgress(mRGBData.getBrightness());
            seekBarProgress = mRGBData.getBrightness();
        }
    }

    @Override
    public void onDestroy() {
        if (colorView != null) {
            colorView.release();
        }
        super.onDestroy();
    }
}
