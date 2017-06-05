package com.orvibo.homemate.device.manage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginType;
import com.orvibo.homemate.device.manage.adapter.DeviceSetAdapter;
import com.orvibo.homemate.device.manage.add.AddVicenterTipActivity;
import com.orvibo.homemate.device.manage.edit.SceneDeviceEditActivity;
import com.orvibo.homemate.device.manage.edit.SensorDeviceEditActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理列表，显示所有设备
 */
public class DeviceManageActivity extends BaseActivity implements AdapterView.OnItemClickListener, OOReport.OnDeviceOOReportListener {
    private static final String TAG = DeviceManageActivity.class.getSimpleName();
    private static final int WHAT_REFRESH = 1;
    private static final String KEY_DEVICES = "devices";
    private static final String KEY_DEVICESTATUSES = "deviceStatuses";
    private DeviceSetAdapter mDeviceSetAdapter;
    private DeviceDao mDeviceDao;

    private boolean toSearchDevice;
    private DeviceStatusDao mDeviceStatusDao;
    private Handler mHandler;

    private FrequentlyModeDao frequentlyModeDao;
    private List<FrequentlyMode> frequentlyModes = new ArrayList<FrequentlyMode>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);
        mDeviceStatusDao = new DeviceStatusDao();
        toSearchDevice = getIntent().getBooleanExtra(IntentKey.SEARCH_DEVICE, false);

        mDeviceDao = new DeviceDao();
        frequentlyModeDao = new FrequentlyModeDao();

        OOReport.getInstance(mAppContext).registerOOReport(this);
        initHandler();
        //绑定界面进入到设备管理界面，自动搜索设备
        if (toSearchDevice) {
            addDevice(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refreshDeviceList();
        new Thread(refreshDevicesRunnable).start();
        LogUtil.d(TAG, "onResume()");

    }

    /**
     * 添加设备
     *
     * @param v
     */
    public void addDevice(View v) {
        //go to add vicenter device
        startActivity(new Intent(mContext, AddVicenterTipActivity.class));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = (Device) view.getTag(R.id.tag_device);
        LogUtil.d(TAG, "onItemClick()-device:" + device);
        if (device == null) {
            ToastUtil.toastError(ErrorCode.DATA_NOT_EXIST);
            return;
        }
        Intent intent;
        if (device.getDeviceType() == DeviceType.INFRARED_SENSOR ||
                device.getDeviceType() == DeviceType.MAGNETIC ||
                device.getDeviceType() == DeviceType.MAGNETIC_WINDOW ||
                device.getDeviceType() == DeviceType.MAGNETIC_DRAWER ||
                device.getDeviceType() == DeviceType.MAGNETIC_OTHER ||
                device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
            intent = new Intent(mContext, SensorDeviceEditActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            startActivity(intent);
        } else if (device.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD ||
                device.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD ||
                device.getDeviceType() == DeviceType.SCENE_KEYPAD) {
            intent = new Intent(mContext, SceneDeviceEditActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            startActivity(intent);
        } else if (device.getDeviceType() == DeviceType.CURTAIN_PERCENT || device.getDeviceType() == DeviceType.ROLLER_SHADES_PERCENT) {
            frequentlyModes = frequentlyModeDao.selFrequentlyModes(device.getDeviceId());
            if (frequentlyModes.size() != 0) {
                intent = new Intent(this, PercentCurtainSetDeviceActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            } else {
                intent = new Intent(mContext, SetDeviceActivity.class);
                intent.putExtra("device", device);
                intent.putExtra("floorRoomName", view.getContentDescription() + "");
                startActivityForResult(intent, 0);
            }
        } else {
            intent = new Intent(mContext, SetDeviceActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra("floorRoomName", view.getContentDescription() + "");
            startActivityForResult(intent, 0);
        }
    }

    public void learnIr(View view) {
        Device device = (Device) view.getTag();
        LogUtil.d(TAG, "learnIr()-device:" + device);
        Intent intent = new Intent(mContext, SetDeviceActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        intent.putExtra("floorRoomName", view.getContentDescription() + "");
        startActivityForResult(intent, 0);
    }

    private void refreshDeviceList() {
        new Thread() {
            @Override
            public void run() {
                List<Device> devices = mDeviceDao.selAllDevices(currentMainUid);
                List<DeviceStatus> statuses = new ArrayList<DeviceStatus>();
                for (Device device : devices) {
                    String uid = device.getUid();
                    String deviceId = device.getDeviceId();
                    DeviceStatus deviceStatus;
                    if (DeviceUtil.isIrDevice(uid, deviceId)) {
                        deviceStatus = mDeviceStatusDao.selIrDeviceStatus(uid, device.getExtAddr());
                    } else {
                        deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
                    }
                    if (deviceStatus != null) {
                        statuses.add(deviceStatus);
                    } else {
                        LogUtil.w(TAG, "refreshAllRoomDevices()-Count not found deviceId:" + deviceId + ",deviceName:" + device.getDeviceName() + "'s deviceStatus.");
                    }
                }
                Message msg = mHandler.obtainMessage(WHAT_REFRESH);
                Bundle bundle = msg.getData();
                bundle.putSerializable(KEY_DEVICES, (Serializable) devices);
                bundle.putSerializable(KEY_DEVICESTATUSES, (Serializable) statuses);
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",deviceId:" + deviceId + ",online:" + online);
        if (mDeviceSetAdapter != null) {
            mDeviceSetAdapter.refreshOnline(uid, deviceId, online);
        }
    }

    private void exitManage() {
        new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
    }

    @Override
    protected void onDestroy() {
        if (toSearchDevice) {
            //exit manage
            exitManage();
        }
        OOReport.getInstance(mAppContext).removeOOReport(this);
        super.onDestroy();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_REFRESH) {
                    if (isFinishingOrDestroyed()) {
                        return;
                    }
                    Bundle bundle = msg.getData();
                    List<Device> devices = (List<Device>) bundle.get(KEY_DEVICES);
                    List<DeviceStatus> statuses = (List<DeviceStatus>) bundle.get(KEY_DEVICESTATUSES);
                    if (mDeviceSetAdapter == null) {
                        mDeviceSetAdapter = new DeviceSetAdapter(DeviceManageActivity.this, devices, statuses);
                        ListView device_lv = (ListView) findViewById(R.id.device_lv);
                        LinearLayout emptyView = (LinearLayout) findViewById(R.id.empty_ll);
                        device_lv.setEmptyView(emptyView);
                        device_lv.setOnItemClickListener(DeviceManageActivity.this);
                        device_lv.setAdapter(mDeviceSetAdapter);
                    } else {
                        mDeviceSetAdapter.refreshDevices(devices, statuses);
                    }
                }
            }
        };
    }

    private Runnable refreshDevicesRunnable = new Runnable() {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            LogUtil.d(TAG, "run()-Start to obtain " + currentMainUid + "'s all devices.");
            List<Device> devices = mDeviceDao.selAllDevices(currentMainUid);
            List<DeviceStatus> statuses = new ArrayList<DeviceStatus>();
            for (Device device : devices) {
                String uid = device.getUid();
                String deviceId = device.getDeviceId();
                DeviceStatus deviceStatus;
                if (DeviceUtil.isIrDevice(uid, deviceId)) {
                    deviceStatus = mDeviceStatusDao.selIrDeviceStatus(uid, device.getExtAddr());
                } else {
                    deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
                }
                if (deviceStatus != null) {
                    statuses.add(deviceStatus);
                } else {
                    LogUtil.w(TAG, "refreshAllRoomDevices()-Count not found deviceId:" + deviceId + ",deviceName:" + device.getDeviceName() + "'s deviceStatus.");
                }
            }
            long totalTime = System.currentTimeMillis() - startTime;
            LogUtil.d(TAG, "run()-Finish obtain " + currentMainUid + "'s all devices;Cost time is " + totalTime);
            Message msg = mHandler.obtainMessage(WHAT_REFRESH);
            Bundle bundle = msg.getData();
            bundle.putSerializable(KEY_DEVICES, (Serializable) devices);
            bundle.putSerializable(KEY_DEVICESTATUSES, (Serializable) statuses);
            mHandler.sendMessage(msg);
        }
    };

}
