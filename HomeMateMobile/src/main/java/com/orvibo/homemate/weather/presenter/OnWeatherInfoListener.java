package com.orvibo.homemate.weather.presenter;


import com.orvibo.homemate.data.WeatherInfo;

/**
 * Created by Smagret on 2015/11/18.
 * 在Presenter层实现，给Model层回调，更改View层的状态，确保Model层不直接操作View层
 */
public interface OnWeatherInfoListener {
    /**
     * 成功时回调
     *
     * @param weatherInfo
     */
    void onSuccess(WeatherInfo weatherInfo,String greetingText);
    /**
     * 失败时回调，简单处理，没做什么
     */
    void onError();

}
