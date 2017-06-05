package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DataStatus;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.RealTimeStatus;
import com.orvibo.homemate.common.adapter.MyFragmentPagerAdapter;
import com.orvibo.homemate.core.load.LoadTarget;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.RealTimeStatusDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TableName;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.event.TmperatureUnitEvent;
import com.orvibo.homemate.core.load.loadtable.LoadTable;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.sharedPreferences.TipsCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.TipsLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by snow on 2016/2/24.
 *
 * @描述 温湿度记录界面展示
 */
public class TemperatureAndHumidityActivity extends BaseControlActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, LoadTable.OnLoadTableListener {
    private static final String TAG = "TemperatureAndHumidityActivity";
    private ViewPager viewPager;
    private int currentIndex;
    private android.widget.TextView contentTip;
    private ImageView tmpImage;
    private android.widget.TextView dateTextView;
    private NavigationGreenBar navigationGreenBar;
    private TextView currentData, currentUnit, unitTip, noDataTip;
    private Map<String, List<RealTimeStatus>> hashMap = new LinkedHashMap<>();
    private List<String> dates = new ArrayList<>();//存储时间
    private List<DataStatus> dataStatuses = new ArrayList<>();//每天的温湿度数据集合封装类
    private List<Fragment> fragments;//多天的曲线图fragment集合
    private ImageView leftImageView, rightImageView;
    private DeviceStatus deviceStatus;
    private DeviceStatusDao deviceStatusDao;
    private TipsLayout tipsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_and_humidity_activity);
        deviceStatusDao = new DeviceStatusDao();
        deviceStatus = new DeviceStatusDao().selDeviceStatus(deviceId);
        findViews();
        if (device != null)
            initViewData();
    }

    private void findViews() {
        this.viewPager = (ViewPager) findViewById(R.id.viewPager);
        this.dateTextView = (TextView) findViewById(R.id.dateTextView);
        this.tmpImage = (ImageView) findViewById(R.id.tmpImage);
        this.leftImageView = (ImageView) findViewById(R.id.leftImageView);
        this.rightImageView = (ImageView) findViewById(R.id.rightImageView);
        this.contentTip = (TextView) findViewById(R.id.contentTip);
        this.navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationGreenBar);
        this.currentData = (TextView) findViewById(R.id.currentData);
        this.currentUnit = (TextView) findViewById(R.id.currentUnit);
        this.unitTip = (TextView) findViewById(R.id.unitTip);
        this.noDataTip = (TextView) findViewById(R.id.noDataTip);
        tipsLayout = (TipsLayout) findViewById(R.id.tipsLayout);
    }

    private void initViewData() {
        navigationGreenBar.setMiddleTextColor(getResources().getColor(R.color.black));
        if (device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
            contentTip.setText(getString(R.string.conditioner_current_temperature));
            tmpImage.setImageResource(R.drawable.pic_temperature);
            currentData.setTextColor(getResources().getColor(R.color.yellow));
            currentUnit.setTextColor(getResources().getColor(R.color.yellow));
            currentUnit.setText(CommonCache.getTemperatureUnit());
            unitTip.setText(String.format(getString(R.string.temperature_tip), CommonCache.getTemperatureUnit()));
        } else if (device.getDeviceType() == DeviceType.HUMIDITY_SENSOR) {
            contentTip.setText(getString(R.string.current_humidity));
            tmpImage.setImageResource(R.drawable.pic_water);
            currentData.setTextColor(getResources().getColor(R.color.green));
            currentUnit.setTextColor(getResources().getColor(R.color.green));
            currentUnit.setText("%");
            unitTip.setText(getString(R.string.humidity_tip));
        }
        String language = PhoneUtil.getPhoneLanguage(mAppContext);
        String tips = TipsCache.getTips(language);
        List<String> tipsList = new ArrayList<>();
        if (!TextUtils.isEmpty(tips)) {
            try {
                JSONArray tipsJsonArray = new JSONArray(tips);
                for (int i = 0; i < tipsJsonArray.length(); i++) {
                    JSONObject tipsJsonObject = tipsJsonArray.getJSONObject(i);
                    int deviceType = tipsJsonObject.getInt("deviceType");
                    if (deviceType == device.getDeviceType()) {
                        String text = tipsJsonObject.getString("text").replace("\\n", "\n");
                        tipsList.add(text);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tipsLayout.setTipsList(tipsList);
        if (TipsCache.isShowGuide()) {
            tipsLayout.sleep();
        } else {
            TipsCache.hasShowGuide(true);
        }
        setValue();
        new DataLoad(false).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            deviceName = device.getDeviceName();
            navigationGreenBar.setText(deviceName);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public void rightTitleClick(View v) {
        Intent intent = new Intent(this, SetDeviceActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }


    @Override
    public void onPageSelected(int position) {
        currentIndex = position;
        setSeletBg();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftImageView:
                currentIndex--;
                if (currentIndex < 0)
                    currentIndex = 0;
                viewPager.setCurrentItem(currentIndex);
                setSeletBg();
                break;
            case R.id.rightImageView:
                currentIndex++;
                if (currentIndex >= dates.size())
                    currentIndex = dates.size() - 1;
                viewPager.setCurrentItem(currentIndex);
                setSeletBg();
                break;
            case R.id.btnToday:
                viewPager.setCurrentItem(dates.size() > 0 ? dates.size() - 1 : 0);
                setSeletBg();
                break;
        }
    }

    /**
     * 设置选择按钮处理事件
     */
    private void setSeletBg() {
        setDateTextView(currentIndex, null);
        if (dates.size() > 1) {
            if (currentIndex == 0) {
                leftImageView.setEnabled(false);
                rightImageView.setEnabled(true);
            } else if (currentIndex == dates.size() - 1) {
                leftImageView.setEnabled(true);
                rightImageView.setEnabled(false);
            } else {
                leftImageView.setEnabled(true);
                rightImageView.setEnabled(true);
            }
        } else {
            leftImageView.setEnabled(false);
            rightImageView.setEnabled(false);
        }
    }

    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (!isFinishingOrDestroyed()) {
            if (deviceStatus != null && device != null) {
                if (device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
                    deviceStatus.setValue1(value1);
                } else if (device.getDeviceType() == DeviceType.HUMIDITY_SENSOR) {
                    deviceStatus.setValue2(value2);
                }
                startLoadData();
                deviceStatusDao.updDeviceStatus(deviceStatus);
            }
            setValue();
        }
    }

    /**
     * 设置温度显示
     */
    private void setValue() {
        if (deviceStatus != null && device != null) {
            if (device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
                if (!CommonCache.isCelsius()) {
                    currentData.setText(MathUtil.geFahrenheitData(MathUtil.getRoundData(deviceStatus.getValue1())) + "");
                } else
                    currentData.setText(MathUtil.getRoundData(deviceStatus.getValue1()) + "");
            } else if (device.getDeviceType() == DeviceType.HUMIDITY_SENSOR) {
                currentData.setText(MathUtil.getRoundData(deviceStatus.getValue2()) + "");
            }
        }
    }

    /**
     * 设置日期显示
     *
     * @param postion
     * @param time
     */
    private void setDateTextView(int postion, String time) {
        if (time == null)
            dateTextView.setText(dates.size() > 0 && postion >= 0 ? dates.get(postion) : null);
        else
            dateTextView.setText(time);
    }

    @Override
    public void onLoadTableFinish(LoadTarget loadTarget, int result) {
        LoadTable.getInstance(getApplicationContext()).removeListener(TemperatureAndHumidityActivity.this);
//        RealTimeStatusDao.getInstance().selDeviceRealTimeStatuses(deviceId);
        LogUtil.i(TAG, "onLoadTableFinish()-loadTarget:" + loadTarget + ",result:" + result);
        if (!isFinishingOrDestroyed() && result == ErrorCode.SUCCESS) {
            new DataLoad(true).execute();
        }
    }

    /**
     * 异步加载数据
     */
    private class DataLoad extends AsyncTask<Void, Void, Void> {
        public boolean isReload = false;

        public DataLoad(boolean isReload) {
            this.isReload = isReload;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!isReload) {
                startLoadData();
            }
            initRealData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            initData();
        }
    }

    /**
     * 获取到数据后加载到界面控件
     */
    private void initData() {
        setSeletBg();
        setValue();
        if (fragments != null && !fragments.isEmpty()) {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //把所有缓存碎片都删了。
                for (Fragment childFragment : fragments) {
                    fragmentTransaction.remove(childFragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
                //再全部重新创建。
                fragments.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fragments = new ArrayList<>();
        if (dataStatuses.size() > 0) {
            for (int i = 0; i < dataStatuses.size(); i++) {
                Fragment fragment = new TemperatureAndHumidityFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("dataStatus", dataStatuses.size() > 0 ? dataStatuses.get(i) : null);
                bundle.putInt("deviceType", device.getDeviceType());
                fragment.setArguments(bundle);
                fragments.add(fragment);
            }
            viewPager.setVisibility(View.VISIBLE);
            unitTip.setVisibility(View.VISIBLE);
            MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
            viewPager.setAdapter(adapter);
            viewPager.setOnPageChangeListener(this);
            if (dates.size() > 0) {
                viewPager.setCurrentItem(dates.size() - 1);
                currentIndex = viewPager.getCurrentItem();
                setDateTextView(dates.size() - 1, null);
            }
        } else {
            noDataTip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化实时数据
     */
    private void initRealData() {
        dataStatuses.clear();
        dates.clear();
        hashMap = RealTimeStatusDao.getInstance().getRealTimeStatusList(deviceId);
        for (String key : hashMap.keySet()) {
            dates.add(key);
        }
        for (String date : dates) {
            List<RealTimeStatus> realTimeStatuses = hashMap.get(date);
            DataStatus dataStatus = RealTimeStatusDao.getInstance().getMaxAndMinData(hashMap.get(date), device.getDeviceType());
            ArrayList<Entry> yVals = new ArrayList<Entry>();
            if (device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
                //
                for (RealTimeStatus realTimeStatus : realTimeStatuses) {
                    Entry entry = new Entry(realTimeStatus.getValue1(), TimeUtil.getSecondTime(realTimeStatus.getCreateTime()));
                    yVals.add(entry);
                }
            } else {
                for (RealTimeStatus realTimeStatus : realTimeStatuses) {
                    Entry entry = new Entry(realTimeStatus.getValue2(), TimeUtil.getSecondTime(realTimeStatus.getCreateTime()));
                    yVals.add(entry);
                }
            }
            dataStatus.setEntries(yVals);
            dataStatuses.add(dataStatus);
        }
    }

    @Override
    protected void onDestroy() {
        LoadTable.getInstance(mAppContext).removeListener(this);
        super.onDestroy();
    }


    public void onEventMainThread(TmperatureUnitEvent event) {
        if (device.getDeviceType() == DeviceType.TEMPERATURE_SENSOR) {
            //单位切换后重新刷新界面和数据
            currentUnit.setText(CommonCache.getTemperatureUnit());
            unitTip.setText(String.format(getString(R.string.temperature_tip), CommonCache.getTemperatureUnit()));
            new DataLoad(false).execute();
            setValue();
        }
    }

    /**
     * 去服务器下载数据
     */
    private void startLoadData() {
        LoadTable.getInstance(getApplicationContext()).load(UserCache.getCurrentMainUid(getApplicationContext()), TableName.R_STATUS);
        LoadTable.getInstance(getApplicationContext()).setOnLoadTableListener(TemperatureAndHumidityActivity.this);
    }


}
