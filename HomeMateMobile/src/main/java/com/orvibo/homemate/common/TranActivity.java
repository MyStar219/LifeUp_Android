package com.orvibo.homemate.common;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.smartgateway.app.R;

/**
 * 检测到资源被回收时调整到此activity
 *
 * 透明无标题activity
 * Created by huangqiyao on 2015/6/25.
 */
public class TranActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getBooleanExtra("toast",false)) {
            Toast.makeText(this, R.string.crash_toast, Toast.LENGTH_LONG).show();
        }
//        ToastUtil.showToast(R.string.crash_toast);
        finish();
       // System.exit(0);
        System.out.println("Goodbye HomeMate.");
    }
}
