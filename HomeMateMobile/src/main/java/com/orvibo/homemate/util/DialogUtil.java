package com.orvibo.homemate.util;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.orvibo.homemate.device.sg.DialogFragmentOneButton2;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;

/**
 * Created by snown on 2016/5/23.
 *
 * @描述: 弹框，对话框等辅助类
 */
public class DialogUtil {
    private static Context context = ViHomeProApp.getContext();

    private ProgressDialogFragment progressDialogFragment;

    private final int SHOWDIALOG = 1;

    FragmentManager fragmentManager;

    private static DialogUtil ourInstance = new DialogUtil();

    public static DialogUtil getInstance() {
        return ourInstance;
    }

    private DialogUtil() {
    }

    /**
     * 设置coco离线弹框提示
     *
     * @param fragmentManager
     */
    public static void showCocoOffline(FragmentManager fragmentManager) {
        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
        dialogFragmentOneButton.setTitle(context.getString(R.string.how_to_fix_title));
        dialogFragmentOneButton.setContent(context.getString(R.string.how_to_fix_content));
        dialogFragmentOneButton.show(fragmentManager, "");
    }

    public static void showCocoOfflineSupport(android.support.v4.app.FragmentManager fragmentManager) {
        DialogFragmentOneButton2 dialogFragmentOneButton = new DialogFragmentOneButton2();
        dialogFragmentOneButton.setTitle(context.getString(R.string.how_to_fix_title));
        dialogFragmentOneButton.setContent(context.getString(R.string.how_to_fix_content));
        dialogFragmentOneButton.show(fragmentManager, "");
    }


    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case SHOWDIALOG:
                    progressDialogFragment.show(fragmentManager, null);
                    break;
            }
        }
    };


    /**
     * 延迟1S显示进度对话框
     */
    public void showDialog(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        showDialog(null, context.getString(R.string.loading));
    }

    public void showDialog(ProgressDialogFragment.OnCancelClickListener onCancelClickListener, String content) {
        dismissDialog();
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setOnCancelClickListener(onCancelClickListener);
        progressDialogFragment.setContent(content);
        if (mHandler.hasMessages(SHOWDIALOG)) {
            mHandler.removeMessages(SHOWDIALOG);
        }
        mHandler.sendEmptyMessageDelayed(SHOWDIALOG, 1000);
    }

    /**
     * 关闭进度对话款
     */
    public void dismissDialog() {
        if (mHandler.hasMessages(SHOWDIALOG)) {
            mHandler.removeMessages(SHOWDIALOG);
        }
        if (progressDialogFragment != null) {
            try {
                progressDialogFragment.dismissAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
