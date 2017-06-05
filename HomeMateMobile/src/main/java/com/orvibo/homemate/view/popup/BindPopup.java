package com.orvibo.homemate.view.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.smartgateway.app.R;

public class BindPopup {
	private Context mContext;

	private PopupWindow mPopup;
	private AnimationDrawable mProgressAnim;

	public BindPopup(Context context) {
		this.mContext = context;

	}

	@SuppressLint("InflateParams")
	public void show() {
		View contentView = LayoutInflater.from(mContext).inflate(
				R.layout.bind_popup, null);
		ImageView iv = (ImageView) contentView.findViewById(R.id.progress_iv);
		mProgressAnim = (AnimationDrawable) iv.getDrawable();
		mProgressAnim.start();
		mPopup = new PopupWindow(contentView,
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		mPopup.setFocusable(false);
		mPopup.setTouchable(false);
		mPopup.showAtLocation(contentView, Gravity.CENTER, 0, 0);
	}

	public void dismiss() {
		if (isShowing()) {
			mPopup.dismiss();
		}
		if (mProgressAnim != null) {
			mProgressAnim.stop();
		}
	}

	public boolean isShowing() {
		return mPopup != null && mPopup.isShowing();
	}
}
