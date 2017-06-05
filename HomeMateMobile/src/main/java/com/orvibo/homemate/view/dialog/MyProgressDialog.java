package com.orvibo.homemate.view.dialog;



import android.app.Dialog;
import android.content.Context;
import android.os.Handler;

import com.smartgateway.app.R;



public class MyProgressDialog {
	private static String TAG = "MyDialog";

	/**
	 * 得到进度对话框
	 * 
	 * @param context
	 * @return
	 */
	public static Dialog getMyDialog(Context context) {
		Dialog progDialog = new Dialog(context, R.style.theme_dialog_alert);
		progDialog.setContentView(R.layout.progress_dialog);
		return progDialog;
	}

	/**
	 * 显示进度对话框
	 * 
	 * @param context
	 * @param progDialog
	 */
	public void showProgressDialog(Context context, Dialog progDialog) {
		// 创建进度对话框
		if (progDialog == null) {
			progDialog = new Dialog(context, R.style.theme_dialog_alert);
			progDialog.setContentView(R.layout.progress_dialog);
		} else {
			if (progDialog.isShowing()) {
				progDialog.dismiss();
			}
			progDialog.show();

		}
	}

	/**
	 * 显示进度对话框
	 * 
	 * @param context
	 * @param progDialog
	 */
	public static void show(Context context, Dialog progDialog) {
		// 创建进度对话框
		if (context != null && progDialog != null) {
			if (progDialog.isShowing()) {
				progDialog.dismiss();
			}
			progDialog.show();
		}
	}

	/**
	 * 显示进度对话框
	 * 
	 * @param context
	 * @param progDialog
	 */
	public static void show(Context context, final Dialog progDialog,
			Handler mHandler) {
		// 创建进度对话框
		if (context != null && progDialog != null && mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (progDialog.isShowing()) {
						progDialog.dismiss();
					}
					progDialog.show();
				}
			});
		}
	}

	/**
	 * 消失进度对话框
	 * 
	 * @param progDialog
	 */
	public void dismissProgressDialog(Dialog progDialog) {
		if (progDialog != null && progDialog.isShowing()) {
			progDialog.dismiss();
		}
	}

	/**
	 * 消失进度对话框
	 * 
	 * @param progDialog
	 */
	public static void dismiss(Dialog progDialog) {
		if (progDialog != null && progDialog.isShowing()) {
			progDialog.dismiss();
		}
	}

	public static void dismiss(final Dialog progDialog, Handler mHandler) {
	
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
				
					if (progDialog != null && progDialog.isShowing()) {
					
						progDialog.dismiss();
					} 
				}
			});
		} 
	}

}
