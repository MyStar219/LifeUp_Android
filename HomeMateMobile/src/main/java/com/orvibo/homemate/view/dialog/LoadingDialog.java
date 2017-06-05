//package com.homemate.cloud.view;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.homemate.cloud.R;
//import com.homemate.cloud.data.Constant;
//
///**
// * 可以设置延迟消失
// * Created by smagret on 2015/9/16.
// */
//public class LoadingDialog {
//    private static Dialog sDialog;
//    private static TextView sContent_tv;
//    private static int mDefaultDelayTime = 1500;
//
//    /**
//     * 得到进度对话框content_tv
//     *
//     * @param context
//     * @return
//     */
//    public static Dialog getMyDialog(Context context) {
//        if (sDialog == null) {
//            sDialog = new Dialog(context, R.style.loading_dialog);
//            sDialog.setCanceledOnTouchOutside(false);
//
//            View v = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);// 得到加载view
//            LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
//            // main.xml中的ImageView
//            ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
//            sContent_tv = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
//            // 加载动画
//            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
//                    context, R.anim.loading_animation);
//            // 使用ImageView显示动画
//            spaceshipImage.startAnimation(hyperspaceJumpAnimation);
//            sDialog.setContentView(layout, new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
//        }
//        return sDialog;
//    }
//
//    /**
//     * @param content   提示内容
//     * @param delayTime 延迟关闭时间，如果是Constant#INVALID_NUM则需要手动关闭，单位ms
//     */
//    public static void showToast(Context context, String content, int delayTime) {
//        if (sDialog == null) {
//            sDialog = getMyDialog(context);
//        } else {
//            // 创建进度对话框
//            sHandler.removeCallbacksAndMessages(null);
//            if (sDialog.isShowing()) {
//                sDialog.dismiss();
//            }
//        }
//        sContent_tv.setText(content);
//        sDialog.showToast();
//        if (delayTime != Constant.INVALID_NUM) {
//            sHandler.sendEmptyMessageDelayed(0, delayTime);
//        }
//    }
//
//    public static void showToast(Context context, String content) {
//        if (sDialog == null) {
//            sDialog = getMyDialog(context);
//        } else {
//            // 创建进度对话框
//            sHandler.removeCallbacksAndMessages(null);
//            if (sDialog.isShowing()) {
//                sDialog.dismiss();
//            }
//        }
//        sContent_tv.setText(content);
//        sDialog.showToast();
//        if (mDefaultDelayTime != Constant.INVALID_NUM) {
//            sHandler.sendEmptyMessageDelayed(0, mDefaultDelayTime);
//        }
//    }
//
//    private static Handler sHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            dismiss();
//        }
//    };
//
//    public static void dismiss() {
//        if (sDialog != null && sDialog.isShowing()) {
//            sDialog.dismiss();
//        }
//    }
//}
