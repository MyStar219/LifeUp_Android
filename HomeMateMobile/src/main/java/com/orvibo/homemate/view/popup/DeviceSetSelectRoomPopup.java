package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.WheelAdapter;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.room.SetFloorAndRoomActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;
import com.orvibo.homemate.view.custom.wheelview.WheelView;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置设备-选择房间
 * Created by huangqiyao on 2015/4/9.
 */
public class DeviceSetSelectRoomPopup extends CommonPopup implements View.OnClickListener {
    private static final String TAG = DeviceSetSelectRoomPopup.class.getSimpleName();
    private Context mContext;
    private String mUid;
    private SelectRoom mSelectRoom;

    private LinearLayout wheel_ll;
    private WheelView mFloor_wv;
    private WheelView mRoom_wv;

    private WheelAdapter mRoomAdapter;
    private TextView cancel_tv;

    public DeviceSetSelectRoomPopup(Context context, String uid) {
        mContext = context;
        mUid = uid;
        initSelectRoom(context, uid);
    }

    private void initSelectRoom(Context context, final String uid) {
        mSelectRoom = new SelectRoom(context, uid, false, true) {
            @Override
            protected void onSelected(Floor floor, Room room) {
                //  LogUtil.d(TAG, "onSelected()-floor:" + floor + ",room:" + room);
            }

            @Override
            protected void onSelectFloor(String floorId, List<Room> rooms) {
                int roomPos = 0;
                String selectRoomId = Constant.ALL_ROOM;
                if (rooms != null && !rooms.isEmpty()) {
                    final int count = rooms.size();
                    selectRoomId = mSelectRoom.getSelectedRoomId();
                    for (int i = 0; i < count; i++) {
                        if (selectRoomId.equals(rooms.get(i).getRoomId())) {
                            roomPos = i;
                            break;
                        }
                    }
                    if (roomPos == 0) {
                        selectRoomId = rooms.get(0).getRoomId();
                    }
                }
                mRoom_wv.setSelection(roomPos);
                LogUtil.d(TAG, "onSelectFloor()-floorId:" + floorId + ",roomPos:" + roomPos + ",rooms:" + rooms);
                mRoomAdapter.setData(getRoomContent(rooms));
                mSelectRoom.selectRoom(selectRoomId);
            }
        };
    }


    public void show(String selectedRoomId) {
        mSelectRoom.init(selectedRoomId);

        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_set_room, null);
        wheel_ll = (LinearLayout) contentView.findViewById(R.id.wheel_ll);
        inAnim();

        cancel_tv = (TextView) contentView.findViewById(R.id.cancel_tv);
        cancel_tv.setOnClickListener(this);
        cancel_tv.setText(mContext.getResources().getString(R.string.device_set_self_remote_edit));
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);

        List<Floor> floors = mSelectRoom.getFloors();
        List<WheelBo> contents = new ArrayList<WheelBo>();
        String selectedFloorId = mSelectRoom.getSelectedFloorId();
        int floorPos = 0;
        if (floors != null && !floors.isEmpty()) {
            int i = 0;
            for (Floor floor : floors) {
                final String id = floor.getFloorId();
                if (id.equals(selectedFloorId)) {
                    floorPos = i;
                }
                WheelBo wheelBo = new WheelBo();
                wheelBo.setId(id);
                wheelBo.setName(floor.getFloorName());
                contents.add(wheelBo);
                i += 1;
            }
        }

        //楼层
        mFloor_wv = (WheelView) contentView.findViewById(R.id.floor_wv);
        mFloor_wv.setScrollCycle(false);
        mFloor_wv.setAdapter(new WheelAdapter(mContext, contents));
        mFloor_wv.setOnEndFlingListener(mFloorSelectListener);
        mFloor_wv.setSelection(floorPos);

        //房间
