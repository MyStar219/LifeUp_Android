package com.orvibo.homemate.device.smartlock;

import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 远程开锁界面
 * Created by snown on 2016/6/2.
 */
public class UnlockActivity extends BaseControlActivity {

    private NavigationGreenBar navigationGreenBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_door);
        this.navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationGreenBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            navigationGreenBar.setText(device.getDeviceName());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unlock:
                showDialog();
                DeviceControlApi.unlock(uid, deviceId, 0, new BaseResultListener() {
                    @Override
                    public void onResultReturn(BaseEvent baseEvent) {
                        dismissDialog();
                        if (baseEvent.isSuccess()) {
                            ToastUtil.showToast(R.string.lock_opened);
                        } else {
                            ToastUtil.toastError(baseEvent.getResult());
                        }
                    }
                });
                break;
        }
    }


}
