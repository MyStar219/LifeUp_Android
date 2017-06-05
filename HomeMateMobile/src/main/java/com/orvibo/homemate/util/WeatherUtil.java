package com.orvibo.homemate.util;


import android.content.res.Resources;
import android.text.TextUtils;

import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.LocationType;
import com.orvibo.homemate.data.WeatherInfo;
import com.orvibo.homemate.weather.CityDBDao;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class WeatherUtil {
    private static final String TAG = "WeatherUtil";
    private static String mCity = null;
    private static String mLongitude = null;
    private static String mLatotide = null;

    /**
     * 天气图标解析
     */
    private void initCityNameZH_CN() {
        String[] code_meaning = new String[48];
        code_meaning[0] = "龙卷风";
        code_meaning[1] = "热带风暴";
        code_meaning[2] = "飓风";
        code_meaning[3] = "强烈雷暴天气";
        code_meaning[4] = "雷暴天气";
        code_meaning[5] = "雨夹雪";
        code_meaning[6] = "冻雨";
        code_meaning[7] = "冰雹夹雪";
        code_meaning[8] = "冻毛雨";
        code_meaning[9] = "雾雨";
        code_meaning[10] = "冻雨";
        code_meaning[11] = "阵雨";
        code_meaning[12] = "阵雨";
        code_meaning[13] = "阵雪";
        code_meaning[14] = "小阵雪";
        code_meaning[15] = "高吹雪";
        code_meaning[16] = "雪";
        code_meaning[17] = "冰雹";
        code_meaning[18] = "冻雨";
        code_meaning[19] = "灰尘天气";
        code_meaning[20] = "有雾";
        code_meaning[21] = "薄雾";
        code_meaning[22] = "烟雾天气";
        code_meaning[23] = "大风";
        code_meaning[24] = "有风";
        code_meaning[25] = "冷";
        code_meaning[26] = "阴";
        code_meaning[27] = "晚间多云";
        code_meaning[28] = "白天多云";
        code_meaning[29] = "晚间局部多云";
        code_meaning[30] = "白天局部多云";
        code_meaning[31] = "晚间天晴";
        code_meaning[32] = "晴朗";
        code_meaning[33] = "晚间晴朗";
        code_meaning[34] = "白天晴朗";
        code_meaning[35] = "雨夹雹";
        code_meaning[36] = "热";
        code_meaning[37] = "局部风暴";
        code_meaning[38] = "零星风暴";
        code_meaning[39] = "零星风暴";
        code_meaning[40] = "局部阵雨";
        code_meaning[41] = "大雪";
        code_meaning[42] = "零星阵雪";
        code_meaning[43] = "大雪";
        code_meaning[44] = "少云";
        code_meaning[45] = "雷暴雨";
        code_meaning[46] = "阵雪";
        code_meaning[47] = "局部风暴";
    }

    /**
     * 天气图标解析
     */

    private void initCityName() {
        String[] code_meaning = new String[48];
        code_meaning[0] = "w_tornado_0";
        code_meaning[1] = "w_tropical_storm_1";
        code_meaning[2] = "w_hurricane_2";
        code_meaning[3] = "w_severe_thunderstorms_3";
        code_meaning[4] = "w_thunderstorms_4";
        code_meaning[5] = "w_mixed_rain_and_snow_5";
        code_meaning[6] = "w_mixed_rain_and_sleet_6";
        code_meaning[7] = "w_mixed_snow_and_sleet_7";
        code_meaning[8] = "w_freezing_drizzle_8";
        code_meaning[9] = "w_drizzle_9";
        code_meaning[10] = "w_freezing_rain_10";
        code_meaning[11] = "w_showers_11";
        code_meaning[12] = "w_showers_12";
        code_meaning[13] = "w_snow_flurries_13";
        code_meaning[14] = "w_light_snow_showers_14";
        code_meaning[15] = "w_blowing_snow_15";
        code_meaning[16] = "w_snow_16";
        code_meaning[17] = "w_hail_17";
        code_meaning[18] = "w_sleet_18";
        code_meaning[19] = "w_dust_19";
        code_meaning[20] = "w_foggy_20";
        code_meaning[21] = "w_haze_21";
        code_meaning[22] = "w_smoky_22";
        code_meaning[23] = "w_blustery_23";
        code_meaning[24] = "w_windy_24";
        code_meaning[25] = "w_cold_25";
        code_meaning[26] = "w_cloudy_26";
        code_meaning[27] = "w_mostly_cloudy_night_27";
        code_meaning[28] = "w_mostly_cloudy_day_28";
        code_meaning[29] = "w_partly_cloudy_night_29";
        code_meaning[30] = "w_partly_cloudy_day_30";
        code_meaning[31] = "w_clear_night_31";
        code_meaning[32] = "w_sunny_32";
        code_meaning[33] = "w_fair_night_33";
        code_meaning[34] = "w_fair_day_34";
        code_meaning[35] = "w_mixed_rain_and_hail_35";
        code_meaning[36] = "w_hot_36";
        code_meaning[37] = "w_isolated_thunderstorms_37";
        code_meaning[38] = "w_scattered_thunderstorms_38";
        code_meaning[39] = "w_scattered_thunderstorms_39";
        code_meaning[40] = "w_scattered_showers_40";
        code_meaning[41] = "w_heavy_snow_41";
        code_meaning[42] = "w_scattered_snow_showers_42";
        code_meaning[43] = "w_heavy_snow_43";
        code_meaning[44] = "w_partly_cloudy_44";
        code_meaning[45] = "w_thundershowers_45";
        code_meaning[46] = "w_snow_showers_46";
        code_meaning[47] = "w_isolated_thundershowers_47";
    }

    /**
     * 通过天气状态获取天气背景图片。
     *
     * @param code
     * @return
     */
    public static int getWeahterBgId(int code) {
        int picId = 0;
        switch (code) {
            case 0:
                picId = R.drawable.bg_weather_green;
                break;
            case 1:
                picId = R.drawable.bg_weather_green;
                break;
            case 2:
                picId = R.drawable.bg_weather_green;
                break;
            case 3:
                picId = R.drawable.bg_weather_green;
                break;
            case 4:
                picId = R.drawable.bg_weather_green;
                break;
            case 5:
                picId = R.drawable.bg_weather_green;
                break;
            case 6:
                picId = R.drawable.bg_weather_green;
                break;
            case 7:
                picId = R.drawable.bg_weather_green;
                break;
            case 8:
                picId = R.drawable.bg_weather_green;
                break;
            case 9:
                picId = R.drawable.bg_weather_green;
                break;
            case 10:
                picId = R.drawable.bg_weather_green;
                break;
            case 11:
                picId = R.drawable.bg_weather_green;
                break;
            case 12:
                picId = R.drawable.bg_weather_green;
                break;
            case 13:
                picId = R.drawable.bg_weather_green;
                break;
            case 14:
                picId = R.drawable.bg_weather_green;
                break;
            case 15:
                picId = R.drawable.bg_weather_green;
                break;
            case 16:
                picId = R.drawable.bg_weather_green;
                break;
            case 17:
                picId = R.drawable.bg_weather_green;
                break;
            case 18:
                picId = R.drawable.bg_weather_green;
                break;
            case 19:
                picId = R.drawable.bg_weather_green;
                break;
            case 20:
                picId = R.drawable.bg_weather_green;
                break;
            case 21:
                picId = R.drawable.bg_weather_green;
                break;
            case 22:
                picId = R.drawable.bg_weather_green;
                break;
            case 23:
                picId = R.drawable.bg_weather_green;
                break;
            case 24:
                picId = R.drawable.bg_weather_green;
                break;
            case 25:
                picId = R.drawable.bg_weather_green;
                break;
            case 26:
                picId = R.drawable.bg_weather_green;
                break;
            case 27:
                picId = R.drawable.bg_weather_green;
                break;
            case 28:
                picId = R.drawable.bg_weather_green;
                break;
            case 29:
                picId = R.drawable.bg_weather_green;
                break;
            case 30:
                picId = R.drawable.bg_weather_green;
                break;
            case 31:
                picId = R.drawable.bg_weather_green;
                break;
            case 32:
                picId = R.drawable.bg_weather_green;
                break;
            case 33:
                picId = R.drawable.bg_weather_green;
                break;
            case 34:
                picId = R.drawable.bg_weather_green;
                break;
            case 35:
                picId = R.drawable.bg_weather_green;
                break;
            case 36:
                picId = R.drawable.bg_weather_green;
                break;
            case 37:
                picId = R.drawable.bg_weather_green;
                break;
            case 38:
                picId = R.drawable.bg_weather_green;
                break;
            case 39:
                picId = R.drawable.bg_weather_green;
                break;
            case 40:
                picId = R.drawable.bg_weather_green;
                break;
            case 41:
                picId = R.drawable.bg_weather_green;
                break;
            case 42:
                picId = R.drawable.bg_weather_green;
                break;
            case 43:
                picId = R.drawable.bg_weather_green;
                break;
            case 44:
                picId = R.drawable.bg_weather_green;
                break;
            case 45:
                picId = R.drawable.bg_weather_green;
                break;
            case 46:
                picId = R.drawable.bg_weather_green;
                break;
            case 47:
                picId = R.drawable.bg_weather_green;
                break;
            default:
                picId = R.drawable.bg_weather_green;
                break;

        }
        return picId;
    }

    /**
     * 通过天气状态获取天气图片。
     *
     * @param code
     * @return
     */
    public static int getWeahterPicId(int code) {
        int picId = 0;
        switch (code) {
            case 0:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 1:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 2:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 3:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 4:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 5:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 6:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 7:
                picId = R.drawable.icon_code_meaning_s_7;
                break;
            case 8:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 9:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 10:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 11:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 12:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 13:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 14:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 15:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 16:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 17:
                picId = R.drawable.icon_code_meaning_s_7;
                break;
            case 18:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 19:
                picId = R.drawable.icon_code_meaning_s_19;
                break;
            case 20:
                picId = R.drawable.icon_code_meaning_s_19;
                break;
            case 21:
                picId = R.drawable.icon_code_meaning_s_19;
                break;
            case 22:
                picId = R.drawable.icon_code_meaning_s_19;
                break;
            case 23:
                picId = R.drawable.icon_code_meaning_s_23;
                break;
            case 24:
                picId = R.drawable.icon_code_meaning_s_23;
                break;
            case 25:
                picId = R.drawable.icon_code_meaning_s_25;
                break;
            case 26:
                picId = R.drawable.icon_code_meaning_s_26;
                break;
            case 27:
                picId = R.drawable.icon_code_meaning_s_28;
                break;
            case 28:
                picId = R.drawable.icon_code_meaning_s_28;
                break;
            case 29:
                picId = R.drawable.icon_code_meaning_s_28;
                break;
            case 30:
                picId = R.drawable.icon_code_meaning_s_28;
                break;
            case 31:
                picId = R.drawable.icon_code_meaning_s_32;
                break;
            case 32:
                picId = R.drawable.icon_code_meaning_s_32;
                break;
            case 33:
                picId = R.drawable.icon_code_meaning_s_32;
                break;
            case 34:
                picId = R.drawable.icon_code_meaning_s_32;
                break;
            case 35:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 36:
                picId = R.drawable.icon_code_meaning_s_36;
                break;
            case 37:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 38:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 39:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            case 40:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 41:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 42:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 43:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 44:
                picId = R.drawable.icon_code_meaning_s_44;
                break;
            case 45:
                picId = R.drawable.icon_code_meaning_s_6;
                break;
            case 46:
                picId = R.drawable.icon_code_meaning_s_5;
                break;
            case 47:
                picId = R.drawable.icon_code_meaning_s_0;
                break;
            default:
                picId = R.drawable.icon_code_meaning_s_32;
        }
        return picId;
    }

    /**
     * 通过天气状态获取天气图片。
     *
     * @param code
     * @return
     */
    public static int getWeahterBigPicId(int code) {
        int picId = 0;
        switch (code) {
            case 0:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 1:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 2:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 3:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 4:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 5:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 6:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 7:
                picId = R.drawable.icon_code_meaning_big_7;
                break;
            case 8:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 9:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 10:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 11:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 12:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 13:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 14:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 15:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 16:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 17:
                picId = R.drawable.icon_code_meaning_big_7;
                break;
            case 18:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 19:
                picId = R.drawable.icon_code_meaning_big_19;
                break;
            case 20:
                picId = R.drawable.icon_code_meaning_big_19;
                break;
            case 21:
                picId = R.drawable.icon_code_meaning_big_19;
                break;
            case 22:
                picId = R.drawable.icon_code_meaning_big_19;
                break;
            case 23:
                picId = R.drawable.icon_code_meaning_big_23;
                break;
            case 24:
                picId = R.drawable.icon_code_meaning_big_23;
                break;
            case 25:
                picId = R.drawable.icon_code_meaning_big_25;
                break;
            case 26:
                picId = R.drawable.icon_code_meaning_big_26;
                break;
            case 27:
                picId = R.drawable.icon_code_meaning_big_28;
                break;
            case 28:
                picId = R.drawable.icon_code_meaning_big_28;
                break;
            case 29:
                picId = R.drawable.icon_code_meaning_big_28;
                break;
            case 30:
                picId = R.drawable.icon_code_meaning_big_28;
                break;
            case 31:
                picId = R.drawable.icon_code_meaning_big_32;
                break;
            case 32:
                picId = R.drawable.icon_code_meaning_big_32;
                break;
            case 33:
                picId = R.drawable.icon_code_meaning_big_32;
                break;
            case 34:
                picId = R.drawable.icon_code_meaning_big_32;
                break;
            case 35:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 36:
                picId = R.drawable.icon_code_meaning_big_36;
                break;
            case 37:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 38:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 39:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            case 40:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 41:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 42:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 43:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 44:
                picId = R.drawable.icon_code_meaning_big_44;
                break;
            case 45:
                picId = R.drawable.icon_code_meaning_big_6;
                break;
            case 46:
                picId = R.drawable.icon_code_meaning_big_5;
                break;
            case 47:
                picId = R.drawable.icon_code_meaning_big_0;
                break;
            default:
                picId = R.drawable.icon_code_meaning_big_32;
        }
        return picId;
    }

    public static String getClimate(int code) {
        String picName = "";
        Resources res = ViHomeProApp.getContext().getResources();
        switch (code) {
            case 0:
                //龙卷风
                picName = res.getString(R.string.get_weatherInfo_name1);
                break;
            case 1:
                //"热带风暴"
                picName = res.getString(R.string.get_weatherInfo_name2);
                break;
            case 2:
                //"飓风"
                picName = res.getString(R.string.get_weatherInfo_name3);
                break;
            case 3:
                // picName = "强烈雷暴天气";
                picName = res.getString(R.string.get_weatherInfo_name4);
                break;
            case 4:
                // picName = "雷暴天气";
                picName = res.getString(R.string.get_weatherInfo_name5);
                break;
            case 5:
                //picName = "雨夹雪";
                picName = res.getString(R.string.get_weatherInfo_name6);
                break;
            case 6:
                // picName = "冻雨";
                picName = res.getString(R.string.get_weatherInfo_name7);
                break;
            case 7:
                //picName = "冰雹夹雪";
                picName = res.getString(R.string.get_weatherInfo_name8);
                break;
            case 8:
                // picName = "冻毛雨";
                picName = res.getString(R.string.get_weatherInfo_name9);
                break;
            case 9:
                //picName = "雾雨";
                picName = res.getString(R.string.get_weatherInfo_name10);
                break;
            case 10:
                // picName = "冻雨";
                picName = res.getString(R.string.get_weatherInfo_name11);
                break;
            case 11:
                // picName = "阵雨";
                picName = res.getString(R.string.get_weatherInfo_name12);
                break;
            case 12:
                // picName = "阵雨";
                picName = res.getString(R.string.get_weatherInfo_name13);
                break;
            case 13:
                // picName = "阵雪";
                picName = res.getString(R.string.get_weatherInfo_name14);
                break;
            case 14:
                // picName = "小阵雪";
                picName = res.getString(R.string.get_weatherInfo_name15);
                break;
            case 15:
                // picName = "高吹雪";
                picName = res.getString(R.string.get_weatherInfo_name16);
                break;
            case 16:
                // picName = "雪";
                picName = res.getString(R.string.get_weatherInfo_name17);
                break;
            case 17:
                // picName = "冰雹";
                picName = res.getString(R.string.get_weatherInfo_name18);
                break;
            case 18:
                //picName = "冻雨";
                picName = res.getString(R.string.get_weatherInfo_name19);
                break;
            case 19:
                // picName = "灰尘天气";
                picName = res.getString(R.string.get_weatherInfo_name20);
                break;
            case 20:
                // picName = "有雾";
                picName = res.getString(R.string.get_weatherInfo_name21);
                break;
            case 21:
                //picName = "薄雾";
                picName = res.getString(R.string.get_weatherInfo_name22);
                break;
            case 22:
                // picName = "烟雾天气";
                picName = res.getString(R.string.get_weatherInfo_name23);
                break;
            case 23:
                //picName = "大风";
                picName = res.getString(R.string.get_weatherInfo_name24);
                break;
            case 24:
                //picName = "有风";
                picName = res.getString(R.string.get_weatherInfo_name25);
                break;
            case 25:
                // picName = "冷";
                picName = res.getString(R.string.get_weatherInfo_name26);
                break;
            case 26:
                //picName = "阴";
                picName = res.getString(R.string.get_weatherInfo_name27);
                break;
            case 27:
                //picName = "晚间多云";
                picName = res.getString(R.string.get_weatherInfo_name28);
                break;
            case 28:
                //picName = "白天多云";
                picName = res.getString(R.string.get_weatherInfo_name29);
                break;
            case 29:
                //picName = "晚间局部多云";
                picName = res.getString(R.string.get_weatherInfo_name30);
                break;
            case 30:
                //picName = "白天局部多云";
                picName = res.getString(R.string.get_weatherInfo_name31);
                break;
            case 31:
                // picName = "晚间天晴";
                picName = res.getString(R.string.get_weatherInfo_name32);
                break;
            case 32:
                //picName = "晴朗";
                picName = res.getString(R.string.get_weatherInfo_name33);
                break;
            case 33:
                // picName = "晚间晴朗";
                picName = res.getString(R.string.get_weatherInfo_name34);
                break;
            case 34:
                //picName = "白天晴朗";
                picName = res.getString(R.string.get_weatherInfo_name35);
                break;
            case 35:
                // picName = "雨夹雹";
                picName = res.getString(R.string.get_weatherInfo_name36);
                break;
            case 36:
                //picName = "热";
                picName = res.getString(R.string.get_weatherInfo_name37);
                break;
            case 37:
                //picName = "局部风暴";
                picName = res.getString(R.string.get_weatherInfo_name38);
                break;
            case 38:
                // picName = "零星风暴";
                picName = res.getString(R.string.get_weatherInfo_name39);
                break;
            case 39:
                //picName = "零星风暴";
                picName = res.getString(R.string.get_weatherInfo_name40);
                break;
            case 40:
                //picName = "局部阵雨";
                picName = res.getString(R.string.get_weatherInfo_name41);
                break;
            case 41:
                //picName = "大雪";
                picName = res.getString(R.string.get_weatherInfo_name42);
                break;
            case 42:
                //picName = "零星阵雪";
                picName = res.getString(R.string.get_weatherInfo_name43);
                break;
            case 43:
                //picName = "大雪";
                picName = res.getString(R.string.get_weatherInfo_name44);
                break;
            case 44:
                //picName = "少云";
                picName = res.getString(R.string.get_weatherInfo_name45);
                break;
            case 45:
                //picName = "雷暴雨";
                picName = res.getString(R.string.get_weatherInfo_name46);
                break;
            case 46:
                // picName = "阵雪";
                picName = res.getString(R.string.get_weatherInfo_name47);
                break;
            case 47:
                // picName = "局部风暴";
                picName = res.getString(R.string.get_weatherInfo_name48);
                break;
        }
        return picName;
    }

    public static void getWeatherInfoByWeatherJson(GetWeatherInfoListener weatherInfoListener, String weatherJson, int locationType) throws JSONException {
        LogUtil.d(TAG, "getWeatherInfoByWeatherJson()-weatherJson:" + weatherJson + ",locationType:" + locationType);
        if (!StringUtil.isEmpty(weatherJson)) {
            JSONObject jObject = new JSONObject(weatherJson);
            JSONObject query_data = jObject.getJSONObject("query");
            if (!query_data.isNull("results")) {
                JSONObject results_data = query_data.getJSONObject("results");
                JSONObject channel_data = results_data.getJSONObject("channel");
                JSONObject location_data = channel_data.getJSONObject("location");
                JSONObject item_data = channel_data.getJSONObject("item");
                JSONArray forecast_array = item_data.getJSONArray("forecast");
                JSONObject forecast_data = forecast_array.getJSONObject(0);
                String country = location_data.getString("country");
                String cityName = location_data.getString("city");
                int high = forecast_data.getInt("high");
                int low = forecast_data.getInt("low");
                int code = forecast_data.getInt("code");

                String highTemp = "";
                String lowTemp = "";
                if (StringUtil.isEqual(country, "China")) {
                    //获取城市汉语名称
                    if (cityName.equals("Sutang")) {
                        cityName = "suzhou";
                    }

                    if (cityName.equals("Urumqi")) {
                        cityName = "wulumuqi";
                    }

                    if (cityName.equals("Hsi-an")) {
                        cityName = "xian";
                    }

                    if (cityName.equals("Hohhot")) {
                        cityName = "huhehaote";
                    }
                    //多个城市
                    cityName = cityName.toLowerCase();
                    String cityNameCN = CityDBDao.getInstance().getCityInfoByPinyin(cityName.toLowerCase());//通过Google天气返回的城市获取城市汉语名称
                    String mCityCN = CityDBDao.getInstance().getCityInfoByPinyin(mCity.toLowerCase());
                    String cityNamePinyin = CityDBDao.getInstance().getCityPinyin(mCity);
                    if (!StringUtil.isEmpty(mCity) && !(mCity.equals(cityNameCN) || mCityCN.equals(cityNameCN)
                            || cityNamePinyin.equals(cityName) || mCityCN.contains(cityNameCN) || cityNameCN.contains(mCityCN))) {
                        weatherInfoListener.onWeatherInfo(null, "");
                        return;
                    }
                    if (!StringUtil.isEmpty(cityNamePinyin)) {
                        if (cityName.equals(cityNamePinyin) || cityName.contains(cityNamePinyin) || cityNamePinyin.contains(cityName)
                                || mCity.equals(cityNameCN) || mCityCN.equals(cityNameCN) || mCityCN.contains(cityNameCN) || cityNameCN.contains(mCityCN)) {
                            cityName = mCityCN;
                        }
                    }
                    high = getCelsius(high);
                    low = getCelsius(low);
                    highTemp = "~" + String.valueOf(high) + "℃";
                    lowTemp = String.valueOf(low) + "";
                } else {
                    if (CityDBDao.getInstance().hasCity(mCity)) {
                        //搜索的城市是中国城市数据库，但返回的国家不是中国说明Google天气没有返回该城市天气信息
                        weatherInfoListener.onWeatherInfo(null, "");
                        return;
                    }

                    if (mCity.equals("Alaska")) {
                        if (!cityName.equals("Alaska")) {
                            weatherInfoListener.onWeatherInfo(null, "");
                            return;
                        }
                    } else {
                        if (cityName.equals("Alaska")) {
                            weatherInfoListener.onWeatherInfo(null, "");
                            return;
                        }
                    }
                }

                if (!PhoneUtil.isCN(ViHomeProApp.getContext())) {
                    cityName = location_data.getString("city");
                }

                if (StringUtil.isEqual(country, "United States")
                        || StringUtil.isEqual(country, "Myanmar")
                        || StringUtil.isEqual(country, "Libya")) {
                    highTemp = "~" + String.valueOf(high) + "℉";
                    lowTemp = String.valueOf(low) + "";
                }

                WeatherInfo weatherInfo = new WeatherInfo(country, cityName, highTemp, lowTemp, code);
                weatherInfoListener.onWeatherInfo(weatherInfo, "");
            } else {
                weatherInfoListener.onWeatherInfo(null, "");
            }
        } else {
            weatherInfoListener.onWeatherInfo(null, "");
        }
    }

    /**
     * 说明：首先判断cityName有无填写，如果有则按照cityName获取天气信息。如果没有填写，则按照经纬度获取天气信息。
     * 如果都没填写，则服务器根据客户端的ip来定位，并且返回天气信息。
     *
     * @param onWeatherInfoListener
     * @param cityName
     * @param latotide
     * @param longitude
     */
    public static void getWeather(final GetWeatherInfoListener onWeatherInfoListener, String cityName, String latotide, String longitude) {
//        mWeatherInfoListener = onWeatherInfoListener;
        mCity = cityName;
        mLatotide = latotide;
        mLongitude = longitude;
        String tempCity = "";
        if (!StringUtil.isEmpty(mCity)) {
            tempCity = StringUtil.replace(new StringBuffer(mCity));
        }

        String cityurl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + tempCity + "%2C%20ak%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(SELECT%20woeid%20FROM%20geo.placefinder%20WHERE%20text%3D%22" + mLongitude + "," + mLatotide + "%22%20and%20gflags%3D%22R%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        int locationType = Constant.INVALID_NUM;
        if (!StringUtil.isEmpty(cityName)) {
            locationType = LocationType.CITY_TYPE;
        } else if (StringUtil.isEmpty(cityName) && !StringUtil.isEmpty(longitude) && !StringUtil.isEmpty(latotide)) {
            locationType = LocationType.LATITUDE_LONGITUDE_TYPE;
        } else if (StringUtil.isEmpty(cityName) && StringUtil.isEmpty(longitude) && StringUtil.isEmpty(latotide)) {
            //通过ip定位获取城市名
            locationType = LocationType.IP_TYPE;
        }
        if (locationType == LocationType.CITY_TYPE) {
            if (!TextUtils.isEmpty(cityurl)) {
                cityurl = cityurl.replaceAll(" ", "%20");
            }
            LogUtil.d(TAG, "通过城市名获取天气信息 cityurl = " + cityurl);
            getWeatherByUrl(onWeatherInfoListener, cityurl, locationType);
        } else if (locationType == LocationType.LATITUDE_LONGITUDE_TYPE) {
            LogUtil.d(TAG, "通过 经纬度获取城市信息url = " + url);
            getWeatherByUrl(onWeatherInfoListener, url, locationType);
        } else {
            LogUtil.d(TAG, "无法获取城市名，通过IP定位来获取城市名，进而获取天气信息" + url);
            getWebIp(onWeatherInfoListener, "http://www.ip138.com/ip2city.asp");
        }
    }

    /**
     * @param url
     */
    public static void getWeatherByUrl(final GetWeatherInfoListener mWeatherInfoListener, String url, final int locationType) {
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(20 * 1000);// 超时时间10s
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString == null || responseString.length() == 0) {
                    return;
                }
                try {
                    getWeatherInfoByWeatherJson(mWeatherInfoListener, responseString, locationType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mWeatherInfoListener.onWeatherInfo(null, "");
            }

            @Override
            public void onFinish() {
            }
        });
    }

    public static void getWebIp(final GetWeatherInfoListener onWeatherInfoListener, String strUrl) {
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(20 * 1000);// 超时时间10s
        LogUtil.i(TAG, "getWebIp()-strUrl=" + strUrl);
        client.get(strUrl, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString == null || responseString.length() == 0) {
                    return;
                }
                int start = responseString.indexOf("[") + 1;
                int end = responseString.indexOf("]");
                if (end > start) {
                    responseString = responseString.substring(start, end);
                    LogUtil.d(TAG, "getWebIp() ip = " + responseString);
                    String url = "http://ip.taobao.com/service/getIpInfo.php?ip="
                            + responseString;
                    WeatherUtil.getHanYuCity(onWeatherInfoListener, url);
                } else {
                    onWeatherInfoListener.onWeatherInfo(null, "");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                LogUtil.e(TAG, "onFailure()-获取城市id失败");
                onWeatherInfoListener.onWeatherInfo(null, "");
            }

            @Override
            public void onFinish() {
            }

        });
    }

    public static void getHanYuCity(final GetWeatherInfoListener onWeatherInfoListener, String strUrl) {

        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(20 * 1000);// 超时时间10s
        client.get(strUrl, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString == null || responseString.length() == 0) {
                    return;
                }
                LogUtil.i(TAG, "getCityId(0)-response=" + responseString);
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(responseString).getJSONObject("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonData != null) {
                    Map<String, String> mapJson = toMap(jsonData);
                    mCity = mapJson.get("city").toString()
                            .replace("市", "").replace("县", "").replace("区", "");
                    String tempCity = StringUtil.replace(new StringBuffer(mCity));
                    String cityurl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + tempCity + "%2C%20ak%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
                    getWeatherByUrl(onWeatherInfoListener, cityurl, LocationType.CITY_TYPE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                LogUtil.e(TAG, "onFailure()-获取城市id失败");
                onWeatherInfoListener.onWeatherInfo(null, "");
            }

            @Override
            public void onFinish() {
            }

        });
    }

    /**
     * 将json对象转换成Map
     *
     * @param jsonObject json对象
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> toMap(JSONObject jsonObject) {
        Map<String, String> result = new HashMap<String, String>();
        Iterator<String> iterator = jsonObject.keys();
        String key = null;
        String value = null;
        while (iterator.hasNext()) {
            key = iterator.next();
            try {
                value = jsonObject.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.put(key, value);
        }
        return result;
    }


//    private static GetWeatherInfoListener mWeatherInfoListener;

    public interface GetWeatherInfoListener {
        void onWeatherInfo(WeatherInfo weatherInfo, String greetingText);
    }

    //1C = (F- 32)* 5/9 
    public static int getCelsius(int fahrenheit) {
        int celsius = (fahrenheit - 32) * 5 / 9;
        return celsius;
    }

    public static String HanyuToPinyin(String name) {
        String pinyinName = "";

        char[] nameChar = name.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(
                            nameChar[i], defaultFormat)[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (StringUtil.isEmpty(pinyinName)) {
            pinyinName = name;
        }
        return pinyinName;
    }


}
