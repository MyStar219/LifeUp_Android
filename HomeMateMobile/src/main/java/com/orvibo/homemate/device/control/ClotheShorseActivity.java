package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.api.listener.OnClotheShorseStatusReportListener;
import com.orvibo.homemate.api.listener.OnClotheShorseTimeReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.clotheshorse.ClotheShorseAllStatus;
import com.orvibo.homemate.bo.clotheshorse.ClotheShorseCountdown;
import com.orvibo.homemate.bo.clotheshorse.ClotheShorseStatus;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.ClotheShorseCountdownDao;
import com.orvibo.homemate.dao.ClotheShorseStatusDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.ClotheShorseOrder;
import com.orvibo.homemate.data.ClotheshorseType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.device.manage.edit.ClotheShorseDeviceEditActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.clotheshorse.ClotheShorseAllStatusEvent;
import com.orvibo.homemate.model.clotheshorse.ClotheShorseStatusReport;
import com.orvibo.homemate.model.clotheshorse.ClotheShorseTimeReport;
import com.orvibo.homemate.sharedPreferences.WindDryingCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 奥科、紫程、晾霸晾衣机
 */
public class ClotheShorseActivity extends BaseActivity implements NavigationCocoBar.OnRightClickListener,
        BaseResultListener,
        OnClotheShorseStatusReportListener, OnClotheShorseTimeReportListener {
    private final String TAG = ClotheShorseActivity.class.getSimpleName();
    private TextView airer_off, airer_lighting, airer_wind_drying, airer_sterilizing, airer_heat_drying, airer_up, airer_stop, airer_down;
    private TextView cth_lighting, cth_drying, cth_sterilizing;
    private NavigationCocoBar navigationBar;
    private Device mDevice;
    private String deviceId, uid, deviceName;

    private List<String> mDeviceIdLists = new ArrayList<String>();

    private ClotheShorseStatus clotheShorseStatus;
    private ClotheShorseCountdown clotheShorseCountdown;
    private ClotheShorseStatusDao clotheShorseStatusDao;
    private ClotheShorseCountdownDao clotheShorseCountdownDao;

    private static int MOTOR_POSITION_MAX = 100;
    private static int MOTOR_POSITION_MIN = 0;
    private static int ZI_CHENG_COUNT_DOWN_TIME = 40;
    private static int LIANG_BA_COUNT_DOWN_TIME = 10;

    private int clotheshorseType;

    String order = ClotheShorseOrder.ON;

    private DeviceDao deviceDao;


    private View bottom_view;

    private Timer mTimer;
    private static final int WHAT_REFRESH_TIME = 0;
    private static final int WHAT_REFRESH_WIND_TIME = 1;
    //    private static final int WHAT_REFRESH_WIND_TIME_STOP = 2;
    private boolean isWindTimeCountDowning;
    private int countdownTime = ZI_CHENG_COUNT_DOWN_TIME;
    //点击关闭烘干触发风干倒计时的标志(为了区分倒计时过程中退出控制界面后又进入界面的继续倒计时的情景)
    private boolean triggerWindTimeCountDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothe_shorse);

        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            mDevice = (Device) serializable;
            deviceId = mDevice.getDeviceId();
            uid = mDevice.getUid();
            deviceName = mDevice.getDeviceName();
        }
        deviceDao = new DeviceDao();
        clotheShorseStatusDao = new ClotheShorseStatusDao();
        clotheShorseCountdownDao = new ClotheShorseCountdownDao();
        ClotheShorseStatusReport.getInstance(mAppContext).registerClotheShorseStatusReport(this);
        mDeviceIdLists.add(deviceId);
        DeviceApi.queryClotheShorseStatus(mDeviceIdLists, this);
//        clotheShorseStatus = clotheShorseStatusDao.selClotheShorseStatus(deviceId);
//        clotheShorseCountdown = clotheShorseCountdownDao.selClotheShorseCountdown(deviceId);
        initView();
