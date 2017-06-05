package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.adapter.DevicesSelectSingleAdapter;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.popup.SelectFloorRoomPopup;

import java.util.List;


/**
 * 情景面板和遥控器-选择设备列表，只能选择一个设备
 */
public class SelectSingleDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        OOReport.OnDeviceOOReportListener {

    /**
     * intent传递数据使用的key
     */
    public static final String KEY_DEVICE_IDS = "deviceIds";

    private  final String TAG = SelectSingleDeviceActivity.class.getSimpleName();
    private DevicesSelectSingleAdapter mDeviceSelectSingleAdapter;
    private DeviceDao mDeviceDao;
    private DeviceStatusDao mDeviceStatusDao;
    private String mCurrentRoomId = Constant.ALL_ROOM;
    // 主机的uid
    private String mMainUid;

    private SelectFloorRoomPopup mSelectRoomPopup;
    private View mBar;
    private LinearLayout mSelectRoom_ll;
    private LinearLayout deviceEmptyLinearLayout;
    private ImageView mArrow_iv;
    private ImageView deviceEmptyImageView;
    private TextView deviceEmptyTextView;

    private ListView mDevices_lv;
    private TextView mTitle_tv;
    private Device checkedDevice;
//    private Device securityDevice;
    private List<Device> devices;
    private List<DeviceStatus> deviceStatuses;
//    private int deviceType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_devices);
        init();
    }

    private void init() {
//        Intent intent = getIntent();
//        Serializable serializable = intent.getSerializableExtra(IntentKey.SECURITY_DEVICE);
//        if (serializable != null && serializable instanceof Device) {
//            securityDevice = (Device) serializable;
//            if (securityDevice != null) {
//                deviceType = securityDevice.getDeviceType();
//            }
//        }
        initView();
        initEmptyView();
        initListener();
        initData();
    }

    private void initView() {
        mBar = findViewById(R.id.bar_rl);
        deviceEmptyLinearLayout = (LinearLayout) findViewById(R.id.deviceEmptyLinearLayout);
        mSelectRoom_ll = (LinearLayout) findViewById(R.id.selectRoom_ll);
        mSelectRoom_ll.setOnClickListener(this);
        mArrow_iv = (ImageView) findViewById(R.id.arrow_iv);

        findViewById(R.id.back_iv).setVisibility(View.VISIBLE);
        mTitle_tv = (TextView) findViewById(R.id.title_tv);
        mDevices_lv = (ListView) findViewById(R.id.devices_lv);
        deviceEmptyImageView = (ImageView)findViewById(R.id.deviceEmptyImageView);
        deviceEmptyTextView = (TextView)findViewById(R.id.deviceEmptyTextView);
    }

    private void initEmptyView() {
        if  (devices == null || devices.size() == 0) {
            //如果帐号下没有设备
            deviceEmptyTextView.setText(getResources().getString(R.string.device_add_tip));
            deviceEmptyImageView.setBackgroundResource(R.drawable.bg_no_device);
        }
    }

    private void initListener() {
        mDevices_lv.setOnItemClickListener(this);
    }

    private void initData() {
        mMainUid = UserCache.getCurrentMainUid(mContext);
        checkedDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        OOReport.getInstance(mAppContext).registerOOReport(this);

        selectRoom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllRoomDevices();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.selectRoom_ll) {
            if (mSelectRoomPopup != null) {
                if (!mSelectRoomPopup.isShowing()) {
                    mSelectRoomPopup.show(mBar);
                } else {
                    mSelectRoomPopup.dismissAfterAnim();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = (Device) view.getTag(R.id.tag_device);
        //设备在线
        if (mDeviceSelectSingleAdapter.isCanChecked(device)) {
            if (checkedDevice != null && device.getDeviceId().equals(checkedDevice.getDeviceId())) {
                checkedDevice = null;
            } else {
                checkedDevice = device;
            }
            mDeviceSelectSingleAdapter.check(checkedDevice);
        }
    }

    private void selectRoom() {
        mSelectRoomPopup = new SelectFloorRoomPopup(this, mArrow_iv, false,false,false,true) {
            @Override
            protected void onRoomSelected(Floor floor, Room room) {
                super.onRoomSelected(floor, room);
                LogUtil.d(TAG, "onRoomSelected()-floor:" + floor + ",room:" + room);

                String title = "";
                if (floor != null) {
                    if (room != null) {
                        if (Constant.ALL_ROOM.equals(room.getRoomId()) || Constant.EMPTY_FLOOR.equals(floor.getFloorId())) {
                            //如果选择所有房间，则只显示房间名称
                            RoomDao roomDao = new RoomDao();
                            int roomCount = roomDao.selHasDeviceRoomNo(currentMainUid);

                            if (roomCount > 0) {
                                title = room.getRoomName();
                            } else {
                                title = getString(R.string.device_add);
                                mArrow_iv.setVisibility(View.GONE);
                            }
                            mCurrentRoomId = Constant.ALL_ROOM;
                        } else {
                            title = floor.getFloorName() + " " + room.getRoomName();
                            mCurrentRoomId = room.getRoomId();
                        }
                    }
                } else {
                    mCurrentRoomId = Constant.ALL_ROOM;
                    title = getString(R.string.device_add);
                    mArrow_iv.setVisibility(View.GONE);
                }
                mTitle_tv.setText(title);
                refreshAllRoomDevices();
            }
        };
    }


    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (mDeviceSelectSingleAdapter != null) {
            mDeviceSelectSingleAdapter.refreshOnline(uid, deviceId, online);
        }
    }

    // 设备在线和离线报告
//    @Override
//    public void onOOReport(String uid, String extAddr, int ooStatus) {
//        LogUtil.d(TAG, "onOOReport()-uid:" + uid + ",extAddr:" + extAddr + ",ooStatus:" + ooStatus);
//        if (mDeviceSelectSingleAdapter != null) {
//            if (ooStatus == OnlineStatus.ONLINE) {
//                mDeviceSelectSingleAdapter.deviceOnline(uid, extAddr);
//            } else {
//                mDeviceSelectSingleAdapter.deviceOffline(uid, extAddr);
//            }
//        }
//    }

    private void refreshAllRoomDevices() {

        boolean isEmptyDevice = mDeviceDao.isEmptyDevice(mMainUid);
        if (isEmptyDevice) {
        } else {
            //TODO 先去掉门锁
            if (StringUtil.isEmpty(mCurrentRoomId) || mCurrentRoomId.equals(Constant.ALL_ROOM)) {
//                if (DeviceTool.isSecurityDevice(deviceType)) {
//                    devices = mDeviceDao.selBindDevicesByRoomWithoutSecurity(mMainUid, "");
//                } else {
//                    devices = mDeviceDao.selBindDevicesByRoom(mMainUid, "");
//                }
                devices = mDeviceDao.selBindDevicesByRoom(mMainUid, "");
                deviceStatuses = mDeviceStatusDao
                        .selDeviceStatusesByRoom(mMainUid, "");
            } else {
//                if (DeviceTool.isSecurityDevice(deviceType)) {
//                    devices = mDeviceDao.selBindDevicesByRoomWithoutSecurity(mMainUid, mCurrentRoomId);
//                } else {
//                    devices = mDeviceDao.selBindDevicesByRoom(mMainUid, mCurrentRoomId);
//                }
                devices = mDeviceDao.selBindDevicesByRoom(mMainUid, mCurrentRoomId);
                deviceStatuses = mDeviceStatusDao
                        .selDeviceStatusesByRoom(mMainUid, mCurrentRoomId);
            }
            // LogUtil.d(TAG, "refreshAllRoomDevices()-mCurrentRoomId:" + mCurrentRoomId);
//             LogUtil.d(TAG, "refreshAllRoomDevices()-devices:" + devices );

            if (mDeviceSelectSingleAdapter == null) {
                mDeviceSelectSingleAdapter = new DevicesSelectSingleAdapter(this, devices, checkedDevice,
                        deviceStatuses);
                mDevices_lv.setAdapter(mDeviceSelectSingleAdapter);
                mDeviceSelectSingleAdapter.notifyDataSetChanged();
            } else {
                mDeviceSelectSingleAdapter.refreshDevices(devices, checkedDevice,
                        deviceStatuses);
            }

            if (devices == null || devices.size() == 0) {
                ((ViewGroup) mDevices_lv.getParent()).removeView(deviceEmptyLinearLayout);
                ((ViewGroup) mDevices_lv.getParent()).addView(deviceEmptyLinearLayout);
                mDevices_lv.setEmptyView(deviceEmptyLinearLayout);
            } else {
                if (deviceEmptyLinearLayout != null) {
                    ((ViewGroup) mDevices_lv.getParent()).removeView(deviceEmptyLinearLayout);
                }
            }
        }
    }

    @Override
    public void leftTitleClick(View v) {
        returnData();
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        returnData();
        super.onBackPressed();
    }

    private void returnData() {
        Intent data = new Intent();
        data.putExtra(IntentKey.DEVICE, checkedDevice);
        LogUtil.d(TAG, "returnData() - checkedDevices" + checkedDevice);
        setResult(RESULT_OK, data);
    }

    @Override
    protected void onDestroy() {
        OOReport.getInstance(mAppContext).removeOOReport(this);
        super.onDestroy();
    }
}
