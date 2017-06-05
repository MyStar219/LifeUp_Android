package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.TimerPush;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by smagret on 2015/5/28.
 */
public class DeviceTimingInfoPushSetActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener {
    private static final String TAG = DeviceTimingInfoPushSetActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private ImageView ctrl_iv;
    private MessagePushDao messagePushDao;
    private int isPush;
    private Device device;
    private TimerPush timerPush;
    private MessagePush messagePush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_timing_noti_activity);
        init();
        initData();
        initTimerPush();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        ctrl_iv = (ImageView)findViewById(R.id.ctrl_iv);
    }

    private void initData() {
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(Constant.DEVICE);
        if (device == null) {
            return;
        }
        messagePushDao = new MessagePushDao();
        refresh();
    }


    private void refresh() {
        messagePush = messagePushDao.selMessagePushByDeviceId(device.getDeviceId());
        MessagePush allMessagePush = messagePushDao.selAllSetMessagePushByType(UserCache.getCurrentUserId(mAppContext), MessagePushType.All_TIMER_TYPE);
        if (allMessagePush != null && allMessagePush.getIsPush() == MessagePushStatus.OFF){
            isPush = MessagePushStatus.OFF;
        } else if (messagePush == null) {
            isPush =  MessagePushStatus.ON;
        } else {
            isPush = messagePush.getIsPush();
        }
        if (isPush == MessagePushStatus.ON) {
            ctrl_iv.setImageLevel(1);
        } else  {
            ctrl_iv.setImageLevel(0);
        }

    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_AlertSettings_Back), null);
        super.onBackPressed();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    public void control(View view) {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_AlertSettings_Switch), null);
        if (isPush == 0) {
            timerPush.startSetDeviceTimerPush(device.getDeviceId(),1);
        } else if (isPush == 1) {
            timerPush.startSetDeviceTimerPush(device.getDeviceId(), 0);
        }
    }


    public void initTimerPush() {
        timerPush = new TimerPush(mContext) {
            @Override
            public void onTimerPushResult(int result) {
                LogUtil.d(TAG,"onTimerPushResult() - result：" + result);
                if (result == 0) {
                    refresh();
                }
            }

            @Override
            public void onAllSetTimerPushResult(int result) {

            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
