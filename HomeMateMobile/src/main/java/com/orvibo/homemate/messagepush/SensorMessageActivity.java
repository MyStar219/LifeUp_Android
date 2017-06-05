package com.orvibo.homemate.messagepush;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Message;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.tencent.stat.StatService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by smagret on 2015/8/21.
 */
public class SensorMessageActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = SensorMessageActivity.class.getSimpleName();
    private ListView messagesListView;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private MessageDao messageDao;
    private String userId;
    private String deviceId;
    private View emptyView = null;
    private InfoPushManager infoPushManager;
    private Device device;
    private NavigationGreenBar navigationGreenBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_message_activity);

        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationGreenBar);
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(mContext);
//        refWatcher.watch(this);

        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            deviceId = device.getDeviceId();
        }
        userId = UserCache.getCurrentUserId(ViHomeApplication.getAppContext());

        infoPushManager = InfoPushManager.getInstance(mAppContext);
        messageDao = new MessageDao();
        emptyView = LayoutInflater.from(mContext).inflate(
                R.layout.empty_message_view, null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        init();
        infoPushManager.notShowNotificationDeviceId = deviceId;
    }

    private void init() {
        infoPushManager.cancelNotificationByDeviceId(userId, deviceId);
//        InfoPushCountCache.saveInfoPushCount(mContext,UserCache.getCurrentUserId(mContext), 0);
        messageDao.setReadByDeviceId(userId,deviceId);
    }

    private void refresh() {
        if (device != null) {
            messages = messageDao.selMessagesByUserIdAndDeviceId(userId, deviceId);
        }
        messageAdapter = new MessageAdapter(mContext, messages);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        messagesListView.setAdapter(messageAdapter);
        if (messages == null || messages.size() == 0) {
            ((ViewGroup) messagesListView.getParent()).removeView(emptyView);
            ((ViewGroup) messagesListView.getParent()).addView(emptyView);
            messagesListView.setEmptyView(emptyView);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        } else {
            navigationGreenBar.setRightTextVisibility(View.VISIBLE);
            if (emptyView != null) {
                ((ViewGroup) messagesListView.getParent()).removeView(emptyView);
            }
        }
        //如果所有消息界面是在前台，则取消所有定时消息推送通知，同时设置消息推送个数为0
        if (AppTool.isForeground(mContext,getPackageName() + "." + getLocalClassName())) {
            init();
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Back), null);
        super.onBackPressed();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    public void rightTitleClick(View v) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.message_clear_confirm));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.message_clear));
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    @Override
    protected void onPause() {
        super.onPause();
        infoPushManager.notShowNotificationDeviceId = "";
    }

    @Override
    protected void onDestroy() {
        infoPushManager.cancelNotificationByDeviceId(userId, deviceId);
        super.onDestroy();
    }

    @Override
    public void onLeftButtonClick(View view) {
        messageDao.delMessagesByUserIdAndDeviceId(userId,deviceId);
        refresh();
    }

    @Override
    public void onRightButtonClick(View view) {

    }
}
