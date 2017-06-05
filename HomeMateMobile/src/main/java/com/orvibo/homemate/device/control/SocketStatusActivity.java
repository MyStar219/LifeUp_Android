package com.orvibo.homemate.device.control;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.EventDataListener;
import com.orvibo.homemate.api.listener.OnDeviceDeletedListener;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.bo.TimingGroup;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CountdownDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.dao.TimingGroupDao;
import com.orvibo.homemate.dao.UserGatewayBindDao;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.CountdownConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.OrviboTime;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.CocoDeviceOfflineTipsActivity;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.device.manage.edit.DeviceEditActivity;
import com.orvibo.homemate.device.timing.DeviceCountdownCreateActivity;
import com.orvibo.homemate.device.timing.DeviceTimingListActivity;
import com.orvibo.homemate.device.timing.ModeTimingListActivity;
import com.orvibo.homemate.device.timing.TimingCountdownActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.device.DeviceDeletedReport;
import com.orvibo.homemate.model.power.DevicePower;
import com.orvibo.homemate.model.power.QueryPower;
import com.orvibo.homemate.model.power.QueryPowerEvent;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.family.FamilyInviteActivity;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DialogUtil;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

/**
 * 不管wifi设备的显示状态是否是离线，点击后都可以发送控制指令给设备
 * 设备如果当前显示离线
 * 1.如果控制成功，底层会修改状态
 */
public class SocketStatusActivity extends BaseControlActivity implements View.OnClickListener,
        NavigationCocoBar.OnRightClickListener,
        EventDataListener, OOReport.OnDeviceOOReportListener, OnDeviceDeletedListener {
    private static final String TAG = SocketStatusActivity.class.getSimpleName();
    private static final String LOCK = "lock";
    private View contentView;
    private Context context = this;
    private TextView timingTextView, coundDownTextView, mode_show_text, power_show_text;
    private TextView table_pattern, table_time, table_count, table_electricity;
    private TextView zigbee_time_tv;
    private RelativeLayout wifi_content;
    private View wifi_fill_view;
    private NavigationCocoBar navigationBar;
    //    protected ControlDevice controlDevice;

    private LinearLayout  wifi_add_time_layout,
            zigbee_add_time_layout, power_layout;
    //    private ImageView  statusImageView;
    private ImageView onOffImageView;
    private int status = DeviceStatusConstant.OFF;
    private static final int STOP_ANIM = 1; // 结束动画标识
    private static final int START_ANIM = 2;// 开始动画标识
    private static final int WHAT_PROPERTY_NOT_CALLBACK = 3;
    private int WHAT_REFRESH = 5;
    private boolean isControlSuccess = false;
    private ExecutorService singleThreadExecutor = null; // 线程池
    private boolean canShare = false;
    private Timer timer = new Timer();
    /**
     * 最近需要执行的动作类型
     * 0 表示定时类型； 1表示倒计时类型
     */
    private int latestType;

    private static final int RECYCLE_CONTROL = 4;
    private StringBuilder stringBuilder = new StringBuilder();
    private int count = 0;
    private final String fileName = "ControlRecord_" + LogcatHelper.getDateEN() + ".txt";

    private DeviceDao mDeviceDao;
    private int WHAT_LOAD_TIMER = 1;
    private DeviceStatus mDeviceStatus;

    private QueryPower queryPower;
    private static final int FRESH_POWER = 0x222;

    private TimingGroupDao timingGroupDao;
    /**
     * 实时功耗
     */
    private String mCurrentPower = "0";

    private ImageView mNormal, mWave1, mWave2, mWave3;

    private AnimationSet mAnimationSet1, mAnimationSet2, mAnimationSet3;
    private static final int OFFSET = 600;  //每个动画的播放时间间隔
    private static final int MSG_WAVE2_ANIMATION = 2;
    private static final int MSG_WAVE3_ANIMATION = 3;

    private Handler mAniHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAVE2_ANIMATION:
                    mWave2.setVisibility(View.VISIBLE);
                    mWave2.startAnimation(mAnimationSet2);
                    break;
                case MSG_WAVE3_ANIMATION:
                    mWave3.setVisibility(View.VISIBLE);
                    mWave3.startAnimation(mAnimationSet3);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        mDeviceDao = new DeviceDao();
        initView();
        initTimer();
        OOReport.getInstance(mAppContext).registerOOReport(this);
        DeviceDeletedReport.getInstance().addDeviceDeletedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        setCompentent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OOReport.getInstance(mAppContext).removeOOReport(this);
        isControlSuccess = true;
        handler.sendEmptyMessage(STOP_ANIM);
        mHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        saveControlRecord();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        //终止查询实时功耗
        if (queryPower != null) {
            queryPower.stopQuery();
        }
        DeviceDeletedReport.getInstance().removeDeviceDeletedListener(this);
    }

    /**
     * 初始化控件、参数
     */
    private void initView() {

        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);

        power_show_text = (TextView) findViewById(R.id.power_show_text);
        timingTextView = (TextView) findViewById(R.id.timingTextView);
        coundDownTextView = (TextView) findViewById(R.id.coundDownTextView);
        mode_show_text = (TextView) findViewById(R.id.mode_show_text);
        table_pattern = (TextView) findViewById(R.id.table_pattern);
        table_time = (TextView) findViewById(R.id.table_time);
        table_count = (TextView) findViewById(R.id.table_count);
        table_electricity = (TextView) findViewById(R.id.table_electricity);
        zigbee_time_tv = (TextView) findViewById(R.id.zigbee_time_tv);
        wifi_content = (RelativeLayout) findViewById(R.id.wifi_content);
        wifi_fill_view = (View) findViewById(R.id.wifi_fill_view);

        mWave1 = (ImageView) findViewById(R.id.wave1);
        mWave2 = (ImageView) findViewById(R.id.wave2);
        mWave3 = (ImageView) findViewById(R.id.wave3);

        mAnimationSet1 = initAnimationSet();
        mAnimationSet2 = initAnimationSet();
        mAnimationSet3 = initAnimationSet();


