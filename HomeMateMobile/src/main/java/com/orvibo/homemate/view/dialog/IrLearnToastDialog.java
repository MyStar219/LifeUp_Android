package com.orvibo.homemate.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 可以设置延迟消失
 * Created by huangqiyao on 2015/4/20.
 */
public abstract class IrLearnToastDialog implements DialogInterface.OnDismissListener {
    private static final String TAG = IrLearnToastDialog.class.getSimpleName();
    private Dialog sDialog;
    private TextView sContent_tv;
    private static int mDefaultDelayTime = 15 * 1000;
    private Context mContext;
    private String keyName;

    private int countDownTime = 60;
    private Timer timer;

    /**
     * 倒计时
     */
    private final int COUNTDOWN_MSG = 1;

    public IrLearnToastDialog(Context context) {
        mContext = context;
    }

    /**
     * 得到进度对话框content_tv
     *
     * @param context
     * @return
     */
    public Dialog getMyDialog(Context context) {
        sDialog = new Dialog(context, R.style.theme_dialog_alert);
        sDialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_toast, null, false);
        sContent_tv = (TextView) view.findViewById(R.id.content_tv);
        sDialog.setContentView(view);
        sDialog.setOnDismissListener(this);
        return sDialog;
    }

    public void show(Context context, String keyName) {
        if (sDialog == null) {
            sDialog = getMyDialog(context);
        } else {
            // 创建进度对话框
            sHandler.removeCallbacksAndMessages(null);
            if (sDialog.isShowing()) {
                sDialog.dismiss();
            }
        }
        this.keyName = keyName;
        String message = mContext.getResources().getString(R.string.press_remote_key);
        String finalMessage = String.format(message, keyName, mDefaultDelayTime / 1000 + "");
        sContent_tv.setText(finalMessage);
        sDialog.setCanceledOnTouchOutside(false);
        sDialog.show();
        setTimer();
    }

    private Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COUNTDOWN_MSG:
                    // 设置发送验证码按钮倒计时
                    String message = mContext.getResources().getString(R.string.press_remote_key);
                    String finalMessage = String.format(message, keyName, countDownTime + "");
                    sContent_tv.setText(finalMessage);
                    LogUtil.d(TAG, "handleMessage()- countDownTime = " + countDownTime);
                    if (countDownTime == 0) {
                        dismiss();
                        String failMessage = mContext.getResources().getString(R.string.ir_learn_fail);
                        ToastUtil.showToast(
                                failMessage,
                                ToastType.ERROR, ToastType.SHORT);
                    }
                    break;
            }
        }
    };

    private void setTimer() {
        countDownTime = mDefaultDelayTime / 1000 + 1;
        if (timer == null) {
            timer = new Timer();
        }
        LogUtil.d(TAG, "setTimer()- timer = " + timer);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                countDownTime--;
                if (countDownTime >= 0) {
                    sHandler.sendEmptyMessage(COUNTDOWN_MSG);
                }
            }
        }, 0, 1000);
    }

    private void cancleTimer() {
        LogUtil.d(TAG, "cancleTimer()- timer = " + timer);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void dismiss() {
        try {
            if (sDialog != null && sDialog.isShowing()) {
                sDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void stopLearn();

    public boolean isShowing() {
        if (sDialog != null && sDialog.isShowing()) {
            return true;
        }
        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        LogUtil.d(TAG, "onDismiss()");
        cancleTimer();
        if (sHandler.hasMessages(COUNTDOWN_MSG)) {
            sHandler.removeMessages(COUNTDOWN_MSG);
        }
        stopLearn();
    }

}
