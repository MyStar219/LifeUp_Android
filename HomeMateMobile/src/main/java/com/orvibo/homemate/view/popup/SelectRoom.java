package com.orvibo.homemate.view.popup;

import android.content.Context;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.RoomType;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqiyao on 2015/4/10.
 */
public abstract class SelectRoom {
    private static final String TAG = SelectRoom.class.getSimpleName();
    private Context mContext;
    private String mUid;

    private FloorDao mFloorDao;
    private RoomDao mRoomDao;
    private DeviceDao mDeviceDao;
    /**
     * key:floorId,value:该楼层下的所有room.
     */
    private Map<String, LinkedList<Room>> mRooms;

    private List<Floor> mFloors;

    private String mOldFloorId;
    private String mOldRoomId;

    /**
     * 当前选中所在的楼层
     */
    private String mSelectedFloorId;
    /**
     * 点击了楼层，但还没有选择房间时记录的楼层
     */
    private String mTempSelectedFloorId;
    private String mSelectedRoomId;

    /**
     * true显示所有房间的设备
     */
    private boolean showAllRoom = false;

    /**
     * 标记房间中没有设备时，是否显示该房间 true：房间中没有设备也显示该房间，false房间中没有设备不显示该房间
     */
    private volatile boolean mShowRoomNoHasDevices = false;

    /**
     * @param context
     * @param uid
     * @param showAllRoom true显示所有房间的设备
     */
    public SelectRoom(Context context, String uid, boolean showAllRoom, boolean showRoomNoHasDevices) {
        this.mContext = context;
        mUid = uid;
        this.showAllRoom = showAllRoom;
        this.mShowRoomNoHasDevices = showRoomNoHasDevices;
        mFloorDao = new FloorDao();
        mRoomDao = new RoomDao();
        mDeviceDao = new DeviceDao();

        LogUtil.d(TAG, "SelectRoom()-uid:" + uid);
    }

    public void init(String selectedRoomId) {
        String selectedFloorId = Constant.NULL_DATA;
        if (!StringUtil.isEmpty(selectedRoomId)) {
            if (selectedRoomId.equals(Constant.ALL_ROOM)) {
                selectedFloorId = Constant.EMPTY_FLOOR;
            } else {
                Room room = mRoomDao.selRoom(mUid, selectedRoomId);
                if (!mShowRoomNoHasDevices) {
                    //显示有设备(只显示我的设备列表的设备)的房间
                    List<Device> devices = mDeviceDao.selDevicesByRoom(mUid, selectedRoomId);
                    if (devices == null || devices.isEmpty()) {
                        room = null;
                    }
                }
                if (room != null) {
                    selectedFloorId = room.getFloorId();
                } else {
                    selectedFloorId = Constant.EMPTY_FLOOR;
                }
            }
        } else {
            selectedFloorId = Constant.EMPTY_FLOOR;
            selectedRoomId = Constant.ALL_ROOM;
        }
        mOldFloorId = selectedFloorId;
        mOldRoomId = selectedRoomId;

        mSelectedFloorId = selectedFloorId;
        mSelectedRoomId = selectedRoomId;

        initData();
        Floor floor = null;
        Room room = null;
        LogUtil.d(TAG, "init()-mSelectedFloorId:" + mSelectedFloorId + ",mSelectedRoomId:" + mSelectedRoomId);
        if (Constant.EMPTY_FLOOR.equals(mSelectedFloorId) || Constant.ALL_ROOM.equals(mSelectedRoomId)) {
            floor = getEmptyFloor();
            room = getAllRoom();
            room.setFloorId(mSelectedFloorId);
        } else {
            if (!StringUtil.isEmpty(mSelectedFloorId) && mFloors != null && !mFloors.isEmpty()) {
                for (Floor f : mFloors) {
                    if (f.getFloorId().equals(mSelectedFloorId)) {
                        floor = f;
                        break;
                    }
                }
            }

            if (floor != null && mSelectedRoomId != null && mRooms != null) {
                List<Room> rooms = mRooms.get(floor.getFloorId());
                if (rooms != null && !rooms.isEmpty()) {
                    for (Room r : rooms) {
                        if (r.getRoomId().equals(mSelectedRoomId)) {
                            room = r;
                            break;
                        }
                    }
                }
            }
        }

        LogUtil.d(TAG, "init()-selectedRoomId :" + selectedRoomId + ",floor:" + floor + ",room:" + room);
        //RoomCache.saveRoom(context, mUid, room == null ? Constant.INVALID_NUM : room.getRoomId());
        //TODO floor room都为空则显示所有房间
        onSelected(floor, room);
    }

    public List<Floor> getFloors() {
        return mFloors;
    }

