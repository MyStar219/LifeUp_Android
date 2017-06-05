package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.IntelligentSceneTool;

import java.util.List;


public class SecuritySelectConditionAdapter extends BaseAdapter {
    private Resources    mRes;
    private List<Device> mSecuritySensorDevices;
    private List<Device> mselectedSensorDevices;

    private RoomDao  mRoomDao  = new RoomDao();
    private FloorDao mFloorDao = new FloorDao();
    private String mMainUid;
    private Floor  mDeviceFloor;

    public SecuritySelectConditionAdapter(Context context, List<Device> allSecuritySensorDevices, List<Device> selectedSensorDevices) {
        mSecuritySensorDevices = allSecuritySensorDevices;
        mselectedSensorDevices = selectedSensorDevices;
        mRes = context.getResources();
    }

    public void setMainUid(String mainUid) {
        mMainUid = mainUid;
    }

    public void setSelectedSensorDevices(List<Device> selectedSensorDevices) {
        mselectedSensorDevices = selectedSensorDevices;
    }

    @Override
    public int getCount() {
        return mSecuritySensorDevices == null ? 0 : mSecuritySensorDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mSecuritySensorDevices == null ? null : mSecuritySensorDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.intelligent_securitycondtion_list_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.securityImageView);
            holder.name = (TextView) convertView.findViewById(R.id.tvSecurityCondtion);
            holder.location = (TextView) convertView.findViewById(R.id.tvSecurityLocation);
            holder.checkIcon = (CheckBox) convertView.findViewById(R.id.securityCheck);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Device securityDevice = mSecuritySensorDevices.get(position);
        int nameId = IntelligentSceneTool.getSecurityConditionTypeNameResId(securityDevice.getDeviceType());
        String deviceName = securityDevice.getDeviceName();
        switch (securityDevice.getDeviceType()) {
            //打开门窗传感器
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
                holder.name.setText(mRes.getString(nameId) + deviceName);
                break;
            //有人经过人体传感器
            case DeviceType.INFRARED_SENSOR:
                holder.name.setText(mRes.getString(nameId) + deviceName);
                break;
            //烟雾报警器报警
            case DeviceType.SMOKE_SENSOR:
                holder.name.setText(deviceName + mRes.getString(nameId));
                break;
            //一氧化碳报警器报警
            case DeviceType.CO_SENSOR:
                holder.name.setText(deviceName + mRes.getString(nameId));
                break;
            //可燃气体报警器报警
            case DeviceType.FLAMMABLE_GAS:
                holder.name.setText(deviceName + mRes.getString(nameId));
                break;
            //有水浸入水浸警器报警
            case DeviceType.WATER_SENSOR:
                holder.name.setText(deviceName + mRes.getString(nameId));
                break;
            //有人按下紧急按钮
            case DeviceType.SOS_SENSOR:
                holder.name.setText(mRes.getString(nameId) + deviceName);
                break;
        }

        final int conditionResid = IntelligentSceneTool.getSecurityConditionIconResId(securityDevice.getDeviceType());
        holder.icon.setImageResource(conditionResid);
        String deviceRoomName = "";
        String deviceFloorName = "";
        String roomId = securityDevice.getRoomId();
        //设备未设置房间
        Room deviceRoom = mRoomDao.selRoom(mMainUid, securityDevice.getRoomId());
        if (deviceRoom != null) {
            deviceRoomName = deviceRoom.getRoomName();
            mDeviceFloor = mFloorDao.selFloor(mMainUid, deviceRoom.getFloorId());
            if (mDeviceFloor != null) {
                deviceFloorName = mDeviceFloor.getFloorName();
            }
        }
        if (deviceRoomName.isEmpty() && deviceFloorName.isEmpty()) {
            if (mRoomDao.selAllRooms(mMainUid) == null && mFloorDao.selAllFloors(mMainUid) == null) {
                holder.location.setText("");
                holder.location.setVisibility(View.GONE);
            } else {
                holder.location.setVisibility(View.VISIBLE);
                holder.location.setText(mRes.getString(R.string.intelligent_scene_no_set_floorAndroom));
            }
        } else {
            holder.location.setText(deviceFloorName + " " + deviceRoomName);
        }
        // convertView.setTag(R.id.tag_conditionType, securityDevice);
        if (mselectedSensorDevices != null && mselectedSensorDevices.contains(securityDevice)) {
            holder.checkIcon.setChecked(true);
            holder.checkIcon.setTag(true);
        } else {
            holder.checkIcon.setChecked(false);
            holder.checkIcon.setTag(false);
        }
        return convertView;
    }

    public void refresh(List<Device> allSecuritySensorDevices, List<Device> selectedSensorDevices) {
        setSelectedSensorDevices(selectedSensorDevices);
        mSecuritySensorDevices = allSecuritySensorDevices;
        notifyDataSetChanged();
    }

    class ViewHolder {
        private ImageView icon;
        private TextView  name;
        private TextView  location;
        private CheckBox  checkIcon;
    }
}
