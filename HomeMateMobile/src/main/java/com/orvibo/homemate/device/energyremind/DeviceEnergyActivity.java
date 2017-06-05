package com.orvibo.homemate.device.energyremind;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.SaveReminderFlag;
import com.orvibo.homemate.event.EnergyRemindEvent;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.EnergyReminderCache;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenu;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuCreator;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuItem;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuListView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 节能提示设备列表
 *
 * @author 熊文超
 */
public class DeviceEnergyActivity extends BaseActivity implements OnPropertyReportListener {
    private static final String TAG = DeviceEnergyActivity.class.getSimpleName();

    private SwipeMenuListView devicesEnergyListView;
    private DeviceEnergyAdapter deviceEnergyAdapter;
    private DeviceDao deviceDao;
    private DeviceStatusDao deviceStatusDao;
    private ArrayList<Device> devices;
    private ArrayList<Device> energyRemindDevices = new ArrayList<>();
    private ArrayList<Device> energyRemindDevicesBack = new ArrayList<>();
    private SwipeMenuCreator creator;
    private TextView energy_remind_close_all;
    private int energyRemindDevicesCount;

    private static final int WHAT_CLOSE_ALL = 0;
    private ConfirmAndCancelPopup mDeletePopup;
    private ControlDevice mControlDevice;

    private boolean isCloseAllDevices = false;

    /**
     * 关闭个设备的设备deviceId
     */
    private String curDeviceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy);
        init();
        initControl();
        PropertyReport.getInstance(mAppContext).registerPropertyReport(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void init() {
        deviceDao = new DeviceDao();
        deviceStatusDao = new DeviceStatusDao();
        devicesEnergyListView = (SwipeMenuListView) findViewById(R.id.devicesEnergyListView);
        energy_remind_close_all = (TextView) findViewById(R.id.energy_remind_close_all);
        energy_remind_close_all.setOnClickListener(this);
        LinearLayout emptyView = (LinearLayout) findViewById(R.id.empty_ll);
        devicesEnergyListView.setEmptyView(emptyView);

        creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red)));
                deleteItem.setWidth(getResources().getDimensionPixelSize(R.dimen.swipe_button_width));
                deleteItem.setTitle(getString(R.string.energy_remind_cancel));
                deleteItem.setTitleSize((int) (getResources().getDimensionPixelSize(R.dimen.text_normal) / getResources().getDisplayMetrics().scaledDensity));
                deleteItem.setTitleColor(getResources().getColor(R.color.white));
                menu.addMenuItem(deleteItem);
            }
        };

        devicesEnergyListView.setMenuCreator(creator);
        devicesEnergyListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                showCloseDevicePopup(position);
