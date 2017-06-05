//package com.orvibo.homemate.view;
//
//import android.app.Activity;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.util.AppTool;
//import com.orvibo.homemate.util.PopupWindowUtil;
//
///**
// * 对话框
// *
// * @author smagret
// */
//public class SimplePopup {
//    private PopupWindow popup;
//
//    /**
//     * @param context
//     * @param message 显示的内容
//     */
//    public void showPopup(Activity context, String message) {
//        dismiss();
//        View view = LayoutInflater.from(context).inflate(R.layout.simple_popup,
//                null);
//        TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
//        content_tv.setText(message);
//
//        int[] piex = AppTool.getScreenPixels(context);
//        int w = piex[0] * 500 / 640;
//        popup = new PopupWindow(view, w, LinearLayout.LayoutParams.WRAP_CONTENT);
//        PopupWindowUtil.initPopup(popup,
//                context.getResources().getDrawable(R.drawable.bg_toast_not_operate), 1);
//        popup.setOutsideTouchable(false);
//        popup.setTouchable(false);
//        //setPopupWindowTouchModal(popup,true);
//        //popup.update();
//
//        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                SimplePopup.this.onDismiss();
//            }
//        });
//        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
//    }
//
//    protected void onDismiss() {
//
//    }
//
//    public void dismiss() {
//        PopupWindowUtil.disPopup(popup);
//    }
//
//    /**
//     * @param context
//     * @param message 显示的内容
//     */
//    public void showPopup(Activity context, int message) {
//        dismiss();
//        View view = LayoutInflater.from(context).inflate(R.layout.simple_popup,
//                null);
//        TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
//        content_tv.setText(context.getResources().getString(message));
//
//
//        int[] piex = AppTool.getScreenPixels(context);
//        int w = piex[0] * 500 / 640;
//        popup = new PopupWindow(view, w, LinearLayout.LayoutParams.WRAP_CONTENT);
//        PopupWindowUtil.initPopup(popup,
//                context.getResources().getDrawable(R.drawable.bg_toast_not_operate), 1);
//        popup.setOutsideTouchable(false);
//        popup.setTouchable(false);
//        popup.update();
//
//        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                SimplePopup.this.onDismiss();
//            }
//        });
//        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
//    }
//
//    public boolean isShowing() {
//        if (popup != null && popup.isShowing()) {
//            return true;
//        }
//        return false;
//    }
//
//    public void cancel() {
//        dismiss();
//    }
//
//    /**
//     * 点击确定按钮
//     */
//    public void confirm() {
//    }
//
////    /**
////     * Set whether this window is touch modal or if outside touches will be sent
////     * to
////     * other windows behind it.
////     *
////     */
////    public static void setPopupWindowTouchModal(PopupWindow popupWindow,
////                                                boolean touchModal) {
////        if (null == popupWindow) {
////            return;
////        }
////        Method method;
////        try {
////
////            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
////                    boolean.class);
////            method.setAccessible(true);
////            method.invoke(popupWindow, touchModal);
////
////        }
////        catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//}
