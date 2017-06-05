package com.orvibo.homemate.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.util.AnimationHelper;
import com.orvibo.homemate.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Smagret
 * @data 2015/03/28
 */
public class FirstSetFloorListAdapter extends BaseAdapter {
    // private List<Floor> floors;
    private LayoutInflater inflater;
    private Context mContext;
    private final static String TAG = FirstSetFloorListAdapter.class.getSimpleName();
    private ArrayList<HashMap<Floor, List<Room>>> floorsList;
    private static boolean DELETE_REFRESH = false;
    private boolean DELETEIMG_SHOW = true;
    //    private boolean ROOM_SHOW_FLAG = true;
//    private String CURRENT_FLOOR_ID;
    private OnModifyTitleBarListener onModifyTitleBarListener;
    /**
     * key：楼层id，Value：保存楼层下房间是否显示标记 true：显示；false：不显示
     */
    private HashMap<String, Boolean> roomShowHashMap = new HashMap<String, Boolean>();

    public FirstSetFloorListAdapter(Context context,
                                    ArrayList<HashMap<Floor, List<Room>>> floorsList,
                                    boolean deleteImg_show) {
        inflater = LayoutInflater.from(context);
        this.floorsList = floorsList;
        mContext = context;
        DELETEIMG_SHOW = deleteImg_show;
    }

    @Override
    public int getCount() {
        return floorsList == null ? 0 : floorsList.size();
    }

