package com.orvibo.homemate.device.sg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.orvibo.homemate.api.listener.OnDeviceDeletedListener;
import com.orvibo.homemate.api.listener.OnNewPropertyReportListener;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceMiniStatus;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.CameraInfoDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.OrviboTime;
import com.orvibo.homemate.data.SaveReminderFlag;
import com.orvibo.homemate.device.CocoDeviceOfflineTipsActivity;
import com.orvibo.homemate.device.ControlRecord;
import com.orvibo.homemate.device.HopeMusic.HopeMusicHelper;
import com.orvibo.homemate.device.control.CameraActivity;
import com.orvibo.homemate.device.energyremind.DeviceEnergyActivity;
import com.orvibo.homemate.device.energyremind.EnergyRemindManager;
import com.orvibo.homemate.device.manage.ClassificationActivity;
import com.orvibo.homemate.device.manage.CommonDeviceActivity;
import com.orvibo.homemate.device.manage.adapter.DevicesAdapter;
import com.orvibo.homemate.device.manage.adapter.HomeCommonDeviceAdapter;
import com.orvibo.homemate.device.manage.add.DeviceAddActivity;
import com.orvibo.homemate.device.manage.add.DeviceAddListActivity;
import com.orvibo.homemate.device.manage.edit.DeviceEditActivity;
import com.orvibo.homemate.device.smartlock.LockRecordActivity;
import com.orvibo.homemate.device.timing.ModeTimingListActivity;
import com.orvibo.homemate.device.timing.TimingCountdownActivity;
import com.orvibo.homemate.device.ys.YsCameraActivity;
import com.orvibo.homemate.event.AlloneViewEvent;
import com.orvibo.homemate.messagepush.MessageActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.model.device.DeviceDeletedReport;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.sharedPreferences.CommonDeviceCache;
import com.orvibo.homemate.sharedPreferences.EnergyReminderCache;
import com.orvibo.homemate.sharedPreferences.GatewayOnlineCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.DialogUtil;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;
import com.orvibo.homemate.view.dialog.ToastDialog;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.SelectFloorRoomPopup;
import com.orvibo.homemate.weather.ui.YahooWeatherView;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;

//import com.squareup.leakcanary.RefWatcher;

/**
 * 设备控制列表。我的设备界面。
 */
public class
DeviceFragment2 extends BaseFragment2 implements OnNewPropertyReportListener,
        OOReport.OnDeviceOOReportListener,
        ProgressDialogFragment.OnCancelClickListener,
        EnergyRemindManager.OnEnergyRemindListener, OnDeviceDeletedListener {
    private static final String TAG = DeviceFragment2.class.getSimpleName();
    private static final int WHAT_REFRESH_DEVICES = 0;
    private static final int WHAT_EMPTY = 1;
    /**
     * 属性报告没有回调
     */
    private static final int WHAT_PROPERTY_NOT_CALLBACK = 2;
    private static final int WHAT_REFRESH_COMMON_DEVICES = 3;

    /**
     * 节能提醒通知
     */
    private static final int WHAT_SHOW_ENERGY_REMIND_TIP = 4;
    /**
     * 属性报告上来
     */
    private static final int WHAT_PROPERTY = 5;
    /**
     * 延时刷新属性报告，如果属性报告不断上来会造成app卡死
     */
    private static final int WHAT_PROPERTY_DELAY = 6;

    /**
     * 选择楼层房间
     */
    private static final int WHAT_SELECT_ROOM = 7;

    /**
     * 延时100刷新百分比窗帘界面
     */
    private static final int DELAY_REFRESH_TIME = 100;

    private static final int WHAT_LOAD_TIMER = 8;

    /**
     * 延时5*60*1000 刷新背景音乐的状态
     */
    private static final int FRESH_ONLINE_WHAT = 0;
    private static final int FRESH_ONLINE_DELAY_TIME = 3 * 60 * 1000;
    /**
     * 单位ms
     */
    public static final int TIME_LOAD_TIMER = 500;//结束到属性报告后延时时间进行读取数据
    private DevicesAdapter mDevicesAdapter;

    private DeviceDao mDeviceDao;
    private DeviceStatusDao mDeviceStatusDao;
    private String mCurrentRoomId = "";
    private ControlRecord mControlRecord;
    /**
     * 当前正在控制的设备
     */
    private volatile String mCurrentCtrlDeviceId;

    // 主机的uid
    private ControlDevice mControlDevice;
    private Handler sHandler;

    // view
    private View mBar, energyRemindView;
    private View emptyView;
    private SelectFloorRoomPopup mSelectRoomPopup;
    private ListView mDevices_lv;
    private TextView mTitle_tv, energyRemindText;
    private LinearLayout mSelectRoom_ll;
    private ImageView mArrow_iv;
    private ImageView mAdd_iv;

    private ImageView frag_add_device;
    private TextView centerTextView;

    private YahooWeatherView yahooWeatherView;
    private SwipeRefreshLayout progressView;

    private DeviceOfflinePopup deviceOfflinePopup;

    private volatile boolean isEmptyRoom = true;
    private SensorTimerPush sensorTimerPush;

    private int mHeaderHeight;
    private int mBarHeight;
    private int mMinHeaderTranslation;
    //  private ImageView roomImageView;
    private View tipView;
    private LinearLayout weatherLinearLayout;
    private LinearInterpolator mSmoothInterpolator;

    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();

    private View commonDeviceAdd, deviceGirdView, commonDeviceView, deleteEnergyRemind;
    private GridView commonDeviceGrid;
    private HomeCommonDeviceAdapter commonDeviceAdapter;
    private HopeMusicHelper hopeMusicHelper;

    /**
     * listview滚动状态
     */
    private volatile int mListviewScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * 缓存设备状态。key:uid_deviceId，value:设备开关、离线在线状态。
     */
    private ConcurrentHashMap<String, DeviceMiniStatus> mDeviceMiniStatusMap = new ConcurrentHashMap<>();

    private int mRefreshCount = 0;

    /**
     * 刷新设备线程
     */
    private final ExecutorService mDeviceExecutor = Executors.newSingleThreadExecutor();

    /**
     * 刷新设备线程返回的future，可以取消刷新操作。
     */
    private Future mRefreshFuture;

    private boolean isOverseansVersion;

//    private SearchDevice mSearchDevice;
//    private QueryUnbinds queryUnbinds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        String userName = UserCache.getCurrentUserName(context);
        LogUtil.d(TAG, "onCreate()-userName:" + userName + ",currentMainUid:" + mainUid);
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSmoothInterpolator = new LinearInterpolator();
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mBarHeight = getResources().getDimensionPixelSize(R.dimen.bar_height);
        mMinHeaderTranslation = -mHeaderHeight + mBarHeight;
        isOverseansVersion = !PhoneUtil.isCN(getActivity());
        //初始背景音乐查询
        initHopeMusicHelper();
        mControlRecord = new ControlRecord();
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.fragment_devices2,
                container, false);
        mBar = parentView.findViewById(R.id.bar_rl);
        yahooWeatherView = (YahooWeatherView) parentView.findViewById(R.id.yahooWeatherView);
        weatherLinearLayout = (LinearLayout) yahooWeatherView.findViewById(R.id.weatherLinearLayout);
        mTitle_tv = (TextView) parentView.findViewById(R.id.title_tv);
        mSelectRoom_ll = (LinearLayout) parentView.findViewById(R.id.selectRoom_ll);
        mSelectRoom_ll.setOnClickListener(this);
        mArrow_iv = (ImageView) parentView.findViewById(R.id.arrow_iv);
        mArrow_iv.setVisibility(View.GONE);
        mAdd_iv = (ImageView) parentView.findViewById(R.id.add_iv);
        mAdd_iv.setOnClickListener(this);
        centerTextView = (TextView) parentView.findViewById(R.id.centerTextView);
        mDevices_lv = (ListView) parentView.findViewById(R.id.devices_lv);
        //  mRefreshLayout = (SwipeRefreshLayout) parentView.findViewById(R.id.srl_progress);
        // mRefreshLayout.setColorSchemeResources(R.color.red, R.color.yellow, R.color.red, R.color.yellow, R.color.red, R.color.yellow);
        // initProgress(mRefreshLayout);
        tipView = getActivity().getLayoutInflater().inflate(R.layout.item_devices_adpter_tip_without_header, null, false);
        //mDevices_lv.addHeaderView(tipView);
        progressView = (SwipeRefreshLayout)parentView.findViewById(R.id.srl_progress);

        //  roomImageView = (ImageView) parentView.findViewById(R.id.roomImageView);