//        if (clotheShorseStatus != null) {
//            refreshMotorView();
//            refreshStatus();
//        }
//        if (clotheShorseCountdown != null || clotheShorseStatus != null) {
//            refreshTime();
//        }
        ClotheShorseTimeReport.getInstance(mAppContext).registerClotheShorseTimeReport(this);
        //延时一分钟，每分钟执行一次refreshTime();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(WHAT_REFRESH_TIME);
            }
        }, 60 * 1000, 60 * 1000);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_REFRESH_TIME: {
                    refreshTime();
                    break;
                }
                case WHAT_REFRESH_WIND_TIME: {
//                    LogUtil.d(TAG,"countdownTime = " + countdownTime);
                    //只记住关闭烘干的那个时刻
                    if (!WindDryingCache.getCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId)) {
                        WindDryingCache.setCloseHeatDryingTime(ClotheShorseActivity.this, deviceId);
                        WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, true);
                        WindDryingCache.setWindDryingTime(mContext, countdownTime, deviceId);
                    }
                    if (countdownTime > 0) {
                        countdownTime--;
                        isWindTimeCountDowning = true;
                        if ((clotheshorseType == ClotheshorseType.ZI_CHENG || clotheshorseType == ClotheshorseType.MAI_RUN || clotheshorseType == ClotheshorseType.LIANG_BA) && countdownTime > 0) {
                            cth_drying.setVisibility(View.VISIBLE);
                            airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_select_disable, 0, 0);
                            airer_wind_drying.setEnabled(false);
                        }
                        if (isOverseasVersion) {
                            cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + getString(R.string.cth_close_later) + countdownTime + getString(R.string.time_second));
                        } else {
                            cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + countdownTime + getString(R.string.time_second) + getString(R.string.cth_close_later));
                        }

                        mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_WIND_TIME, 1000);
                    } else {
                        if (!clotheShorseStatus.getHeatDryingState().isEmpty() && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.OFF)) {
                            cth_drying.setVisibility(View.GONE);
                        }
                        // 这个值不一定能赋值成功（当倒计时在最后的几秒退出界面，再进入界面，（倒计时已经结束），windDryingleftTime会小于“0”，那么倒计时会重新开始(设备离线后也会)）
                        airer_wind_drying.setEnabled(true);
                        isWindTimeCountDowning = false;
                        triggerWindTimeCountDown = false;
                        //    refreshStatus();
                        WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, false);
                        LogUtil.d(TAG, "WHAT_REFRESH_WIND_TIME isWindTimeCountDowning = " + isWindTimeCountDowning);
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        clotheShorseStatus = clotheShorseStatusDao.selClotheShorseStatus(deviceId);
        clotheShorseCountdown = clotheShorseCountdownDao.selClotheShorseCountdown(deviceId);
        if (clotheShorseStatus != null) {
            refreshMotorView();
            refreshStatus();
        }
        if (clotheShorseCountdown != null || clotheShorseStatus != null) {
            refreshTime();
        }
        mDevice = deviceDao.selDevice(uid, deviceId);
        if (mDevice == null) {
            ClotheShorseStatusReport.getInstance(mAppContext).unregisterClotheShorseStatusReport(this);
            ClotheShorseTimeReport.getInstance(mAppContext).unregisterClotheShorseTimeReport(this);
            if (mTimer != null) {
                mTimer.cancel();
            }
            finish();
            return;
        }
        navigationBar.setCenterText(mDevice.getDeviceName());
    }

    private void initView() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        navigationBar.setCenterText(deviceName);
        navigationBar.setOnRightClickListener(this);
        airer_off = (TextView) findViewById(R.id.airer_off);
        airer_lighting = (TextView) findViewById(R.id.airer_lighting);
        airer_wind_drying = (TextView) findViewById(R.id.airer_wind_drying);
        airer_sterilizing = (TextView) findViewById(R.id.airer_sterilizing);
        airer_heat_drying = (TextView) findViewById(R.id.airer_heat_drying);
        airer_up = (TextView) findViewById(R.id.airer_up);
        airer_stop = (TextView) findViewById(R.id.airer_stop);
        airer_down = (TextView) findViewById(R.id.airer_down);
        cth_lighting = (TextView) findViewById(R.id.cth_lighting);
        cth_drying = (TextView) findViewById(R.id.cth_drying);
        cth_sterilizing = (TextView) findViewById(R.id.cth_sterilizing);
        bottom_view = findViewById(R.id.bottom_view);
        if (mDevice != null && !StringUtil.isEmpty(mDevice.getModel())
                && mDevice.getModel().equals(ModelID.CLH001)) {
            clotheshorseType = ClotheshorseType.AO_KE;
            bottom_view.setVisibility(View.GONE);
        } else if (mDevice != null && !StringUtil.isEmpty(mDevice.getModel())
                && mDevice.getModel().equals(ModelID.SH40)) {
            clotheshorseType = ClotheshorseType.LIANG_BA;
            // bottom_view.setVisibility(View.GONE);
            cth_lighting.setVisibility(View.GONE);
            cth_sterilizing.setVisibility(View.GONE);
        }
