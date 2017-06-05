package com.orvibo.homemate.device.timing;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.UploadLocation;
import com.orvibo.homemate.model.device.DeviceDeletedReport;
import com.orvibo.homemate.api.listener.OnDeviceDeletedListener;
import com.orvibo.homemate.model.location.LocationServiceUtil;
import com.orvibo.homemate.service.LocationService;
import com.orvibo.homemate.sharedPreferences.LocationCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.JudgeLocationUtil;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.TimingCountdownTabView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import de.greenrobot.event.EventBus;

/**
 * @author Smagret
 * @date 2015/12/08
 */
public class TimingCountdownActivity extends BaseActivity implements TimingCountdownTabView.OnTabSelectedListener,
        DeviceTimingListFragment.OnTimingSelectedListener, DeviceCountdownListFragment.OnCountdownSelectedListener, OnDeviceDeletedListener {
    private static final String TAG = TimingCountdownActivity.class.getSimpleName();

    private DeviceTimingListFragment mDeviceTimingListFragment;
    private DeviceCountdownListFragment mDeviceCountdownListFragment;
    private TimingCountdownTabView topTimingCountdownTabView;
    private ImageView confirmImageView;
    private Device device;
    private int currentPosition = 0;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;

    private UploadLocation uploadLocation;
    private RelativeLayout bar_rl;
    private int latestType;
    private Acpanel mAcPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timing_countdown_container);
        bar_rl = (RelativeLayout) findViewById(R.id.bar_rl);

        Intent intent = getIntent();
        boolean isSingle = intent.getBooleanExtra("isSingle", false);
        if (isSingle) {
            ((TimingCountdownTabView) findViewById(R.id.topTimingCountdownTabView)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.title_text)).setVisibility(View.VISIBLE);
            latestType = intent.getIntExtra("latestType", 0);
            if (latestType == 0) {
                ((TextView) findViewById(R.id.title_text)).setText(R.string.timing);
            } else {
                ((TextView) findViewById(R.id.title_text)).setText(R.string.countdown_);
            }
        } else {
            ((TextView) findViewById(R.id.title_text)).setVisibility(View.GONE);
            ((TimingCountdownTabView) findViewById(R.id.topTimingCountdownTabView)).setVisibility(View.VISIBLE);
            latestType = intent.getIntExtra("latestType", 0);
        }

        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        //获取acpanel对象
        mAcPanel = (Acpanel) intent.getSerializableExtra(IntentKey.ACPANEL);
        if (device == null) {
            finish();
        }
        confirmImageView = (ImageView) findViewById(R.id.confirmImageView);
