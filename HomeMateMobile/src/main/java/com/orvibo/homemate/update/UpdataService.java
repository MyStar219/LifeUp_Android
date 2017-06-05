package com.orvibo.homemate.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;

import java.io.File;
import java.util.HashMap;

/**
 * Created by baoqi on 2016/6/12.
 */
public class UpdataService extends Service implements OrviboUpdateAgent.OrviboUpdateDownloadListener {
    private static final String TAG = UpdataService.class.getSimpleName();
    private OrviboUpdateAgent mOrviboUpdateAgent;
    private NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private UpdataService instance;
    //   private UpdataBinder mUpdataBinder = new UpdataBinder();
    private InstallBroadcastReceiver mInstallBroadcastReceiver;
    private Handler mHandler;
    private HashMap<String, String> mUpdateInfos;
    private Notification mNotification;
    private static final String PAUSE_ACTION = "pause";
    private static final String CANCEL_ACTION = "cancel";
    private NetChangeBroadcastReceiver mNetChangeBroadcastReceiver;
    private ConnectivityManager mMConnectivityManager;
    private boolean isFirstNetChange = true;
    private int id = 1024;
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mUpdataBinder;
//    }
//
//    private class UpdataBinder extends Binder {
//        public UpdataService getUpdataService() {
//            return instance;
//        }
//    }

    @Override
    public void onCreate() {

        LogUtil.e(TAG, "======onCreate====");
        super.onCreate();
        instance = this;
        init();
        //  initNotification();
        registerInstallBoradcastRecevice();
        registerNetChangeBoradcastRecevice();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //   showNotification(0, 0, getString(R.string.orvibo_update), getString(R.string.orvibo_update_notification_pause), getString(R.string.orvibo_update_notification_cancel));
    }

