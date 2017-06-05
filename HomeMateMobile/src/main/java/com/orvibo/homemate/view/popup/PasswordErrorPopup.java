package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.PopupWindowUtil;

/**
 * 对话框
 * 
 * @author smagret
 * 
 */
public abstract class PasswordErrorPopup extends CommonPopup{
	private PopupWindow popup;
    private TextView tvForgetPassword;

	/**
	 * 
	 * @param context
	 * @param message
	 *            显示的内容
	 * @param yes
	 *            确定按钮显示的文字
	 */
	public void showPopup(Activity context, String message, String yes) {
		dismiss();
		View view = LayoutInflater.from(context).inflate(R.layout.password_error_popup,
				null);
		TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
		content_tv.setText(message);
		TextView yes_tv = (TextView) view.findViewById(R.id.yes_tv);
		yes_tv.setText(yes);

        tvForgetPassword = (TextView) view.findViewById(R.id.tvForgetPassword);
        tvForgetPassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                forgetPassword();
            }
        });

		yes_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				confirm();
			}
		});

//		int[] piex = AppTool.getScreenPixels(context);
//		int w = piex[0] * 480 / 640;
		popup = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		PopupWindowUtil.initPopup(popup,
				context.getResources().getDrawable(R.color.popup_bg), 1);
		popup.showAtLocation(view, Gravity.CENTER, 0, 0);
	}

	public void dismiss() {
		PopupWindowUtil.disPopup(popup);
	}

	/**
	 * 
	 * @param context
	 * @param message
	 *            显示的内容
	 * @param yes
	 *            确定按钮显示的文字
	 */
	public void showPopup(Context context, int message, int yes) {
		dismiss();
		View view = LayoutInflater.from(context).inflate(R.layout.single_button_popup,
				null);
		TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
		content_tv.setText(message);
		TextView yes_tv = (TextView) view.findViewById(R.id.yes_tv);
		yes_tv.setText(yes);

		view.findViewById(R.id.yes_tv).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						confirm();
					}
				});

		popup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		PopupWindowUtil.initPopup(popup,
				context.getResources().getDrawable(R.color.popup_bg), 1);
		try {
			popup.showAtLocation(view, Gravity.CENTER, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isShowing() {
		if (popup != null && popup.isShowing()) {
			return true;
		}
		return false;
	}

	public void cancel() {
		dismiss();
	}

	/**
	 * 点击确定按钮
	 */
	public void confirm() {
	}

    public abstract void forgetPassword() ;

}