//        latestType = intent.getIntExtra("latestType", 0);
//        load = new Load(mAppContext);
        loadTimer();
        initTopTab();
        noLocationPermissionPopup = new NoLocationPermissionPopup();
        locationFailPopup = new LocationFailPopup();

        mIntentSource = intent.getStringExtra(IntentKey.INTENT_SOURCE);
        DeviceDeletedReport.getInstance().addDeviceDeletedListener(this);
    }

    /**
     */
    private void initTopTab() {
        topTimingCountdownTabView = (TimingCountdownTabView) findViewById(R.id.topTimingCountdownTabView);
        topTimingCountdownTabView.setOnTabSelectedListener(this);
        if (latestType == TimingCountdownTabView.TIMING_POSITION) {
            topTimingCountdownTabView.setSelectedPosition(TimingCountdownTabView.TIMING_POSITION);
        } else {
            topTimingCountdownTabView.setSelectedPosition(TimingCountdownTabView.COUNTDOWN_POSITION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bar_rl.setBackgroundColor(getResources().getColor(R.color.green));
        topTimingCountdownTabView.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishingOrDestroyed()) {
                    if (!LocationCache.getUploadFlag(mContext, userId)) {
                        locationPosition();
                    }
                }
            }
        });
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (mDeviceTimingListFragment != null) {
            mDeviceTimingListFragment.onRefresh();
        }
        if (mDeviceCountdownListFragment != null) {
            mDeviceCountdownListFragment.onRefresh();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment != null) {
            if (mDeviceTimingListFragment == null && fragment instanceof DeviceTimingListFragment) {
                mDeviceTimingListFragment = (DeviceTimingListFragment) fragment;
            } else if (mDeviceCountdownListFragment == null && fragment instanceof DeviceCountdownListFragment) {
                mDeviceCountdownListFragment = (DeviceCountdownListFragment) fragment;
            }
        }
        super.onAttachFragment(fragment);
    }

    @Override
    public void onTabSelected(int position) {
        LogUtil.d(TAG, "onTabSelected()-position:" + position);
        if (isFinishingOrDestroyed()) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // hideAllFragment(transaction);
        switch (position) {
            case TimingCountdownTabView.TIMING_POSITION:
                hideFragment(transaction, mDeviceCountdownListFragment);
                if (mDeviceTimingListFragment == null) {
                    mDeviceTimingListFragment = new DeviceTimingListFragment();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(Constant.DEVICE, device);
                    //传递acpanel对象
                    mBundle.putSerializable(IntentKey.ACPANEL, mAcPanel);
                    //通过setArguments给fragment传递数据
                    mDeviceTimingListFragment.setArguments(mBundle);
                }
                currentPosition = TimingCountdownTabView.TIMING_POSITION;
                showFragment(transaction, mDeviceTimingListFragment);
                break;
            case TimingCountdownTabView.COUNTDOWN_POSITION:
                hideFragment(transaction, mDeviceTimingListFragment);
                if (mDeviceCountdownListFragment == null) {
                    mDeviceCountdownListFragment = new DeviceCountdownListFragment();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(Constant.DEVICE, device);
                    mBundle.putSerializable(IntentKey.ACPANEL, mAcPanel);
                    //通过setArguments给fragment传递数据
                    mDeviceCountdownListFragment.setArguments(mBundle);
                }
                currentPosition = TimingCountdownTabView.COUNTDOWN_POSITION;
                showFragment(transaction, mDeviceCountdownListFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideAllFragment(FragmentTransaction transaction) {
        if (mDeviceTimingListFragment != null) {
            transaction.hide(mDeviceTimingListFragment);
        }
        if (mDeviceCountdownListFragment != null) {
            transaction.hide(mDeviceCountdownListFragment);
        }
    }

    private void hideFragment(FragmentTransaction transaction, BaseFragment fragment) {
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private synchronized void showFragment(FragmentTransaction ft, BaseFragment baseFragment) {
        boolean isAdded = baseFragment.isAdded();
        LogUtil.d(TAG, "showFragment()-baseFragment:" + baseFragment + ",isAdded:" + isAdded);
        if (!isAdded) {
            ft.add(R.id.container, baseFragment);
        } else {
            ft.show(baseFragment);
        }
        baseFragment.onVisible();
    }

    private void loadTimer() {
        if (NetUtil.isNetworkEnable(mAppContext)) {
            LoadUtil.noticeLoadData(mAppContext, device.getUid(), Constant.INVALID_NUM);
        }
    }

    @Override
    public void onLeftButtonClick(View view) {

    }

    public void rightTitleClick(View view) {
        boolean uploadFlag = LocationCache.getUploadFlag(mContext, userId);
        if (PhoneUtil.isCN(this) || JudgeLocationUtil.isLocation(UserCache.getCurrentUserId(this)) || uploadFlag) {// 判断是否是否有地位信息(有上传信息标志也行)
            toSetTimingCoundown();
        } else if (!NetUtil.isNetworkEnable(this)) {
            locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
                    getResources().getString(R.string.location_fail_tips),
                    getResources().getString(R.string.know), null);

        } else if (!LocationServiceUtil.isOpenLocService(this)) {
            noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                    getResources().getString(R.string.location_no_permission_tips),
                    getResources().getString(R.string.to_set),
                    getResources().getString(R.string.cancel));
        } else {
            locationPosition();
        }

    }

    private void toSetTimingCoundown() {
        if ((device != null && ProductManage.getInstance().isWifiDevice(device)) || (device != null && device.getDeviceType() == DeviceType.AC_PANEL) || (device != null && device.getDeviceType() == DeviceType.AC)) {
            if (currentPosition == TimingCountdownTabView.TIMING_POSITION) {
                Intent intent = new Intent(mContext, DeviceTimingCreateActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                //点击添加传递acpanel对象
                intent.putExtra(IntentKey.ACPANEL, mAcPanel);
                startActivity(intent);
            } else if (currentPosition == TimingCountdownTabView.COUNTDOWN_POSITION) {
                Intent intent = new Intent(mContext, DeviceCountdownCreateActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                //点击添加传递acpanel对象
                intent.putExtra(IntentKey.ACPANEL, mAcPanel);
                startActivity(intent);
            }
        }
    }

    public final void onEventMainThread(LocationResultEvent event) {
        String country = event.getCountry();
        String state = event.getState();
        String city = event.getCity();
        double latitude = event.getLatitude();
        double longitude = event.getLongitude();
        int result = event.getResult();

        if (result == 0) {
            String latitudeString = String.valueOf(latitude);
            String longitudeString = String.valueOf(longitude);
            initUploadLoaction();
            uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(mContext),
                    longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            // 当定位的权限关闭，
            if (!PhoneUtil.isCN(TimingCountdownActivity.this)) {
                noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            }
            //  }
        } else {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(TimingCountdownActivity.this)) {
                locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
                        getResources().getString(R.string.location_fail_tips),
                        getResources().getString(R.string.know), null);
            }

            //  }
        }

    }


    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext.startService(new Intent(mContext, LocationService.class));
//                if (result == 0) {
//                    String latitudeString = String.valueOf(latitude);
//                    String longitudeString = String.valueOf(longitude);
//                    uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(mContext),
//                            longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
//                } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
//                    // if (LocationCache.getTimeLag(mContext, userId)) {
//                    noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
//                            getResources().getString(R.string.location_no_permission_tips),
//                            getResources().getString(R.string.to_set),
//                            getResources().getString(R.string.cancel));
//                    //  }
//                } else {
//                    // if (LocationCache.getTimeLag(mContext, userId)) {
//                    locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
//                            getResources().getString(R.string.location_fail_tips),
//                            getResources().getString(R.string.know), null);
//                    //   }
//                }
//                //  LocationCache.saveUploadFlag(mContext, true, userId);
//            }
//        };
        // mLocationCity.location();
    }

    @Override
    public void onEmptyTiming(boolean emptyFlag) {
        if (currentPosition == TimingCountdownTabView.TIMING_POSITION) {
            if (emptyFlag) {
                confirmImageView.setVisibility(View.GONE);
            } else {
                confirmImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @param emptyFlag true 非空；false 空
     */
    @Override
    public void onEmptyCountdown(boolean emptyFlag) {
        if (currentPosition == TimingCountdownTabView.COUNTDOWN_POSITION) {
            if (emptyFlag) {
                confirmImageView.setVisibility(View.GONE);
            } else {
                confirmImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDeviceDeleted(String uid, String deviceId) {
        //从DeviceFragment直接进入此页面，当监听到此设备被删除时直接调回首页
        if (!TextUtils.isEmpty(uid) && device != null
                && uid.equals(device.getUid())
                && !TextUtils.isEmpty(mIntentSource)
                && mIntentSource.equals(DeviceFragment.class.getSimpleName())) {
            if (TextUtils.isEmpty(deviceId) || deviceId.equals(device.getDeviceId())) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    private class NoLocationPermissionPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            LocationServiceUtil.gotoLocServiceSettings(mContext);
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private class LocationFailPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }


    private void initUploadLoaction() {
        uploadLocation = new UploadLocation() {
            @Override
            public void onUploadLoactionResult(int errorCode, String errorMessage) {
                LogUtil.e(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                if (errorCode == 0) {
                    LocationCache.saveUploadFlag(mContext, true, userId);
                    toSetTimingCoundown();
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceDeletedReport.getInstance().removeDeviceDeletedListener(this);
    }
}