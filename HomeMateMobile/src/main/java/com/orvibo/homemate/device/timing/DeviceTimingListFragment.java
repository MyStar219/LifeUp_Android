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
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.InfoPushTimerInfo;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.model.ActivateTimer;
import com.orvibo.homemate.model.InfoPush;
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
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 定时列表
 * Created by smagret on 2015/12/08
 */
public class DeviceTimingListFragment extends BaseFragment implements
        AdapterView.OnItemClickListener,
        OnPropertyReportListener,
        InfoPushManager.OnPushTimingListener {
    private static final String TAG = DeviceTimingListFragment.class
            .getSimpleName();
    private int WHAT_LOAD_TIMER = 1;
    private ListView lvTiming;
    private TextView tvAddTimer;
    private LinearLayout emptyView = null;
    private View view;
    private DeviceTimingAdapter deviceTimingAdapter;
    private ToastPopup toastPopup;

    private List<Timing> timings;
    private Timing timing;
    private Action action;
    private String uid;
    private Device device;
    private String deviceId;
    private String timingId;
    private int isPause;

    private TimingDao timingDao;
    private ActivateTimerControl activateTimerControl;
    //    private Load                     load;
    private OnTimingSelectedListener onTimingSelectedListener;
    private Acpanel mAcPanel;
    private UploadLocation uploadLocation;
    private LocationCity mLocationCity;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;

    private InfoPush mInfoPush;
    private InfoPushManager mInfoPushManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            device = (Device) bundle.getSerializable(Constant.DEVICE);
            //获取acpanel对象
            mAcPanel = (Acpanel) bundle.getSerializable(IntentKey.ACPANEL);
        }
