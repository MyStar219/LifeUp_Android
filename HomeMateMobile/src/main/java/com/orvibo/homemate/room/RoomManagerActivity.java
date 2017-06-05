package com.orvibo.homemate.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;

/**
 * @author Smagret
 * @data 2015/03/28
 */
public class RoomManagerActivity extends BaseActivity {
	private static final String TAG = RoomManagerActivity.class
			.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_manager);
	}
	
	public void set(View v) {
		Intent intent = new Intent(RoomManagerActivity.this,
				SetFloorAndRoomActivity.class);
		mContext.startActivity(intent);
	}

}