//        String selectedFloorId = mSelectRoom.getSelectedFloorId();
        if (!StringUtil.isEmpty(selectedFloorId) && selectedFloorId.equals(Constant.EMPTY_FLOOR) && floors != null && !floors.isEmpty()) {
            selectedFloorId = floors != null && floors.size() > 0 ? floors.get(0).getFloorId() : Constant.EMPTY_FLOOR;
        }
        List<Room> rooms = mSelectRoom.getRooms(selectedFloorId);
        mRoom_wv = (WheelView) contentView.findViewById(R.id.room_wv);
        mRoom_wv.setScrollCycle(false);
        mRoom_wv.setOnEndFlingListener(mRoomSelectListener);
        mRoomAdapter = new WheelAdapter(mContext, getRoomContent(rooms));
        mRoom_wv.setAdapter(mRoomAdapter);
        setRoomPos(rooms);

        show(mContext, contentView, true);
    }

    private void inAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_in));
    }

    public void outAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_out));
    }

    private void setRoomPos(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            rooms = mSelectRoom.getRooms(mSelectRoom.getSelectedFloorId());
        }
        int roomPos = 0;
        if (rooms != null && !rooms.isEmpty()) {
            final int count = rooms.size();
            for (int i = 0; i < count; i++) {
                if (rooms.get(i).getRoomId().equals(mSelectRoom.getSelectedRoomId())) {
                    roomPos = i;
                    break;
                }
            }
        }
        if (mSelectRoom.getSelectedRoomId().equals(Constant.ALL_ROOM)) {
        }
        mRoom_wv.setSelection(roomPos);
    }

    /**
     * 获取楼层下的roomNames
     *
     * @return
     */
    private List<WheelBo> getRoomContent(List<Room> rooms) {
        List<WheelBo> contents = new ArrayList<WheelBo>();
//        List<Room> rooms = mSelectRoom.getRooms(mSelectRoom.getSelectedFloorId());
        if (rooms != null && !rooms.isEmpty()) {
            for (Room room : rooms) {
                WheelBo wheelBo = new WheelBo();
                wheelBo.setId(room.getRoomId());
                wheelBo.setName(room.getRoomName());
                contents.add(wheelBo);
            }
        }
        return contents;
    }

    private TosGallery.OnEndFlingListener mFloorSelectListener = new TosGallery.OnEndFlingListener() {
        @Override
        public void onEndFling(TosGallery v) {
            WheelBo wheelBo = (WheelBo) v.getSelectedItem();
            //LogUtil.d(TAG, "onEndFling(floor)-wheelBo:" + wheelBo);
            //选择楼层
//            WheelBo wheelBo = (WheelBo) v.getTag(R.id.tag_wheel);
            String floorId = wheelBo.getId();
            mSelectRoom.selectFloor(floorId);
        }
    };

    private TosGallery.OnEndFlingListener mRoomSelectListener = new TosGallery.OnEndFlingListener() {
        @Override
        public void onEndFling(TosGallery v) {
            //选择房间
            WheelBo wheelBo = (WheelBo) v.getSelectedItem();
            // LogUtil.d(TAG, "onEndFling(room)-wheelBo:" + wheelBo);
//            WheelBo wheelBo = (WheelBo) v.getTag(R.id.tag_wheel);
            if (wheelBo != null) {
                String roomId = wheelBo.getId();
                mSelectRoom.selectRoom(roomId);
            }
            mRoom_wv.setSelection(v.getSelectedItemPosition());
        }
    };

    @Override
    public final void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_tv:
                Floor selectedFloor = mSelectRoom.getCurrentFloor();
                Room selectedRoom = mSelectRoom.getCurrentRoom();
                if (selectedFloor == null) {
                    List<Floor> floors = mSelectRoom.getFloors();
                    selectedFloor = floors != null && floors.size() > 0 ? floors.get(0) : null;
                }

                if (selectedRoom == null) {
                    if (selectedFloor != null) {
                        List<Room> rooms = mSelectRoom.getRooms(selectedFloor.getFloorId());
                        selectedRoom = rooms != null && rooms.size() > 0 ? rooms.get(0) : null;
                    }
                }
                if (selectedRoom == null) {
                    String content =mContext.getString(R.string.device_set_room_popup_no_room);
                    ToastUtil.showToast(content, ToastType.ERROR, Toast.LENGTH_LONG);
                } else {
                    outAnim();
                    onSelect(selectedFloor, selectedRoom);
                    dismiss();
                }
                break;
            case R.id.cancel_tv:
                outAnim();
                dismiss();
                Intent intent = new Intent(mContext, SetFloorAndRoomActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.v1:
                dismiss();
                break;
        }
    }


    /**
     * 选择楼层和房间
     *
     * @param selectedFloor
     * @param selectedRoom
     */
    public void onSelect(Floor selectedFloor, Room selectedRoom) {
    }

}