//        load = new Load(mAppContext);

        noLocationPermissionPopup = new NoLocationPermissionPopup();
        locationFailPopup = new LocationFailPopup();

        initUploadLoaction();
        mInfoPushManager = InfoPushManager.getInstance(mAppContext);
        mInfoPushManager.registerOnPushTimingListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_wifi_device_timing_list, container, false);
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
            onTimingSelectedListener = (OnTimingSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onVisible() {
        super.onVisible();
        if (timingDao != null && uid != null && deviceId != null && onTimingSelectedListener != null) {
//            timings = timingDao.selTimingsByDevice(uid, deviceId);
//            if (timings == null || timings.size() == 0) {
//                onTimingSelectedListener.onEmptyTiming(true);
//            } else {
//                onTimingSelectedListener.onEmptyTiming(false);
//            }
//
//            timings = timingDao.selTimingsByDevice(uid, deviceId);
//            deviceTimingAdapter = new DeviceTimingAdapter(context, timings, clickListener, device);
//            lvTiming.setAdapter(deviceTimingAdapter);
//            deviceTimingAdapter.notifyDataSetChanged();
//
//            // 如果没有主机时的界面提示
//            if ((timings == null || timings.size() == 0)
//                    && lvTiming != null) {
//                ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//                ((ViewGroup) lvTiming.getParent()).addView(emptyView);
//                lvTiming.setEmptyView(emptyView);
//                deviceTimingAdapter.notifyDataSetChanged();
//                onTimingSelectedListener.onEmptyTiming(true);
//            } else {
//                if (emptyView != null) {
//                    ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//                    lvTiming.setVisibility(View.VISIBLE);
//                }
//                onTimingSelectedListener.onEmptyTiming(false);
//            }
            refresh();
        } else {
            LogUtil.w(TAG, "onVisible()-timingDao:" + timingDao
                    + ",onTimingSelectedListener:" + onTimingSelectedListener + ",uid:" + uid + ",deviceId:" + deviceId);
        }
    }

    private void refresh() {
        LogUtil.d(TAG, "refresh()");
        timings = timingDao.selTimingsByDevice(uid, deviceId);
        deviceTimingAdapter = new DeviceTimingAdapter(context, timings, clickListener, device);
        lvTiming.setAdapter(deviceTimingAdapter);
        deviceTimingAdapter.notifyDataSetChanged();

        // 如果没有主机时的界面提示
        if ((timings == null || timings.size() == 0)
//                && lvTiming != null
                ) {
//            ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//            ((ViewGroup) lvTiming.getParent()).addView(emptyView);
//            lvTiming.setEmptyView(emptyView);
//            deviceTimingAdapter.notifyDataSetChanged();
            onTimingSelectedListener.onEmptyTiming(true);
        } else {
//            if (emptyView != null) {
//                ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//            }
//            if (lvTiming != null) {
//                lvTiming.setVisibility(View.VISIBLE);
//            }
            onTimingSelectedListener.onEmptyTiming(false);
        }

    }

    private void initView() {
        lvTiming = (ListView) view.findViewById(R.id.lvTiming);
        emptyView = (LinearLayout) view.findViewById(R.id.empty_timing_view);
        lvTiming.setEmptyView(emptyView);
//        emptyView = LayoutInflater.from(context).inflate(
//                R.layout.activity_device_timing_empty, null);
        tvAddTimer = (TextView) emptyView.findViewById(R.id.tvAddTimer);
        toastPopup = new ToastPopup();
    }

    private void initListener() {
        tvAddTimer.setOnClickListener(addTimerListener);
        lvTiming.setOnItemClickListener(this);
    }

    private void initData() {
        timingDao = new TimingDao();
        activateTimerControl = new ActivateTimerControl(mAppContext);
        //TODO device == null
        deviceId = device.getDeviceId();
        uid = device.getUid();
        refresh();
//        timings = timingDao.selTimingsByDevice(uid, deviceId);
//        deviceTimingAdapter = new DeviceTimingAdapter(context, timings, clickListener, device);
//        lvTiming.setAdapter(deviceTimingAdapter);
//        deviceTimingAdapter.notifyDataSetChanged();
//
//        // 如果没有主机时的界面提示
//        if ((timings == null || timings.size() == 0)
//                && lvTiming != null) {
//            ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//            ((ViewGroup) lvTiming.getParent()).addView(emptyView);
//            lvTiming.setEmptyView(emptyView);
//            deviceTimingAdapter.notifyDataSetChanged();
//            onTimingSelectedListener.onEmptyTiming(true);
//        } else {
//            if (emptyView != null) {
//                ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//                lvTiming.setVisibility(View.VISIBLE);
//            }
//            onTimingSelectedListener.onEmptyTiming(false);
//        }

    }

    /**
     * 定时被选择，返回定时列表是否为空,true 空；false 非空
     */
    public interface OnTimingSelectedListener {
        void onEmptyTiming(boolean emptyFlag);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_LOAD_TIMER) {
                if (NetUtil.isNetworkEnable(mAppContext)) {
                    loadTimer();
                }
            }
        }
    };

    private View.OnClickListener addTimerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (PhoneUtil.isCN(mAppContext) || JudgeLocationUtil.isLocation(UserCache.getCurrentUserId(mAppContext)) || LocationCache.getUploadFlag(mAppContext, userId)) {
                toTimming();
            } else if (!NetUtil.isNetworkEnable(DeviceTimingListFragment.this.getActivity())) {
                locationFailPopup.showPopup(DeviceTimingListFragment.this.getActivity(), getResources().getString(R.string.warm_tips),
                        getResources().getString(R.string.location_fail_tips),
                        getResources().getString(R.string.know), null);

            } else if (!LocationServiceUtil.isOpenLocService(DeviceTimingListFragment.this.getActivity())) {
                noLocationPermissionPopup.showPopup(DeviceTimingListFragment.this.getActivity(), getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            } else {
                locationPosition();
            }
        }

    };

    private void toTimming() {
        Intent intent = new Intent(context, DeviceTimingCreateActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        intent.putExtra(IntentKey.ACPANEL, mAcPanel);
        startActivity(intent);
    }

    private void loadTimer() {
        if (NetUtil.isNetworkEnable(mAppContext)) {
            LoadUtil.noticeLoadData(mAppContext, device.getUid(), Constant.INVALID_NUM);
        }
    }

    @Override
    public void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport()-callback" + deviceId + " property"
                + ",value1:" + value1
                + ",value2:" + value2
                + ",value3:" + value3
                + ",value4:" + value4);
        //检测到属性报告上来，如果是同一个设备且设置的动作与返回的属性状态一样，进行读表
        if (deviceId.equals(device.getDeviceId())
                && timings != null
                && !timings.isEmpty()) {
            for (Timing timing : timings) {
                if (timing.getWeek() == 0
                        && timing.getValue1() == value1
                        && timing.getValue2() == value2
                        && timing.getValue3() == value3
                        && timing.getValue4() == value4) {
                    mHandler.removeMessages(WHAT_LOAD_TIMER);
                    mHandler.sendEmptyMessageDelayed(WHAT_LOAD_TIMER, DeviceFragment.TIME_LOAD_TIMER);
                    break;
                }
            }
        }
    }

    @Override
    public void onPushTiming(InfoPushTimerInfo infoPushTimerInfo) {
        LogUtil.d(TAG, "onPushTiming()-infoPushTimerInfo:" + infoPushTimerInfo);
        if (infoPushTimerInfo != null && infoPushTimerInfo.getDeviceId() != null && infoPushTimerInfo.getDeviceId().equals(deviceId)) {
            for (Timing timing : timings) {
                if (timing.getWeek() == 0 && timing.getTimingId().equals(infoPushTimerInfo.getTimingId())) {
                    refreshDeviceList();
                    break;
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            timing = timings.get(position);
            action = (Action) view.getTag(R.id.tag_action);
            Intent intent = new Intent(context, DeviceTimingCreateActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra("timing", timing);
            //点击item的时候传递Acpanel对象，编辑定时对象
            intent.putExtra(IntentKey.ACPANEL, mAcPanel);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ActivateTimerControl extends ActivateTimer {

        public ActivateTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onActivateTimerResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                refreshDeviceList();
            } else if (result == ErrorCode.MODE_EXIST) {
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.MODE_EXIST));
                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(context, result),
                        ToastType.NULL, ToastType.SHORT);
                //timing = timingDao.selTiming(uid, timingId);
                //cbIsPaused.setChecked(timing.getIsPause() == TimingConstant.TIMEING_EFFECT);
            }
        }
    }


    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    @Override
    public void onRefresh() {
        refreshDeviceList();
    }

    private void refreshDeviceList() {
        new AsyncTask<Void, Void, List<Timing>>() {
            @Override
            protected List<Timing> doInBackground(Void... params) {
                return timingDao.selTimingsByDevice(uid, deviceId);
            }

            @Override
            protected void onPostExecute(List<Timing> timings) {
                DeviceTimingListFragment.this.timings = timings;

                if (deviceTimingAdapter == null) {
                    deviceTimingAdapter = new DeviceTimingAdapter(context, timings, clickListener, device);
                    lvTiming = (ListView) view.findViewById(R.id.lvTiming);
                    lvTiming.setOnItemClickListener(DeviceTimingListFragment.this);
                    lvTiming.setAdapter(deviceTimingAdapter);
                } else {
                    deviceTimingAdapter.refresh(timings);
                }
            }
        }.execute();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            timing = (Timing) view.getTag();
            isPause = timing.getIsPause();
            timingId = timing.getTimingId();
            if (isPause == TimingConstant.TIMEING_EFFECT) {
                showDialog();
                activateTimerControl.startActivateTimer(uid, UserCache.getCurrentUserName(mAppContext), timingId, TimingConstant.TIMEING_PAUSE);
            } else if (isPause == TimingConstant.TIMEING_PAUSE) {
                showDialog();
                activateTimerControl.startActivateTimer(uid, UserCache.getCurrentUserName(mAppContext), timingId, TimingConstant.TIMEING_EFFECT);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        mInfoPushManager.unregisterOnPushTimingListener(this);
    }

    private void initUploadLoaction() {
        uploadLocation = new UploadLocation() {
            @Override
            public void onUploadLoactionResult(int errorCode, String errorMessage) {
                LogUtil.e(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                if (errorCode == 0) {
                    LocationCache.saveUploadFlag(mAppContext, true, UserCache.getCurrentUserId(mAppContext));
                    toTimming();
                    // ToastUtil.showToast(getString(R.string.location_success_tips));
                }
            }
        };
    }

    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        DeviceTimingListFragment.this.getActivity().startService(new Intent(DeviceTimingListFragment.this.getActivity(), LocationService.class));
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
            uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(DeviceTimingListFragment.this.getActivity()),
                    longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceTimingListFragment.this.getActivity())) {
                noLocationPermissionPopup.showPopup(DeviceTimingListFragment.this.getActivity(), getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            }
            //  }
        } else {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceTimingListFragment.this.getActivity())) {
                locationFailPopup.showPopup(DeviceTimingListFragment.this.getActivity(), getResources().getString(R.string.warm_tips),
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
