package com.orvibo.homemate.device.timing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CountdownDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.CountdownConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.model.ActivateCountdown;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.UploadLocation;
import com.orvibo.homemate.model.location.LocationCity;
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
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 倒计时列表
 * Created by smagret on 2015/12/08
 */
public class DeviceCountdownListFragment extends BaseFragment implements
        AdapterView.OnItemClickListener,
//        Load.OnLoadListener,
        OnPropertyReportListener {
    private static final String TAG = DeviceCountdownListFragment.class
            .getSimpleName();
    private int WHAT_LOAD_TIMER = 1;

    private ListView lvCountdown;
    private TextView tvAddCountdown;
    private LinearLayout emptyView = null;
    private View view;
    private DeviceCountdownAdapter deviceCountdownAdapter;
    private ToastPopup toastPopup;

    private List<Countdown> countdowns;
    private Countdown countdown;
    private Action action;
    private String uid;
    private Device device;
    private String deviceId;
    private String countdownId;
    private int isPause;

    private CountdownDao countdownDao;
    private ActivateCountdownControl activateCountdownControl;
//    private Load load;

    private OnCountdownSelectedListener onCountdownSelectedListener;
    private UploadLocation uploadLocation;
    private LocationCity mLocationCity;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            device = (Device) bundle.getSerializable(Constant.DEVICE);
        }
        countdownDao = new CountdownDao();
//        load = new Load(mAppContext);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_wifi_device_countdown_list, container, false);
        initView();
        initListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
//        if (!ProductManage.getInstance().isCOCO(device)
//                && !ProductManage.getInstance().isS20(device)) {
//            //不是coco的话主机不会推送定时消息
        PropertyReport.getInstance(mAppContext).registerPropertyReport(this);//coco关闭定时提醒收不到
//        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onCountdownSelectedListener = (OnCountdownSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onRefresh() {
        refreshDeviceList();
    }

    @Override
    public void onVisible() {
        super.onVisible();
        if (countdownDao != null && uid != null && deviceId != null && onCountdownSelectedListener != null) {
            refresh();
        } else {
            LogUtil.w(TAG, "onVisible()-countdownDao:" + countdownDao
                    + ",onCountdownSelectedListener:" + onCountdownSelectedListener + ",uid:" + uid + ",deviceId:" + deviceId);
        }
    }

    private void initView() {
        lvCountdown = (ListView) view.findViewById(R.id.lvCountdown);
        emptyView = (LinearLayout) view.findViewById(R.id.empty_countdown_view);
        lvCountdown.setEmptyView(emptyView);
        tvAddCountdown = (TextView) emptyView.findViewById(R.id.tvAddCountdown);
        toastPopup = new ToastPopup();
    }

    private void initListener() {
        tvAddCountdown.setOnClickListener(addCountdownListener);
        lvCountdown.setOnItemClickListener(this);
    }

    private void initData() {
        countdownDao = new CountdownDao();
        activateCountdownControl = new ActivateCountdownControl(mAppContext);
        if (device != null) {
            deviceId = device.getDeviceId();
            uid = device.getUid();
        }
        refresh();
    }

    private void refresh() {
        LogUtil.d(TAG, "refresh()");
        countdowns = countdownDao.selCountdownsByDevice(uid, deviceId);
        deviceCountdownAdapter = new DeviceCountdownAdapter(context, countdowns, clickListener);
        lvCountdown.setAdapter(deviceCountdownAdapter);
        deviceCountdownAdapter.notifyDataSetChanged();

        // 如果没有主机时的界面提示
        if ((countdowns == null || countdowns.size() == 0)
                && lvCountdown != null) {
            onCountdownSelectedListener.onEmptyCountdown(true);
        } else {
            onCountdownSelectedListener.onEmptyCountdown(false);
        }
        noLocationPermissionPopup = new NoLocationPermissionPopup();
        locationFailPopup = new LocationFailPopup();
        initUploadLoaction();
    }


    /**
     * 倒计时被选择，返回定时列表是否为空，true 空；false 非空
     */
    public interface OnCountdownSelectedListener {
        void onEmptyCountdown(boolean emptyFlag);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_LOAD_TIMER) {
                if (NetUtil.isNetworkEnable(mAppContext)) {
                    loadCountdown();
                }
            }
        }
    };

    private View.OnClickListener addCountdownListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (PhoneUtil.isCN(mAppContext) || JudgeLocationUtil.isLocation(UserCache.getCurrentUserId(mAppContext)) || LocationCache.getUploadFlag(mAppContext, userId)) {
                toCountdown();
            } else if (!NetUtil.isNetworkEnable(DeviceCountdownListFragment.this.getActivity())) {
                locationFailPopup.showPopup(DeviceCountdownListFragment.this.getActivity(), getResources().getString(R.string.warm_tips),
                        getResources().getString(R.string.location_fail_tips),
                        getResources().getString(R.string.know), null);

            } else if (!LocationServiceUtil.isOpenLocService(DeviceCountdownListFragment.this.getActivity())) {
                noLocationPermissionPopup.showPopup(DeviceCountdownListFragment.this.getActivity(), getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            } else {
                locationPosition();
            }
        }


    };


    private void loadCountdown() {
        if (NetUtil.isNetworkEnable(mAppContext)) {
            LoadUtil.noticeLoadData(mAppContext, device.getUid(), Constant.INVALID_NUM);
        }
    }
