//package com.orvibo.homemate.device.timing;
//
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.format.DateFormat;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.api.listener.OnPropertyReportListener;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.bo.InfoPushMsg;
//import com.orvibo.homemate.bo.Timing;
//import com.orvibo.homemate.common.BaseActivity;
//import com.orvibo.homemate.core.Load;
//import com.orvibo.homemate.core.product.ProductManage;
//import com.orvibo.homemate.dao.TimingDao;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.messagepush.InfoPushManager;
//import com.orvibo.homemate.model.PropertyReport;
//import com.orvibo.homemate.util.GatewayTool;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.NetUtil;
//import com.tencent.stat.StatService;
//
//import java.util.List;
//
//
///**
// * 1.进来此界面会读取改主机或coco的所有更新数据
// * 2.只要接收到属性状态报告且与设置的定时的动作一样，也会读取所有更新数据。
// */
//public class TimingActivity extends BaseActivity implements View.OnClickListener,
//        AdapterView.OnItemClickListener,
//        InfoPushManager.OnPushTimerListener,
//        OnPropertyReportListener,
//        Load.OnLoadListener {
//
//    private final String TAG = TimingActivity.class.getSimpleName();
//    private int TIME_LOAD_TIMER = 3 * 1000;//结束到属性报告后延时时间进行读取数据
//    private int WHAT_LOAD_TIMER = 1;
//    // private List<Fragment> mTabs = new ArrayList<Fragment>();
//
//    private TextView timingTextView;
//    private ImageView addTimingImageView;
//    private ListView mListView;
//    private View emptyView;
//    private Button timerAddButton;
//    private Device device;
//    private String uid;
//    private TimingAdapter timingAdapter;
//    private boolean showEmptyView;
//    //    private TimingListFragment timingListFragment;
//    private List<Timing> timings;
//
//    private boolean isCoco;
//    private Load load;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.timing_activity);
//        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
//        initView();
//        isCoco = !ProductManage.getInstance().isVicenter300ByUid(device.getUid());
//        if (!isCoco) {
//            //不是coco的话主机不会推送定时消息
//            PropertyReport.getInstance(mAppContext).registerPropertyReport(this);
//        }
//        load = new Load(mAppContext);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        refresh();
//    }
//
//    private void initView() {
//        addTimingImageView = (ImageView) findViewById(R.id.addTimingImageView);
//        timingTextView = (TextView) findViewById(R.id.timingTextView);
//        timingTextView.setOnClickListener(this);
//
//        uid = device.getUid();
//        emptyView = findViewById(R.id.mLinear);
//        timerAddButton = (Button) findViewById(R.id.timerAddButton);
//        timerAddButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Add), null);
//                Intent intent = new Intent(mContext, TimingAddActivity.class);
//                intent.putExtra(Constant.DEVICE, device);
//                startActivity(intent);
//            }
//        });
//        mListView = (ListView) findViewById(R.id.socketListView);
//        mListView.setEmptyView(emptyView);
//        InfoPushManager.getInstance(mAppContext).registerPushTimerListener(this);
//        refresh();
//
//    }
//
//    public void refresh() {
//        new AsyncTask<Void, Void, List<Timing>>() {
//
//            @Override
//            protected List<Timing> doInBackground(Void... params) {
//                return new TimingDao().selTimingsByDevice(uid, device.getDeviceId());
//            }
//
//            @Override
//            protected void onPostExecute(List<Timing> timings) {
//                TimingActivity.this.timings = timings;
//                if (timingAdapter == null) {
//                    LogUtil.d(TAG, "onPostExecute: timings = " + timings);
//                    timingAdapter = new TimingAdapter(TimingActivity.this, timings, DateFormat.is24HourFormat(TimingActivity.this));
//                    mListView.setAdapter(timingAdapter);
//                    mListView.setOnItemClickListener(TimingActivity.this);
//                    if (timings != null && !timings.isEmpty()) {
//                        loadTimer();
//                    }
//                } else {
//                    LogUtil.d(TAG, "onPostExecute: timings = " + timings);
//                    timingAdapter.setData(timings);
//                }
//                if (mListView.getCount() == 0) {
//                    showEmptyView = true;
//                    setRightTopVisibility();
//                } else {
//                    showEmptyView = false;
//                    setRightTopVisibility();
//                }
//            }
//
//        }.execute();
//    }
//
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Edit), null);
//        Timing timing = (Timing) view.getTag(R.id.timing);
//        LogUtil.d(TAG, "onItemClick()-timing:" + timing);
//        Intent intent;
//        if (device == null) {
//            intent = new Intent(mContext, TimingAddActivity.class);
//        } else {
//            intent = new Intent(mContext, TimingAddActivity.class);
//        }
//        intent.putExtra(Constant.UID, uid);
//        intent.putExtra(Constant.TIMING, timing);
//        intent.putExtra(Constant.DEVICE, device);
//        startActivity(intent);
//    }
//
//    public void setRightTopVisibility() {
//        if (showEmptyView) {
//            emptyView.setVisibility(View.VISIBLE);
//            setAddTimingImageViewGone();
//        } else {
//            emptyView.setVisibility(View.GONE);
//            setAddTimingImageViewVisible();
//        }
//    }
//
//    @Override
//    public void onPushTimer(InfoPushMsg infoPushMsg) {
//        refresh();
//    }
//
//    @Override
//    public void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType) {
//        //检测到属性报告上来，如果是同一个设备且设置的动作与返回的属性状态一样，进行读表
//        if (!isFinishingOrDestroyed()
//                && !isCoco
//                && deviceId.equals(device.getDeviceId())
//                && timings != null
//                && !timings.isEmpty()) {
//            for (Timing timing : timings) {
//                if (timing.getWeek() == 0 && timing.getValue1() == value1) {
//                    mHandler.removeMessages(WHAT_LOAD_TIMER);
//                    mHandler.sendEmptyMessageDelayed(WHAT_LOAD_TIMER, TIME_LOAD_TIMER);
//                    break;
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onLoadFinish(String uid, int result) {
//        LogUtil.d(TAG, "onLoadFinish()-uid:" + uid + ",result:" + result);
//        if (!isFinishingOrDestroyed() && result == ErrorCode.SUCCESS) {
//            refresh();
//        }
//    }
//
//    public void back(View view) {
//        finish();
//    }
//
//    public void leftTitleClick(View view) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Back), null);
//        finish();
//    }
//
//    public void addTiming(View view) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Add), null);
//        Intent intent = new Intent(TimingActivity.this, TimingAddActivity.class);
//        intent.putExtra(Constant.DEVICE, device);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onClick(View v) {
//        setRightTopVisibility();
//    }
//
//    public void setAddTimingImageViewVisible() {
//        addTimingImageView.setVisibility(View.VISIBLE);
//    }
//
//    public void setAddTimingImageViewGone() {
//        addTimingImageView.setVisibility(View.INVISIBLE);
//    }
//
//    private void loadTimer() {
//        if (NetUtil.isNetworkEnable(mAppContext) && load != null) {
//            load.setOnLoadListener(this);
//            if (!load.isLoading(device.getUid())) {
//                load.load(device.getUid());
//            } else {
//                LogUtil.w(TAG, "loadTimer()-" + device.getUid() + " is loading...");
//            }
//        }
//    }
//
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == WHAT_LOAD_TIMER) {
//                if (NetUtil.isNetworkEnable(mAppContext)) {
//                    loadTimer();
//                }
//            }
//        }
//    };
//
//    @Override
//    protected void onDestroy() {
//        if (mHandler != null) {
//            mHandler.removeCallbacksAndMessages(null);
//        }
//        if (load != null) {
//            load.removeListener(this);
//            load.cancelLoad();
//        }
//        InfoPushManager.getInstance(mAppContext).unregisterPushTimerListener(this);
//        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
//        super.onDestroy();
//    }
//}
