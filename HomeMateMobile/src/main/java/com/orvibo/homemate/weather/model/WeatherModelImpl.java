package com.orvibo.homemate.weather.model;


import android.content.Intent;

import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Greeting;
import com.orvibo.homemate.dao.GreetingDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.WeatherInfo;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.model.QueryGreetings;
import com.orvibo.homemate.service.LocationService;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.WeatherUtil;
import com.orvibo.homemate.weather.presenter.OnWeatherInfoListener;

import de.greenrobot.event.EventBus;

/**
 * Created by Smagret on 2015/11/18.
 * 天气Model实现
 */
public class WeatherModelImpl implements WeatherModelListener, WeatherUtil.GetWeatherInfoListener {
    private static final String TAG = WeatherModelImpl.class.getSimpleName();
    private OnWeatherInfoListener mListener;
    private String mCityName;
    private QueryGreetings queryGreetings;

    @Override
    public void loadWeather(String cityName, final OnWeatherInfoListener listener) {
        /*数据层操作*/
        mListener = listener;
        mCityName = cityName;

        if (!StringUtil.isEmpty(cityName)) {
            //不用定位，直接查询
            MyLogger.sLog().d("loadWeather()-不用定位，直接查询 mCityName:" + mCityName);
            getWeatherInfo(mCityName, "", "");
        } else {
            MyLogger.sLog().d("loadWeather()-开始定位");
            locationPosition();
        }
        initUploadLoaction();
    }

    public final void onEventMainThread(LocationResultEvent event) {
        int result = event.getResult();
        String city = event.getCity();
        if (result == 0) {
            getWeatherInfo(city, "", "");
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            getWeatherInfo("", "", "");
            mListener.onError();
        } else {
            getWeatherInfo("", "", "");
            mListener.onError();
        }
    }


    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ViHomeApplication.getAppContext().startService(new Intent(ViHomeApplication.getAppContext(), LocationService.class));
    }

    private void getWeatherInfo(final String cityName, final String latitude, final String longitude) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeatherUtil.getWeather(WeatherModelImpl.this, cityName, latitude, longitude);
            }
        }).start();
    }

    private void initUploadLoaction() {
        queryGreetings = new QueryGreetings() {
            @Override
            public void onQueryGreetingsResult(int errorCode, String errorMessage) {
                if (errorCode == 0) {
                    int week = DateUtil.getWeekOfDate(null);
                    int time = DateUtil.getStartTimeAndEndTime();
                    Greeting greeting = new GreetingDao().selGreeting(week, time);
                    String greetingText = "";
                    if (greeting != null) {
                        greetingText = greeting.getText();
                    }
                    mListener.onSuccess(null, greetingText);
                } else if (errorCode == ErrorCode.NO_GREETINGS) {
                } else {
                }
            }
        };
        final int greetingVersion = new GreetingDao().selGreetingVersion(1);
        LogUtil.d(TAG, "initUploadLoaction() - greetingVersion = " + greetingVersion);


        new Thread(new Runnable() {
            @Override
            public void run() {
                queryGreetings.startQueryGreetings(PhoneUtil.getPhoneLanguage(ViHomeApplication.getAppContext()), greetingVersion);
            }
        }).start();
    }

    @Override
    public void onWeatherInfo(WeatherInfo weatherInfo, String greetingText) {
        mListener.onSuccess(weatherInfo, greetingText);
    }
}
