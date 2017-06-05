package com.orvibo.homemate.device.manage.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApConstant;
import com.orvibo.homemate.ap.EntityWifi;
import com.orvibo.homemate.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Allen on 2015/8/13.
 */
public class ApWifiAdapter extends BaseAdapter {

    private List<EntityWifi> entityWifis = new ArrayList<EntityWifi>();
    private String checkedSSID;

    public void addEntityWifi(EntityWifi entityWifi, List<String> filterSSIDS) {
        String ssid = entityWifi.getSsid();
        for (String filterSSID : filterSSIDS) {
            if (ssid.contains(filterSSID)) {
                return;
            }
        }
        if (ssid != null) {
            int index = 0;
            boolean contains = false;
            int rssi = entityWifi.getRssi();
            for (int i = 0; i < entityWifis.size(); i++) {
                EntityWifi temp = entityWifis.get(i);
                if (temp.getSsid().equals(entityWifi.getSsid())) {
                    contains = true;
                    break;
                }
                if (rssi < temp.getRssi()) {
                    index = i + 1;
                }
            }
            if (!contains) {
                LogUtil.d(this.getClass().getName(), "addEntityWifi()-rssi:" + rssi + " index:" + index);
                entityWifis.add(index, entityWifi);
            }
        }
        notifyDataSetChanged();
    }

    public void clearEntityWifis() {
        entityWifis.clear();
        notifyDataSetChanged();
    }

    public void setCheckedSSID(String ssid) {
        this.checkedSSID = ssid;
        notifyDataSetChanged();
    }

    public EntityWifi getEntityWifiByPosition(int position) {
        return entityWifis.get(position);
    }

    @Override
    public int getCount() {
        return entityWifis == null ? 0 : entityWifis.size();
    }

    @Override
    public Object getItem(int position) {
        return entityWifis == null || position > entityWifis.size() - 1 ? null : entityWifis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.ap_wifi_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivDivide = (ImageView) convertView.findViewById(R.id.ivDivide);
            viewHolder.ivChecked = (ImageView) convertView.findViewById(R.id.ivChecked);
            viewHolder.ivLock = (ImageView) convertView.findViewById(R.id.ivLock);
            viewHolder.ivRssi = (ImageView) convertView.findViewById(R.id.ivRssi);
            viewHolder.ivDivideEnd = (ImageView) convertView.findViewById(R.id.ivDivideEnd);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (position != 0) {
            params.setMargins(convertView.getResources().getDimensionPixelSize(R.dimen.margin_x4), 0, 0, 0);
        } else {
            params.setMargins(0, 0, 0, 0);
        }
        viewHolder.ivDivide.setLayoutParams(params);
        if (position == getCount() - 1) {
            viewHolder.ivDivideEnd.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivDivideEnd.setVisibility(View.GONE);
        }
        EntityWifi entityWifi = entityWifis.get(position);
        String ssid = entityWifi.getSsid();
        if (ssid != null) {
            viewHolder.tvName.setText(ssid);
        }
        if (ssid != null && ssid.equals(checkedSSID)) {
            viewHolder.ivChecked.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivChecked.setVisibility(View.INVISIBLE);
        }
        String auth = entityWifi.getAuth();
        String enc = entityWifi.getEnc();
        if ((auth == null || auth.equals(ApConstant.AUTH_OPEN)) && (enc == null || enc.equals(ApConstant.ENC_NONE))) {
            viewHolder.ivLock.setVisibility(View.GONE);
        } else {
            viewHolder.ivLock.setVisibility(View.VISIBLE);
        }
        int rssi = entityWifi.getRssi();
        if (rssi < 34) {
            viewHolder.ivRssi.setBackgroundResource(R.drawable.ap_wifi_rssi1);
        } else if (rssi < 67) {
            viewHolder.ivRssi.setBackgroundResource(R.drawable.ap_wifi_rssi2);
        } else {
            viewHolder.ivRssi.setBackgroundResource(R.drawable.ap_wifi_rssi3);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView ivDivide, ivChecked, ivLock, ivRssi, ivDivideEnd;
        TextView tvName;
    }
}