    private void registerInstallBoradcastRecevice() {
        mInstallBroadcastReceiver = new InstallBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdataService.PAUSE_ACTION);
        intentFilter.addAction(UpdataService.CANCEL_ACTION);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.registerReceiver(mInstallBroadcastReceiver, intentFilter);
    }

    private void registerNetChangeBoradcastRecevice() {
        mNetChangeBroadcastReceiver = new NetChangeBroadcastReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetChangeBroadcastReceiver, mFilter);
    }

    private void init() {
        mOrviboUpdateAgent = OrviboUpdateAgent.getInstance(this);
        mHandler = mOrviboUpdateAgent.getHandler();
        mOrviboUpdateAgent.setOrviboUpdateDownloadListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        在运行onStartCommand后service进程被kill后，系统将会再次启动service，并传入最后一个intent给onstartCommand。直到调用stopSelf(int)才停止传递intent。如果在被kill后还有未处理好的intent，那被kill后服务还是会自动启动。因此onstartCommand不会接收到任何null的intent。
        LogUtil.e(TAG, "======onStartCommand====" + " intent=" + intent);
        if (intent != null) {
            mUpdateInfos = (HashMap<String, String>) intent.getSerializableExtra(IntentKey.UPDATEINFOS);


            File apkFile = mOrviboUpdateAgent.downloadedFile(mUpdateInfos);
            LogUtil.d(TAG, "onStartCommand()-mUpdateInfos=" + mUpdateInfos);
            //下载的apk文件存在，apk文件下载完成，
            //如果上一次下载的没有安装，finish.txt 文件就不会删除，但是apk文件已经删除(点击下载的时候已经生成，但是文件里的内容是空的)，这个时候已经满足安装条件，所以会报文件无法解析
            if (apkFile != null && apkFile.exists() && apkFile.getName().startsWith(mUpdateInfos.get(UpdateInfo.VERSION) + "_") && AppDownloadCache.getFinish(this)) {
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            } else {
                LogUtil.e(TAG, "UpdataService :onStartCommand()");
                // mOrviboUpdateAgent.startDownload(mUpdateInfos, 3);
                mOrviboUpdateAgent.startDownloadByNOHttp(mUpdateInfos);
            }

            //  mOrviboUpdateAgent.startDownload(mUpdateInfos, 3);
        }
        return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {
        LogUtil.e(TAG, "======onDestroy====");
        unregisterReceiver(mInstallBroadcastReceiver);
        unregisterReceiver(mNetChangeBroadcastReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLoadFinsh() {
        //下载结束后取消通知
        if (mUpdateInfos != null && mUpdateInfos.size() > 0) {
            mNotificationManager.cancel(R.id.progressBar);
            mOrviboUpdateAgent.startInstall(this, mOrviboUpdateAgent.downloadedFile(mUpdateInfos));
        }

    }

    @Override
    public void onPause(int progress, int contentLength) {
        //   showNotification(progress, contentLength, getString(R.string.orvibo_update_title_pause), getString(R.string.orvibo_update_notification_goon), getString(R.string.orvibo_update_notification_cancel));
        showNotification(getString(R.string.orvibo_update_fail));
    }

    @Override
    public void onCancel() {
        mNotificationManager.cancel(R.id.progressBar);
    }

    @Override
    public void onProgress(int progress, int contentLength) {
        //2147483647
        showNotification(progress, contentLength, getString(R.string.orvibo_update), getString(R.string.orvibo_update_notification_pause), getString(R.string.orvibo_update_notification_cancel));
    }

    @Override
    public void onFail() {
        showNotification(getString(R.string.orvibo_update_fail));
    }


    /**
     * 更新进度的通知
     *
     * @param progress      进度
     * @param contentLength 最大值
     */
    private void showNotification(int progress, int contentLength, String title, String pause, String cancel) {

        //  mNotificationManager.cancel(R.id.progressBar);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setTicker(title);
        mBuilder.setContentTitle(getString(R.string.app_name) + title);
        mBuilder.setSmallIcon(R.mipmap.ic_sg_launcher);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        //每次发送通知的时候都要重新创建该对象,这个远程view里面的action是累加的，每执行一次都要把每个action执行一下
        //对于这种不断更新进度的action，最好重新创建一个
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_update);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContent(mRemoteViews);

        mBuilder.setContentIntent(pendingIntent);
        if (mNotification == null) {
            mNotification = mBuilder.build();
        }//下载过程中不要反复创建
//        // String percent = ((int) (((progress * 100) / contentLength) )) + "%";//下载过程中出现负数：(progress * 100)乘以100后值超过Int型的最大值
        String percent = ((int) (((progress + 0.0f) / contentLength) * 100)) + "%";
        LogUtil.e(TAG, "progress=" + progress + ",contentLength=" + contentLength + ",percent=" + percent);
        mRemoteViews.setTextViewText(R.id.progress, percent);
        mRemoteViews.setProgressBar(R.id.progressBar, contentLength, progress, false);
        //这个图标在有些机型上没有显示出来
        //  mRemoteViews.setImageViewResource(R.id.title_icon, R.drawable.app_logo);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_sg_launcher);//用这个不会
        mRemoteViews.setImageViewBitmap(R.id.title_icon, bitmap);
        mRemoteViews.setTextViewText(R.id.title, getString(R.string.app_name) + title);
//暂停
        Intent BroadcastPauseIntent = new Intent(UpdataService.PAUSE_ACTION);
        PendingIntent BroadcastPausePendingIntent = PendingIntent.getBroadcast(this, 0, BroadcastPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.pause, BroadcastPausePendingIntent);
        mRemoteViews.setTextViewText(R.id.pause, pause);

//取消
        Intent BroadcastCancelIntent = new Intent(UpdataService.CANCEL_ACTION);
        PendingIntent BroadcastCancelPendingIntent = PendingIntent.getBroadcast(this, 0, BroadcastCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.cancel, BroadcastCancelPendingIntent);
        mRemoteViews.setTextViewText(R.id.cancel, cancel);
        //   mRemoteViews.setTextColor(R.id.title, getResources().getColor(R.color.black));//已经在布局中设置
        if (mNotification != null) {
            //因为RemoteView每次都会重新创建
            mNotification.contentView = mRemoteViews;
        }
        mNotificationManager.notify(R.id.progressBar, mNotification);

    }

    private void show(int progress, int contentLength, String title, String pause, String cancel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_update);
        } else {
            mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_update_low);
        }
        String percent = ((int) (((progress + 0.0f) / contentLength) * 100)) + "%";
        LogUtil.e(TAG, "progress=" + progress + ",contentLength=" + contentLength + ",percent=" + percent);
        mRemoteViews.setTextViewText(R.id.progress, percent);
        mRemoteViews.setProgressBar(R.id.progressBar, contentLength, progress, false);
        //这个图标在有些机型上没有显示出来
        //  mRemoteViews.setImageViewResource(R.id.title_icon, R.drawable.app_logo);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.app_logo);//用这个不会
        mRemoteViews.setImageViewBitmap(R.id.title_icon, bitmap);
        mRemoteViews.setTextViewText(R.id.title, getString(R.string.app_name) + title);
        mRemoteViews.setTextViewText(R.id.pause, pause);
        mRemoteViews.setTextViewText(R.id.cancel, cancel);
