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
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Allen on 2015/10/14
 */
public class DeviceEnergyAdapter extends BaseAdapter {

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

    public DeviceEnergyAdapter(Context context, View.OnClickListener onClickListener, List<Device> devices) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mClickListener = onClickListener;
        this.devices = devices;
        mRoomDao = new RoomDao();
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        initRooms(devices);
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NormalDeviceHolder normalDeviceHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_device_energy,
                    parent, false);
            normalDeviceHolder = new NormalDeviceHolder();
            normalDeviceHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
            normalDeviceHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            normalDeviceHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            normalDeviceHolder.action_off_tv = (TextView) convertView.findViewById(R.id.action_off_tv);
            convertView.setTag(normalDeviceHolder);
        } else {
            normalDeviceHolder = (NormalDeviceHolder) convertView.getTag();
        }
        Device device = devices.get(position);
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        final String floorRoomName = getRoom(uid, deviceId);
        boolean isOnline = true;
        Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
        normalDeviceHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
        normalDeviceHolder.deviceName_tv.setText(device.getDeviceName());
        normalDeviceHolder.room_tv.setText(floorRoomName);
        normalDeviceHolder.action_off_tv.setOnClickListener(mClickListener);
        normalDeviceHolder.action_off_tv.setTag(device);
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

    class NormalDeviceHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView room_tv;
        private TextView action_off_tv;
    }

}
