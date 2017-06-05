package com.orvibo.homemate.device.action.infrareddevice;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.ACPanelModelAndWindConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.device.action.BaseActionActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.ACPanelCircularSeekBar;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 创维空调 空调面板绑定
 * Created by Smagret on 2016/3/24.
 */
public class ActionAcPanelActivity extends BaseActionActivity implements ACPanelCircularSeekBar.OnChangeTemperatureListener, ACPanelCircularSeekBar.OnTouchStateListener {

    private static final String TAG = ActionAcPanelActivity.class.getSimpleName();
    private IrButton mBtnOpen;
    private IrButton mBtnShutdown;
    private ACPanelCircularSeekBar mCircularSeekBar;
    private IrButton mTvTemperature;
    private TextView mTemperratureTips;
    private ImageView mIvModel;
    private ImageView mIvSpeed;
    private IrButton mBtnCold;
    private IrButton mBtnHot;
    private IrButton mBtnDehumidifier;
    private IrButton mBtnAuto;
    private IrButton mBtnLow;
    private IrButton mBtnMiddle;
    private IrButton mBtnHigh;
    private IrButton mBtnAutoWind;
    private List<IrButton> mIrButtons = new ArrayList<>();
    private final int DEFAULT_TEMPERATURE = 250;
    private TextView mTvSetTemperature;
    private LinearLayout mCurrentStateLL;
    private GradientDrawable mGradientDrawable;
    private int defaut_model = ACPanelModelAndWindConstant.MODEL_AUTO;
    private int defaut_windLevel = ACPanelModelAndWindConstant.WIND_AUTO;
    private int defaut_onoff = ACPanelModelAndWindConstant.AC_CLOSE;
    private int defaut_lock = ACPanelModelAndWindConstant.AC_UNLOCK;
    private int current_model = defaut_model;
    private int current_windLevel = defaut_windLevel;
    private int current_onoff = defaut_onoff;
    private int current_Temperature = DEFAULT_TEMPERATURE;
    private int current_setTemperature = DEFAULT_TEMPERATURE;
    private int current_Lock = defaut_lock;
    private boolean isClose = false;
    private boolean isLock = false;
    private boolean isHs = false;//
    private boolean isSilence = false;//静音开关的状态
    private int lastSetTemperature = 250;
    private Acpanel mAcpanel;
    private DeviceStatusDao mDeviceStatusDao;
    private DeviceStatus mDeviceStatus;
    private ImageView mCircularSeekBar_bg;
    private LinearLayout mModeAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_panel);
        initView();
        initData();
        initEvent();
        initAcPanel();
        initAcPanelStatus();
    }

    private void initAcPanelStatus() {
        if (action != null) {
            mAcpanel.setValue1(action.getValue1());
            mAcpanel.setValue2(action.getValue2());
            mAcpanel.setValue3(action.getValue3());
            mAcpanel.setValue4(action.getValue4());
        } else {

            mDeviceStatusDao = new DeviceStatusDao();
            mDeviceStatus = mDeviceStatusDao.selDeviceStatus(currentMainUid, device);
            if (mDeviceStatus != null) {
                mAcpanel.setValue1(mDeviceStatus.getValue1());
                mAcpanel.setValue2(mDeviceStatus.getValue2());
                mAcpanel.setValue3(mDeviceStatus.getValue3());
                mAcpanel.setValue4(mDeviceStatus.getValue4());

            }
        }
        current_model = mAcpanel.getModel();
        current_onoff = mAcpanel.getOnoff();
        current_windLevel = mAcpanel.getWindLevel();
        current_Lock = mAcpanel.getLock();
        if (current_onoff == ACPanelModelAndWindConstant.AC_CLOSE) {
            mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));
        } else {
            mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
        }
        mBtnOpen.refresh();

        if (current_Lock == ACPanelModelAndWindConstant.AC_LOCK) {
            mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
        } else {
            mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));
        }
        mBtnShutdown.refresh();
        LogUtil.d(TAG, "initAcPanelStatus() - mAcpanel:" + mAcpanel);
        refresh(current_model, current_windLevel, current_onoff, current_Lock);
    }

    private void initAcPanel() {
        mAcpanel = new Acpanel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO
        //setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    private void initData() {
        //初始化circularSeekBar

        if (device.getDeviceType() == DeviceType.AC_PANEL) {
            mCircularSeekBar.setAsACPanel();
        } else {
            mCircularSeekBar.setAsWifiACPanel();
        }

        mCircularSeekBar.setTemperature(current_setTemperature * 10);//初始化一下进度
        mCircularSeekBar.setKeyName(current_Temperature + getString(R.string.conditioner_temperature_unit));


        //初始化tvTemperture
        mTvTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        mTvTemperature.setKeyName(current_Temperature + getString(R.string.conditioner_temperature_unit));

        mTvTemperature.setTextColor(getResources().getColor(R.color.font_white_gray));

        //初始化GradientDrawable.用于tvSetTemperature的背景
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.setShape(GradientDrawable.OVAL);
        //初始化tvSetTemperature
        mTvSetTemperature.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/digital.ttf"));
        mTvSetTemperature.setBackgroundDrawable(mGradientDrawable);

        //初始化按键状态
        mCircularSeekBar.initStatus(ActionAcPanelActivity.this, uid, userName, deviceId);
        for (IrButton irButton : mIrButtons) {
            //空调面板不需要学习，直接设置为学习状态
            irButton.setLearnedStatus(true);
            irButton.initStatus(ActionAcPanelActivity.this, uid, userName, deviceId);
        }
    }

    private void initView() {
        final NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
       /* if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }*/
        mModeAuto = (LinearLayout) findViewById(R.id.mode_auto);
        mBtnOpen = (IrButton) findViewById(R.id.btnOpen);
        mBtnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        mCircularSeekBar = (ACPanelCircularSeekBar) findViewById(R.id.circularSeekBar);
        //（在拖动圆盘的过程中显示设定的温度）
        mTvSetTemperature = (TextView) findViewById(R.id.tvSetTemperature);
        mCurrentStateLL = (LinearLayout) findViewById(R.id.currentStateLL);
        //（1.在不拖动圆盘的情况下显示value2中的环境温度）
        mTvTemperature = (IrButton) findViewById(R.id.tvTemperature);
        mTemperratureTips = (TextView) findViewById(R.id.temperatureTips);
        mIvModel = (ImageView) findViewById(R.id.ivModel);
        mIvSpeed = (ImageView) findViewById(R.id.ivSpeed);
        mBtnCold = (IrButton) findViewById(R.id.btnCold);
        mBtnHot = (IrButton) findViewById(R.id.btnHot);
        mBtnDehumidifier = (IrButton) findViewById(R.id.btnDehumidifier);
        mBtnAuto = (IrButton) findViewById(R.id.btnAuto);
        mBtnLow = (IrButton) findViewById(R.id.btnLow);
        mBtnMiddle = (IrButton) findViewById(R.id.btnMiddle);
        mBtnHigh = (IrButton) findViewById(R.id.btnHigh);
        mBtnAutoWind = (IrButton) findViewById(R.id.btnAutoWind);
        mCircularSeekBar_bg = (ImageView) findViewById(R.id.circularSeekBar_bg);
        mIrButtons.add(mBtnOpen);
        mIrButtons.add(mBtnShutdown);
        mIrButtons.add(mBtnCold);
        mIrButtons.add(mBtnHot);
        mIrButtons.add(mBtnDehumidifier);
        mIrButtons.add(mBtnAuto);
        mIrButtons.add(mBtnLow);
        mIrButtons.add(mBtnMiddle);
        mIrButtons.add(mBtnHigh);
        mIrButtons.add(mBtnAutoWind);
        mIrButtons.add(mTvTemperature);
        if (device != null) {
            if (device.getDeviceType() == DeviceType.AC) {
                mBtnShutdown.setVisibility(View.GONE);
                //16~31
                mCircularSeekBar_bg.setBackgroundResource(R.drawable.cw_controller_scale_on);
            } else if (device.getDeviceType() == DeviceType.AC_PANEL) {
                mBtnShutdown.setVisibility(View.VISIBLE);
                //10~30
                mCircularSeekBar_bg.setBackgroundResource(R.drawable.ac_controller_scale_on_yl);
                //艺林的空调面板模式（自动）不可用
//                mBtnAuto.setEnabled(false);
//                mBtnAuto.setCloseStatus(true);
//                mIrButtons.remove(mBtnAuto);
                mModeAuto.setVisibility(View.GONE);
            }
        }

    }

    private void initEvent() {
        mCircularSeekBar.setOnChangeTemperatureListener(this);
        mCircularSeekBar.setOnTouchStateListener(this);
        for (final IrButton irButton : mIrButtons) {
            if (irButton.equals(mTvTemperature)) {
                continue;
            }
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE && !v.equals(mBtnOpen)) {
                        // ToastUtil.showToast(getString(R.string.conditioner_power_close));
                        return;
                    }
                    if (mAcpanel.getLock() == ACPanelModelAndWindConstant.AC_LOCK && !v.equals(mBtnShutdown)) {
                        // ToastUtil.showToast(getString(R.string.conditioner_lock));
                        return;
                    }

                    switch (v.getId()) {
                        case R.id.btnOpen:

                            if (device.getDeviceType() == DeviceType.AC) {
                                command = DeviceOrder.AC_CTRL;
                            } else {
                                if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
                                    command = DeviceOrder.ON;
                                } else {
                                    command = DeviceOrder.OFF;
                                }
                            }

                            if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
                                mAcpanel.setOnoff(ACPanelModelAndWindConstant.AC_OPEN);
                                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
                                mBtnOpen.refresh();
                            } else {
                                mAcpanel.setOnoff(ACPanelModelAndWindConstant.AC_CLOSE);
                                mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));
                                mBtnOpen.refresh();
                            }
                            break;
                        case R.id.btnShutdown:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                //  command = DeviceOrder.AC_LOCK_SETTING;
                            }
                            if (mAcpanel.getLock() == ACPanelModelAndWindConstant.AC_LOCK) {
                                mAcpanel.setLock(ACPanelModelAndWindConstant.AC_UNLOCK);
                                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));
                                mBtnShutdown.refresh();
                            } else {
                                mAcpanel.setLock(ACPanelModelAndWindConstant.AC_LOCK);
                                mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
                                mBtnShutdown.refresh();
                            }
                            break;
                        case R.id.btnCold:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_MODE_SETTING;
                            }
                            mAcpanel.setModel(ACPanelModelAndWindConstant.MODEL_COOL);
                            break;
                        case R.id.btnHot:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_MODE_SETTING;
                            }
                            mAcpanel.setModel(ACPanelModelAndWindConstant.MODEL_HOT);
                            break;
                        case R.id.btnDehumidifier:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_MODE_SETTING;
                            }
                            mAcpanel.setModel(ACPanelModelAndWindConstant.MODEL_SEND_WIND);
                            break;
                        case R.id.btnAuto:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_MODE_SETTING;
                            }
                            mAcpanel.setModel(ACPanelModelAndWindConstant.MODEL_AUTO);
                            break;
                        case R.id.btnLow:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_WIND_SETTING;
                            }
                            mAcpanel.setWindLevel(ACPanelModelAndWindConstant.WIND_LOW1);
                            break;
                        case R.id.btnMiddle:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_WIND_SETTING;
                            }
                            mAcpanel.setWindLevel(ACPanelModelAndWindConstant.WIND_MEDIUM3);
                            break;
                        case R.id.btnHigh:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_WIND_SETTING;
                            }
                            mAcpanel.setWindLevel(ACPanelModelAndWindConstant.WIND_STRONG5);
                            break;
                        case R.id.btnAutoWind:
                            if (device.getDeviceType() == DeviceType.AC_PANEL) {
                                command = DeviceOrder.AC_WIND_SETTING;
                            }
                            mAcpanel.setWindLevel(ACPanelModelAndWindConstant.WIND_AUTO);
                            break;
                    }
                    irButton.setACPanel(mAcpanel);
                    current_model = mAcpanel.getModel();
                    current_onoff = mAcpanel.getOnoff();
                    current_windLevel = mAcpanel.getWindLevel();
                    current_Lock = mAcpanel.getLock();
                    if (device != null) {
                        current_setTemperature = mAcpanel.getSetTemperature(device.getDeviceType());
                    }
                    refresh(current_model, current_windLevel, current_onoff, current_Lock);
                    initValue();
                }
            });
        }

    }

    private void initValue() {
        value1 = mAcpanel.getValue1();
        value2 = mAcpanel.getValue2();
        value3 = mAcpanel.getValue3();
        value4 = mAcpanel.getValue4();
        if (device.getDeviceType() == DeviceType.AC) {
            command = DeviceOrder.AC_CTRL;
        } else {
            //点击的时候去赋值
            // command = DeviceOrder.AC_POWER_SETTING;
        }
        LogUtil.d(TAG, "initAcPanelStatus() - mAcpanel:" + mAcpanel);
    }

    /* 1.电源开关关闭时其他所有按钮置灰，圆盘置灰，显示最后的室温，取不到室温用25度；设定温度显示最后数值
        ，取不到数据放在25度位置上。电源关闭时，点击其他任意灰色按钮或滑块，toast提示“设备电源未开启”
       2.锁定时其他所有按钮置灰，当前温度数值不置灰。点击其他任意灰色按钮或滑块，toast提示“按键已锁定
       3.设置温度范围：16-28℃，精度为0.1度，拨动温度球时中央显示"设置温度"，3秒无操作后显示回当前温度
        （拖动圆球，设定温度实时变化，精确到0.1度，抬手停留在当前位置，
         但发送命令精度为0.5。如设定24.3，发送24.5；设定24.2发送24；设定24.5，发送24.5）
       4.模式和风力，点击后切换小icon的显示，并根据属性报告切换icon
       5.定时倒计时，沿用COCO逻辑，只可设置开关动作和周期*/
    @Override
    public void onChangeTemperature(ACPanelCircularSeekBar view, int temperature, int color) {
        //拖动过程中，温度变化的精度是0.1度

        mTvTemperature.setTextColor(color);
        if (mAcpanel.getOnoff() == ACPanelModelAndWindConstant.AC_CLOSE) {
            mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_gray));
        }
