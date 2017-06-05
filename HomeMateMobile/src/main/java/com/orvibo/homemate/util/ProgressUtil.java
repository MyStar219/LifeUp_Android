package com.orvibo.homemate.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @author smagret
 * 
 */
public class ProgressUtil {

	private ProgressDialog progressDialog;

	public ProgressUtil(Context context) {
		// 创建ProgressDialog对象
		progressDialog = new ProgressDialog(context);
	}

	public void showProgress(String title, String content, int resId) {

		// 设置进度条风格，风格为圆形，旋转的
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 标题
		progressDialog.setTitle(title);
		// 设置ProgressDialog 提示信息
		progressDialog.setMessage(content);
		// 设置ProgressDialog 标题图标
		progressDialog.setIcon(resId);
		// 设置ProgressDialog 的进度条是否不明确
		progressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		progressDialog.setCancelable(true);
		// 让ProgressDialog显示
		progressDialog.show();
	}

	public void dismissProgress() {

		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
}
