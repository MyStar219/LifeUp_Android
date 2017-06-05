package com.orvibo.homemate.weather.presenter;

import com.orvibo.homemate.data.WeatherInfo;
import com.orvibo.homemate.weather.model.WeatherModelListener;
import com.orvibo.homemate.weather.model.WeatherModelImpl;
import com.orvibo.homemate.weather.ui.WeatherViewListener;

/**
 * Created by Smagert on 2015/11/18.
 * 天气 Presenter实现
 */
public class WeatherPresenterImpl implements WeatherPresenterListener, OnWeatherInfoListener {
    /*Presenter作为中间层，持有View和Model的引用*/
    private WeatherViewListener weatherView;
    private WeatherModelListener weatherModel;

    public WeatherPresenterImpl() {
        weatherModel = new WeatherModelImpl();
    }

    @Override
    public void setWeatherView(WeatherViewListener weatherView) {
        this.weatherView = weatherView;
    }

    @Override
    public void getWeather(String cityName) {
        weatherModel.loadWeather(cityName, this);
    }

    @Override
    public void onSuccess(WeatherInfo weatherInfo,String greetingText) {
        weatherView.setWeatherInfo(weatherInfo,greetingText);
    }

    @Override
    public void onError() {
        weatherView.showError();
    }
}
