package com.orvibo.homemate.weather.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Greeting;
import com.orvibo.homemate.dao.GreetingDao;
import com.orvibo.homemate.data.WeatherInfo;
import com.orvibo.homemate.sharedPreferences.WeatherCache;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.WeatherUtil;
import com.orvibo.homemate.view.custom.MarqueeTextView;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;
import com.orvibo.homemate.weather.LocationActivity;
import com.orvibo.homemate.weather.presenter.WeatherPresenterImpl;
import com.orvibo.homemate.weather.presenter.WeatherPresenterListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smagret
 * @date 2015/11/20
 * 天气界面
 */
public class YahooWeatherView extends LinearLayout implements WeatherViewListener, View.OnClickListener {
    private static final String TAG = YahooWeatherView.class.getSimpleName();
    private TextView lowestTempTextView;
    private TextView highestTempTextView;
    private MarqueeTextView climateTextView;
    private MarqueeTextView cityNameTextView;

    private TextView lowestTempTextViewEN;
    private TextView highestTempTextViewEN;
    private MarqueeTextView climateTextViewEN;
    private MarqueeTextView cityNameTextViewEN;
    private TextView weatherHintTextView;
    private ImageView climateImageViewEN;
    private LinearLayout rootLinearLayout;
    private LinearLayout weacherLayoutEN;
    private LinearLayout weacherLayoutCN;
    private WeatherPresenterListener weatherPresenter;
    private Context mContext;

    private List<String> citys = new ArrayList<>();

    /**
     * 刷新进度
     */
    private SwipeRefreshLayout mRefreshLayout;

    public YahooWeatherView(Context context) {
        super(context);
        init(context);
    }

    public YahooWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtil.i(TAG, "YahooWeatherView()-初始化");
        init(context);
    }

    public YahooWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
        init(context);
    }


    public YahooWeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.weather_activity, this, true);

        lowestTempTextView = findView(R.id.lowestTempTextView);
        highestTempTextView = findView(R.id.highestTempTextView);
        climateTextView = findView(R.id.climateTextView);
        cityNameTextView = findView(R.id.cityNameTextView);
        weatherHintTextView = findView(R.id.weatherHintTextView);

        rootLinearLayout = findView(R.id.rootLinearLayout);
        weacherLayoutEN = findView(R.id.weacherLayoutEN);
        weacherLayoutCN = findView(R.id.weacherLayoutCN);

        lowestTempTextViewEN = findView(R.id.lowestTempTextViewEN);
        highestTempTextViewEN = findView(R.id.highestTempTextViewEN);
        climateTextViewEN = findView(R.id.climateTextViewEN);
        cityNameTextViewEN = findView(R.id.cityNameTextViewEN);
        climateImageViewEN = findView(R.id.climateImageViewEN);

        cityNameTextView.setOnClickListener(this);
        cityNameTextViewEN.setOnClickListener(this);
        weatherPresenter = new WeatherPresenterImpl(); //传入WeatherView
        weatherPresenter.setWeatherView(this);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_progress);
