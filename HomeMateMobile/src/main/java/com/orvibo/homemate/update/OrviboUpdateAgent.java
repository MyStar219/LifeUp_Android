package com.orvibo.homemate.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.core.OrviboThreadPool;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.sharedPreferences.FindNewVersion;
import com.orvibo.homemate.sharedPreferences.ServerVersion;
import com.orvibo.homemate.sharedPreferences.TimeCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智家365 apk 升级管理
 * 升级功能一定要稳定
 * Created by baoqi on 2016/6/7.
 */
public class OrviboUpdateAgent {
    private static final String LOCK_DOWNLOAD = "downloadLock";
    private static final String TAG = OrviboUpdateAgent.class.getSimpleName();
    private static Context mContext;
    private OrviboUpdateApkTask mOrviboUpdateApkTask;
    private OrviboCheckUpdateListener mOrviboCheckUpdateListener;
    private OrviboUpdateDownloadListener mOrviboUpdateDownloadListener;
    private static OrviboUpdateAgent mOrviboUpdateAgent = new OrviboUpdateAgent();

    public static final int STATE_DOWNLOADING = 0;
    public static final int STATE_UNDOWNLOAD = 1;
    public static final int STATE_CANCEL = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_DOWNLOADED = 4;
    public static final int STATE_DOWNLOAD_FAIL = 5;
    public static final int STATE_DOWNLOAD_START = 6;
    //下载状态
    private static volatile int current_state = STATE_UNDOWNLOAD;
    //下载进度的记录
    private volatile int progress = 0;
    //下载文件的内容长度
    public static int mContentLength;
    //下载过程中记录下载进度的文件名
    private static String progressFilename = "progress.txt";
    // 开始下载前，需要读进度记录文件赋值progress,然后标志已经赋值
    private boolean isInitProgress = false;
    //记录下载进度的文件
    private File progressFile;
    //    //记录文件进度的输出流
//    private RandomAccessFile bw_sum = null;
    //发送消息时的数据的设置标志
    public final static String PROGRESS_KEY = "progress";
    public final static String RESULT_KEY = "result";
    //消息类型的分类
    public final static int WHAT_PROGRSS = 0;
    public final static int WHAT_FINISH = 2;
    public final static int WHAT_RESULT = 1;
    public final static int WHAT_DOWNLOADING = 3;
    public final static int WHAT_PAUSE = 4;
    public final static int WHAT_CANCEL = 5;
    public final static int WHAT_START = 6;
    //下载过程中累加下载进度
    private int delta_progress = 0;
    //下载过程中累加下载进度，如果超过该值，需要通知通知栏更新进度
    private final float delta = 0.01f;
    // 更新信息
    public static HashMap<String, String> updateInfos = new HashMap<String, String>();
    //设置记录数据一天有效
    private long druation = 24 * 3600 * 1000;
    private int threadCount;
    private static NoHttpDownloadRequest mNoHttpDownloadRequest;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case WHAT_PROGRSS:
                    if (mOrviboUpdateDownloadListener != null) {
                        Bundle data = msg.getData();
                        Integer progress = data.getInt(OrviboUpdateAgent.PROGRESS_KEY);
                        mOrviboUpdateDownloadListener.onProgress(progress, mContentLength);
                    }
                    break;
                case WHAT_FINISH:
                    if (mOrviboUpdateDownloadListener != null) {
                        mOrviboUpdateDownloadListener.onLoadFinsh();
                        stopUpdateService();
                    }
                    break;
                case WHAT_DOWNLOADING:
                    ToastUtil.showToast(mContext.getString(R.string.orvibo_update_downloading));
                    break;
                case WHAT_CANCEL:
                    if (mOrviboUpdateDownloadListener != null) {
                        mOrviboUpdateDownloadListener.onCancel();
                        stopUpdateService();
                    }
                    ToastUtil.showToast(mContext.getString(R.string.orvibo_update_cancel));
                    break;
                case WHAT_PAUSE:
                    if (mOrviboUpdateDownloadListener != null) {
                        mOrviboUpdateDownloadListener.onPause(progress, mContentLength);
                        //   stopUpdateService();
                    }
                    ToastUtil.showToast(mContext.getString(R.string.orvibo_update_pause));
                    break;
                case WHAT_START:
                    startDownload(mContext, updateInfos);
                    break;
                case WHAT_RESULT:
                    // 处理一些错误/结果/安装信息
                    handleResultMessage(msg);
                    break;
            }
        }
    };

    /**
     * 下载完成，下载失败都停止更新服务
     */
    private void stopUpdateService() {
        Intent intent = new Intent(mContext, UpdataService.class);
        mContext.stopService(intent);
    }

    /**
     * 提供给外部对象使用，更新的apk的消息都统一由OrviboUpdateAgent来处理
     *
     * @return
     */
    public Handler getHandler() {
        return mHandler;
    }

    public void handleResultMessage(Message msg) {
        Bundle data = msg.getData();
        int result = data.getInt(RESULT_KEY);
        switch (result) {
            case ErrorCode.SUCCESS:
                LogUtil.d(TAG, mContext.getString(R.string.orvibo_install_success));
                Intent intent = new Intent(mContext, UpdataService.class);
                mContext.stopService(intent);
                //  ToastUtil.showToast(mContext.getString(R.string.orvibo_install_success));
                break;
            case ErrorCode.FAIL:
                if (mOrviboUpdateDownloadListener != null) {
                    mOrviboUpdateDownloadListener.onFail();
                    stopUpdateService();
                }
                //   ToastUtil.showToast(mContext.getString(R.string.orvibo_update_fail));
                break;
            case ErrorCode.TIMEOUT:
                //   ToastUtil.showToast(mContext.getString(R.string.TIMEOUT));
                LogUtil.e(TAG, "升级更新信息：" + mContext.getString(R.string.TIMEOUT));
                break;
        }
    }

    private OrviboUpdateAgent() {

    }

    //设置成单例模式，（MainActivity 和AboutActivity会使用到）
    public static OrviboUpdateAgent getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mNoHttpDownloadRequest == null) {
            synchronized (LOCK_DOWNLOAD) {
                if (mNoHttpDownloadRequest == null) {
                   // mContext = context.getApplicationContext();
                    mNoHttpDownloadRequest = new NoHttpDownloadRequest(mContext);
                }
            }
        }
        return mOrviboUpdateAgent;
    }

    /**
     * 获取客户端与服务端的IO流：更新信息
     *
     * @param url
     * @return
     */
    private InputStream requestInputStream(String url, int reconnect) {
        InputStream inputStream = null;
        try {
            URL netUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) netUrl.openConnection();
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(10 * 1000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                //获得内容的长度
                inputStream = connection.getInputStream();
            }
//            HttpGet httpGet = new HttpGet(url); //android 6.0 没有了这个api
//            httpGet.addHeader("Range","bytes="+progress+"-");
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpResponse httpResponse = httpClient.execute(httpGet);
//            long contentLength = httpResponse.getEntity().getContentLength();
//            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            if (statusCode == 200) {
//
//                inputStream = httpResponse.getEntity().getContent();
//            } else {
//                //   Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
//            }
        } catch (SocketTimeoutException e) {
            LogUtil.e(TAG, "倒计重发：第" + reconnect + "次");
            reconnect--;
            if (reconnect > 0) {
                requestInputStream(url, reconnect);
            } else {
                //  超时
                LogUtil.e(TAG, "获取升级信息超时" + "--倒计重发结束");
                Message message = mHandler.obtainMessage();
                message.what = WHAT_RESULT;
                Bundle data = message.getData();
                data.putInt(RESULT_KEY, ErrorCode.TIMEOUT);
                mHandler.sendMessage(message);
            }

        } catch (Exception e) {
            if (mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.what = WHAT_RESULT;
                Bundle data = message.getData();
                data.putInt(RESULT_KEY, ErrorCode.FAIL);
                mHandler.sendMessage(message);
            }
            e.printStackTrace();
        }
        return inputStream;
    }

    /**
     * 获取客户端与服务端的IO流：把文件分段，获得对应内容段的io流
     *
     * @param url
     * @return
     */
    private Map requestInputStream(String url, File file, int start, int end, int threadId, int reconnect) {
        LogUtil.e(TAG, "threadId=" + threadId + ",start:" + start + "---end:" + end);
        HashMap map = null;
        InputStream inputStream = null;
        try {

            URL netUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) netUrl.openConnection();
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(10 * 1000);
            connection.setAllowUserInteraction(true);
            //一旦设置了该请求头，返回的状态码是206，不是200
            //告诉服务器，我这个线程，从什么位置下载到什么位置。服务器只会返回处于这一段区域的数据流
            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);//为了可以断点下载，后台运行或者点击暂停下载时需要记住起始点
            connection.connect();
            Map<String, List<String>> headerFields = connection.getHeaderFields();

            LogUtil.e(TAG, "headerFields=" + headerFields);
            int responseCode = connection.getResponseCode();
            LogUtil.e(TAG, "responseCode=" + responseCode);
            if (responseCode == 206 || responseCode == 200) {
                int length = connection.getContentLength();
                LogUtil.e(TAG, "threadId=" + threadId + ",length:" + length);
                map = new HashMap();
                //缓存文件，一个任务对应一个
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rws");
                map.put(getFileKey(threadId), randomAccessFile);
                //设置起始点的偏移量,每个任务的设置不一样
                randomAccessFile.seek(start);
                inputStream = connection.getInputStream();
                map.put(getStreamKey(threadId), inputStream);
            }
