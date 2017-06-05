package com.orvibo.homemate.messagepush;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Message;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;

import java.util.List;

/**
 * Created by smagret on 2015/8/21.
 */
public class MessageAdapter extends BaseAdapter {

    private final String TAG = MessageAdapter.class.getSimpleName();
    private List<Message> infoPushMsgs;
    private Context mContext;
    private String previousDeviceId;

    public MessageAdapter(Context context, List<Message> infoPushMsgs) {
        this.infoPushMsgs = infoPushMsgs;
        mContext = context;
    }

    // 获取数据量
    @Override
    public int getCount() {
        return infoPushMsgs.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public Message getItem(int position) {
        return infoPushMsgs.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.d(TAG, "getView() - position = " + position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.message_item, null);
            holder.deviceIconImageView = (ImageView) convertView.findViewById(R.id.deviceIconImageView);
            holder.deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
            holder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            holder.messageTimeTextView = (TextView) convertView.findViewById(R.id.messageTimeTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Message infoPushMsg = infoPushMsgs.get(position);
            String deviceId = infoPushMsg.getDeviceId();
            Device device = new DeviceDao().selDevice(deviceId);
            String text = infoPushMsg.getText();
//            int index = text.indexOf(":");
            if (device != null) {
//                String deviceName = device.getDeviceName();
                holder.deviceNameTextView.setText(device.getDeviceName());
            } else {
                holder.deviceNameTextView.setText("");
            }
            if (position > 0) {
                Message previousInfoPushMsg = infoPushMsgs.get(position - 1);
                    if (!StringUtil.isEmpty(previousInfoPushMsg.getDeviceId())) {
                        previousDeviceId = previousInfoPushMsg.getDeviceId();
                    }

                    if (deviceId.equals(previousDeviceId)) {
                        holder.deviceIconImageView.setVisibility(View.INVISIBLE);
                        holder.deviceNameTextView.setVisibility(View.GONE);
                    } else {
                        holder.deviceIconImageView.setVisibility(View.VISIBLE);
                        holder.deviceNameTextView.setVisibility(View.VISIBLE);

                    }

            } else if (position == 0) {
                holder.deviceIconImageView.setVisibility(View.VISIBLE);
                holder.deviceNameTextView.setVisibility(View.VISIBLE);
            }

            holder.deviceIconImageView.setBackgroundDrawable(DeviceTool.getPushDeviceDrawable(device));
            holder.messageTextView.setText(text);
            holder.messageTimeTextView.setText(TimeUtil.secondToDateString(infoPushMsg.getTime()));

        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public ImageView deviceIconImageView;
        public TextView deviceNameTextView;
        public TextView messageTextView;
        public TextView messageTimeTextView;
    }

}
