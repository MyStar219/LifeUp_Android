package com.orvibo.homemate.common.appwidget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.DeviceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuliquan on 2016/6/12.
 */
public class WidgetDeviceAndSceneAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;

    //添加到桌面的电器
    private List<WidgetItem> mDevices ;
    //添加到桌面的情景
    private List<WidgetItem>  mScenes ;

    private final String ROOM_NOT_SET;
    private RoomDao mRoomDao;
    private TypedArray icons;
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();

    public WidgetDeviceAndSceneAdapter(Context context, List<WidgetItem> devices, List<WidgetItem> scenes) {
        this.context = context;
        Resources res = context.getResources();
        icons = res.obtainTypedArray(R.array.widget_icons);
        if(devices!=null) {
            if(devices.size()!=0) {
                this.mDevices = devices;
                this.mDevices.add(0, null);
            }
        }
        if(scenes!=null) {
            if(scenes.size()!=0) {
                this.mScenes = scenes;
                this.mScenes.add(0, null);
            }
        }

        mRoomDao = new RoomDao();
        mInflater = LayoutInflater.from(context);
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);

    }

    @Override
    public int getCount() {
        //多了“设备”和“情景模式”两行显示
        return (mDevices==null?0:mDevices.size())+(mScenes==null?0:mScenes.size());
    }

    @Override
    public Object getItem(int position) {
        if(position<mDevices.size())
        return mDevices.get(position);
        else
        return mScenes.get(position-mDevices.size());
    }

    public void freshDevice(List<WidgetItem> devices){
        if(devices!=null){

            if(devices.size()!=0) {
                mDevices=devices;
                mDevices.add(0, null);
            }else{
                mDevices=null;
            }
            notifyDataSetChanged();
        }
    }

    public void freshScene(List<WidgetItem> scenes){
        if(scenes!=null){
            if(scenes.size()!=0) {
                mScenes=scenes;
                mScenes.add(0, null);
            }else{
                mScenes=null;
            }
            notifyDataSetChanged();
        }
    }

    public String getTableId(int position){
        //第一种情况
        if (mDevices == null && mScenes == null) {
            return "0";
        }
        //第二种情况
        else if (mDevices == null && mScenes != null) {
            return mScenes.get(position).getWidgetId();
        }
        //第三种情况
        else if (mDevices != null && mScenes == null) {
            return mDevices.get(position).getWidgetId();
        }
        //第四种情况
        else if (mDevices != null && mScenes != null) {
            if (position < mDevices.size()) {
                return mDevices.get(position).getWidgetId();
            } else {
                return mScenes.get(position - mDevices.size()).getWidgetId();
            }

        }
        return "0";
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public boolean isTitle(int position){
        if (mDevices == null && mScenes == null) {
            return false;
        }
        //第二种情况
        else if (mDevices == null && mScenes != null) {
            if(position==0)
                return true;
            else
                return false;
        }
        //第三种情况
        else if (mDevices != null && mScenes == null) {
            if(position==0)
                return true;
            else
                return false;
        }
        //第四种情况
        else if (mDevices != null && mScenes != null) {
            if(position==0)
                return true;
            else if(position==mDevices.size())
                return true;
        }
        return false;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_widget_device_and_scene,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon_layout = (RelativeLayout) convertView.findViewById(R.id.icon_layout);
            viewHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
            viewHolder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            viewHolder.room_tv = (TextView) convertView.findViewById(R.id.room_tv);
            viewHolder.set_icon_tv = (TextView) convertView.findViewById(R.id.set_icon_tv);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        /**
         * 这里分四种情况
         * 1.当devices和scenes都为null
         * 2.当devices为null ，scenes不为null时
         * 3.当scenes为null，devices不为null时
         * 4.当devices不为null，scenes不为null时
         */

        //第一种情况
        if (mDevices==null&&mScenes==null){}
        //第二种情况
        else if(mDevices==null&&mScenes!=null){
            if(position==0){
                viewHolder.icon_layout.setVisibility(View.GONE);
                viewHolder.deviceIcon_iv.setVisibility(View.GONE);
                viewHolder.room_tv.setVisibility(View.GONE);
                viewHolder.set_icon_tv.setVisibility(View.GONE);
                viewHolder.deviceName_tv.setText(context.getString(R.string.scene));
            }
            else{
                final WidgetItem scene = mScenes.get(position);
                viewHolder.deviceName_tv.setText(scene.getWidgetName());
                viewHolder.deviceIcon_iv.setImageDrawable(icons.getDrawable(scene.getSrcId()));
                viewHolder.set_icon_tv.setVisibility(View.VISIBLE);
                viewHolder.icon_layout.setVisibility(View.VISIBLE);
                viewHolder.deviceIcon_iv.setVisibility(View.VISIBLE);
                viewHolder.room_tv.setVisibility(View.GONE);
                viewHolder.set_icon_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,WidgetIconEditActivity.class);
                        intent.putExtra("srcId",scene.getSrcId());
                        intent.putExtra("widgetId",scene.getWidgetId());
                        context.startActivity(intent);
                    }
                });
            }
        }
        //第三种情况
        else if(mDevices!=null&&mScenes==null){
            if(position==0){
                viewHolder.icon_layout.setVisibility(View.GONE);
                viewHolder.deviceIcon_iv.setVisibility(View.GONE);
                viewHolder.room_tv.setVisibility(View.GONE);
                viewHolder.set_icon_tv.setVisibility(View.GONE);
                viewHolder.deviceName_tv.setText(context.getString(R.string.device));
            }else {
                WidgetItem device = mDevices.get(position);
                viewHolder.deviceName_tv.setText(device.getWidgetName());
                viewHolder.icon_layout.setVisibility(View.VISIBLE);
                viewHolder.deviceIcon_iv.setVisibility(View.VISIBLE);
                viewHolder.room_tv.setVisibility(View.VISIBLE);
                viewHolder.room_tv.setText(getRoom(device.getUid(),device.getDeviceId(),device.getRoomId()));
                viewHolder.set_icon_tv.setVisibility(View.GONE);
                viewHolder.deviceIcon_iv.setImageDrawable(getDrawable(device.getDeviceType()));

            }
        }
        //第四种情况
        else if(mDevices!=null&&mScenes!=null){
            //这一段是显示设备
            if(position==0){
                viewHolder.icon_layout.setVisibility(View.GONE);
                viewHolder.deviceIcon_iv.setVisibility(View.GONE);
                viewHolder.room_tv.setVisibility(View.GONE);
                viewHolder.set_icon_tv.setVisibility(View.GONE);
                viewHolder.deviceName_tv.setText(context.getString(R.string.device));
            }
            else if(position>0&&position<mDevices.size()){
                WidgetItem device = mDevices.get(position);
                viewHolder.deviceName_tv.setText(device.getWidgetName());
                viewHolder.icon_layout.setVisibility(View.VISIBLE);
                viewHolder.deviceIcon_iv.setVisibility(View.VISIBLE);
                viewHolder.room_tv.setVisibility(View.VISIBLE);
                viewHolder.room_tv.setText(getRoom(device.getUid(),device.getDeviceId(),device.getRoomId()));
                viewHolder.set_icon_tv.setVisibility(View.GONE);
                viewHolder.deviceIcon_iv.setImageDrawable(getDrawable(device.getDeviceType()));
            }
            //这一段是显示情景
            else if(position>=mDevices.size()&&position<mDevices.size()+mScenes.size()){
                if(position-mDevices.size()==0){
                    viewHolder.icon_layout.setVisibility(View.GONE);
                    viewHolder.deviceIcon_iv.setVisibility(View.GONE);
                    viewHolder.room_tv.setVisibility(View.GONE);
                    viewHolder.set_icon_tv.setVisibility(View.GONE);
                    viewHolder.deviceName_tv.setText(context.getString(R.string.scene));
                }else{
                    final WidgetItem scene = mScenes.get(position-mDevices.size());
                    viewHolder.deviceName_tv.setText(scene.getWidgetName());
                    viewHolder.icon_layout.setVisibility(View.VISIBLE);
                    viewHolder.deviceIcon_iv.setVisibility(View.VISIBLE);
                    viewHolder.deviceIcon_iv.setImageDrawable(icons.getDrawable(scene.getSrcId()));
                    viewHolder.room_tv.setVisibility(View.GONE);
                    viewHolder.set_icon_tv.setVisibility(View.VISIBLE);
                    viewHolder.set_icon_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context,WidgetIconEditActivity.class);
                            intent.putExtra("srcId",scene.getSrcId());
                            intent.putExtra("widgetId",scene.getWidgetId());
                            context.startActivity(intent);
                        }
                    });
                }

            }
        }



        return convertView;
    }



    private String getRoom(String uid, String deviceId,String roomId) {
        final String key = getKey(uid, deviceId);
         new HashMap<String, String>();
        String name = ROOM_NOT_SET;
        Map<String, String> roomAndFloorName  = mRoomDao.getRoomNameAndFloorName(uid,deviceId,roomId);
        if(roomAndFloorName!=null){
            if(roomAndFloorName.containsKey(key)){
                return roomAndFloorName.get(key);
            }
        }
        return name;
    }

    private String getKey(String uid, String deviceId) {
        return uid + "_" + deviceId;
    }

    private Drawable getDrawable(int deviceType){
        Drawable icon = context.getResources().getDrawable(getDeviceIcon(deviceType));
        return icon;
    }
    private int getDeviceIcon(int deviceType){
        int resId = R.drawable.icon_otherequipment;
        switch (deviceType) {
            case DeviceType.DIMMER:
            case DeviceType.RGB:
            case DeviceType.LAMP:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
                // 灯光
                resId = R.drawable.icon_lighting;
                break;
            case DeviceType.S20:
            case DeviceType.OUTLET:
                // 插座
                resId = R.drawable.icon_socket;
                break;
        }
        return resId;
    }
    private class ViewHolder {
        private RelativeLayout icon_layout;
        private ImageView deviceIcon_iv;
        private TextView  deviceName_tv;
        private TextView  room_tv;
        private TextView set_icon_tv;
    }

}