//        else if (mDevice != null && !StringUtil.isEmpty(mDevice.getModel())
//                && mDevice.getModel().equals(ModelID.CLH002)) {
//            clotheshorseType = ClotheshorseType.ZI_CHENG;
//        }
//        else if (mDevice != null && !StringUtil.isEmpty(mDevice.getModel()) && mDevice.getModel().equals(ModelID.CLH003)) {
//            clotheshorseType = ClotheshorseType.MAI_RUN;
//        }
        else {
            //其他未识别的均走紫程逻辑
            clotheshorseType = ClotheshorseType.ZI_CHENG;
        }

        airer_off.setOnClickListener(this);
        airer_lighting.setOnClickListener(this);
        airer_wind_drying.setOnClickListener(this);
        airer_sterilizing.setOnClickListener(this);
        airer_heat_drying.setOnClickListener(this);
        airer_up.setOnClickListener(this);
        airer_stop.setOnClickListener(this);
        airer_down.setOnClickListener(this);
    }

    private void refreshMotorView() {
        if (clotheShorseStatus.getMotorPosition() == MOTOR_POSITION_MAX) {
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN)) {//紫程  100 上升到顶部；0下降到底部
                airer_up.setEnabled(false);
                airer_up.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_up_disable, 0, 0);
                airer_down.setEnabled(true);
                airer_down.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_down, 0, 0);
            } else {
                airer_up.setEnabled(true);
                airer_up.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_up, 0, 0);
                airer_down.setEnabled(false);
                airer_down.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_down_disable, 0, 0);
            }
        } else if (clotheShorseStatus.getMotorPosition() == MOTOR_POSITION_MIN) {
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN)) {
                airer_up.setEnabled(true);
                airer_up.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_up, 0, 0);
                airer_down.setEnabled(false);
                airer_down.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_down_disable, 0, 0);
            } else {
                airer_up.setEnabled(false);
                airer_up.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_up_disable, 0, 0);
                airer_down.setEnabled(true);
                airer_down.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_down, 0, 0);
            }
        } else {
            airer_up.setEnabled(true);
            airer_up.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_up, 0, 0);
            airer_down.setEnabled(true);
            airer_down.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_down, 0, 0);
        }
    }

    private void refreshStatus() {
        //照明
        if (!StringUtil.isEmpty(clotheShorseStatus.getLightingState()) && clotheShorseStatus.getLightingState().equals(ClotheShorseOrder.ON)) {
            airer_lighting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_lighting_select, 0, 0);
        } else {
            airer_lighting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_lighting_unselect, 0, 0);
        }
        if (!StringUtil.isEmpty(clotheShorseStatus.getSterilizingState()) && clotheShorseStatus.getSterilizingState().equals(ClotheShorseOrder.ON)) {
            airer_sterilizing.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_disinfect_select, 0, 0);
        } else {
            airer_sterilizing.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_disinfect_unselect, 0, 0);
        }
        //烘干
        if (!StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState()) && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.ON)) {
            airer_heat_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_dry_select, 0, 0);
            //烘干 -1后关闭(当时并没有处理麦润的逻辑，导致countdownTime在refreshTime中的没有再次赋值)
            //烘干于关闭到再次打开由于handler还在倒计时导致风干的图标一直置灰
            //bug 10466烘干时，点击烘干键，关闭烘干后。即刻再次开启烘干，风干置灰，返回上级再次进入后显示才正常
            countdownTime = -1;
            isWindTimeCountDowning = false;//烘干的状态如果是开的，那么风干的也是开的，也就没有倒计时了
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN) || (clotheshorseType == ClotheshorseType.LIANG_BA)) {
                airer_wind_drying.setEnabled(true);
                WindDryingCache.setCloseHeatDryingTimeTag(mContext, deviceId, false);
            }
        } else {
            airer_heat_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_dry_unselect, 0, 0);
        }
        //风干
        if (!StringUtil.isEmpty(clotheShorseStatus.getWindDryingState()) && clotheShorseStatus.getWindDryingState().equals(ClotheShorseOrder.ON)) {
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN) || (clotheshorseType == ClotheshorseType.LIANG_BA)) {
                if (!isWindTimeCountDowning
                        || (!StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState()) && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.ON))) {
                    airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_select, 0, 0);
                    airer_wind_drying.setEnabled(true);
                }
            } else {
                airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_select, 0, 0);
            }
        } else {
            countdownTime = 0;
            cth_drying.setVisibility(View.GONE);
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN)) {
                airer_wind_drying.setEnabled(true);
            }
            airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_unselect, 0, 0);
        }
