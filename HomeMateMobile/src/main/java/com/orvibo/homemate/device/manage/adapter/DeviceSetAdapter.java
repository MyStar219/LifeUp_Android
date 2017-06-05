package com.orvibo.homemate.device.manage.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.device.BaseDevicesAdapter;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理-设备列表
 * Created by huangqiyao on 2015/4/3.
 */
public class DeviceSetAdapter extends BaseDevicesAdapter {
    private static final String TAG = DeviceSetAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private RoomDao mRoomDao;

    private static final int NORMAL_DEVICE_ITEM = 0;
    public static final int IR_DEVICE_ITEM = 1;

    /**
     * key:uid_deviceId,value:floorName roomName
     */
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();

    private final String ROOM_NOT_SET;

    public DeviceSetAdapter(Activity context, List<Device> devices, List<DeviceStatus> deviceStatuses) {
        super(context, DeviceSort.sortDevices(devices), deviceStatuses);
        mInflater = LayoutInflater.from(context);
        mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    public void refreshDevices(List<Device> devices, List<DeviceStatus> deviceStatuses) {
        this.mDevices = orderDevices(devices);
        initRooms(mDevices);
        setData(mDevices, deviceStatuses);
        notifyDataSetChanged();
    }

    /**
     * 按照协议排序设备
     *
     * @param devices
     * @return
     */
    private List<Device> orderDevices(List<Device> devices) {
        return DeviceSort.sortDevices(devices);
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
        LogUtil.d(TAG, "initRooms()-mFloorNameAndRoomNames:" + mFloorNameAndRoomNames);
    }

    @Override
    public void refreshOnline(String uid, String deviceId, int online) {
        super.refreshOnline(uid, deviceId, online);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        final Device device = mDevices.get(position);
        int itemType;
        String uid = device.getUid();
        String deviceId = device.getDeviceId();
        if (DeviceUtil.isIrDevice(uid, deviceId)) {
            if (DeviceTool.isIrDevicelearned(device)) {
                itemType = NORMAL_DEVICE_ITEM;
            } else {
                itemType = IR_DEVICE_ITEM;
            }
        } else {
            itemType = NORMAL_DEVICE_ITEM;
        }
        return itemType;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int itemType = getItemViewType(position);

        NormalDeviceHolder normalDeviceHolder = null;
        IrDeviceViewIrHolder irDeviceViewIrHolder = null;
        if (convertView == null) {
            if (itemType == NORMAL_DEVICE_ITEM) {
                convertView = mInflater.inflate(R.layout.item_device_set,
                        parent, false);
                normalDeviceHolder = new NormalDeviceHolder();
                normalDeviceHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
                normalDeviceHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
                normalDeviceHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
                normalDeviceHolder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
                convertView.setTag(normalDeviceHolder);
            } else if (itemType == IR_DEVICE_ITEM) {
                convertView = mInflater.inflate(R.layout.item_device_ir_learn,
                        parent, false);
                irDeviceViewIrHolder = new IrDeviceViewIrHolder();
                irDeviceViewIrHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
                irDeviceViewIrHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
                irDeviceViewIrHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
                irDeviceViewIrHolder.learn_ir_tv = (TextView) convertView.findViewById(R.id.learn_ir_tv);
                irDeviceViewIrHolder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
                convertView.setTag(irDeviceViewIrHolder);
            }

        } else {
            if (itemType == NORMAL_DEVICE_ITEM) {
                normalDeviceHolder = (NormalDeviceHolder) convertView.getTag();
            } else if (itemType == IR_DEVICE_ITEM) {
                irDeviceViewIrHolder = (IrDeviceViewIrHolder) convertView.getTag();
            }
        }
        convertView.setBackgroundResource(R.drawable.item_selector);
        Device device = mDevices.get(position);
        final String uid = device.getUid();
        final int deviceType = device.getDeviceType();
        final String deviceId = device.getDeviceId();
        final String floorRoomName = getRoom(uid, deviceId);

        // true在线，false离线
        boolean isOnline = true;
        synchronized (this) {
            try {
                isOnline = isOnline(deviceId);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
        if (itemType == NORMAL_DEVICE_ITEM) {
            if (!isOnline) {
                boolean sleep = DeviceTool.isSleep(deviceType);
                if (sleep) {
                    normalDeviceHolder.offline_view.setText(R.string.sleep);
                } else {
                    normalDeviceHolder.offline_view.setText(R.string.offline);
                }
            }
            normalDeviceHolder.offline_view.setVisibility(isOnline ? View.INVISIBLE : View.VISIBLE);
            normalDeviceHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
            normalDeviceHolder.deviceName_tv.setText(device.getDeviceName());
            normalDeviceHolder.room_tv.setText(floorRoomName);
        } else if (itemType == IR_DEVICE_ITEM) {
            if (!isOnline) {
                boolean sleep = DeviceTool.isSleep(deviceType);
                if (sleep) {
                    irDeviceViewIrHolder.offline_view.setText(R.string.sleep);
                } else {
                    irDeviceViewIrHolder.offline_view.setText(R.string.offline);
                }
            }
            irDeviceViewIrHolder.offline_view.setVisibility(isOnline ? View.INVISIBLE : View.VISIBLE);
            irDeviceViewIrHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
            irDeviceViewIrHolder.deviceName_tv.setText(device.getDeviceName());
            irDeviceViewIrHolder.room_tv.setText(floorRoomName);
            irDeviceViewIrHolder.learn_ir_tv.setTag(device);
            irDeviceViewIrHolder.learn_ir_tv.setContentDescription(floorRoomName);
        }
        convertView.setTag(R.id.tag_device, device);
        convertView.setContentDescription(floorRoomName);
        return convertView;
    }

    /**
     * @param uid
     * @param deviceId
     * @return
     */
    private String getRoom(String uid, String deviceId) {
        final String key = getKey(uid, deviceId);
        String name = ROOM_NOT_SET;
        if (mFloorNameAndRoomNames.containsKey(key)) {
            name = mFloorNameAndRoomNames.get(key);
        }
        return name;
    }

    private String getKey(String uid, String deviceId) {
        return uid + "_" + deviceId;
    }

    /**
     * 普通设备包括以学习按键的红外设备
     */
    class NormalDeviceHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView room_tv;
        private OfflineView offline_view;
    }

    /**
     * 红外设备未学习红外码
     */
    class IrDeviceViewIrHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView room_tv;
        private OfflineView offline_view;
        private TextView learn_ir_tv;
    }

}
