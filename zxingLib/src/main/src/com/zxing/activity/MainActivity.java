package com.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zxinglib.R;

public class MainActivity extends Activity{
	
	private final int CAPTURE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivityForResult(intent, CAPTURE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CAPTURE){
			if(data != null){
				Bundle bundle = data.getExtras();
				if(bundle != null){
					final String result = bundle.getString("result");
					Log.v("zxing", "result:" + result);
				}
			}
		}
	}
}
