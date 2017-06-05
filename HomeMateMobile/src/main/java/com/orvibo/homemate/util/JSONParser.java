package com.orvibo.homemate.util;

import android.content.Context;
import android.util.Log;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONParser {
    private final static String TAG = JSONParser.class.getSimpleName();
    public static final String ADDRESS_PATH = "addr.json";

    private static JSONArray mAddrArray;
    private static List<String> mProvinceList;


    private static JSONArray getJSONArrayFromAssets(Context context, String uri) {
        /*final String path = "path/" +   "filename";*/
        JSONArray array = null;
        try {
            array = new JSONArray(
                    getStringFromAssets(context.getApplicationContext(), uri));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }


    /**
     * 从res/raw 路径下面读取数据
     */
    private static String getStringFromRawFile(Context context, int sourceId) {
        String res = "";
        try {
            InputStream inputStream = context.getApplicationContext()
                    .getResources().openRawResource(sourceId);
            int length = inputStream.available();
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, res);
        return res;
    }

    /**
     * 获取Assets路径下的文件
     */
    private static String getStringFromAssets(Context context, String uri) {

        String res = "";
        try {
            //File file = new File(uri);
            InputStream inputStream = context.getApplicationContext()
                    .getResources().getAssets().open(uri);

            int length = inputStream.available();
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<String> getProvinceList(Context context) {
        if (mAddrArray == null) {
            mAddrArray = getJSONArrayFromAssets(context, ADDRESS_PATH);
        }
        if (mProvinceList == null) {
            mProvinceList = new ArrayList<>();
            for (int i = 0; i < mAddrArray.length(); i++) {
                try {
                    JSONObject provinceObj = mAddrArray.getJSONObject(i);
                    Iterator<String> iterator = provinceObj.keys();
                    if (iterator != null && iterator.hasNext()) {
                        String provinceStr = iterator.next();
                        mProvinceList.add(provinceStr);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        return mProvinceList;
    }

    public static List<String> getCityList(Context context, int provincePos, String province) {

        List<String> cityList = new ArrayList<>();
        if (mAddrArray == null) {
            mAddrArray = getJSONArrayFromAssets(context, ADDRESS_PATH);
        }
        try {
            JSONObject provinceObj = mAddrArray.getJSONObject(provincePos);
            JSONArray cityArray = provinceObj.getJSONArray(province);
            for (int i = 0; i < cityArray.length(); i++) {
                JSONObject cityObj = cityArray.getJSONObject(i);
                Iterator<String> iterator = cityObj.keys();
                if (iterator != null && iterator.hasNext()) {
                    String cityStr = iterator.next();
                    cityList.add(cityStr);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return cityList;
    }

    public static List<String> getDistrictList(Context context, int provincePos, int cityPos, String province, String city) {
        List<String> districtList = new ArrayList<>();
        if (mAddrArray == null) {
            mAddrArray = getJSONArrayFromAssets(context, ADDRESS_PATH);
        }

        JSONObject provinceObj = null;
        try {
            provinceObj = mAddrArray.getJSONObject(provincePos);
            JSONArray cityArray = provinceObj.getJSONArray(province);
            JSONObject cityObject = cityArray.getJSONObject(cityPos);
            JSONArray districtArray = cityObject.getJSONArray(city);
            for (int i = 0; i < districtArray.length(); i++) {
                districtList.add(districtArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return districtList;
    }


}