//            HttpGet httpGet = new HttpGet(url); //android 6.0 没有了这个api
//            httpGet.addHeader("Range","bytes="+progress+"-");
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpResponse httpResponse = httpClient.execute(httpGet);
//            long contentLength = httpResponse.getEntity().getContentLength();
//            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            if (statusCode == 200) {
//
//                inputStream = httpResponse.getEntity().getContent();
//            } else {
//                //   Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
//            }
        } catch (SocketTimeoutException e) {
            //这里可能会捕获某个子任务的异常
            LogUtil.e(TAG, "倒计重发：第" + reconnect + "次" + ",子任务threadId=" + threadId);
            reconnect--;
            if (reconnect > 0) {
                requestInputStream(url, file, start, end, threadId, reconnect);
            } else {
                // 超时
                LogUtil.e(TAG, "子任务threadId=" + threadId + "，倒计重发结束");
                Message message = mHandler.obtainMessage();
                message.what = WHAT_RESULT;
                Bundle data = message.getData();
                data.putInt(RESULT_KEY, ErrorCode.TIMEOUT);
                mHandler.sendMessage(message);
            }
        } catch (Exception e) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(ErrorCode.FAIL);
            }
            e.printStackTrace();
        }
        return map;
    }

    private String getFileKey(int threadId) {
        return threadId + "file";
    }

    private String getStreamKey(int threadId) {
        return threadId + "stream";
    }

    /**
     * 启动更新apk
     */
    public void update() {


        mOrviboUpdateApkTask = new OrviboUpdateApkTask();
        mOrviboUpdateApkTask.execute(UpdateUrlConstant.getUpdateUrl(mContext));
    }

    /**
     * 判断是否正在下载中，正在更新的时候设置不弹框
     *
     * @return
     */
    public boolean isDownloading() {
        synchronized (LOCK_DOWNLOAD) {
            return current_state == STATE_DOWNLOADING;
        }
    }

    /**
     * 控制下载与暂停
     */
    public void togggleState() {
        synchronized (LOCK_DOWNLOAD) {
            if (current_state == STATE_DOWNLOADING) {
                current_state = STATE_UNDOWNLOAD;
            } else {
                current_state = STATE_DOWNLOADING;
            }
        }
    }

    /**
     * 更新的任务
     */
    private class OrviboUpdateApkTask extends AsyncTask<String, Void, HashMap<String, String>> {

        private HashMap<String, String> responseInfos = new HashMap();

        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            InputStream inputStream = null;
            BufferedReader br = null;
            try {
                inputStream = requestInputStream(params[0], 2);
                if (inputStream != null) {
                    br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    StringBuilder result = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    LogUtil.d(TAG, "result =" + result);
                    JSONObject apkUrlObject = new JSONObject(result.toString());
                    //解析出service端的versioncode，升级描述，apk的路径
                    String versionCode = apkUrlObject.getString(UpdateInfo.VERSION);
                    String oldversionCode = apkUrlObject.getString(UpdateInfo.OLD_VERSION);//如果当前的版本比老版本还低，强制更新
                    String day = apkUrlObject.getString(UpdateInfo.DAY);
                    String message_ch = apkUrlObject.getString(UpdateInfo.MESSAGE);
                    String message_en = apkUrlObject.getString(UpdateInfo.MESSAGE_EN);
                    String apkurl = apkUrlObject.getString(UpdateInfo.URL);
                    String size = apkUrlObject.getString(UpdateInfo.SIZE);
                    String packagename = apkUrlObject.getString(UpdateInfo.PACKAGENAME);
                    String name = apkUrlObject.getString(UpdateInfo.NAME);
//                    responseInfos.put("version", versionCode);
//                    responseInfos.put("message_ch", message_ch);
//                    responseInfos.put("message_en", message_en);
//                    responseInfos.put("apkurl", apkurl);
                    responseInfos.put(UpdateInfo.VERSION, versionCode);
                    responseInfos.put(UpdateInfo.OLD_VERSION, oldversionCode);
                    responseInfos.put(UpdateInfo.DAY, day);
                    responseInfos.put(UpdateInfo.MESSAGE, message_ch);
                    responseInfos.put(UpdateInfo.MESSAGE_EN, message_en);
                    responseInfos.put(UpdateInfo.URL, apkurl);
                    responseInfos.put(UpdateInfo.SIZE, size);
                    responseInfos.put(UpdateInfo.PACKAGENAME, packagename);
                    responseInfos.put(UpdateInfo.NAME, name);
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return responseInfos;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> responseInfos) {
            super.onPostExecute(responseInfos);
            updateInfos = responseInfos;
            boolean isCanUpdate = false;
            if (responseInfos != null && responseInfos.size() > 0) {
                int localVersionCode = getAppVersionCode(mContext);
                LogUtil.e(TAG, "localVersionCode=" + localVersionCode);
                String serviceVersionCode = (String) responseInfos.get(UpdateInfo.VERSION);
                //如果service端的版本号高于客户端，并且包名一致，则弹框提示更新
                if (Integer.parseInt(serviceVersionCode) > localVersionCode && ViHomeApplication.getAppContext().getPackageName().equals(responseInfos.get(UpdateInfo.PACKAGENAME))) {
                    isCanUpdate = true;
                    if (mOrviboCheckUpdateListener != null) {
                        // FindNewVersion.setIsNewVersion(mContext, true);这句代码是给AboutActivity做标记的
                        mOrviboCheckUpdateListener.onUpdateReturned(isCanUpdate, responseInfos);

                    }
                } else {
                    isCanUpdate = false;
                    if (mOrviboCheckUpdateListener != null) {
                        mOrviboCheckUpdateListener.onUpdateReturned(isCanUpdate, responseInfos);
                    }
                }
            } else {
                isCanUpdate = false;
                if (mOrviboCheckUpdateListener != null) {
                    mOrviboCheckUpdateListener.onUpdateReturned(isCanUpdate, responseInfos);
                }
            }
        }
    }

    /**
     * 获得当前应用的版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        try {
            PackageManager pkManager = context.getPackageManager();//
            PackageInfo pkInfo = pkManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return pkInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 检查更新结果回调
     */
    public interface OrviboCheckUpdateListener {
        //检查是否有可更新的版本,有的话弹框提示用户
        public void onUpdateReturned(boolean isCanUpdate, HashMap<String, String> responseInfos);

    }

    /**
     * 下载过程的回调
     */
    public interface OrviboUpdateDownloadListener {
        //apk文件下载完后回调（可以设置跳转到应用的安装界面）
        public void onLoadFinsh();

        public void onPause(int progress, int contentLength);

        public void onCancel();

        //更新通知栏进度
        public void onProgress(int progress, int contentLength);

        public void onFail();
    }

    public void setOrviboUpdateDownloadListener(OrviboUpdateDownloadListener listener) {
        mOrviboUpdateDownloadListener = listener;
    }

    public void setOrviboCheckUpdateListener(OrviboCheckUpdateListener listener) {
        mOrviboCheckUpdateListener = listener;
    }

    /**
     * 开始下载服务器上的apk，这个方法应该启动service后台运行时调用
     * 这个方法不能连续调用
     *
     * @param responseInfos
     */


    public void startDownload(HashMap<String, String> responseInfos, int threadCount) {
        LogUtil.d(TAG, "startDownload():service");
        //当进程kill后，OrviboUpdateAgent里的updateInfos为empty，服务里的onStartCommand自动重启时updateInfos不为empty，所以这里必须在次赋值
        //TODO
        this.updateInfos = responseInfos;
        this.threadCount = threadCount;
        //  LogUtil.e(TAG, "responseInfos=" + responseInfos + "=====this.updateInfos" + this.updateInfos);
        boolean networkEnable = NetUtil.isNetworkEnable(mContext);
        if (networkEnable) {
            //避免多次调用时 确保 current_state = STATE_DOWNLOAD_START能被赋值
            synchronized (LOCK_DOWNLOAD) {
                if (current_state == STATE_DOWNLOADING || current_state == STATE_DOWNLOAD_START) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(WHAT_DOWNLOADING);
                    }
                    return;
                }
                if (current_state == STATE_PAUSE || current_state == STATE_CANCEL || current_state == STATE_UNDOWNLOAD) {
                    current_state = STATE_DOWNLOAD_START;
                }
            }
            String apkUrl = (String) responseInfos.get(UpdateInfo.URL);
            if (!downloadedFile(responseInfos).exists()) {
                deleteFile();
            } else {
                //如果会去下载，说明apk没有下载完成(如果finish文件存在，那么是上一次已经下载完成，但是没有安装apk时残留下来的，这时要把他删除，解决提示安装包解析错误的问题)
                File downloadfinishFile = getCacheDownloadfinishFile();
                if (downloadfinishFile.exists()) {
                    downloadfinishFile.delete();
                }

                File cacheTimeFile = getCacheTimeFile();
                if (cacheTimeFile.exists()) {
                    try {
                        BufferedReader br_time = new BufferedReader(new FileReader(cacheTimeFile));
                        String timeString = br_time.readLine();
                        if (timeString != null) {
                            long cachetime = Long.parseLong(timeString);
                        } else {
                            cacheTimeFile.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            // mApkUrl = "http://192.168.2.170:8080/lenovo.apk";
            if (responseInfos != null && responseInfos.size() > 0) {
                isInitProgress = false;//每次重新下载时都要赋值为false，因为要再次为progress赋值初始化已经下载的进度
                progress = 0;//（这里为什么赋值“0”的原因）中途断了下载，应用在后台运行，但是progress的值是当前进度的值，删除记录文件，再次下载但是progress依然记录了上一次进度的值，导致进度失败

                OrviboThreadPool.getInstance().submitDownloadTask(new DownloaderTask(apkUrl, threadCount));

            }
        } else {
            ToastUtil.showToast(mContext.getString(R.string.network_canot_work));
        }
    }

    /**
     * 前台调用
     *
     * @param context
     * @param responseInfos
     */
    public void startDownload(Context context, HashMap<String, String> responseInfos) {
        updateInfos = responseInfos;
        Intent intent = new Intent(context, UpdataService.class);
        intent.putExtra(IntentKey.UPDATEINFOS, (Serializable) responseInfos);
        context.startService(intent);
    }

    /**
     * 通过NOHttp下载
     */
    public void startDownloadByNOHttp(HashMap<String, String> updateInfos) {
        this.updateInfos = updateInfos;
        //mNoHttpDownloadRequest.setHandler(mHandler);
        mNoHttpDownloadRequest.download(updateInfos);
    }

    /**
     * apk 存储的路径
     *
     * @return
     */
    public static String getSavePath() {
        String mSdcardPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !Environment.isExternalStorageRemovable()) {
            mSdcardPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + LogcatHelper.DIRECTORY_NAME;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            mSdcardPath = mContext.getFilesDir().getAbsolutePath() + File.separator + LogcatHelper.DIRECTORY_NAME;

        }
        return mSdcardPath;
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
     * 下载任务，他会分配几个线程去完成任务
     */
    private class DownloaderTask implements Runnable {
        private String url;
        private int threadCount;

        public DownloaderTask(String url, int threadCount) {
            this.url = url;
            this.threadCount = threadCount;
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(this.url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10 * 1000);
                urlConnection.setReadTimeout(10 * 1000);
                urlConnection.connect();//打开连接
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    mContentLength = urlConnection.getContentLength();
                    LogUtil.d(TAG, "文件的大小,mContentLength=" + mContentLength + ",updateInfos:" + updateInfos);

                    File outFile = downloadedFile(updateInfos); //sdcard上的目标地址,应该截取路径上的名字
                    RandomAccessFile randomAccessFile = new RandomAccessFile(outFile, "rws");
                    //在本地生成一个与服务端大小一样的文件
                    randomAccessFile.setLength(mContentLength);
                    randomAccessFile.close();

                    //给每个任务分段
                    int blockSize = mContentLength / threadCount;
                    for (int threadId = 0; threadId < threadCount; threadId++) {
                        int start = threadId * blockSize;
                        int end = (threadId + 1) * blockSize - 1;
                        if (threadId == threadCount - 1) {
                            end = mContentLength - 1;
                        }
                        LogUtil.d(TAG, "threadId =" + threadId + " ,Range:" + start + "--" + end);
                        // 启动子任务
//                        OrviboThreadPool.getInstance().submitDownloadTask(new SonDownloadTask(outFile, threadId, start, end, this.url, threadCount));
                        new Thread(new SonDownloadTask(outFile, threadId, start, end, this.url, threadCount)).start();
                    }
                }

            } catch (SocketTimeoutException e) {
                LogUtil.e(TAG, "DownloaderTask:" + "获取文件长度时相应超时");
                Message message = mHandler.obtainMessage();
                message.what = WHAT_RESULT;
                Bundle data = message.getData();
                data.putInt(RESULT_KEY, ErrorCode.TIMEOUT);
                mHandler.sendMessage(message);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

        }
    }

    /**
     * 子任务
     */
    private class SonDownloadTask implements Runnable {
        //   private static final String TAG = "SonDownloadTask";
        File file;
        int threadId;
        int start;
        int end;
        String url;
        int threadCount;

        public SonDownloadTask(File file, int threadId, int start, int end, String url, int threadCount) {
            this.file = file;
            this.threadId = threadId;
            this.start = start;
            this.end = end;
            this.url = url;
            this.threadCount = threadCount;
        }

        @Override
        public void run() {

            int currentPosition = start;
            // 读取子任务进度起始点
            //  File cacheFile = new File(getSavePath(), threadId + ".txt");
            File cacheFile = getCacheFile(updateInfos, threadId);
            File cachetimeFile = getCacheTimeFile();
            if (cachetimeFile.exists()) {
                if (cacheFile.exists()) {
                    BufferedReader br_time = null;
                    BufferedReader br = null;
                    try {
                        if (cachetimeFile.exists()) {
                            br_time = new BufferedReader(new FileReader(cachetimeFile));
                            long cachetime = Long.parseLong(br_time.readLine());
                            if (cachetime + 24 * 60 * 60 * 1000 >= System.currentTimeMillis()) {
                                br = new BufferedReader(new FileReader(cacheFile));
                                //读取文件，赋值当前子任务的进度
                                String tmp = br.readLine();
                                if (!TextUtils.isEmpty(tmp)) {
                                    currentPosition = Integer.parseInt(tmp);
                                } else {
                                    currentPosition = start;
                                    LogUtil.e(TAG, "run()-" + threadId + " 重新下载。");
                                }
                                //初始化进度
                                synchronized (DownloaderTask.class) {
                                    progress += (currentPosition - start);
                                    LogUtil.d(TAG, "threadId=" + threadId + ",currentPosition=" + currentPosition);
                                }
                            } else {
                                LogUtil.w(TAG, "run()-数据超过一天，丢弃旧数据，重新开始下载。");
//                                cacheFile.delete();
//                                cachetimeFile.delete();
//                                File apkfile = downloadedFile(updateInfos);
                                onTimeOutOneDayToDeleteFile();
                            }
                        } else {
                            LogUtil.e(TAG, "run()-找不到 cachetimeFile" + cachetimeFile);
                        }
                    } catch (Exception e) {
                        //  onTimeOutOneDayToDeleteFile();
                        e.printStackTrace();
                    } finally {
                        //  LogUtil.d(TAG, "run()-finish threadId:" + threadId);
                        try {
                            if (br != null) {
                                br.close();
                            }
                            if (br_time != null) {
                                br_time.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            //文件的输入流
            BufferedInputStream bis = null;
            //记录文件进度的输出流
            RandomAccessFile bw_sum = null;
            //记录子任务的进度的输出流
            RandomAccessFile bw = null;
            try {
                if (currentPosition > this.end) {
                    //当某个任务结束后currentPosition>this.end  range code==416的原因
                    return;
                }
                Map map = requestInputStream(this.url, this.file, currentPosition, this.end, this.threadId, 2);
                LogUtil.d(TAG, "run()-threadId:" + threadId + ",currentPosition:" + currentPosition + ",end:" + end + ",map:" + map);
                if (map != null) {

                    bis = new BufferedInputStream((InputStream) map.get(getStreamKey(this.threadId)));
                    // RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rws");
                    byte[] bytes = new byte[1024 * 1024 * 3];
                    int len = -1;
                    File currentThreadIdFile = null;
                    while (((len = bis.read(bytes)) != -1)) {
                        //    synchronized (LOCK_DOWNLOAD) {
                        if (current_state == STATE_CANCEL || current_state == STATE_PAUSE) {
                            LogUtil.e(TAG, "SonDownloadTask :run() current_state=" + current_state);
                            return;
                        }
//                        if (progress > 1000000) {
//                            throw new Exception();
//                        }
                        //   }
                        // bos.write(bytes, 0, len);
                        ((RandomAccessFile) (map.get(getFileKey(this.threadId)))).write(bytes, 0, len);

                        //记录下载进度(总)
                        // 记录总任务进度起始点,哪个任务最先执行到这里，由他初始化
                        if (progressFile == null) {
                            progressFile = getCacheProgressFile();
                        }
                        if (bw_sum == null) {
                            LogUtil.d(TAG, "threadId=" + threadId);
                            bw_sum = new RandomAccessFile(progressFile, "rws");
                        }
                        synchronized (DownloaderTask.class) {
                            current_state = STATE_DOWNLOADING;
                            progress += len;
                            delta_progress += len;
                            //用文件记录当前下载的宗的进度(总进度的记录文件时共享文件，避免多线程操作导致数据错误必须枷锁)
                            //  bw_sum.setLength(0); //记录之前先清空
                            bw_sum.seek(0);//标记从position位置写入
                            bw_sum.write((progress + "").getBytes());
                            LogUtil.d(TAG, "contentLength =" + mContentLength + "，当前的总下载进度：progress =" + progress + ",threadId=" + threadId);
                        }


                        //记录下载进度(分支)(线程内的对象无需枷锁)
                        currentPosition += len;
                        //  LogUtil.d(TAG, "threadId =" + this.threadId + ",当前子任务的下载进度：currentPosition =" + currentPosition);
                        // 记录子任务进度起始点
                        if (currentThreadIdFile == null) {
                            //TODO
                            currentThreadIdFile = getCacheFile(updateInfos, threadId);
                        }
                        if (bw == null) {
                            cacheCurrentSystemTime();//缓存最新的系统时间
                            bw = new RandomAccessFile(currentThreadIdFile, "rws");
                        }
                        //用文件记录当前分支的进度
                        bw.seek(0); //记录之前先清空
                        bw.write((currentPosition + "").getBytes());

                        if (mOrviboUpdateDownloadListener != null) {
                            Message message = mHandler.obtainMessage();
                            message.what = OrviboUpdateAgent.WHAT_PROGRSS;
                            Bundle data = message.getData();
                            data.putInt(OrviboUpdateAgent.PROGRESS_KEY, progress);
                            if ((delta_progress + 0.0f) / mContentLength > delta || progress == mContentLength) {
                                delta_progress = 0;
                                mHandler.sendMessage(message);
                            }
                            // mOrviboCheckUpdateListener.onProgress(progress, mContentLength, len);
                        }
                    }
                    LogUtil.e(TAG, "下载完成:threadId=" + threadId + ",currentPosition=" + currentPosition + "-----progress=" + progress + ";end=" + end);
                    ((RandomAccessFile) (map.get(getFileKey(this.threadId)))).close();

//                if (currentThreadIdFile.exists()) {
//                    currentThreadIdFile.delete();//如果在这里删除（但某个任务已经完成，总任务没有完成，并且下载任务被中断，再次启动下载任务，因为记录文件已经删除，这个任务起始点就会赋上错误的值）
//                }

                    if (progress == mContentLength) {
                        current_state = STATE_DOWNLOADED;
                        //下载完成后需要记录一个下载完成的标记
                        cacheDownloadFinishTag();
                        //下载完成后把一些记录文件删除
                        for (int threadId = 0; threadId < threadCount; threadId++) {
                            File progressCacheFile = getCacheFile(updateInfos, threadId);
                            if (progressCacheFile.exists()) {
                                progressCacheFile.delete();//这是完整下载成功之后会执行删除缓存文件
                            }
                        }
                        if (progressFile.exists()) {
                            progress = 0;
                            progressFile.delete();
                        }

                        if (mOrviboUpdateDownloadListener != null) {
                            mHandler.sendEmptyMessage(OrviboUpdateAgent.WHAT_FINISH);
                            //  mOrviboCheckUpdateListener.onLoadFinsh();
                        }
                        if (bw_sum != null) {
                            try {
                                bw_sum.close();//
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            } catch (SocketException e) {
                //如果下载过程中网络断开,会捕捉到此异常,这个异常的断点数据可以保留
                current_state = STATE_PAUSE;
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                current_state = STATE_PAUSE;
                e.printStackTrace();
            } catch (Exception e) {
                current_state = STATE_DOWNLOAD_FAIL;
                if (mHandler != null) {
                    Message message = mHandler.obtainMessage();
                    message.what = WHAT_RESULT;
                    Bundle data = message.getData();
                    data.putInt(RESULT_KEY, ErrorCode.FAIL);
                    mHandler.sendMessage(message);
                }
                //   onDownloadFailDeleteFile();
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
//                    if (bw_sum != null) {
//                        bw_sum.close();
//                    } 如果有一个任务已经结束，在这里会提前关闭进度的输出流
                    if (bw != null) {
                        bw.close();
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

        }


    }

    /**
     * 设置当前状态
     *
     * @param state
     */
    public void setCurrenState(int state) {
        current_state = state;
    }

    /**
     * 获取当前状态
     */
    public int getCurrenState() {

        return current_state;

    }

    /**
     * 缓存当前的系统时间
     * 当缓存的文件超过一天时，缓存的数据无效
     */
    private void cacheCurrentSystemTime() {
        try {
            RandomAccessFile rws = new RandomAccessFile(getCacheTimeFile(), "rws");
            rws.seek(0);
            rws.write((System.currentTimeMillis() + "").getBytes());
            rws.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据任务编号生成对应的缓存文件 eg:101_1.text 防止多个版本使用同一个配置文件
     *
     * @param threadId
     * @return
     */
    private File getCacheFile(HashMap<String, String> userInfos, int threadId) {
        return new File(getSavePath(), userInfos.get(UpdateInfo.VERSION) + "_" + threadId + ".txt");
    }

    /**
     * 获取缓存时间的文件,如果这个文件没有了，则从头下载
     *
     * @return
     */
    private static File getCacheTimeFile() {
        return new File(getSavePath(), updateInfos.get(UpdateInfo.VERSION) + "_" + "cachetime.txt");
    }

    /**
     * apk安装完后时会调用
     *
     * @param version
     * @return
     */
    private static File getCacheTimeFile(int version) {
        return new File(getSavePath(), updateInfos.get(UpdateInfo.VERSION) + "_" + "cachetime.txt");
    }

    /**
     * 获取缓存进度的文件
     *
     * @return
     */
    private File getCacheProgressFile() {
        File progressFile = new File(getSavePath(), updateInfos.get(UpdateInfo.VERSION) + "_" + progressFilename);
        return progressFile;
    }

    /**
     * 获取缓存文件大小的文件
     *
     * @return
     */
    public static File getCacheContentLengthFile() {
        return new File(getSavePath(), updateInfos.get(UpdateInfo.VERSION) + "_" + "contentLength.txt");
    }

    public static File getCacheDownloadfinishFile() {
        return new File(getSavePath(), updateInfos.get(UpdateInfo.VERSION) + "_" + "finish.txt");
    }

    /**
     * apk安装完后时会调用
     *
     * @param version
     * @return
     */
    public static File getCacheDownloadfinishFile(int version) {
        return new File(getSavePath(), version + "_" + "finish.txt");
    }

    /**
     * 缓存下载完成标志
     */
    private void cacheDownloadFinishTag() {
        try {
            RandomAccessFile rws = new RandomAccessFile(getCacheDownloadfinishFile(), "rws");
            rws.seek(0);
            rws.write((1 + "").getBytes());
            rws.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得保存apk文件的文件对象
     * <p/>
     * 安装完后删除apk文件
     * 在点击下载的时候就已经生成该文件，文件的大小是服务端返回的大小
     *
     * @return
     */
    public File downloadedFile(HashMap<String, String> updateInfos) {
        if (updateInfos != null && updateInfos.size() > 0) {
            //        File outFile = new File(getSavePath(), getFileName(updateInfos.get(UpdateInfo.URL), Integer.parseInt(updateInfos.get(UpdateInfo.VERSION)))); //sdcard上的目标地址
            File pathFile = new File(getSavePath());
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            File outFile = new File(getSavePath(), getFileName(updateInfos.get(UpdateInfo.URL), Integer.parseInt(updateInfos.get(UpdateInfo.VERSION)))); //sdcard上的目标地址
            return outFile;
        }
        return null;
    }

    /**
     * 在启动页判断是否当前安装的版本是否与下载的版本是否一致
     *
     * @return
     */
    public static boolean isInstalled(int versionCode) {
        File apkFile = new File(getSavePath());
        if (apkFile.exists()) {
            File[] files = apkFile.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".apk")) {
                    String fileName = file.getName();
                    if (file.getName().startsWith((versionCode + "_"))) {
                        FindNewVersion.setIsNewVersion(mContext, false);
                        file.delete();
                        File cacheDownloadfinishFile = getCacheDownloadfinishFile(versionCode);
                        File cacheTimeFile = getCacheTimeFile(versionCode);
                        if (cacheTimeFile.exists()) {
                            cacheTimeFile.delete();
                        }
                        if (cacheDownloadfinishFile.exists()) {
                            cacheDownloadfinishFile.delete();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 安装apk
     *
     * @param mAppContext
     * @param apkFile
     */
    public void startInstall(Context mAppContext, File apkFile) {
        try {
            mAppContext.openFileOutput(apkFile.getName(), mAppContext.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // File file = new File("file://" + getSavePath() + getFileName(mApkUrl));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        mAppContext.startActivity(intent);

    }

    /**
     * 判断是否下载成功
     * 会生成一个finish.txt文件
     *
     * @return
     */
    public boolean downloadFinish() {
        File cacheDownloadfinishFile = getCacheDownloadfinishFile();
        File cacheTimeFile = getCacheTimeFile();
        boolean isFinish = false;
        if (cacheTimeFile.exists()) {
            if (cacheDownloadfinishFile.exists()) {
                BufferedReader bufferedReader = null;
                BufferedReader br_time = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(cacheDownloadfinishFile));
                    br_time = new BufferedReader(new FileReader(cacheTimeFile));
                    Long cacheTime = Long.parseLong(br_time.readLine());
                    if (cacheTime + druation > System.currentTimeMillis()) {
                        int finishTag = Integer.parseInt(bufferedReader.readLine());
                        return isFinish = finishTag == 1 ? true : false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (br_time != null) {
                            br_time.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 当下载的过程中断，又继续下载时，需要判断是否已经下载完成（断点一天有效）
     *
     * @return
     * @hide
     */
    private boolean isEqualsLength() {
        File cacheProgressFile = getCacheProgressFile();
        File cacheTimeFile = getCacheTimeFile();
        File cacheContentLengthFile = getCacheContentLengthFile();
        if (cacheTimeFile.exists()) {
            if (cacheProgressFile.exists()) {
                BufferedReader br_time = null;
                BufferedReader br_progress = null;
                BufferedReader br_contentLength = null;
                int cacheProgresss = 0;
                int contentLength = 0;
                try {

                    br_time = new BufferedReader(new FileReader(cacheTimeFile));
                    long cachetime = Long.parseLong(br_time.readLine());
                    if (cachetime + 24 * 60 * 60 * 1000 >= System.currentTimeMillis()) {
                        br_progress = new BufferedReader(new FileReader(cacheProgressFile));
                        br_contentLength = new BufferedReader(new FileReader(cacheContentLengthFile));
                        cacheProgresss = Integer.parseInt(br_progress.readLine());
                        contentLength = Integer.parseInt(br_contentLength.readLine());
                        if (cacheProgresss == contentLength) {
                            return true;
                        }

                    } else {
                        cacheProgressFile.delete();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br_time != null) {
                            br_time.close();
                        }
                        if (br_progress != null) {
                            br_progress.close();
                        }
                        if (br_contentLength != null) {
                            br_contentLength.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 升级的弹框提示（会根据再次显示的时间，如果又有新的版本再显示）
     *
     * @param activity
     * @param mHashMap
     */
    public void showOrviboUpdateTipsDialog(final Activity activity, HashMap<String, String> mHashMap) {
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

        showDialog(activity, forceUpdate, msg);
    }

    private void showDialog(final Activity activity, boolean forceUpdate, String msg) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        if (forceUpdate && NetUtil.isWifi(mContext)) {
            dialogFragmentTwoButton.setContent(activity.getString(R.string.about_update_force_content));
            dialogFragmentTwoButton.setCancelable(false);
        } else {
            dialogFragmentTwoButton.setTitle(activity.getString(R.string.about_update_tip));
            dialogFragmentTwoButton.setContent(msg);
            dialogFragmentTwoButton.setLeftButtonText(activity.getString(R.string.about_update_not_now));
        }

        dialogFragmentTwoButton.setRightButtonText(activity.getString(R.string.about_update_now));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {

            }

            @Override
            public void onRightButtonClick(View view) {
                if (updateInfos != null && updateInfos.size() > 0) {
                    File apkFile = mOrviboUpdateAgent.downloadedFile(updateInfos);

                    //下载的apk文件存在，apk文件下载完成，
                    //如果上一次下载的没有安装，finish.txt 文件就不会删除，但是apk文件已经删除(点击下载的时候已经生成，但是文件里的内容是空的)，这个时候已经满足安装条件，所以会报文件无法解析
                    if (apkFile != null && apkFile.exists() && apkFile.getName().startsWith(updateInfos.get(UpdateInfo.VERSION) + "_") && AppDownloadCache.getFinish(mContext)) {
                        mOrviboUpdateAgent.startInstall(mContext, apkFile);
                    } else {
                        mOrviboUpdateAgent.startDownload(mContext, updateInfos);
                    }
                }
            }
        });

        try {
            TimeCache.saveVersionTime(mContext);
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

        String msg;
        if (PhoneUtil.isCN(mContext)) {// 中文
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
            int cacheVersion = ServerVersion.getServerVersion(mContext);
            if (cacheVersion < serverVersion) {
                ServerVersion.setServerVersion(mContext, serverVersion);
            }
        }

        showDialog(activity, false, msg);
    }


    private void deleteFile() {
        File cacheDownloadfinishFile = getCacheDownloadfinishFile();
        File cacheProgressFile = getCacheProgressFile();
        File cacheTimeFile = getCacheTimeFile();
        File downloadedFile = downloadedFile(updateInfos);
        if (cacheDownloadfinishFile.exists()) {
            cacheDownloadfinishFile.delete();
        }
        if (cacheProgressFile.exists()) {
            cacheProgressFile.delete();
        }
        if (cacheTimeFile.exists()) {
            cacheTimeFile.delete();
        }
        if (downloadedFile.exists()) {
            downloadedFile.delete();
        }
        for (int i = 0; i < threadCount; i++) {
            File cacheFile = getCacheFile(updateInfos, i);
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
        }
    }

    /**
     * 一旦记录文件超过一天,从头开始下载；
     */
    private void onTimeOutOneDayToDeleteFile() {
        deleteFile();
    }


}
