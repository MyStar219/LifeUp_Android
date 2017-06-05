//package com.orvibo.homemate.weather;
//
//import android.content.Context;
//import android.location.Address;
//import android.location.Criteria;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.text.TextUtils;
//
//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationClient;
//import com.amap.api.location.AMapLocationClientOption;
//import com.amap.api.location.AMapLocationListener;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.util.LocationServiceUtil;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.StringUtil;
//
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Locale;
//
///**
// * Created by huangqiyao on 2016/1/4.
// */
//public abstract class LocationCity implements AMapLocationListener {
//    private static final String TAG = LocationCity.class.getSimpleName();
//
//    private Context mContext;
//    private LocationManager locationManager;
//    private Handler mHandler = new Handler();
//    // en_US，得到的countCode为国家简称和city的拼音。但有些手机就获取不到国家简称和city拼音
//    private static final String language = "en";
//
//    private HandlerThread mLocationThread;
//    private Handler mLocationHandler;
//    private static final int MSG_LOCATION = 0;
//
//    /**
//     * true已经回调成功结果
//     */
//    private volatile boolean isCallbacked = false;
//
//    //声明AMapLocationClient类对象
//    private AMapLocationClient mLocationClient = null;
//    //声明mLocationOption对象
//    private AMapLocationClientOption mLocationOption = null;
//
//    private String state;
//    private String city;
//
//
//    public LocationCity(Context context) {
//        this.mContext = context;
//        initAMap();
//    }
//
//    private void initAMap() {
//        //初始化定位
//        mLocationClient = new AMapLocationClient(mContext.getApplicationContext());
//        //设置定位回调监听
//        mLocationClient.setLocationListener(this);
//
//        //初始化定位参数
//        mLocationOption = new AMapLocationClientOption();
//        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置是否返回地址信息（默认返回地址信息）
//        mLocationOption.setNeedAddress(true);
//        //设置是否只定位一次,默认为false
//        mLocationOption.setOnceLocation(true);
//        //设置是否强制刷新WIFI，默认为强制刷新
//        mLocationOption.setWifiActiveScan(true);
//        //设置是否允许模拟位置,默认为false，不允许模拟位置
//        mLocationOption.setMockEnable(false);
//        //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(2000);
//        //给定位客户端对象设置定位参数
//        mLocationClient.setLocationOption(mLocationOption);
//    }
//
//    public final void location() {
//        setCallback(false);
//        if (mLocationClient != null) {
//            stop();
//        }
//        initAMap();
//        initBackThread();
//        mLocationHandler.sendEmptyMessage(MSG_LOCATION);
//    }
//
//    private void initBackThread() {
//        mLocationThread = new HandlerThread("location");
//        mLocationThread.start();
//        mLocationHandler = new Handler(mLocationThread.getLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                googleLocation();
//                amdLocation();
//            }
//        };
//    }
//
//    /**
//     * 高德定位
//     */
//    private void amdLocation() {
//        //启动定位
//        mLocationClient.startLocation();
//    }
//
//    @Override
//    public final void onLocationChanged(AMapLocation location) {
//        LogUtil.d(TAG, "onLocationChanged(amap)-location:"
//                + location);
//        if (location == null) {
//            return;
//        }
//        state = location.getProvince();
//        city = location.getCity();
//        if (location.getLatitude() != 0 || location.getLongitude() != 0) {
//            processPosition(location.getLatitude(), location.getLongitude());
//        } else {
//            LogUtil.e(TAG, "onLocationChanged(amap)-Fail to location");
//        }
//    }
//
//    private void stopAMapLocation() {
//        if (mLocationClient != null) {
//            mLocationClient.stopLocation();
//            mLocationClient.onDestroy();
//            mLocationClient = null;
//        }
//    }
//
//    public void stop() {
//        if (locationManager != null) {
//            locationManager.removeUpdates(locationListener);
//        }
//        if (mLocationHandler != null) {
//            mLocationHandler.removeMessages(MSG_LOCATION);
//        }
//        stopAMapLocation();
//        //释放资源
//        if (mLocationThread != null) {
//            mLocationThread.quit();
//        }
//    }
//
//    private void googleLocation() {
//        // 设置Criteria（服务商）的信息
//        Criteria criteria = new Criteria();
//        // 经度要求
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(false);
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        // 取得效果最好的criteria
//        locationManager = (LocationManager) mContext
//                .getSystemService(Context.LOCATION_SERVICE);
//
//        List<String> allProviders = locationManager.getAllProviders();
//        List<String> permitProviders = locationManager.getProviders(true);
//        LogUtil.d(TAG, "googleLocation()-allProviders:" + allProviders
//                + ",permitProviders:" + permitProviders);
//
//        // 注册一个周期性的更新，2000ms更新一次
//        // locationListener用来监听定位信息的改变
//        Iterator<String> iterator = permitProviders.iterator();
//        while (iterator.hasNext()) {
//            String provider = iterator.next();
//            if (provider != null) {
//                locationManager.requestLocationUpdates(provider, 2000, 5,
//                        locationListener);
//            }
//        }
//
//        try {
//            mHandler.removeCallbacks(stop);
//            mHandler.postDelayed(stop, 10 * 1000);// 设置超过6秒还没有定位到就停止定位
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void processPosition(final double latitude, final double longitude) {
//        new Thread() {
//            @Override
//            public void run() {
//                String country = "";
//                String state = "";
//                String city = "";
//                //LogUtil.d(TAG, "processPosition(0)-latitude:" + latitude + ",longitude:" + longitude + ",thread:" + Thread.currentThread());
//                Geocoder gc = new Geocoder(mContext, new Locale(language));
//                // LogUtil.d(TAG, "processPosition(1)-latitude:" + latitude + ",longitude:" + longitude);
//                try {
//                    // 取得地址相关的一些信息\经度、纬度
//                    List<Address> addresses = gc
//                            .getFromLocation(latitude, longitude, 1);
//                    LogUtil.i(TAG, "processPosition()-addresses:" + addresses);
//                    if (addresses != null && addresses.size() > 0) {
//                        Address address = addresses.get(0);
//                        if (address != null) {
//                            country = !TextUtils.isEmpty(address.getCountryCode()) ? address.getCountryCode().toLowerCase() : "";
//                            state = !TextUtils.isEmpty(address.getAdminArea()) ? address.getAdminArea().toLowerCase() : "";
//                            city = !TextUtils.isEmpty(address.getLocality()) ? address.getLocality().toLowerCase() : "";
//                        }
//
//                        if (!isCallbackedOk()) {
//                            setCallback(true);
//                            if (StringUtil.isEmpty(state)) {
//                                state = LocationCity.this.state;
//                            }
//                            if (StringUtil.isEmpty(city)) {
//                                city = LocationCity.this.city;
//                            }
//                            callback(country, state, city, latitude, longitude, 0);
//                            LocationCity.this.stop();
//                        }
//                    }
//                } catch (IOException e) {
//                    if (StringUtil.isEmpty(state)) {
//                        state = LocationCity.this.state;
//                    }
//                    if (StringUtil.isEmpty(city)) {
//                        city = LocationCity.this.city;
//                    }
//                    callback(country, state, city, latitude, longitude, 0);
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//
//    private final LocationListener locationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            LogUtil.i(TAG, "onLocationChanged(google)-location = " + location);
//            updateWithNewLocation(location);
//        }
//
//        //Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            LogUtil.d(TAG, "onStatusChanged(google)-called with " + "provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
//            switch (status) {
//                case LocationProvider.AVAILABLE:
//                    LogUtil.i(TAG, "AVAILABLE");
//                    break;
//                case LocationProvider.OUT_OF_SERVICE:
//                    LogUtil.i(TAG, "OUT_OF_SERVICE");
//                    break;
//                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    LogUtil.i(TAG, "TEMPORARILY_UNAVAILABLE");
//                    break;
//            }
//        }
//
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            LogUtil.i(TAG, "onProviderEnabled(google)");
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            LogUtil.i(TAG, "onProviderDisabled(google)");
//            updateWithNewLocation(null);
//        }
//    };
//
//    protected final void updateWithNewLocation(Location location) {
//        LogUtil.i(TAG, "updateWithNewLocation()-location:" + location);
//        if (location == null) {
//            return;
//        }
//        if (mHandler != null) {
//            mHandler.removeCallbacks(stop);
//        }
//        processPosition(location.getLatitude(), location.getLongitude());
//    }
//
//    private Runnable stop = new Runnable() {
//
//        @Override
//        public void run() {
//            LocationCity.this.stop();// 销毁掉定位
//            if (!isCallbackedOk()) {
//                setCallback(true);
//                int result = ErrorCode.FAIL;
//                if (!LocationServiceUtil.isOpenLocService(mContext)) {
//                    result = ErrorCode.PERMISSION_POSITION_REFUSE;
//                }
//                callback("", "", "", 0, 0, result);
//            }
//        }
//    };
//
//    private synchronized void setCallback(boolean b) {
//        isCallbacked = b;
//    }
//
//    private synchronized boolean isCallbackedOk() {
//        return isCallbacked;
//    }
//
//    private void callback(String country, String state, String city, double latitude, double longitude, int result) {
//        onLocation(country, state, city, latitude, longitude, result);
//    }
//
//    /**
//     * @param latitude  维度
//     * @param longitude 经度
//     * @param result    0:定位成功  1：定位失败
//     */
//    public abstract void onLocation(String country, String state, String city, double latitude, double longitude, int result);
//
//}
