package com.orvibo.homemate.update;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.FindNewVersion;
import com.orvibo.homemate.sharedPreferences.ServerVersion;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.sharedPreferences.UpdateCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by baoqi on 2016/6/29.
 */
public class DownloadAgent implements DialogFragmentTwoButton.OnTwoButtonClickListener {
    private final static String TAG = DownloadAgent.class.getSimpleName();
    private static DownloadAgent istance = new DownloadAgent();
    private static Context mContext;
    private static DownloadManager mDownloadManager;
    private static HashMap<String, String> mUpdateInfos;
    public static final int STATE_DOWNLOADING = 0;
    public static final int STATE_UNDOWNLOAD = 1;
    public static final int STATE_CANCEL = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_DOWNLOADED = 4;
    public static final int STATE_DOWNLOAD_FAIL = 5;
    public static final int STATE_DOWNLOAD_START = 6;
    //下载状态
    public int current_state = STATE_UNDOWNLOAD;
    private long preTaskId;

    public int getCurrentState() {
        return current_state;
    }

    public void setCurrentState(int state) {
        current_state = state;
    }

    private DownloadAgent() {

    }

    public static DownloadAgent getIstance(Context context) {
        mContext = context.getApplicationContext();
        //将下载请求加入下载队列
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        return istance;
    }

    public DownloadManager getDownloadManager() {
        if (mDownloadManager != null) {
            return mDownloadManager;
        }
        return null;
    }

    //使用系统下载器下载
    public long downloadAPK(String versionUrl, HashMap<String, String> updateInfos) {

        mDownloadManager.remove(preTaskId);
        //创建下载任务
        mUpdateInfos = updateInfos;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        // request.setShowRunningNotification(false);千万不要设置这个,会报错，在有些版本上 java.lang.SecurityException: Invalid value for visibility: 2
        request.setVisibleInDownloadsUi(true);
        request.setTitle(mContext.getString(R.string.app_name) + mContext.getString(R.string.orvibo_update));

        //sdcard的目录下的download文件夹，必须设置
        String path = File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator;
        //有的机型不会自动创建自定义的目录，导致无法下载（updateInfos.get(UpdateInfo.NAME)）
        File downloadApkParentsPath = getDownloadApkParentsPath(updateInfos);
        if (!downloadApkParentsPath.exists()) {
            downloadApkParentsPath.mkdirs();
        }
        request.setDestinationInExternalPublicDir(path, updateInfos.get(UpdateInfo.NAME) + File.separator + getFileName(updateInfos.get(UpdateInfo.URL), Integer.parseInt(updateInfos.get(UpdateInfo.VERSION))));
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径


        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        UpdateCache.setBoolean(mContext, false);
        long mTaskId = mDownloadManager.enqueue(request);
        preTaskId = mTaskId;
        setCurrentState(DownloadAgent.STATE_DOWNLOADING);
        return mTaskId;

//        //注册广播接收者，监听下载状态
//        mContext.registerReceiver(receiver,
//                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    /**
     * 注册下载监听的广播
     *
     * @param context
     * @param receiver
     * @param filter
     */

    public void registerDownloadReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        context.registerReceiver(receiver, filter);

    }

