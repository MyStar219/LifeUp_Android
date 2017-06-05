package com.orvibo.homemate.user;

import android.app.Notification;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.DBHelper;
import com.orvibo.homemate.data.DeviceDescDBHelper;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.logcat.FileTool;
import com.orvibo.homemate.sharedPreferences.FindNewVersion;
import com.orvibo.homemate.sharedPreferences.ServerVersion;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.update.OrviboUpdateAgent;
import com.orvibo.homemate.update.UpdateInfo;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateResponse;

import java.util.HashMap;

/**
 * Created by Allen on 2015/5/28.
 */
public class AboutActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener,
        View.OnClickListener,
        DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = AboutActivity.class.getSimpleName();
    private NavigationCocoBar navigationBar;
    private TextView versionTextView, updateHasTextView;
    private LinearLayout contactLinearLayout, updateLinearLayout;
    private HashMap<String, String> mHashMap;
    // 通知栏
    private Notification updateNotification;
    private UpdateResponse response;
    private HashMap<String, String> updateInfos;
    private OrviboUpdateAgent mOrviboUpdateAgent;
    private OrviboCheckUpdateListener mOrviboCheckUpdateListener;
//    private DownloadAgent mDownloadAgent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
        initOrviboUpdate();
        ImageView logo_iv = (ImageView) findViewById(R.id.logo_iv);
        logo_iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Conf.DEBUG_DB) {
                    FileTool.copyDB2SDCard(DBHelper.DATABASE_NAME);
                    FileTool.copyDB2SDCard(DeviceDescDBHelper.DB_NAME);

                    UserCache.logAllCache(mAppContext);
                }
                return false;
            }
        });
        long curT = System.currentTimeMillis() / 1000;
        int t1 = DateUtil.getZoneOffset();
        int t2 = DateUtil.getDstOffset();
        LogUtil.d(TAG, "onCreate()-t1:" + t1 + ",t2:" + t2 + ",curT:" + curT);
    }

    private void initOrviboUpdate() {
//        mDownloadAgent = DownloadAgent.getIstance(this);
        mOrviboUpdateAgent = OrviboUpdateAgent.getInstance(mAppContext);
        mOrviboCheckUpdateListener = new OrviboCheckUpdateListener();
        mOrviboUpdateAgent.setOrviboCheckUpdateListener(mOrviboCheckUpdateListener);
        //网络可用时调用
        if (NetUtil.isNetworkEnable(mAppContext)) {
            mOrviboUpdateAgent.update();
        }
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        versionTextView = (TextView) findViewById(R.id.versionTextView);
        versionTextView.setText(getVersion());
        updateHasTextView = (TextView) findViewById(R.id.updateHasTextView);
        contactLinearLayout = (LinearLayout) findViewById(R.id.contactLinearLayout);
        contactLinearLayout.setOnClickListener(this);
        updateLinearLayout = (LinearLayout) findViewById(R.id.updateLinearLayout);
        updateLinearLayout.setOnClickListener(this);
        updateNotification = new Notification();
        updateNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification_update);
        updateNotification.icon = R.mipmap.ic_sg_launcher;
        setVerText(FindNewVersion.getIsNewVersion(mAppContext));
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    private String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return "v" + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_About_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateLinearLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_About_CheckForUpdates), null);
                if (NetUtil.isNetworkEnable(mAppContext)) {
                    if (updateInfos != null && updateInfos.size() > 0) {
                        //  showDownloadDialog(updateInfos);
                        mOrviboUpdateAgent.showOrviboUpdateTipsDialogNow(AboutActivity.this, updateInfos);
                        // mDownloadAgent.showOrviboUpdateTipsDialogNow(AboutActivity.this, responseInfos);
                    }
                } else {
                    ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                }
                break;
            }
            case R.id.contactLinearLayout: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_About_CopyTheWechat), null);
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                c.setText(getString(R.string.about_contact));
                ToastUtil.showToast(R.string.copy_success);
                break;
            }
        }
    }

    /**
     * orvibo 检查更新接口 version 1.9
     */
    private class OrviboCheckUpdateListener implements OrviboUpdateAgent.OrviboCheckUpdateListener {

        @Override
        public void onUpdateReturned(boolean isCanUpdate, HashMap<String, String> responseInfos) {
            LogUtil.d(TAG, "isCanUpdate:" + isCanUpdate + ",updateInfos=" + responseInfos);
            if (isCanUpdate) {
                FindNewVersion.setIsNewVersion(getApplicationContext(), true);
                if (responseInfos != null && responseInfos.size() > 0) {
                    updateInfos = responseInfos;
                    // showDownloadDialog(responseInfos);
                    //mOrviboUpdateAgent.showOrviboUpdateTipsDialogNow(AboutActivity.this,responseInfos);
                    // mDownloadAgent.showOrviboUpdateTipsDialogNow(AboutActivity.this, responseInfos);

                    setVerText(true);
                }
            } else {
                setVerText(false);
                FindNewVersion.setIsNewVersion(getApplicationContext(), false);
            }
        }
    }

    private void setVerText(boolean hasNewVer) {
        if (hasNewVer) {
            updateHasTextView.setText(R.string.about_update_has);
            updateHasTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            updateHasTextView.setText(R.string.about_update_no);
            updateHasTextView.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    private void showDownloadDialog(final HashMap<String, String> mHashMap) {
        if (!isFinishingOrDestroyed()) {
            // 发现新版本，提示用户更新
            this.mHashMap = mHashMap;
            String msg;
            if (PhoneUtil.isCN(mAppContext)) {// 中文
                msg = mHashMap.get(UpdateInfo.MESSAGE);
            } else {// 英文
                msg = mHashMap.get(UpdateInfo.MESSAGE_EN);
                if (msg == null || msg.length() == 0) {
                    msg = mHashMap.get(UpdateInfo.MESSAGE);
                }
            }
            final String v = UpdateInfo.VERSION;
            if (mHashMap.containsKey(v) && mHashMap.get(v) != null) {
                int serverVersion = Integer.valueOf(mHashMap.get(v));
                int cacheVersion = ServerVersion.getServerVersion(mAppContext);
                if (cacheVersion < serverVersion) {
                    ServerVersion.setServerVersion(mAppContext, serverVersion);
                }
            }

            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.about_update_tip));
            dialogFragmentTwoButton.setContent(msg);
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.about_update_not_now));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.about_update_now));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
            TimeCache.saveVersionTime(mAppContext);
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        }
    }

    /*  @Override
      public void onLeftButtonClick(View view) {

      }

      @Override
      public void onRightButtonClick(View view) {
        *//*  new Thread() {
            @Override
            public void run() {
                // 启动线程下载资源（退出程序调用System.exit(0)或者android.os.Process.killProcess(android.os.Process.myPid())都会结束下载线程，下载不能继续进行下去，
                // 如有service，service会销毁（销毁不会调用Ondestory方法）重启service(conCreate方法开始)）
                Downloader downloader = new Downloader(AboutActivity.this, handler);
                try {
                    URL url = new URL(mHashMap.get(UpdateInfo.URL));
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.deleteOnExit();
                    }
                    File fileDir = new File(Environment.getExternalStorageDirectory() + "/HomeMate/");
                    fileDir.mkdirs();
                    file.createNewFile();
                    downloader.download(url, file, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();*//*
        //        if (response != null) {
        File apkFile = UmengUpdateAgent.downloadedFile(mAppContext, response);
        if (apkFile != null && apkFile.exists())
            UmengUpdateAgent.startInstall(mAppContext, apkFile);
        else
            UmengUpdateAgent.startDownload(mAppContext, response);


    }
*/
    @Override
    protected void onDestroy() {
        UmengUpdateAgent.setUpdateListener(null);
        response = null;
        super.onDestroy();
    }
}
