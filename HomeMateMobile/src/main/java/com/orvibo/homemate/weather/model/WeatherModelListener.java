package com.orvibo.homemate.weather.model;


import com.orvibo.homemate.weather.presenter.OnWeatherInfoListener;

/**
 * Created by Smagret 2015/11/18
 * 天气Model接口
 */
public interface WeatherModelListener {
    void loadWeather(String cityName, OnWeatherInfoListener listener);
}
