package com.orvibo.homemate.device.energyremind;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.SaveReminderFlag;
import com.orvibo.homemate.sharedPreferences.EnergyReminderCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Allen on 2015/10/14
 */
public class DeviceEnergySettingAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View.OnClickListener mClickListener;
    private List<Device> devices;
    private RoomDao mRoomDao;
    /**
     * key:uid_deviceId,value:floorName roomName
     */
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();

    private final String ROOM_NOT_SET;

    public DeviceEnergySettingAdapter(Context context, View.OnClickListener onClickListener, List<Device> devices) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mClickListener = onClickListener;
        this.devices = devices;mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
    }

    @Override
    public int getCount() {
        return devices.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (devices.size() > position) {
            return devices.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = mLayoutInflater.inflate(R.layout.message_setting_all_energy_item, null);
            ImageView infoPushSwitchImageView = (ImageView) convertView.findViewById(R.id.allInfoPushSetImageView);
            infoPushSwitchImageView.setOnClickListener(mClickListener);
            if (EnergyReminderCache.getEnergyReminder(mContext,UserCache.getCurrentMainUid(mContext)) == SaveReminderFlag.OFF) {
                infoPushSwitchImageView.setImageLevel(0);
            } else {
                infoPushSwitchImageView.setImageLevel(1);
            }
        } else {
            Device device = devices.get(position - 1);
            convertView = mLayoutInflater.inflate(R.layout.item_device_energy_setting, null);
            ImageView infoPushSwitchImageView = (ImageView) convertView.findViewById(R.id.infoPushSwitchImageView);
            ImageView deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
            TextView deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            TextView room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            final String uid = device.getUid();
            final String deviceId = device.getDeviceId();
            final String floorRoomName = getRoom(uid, deviceId);
            boolean isOnline = true;
            Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
            deviceIcon_iv.setImageDrawable(deviceIcon);
            deviceName_tv.setText(device.getDeviceName());
            room_tv.setText(floorRoomName);
            infoPushSwitchImageView.setOnClickListener(mClickListener);
            infoPushSwitchImageView.setTag(device);
            if (device.getSaveReminderFalg() == SaveReminderFlag.OFF) {
                infoPushSwitchImageView.setImageLevel(0);
            } else {
                infoPushSwitchImageView.setImageLevel(1);
            }
        }
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

}