//        if (mNotification != null) {
//            //因为RemoteView每次都会重新创建
//            int version = Build.VERSION.SDK_INT;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                mNotification.bigContentView = mRemoteViews;
//            } else {
//                mNotification.contentView = mRemoteViews;
//            }
//
//        }
        mNotificationManager.notify(R.id.progressBar, mNotification);
    }

    /**
     * 更新失败的通知
     *
     * @param title
     */
    private void showNotification(String title) {

        Notification notification = null;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setTicker(title);
        mBuilder.setContentTitle(getString(R.string.app_name));
        mBuilder.setContentText(title);
        mBuilder.setSmallIcon(R.mipmap.ic_sg_launcher);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_sg_launcher);//用这个不会
        mBuilder.setLargeIcon(bitmap);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(pendingIntent);
        if (PhoneUtil.getAndroidSdk(this) >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            notification = mBuilder.build();
        } else {
            mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            notification = mBuilder.build();
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(R.id.progressBar, notification);

    }


    /**
     * 监听apk安装（没用，安装之前我们的应用的进程已经销毁了）
     */
    private class InstallBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
//
                if (UpdataService.CANCEL_ACTION.equals(intent.getAction())) {
                    //TODO
                    mOrviboUpdateAgent.setCurrenState(OrviboUpdateAgent.STATE_CANCEL);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(OrviboUpdateAgent.WHAT_CANCEL);
                    }
                } else if (UpdataService.PAUSE_ACTION.equals(intent.getAction())) {
                    //TODO
                    int currenState = mOrviboUpdateAgent.getCurrenState();
                    switch (currenState) {
                        case OrviboUpdateAgent.STATE_PAUSE:
                            mOrviboUpdateAgent.setCurrenState(OrviboUpdateAgent.STATE_DOWNLOAD_START);
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(OrviboUpdateAgent.WHAT_START);
                            }
                            break;
                        case OrviboUpdateAgent.STATE_DOWNLOADING:
                            mOrviboUpdateAgent.setCurrenState(OrviboUpdateAgent.STATE_PAUSE);
                            if (mHandler != null) {
                                mHandler.sendEmptyMessageDelayed(OrviboUpdateAgent.WHAT_PAUSE, 2000);
                            }
                            break;
                    }


                }
            }

        }
    }

    /**
     * wifi 断开重连后继续下载
     */
    private class NetChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFirstNetChange) {
                //一注册时监听到的广播不需要处理
                isFirstNetChange = false;
            } else {
                String action = intent.getAction();
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                    mMConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = mMConnectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
                        String name = activeNetworkInfo.getTypeName();

                        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            LogUtil.d(TAG, "NetChangeBroadcastReceiver--onReceive():netTypeName=" + name);
                            File apkFile = mOrviboUpdateAgent.downloadedFile(mUpdateInfos);

                            //下载的apk文件存在，apk文件下载完成，
                            //如果上一次下载的没有安装，finish.txt 文件就不会删除，但是apk文件已经删除(点击下载的时候已经生成，但是文件里的内容是空的)，这个时候已经满足安装条件，所以会报文件无法解析
                            if (apkFile != null && apkFile.exists() && apkFile.getName().startsWith(mUpdateInfos.get(UpdateInfo.VERSION) + "_") && AppDownloadCache.getFinish(UpdataService.this)) {
                                stopSelf();
                            } else {
                                LogUtil.e(TAG, "NetChangeBroadcastReceiver:onReceive()");
                                mOrviboUpdateAgent.startDownloadByNOHttp(mUpdateInfos);
                            }
                        } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            LogUtil.d(TAG, "NetChangeBroadcastReceiver--onReceive():netTypeName=" + name);
                        }
                    } else {
                        mOrviboUpdateAgent.setCurrenState(OrviboUpdateAgent.STATE_PAUSE);
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(OrviboUpdateAgent.WHAT_PAUSE, 2000);
                        }

                    }
                }

            }
        }

    }
}

