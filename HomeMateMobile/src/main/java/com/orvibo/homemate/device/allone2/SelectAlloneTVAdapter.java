package com.orvibo.homemate.device.allone2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;

import java.util.HashMap;
import java.util.List;

/**
 * allone 机顶盒绑定电视
 * Created by Smagret on 2016/4/6.
 */
public class SelectAlloneTVAdapter extends BaseAdapter {
    private static final String TAG = SelectAlloneTVAdapter.class.getSimpleName();
    private LayoutInflater            mInflater;
    private List<Device>              mDevices;
    public  static HashMap<Integer, Boolean> isSelected;

    public SelectAlloneTVAdapter(Context context, List<Device> devices) {
        this.mDevices = devices;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void refresh(List<Device> devices) {
        this.mDevices = devices;
        notifyDataSetChanged();
    }

    // 初始化 设置所有checkbox都为未选择
    public void init() {
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < mDevices.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return (mDevices == null || position >= mDevices.size()) ? null : mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlloneTVHolder alloneTVHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_select_allone_tv,
                    parent, false);
            alloneTVHolder = new AlloneTVHolder();
            alloneTVHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            alloneTVHolder.leftButton = (Button) convertView.findViewById(R.id.leftButton);
            alloneTVHolder.rightButton = (Button) convertView.findViewById(R.id.rightButton);
            alloneTVHolder.cbDevice = (CheckBox) convertView
                    .findViewById(R.id.cbDevice);
            convertView.setTag(alloneTVHolder);
        } else {
            alloneTVHolder = (AlloneTVHolder) convertView.getTag();
        }
        convertView.setBackgroundResource(R.drawable.item_selector);
        Device device = mDevices.get(position);

//        if (!TextUtils.isEmpty(selectDeviceId) && selectDeviceId.equals(deviceId)) {
//            normalDeviceHolder.cbDevice.setChecked(true);
//        } else {
//            normalDeviceHolder.cbDevice.setChecked(false);
//        }
        alloneTVHolder.cbDevice.setChecked(isSelected.get(position));
        alloneTVHolder.cbDevice.setTag(R.id.tag_device, device);
        alloneTVHolder.deviceName_tv.setText(device.getDeviceName());
        convertView.setTag(R.id.tag_device, device);
        return convertView;
    }

    public void setChecked(int position){

    }

    class AlloneTVHolder {
        private TextView deviceName_tv;
        private CheckBox cbDevice;
        private Button   leftButton;
        private Button   rightButton;

    }
}