    /**
     * 创建一个空的房间，名字为所有房间
     *
     * @return
     */
    public Room getAllRoom(String currentFloorId) {
        return new Room(mUid, UserCache.getCurrentUserName(mContext),
                Constant.ALL_ROOM, mContext.getString(R.string.all_room),
                currentFloorId, RoomType.ALL, DeleteFlag.NORMAL, 0L);
    }

    /**
     * 当只有一层楼时只需要显示房间列表
     *
     * @return true只有一层楼
     */
    public boolean isOnlyOneFloor() {
        return mFloors != null && mFloors.size() == 1;
    }

    /**
     * @return true没有楼层，不显示选择房间popup
     */
    public boolean isNoneFloor() {
        return mFloors == null || mFloors.isEmpty();
    }

    private void initData() {
        mRooms = new HashMap<String, LinkedList<Room>>();
        List<Room> allRooms = null;
        if (mShowRoomNoHasDevices) {
            allRooms = mRoomDao.selAllRooms(mUid);
            mFloors = mFloorDao.selAllFloors(mUid);
        } else {
            allRooms = mRoomDao.selRoomsHasDevices(mUid);
            mFloors = mFloorDao.selFloorsHasDevices(mUid);
        }

        //LogUtil.d(TAG, "initData()-allRooms:" + allRooms);

        for (Room room : allRooms) {
            //LogUtil.d(TAG, "initData()-room:" + room);
            final String floorId = room.getFloorId();
            LinkedList<Room> rooms = mRooms.get(floorId);
            if (rooms == null || rooms.isEmpty()) {
                rooms = new LinkedList<>();
            }
            rooms.add(room);
            mRooms.put(floorId, rooms);
        }

        if (isOnlyOneFloor() && allRooms != null && allRooms.size() > 0) {
            //单层mRooms中添加所有房间
            if (showAllRoom) {
                String currentFloorId = mFloors.get(0).getFloorId();
                mRooms.get(currentFloorId).addFirst(getAllRoom(currentFloorId));
            }
        }
        boolean containOldFloorId = false;

        if (mFloors != null && !mFloors.isEmpty()) {
            final int count = mFloors.size();
            String hasRoomFloorId = Constant.EMPTY_FLOOR;
            for (int i = 0; i < count; i++) {
                Floor floor = mFloors.get(i);
                //LogUtil.d(TAG, "initData()-floor:" + floor);
                final String floorId = floor.getFloorId();
                if (floorId.equals(mSelectedFloorId)) {
                    containOldFloorId = true;
                    break;
                } else if ((Constant.EMPTY_FLOOR.equals(mSelectedFloorId) || Constant.ALL_ROOM.equals(mSelectedRoomId)) && count == 1) {
                    containOldFloorId = true;
                    mSelectedFloorId = floorId;
                    mSelectedRoomId = Constant.ALL_ROOM;
                    break;
                }
//                else {
//                    if (hasRoomFloorId.equals(Constant.EMPTY_FLOOR)) {
//                        if (mRooms.containsKey(floorId)) {
//                            List<Room> rooms = mRooms.get(floorId);
//                            if (!rooms.isEmpty()) {
//                                hasRoomFloorId = floorId;
//                            }
//                        }
//                    }
//                }
            }
            if (!containOldFloorId) {
                if (!hasRoomFloorId.equals(Constant.EMPTY_FLOOR)) {
                    mSelectedFloorId = hasRoomFloorId;
                    List<Room> rooms = mRooms.get(mSelectedFloorId);
                    if (rooms != null && !rooms.isEmpty()) {
                        mSelectedRoomId = rooms.get(0).getRoomId();
                    }
                } else {
                    mSelectedFloorId = Constant.EMPTY_FLOOR;
                    mSelectedRoomId = Constant.ALL_ROOM;

                }
            }
            mTempSelectedFloorId = mSelectedFloorId;
        }

        LogUtil.d(TAG, "initData()-mSelectedFloorId:" + mSelectedFloorId + ",mSelectedRoomId:" + mSelectedRoomId);
    }

    public String getSelectedFloorId() {
        return mSelectedFloorId;
    }

    public String getSelectedRoomId() {
        return mSelectedRoomId;
    }

    /**
     * 选择楼层
     *
     * @param floorId
     * @return
     */
    public void selectFloor(String floorId) {
        mTempSelectedFloorId = floorId;
        List<Room> rooms = getRooms(floorId);
        LogUtil.d(TAG, "selectFloor()-floorId :" + floorId + ",rooms:" + rooms);
        onSelectFloor(floorId, rooms);
    }

    public Room getCurrentRoom() {
        return mRoomDao.selRoom(mUid, mSelectedRoomId);
    }

