package com.orvibo.homemate.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadQueue;
import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.error.ArgumentError;
import com.yolanda.nohttp.error.NetworkError;
import com.yolanda.nohttp.error.ServerError;
import com.yolanda.nohttp.error.StorageReadWriteError;
import com.yolanda.nohttp.error.StorageSpaceNotEnoughError;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.error.URLError;
import com.yolanda.nohttp.error.UnKnownHostError;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by baoqi on 2016/7/8.
 */
public class NoHttpDownloadRequest {
    private static final String TAG = NoHttpDownloadRequest.class.getSimpleName();

    /**
     * 下载请求.
     */
    private DownloadRequest mDownloadRequest;
    private DownloadQueue downloadQueue;
    private Context mContext;
    private Handler mHandler;
    private RemoteViews mRemoteViews;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private HashMap<String, String> mUpdateInfos;

    public NoHttpDownloadRequest(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * 开始下载。
     */
    public void download(HashMap<String, String> updateInfos) {
        this.mUpdateInfos = updateInfos;
        // 开始下载了，但是任务没有完成，代表正在下载，那么暂停下载。
        if (mDownloadRequest != null && mDownloadRequest.isStarted() && !mDownloadRequest.isFinished()) {
            // 暂停下载。
            // mDownloadRequest.cancel();
            ToastUtil.showToast(mContext.getString(R.string.orvibo_update_downloading));
            LogUtil.e(TAG, "download()-已经在下载");
        } else if (mDownloadRequest == null || mDownloadRequest.isFinished()) {// 没有开始或者下载完成了，就重新下载。
            LogUtil.e(TAG, "download()-开始下载");
            /**
             * 这里不传文件名称、不断点续传，则会从响应头中读取文件名自动命名，如果响应头中没有则会从url中截取。
             */
            // url 下载地址。
            // fileFolder 文件保存的文件夹。
            // isDeleteOld 发现文件已经存在是否要删除重新下载。
//            mDownloadRequest = NoHttp.createDownloadRequest(Constants.URL_DOWNLOADS[0], AppConfig.getInstance().APP_PATH_ROOT, true);

            /**
             * 如果使用断点续传的话，一定要指定文件名喔。
             */
            // url 下载地址。
            // fileFolder 保存的文件夹。
            // fileName 文件名。
            // isRange 是否断点续传下载。
            // isDeleteOld 如果发现存在同名文件，是否删除后重新下载，如果不删除，则直接下载成功。

            mDownloadRequest = NoHttp.createDownloadRequest(updateInfos.get(UpdateInfo.URL), OrviboUpdateAgent.getSavePath(), OrviboUpdateAgent.getFileName(updateInfos.get(UpdateInfo.URL), Integer.parseInt(updateInfos.get(UpdateInfo.VERSION))), true, false);

            // what 区分下载。
            // downloadRequest 下载请求对象。
            // downloadListener 下载监听。
            //        CallServer.getDownloadInstance().add(0, mDownloadRequest, downloadListener);

            // 添加到队列，在没响应的时候让按钮不可用。
            //       mBtnStart.setEnabled(false);

            getDownloadInstance();
            downloadQueue.add(0, mDownloadRequest, downloadListener);
        }
    }

    /**
     * 下载队列.
     */
    public DownloadQueue getDownloadInstance() {
        if (downloadQueue == null)
            downloadQueue = NoHttp.newDownloadQueue(3);
        return downloadQueue;
    }


    /**
     * 下载监听
     */
    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onStart(int what, boolean isResume, long beforeLength, Headers headers, long allCount) {
            LogUtil.d(TAG, "onStart()-what:" + what + ",isResume:" + isResume + ",beforeLength:" + beforeLength + ",allCount:" + allCount + ",headers:" + headers);

            AppDownloadCache.saveFinish(mContext, false);
        }

        @Override
        public void onDownloadError(int what, Exception exception) {
            try {
                String message = mContext.getString(R.string.download_error);
                String messageContent;
                if (exception instanceof ServerError) {
                    messageContent = mContext.getString(R.string.download_error_server);
                } else if (exception instanceof NetworkError) {
                    messageContent = mContext.getString(R.string.download_error_network);
                } else if (exception instanceof StorageReadWriteError) {
                    messageContent = mContext.getString(R.string.download_error_storage);
                } else if (exception instanceof StorageSpaceNotEnoughError) {
                    messageContent = mContext.getString(R.string.download_error_space);
                } else if (exception instanceof TimeoutError) {
                    messageContent = mContext.getString(R.string.download_error_timeout);
                } else if (exception instanceof UnKnownHostError) {
                    messageContent = mContext.getString(R.string.download_error_un_know_host);
                } else if (exception instanceof URLError) {
                    messageContent = mContext.getString(R.string.download_error_url);
                } else if (exception instanceof ArgumentError) {
                    messageContent = mContext.getString(R.string.download_error_argument);
                } else {
                    messageContent = mContext.getString(R.string.download_error_un);
                }
                message = String.format(Locale.getDefault(), message, messageContent);
                LogUtil.e(TAG, "onDownloadError()-what:" + what + ",message:" + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showNotification(mContext.getString(R.string.orvibo_update_fail));
        }

        @Override
        public void onProgress(int what, int progress, long fileCount) {
            LogUtil.d(TAG, "onProgress()-what:" + what + ",progress:" + progress + ",fileCount:" + fileCount);
            showNotification(progress, mContext.getString(R.string.orvibo_update), mContext.getString(R.string.orvibo_update_notification_pause),
                    mContext.getString(R.string.orvibo_update_notification_cancel));
        }

        @Override
        public void onFinish(int what, String filePath) {
            LogUtil.i(TAG, "onFinish()-what:" + what + ",filePath:" + filePath);
            //下载结束后取消通知
            if (mUpdateInfos != null && mUpdateInfos.size() > 0) {
                mNotificationManager.cancel(R.id.progressBar);
                Intent intent = new Intent(mContext, UpdataService.class);
                mContext.stopService(intent);
                OrviboUpdateAgent.getInstance(mContext).startInstall(mContext, OrviboUpdateAgent.getInstance(mContext).downloadedFile(mUpdateInfos));
            } else {
                LogUtil.e(TAG, "onFinish()-mUpdateInfos is empty;" + mUpdateInfos);
            }
            AppDownloadCache.saveFinish(mContext, true);
        }

        @Override
        public void onCancel(int what) {
            LogUtil.e(TAG, "onCancel()-what:" + what);
        }


    };

    /**
     * 更新进度的通知
     *
     * @param progress 进度。
     */
    private void showNotification(int progress, String title, String pause, String cancel) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setTicker(title);
        mBuilder.setContentTitle(mContext.getString(R.string.app_name) + title);
        mBuilder.setSmallIcon(R.mipmap.ic_sg_launcher);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        //每次发送通知的时候都要重新创建该对象,这个远程view里面的action是累加的，每执行一次都要把每个action执行一下
        //对于这种不断更新进度的action，最好重新创建一个
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_update);
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContent(mRemoteViews);

        mBuilder.setContentIntent(pendingIntent);
        if (mNotification == null) {
            mNotification = mBuilder.build();
        }
        String percent = progress + "%";
        LogUtil.i(TAG, "showNotification()-progress=" + percent);
        mRemoteViews.setTextViewText(R.id.progress, percent);
        //100下载完成后进度为100，progress为当前实时进度。false表示进度已经确定http://www.cnblogs.com/over140/archive/2011/11/28/2265736.html
        mRemoteViews.setProgressBar(R.id.progressBar, 100, progress, false);
        //这个图标在有些机型上没有显示出来
        //  mRemoteViews.setImageViewResource(R.id.title_icon, R.drawable.app_logo);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getApplicationContext().getResources(), R.mipmap.ic_sg_launcher);//用这个不会
        mRemoteViews.setImageViewBitmap(R.id.title_icon, bitmap);
        mRemoteViews.setTextViewText(R.id.title,mContext. getString(R.string.app_name) + title);
////暂停
//        Intent BroadcastPauseIntent = new Intent(UpdataService.PAUSE_ACTION);
//        PendingIntent BroadcastPausePendingIntent = PendingIntent.getBroadcast(this, 0, BroadcastPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.pause, BroadcastPausePendingIntent);
//        mRemoteViews.setTextViewText(R.id.pause, pause);
//
////取消
//        Intent BroadcastCancelIntent = new Intent(UpdataService.CANCEL_ACTION);
//        PendingIntent BroadcastCancelPendingIntent = PendingIntent.getBroadcast(this, 0, BroadcastCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.cancel, BroadcastCancelPendingIntent);
//        mRemoteViews.setTextViewText(R.id.cancel, cancel);
        //   mRemoteViews.setTextColor(R.id.title, getResources().getColor(R.color.black));//已经在布局中设置
        if (mNotification != null) {
            //因为RemoteView每次都会重新创建
            mNotification.contentView = mRemoteViews;
        }
        mNotificationManager.notify(R.id.progressBar, mNotification);

    }


    /**
     * 更新失败的通知
     *
     * @param title
     */
    private void showNotification(String title) {

        Notification notification = null;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setTicker(title);
        mBuilder.setContentTitle(mContext.getString(R.string.app_name));
        mBuilder.setContentText(title);
        mBuilder.setSmallIcon(R.mipmap.ic_sg_launcher);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getApplicationContext().getResources(), R.mipmap.ic_sg_launcher);//用这个不会
        mBuilder.setLargeIcon(bitmap);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(pendingIntent);
        if (PhoneUtil.getAndroidSdk(mContext) >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            notification = mBuilder.build();
        } else {
            mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            notification = mBuilder.build();
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(R.id.progressBar, notification);

    }


}
