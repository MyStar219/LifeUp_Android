package com.orvibo.homemate.device.manage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.messagepush.MessageActivity;
import com.orvibo.homemate.messagepush.SensorStatusRecordActivity;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 新传感器接收到报警推送时弹出该activity
 * Created by wenchao on 2016/2/26.
 */
public class SensorDialogActivity extends BaseActivity implements DialogInterface.OnCancelListener{
    private DialogFragmentTwoButton dialogFragmentTwoButton;
    private ControlDevice controlDevice;
    private List<InfoPushMsg> infoPushMsgs = new ArrayList<>();
    private DeviceDao deviceDao;
    private int num=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        num=1;
        deviceDao = new DeviceDao();
        Intent intent = getIntent();
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        infoPushMsgs.add(infoPushMsg);
        dealPushSensorMessage();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        num++;
        if(num>2)
        num=1;
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        infoPushMsgs.add(0, infoPushMsg);
        if (infoPushMsgs.size() > 10) {
            infoPushMsgs = infoPushMsgs.subList(0, 10);
        }
        dealPushSensorMessage();
    }

    private void dealPushSensorMessage() {
        if (dialogFragmentTwoButton != null) {
            dialogFragmentTwoButton.dismiss();
        }
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setOnCancelListener(this);
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.gray));
        boolean moreThenOneDevice = false;
        String deviceId = infoPushMsgs.get(0).getPushPropertyReportInfo().getDeviceId();
        for (int i = 1; i < infoPushMsgs.size(); i++) {
            String id = infoPushMsgs.get(i).getPushPropertyReportInfo().getDeviceId();
            if (!deviceId.equals(id)) {
                moreThenOneDevice = true;
                break;
            }
        }
        if (!moreThenOneDevice) {
            final InfoPushMsg infoPushMsg = infoPushMsgs.get(0);
            if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.SMOKE_SENSOR) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_smoke_title));
            } else if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.CO_SENSOR) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_co_title));
            } else if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.FLAMMABLE_GAS) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_flammable_gas_title));
            } else if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.WATER_SENSOR) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_water_title));
            } else if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.SOS_SENSOR) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_sos_title));
            } else if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.INFRARED_SENSOR) {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_infr_title));
            } else {
                dialogFragmentTwoButton.setTitle(getString(R.string.sensor_door_title));
            }
            String content = "";
            for (InfoPushMsg msg: infoPushMsgs) {
                content += msg.getText() + "\n";
            }
            dialogFragmentTwoButton.setContent(content);
            if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.SMOKE_SENSOR) {
                if(num==1){
                    dialogFragmentTwoButton.setLeftButtonText(getString(R.string.tv_silence));
                }else{
                    dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                }

            } else{
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            }
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    if (infoPushMsg.getPushPropertyReportInfo().getDeviceType() == DeviceType.SMOKE_SENSOR) {
                        if(num==1){
                            dealSmokeLeftButtonClick(infoPushMsg);
                        }else{
                            finish();
                        }

                    } else {
                        finish();
                    }
                }

                @Override
                public void onRightButtonClick(View view) {
                    toSensorActivity(infoPushMsg);
                }
            });
        } else {
            dialogFragmentTwoButton.setTitle(getString(R.string.sensor_alarm));
            String content = "";
            for (InfoPushMsg infoPushMsg: infoPushMsgs) {
                content += infoPushMsg.getText() + "\n";
            }
            dialogFragmentTwoButton.setContent(content);
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    finish();
                }

                @Override
                public void onRightButtonClick(View view) {
                    toAllMessageActivity();
                }
            });
        }
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.sensor_to_message));
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    private void dealSmokeLeftButtonClick(final InfoPushMsg infoPushMsg) {
        if (dialogFragmentTwoButton != null) {
            dialogFragmentTwoButton.dismiss();
        }
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setOnCancelListener(this);
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.gray));
        dialogFragmentTwoButton.setTitle(getString(R.string.sensor_smoke_tip_title));
        dialogFragmentTwoButton.setContent(getString(R.string.sensor_smoke_tip_content));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.tv_silence));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
                finish();
            }

            @Override
            public void onRightButtonClick(View view) {
                if (controlDevice == null) {
                    initControlDevice();
                }
                controlDevice.mute(UserCache.getCurrentMainUid(mAppContext), infoPushMsg.getPushPropertyReportInfo().getDeviceId());
                finish();
            }
        });
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    private void initControlDevice() {
        controlDevice = new ControlDevice(mAppContext) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.sensor_silent_success);
                }
            }
        };
    }

    private void toSensorActivity(final InfoPushMsg infoPushMsg) {
        Device device = deviceDao.selDevice(infoPushMsg.getPushPropertyReportInfo().getDeviceId());
       // Intent intent = new Intent(SensorDialogActivity.this, SensorMessageActivity.class);
        Intent intent = new Intent(SensorDialogActivity.this, SensorStatusRecordActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
        finish();
    }

    private void toAllMessageActivity() {
        Intent intent = new Intent(SensorDialogActivity.this, MessageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    protected void onDestroy() {
        infoPushMsgs.clear();
        super.onDestroy();
    }
}
