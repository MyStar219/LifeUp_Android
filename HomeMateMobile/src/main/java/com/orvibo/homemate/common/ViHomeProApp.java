package com.orvibo.homemate.common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;
import com.evideo.voip.sdk.EVVoipAccount;
import com.evideo.voip.sdk.EVVoipCall;
import com.evideo.voip.sdk.EVVoipManager;
import com.hzy.tvmao.KookongSDK;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.kookong.app.data.api.IrData;
import com.onesignal.OneSignal;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.apatch.FileUtils;
import com.orvibo.homemate.common.apatch.HandleApatch;
import com.orvibo.homemate.common.apatch.PatchConfig;
import com.orvibo.homemate.common.apatch.apatchinterface.OnDownAllPatchSuccessListener;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.StringUtil;
import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.weiju.InCallActivity;
import com.umeng.socialize.PlatformConfig;
import com.videogo.constant.Config;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.videogo.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.ImageLoaderConfiguration;
import com.videogo.universalimageloader.core.assist.QueueProcessingType;
import com.videogo.universalimageloader.core.decode.BaseImageDecoder;
import com.videogo.universalimageloader.core.download.BaseImageDownloader;
import com.yolanda.nohttp.NoHttp;

import java.io.File;
import java.util.List;

import static com.smartgateway.app.weiju.InCallActivity.evVoipCall;

//import com.squareup.leakcanary.LeakCanary;

//import com.squareup.leakcanary.LeakCanary;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by huangqiyao on 2015/5/14 22:16.
 *
 * @version v
 */
public class ViHomeProApp extends ViHomeApplication {
    private static final String TAG = ViHomeProApp.class.getSimpleName();
    private static Context context;
    public static final String WX_APP_ID = "wxcb71456524ccb8b1";

    public static final String APP_KEY_KUKONG = "28EC337CAF8FB0C42E4ABF8BDCBEA5B7";//"FDE3F0415E3444D9BBB62927BD049B94";

    public static List<IrData> irDatas;//数据会有时比较多，全局化传输，再进行清理

    private InComingHandler inComingHandler;
    public static EVVoipAccount evVoipAccount;

    //补丁管理类
    private PatchManager mPatchManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate started");

        // Init OneSignal
        OneSignal.startInit(this).init();