    @Override
    public Object getItem(int position) {
        return floorsList == null ? null : floorsList.get(position).keySet().toArray()[0];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // LogUtil.d(TAG, "getView() - floors" +
        // floorsList.get(position).keySet().toArray()[0]);
        DeviceTypeHolder holder = null;
        if (convertView == null) {
            holder = new DeviceTypeHolder();
            convertView = inflater.inflate(R.layout.floor_list_item, null);

            holder.floorNameTextView = (TextView) convertView
                    .findViewById(R.id.floorNameTextView);
            holder.deleteFloorTextView = (TextView) convertView
                    .findViewById(R.id.deleteFloorTextView);
            holder.editFloorImageView = (ImageView) convertView
                    .findViewById(R.id.editFloorImageView);
            holder.showRoomsImageView = (ImageView) convertView
                    .findViewById(R.id.showRoomsImageView);
            holder.gridView = (GridView) convertView
                    .findViewById(R.id.rooms_gv);
            holder.showRoomsLinearLayout = (LinearLayout) convertView
                    .findViewById(R.id.showRoomsLinearLayout);
            holder.floorRelativeLayout = (RelativeLayout) convertView
                    .findViewById(R.id.floorRelativeLayout);
            convertView.setTag(holder);
        } else {
            holder = (DeviceTypeHolder) convertView.getTag();
        }

        if (floorsList != null && floorsList.size() > 0) {

            Floor floor = (Floor) floorsList.get(position).keySet().toArray()[0];
            //LogUtil.d(TAG, "getView() - floor" + floorsList.get(position));
            String floorName = floor.getFloorName();
            holder.floorNameTextView.setText(floorName);
            holder.floorNameTextView.setTag(floor);
            holder.floorNameTextView.setContentDescription(String.valueOf(position));
            holder.editFloorImageView.setTag(floor);
            holder.editFloorImageView.setContentDescription(String.valueOf(position));
            //holder.showRoomsImageView.setTag(floor);
            holder.deleteFloorTextView.setContentDescription(String.valueOf(position));
            holder.deleteFloorTextView.setTag(floor);
            holder.showRoomsLinearLayout.setTag(floor);

            if (roomShowHashMap != null) {
                if (roomShowHashMap.get(floor.getFloorId())) {
                    holder.gridView.setVisibility(View.VISIBLE);
                    holder.deleteFloorTextView.setVisibility(View.VISIBLE);
                    holder.showRoomsImageView.setBackgroundResource(R.drawable.arrow_up_selector);
                } else {
                    holder.gridView.setVisibility(View.GONE);
                    holder.deleteFloorTextView.setVisibility(View.GONE);
                    holder.showRoomsImageView.setBackgroundResource(R.drawable.arrow_down_selector);
                }
                if (roomShowHashMap.size() <= 1) {
                    holder.deleteFloorTextView.setVisibility(View.GONE);
                    holder.floorRelativeLayout.setVisibility(View.GONE);
                    holder.gridView.setVisibility(View.VISIBLE);
                } else {
                    holder.floorRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        @SuppressWarnings("unchecked")
        HashMap<Floor, List<Room>> roomHashMap = (HashMap<Floor, List<Room>>) floorsList
                .get(position);
        RoomGridViewAdapter roomGridViewAdapter = new RoomGridViewAdapter(
                mContext, roomHashMap, position, DELETEIMG_SHOW);
        holder.gridView.setAdapter(roomGridViewAdapter);

        if (position == floorsList.size() - 1) {
            checkIfItemHasBeenMarkedAsDeleted(convertView);
        }
        return convertView;
    }

    private class DeviceTypeHolder {
        private TextView floorNameTextView;
        private TextView deleteFloorTextView;
        private ImageView editFloorImageView;
        private ImageView showRoomsImageView;
        private GridView gridView;
        private LinearLayout showRoomsLinearLayout;
        private RelativeLayout floorRelativeLayout;
    }

//    /**
//     * 设置楼层是否显示房间
//     *
//     * @param floorId     楼层ID
//     * @param roomShowFlag 楼层中的房间是否显示  true：显示；false：不显示
//     */
//    public void setRoomShow(String floorId, boolean roomShowFlag) {
//        CURRENT_FLOOR_ID = floorId;
//        ROOM_SHOW_FLAG = roomShowFlag;
//    }

    public void setRoomShowHashMap(HashMap<String, Boolean> roomShowHashMap) {
        this.roomShowHashMap = roomShowHashMap;
    }

    /**
     * @param deleteRefresh true 表示有删除动画;false 没有删除动画
     */
    public void deleteRefresh(boolean deleteRefresh) {
        DELETE_REFRESH = deleteRefresh;
        notifyDataSetChanged();
    }

    private void checkIfItemHasBeenMarkedAsDeleted(View view) {
        if (itemIsDeletable()) {
            //LogUtil.e(TAG, "checkIfItemHasBeenMarkedAsDeleted()");
            if (floorsList.size() > 2) {
                Animation fadeout = AnimationHelper.createFadeoutAnimation();
                deleteOnAnimationComplete(fadeout);
                animate(view, fadeout);
            } else {
                floorsList.remove(floorsList.size() - 1);
                notifyDataSetChanged();
                if (onModifyTitleBarListener != null) {
                    onModifyTitleBarListener.onModifyTitleBar();
                }
            }
            DELETE_REFRESH = false;
        }
    }

    private static boolean itemIsDeletable() {
        return DELETE_REFRESH == true;
    }

    private void deleteOnAnimationComplete(Animation fadeout) {
        fadeout.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floorsList.remove(floorsList.size() - 1);
//                LogUtil.e(TAG, "deleteOnAnimationComplete() - floorSize"
//                        + floorsList.size());
                notifyDataSetChanged();
                if (onModifyTitleBarListener != null) {
                    onModifyTitleBarListener.onModifyTitleBar();
                }
            }
        });
    }

    private static void animate(View view, Animation animation) {
        view.startAnimation(animation);
        //LogUtil.e(TAG, "animate() view = " + view);
    }


    public void setOnModifyTitleBarListener(OnModifyTitleBarListener onModifyTitleBarListener) {
        this.onModifyTitleBarListener = onModifyTitleBarListener;
    }

    public interface OnModifyTitleBarListener {
        /**
         */
        void onModifyTitleBar();

    }
}
