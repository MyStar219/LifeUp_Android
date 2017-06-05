package com.orvibo.homemate.device.smartlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

/**
 * 手势密码管理界面
 * Created by snown on 2016/03/29
 */
public class GestureManageActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener {

    private LinearLayout changeGesture;
    private LinearLayout forgetGesture;
    private Device device;
    private AuthUnlockData authUnlockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_manage);
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        authUnlockData = (AuthUnlockData) getIntent().getSerializableExtra(IntentKey.AUTH_UNLOCK_DATA);
        this.forgetGesture = (LinearLayout) findViewById(R.id.forgetGesture);
        forgetGesture.setOnClickListener(this);
        this.changeGesture = (LinearLayout) findViewById(R.id.changeGesture);
        changeGesture.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgetGesture:
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
                dialogFragmentTwoButton.setContent(getString(R.string.dialog_content_gesture_forget));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.login_again));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                    }

                    @Override
                    public void onRightButtonClick(View view) {
                        SmartLockCache.clearPwd();
                        ActivityTool.toLoginActivity(mContext, userName, Constant.MAIN_ACTIVITY, Constant.INVALID_NUM);
                        finish();
                    }
                });
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
            case R.id.changeGesture:
                Intent intent = new Intent(GestureManageActivity.this, GestureActivity.class);
                intent.putExtra("isChangePass", true);
                intent.putExtra(IntentKey.DEVICE, device);
                intent.putExtra(IntentKey.AUTH_UNLOCK_DATA, authUnlockData);
                startActivity(intent);
                break;
        }
    }

}

