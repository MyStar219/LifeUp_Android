//package com.orvibo.homemate.view;
//
//import android.app.Activity;
//import android.content.Context;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
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
// * @author huangqiyao
// *
// */
//public class DeletePopup {
//	private PopupWindow popup;
//
//	/**
//	 *
//	 * @param context
//	 * @param message
//	 *            显示的内容
//	 * @param yes
//	 *            确定按钮显示的文字
//	 * @param no
//	 *            取消按钮显示的文字
//	 */
//	public void showPopup(Activity context, String message, String yes,
//			String no) {
//		dismiss();
//		View view = LayoutInflater.from(context).inflate(R.layout.delete_popup,
//				null);
//		TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
//		content_tv.setText(message);
//		TextView yes_tv = (TextView) view.findViewById(R.id.yes_tv);
//		yes_tv.setText(yes);
//		TextView no_tv = (TextView) view.findViewById(R.id.no_tv);
//		no_tv.setText(no);
//
//		yes_tv.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				confirm();
//			}
//		});
//
//		no_tv.setOnClickListener(
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						dismiss();
//						cancel();
//					}
//				});
//		int[] piex = AppTool.getScreenPixels(context);
//		int w = piex[0] * 500 / 640;
//		popup = new PopupWindow(view, w, LinearLayout.LayoutParams.WRAP_CONTENT);
//		PopupWindowUtil.initPopup(popup,
//				context.getResources().getDrawable(R.drawable.bg_toast_has_operate), 1);
//		popup.showAtLocation(view, Gravity.CENTER, 0, 0);
//	}
//
//	public void dismiss() {
//		PopupWindowUtil.disPopup(popup);
//	}
//
//	/**
//	 *
//	 * @param context
//	 * @param message
//	 *            显示的内容
//	 * @param yes
//	 *            确定按钮显示的文字
//	 * @param no
//	 *            取消按钮显示的文字
//	 */
//	public void showPopup(Context context, int message, int yes, int no) {
//		dismiss();
//		View view = LayoutInflater.from(context).inflate(R.layout.delete_popup,
//				null);
//		TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
//		content_tv.setText(message);
//		TextView yes_tv = (TextView) view.findViewById(R.id.yes_tv);
//		yes_tv.setText(yes);
//		TextView no_tv = (TextView) view.findViewById(R.id.no_tv);
//		no_tv.setText(no);
//
//		view.findViewById(R.id.yes_tv).setOnClickListener(
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						confirm();
//					}
//				});
//
//		view.findViewById(R.id.no_tv).setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				dismiss();
//				cancel();
//			}
//		});
//
//		popup = new PopupWindow(view, -1, -1);
//		PopupWindowUtil.initPopup(popup,
//				context.getResources().getDrawable(R.drawable.bg_toast_has_operate), 1);
//		try {
//			popup.showAtLocation(view, Gravity.CENTER, 0, 0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public boolean isShowing() {
//		if (popup != null && popup.isShowing()) {
//			return true;
//		}
//		return false;
//	}
//
//	public void cancel() {
//		dismiss();
//	}
//
//	/**
//	 * 点击确定按钮
//	 */
//	public void confirm() {
//	}
//}
