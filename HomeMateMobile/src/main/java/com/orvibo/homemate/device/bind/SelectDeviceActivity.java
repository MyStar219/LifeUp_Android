package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
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
import com.orvibo.homemate.dao.SceneBindDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.adapter.DevicesSelectAdapter;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.SelectFloorRoomPopup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 情景联动绑定-选择设备列表，可以选择多个设备，但是非红外设备一个设备只能选择一次，红外设备由于可以选择多个按键，所有可以选择多次
 */
public class SelectDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        OOReport.OnDeviceOOReportListener {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();
    private DevicesSelectAdapter mDeviceSelectAdapter;
    private DeviceDao mDeviceDao;
    private DeviceStatusDao mDeviceStatusDao;
    private String mCurrentRoomId;
    // 主机的uid
    private String mMainUid;

    private SelectFloorRoomPopup mSelectRoomPopup;
    private ListView mDevices_lv;
    private TextView mTitle_tv;
    private ImageView deviceEmptyImageView;
    private TextView deviceEmptyTextView;
    private View mBar;
    private LinearLayout mSelectRoom_ll;
    private LinearLayout deviceEmptyLinearLayout;
    private ImageView mArrow_iv;
    private ArrayList<Device> checkedDevices = new ArrayList<Device>();
    private ArrayList<Device> checkedDevicesTemp = new ArrayList<Device>();
    private List<Device> devices;
    private List<DeviceStatus> deviceStatuses;

    private ArrayList<Integer> mSelectedDeviceIds;

    private int mBindActionType;
    private int mBindSize;
//    private int commonDevice;

    /**
     * {@link IntelligentSceneConditionType}
     */
    private int conditionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_devices);

        mSelectedDeviceIds = getIntent().getIntegerArrayListExtra(IntentKey.SELECTED_BIND_IDS);
        mBindActionType = getIntent().getIntExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);
        mBindSize = getIntent().getIntExtra(IntentKey.BIND_SIZE, 0);
