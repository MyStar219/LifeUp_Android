package com.orvibo.homemate.device.bind.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqiyao on 2016/7/21 10:49.
 * <p/>
 * 选择小方子设备adapter，可以多选
 *
 * @version v1.10
 */
public class SelectAlloneChildDevicesAdapter extends BaseAdapter {
    private static final int ALLONE_CHILD_DEVICE = 1;
    private static final int ALLONE = 0;
    private static final String TAG = SelectAlloneChildDevicesAdapter.class.getSimpleName();
    private Context mContext;
    private List<Device> mDevices;
    private LayoutInflater mInflater;
    private final Resources mRes;
    private final int mFontNormalColor;
    private final int mFontOfflineColor;
    /**
     * key:deviceId,value:true有学习过红外码，false没学习过红外码，不让选择。
     */
    private Map<String, Boolean> mDeviceIrCodes = new HashMap<String, Boolean>();

    /**
     * 选中的设备
     */
    private ArrayList<Device> mCheckedDevices = new ArrayList<>();

    /**
     * @param context
     * @param devices        包括allone本身
     * @param checkedDevices 已选中的设备，不包括allone自己。
     */
    public SelectAlloneChildDevicesAdapter(Activity context, List<Device> devices, ArrayList<Device> checkedDevices) {
        if (devices == null) {
            devices = new ArrayList<>();
        }
        mDevices = devices;
        mCheckedDevices = checkedDevices;
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mFontOfflineColor = mRes.getColor(R.color.font_offline);
        mContext = context.getApplicationContext();
    }

    public void refresh(List<Device> devices, ArrayList<Device> checkedDevices) {
        mDevices = devices;
        mCheckedDevices = checkedDevices;
        notifyDataSetChanged();
    }

    public void selectDevices(ArrayList<Device> checkedDevices) {
        mCheckedDevices = checkedDevices;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Device device = mDevices.get(position);
        final int deviceType = device.getDeviceType();
        if (deviceType == DeviceType.ALLONE) {
            return ALLONE;
        }
        return ALLONE_CHILD_DEVICE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = mDevices.get(position);
        //  LogUtil.e(TAG, "getView()-mDevices:" + mDevices);
        ViewHolder holder = null;
        NameHolder nameHolder = null;
        final int itemType = getItemViewType(position);
        if (convertView == null) {
            if (itemType == ALLONE) {
                nameHolder = new NameHolder();
                convertView = mInflater.inflate(R.layout.item_name,
                        parent, false);
                nameHolder.tv_name = (TextView) convertView
                        .findViewById(R.id.tv_name);
                convertView.setTag(nameHolder);
            } else if (itemType == ALLONE_CHILD_DEVICE) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_allone_child_device_check,
                        parent, false);
                holder.iv_deviceIcon = (ImageView) convertView
                        .findViewById(R.id.ivDeviceIcon);
                holder.tv_name = (TextView) convertView
                        .findViewById(R.id.tvDeviceName);
                holder.iv_select = (ImageView) convertView
                        .findViewById(R.id.iv_select);
                holder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
                convertView.setTag(holder);
            }
        } else {
            if (itemType == ALLONE_CHILD_DEVICE) {
                holder = (ViewHolder) convertView.getTag();
            } else if (itemType == ALLONE) {
                nameHolder = (NameHolder) convertView.getTag();
            }
        }

        if (itemType == ALLONE_CHILD_DEVICE) {
            boolean isOnline = true;//true设备在线，可以选择设备。只有小方设备，设备离线也能选择并设置场景
            boolean isLearned;//true 复制的遥控器按键已经学习。true
            String deviceId = device.getDeviceId();
            if (ProductManage.isAlloneLearnDevice(device)) {
                if (KKIrDao.getInstance().sunDeviceIsLearned(device.getDeviceId())) {
                    isLearned = true;
                } else {
                    isLearned = false;
                }
            } else {
                isLearned = true;
            }
            boolean check = isLearned&&isOnline;//true不能选择，false可以选择
            String notLearned = mRes.getString(R.string.bind_device_no_ir);

            if (ProductManage.isAlloneLearnDevice(device)) {
                String key = deviceId;
                if (mDeviceIrCodes.containsKey(key)) {
                    check = mDeviceIrCodes.get(key);
                } else {
                    mDeviceIrCodes.put(key, check);
                }
                LogUtil.d(TAG, "getView()-device:" + device + ",check:" + check + ",mDeviceIrCodes:" + mDeviceIrCodes);
            }

            holder.tv_name.setText(device.getDeviceName());
            holder.tv_name.setTextColor(isOnline ? mFontNormalColor : mFontOfflineColor);
            holder.offline_view.setText(notLearned);

            //设备图标、未学习图标
            Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, check);
            holder.iv_deviceIcon.setImageDrawable(deviceIcon);
            if (check) {
                holder.iv_deviceIcon.setVisibility(View.VISIBLE);
                holder.offline_view.setVisibility(View.GONE);
            } else {
                //未学习图标
                holder.iv_deviceIcon.setVisibility(View.VISIBLE);
                holder.offline_view.setVisibility(View.VISIBLE);
            }
            holder.iv_select.setVisibility(check ? View.VISIBLE : View.INVISIBLE);

            if (isSelected(device)) {
                holder.iv_select.setImageResource(R.drawable.checkbox_selected);
            } else {
                holder.iv_select.setImageResource(R.drawable.allone2_select_child_device_selector);
            }
        } else if (itemType == ALLONE) {
            nameHolder.tv_name.setText(device.getDeviceName());
        }
        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    /**
     * 判断设备是否处于选中状态
     *
     * @param device
     * @return
     */
    private boolean isSelected(Device device) {
        final String deviceId = device.getDeviceId();
        Iterator<Device> iterator = mCheckedDevices.iterator();
        while (iterator.hasNext()) {
            Device d = iterator.next();
            if (d.getDeviceId().equalsIgnoreCase(deviceId)) {
                return true;
            }
        }
        return false;
    }

    private class ViewHolder {
        private ImageView iv_deviceIcon;
        private OfflineView offline_view;
        private TextView tv_name;
        private ImageView iv_select;
    }

    private class NameHolder {
        private TextView tv_name;
    }
}
