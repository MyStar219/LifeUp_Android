//package com.orvibo.homemate.device.manage.add;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.common.BaseActivity;
//import com.orvibo.homemate.common.MainActivity;
//import com.orvibo.homemate.data.IntentKey;
//import com.orvibo.homemate.view.popup.AddDevicePopup;
//
//
//public class AddDeviceFailActivity extends BaseActivity {
//    // view
//    private AddDevicePopup mAddDevicePopup;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_device_fail);
//        //mAddDevicePopup = new AddDevicePopup(mContext);
//        final boolean isToMainActivity = getIntent().getBooleanExtra(IntentKey.TO_MAIN, false);
//        mAddDevicePopup = new AddDevicePopup(mAppContext) {
//            @Override
//            protected void onSetDevices() {
//                if (isToMainActivity) {
//                    startActivity(new Intent(mContext, MainActivity.class));
//                }
//                super.onSetDevices();
//                finish();
//            }
//        };
//    }
//
//    public void tryAgain(View v) {
//        if (mAddDevicePopup.isShowing()) {
//            mAddDevicePopup.dismiss();
//        }
//        mAddDevicePopup.show(false);
//    }
//
//    @Override
//    protected void onDestroy() {
//        mAddDevicePopup.dismiss();
//        super.onDestroy();
//    }
//
//}