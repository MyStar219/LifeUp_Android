package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;

import java.util.List;

/**
 * Created by snown on 2015/10/13.
 */
public class SceneBindTipAdapter extends BaseAdapter {
    private Context mContext= ViHomeProApp.getContext();
    private List<CharSequence> tips;

    public SceneBindTipAdapter( List<CharSequence> tips) {
        this.tips = tips;
    }


    @Override
    public int getCount() {
        return tips.size();
    }

    @Override
    public Object getItem(int position) {
        return tips.get(position);
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
            convertView =  LayoutInflater.from(mContext).inflate(R.layout.item_bind_fail, parent, false);
            holder.roomName_tv = (TextView) convertView.findViewById(R.id.roomName_tv);
            holder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CharSequence tip = tips.get(position);
        holder.roomName_tv.setText(tip);
        holder.deviceName_tv.setVisibility(View.GONE);
        return convertView;
    }

    private class ViewHolder {
        private TextView roomName_tv;
        private TextView deviceName_tv;
    }
}
