package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.util.FloorAndRoomUtil;

import java.util.List;

public class RoomTypeAdapter extends BaseAdapter {
    private static final String TAG = RoomTypeAdapter.class.getSimpleName();
    private Context mContext = null;
    private LayoutInflater mInflater;
    private List<Integer> mRoomTypes;
    private int mSelectedRoomType;
    private final int mNormalFontColor;
    private final int mSelectedFontColor;

    public RoomTypeAdapter(Context context, int selectedRoomType) {
        mContext = context;
        mSelectedRoomType = selectedRoomType;
        mInflater = LayoutInflater.from(context);
        mRoomTypes = FloorAndRoomUtil.getAllRoomType();

        mNormalFontColor = context.getResources().getColor(R.color.identity_tip);
        mSelectedFontColor = context.getResources().getColor(R.color.green);
    }

    public void selectRoomType(int roomType) {
        mSelectedRoomType = roomType;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (null != mRoomTypes) ? mRoomTypes.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (null != mRoomTypes && mRoomTypes.size() > position) ? mRoomTypes.get(position) : null;
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
            convertView = mInflater.inflate(R.layout.item_select_room, parent, false);
            holder.roomNameTextView = (TextView) convertView.findViewById(R.id.roomNameTextView);
            holder.roomTypeImageView = (ImageView) convertView.findViewById(R.id.roomTypeImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final int roomType = mRoomTypes.get(position);
        holder.roomNameTextView.setText(FloorAndRoomUtil.getRoomNameByRoomType(roomType));
        holder.roomNameTextView.setTextColor(roomType == mSelectedRoomType ? mSelectedFontColor : mNormalFontColor);
        holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(roomType, false));

        if (roomType == mSelectedRoomType) {
            holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(roomType, true));
        } else {
            holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(roomType, false));
        }

        convertView.setTag(R.id.tag_roomType, roomType);
        return convertView;
    }

    private class ViewHolder {
        private TextView roomNameTextView;
        private ImageView roomTypeImageView;
    }
}
