package com.orvibo.homemate.device.bind;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;

/**
 * @author Smagret
 * @data 2015/03/28
 */
public class BindingSussessActivity extends Activity {

	private static final String TAG = BindingSussessActivity.class
			.getSimpleName();
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.binding_success);
		init();
	}

	private void init() {
		mContext = BindingSussessActivity.this;
		initView();
		initListener();
	}

	private void initView() {
	}

	private void initListener() {

	}

	public void rightTitleClick(View v) {
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
