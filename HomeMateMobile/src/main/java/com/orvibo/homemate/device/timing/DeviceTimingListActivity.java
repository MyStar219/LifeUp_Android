package com.orvibo.homemate.device.timing;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.event.LocationResultEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.ActivateTimer;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.UploadLocation;
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
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * zigbee设备的定时列表。wifi设备的定时不在此activity
 * 定时列表，所有设备的定时都先进入此界面
 * Created by Allen on 2015/4/3.
 * Modified by smagret on 2015/04/21
 */
public class DeviceTimingListActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        OnPropertyReportListener {
    private static final String TAG = DeviceTimingListActivity.class
            .getSimpleName();
    private static final int WHAT_LOAD_TIMER = 1;
    private Context mContext;
    private ListView lvTiming;
    private CheckBox cbIsPaused;
    private TextView tvAddTimer;
    private NavigationGreenBar nb_title;
    private View emptyView = null;
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

    private int deviceType;
    private TimingDao timingDao;
    private ActivateTimerControl activateTimerControl;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;
    private UploadLocation mUploadLocation;

    //  private Load load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_list);
        mContext = DeviceTimingListActivity.this;
        // load = new Load(appContext);
        timingDao = new TimingDao();
        activateTimerControl = new ActivateTimerControl(mAppContext);
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        deviceId = device.getDeviceId();
        deviceType = device.getDeviceType();
        uid = device.getUid();

        init();

        noticeLoadData(uid);
        noLocationPermissionPopup = new NoLocationPermissionPopup();
        locationFailPopup = new LocationFailPopup();
        // initSystemBar(DeviceTimingListActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initData();
        refreshDeviceList();
//        if (!ProductManage.getInstance().isCOCO(device)
//                && !ProductManage.getInstance().isS20(device)) {
//            //不是coco的话主机不会推送定时消息
        PropertyReport.getInstance(mAppContext).registerPropertyReport(this);//coco关闭定时提醒收不到
//        }
        if (!LocationCache.getUploadFlag(mContext, userId)) {
            locationPosition();
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        if (event != null && !StringUtil.isEmpty(event.uid) && event.uid.equals(uid)) {

            refreshDeviceList();
        }
    }

    private void init() {
        initView();
        initListener();
    }

    private void initView() {
        lvTiming = (ListView) findViewById(R.id.lvTiming);
        cbIsPaused = (CheckBox) findViewById(R.id.cbIsPaused);
        nb_title = (NavigationGreenBar) findViewById(R.id.nbTitle);
        emptyView = findViewById(R.id.layout_empty);
        lvTiming.setEmptyView(emptyView);
//        emptyView = LayoutInflater.from(mContext).inflate(
//                R.layout.activity_device_timing_empty, null);
        tvAddTimer = (TextView) emptyView.findViewById(R.id.tvAddTimer);
        toastPopup = new ToastPopup();
    }

    private void initListener() {
        tvAddTimer.setOnClickListener(addTimerListener);
        lvTiming.setOnItemClickListener(this);
    }

    private void initData() {
        boolean isFirst = deviceTimingAdapter == null;
        timings = timingDao.selTimingsByDevice(uid, deviceId);
        deviceTimingAdapter = new DeviceTimingAdapter(mContext, timings, clickListener, device);
        lvTiming.setAdapter(deviceTimingAdapter);
//        deviceTimingAdapter.notifyDataSetChanged();

        // 如果没有主机时的界面提示
        if ((timings == null || timings.size() == 0)
                && lvTiming != null) {
//            ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
//            ((ViewGroup) lvTiming.getParent()).addView(emptyView);
//            lvTiming.setEmptyView(emptyView);
            deviceTimingAdapter.notifyDataSetChanged();
            nb_title.setRightText("");
        } else {
            if (emptyView != null) {
                // ((ViewGroup) lvTiming.getParent()).removeView(emptyView);
                // lvTiming.setVisibility(View.VISIBLE);
                nb_title.setRightText(getResources().getString(R.string.device_timing_add));
            }
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        loadTimer();
//    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int what = msg.what;
            switch (what) {
                case WHAT_LOAD_TIMER:
                    noticeLoadData(uid);
                    break;
            }
        }
    };

