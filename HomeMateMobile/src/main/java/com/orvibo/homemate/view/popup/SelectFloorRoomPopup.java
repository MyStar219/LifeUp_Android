package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.room.adapter.SelectFloorAdapter;
import com.orvibo.homemate.room.adapter.SelectRoomAdapter;
import com.orvibo.homemate.sharedPreferences.RoomCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.ObservableHorizontalScrollView;

import java.util.List;

/**
 * Created by huangqiyao on 2015/4/10.
 */
public class SelectFloorRoomPopup extends CommonPopup implements View.OnClickListener, View.OnKeyListener {
    private static final String TAG = SelectFloorRoomPopup.class.getSimpleName();
    public SelectRoom mSelectRoom;
    private SelectFloorAdapter mFloorAdapter;
    private SelectRoomAdapter mRoomAdapter;

    private View mContentView;
    private TextView bottomView;
    private GridView roomGridView;
    private GridView floorGridView;
    private TextView allRoomTextView;
    private LinearLayout mContent_ll;
    private ObservableHorizontalScrollView floorHorizontalScrollView;

    private ImageView mArrow_iv;
    private String mUid;
    private Activity activity;

    /**
     * true记录选择了的房间，下次进来时就显示该房间。
     */
    private volatile boolean recordRoom = false;

    /**
     * true在设备未知房间先显示主机，就算没有设备在未知房间，但如果设置为true，就需要显示未知楼层和未知房间
     */
    private volatile boolean mShowGatewayIfNoneDevice = false;

    /**
     * 标记房间中没有设备时，是否显示该房间 true：房间中没有设备也显示该房间，false房间中没有设备不显示该房间
     */
    private volatile boolean mShowRoomNoHasDevices = false;

    /**
     * true 显示箭头；false 不显示箭头
     */
    private volatile boolean mShowArrow = true;
    /**
     * true只有1个楼层
     */
    private boolean isOnlyOneFloor;

    /**
     * @param activity
     * @param arrow
     * @param recordRoom              true记录控制设备列表选择的房间，false记录选择设备列表界面选择的房间
     * @param showGatewayIfNoneDevice true在设备未知房间先显示主机，就算没有设备在未知房间，但如果设置为true，就需要显示未知楼层和未知房间
     * @param showRoomNoHasDevices    标记房间中没有设备时，是否显示该房间 true：房间中没有设备也显示该房间，false房间中没有设备不显示该房间
     * @param showArrow               true 显示箭头；false 不显示箭头
     */
    public SelectFloorRoomPopup(Activity activity, View arrow, boolean recordRoom, boolean showGatewayIfNoneDevice,
                                boolean showRoomNoHasDevices, boolean showArrow) {
        this(activity, arrow, recordRoom, showGatewayIfNoneDevice, showRoomNoHasDevices, showArrow, true);
    }

    /**
     * @param activity
     * @param arrow
     * @param recordRoom
     * @param showGatewayIfNoneDevice
     * @param showRoomNoHasDevices
     * @param showArrow
     * @param init                    true创建构造函数时初始化楼层房间
     */
    public SelectFloorRoomPopup(Activity activity, View arrow, boolean recordRoom, boolean showGatewayIfNoneDevice,
                                boolean showRoomNoHasDevices, boolean showArrow, boolean init) {
        // this(activity, arrow, recordRoom, showGatewayIfNoneDevice, showRoomNoHasDevices, showArrow);
        this.activity = activity;
        mCommonPopupContext = activity;
        mArrow_iv = (ImageView) arrow;
        this.recordRoom = recordRoom;
        this.mShowGatewayIfNoneDevice = showGatewayIfNoneDevice;
        this.mShowRoomNoHasDevices = showRoomNoHasDevices;
        this.mShowArrow = showArrow;
        mUid = UserCache.getCurrentMainUid(activity);
        if (init) {
            init();
        }
    }

