//package com.orvibo.homemate;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//
///**
// * @author Smagret
// * @data 2015/03/28
// */
//public class BindingFailActivity extends Activity {
//
//	private static final String TAG = BindingFailActivity.class.getSimpleName();
//	private Context mContext;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.binding_fail);
//		init();
//	}
//
//	private void init() {
//		mContext = BindingFailActivity.this;
//		initView();
//		initListener();
//	}
//
//	private void initView() {
//	}
//
//	private void initListener() {
//
//	}
//
//	public void binding(View v) {
//		Intent intent = new Intent(BindingFailActivity.this,
//				BindingActivity.class);
//		mContext.startActivity(intent);
//	}
//
//	@Override
//	protected void onResume() {
//		// TODO Auto-generated method stub
//		super.onResume();
//	}
//
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//	}
//
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//	}
//
//}