//        mRefreshLayout.setColorScheme(R.color.red,
//                R.color.yellow,
//                R.color.orange,
//                R.color.red);
        mRefreshLayout.setColorScheme(R.color.process_color1,
                R.color.process_color2,
                R.color.process_color3,
                R.color.process_color4);

        if (PhoneUtil.isCN(mContext)) {
            weacherLayoutCN.setVisibility(View.VISIBLE);
            weacherLayoutEN.setVisibility(View.GONE);
        } else {
            weacherLayoutCN.setVisibility(View.GONE);
            weacherLayoutEN.setVisibility(View.VISIBLE);
        }
    }

    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cityNameTextView:
            case R.id.cityNameTextViewEN:
                if (mContext != null) {
                    Intent intent = new Intent(mContext, LocationActivity.class);
                    mContext.startActivity(intent);
                }
                break;
        }
    }

    public void refreshWeather() {
        if (mContext != null) {
            String cityName = WeatherCache.getCity(mContext);
            weatherPresenter.getWeather(cityName);
        }
    }

    @Override
    public void showError() {
        //Do something
        //Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setWeatherInfo(final WeatherInfo weatherInfo, final String greetingText) {
        LogUtil.d(TAG, "setWeatherInfo()-" + weatherInfo + "greetingText = " + greetingText);

        rootLinearLayout.post(new Runnable() {
            @Override
            public void run() {
                if (weatherInfo != null) {
                    lowestTempTextView.setText(weatherInfo.getLowTemperature());
                    highestTempTextView.setText(weatherInfo.getHighTemperature());
                    lowestTempTextViewEN.setText(weatherInfo.getLowTemperature());
                    highestTempTextViewEN.setText(weatherInfo.getHighTemperature());
                    climateTextView.setText(WeatherUtil.getClimate(weatherInfo.getWeatherCode()));
                    cityNameTextView.setText(weatherInfo.getCityName());
                    climateTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().
                            getDrawable(WeatherUtil.getWeahterPicId(weatherInfo.getWeatherCode())), null, null, null);

                    climateTextViewEN.setText(WeatherUtil.getClimate(weatherInfo.getWeatherCode()));
                    cityNameTextViewEN.setText(weatherInfo.getCityName());
                    climateImageViewEN.setBackgroundResource(WeatherUtil.getWeahterBigPicId(weatherInfo.getWeatherCode()));

                    rootLinearLayout.setBackgroundResource(WeatherUtil.getWeahterBgId(weatherInfo.getWeatherCode()));
                    if (mContext != null) {
                        WeatherCache.setCity(mContext, weatherInfo.getCityName());
                        WeatherCache.setClimate(mContext, WeatherUtil.getClimate(weatherInfo.getWeatherCode()));
                        WeatherCache.setLowestTemp(mContext, weatherInfo.getLowTemperature());
                        WeatherCache.setHighestTemp(mContext, weatherInfo.getHighTemperature());
                        WeatherCache.setWeatherCode(mContext, weatherInfo.getWeatherCode());
                    }
                }

                if (!StringUtil.isEmpty(greetingText)) {
                    weatherHintTextView.setText(greetingText);
                }
            }
        });
    }

    public void initWeatherInfo() {
        rootLinearLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) {
                    return;
                }
                String cityName = WeatherCache.getCity(mContext);
                if (!StringUtil.isEmpty(cityName)) {
                    lowestTempTextView.setText(WeatherCache.getLowestTemp(mContext));
                    highestTempTextView.setText(WeatherCache.getHighestTemp(mContext));
                    climateTextView.setText(WeatherCache.getClimate(mContext));
                    int weatherCode = WeatherCache.getWeatherCode(mContext);

                    climateTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().
                            getDrawable(WeatherUtil.getWeahterPicId(weatherCode)), null, null, null);

                    lowestTempTextViewEN.setText(WeatherCache.getLowestTemp(mContext));
                    highestTempTextViewEN.setText(WeatherCache.getHighestTemp(mContext));
                    climateTextViewEN.setText(WeatherCache.getClimate(mContext));
                    climateImageViewEN.setBackgroundResource(WeatherUtil.getWeahterBigPicId(WeatherCache.getWeatherCode(mContext)));

                    rootLinearLayout.setBackgroundResource(WeatherUtil.getWeahterBgId(weatherCode));
                    cityNameTextView.setText(cityName);
                    cityNameTextViewEN.setText(cityName);
                }

                int week = DateUtil.getWeekOfDate(null);
                int time = DateUtil.getStartTimeAndEndTime();
                Greeting greeting = new GreetingDao().selGreeting(week, time);
                String greetingText = "";
                if (greeting != null) {
                    greetingText = greeting.getText();
                }
                weatherHintTextView.setText(greetingText);
            }
        });
    }

    /**
     * 释放资源。
     */
    public void release() {
        if (weatherPresenter != null) {
            weatherPresenter.setWeatherView(this);
        }
    }


    /**
     * 显示进度
     */
    public void showProgress() {
//        if (mRefreshLayout == null) {
//            mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_weather_progress);
//            mRefreshLayout.setColorSchemeResources(R.color.red, R.color.yellow, R.color.red, R.color.yellow, R.color.red, R.color.yellow);
//        }
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
            }
        });
    }

    /**
     * 停止进度
     */
    public void stopProgress() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 进度正在执行
     *
     * @return
     */
    public boolean isProgressGoing() {
//        return mRefreshLayout != null && mRefreshLayout.isRefreshing();
        return false;
    }

}