//        if (!StringUtil.isEmpty(clotheShorseStatus.getExceptionInfo()) && clotheShorseStatus.getExceptionInfo().equals(ClotheShorseOrder.OVER_WEIGHT)) {
//            Toast.makeText(this,getString(R.string.cth_over_weight_toast),Toast.LENGTH_LONG).show();
//        }
    }

    private void refreshTime() {
        //照明
        if (StringUtil.isEmpty(clotheShorseStatus.getLightingState()) || StringUtil.isEmpty(clotheShorseStatus.getLightingStateTime())) {
            cth_lighting.setVisibility(View.GONE);
        } else if (clotheShorseStatus.getLightingState().equals(ClotheShorseOrder.OFF)) {
            cth_lighting.setVisibility(View.GONE);
        } else {
            //照明的倒计时
            int lightingTime = clotheShorseCountdown.getLightingTime();
            //常亮
            if (lightingTime == 0) {
                cth_lighting.setVisibility(View.VISIBLE);
                cth_lighting.setText(getString(R.string.cth_lighting) + ": " + getString(R.string.cth_steady));
                if (clotheshorseType == ClotheshorseType.LIANG_BA) {
                    cth_lighting.setVisibility(View.GONE);
                }
            } else {
                // 如果手机上的时间没有和服务器同步，会出现负值
                //照明的触发的时间与当前时间的差值（差值如果大于倒计时说明倒计时已经结束）
                int utcDuration = TimeUtil.getUtcDurationByMin(clotheShorseStatus.getLightingStateTime());
                LogUtil.e(TAG, "refreshTime():utcDuration=" + utcDuration);

//                if (utcDuration == -1) {
//                    cth_lighting.setVisibility(View.GONE);
//                } else {
                //倒计时的剩余时间
                int leftTime = lightingTime - utcDuration;
                if (leftTime > 0) {
                    String time = getTime(TimeUtil.getTimeByMin(leftTime));
                    cth_lighting.setVisibility(View.VISIBLE);
                    if (isOverseasVersion) {
                        cth_lighting.setText(getString(R.string.cth_lighting) + ": " + getString(R.string.cth_close_later) + time);
                    } else {
                        cth_lighting.setText(getString(R.string.cth_lighting) + ": " + time + getString(R.string.cth_close_later));
                    }
                } else {
                    cth_lighting.setVisibility(View.GONE);
                }
//                }
            }
        }
        //消毒
        if (StringUtil.isEmpty(clotheShorseStatus.getSterilizingState()) || StringUtil.isEmpty(clotheShorseStatus.getSterilizingStateTime())) {
            cth_sterilizing.setVisibility(View.GONE);
        } else if (clotheShorseStatus.getSterilizingState().equals(ClotheShorseOrder.OFF)) {
            cth_sterilizing.setVisibility(View.GONE);
        } else {
            int sterilizingTime = clotheShorseCountdown.getSterilizingTime();
            int utcDuration = TimeUtil.getUtcDurationByMin(clotheShorseStatus.getSterilizingStateTime());
//            if (utcDuration == -1) {
//                cth_sterilizing.setVisibility(View.GONE);
//            } else {
            int leftTime = sterilizingTime - utcDuration;
            if (leftTime > 0) {
                String time = getTime(TimeUtil.getTimeByMin(leftTime));
                cth_sterilizing.setVisibility(View.VISIBLE);
                if (isOverseasVersion) {
                    cth_sterilizing.setText(getString(R.string.cth_sterilizing) + ": " + getString(R.string.cth_close_later) + time);
                } else {
                    cth_sterilizing.setText(getString(R.string.cth_sterilizing) + ": " + time + getString(R.string.cth_close_later));
                }
            } else {
                cth_sterilizing.setVisibility(View.GONE);
            }
//            }
        }
        //烘干和风干
        if (StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState()) || StringUtil.isEmpty(clotheShorseStatus.getHeatDryingStateTime())) {
            refreshWindDryingTime();
            //烘干关闭后风干的相应的处理
        } else if (clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.OFF)) {
            if (!isWindTimeCountDowning && !StringUtil.isEmpty(clotheShorseStatus.getWindDryingState()) &&
                    clotheShorseStatus.getWindDryingState().equals(DeviceOrder.ON) &&
                    TimeUtil.getUtcTimeByMillis(clotheShorseStatus.getHeatDryingStateTime()) > TimeUtil.getUtcTimeByMillis(clotheShorseStatus.getWindDryingStateTime())) {//唯一表示关烘干动作
                //倒计时40秒剩余时间
                // 这里会有一个问题，当倒计时在最后的几秒退出界面，再进入界面，（倒计时已经结束），windDryingleftTime会小于或等于“0”（等于0的原因是因为/1000），如果属性报告没有返回那么倒计时会重新开始
                int windDryingleftTime = getWindDryingleftTime();
                if (windDryingleftTime > 0) {
                    //继续风干的倒计时
                    countdownTime = windDryingleftTime;
                    cth_drying.setVisibility(View.VISIBLE);
                    airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_select_disable, 0, 0);
                    airer_wind_drying.setEnabled(false);
                } else {
                    //倒计时结束，再次回到控制界面，需要再次判断倒计时是否结束
//                    if (windDryingleftTime <= 0) {
//                        isWindTimeCountDowning = false;
//                        cth_drying.setVisibility(View.GONE);
//                        return;
//                    }

                    if (isCountdownFinish()) {
                        WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, false);
                        isWindTimeCountDowning = false;
                        airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_unselect, 0, 0);
                        cth_drying.setVisibility(View.GONE);
                        return;
                    }
                    //1当断开网络，倒计时退出控制界面，会重新计时(triggerWindTimeCountDown)，2当断开网络，定时器每一分钟会都会刷新调用refreshTime（）(isWindTimeCountFinish)
                    if (triggerWindTimeCountDown) {
                        if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN)) {
                            countdownTime = ZI_CHENG_COUNT_DOWN_TIME;
                        } else if (clotheshorseType == ClotheshorseType.LIANG_BA) {
                            countdownTime = LIANG_BA_COUNT_DOWN_TIME;
                        }
                    } else {
                        //再次进入界面，如果倒计时已经结束
                        countdownTime = -1;
                        airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_unselect, 0, 0);
                    }
                }
                cth_drying.setVisibility(View.VISIBLE);
                // (cth_drying用于显示风干和烘干，烘干打开时优先显示烘干)
                if (isOverseasVersion) {
                    cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + getString(R.string.cth_close_later) + countdownTime);
                } else {
                    cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + countdownTime + getString(R.string.cth_close_later));
                }
                //开始风干的倒计时
                mHandler.sendEmptyMessage(WHAT_REFRESH_WIND_TIME);
            } else if (!isWindTimeCountDowning) {
                refreshWindDryingTime();
            } else {//在倒计时过程中烘干关闭不处理
                LogUtil.d(TAG, "isWindTimeCountDowning = " + isWindTimeCountDowning);
            }
        } else {
            //烘干打开后
            int heatDryingTime = clotheShorseCountdown.getHeatDryingTime();
            //当前时间与触发时间之差
            int utcDuration = TimeUtil.getUtcDurationByMin(clotheShorseStatus.getHeatDryingStateTime());
//            if (utcDuration == -1) {
//                cth_drying.setVisibility(View.GONE);
//            } else {
            int leftTime = heatDryingTime - utcDuration;
            if (leftTime > 0) {
                String time = getTime(TimeUtil.getTimeByMin(leftTime));
                cth_drying.setVisibility(View.VISIBLE);
                if (isOverseasVersion) {
                    cth_drying.setText(getString(R.string.cth_heat_drying) + ": " + getString(R.string.cth_close_later) + time);
                } else {
                    cth_drying.setText(getString(R.string.cth_heat_drying) + ": " + time + getString(R.string.cth_close_later));
                }
            } else {
                refreshWindDryingTime();
            }
//            }
        }

