package com.orvibo.homemate.device.bind.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能场景选择安防设备，目前只显示门锁，门窗传感器，人体红外传感器
 * Created by Smagret on 2015/10/16.
 */
public class SelectDeviceAdapter extends BaseAdapter {
    private static final String TAG = SelectDeviceAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<Device> mDevices;
    private RoomDao mRoomDao;
    private String selectDeviceId;
    /**
     * key:uid_deviceId,value:floorName roomName
     */
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();

//    private Set<String> mCheckedDevices = new HashSet<String>();

    private final String ROOM_NOT_SET;

    public SelectDeviceAdapter(Context context, List<Device> devices, String selectDeviceId) {
        this.mDevices = devices;
        this.selectDeviceId = selectDeviceId;
        mInflater = LayoutInflater.from(context);
        mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    public void refresh(List<Device> devices) {
        this.mDevices = devices;
        initRooms(devices);
        notifyDataSetChanged();
    }

    public void setChecked(String selectDeviceId) {
        this.selectDeviceId = selectDeviceId;
        notifyDataSetChanged();
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
        LogUtil.d(TAG, "initRooms()-mFloorNameAndRoomNames:" + mFloorNameAndRoomNames);
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return (mDevices == null || position >= mDevices.size()) ? null : mDevices.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NormalDeviceHolder normalDeviceHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_select_device,
                    parent, false);
            normalDeviceHolder = new NormalDeviceHolder();
            normalDeviceHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            normalDeviceHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            normalDeviceHolder.ivDevice = (ImageView) convertView
                    .findViewById(R.id.ivDevice);
            convertView.setTag(normalDeviceHolder);
        } else {
            normalDeviceHolder = (NormalDeviceHolder) convertView.getTag();
        }
        convertView.setBackgroundResource(R.drawable.item_selector);
        Device device = mDevices.get(position);
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        final String floorRoomName = getRoom(uid, deviceId);

//        final String checkedKey = getKey(uid, deviceId);
        if (!TextUtils.isEmpty(selectDeviceId) && selectDeviceId.equals(deviceId)) {
            normalDeviceHolder.ivDevice.setImageResource(R.drawable.icon_device_choiced);
        } else {
            normalDeviceHolder.ivDevice.setImageResource(R.drawable.icon_not_selected);
//            LogUtil.d(TAG, "getView()-key:" + checkedKey + ",mCheckedDevices:" + mCheckedDevices);
        }
        //wifi设备去掉楼层房间ui
        if (ProductManage.getInstance().isWifiDeviceByModel(device.getModel())) {
            normalDeviceHolder.room_tv.setVisibility(View.GONE);
        } else {
            normalDeviceHolder.room_tv.setVisibility(View.VISIBLE);
        }
        normalDeviceHolder.ivDevice.setTag(R.id.tag_device, device);
        normalDeviceHolder.deviceName_tv.setText(device.getDeviceName());
        normalDeviceHolder.room_tv.setText(floorRoomName);
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
     * 普通安防设备
     */
    class NormalDeviceHolder {
        private TextView deviceName_tv;
        private TextView room_tv;
        private ImageView ivDevice;
    }


}
