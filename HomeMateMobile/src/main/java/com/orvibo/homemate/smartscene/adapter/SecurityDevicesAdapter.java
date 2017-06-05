package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceCommenDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.OfflineView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityDevicesAdapter extends BaseAdapter {
    // private static final String TAG = SecurityDevicesAdapter.class.getSimpleName();
    private static final int ON_OFF_ITEM = 0;
    public static final int ARROW_ITEM = 1;

    private LayoutInflater mInflater;
    private List<Device> mDevices;
    private View.OnClickListener mClickListener;
    private final Resources mRes;
    private final int mFontNormalColor;
    private final int mFontOfflineColor;

    private DeviceCommenDao deviceCommenDao;

    private Map<String, DeviceStatus> mDeviceStatuses = new HashMap<String, DeviceStatus>();

    /**
     * 设备在线和离线状态。key:deviceId,value:true online,false offline
     */
    private Map<String, Boolean> mDeviceOOs = new HashMap<String, Boolean>();


    /**
     * @param context
     * @param devices
     * @param deviceStatuses
     */
    public SecurityDevicesAdapter(Context context, List<Device> devices,
                                  List<DeviceStatus> deviceStatuses, View.OnClickListener clickListener) {
//        this.mDevices = devices;
        this.mClickListener = clickListener;
        mInflater = LayoutInflater.from(context);
        setDeviceStatus(devices, deviceStatuses);

        this.mDevices = DeviceSort.sortSecurityDevices(devices);

        mRes = context.getResources();
        mFontNormalColor = mRes.getColor(R.color.font_black);
        mFontOfflineColor = mRes.getColor(R.color.font_offline);
        deviceCommenDao = new DeviceCommenDao();
    }

    private void setDeviceStatus(List<Device> devices,
                                 List<DeviceStatus> deviceStatuses) {
        if (deviceStatuses != null) {
            synchronized (this) {
                for (DeviceStatus deviceStatus : deviceStatuses) {
                    final String deviceId = deviceStatus.getDeviceId();
                    mDeviceStatuses.put(deviceStatus.getDeviceId(), deviceStatus);
                    for (Device device : devices) {
                        if (deviceId == device.getDeviceId()) {
                            mDeviceOOs
                                    .put(deviceId,
                                            deviceStatus.getOnline() == OnlineStatus.ONLINE);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void refresh(List<Device> devices, List<DeviceStatus> deviceStatuses) {
        setDeviceStatus(devices, deviceStatuses);
        this.mDevices = DeviceSort.sortSecurityDevices(devices);
        notifyDataSetChanged();
    }

    /**
     * 设备状态变化
     *
     * @param deviceStatus
     */
    public void refreshDeviceStatus(DeviceStatus deviceStatus) {
        synchronized (this) {
            mDeviceStatuses.put(deviceStatus.getDeviceId(), deviceStatus);
        }
        notifyDataSetChanged();
    }

    public void refreshOnline(String uid, String deviceId, int online) {
        //  LogUtil.d(TAG, "refreshOnline()-uid:" + uid + ",deviceId:" + deviceId + ",online:" + online);
        refreshOO(uid, deviceId, online);
    }

    private void refreshOO(String uid, String deviceId, int online) {
        synchronized (this) {
            mDeviceOOs.put(deviceId, online == OnlineStatus.ONLINE);
        }
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
    public int getItemViewType(int position) {
//    public int getItemViewType(Device device, int position) {
        final Device device = mDevices.get(position);
//        LogUtil.d(TAG, "getItemViewType()-device:" + device);
        int itemType = ARROW_ITEM;
        final int deviceType = device.getDeviceType();
        if (DeviceUtil.isNotSet(deviceType)) {
            // 此设备还没有设置过
            final int appDeviceId = device.getAppDeviceId();
            if (appDeviceId == AppDeviceId.SECURITY_SENSORS) {
                itemType = ON_OFF_ITEM;
            }
        } else {
            if (deviceType == DeviceType.FLAMMABLE_GAS
                    || deviceType == DeviceType.INFRARED_SENSOR
                    || deviceType == DeviceType.SMOKE_SENSOR
                    || deviceType == DeviceType.PANALARM) {
                itemType = ON_OFF_ITEM;
            }
        }
        return itemType;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Device device = mDevices.get(position);
        final int itemType = getItemViewType(position);
        OnOffViewHolder onOffHolder = null;
        ArrowViewHolder arrowViewHolder = null;
        if (convertView == null) {
            if (itemType == ON_OFF_ITEM) {
                onOffHolder = new OnOffViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_arm_disarm,
                        parent, false);
                onOffHolder.deviceIcon_iv = (ImageView) convertView
                        .findViewById(R.id.deviceIcon_iv);
                onOffHolder.deviceName_tv = (TextView) convertView
                        .findViewById(R.id.deviceName_tv);
                onOffHolder.deviceLocation_tv = (TextView) convertView
                        .findViewById(R.id.deviceLocation_tv);
                onOffHolder.ctrl_tv = (TextView) convertView
                        .findViewById(R.id.ctrl_tv);
                onOffHolder.tvAlarming = (TextView) convertView
                        .findViewById(R.id.tvAlarming);
                onOffHolder.tvAlarmingTips = (TextView) convertView
                        .findViewById(R.id.tvAlarmingTips);
                onOffHolder.offline_view = (OfflineView) convertView.findViewById(R.id.offline_view);
                convertView.setTag(onOffHolder);
            } else {
                arrowViewHolder = new ArrowViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_arrow_security,
                        parent, false);
                arrowViewHolder.deviceIcon_iv = (ImageView) convertView
                        .findViewById(R.id.deviceIcon_iv);
                arrowViewHolder.deviceName_tv = (TextView) convertView
                        .findViewById(R.id.deviceName_tv);
                arrowViewHolder.deviceLocation_tv = (TextView) convertView
                        .findViewById(R.id.deviceLocation_tv);
                arrowViewHolder.offline_view = (OfflineView) convertView
                        .findViewById(R.id.offline_view);
                convertView.setTag(arrowViewHolder);
            }
        } else {
            if (itemType == ON_OFF_ITEM) {
                onOffHolder = (OnOffViewHolder) convertView.getTag();
            } else {
                arrowViewHolder = (ArrowViewHolder) convertView.getTag();
            }
        }

        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        final String deviceName = device.getDeviceName();
//      final String location = deviceCommenDao.getDeviceRoomFloorNameStr(uid, deviceId);
        String location = DeviceCommenDao.floorNameAndRoomName(uid, deviceId);
        if (StringUtil.isEmpty(location)) {
            location = convertView.getContext().getString(R.string.not_set_floor_room);
        }
        // true在线，false离线
        boolean isOnline = true;
        synchronized (this) {
            try {
                isOnline = mDeviceOOs.get(deviceId);
            } catch (NullPointerException e) {
                //e.printStackTrace();
                // LogUtil.e(TAG, "getView()-mDeviceOOs:" + mDeviceOOs);
            }
        }
        //LogUtil.d(TAG, "getView()-device:" + device + ",isOnline:" + isOnline);
        Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
        DeviceStatus deviceStatus = null;
        if (mDeviceStatuses.containsKey(deviceId)) {
            deviceStatus = mDeviceStatuses.get(deviceId);
        }
        if (itemType == ON_OFF_ITEM) {
            onOffHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
            onOffHolder.deviceName_tv.setText(deviceName);
            onOffHolder.deviceName_tv.setTextColor(isOnline ? mFontNormalColor : mFontOfflineColor);
            onOffHolder.deviceLocation_tv.setText(location);
            int alarm = DeviceStatusConstant.NOT_ALARM;
            int status = DeviceStatusConstant.DISARM;
            int battery = DeviceStatusConstant.NORMAL_BATTERY;
            if (deviceStatus != null) {
                alarm = deviceStatus.getValue1();
                status = deviceStatus.getAlarmType();
                battery = deviceStatus.getValue3();
            }
            if (alarm == DeviceStatusConstant.NOT_ALARM) {
                onOffHolder.tvAlarming.setVisibility(View.GONE);
                onOffHolder.tvAlarmingTips.setVisibility(View.GONE);
                onOffHolder.tvAlarming.clearAnimation();
            } else if (alarm == DeviceStatusConstant.ALARM) {
                onOffHolder.tvAlarming.setVisibility(View.VISIBLE);
                onOffHolder.tvAlarmingTips.setVisibility(View.VISIBLE);
                AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(convertView.getContext(), R.anim.security_alarm);
                onOffHolder.tvAlarming.startAnimation(animationSet);
            }
            onOffHolder.ctrl_tv.setOnClickListener(mClickListener);
            onOffHolder.ctrl_tv.setContentDescription(status + "");// 缓存当前设备的状态，控制时直接获取到device状态
            if (status == DeviceStatusConstant.ARM) {
                onOffHolder.ctrl_tv.setText(R.string.security_disarm);
                onOffHolder.ctrl_tv.setBackgroundResource(R.drawable.security_disarm_small_btn);
            } else {
                onOffHolder.ctrl_tv.setText(R.string.security_arm);
                onOffHolder.ctrl_tv.setBackgroundResource(R.drawable.security_arm_small_btn);
            }
            onOffHolder.ctrl_tv.setTag(R.id.tag_device, device);

            if (isOnline) {
                if (battery == DeviceStatusConstant.NORMAL_BATTERY) {
                    onOffHolder.offline_view.setVisibility(View.GONE);
                } else {
                    onOffHolder.offline_view.setVisibility(View.VISIBLE);
                    onOffHolder.offline_view.setText(R.string.security_device_low_battery);
                    onOffHolder.offline_view.setTextColor(mRes.getColor(R.color.red));
                }
            } else {
                onOffHolder.offline_view.setVisibility(View.VISIBLE);
                onOffHolder.offline_view.setText(R.string.offline);
                onOffHolder.offline_view.setTextColor(mRes.getColor(R.color.font_offline));
            }
        } else if (itemType == ARROW_ITEM) {
            arrowViewHolder.deviceIcon_iv.setImageDrawable(deviceIcon);
            arrowViewHolder.deviceName_tv.setText(deviceName);
            arrowViewHolder.deviceName_tv.setTextColor(isOnline ? mFontNormalColor : mFontOfflineColor);
            arrowViewHolder.deviceLocation_tv.setText(location);
//            arrowViewHolder.deviceLocation_tv.setText(DeviceCommenDao.bindItemName(uid, deviceId));
            convertView.setTag(R.id.tag_device, device);
            arrowViewHolder.offline_view.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        }

        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    private class OnOffViewHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView deviceLocation_tv;
        private TextView ctrl_tv;
        private TextView tvAlarming;
        private TextView tvAlarmingTips;
        private OfflineView offline_view;
    }

    private class ArrowViewHolder {
        private ImageView deviceIcon_iv;
        private TextView deviceName_tv;
        private TextView deviceLocation_tv;
        private OfflineView offline_view;
    }

}
