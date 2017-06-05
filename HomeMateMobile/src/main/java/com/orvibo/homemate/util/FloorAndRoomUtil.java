package com.orvibo.homemate.util;

import android.content.Context;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.RoomType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by smagret on 2015/4/27.
 */
public class FloorAndRoomUtil {

    private static final String TAG = FloorAndRoomUtil.class.getSimpleName();

    private static void addCommonRoom(Context context, List<Room> rooms) {
        String[] roomNames = new String[9];
        int[] roomTypes = new int[9];
        roomNames[0] = context.getResources().getString(R.string.drawing_room);
        roomNames[1] = context.getResources().getString(R.string.bedroom_one);
        roomNames[2] = context.getResources().getString(R.string.bedroom_two);
        roomNames[3] = context.getResources().getString(R.string.bedroom_three);
        roomNames[4] = context.getResources().getString(R.string.study);
        roomNames[5] = context.getResources().getString(R.string.dining_room);
        roomNames[6] = context.getResources().getString(R.string.toliet);
        roomNames[7] = context.getResources().getString(R.string.kitchen);
        roomNames[8] = context.getResources().getString(R.string.balcony);

        roomTypes[0] = RoomType.DRAWING_ROOM;
        roomTypes[1] = RoomType.BEDROOM_ONE;
        roomTypes[2] = RoomType.BEDROOM_TWO;
        roomTypes[3] = RoomType.BEDROOM_TWO;
        roomTypes[4] = RoomType.STUDY;
        roomTypes[5] = RoomType.DINING_ROOM;
        roomTypes[6] = RoomType.TOLIET;
        roomTypes[7] = RoomType.KITCHEN;
        roomTypes[8] = RoomType.BALCONY;
        for (int i = 0; i < roomNames.length; i++) {
            Room room = new Room();
            room.setRoomName(roomNames[i]);
            room.setRoomType(roomTypes[i]);
            rooms.add(room);
        }
    }

    public static void AddLastRoom(Context context, List<Room> rooms) {
        Room lastRoom = new Room();
        lastRoom.setDelFlag(Constant.INVALID_NUM);
        lastRoom.setFloorId(Constant.NULL_DATA);
        lastRoom.setRoomId(Constant.NULL_DATA);
        lastRoom.setRoomName(context.getResources().getString(R.string.last_room));
        lastRoom.setUid(Constant.NULL_DATA);
        lastRoom.setUpdateTime(Constant.DEFAULT_NUM_LONG);
        rooms.add(lastRoom);
    }

    /**
     * 获取一个楼层和房间，不包括+ADD这个房间
     *
     * @param context
     * @param floorsListSize
     * @return
     */
    public static HashMap<Floor, List<Room>> getFloorAndCommonRooms(Context context, int floorsListSize) {
        Floor floor = new Floor();
        String floorNo = FloorAndRoomUtil.chineseNum(context, floorsListSize + 1);

        String floorName = String.format(context.getResources().getString(R.string.floor), floorNo);
        floor.setFloorName(floorName);
        HashMap<Floor, List<Room>> floorHashMap = new HashMap<Floor, List<Room>>();
        List<Room> rooms = new ArrayList<Room>();
        addCommonRoom(context, rooms);
        floorHashMap.put(floor, rooms);
        return floorHashMap;
    }

//    /**
//     * 获取一个楼层和房间，包括+ADD这个房间
//     *
//     * @param context
//     * @param floorsListSize
//     * @return
//     */
//    public static HashMap<Floor, List<Room>> getFloorAndCommonRooms2(Context context, int floorsListSize) {
//        Floor floor = new Floor();
//        String floorName = (floorsListSize + 1)
//                + context.getResources().getString(R.string.floor);
//        floor.setFloorName(floorName);
//        HashMap<Floor, List<Room>> floorHashMap = new HashMap<Floor, List<Room>>();
//        List<Room> rooms = new ArrayList<Room>();
//        addCommonRoom(context, rooms);
//        AddLastRoom(context, rooms);
//        floorHashMap.put(floor, rooms);
//        return floorHashMap;
//    }


