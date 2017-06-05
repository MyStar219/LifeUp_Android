package com.orvibo.homemate.common.crash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.HttpUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示用户出现异常，是否将异常信息发送给服务器和重启APP
 *
 * @author huangqiyao
 */
public class CrashActivity extends Activity {
    private final String TAG = CrashActivity.class.getSimpleName();
    // private final String errReportUrl =
    // "http://wifisocket.orvibo.com:10003/ErrorMessage";// 发送错误报告url
    // wifisocket.orvibo.com/ErrorMessage！execute.action
    // private final String errReportUrl =
    // "http://wifisocket.orvibo.com/ErrorMessage";// 发送错误报告url
    private final String errReportUrl = "http://wifisocket.orvibo.com/jsp/errormsg.jsp";// 发送错误报告url

    private String error = null;
    private static boolean isReportCrash = false;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int msgWhat = msg.what;
            if (msgWhat == 1) {
                LogUtil.i(TAG, "提交错误信息成功");
            } else if (msgWhat == 2) {
                LogUtil.e(TAG, "提交错误报告失败");
            } else if (msgWhat == 3) {
                LogUtil.e(TAG, "连接到服务器提交数据后返回码非200");
            } else if (msgWhat == 4) {
                LogUtil.e(TAG, "未能连接到服务器提交数据");
            } else if (msgWhat == 5) {
                LogUtil.e(TAG, "连接到服务器但超时");
            }
            restart(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        try {
            error = getIntent().getStringExtra("error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        isReportCrash = true;// 默认是将错误发送到服务器
    }

    /**
     * 将异常发送给服务器
     *
     * @param v
     */
    public void reportCrash(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked()) {
            LogUtil.d(TAG, "发送异常信息到服务器");
            isReportCrash = true;
        } else {
            LogUtil.d(TAG, "不发送异常信息到服务器");
            isReportCrash = false;
        }
    }

    /**
     * 重启APP
     *
     * @param v
     */
    public void restartViHome(View v) {
        LogUtil.d(TAG, "重启APP");
        if (isReportCrash) {// 发送错误信息到服务器
            // 显示进度对话框
            // LogUtil.d(TAG, "发送错误信息到服务器");
            new ReportCrash().start();
        } else {// 直接重启APP
            // LogUtil.d(TAG, "没有选择发送错误信息到服务器");
            restart(false);
        }
    }

    private class ReportCrash extends Thread {
        @Override
        public void run() {
            LogUtil.d(TAG, "在线程中发送异常数据到服务器");
            handler.sendEmptyMessage(HttpUtil.HttpClientDoPost(getErrInfoMap(),
                    errReportUrl));
        }
    }

    private void restart(boolean isFinish) {
        LogUtil.e(TAG, "restart()-isFinish:" + isFinish);
        Intent i = getPackageManager().getLaunchIntentForPackage(
                getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(IntentKey.FINISH, isFinish);
        startActivity(i);
        finish();
        System.exit(0);
    }

    // 返回错误信息map
    private Map<String, String> getErrInfoMap() {
        // String appName = intent.getStringExtra("appName");
        String model = "phoneModel:" + android.os.Build.MODEL;
        String phoneCompany = "company:" + android.os.Build.MANUFACTURER;
        String SDK = "SDK:" + android.os.Build.VERSION.SDK_INT;
        int[] temp = PhoneUtil.getScreenPixels(CrashActivity.this);
        String piexl = "piexl:" + temp[0] + " x " + temp[1];

        String appVersion = AppTool.getAppVersionName(CrashActivity.this);
        String content = model + "\n" + phoneCompany + "\n" + SDK + "\n"
                + piexl;
        content = content + "\n" + error;
        LogUtil.d(TAG, "发送给服务器的异常信息content:\n" + content);

        Map<String, String> map = new HashMap<String, String>();
        map.put("appName", "HomeMate");
        // map.put("appName",
        // CrashActivity.this.getResources().getString(R.string.app_name));
        map.put("version", appVersion);
        map.put("errorMessage", content);

        // 打印错误信息
        //LogUtil.d(TAG, map.toString());
        return map;
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
