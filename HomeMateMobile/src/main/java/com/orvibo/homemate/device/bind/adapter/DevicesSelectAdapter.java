
package com.orvibo.homemate.device.bind.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DevicesSelectAdapter extends BaseDevicesAdapter {
    private static final String TAG = DevicesSelectAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private final Resources mRes;
    private final int mFontNormalColor;
    private final int mFontOfflineColor;

    private Set<String> mCheckedDevices = new HashSet<String>();

    private boolean isFromCommonDevice;

    /**
     * true显示楼层房间信息
     */
    private boolean isShowRoomInfo = true;

    /**
     * key:extAddr_deviceId,value:true有学习过红外码，false学习过红外码，不让选择。
     */
    private Map<String, Boolean> mDeviceIrCodes = new HashMap<String, Boolean>();

    /**
     * 房间与楼层
     */
    private RoomDao mRoomDao;
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();
    //默认房间
    private final String ROOM_NOT_SET;

    /**
     * @param context
     * @param devices        如果某个position的deviceId为-1，表示这个position为大分割view
     * @param deviceStatuses
     */
    public DevicesSelectAdapter(Activity context, List<Device> devices, List<Device> checkedDevices,
                                List<DeviceStatus> deviceStatuses) {
        super(context, DeviceSort.sortDevices(devices,true), deviceStatuses);
        mInflater = LayoutInflater.from(context);
        setCheckedDevices(checkedDevices);
        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mFontOfflineColor = mRes.getColor(R.color.font_offline);
        mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    /**
     * @param isShowRoomInfo true显示楼层房间信息
     */
    public void showRoomInfo(boolean isShowRoomInfo) {
        this.isShowRoomInfo = isShowRoomInfo;
    }

    /**
     * 数据请求回来时，需要再次调用初始化
     *
     * @param devices
     */
    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
    }

    private void setCheckedDevices(List<Device> checkedDevices) {
        if (checkedDevices == null || checkedDevices.isEmpty()) {
            mCheckedDevices.clear();
        } else {
            mCheckedDevices.clear();
            for (Device device : checkedDevices) {
                final String key = getKey(device.getUid(), device.getDeviceId());
                mCheckedDevices.add(key);
            }
        }
    }

    public void setIsFromCommonDevice(boolean isFromCommonDevice) {
        this.isFromCommonDevice = isFromCommonDevice;
    }

    public void refreshDevices(List<Device> devices, List<Device> checkedDevices, List<DeviceStatus> deviceStatuses) {
        setData(DeviceSort.sortDevices(devices), deviceStatuses);
        setCheckedDevices(checkedDevices);
        initRooms(devices);
        notifyDataSetChanged();
    }

    /**
     * 刷新选中状态
     *
     * @param checkedDevices
     */
    public void refreshCheckDevice(List<Device> checkedDevices) {
        setCheckedDevices(checkedDevices);
        notifyDataSetChanged();
    }

    @Override
    public void refreshOnline(String uid, String deviceId, int online) {
        super.refreshOnline(uid, deviceId, online);
        notifyDataSetChanged();
    }

    private String getKey(String uid, String deviceId) {
        return uid + "_" + deviceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = mDevices.get(position);
        //  LogUtil.e(TAG, "getView()-mDevices:" + mDevices);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.device_item_check,
                    parent, false);
            holder.ivDeviceIcon = (ImageView) convertView
                    .findViewById(R.id.ivDeviceIcon);
            holder.tvDeviceName = (TextView) convertView
                    .findViewById(R.id.tvDeviceName);
            holder.cbDevice = (CheckBox) convertView
                    .findViewById(R.id.cbDevice);
            holder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
            holder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String uid = device.getUid();
        String deviceId = device.getDeviceId();
        String deviceName = device.getDeviceName();
        String offlineOrIrCode = mRes.getString(R.string.offline);
        int deviceType = device.getDeviceType();
        boolean check = true;//true不能选择，false可以选择
        synchronized (this) {
            try {
                check = isOnline(deviceId);
            } catch (NullPointerException e) {
                //e.printStackTrace();
                LogUtil.w(TAG, "getView()-deviceName:" + deviceName);
            }
        }

        if (check && DeviceUtil.isIrDevice(device)) {
            String key = getNoCheckKey(device.getExtAddr(), deviceId);
            if (mDeviceIrCodes.containsKey(key)) {
                check = mDeviceIrCodes.get(key);
                offlineOrIrCode = mRes.getString(R.string.bind_device_no_ir);
            } else {
                if (!DeviceTool.isIrDevicelearned(device)) {
                    check = false;
                    offlineOrIrCode = mRes.getString(R.string.bind_device_no_ir);
                }
                mDeviceIrCodes.put(key, check);
            }
            LogUtil.d(TAG, "getView()-device:" + device + ",check:" + check + ",mDeviceIrCodes:" + mDeviceIrCodes);
        }
        Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, check);
        holder.ivDeviceIcon.setImageDrawable(deviceIcon);
        holder.tvDeviceName.setText(deviceName);
        holder.tvDeviceName.setTextColor(check ? mFontNormalColor : mFontOfflineColor);
        holder.room_tv.setText(getRoom(uid, deviceId));

        //显示or隐藏楼层房间信息
        if (isShowRoomInfo) {
            holder.room_tv.setVisibility(View.VISIBLE);
        } else {
            holder.room_tv.setVisibility(View.GONE);
        }
        //设备离线，隐藏checkbox。不能选择离线设备
        if (isFromCommonDevice) {
            holder.cbDevice.setVisibility(View.VISIBLE);
        } else {
            holder.cbDevice.setVisibility(check ? View.VISIBLE : View.INVISIBLE);
        }
        final String checkedKey = getKey(uid, deviceId);
//        LogUtil.d(TAG, "getView()-checkedKey:" + checkedKey + ",mCheckedDevices:" + mCheckedDevices);
        if (mCheckedDevices.contains(checkedKey)) {
            holder.cbDevice.setChecked(true);
        } else {
            holder.cbDevice.setChecked(false);
        }
        holder.cbDevice.setTag(R.id.tag_device, device);
        holder.offline_view.setVisibility(check ? View.GONE : View.VISIBLE);
        holder.offline_view.setText(offlineOrIrCode);

        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    /**
     * 获取房间（add by yuwei,2016-7-8;处理没有显示房间和楼层的问题）
     *
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

    private String getNoCheckKey(String extaddr, String deviceId) {
        return extaddr + "_" + deviceId;
    }

    private String getCheckedDeviceKey(String uid, int deviceId) {
        return uid + "_" + deviceId;
    }

    private class ViewHolder {
        private ImageView ivDeviceIcon;
        private TextView tvDeviceName;
        private CheckBox cbDevice;
        private OfflineView offline_view;
        private TextView room_tv;
    }

}

