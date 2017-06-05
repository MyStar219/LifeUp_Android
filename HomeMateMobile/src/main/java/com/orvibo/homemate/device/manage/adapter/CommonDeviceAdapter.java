
package com.orvibo.homemate.device.manage.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.device.BaseDevicesAdapter;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonDeviceAdapter extends BaseDevicesAdapter {
    private static final String TAG = CommonDeviceAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private final Resources mRes;
    private final int mFontNormalColor;
    private List<Device> devices;
    private RoomDao mRoomDao;
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();

    private final String ROOM_NOT_SET;


    /**
     * @param context
     * @param devices 如果某个position的deviceId为-1，表示这个position为大分割view
     */
    public CommonDeviceAdapter(Activity context, List<Device> devices) {
        super(context, devices, null);
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mDevices = devices;
        this.devices = devices;
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
    }

    public void refreshDevices(List<Device> devices) {
        LogUtil.d(TAG, "commonDevices=" + devices);
        mDevices = devices;
        this.devices = devices;
        initRooms(devices);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_common_device,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
            viewHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            viewHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            viewHolder.unLearned = (OfflineView) convertView.findViewById(R.id.offline_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Device device = devices.get(position);
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        final String floorRoomName = getRoom(uid, deviceId);
        viewHolder.deviceName_tv.setText(device.getDeviceName());
        viewHolder.room_tv.setText(floorRoomName);
        //********************
        //如果红外设备没有学习，显示未学习
        if (DeviceUtil.isIrDevice(device)) {
            if (!DeviceTool.isIrDevicelearned(device)) {
                Drawable unLearnedDeviceIcon = DeviceTool.getDeviceDrawable(device, false);
                viewHolder.deviceIcon_iv.setImageDrawable(unLearnedDeviceIcon);
                viewHolder.unLearned.setText(mRes.getString(R.string.bind_device_no_ir));
            } else {
                Drawable learnedDeviceIcon = DeviceTool.getDeviceDrawable(device, true);
                viewHolder.deviceIcon_iv.setImageDrawable(learnedDeviceIcon);
                viewHolder.unLearned.setText("");
            }
        } else {
            boolean isOnline = true;
            Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
            viewHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
            viewHolder.unLearned.setText("");
        }
        //********************
        return convertView;
    }

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

    private class ViewHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView room_tv;
        private OfflineView unLearned;
    }

}

