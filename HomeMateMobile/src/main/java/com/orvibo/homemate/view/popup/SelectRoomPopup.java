//package com.orvibo.homemate.view.popup;
//
//import android.content.Context;
//import android.os.Handler;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.AdapterView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Floor;
//import com.orvibo.homemate.bo.Room;
//import com.orvibo.homemate.dao.DeviceDao;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.room.adapter.SelectFloorAdapter;
//import com.orvibo.homemate.room.adapter.SelectRoomAdapter;
//import com.orvibo.homemate.sharedPreferences.RoomCache;
//import com.orvibo.homemate.sharedPreferences.UserCache;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.StringUtil;
//
//import java.util.List;
//
///**
// * Created by huangqiyao on 2015/4/10.
// */
//public class SelectRoomPopup extends CommonPopup implements View.OnClickListener, View.OnKeyListener {
//    private static final String TAG = SelectRoomPopup.class.getSimpleName();
//    private SelectRoom mSelectRoom;
//    private SelectFloorAdapter mFloorAdapter;
//    private SelectRoomAdapter mRoomAdapter;
//
//    private View mContentView;
//    private ListView room_lv;
//    private LinearLayout mContent_ll;
//    private ImageView mDrag_iv;
//    private ImageView mArrow_iv;
//    private String mUid;
//
//    /**
//     * true记录选择了的房间，下次进来时就显示该房间。
//     */
//    private volatile boolean recordRoom = false;
//
//    /**
//     * true在设备未知房间先显示主机，就算没有设备在未知房间，但如果设置为true，就需要显示未知楼层和未知房间
//     */
//    private volatile boolean mShowGatewayIfNoneDevice = false;
//
//    /**
//     * 标记房间中没有设备时，是否显示该房间 true：房间中没有设备也显示该房间，false房间中没有设备不显示该房间
//     */
//    private volatile boolean mShowRoomNoHasDevices = false;
//    /**
//     * true只有1个楼层
//     */
//    private boolean isOnlyOneFloor;
//
//    /**
//     * @param context
//     * @param arrow
//     * @param recordRoom true记录房间，就算退出app，下次打开时也会显示上一次选择的房间
//     */
//    public SelectRoomPopup(Context context, View arrow, boolean recordRoom) {
//        this(context, arrow, recordRoom, false,true);
//    }
//
//    /**
//     * @param context
//     * @param arrow
//     * @param recordRoom              true记录房间，就算退出app，下次打开时也会显示上一次选择的房间
//     * @param showGatewayIfNoneDevice true在设备未知房间先显示主机，就算没有设备在未知房间，但如果设置为true，就需要显示未知楼层和未知房间
//     * @param showRoomNoHasDevices    标记房间中没有设备时，是否显示该房间 true：房间中没有设备也显示该房间，false房间中没有设备不显示该房间
//     */
//    public SelectRoomPopup(Context context, View arrow, boolean recordRoom, boolean showGatewayIfNoneDevice,boolean showRoomNoHasDevices) {
//        mContext = context;
//        mArrow_iv = (ImageView) arrow;
//        this.recordRoom = recordRoom;
//        this.mShowGatewayIfNoneDevice = showGatewayIfNoneDevice;
//        this.mShowRoomNoHasDevices = showRoomNoHasDevices;
//        mUid = UserCache.getCurrentMainUid(context);
//
//        initSelectRoom(context, mUid);
//        isOnlyOneFloor = mSelectRoom.isOnlyOneFloor();
//        String oldRoomId = Constant.NULL_DATA;
//        if (recordRoom) {
//            oldRoomId = RoomCache.getRoom(mContext, mUid);
//        }
//        LogUtil.d(TAG, "SelectRoomPopup()-uid:" + mUid + ",oldRoomId:" + oldRoomId);
//        mSelectRoom.init(oldRoomId);
//    }
//
//    @Override
//    protected void onPopupDismiss() {
//        super.onPopupDismiss();
//        mArrow_iv.setAnimation(null);
//        mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);
////        mArrow_iv.clearAnimation();
//    }
//
//    public void show(View topView) {
//        mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);
//        startArrowInAnim();
//
//        mContentView = LayoutInflater.from(mContext).inflate(
//                isOnlyOneFloor ? R.layout.popup_select_room : R.layout.popup_select_floor_room, null);
//        mContent_ll = (LinearLayout) mContentView.findViewById(R.id.content_ll);
//        mDrag_iv = (ImageView) mContentView.findViewById(R.id.drag_iv);
//        mDrag_iv.setOnClickListener(this);
//        mContentView.findViewById(R.id.top_view).setOnClickListener(this);
//        mContentView.findViewById(R.id.bottom_view).setOnClickListener(this);
//
//        if (!isOnlyOneFloor) {
//            ListView floor_lv = (ListView) mContentView.findViewById(R.id.floor_lv);
//            floor_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Floor floor = (Floor) view.getTag(R.id.tag_floor);
//                    mSelectRoom.selectFloor(floor.getFloorId());
//                }
//            });
//            List<Floor> floors = mSelectRoom.getFloors();
//            LogUtil.d(TAG, "showPopup()-floors:" + floors + ",selectedFloorId:" + mSelectRoom.getSelectedFloorId());
//            mFloorAdapter = new SelectFloorAdapter(mContext, floors, mSelectRoom.getSelectedFloorId());
//            floor_lv.setAdapter(mFloorAdapter);
//            floor_lv.setOnKeyListener(this);
//        }
//        room_lv = (ListView) mContentView.findViewById(R.id.room_lv);
//        room_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                dismissAfterAnim();
//                Room room = (Room) view.getTag(R.id.tag_room);
//                LogUtil.d(TAG, "onItemClick()-room:" + room);
//                mRoomAdapter.selectRoom(room.getRoomId());
//                mSelectRoom.selectRoom(room.getRoomId());
//            }
//        });
//
//        room_lv.setOnKeyListener(this);
//        List<Room> rooms = mSelectRoom.getRooms(mSelectRoom.getSelectedFloorId());
//        String selectedRoomId = mSelectRoom.getSelectedRoomId();
//        LogUtil.d(TAG, "showPopup()-rooms:" + rooms + ",selectedRoomId:" + selectedRoomId);
//        mRoomAdapter = new SelectRoomAdapter(mContext, rooms, selectedRoomId);
//        room_lv.setAdapter(mRoomAdapter);
//        //当房间很多时也能显示当前选中的房间
//        if (!StringUtil.isEmpty(selectedRoomId) && rooms != null && !rooms.isEmpty()) {
//            final int size = rooms.size();
//            int pos = Constant.INVALID_NUM;
//            for (int i = 0; i < size; i++) {
//                if (selectedRoomId.equals(rooms.get(i).getRoomId())) {
//                    pos = i;
//                    break;
//                }
//            }
//            room_lv.setSelection(pos);
//        }
//        show(mContext, mContentView, true);
////        showPopup(mContext, mContentView, topView, true);
//        startInAnim();
//    }
//
//    /**
//     * 显示选择房间界面动画
//     */
//    public void startInAnim() {
//        startAnim(R.anim.top_2_bottom_in);
//    }
//
//    private void startAnim(int anim) {
//        mContent_ll.startAnimation(AnimationUtils.loadAnimation(mContext, anim));
//    }
//
//    @Override
//    public void onClick(View v) {
//        dismissAfterAnim();
//    }
//
//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0 && isShowing()) {
//            LogUtil.d(TAG, "onKey()");
//            dismissAfterAnim();
//            return true;
//        }
//        return false;
//    }
//
//    private void startArrowInAnim() {
//        final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.roate_0_to_180);
//        animation.setFillAfter(true);
//        animation.setFillBefore(false);
//        animation.setFillEnabled(true);
//        mArrow_iv.startAnimation(animation);
//    }
//
//    private void startArrowOutAnim() {
//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.roate_180_to_0);
//        animation.setFillAfter(true);
//        animation.setFillBefore(false);
//        animation.setFillEnabled(true);
//        mArrow_iv.startAnimation(animation);
//    }
//
//    /**
//     * 动画界面后再关闭界面
//     */
//    public void dismissAfterAnim() {
//        mArrow_iv.clearAnimation();
//        mArrow_iv.setImageResource(R.drawable.arrow_up_selector);
//        startArrowOutAnim();
//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.top_2_bottom_out);
//        mContent_ll.startAnimation(animation);
//        mContentView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.to_tran));
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                //注意：4.1版本不能直接dismiss()。
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        dismiss();
//                    }
//                });
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//    }
//
//    private void initSelectRoom(Context context, final String uid) {
//        DeviceDao deviceDao = new DeviceDao();
//        boolean hasUnknownRoom = deviceDao.hasUnknownRoom(uid);
//        boolean showUnknownRoom = mShowGatewayIfNoneDevice || hasUnknownRoom;
//        boolean hasCOCODevice = !deviceDao.selWifiDevicesByUserId(UserCache.getCurrentUserId(context)).isEmpty();
//        mSelectRoom = new SelectRoom(context, uid, showUnknownRoom || hasCOCODevice,mShowRoomNoHasDevices) {
//            @Override
//            protected void onSelected(Floor floor, Room room) {
//                //选择房间
//                if (recordRoom) {
//                    if (room != null) {
//                        RoomCache.saveRoom(mContext, uid, room.getRoomId());
//                    }
//                }
//                onRoomSelected(floor, room);
//            }
//
//            @Override
//            protected void onSelectFloor(String floorId, List<Room> rooms) {
//                //选择楼层
//                mFloorAdapter.selectFloor(floorId);
//                mRoomAdapter = new SelectRoomAdapter(mContext, rooms, mSelectRoom.getSelectedRoomId());
//                room_lv.setAdapter(mRoomAdapter);
////                if (floorId == Constant.UNKOWN_FLOOR && recordRoom) {
////                    dismissAfterAnim();
////                    RoomCache.saveRoom(mContext, uid, Constant.UNKOWN_ROOM);
////                    mRoomAdapter.selectRoom(Constant.UNKOWN_ROOM);
////                    mSelectRoom.selectRoom(Constant.UNKOWN_ROOM);
////                }
//            }
//        };
//    }
//
//    /**
//     * @return true此住宅还没有创建房间
//     */
//    public boolean isEmptyRoom() {
//        return mSelectRoom.isEmptyRoom();
//    }
//
//    protected void onRoomSelected(Floor floor, Room room) {
//
//    }
//}