//                final Device device = energyRemindDevices.get(position);
//                device.setSaveReminderFalg(SaveReminderFlag.OFF);
//                List<Device> updDevices = new ArrayList<>();
//                updDevices.add(device);
//                deviceDao.updateDevices(mAppContext, updDevices);
//                refresh();
            }
        });
    }

    private void showCloseDevicePopup(int position) {
        final Device device = energyRemindDevices.get(position);
        device.setSaveReminderFalg(SaveReminderFlag.OFF);
        mDeletePopup = new ConfirmAndCancelPopup() {
            @Override
            public void confirm() {
                List<Device> updDevices = new ArrayList<>();
                updDevices.add(device);
                deviceDao.updateDevices(mAppContext, updDevices);
                mDeletePopup.dismiss();
                refresh();
            }
        };
        mDeletePopup.showPopup(mContext, String.format(getString(R.string.energy_remind_delete_content), device.getDeviceName()), getString(R.string.confirm), getString(R.string.cancel));
    }

    private void initControl() {
        // 控制回调
        mControlDevice = new ControlDevice(mAppContext) {

            @Override
            public void onControlDeviceResult(String uid, String deviceId,
                                              int result) {
                LogUtil.d(TAG, "onControlDeviceResult()-uid:" + uid + ",deviceId:" + deviceId + ",result:" + result);
                if (isFinishingOrDestroyed()) {
                    return;
                }
//                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    //  stopProgress();
                    ToastUtil.toastError(result);
                }
            }
        };
        //关闭整个rgbw灯的标志位
        mControlDevice.setForAllDevice(true);
    }

    private void refresh() {
        energyRemindDevices.clear();
        devices = (ArrayList) deviceDao.selNeedCloseZigbeeLampsDevices(currentMainUid);
        DeviceSort.sortDevices(devices, null, false, false, false);
        for (Device device : devices) {
            String uid = device.getUid();
            //   String deviceId = device.getDeviceId();
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(uid, device);
            if (deviceStatus != null && EnergyReminderCache.getEnergyReminder(mAppContext, currentMainUid) == SaveReminderFlag.ON) {
                if (deviceStatus.getValue1() == DeviceStatusConstant.ON && deviceStatus.getOnline() == OnlineStatus.ONLINE) {
                    energyRemindDevices.add(device);
                }
            }
        }
        if (deviceEnergyAdapter == null) {
            deviceEnergyAdapter = new DeviceEnergyAdapter(mAppContext, this, energyRemindDevices);
            devicesEnergyListView.setAdapter(deviceEnergyAdapter);
        } else {
            deviceEnergyAdapter.notifyDataSetChanged();
        }
        if (energyRemindDevices.size() == 0) {
            energy_remind_close_all.setVisibility(View.GONE);
            stopProgress();
        } else {
            energy_remind_close_all.setVisibility(View.VISIBLE);
        }
        boolean isEnergyRemindTime = DateUtil.isEnergyRemindTime();
        if (isEnergyRemindTime) {
            EventBus.getDefault().post(new EnergyRemindEvent());
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        showProgress();
        if (vId == R.id.action_off_tv) {
            Device device = (Device) v.getTag();
//            showDialog();
            isCloseAllDevices = false;
            curDeviceId = device.getDeviceId();

//            DeviceControlApi.deviceClose(device.getUid(), device.getDeviceId(), 0, this);
//            mControlDevice.stopControl();
            mControlDevice.off(device.getUid(), device.getDeviceId());
        } else if (vId == R.id.energy_remind_close_all) {
//            showDialog();
            curDeviceId = null;
            isCloseAllDevices = true;
            energyRemindDevicesBack.clear();
            energyRemindDevicesBack.addAll(energyRemindDevices);
            energyRemindDevicesCount = energyRemindDevicesBack.size();
            if (energyRemindDevicesCount > 0) {
                if (mHandler.hasMessages(WHAT_CLOSE_ALL)) {
                    mHandler.removeMessages(WHAT_CLOSE_ALL);
                }
                mHandler.sendEmptyMessage(WHAT_CLOSE_ALL);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_CLOSE_ALL) {
                energyRemindDevicesCount--;
                Device device = energyRemindDevicesBack.get(energyRemindDevicesCount);
                mControlDevice.off(device.getUid(), device.getDeviceId());
                if (energyRemindDevicesCount > 0) {
                    if (mHandler.hasMessages(WHAT_CLOSE_ALL)) {
                        mHandler.removeMessages(WHAT_CLOSE_ALL);
                    }
                    mHandler.sendEmptyMessageDelayed(WHAT_CLOSE_ALL, 100);
                } else {
                    stopProgress();
                }
            }
            return true;
        }
    });

//    @Override
//    public void onResultReturn(BaseEvent baseEvent) {
//        if (isFinishingOrDestroyed()) {
//            return;
//        }
//        dismissDialog();
//        int result = baseEvent.getResult();
//        if (result != ErrorCode.SUCCESS) {
//            ToastUtil.toastError(result);
//        }
//    }


    @Override
    public void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        //关闭单个设备
        if (!isCloseAllDevices && !TextUtils.isEmpty(curDeviceId) && curDeviceId.equals(deviceId)) {
            stopProgress();
        }
        refresh();
    }

//    @Override
//    public void onDeviceOOReport(String uid, String deviceId, int online) {
//        refresh();
//    }

    @Override
    public void rightTitleClick(View v) {
        Intent intent = new Intent(DeviceEnergyActivity.this, DeviceEnergySettingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        PropertyReport.getInstance(mAppContext).unregisterPropertyReport(this);
        if (mControlDevice != null) {
            mControlDevice.stopControl();
        }
        super.onDestroy();
    }


}
