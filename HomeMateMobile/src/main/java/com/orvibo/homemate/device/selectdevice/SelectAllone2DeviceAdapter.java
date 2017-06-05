package com.orvibo.homemate.device.selectdevice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;

import java.util.List;

/**
 * Created by huangqiyao on 2016/7/16 13:37.
 * 选择小方设备
 *
 * @version v1.10
 */
public class SelectAllone2DeviceAdapter extends BaseAdapter {
    private List<Device> mDevices;
    private List<DeviceStatus> mDeviceStatuses;
    private LayoutInflater mInflater;
    private String mSelectedDeviceId;

    private final int mNormalFontColor;
    private final int mSelectedFontColor;

    public SelectAllone2DeviceAdapter(Context context, List<Device> devices, List<DeviceStatus> deviceStatuses, String selectedDeviceId) {
        mDevices = devices;
        mDeviceStatuses = deviceStatuses;
        mSelectedDeviceId = selectedDeviceId;
        mInflater = LayoutInflater.from(context);
        mNormalFontColor = context.getResources().getColor(R.color.identity_tip);
        mSelectedFontColor = context.getResources().getColor(R.color.green);
    }

    public void selectDevice(String selectedDeviceId) {
        mSelectedDeviceId = selectedDeviceId;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_select_room, parent, false);
            holder.tv_name = (TextView) convertView.findViewById(R.id.roomNameTextView);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.roomTypeImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Device device = mDevices.get(position);

//        LogUtil.d(TAG, "getView(" + position + ")-room:" + room);
        holder.tv_name.setText(device.getDeviceName());
        final boolean isSelected = device.getDeviceId().equals(mSelectedDeviceId);//当前选中的设备
        holder.tv_name.setTextColor(isSelected ? mSelectedFontColor : mNormalFontColor);

        convertView.setTag(R.id.tag_device, device);
//        holder.iv_icon.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), false));
//
//        if (isSelected) {
//            holder.iv_icon.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), true));
//        } else {
//            holder.iv_icon.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), false));
//        }
        return convertView;
    }


    private class ViewHolder {
        private TextView tv_name;
        private ImageView iv_icon;
    }
}
