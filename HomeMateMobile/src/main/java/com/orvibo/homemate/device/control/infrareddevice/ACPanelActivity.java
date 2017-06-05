package com.orvibo.homemate.device.control.infrareddevice;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.view.custom.CircularSeekBar;
import com.orvibo.homemate.view.custom.IrButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.text.DecimalFormat;

/**
 * 控制wifi空调和空调面板
 * Created by Allen on 2016/2/22.
 * baoqi完成控制逻辑
 */
public class ACPanelActivity extends BaseIrControlActivity implements CircularSeekBar.
        OnChangeTemperatureListener, CircularSeekBar.OnTouchStateListener, IrButton.OnControlResultListener {
    private IrButton btnOpen, btnShutdown;
    private CircularSeekBar circularSeekBar;
    private View currentStateLL;
    private TextView temperatureTips, tvSetTemperature;
    private IrButton tvTemperature;
    private ImageView ivModel, ivSpeed;
    private IrButton btnCold, btnHot, btnDehumidifier, btnAuto, btnLow, btnMiddle, btnHigh, btnAutoWind;
    private GradientDrawable gradientDrawable;

    private final int COLD_FLAG = 0;
    private final int HOT_FLAG = 1;
    private final int DEHUMIDIFIER_FLAG = 2;
    private final int LOW_FLAG = 3;
    private final int MIDDLE_FLAG = 4;
    private final int HIGH_FLAG = 5;
    private final int SWEEP_FLAG = 6;
    private final int STOPSWEEP_FLAG = 7;
    private int currentFlag = -1;
    private int firstRefresh = 1;

    private final int DEFAULT_TEMPERATURE = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_panel);
        findViews();
        init();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    private void findViews() {
        btnOpen = (IrButton) findViewById(R.id.btnOpen);
        btnShutdown = (IrButton) findViewById(R.id.btnShutdown);
        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        circularSeekBar.setGravity(Gravity.NO_GRAVITY);
        circularSeekBar.initStatus(ACPanelActivity.this, uid, userName, deviceId);
        currentStateLL = findViewById(R.id.currentStateLL);
        temperatureTips = (TextView) findViewById(R.id.temperatureTips);
        tvTemperature = (IrButton) findViewById(R.id.tvTemperature);
        ivModel = (ImageView) findViewById(R.id.ivModel);
        ivSpeed = (ImageView) findViewById(R.id.ivSpeed);
        tvSetTemperature = (TextView) findViewById(R.id.tvSetTemperature);
        btnCold = (IrButton) findViewById(R.id.btnCold);
        btnHot = (IrButton) findViewById(R.id.btnHot);
        btnAuto = (IrButton) findViewById(R.id.btnAuto);
        btnDehumidifier = (IrButton) findViewById(R.id.btnDehumidifier);
        btnLow = (IrButton) findViewById(R.id.btnLow);
        btnMiddle = (IrButton) findViewById(R.id.btnMiddle);
        btnHigh = (IrButton) findViewById(R.id.btnHigh);
        btnAutoWind = (IrButton) findViewById(R.id.btnAutoWind);
        irNoButtons.add(btnOpen);
        irNoButtons.add(btnShutdown);
        irNoButtons.add(tvTemperature);
        irNoButtons.add(btnCold);
        irNoButtons.add(btnHot);
        irNoButtons.add(btnDehumidifier);
        irNoButtons.add(btnAuto);
        irNoButtons.add(btnLow);
        irNoButtons.add(btnMiddle);
        irNoButtons.add(btnHigh);
        irNoButtons.add(btnAutoWind);

    }

    private void init() {
        final NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        circularSeekBar.setOnTouchStateListener(this);
        circularSeekBar.setOnChangeTemperatureListener(this);
        circularSeekBar.setAsACPanel();
        circularSeekBar.setTemperature(DEFAULT_TEMPERATURE);
        tvTemperature.setTypeface(typeface);
        tvTemperature.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
        tvTemperature.setOrder(getCommand(DEFAULT_TEMPERATURE));
        circularSeekBar.setKeyName(DEFAULT_TEMPERATURE + getString(R.string.conditioner_temperature_unit));
        circularSeekBar.setOrder(getCommand(DEFAULT_TEMPERATURE));
        tvSetTemperature.setBackgroundDrawable(gradientDrawable);
        tvSetTemperature.setTypeface(typeface);
    }

    private void initData() {
        for (final IrButton irButton : irNoButtons) {
            irButton.initStatus(ACPanelActivity.this, uid, userName, deviceId);
        }
    }

    private void initListener() {

        for (final IrButton irButton : irNoButtons) {
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            irButton.setOnControlResultListener(this);
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irButton.onClick();
                    if (view.getTag() != null) {
                        currentFlag = Integer.parseInt(view.getTag().toString());
                    }
                }
            });
        }

    }

    private void refreshStatus(int statusFlag) {
        if (statusFlag == COLD_FLAG) {
            ivModel.setBackgroundResource(R.drawable.conditioner_cold);
        } else if (statusFlag == HOT_FLAG) {
            ivModel.setBackgroundResource(R.drawable.conditioner_hot);
        } else if (statusFlag == LOW_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_weak_wind_on);
        } else if (statusFlag == MIDDLE_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_medium_wind_on);
        } else if (statusFlag == HIGH_FLAG) {
            ivSpeed.setBackgroundResource(R.drawable.pic_strong_wind_on);
        }

    }

    private String getCommand(int temp) {
        return "3110" + temp;
    }

    @Override
    public void onChangeTemperature(CircularSeekBar view, int temperature, int color) {
        if (tvTemperature.isLearned()) {
            tvTemperature.setTextColor(color);
        }
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
        setTemperature(temperatureText);
        circularSeekBar.setKeyName(temperatureText);
        circularSeekBar.setOrder(getCommand(temperature));
        gradientDrawable.setColor(color);
        tvSetTemperature.setBackgroundDrawable(gradientDrawable);

    }

    @Override
    public void onResultTemperature(CircularSeekBar view, int temperature, int color) {
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        float t = Float.parseFloat(decimalFormat.format(temperature / 10f));
        String temperatureText = t + getString(R.string.conditioner_temperature_unit);
        setTemperature(temperatureText);
        tvTemperature.setTextColor(color);
        tvTemperature.setKeyName(temperatureText);
        tvTemperature.setOrder(getCommand(temperature));
        tvTemperature.onClick();
        circularSeekBar.setKeyName(temperatureText);
        circularSeekBar.setOrder(getCommand(temperature));

        gradientDrawable.setColor(color);
        tvSetTemperature.setBackgroundDrawable(gradientDrawable);
//        circularSeekBar.onClick();
    }

    private void setTemperature(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_huge));
        builder.setSpan(absoluteSizeSpan, 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTemperature.setText(builder);
        tvSetTemperature.setText(builder);
    }

    @Override
    public void onTouchState(int motionEvent) {
        if (motionEvent == MotionEvent.ACTION_DOWN) {
            temperatureTips.setText(R.string.conditioner_set_temperature);
            currentStateLL.setVisibility(View.GONE);
            tvSetTemperature.setVisibility(View.VISIBLE);
        } else if (motionEvent == MotionEvent.ACTION_UP) {
            temperatureTips.setText(R.string.conditioner_current_temperature);
            currentStateLL.setVisibility(View.VISIBLE);
            tvSetTemperature.setVisibility(View.GONE);
        }
    }

    @Override
    public void onControlResult(int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            refreshStatus(currentFlag);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
