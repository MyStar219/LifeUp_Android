package com.orvibo.homemate.messagepush;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.device.energyremind.DeviceEnergySettingActivity;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.SensorTimerPush;
import com.orvibo.homemate.model.TimerPush;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smagret on 2015/8/21.
 */
public class MessageSettingActivity extends BaseActivity implements AdapterView.OnItemClickListener, DialogFragmentTwoButton.OnTwoButtonClickListener {
    private static final String TAG = MessageSettingActivity.class.getSimpleName();
    private NavigationGreenBar navigationGreenBar;
    private ListView devicesInfoPushSetListView;
//    private TextView clearTextView;
    private MessageSettingAdapter messageSettingAdapter;
    private DeviceDao deviceDao;
    private TimerPush timerPush;
    private SensorTimerPush sensorTimerPush;
    private ArrayList<Device> zigbeeDevices;
    private LinearLayout deviceEmptyLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_setting_activity);
        init();
    }

    private void init() {
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationBar);
        devicesInfoPushSetListView = (ListView) findViewById(R.id.devicesInfoPushSetListView);
        deviceEmptyLinearLayout = (LinearLayout) findViewById(R.id.deviceEmptyLinearLayout);
        devicesInfoPushSetListView.setEmptyView(deviceEmptyLinearLayout);
        devicesInfoPushSetListView.setOnItemClickListener(this);
        deviceDao = new DeviceDao();
        initTimerPush();
//        initTopTitle();
    }

    @Override
    protected void onResume() {
        refreshDeviceInfoPushSet();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private void initTopTitle() {
//        String currentMainUid = UserCache.getCurrentMainUid(mContext);
//        if (currentMainUid == null) {
//            navigationGreenBar.setText(getResources().getString(R.string.personal_message_setting));
//        } else {
//            navigationGreenBar.setText(getResources().getString(R.string.message_setting_vicenter300));
//        }
//    }

    public void refreshDeviceInfoPushSet() {
        zigbeeDevices = (ArrayList) deviceDao.selZigbeeLampsDevices(UserCache.getCurrentMainUid(mContext));
        boolean isLogin = UserManage.getInstance(mAppContext).isLogined();
        LogUtil.d(TAG, "refreshDeviceInfoPushSet isLogin" + isLogin);
        if (isLogin) {
            //处于登录状态
            new AsyncTask<Void, Void, List<Device>>() {
                @Override
                protected List<Device> doInBackground(Void... params) {
                    //修复了子账户也能设置问题
                    String userId = UserCache.getCurrentUserId(mAppContext);
                    LogUtil.d(TAG, "refreshDeviceInfoPushSet userId:" + userId);
                    return deviceDao.selMessageSettingDevicesByUserId(userId);
                }

                @Override
                protected void onPostExecute(List<Device> devices) {
                    LogUtil.d(TAG, "refreshDeviceInfoPushSet devices:" + devices);

                    messageSettingAdapter = new MessageSettingAdapter(mContext, MessageSettingActivity.this, devices, zigbeeDevices);
                    devicesInfoPushSetListView.setAdapter(messageSettingAdapter);

                }
            }.execute();
        } else {
            ToastUtil.showToast(R.string.NOT_LOGIN_ERROR);
            List<Device> devices = new ArrayList<Device>();
            messageSettingAdapter = new MessageSettingAdapter(mContext, MessageSettingActivity.this, devices, zigbeeDevices);
            devicesInfoPushSetListView.setAdapter(messageSettingAdapter);
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Settings_Back), null);
        super.onBackPressed();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);

        refreshDeviceInfoPushSet();
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.clearTextView) {
//            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
//            dialogFragmentTwoButton.setTitle(getString(R.string.message_clear_confirm));
//            dialogFragmentTwoButton.setUpButtonText(getString(R.string.message_clear));
//            dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
//            dialogFragmentTwoButton.setDownButtonText(getString(R.string.cancel));
//            dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
//            dialogFragmentTwoButton.show(getFragmentManager(), "");
//        } else {
            showDialog();
            MessagePush messagePush = (MessagePush) v.getTag();
            if (messagePush.getType() == MessagePushType.All_TIMER_TYPE) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Settings_All), null);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    timerPush.startSetAllDeviceTimerPush(MessagePushStatus.ON);
                } else {
                    timerPush.startSetAllDeviceTimerPush(MessagePushStatus.OFF);
                }
            } else if (messagePush.getType() == MessagePushType.SINGLE_TIMER_TYPE) {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Settings_About), null);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    timerPush.startSetDeviceTimerPush(messagePush.getTaskId(), MessagePushStatus.ON);
                } else {
                    timerPush.startSetDeviceTimerPush(messagePush.getTaskId(), MessagePushStatus.OFF);
                }
            } else if (messagePush.getType() == MessagePushType.ALL_SENSOR_TYPE) {
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    sensorTimerPush.startSetAllDeviceTimerPush(MessagePushStatus.ON);
                } else {
                    sensorTimerPush.startSetAllDeviceTimerPush(MessagePushStatus.OFF);
                }
            }
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Object tag = view.getTag();
        if (position == messageSettingAdapter.getCount() -1 && messageSettingAdapter.isShowEnergyRemind()) {
            //节能提醒
//            Device device = (Device) tag;
//            if (!StringUtil.isEmpty(device.getDeviceId())) {
//                Intent intent = new Intent(mContext, SensorMessageSettingActivity.class);
//                intent.putExtra(Constant.DEVICE, device);
//                startActivity(intent);
//            } else {
//                Intent intent = new Intent(mContext, DeviceEnergySettingActivity.class);
//                startActivity(intent);
//            }
//        } else {
            Intent intent = new Intent(mContext,DeviceEnergySettingActivity.class);
            startActivity(intent);
        }
    }

    public void initTimerPush() {
        timerPush = new TimerPush(mContext) {
            @Override
            public void onTimerPushResult(int result) {
                LogUtil.d(TAG, "onTimerPushResult() - result：" + result);
                if (result == 0) {
                    refreshDeviceInfoPushSet();
                } else {
                    ToastUtil.toastError(result);
                }
                dismissDialog();
            }

            @Override
            public void onAllSetTimerPushResult(int result) {
                LogUtil.d(TAG, "onAllSetTimerPushResult() - result：" + result);
                if (result == 0) {
                    refreshDeviceInfoPushSet();
                } else {
                    ToastUtil.toastError(result);
                }
                dismissDialog();
            }
        };

        sensorTimerPush = new SensorTimerPush(mContext) {
            @Override
            public void onSensorTimerPushResult(int result,int type) {
                LogUtil.d(TAG, "onSensorTimerPushResult() - result：" + result);
                if (result == 0) {
                    refreshDeviceInfoPushSet();
                } else {
                    ToastUtil.toastError(result);
                }
                dismissDialog();
            }

            @Override
            public void onAllSensorSetTimerPushResult(int result) {
                LogUtil.d(TAG, "onAllSensorSetTimerPushResult() - result：" + result);
                if (result == 0) {
                    refreshDeviceInfoPushSet();
                } else {
                    ToastUtil.toastError(result);
                }
                dismissDialog();
            }
        };
    }

    @Override
    public void onLeftButtonClick(View view) {
        new MessageDao().delMessagesByUserId(UserCache.getCurrentUserId(mAppContext));
        finish();
    }

    @Override
    public void onRightButtonClick(View view) {

    }
}
