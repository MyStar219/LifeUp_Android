package com.orvibo.homemate.messagepush;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.MessagePushUtil;
import com.orvibo.homemate.util.WeekUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Allen on 2015/10/14
 */
public class MessageSettingAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View.OnClickListener mClickListener;
    private List<MessagePush> messagePushes;
    private Map<String, Device> deviceMap;
    private MessagePushDao messagePushDao;
    private ArrayList<Device> zigbeeDevices;
    private boolean showEnergyRemind;

    public MessageSettingAdapter(Context context, View.OnClickListener onClickListener, List<Device> devices, List<Device> zigbeeDevices) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mClickListener = onClickListener;
        messagePushes = new ArrayList<MessagePush>();
        deviceMap = new HashMap<String, Device>();
        messagePushDao = new MessagePushDao();
        this.zigbeeDevices = (ArrayList)zigbeeDevices;
        init(devices);
    }

    private void init(List<Device> devices) {
        List<MessagePush> timerMessagePush = new ArrayList<MessagePush>();
        List<MessagePush> sensorMessagePush = new ArrayList<MessagePush>();
        ProductManage pm = ProductManage.getInstance();
        for (Device device : devices) {
            String deviceId = device.getDeviceId();
            deviceMap.put(deviceId, device);
            MessagePush messagePush = messagePushDao.selMessagePushByDeviceId(deviceId);
            if (messagePush == null) {
                messagePush = new MessagePush();
                messagePush.setTaskId(deviceId);
                messagePush.setIsPush(MessagePushStatus.ON);
                messagePush.setStartTime("00:00:00");
                messagePush.setEndTime("00:00:00");
                if (pm.isWifiDevice(device)) {
                    messagePush.setType(MessagePushType.SINGLE_TIMER_TYPE);
                } else if (DeviceUtil.isSensorDevice(device.getDeviceType())) {
                    messagePush.setType(MessagePushType.SINGLE_SENSOR_TYPE);
                    messagePush.setWeek(255);
                }
            }
            int type = messagePush.getType();
            if (type == MessagePushType.SINGLE_TIMER_TYPE) {
                timerMessagePush.add(messagePush);
            } else if (type == MessagePushType.SINGLE_SENSOR_TYPE) {
//                sensorMessagePush.add(messagePush);
            }
        }
        String userId = UserCache.getCurrentUserId(mContext);
//        if (!sensorMessagePush.isEmpty()) {
//            MessagePush messagePush = messagePushDao.selAllSetMessagePushByType(userId, MessagePushType.ALL_SENSOR_TYPE);
//            if (messagePush == null) {
//                messagePush = new MessagePush();
//                messagePush.setIsPush(MessagePushStatus.ON);
//                messagePush.setType(MessagePushType.ALL_SENSOR_TYPE);
//            }
//            messagePushes.add(messagePush);
//            if (messagePush.getIsPush() == MessagePushStatus.ON) {
//                messagePushes.addAll(sensorMessagePush);
//            }
//        }
        if (!timerMessagePush.isEmpty()) {
            MessagePush messagePush = messagePushDao.selAllSetMessagePushByType(userId, MessagePushType.All_TIMER_TYPE);
            if (messagePush == null) {
                messagePush = new MessagePush();
                messagePush.setIsPush(MessagePushStatus.ON);
                messagePush.setType(MessagePushType.All_TIMER_TYPE);
            }
            messagePushes.add(messagePush);
            if (timerMessagePush.size() > 1 && messagePush.getIsPush() == MessagePushStatus.ON) {
                messagePushes.addAll(timerMessagePush);
            }
        }
    }

    public void refresh(List<Device> devices) {
        messagePushes.clear();
        deviceMap.clear();
        init(devices);
        this.notifyDataSetChanged();
    }

    public boolean isShowEnergyRemind() {
        return showEnergyRemind;
    }

    @Override
    public int getCount() {
//        return zigbeeDevices.size() > 0 ? messagePushes.size() + 1 : messagePushes.size();
        int size = new DeviceDao().selZigbeeLampCount(UserCache.getCurrentMainUid(mContext));
        if (size > 0) {
            showEnergyRemind = true;
        }
        return messagePushes.size()+ (showEnergyRemind?1:0);
    }

    @Override
    public Object getItem(int position) {
        if (messagePushes.size() > position) {
            return messagePushes.get(position);
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
        if (position == getCount()-1 && showEnergyRemind) {
            convertView = mLayoutInflater.inflate(R.layout.message_setting_energy_saving, null);
        } else {
            MessagePush messagePush = messagePushes.get(position);
            if (messagePush.getType() == MessagePushType.All_TIMER_TYPE) {
                convertView = mLayoutInflater.inflate(R.layout.message_setting_timer_group, null);
                ImageView infoPushSwitchImageView = (ImageView) convertView.findViewById(R.id.allInfoPushSetImageView);
                infoPushSwitchImageView.setOnClickListener(mClickListener);
                infoPushSwitchImageView.setTag(messagePush);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    infoPushSwitchImageView.setImageLevel(0);
                } else {
                    infoPushSwitchImageView.setImageLevel(1);
                }
            } else if (messagePush.getType() == MessagePushType.SINGLE_TIMER_TYPE) {
                convertView = mLayoutInflater.inflate(R.layout.message_setting_timer_item, null);
                TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
                deviceNameTextView.setText(deviceMap.get(messagePush.getTaskId()).getDeviceName());
                ImageView infoPushSwitchImageView = (ImageView) convertView.findViewById(R.id.infoPushSwitchImageView);
                infoPushSwitchImageView.setOnClickListener(mClickListener);
                infoPushSwitchImageView.setTag(messagePush);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    infoPushSwitchImageView.setImageLevel(0);
                } else {
                    infoPushSwitchImageView.setImageLevel(1);
                }
            } else if (messagePush.getType() == MessagePushType.ALL_SENSOR_TYPE) {
                convertView = mLayoutInflater.inflate(R.layout.message_setting_sensor_group, null);
                ImageView infoPushSwitchImageView = (ImageView) convertView.findViewById(R.id.allInfoPushSetImageView);
                infoPushSwitchImageView.setOnClickListener(mClickListener);
                infoPushSwitchImageView.setTag(messagePush);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    infoPushSwitchImageView.setImageLevel(0);
                } else {
                    infoPushSwitchImageView.setImageLevel(1);
                }
            } else if (messagePush.getType() == MessagePushType.SINGLE_SENSOR_TYPE) {
                convertView = mLayoutInflater.inflate(R.layout.message_setting_sensor_item, null);
                TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
                Device device = deviceMap.get(messagePush.getTaskId());
                deviceNameTextView.setText(device.getDeviceName());
                TextView timeIntervalTextView = (TextView) convertView.findViewById(R.id.timeIntervalTextView);
                TextView timeRepeatTextView = (TextView) convertView.findViewById(R.id.timeRepeatTextView);
                if (messagePush.getIsPush() == MessagePushStatus.OFF) {
                    timeRepeatTextView.setText(mContext.getString(R.string.device_timing_action_shutdown));
                    timeIntervalTextView.setVisibility(View.GONE);
                } else if (messagePush.getStartTime().equals(messagePush.getEndTime())) {
                    timeIntervalTextView.setVisibility(View.GONE);
                    timeRepeatTextView.setText(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
                } else {
                    timeIntervalTextView.setText(MessagePushUtil.getTimeInterval(mContext, messagePush));
                    timeIntervalTextView.setVisibility(View.VISIBLE);
                    timeRepeatTextView.setText(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
                }
                convertView.setTag(device);
            }
        }
        return convertView;
    }

}