//        mDevices_lv.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                mListviewScrollState = scrollState;
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    synchronized (DeviceFragment2.this) {
//                        LogUtil.i(TAG, "onScrollStateChanged()-Ready to refresh,mDeviceMiniStatusMap:" + mDeviceMiniStatusMap);
//                        if (!mDeviceMiniStatusMap.isEmpty()) {
//                            mDevicesAdapter.refreshDeviceStatus(mDeviceMiniStatusMap);
//                            mDeviceMiniStatusMap.clear();
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int scrollY = getScrollY();
//                //  LogUtil.d(TAG, "onScroll() - scrollY = " + scrollY);
//                weatherViewScroll(scrollY);
//            }
//        });

        energyRemindView = parentView.findViewById(R.id.energyRemindView);
        energyRemindView.setOnClickListener(this);
        energyRemindText = (TextView) parentView.findViewById(R.id.energyRemindText);
        deleteEnergyRemind = parentView.findViewById(R.id.deleteEnergyRemind);
        deleteEnergyRemind.setOnClickListener(this);

        commonDeviceAdd = parentView.findViewById(R.id.commonDeviceAdd);
        commonDeviceAdd.setOnClickListener(this);
        deviceGirdView = parentView.findViewById(R.id.deviceGirdView);
        commonDeviceGrid = (GridView) parentView.findViewById(R.id.commonDeviceGrid);
        commonDeviceView = parentView.findViewById(R.id.commonDeviceView);
        commonDeviceView.setVisibility(View.VISIBLE);

        emptyView = parentView.findViewById(R.id.deviceEmptyLinearLayout);
        frag_add_device = (ImageView) parentView.findViewById(R.id.frag_add_device);
        if (isOverseansVersion) {
            frag_add_device.setImageResource(R.drawable.empty_overseans);
            centerTextView.setVisibility(View.GONE);
        } else {
            frag_add_device.setOnClickListener(this);
            centerTextView.setOnClickListener(this);
        }

        deviceOfflinePopup = new DeviceOfflinePopup();

        initToolbar(parentView);

        initHandler();
        initSensorTimerPush();
        initControl();

        yahooWeatherView.refreshWeather();

        yahooWeatherView.setVisibility(View.GONE);
        mBar.setVisibility(View.GONE);
        View toolbar = parentView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }

        PropertyReport.getInstance(mAppContext).registerNewPropertyReport(this);
        OOReport.getInstance(mAppContext).registerOOReport(this);
        EnergyRemindManager.getInstance(mAppContext).registerEnergyRemindListener(this);
        return parentView;
    }

    public void initToolbar(View parentView) {
        Toolbar toolbar = (Toolbar)parentView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
        toolbar.setTitle(R.string.devices);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListviewScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
        onRefresh();
        if (this.isVisible()) {
            checkBackMusic();
        }
        DeviceDeletedReport.getInstance().addDeviceDeletedListener(this);


    }

    @Override
    public void onRefresh() {
        mainUid = UserCache.getCurrentMainUid(context);
        LogUtil.i(TAG, "onRefresh()-mainUid:" + mainUid);
        selectRoom();
        yahooWeatherView.initWeatherInfo();
    }

    @Override
    public void onVisible() {
        super.onVisible();
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "onVisible()");
        }
        refreshAllRoomDevices();
        checkBackMusic();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (HopeOnlineHandler != null) {
            HopeOnlineHandler.removeMessages(FRESH_ONLINE_WHAT);
        }
    }

    /**
     * 初始向往背景音乐帮助类
     */
    public void initHopeMusicHelper() {
        hopeMusicHelper = new HopeMusicHelper(getActivity());
        initLoginHopeServer();
    }

    /**
     * 登陆向往服务器查询状态
     */
    private void checkBackMusic() {
        String username = UserCache.getCurrentUserName(getActivity());
        Account account = new AccountDao().selMainAccountdByUserName(userName);
        String phone = "";
        if (account != null) {
            phone = account.getPhone();
            if (phone == null) {
                phone = "";
            }
        }
        if (username != null && mDeviceDao.userIsHaveDeviceType(username, phone, DeviceType.BACK_MUSIC)) {
            HopeOnlineHandler.removeMessages(FRESH_ONLINE_WHAT);
            HopeOnlineHandler.sendEmptyMessage(FRESH_ONLINE_WHAT);
        }
    }

    private void weatherViewScroll(int scrollY) {
        int translationY = Math.max(-scrollY, mMinHeaderTranslation);
        yahooWeatherView.setTranslationY(Math.min(translationY, 0));
        float ratio = clamp(yahooWeatherView.getTranslationY() / mMinHeaderTranslation, 0.0f, 1.0f);
        interpolate(weatherLinearLayout, mBar, mSmoothInterpolator.getInterpolation(ratio));
    }

    private void initSensorTimerPush() {
        sensorTimerPush = new SensorTimerPush(context) {
            @Override
            public void onSensorTimerPushResult(int result, int type) {
                if (Conf.DEBUG_MAIN) {
                    LogUtil.d(Conf.TAG_MAIN, "onSensorTimerPushResult()-result:" + result);
                }
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    refreshAllRoomDevices();
                }
            }

            @Override
            public void onAllSensorSetTimerPushResult(int result) {

            }
        };
    }

    private void selectRoom() {
        LogUtil.d(TAG, "selectRoom()-start");
        //!isFirstEnter
        mSelectRoomPopup = new SelectFloorRoomPopup(getActivity(), mArrow_iv, true, true, false, false, true) {
            @Override
            protected void onRoomSelected(Floor floor, Room room) {
                super.onRoomSelected(floor, room);
                LogUtil.d(TAG, "onRoomSelected()-floor:" + floor + ",room:" + room + ",thread:" + Thread.currentThread());
                if (isDetached() || isRemoving()) {
                    return;
                }
                if (Conf.DEBUG_MAIN) {
                    LogUtil.d(Conf.TAG_MAIN, "onRoomSelected()-floor:" + floor + ",room:" + room + ",thread:" + Thread.currentThread());
                }
                if (floor != null) {
                    if (room != null) {
                        mCurrentRoomId = room.getRoomId();
                    }
                    if (room != null && !StringUtil.isEmpty(mCurrentRoomId) && mCurrentRoomId.equals(Constant.ALL_ROOM)) {
                        mCurrentRoomId = Constant.ALL_ROOM;
                    }
                } else {
                    mCurrentRoomId = Constant.ALL_ROOM;
                }

                if (sHandler != null) {
                    Message msg = sHandler.obtainMessage(WHAT_SELECT_ROOM);
                    Bundle bundle = msg.getData();
                    bundle.putSerializable(IntentKey.ROOM, room);
                    bundle.putSerializable(IntentKey.FLOOR, floor);
                    msg.setData(bundle);
                    sHandler.sendMessage(msg);
                }
                refreshAllRoomDevices();
            }
        };
        mDeviceExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mSelectRoomPopup.init();
                isEmptyRoom = mSelectRoomPopup.isEmptyRoom();
            }
        });