//        commonDevice = getIntent().getIntExtra(IntentKey.COMMON_DEVICE, 0);
        conditionType = getIntent().getIntExtra(IntentKey.LINKAGE_CONDITION_TYPE, Constant.INVALID_NUM);
        checkedDevicesTemp = (ArrayList<Device>) getIntent().getSerializableExtra(IntentKey.DEVICES);
        if (checkedDevicesTemp != null) {
            checkedDevices.addAll(checkedDevicesTemp);
        }
        init();
    }

    private void init() {
        mContext = SelectDeviceActivity.this;
        initView();
        initEmptyView();
        initListener();
        initData();
        initRoom();
    }

    private void initRoom() {

    }

    private void initView() {
        findViewById(R.id.back_iv).setVisibility(View.VISIBLE);
        mTitle_tv = (TextView) findViewById(R.id.title_tv);
        mBar = findViewById(R.id.bar_rl);
        mSelectRoom_ll = (LinearLayout) findViewById(R.id.selectRoom_ll);
        mSelectRoom_ll.setOnClickListener(this);
        deviceEmptyLinearLayout = (LinearLayout) findViewById(R.id.deviceEmptyLinearLayout);
        mArrow_iv = (ImageView) findViewById(R.id.arrow_iv);
        mDevices_lv = (ListView) findViewById(R.id.devices_lv);
        deviceEmptyImageView = (ImageView) findViewById(R.id.deviceEmptyImageView);
        deviceEmptyTextView = (TextView) findViewById(R.id.deviceEmptyTextView);
    }

    private void initListener() {
        mDevices_lv.setOnItemClickListener(this);
    }

    private void initData() {
        mMainUid = UserCache.getCurrentMainUid(mContext);

        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        OOReport.getInstance(mAppContext).registerOOReport(this);
        selectRoom();
    }

    private void initEmptyView() {
        if (devices == null || devices.size() == 0) {
            if (mBindActionType == BindActionType.SET_WIDGET) {
                //如果帐号下没有设备
                deviceEmptyTextView.setText(getResources().getString(R.string.no_widget_device_tip));
                deviceEmptyImageView.setBackgroundResource(R.drawable.bg_no_device);
            } else {
                //如果帐号下没有设备
                deviceEmptyTextView.setText(getResources().getString(R.string.intelligent_scene_device_empty_tips));
                deviceEmptyImageView.setBackgroundResource(R.drawable.bg_no_device);
            }

        }
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
        LogUtil.d(TAG, "onItemClick()-device:" + device);
        LogUtil.d(TAG, "onItemClick(0)-checkedDevices:" + checkedDevices);
        boolean isOnline = mDeviceSelectAdapter.isOnline(device.getDeviceId());
        boolean isLearned = DeviceTool.isIrDevicelearned(device);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbDevice);
        boolean canCheck = true;
        if (mBindActionType != BindActionType.COMMON) {
            if (!isOnline) {
                canCheck = false;
            } else if (DeviceUtil.isIrDevice(device) && !isLearned) {
                canCheck = false;
            }
        }
        if (canCheck) {
            checkBox.performClick();
            if (checkBox.isChecked()) {
                if (!isContainDevice(device)) {
                    if (mBindActionType == BindActionType.COMMON && checkedDevices.size() >= 4) {
                        ToastUtil.showToast(R.string.personal_common_device_max_toast);
                    } else if (isAddedTo32SceneBind(device) && mBindActionType == BindActionType.SCENE) {
                        showSceneBindMaxToast(device);
                    } else if (mBindActionType == BindActionType.LINKAGE && checkedDevices != null && checkedDevices.size() + mBindSize >= 16) {
                        ToastUtil.toastError(ErrorCode.LINKAGE_BIND_MAX_ERROR);
                    } else if (mBindActionType == BindActionType.SET_WIDGET && checkedDevices.size() >= mBindSize) {
                        ToastUtil.showToast(getResources().getString(R.string.out_widget_device_tip));
                    } else {
                        checkedDevices.add(device);
                    }
                }
            } else {
                if (isContainDevice(device)) {
                    if (checkedDevices != null && !checkedDevices.isEmpty()) {
                        int pos = Constant.INVALID_NUM;
                        final int size = checkedDevices.size();
                        final String deviceId = device.getDeviceId();
                        for (int i = 0; i < size; i++) {
                            if (deviceId.equals(checkedDevices.get(i).getDeviceId())) {
                                pos = i;
                                break;
                            }
                        }
                        if (pos >= 0) {
                            checkedDevices.remove(pos);
                        }
                    }
                    checkedDevices.remove(device);
                }
            }
            LogUtil.d(TAG, "onItemClick(1)-checkedDevices:" + checkedDevices);
            mDeviceSelectAdapter.refreshDevices(devices, checkedDevices,
                    deviceStatuses);
        } else {
            checkBox.setChecked(false);
        }
        LogUtil.d(TAG, "onItemClick()-finish");
        //}
    }

    /**
     * @return true:一个设备已经添加到32个情景；false一个设备添加到情景个数小于32
     * @author smagret
     * @date 2015/09/17
     * 判断一个设备是否已经添加到32个情景中（一个设备只能添加到32个情景中,虚拟红外设备情景绑定是存储在主机中，所以没有这个限制
     * 灯光类一路设备默认加入到全开全关情景中，所以手动添加的情景绑定只能添加30个，非灯光类非虚拟红外设备可以添加32个
     * 灯光类多路设备，多路共用这32个名额）
     */
    private boolean isAddedTo32SceneBind(Device device) {
        if (DeviceUtil.isIrOrWifiAC(device)) {
            return false;
        } else {
            int deviceType = device.getDeviceType();
            if (deviceType == DeviceType.DIMMER
                    || deviceType == DeviceType.RGB || deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
                int sceneBindCount = new SceneBindDao().selSceneBindCountByDeviceId(device.getDeviceId());
                return sceneBindCount > 29;
            } else if (deviceType == DeviceType.LAMP) {
                List<String> deviceIds = new DeviceDao().selLampDeviceIdByExtAddr(device.getUid(), device.getExtAddr());
                if (deviceIds != null) {
                    int sceneBindCount = new SceneBindDao().selLampSceneBindCountByDeviceIds(deviceIds);
                    return sceneBindCount > (31 - deviceIds.size() * 2);
                } else {
                    return false;
                }

            } else {
                int sceneBindCount = new SceneBindDao().selSceneBindCountByDeviceId(device.getDeviceId());
                return sceneBindCount > 31;
            }
        }
    }

    /**
     * @author smagret
     * @date 2015/09/17
     */
    private void showSceneBindMaxToast(Device device) {

        int deviceType = device.getDeviceType();
        if (deviceType == DeviceType.LAMP) {
            List<String> deviceIds = new DeviceDao().selLampDeviceIdByExtAddr(device.getUid(), device.getExtAddr());
            if (deviceIds.size() > 1) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String deviceId : deviceIds) {

                    String deviceName = new DeviceDao().selDevice(deviceId).getDeviceName();
                    stringBuffer.append(deviceName + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                String finalString = String.format(getResources().getString(R.string.device_bind_scene_max_count_tips2), deviceIds.size(), stringBuffer.toString());
                new ToastPopup().showPopup(mContext, finalString, getResources()
                        .getString(R.string.know), null);
            } else {
                new ToastPopup().showPopup(mContext, getResources().getString(R.string.device_bind_scene_max_count_tips), getResources()
                        .getString(R.string.know), null);
            }

        } else {
            new ToastPopup().showPopup(mContext, getResources().getString(R.string.device_bind_scene_max_count_tips), getResources()
                    .getString(R.string.know), null);
        }

    }


    private boolean isContainDevice(Device selectedDevice) {
        boolean selected = false;
        if (checkedDevices != null && !checkedDevices.isEmpty() && selectedDevice != null) {
            String deviceId = selectedDevice.getDeviceId();
            if (!StringUtil.isEmpty(deviceId)) {
                for (Device device : checkedDevices) {
                    if (deviceId.equals(device.getDeviceId())) {
                        selected = true;
                        break;
                    }
                }
            }
        }
        return selected;
    }

    private void selectRoom() {
        mSelectRoomPopup = new SelectFloorRoomPopup(this, mArrow_iv, false, false, false, true) {
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
        if (mDeviceSelectAdapter != null) {
            mDeviceSelectAdapter.refreshOnline(uid, deviceId, online);
        }
    }

    private void refreshAllRoomDevices() {
        boolean isEmptyDevice = mDeviceDao.isEmptyDevice(mMainUid);
        if (isEmptyDevice) {
            ((ViewGroup) mDevices_lv.getParent()).removeView(deviceEmptyLinearLayout);
            ((ViewGroup) mDevices_lv.getParent()).addView(deviceEmptyLinearLayout);
            mDevices_lv.setEmptyView(deviceEmptyLinearLayout);
        } else {
            if (StringUtil.isEmpty(mCurrentRoomId) || mCurrentRoomId.equals(Constant.ALL_ROOM)) {
                // devices = mDeviceDao.selNoRoomControlDevices(mMainUid);
                if (mBindActionType == BindActionType.COMMON) {
//                    devices = mDeviceDao.selCommonDevicesByRoom(userId, "", DeviceUtil.getTypeSQL(5));
//                    deviceStatuses = mDeviceStatusDao
//                            .selCommonDeviceStatusesByRoom(userId, "");
                    devices = DeviceTool.getAllCommonDevices(mContext);
                    deviceStatuses = DeviceTool.getDeviceStatusesByDevices(devices);
                } else if (mBindActionType == BindActionType.SET_WIDGET) {

                    devices = DeviceTool.getLampAndSocket(mContext);
                    deviceStatuses = DeviceTool.getDeviceStatusesByDevices(devices);
                } else {
                    devices = mDeviceDao.selBindDevicesByRoom(mMainUid, "");
                    deviceStatuses = mDeviceStatusDao
                            .selDeviceStatusesByRoom(mMainUid, "");
                }
            } else {
                if (mBindActionType == BindActionType.COMMON) {
//                    devices = mDeviceDao.selCommonDevicesByRoom(userId, mCurrentRoomId, DeviceUtil.getTypeSQL(5));
//                    deviceStatuses = mDeviceStatusDao
//                            .selCommonDeviceStatusesByRoom(userId, mCurrentRoomId);
                    devices = DeviceTool.getCommonDevicesByRoom(mContext, mCurrentRoomId);
                    deviceStatuses = DeviceTool.getDeviceStatusesByDevices(devices);
                } else if (mBindActionType == BindActionType.SET_WIDGET) {
                    devices = DeviceTool.getLampAndSocket(mContext);
                    deviceStatuses = DeviceTool.getDeviceStatusesByDevices(devices);
                } else {
                    devices = mDeviceDao.selBindDevicesByRoom(mMainUid, mCurrentRoomId);
                    deviceStatuses = mDeviceStatusDao
                            .selDeviceStatusesByRoom(mMainUid, mCurrentRoomId);
                }
            }
            //TODO 情景选择非虚拟红外设备只能选择一次，联动可以选择多次
            // 过滤已被选中的设备
            if (devices != null && !devices.isEmpty() && mSelectedDeviceIds != null && !mSelectedDeviceIds.isEmpty()) {
                List<Device> noSelectedDevices = new ArrayList<Device>();
                List<Device> tempDevices = devices;
                ArrayList<Integer> tempDeviceIds = mSelectedDeviceIds;
                Set<String> showDeviceIds = new HashSet<String>();//存放显示在列表的设备
                for (Device device : tempDevices) {
                    String deviceId = device.getDeviceId();
                    if (showDeviceIds.contains(deviceId)) {
                        continue;
                    }
                    if (mBindActionType == BindActionType.LINKAGE) {
                        noSelectedDevices.add(device);
                        showDeviceIds.add(deviceId);
                    } else {
                        if (!tempDeviceIds.contains(deviceId)) {
                            noSelectedDevices.add(device);
                            showDeviceIds.add(deviceId);
                        } else {
                            if (DeviceUtil.isIrOrWifiAC(device)) {
                                noSelectedDevices.add(device);
                                showDeviceIds.add(deviceId);
                            }
                        }
                    }
                }
                devices = noSelectedDevices;
            }
            LogUtil.d(TAG, "refreshAllRoomDevices()-devices:" + devices);
            // LogUtil.d(TAG, "refreshAllRoomDevices()-deviceStatuses:" + deviceStatuses);
            LogUtil.d(TAG, "refreshAllRoomDevices()-checkedDevices:" + checkedDevices);
            LogUtil.d(TAG, "refreshAllRoomDevices()-mSelectedDeviceIds:" + mSelectedDeviceIds);

            if (mDeviceSelectAdapter == null) {
                mDeviceSelectAdapter = new DevicesSelectAdapter(this, devices, checkedDevices,
                        deviceStatuses);
                mDevices_lv.setAdapter(mDeviceSelectAdapter);
                mDeviceSelectAdapter.notifyDataSetChanged();
            } else {
                mDeviceSelectAdapter.refreshDevices(devices, checkedDevices,
                        deviceStatuses);
            }
            if (mBindActionType == BindActionType.COMMON) {
                mDeviceSelectAdapter.setIsFromCommonDevice(true);
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
        returnResult();
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        returnResult();
        super.onBackPressed();
    }

    private void returnResult() {
        if (checkedDevices != null && !checkedDevices.isEmpty()) {
            Intent data = new Intent();
            data.putExtra("checkedDevices", checkedDevices);
            LogUtil.d(TAG, "onBackPressed() - checkedDevices" + checkedDevices);
            setResult(RESULT_OK, data);
        } else if (mBindActionType == BindActionType.COMMON) {
            Intent data = new Intent();
            data.putExtra("checkedDevices", checkedDevices);
            LogUtil.d(TAG, "onBackPressed() - checkedDevices" + checkedDevices);
            setResult(RESULT_OK, data);
        }
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        OOReport.getInstance(mAppContext).removeOOReport(this);
        super.onDestroy();
    }
}
