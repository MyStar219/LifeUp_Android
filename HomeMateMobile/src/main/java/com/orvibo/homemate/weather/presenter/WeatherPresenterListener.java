package com.orvibo.homemate.weather.presenter;

import com.orvibo.homemate.weather.ui.WeatherViewListener;

/**
 * Created by Smagret on 2015/11/18.
 * 天气 Presenter接口
 */
public interface WeatherPresenterListener {
    /**
     * 获取天气的逻辑
     */
    void getWeather(String cityName);

    void setWeatherView(WeatherViewListener weatherView);

}