//
//    @Override
//    public void onLoadFinish(String uid, int result) {
//        if (result == ErrorCode.SUCCESS) {
//            refreshDeviceList();
//        }
//    }

    @Override
    public void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport()-callback" + deviceId + " property"
                + ",value1:" + value1
                + ",value2:" + value2
                + ",value3:" + value3
                + ",value4:" + value4);
        //检测到属性报告上来，如果是同一个设备且设置的动作与返回的属性状态一样，进行读表
        if (deviceId.equals(device.getDeviceId())
                && countdowns != null
                && !countdowns.isEmpty()) {
            for (Countdown countdown : countdowns) {
                if (countdown.getValue1() == value1
                        && countdown.getValue2() == value2
                        && countdown.getValue3() == value3
                        && countdown.getValue4() == value4) {
                    mHandler.removeMessages(WHAT_LOAD_TIMER);
                    mHandler.sendEmptyMessageDelayed(WHAT_LOAD_TIMER, DeviceFragment.TIME_LOAD_TIMER);
                    break;
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            LogUtil.d(TAG, "onItemClick() - view = " + view);
            countdown = countdowns.get(position);
            action = (Action) view.getTag(R.id.tag_action);
            Intent intent = new Intent(context, DeviceCountdownCreateActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra("countdown", countdown);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ActivateCountdownControl extends ActivateCountdown {

        public ActivateCountdownControl(Context context) {
            super(context);
        }

        @Override
        public void onActivateCountdownResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                refreshDeviceList();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(context, result),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }

    public void rightTitleClick(View view) {
        //修复了coco只能添加4个定时问题
        if (device != null && (ProductManage.getInstance().isWifiDevice(device) || countdowns == null || countdowns != null && countdowns.size() < 4)) {
            toCountdown();
        } else {
            toastPopup.showPopup(context, ErrorMessage.getError(context, ErrorCode.FOUR_TIMER_ERROR), getResources()
                    .getString(R.string.know), null);
        }
    }

    private void toCountdown() {
        Intent intent = new Intent(context, DeviceCountdownCreateActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    private void refreshDeviceList() {
        if (countdownDao == null) {
            countdownDao = new CountdownDao();
        }
        new AsyncTask<Void, Void, List<Countdown>>() {
            @Override
            protected List<Countdown> doInBackground(Void... params) {
                return countdownDao.selCountdownsByDevice(uid, deviceId);
            }

            @Override
            protected void onPostExecute(List<Countdown> countdowns) {
                DeviceCountdownListFragment.this.countdowns = countdowns;
                if (deviceCountdownAdapter == null) {
                    deviceCountdownAdapter = new DeviceCountdownAdapter(context, countdowns, clickListener);
                    lvCountdown = (ListView) view.findViewById(R.id.lvCountdown);
                    lvCountdown.setOnItemClickListener(DeviceCountdownListFragment.this);
                    lvCountdown.setAdapter(deviceCountdownAdapter);
                } else {
                    deviceCountdownAdapter.refresh(countdowns);
                }
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        super.onDestroy();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LogUtil.d(TAG, "onClick() - view = " + view);
            countdown = (Countdown) view.getTag();
            isPause = countdown.getIsPause();
            countdownId = countdown.getCountdownId();
            if (isPause == CountdownConstant.COUNTDOWN_EFFECT) {
                showDialog();
                activateCountdownControl.startActivateCountdown(uid, UserCache.getCurrentUserName(mAppContext),
                        countdownId, CountdownConstant.COUNTDOWN_PAUSE, DateUtil.getUTCTime());
            } else if (isPause == CountdownConstant.COUNTDOWN_PAUSE) {
                showDialog();
                activateCountdownControl.startActivateCountdown(uid, UserCache.getCurrentUserName(mAppContext),
                        countdownId, CountdownConstant.COUNTDOWN_EFFECT, DateUtil.getUTCTime());
            }
        }
    };

    private void initUploadLoaction() {
        uploadLocation = new UploadLocation() {
            @Override
            public void onUploadLoactionResult(int errorCode, String errorMessage) {
                LogUtil.e(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                if (errorCode == 0) {
                    LocationCache.saveUploadFlag(mAppContext, true, UserCache.getCurrentUserId(mAppContext));
                    toCountdown();
                    // ToastUtil.showToast(getString(R.string.location_success_tips));
                }
            }
        };
    }

    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        DeviceCountdownListFragment.this.getActivity().startService(new Intent(DeviceCountdownListFragment.this.getActivity(), LocationService.class));
//
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
            uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(DeviceCountdownListFragment.this.getActivity()),
                    longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceCountdownListFragment.this.getActivity())) {
                noLocationPermissionPopup.showPopup(DeviceCountdownListFragment.this.getActivity(), getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            }
            //  }
        } else {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceCountdownListFragment.this.getActivity())) {
                locationFailPopup.showPopup(DeviceCountdownListFragment.this.getActivity(), getResources().getString(R.string.warm_tips),
                        getResources().getString(R.string.location_fail_tips),
                        getResources().getString(R.string.know), null);
            }
            //  }
        }

    }

    private class NoLocationPermissionPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            LocationServiceUtil.gotoLocServiceSettings(mAppContext);
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

}
