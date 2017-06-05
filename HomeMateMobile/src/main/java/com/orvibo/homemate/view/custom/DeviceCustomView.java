package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;

/**
 * Created by snown on 2016/1/11.
 * 自定义的设备信息，包括icon，名称，位置
 */
public class DeviceCustomView extends LinearLayout {
    private OfflineView deviceOffLine;//图标作为背景
    private TextView deviceName;
    public MyCountdownTextView deviceRoom;
    private ImageView deviceIcon;
    private Context context = ViHomeProApp.getContext();

    public DeviceCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.item_device, this);
        deviceOffLine = (OfflineView) findViewById(R.id.offline_view);
        deviceName = (TextView) findViewById(R.id.deviceName);
        deviceRoom = (MyCountdownTextView) findViewById(R.id.deviceRoom);
        deviceIcon = (ImageView) findViewById(R.id.deviceIcon);
    }

    /**
     * 设置icon
     *
     * @param isOnLine 设备是否在线
     * @param bg       设备图标
     */
    public void setIsOnLine(boolean isOnLine, Drawable bg) {
        setIsOnLine(isOnLine, bg, null);
    }

    /**
     * 设置icon
     *
     * @param isOnLine 设备是否在线
     * @param drawable 设备图标
     * @param text     可以为离线，也可以自行设置
     */
    public void setIsOnLine(boolean isOnLine, Drawable drawable, String text) {
        deviceIcon.setVisibility(isOnLine ? VISIBLE : GONE);
        deviceOffLine.setVisibility(!isOnLine ? VISIBLE : GONE);
        if (isOnLine) {
            deviceIcon.setImageDrawable(drawable);
        } else {
            if (TextUtils.isEmpty(text)) {
                deviceOffLine.setText(context.getString(R.string.offline));
            } else {
                deviceOffLine.setText(text);
            }
        }

    }

    /**
     * 设置设备名称
     *
     * @param deviceNameStr
     */
    public void setDeviceInfo(String deviceNameStr) {
        setDeviceInfo(deviceNameStr, null);
    }

    /**
     * 设置设备名称和房间名称
     *
     * @param deviceNameStr
     * @param deviceRoomStr
     */
    public void setDeviceInfo(String deviceNameStr, String deviceRoomStr) {
        if (!TextUtils.isEmpty(deviceNameStr))
            deviceName.setText(deviceNameStr);
        else
            deviceName.setText("");
        if (!TextUtils.isEmpty(deviceRoomStr)) {
            deviceRoom.setVisibility(VISIBLE);
            deviceRoom.setText(deviceRoomStr);
        } else {
            deviceRoom.setVisibility(GONE);
        }
    }

    public void setTextColor(int nameColor, int roomColor) {
        deviceName.setTextColor(nameColor);
        deviceRoom.setTextColor(roomColor);
    }
}
