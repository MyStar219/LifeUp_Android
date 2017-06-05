
package com.orvibo.homemate.device.manage.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.device.ControlRecord;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.List;

/**
 * 常用设备展示adapter
 * Created by snown on 2016/1/26.
 */
public class HomeCommonDeviceAdapter extends BaseAdapter {
    private static final String TAG = "HomeCommonDeviceAdapter";
    private ControlRecord mControlRecord;
    private List<Device> devices;
    private DeviceStatusDao deviceStatusDao;

    /**
     * @param devices
     */
    public HomeCommonDeviceAdapter(List<Device> devices, ControlRecord controlRecord) {
        this.devices = devices;
        deviceStatusDao = new DeviceStatusDao();
        mControlRecord = controlRecord;
    }

    /**
     * @param devices
     */
    public void setDataChanged(List<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Device getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_common,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.deviceOffLine = (OfflineView) convertView.findViewById(R.id.deviceOffLine);
            viewHolder.deviceIcon = (ImageView) convertView.findViewById(R.id.deviceIcon);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.deviceName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Device device = devices.get(position);
        String deviceId = device.getDeviceId();
        if (TextUtils.isEmpty(device.getDeviceId())) {
            viewHolder.deviceName.setText(context.getString(R.string.add));
            viewHolder.deviceOffLine.setText("");
            viewHolder.deviceIcon.setImageResource(R.drawable.common_device_add_selector);
        } else {
            boolean isOnline = DeviceUtil.isDeviceOnline(parent.getContext(), device.getUid(), deviceId) == OnlineStatus.ONLINE;
            int status = DeviceStatusConstant.OFF;
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(deviceId);
            if (deviceStatus != null) {
                status = deviceStatus.getValue1();
            }

            int realStatus = status;
            //当前控制设备的状态
            if (mControlRecord.hasDeviceAction(deviceId)) {
                status = mControlRecord.getDeviceAction(deviceId);
                LogUtil.d(TAG, "getView()-deviceId:" + deviceId + ",status:" + status);
            }
            Drawable deviceIcon = DeviceTool.getCommonDeviceDrawable(device, isOnline);
            if (isOnline) {
                viewHolder.deviceOffLine.setText("");
                if (isCanClick(device) && status == DeviceStatusConstant.OFF) {
                    deviceIcon = DeviceTool.getCloseCommonDeviceDrawable(device);
                }
                // version 1.8 常用设备上的调光灯的控制需要跳转到二级界面，但是需要在设备列表显示开关状态 2016/5/30 by zhoubaoqi
                if (DeviceType.DIMMER == device.getDeviceType() && status == DeviceStatusConstant.OFF) {
                    //调光灯的value1代表value1里面填写on/off属性值
                    deviceIcon = DeviceTool.getCloseCommonDeviceDrawable(device);
                }
            } else {
                viewHolder.deviceOffLine.setText(context.getString(R.string.offline));
            }
            viewHolder.deviceName.setText(device.getDeviceName());
            viewHolder.deviceIcon.setImageDrawable(deviceIcon);
            convertView.setContentDescription(realStatus + "");
        }
        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }


    private class ViewHolder {
        private OfflineView deviceOffLine;
        private TextView deviceName;
        private ImageView deviceIcon;
    }

    /**
     * 判断是否为可直接控制开关设备
     *
     * @param device
     * @return
     */
    public boolean isCanClick(Device device) {
        int deviceType = device.getDeviceType();
        if (deviceType == DeviceType.LAMP
                || deviceType == DeviceType.OUTLET
                || deviceType == DeviceType.SWITCH_RELAY
                || deviceType == DeviceType.COCO
                || ProductManage.getInstance().isWifiOnOffDevice(device)
                ) {
            return true;
        }
        return false;
    }
}

