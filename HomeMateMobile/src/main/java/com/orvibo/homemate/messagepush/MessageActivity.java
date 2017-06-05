package com.orvibo.homemate.messagepush;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Message;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by smagret on 2015/8/21.
 */
public class MessageActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = MessageActivity.class.getSimpleName();
    private ListView messagesListView;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private MessageDao messageDao;
    private String userId;
    private View emptyView = null;
    private InfoPushManager infoPushManager;
    private NavigationGreenBar navigationGreenBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);

        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationGreenBar);
//        RefWatcher refWatcher = ViHomeProApp.getRefWatcher(mContext);
//        refWatcher.watch(this);
        infoPushManager = InfoPushManager.getInstance(mAppContext);
        emptyView = LayoutInflater.from(mContext).inflate(
                R.layout.empty_message_view, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        init();
        infoPushManager.notShowNotification = true;
    }

    private void init() {
        infoPushManager.cancelAllNotification(userId);
//        InfoPushCountCache.saveInfoPushCount(mContext,UserCache.getCurrentUserId(mContext), 0);
        messageDao.setRead(UserCache.getCurrentUserId(ViHomeApplication.getAppContext()));
    }

    private void refresh() {
        userId = UserCache.getCurrentUserId(mAppContext);
        LogUtil.d(TAG, "userId:"+userId);
        messageDao = new MessageDao();
        messages = messageDao.selMessagesByUserId(userId);
        LogUtil.d(TAG, "messages:"+messages);
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
        /**
         判断MainActivity是否在后台运行，如果是在后台运行不做处理，如果不在后台启动MainActivity
         */
        if(isActivityRunning(MessageActivity.this,Constant.ACTIVITY_NAME_MAIN)){
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        refresh();
    }

    public void rightTitleClick(View v) {
//        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Settings), null);
//        Intent intent = new Intent(mContext, MessageSettingActivity.class);
//        startActivity(intent);
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
        infoPushManager.notShowNotification = false;
    }

    @Override
    protected void onDestroy() {
        infoPushManager.cancelAllNotification(userId);
        super.onDestroy();
    }

    @Override
    public void onLeftButtonClick(View view) {
        new MessageDao().delMessagesByUserId(UserCache.getCurrentUserId(mAppContext));
        finish();
    }

    @Override
    public void onRightButtonClick(View view) {

    }

    public static boolean isActivityRunning(Context mContext, String activityClassName){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if(info != null && info.size() > 0){

            Iterator<ActivityManager.RunningTaskInfo> iterator = info.iterator();
            while (iterator.hasNext()){
                ActivityManager.RunningTaskInfo task=iterator.next();
                if(task.baseActivity.getClassName().equals(activityClassName)){
                    return true;
                }
            }
        }
        return false;
    }


}