//        timingTextView.setOnClickListener(this);
        onOffImageView = (ImageView) findViewById(R.id.onOffImageView);
//        statusImageView = (ImageView) findViewById(R.id.statusImageView);
        wifi_add_time_layout = (LinearLayout) findViewById(R.id.wifi_add_time_layout);
        zigbee_add_time_layout = (LinearLayout) findViewById(R.id.zigbee_add_time_layout);
        power_layout = (LinearLayout) findViewById(R.id.power_layout);
        contentView = findViewById(R.id.rl_content_ll);

        onOffImageView.setOnClickListener(this);
        navigationBar.setOnRightClickListener(this);
        table_pattern.setOnClickListener(this);
        table_time.setOnClickListener(this);
        table_count.setOnClickListener(this);
        table_electricity.setOnClickListener(this);
        zigbee_add_time_layout.setOnClickListener(this);

//        DeviceStatusDao deviceStatusDao = new DeviceStatusDao();
        mDeviceStatus = mDeviceStatusDao.selDeviceStatus(uid, deviceId);
        if (mDeviceStatus != null) {
            if (mDeviceStatus.getOnline() == OnlineStatus.ONLINE) {
                status = mDeviceStatus.getValue1();
            } else {
                status = mDeviceStatus.getValue1();
                setCurrentPower("0", false);
            }
        } else {
            LogUtil.e(TAG, "initView()-Can't obtain " + deviceId + " device's deviceStatus at " + uid);
        }
        timingGroupDao = new TimingGroupDao();
    }

    private void setCompentent() {
        //先判断是否是S31(带有能量统计)
        if (ProductManage.getInstance().isS31(device)) {

            initQueryPower();
            wifi_fill_view.setVisibility(View.VISIBLE);
            power_layout.setVisibility(View.VISIBLE);
            mode_show_text.setVisibility(View.VISIBLE);
            wifi_add_time_layout.setVisibility(View.VISIBLE);
            zigbee_add_time_layout.setVisibility(View.GONE);
            table_electricity.setVisibility(View.VISIBLE);
        } else {
            //不带能量统计的普通wifi插座
            if (ProductManage.getInstance().isWifiDevice(device)) {
                power_layout.setVisibility(View.GONE);
                mode_show_text.setVisibility(View.VISIBLE);
                wifi_add_time_layout.setVisibility(View.VISIBLE);
                zigbee_add_time_layout.setVisibility(View.GONE);
                ((ViewGroup) (table_electricity.getParent())).setVisibility(View.GONE);
            }
            //zigbee 插座
            else {
                wifi_fill_view.setVisibility(View.GONE);
                wifi_content.setVisibility(View.GONE);
                zigbee_add_time_layout.setVisibility(View.VISIBLE);
            }
        }
        if (mode_show_text.getVisibility() == View.VISIBLE && device != null) {
            TimingGroup effectTimingGroup = null;
            List<TimingGroup> timingGroups = timingGroupDao.selTimingGroupsByDeviceId(device.getUid(), device.getDeviceId());
            for (TimingGroup timingGroup : timingGroups) {
                if (timingGroup.getIsPause() == TimingConstant.TIMEING_EFFECT) {
                    effectTimingGroup = timingGroup;
                    break;
                }
            }
            if (effectTimingGroup != null) {
                mode_show_text.setText(getString(R.string.mode_current) + effectTimingGroup.getName());
            } else {
                mode_show_text.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 初始化定时器
     */
    private void initTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {//倒计时只显示在TextView上面，倒计时的逻辑应该内聚到TextView里面，避免太大的关联性，不会影响到程序的其他功能，执行效率也会高一些。—— By Allen
            @Override
            public void run() {
                Message mesasge = new Message();
                mesasge.what = WHAT_REFRESH;
                mHandler.sendMessage(mesasge);
            }
        }, 0, 1000);
    }

    private void initQueryPower() {
        if (queryPower == null) {
            queryPower = new QueryPower(mAppContext);
            queryPower.setEventDataListener(this);
        }
        mHandler.removeMessages(FRESH_POWER);
        mHandler.sendEmptyMessageDelayed(FRESH_POWER, 0);
    }

    private AnimationSet initAnimationSet() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1f, 1.6f, 1f, 1.6f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(OFFSET * 3);
        sa.setRepeatCount(Animation.INFINITE);// 设置循环
        AlphaAnimation aa = new AlphaAnimation(1, 0.1f);
        aa.setDuration(OFFSET * 3);
        aa.setRepeatCount(Animation.INFINITE);//设置循环
        as.addAnimation(sa);
        as.addAnimation(aa);
        return as;
    }

    private void showWaveAnimation() {
        mWave1.setVisibility(View.VISIBLE);
        mWave1.startAnimation(mAnimationSet1);
        mAniHandler.sendEmptyMessageDelayed(MSG_WAVE2_ANIMATION, OFFSET);
        mAniHandler.sendEmptyMessageDelayed(MSG_WAVE3_ANIMATION, OFFSET * 2);
    }

    private void clearWaveAnimation() {
        mWave1.setVisibility(View.GONE);
        mWave2.setVisibility(View.GONE);
        mWave3.setVisibility(View.GONE);
        mWave1.clearAnimation();
        mWave2.clearAnimation();
        mWave3.clearAnimation();
        mAniHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置S31实时功耗
     *
     * @param power 功耗
     * @param isOn  true设备开状态 false关闭状态 插座关的情况下，实时功率显示为0W
     */
    private void setCurrentPower(String power, boolean isOn) {
        LogUtil.d(TAG, "setCurrentPower()-power:" + power + ",isOn:" + isOn);
        if (isOn) {
            power_show_text.setText(power + "W");
        } else {
            power_show_text.setText(0 + "W");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_REFRESH) {
                if (!isFinishingOrDestroyed() && device != null) {
                    synchronized (LOCK) {
                        setTiming();
                    }
                }
            } else if (msg.what == WHAT_LOAD_TIMER) {
                loadTimer();
            } else if (msg.what == FRESH_POWER) {
                if (queryPower != null) {
                    mHandler.removeMessages(FRESH_POWER);
                    mHandler.sendEmptyMessageDelayed(FRESH_POWER, 10 * 1000);
                    queryPower.queryPower(uid, deviceId);
                }
            }
        }
    };

    private void loadTimer() {
        if (NetUtil.isNetworkEnable(mAppContext)) {
            LoadUtil.noticeLoadData(mAppContext, uid, Constant.INVALID_NUM);
        }
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        //true立即处理控制失败结果，false交给父类处理控制失败结果。
        boolean processNow = false;
        if (result != ErrorCode.SUCCESS) {
//            if (ErrorCode.isCommonError(result)) {
//                processNow = false;
//            } else
            if (result == ErrorCode.OFFLINE_DEVICE || !isOnline()) {
                if (ProductManage.getInstance().isS20orS25(device) || ProductManage.getInstance().isOrviboCOCO(device)) {
                    showS20OfflineTips();
                } else {
                    ToastUtil.showToast(getString(R.string.device_offline));
                }
                processNow = true;
            } else if (ErrorCode.isCommonError(result)) {
                processNow = false;
            }
//            else {
//                ToastUtil.toastError(result);
//            }
            isControlSuccess = false;
            stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + count + "次控制:失败(" + result + ")\n");
        } else {
            isControlSuccess = true;
            sendCheckPropertyCallbackMessage(deviceId);
            stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + count + "次控制:成功\n");
        }
        if (stringBuilder.length() > 1024) {
            saveControlRecord();
        }
        Message msg = new Message();
        msg.what = STOP_ANIM;
        Bundle bundle = new Bundle();
        bundle.putInt("result", result);
        msg.setData(bundle);
        handler.sendMessage(msg);
        if (processNow) {
            return false;
        } else {
            return super.onControlResult(uid, deviceId, result);
        }
    }

    private void saveControlRecord() {
        if (Conf.TEST_DEBUG) {
            String string = stringBuilder.toString();
            String filePath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + fileName;
            try {
                RandomAccessFile raf = new RandomAccessFile(new File(filePath), "rws");
                long length = raf.length();
                raf.seek(length);
                raf.write(string.getBytes());
                raf.close();
                stringBuilder.delete(0, stringBuilder.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport value1:" + value1);
        isControlSuccess = true;
        removeCheckPropertyCallbackMessage();
        status = value1;
        setStatus(value1);
        setCurrentPower(mCurrentPower, value1 == DeviceStatusConstant.ON);
        handler.sendEmptyMessage(STOP_ANIM);
        if (device != null) {
            List<Timing> timings = new TimingDao().selTimingsByDevice(device.getUid(), device.getDeviceId());
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
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",deviceId:" + deviceId + ",online:" + online);
        if (!TextUtils.isEmpty(deviceId) && deviceId.equals(this.deviceId)) {
            if (OnlineStatus.OFFLINE == online) {
                setCurrentPower(mCurrentPower, false);
            }
        }
    }

    @Override
    public void onDeviceDeleted(String delUid, String deviceId) {
        if (!TextUtils.isEmpty(delUid) && delUid.equals(uid)) {
            if (TextUtils.isEmpty(deviceId) || deviceId.equals(SocketStatusActivity.this.deviceId)) {
                //监听到当前页面的wifi设备被删除，跳转到首页。
                MyLogger.kLog().e(uid + " has been deleted,go back main.");
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    private void refresh() {
        if (device != null) {
            //创维普通灯光到通用设置处可设置缓亮缓灭
            if (ProductManage.isSkySingleLight(device))
                useBaseSetting = true;
            navigationBar.setCenterText(device.getDeviceName());
            canShare = new UserGatewayBindDao().canShare(UserCache.getCurrentUserId(this), uid);
            setStatus(status);
//            initTimer();
        }
    }

    //查询实时功耗返回结果
    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (baseEvent instanceof QueryPowerEvent) {
            if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                DevicePower devicePower = ((QueryPowerEvent) baseEvent).getDevicePower();
                if (devicePower != null & !StringUtil.isEmpty(devicePower.getPower())) {
                    String power = devicePower.getPower();
                    //设备上报数据有可能出现-的。如-0.0,-0.4。app需要把这些设置为0
                    if (Float.parseFloat(devicePower.getPower()) <= 0) {
                        power = "0";
                    }
                    mCurrentPower = power;
                    DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(deviceId);
                    if (deviceStatus != null) {
                        setCurrentPower(mCurrentPower, deviceStatus.isOnline() && deviceStatus.getValue1() == DeviceStatusConstant.ON);
                    }
                }
            } else {

            }
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    @Override
    public void onRightClick(View v) {
        if (ProductManage.getInstance().isWifiDevice(device) && canShare) {
            popupWindow.showAtLocation(contentView, Gravity.RIGHT | Gravity.TOP, 0, getResources().getDimensionPixelSize(R.dimen.coco_popup_margin));
        } else {
            toSettingsActivity();
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Back), null);
        super.onBackPressed();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(FRESH_POWER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOffImageView:
                removeCheckPropertyCallbackMessage();
                onOff();
                break;
            case R.id.table_pattern: {
                Intent intent = new Intent(this, ModeTimingListActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.table_time:
                //定时
                toTimmingActivity(0);
                break;
            case R.id.table_count:
                //倒计时
                toTimmingActivity(1);
//                toCountdownActivity();
                break;
            case R.id.table_electricity:
                Intent intent_ele = new Intent(this, EnergyStatisticActivity.class);
                intent_ele.putExtra(Constant.DEVICE, device);
                startActivity(intent_ele);
                break;
            case R.id.zigbee_add_time_layout:
                //zigbee设备，添加定时
                toTimmingActivity(0);
                break;
            case R.id.settingsTextView: {
                popupWindow.dismiss();
                toSettingsActivity();
                break;
            }
            case R.id.shareTextView: {
                popupWindow.dismiss();
                Intent intent = new Intent(this, FamilyInviteActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    /**
     * 跳转到倒计时页面
     */
    private void toCountdownActivity() {
        Intent intent = new Intent(this, DeviceCountdownCreateActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }

    /**
     * @param latestType 判断跳转到定时还是倒计时
     */
    private void toTimmingActivity(int latestType) {
        if (ProductManage.getInstance().isCOCO(device) || ProductManage.getInstance().isS20(device)) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Timer), null);
            Intent intent = new Intent(this, TimingCountdownActivity.class);
            intent.putExtra("isSingle", true);
            intent.putExtra(Constant.DEVICE, device);
            intent.putExtra("latestType", latestType);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DeviceTimingListActivity.class);
            intent.putExtra(Constant.DEVICE, device);
            startActivity(intent);
        }
    }

    private void toSettingsActivity() {
        if (ProductManage.getInstance().isWifiDevice(device)) {
            if (ProductManage.getInstance().isCOCO(device)) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Settings), null);
            }
            Intent intent = new Intent(this, DeviceEditActivity.class);
            intent.putExtra(Constant.DEVICE, device);
            startActivity(intent);
        } else {
            toSetDevice();
        }
    }

    /**
     * 开关点击
     */
    private void onOff() {
        // 判断网络是否连接
        if (!NetUtil.isNetworkEnable(context)) {
            ToastUtil.showToast(R.string.network_canot_work, ToastType.NULL, Toast.LENGTH_SHORT);
            return;
        }
        if (device == null) {
            return;
        }

        // 开始动画效果
        handler.sendEmptyMessage(START_ANIM);

        try {
            String uid = device.getUid();
            String deviceId = device.getDeviceId();
            if (status == DeviceStatusConstant.OFF) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Open), null);
                controlDevice.on(uid, deviceId);
                stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + ++count + "次控制：开\n");
            } else {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_Close), null);
                controlDevice.off(uid, deviceId);
                stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + ++count + "次控制：关\n");
            }
            if (Conf.TEST_DEBUG) {//TODO 循环控制，正式版本改为false
                handler.sendEmptyMessageDelayed(RECYCLE_CONTROL, 5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * modify by wuliquan 2016-07-18
     * 现在修改为“同时显示最近的定时和倒计时”
     * 原先为：显示最近的定时或者倒计时
     */
    private void setTiming() {
        List<Timing> timings = new TimingDao().selTimingsByDevice(device.getUid(), device.getDeviceId());
        List<Countdown> countdowns = new CountdownDao().selCountdownsByDevice(device.getUid(), device.getDeviceId());
        List<Timing> effectTimings = new ArrayList<Timing>();
        List<Countdown> effectCountdowns = new ArrayList<Countdown>();
        //筛选有效的定时
        for (Timing timing : timings) {
            if (timing.getIsPause() == TimingConstant.TIMEING_EFFECT) {
                effectTimings.add(timing);
            }
        }
        //筛选有效的倒计时
        for (Countdown countdown : countdowns) {
            long countdownInMillis = getLatestCountdownInMills(countdown);
            if (countdown.getIsPause() == CountdownConstant.COUNTDOWN_EFFECT && countdownInMillis > System.currentTimeMillis()) {
                effectCountdowns.add(countdown);
            }
        }
        Timing latestTiming = null;
        Countdown latestCountdown = null;
        StringBuilder timeStringBuilder = new StringBuilder("");
        StringBuilder countStringBuilder = new StringBuilder("");
        long min = Long.MAX_VALUE;
        for (int i = 0; i < effectTimings.size(); i++) {
            Timing timing = effectTimings.get(i);
            long timeInMillis = getLatestTimeInMills(timing);
            if (timeInMillis < min) {
                min = timeInMillis;
//                LogUtil.d(TAG, "setTiming()-timeInMillis:" + timeInMillis);
                latestTiming = timing;
            }
        }

        long minCountdown = Long.MAX_VALUE;
        for (int i = 0; i < effectCountdowns.size(); i++) {
            Countdown countdown = effectCountdowns.get(i);
            long countdownInMillis = getLatestCountdownInMills(countdown);
            if (countdownInMillis < minCountdown) {
                minCountdown = countdownInMillis;
//                LogUtil.d(TAG, "setTiming()-countdownInMillis:" + countdownInMillis);
                latestCountdown = countdown;
            }
        }
////        timingTextView.setVisibility(View.VISIBLE);
//        //TODO
////        if(latestCountdown == null) {
////            latestType = TimingCountdownTabView.TIMING_POSITION;
//            if (latestTiming == null) {
//                if (ProductManage.getInstance().isWifiDevice(device)) {
////                    timingTextView.setText(R.string.device_timing_countdown_add);
//                    //timingTextView.setText("");
//                } else {
////                    timingTextView.setText(R.string.device_timing_add);
//                    //timingTextView.setText("");
//                }
//            }
        if (latestTiming != null) {
            String order = latestTiming.getCommand().equals(DeviceOrder.ON) ? getString(R.string.timing_action_on) : getString(R.string.timing_action_off);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(min);
            String formatString;
            int format = DateUtil.getTimingDateFormat(calendar);
//                LogUtil.d(TAG, "setTiming()-latestTiming:" + latestTiming);
//                LogUtil.d(TAG, "setTiming()-min:" + min + ",format:" + format);
            if (format == 0) {
                formatString = TimeUtil.getTime(mAppContext, latestTiming.getHour(), latestTiming.getMinute());
            } else if (format == 1) {
                formatString = getString(R.string.timing_tomorrow) + TimeUtil.getTime(mAppContext, latestTiming.getHour(), latestTiming.getMinute());
            } else if (format == 2) {
                formatString = getString(R.string.timing_thedayaftertomorrow) + TimeUtil.getTime(mAppContext, latestTiming.getHour(), latestTiming.getMinute());
            } else {
                formatString = (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DAY_OF_MONTH) + " " + TimeUtil.getTime(mAppContext, latestTiming.getHour(), latestTiming.getMinute());
            }
            timeStringBuilder.append(formatString + " " + order);

        }
//        }
//        else {
        if (latestCountdown != null) {
            String remainTime = DateUtil.getRemainCountdownString(latestCountdown.getStartTime(), latestCountdown.getTime());
            String tempAction = mContext.getResources().getString(R.string.device_countdown_action_content);
            String actionString = String.format(tempAction, DeviceTool.getActionName(mContext, latestCountdown));

            countStringBuilder.append(remainTime + "" + actionString);
//            latestType = TimingCountdownTabView.COUNTDOWN_POSITION;
        }
//        }
        String time = timeStringBuilder.toString();
        String countDown = countStringBuilder.toString();

        if (ProductManage.getInstance().isWifiDevice(device)) {
            if (TextUtils.isEmpty(time)) {
                timingTextView.setVisibility(View.GONE);
            } else {
                timingTextView.setVisibility(View.VISIBLE);
                timingTextView.setText(time);
            }

            if (TextUtils.isEmpty(countDown)) {
                coundDownTextView.setVisibility(View.GONE);
            } else {
                coundDownTextView.setVisibility(View.VISIBLE);
                coundDownTextView.setText(countDown);
            }
        } else {
            if (TextUtils.isEmpty(time)) {
                zigbee_time_tv.setText(R.string.device_timing_add);
            } else {
                zigbee_time_tv.setText(time);
            }
        }


    }

    /**
     * 有些定时可能是需要重复执行的定时，计算现在以后每次定时的时间，选择距离现在最近一次
     *
     * @param timing
     * @return
     */
    private long getLatestTimeInMills(Timing timing) {
        long timeInMillis = getTimeInMillis(timing);
        //LogUtil.d(TAG, "getLatestTimeInMills()-timeInMillis:" + timeInMillis);
        int week = timing.getWeek();
        Map<Integer, Integer> weekMap = WeekUtil.weekIntToMap(week);
        if (!weekMap.isEmpty() && weekMap.get(0) > 0) {//重复执行的定时
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timing.getHour());
            calendar.set(Calendar.MINUTE, timing.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            //转化为自定义的星期
            if (dayOfWeek == 1) {
                dayOfWeek = 7;
            } else {
                dayOfWeek = dayOfWeek - 1;
            }
            long now = System.currentTimeMillis();
            long timingTodayTimeMillis = calendar.getTimeInMillis();
            List<Long> weekTimeInMillis = new ArrayList<Long>();
            for (int j = 1; j < 7 + 1; j++) {
                long time;
                Integer integer = weekMap.get(j);
                if (integer != null) {
                    int deltaDay = 0;
                    if (integer > dayOfWeek) {
                        deltaDay = integer - dayOfWeek;
                    } else if (integer == dayOfWeek) {
                        if (now > timingTodayTimeMillis) {
                            deltaDay = integer + 7 - dayOfWeek;
                        }
                    } else {
                        deltaDay = integer + 7 - dayOfWeek;
                    }
                    time = timingTodayTimeMillis + deltaDay * 24 * 60 * 60 * 1000;
                    weekTimeInMillis.add(time);
                }
            }
            // LogUtil.d(TAG, "getLatestTimeInMills()-weekTimeInMillis:" + weekTimeInMillis);
            long min = Long.MAX_VALUE;
            for (int k = 0; k < weekTimeInMillis.size(); k++) {
                long wTimeInMillis = weekTimeInMillis.get(k);
                if (wTimeInMillis < min) {
                    min = wTimeInMillis;
                    timeInMillis = wTimeInMillis;
                }
            }
        }
        return timeInMillis;
    }

    /**
     * 获取最近的一次倒计时执行时间
     *
     * @param countdown
     * @return
     */
    private long getLatestCountdownInMills(Countdown countdown) {
        long countdownInMillis = ((long) (countdown.getStartTime() + countdown.getTime() * 60)) * 1000;
        //LogUtil.d(TAG, "getLatestCountdownInMills()-countdownInMillis:" + countdownInMillis);
        return countdownInMillis;
    }

    /**
     * 获取毫秒
     */
    private long getTimeInMillis(Timing timing) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timing.getHour());
        calendar.set(Calendar.MINUTE, timing.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        //  LogUtil.d(TAG, "getTimeInMillis()-time:" + time + ",now:" + now);
        if (time < now) {
            time += 24 * 60 * 60 * 1000;
        }
        return time;
    }

    /**
     * 设置背景状态
     */
    private void setStatus(int status) {
        if (status == DeviceStatusConstant.ON) {
            navigationBar.setBarColor(getResources().getColor(R.color.green));
            navigationBar.setLeftDrawableLeft(getResources().getDrawable(R.drawable.back));
            if (ProductManage.getInstance().isWifiDevice(device) && canShare) {
                // navigationBar.setRightDrawableLeft(getResources().getDrawable(R.drawable.more_white_selector));
                navigationBar.setRightDrawableRight(getResources().getDrawable(R.drawable.more_white_selector));
            } else {
                // navigationBar.setRightDrawableLeft(getResources().getDrawable(R.drawable.setting_white_selector));
                navigationBar.setRightDrawableRight(getResources().getDrawable(R.drawable.setting_white_selector));
            }

            navigationBar.setCenterTextColor(getResources().getColor(R.color.white));
//            statusImageView.setImageResource(R.drawable.socket_open_background);
            onOffImageView.setImageResource(R.drawable.bt_switch_open_normal);
            onOffImageView.setTag("on");
            contentView.setBackgroundColor(getResources().getColor(R.color.green));
//            timingTextView.setTextColor(getResources().getColor(R.color.white));
//            timingTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_white_timing), null, null, null);

        } else {
            navigationBar.setBarColor(getResources().getColor(R.color.socket_close_dark));
            navigationBar.setLeftDrawableLeft(getResources().getDrawable(R.drawable.back));
            if (ProductManage.getInstance().isWifiDevice(device) && canShare) {
                navigationBar.setRightDrawableRight(getResources().getDrawable(R.drawable.more_white_selector));
            } else {
                navigationBar.setRightDrawableRight(getResources().getDrawable(R.drawable.setting_white_selector));
            }

            navigationBar.setCenterTextColor(getResources().getColor(R.color.white));
//            statusImageView.setImageResource(R.drawable.socket_close_background);
            onOffImageView.setTag("off");
            onOffImageView.setImageResource(R.drawable.bt_switch_close);
            contentView.setBackgroundColor(getResources().getColor(R.color.socket_close_dark));
//            timingTextView.setTextColor(getResources().getColor(R.color.green));
//            timingTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_green_timing), null, null, null);
        }
//        statusImageView.setVisibility(View.VISIBLE);
        onOffImageView.setClickable(true);
    }

    /**
     * 消息处理handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 停止动画
                case STOP_ANIM:
                    clearWaveAnimation();
                    onOffImageView.setBackgroundResource(R.drawable.bg_switch);
                    onOffImageView.setClickable(true);

//                    statusImageView.setVisibility(View.VISIBLE);
                    //  if (!isControlSuccess) {
                    //  int result = msg.getData().getInt("result", 0);
                    // LogUtil.d(TAG, "result = " + result);
//                        ToastUtil.toastError( result);
//                        ToastUtil.showToast( R.string.ctrl_fail, ToastType.NULL, Toast.LENGTH_SHORT);
                    // showToast(result);
                    //   }
                    break;

                // 开始动画
                case START_ANIM:
                    showWaveAnimation();
                    if (onOffImageView.getTag().equals("on")) {
                        onOffImageView.setBackgroundResource(R.drawable.circle_socket_open_shape);
                    } else {
                        onOffImageView.setBackgroundResource(R.drawable.circle_socket_close_shape);
                    }

                    onOffImageView.setClickable(false);
                    break;
                case WHAT_PROPERTY_NOT_CALLBACK:
                    if (!isFinishingOrDestroyed() && getIsResumed()) {
                        ToastUtil.toastError(ErrorCode.TIMEOUT_CD);
                    }
                    break;
                case RECYCLE_CONTROL:
                    controlDevice.stopControl();
                    String uid = device.getUid();
                    String deviceId = device.getDeviceId();
                    if (status == DeviceStatusConstant.OFF) {
                        controlDevice.on(uid, deviceId);
                        stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + ++count + "次控制：开\n");
                    } else {
                        controlDevice.off(uid, deviceId);
                        stringBuilder.append(DateUtil.getNowStr1() + "设备：" + deviceName + "第" + ++count + "次控制：关\n");
                    }
                    if (stringBuilder.length() > 1024) {
                        saveControlRecord();
                    }
                    handler.sendEmptyMessageDelayed(RECYCLE_CONTROL, 5000);
                    break;
            }
        }
    };

    private void showToast(int result) {
        if (result == ErrorCode.OFFLINE_GATEWAY) {
            if (ProductManage.getInstance().isWifiDevice(device)) {
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.device_offline));
                dialogFragmentTwoButton.setContent(getString(R.string.device_offline_content));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.how_to_fix));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                        dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                        dialogFragmentOneButton.setTitle(getString(R.string.how_to_fix_title));
                        dialogFragmentOneButton.setContent(getString(R.string.how_to_fix_content));
                        dialogFragmentOneButton.show(getFragmentManager(), "");
                    }

                    @Override
                    public void onRightButtonClick(View view) {

                    }
                });
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.confirm));
                dialogFragmentTwoButton.show(getFragmentManager(), "");
            } else {
                ToastUtil.showToast(R.string.DEVICE_OFFIINE_ERROR, ToastType.NULL, Toast.LENGTH_SHORT);
            }
        } else if (result == ErrorCode.TIMEOUT) {
            ToastUtil.showToast(R.string.TIMEOUT, ToastType.NULL, Toast.LENGTH_SHORT);
        } else if (result == ErrorCode.DEVICE_NOT_BIND_USERID) {
            StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_COCO_NoAuthority), null);
            ToastUtil.showToast(R.string.DEVICE_NOT_BIND_USERID, ToastType.NULL, Toast.LENGTH_SHORT);
        } else {
            ToastUtil.toastError(result);
//            ToastUtil.showToast(R.string.ctrl_fail, ToastType.NULL, Toast.LENGTH_SHORT);
        }
    }

    private void showS20OfflineTips() {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.device_offline_content));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.how_to_fix));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
                if (ProductManage.getInstance().isOrviboCOCO(device)) {
                    DialogUtil.showCocoOffline(getFragmentManager());
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
        handler.removeMessages(WHAT_PROPERTY_NOT_CALLBACK);
    }

    private void sendCheckPropertyCallbackMessage(String deviceId) {
        removeCheckPropertyCallbackMessage();
        Message msg = handler.obtainMessage(WHAT_PROPERTY_NOT_CALLBACK);
        msg.obj = deviceId;
        handler.sendMessageDelayed(msg, OrviboTime.WAIT_DEVICE_PROPERTY_TIME);
    }


    /**
     * version 1.8.6 查询设备是否离线 at 2016/6/3 by zhoubaoqi
     *
     * @return
     */
    private boolean isOnline() {
        return mDeviceStatusDao.isOnline(uid, deviceId);
    }

}