    public void init() {
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "init()-Start to init room");
        }
        initSelectRoom(activity, mUid);
        String oldRoomId;
        if (recordRoom) {
            oldRoomId = RoomCache.getRoom(mCommonPopupContext, mUid);
        } else {
            oldRoomId = RoomCache.getSelectDeviceRoom(mCommonPopupContext, mUid);
        }
        LogUtil.d(TAG, "init()-uid:" + mUid + ",oldRoomId:" + oldRoomId);
        mSelectRoom.init(oldRoomId);
        isOnlyOneFloor = mSelectRoom.isOnlyOneFloor();
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "init()-Finish to init room");
        }
    }

    @Override
    protected void onPopupDismiss() {
        super.onPopupDismiss();
        mArrow_iv.setAnimation(null);
        mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);
    }

    public void show(View topView) {
        if (mSelectRoom.isNoneFloor()) {
            mArrow_iv.setVisibility(View.GONE);
        } else {
            if (mShowArrow) {
                mArrow_iv.setVisibility(View.VISIBLE);
            } else {
                mArrow_iv.setVisibility(View.GONE);
            }
            mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);

            mContentView = LayoutInflater.from(mCommonPopupContext).inflate(R.layout.popup_select_floor_and_room, null);

            allRoomTextView = (TextView) mContentView.findViewById(R.id.allRoomTextView);
            floorHorizontalScrollView = (ObservableHorizontalScrollView) mContentView.findViewById(R.id.floorHorizontalScrollView);
            mContent_ll = (LinearLayout) mContentView.findViewById(R.id.content_ll);
            mContentView.findViewById(R.id.top_view).setOnClickListener(this);
            bottomView = (TextView) mContentView.findViewById(R.id.bottom_view);
            bottomView.setOnClickListener(this);
            allRoomTextView.setOnClickListener(this);

            if (!isOnlyOneFloor) {
                floorGridView = (GridView) mContentView.findViewById(R.id.floorGridView);
                List<Floor> floors = mSelectRoom.getFloors();
                String selectFloorId = mSelectRoom.getSelectedFloorId();
                LogUtil.d(TAG, "showPopup()-floors:" + floors + ",selectedFloorId:" + selectFloorId);
                if (Constant.ALL_ROOM.equals(mSelectRoom.getSelectedRoomId())) {
                    selectFloorId = Constant.EMPTY_FLOOR;
                }
                mFloorAdapter = new SelectFloorAdapter(mCommonPopupContext, floors, selectFloorId, this);
                floorGridView.setAdapter(mFloorAdapter);
                floorGridView.setOnKeyListener(this);
                setGridView(floors);
                floorHorizontalScrollView.setVisibility(View.VISIBLE);
            } else {
                floorHorizontalScrollView.setVisibility(View.GONE);
            }
            roomGridView = (GridView) mContentView.findViewById(R.id.roomGridView);
            roomGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dismissAfterAnim();
                    Room room = (Room) view.getTag(R.id.tag_room);
                    LogUtil.d(TAG, "onItemClick()-room:" + room);
                    mRoomAdapter.selectRoom(room.getRoomId());
                    mSelectRoom.selectRoom(room.getRoomId());
                }
            });

            roomGridView.setOnKeyListener(this);
            String selectedFloorId = mSelectRoom.getSelectedFloorId();
            List<Room> rooms = mSelectRoom.getRooms(selectedFloorId);
            String selectedRoomId = mSelectRoom.getSelectedRoomId();
            LogUtil.d(TAG, "showPopup()-rooms:" + rooms + ",selectedRoomId:" + selectedRoomId + ",selectedFloorId:" + selectedFloorId);
            mRoomAdapter = new SelectRoomAdapter(mCommonPopupContext, rooms, selectedRoomId, this);
            roomGridView.setAdapter(mRoomAdapter);
            //当房间很多时也能显示当前选中的房间
            if (Constant.ALL_ROOM.equals(selectedRoomId) || Constant.EMPTY_FLOOR.equals(selectedFloorId)) {
                selectAllRooms(true);
            } else if (rooms != null && !rooms.isEmpty()) {
                final int size = rooms.size();
                int pos = Constant.INVALID_NUM;
                for (int i = 0; i < size; i++) {
                    if (selectedRoomId.equals(rooms.get(i).getRoomId())) {
                        pos = i;
                        break;
                    }
                }
                selectAllRooms(false);
                roomGridView.setSelection(pos);
            }
            show(mCommonPopupContext, mContentView, topView, true);
            scroll();
        }
    }

    private void selectAllRooms(boolean selected) {
        if (allRoomTextView != null && mCommonPopupContext != null) {
            allRoomTextView.setTextColor(mCommonPopupContext.getResources().getColor(selected ? R.color.green : R.color.identity_tip));
        }
    }

    /**
     * 设置GirdView参数
     */
    private void setGridView(List<Floor> floors) {
        int size = floors.size();
        int length = 100;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * (length + 1) * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        floorGridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        floorGridView.setColumnWidth(itemWidth); // 设置列表项宽
        floorGridView.setHorizontalSpacing(1); // 设置列表项水平间距
        floorGridView.setStretchMode(GridView.NO_STRETCH);
        floorGridView.setNumColumns(size); // 设置列数量=列表集合数
    }


    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.top_view || vId == R.id.bottom_view) {
            mSelectRoom.correctSelectedRoom();
            dismissAfterAnim();

        } else if (vId == R.id.allRoomTextView) {
            dismiss();
            if (recordRoom) {
                RoomCache.saveRoom(mCommonPopupContext, mUid, Constant.ALL_ROOM);
            } else {
                RoomCache.saveSelectDeviceRoom(mCommonPopupContext, mUid, Constant.ALL_ROOM);
            }
            mSelectRoom.selectRoom(Constant.ALL_ROOM);
        } else if (vId == R.id.floorNameTextView) {
            Floor floor = (Floor) v.getTag(R.id.tag_floor);
            mSelectRoom.selectFloor(floor.getFloorId());
            allRoomTextView.setTextColor(mCommonPopupContext.getResources().getColor(R.color.identity_tip));
        } else if (vId == R.id.roomLinearLayout) {
            dismissAfterAnim();
            Room room = (Room) v.getTag(R.id.tag_room);
            LogUtil.d(TAG, "onItemClick()-room:" + room);
            mRoomAdapter.selectRoom(room.getRoomId());
            mSelectRoom.selectRoom(room.getRoomId());
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0 && isShowing()) {
            LogUtil.d(TAG, "onKey()");
            mSelectRoom.correctSelectedRoom();
            dismissAfterAnim();
            return true;
        }
        return false;
    }


    /**
     * 动画界面后再关闭界面
     */
    public void dismissAfterAnim() {
        mArrow_iv.setImageResource(R.drawable.select_room_arrow_up_selector);
        bottomView.setVisibility(View.GONE);
        dismiss();
    }

    private void initSelectRoom(Context context, final String uid) {
        LogUtil.d(TAG, "initSelectRoom()-uid:" + uid);
        DeviceDao deviceDao = new DeviceDao();
        boolean hasDevice = deviceDao.hasDevice(uid);
        boolean showAllRoom = mShowGatewayIfNoneDevice || hasDevice;
        boolean hasCOCODevice = !deviceDao.selWifiDevicesByUserId(UserCache.getCurrentUserId(context)).isEmpty();
        mSelectRoom = new SelectRoom(context, uid, showAllRoom || hasCOCODevice, mShowRoomNoHasDevices) {
            @Override
            protected void onSelected(Floor floor, Room room) {
                //选择房间
                if (room != null) {
                    if (recordRoom) {
                        RoomCache.saveRoom(mCommonPopupContext, uid, room.getRoomId());
                    } else {
                        RoomCache.saveSelectDeviceRoom(mCommonPopupContext, uid, room.getRoomId());
                    }
                }
                if (room != null && Constant.ALL_ROOM.equals(room.getRoomId())) {
                    selectAllRooms(true);
                } else {
                    selectAllRooms(false);
                }
                onRoomSelected(floor, room);
            }

            @Override
            protected void onSelectFloor(String floorId, List<Room> rooms) {
                //选择楼层
                mFloorAdapter.selectFloor(floorId);
                mRoomAdapter = new SelectRoomAdapter(mCommonPopupContext, rooms, mSelectRoom.getSelectedRoomId(), SelectFloorRoomPopup.this);
                roomGridView.setAdapter(mRoomAdapter);
            }
        };
    }

    /**
     * 将选中的楼层移到开头
     * 将HorizontalScrollView滑动到指定位置
     */
    private void scroll() {
        List<Floor> floors = new FloorDao().selFloorsHasDevices(mUid);
        String selectFloorId = mSelectRoom.getSelectedFloorId();
        int selectFloorPosition = 0;
        for (int i = 0; i < floors.size(); i++) {
            if (!selectFloorId.equals(Constant.EMPTY_FLOOR)
                    && floors.get(i).getFloorId().equals(selectFloorId)) {
                selectFloorPosition = i + 1;
                break;
            }
        }
        int length = 100;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        final int gridviewWidth = (int) (selectFloorPosition * length * density);
        if (floorHorizontalScrollView != null) {
            floorHorizontalScrollView.post(new Runnable() {
                @Override
                public void run() {
                    floorHorizontalScrollView.smoothScrollTo(gridviewWidth, 0);
                }
            });
        }

    }

    /**
     * @return true此住宅还没有创建房间
     */
    public boolean isEmptyRoom() {
        LogUtil.d(TAG, "isEmptyRoom()-mSelectRoom:" + mSelectRoom);
        if (mSelectRoom == null) {
            return true;
        }
        return mSelectRoom.isEmptyRoom();
    }

    protected void onRoomSelected(Floor floor, Room room) {

    }
}