//        new Thread() {
//            @Override
//            public void run() {
//                mSelectRoomPopup.init();
//                isEmptyRoom = mSelectRoomPopup.isEmptyRoom();
//            }
//        }.start();
        LogUtil.d(TAG, "selectRoom()-end");
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.classification_item) {
            //传感器、门锁类
            List<Device> devices = (List<Device>) v.getTag();
            if (devices.size() == 1) {
                Device device = devices.get(0);
                int deviceType = device.getDeviceType();
                if (deviceType == DeviceType.LOCK) {
                    if (ProductManage.isSmartLock(device)) {
                        Intent intent = new Intent(context, LockRecordActivity.class);
                        intent.putExtra(IntentKey.DEVICE, device);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, ClassificationActivity.class);
                        intent.putExtra(IntentKey.DEVICES, (Serializable) devices);
                        context.startActivity(intent);
                    }
                } else {
                    jumpControl(device);
                }
            } else {
                Intent intent = new Intent(context, ClassificationActivity.class);
                intent.putExtra(IntentKey.DEVICES, (Serializable) devices);
                context.startActivity(intent);
            }
        } else if (vId == R.id.timingTextView) {
            //设备定时倒计时s
            Device device = (Device) v.getTag(R.id.tag_device);
            toTimmingActivity(device);
        } else if (vId == R.id.model_layout) {
            //wifi设备数量<3时显示的item，模式定时
            Device device = (Device) v.getTag(R.id.tag_device);
            Intent intent = new Intent(context, ModeTimingListActivity.class);
            intent.putExtra(IntentKey.INTENT_SOURCE, DeviceFragment2.class.getSimpleName());
            intent.putExtra(IntentKey.DEVICE, device);
            startActivity(intent);
        } else if (vId == R.id.time_layout) {
            //wifi设备数量<3时显示的item，定时
            Device device = (Device) v.getTag(R.id.tag_device);
            Intent intent = new Intent(context, TimingCountdownActivity.class);
            intent.putExtra("isSingle", true);
            intent.putExtra(IntentKey.INTENT_SOURCE, DeviceFragment2.class.getSimpleName());
            intent.putExtra(Constant.DEVICE, device);
            intent.putExtra("latestType", 0);
            startActivity(intent);
        } else if (vId == R.id.countdown_layout) {
            //wifi设备数量<3时显示的item，倒计时
            Device device = (Device) v.getTag(R.id.tag_device);
            Intent intent = new Intent(context, TimingCountdownActivity.class);
            intent.putExtra(IntentKey.INTENT_SOURCE, DeviceFragment2.class.getSimpleName());
            intent.putExtra("isSingle", true);
            intent.putExtra(Constant.DEVICE, device);
            intent.putExtra("latestType", 1);
            startActivity(intent);
        } else if (vId == R.id.ctrl_iv || vId == R.id.ctrl_tv || vId == R.id.lock_tv || vId == R.id.unlock_tv || vId == R.id.message_tv || vId == R.id.deviceIcon_iv
                || vId == R.id.curtain_on || vId == R.id.curtain_stop_left || vId == R.id.curtain_close || vId == R.id.curtain_stop_right) {
            //控制设备
            control(v);
        } else if (vId == R.id.message_tv) {
            messagePush(v);
        } else if (vId == R.id.title_layout) {
            if (ClickUtil.isFastDoubleClick()) {
                LogUtil.w(TAG, "onClick()-Click too fast,only do once.");
                return;
            }
            Device device = (Device) v.getTag(R.id.tag_device);
            jumpControl(device);
        } else if (vId == R.id.deviceCustomView || vId == R.id.itemView) {
            if (ClickUtil.isFastDoubleClick()) {
                LogUtil.w(TAG, "onClick()-Click too fast,only do once.");
                return;
            }
            final Device device = (Device) v.getTag(R.id.tag_device);
            //如果是机顶盒，把机顶盒的红外码下载下来，方便适配epg和stb
            if (device.getDeviceType() == DeviceType.STB && ProductManage.isAlloneSunDevice(device) && !ProductManage.isAlloneLearnDevice(device)) {
                IrData irData = AlloneCache.getIrData(device.getDeviceId());
                if (irData == null) {
//                showDialog();
                    showDeviceProgress();
                    KookongSDK.getIRDataById(device.getIrDeviceId(), new IRequestResult<IrDataList>() {

                        @Override
                        public void onSuccess(String s, IrDataList irDataList) {
                            //  dismissDialog();
                            stopDeviceProgress();
                            List<IrData> irDatas = irDataList.getIrDataList();
                            IrData localIrData = irDatas.get(0);
                            AlloneCache.saveIrData(localIrData, device.getDeviceId());
                            jumpControl(device);
                        }

                        @Override
                        public void onFail(String s) {
                            //    dismissDialog();
                            stopDeviceProgress();
                            ToastUtil.showToast(R.string.allone_error_data_tip);
                        }
                    });
                } else
                    jumpControl(device);
            } else
                jumpControl(device);
        } else if (vId == R.id.selectRoom_ll) {
            if (mSelectRoomPopup != null) {
                if (!mSelectRoomPopup.isShowing()) {
                    mSelectRoomPopup.show(mBar);
                } else {
                    mSelectRoomPopup.dismissAfterAnim();
                }
            }
        } else if (vId == R.id.add_iv) {
            //添加
            if (!ClickUtil.isFastDoubleClick()) {
                if (!PhoneUtil.isCN(getActivity())) {
                    toDeviceAddListActivity();
                } else {
                    toAddDeviceActivity();
                }
            }
        } else if (vId == R.id.infoPushCountView) {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            startActivity(intent);
            context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (vId == R.id.frag_add_device || vId == R.id.centerTextView) {
            // toAddDeviceActivity();
            if (!PhoneUtil.isCN(getActivity())) {
                return;
            } else {
                toCaptureActivity();
            }
        }
        switch (vId) {
            case R.id.energyRemindView:
                //节能提醒
                ActivityJumpUtil.jumpAct(getActivity(), DeviceEnergyActivity.class);
                break;
            case R.id.deleteEnergyRemind:
                energyRemindView.setVisibility(View.GONE);
                EnergyReminderCache.setEnergyReminderTime(mainUid, System.currentTimeMillis());
                break;
            case R.id.commonDeviceAdd:
                ActivityJumpUtil.jumpAct(getActivity(), CommonDeviceActivity.class);
                break;
        }
    }

    private void toCaptureActivity() {
        Intent intent = new Intent(context, CaptureActivity.class);
        startActivity(intent);
    }

    private void toAddDeviceActivity() {
        Intent intent = new Intent(context, DeviceAddActivity.class);
        startActivity(intent);
    }

    /**
     * 添加设备时
     * 海外版本直接跳转到设备列表界面
     */
    private void toDeviceAddListActivity() {
        Intent intent = new Intent(context, DeviceAddListActivity.class);
        startActivity(intent);
    }

    private void toTimmingActivity(Device device) {
        StatService.trackCustomKVEvent(context, getString(R.string.MTAClick_COCO_Timer), null);
        Intent intent = new Intent(context, TimingCountdownActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        intent.putExtra(IntentKey.INTENT_SOURCE, DeviceFragment2.class.getSimpleName());
        intent.putExtra("latestType", mDevicesAdapter.latestTypes.get(device.getDeviceId()));
        startActivity(intent);
    }

    private void toSettingsActivity(Device device) {
        if (ProductManage.getInstance().isCOCO(device)) {
            StatService.trackCustomKVEvent(context, getString(R.string.MTAClick_COCO_Settings), null);
        }
        Intent intent = new Intent(context, DeviceEditActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }

    /**
     * 控制设备
     *
     * @param view
     */
    public void control(View view) {
        if (view.getId() != R.id.message_tv && !NetUtil.isNetworkEnable(context)) {
            removeCheckPropertyCallbackMessage();
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            return;
        }
        Device device = (Device) view.getTag(R.id.tag_device);
        String deviceId = device.getDeviceId();
        if (mControlRecord.hasDeviceAction(deviceId)) {
            LogUtil.w(TAG, "control()-正在控制" + device.getDeviceName() + ",mControlRecord:" + mControlRecord);
            return;
        }
        removeCheckPropertyCallbackMessage();
        showDeviceProgress();
        int curStatus = Integer.valueOf(view.getContentDescription() + "");
        mControlRecord.recordDeviceAction(deviceId, DeviceTool.getOppositeValue1(device, curStatus));
        //如果设备离线，则只请求一次socket。控制前不判断是否是否离线。
//        boolean isOnline = checkOnline(device);
        final int appDeviceId = device.getAppDeviceId();
        final int deviceType = device.getDeviceType();
        if (appDeviceId == AppDeviceId.SWITCH
                || appDeviceId == AppDeviceId.DIMMER_SWITCH
                || appDeviceId == AppDeviceId.OUTLET
                || appDeviceId == AppDeviceId.LAMP
                || appDeviceId == AppDeviceId.RGB
                || appDeviceId == AppDeviceId.RGB_CONTROLLER
                || appDeviceId == AppDeviceId.DIMMER
                || appDeviceId == AppDeviceId.SWITCH_LAMP
                || appDeviceId == AppDeviceId.COCO
                || deviceType == DeviceType.DIMMER
                || deviceType == DeviceType.LAMP
                || deviceType == DeviceType.RGB
                || deviceType == DeviceType.OUTLET
                || deviceType == DeviceType.SWITCH_RELAY
                || deviceType == DeviceType.COLOR_TEMPERATURE_LAMP
                || deviceType == DeviceType.COCO
                || ProductManage.getInstance().isWifiOnOffDevice(device)
                ) {
            //开关控制
            deviceControl(view);
        } else if (appDeviceId == AppDeviceId.CURTAIN_CONTROL_BOX
                || appDeviceId == AppDeviceId.CURTAIN_CONTROLLER
                || deviceType == DeviceType.SCREEN
                || deviceType == DeviceType.WINDOW_SHADES
                || deviceType == DeviceType.CURTAIN
                || deviceType == DeviceType.CURTAIN_PERCENT
                || deviceType == DeviceType.ROLLER_SHADES_PERCENT
//                || deviceType == DeviceType.WINDOW_SHADES_PERCENT
                || deviceType == DeviceType.PUSH_WINDOW
                ) {
            //窗帘控制
            curtainControl(view);
        } else if (appDeviceId == AppDeviceId.LOCK
                || deviceType == DeviceType.LOCK) {
        }
    }

    private void jumpControl(Device device) {
        LogUtil.d(TAG, "jumpControl()-device:" + device);
        if (device != null && !StringUtil.isEmpty(device.getDeviceId())) {
            // 进入设备二级控制界面
            String actionActivityName;
            int deviceType = device.getDeviceType();
            // String model = device.getModel();
            if (deviceType == DeviceType.CAMERA) {
                CameraInfoDao cameraInfoDao = new CameraInfoDao();
                CameraInfo cameraInfo = new CameraInfoDao().selCameraInfoByUid(device.getUid());
                if (cameraInfo == null) {
                    cameraInfo = cameraInfoDao.selCameraInfoByUid(device.getExtAddr());
                }
                if (cameraInfo != null && cameraInfo.getType() == CameraType.CAMERA_530) {
                    actionActivityName = CameraActivity.class.getName();
                } else if (cameraInfo != null && (cameraInfo.getType() == CameraType.YS_NO_YUNTAI || cameraInfo.getType() == CameraType.YS_YUNTAI)) {
                    actionActivityName = YsCameraActivity.class.getName();
                } else {
                    actionActivityName = YsCameraActivity.class.getName();
                }
            } else if (ProductManage.isSmartLock(device)) {
                actionActivityName = LockRecordActivity.class.getName();
            } else {
                actionActivityName = DeviceTool.getControlActivityNameByDeviceType(device);
            }
            if (!StringUtil.isEmpty(actionActivityName)) {
                Intent intent = new Intent();
                intent.setClassName(context, actionActivityName);
                intent.putExtra(IntentKey.DEVICE, device);
                intent.putExtra(IntentKey.IS_HOME_CLICK, true);
                //http://blog.csdn.net/aiqing0119/article/details/7785813
                Activity activity = getActivity();
                if (activity != null) {
                    activity.startActivity(intent);
                } else {
                    startActivity(intent);
                }
            }
        }
    }

    private void messagePush(View v) {
        MessagePush messagePush = (MessagePush) v.getTag(R.id.tag_message_push);
        Device device = (Device) v.getTag(R.id.tag_device);
        if (messagePush == null) {
            messagePush = new MessagePush();
            messagePush.setTaskId(device.getDeviceId());
            messagePush.setIsPush(MessagePushStatus.ON);
            messagePush.setType(MessagePushType.SINGLE_SENSOR_TYPE);
            messagePush.setStartTime("00:00:00");
            messagePush.setEndTime("00:00:00");
            messagePush.setWeek(255);
        }
        if (messagePush.getIsPush() == MessagePushStatus.ON) {
            messagePush.setIsPush(MessagePushStatus.OFF);
        } else {
            messagePush.setIsPush(MessagePushStatus.ON);
        }
        sensorTimerPush.startSetDeviceTimerPush(device.getDeviceId(), messagePush.getIsPush(), messagePush.getStartTime(), messagePush.getEndTime(), messagePush.getWeek());

    }

//    private boolean checkOnline(Device device) {
//        boolean isOnline = true;
//        if (DeviceUtil.isDeviceOnline(context, device.getUid(), device.getDeviceId()) == OnlineStatus.OFFLINE) {
////            deviceOfflinePopup.showPopup(context, getResources()
////                    .getString(R.string.device_offline_tips), getResources()
////                    .getString(R.string.know), null);
//            if (mDevicesAdapter != null) {
//                mDevicesAdapter.refreshOnline(device.getUid(), device.getDeviceId(), OnlineStatus.OFFLINE);
//            }
//            isOnline = false;
//        }
//        return isOnline;
//    }

    private void setEmptyTitle() {
        if (mTitle_tv != null) {
            mTitle_tv.setText("");
        }
    }

    private void setRoomView(Floor floor, Room room) {
        if (isEmptyRoom) {
            setEmptyTitle();
        }
        String title = getString(R.string.main_bottom_tab_device);
        if (floor != null) {
            String floorName = floor.getFloorName();
            if (room != null) {
                title = floorName + " " + room.getRoomName();
                mCurrentRoomId = room.getRoomId();
            }
            if (room != null && !StringUtil.isEmpty(mCurrentRoomId) && mCurrentRoomId.equals(Constant.ALL_ROOM)) {
                title = room.getRoomName();
                mCurrentRoomId = Constant.ALL_ROOM;
            }
        } else {
            mCurrentRoomId = Constant.ALL_ROOM;
        }
        mTitle_tv.setText(title);
    }

    /**
     * 设备控制(开关)
     *
     * @param v
     */
    private void deviceControl(View v) {
//        showEmptyDialog();
//        showProgress();
        //  mControlDevice.stopControl();
        int curStatus = Integer.valueOf(v.getContentDescription() + "");
        final Device device = (Device) v.getTag(R.id.tag_device);
        LogUtil.d(TAG, "deviceControl()-device:" + device + ",status:" + curStatus);
        final String deviceId = device.getDeviceId();
        final String uid = device.getUid();
        if (curStatus == DeviceStatusConstant.ON) {
            mControlDevice.off(uid, deviceId);
        } else {
            mControlDevice.on(uid, deviceId);
        }
        refreshDeviceStatus();
    }

    /**
     * 窗帘开、关、停控制
     *
     * @param v
     */
    private void curtainControl(View v) {
//        showDeviceControlProgressDialog();
        showDeviceProgress();
        int curStatus = Integer.valueOf(v.getContentDescription() + "");
        final Device device = (Device) v.getTag(R.id.tag_device);
        LogUtil.d(TAG, "curtainControl()-device:" + device + ",status:" + curStatus);
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        mControlDevice.stopControl();
        int vId = v.getId();
        if (vId == R.id.curtain_stop_left || vId == R.id.curtain_stop_right) {
            mControlDevice.curtainStop(uid, deviceId);
        } else if (vId == R.id.curtain_on) {
            if (device.getDeviceType() == DeviceType.SCREEN) {
                mControlDevice.curtainClose(uid, deviceId);
            } else {
                mControlDevice.curtainOpen(uid, deviceId);
            }
        } else if (vId == R.id.curtain_close) {
            if (device.getDeviceType() == DeviceType.SCREEN) {
                mControlDevice.curtainOpen(uid, deviceId);
            } else {
                mControlDevice.curtainClose(uid, deviceId);
            }
        }
    }

    // 设备属性报告
    @Override
    public void onNewPropertyReport(Device device, DeviceStatus deviceStatus, PayloadData payloadData) {
        LogUtil.i(TAG, "onNewPropertyReport()-device:" + device + ",deviceStatus:" + deviceStatus);
        if (device == null || deviceStatus == null) {
            return;
        }
        final String deviceId = device.getDeviceId();
        final int appDeviceId = device.getAppDeviceId();
        final int deviceType = device.getDeviceType();
        final String uid = device.getUid();
        final int value1 = deviceStatus.getValue1();
        if (mCurrentCtrlDeviceId != null && mCurrentCtrlDeviceId.equals(deviceId)) {
            removeCheckPropertyCallbackMessage();
        }
        mControlRecord.removeCtrlAction(deviceId);
        DeviceMiniStatus deviceMiniStatus = new DeviceMiniStatus(uid, deviceId, value1, OnlineStatus.ONLINE, deviceStatus == null ? System.currentTimeMillis() : deviceStatus.getUpdateTime());

        //listview不滚动，刷新数据
        if (mListviewScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            //常用设备（ListView）上的数据刷新
            if (mDevicesAdapter != null) {
                if (appDeviceId == AppDeviceId.CURTAIN_CONTROL_BOX
                        || appDeviceId == AppDeviceId.CURTAIN_CONTROLLER
                        || deviceType == DeviceType.SCREEN
                        || deviceType == DeviceType.WINDOW_SHADES
                        || deviceType == DeviceType.CURTAIN
                        || deviceType == DeviceType.CURTAIN_PERCENT
                        || deviceType == DeviceType.ROLLER_SHADES_PERCENT
                        || deviceType == DeviceType.PUSH_WINDOW
                        ) {
                    if (value1 == DeviceStatusConstant.CURTAIN_INIT) {
                        ToastUtil.showToast(R.string.curtain_init_tips);
                    } else {
                        Message message = sHandler.obtainMessage(WHAT_PROPERTY_DELAY);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("deviceMiniStatus", deviceMiniStatus);
                        message.setData(bundle);
                        if (sHandler.hasMessages(WHAT_PROPERTY_DELAY)) {
                            sHandler.removeMessages(WHAT_PROPERTY_DELAY);
                        }
                        sHandler.sendMessageDelayed(message, DELAY_REFRESH_TIME);
                    }
                } else {
                    //mDevicesAdapter.refreshDeviceStatus(uid, deviceId, value1, payloadData);
                    mDevicesAdapter.refreshDeviceStatus(deviceMiniStatus);
                    LogUtil.e(TAG, "DeviceMiniStatus=" + deviceMiniStatus);
                }
            } else {
                LogUtil.e(TAG, "onNewPropertyReport()-mDevicesAdapter is null");
            }
            //常用设备（GridView）上的数据刷新
            if (commonDeviceAdapter != null) {
                commonDeviceAdapter.notifyDataSetChanged();
            }

            if (deviceType == DeviceType.LOCK) {
                if (!ProductManage.isSmartLock(device)) {
                    //旧门锁弹框提示门锁开or关
                    if (value1 == DeviceStatusConstant.ON) {
                        ToastDialog.show(context, context.getResources().getString(R.string.lock_opened));
                    } else if (value1 == DeviceStatusConstant.OFF) {
                        ToastDialog.show(context, context.getResources().getString(R.string.lock_closed));
                    } else {
                        MyLogger.kLog().e(device + "," + deviceStatus + " 's value1 error.");
                    }
                }
            } else if (ProductManage.getInstance().isLight(deviceType)) {
                //显示节能提醒
                showEnergyRemindTip();
            }
        } else {
            //如果listview正在滚动，则先缓存状态且不刷新，当listview停止滚动时再刷新
//            synchronized (this) {
//                mDeviceStatuses.put(deviceId, deviceStatus);
//            }
            synchronized (this) {
                mDeviceMiniStatusMap.put(deviceId, deviceMiniStatus);
            }
        }

        //有coco，并且少于三个设备
        if (mDevicesAdapter != null && mDevicesAdapter.hasCOCOItem) {
            sHandler.removeMessages(WHAT_LOAD_TIMER);
            Message message = sHandler.obtainMessage(WHAT_LOAD_TIMER);
            message.obj = device;
            sHandler.sendMessageDelayed(message, TIME_LOAD_TIMER);
        }
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (mDevicesAdapter != null) {
            if (ProductManage.getInstance().isWifiDeviceByUid(uid)) {
                mDevicesAdapter.refreshOnline(uid, deviceId, online);
            } else if (DeviceUtil.isAllowZigbeeDeviceOfflineByDeviceId(deviceId)) {
                mDevicesAdapter.refreshOnline(uid, deviceId, online);
            }
        }
        if (commonDeviceAdapter != null) {
            commonDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCancelClick(View view) {
        LogUtil.d(TAG, "onCancelClick()-cancel progressdialog");
        if (mControlDevice != null) {
            mControlDevice.stopControl();
        }
        stopProgress();
    }

    @Override
    public void onDeviceDeleted(String uid, String deviceId) {
        //首页处于可见状态才刷新页面
        try {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.isMainVisible()) {
                refreshAllRoomDevices();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHandler() {
        sHandler = new MyHandler();
    }

    @Override
    public void onEnergyRemind(List<Device> energyRemindDevices) {
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (!isAdded()) {
                LogUtil.w(TAG, "handleMessage()-DeviceFragment is not added to activity.");
                return;
            }

            switch (msg.what) {
                case WHAT_REFRESH_DEVICES:
                    showEmptyView(false);
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "handleMessage()-start to refresh device list at ui thread");
                    }
                    //add 没有modify
                    //没有房间，不显示选择界面
                    if (mSelectRoomPopup != null && mSelectRoomPopup.isEmptyRoom()) {
                        mSelectRoom_ll.setVisibility(View.GONE);
                        mSelectRoom_ll.setOnClickListener(null);
                    } else {
                        mArrow_iv.setVisibility(View.GONE);
                        mSelectRoom_ll.setOnClickListener(DeviceFragment2.this);
                    }
                    Bundle bundle = msg.getData();
                    List<Device> devices = (List<Device>) bundle.getSerializable("devices");
                    List<DeviceStatus> deviceStatuses = (List<DeviceStatus>) bundle.getSerializable("deviceStatuses");
                    // LogUtil.d(TAG, "handleMessage()-devices:" + devices);
                    boolean firstLoadDevice = bundle.getBoolean("firstLoadDevice");
                    if (mDevicesAdapter == null || firstLoadDevice) {
//                        if (mDevicesAdapter == null) {
//                            mDevicesAdapter = new DevicesAdapter(context, devices, new ArrayList<List<Device>>(),
//                                    deviceStatuses, DeviceFragment.this);
//                        }
                        mDevices_lv.setAdapter(mDevicesAdapter);
                    } else {
//                        if (devices != null && devices.size() == 0) {
//                            if (isFragmentVisible()) {
//                                ToastUtil.showToast(R.string.device_empty_toast);
//                            }
//                        }
                        mDevicesAdapter.setDataChanged(devices,
                                deviceStatuses);
                    }
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "handleMessage()-Finish to refresh device list at ui thread");
                    }
                    setIsAllRoom();
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "handleMessage()-Finish to refresh device room at ui thread");
                    }
                    break;
                case WHAT_EMPTY:
                    showEmptyView(true);
                    break;
                case WHAT_PROPERTY_NOT_CALLBACK:
                    if (isFragmentVisible()) {
                        //控制的设备在指定时间内没有属性报告，app就提示超时
                        ToastUtil.toastError(ErrorCode.TIMEOUT_CD);

                        //指定时间内没有属性报告上报，刷新设备列表，显示当前的设备状态
                        String deviceId = (String) msg.obj;
                        if (!TextUtils.isEmpty(deviceId) && mControlRecord != null) {
                            if (mControlRecord.hasDeviceAction(deviceId)) {
                                mControlRecord.removeCtrlAction(deviceId);
                                refreshDeviceStatus();
                            }
                        }
                    }
                    break;
                case WHAT_REFRESH_COMMON_DEVICES:
                    if (isAdded()) {
                        Bundle commonBundle = msg.getData();
                        List<Device> allCommonDevices = (List<Device>) commonBundle.getSerializable("allCommonDevices");
                        List<Device> commonDevices = (List<Device>) commonBundle.getSerializable("commonDevices");
                        updateCommonDeviceData(allCommonDevices, commonDevices);
                    }
                    break;
                case WHAT_SHOW_ENERGY_REMIND_TIP:
                    //节能提醒
                    int lightOpenCount = msg.arg1;
                    if (lightOpenCount > 0) {
                        String s = null;
                        if (lightOpenCount > 1) {
                            s = String.format(getString(R.string.energy_remind_tips4), lightOpenCount);
                        } else {
                            s = String.format(getString(R.string.energy_remind_tips5), lightOpenCount);
                        }
                        energyRemindView.setVisibility(View.VISIBLE);
                        int green = getResources().getColor(R.color.green);
                        energyRemindText.setText(Html.fromHtml(String.format("%s&nbsp;<u><font color=\"#31c37c\">%s</font></u>", s, getString(R.string.energy_remind_tips3))));
                    } else {
                        energyRemindView.setVisibility(View.GONE);
                    }
                    break;
                case WHAT_PROPERTY:
                    break;
                case WHAT_SELECT_ROOM:
                    Bundle roomBundle = msg.getData();
                    Floor floor = (Floor) roomBundle.getSerializable(IntentKey.FLOOR);
                    Room room = (Room) roomBundle.getSerializable(IntentKey.ROOM);
                    setRoomView(floor, room);
                    break;
                case WHAT_PROPERTY_DELAY:
                    Bundle proBundle = msg.getData();
//                    int value1 = proBundle.getInt("value1");
//                    String uid = proBundle.getString("uid");
//                    String deviceId = proBundle.getString("deviceId");
                    DeviceMiniStatus deviceMiniStatus = (DeviceMiniStatus) proBundle.getSerializable("deviceMiniStatus");
                    LogUtil.d(TAG, "handleMessage()-refresh delay deviceMiniStatus = " + deviceMiniStatus);
                    if (mDevicesAdapter != null) {
                        mDevicesAdapter.refreshCurtainStatus(deviceMiniStatus);
                    }
                    break;
                case WHAT_LOAD_TIMER:
                    loadTimer((Device) msg.obj);
                    break;
            }
        }
    }

    private void loadTimer(Device device) {
        if (device == null) {
            return;
        }
        if (NetUtil.isNetworkEnable(mAppContext)) {
            LoadUtil.noticeLoadServerData(LoadParam.getLoadServerParam(mAppContext));
        }
    }


    private void initControl() {
        // 控制回调
        mControlDevice = new ControlDevice(mAppContext) {

            @Override
            public void onControlDeviceResult(String uid, String deviceId,
                                              int result) {
                LogUtil.d(TAG, "onControlDeviceResult()-uid:" + uid + ",deviceId:" + deviceId + ",result:" + result);
//                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    if (mControlRecord.hasDeviceAction(deviceId)) {
                        mControlRecord.removeCtrlAction(deviceId);
                        refreshDeviceStatus();
                    }
                }

                stopDeviceProgress();
                mCurrentCtrlDeviceId = deviceId;
                if (result == ErrorCode.SUCCESS) {
                    sendCheckPropertyCallbackMessage(deviceId);
                    refreshDeviceStatus();
                } else if (result == ErrorCode.OFFLINE_GATEWAY
                        || result == ErrorCode.OFFLINE_DEVICE) {
                    //优先处理离线问题。一些wifi设备需要弹界面告诉用户，zigbee设备只需toast
                    processOffline(mDeviceDao.selDevice(uid, deviceId), result);
                } else if (ErrorCode.isCommonError(result)) {
                    ToastUtil.toastError(result);
                } else if (!mDeviceStatusDao.isOnline(uid, deviceId)) {
                    processOffline(mDeviceDao.selDevice(uid, deviceId), result);
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
        mControlDevice.setForAllDevice(true);
    }

    /**
     * 仅仅刷新列表状态
     */
    private void refreshDeviceStatus() {
        mDevicesAdapter.notifyDataSetChanged();

        if (commonDeviceAdapter != null) {
            commonDeviceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 处理离线控制结果
     *
     * @param device 控制的设备
     * @param result 控制结果
     */
    private void processOffline(Device device, int result) {
        String uid = device.getUid();
        String deviceId = device.getDeviceId();
        //不需要处理zigbee设备离线
        if (mDevicesAdapter != null && ProductManage.getInstance().isWifiDevice(device)) {
            mDevicesAdapter.refreshOnline(uid, deviceId, OnlineStatus.OFFLINE);
        }
        if (ProductManage.getInstance().isS20orS25(device) || ProductManage.getInstance().isOrviboCOCO(device)) {
            showS20OfflineTips(device);
        } else if (ProductManage.getInstance().isWifiDevice(device)) {
            ToastUtil.toastError(result);
        } else {
            ToastUtil.toastError(ErrorCode.OFFLINE_DEVICE);
        }
    }

    private void showS20OfflineTips(final Device device) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.device_offline_content));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.how_to_fix));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
                if (ProductManage.getInstance().isOrviboCOCO(device)) {
                    DialogUtil.showCocoOfflineSupport(getChildFragmentManager());
                } else {
                    Intent intent = new Intent(mAppContext, CocoDeviceOfflineTipsActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onRightButtonClick(View view) {

            }
        });
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.confirm));
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    private void removeCheckPropertyCallbackMessage() {
        if (sHandler != null) {
            sHandler.removeMessages(WHAT_PROPERTY_NOT_CALLBACK);
        }
    }

    private void sendCheckPropertyCallbackMessage(String deviceId) {
        removeCheckPropertyCallbackMessage();
        if (sHandler != null) {
            Message msg = sHandler.obtainMessage(WHAT_PROPERTY_NOT_CALLBACK);
            msg.obj = deviceId;
            sHandler.sendMessageDelayed(msg, OrviboTime.WAIT_DEVICE_PROPERTY_TIME);
        }
    }

    private void refreshAllRoomDevices() {
        mainUid = UserCache.getCurrentMainUid(context);
        if (mRefreshFuture != null) {
            mRefreshFuture.cancel(true);
        }
        mRefreshFuture = mDeviceExecutor.submit(new RefreshDevicesRunnable());
    }

    private class RefreshDevicesRunnable implements Runnable {
        @Override
        public void run() {
            mRefreshCount += 1;
            mControlRecord.reset();
            long startRefreshTime = System.currentTimeMillis();
            if (Conf.DEBUG_MAIN) {
                LogUtil.e(Conf.TAG_MAIN, "============================Start to refresh device list(" + mRefreshCount + ")================================");
            }
            if (!isAdded()) {
                LogUtil.w(TAG, "RefreshDevicesRunnable()-DeviceFragment not added.");
                return;
            }

            if (sHandler == null) {
                LogUtil.w(TAG, "RefreshDevicesRunnable()-sHandler is null.");
                return;
            }

            mainUid = UserCache.getCurrentMainUid(context);

            int totalCount = mDeviceDao.selZigbeeDeviceTotalCount(mainUid);

            boolean isNoneZigbeeDevice = totalCount <= 0;

//            if (Conf.DEBUG_MAIN) {
//                LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-refresh 1");
//            }
            if (Conf.DEBUG_MAIN) {
                LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-refresh 2");
            }
            String userId = UserCache.getCurrentUserId(context);
            MyLogger.kLog().d("userId:" + userId + ",uid:" + mainUid);
            List<Device> wifiDevices = mDeviceDao.selWifiDevicesByUserId(userId);
            if (Conf.DEBUG_MAIN) {
                LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-refresh 3");
            }
//            LogUtil.d(TAG, "RefreshDevicesRunnable()-isEmptyDevice:" + isEmptyDevice + ",mainUid:" + mainUid);
//            LogUtil.d(TAG, "RefreshDevicesRunnable()-wifiDevices:" + wifiDevices + ",userId:" + userId);
            //true没有zigbee设备和wifi设备
            boolean noZigbeeDeviceAndWifiDevice = false;
            if (isNoneZigbeeDevice && (wifiDevices == null || wifiDevices.isEmpty())) {
                //显示empty设备界面
                sHandler.sendEmptyMessage(WHAT_EMPTY);
                //没有zigbee设备和wifi设备，尝试搜索设备
                noZigbeeDeviceAndWifiDevice = true;
            }
//            LogUtil.d(TAG, "run()-isCheckedUnbindDevice:" + isCheckedUnbindDevice);
//            if (!isCheckedUnbindDevice) {
//                isCheckedUnbindDevice = true;
//                mSearchDevice = new SearchDevice(getActivity(), DeviceFragment.this);
//                mainUid = UserCache.getCurrentMainUid(context);
//                if (TextUtils.isEmpty(mainUid)) {
//                    //没有绑定主机
//                    mSearchDevice.searchHub(noZigbeeDeviceAndWifiDevice);
//                } else if (noZigbeeDeviceAndWifiDevice) {
//                    //搜索wifi设备
//                    mSearchDevice.searchWifiDevice();
//                }
//            }
            if (!noZigbeeDeviceAndWifiDevice) {
                List<Device> tempDevices;
                if (mCurrentRoomId.equals(Constant.ALL_ROOM)) {
                    tempDevices = mDeviceDao.selAllRoomControlDevices(mainUid);
                    List<Device> aDevices = new ArrayList<>();
                    for (Device wifiDevice : wifiDevices) {
                        boolean contains = false;
                        String wifiDeviceId = wifiDevice.getDeviceId();
                        if (StringUtil.isEmpty(wifiDeviceId)) {
                            continue;
                        }
                        for (Device tempDevice : tempDevices) {
                            if (wifiDeviceId.equals(tempDevice.getDeviceId())) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            aDevices.add(wifiDevice);
                        }
                    }
                    if (!aDevices.isEmpty()) {
                        tempDevices.addAll(aDevices);
                    }
                } else {
                    tempDevices = mDeviceDao.selDevicesByRoom(mainUid, mCurrentRoomId);
                }
                if (Conf.DEBUG_MAIN) {
                    LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-refresh 4");
                }
                List<DeviceStatus> statuses = new ArrayList<DeviceStatus>();
                for (Device device : tempDevices) {
                    String uid = device.getUid();
                    String deviceId = device.getDeviceId();
                    DeviceStatus deviceStatus;
                    if (DeviceUtil.isIrDevice(device)) {
                        deviceStatus = mDeviceStatusDao.selIrDeviceStatus(uid, device.getExtAddr());
                    } else {
                        deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
                    }
                    if (deviceStatus != null) {
                        statuses.add(deviceStatus);
                    } else {
                        LogUtil.w(TAG, "RefreshDevicesRunnable()-Count not found deviceId:" + deviceId + ",deviceName:" + device.getDeviceName() + "'s deviceStatus.");
                    }
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-" + device.getDeviceName() + "'s deviceStatus is " + deviceStatus);
                    }
                    // LogUtil.d(TAG, "" + device);
                }

//                LogUtil.d(TAG, "RefreshDevicesRunnable()-devices:" + tempDevices);
//                LogUtil.d(TAG, "RefreshDevicesRunnable()-deviceStatuses:" + statuses);
                if (sHandler != null) {
                    Message msg = sHandler.obtainMessage(WHAT_REFRESH_DEVICES);
                    Bundle bundle = msg.getData();
                    if (mDevicesAdapter == null) {
                        bundle.putBoolean("firstLoadDevice", true);
                        mDevicesAdapter = new DevicesAdapter(context, tempDevices, new ArrayList<List<Device>>(),
                                statuses, DeviceFragment2.this, mControlRecord);
                    } else {
                        bundle.putBoolean("firstLoadDevice", false);
                    }
                    bundle.putSerializable("devices", (Serializable) tempDevices);
                    bundle.putSerializable("deviceStatuses", (Serializable) statuses);
                    sHandler.sendMessage(msg);
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-refresh 6");
                    }

                    //常用设备
                    List<Device> deviceList = DeviceTool.getAllCommonDevices(context);
//                    MyLogger.jLog().d("userId=" + UserCache.getCurrentUserId(context) + "  size=" + deviceList.size());
//        List<Device> devices = mDeviceDao.selDevicesByCommonFlag(UserCache.getCurrentUserId(context), CommonFlag.COMMON);
                    List<Device> devices = DeviceTool.getCommonDevices(deviceList);
                    Message commonMsg = sHandler.obtainMessage(WHAT_REFRESH_COMMON_DEVICES);
                    Bundle commonBundle = commonMsg.getData();
                    commonBundle.putSerializable("allCommonDevices", (Serializable) deviceList);
                    commonBundle.putSerializable("commonDevices", (Serializable) devices);
                    sHandler.sendMessage(commonMsg);

                    //节能提醒
                    showEnergyRemindTip();
                } else {
                    LogUtil.w(TAG, "RefreshDevicesRunnable()-sHandler is null!");
                }
                if (Conf.DEBUG_MAIN) {
                    LogUtil.d(Conf.TAG_MAIN, "RefreshDevicesRunnable()-Finish to refresh,cost " + (System.currentTimeMillis() - startRefreshTime) + "ms");
                }
                if (Conf.DEBUG_MAIN) {
                    long curTime = System.currentTimeMillis();
                    long costTime = curTime - startRefreshTime;
                    LogUtil.e(Conf.TAG_MAIN, "============================Finish to refresh device list(" + costTime + "ms)================================");
                }
            }
        }
    }

    private class DeviceOfflinePopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    private void showEmptyView(boolean isEmpty) {
        //weatherViewScroll(0);
        if (isEmpty == true) {
            deviceGirdView.setVisibility(View.GONE);
        }
        mDevices_lv.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        mSelectRoom_ll.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);

        float scaleX = 1.0F + interpolation * (-0.2F);
        float scaleY = 1.0F + interpolation * (-0.2F);
        float alpha = 1.0F + interpolation * (-1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.25F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - yahooWeatherView.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
        view1.setAlpha(alpha);
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    @Override
    public void onDestroy() {
        if (mSelectRoomPopup != null && mSelectRoomPopup.isShowing()) {
            mSelectRoomPopup.dismissAfterAnim();
        }

        if (deviceOfflinePopup != null && deviceOfflinePopup.isShowing()) {
            deviceOfflinePopup.dismiss();
        }
        if (context != null) {
            Context appContext = context.getApplicationContext();
            PropertyReport.getInstance(appContext).unregisterNewPropertyReport(this);
            OOReport.getInstance(appContext).removeOOReport(this);
            EnergyRemindManager.getInstance(appContext).unregisterEnergyRemindListener(this);
        }

        if (sHandler != null) {
            sHandler.removeCallbacksAndMessages(null);
            sHandler = null;
        }

        if (hopeMusicHelper != null) {
            hopeMusicHelper.setLoginHopeServerListener(null);
        }
        if (HopeOnlineHandler != null) {
            HopeOnlineHandler.removeCallbacksAndMessages(null);
            HopeOnlineHandler = null;
        }
        super.onDestroy();
        if (mDeviceExecutor != null) {
            mDeviceExecutor.shutdownNow();
        }
        if (mControlDevice != null) {
            mControlDevice.stopControl();
        }

        if (yahooWeatherView != null) {
            yahooWeatherView.release();
        }
        DeviceDeletedReport.getInstance().stopAcceptDeviceDeletedReport();
    }

    /**
     * 节能提醒tip
     */
    private void showEnergyRemindTip() {
        if (EnergyReminderCache.getEnergyReminder(getActivity().getApplicationContext(), mainUid) == SaveReminderFlag.OFF) {
            noticeRefreshEnergyRemindTip(0);
            return;
        }
        int hour = Integer.parseInt(TimeUtil.getHour(System.currentTimeMillis()));
        if (hour < 10 || hour > 15) {
            noticeRefreshEnergyRemindTip(0);
            return;
        }
        long deleteTime = EnergyReminderCache.getEnergyReminderTime(mainUid);
        int deleteHour = Integer.parseInt(TimeUtil.getHour(deleteTime));
        if (deleteHour >= 10 && deleteHour <= 15 && (System.currentTimeMillis() - deleteTime) <= 14 * 60 * 60 * 1000) {
            noticeRefreshEnergyRemindTip(0);
            return;
        }
        int size = DeviceUtil.getEnergyDevices(mainUid).size();
        noticeRefreshEnergyRemindTip(size);
    }

    /**
     * handler通知释放显示节能提醒
     *
     * @param count 开灯的数量
     */
    private void noticeRefreshEnergyRemindTip(int count) {
        if (sHandler != null) {
            Message msg = sHandler.obtainMessage(WHAT_SHOW_ENERGY_REMIND_TIP);
            msg.arg1 = count;
            sHandler.sendMessage(msg);
        }
    }

    /**
     * 常用设备展示
     */
    private void updateCommonDeviceData(List<Device> allCommonDevices, List<Device> commonDevices) {
//        List<Device> deviceList = mDeviceDao.selCommonDevicesByRoom(UserCache.getCurrentUserId(context), "", DeviceUtil.getTypeSQL(5));
        //   List<Device> deviceList = DeviceTool.getAllCommonDevices(context);
//        MyLogger.jLog().d("userId=" + UserCache.getCurrentUserId(context) + "  size=" + allCommonDevices.size());
//        List<Device> devices = mDeviceDao.selDevicesByCommonFlag(UserCache.getCurrentUserId(context), CommonFlag.COMMON);
        // List<Device> devices = DeviceTool.getCommonDevices(context);
        String currentUserId = UserCache.getCurrentUserId(ViHomeApplication.getAppContext());
        boolean showCommonDeviceView = CommonDeviceCache.getBoolean(ViHomeApplication.getAppContext(), currentUserId);
        if (allCommonDevices.size() < 10 && commonDevices.isEmpty() && !showCommonDeviceView) {
            deviceGirdView.setVisibility(View.GONE);
            commonDeviceAdd.setVisibility(View.GONE);
            return;
        }
        //一旦常用设备显示出来，就不消失
        //设置标志为ture
        CommonDeviceCache.putBoolean(ViHomeApplication.getAppContext(), currentUserId, true);
        if (commonDevices.size() > 0) {
            deviceGirdView.setVisibility(View.VISIBLE);
            commonDeviceAdd.setVisibility(View.GONE);
            if (commonDevices.size() < 4) {
                Device device = new Device();
                device.setDeviceId("");
                commonDevices.add(device);
            }
            if (commonDeviceAdapter == null) {
                commonDeviceAdapter = new HomeCommonDeviceAdapter(commonDevices, mControlRecord);
                commonDeviceGrid.setAdapter(commonDeviceAdapter);
                commonDeviceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Device device = commonDeviceAdapter.getItem(position);
                        LogUtil.d(TAG, "onItemClick()-control " + device);
                        if (TextUtils.isEmpty(device.getDeviceId()))
                            ActivityJumpUtil.jumpAct(getActivity(), CommonDeviceActivity.class);
                        else if (commonDeviceAdapter.isCanClick(device))
                            //常用设备控制
                            control(view);
                        else {
                            jumpControl(device);
                        }
                    }
                });
            }
            commonDeviceAdapter.setDataChanged(commonDevices);
        } else {
            deviceGirdView.setVisibility(View.GONE);
            commonDeviceAdd.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置是否为所有房间判断
     */
    private void setIsAllRoom() {
        if (mCurrentRoomId.equals(Constant.ALL_ROOM))
            mDevicesAdapter.setIsAllRoom(true);
        else
            mDevicesAdapter.setIsAllRoom(false);
    }

    /**
     * 获取listview滑动高度
     *
     * @return
     */
    public int getScrollY() {
        View c = mDevices_lv.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = mDevices_lv.getFirstVisiblePosition();
        int top = c.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = tipView.getHeight();
        }
        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private void initLoginHopeServer() {
        if (hopeMusicHelper == null) {
            return;
        }
        hopeMusicHelper.setLoginHopeServerListener(new HopeMusicHelper.LoginHopeServerListener() {
            @Override
            public void loginSuccess(String token) {
                hopeMusicHelper.getHopeIsOnlineList(new HopeMusicHelper.GetHopeOnlineListener() {
                    @Override
                    public void callBackOnlineList(List<HashMap<String, String>> list) {

                        if (list != null) {
                            int size = list.size();
                            //如果该账号下没有背景音乐，则停止刷新
                            if (size == 0) {
                                HopeOnlineHandler.removeMessages(FRESH_ONLINE_WHAT);
                            } else {
                                for (int i = 0; i < size; i++) {
                                    HashMap<String, String> hashMap = list.get(i);
                                    String uid = hashMap.get("uid");
                                    String IsOnline = hashMap.get("IsOnline");
                                    if (IsOnline.equals("true")) {
                                        GatewayOnlineCache.setOnline(getActivity(), uid);
                                    } else {
                                        GatewayOnlineCache.setOffline(getActivity(), uid);
                                    }
                                }
                                if (mDevicesAdapter != null) {
                                    mDevicesAdapter.notifyDataSetChanged();
                                }
                                if (HopeOnlineHandler != null) {
                                    HopeOnlineHandler.removeMessages(FRESH_ONLINE_WHAT);
                                    HopeOnlineHandler.sendEmptyMessageDelayed(FRESH_ONLINE_WHAT, FRESH_ONLINE_DELAY_TIME);
                                }
                            }
                        }
                    }

                    @Override
                    public void error(String error) {

                    }
                });

            }

            @Override
            public void loginFail(String msg) {

            }
        });
    }

    private Handler HopeOnlineHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FRESH_ONLINE_WHAT) {
                hopeMusicHelper.loginHopeServer();
            }
        }
    };

    /**
     * 更换遥控器或者空调点击状态有变化时，更新对应的view
     *
     * @param event
     */
    public void onEventMainThread(AlloneViewEvent event) {
        if (event.isHomeRefresh()) {
            selectRoom();//首页控制小方子设备，如果设备离线，控制成功，更新设备状态
        } else if (mDevicesAdapter != null && !event.isControl())
            mDevicesAdapter.notifyDataSetChanged();
//        notifyDataSetChanged();
    }

    /**
     * 显示进度
     */
    public void showDeviceProgress() {
        if (progressView == null) {
            Log.e("DeviceFragment", "progress is null, can't run progress");
            return;
        }

        progressView.post(new Runnable() {
            @Override
            public void run() {
                progressView.setRefreshing(true);
            }
        });
    }

    /**
     * 停止进度
     */
    public void stopDeviceProgress() {
        if (progressView == null) {
            Log.e("DeviceFragment", "progress is null, can't stop progress");
            return;
        }

        if (progressView != null) {
            progressView.setRefreshing(false);
        }
    }
}
