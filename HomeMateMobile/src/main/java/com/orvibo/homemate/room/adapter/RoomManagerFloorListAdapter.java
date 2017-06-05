package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;

import java.util.HashSet;
import java.util.List;

/**
 * @author Smagret
 * @data 2015/03/28
 */
public class RoomManagerFloorListAdapter extends BaseAdapter {

    private Context context;
    private List<Floor> floorList;
    private HashSet<Integer> selectedItemPositionSet = new HashSet<Integer>();
    private boolean IS_CHECKED = false;

    public RoomManagerFloorListAdapter(Context context, List<Floor> floorList) {
        this.context = context;
        this.floorList = floorList;
    }

    @Override
    public int getCount() {
        return floorList == null ? 0 : floorList.size();
    }

    @Override
    public Object getItem(int position) {
        return floorList == null ? null : floorList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.floor_select_item, null);
            holder = new ViewHolder();
            holder.floorName = (TextView) convertView.findViewById(R.id.textViewFloorName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RadioButton radio = (RadioButton) convertView.findViewById(R.id.btnRadio);
        holder.btnRadio = radio;

        holder.floorName.setText(floorList.get(position).getFloorName());

        IS_CHECKED = false;

        if (selectedItemPositionSet.contains(position)) {
            IS_CHECKED = true;
        }
        holder.btnRadio.setChecked(IS_CHECKED);
        return convertView;
    }

    static class ViewHolder {
        TextView floorName;
        RadioButton btnRadio;
    }

    public void setchecked(int position) {
        if (!selectedItemPositionSet.contains(position)) {
            selectedItemPositionSet.clear();
            selectedItemPositionSet.add(position);
        }
        notifyDataSetChanged();
    }
}
