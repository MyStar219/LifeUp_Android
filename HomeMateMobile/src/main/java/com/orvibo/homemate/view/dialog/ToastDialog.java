package com.orvibo.homemate.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.Constant;

/**
 * 可以设置延迟消失
 * Created by huangqiyao on 2015/4/20.
 */
public class ToastDialog {
    private static Dialog sDialog;
    private static TextView sContent_tv;
    private static int mDefaultDelayTime = 1500;

    /**
     * 得到进度对话框content_tv
     *
     * @param context
     * @return
     */
    public static Dialog getMyDialog(Context context) {
        if (sDialog == null) {
            sDialog = new Dialog(context, R.style.theme_dialog_alert);
            sDialog.setCanceledOnTouchOutside(false);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_toast, null);
            sContent_tv = (TextView) view.findViewById(R.id.content_tv);
            sDialog.setContentView(view);
        }
        return sDialog;
    }

    /**
     * @param content   提示内容
     * @param delayTime 延迟关闭时间，如果是Constant#INVALID_NUM则需要手动关闭，单位ms
     */
    public static void show(Context context, String content, int delayTime) {
        Activity activity = (Activity) context;
        if (activity != null && !activity.isFinishing()) {
            try {
                if (sDialog == null) {
                    sDialog = getMyDialog(context);
                } else {
                    // 创建进度对话框
                    sHandler.removeCallbacksAndMessages(null);
                    if (sDialog.isShowing()) {
                        sDialog.dismiss();
                    }
                }
                sContent_tv.setText(content);
                sDialog.show();
                if (delayTime != Constant.INVALID_NUM) {
                    sHandler.sendEmptyMessageDelayed(0, delayTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void show(Context context, String content) {
        Activity activity = (Activity) context;
        if (activity != null && !activity.isFinishing()) {
            try {
                if (sDialog == null) {
                    sDialog = getMyDialog(context);
                } else {
                    // 创建进度对话框
                    sHandler.removeCallbacksAndMessages(null);
                    if (sDialog.isShowing()) {
                        sDialog.dismiss();
                    }
                }
                sContent_tv.setText(content);
                sDialog.show();
                if (mDefaultDelayTime != Constant.INVALID_NUM) {
                    sHandler.sendEmptyMessageDelayed(0, mDefaultDelayTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismiss();
        }
    };

    public static void dismiss() {
        if (sDialog != null && sDialog.isShowing()) {
            sDialog.dismiss();
        }
    }

    /**
     * 置空dialog
     */
    public static void cancel() {
        dismiss();
        sDialog=null;
    }

}
