package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.PopupWindowUtil;

/**
 * loading对话框
 * 
 * @author smagret
 * 
 */
public class LoadingPopup {
	private static PopupWindow popup;

	/**
	 * 
	 * @param context
	 * @param message
	 *            显示的内容
	 */
	public static void showPopup(Activity context, String message) {
		dismiss();
		View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog,
				null);
		TextView content_tv = (TextView) view.findViewById(R.id.tipTextView);
		content_tv.setText(message);

		ImageView spaceshipImage = (ImageView) view.findViewById(R.id.img);
		// 加载动画
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				context, R.anim.loading_animation);
		// 使用ImageView显示动画
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);

		int[] piex = AppTool.getScreenPixels(context);
		int w = piex[0] * 480 / 640;
		popup = new PopupWindow(view, w, LinearLayout.LayoutParams.WRAP_CONTENT);
		PopupWindowUtil.initPopup(popup,
				context.getResources().getDrawable(R.color.popup_bg), 1);
		popup.showAtLocation(view, Gravity.CENTER, 0, 0);
	}

	public static void dismiss() {
		PopupWindowUtil.disPopup(popup);
	}

	public boolean isShowing() {
		if (popup != null && popup.isShowing()) {
			return true;
		}
		return false;
	}
}
