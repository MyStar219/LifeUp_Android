package com.orvibo.homemate.room.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.util.AppTool;

import java.util.HashMap;
import java.util.List;

public class RoomGridViewAdapter extends BaseAdapter {
    private HashMap<Floor, List<Room>> roomHashMap;
    private Context mContext;
    private final String TAG = RoomGridViewAdapter.class.getSimpleName();
    private int itemW, itemH;
    private boolean DELETEIMG_SHOW = true;
    private int floorPosition;

    public RoomGridViewAdapter(Context context,
                               HashMap<Floor, List<Room>> roomHashMap, int floorPosition, boolean deleteImg_show) {
        this.mContext = context;
        this.roomHashMap = roomHashMap;
        this.floorPosition = floorPosition;
        DELETEIMG_SHOW = deleteImg_show;
        int[] p = AppTool.getScreenPixels((Activity) context);
        itemW = p[0] * 150 / 480;
        itemH = p[1] * 65 / 800;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getCount() {
        return ((List<Room>) (roomHashMap.values().toArray()[0])).size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getItem(int position) {
        return ((List<Room>) (roomHashMap.values().toArray()[0])).get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // LogUtil.d(TAG, "getView() - rooms.size = "
        // + ((List<Room>) (roomHashMap.values().toArray()[0])).size());

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.room_gv_item, null);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                    itemW, itemH);
            convertView.setLayoutParams(param);
            holder.room_name_tv = (TextView) convertView
                    .findViewById(R.id.roomNameTextView);
            holder.del_iv = (ImageView) convertView.findViewById(R.id.deleteRoomImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Room room = ((List<Room>) (roomHashMap.values().toArray()[0]))
                .get(position);
        String roomName = room.getRoomName();
        final Floor floor = (Floor) roomHashMap.keySet().toArray()[0];
        holder.room_name_tv.setText(roomName);
        holder.del_iv.setTag(room);
        holder.del_iv.setContentDescription(floorPosition + "|"
                + position);

        //该楼层包含的房间数目
        int roomSize = ((List<Room>) (roomHashMap.values().toArray()[0])).size();
        if (position < (roomSize - 1)) {
        } else {
            holder.del_iv.setVisibility(View.GONE);
            holder.room_name_tv.setTextColor(mContext.getResources().getColor(R.color.green));
        }
        if (!DELETEIMG_SHOW) {
            holder.del_iv.setVisibility(View.GONE);
        }
        holder.room_name_tv.setTag(room);
        holder.room_name_tv.setContentDescription(floorPosition + "|"
                + position + "|" + roomSize);
        return convertView;
    }

    public void refresh(String tzId) {
        this.notifyDataSetChanged();
    }

    private static class ViewHolder {
        private TextView room_name_tv;
        private ImageView del_iv;
    }
}
