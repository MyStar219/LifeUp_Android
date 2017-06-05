
package com.orvibo.homemate.device.bind.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.device.BaseDevicesAdapter;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesSelectSingleAdapter extends BaseDevicesAdapter {
    private static final String TAG = DevicesSelectSingleAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Device checkedDevice;
    private final Resources mRes;
    private final int mFontNormalColor;
    private final int mFontOfflineColor;

    private Map<String, Integer> mDeviceStatuses = new HashMap<String, Integer>();

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
    public DevicesSelectSingleAdapter(Activity context, List<Device> devices, Device checkedDevice,
                                      List<DeviceStatus> deviceStatuses) {
        super(context, DeviceSort.sortDevices(devices), deviceStatuses);
        mInflater = LayoutInflater.from(context);

        this.checkedDevice = checkedDevice;

        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mFontOfflineColor = mRes.getColor(R.color.font_offline);
        mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    /**
     * 数据请求回来时，需要再次调用初始化
     *
     * @param devices
     */
    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
//        MyLogger.kLog().d(mFloorNameAndRoomNames);
    }

    public boolean isCanChecked(Device device) {
        final boolean isIrDevice = DeviceUtil.isIrDevice(device);
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        try {
            boolean isOnline = isOnline(deviceId);
            if (isIrDevice) {
                final boolean isIrDevicelearned = DeviceTool.isIrDevicelearned(device);
                return isOnline && isIrDevicelearned;
            } else {
                return isOnline;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void refreshDevices(List<Device> devices, Device checkedDevice, List<DeviceStatus> deviceStatuses) {
        this.checkedDevice = checkedDevice;
        setData(DeviceSort.sortDevices(devices), deviceStatuses);
        initRooms(devices);
        notifyDataSetChanged();
    }

    public void check(Device checkedDevice) {
        this.checkedDevice = checkedDevice;
        notifyDataSetChanged();
    }

    @Override
    public void refreshOnline(String uid, String deviceId, int online) {
        super.refreshOnline(uid, deviceId, online);
        notifyDataSetChanged();
    }

    /**
     * 要跟{@link RoomDao#getRoomNameAndFloorNames}的分割线一样
     *
     * @param uid
     * @param deviceId
     * @return
     */
    private String getKey(String uid, String deviceId) {
        return uid + "_" + deviceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Device device = mDevices.get(position);
        //  LogUtil.e(TAG, "getView()-mDevices:" + mDevices);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.device_item_single,
                    parent, false);
            holder.ivDeviceIcon = (ImageView) convertView
                    .findViewById(R.id.ivDeviceIcon);
            holder.tvDeviceName = (TextView) convertView
                    .findViewById(R.id.tvDeviceName);
            holder.tvRoomName = (TextView) convertView.findViewById(R.id.room_tv);
            holder.rbDevice = (RadioButton) convertView
                    .findViewById(R.id.rbDevice);
            holder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        final String deviceName = device.getDeviceName();
        // true在线，false离线
//        boolean isOnline = true;
        String offlineOrIrCode = mRes.getString(R.string.offline);
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
        if (isShowRoom(uid)) {
            //大于10个设备，需要显示房间楼层
            holder.tvRoomName.setVisibility(View.VISIBLE);
            holder.tvRoomName.setText(getRoom(uid, deviceId));
        } else {
            //少于10个设备,不显示房间楼层
            holder.tvRoomName.setVisibility(View.GONE);
        }
        holder.tvDeviceName.setTextColor(check ? mFontNormalColor : mFontOfflineColor);
        holder.offline_view.setText(offlineOrIrCode);

        int status = DeviceStatusConstant.OFF;
        final String key = getKey(uid, deviceId);
        synchronized (this) {
            if (mDeviceStatuses.containsKey(key)) {
                status = mDeviceStatuses.get(key);
            }
        }

        try {
            if (check) {
                holder.rbDevice.setVisibility(View.VISIBLE);
                if (checkedDevice != null && checkedDevice.getDeviceId().equals(device.getDeviceId())) {
                    holder.rbDevice.setChecked(true);
                } else {
                    holder.rbDevice.setChecked(false);
                }
            } else {
                holder.rbDevice.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e) {
            LogUtil.d(TAG, "getView()-\ncheckedDevice:" + checkedDevice + "\ndevice:" + device);
        }
        holder.rbDevice.setTag(R.id.tag_device, device);
        holder.offline_view.setVisibility(check ? View.GONE : View.VISIBLE);
        holder.offline_view.setText(offlineOrIrCode);

        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    private String getNoCheckKey(String extaddr, String deviceId) {
        return extaddr + "_" + deviceId;
    }

    /**
     * 获取房间（add by yuwei,2016-8-5;处理没有显示房间和楼层的问题）
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
//        MyLogger.kLog().d("room:" + name + ",key:" + key);
        return name;
    }

    /**
     * 设置房间是否显示
     *
     * @return
     */
    private boolean isShowRoom(String uid) {
        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(uid);
        int floorCount = new FloorDao().selFloorNo(uid);
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                return false;
            }
        }
        return true;
    }

    private class ViewHolder {
        private ImageView ivDeviceIcon;
        private TextView tvDeviceName;
        private TextView tvRoomName;
        private RadioButton rbDevice;
        private OfflineView offline_view;
    }

}