    public Floor getCurrentFloor() {
        Floor floor = null;
        if (!mSelectedFloorId.isEmpty() && mFloors != null && !mFloors.isEmpty()) {
            for (Floor f : mFloors) {
                if (f.getFloorId().equals(mSelectedFloorId)) {
                    floor = f;
                    break;
                }
            }
        }
        return floor;
    }

    public List<Room> getRooms(String floorId) {
        List<Room> rooms = new ArrayList<Room>();
        if (hasRoom(floorId)) {
            rooms = mRooms.get(floorId);
        }
        return rooms;
    }

    /**
     * 选中房间
     *
     * @param roomId
     */
    public void selectRoom(String roomId) {
        LogUtil.d(TAG, "selectRoom()-mTempSelectedFloorId:" + mTempSelectedFloorId + ",roomId :" + roomId);
        Floor floor = null;
        if (mFloors != null) {
            for (Floor f : mFloors) {
                if (f.getFloorId().equals(mTempSelectedFloorId)) {
                    floor = f;
                    break;
                }
            }
        }
        Room room = null;
        List<Room> rooms = getRooms(mTempSelectedFloorId);
        if (rooms != null && !rooms.isEmpty()) {
            for (Room r : rooms) {
                if (r.getRoomId().equals(roomId)) {
                    room = r;
                    break;
                }
            }
        }

        if (room != null) {
            mSelectedFloorId = room.getFloorId();
            mSelectedRoomId = room.getRoomId();
        } else {
            if (roomId.equals(Constant.ALL_ROOM)) {
                if (floor != null)
                    mSelectedFloorId = floor.getFloorId();
                else
                    mSelectedFloorId = Constant.EMPTY_FLOOR;
                mTempSelectedFloorId = Constant.EMPTY_FLOOR;
                mSelectedRoomId = Constant.ALL_ROOM;
                floor = getEmptyFloor();
                room = getAllRoom();
            } else {
                room = mRoomDao.selRoom(mUid, roomId);
                floor = mFloorDao.selFloor(mUid, room.getFloorId());
                mSelectedFloorId = room.getFloorId();
                mTempSelectedFloorId = mSelectedFloorId;
                mSelectedRoomId = roomId;
            }
        }

        LogUtil.d(TAG, "selectRoom()-mSelectedFloorId :" + mSelectedFloorId + ",mSelectedRoomId:" + mSelectedRoomId);
        LogUtil.d(TAG, "selectRoom()-floor :" + floor + ",room:" + room);
        onSelected(floor, room);
    }

    /**
     * 创建一个空的楼层
     *
     * @return
     */
    public Floor getEmptyFloor() {
        return new Floor(mUid, UserCache.getCurrentUserName(mContext), Constant.EMPTY_FLOOR, Constant.EMPTY_FLOOR, DeleteFlag.NORMAL, 0L);
    }

    /**
     * 创建一个空的房间，名字为所有房间
     *
     * @return
     */
    public Room getAllRoom() {
        return new Room(mUid, UserCache.getCurrentUserName(mContext),
                Constant.ALL_ROOM, mContext.getString(R.string.all_room),
                Constant.EMPTY_FLOOR, RoomType.ALL, DeleteFlag.NORMAL, 0L);
    }

    public void correctSelectedRoom() {
        Room selectedRoom = mRoomDao.selRoom(mUid, mSelectedRoomId);
        if (selectedRoom != null) {
            if (!mSelectedFloorId.equals(selectedRoom.getFloorId())) {
                mSelectedFloorId = selectedRoom.getFloorId();
                mTempSelectedFloorId = selectedRoom.getFloorId();
            }
        } else {
            if (mSelectedRoomId.equals(Constant.ALL_ROOM)) {
                if (isOnlyOneFloor()) {
                    mSelectedFloorId = mFloors.get(0).getFloorId();
                    mTempSelectedFloorId = mSelectedFloorId;
                } else {
                    mSelectedFloorId = Constant.EMPTY_FLOOR;
                    mTempSelectedFloorId = Constant.EMPTY_FLOOR;
                }
            }
        }

    }

    /**
     * @return true此住宅还没有创建房间
     */
    public boolean isEmptyRoom() {
        int floorNo = mFloorDao.selFloorNo(mUid);
        return floorNo > 0 ? false : true;
    }

    private boolean hasRoom(String floorId) {
        return mRooms != null && !mRooms.isEmpty() && mRooms.containsKey(floorId);
    }

    /**
     * 选择了房间
     *
     * @param floor
     * @param room
     */
    protected abstract void onSelected(Floor floor, Room room);

    /**
     * 选择楼层时会返回其所有房间
     *
     * @param floorId
     * @param rooms
     */
    protected abstract void onSelectFloor(String floorId, List<Room> rooms);
}
