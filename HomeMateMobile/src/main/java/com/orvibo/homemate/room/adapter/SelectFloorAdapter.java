package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;

import java.util.List;

public class SelectFloorAdapter extends BaseAdapter {
    private Context mContext = null;
    private LayoutInflater mInflater;
    private List<Floor> mFloors;
    private String mSelectFloorId;
    private final int mNormalFontColor;
    private final int mSelectedFontColor;
    private View.OnClickListener mClickListener;

    public SelectFloorAdapter(Context context, List<Floor> floors, String selectedFloorId, View.OnClickListener listener) {
        mContext = context;
        mFloors = floors;
        mSelectFloorId = selectedFloorId;
        mInflater = LayoutInflater.from(context);
        mNormalFontColor = context.getResources().getColor(R.color.identity_tip);
        mSelectedFontColor = context.getResources().getColor(R.color.green);
        mClickListener = listener;
    }

    public void setData(List<Floor> floors) {
        mFloors = floors;
        notifyDataSetChanged();
    }

    public void selectFloor(String floorId) {
        mSelectFloorId = floorId;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (null != mFloors) ? mFloors.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (null != mFloors && mFloors.size() > position) ? mFloors.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_select_floor, parent, false);
            holder.floorNameTextView = (TextView) convertView.findViewById(R.id.floorNameTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Floor floor = mFloors.get(position);
        holder.floorNameTextView.setText(floor.getFloorName());
        holder.floorNameTextView.setTextColor(floor.getFloorId().equals(mSelectFloorId) ? mSelectedFontColor : mNormalFontColor);
        if (mClickListener != null) {
            holder.floorNameTextView.setOnClickListener(mClickListener);
        }

        holder.floorNameTextView.setTag(R.id.tag_floor, floor);
        convertView.setTag(R.id.tag_floor, floor);
        return convertView;
    }

    private class ViewHolder {
        private TextView floorNameTextView;
    }
}