        if (isHomeMateProcess()) {
            Log.d(TAG, "HM onCreate started");

            super.onCreate();
            Iconify.with(new FontAwesomeModule());

            context = getApplicationContext();
            ViHomeApplication.context = context;
            int appVersionCode = AppTool.getAppVersionCode(context);
//            String appVersionName = AppTool.getAppVersionName(context);

            //LeakCanary.install(this);
            /**
             *  由于调试的时候去加载发布版的补丁可能造成未知错误，故该补丁加载逻辑最好在发布版放开，在开发调试的时候应该屏蔽
             */
            try {
                mPatchManager = new PatchManager(this);
                mPatchManager.init(appVersionCode + "");
                //先去加载工程目录下的补丁
                mPatchManager.loadPatch();
                //再去请求网络补丁
                HandleApatch handleApatch = new HandleApatch(appVersionCode + "");
                handleApatch.downApatchList(getApplicationContext());
                handleApatch.setOnDownAllPatchSuccessListener(new OnDownAllPatchSuccessListener() {
                    @Override
                    public void allSuccess(String filePath) {
                        addApatch(filePath);
                    }

                    @Override
                    public void downFail() {

                    }
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            EVVoipManager.init(getApplicationContext(), new EVVoipManager.OnInitCallback() {
                @Override
                public void complete() {
                    Log.d("weiju_sdk", "VoipSDK start set income callback");
                    inComingHandler = new InComingHandler();
                    EVVoipManager.setIncomingCallback(inComingHandler);
                }

                @Override
                public void error(Throwable throwable) {
                    Log.d("weiju_sdk", "VoipSDK error while setting income callback " + throwable.getLocalizedMessage());
                }

            });

            String userName = UserCache.getCurrentUserName(this);
            if (!StringUtil.isEmpty(userName)) {
                String md5Password = UserCache.getMd5Password(this, userName);
                if (!StringUtil.isEmpty(md5Password)) {
                    UserCache.setLoginStatus(this, userName, LoginStatus.FAIL);
                } else {
                    UserCache.setLoginStatus(this, userName, LoginStatus.NO_USER);
                }
            } else {
                UserCache.setLoginStatus(this, userName, LoginStatus.NO_USER);
            }
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.setCustomCrashHanler(context);
//            boolean isForeground = AppTool.isAppOnForeground(this);
//            String brand = Build.BRAND;
//            String model = Build.MODEL;
//            LogUtil.d(TAG, "onCreate()-isForeground:" + isForeground + ",version:v" + appVersionName + "_" + appVersionCode + ",phone:" + brand + "_" + model);
            //如果app不处于前台，就把他finish--service已保留，不再关闭service
//        if (!isForeground) {
//            LogUtil.e(TAG, "onCreate()-App has been destroy.");
//            AppTool.exitApp(this);
//            Intent intent = new Intent(this, TranActivity.class);
//            intent.putExtra("toast", false);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            System.exit(0);
//        }
        }
    }

    public void initServices() {
        Log.v("InitServices On", "ViHomeProApp");
        Config.LOGGING = true;
        EZOpenSDK.initLib(this, Constant.EZ_APP_KEY, "");
        KookongSDK.init(this, APP_KEY_KUKONG);
        KookongSDK.setDebugMode(false);
        // 程序启动时，需要对Danale SDK进行初始化
//        Danale.initialize(getApplicationContext(), ApiType.VIDEO, ManufacturerCode.DANALE.getCode());
        initImageloder();
        initPlatformConfig();
        NoHttp.initialize(this);
//        refWatcher = LeakCanary.install(this);

        new HomeMateHelper().startInit();
    }

    private class InComingHandler implements EVVoipManager.IncomingCallback{

        @Override
        public void inComing(EVVoipCall call) {
            EVVoipAccount account = call.getRemoteAccount();
            Log.d("weiju_sdk", "in incoming handler for: " + (account != null ? account.getUsername() : "") + " code:" + call.hashCode());

            if(evVoipCall == null){
                evVoipCall = call;
                Intent callIntent = new Intent(getApplicationContext(), InCallActivity.class);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }else{
                //TODO 呼入多路通话，根据需求自行处理，同一时间内只允许接听一个通话
            }
        }

    }
//    private RefWatcher refWatcher;
//
//    public static RefWatcher getRefWatcher(Context context) {
//        ViHomeProApp application = (ViHomeProApp) context.getApplicationContext();
//        return application.refWatcher;
//    }

    public static Context getContext() {
        return context;
    }

    private void initImageloder() {
        //创建默认的ImageLoader配置参数
       /* ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);*/

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .imageDecoder(new BaseImageDecoder()) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()
                .build();


        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }

    private void initPlatformConfig() {
        com.umeng.socialize.Config.IsToastTip = false;
        com.umeng.socialize.Config.dialogSwitch = false;
        //微信 appid appsecret
        PlatformConfig.setWeixin(WX_APP_ID, "7a4c0b776b4223eadc407a8a70aec7cc");
        //  String wechat_appid="wxcb71456524ccb8b1";
        //  mIWXAPI = WXAPIFactory.createWXAPI(this, wechat_appid, true);
        //   mIWXAPI.registerApp(wechat_appid);
        //新浪微博 appkey appsecret
        PlatformConfig.setSinaWeibo("3753653405", "80857ca3fc9d9fb0e3fda6c242fd8b24");
        // QQ和Qzone appid appkey
        //公共账号
        //PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba");
        //自己的账号
        PlatformConfig.setQQZone("1104800362", "TkXTN2LDh7nvdsY3");
    }

    /**
     * 先把下载的补丁加载到工程包下，然后删除下载的补丁
     *
     * @param filePath
     */
    private void addApatch(String filePath) {
        try {
            if (StringUtil.isEmpty(filePath)) {
                return;
            }
            File mfile = new File(filePath);
            File[] files = mfile.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    try {
                        if (file.getName().endsWith(PatchConfig.SUFFIX)) {
                            mPatchManager.addPatch(file.getPath());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                //删除操作
                FileUtils.delete(mfile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
