package com.orvibo.homemate.common.crash;

import android.content.Context;
import android.content.Intent;

import com.orvibo.homemate.common.TranActivity;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;


/**
 * 自定义系统的Crash捕捉类，用Toast替换系统的对话框 将软件版本信息，设备信息，出错信息保存在sd卡中，你可以上传到服务器中
 *
 * @author xiaanming
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    private Context mContext;
    private static CrashHandler mInstance = new CrashHandler();

    private CrashHandler() {
    }

    /**
     * 单例模式，保证只有一个CustomCrashHandler实例存在
     *
     * @return
     */
    public static CrashHandler getInstance() {
        return mInstance;
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        // 将一些信息保存到SDcard中
        // savaInfoToSD(context, ex);
        LogUtil.e(TAG, "uncaughtException()-异常");
        if (ex != null) {
            ex.printStackTrace();
        }
//        Intent intent = new Intent(Intent.ACTION_MAIN);
////        Intent intent = new Intent(mContext, CrashActivity.class);
//       // intent.putExtra("error", FormatStackTrace(ex));
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
////                | Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);
//        System.exit(0);// 去掉的话会卡屏

        //出现异常后退到home桌面并关闭app
        AppTool.exitApp(mContext);
        Intent intent = new Intent(mContext, TranActivity.class);
        intent.putExtra("toast", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        System.exit(0);
    }

    /**
     * 为我们的应用程序设置自定义Crash处理
     */
    public void setCustomCrashHanler(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 将异常信息转化为String
     *
     * @param throwable
     * @return
     */
    private String FormatStackTrace(Throwable throwable) {
        if (throwable == null)
            return "";
        String rtn = throwable.getStackTrace().toString();
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            writer.flush();
            rtn = writer.toString();
            printWriter.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
        }
        return rtn;
    }

}