package com.orvibo.homemate.device.manage.adapter;

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
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.StringUtil;

import java.util.List;

public class AddDeviceSuccessAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Device> mDevices;


    /**
     * @param context
     * @param devices
     */
    public AddDeviceSuccessAdapter(Context context, List<Device> devices) {
        this.mDevices = devices;
        mInflater = LayoutInflater.from(context);
    }

    public void refresh(List<Device> devices) {
        this.mDevices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices == null ? null : mDevices.get(position);
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
            convertView = mInflater.inflate(R.layout.item_add_device_success, parent,
                    false);
            holder.deviceIcon_iv = (ImageView) convertView
                    .findViewById(R.id.icon_iv);
            holder.deviceTypeName_tv = (TextView) convertView
                    .findViewById(R.id.deviceTypeName_tv);
            holder.set_tv = (TextView) convertView.findViewById(R.id.set_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Device device = mDevices.get(position);
        if (device == null) {
            return convertView;
        }
        final int deviceType = device.getDeviceType();
        Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, true);
        String deviceId = device.getDeviceId();
        String deviceName = device.getDeviceName();

        if (StringUtil.isEmpty(deviceName)) {
            holder.deviceTypeName_tv.setText(DeviceTool
                    .getDeviceTypeNameResId(deviceType));
        } else {
            holder.deviceTypeName_tv.setText(deviceName);
        }

        if ((Constant.INVALID_NUM + "").equals(deviceId)) {
            holder.set_tv.setVisibility(View.VISIBLE);
//            deviceIconResId = vicenter300IconResId;
        } else {
            holder.set_tv.setVisibility(View.VISIBLE);
        }
        holder.deviceIcon_iv.setImageDrawable(deviceIcon);

        holder.set_tv.setTag(R.id.tag_device, device);
        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    private class ViewHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceTypeName_tv;
        private TextView set_tv;
    }

}