    public static int getResourceByRoomType(int roomType, boolean checked) {
        int roomTypeRes = 0;
        switch (roomType) {
            case RoomType.DRAWING_ROOM:
                roomTypeRes = checked ? R.drawable.icon_living_room_checked : R.drawable.icon_living_room_normal;
                break;
            case RoomType.BEDROOM_ONE:
                roomTypeRes = checked ? R.drawable.icon_the_master_bedroom_checked : R.drawable.icon_the_master_bedroom_normal;
                break;
            case RoomType.BEDROOM_TWO:
                roomTypeRes = checked ? R.drawable.icon_bedroom_checked : R.drawable.icon_bedroom_normal;
                break;
            case RoomType.DINING_ROOM:
                roomTypeRes = checked ? R.drawable.icon_dining_checked : R.drawable.icon_dining_normal;
                break;
            case RoomType.KITCHEN:
                roomTypeRes = checked ? R.drawable.icon_kitchen_checked : R.drawable.icon_kitchen_normal;
                break;
            case RoomType.TOLIET:
                roomTypeRes = checked ? R.drawable.icon_toilet_checked : R.drawable.icon_toilet_normal;
                break;
            case RoomType.STUDY:
                roomTypeRes = checked ? R.drawable.icon_reading_room_checked : R.drawable.icon_reading_room_normal;
                break;
            case RoomType.CHILD:
                roomTypeRes = checked ? R.drawable.icon_children_checked : R.drawable.icon_children_normal;
                break;
            case RoomType.BALCONY:
                roomTypeRes = checked ? R.drawable.icon_balcony_checked : R.drawable.icon_balcony_normal;
                break;
            case RoomType.CORRIDOR:
                roomTypeRes = checked ? R.drawable.icon_corridor_checked : R.drawable.icon_corridor_normal;
                break;
            case RoomType.GARDEN:
                roomTypeRes = checked ? R.drawable.icon_garden_checked : R.drawable.icon_garden_normal;
                break;
            case RoomType.CLOAKROOM:
                roomTypeRes = checked ? R.drawable.icon_cloakroom_checked : R.drawable.icon_cloakroom_normal;
                break;
            case RoomType.LAUNDRY:
                roomTypeRes = checked ? R.drawable.icon_washing_room_checked : R.drawable.icon_washing_room_normal;
                break;
            case RoomType.GARAGE:
                roomTypeRes = checked ? R.drawable.icon_garage_checked : R.drawable.icon_garage_normal;
                break;
            case RoomType.OTHER:
                roomTypeRes = checked ? R.drawable.icon_other_checked : R.drawable.icon_other_normal;
                break;
            case RoomType.ALL:
                roomTypeRes = checked ? R.drawable.icon_all_room_checked : R.drawable.icon_all_room_normal;
                break;
        }
        return roomTypeRes;
    }

    public static List<Integer> getAllRoomType() {
        List<Integer> roomTypes = new LinkedList<>();
        roomTypes.add(RoomType.DRAWING_ROOM);
        roomTypes.add(RoomType.BEDROOM_ONE);
        roomTypes.add(RoomType.BEDROOM_TWO);
        roomTypes.add(RoomType.DINING_ROOM);
        roomTypes.add(RoomType.KITCHEN);
        roomTypes.add(RoomType.TOLIET);
        roomTypes.add(RoomType.STUDY);
        roomTypes.add(RoomType.CHILD);
        roomTypes.add(RoomType.BALCONY);
        roomTypes.add(RoomType.CORRIDOR);
        roomTypes.add(RoomType.GARDEN);
        roomTypes.add(RoomType.CLOAKROOM);
        roomTypes.add(RoomType.LAUNDRY);
        roomTypes.add(RoomType.GARAGE);
        roomTypes.add(RoomType.OTHER);
        return roomTypes;
    }

    public static int getRoomNameByRoomType(int roomType) {
        int roomName;
        switch (roomType) {
            case RoomType.DRAWING_ROOM:
                roomName = R.string.drawing_room;
                break;
            case RoomType.BEDROOM_ONE:
                roomName = R.string.bedroom_one;
                break;
            case RoomType.BEDROOM_TWO:
                roomName = R.string.bedroom_two;
                break;
            case RoomType.DINING_ROOM:
                roomName = R.string.dining_room;
                break;
            case RoomType.KITCHEN:
                roomName = R.string.kitchen;
                break;
            case RoomType.TOLIET:
                roomName = R.string.toliet;
                break;
            case RoomType.STUDY:
                roomName = R.string.study;
                break;
            case RoomType.CHILD:
                roomName = R.string.child;
                break;
            case RoomType.BALCONY:
                roomName = R.string.balcony;
                break;
            case RoomType.CORRIDOR:
                roomName = R.string.corridor;
                break;
            case RoomType.GARDEN:
                roomName = R.string.garden;
                break;
            case RoomType.CLOAKROOM:
                roomName = R.string.cloakroom;
                break;
            case RoomType.LAUNDRY:
                roomName = R.string.laundry;
                break;
            case RoomType.GARAGE:
                roomName = R.string.garage;
                break;
            case RoomType.OTHER:
                roomName = R.string.other;
                break;
            default:
                roomName = R.string.other;
                break;
        }
        return roomName;
    }


    public static String chineseNum(Context context, int n) {
        String[] num = {context.getResources().getString(R.string.zero),
                context.getResources().getString(R.string.one),
                context.getResources().getString(R.string.two),
                context.getResources().getString(R.string.three),
                context.getResources().getString(R.string.four),
                context.getResources().getString(R.string.five),
                context.getResources().getString(R.string.six),
                context.getResources().getString(R.string.seven),
                context.getResources().getString(R.string.eight),
                context.getResources().getString(R.string.nine),


        };
        String[] digit = {"十", "百", "千", "万", "十万"};
        int length = String.valueOf(n).length();
        String[] results = new String[length];

        for (int i = 0; i < length; i++) {
            int t = n % 10;
            n = n / 10;
            results[i] = "";
            results[i] += num[t];

            if (t != 0 && i > 0) {
                if (t == 1 && i == length - 1 && (i == 1 || i == 5)) {
                    results[i] = "" + digit[i - 1];
                } else {
                    results[i] += digit[i - 1];
                }
            }
        }

        String result = "";
        boolean empty = false;
        for (int i = length - 1; i >= 0; i--) {
            if (results[i].equals(context.getResources().getString(R.string.zero))) {
                empty = true;
            } else {
                if (empty) {
                    result += context.getResources().getString(R.string.zero);
                    empty = false;
                }
                result += results[i];
            }
        }
        return result;
    }
}
