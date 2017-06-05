package com.orvibo.homemate.util;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.PopupWindow;

import com.smartgateway.app.R;

public class PopupWindowUtil {

	/**
	 * 初始化popupwindow
	 * 
	 * @param popupWindow
	 * @param bgDrawable
	 *            背景图片
	 * @return
	 */
	public static void initPopup(PopupWindow popupWindow, Drawable bgDrawable) {
		if (bgDrawable != null) {
			// 设置背景，背景一定要设置，不会触摸菜单外区域时菜单不会消失
			popupWindow.setBackgroundDrawable(bgDrawable);
		}
		// popupWindow.setBackgroundDrawable(bgDrawablecontext.getResources()
		// .getDrawable(R.drawable.black_bg));
		// 触摸菜单外区域时菜单消失
		popupWindow.setOutsideTouchable(true);

		// 使用自定义的动画
		popupWindow.setAnimationStyle(R.style.popup_anim);
		// 使用系统的动画
		// popupWindow.setAnimationStyle(android.R.style.Animation);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
	}

	/**
	 * 初始化popupwindow
	 * 
	 * @param popupWindow
	 * @param bgDrawable
	 *            背景图片
	 * @param anim
	 *            0 使用自定义的动画，由上往下显示，1自定义的动画，打开时由小到大，关闭时由

大到小
	 * @return
	 */
	public static void initPopup(PopupWindow popupWindow, Drawable bgDrawable,
			int anim) {
		if (bgDrawable != null) {
			// 设置背景，背景一定要设置，不会触摸菜单外区域时菜单不会消失
			popupWindow.setBackgroundDrawable(bgDrawable);
		}
		// popupWindow.setBackgroundDrawable(bgDrawablecontext.getResources()
		// .getDrawable(R.drawable.black_bg));
		// 触摸菜单外区域时菜单消失
		popupWindow.setOutsideTouchable(true);

		if (anim == 0) {
			popupWindow.setAnimationStyle(R.style.top_to_bottom_anim);
		} else if (anim == 1) {
			popupWindow.setAnimationStyle(R.style.popup_scale_anim);
		} else if (anim == 2) {
			popupWindow.setAnimationStyle(R.style.bottom_to_top_anim);
		} else {
			popupWindow.setAnimationStyle(android.R.style.Animation);
		}
		// 使用自定义的动画
		// popupWindow.setAnimationStyle(R.style.popup_anim);
		// 使用系统的动画
		// popupWindow.setAnimationStyle(android.R.style.Animation);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
	}

	/**
	 * 初始化popupwindow
	 *
	 * @param popupWindow
	 * @param bgDrawable
	 *            背景图片
	 * @param anim
	 *            0 使用自定义的动画，由上往下显示，1自定义的动画，打开时由小到大，关闭时由

大到小
	 * @return
	 */
	public static void initPopup(PopupWindow popupWindow, Drawable bgDrawable,
			int anim,boolean isFocus) {
		if (bgDrawable != null) {
			// 设置背景，背景一定要设置，不会触摸菜单外区域时菜单不会消失
			popupWindow.setBackgroundDrawable(bgDrawable);
		}
		// popupWindow.setBackgroundDrawable(bgDrawablecontext.getResources()
		// .getDrawable(R.drawable.black_bg));
		// 触摸菜单外区域时菜单消失
		popupWindow.setOutsideTouchable(true);
		//popupWindow.setOutsideTouchable(isFocus);

		if (anim == 0) {
			popupWindow.setAnimationStyle(R.style.top_to_bottom_anim);
		} else if (anim == 1) {
			popupWindow.setAnimationStyle(R.style.popup_scale_anim);
		} else if (anim == 2) {
			popupWindow.setAnimationStyle(R.style.bottom_to_top_anim);
		} else {
			popupWindow.setAnimationStyle(android.R.style.Animation);
		}
		// 使用自定义的动画
		// popupWindow.setAnimationStyle(R.style.popup_anim);
		// 使用系统的动画
		// popupWindow.setAnimationStyle(android.R.style.Animation);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(isFocus);
	}

	public static void initPopup(PopupWindow popupWindow, Context context) {
		popupWindow.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.bg_toast_not_operate));
		// 触摸菜单外区域时菜单消失
		popupWindow.setOutsideTouchable(true);

		// 使用自定义的动画
		// popupWindow.setAnimationStyle(R.style.popup_anim);
		// 使用系统的动画
		// popupWindow.setAnimationStyle(android.R.style.Animation);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
	}

	public static void disPopup(PopupWindow popupWindow) {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	public static void dismiss(Handler mHandler, final PopupWindow popupWindow) {
		if (popupWindow != null && popupWindow.isShowing() && mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					popupWindow.dismiss();
				}
			});
		}
	}

}
