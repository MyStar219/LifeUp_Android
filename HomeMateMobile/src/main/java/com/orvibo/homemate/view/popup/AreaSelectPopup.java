package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.WheelAreaAdapter;
import com.orvibo.homemate.model.location.LocationCity;
import com.orvibo.homemate.util.JSONParser;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;
import com.orvibo.homemate.view.custom.wheelview.WheelView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 省市区选择控件
 * Created by snown on 2016/4/21.
 */
public class AreaSelectPopup extends CommonPopup implements View.OnClickListener, TosGallery.OnEndFlingListener {
    private static final String TAG = AreaSelectPopup.class.getSimpleName();
    private Context mContext;

    private LinearLayout wheel_ll;
    private TextView cancel_tv;


    /**
     * 把全国的省市区的信息以json的格式保存，解析完成后赋值为null
     */
    private JSONObject mJsonObj;
    /**
     * 省的WheelView控件
     */
    private WheelView mProvince;
    /**
     * 市的WheelView控件
     */
    private WheelView mCity;
    /**
     * 区的WheelView控件
     */
    private WheelView mArea;

    /**
     * 所有省
     */
    private List<String> mProvinceDatas;
    /**
     * key - 省 value - 市s
     */
    private Map<String, List<String>> mCitisDatasMap = new HashMap<String, List<String>>();
    /**
     * key - 市 values - 区s
     */
    private Map<String, List<String>> mAreaDatasMap = new HashMap<String, List<String>>();

    /**
     * 当前省的名称
     */
    private String mCurrentProviceName;
    /**
     * 当前市的名称
     */
    private String mCurrentCityName;
    /**
     * 当前区的名称
     */
    private String mCurrentAreaName = "";

    private LocationCity.OnLocationListener onLocationListener;

    private static final int DEFAUT_PROVICE_INDEX = 9;//默认选择广东省

    public AreaSelectPopup(Context context, LocationCity.OnLocationListener onLocationListener) {
        mContext = context;
        this.onLocationListener = onLocationListener;
        initDatas();
    }


    private void initDatas() {
        mProvinceDatas = JSONParser.getProvinceList(mContext);
        for (int i = 0; i < mProvinceDatas.size(); i++) {
            String province = mProvinceDatas.get(i);
            List<String> citys = JSONParser.getCityList(mContext, i, province);
            mCitisDatasMap.put(province, citys);
            for (int j = 0; j < citys.size(); j++) {
                String city = citys.get(j);
                List<String> areas = JSONParser.getDistrictList(mContext, i, j, province, city);
                mAreaDatasMap.put(city, areas);
            }
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismiss();
        }
    };

    public void dismissPopupDelay() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    public void show(String province, String city, String area) {
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_select_area, null);
        wheel_ll = (LinearLayout) contentView.findViewById(R.id.wheel_ll);
        inAnim();

        cancel_tv = (TextView) contentView.findViewById(R.id.cancel_tv);
        cancel_tv.setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);

        mProvince = (WheelView) contentView.findViewById(R.id.province);
        mCity = (WheelView) contentView.findViewById(R.id.city);
        mArea = (WheelView) contentView.findViewById(R.id.area);
        mProvince.setScrollCycle(false);
        mProvince.setAdapter(new WheelAreaAdapter(mContext, mProvinceDatas));
        mProvince.setOnEndFlingListener(this);
        if (TextUtils.isEmpty(province)) {
            mProvince.setSelection(DEFAUT_PROVICE_INDEX);
        } else {
            mCurrentProviceName = province;
            mCurrentCityName = city;
            mCurrentAreaName = area;
            mProvince.setSelection(mProvinceDatas.indexOf(mCurrentProviceName) == -1 ? DEFAUT_PROVICE_INDEX : mProvinceDatas.indexOf(mCurrentProviceName));
        }
        mCity.setOnEndFlingListener(this);
        mCity.setScrollCycle(false);
        mArea.setOnEndFlingListener(this);
        mArea.setScrollCycle(false);
        if (!TextUtils.isEmpty(province))
            updateCities(true);
        else
            updateCities(false);
        show(mContext, contentView, true);
    }

    /**
     * 根据当前的市，更新区WheelView的信息
     */

    private void updateAreas(boolean isFirst) {
        try {
            int pCurrent = mCity.getSelectedItemPosition();
            mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName).get(pCurrent);
            List<String> areas = mAreaDatasMap.get(mCurrentCityName);
            mArea.setAdapter(new WheelAreaAdapter(mContext, areas));
            if (!TextUtils.isEmpty(mCurrentAreaName) && isFirst)
                mArea.setSelection(areas.indexOf(mCurrentAreaName) == -1 ? 0 : areas.indexOf(mCurrentAreaName));
            else
                mArea.setSelection(0);
            mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName).get(mArea.getSelectedItemPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据当前的省，更新市WheelView的信息
     *
     * @param isFirst 是否为第一次进来且有数据
     */
    private void updateCities(boolean isFirst) {
        try {
            int pCurrent = mProvince.getSelectedItemPosition();
            mCurrentProviceName = mProvinceDatas.get(pCurrent);
            List<String> cities = mCitisDatasMap.get(mCurrentProviceName);
            mCity.setAdapter(new WheelAreaAdapter(mContext, cities));
            if (!TextUtils.isEmpty(mCurrentCityName) && isFirst) {
                mCity.setSelection(cities.indexOf(mCurrentCityName) == -1 ? 0 : cities.indexOf(mCurrentCityName));
            } else
                mCity.setSelection(0);
            updateAreas(isFirst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_in));
    }

    public void outAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_out));
    }


    @Override
    public final void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_tv:
                onLocationListener.onLocation(mCurrentProviceName, mCurrentCityName, mCurrentAreaName);
                outAnim();
                dismissPopupDelay();
                break;
            case R.id.cancel_tv:
                outAnim();
                dismissPopupDelay();
                break;
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }


    @Override
    public void onEndFling(TosGallery v) {
        if (v == mProvince) {
            updateCities(false);
        } else if (v == mCity) {
            updateAreas(false);
        } else {
            mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName).get(v.getSelectedItemPosition());
            mArea.setSelection(v.getSelectedItemPosition());
        }
    }


}
