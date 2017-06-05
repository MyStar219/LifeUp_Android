package com.orvibo.homemate.update;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.UpdateCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.util.HashMap;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    private DownloadManager downloadManager;
    private DownloadAgent mDownloadAgent;
    private long mTaskId;
    private HashMap<String, String> mUpdateInfos;
    private DownloadBroadcastReceiver mDownloadBroadcastReceiver;
    private Cursor mCursor;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "=======onCreate()=======");
        mDownloadAgent = DownloadAgent.getIstance(this);
        downloadManager = mDownloadAgent.getDownloadManager();
        registerDownloadReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "=======onStartCommand()=======");
        try {
            if (intent != null) {
                mUpdateInfos = (HashMap<String, String>) intent.getSerializableExtra(IntentKey.UPDATEINFOS);
                if (mUpdateInfos != null && !mUpdateInfos.isEmpty()) {
                    mDownloadAgent.setCurrentState(DownloadAgent.STATE_UNDOWNLOAD);
                    mTaskId = mDownloadAgent.downloadAPK(mUpdateInfos.get(UpdateInfo.URL), mUpdateInfos);
                    LogUtil.e(TAG, "onStartCommand()---TaskId+=" + mTaskId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerDownloadReceiver() {
        mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mDownloadAgent.registerDownloadReceiver(this, mDownloadBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "=======onDestroy()=======");
        mDownloadAgent.setCurrentState(DownloadAgent.STATE_DOWNLOADED);
        mDownloadAgent.unRegisterDownloadReceiver(this, mDownloadBroadcastReceiver);
        mCursor.close();
        super.onDestroy();
    }

    /**
     * 监听下载的广播
     */
    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();
        }
    }

    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        mCursor = downloadManager.query(query);
        if (mCursor.moveToFirst()) {
            int status = mCursor.getInt(mCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    mDownloadAgent.setCurrentState(DownloadAgent.STATE_PAUSE);
                    LogUtil.d(TAG, "checkDownloadStatus()--下载暂停");
                case DownloadManager.STATUS_PENDING:
                    LogUtil.d(TAG, "checkDownloadStatus()--下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    mDownloadAgent.setCurrentState(DownloadAgent.STATE_DOWNLOADING);
                    LogUtil.d(TAG, "checkDownloadStatus()--正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    mDownloadAgent.setCurrentState(DownloadAgent.STATE_DOWNLOADED);
                    LogUtil.d(TAG, "checkDownloadStatus()--下载完成");

                    //下载完成安装APK
                    if (mUpdateInfos != null && mUpdateInfos.size() > 0) {
                        mDownloadAgent.installApk(mDownloadAgent.getDownloadApkPath(mUpdateInfos));
                        UpdateCache.setBoolean(DownloadService.this, true);
                        DownloadService.this.stopSelf();
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    mDownloadAgent.setCurrentState(DownloadAgent.STATE_DOWNLOAD_FAIL);
                    LogUtil.d(TAG, "checkDownloadStatus()--下载失败");
                    ToastUtil.showToast(getString(R.string.orvibo_update_fail));
                    break;
            }
        }
    }
}
