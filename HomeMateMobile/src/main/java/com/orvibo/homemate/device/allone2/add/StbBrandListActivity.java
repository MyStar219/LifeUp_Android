package com.orvibo.homemate.device.allone2.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.SpList;
import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.model.location.LocationCity;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.AreaSelectPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * 机顶盒运营商列表
 * Created by snown on 2016/2/22.
 */
public class StbBrandListActivity extends BaseAlloneControlActivity implements LocationCity.OnLocationListener {

    private android.widget.ListView stbList;
    private android.widget.TextView areaText;
    private LocationCity locationCity;
    private View mainView;
    private static final int MSG_LOCATION_FINISH = 1;
    private String province, city, subCity;
    private TextView stbDataTip;
    private TextView stbNoDataTip;
    private AreaSelectPopup areaSelectPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_brand_list);
        this.stbNoDataTip = (TextView) findViewById(R.id.stbNoDataTip);
        this.stbDataTip = (TextView) findViewById(R.id.stbDataTip);
        this.areaText = (TextView) findViewById(R.id.areaText);
        areaText.setOnClickListener(this);
        this.stbList = (ListView) findViewById(R.id.stbList);
        this.mainView = findViewById(R.id.mainView);
        showDialog();
        locationCity = new LocationCity(mAppContext) {
            @Override
            public void onLocation(String country, String state, String city, double latitude, double longitude, int result) {

            }
        };
        locationCity.setOnLocationListener(this);
        locationCity.setUseGoogle(false);
        locationCity.location();
    }

    @Override
    public void onLocation(String province, String city, String subCity) {
        this.province = province;
        this.city = city;
        this.subCity = subCity;
        handler.sendEmptyMessage(MSG_LOCATION_FINISH);
    }

    private void getOperaters(final int areaId) {
        //获取指定AreaId下的运营商列表
        KookongSDK.getOperaters(areaId, new IRequestResult<SpList>() {

            @Override
            public void onSuccess(String msg, SpList result) {
                dismissDialog();
                mainView.setVisibility(View.VISIBLE);
                final List<SpList.Sp> sps = result.spList;
                List<String> names = new ArrayList<String>();
                for (int i = 0; i < sps.size(); i++) {
                    names.add(sps.get(i).spName);
                    MyLogger.jLog().d("The sp is " + sps.get(i).spName);
                }
                stbDataTip.setVisibility(View.VISIBLE);
                stbNoDataTip.setVisibility(View.GONE);
                stbList.setAdapter(new StbDataAdapter(names));
                stbList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SpList.Sp sp = sps.get(position);
                        if (sp.type == 1) {
                            Intent intent = new Intent();
                            intent.putExtra("spData", sp);
                            intent.putExtra("areaId", areaId);
                            intent.putExtra(IntentKey.DEVICE, device);
                            intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.STB);
                            ActivityJumpUtil.jumpAct(StbBrandListActivity.this, IPTVListActivity.class, intent);
                        } else {
                            loadIrData(com.hzy.tvmao.ir.Device.STB, 0, sp, areaId, null);
                        }
                    }
                });
            }

            @Override
            public void onFail(String msg) {
                dismissDialog();
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_FINISH:
                    if (TextUtils.isEmpty(province) || TextUtils.isEmpty(city) || TextUtils.isEmpty(subCity)) {
                        dismissDialog();
                        stbNoDataTip.setVisibility(View.VISIBLE);
                    } else {
                        showDialog();
                        areaText.setText(province + city + subCity);
                        //获取指定城市的AreaId
                        KookongSDK.getAreaId(province, city, subCity, new IRequestResult<Integer>() {

                            @Override
                            public void onSuccess(String msg, Integer result) {
                                getOperaters(result);
                            }

                            @Override
                            public void onFail(String msg) {
                                dismissDialog();
                                ToastUtil.showToast(R.string.allone_error_data_tip);
                            }
                        });
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.areaText:
                areaSelectPopup = new AreaSelectPopup(StbBrandListActivity.this, this);
                areaSelectPopup.show(province, city, subCity);
                break;
        }
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }
}
