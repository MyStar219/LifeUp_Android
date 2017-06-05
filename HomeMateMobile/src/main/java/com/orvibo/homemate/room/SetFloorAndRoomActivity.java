package com.orvibo.homemate.room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.model.AddFloorAndRoom;
import com.orvibo.homemate.model.DeleteFloor;
import com.orvibo.homemate.model.DeleteRoom;
import com.orvibo.homemate.room.adapter.FirstSetFloorListAdapter;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.FloorAndRoomUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author smagret
 * @data 2015/02/11
 */
public class SetFloorAndRoomActivity extends BaseActivity implements
        OnClickListener, OnScrollListener {
    private TextView addFloorTextView;
    private String userName;
    private ListView floorListView;
    private Room room;
    private Floor floor;
    private String uid;
    private FirstSetFloorListAdapter firstSetFloorListAdapter;
    private List<Room> rooms;
    private List<Floor> floors;
    /**
     * key：楼层id，Value：保存楼层下房间是否显示标记 true：显示；false：不显示
     */
    private HashMap<String, Boolean> roomShowHashMap = new HashMap<String, Boolean>();
    private TextView floorNameTextView;
    private ImageView deleteRoomImageView;
    private DeleteFloorPopup deleteFloorPopup;
    private DeleteRoomPopup deleteRoomPopup;
    private NavigationGreenBar navigationBar;
    private final int REQUEST_CODE_ADD_ROOM = 1;
    private final int REQUEST_CODE_MODIFY_ROOM = 2;
    private final int REQUEST_CODE_MODIFY_FLOOR = 3;
    private final int RESULT_CODE_MODIFY_ROOM = 4;
    private final int RESULT_CODE_MODIFY_FLOOR = 6;
    private RoomDao roomDao;
    private FloorDao floorDao;
    private DeviceDao deviceDao;
    private DeleteFloorControl deleteFloorControl;
    private DeleteRoomControl deleteRoomControl;

    private AddFloorAndRoomControl addFloorAndRoomControl;
    private ArrayList<HashMap<Floor, List<Room>>> floorsList;
    private ArrayList<HashMap<Floor, List<Room>>> tempFloorsList;

    private HashMap<Floor, List<Room>> floorHashMap;
    private int roomPosition;
    private int floorPosition;

    /**
     * 最大楼层数
     */
    private final int MAX_FLOOR_COUNT = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_floor_and_room);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        addFloorTextView = (TextView) findViewById(R.id.addFloorTextView);
        floorListView = (ListView) findViewById(R.id.floorListView);
        View floor_list_item = LayoutInflater.from(mAppContext).inflate(
                R.layout.floor_list_item, null);
        View room_gv_item = LayoutInflater.from(mAppContext).inflate(
                R.layout.room_gv_item, null);
        deleteRoomImageView = (ImageView) room_gv_item
                .findViewById(R.id.deleteRoomImageView);
        floorNameTextView = (TextView) floor_list_item
                .findViewById(R.id.floorNameTextView);
        navigationBar = (NavigationGreenBar) findViewById(R.id.navigation_ll);
        navigationBar.hide(NavigationGreenBar.RIGHT);
        navigationBar.setText(getResources().getString(R.string.room_manage));
        deleteFloorPopup = new DeleteFloorPopup();
        deleteRoomPopup = new DeleteRoomPopup();
    }

    private void initData() {
        userName = UserCache.getCurrentUserName(mAppContext);
        uid = UserCache.getCurrentMainUid(mAppContext);
        roomDao = new RoomDao();
        floorDao = new FloorDao();
        deviceDao = new DeviceDao();
        rooms = new ArrayList<Room>();
        floorsList = new ArrayList<HashMap<Floor, List<Room>>>();
        tempFloorsList = new ArrayList<HashMap<Floor, List<Room>>>();
        deleteFloorControl = new DeleteFloorControl(mAppContext);
        addFloorAndRoomControl = new AddFloorAndRoomControl(mAppContext);
        deleteRoomControl = new DeleteRoomControl(mAppContext);
        initFloorDate();
        firstSetFloorListAdapter = new FirstSetFloorListAdapter(mContext, floorsList, true);
        firstSetFloorListAdapter.setRoomShowHashMap(roomShowHashMap);

        floorListView.setAdapter(firstSetFloorListAdapter);
        addFirstFloor();
        refreshFloorAndRoom();
    }

    private void initListener() {
        addFloorTextView.setOnClickListener(this);
        floorNameTextView.setOnClickListener(this);
        deleteRoomImageView.setOnClickListener(this);
        floorListView.setOnScrollListener(this);
    }

    private void initFloorDate() {
        if (rooms != null) {
            rooms.clear();
        }
        if (floorsList != null) {
            floorsList.clear();
        }
        if (tempFloorsList != null) {
            tempFloorsList.clear();
        }

        floors = floorDao.selAllFloors(uid);
        for (Floor floor : floors) {
            floorHashMap = new HashMap<Floor, List<Room>>();
            rooms = roomDao.selRoomsByFloor(uid, floor.getFloorId());
            FloorAndRoomUtil.AddLastRoom(mAppContext, rooms);
            floorHashMap.put(floor, rooms);
            if (roomShowHashMap != null && !roomShowHashMap.containsKey(floor.getFloorId())) {
                roomShowHashMap.put(floor.getFloorId(), true);
            }
            if (floorHashMap != null && floorHashMap.size() > 0) {
                floorsList.add(floorHashMap);
            }
        }
    }

    /**
     * 如果没有楼层房间则添加一层楼
     */
    private void addFirstFloor() {
        if (floors == null || floors != null && floors.size() == 0) {
            addFloor();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addFloorTextView:
                if (isAddFloorButtonClickable()) {
                    addFloor();
                }
                break;
            case R.id.editFloorImageView:
                break;
            default:
                break;
        }
    }

    private void addFloor() {
        if (tempFloorsList != null && tempFloorsList.size() > 0) {
            tempFloorsList.clear();
        }
        if (floorsList != null && floorsList.size() < MAX_FLOOR_COUNT) {
            tempFloorsList.add(FloorAndRoomUtil.getFloorAndCommonRooms(mAppContext, floorsList.size()));
            showDialogNow();
            addFloorAndRoomControl.startAddFloorAndRoom(userName, uid, tempFloorsList);
        } else if (floorsList != null && floorsList.size() >= MAX_FLOOR_COUNT) {
            ToastUtil.showToast(getResources().getString(R.string.add_floor_max_error));
        }
    }

    public void addRoom(View v) {
        room = (Room) v.getTag();
        String[] contentDescription = v.getContentDescription().toString()
                .split("\\|");
        floorPosition = Integer.valueOf(contentDescription[0]);
        roomPosition = Integer.valueOf(contentDescription[1]);
        int roomSize = Integer.valueOf(contentDescription[2]);
        if (roomPosition == roomSize - 1) {
            //添加房间
            Floor floor = (Floor) (floorsList.get(floorPosition).keySet()
                    .toArray()[0]);
            String floorId = floor.getFloorId();
            Intent intent = new Intent();
            intent.setClass(mAppContext, SelectRoomTypeActivity.class);
            intent.putExtra("floorId", floorId);
            intent.putExtra("srcActivity", SetFloorAndRoomActivity.class.getSimpleName());
            startActivityForResult(intent, REQUEST_CODE_ADD_ROOM);
        } else if (roomPosition < roomSize - 1) {
            Intent intent = new Intent();
            intent.setClass(mAppContext, ModifyRoomActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("room", room);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_MODIFY_ROOM);
        }
    }

    public void leftTitleClick(View v) {
        finish();
    }

    public void renameFloorName(View v) {
        floor = (Floor) v.getTag();
        floorPosition = Integer.valueOf(v.getContentDescription().toString());
        Intent intent = new Intent();
        intent.setClass(mAppContext, ModifyFloorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("floor", floor);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_MODIFY_FLOOR);
    }

    public void deleteFloor(View v) {
        floor = (Floor) v.getTag();
        floorPosition = Integer.valueOf(v.getContentDescription().toString());
        if (DeviceUtil.isFloorHasDevices(uid, floor.getFloorId())) {
            deleteFloorPopup.showPopup(mContext, getResources()
                    .getString(R.string.delete_floor_tips_title), getResources()
                    .getString(R.string.delete_floor_tips), getResources()
                    .getString(R.string.know), null);
        } else {
            if (!ClickUtil.isFastDoubleClick()) {
                showDialogNow();
                deleteFloorControl.startDeleteFloor(userName, uid, floor.getFloorId());
            }
        }
    }

    /**
     * 设置是否显示楼层下的房间
     *
     * @param view
     */
    public void showRoom(View view) {
        floor = (Floor) view.getTag();
        boolean roomShowFlag = roomShowHashMap.get(floor.getFloorId());
        roomShowFlag = !roomShowFlag;
        roomShowHashMap.put(floor.getFloorId(), roomShowFlag);
        firstSetFloorListAdapter.setRoomShowHashMap(roomShowHashMap);
        firstSetFloorListAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(floorListView);
    }

    // android5.0在布局文件中申明onclick函数，activity中 没有此函数会报错
    public void deleteRoom(View v) {
        room = (Room) v.getTag();
        String[] contentDescription = v.getContentDescription().toString()
                .split("\\|");
        floorPosition = Integer.valueOf(contentDescription[0]);
        roomPosition = Integer.valueOf(contentDescription[1]);
        if (DeviceUtil.isRoomHasDevices(uid, room.getRoomId())) {
            deleteRoomPopup.showPopup(mContext, getResources()
                    .getString(R.string.delete_room_tips), getResources()
                    .getString(R.string.delete), getResources()
                    .getString(R.string.cancel));
        } else {
            showDialogNow();
            deleteRoomControl.startDeleteRoom(userName, uid, room.getRoomId());
        }
    }

    private boolean isAddFloorButtonClickable() {
        return addFloorTextView.isClickable() && !ClickUtil.isFastDoubleClick(1000);
    }

    private class DeleteFloorControl extends DeleteFloor {
        public DeleteFloorControl(Context context) {
            super(context);
        }

        @Override
        public void onDeleteFloorResult(String uid, int serial, int result, String floorId) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
//                floorListView.smoothScrollToPosition(floorsList.size() - 1);
//                firstSetFloorListAdapter.deleteRefresh(true);
                ToastUtil.showToast(getResources().getString(R.string.delete_success));
                floorsList.remove(floorPosition);
                if (roomShowHashMap != null && roomShowHashMap.containsKey(floor.getFloorId())) {
                    roomShowHashMap.remove(floorId);
                    firstSetFloorListAdapter.setRoomShowHashMap(roomShowHashMap);
                }
                refreshFloorAndRoom();
            } else {
                ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
            }
        }
    }

    private class AddFloorAndRoomControl extends AddFloorAndRoom {

        public AddFloorAndRoomControl(Context context) {
            super(context);
        }

        @Override
        public void onAddFloorAndRoomResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                hideAllRoom();
                initFloorDate();
                refreshFloorAndRoom();
                floorListView.smoothScrollToPosition(floorsList.size() - 1);
            } else {
                ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
            }
        }
    }

    private void hideAllRoom() {
        Iterator iter = roomShowHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            entry.setValue(false);
        }
    }

    private class DeleteRoomControl extends DeleteRoom {

        public DeleteRoomControl(Context context) {
            super(context);
        }

        @Override
        public void onDeleteRoomResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                HashMap<Floor, List<Room>> floorHashMap = floorsList.get(floorPosition);
                List<Room> rooms = ((List<Room>) (floorHashMap.values().toArray()[0]));
                rooms.remove(roomPosition);
                refreshFloorAndRoom();
            } else {
                ToastUtil.showToast(ErrorMessage.getError(mAppContext, result));
            }
        }
    }

    /**
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * <p/>
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        switch (requestCode) {
            case REQUEST_CODE_ADD_ROOM:
                if (data != null) {
                    String roomId = data.getExtras().getString("roomId");
                    Room room = new RoomDao().selRoom(uid, roomId);
                    if (room != null) {
                        HashMap<Floor, List<Room>> floorHashMap = floorsList.get(floorPosition);
                        List<Room> rooms = ((List<Room>) (floorHashMap.values().toArray()[0]));
                        rooms.add(rooms.size() - 1, room);
                        refreshFloorAndRoom();
                    }
                }
                break;
            case REQUEST_CODE_MODIFY_ROOM:
                if (resultCode == RESULT_CODE_MODIFY_ROOM) {
                    if (data != null) {
                        result = data.getExtras().getString("newRoomName");
                        int roomType = data.getExtras().getInt("newRoomType");
                        HashMap<Floor, List<Room>> floorHashMap = floorsList.get(floorPosition);
                        List<Room> rooms = ((List<Room>) (floorHashMap.values().toArray()[0]));
                        rooms.get(roomPosition).setRoomName(result);
                        rooms.get(roomPosition).setRoomType(roomType);
                        refreshFloorAndRoom();
                    }
                }
                break;
            case REQUEST_CODE_MODIFY_FLOOR:
                if (resultCode == RESULT_CODE_MODIFY_FLOOR) {
                    if (data != null) {
                        result = data.getExtras().getString("newFloorName");
                        Floor floor = (Floor) (floorsList.get(floorPosition).keySet()
                                .toArray()[0]);
                        floor.setFloorName(result);
                        refreshFloorAndRoom();
                    }
                    break;
                }
        }
    }

    private class SavaPopup extends ConfirmAndCancelPopup {
        public void confirm() {
            dismiss();
        }

        public void cancel() {
            finish();
        }
    }

    private class DeleteFloorPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }

    }

    private class DeleteRoomPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            showDialogNow();
            deleteRoomControl.startDeleteRoom(userName, uid, room.getRoomId());
            dismiss();
        }
    }

    private void refreshFloorAndRoom() {
        setListViewHeightBasedOnChildren(floorListView);
        firstSetFloorListAdapter.notifyDataSetChanged();
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (firstSetFloorListAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < firstSetFloorListAdapter.getCount(); i++) {
            View listItem = firstSetFloorListAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (firstSetFloorListAdapter.getCount() - 1));
        //((ViewGroup.MarginLayoutParams) params).setMargins(0, 10, 10, 0);
        listView.setLayoutParams(params);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}