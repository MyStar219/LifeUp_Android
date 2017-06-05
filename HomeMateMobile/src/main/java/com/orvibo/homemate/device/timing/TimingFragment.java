//package com.orvibo.homemate.device.timing;
//
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.format.DateFormat;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
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
//import com.orvibo.homemate.common.BaseFragment;
//import com.orvibo.homemate.core.Load;
//import com.orvibo.homemate.core.product.ProductManage;
//import com.orvibo.homemate.dao.TimingDao;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.data.TimingConstant;
//import com.orvibo.homemate.messagepush.InfoPushManager;
//import com.orvibo.homemate.model.ActivateTimer;
//import com.orvibo.homemate.model.PropertyReport;
//import com.orvibo.homemate.sharedPreferences.UserCache;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.NetUtil;
//import com.orvibo.homemate.util.ToastUtil;
//import com.tencent.stat.StatService;
//
//import java.util.List;
//
//
///**
// * 1.进来此界面会读取改主机或coco的所有更新数据
// * 2.只要接收到属性状态报告且与设置的定时的动作一样，也会读取所有更新数据。
// */
//public class TimingFragment extends BaseFragment implements View.OnClickListener,
//        AdapterView.OnItemClickListener,
//        InfoPushManager.OnPushTimerListener,
//        OnPropertyReportListener,
//        Load.OnLoadListener {
//
//    private final String TAG = TimingFragment.class.getSimpleName();
//    private int TIME_LOAD_TIMER = 3 * 1000;//结束到属性报告后延时时间进行读取数据
//    private int WHAT_LOAD_TIMER = 1;
//
//    private TextView timingTextView;
//    private ImageView addTimingImageView;
//    private ListView mListView;
//    private View emptyView;
//    private View view;
//    private Button timerAddButton;
//    private Device device;
//    private String uid;
//    private TimingAdapter timingAdapter;
//    private boolean showEmptyView;
//    private List<Timing> timings;
//    private ActivateTimer activateTimer;
//    private Timing timing;
//
//    private boolean isCoco;
//    private Load load;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            device = (Device)bundle.getSerializable(Constant.DEVICE);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        view = inflater.inflate(R.layout.timing_activity, container, false);
//        initView();
//        isCoco = !ProductManage.getInstance().isVicenter300ByUid(device.getUid());
//        if (!isCoco) {
//            //不是coco的话主机不会推送定时消息
//            PropertyReport.getInstance(mAppContext).registerPropertyReport(this);
//        }
//        load = new Load(mAppContext);
//        activateTimer = new ActivateTimer(mAppContext) {
//            @Override
//            public void onActivateTimerResult(String uid, int serial, int result) {
//                dismissDialog();
//                if (result == ErrorCode.SUCCESS) {
//                    int isPause = timing.getIsPause();
//                    if (isPause == TimingConstant.TIMEING_PAUSE) {
//                        timing.setIsPause(TimingConstant.TIMEING_EFFECT);
//                    } else {
//                        timing.setIsPause(TimingConstant.TIMEING_PAUSE);
//                    }
//                    timingAdapter.notifyDataSetChanged();
//                } else{
//                    ToastUtil.toastError(result);
//                }
//            }
//        };
//        return view;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        refresh();
//    }
//
//    private void initView() {
//        addTimingImageView = (ImageView) view.findViewById(R.id.addTimingImageView);
//        timingTextView = (TextView) view.findViewById(R.id.timingTextView);
//        timingTextView.setOnClickListener(this);
//
//        uid = device.getUid();
//        emptyView = view.findViewById(R.id.mLinear);
//        timerAddButton = (Button) view.findViewById(R.id.timerAddButton);
//        timerAddButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Add), null);
//                Intent intent = new Intent(mAppContext, TimingAddActivity.class);
//                intent.putExtra(Constant.DEVICE, device);
//                startActivity(intent);
//            }
//        });
//        mListView = (ListView) view.findViewById(R.id.socketListView);
//        mListView.setEmptyView(emptyView);
//        InfoPushManager.getInstance(mAppContext).registerPushTimerListener(this);
//        refresh();
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
//                TimingFragment.this.timings = timings;
//                if (timingAdapter == null) {
//                    LogUtil.d(TAG, "onPostExecute: timings = " + timings);
//                    timingAdapter = new TimingAdapter(context, timings, DateFormat.is24HourFormat(mAppContext));
//                    mListView.setAdapter(timingAdapter);
//                    mListView.setOnItemClickListener(TimingFragment.this);
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
//            intent = new Intent(mAppContext, TimingAddActivity.class);
//        } else {
//            intent = new Intent(mAppContext, TimingAddActivity.class);
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
//        if (!isCoco
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
//        if (result == ErrorCode.SUCCESS) {
//            refresh();
//        }
//    }
//
//    public void addTiming(View view) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCOTimerList_Add), null);
//        Intent intent = new Intent(mAppContext, TimingAddActivity.class);
//        intent.putExtra(Constant.DEVICE, device);
//        startActivity(intent);
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
//    @Override
//    public void onClick(View v) {
//        setRightTopVisibility();
//    }
//
//    public void activate(View view) {
//        StatService.trackCustomKVEvent(mAppContext, mAppContext.getString(R.string.MTAClick_COCOTimerList_Switch), null);
//        timing = timings.get((Integer) view.getTag());
//        int isPause = timing.getIsPause();
//        if (isPause == TimingConstant.TIMEING_PAUSE) {
//            showDialog(null, mAppContext.getString(R.string.timing_activating));
//            activateTimer.startActivateTimer(timing.getUid(), UserCache.getCurrentUserName(mAppContext), timing.getTimingId(), TimingConstant.TIMEING_EFFECT);
//        } else {
//            showDialog(null, mAppContext.getString(R.string.timing_pausing));
//            activateTimer.startActivateTimer(timing.getUid(), UserCache.getCurrentUserName(mAppContext), timing.getTimingId(), TimingConstant.TIMEING_PAUSE);
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
//    public void onDestroy() {
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
