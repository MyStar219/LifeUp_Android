package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.util.FloorAndRoomUtil;

import java.util.List;
import java.util.Random;

public class SelectRoomAdapter extends BaseAdapter {
    private static final String TAG = SelectRoomAdapter.class.getSimpleName();
    private Context mContext = null;
    private LayoutInflater mInflater;
    private List<Room> mRooms;
    private String mSelectedRoomId;
    private final int mNormalFontColor;
    private final int mSelectedFontColor;
    private TranslateAnimation taLeft, taRight, taTop, taBlow;
    private View.OnClickListener mOnClickListener;

    public SelectRoomAdapter(Context context, List<Room> rooms, String selectedRoomId,View.OnClickListener onClickListener) {
        mContext = context;
        mRooms = rooms;
        mSelectedRoomId = selectedRoomId;
        mInflater = LayoutInflater.from(context);

        mNormalFontColor = context.getResources().getColor(R.color.identity_tip);
        mSelectedFontColor = context.getResources().getColor(R.color.green);
        mOnClickListener = onClickListener;

        initAnima();
    }

    public void setData(List<Room> rooms) {
        mRooms = rooms;
        notifyDataSetChanged();
    }

    public void selectRoom(String roomId) {
        mSelectedRoomId = roomId;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return (null != mRooms) ? mRooms.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (null != mRooms && mRooms.size() > position) ? mRooms.get(position) : null;
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
            holder.roomLinearLayout = (LinearLayout) convertView.findViewById(R.id.roomLinearLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Room room = mRooms.get(position);
//        LogUtil.d(TAG, "getView(" + position + ")-room:" + room);
        holder.roomNameTextView.setText(room.getRoomName());
        holder.roomNameTextView.setTextColor(room.getRoomId().equals(mSelectedRoomId) ? mSelectedFontColor : mNormalFontColor);
        holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), false));

        if (room.getRoomId().equals(mSelectedRoomId)) {
            holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), true));
        } else {
            holder.roomTypeImageView.setBackgroundResource(FloorAndRoomUtil.getResourceByRoomType(room.getRoomType(), false));
        }
        holder.roomLinearLayout.setOnClickListener(mOnClickListener);

        Random ran = new Random();
        int rand = ran.nextInt(4);
        switch (rand) {
//            case 0:
//                convertView.startAnimation(taLeft);
//                break;
//            case 1:
//                convertView.startAnimation(taRight);
//                break;
//            case 2:
//                convertView.startAnimation(taTop);
//                break;
//            case 3:
//                convertView.startAnimation(taBlow);
//                break;

            case 0:
                convertView.startAnimation(taRight);
                break;
            case 1:
                convertView.startAnimation(taRight);
                break;
            case 2:
                convertView.startAnimation(taRight);
                break;
            case 3:
                convertView.startAnimation(taRight);
                break;
        }
        holder.roomLinearLayout.setTag(R.id.tag_room, room);
        convertView.setTag(R.id.tag_room, room);
        return convertView;
    }

    private void initAnima() {
        taLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        taRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        taTop = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        taBlow = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        taLeft.setDuration(300);
        taRight.setDuration(300);
        taTop.setDuration(300);
        taBlow.setDuration(300);
    }

    private class ViewHolder {
        private TextView roomNameTextView;
        private ImageView roomTypeImageView;
        private LinearLayout roomLinearLayout;
    }
}
