package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SmartScene;
import com.orvibo.homemate.data.LinkageActiveType;
import com.orvibo.homemate.util.DeviceTool;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Smagret on 2015/10/14.
 * 目前情景不显示全开全关情景了
 */
public class DeviceIconsAdapter extends BaseAdapter {
    private static final String TAG = DeviceIconsAdapter.class.getSimpleName();
    private Resources mRes;
    private List<Integer> deviceTypes;
    private SmartScene mSmartScene;
    private boolean mIsCountdownStarted = false;

    public DeviceIconsAdapter(Context context, List<Integer> deviceTypes) {
        this.deviceTypes = deviceTypes;
        mRes = context.getResources();
    }

    public DeviceIconsAdapter(Context context, List<Integer> deviceTypes, SmartScene smartScene) {
        this(context, deviceTypes);
        this.mSmartScene = smartScene;
    }

    public void refresh(List<Integer> deviceTypes) {
        this.deviceTypes = deviceTypes;
        notifyDataSetChanged();
    }

    public SmartScene getSmartScene() {
        return mSmartScene;
    }

    public void setIsCountdownStarted(boolean isCountdownStarted) {
        mIsCountdownStarted = isCountdownStarted;
    }

    @Override
    public int getCount() {
        return deviceTypes == null ? 0 : deviceTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceTypes == null ? null : deviceTypes.get(position);
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
            convertView = View.inflate(parent.getContext(), R.layout.device_icon_item, null);
            holder.deviceIconImageView = (ImageView) convertView.findViewById(R.id.deviceIconImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int deviceType = deviceTypes.get(position);
        // holder.deviceIconImageView.setBackgroundResource(DeviceTool.getDeviceIconResIdByDeviceType(deviceType));
        if (mSmartScene.scene != null) {
            holder.deviceIconImageView.setBackgroundResource(DeviceTool.getDeviceIconResIdByDeviceType2scene(deviceType));
        } else if (mSmartScene.scene == null && mSmartScene.linkage == null) {
            if (mSmartScene.security.getIsArm() == Security.SECURITY) {
                if (mSmartScene.security.getSecType() == Security.LEAVE_HOME) {
                    if (!mIsCountdownStarted) {
                        holder.deviceIconImageView.setBackgroundResource(DeviceTool.getSensorDeviceIconResIdByDeviceType2Security(deviceType));
                    } else {
                        holder.deviceIconImageView.setBackgroundResource(DeviceTool.getSensorDeviceIconResIdByDeviceType2Unsecurity(deviceType));
                    }
                } else {
                    holder.deviceIconImageView.setBackgroundResource(DeviceTool.getSensorDeviceIconResIdByDeviceType2Security(deviceType));
                }
            } else {
                holder.deviceIconImageView.setBackgroundResource(DeviceTool.getSensorDeviceIconResIdByDeviceType2Unsecurity(deviceType));
            }

        } else {
            if (mSmartScene.linkage.getIsPause() == LinkageActiveType.ACTIVE) {
                holder.deviceIconImageView.setBackgroundResource(DeviceTool.getDeviceIconResIdByDeviceType2linkage(deviceType));
            } else {
                holder.deviceIconImageView.setBackgroundResource(DeviceTool.getDeviceIconResIdByDeviceType2stop(deviceType));
            }
        }
        return convertView;
    }

    /**
     * 手动执行的场景下方放设备品类图标（按执行时间），最多放5个
     * 品类不超过5个时，有几类显示几个图标。
     * 品类超过5个时，显示前5个品类的图标。
     * 目前品类有：灯光，插座，电视，空调，窗帘
     */
    class ViewHolder {
        private ImageView deviceIconImageView;
    }
}