    /**
     * 取消注册下载监听的广播
     *
     * @param context
     * @param receiver
     */
    public void unRegisterDownloadReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);

    }

    /**
     * 安装apk
     *
     * @param file
     */
    public static void installApk(File file) {
        //下载到本地后执行安装
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + file.toString());
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag,后面解释
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    /**
     * 获得apk下载路径的文件
     *
     * @return
     */
    public static File getDownloadApkPath(HashMap<String, String> updateInfos) {

        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + updateInfos.get(UpdateInfo.NAME) + File.separator + getFileName(updateInfos.get(UpdateInfo.URL), Integer.parseInt(updateInfos.get(UpdateInfo.VERSION)));

        return new File(downloadPath);
    }

    /**
     * 获得apk下载路径的文件的父路径
     *
     * @return
     */
    public static File getDownloadApkParentsPath(HashMap<String, String> updateInfos) {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + updateInfos.get(UpdateInfo.NAME);
        File file = new File(downloadPath);
        return file;
    }

    /**
     * 启动页时调用
     * 获得apk下载路径的文件的父路径
     *
     * @return
     */
    public static File getDownloadApkParentsPath(String name) {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + name;
        return new File(downloadPath);
    }

    /**
     * 获取到文件的名字
     * 以版本号为开头，当结尾的版本号与当前的app的版本号一致时表示已经安装了，并且删除该文件
     *
     * @param path
     * @return
     */
    public static String getFileName(String path, int versionCode) {
//			String path = "http://localhost:8080/kugou.exe" ;
        int index = path.lastIndexOf("/") + 1;
        return versionCode + "_" + path.substring(index);
    }

    /**
     * 前台调用
     *
     * @param context
     * @param responseInfos
     */
    public void startDownload(Context context, HashMap<String, String> responseInfos) {
        //下载管理器如果禁用了就去启用
//        int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
//        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
//            toDownloadsSetting();
//        } else {
        if (current_state == STATE_DOWNLOADING) {
            ToastUtil.showToast(context.getString(R.string.orvibo_update));
            return;
        } else {
            UpdateCache.setBoolean(mContext, false);
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(IntentKey.UPDATEINFOS, (Serializable) responseInfos);
            context.startService(intent);
        }
        //      }
//        }
    }

    /**
     * 跳转到系统下载管理的设置界面
     */
    private void toDownloadsSetting() {
        String packageName = "com.android.providers.downloads";
        current_state = STATE_UNDOWNLOAD;//下载过程中如果禁用下载管理，下载会中断（可是DownloadService中的广播却监听不到导致状态没有改变），再次点击下载，提示“正在更新”(可是这样改变状态再次点击下载，时有一个问题会有新的请求发送（有两个文件同时下载）)
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//必修新建一个任务栈
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 升级的弹框提示（会根据再次显示的时间，如果又有新的版本再显示）
     *
     * @param activity
     * @param mHashMap
     */
    public void showOrviboUpdateTipsDialog(final Activity activity, HashMap<String, String> mHashMap) {
        mUpdateInfos = mHashMap;//这里一定要赋值，更新信息需要从外部传入
        // 发现新版本，提示用户更新
        boolean forceUpdate = false;
        String msg;
        if (PhoneUtil.isCN(activity)) {// 中文
            msg = mHashMap.get(UpdateInfo.MESSAGE);
        } else {// 英文
            msg = mHashMap.get(UpdateInfo.MESSAGE_EN);
            if (msg == null || msg.length() == 0) {
                msg = mHashMap.get(UpdateInfo.MESSAGE);
            }
        }

        //用户当前版本<=oldVersion的话就强制用户升级
        int oldVersion = 0;
        final String ol = UpdateInfo.OLD_VERSION;
        if (mHashMap.containsKey(ol) && mHashMap.get(ol) != null) {
            oldVersion = Integer.valueOf(mHashMap.get(ol));
            int localVersion = AppTool.getAppVersionCode(activity);
            if (localVersion != -1 && localVersion <= oldVersion) {
                forceUpdate = true;
            }
        }

        int day = 0;
        final String d = UpdateInfo.DAY;
        if (mHashMap.containsKey(d) && mHashMap.get(d) != null) {
            day = Integer.valueOf(mHashMap.get(d));
            if (day > 30 || day < 0) {
                day = 7;
            }
        }

        long duration = 0L;
        final String v = UpdateInfo.VERSION;
        if (mHashMap.containsKey(v) && mHashMap.get(v) != null) {
            int serverVersion = Integer.valueOf(mHashMap.get(v));
            int cacheVersion = ServerVersion.getServerVersion(activity);
            if (cacheVersion < serverVersion) {
                ServerVersion.setServerVersion(activity, serverVersion);
            } else {
                long cacheTime = TimeCache.getVersionTime(activity);
                long currentTime = System.currentTimeMillis() / 1000;
                duration = currentTime - cacheTime - day * 24 * 60 * 60;
                if (duration < 0 && !forceUpdate) {
                    return;
                }
            }
        }

        showDialog(activity, forceUpdate, msg, mHashMap);
    }

    private void showDialog(final Activity activity, boolean forceUpdate, String msg, final HashMap<String, String> hashMap) {
        final DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        if (forceUpdate && NetUtil.isWifi(activity)) {
            dialogFragmentTwoButton.setContent(activity.getString(R.string.about_update_force_content));
            dialogFragmentTwoButton.setCancelable(false);
        } else {
            dialogFragmentTwoButton.setTitle(activity.getString(R.string.about_update_tip));
            dialogFragmentTwoButton.setContent(msg);
            dialogFragmentTwoButton.setLeftButtonText(activity.getString(R.string.about_update_not_now));
        }

        dialogFragmentTwoButton.setRightButtonText(activity.getString(R.string.about_update_now));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);

        try {
            TimeCache.saveVersionTime(activity);
            dialogFragmentTwoButton.show(activity.getFragmentManager(), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 升级弹框提示（立即提示）
     *
     * @param activity
     * @param mHashMap
     */
    public void showOrviboUpdateTipsDialogNow(final Activity activity, HashMap<String, String> mHashMap) {

        // 发现新版本，提示用户更新
        mUpdateInfos = mHashMap;
        String msg;
        if (PhoneUtil.isCN(activity)) {// 中文
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
            int cacheVersion = ServerVersion.getServerVersion(activity);
            if (cacheVersion < serverVersion) {
                ServerVersion.setServerVersion(activity, serverVersion);
            }
        }

        showDialog(activity, false, msg, mHashMap);
    }


    @Override
    public void onLeftButtonClick(View view) {

    }

    @Override
    public void onRightButtonClick(View view) {
        if (isCanInstall(mUpdateInfos)) {
            installApk(getDownloadApkPath(mUpdateInfos));
        } else {
            deleteOldVersionFile(getDownloadApkParentsPath(mUpdateInfos));
        }

    }

    /**
     * 判断该版本的apk文件是否已经下载过
     * 确保下载过的apk的版本和服务端的一致
     *
     * @param updateInfos
     * @return boolean
     */
    private boolean isCanInstall(HashMap<String, String> updateInfos) {
        LogUtil.d(TAG, "isCanInstall():current_state=" + current_state);
        if (current_state == STATE_DOWNLOADING) {
            return false;
        }
        if (updateInfos != null && updateInfos.size() > 0) {
            File downloadApkPath = getDownloadApkPath(updateInfos);
            String fileName = downloadApkPath.getName();
            if (downloadApkPath.exists() && downloadApkPath.getName().startsWith(updateInfos.get(UpdateInfo.VERSION) + "_") && UpdateCache.getBoolean(mContext)) {
                return true;
            }

        }
        return false;


    }

    /**
     * 删除老的apk版本文件
     *
     * @param dir
     */
    private void deleteOldVersionFile(File dir) {
        int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            toDownloadsSetting();

        } else {
            if (current_state == STATE_DOWNLOADING) {
                ToastUtil.showToast(mContext.getString(R.string.orvibo_update));
                return;
            }
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }
            } else {
                dir.mkdirs();//有些机型不会自动创建文件，导致无法下载
            }
            istance.startDownload(mContext, mUpdateInfos);
        }
    }

    /**
     * 在启动页判断是否当前安装的版本是否与下载的版本是否一致
     *
     * @param versionCode
     * @param name        要根据app的名字
     * @return
     */
    public static boolean isInstalled(int versionCode, String name) {
        File downloadApkParentsPath = getDownloadApkParentsPath(name);
        if (downloadApkParentsPath.exists()) {
            File[] files = downloadApkParentsPath.listFiles();
            for (File file : files) {
                if (file.getName().endsWith("_" + versionCode)) {
                    String fileName = file.getName();
                    if (file.getName().endsWith(("_" + versionCode))) {
                        FindNewVersion.setIsNewVersion(mContext, false);
                        file.delete();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
