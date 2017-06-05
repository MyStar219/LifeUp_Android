//package com.orvibo.homemate.common.service;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.orvibo.homemate.api.listener.OnInfoPushListener;
//import com.orvibo.homemate.bo.InfoPushMsg;
//import com.orvibo.homemate.event.PullEvent;
//import com.orvibo.homemate.messagepush.InfoPushManager;
//import com.orvibo.homemate.model.InfoPush;
//import com.orvibo.homemate.util.AppTool;
//import com.orvibo.homemate.util.LogUtil;
//
//import de.greenrobot.event.EventBus;
//
///**
// * 消息推送
// * Created by zhaoxiaowei
// */
//public class InfoPushService extends Service implements OnInfoPushListener {
//    public static final int ACTION_GET_ENTER_TYPE = 1;
//    private static final String TAG = InfoPushService.class.getSimpleName();
//    private Context mContext;
//    private InfoPushManager infoPushManager;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        LogUtil.d(TAG, "onCreate()");
//        mContext = this;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        LogUtil.d(TAG, "onStartCommand()-intent:" + intent);
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }
//
//        if (intent != null) {
//
//        }
//        InfoPush.getInstance(this).registerInfoPush(this);
//        infoPushManager = InfoPushManager.getInstance(this);
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void OnInfoPush(InfoPushMsg infoPushMsg) {
//        LogUtil.d(TAG, "OnInfoPush() - infoPushMsg = " + infoPushMsg);
//        infoPushManager.pushInfo(infoPushMsg);
//    }
//
//    public final void onEventMainThread(PullEvent event) {
//        LogUtil.d(TAG, "onEventMainThread()-PullEvent:" + event);
//        if (event.getType() == PullEvent.TYPE_LOGIN) {
//            InfoPushManager.getInstance(mContext).setLogined(event.isLogined());
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        if (AppTool.isStopService(this)) {
//            LogUtil.e(TAG, "onDestroy()-InfoPushService has been stoped.");
//            if (EventBus.getDefault().isRegistered(this)) {
//                EventBus.getDefault().unregister(this);
//            }
//        } else {
//            LogUtil.e(TAG, "onDestroy()-Restart InfoPushService.");
//            startService(new Intent(this, InfoPushService.class));
//        }
//        super.onDestroy();
//    }
//}
