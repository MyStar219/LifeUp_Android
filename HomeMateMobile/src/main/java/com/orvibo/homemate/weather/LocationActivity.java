package com.orvibo.homemate.weather;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.WeatherInfo;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.model.location.LocationServiceUtil;
import com.orvibo.homemate.service.LocationService;
import com.orvibo.homemate.sharedPreferences.WeatherCache;
import com.orvibo.homemate.util.AnimUtils;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeatherUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by smagret on 2015/11/17.
 */
public class LocationActivity extends BaseActivity implements View.OnClickListener,
        SearchCityAdapter.OnFilterCityListener, WeatherUtil.GetWeatherInfoListener, AnimUtils.AnimationCallback {

    private static String TAG = LocationActivity.class.getSimpleName();
    public static int ANIMATION_DURATION = 200;
    /**
     * 搜索栏默认是收缩状态，点击后搜索进入搜索模式
     */
    private boolean mIsExpanded = false;
    private int searchbarCollapsedHeight = 0;
    private int searchbarExpandedHeight = 0;
    private int searchEditTextCollapesedWidth = 0;
    private int searchEditTextExpandedWidth = 0;
    private int searchEditTextExpandedHeight = 0;
    private int searchEditTextCollapsedHeight = 0;
    private int rootCollapsedHeight = 0;
    private int rootExpandedHeight = 0;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;

    private LinearLayout searchBoxCollapsedLinearLayout;
    private LinearLayout rootLinearLayout;
    private RelativeLayout deviceEmptyLinearLayout;
    private LinearLayout searchEmptyLinearLayout;
    private LinearLayout cityContainerLinearLayoutCN;
    private LinearLayout cityContainerLinearLayoutEN;
    private RelativeLayout cityContainerRelativeLayout;
    private RelativeLayout searchBoxCollapsedRelativeLayout;
    private RelativeLayout searchBoxExpandedRelativeLayout;
    private EditText searchEditText;
    private TextView cancelSearchTextView;
    private TextView searchTipsTextView;
    private TextView searchTextView;
    private TextView searchTextView2;
    private TextView locationTextViewCN, locationTextViewEN, shanghaiTextView, beijingTextView, guangzhouTextView,
            shenzhenTextView, hangzhouTextView, wenzhouTextView, nanjingTextView, suzhouTextView,
            chengduTextView, wuhanTextView, chongqingTextView, tianjinTextView, zhengzhouTextView, fuzhouTextView, changshaTextView, titleTextView;
    private ListView cityListView;
    private ImageView topBgImageView;
    private ImageView closeImageView;
    private ImageView locationImageViewCN;
    private ImageView locationImageViewEN;
    private SearchCityAdapter mSearchCityAdapter;
    private List<City> mCityList;
    private Animation animation;
    private boolean filterFlag = true;
    private String locationCity;
    private final int WHAT_LOCATION_FAIL = 0;
    private final int WHAT_ONLY_LOCATION_SUCCESS = 1;
    private boolean onlyLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void initView() {
        cityListView = (ListView) findViewById(R.id.cityListView);
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        cityContainerRelativeLayout = (RelativeLayout) findViewById(R.id.cityContainerRelativeLayout);
        rootLinearLayout = (LinearLayout) findViewById(R.id.rootLinearLayout);
        searchBoxExpandedRelativeLayout = (RelativeLayout) findViewById(R.id.searchBoxExpandedRelativeLayout);
        searchBoxCollapsedRelativeLayout = (RelativeLayout) findViewById(R.id.searchBoxCollapsedRelativeLayout);
        searchBoxCollapsedLinearLayout = (LinearLayout) findViewById(R.id.searchBoxCollapsedLinearLayout);
        deviceEmptyLinearLayout = (RelativeLayout) findViewById(R.id.deviceEmptyLinearLayout);
        searchEmptyLinearLayout = (LinearLayout) findViewById(R.id.searchEmptyLinearLayout);
        cityContainerLinearLayoutCN = (LinearLayout) findViewById(R.id.cityContainerLinearLayoutCN);
        cityContainerLinearLayoutEN = (LinearLayout) findViewById(R.id.cityContainerLinearLayoutEN);
        cancelSearchTextView = (TextView) findViewById(R.id.cancelSearchTextView);
        searchTipsTextView = (TextView) findViewById(R.id.searchTipsTextView);
        searchTextView = (TextView) findViewById(R.id.searchTextView);
        searchTextView2 = (TextView) findViewById(R.id.searchTextView2);
        locationTextViewCN = (TextView) findViewById(R.id.locationTextViewCN);
        locationTextViewEN = (TextView) findViewById(R.id.locationTextViewEN);
        shanghaiTextView = (TextView) findViewById(R.id.shanghaiTextView);
        beijingTextView = (TextView) findViewById(R.id.beijingTextView);
        guangzhouTextView = (TextView) findViewById(R.id.guangzhouTextView);
        shenzhenTextView = (TextView) findViewById(R.id.shenzhenTextView);
        hangzhouTextView = (TextView) findViewById(R.id.hangzhouTextView);
        wenzhouTextView = (TextView) findViewById(R.id.wenzhouTextView);
        nanjingTextView = (TextView) findViewById(R.id.nanjingTextView);
        suzhouTextView = (TextView) findViewById(R.id.suzhouTextView);
        chengduTextView = (TextView) findViewById(R.id.chengduTextView);
        wuhanTextView = (TextView) findViewById(R.id.wuhanTextView);
        chongqingTextView = (TextView) findViewById(R.id.chongqingTextView);
        tianjinTextView = (TextView) findViewById(R.id.tianjinTextView);
        zhengzhouTextView = (TextView) findViewById(R.id.zhengzhouTextView);
        changshaTextView = (TextView) findViewById(R.id.changshaTextView);
        fuzhouTextView = (TextView) findViewById(R.id.fuzhouTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        topBgImageView = (ImageView) findViewById(R.id.topBgImageView);
        closeImageView = (ImageView) findViewById(R.id.closeImageView);
        locationImageViewCN = (ImageView) findViewById(R.id.locationImageViewCN);
        locationImageViewEN = (ImageView) findViewById(R.id.locationImageViewEN);

        noLocationPermissionPopup = new NoLocationPermissionPopup();
        locationFailPopup = new LocationFailPopup();

        cancelSearchTextView.setOnClickListener(this);
        topBgImageView.setOnClickListener(this);
        locationTextViewCN.setOnClickListener(this);
        locationTextViewEN.setOnClickListener(this);
        shanghaiTextView.setOnClickListener(this);
        beijingTextView.setOnClickListener(this);
        guangzhouTextView.setOnClickListener(this);
        shenzhenTextView.setOnClickListener(this);
        hangzhouTextView.setOnClickListener(this);
        wenzhouTextView.setOnClickListener(this);
        nanjingTextView.setOnClickListener(this);
        suzhouTextView.setOnClickListener(this);
        chengduTextView.setOnClickListener(this);
        wuhanTextView.setOnClickListener(this);
        chongqingTextView.setOnClickListener(this);
        tianjinTextView.setOnClickListener(this);
        zhengzhouTextView.setOnClickListener(this);
        fuzhouTextView.setOnClickListener(this);
        changshaTextView.setOnClickListener(this);
        closeImageView.setOnClickListener(this);


        mSearchCityAdapter = new SearchCityAdapter(LocationActivity.this,
                mCityList);
        mSearchCityAdapter.setFilterCityListener(this);
        cityListView.setAdapter(mSearchCityAdapter);
        cityListView.setTextFilterEnabled(true);
        searchTextView.setOnClickListener(mSearchViewOnClickListener);
        animation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
        if (PhoneUtil.isCN(mContext)) {
            cityContainerLinearLayoutCN.setVisibility(View.VISIBLE);
            cityContainerLinearLayoutEN.setVisibility(View.GONE);
            locationTextViewCN.setText(R.string.locating);
            locationImageViewCN.setBackgroundResource(R.drawable.icon_loading);
            locationImageViewCN.startAnimation(animation);
        } else {
            cityContainerLinearLayoutCN.setVisibility(View.GONE);
            cityContainerLinearLayoutEN.setVisibility(View.VISIBLE);
            locationTextViewEN.setText(R.string.locating);
            locationImageViewEN.setBackgroundResource(R.drawable.icon_loading);
            locationImageViewEN.startAnimation(animation);
        }
        onlyLocation = true;
        toLocationPosition();

        locationCity = WeatherCache.getCity(mContext);
        if (StringUtil.isEmpty(locationCity)) {
            locationCity = getResources().getString(R.string.current_city_unknow);
            titleTextView.setText(locationCity);
        } else {
            String title = String.format(getResources().getString(R.string.current_city), locationCity);
            titleTextView.setText(title);
        }

        int code = WeatherCache.getWeatherCode(mContext);
        searchBoxCollapsedLinearLayout.setBackgroundResource(WeatherUtil.getWeahterBgId(code));

        cityListView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                AnimUtils.hideInputMethod(searchEditText);
                return false;
            }
        });

        cityListView
                .setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String selectName;
                        if (PhoneUtil.isCN(mContext)) {
                            selectName = mSearchCityAdapter.getItem(position).getName() + " -" + mSearchCityAdapter.getItem(position).getProvince();
                        } else {
                            selectName = mSearchCityAdapter.getItem(position).getPinyin() + " -" + mSearchCityAdapter.getItem(position).getProvincepinyin();
                        }
                        getWeatherInfo(mSearchCityAdapter.getItem(position).getName(), "", "");
                        filterFlag = false;
                        searchEditText.setText(selectName);
                        searchEditText.setSelection(selectName.length());
                        mSearchCityAdapter.getFilter().filter(" ");
                    }
                });

        searchEditText.setOnKeyListener(mSearchEditTextLayoutListener);

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    AnimUtils.showInputMethod(v);
                } else {
                    AnimUtils.hideInputMethod(v);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MyLogger.sLog().d("s = " + s);
                if (filterFlag) {
                    if (mCityList.size() < 1 || StringUtil.isEmpty(s.toString())) {
                        showCityContainer();
                    } else {
                        hideCityContainer();
                    }
                    mSearchCityAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {

        mCityList = CityDBDao.getInstance().getCityList();
    }

    private void refresh() {

    }

    private class NoLocationPermissionPopup extends ConfirmAndCancelPopup {
        public void confirm() {
            LocationServiceUtil.gotoLocServiceSettings(mContext);
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private class LocationFailPopup extends ConfirmAndCancelPopup {
        public void confirm() {
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private void showNoLocationPermissionPopup() {
        View view = rootLinearLayout;

        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                                getResources().getString(R.string.location_no_permission_tips2),
                                getResources().getString(R.string.to_set),
                                getResources().getString(R.string.cancel));
                    }
                }
            });
        }
    }

    private void showLocationFailPopup() {
        View view = rootLinearLayout;
        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        locationFailPopup.showPopup(mContext, getResources().getString(R.string.location_fail_warm_tips),
                                getResources().getString(R.string.location_fail_tips),
                                getResources().getString(R.string.know), null);
                    }
                }
            });
        }
    }

    /**
     * 退出搜索模式，同时收缩搜索栏
     */
    public void collapse() {
        searchbarAnim(false);
        mIsExpanded = false;
    }

    /**
     * 进入搜索模式，同时展开搜索栏
     */
    public void expand() {

        searchbarAnim(true);
        mIsExpanded = true;
    }

    /**
     * @param expanding
     */
    private void searchbarAnim(boolean expanding) {
        if (searchBoxCollapsedLinearLayout == null) return;

        searchBoxCollapsedLinearLayout.clearAnimation();
        if (expanding) {
            searchbarCollapsedHeight = searchBoxCollapsedLinearLayout.getHeight();
            searchbarExpandedHeight = searchBoxExpandedRelativeLayout.getHeight();

            searchEditTextCollapesedWidth = searchEditText.getWidth();
            searchEditTextExpandedWidth = searchEditTextCollapesedWidth - cancelSearchTextView.getWidth();

            searchEditTextCollapsedHeight = searchEditText.getHeight();
            searchEditTextExpandedHeight = searchEditText.getHeight() + 5;

            rootCollapsedHeight = rootLinearLayout.getHeight();
            rootExpandedHeight = rootCollapsedHeight + (searchbarCollapsedHeight - searchbarExpandedHeight);
        }

        int toYValue = expanding ? (searchbarCollapsedHeight - searchbarExpandedHeight) * (-1) : 0;

        rootLinearLayout.animate()
                .y(toYValue)
                .setDuration(ANIMATION_DURATION)
                .start();

        AnimUtils.animateHeight(
                rootLinearLayout,
                expanding ? rootCollapsedHeight : rootExpandedHeight,
                expanding ? rootExpandedHeight : rootCollapsedHeight,
                ANIMATION_DURATION);

        AnimUtils.animateHeight(
                searchEditText,
                expanding ? searchEditTextCollapsedHeight : searchEditTextExpandedHeight,
                expanding ? searchEditTextExpandedHeight : searchEditTextCollapsedHeight,
                ANIMATION_DURATION);

        AnimUtils.animateHeight(
                searchTextView,
                expanding ? searchEditTextCollapsedHeight : searchEditTextExpandedHeight,
                expanding ? searchEditTextExpandedHeight : searchEditTextCollapsedHeight,
                ANIMATION_DURATION);

        AnimUtils.animateWidth(
                searchEditText,
                expanding ? searchEditTextCollapesedWidth : searchEditTextExpandedWidth,
                expanding ? searchEditTextExpandedWidth : searchEditTextCollapesedWidth,
                ANIMATION_DURATION, this);

        AnimUtils.animateWidth(
                searchTextView,
                expanding ? searchEditTextCollapesedWidth : searchEditTextExpandedWidth,
                expanding ? searchEditTextExpandedWidth : searchEditTextCollapesedWidth,
                ANIMATION_DURATION, this);
    }


    /**
     * Open the search UI when the user clicks on the search box.
     */
    private final View.OnClickListener mSearchViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mIsExpanded) {
                expand();
            }
        }
    };

    /**
     * If the search term is empty and the user closes the soft keyboard, close the search UI.
     */
    private final View.OnKeyListener mSearchEditTextLayoutListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN &&
                    isExpanded()) {
                boolean keyboardHidden = AnimUtils.hideInputMethod(v);
                if (keyboardHidden)
                    return true;
                collapse();
                return true;
            }
            return false;
        }
    };

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public final void onEventMainThread(LocationResultEvent event) {
        int result = event.getResult();
        String city = event.getCity();
        if (result == 0) {
            if (onlyLocation) {
                if (!PhoneUtil.isCN(mContext)) {
                    city = WeatherUtil.HanyuToPinyin(city);
                } else {
                    city = CityDBDao.getInstance().getCityInfoByPinyin(city.toLowerCase());
                }
                Message message = Message.obtain();
                message.what = WHAT_ONLY_LOCATION_SUCCESS;
                message.obj = city;
                mHandler.sendMessage(message);
                onlyLocation = false;
            } else {
                getWeatherInfo(city, "", "", false);
            }
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            dismissDialog();
            showNoLocationPermissionPopup();
            mHandler.sendEmptyMessage(WHAT_LOCATION_FAIL);
        } else {
            dismissDialog();
            //showLocationFailPopup();
            mHandler.sendEmptyMessage(WHAT_LOCATION_FAIL);
        }
    }

    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext.startService(new Intent(mContext, LocationService.class));
    }

    private void cancelLocationPositon() {
        mContext.stopService(new Intent(mContext, LocationService.class));
    }

    private void getWeatherInfo(final String cityName, final String latitude, final String longitude) {
        final String gettingWeather = getResources().getString(R.string.getting_weather);
        if (!NetUtil.isNetworkEnable(LocationActivity.this)) {
            ToastUtil.showToast(getString(R.string.network_canot_work));
            //   showLocationFailPopup();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                showDialogNow(gettingWeather);
                WeatherUtil.getWeather(LocationActivity.this, cityName, latitude, longitude);
            }
        }).start();
    }

    private void getWeatherInfo(final String cityName, final String latitude, final String longitude, final boolean showDialog) {
        if (!NetUtil.isNetworkEnable(LocationActivity.this)) {
            ToastUtil.showToast(getString(R.string.network_canot_work));
            // showLocationFailPopup();
            return;
        }
        final String gettingWeather = getResources().getString(R.string.getting_weather);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (showDialog) {
                    showDialogNow(gettingWeather);
                }
                WeatherUtil.getWeather(LocationActivity.this, cityName, latitude, longitude);
            }
        }).start();
    }


    @Override
    public void onClick(View v) {
        String gettingWeather = getResources().getString(R.string.getting_weather);
        switch (v.getId()) {
            case R.id.cancelSearchTextView:
                if (isExpanded()) {
                    collapse();
                }
                break;
            case R.id.topBgImageView:
                boolean keyboardHidden = AnimUtils.hideInputMethod(v);
                if (keyboardHidden) {
                    collapse();
                }
                break;
            case R.id.locationTextViewCN:
                locationTextViewCN.setText(R.string.getting_weather);
                locationImageViewCN.setBackgroundResource(R.drawable.icon_loading);
                locationImageViewCN.startAnimation(animation);
                toLocationPosition();
                break;
            case R.id.locationTextViewEN:
                locationTextViewEN.setText(R.string.getting_weather);
                locationImageViewEN.setBackgroundResource(R.drawable.icon_loading);
                locationImageViewEN.startAnimation(animation);
                toLocationPosition();
                break;
            case R.id.closeImageView:
                finish();
                break;
            case R.id.shanghaiTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(shanghaiTextView.getText().toString(), "", "");
                break;
            case R.id.beijingTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(beijingTextView.getText().toString(), "", "");
                break;
            case R.id.guangzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(guangzhouTextView.getText().toString(), "", "");
                break;
            case R.id.shenzhenTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(shenzhenTextView.getText().toString(), "", "");
                break;
            case R.id.hangzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(hangzhouTextView.getText().toString(), "", "");
                break;
            case R.id.wenzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(wenzhouTextView.getText().toString(), "", "");
                break;
            case R.id.nanjingTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(nanjingTextView.getText().toString(), "", "");
                break;
            case R.id.suzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(suzhouTextView.getText().toString(), "", "");
                break;
            case R.id.chengduTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(chengduTextView.getText().toString(), "", "");
                break;
            case R.id.wuhanTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(wuhanTextView.getText().toString(), "", "");
                break;
            case R.id.chongqingTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(chongqingTextView.getText().toString(), "", "");
                break;
            case R.id.tianjinTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(tianjinTextView.getText().toString(), "", "");
                break;
            case R.id.zhengzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(getResources().getString(R.string.zhengzhouEn), "", "");
                break;
            case R.id.fuzhouTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(fuzhouTextView.getText().toString(), "", "");
                break;
            case R.id.changshaTextView:
                onlyLocation = false;
                cancelLocationPositon();
                getWeatherInfo(changshaTextView.getText().toString(), "", "");
                break;
            default:
                break;
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
    }

    public void rightTitleClick(View v) {

    }

    @Override
    public void onFilter(boolean filtered, String cityName) {
        if (!filtered) {
            MyLogger.sLog().d("在本地数据库没有匹配到该城市" + cityName);
            ((ViewGroup) cityListView.getParent()).removeView(deviceEmptyLinearLayout);
            ((ViewGroup) cityListView.getParent()).addView(deviceEmptyLinearLayout);
            cityListView.setEmptyView(deviceEmptyLinearLayout);
            if (!cityName.equals(" ")) {
                if (filterFlag) {
                    searchTipsTextView.setVisibility(View.VISIBLE);
                    searchEmptyLinearLayout.setVisibility(View.GONE);
                    if (!StringUtil.isEmpty(cityName)) {
                        getWeatherInfo(cityName, "", "");
                    }
                } else {
                    searchTipsTextView.setVisibility(View.GONE);
                    searchEmptyLinearLayout.setVisibility(View.VISIBLE);
                }

            }
        } else {
            if (deviceEmptyLinearLayout != null) {
                ((ViewGroup) cityListView.getParent()).removeView(deviceEmptyLinearLayout);
            }
        }
    }

    @Override
    public void onWeatherInfo(WeatherInfo weatherInfo, String greetingText) {
        dismissDialog();
        filterFlag = true;
        if (weatherInfo != null) {
            WeatherCache.setCity(mContext, weatherInfo.getCityName());
            WeatherCache.setClimate(mContext, WeatherUtil.getClimate(weatherInfo.getWeatherCode()));
            WeatherCache.setLowestTemp(mContext, weatherInfo.getLowTemperature());
            WeatherCache.setHighestTemp(mContext, weatherInfo.getHighTemperature());
            WeatherCache.setWeatherCode(mContext, weatherInfo.getWeatherCode());
            finish();
        } else {
            //获取天气信息失败
            rootLinearLayout.post(new Runnable() {

                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        ((ViewGroup) cityListView.getParent()).removeView(deviceEmptyLinearLayout);
                        ((ViewGroup) cityListView.getParent()).addView(deviceEmptyLinearLayout);
                        cityListView.setEmptyView(deviceEmptyLinearLayout);
                        searchTipsTextView.setVisibility(View.GONE);
                        searchEmptyLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onAnimationEnd(boolean expanding) {
        if (expanding) {
            searchBoxCollapsedRelativeLayout.setVisibility(View.VISIBLE);
            topBgImageView.setVisibility(View.GONE);
            searchEditText.setText("");
            searchEditText.clearFocus();
        } else {
            searchBoxCollapsedRelativeLayout.setVisibility(View.GONE);
            topBgImageView.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
        }
    }

    @Override
    public void onAnimationCancel(boolean expanding) {

    }

    @Override
    public void onAnimationStart(boolean expanding) {


    }

    private void showCityContainer() {
        cityContainerRelativeLayout.setVisibility(View.VISIBLE);
        cityListView.setVisibility(View.GONE);
    }

    private void hideCityContainer() {
        cityContainerRelativeLayout.setVisibility(View.GONE);
        searchEmptyLinearLayout.setVisibility(View.GONE);
        cityListView.setVisibility(View.VISIBLE);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            locationImageViewCN.clearAnimation();
            locationImageViewEN.clearAnimation();
            if (message.what == WHAT_ONLY_LOCATION_SUCCESS) {
                locationCity = (String) message.obj;
                locationTextViewCN.setText(locationCity);
                locationImageViewCN.setBackgroundResource(R.drawable.icon_location_success);
                locationTextViewEN.setText(locationCity);
                locationImageViewEN.setBackgroundResource(R.drawable.icon_location_success);
            } else if (message.what == WHAT_LOCATION_FAIL) {
                locationTextViewCN.setText(R.string.location_error);
                locationImageViewCN.setBackgroundResource(R.drawable.icon_location_fail);

                locationTextViewEN.setText(R.string.location_error);
                locationImageViewEN.setBackgroundResource(R.drawable.icon_location_fail);
            }
            return true;
        }
    });

    private void toLocationPosition() {
        if (NetUtil.isNetworkEnable(LocationActivity.this)) {
            locationPosition();
        } else {
            //   ToastUtil.showToast(getString(R.string.network_canot_work));
            showLocationFailPopup();
            locationImageViewEN.clearAnimation();
            mHandler.sendEmptyMessage(WHAT_LOCATION_FAIL);
        }
    }

}
