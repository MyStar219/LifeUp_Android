package com.orvibo.homemate.weather;

import android.os.Environment;

import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.WeatherUtil;
//import com.videogo.universalimageloader.utils.L;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by smagret on 2015/11/17.
 */
public class CityDBDao {
    private static final String FORMAT = "^[a-z,A-Z].*$";

    private CityDB mCityDB;
    private static CityDBDao cityDBDao = null;
    private static List<City> mCityList;

    public List<City> getCityList() {
        return mCityList;
    }

    /**
     * 获取CityDao对象同时将assets中的db文件拷贝到databases中，并且将数据库中城市信息载入内存中
     * @return
     */
    public static CityDBDao getInstance() {
        if (cityDBDao == null) {
            init();
        }
        return cityDBDao;
    }

    private synchronized static void init() {
        if (cityDBDao == null) {
            cityDBDao = new CityDBDao();
        }
    }

    public CityDBDao() {
        initData();
    }

    private void initData(){
        mCityDB = getCityDB();
        initCityList();
    }

    private synchronized CityDB getCityDB() {
        if (mCityDB == null || !mCityDB.isOpen())
            mCityDB = openCityDB();
        return mCityDB;
    }

    private CityDB openCityDB() {
        String packageName = ViHomeProApp.getContext().getPackageName();
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + packageName + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        if (!db.exists()) {
            try {
                InputStream is = ViHomeProApp.getContext().getAssets().open(CityDB.CITY_DB_NAME);
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(ViHomeProApp.getContext(), path);
    }

    private void initCityList() {
        mCityList = mCityDB.getAllCity();// 获取数据库中所有城市
    }

    /**
     * 通过城市名的拼音获取城市的汉语名称
     * @param cityName
     * @return
     */
    public String getCityInfoByPinyin(String cityName){
        City city = mCityDB.getCityInfoByPinyin(cityName.toLowerCase());
        if (city != null) {
            cityName = city.getName();
        }
        return cityName;
    }

    public boolean hasCity(String cityName){
        return mCityDB.hasCity(cityName);
    }

    /**
     * 将城市名的汉语转拼音
     * @param cityName
     * @return
     */
    public String getCityPinyin(String cityName){
        //String cityNamePinyin = cityName.replace("市", "").replace("县", "").replace("区", "");

        String cityNamePinyin = WeatherUtil.HanyuToPinyin(cityName);

        if (StringUtil.isEmpty(cityNamePinyin)) {
            return cityName;
        } else {
            return cityNamePinyin;
        }
    }

    public List<City> getAllCity(){
        return  mCityDB.getAllCity();
    }

    /**
     * 去掉市或县搜索
     *
     * @param city
     * @return
     */
    private String parseName(String city) {
        city = city.replaceAll("市$", "").replaceAll("县$", "")
                .replaceAll("区$", "");
        return city;
    }
}