//        DecimalFormat decimalFormat = new DecimalFormat(".#");
//        float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
//        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
//        setTemperature(temperatureText);
        setTemperature(mTvSetTemperature, temperature, false);
        //  mCircularSeekBar.setKeyName(temperatureText);
        //  mCircularSeekBar.setOrder(getCommand(temperature));
        mGradientDrawable.setColor(color);
        mTvSetTemperature.setBackgroundDrawable(mGradientDrawable);

    }

    private void setTemperature(TextView view, int temperature, boolean isSet) {
        // DecimalFormat decimalFormat = new DecimalFormat("##.#");
        // float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
        DecimalFormat decimalFormat = new DecimalFormat("##");
        //  float t = Float.parseFloat(decimalFormat.format(temperature / 10));
        Integer t = Integer.parseInt(decimalFormat.format(temperature / 10));
        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
        SpannableStringBuilder builder = new SpannableStringBuilder(temperatureText);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_huge));
        // builder.setSpan(absoluteSizeSpan, 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//设置（小数点和第一位小数）字体大小
        mCircularSeekBar.setKeyName(temperatureText);
        //mTvTemperature.setText(builder);
        // mTvSetTemperature.setText(builder);
        if (isSet) {
            if (device != null && device.getDeviceType() == DeviceType.AC_PANEL) {
                mAcpanel.setTemperature((temperature / 10) - 10);
            } else {
                mAcpanel.setTemperature((temperature / 10) - 16);
            }
        }
        view.setText(builder);
        initValue();
    }

    private void setTemperature(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_huge));
        builder.setSpan(absoluteSizeSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTemperature.setText(builder);
        mTvSetTemperature.setText(builder);
    }

    @Override
    public void onResultTemperature(ACPanelCircularSeekBar view, int temperature, int color) {
        //设置结束后，温度的精度为0.5
        //拨动温度球时中央显示"设置温度"，3秒无操作后显示回当前温度
        //mTvTemperature.setTextColor(color);
        mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_green));
        mTemperratureTips.setText(getString(R.string.ac_panel_current_temperature));
        setTemperature(mTvTemperature, temperature, true);
        mTvTemperature.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        //  mTvTemperature.setOrder(getCommand(temperature));
        mCircularSeekBar.setKeyName(temperature + getString(R.string.conditioner_temperature_unit));
        // mCircularSeekBar.setOrder(getCommand(temperature));
        //温度设置完后，mTvTemperature（IrButton）发送命令
        if (device.getDeviceType() == DeviceType.AC) {
            mTvTemperature.setOrder(DeviceOrder.AC_CTRL);
        } else {
            mTvTemperature.setOrder(DeviceOrder.AC_TEMPERATURE_SETTING);
            command = DeviceOrder.AC_TEMPERATURE_SETTING;
        }
        mTvTemperature.setACPanel(mAcpanel);

    }

    private String getCommand(int temperature) {
        return "3110" + temperature;
    }

    @Override
    public void onTouchState(int motionEvent) {
        if (motionEvent == MotionEvent.ACTION_DOWN) {
            mTemperratureTips.setText(R.string.ac_panel_setting_temperature);
            mCurrentStateLL.setVisibility(View.GONE);
            mTvSetTemperature.setVisibility(View.VISIBLE);
        } else if (motionEvent == MotionEvent.ACTION_UP) {
            mTemperratureTips.setText(R.string.ac_panel_current_temperature);
            mCurrentStateLL.setVisibility(View.VISIBLE);
            mTvSetTemperature.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSelectedAction(Action action) {
        initAcPanelStatus();
    }


    private void refresh(int model, int windLevel, int onoff, int lock) {
        //刷新模式
        int resId_model = R.drawable.pic_auto_off;
        if (model == ACPanelModelAndWindConstant.MODEL_COOL) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_cooling_on : R.drawable.pic_cooling_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_HOT) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_heating_on : R.drawable.pic_heating_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_AUTO) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_auto_on : R.drawable.pic_auto_off);
        } else if (model == ACPanelModelAndWindConstant.MODEL_SEND_WIND) {
            mIvModel.setBackgroundResource(resId_model = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_exhaust_on : R.drawable.pic_exhaust_off);
        }
        //刷新风级
        int resId_wind = R.drawable.pic_auto_wind_off;
        if (windLevel == ACPanelModelAndWindConstant.WIND_AUTO) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_auto_wind_on : R.drawable.pic_auto_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_LOW1) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_weak_wind_on : R.drawable.pic_weak_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM2 || windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM3 || windLevel == ACPanelModelAndWindConstant.WIND_MEDIUM4) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_medium_wind_on : R.drawable.pic_medium_wind_off);
        } else if (windLevel == ACPanelModelAndWindConstant.WIND_STRONG5) {
            mIvSpeed.setBackgroundResource(resId_wind = onoff == ACPanelModelAndWindConstant.AC_OPEN ? R.drawable.pic_strong_wind_on : R.drawable.pic_strong_wind_off);
        }
        if (device != null) {
            //圆盘设置最后设定的温度
            int setTemperature = mAcpanel.getSetTemperature(device.getDeviceType());//属性报告返回的是实际的设定温度，坑呀
//        if (device.getDeviceType() == DeviceType.AC_PANEL) {
//            if (setTemperature >= 32) {
//                setTemperature = setTemperature - 16;
//            }
//        }
            mCircularSeekBar.setTemperature(setTemperature * 10);
            //设置mTvTemperature显示环境温度
            setTemperature(mTvTemperature, setTemperature * 10, false);
        }
        //电源开关关闭时其他所有按钮置灰，圆盘置灰，显示最后的室温，取不到室温用25度；设定温度显示最后数值，取不到数据放在25度位置上。
        // 电源关闭时，点击其他任意灰色按钮或滑块，toast提示“设备电源未开启”
        if (onoff == ACPanelModelAndWindConstant.AC_CLOSE || lock == ACPanelModelAndWindConstant.AC_LOCK) {
            for (IrButton irButton : mIrButtons) {
                if (onoff == ACPanelModelAndWindConstant.AC_CLOSE) {
                    if (irButton.equals(mBtnOpen)) {
                        mCircularSeekBar.setInActivateStatus(true);
                        //电源关闭时其他所有按钮置灰，圆盘置灰
                        mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_gray));
                        continue;
                    }
                    irButton.setCloseStatus(true);
                    mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_off_selector));

                }
                if (lock == ACPanelModelAndWindConstant.AC_LOCK) {
                    if (irButton.equals(mBtnShutdown)) {
                        mCircularSeekBar.setLockStatus(true);
                        mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_lock_selector));
                        continue;
                    }
                    irButton.setCloseStatus(true);
                }
            }

        } else if (onoff == ACPanelModelAndWindConstant.AC_OPEN && lock == ACPanelModelAndWindConstant.AC_UNLOCK) {
            mCircularSeekBar.setInActivateStatus(false);
            mCircularSeekBar.setLockStatus(false);
            mTvTemperature.setTextColor(getResources().getColor(R.color.temprature_green));
            for (IrButton irButton : mIrButtons) {
                irButton.setCloseStatus(false);
            }
            mBtnOpen.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_onoff_on_selector));
            mBtnShutdown.setLearnDrawable(getResources().getDrawable(R.drawable.bg_ac_lockunlock_unlock_selector));

        }

    }

    @Override
    public void rightTitleClick(View v) {
        super.rightTitleClick(v);
        onBackPressed();
    }
}
