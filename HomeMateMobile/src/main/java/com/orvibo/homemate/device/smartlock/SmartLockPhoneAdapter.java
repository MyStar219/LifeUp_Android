package com.orvibo.homemate.device.smartlock;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 最近使用手机号码
 * Created by snown on 2015/11/27
 */
public class SmartLockPhoneAdapter extends BaseAdapter {

    private Map<String, String> mapData;
    private List<String> phoneDatas = new ArrayList<>();

    public SmartLockPhoneAdapter(Map<String, String> mapData) {
        this.mapData = mapData;
        if (mapData.size() > 0) {
            for (Map.Entry<String, String> entry : mapData.entrySet()) {
                phoneDatas.add(entry.getKey());
            }
        }
        Collections.reverse(phoneDatas);
    }


    // 获取数据量
    @Override
    public int getCount() {
        return phoneDatas.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public String getItem(int position) {
        return phoneDatas.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.list_item_smart_lock_phone, null);
            holder.phoneItem = (TextView) convertView.findViewById(R.id.phoneItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final String phone = phoneDatas.get(position);
        String phoneName = mapData.get(phone);
        if (phone.equalsIgnoreCase(phoneName))
            holder.phoneItem.setText(StringUtil.splitString(phoneName));
        else
            holder.phoneItem.setText(StringUtil.splitString(phoneName) + "   " + phone);
        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public TextView phoneItem;
    }
}
