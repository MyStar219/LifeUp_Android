package com.orvibo.homemate.weather.ui;


import com.orvibo.homemate.data.WeatherInfo;
/**
 * Created by Smagret on 2015/11/18.
 */
public interface WeatherViewListener {

    void showError();

    void setWeatherInfo(WeatherInfo weatherInfo,String greetingText);
}