//        DeviceStatus deviceStatus = new DeviceStatusDao().selDeviceStatus(uid, deviceId);
//        if (deviceStatus != null && deviceStatus.getOnline() == OnlineStatus.OFFLINE) {
//            cth_lighting.setVisibility(View.GONE);
//            cth_sterilizing.setVisibility(View.GONE);
//            cth_drying.setVisibility(View.GONE);
//        }
        boolean online = new DeviceStatusDao().isOnline(uid, deviceId);
        if (!online) {
            cth_lighting.setVisibility(View.GONE);
            cth_sterilizing.setVisibility(View.GONE);
            cth_drying.setVisibility(View.GONE);
        }
    }

    /**
     * 判断风干的倒计时是否结束
     *
     * @return
     */
    private boolean isCountdownFinish() {
        if (WindDryingCache.getCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId)) {
            long closeHeatDryingTime = WindDryingCache.getCloseHeatDryingTime(ClotheShorseActivity.this, deviceId);
            int countdownTime = 0;
            if ((clotheshorseType == ClotheshorseType.ZI_CHENG) || (clotheshorseType == ClotheshorseType.MAI_RUN)) {
                countdownTime = ZI_CHENG_COUNT_DOWN_TIME;
            } else if (clotheshorseType == ClotheshorseType.LIANG_BA) {
                countdownTime = LIANG_BA_COUNT_DOWN_TIME;
            }
            if ((closeHeatDryingTime + countdownTime * 1000) > System.currentTimeMillis()) {
                return false;
            }
        } else {
            return false;
        }
        return true;

    }

    private int getWindDryingleftTime() {
        long currentTime = System.currentTimeMillis();
        long windDryingTime = WindDryingCache.getWindDryingTime(mContext, deviceId);
        long closeHeatDryingTime = WindDryingCache.getCloseHeatDryingTime(ClotheShorseActivity.this, deviceId);
        int windDryingleftTime = (int) ((windDryingTime * 1000 + closeHeatDryingTime - currentTime) / 1000.0);
        LogUtil.d(TAG, "getWindDryingleftTime() - currentTime = " + currentTime
                + " closeHeatDryingTime = " + closeHeatDryingTime
                + " windDryingTime = " + windDryingTime
                + " windDryingleftTime = " + windDryingleftTime);
        return windDryingleftTime;
    }

    /**
     * 风干
     */
    private void refreshWindDryingTime() {
        if (StringUtil.isEmpty(clotheShorseStatus.getWindDryingState()) || StringUtil.isEmpty(clotheShorseStatus.getWindDryingStateTime())) {
            cth_drying.setVisibility(View.GONE);
        } else if (clotheShorseStatus.getWindDryingState().equals(ClotheShorseOrder.OFF)) {
            cth_drying.setVisibility(View.GONE);
        } else {
            //倒计时40秒剩余时间
            int windDryingleftTime = getWindDryingleftTime();
            if (windDryingleftTime > 0) {
                cth_drying.setVisibility(View.VISIBLE);
                if (isOverseasVersion) {
                    cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + getString(R.string.cth_close_later) + windDryingleftTime);
                } else {
                    cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + windDryingleftTime + getString(R.string.cth_close_later));
                }
                airer_wind_drying.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airer_icon_air_dry_select_disable, 0, 0);
                airer_wind_drying.setEnabled(false);
                countdownTime = windDryingleftTime;
                mHandler.sendEmptyMessage(WHAT_REFRESH_WIND_TIME);
            } else {
                int windDryingTime = clotheShorseCountdown.getWindDryingTime();
                int utcDuration = TimeUtil.getUtcDurationByMin(clotheShorseStatus.getWindDryingStateTime());
//                if (utcDuration == -1) {
//                    cth_drying.setVisibility(View.GONE);
//                } else {
                int leftTime = windDryingTime - utcDuration;
                if (leftTime > 0) {
                    String time = getTime(TimeUtil.getTimeByMin(leftTime));
                    cth_drying.setVisibility(View.VISIBLE);
                    if (isOverseasVersion) {
                        cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + getString(R.string.cth_close_later) + time);
                    } else {
                        cth_drying.setText(getString(R.string.cth_wind_drying) + ": " + time + getString(R.string.cth_close_later));
                    }
                } else {
                    cth_drying.setVisibility(View.GONE);
                }
