//package com.orvibo.homemate.device.bind.adapter;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.drawable.Drawable;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.data.DeviceType;
//import com.orvibo.homemate.util.DeviceTool;
//import com.orvibo.homemate.view.custom.OfflineView;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created by huangqiyao on 2016/7/21 20:41.
// *
// * @version v
// */
//public class SelectAlloneChildDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private static final int ALLONE_CHILD_DEVICE = 1;
//    private static final int ALLONE = 0;
//    private List<Device> mDevices;
//    /**
//     * 选中的设备
//     */
//    private ArrayList<Device> mCheckedDevices = new ArrayList<>();
//    private Context mContext;
//    private LayoutInflater mInflater;
//    private final Resources mRes;
//    private final int mFontNormalColor;
//    private final int mFontOfflineColor;
//
//    public SelectAlloneChildDeviceAdapter(Context context, List<Device> devices, ArrayList<Device> checkedDevices) {
//        if (devices == null) {
//            devices = new ArrayList<>();
//        }
//        mDevices = devices;
//        mCheckedDevices = checkedDevices;
//        mInflater = LayoutInflater.from(context);
//        mRes = context.getResources();
//        mFontNormalColor = mRes.getColor(R.color.font_black);
//        mFontOfflineColor = mRes.getColor(R.color.font_offline);
//        mContext = context.getApplicationContext();
//    }
//
//    public void refresh(List<Device> devices, ArrayList<Device> checkedDevices) {
//        mDevices = devices;
//        mCheckedDevices = checkedDevices;
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == ALLONE_CHILD_DEVICE) {
//            View view = mInflater.inflate(R.layout.item_allone_child_device_check,
//                    parent, false);
//            return new ViewHolder(view);
//        } else if (viewType == ALLONE) {
//            View view = mInflater.inflate(R.layout.item_name,
//                    parent, false);
//            return new NameHolder(view);
//        }
//        return null;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        Device device = mDevices.get(position);
//        if (holder instanceof ViewHolder) {
//            boolean isOnline = true;//true设备在线，可以选择设备。只有小方设备，设备离线也能选择并设置场景
//
//            ((ViewHolder) holder).tv_name.setText(device.getDeviceName());
//            ((ViewHolder) holder).tv_name.setTextColor(isOnline ? mFontNormalColor : mFontOfflineColor);
//
//            //设备图标、离线图标
//            if (isOnline) {
//                Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
//                ((ViewHolder) holder).iv_deviceIcon.setImageDrawable(deviceIcon);
//                ((ViewHolder) holder).iv_deviceIcon.setVisibility(View.VISIBLE);
//                ((ViewHolder) holder).offline_view.setVisibility(View.GONE);
//            } else {
//                ((ViewHolder) holder).iv_deviceIcon.setVisibility(View.GONE);
//                ((ViewHolder) holder).offline_view.setVisibility(View.VISIBLE);
//            }
//
//            if (isSelected(device)) {
//                ((ViewHolder) holder).iv_select.setImageResource(R.drawable.checkbox_selected);
//            } else {
//                ((ViewHolder) holder).iv_select.setImageResource(R.drawable.allone2_select_child_device_selector);
//            }
//        } else if (holder instanceof NameHolder) {
//            ((NameHolder) holder).tv_name.setText(device.getDeviceName());
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return mDevices.size();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        Device device = mDevices.get(position);
//        final int deviceType = device.getDeviceType();
//        if (deviceType == DeviceType.ALLONE) {
//            return ALLONE;
//        }
//        return ALLONE_CHILD_DEVICE;
//    }
//
//    /**
//     * 判断设备是否处于选中状态
//     *
//     * @param device
//     * @return
//     */
//    private boolean isSelected(Device device) {
//        final String deviceId = device.getDeviceId();
//        Iterator<Device> iterator = mCheckedDevices.iterator();
//        while (iterator.hasNext()) {
//            Device d = iterator.next();
//            if (d.getDeviceId().equalsIgnoreCase(deviceId)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public ImageView iv_deviceIcon;
//        public OfflineView offline_view;
//        public TextView tv_name;
//        public ImageView iv_select;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            iv_deviceIcon = (ImageView) itemView
//                    .findViewById(R.id.ivDeviceIcon);
//            tv_name = (TextView) itemView
//                    .findViewById(R.id.tvDeviceName);
//            iv_select = (ImageView) itemView
//                    .findViewById(R.id.iv_select);
//            offline_view = (OfflineView) itemView.findViewById(R.id.offline_view);
//        }
//    }
//
//    public static class NameHolder extends RecyclerView.ViewHolder {
//        public TextView tv_name;
//
//        public NameHolder(View itemView) {
//            super(itemView);
//            tv_name = (TextView) itemView
//                    .findViewById(R.id.tv_name);
//        }
//    }
//}
