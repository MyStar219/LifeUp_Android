package com.orvibo.homemate.device.manage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//01-30 11:11:56.925 15662-15662/com.orvibo.homemate D/DevicesAdapter: getView()-position:0 start
public class DeviceAddAdapter extends BaseAdapter {
    private static final String TAG = DeviceAddAdapter.class.getSimpleName();

    private String hostName;//主机的名称第一位，其他按字母排序
    private Context context = ViHomeProApp.getContext();
    private boolean isCn = PhoneUtil.isCN(context);

    private int brandName;

    private LinkedHashMap<Integer, Integer> iconWithName = new LinkedHashMap<>();//icon和nanme的id组合
    private LinkedHashMap<String, Integer> nameHash = new LinkedHashMap<>();//nameid和name组合

    private List<String> names = new ArrayList<>();
    private List<Integer> nameIds = new ArrayList<>();

    public DeviceAddAdapter(LinkedHashMap<Integer, Integer> iconWithName) {
        this.iconWithName = iconWithName;
        setData(iconWithName);
    }


    public DeviceAddAdapter(LinkedHashMap<Integer, Integer> iconWithName, int brandName) {
        this.iconWithName = iconWithName;
        this.brandName = brandName;
        setData(iconWithName);
    }

    public void setData(LinkedHashMap<Integer, Integer> iconWithName) {
        for (Map.Entry<Integer, Integer> entry : iconWithName.entrySet()) {
            int key = entry.getKey();
            String name = context.getString(key);
            nameHash.put(name, key);
            nameIds.add(key);
            if (key == R.string.device_add_host) {
                hostName = context.getString(key);
            } else
                names.add(name);

        }
        if (!isCn)
            sortName();
    }

    // 获取数据量
    @Override
    public int getCount() {
        return iconWithName.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public Integer getItem(int position) {
        return isCn ? nameIds.get(position) : nameHash.get(names.get(position));
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.d(TAG, "getView()-position:" + position);
        ViewHolder holder;
        int nameId = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if (brandName == R.string.device_add_socket) {
                convertView = View.inflate(parent.getContext(), R.layout.device_add_soket_item, null);
                holder.deviceInfoTextView = (TextView) convertView.findViewById(R.id.deviceInfoTextView);
            } else
                convertView = View.inflate(parent.getContext(), R.layout.device_add_item, null);
            holder.deviceImageView = (ImageView) convertView.findViewById(R.id.deviceImageView);
            holder.deviceBrandTextView = (TextView) convertView.findViewById(R.id.deviceBrandTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.deviceImageView.setImageResource(iconWithName.get(nameId));
        holder.deviceBrandTextView.setText(nameId);
        if (holder.deviceInfoTextView != null) {
            if (nameId == R.string.device_add_other_socket) {
                holder.deviceInfoTextView.setVisibility(View.VISIBLE);
                holder.deviceInfoTextView.setText(parent.getResources().getString(R.string.device_add_other_socket_tip));
            } else {
                holder.deviceInfoTextView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public ImageView deviceImageView;
        public TextView deviceBrandTextView;
        public TextView deviceInfoTextView;
    }

    /**
     * 海外版本根据字母排序
     */
    private void sortName() {
        // 根据a-z进行排序
        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String name1, String name2) {
                if (name1.equals("@") || name2.equals("#")) {
                    return 1;
                } else if (name1.equals("#") || name2.equals("@")) {
                    return -1;
                } else {
                    return name1.compareTo(name2);
                }
            }
        });
        if (!TextUtils.isEmpty(hostName)) {
            names.add(0, hostName);
        }
    }
}
