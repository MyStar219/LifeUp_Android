package com.orvibo.homemate.device.allone2.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.allone2.irlearn.RemoteLearnActivity;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import de.greenrobot.event.EventBus;

/**
 * Created by yuwei on 2016/4/14.
 * 添加自定义和复制遥控器界面
 */
public class RemoteNameAddActivity extends BaseControlActivity {

    private NavigationCocoBar nbTitle;
    private EditTextWithCompound deviceName;
    private Button nextButton;

    private int mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_nickname);

        initView();
        initListener();
    }

    private void initView() {
        nbTitle = (NavigationCocoBar) findViewById(R.id.navigationBar);
        deviceName = (EditTextWithCompound) findViewById(R.id.userNicknameEditText);
        deviceName.setNeedRestrict(false);
        deviceName.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        nextButton = (Button) findViewById(R.id.saveButton);
        nbTitle.setCenterText(getString(R.string.device_set_add_remote));
        deviceName.setHint(R.string.DEVICE_NAME_IR_NULL);
        nextButton.setText(R.string.next);

        mDeviceType = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, -1);
    }

    private void initListener() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断网络是否连接
                if (!NetUtil.isNetworkEnable(RemoteNameAddActivity.this)) {
                    ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
                    return;
                }
                String deviceNameStr = deviceName.getText().toString().trim();
                if (StringUtil.isEmpty(deviceNameStr)) {
                    ToastUtil.showToast(R.string.device_set_input_devicename_hint);
                    return;
                }
                if (StringUtil.isHasSpecialChar(deviceNameStr)) {
                    ToastUtil.showToast(R.string.SPECIAL_CHAR_ERROR);
                    return;
                }
                copyRemoteControl(uid, userName, deviceNameStr, mDeviceType, null, String.valueOf(System.currentTimeMillis() / 1000), deviceId, "-1");
            }
        });
    }

    private void copyRemoteControl(String uid, String userName, String deviceName, int deviceType, String roomId, String irDeviceId, String deviceId, String company) {
        showDialog();
        DeviceApi.createDevice(userName, uid, deviceType, deviceName, roomId, irDeviceId, deviceId, company, new BaseResultListener.DataListener() {
            @Override
            public void onResultReturn(BaseEvent baseEvent, Object data) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                        ToastUtil.showToast(getString(R.string.allone_device_add) + getString(R.string.SUCCESS));
                        Intent intent = new Intent(RemoteNameAddActivity.this, RemoteLearnActivity.class);
                        intent.putExtra(IntentKey.DEVICE, (Device) data);
                        intent.putExtra(IntentKey.IS_START_LEARN, true);
                        startActivity(intent);
                        //添加成功后发送结束添加activity事件
                        EventBus.getDefault().post(new ActivityFinishEvent());
                        finish();
                    } else {
                        ToastUtil.toastError(baseEvent.getResult());
                    }
                } else {
                    ToastUtil.toastError(baseEvent.getResult());
                }
            }
        });
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     *
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }
}