    private View.OnClickListener addTimerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toSetTiming();
        }
    };

    private void toSetTiming() {
        if (PhoneUtil.isCN(DeviceTimingListActivity.this) || JudgeLocationUtil.isLocation(UserCache.getCurrentUserId(DeviceTimingListActivity.this)) || LocationCache.getUploadFlag(mContext, userId)) {// 判断是否是否有地位信息(有上传信息标志也行)
            toTiming();
        } else if (!NetUtil.isNetworkEnable(DeviceTimingListActivity.this)) {
            locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
                    getResources().getString(R.string.location_fail_tips),
                    getResources().getString(R.string.know), null);

        } else if (!LocationServiceUtil.isOpenLocService(DeviceTimingListActivity.this)) {
            noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                    getResources().getString(R.string.location_no_permission_tips),
                    getResources().getString(R.string.to_set),
                    getResources().getString(R.string.cancel));
        } else {
            locationPosition();
        }
    }

    /**
     * 通知数据同步
     *
     * @param uid
     */
    private void noticeLoadData(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            LoadUtil.noticeLoadData(mAppContext, uid, Constant.INVALID_NUM);
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
        if (!isFinishingOrDestroyed()
                && deviceId.equals(device.getDeviceId())
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            timing = timings.get(position);
            action = (Action) view.getTag(R.id.tag_action);
            Intent intent = new Intent(mContext, DeviceTimingCreateActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra("timing", timing);
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
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, result),
                        ToastType.NULL, ToastType.SHORT);
                //timing = timingDao.selTiming(uid, timingId);
                //cbIsPaused.setChecked(timing.getIsPause() == TimingConstant.TIMEING_EFFECT);
            }
        }
    }

    public void rightTitleClick(View view) {
        //修复了coco只能添加4个定时问题
//        if (device != null && (ProductManage.getInstance().isWifiDevice(device) || timings == null || timings != null && timings.size() < 4)) {
        toSetTiming();
//        } else {
//            toastPopup.showPopup(DeviceTimingListActivity.this, ErrorMessage.getError(mContext, ErrorCode.FOUR_TIMER_ERROR), getResources()
//                    .getString(R.string.know), null);
//        }
    }

    private void toTiming() {
        Intent intent = new Intent(mContext, DeviceTimingCreateActivity.class);
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
        new AsyncTask<Void, Void, List<Timing>>() {
            @Override
            protected List<Timing> doInBackground(Void... params) {
                return timingDao.selTimingsByDevice(uid, deviceId);
            }

            @Override
            protected void onPostExecute(List<Timing> timings) {
                DeviceTimingListActivity.this.timings = timings;

                if (deviceTimingAdapter == null) {
                    deviceTimingAdapter = new DeviceTimingAdapter(mContext, timings, clickListener, device);
                    lvTiming = (ListView) findViewById(R.id.lvTiming);
                    lvTiming.setOnItemClickListener(DeviceTimingListActivity.this);
                    lvTiming.setAdapter(deviceTimingAdapter);
                } else {
                    deviceTimingAdapter.refresh(timings);
                }

                if ((timings == null || timings.size() == 0)
                        && lvTiming != null) {
                    nb_title.setRightText("");
                } else {
                    nb_title.setRightText(getResources().getString(R.string.device_timing_add));
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
                activateTimerControl.startActivateTimer(uid, UserCache.getCurrentUserName(mContext), timingId, TimingConstant.TIMEING_PAUSE);
            } else if (isPause == TimingConstant.TIMEING_PAUSE) {
                showDialog();
                activateTimerControl.startActivateTimer(uid, UserCache.getCurrentUserName(mContext), timingId, TimingConstant.TIMEING_EFFECT);
            }
        }
    };

    @Override
    protected void onDestroy() {
//        if (load != null) {
//            load.removeListener(this);
//            load.cancelLoad();
//        }
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        super.onDestroy();
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
        mUploadLocation = new UploadLocation() {
            @Override
            public void onUploadLoactionResult(int errorCode, String errorMessage) {
                LogUtil.e(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                if (errorCode == ErrorCode.SUCCESS) {
                    LocationCache.saveUploadFlag(mContext, true, userId);
                    toTiming();
                }
            }
        };
    }

    private void locationPosition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext.startService(new Intent(mContext, LocationService.class));
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
            mUploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(mContext),
                    longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
        } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceTimingListActivity.this)) {
                noLocationPermissionPopup.showPopup(mContext, getResources().getString(R.string.location_permission_no_get_tips),
                        getResources().getString(R.string.location_no_permission_tips),
                        getResources().getString(R.string.to_set),
                        getResources().getString(R.string.cancel));
            }
            //  }
        } else {
            //  if (LocationCache.getTimeLag(mContext, userId)) {
            if (!PhoneUtil.isCN(DeviceTimingListActivity.this)) {
                locationFailPopup.showPopup(mContext, getResources().getString(R.string.warm_tips),
                        getResources().getString(R.string.location_fail_tips),
                        getResources().getString(R.string.know), null);
            }
            //  }
        }

    }
}