//                }
            }
        }
    }

    /**
     * 时间组装
     *
     * @return
     */
    private String getTime(int time[]) {
        StringBuilder stringBuilder = new StringBuilder();
        String h = getString(R.string.time_hours);
        String m = getString(R.string.time_minutes);
        int hour = time[0];
        int minute = time[1];
        if (hour == 0) {
            stringBuilder.append(minute).append(m);
        } else if (minute == 0) {
            stringBuilder.append(hour).append(h);
        } else {
            stringBuilder.append(hour).append(h).append(",").append(minute).append(m);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtil.isFastDoubleClick()) {
            if (v.getId() != R.id.airer_wind_drying) {
                showDialog();
            }
            switch (v.getId()) {
                case R.id.airer_up:
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, ClotheShorseOrder.UP, null, null, null, null, null, null, this);
                    break;
                case R.id.airer_stop:
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, ClotheShorseOrder.STOP, null, null, null, null, null, null, this);
                    break;
                case R.id.airer_down:
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, ClotheShorseOrder.DOWN, null, null, null, null, null, null, this);
                    break;
                case R.id.airer_lighting:
                    if (clotheShorseStatus != null && !StringUtil.isEmpty(clotheShorseStatus.getLightingState())
                            && clotheShorseStatus.getLightingState().equals(ClotheShorseOrder.ON)) {
                        order = ClotheShorseOrder.OFF;
                    } else {
                        order = ClotheShorseOrder.ON;
                    }
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, null, order, null, null, null, null, null, this);
                    break;
                case R.id.airer_sterilizing:
                    if (clotheShorseStatus != null && !StringUtil.isEmpty(clotheShorseStatus.getSterilizingState())
                            && clotheShorseStatus.getSterilizingState().equals(ClotheShorseOrder.ON)) {
                        order = ClotheShorseOrder.OFF;
                    } else {
                        order = ClotheShorseOrder.ON;
                    }
                    //消毒 晾霸消毒需要晾衣架全部升到顶部才能打开
                    if (clotheshorseType == ClotheshorseType.LIANG_BA && clotheShorseStatus != null
                            && clotheShorseStatus.getMotorPosition() != 0) {
                        dismissDialog();
                        ToastUtil.showToast(R.string.sterilizing_error_tips);
                    } else {
                        DeviceApi.clotheShorseControl(userName, uid, deviceId, null, null, order, null, null, null, null, this);
                    }
                    break;
                case R.id.airer_heat_drying:
                    if (clotheShorseStatus != null && !StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState())
                            && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.ON)) {
                        order = ClotheShorseOrder.OFF;
                        triggerWindTimeCountDown = true;
                        WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, false);
                    } else {
                        order = ClotheShorseOrder.ON;
                    }
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, null, null, null, order, null, null, null, this);
                    break;
                case R.id.airer_wind_drying:
                    //奥科 烘干的时候，可以点击风干，15s后热机自动关闭。但是紫程和晾霸和烘干的时候不能点击风干关闭热机（切记）
                    if ((clotheshorseType == ClotheshorseType.ZI_CHENG || clotheshorseType == ClotheshorseType.MAI_RUN || clotheshorseType == ClotheshorseType.LIANG_BA) && clotheShorseStatus != null
                            && !StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState())
                            && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.ON)) {
//                        DeviceStatus deviceStatus = new DeviceStatusDao().selDeviceStatus(uid, deviceId);
//                        if (deviceStatus != null && deviceStatus.getOnline() == OnlineStatus.OFFLINE) {
//                            ToastUtil.showToast(R.string.offline);
//                        } else {
//                            ToastUtil.showToast(R.string.cth_heat_drying_toast);
//                        }
                        boolean online = new DeviceStatusDao().isOnline(uid, deviceId);
                        if (!online) {
                            ToastUtil.showToast(R.string.offline);
                        } else {
                            ToastUtil.showToast(R.string.cth_heat_drying_toast);
                        }
                    } else {
                        if (!StringUtil.isEmpty(clotheShorseStatus.getWindDryingState()) && clotheShorseStatus.getWindDryingState().equals(ClotheShorseOrder.ON)) {
                            order = ClotheShorseOrder.OFF;
                        } else {
                            order = ClotheShorseOrder.ON;
                        }
                        showDialog();
                        DeviceApi.clotheShorseControl(userName, uid, deviceId, null, null, null, null, order, null, null, this);
                    }
                    break;
                case R.id.airer_off:
                    DeviceApi.clotheShorseControl(userName, uid, deviceId, null, null, null, null, null, ClotheShorseOrder.OFF, null, this);
                    if (!StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState()) && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.ON)) {
                        WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, false);
                        triggerWindTimeCountDown = true;
                    }
                    break;
            }
        }
    }

    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent(this, ClotheShorseDeviceEditActivity.class);
        intent.putExtra(IntentKey.DEVICE, mDevice);
        intent.putExtra(IntentKey.CLOTHE_SHORSE_COUNTDOWN, clotheShorseCountdown);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (isFinishingOrDestroyed()) {
            return;
        }
        dismissDialog();
        int result = baseEvent.getResult();
        if (result != ErrorCode.SUCCESS) {
            ToastUtil.toastError(result);
            refreshTime();
        }
        if (baseEvent instanceof ClotheShorseAllStatusEvent) {
            List<ClotheShorseAllStatus> clotheShorseAllStatuses = ((ClotheShorseAllStatusEvent) baseEvent).getClotheShorseAllStatusList();
            if (clotheShorseAllStatuses != null && clotheShorseAllStatuses.size() > 0) {
                for (int i = 0; i < clotheShorseAllStatuses.size(); i++) {
                    ClotheShorseAllStatus clotheShorseAllStatus = clotheShorseAllStatuses.get(i);
                    if (!StringUtil.isEmpty(deviceId) && deviceId.equals(clotheShorseAllStatus.getDeviceId())) {
                        //clotheShorseStatus设置成最新的状态
                        int motorPosition = clotheShorseAllStatus.getMotorPosition();
                        String lightingState = clotheShorseAllStatus.getLightingState();
                        String sterilizingState = clotheShorseAllStatus.getSterilizingState();
                        String heatDryingState = clotheShorseAllStatus.getHeatDryingState();
                        String windDryingState = clotheShorseAllStatus.getWindDryingState();
                        String lightingStateTime = clotheShorseAllStatus.getLightingStateTime();
                        String sterilizingStateTime = clotheShorseAllStatus.getSterilizingStateTime();
                        String heatDryingStateTime = clotheShorseAllStatus.getHeatDryingStateTime();
                        String windDryingStateTime = clotheShorseAllStatus.getWindDryingStateTime();
                        clotheShorseStatus.setMotorPosition(motorPosition);
                        clotheShorseStatus.setLightingState(lightingState);
                        clotheShorseStatus.setSterilizingState(sterilizingState);
                        clotheShorseStatus.setHeatDryingState(heatDryingState);
                        clotheShorseStatus.setWindDryingState(windDryingState);
                        clotheShorseStatus.setLightingState(lightingState);
                        clotheShorseStatus.setLightingStateTime(lightingStateTime);
                        clotheShorseStatus.setSterilizingStateTime(sterilizingStateTime);
                        clotheShorseStatus.setHeatDryingStateTime(heatDryingStateTime);
                        clotheShorseStatus.setWindDryingStateTime(windDryingStateTime);
                        // clotheShorseCountdown设置成最新的倒计时信息
                        int lightingTime = clotheShorseAllStatus.getLightingTime();
                        int sterilizingTime = clotheShorseAllStatus.getSterilizingTime();
                        int heatDryingTime = clotheShorseAllStatus.getHeatDryingTime();
                        int windDryingTime = clotheShorseAllStatus.getWindDryingTime();
                        clotheShorseCountdown.setLightingTime(lightingTime);
                        clotheShorseCountdown.setSterilizingTime(sterilizingTime);
                        clotheShorseCountdown.setHeatDryingTime(heatDryingTime);
                        clotheShorseCountdown.setWindDryingTime(windDryingTime);

                        refreshMotorView();
                        refreshStatus();
                    }
                }
            }

        }
    }

    @Override
    public void onClotheShorseStatusReport(ClotheShorseStatus clotheShorseStatus) {
        LogUtil.d(TAG, "onClotheShorseStatusReport clotheShorseStatus = " + clotheShorseStatus);
        if (!isFinishingOrDestroyed() && (!StringUtil.isEmpty(deviceId) && deviceId.equals(clotheShorseStatus.getDeviceId()))) {
            this.clotheShorseStatus = clotheShorseStatus;
            dismissDialog();
            if (clotheShorseStatus != null) {
                refreshMotorView();
                refreshStatus();
                /**以前ParseTableData把异常信息屏蔽了,导致无法提示异常{@link ParseTableData#parseClotheShorseStatus（）}*/
                if (!StringUtil.isEmpty(clotheShorseStatus.getExceptionInfo()) && clotheShorseStatus.getExceptionInfo().equals(ClotheShorseOrder.OVER_WEIGHT)) {
                    ToastUtil.showToast(getString(R.string.cth_over_weight_toast));
                } else if (!StringUtil.isEmpty(clotheShorseStatus.getExceptionInfo()) && clotheShorseStatus.getExceptionInfo().equals(ClotheShorseOrder.HIT_OBSTACLE)) {
                    ToastUtil.showToast(getString(R.string.cth_hit_obstacle_toast));
                }
            }
            if (clotheShorseStatus != null || clotheShorseCountdown != null) {
                if (!StringUtil.isEmpty(clotheShorseStatus.getHeatDryingState()) && clotheShorseStatus.getHeatDryingState().equals(ClotheShorseOrder.OFF) && (!StringUtil.isEmpty(clotheShorseStatus.getWindDryingState()) && clotheShorseStatus.getWindDryingState().equals(ClotheShorseOrder.ON))) {
                    WindDryingCache.setCloseHeatDryingTimeTag(ClotheShorseActivity.this, deviceId, false);
                    triggerWindTimeCountDown = true;
                }
                refreshTime();
            }
            if (isFinishingOrDestroyed()) {
                return;
            }
        }
    }

    @Override
    public void onClotheShorseTimeReport(ClotheShorseCountdown clotheShorseCountdown) {
        if (!isFinishingOrDestroyed() && (!StringUtil.isEmpty(deviceId) || deviceId.equals(clotheShorseCountdown.getDeviceId()))) {
            LogUtil.d(TAG, "onClotheShorseTimeReport()-clotheShorseTime:" + clotheShorseCountdown);
            this.clotheShorseCountdown = clotheShorseCountdown;
            if (!StringUtil.isEmpty(deviceId) && clotheShorseCountdown != null && (clotheShorseStatus != null || clotheShorseCountdown != null)) {
                refreshTime();
            }
        }
    }

    @Override
    protected void onDestroy() {
        ClotheShorseStatusReport.getInstance(mAppContext).unregisterClotheShorseStatusReport(this);
        ClotheShorseTimeReport.getInstance(mAppContext).unregisterClotheShorseTimeReport(this);
        mTimer.cancel();
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_REFRESH_WIND_TIME);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1)
            finish();
    }
}
