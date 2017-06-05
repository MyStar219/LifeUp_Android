package com.orvibo.homemate.device.allone2.add;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.util.List;


/**
 * 最近使用手机号码
 * Created by snown on 2015/11/27
 */
public class StbDataAdapter extends BaseAdapter {

    private List<String> names;

    public StbDataAdapter(List<String> names) {
        this.names = names;
    }


    // 获取数据量
    @Override
    public int getCount() {
        return names.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public String getItem(int position) {
        return names.get(position);
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
            convertView = View.inflate(parent.getContext(), R.layout.item_stb_text, null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(names.get(position));
        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public TextView name;
    }
}
