package com.orvibo.homemate.device.manage.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceMiniStatus;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.DoorLockRecordData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.DoorLockRecordDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.dao.RoomDao;
import com.orvibo.homemate.dao.UserGatewayBindDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.device.BaseDevicesAdapter;
import com.orvibo.homemate.device.ControlRecord;
import com.orvibo.homemate.device.allone2.view.AlloneAcView;
import com.orvibo.homemate.device.allone2.view.AlloneCommonView;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.sharedPreferences.DeviceCache;
import com.orvibo.homemate.sharedPreferences.GatewayOnlineCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LockUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.DeviceCustomView;
import com.orvibo.homemate.view.custom.MyCountdownTextView;
import com.orvibo.homemate.view.custom.TimingCountdownTextView;
import com.orvibo.homemate.view.custom.TouchImageView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DevicesAdapter extends BaseDevicesAdapter implements TimingCountdownTextView.OnSetLatestTypeListener, MyCountdownTextView.OnCountdownFinishedListener {
    private static final String TAG = DevicesAdapter.class.getSimpleName();
    private static final int CLASSIFICATION_ITEM = 0;
    private static final int ON_OFF_ITEM = 1;
    public static final int COCO_ITEM = 2;
    public static final int ARROW_ITEM = 3;
    private static final int EMPTY_ITEM = 4;
    private static final int ON_OFF_STOP_ITEM = 5;
    private static final int ALLONE_ITEM = 6;
    private static final int ALLONE_AC_ITEM = 7;

    private static final int ON_OFF_NORMAL = 0;
    private static final int LEFT_STOP = 1;
    private static final int RIGHT_STOP = 2;
    private static final int CURTAIN_ON = 3;
    private static final int CURTAIN_OFF = 4;

    private LayoutInflater mInflater;
    private View.OnClickListener mClickListener;
    private final Resources mRes;
    private List<List<Device>> classificationDevices;

    private static final int PERCENT_CURTAIN_TIMEOUT = 3000;
    private static final int NON_PERCENT_CURTAIN_TIMEOUT = 20 * 000;
    private final int PROPERTY_REPORT_TIMTOUT_WHAT = 1;
    private int timeout = PERCENT_CURTAIN_TIMEOUT;
    private int buttonStatus = ON_OFF_NORMAL;

    /**
     * 当前设备状态。key:uid_deviceId,value:value1。value存放的状态是最新的数据
     */
//    private Map<String, Integer> mDeviceStatuses = new ConcurrentHashMap<String, Integer>();

    /**
     * 缓存设备状态。key:uid_deviceId，value:设备开关、离线在线状态。
     */
    private ConcurrentHashMap<String, DeviceMiniStatus> mDeviceMiniStatusMap = new ConcurrentHashMap<>();

    /**
     * 当前设备开关停状态。key:uid_deviceId,value:-1，左边停，0，正常开关，1，右边停
     */
    private Map<String, Integer> mDeviceOnOffStopStatuses = new ConcurrentHashMap<String, Integer>();

    private UserGatewayBindDao mUserGatewayBindDao;
    private MessagePushDao messagePushDao;
    private MessageDao messageDao;
    private String userId;
    private int realDeviceCount;
    public Map<String, Integer> latestTypes = new HashMap<>();
    private static final String LOCK = "lock";
    private Map<String, String> mFloorNameAndRoomNames = new HashMap<String, String>();
    private boolean isAllRoom = true;
    private RoomDao mRoomDao;
    private DeviceStatusDao deviceStatusDao;
    private Handler handler;
    private String[] doorLockRecordDataRecent;
    private Animation warningAnimation;
    public boolean hasCOCOItem = false;
    private FloorDao mFloorDao;
    private ControlRecord mControlRecord;


    /**
     * @param context
     * @param devices        如果某个position的deviceId为-1，表示这个position为大分割view
     * @param deviceStatuses
     */
    public DevicesAdapter(Activity context, List<Device> devices, List<List<Device>> classificationDevices,
                          List<DeviceStatus> deviceStatuses, View.OnClickListener clickListener, ControlRecord controlRecord) {
        super(context, DeviceSort.sortControlDevices(devices, classificationDevices), deviceStatuses);
        this.mClickListener = clickListener;
        this.classificationDevices = classificationDevices;
        mInflater = LayoutInflater.from(context);
        messagePushDao = new MessagePushDao();
        messageDao = new MessageDao();
        mUserGatewayBindDao = new UserGatewayBindDao();
        userId = UserCache.getCurrentUserId(context);
        mRes = context.getResources();
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "DevicesAdapter()-start");
        }
        realDeviceCount = getRealDeviceCount(mDevices);
        setDeviceStatus(deviceStatuses);
        mRoomDao = new RoomDao();
        deviceStatusDao = new DeviceStatusDao();
        if (isAllRoom) {
            initRooms(devices);
        }
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "DevicesAdapter()-end");
        }
        initHandler(context);
        warningAnimation = AnimationUtils.loadAnimation(mContext, R.anim.security_alarm);
        initDao();
        mControlRecord = controlRecord;
    }

    private void initDao() {
        mFloorDao = new FloorDao();
    }

    private void setDeviceStatus(
            List<DeviceStatus> deviceStatuses) {
        if (deviceStatuses != null && !deviceStatuses.isEmpty()) {
            synchronized (this) {
                for (DeviceStatus deviceStatus : deviceStatuses) {
                    final String deivceId = deviceStatus.getDeviceId();
                    String key = getKey(deviceStatus.getUid(), deivceId);
                    final int value1 = deviceStatus.getValue1();
                    final int deviceType = mDeviceDao.selDeviceType(deivceId);
                    if (deviceType != Constant.INVALID_NUM) {
                        if (deviceType == DeviceType.SCREEN
                                || deviceType == DeviceType.CURTAIN
                                || deviceType == DeviceType.WINDOW_SHADES
                                || deviceType == DeviceType.PUSH_WINDOW
                                || deviceType == DeviceType.CURTAIN_PERCENT
                                || deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
                            if (value1 >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
                                buttonStatus = CURTAIN_ON;
                            } else if (value1 <= DeviceStatusConstant.CURTAIN_STATUS_OFF) {
                                buttonStatus = CURTAIN_OFF;
                            } else {
                                buttonStatus = ON_OFF_NORMAL;
                            }
                        }
                    }
                    mDeviceOnOffStopStatuses.put(key, buttonStatus);
                    DeviceMiniStatus deviceMiniStatus = new DeviceMiniStatus(deviceStatus.getUid(), deivceId, value1, deviceStatus.getOnline(), deviceStatus.getUpdateTime());
                    if (isRefreshDeviceStatus(deviceMiniStatus)) {
                        mDeviceMiniStatusMap.put(key, deviceMiniStatus);
                    }
//                    if (!mDeviceStatuses.containsKey(key)) {
//                        mDeviceStatuses.put(key, value1);
//                    }
                }
            }
        }
    }

    /**
     * @param deviceMiniStatus
     * @return true说明缓存的设备状态比传进来的状态更新，使用缓存的设备状态
     */
    public boolean isRefreshDeviceStatus(DeviceMiniStatus deviceMiniStatus) {
        String key = getKey(deviceMiniStatus.uid, deviceMiniStatus.deviceId);
        if (mDeviceMiniStatusMap.containsKey(key)) {
            DeviceMiniStatus existDeviceMiniStatus = mDeviceMiniStatusMap.get(key);
            if (existDeviceMiniStatus != null && existDeviceMiniStatus.updateTime > deviceMiniStatus.updateTime && existDeviceMiniStatus.updateTime < System.currentTimeMillis()) {
                LogUtil.w(TAG, "isRefreshDeviceStatus()-deviceMiniStatus:" + deviceMiniStatus + ",existDeviceMiniStatus:" + existDeviceMiniStatus);
                return false;
            }
        }
        return true;
    }

    /**
     * 只是把数据传进来，没有立马刷新
     *
     * @param devices
     * @param deviceStatuses
     */
    public void setDataChanged(List<Device> devices,
                               List<DeviceStatus> deviceStatuses) {
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "setDataChanged()-start");
        }
        hasCOCOItem = false;
        userId = UserCache.getCurrentUserId(mContext);
        devices = DeviceSort.sortControlDevices(devices, classificationDevices);
        realDeviceCount = getRealDeviceCount(devices);
        setDeviceStatus(deviceStatuses);
        setData(devices, deviceStatuses);
        notifyDataSetChanged();
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "setDataChanged()-end");
        }
    }

    public void setIsAllRoom(boolean isAllRoom) {
        this.isAllRoom = isAllRoom;
        initRooms(mDevices);
        notifyDataSetChanged();
    }

    private int getRealDeviceCount(List<Device> devices) {
        int realDeviceCount = 0;
        for (Device device : devices) {
            if (!TextUtils.isEmpty(device.getDeviceId())) {
                realDeviceCount++;
            }
        }
        return realDeviceCount;
    }

    /**
     * @param deviceMiniStatusMap key: deviceId
     */
    public void refreshDeviceStatus(ConcurrentHashMap<String, DeviceMiniStatus> deviceMiniStatusMap) {
        synchronized (this) {
            for (Map.Entry<String, DeviceMiniStatus> entry : deviceMiniStatusMap.entrySet()) {
                final String deviceId = entry.getKey();
                DeviceMiniStatus deviceMiniStatus = entry.getValue();
                final String uid = deviceMiniStatus.uid;
                // int value = deviceMiniStatus.value1;
                final String key = getKey(uid, deviceId);
                refreshCurtainStatus(deviceMiniStatus);
                if (isRefreshDeviceStatus(deviceMiniStatus)) {
                    mDeviceMiniStatusMap.put(key, deviceMiniStatus);
                }
                //保存新状态
//                mDeviceStatuses.put(key, value);
                super.refreshOnline(uid, deviceId, OnlineStatus.ONLINE);
                LogUtil.d(TAG, "refreshDeviceStatus()-" + deviceMiniStatus);
            }
        }
        this.notifyDataSetChanged();
    }

//    /**
//     * 设备状态变化
//     *
//     * @param uid
//     * @param deviceId
//     * @param value
//     */
//    public void refreshDeviceStatus(String uid, String deviceId, int value, PayloadData payloadData) {
//        if (Conf.DEBUG_MAIN) {
//            LogUtil.d(Conf.TAG_MAIN, "refreshDeviceStatus()-deviceId:" + deviceId + ",value:" + value + ", start to refresh status");
//        }
//        synchronized (this) {
//            final String key = getKey(uid, deviceId);
//            //保存新状态
//            mDeviceStatuses.put(key, value);
//        }
//        refreshOnline(uid, deviceId, OnlineStatus.ONLINE);
//    }

    public void refreshDeviceStatus(DeviceMiniStatus deviceMiniStatus) {
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "refreshDeviceStatus()-" + deviceMiniStatus);
        }
        String uid = deviceMiniStatus.uid;
        String deviceId = deviceMiniStatus.deviceId;
        final String key = getKey(uid, deviceId);
//        synchronized (this) {
//            //保存新状态
//            mDeviceStatuses.put(key, value);
//        }
        if (isRefreshDeviceStatus(deviceMiniStatus)) {
            mDeviceMiniStatusMap.put(key, deviceMiniStatus);
        }
        refreshOnline(uid, deviceId, OnlineStatus.ONLINE);
    }

    /**
     * 百分比窗帘设备状态变化
     *
     * @param deviceMiniStatus
     */
    public void refreshCurtainStatus(DeviceMiniStatus deviceMiniStatus) {
        String deviceId = deviceMiniStatus.deviceId;
        String uid = deviceMiniStatus.uid;
        int value = deviceMiniStatus.value1;
        Device device = mDeviceDao.selDevice(deviceId);
        if (device == null) {
            LogUtil.e(TAG, "refreshCurtainStatus()-Could not foud device by " + deviceMiniStatus);
            return;
        }
        synchronized (this) {
            if (device.getDeviceType() == DeviceType.CURTAIN_PERCENT || device.getDeviceType() == DeviceType.ROLLER_SHADES_PERCENT) {
                timeout = PERCENT_CURTAIN_TIMEOUT;
            } else {
                timeout = NON_PERCENT_CURTAIN_TIMEOUT;
            }

            final String key = getKey(uid, deviceId);
            //查询有状态的设备是否已经保存
            if (mDeviceMiniStatusMap.containsKey(key)) {
                //根据uid和devicedid得到有状态的设备的状态值
                if (handler.hasMessages(PROPERTY_REPORT_TIMTOUT_WHAT)) {
                    handler.removeMessages(PROPERTY_REPORT_TIMTOUT_WHAT);
                }

                DeviceMiniStatus existDeviceMiniStatus = mDeviceMiniStatusMap.get(key);//status==0?
                int status = existDeviceMiniStatus.value1;//status==0?
                if (Conf.DEBUG_MAIN) {
                    LogUtil.d(Conf.TAG_MAIN, "refreshCurtainStatus()222222-deviceId:" + deviceId + ",value:" + value + ",status:" + status);
                }

                if (value >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
                    buttonStatus = CURTAIN_ON;
                } else if (value <= DeviceStatusConstant.CURTAIN_STATUS_OFF) {
                    buttonStatus = CURTAIN_OFF;
                } else {
                    if (value > status) {
                        buttonStatus = LEFT_STOP;
                    } else if (value < status) {
                        buttonStatus = RIGHT_STOP;
                    }

                    Message msg = handler.obtainMessage();
                    msg.what = PROPERTY_REPORT_TIMTOUT_WHAT;
                    Bundle bundle = new Bundle();
                    bundle.putString("key", key);
                    msg.setData(bundle);
                    handler.sendMessageDelayed(msg, timeout);
                }
                int currentStatus = mDeviceOnOffStopStatuses.containsKey(key) ? mDeviceOnOffStopStatuses.get(key) : ON_OFF_NORMAL;
                if (currentStatus != buttonStatus) {
                    LogUtil.d(Conf.TAG_MAIN, "refreshCurtainStatus()-buttonStatus:" + buttonStatus + ",currentStatus:" + currentStatus);
                    mDeviceOnOffStopStatuses.put(key, buttonStatus);
                    notifyDataSetChanged();
                }
            }
            if (isRefreshDeviceStatus(deviceMiniStatus)) {
                mDeviceMiniStatusMap.put(key, deviceMiniStatus);
            }
            //保存新状态
//            mDeviceMiniStatusMap.put(key, deviceMiniStatus);
        }
    }

    @Override
    public void refreshOnline(String uid, String deviceId, int online) {
        super.refreshOnline(uid, deviceId, online);
        this.notifyDataSetChanged();
    }

    private String getKey(String uid, String deviceId) {
        return uid + "|" + deviceId;
    }

    @Override
    public int getItemViewType(int position) {
//    public int getItemViewType(Device device, int position) {
//        LogUtil.d(TAG, "getItemViewType()-device:" + device);
        int itemType = ARROW_ITEM;
        if (!classificationDevices.isEmpty() && position == 0) {
            itemType = CLASSIFICATION_ITEM;
        } else {
            if (!classificationDevices.isEmpty()) {
                position = position - 1;
            }
            final Device device = mDevices.get(position);
            String model = device.getModel();
            final int deviceType = device.getDeviceType();
            String deviceId = device.getDeviceId();
            if (TextUtils.isEmpty(deviceId)) {
                itemType = EMPTY_ITEM;
            } else if (ProductManage.getInstance().isWifiOnOffDevice(device) && realDeviceCount < 3) {
                itemType = COCO_ITEM;
            } else if (deviceType == DeviceType.VICENTER || device.getDeviceType() == DeviceType.MINIHUB) {
                itemType = ARROW_ITEM;
            } else if (DeviceUtil.isNotSet(deviceType) || ProductManage.getInstance().isWifiOnOffDevice(device)) {
                // 此设备还没有设置过
                final int appDeviceId = device.getAppDeviceId();
                if (deviceType == DeviceType.COCO
                        || ProductManage.getInstance().isWifiOnOffDevice(device)) {
                    itemType = ON_OFF_ITEM;
                } else if (appDeviceId == AppDeviceId.SWITCH
                        || appDeviceId == AppDeviceId.DIMMER_SWITCH
                        || appDeviceId == AppDeviceId.OUTLET
                        || appDeviceId == AppDeviceId.LAMP
                        || appDeviceId == AppDeviceId.RGB
                        || appDeviceId == AppDeviceId.RGB_CONTROLLER
                        || appDeviceId == AppDeviceId.DIMMER
                        || appDeviceId == AppDeviceId.SWITCH_LAMP) {
                    itemType = ON_OFF_ITEM;
                } else if (appDeviceId == AppDeviceId.CURTAIN_CONTROL_BOX
                        || appDeviceId == AppDeviceId.CURTAIN_CONTROLLER) {
                    itemType = ON_OFF_STOP_ITEM;
                }
            } else if (deviceType == DeviceType.DIMMER
                    || deviceType == DeviceType.LAMP
                    || deviceType == DeviceType.RGB
                    || deviceType == DeviceType.OUTLET
                    || deviceType == DeviceType.SWITCH_RELAY
//                    || deviceType == DeviceType.WINDOW_SHADES
//                    || deviceType == DeviceType.WINDOW_SHADES_PERCENT
                    || deviceType == DeviceType.COLOR_TEMPERATURE_LAMP
                    || deviceType == DeviceType.COCO
                    || ProductManage.getInstance().isWifiOnOffDevice(device)
                //|| deviceType == DeviceType.SPRINKLER
                    ) {
                itemType = ON_OFF_ITEM;
            } else if (deviceType == DeviceType.SCREEN
                    || deviceType == DeviceType.CURTAIN
                    || deviceType == DeviceType.WINDOW_SHADES
                    || deviceType == DeviceType.CURTAIN_PERCENT
                    || deviceType == DeviceType.ROLLER_SHADES_PERCENT
                    || deviceType == DeviceType.PUSH_WINDOW
                    || deviceType == DeviceType.ROLLING_GATE
                    || deviceType == DeviceType.ROLLER_SHUTTERS) {
                itemType = ON_OFF_STOP_ITEM;
            } else if (ProductManage.isAlloneSunDevice(device) && !ProductManage.isAlloneLearnDevice(device)) {
                if (deviceType == DeviceType.STB || deviceType == DeviceType.FAN || deviceType == DeviceType.SPEAKER_BOX) {
                    itemType = ALLONE_ITEM;
                } else if (deviceType == DeviceType.AC && !AlloneCache.isSingleAc(deviceId)) {
                    itemType = ALLONE_AC_ITEM;
                }
            }
        }
        return itemType;
    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public int getCount() {
        int count = classificationDevices.isEmpty() ? mDevices.size() : mDevices.size() + 1;
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0 && !classificationDevices.isEmpty()) {
            return classificationDevices;
        }
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // LogUtil.d(TAG, "getView()-position:" + position + " start");
//        LogUtil.d(TAG, "getView()-mDeviceStatuses" + mDeviceStatuses);
        final int itemType = getItemViewType(position);
//        final int itemType = getItemViewType(device, position);
        ClassificationViewHolder classificationViewHolder = null;
        OnOffViewHolder onOffHolder = null;
        OnOffStopViewHolder onOffStopViewHolder = null;
        COCOViewHolder cocoViewHolder = null;
        ArrowViewHolder arrowViewHolder = null;
        EmptyViewHolder emptyViewHolder = null;
        AlloneViewHolder alloneViewHolder = null;
        AlloneAcViewHolder alloneAcViewHolder = null;

        if (convertView == null) {
            if (itemType == CLASSIFICATION_ITEM) {
                classificationViewHolder = new ClassificationViewHolder();
                convertView = mInflater.inflate(R.layout.device_lock_and_sensor, parent, false);
                classificationViewHolder.gridLayout = (GridLayout) convertView.findViewById(R.id.gridLayout);
                convertView.setTag(classificationViewHolder);
            } else if (itemType == COCO_ITEM) {
                cocoViewHolder = new COCOViewHolder();
                convertView = mInflater.inflate(R.layout.device_coco_item, parent, false);
                cocoViewHolder.deviceIcon_iv = (ImageView) convertView.findViewById(R.id.deviceIcon_iv);
                cocoViewHolder.coco_title_layout = (LinearLayout) convertView.findViewById(R.id.title_layout);
                cocoViewHolder.deviceCustomView = (DeviceCustomView) convertView.findViewById(R.id.deviceCustomView);
                cocoViewHolder.model_layout = (TouchImageView) convertView.findViewById(R.id.model_layout);
                cocoViewHolder.time_layout = (TouchImageView) convertView.findViewById(R.id.time_layout);
                cocoViewHolder.countDown_layout = (TouchImageView) convertView.findViewById(R.id.countdown_layout);
                convertView.setTag(cocoViewHolder);
            } else if (itemType == EMPTY_ITEM) {
                emptyViewHolder = new EmptyViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_empty,
                        parent, false);
                convertView.setTag(emptyViewHolder);
            } else if (itemType == ON_OFF_ITEM) {
                onOffHolder = new OnOffViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_on_off,
                        parent, false);
                onOffHolder.ctrl_iv = (ImageView) convertView
                        .findViewById(R.id.ctrl_iv);
                onOffHolder.deviceCustomView = (DeviceCustomView) convertView.findViewById(R.id.deviceCustomView);
                convertView.setTag(onOffHolder);
            } else if (itemType == ON_OFF_STOP_ITEM) {
                onOffStopViewHolder = new OnOffStopViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_on_off_stop,
                        parent, false);
                onOffStopViewHolder.curtain_on = (TextView) convertView
                        .findViewById(R.id.curtain_on);
                onOffStopViewHolder.curtain_stop_left = (TextView) convertView
                        .findViewById(R.id.curtain_stop_left);
                onOffStopViewHolder.curtain_close = (TextView) convertView
                        .findViewById(R.id.curtain_close);
                onOffStopViewHolder.curtain_stop_right = (TextView) convertView
                        .findViewById(R.id.curtain_stop_right);
                onOffStopViewHolder.deviceCustomView = (DeviceCustomView) convertView.findViewById(R.id.deviceCustomView);
                convertView.setTag(onOffStopViewHolder);
            } else if (itemType == ALLONE_ITEM) {
                alloneViewHolder = new AlloneViewHolder();
                alloneViewHolder.alloneItem = new AlloneCommonView(mContext, null);
                convertView = alloneViewHolder.alloneItem;
                convertView.setTag(alloneViewHolder);
            } else if (itemType == ALLONE_AC_ITEM) {
                alloneAcViewHolder = new AlloneAcViewHolder();
                alloneAcViewHolder.alloneAcItem = new AlloneAcView(mContext, null);
                convertView = alloneAcViewHolder.alloneAcItem;
                convertView.setTag(alloneAcViewHolder);
            } else {
                arrowViewHolder = new ArrowViewHolder();
                convertView = mInflater.inflate(R.layout.device_item_arrow,
                        parent, false);
                arrowViewHolder.deviceCustomView = (DeviceCustomView) convertView
                        .findViewById(R.id.deviceCustomView);
                arrowViewHolder.itemView = convertView
                        .findViewById(R.id.itemView);
                convertView.setTag(arrowViewHolder);
            }
        } else {
            if (itemType == CLASSIFICATION_ITEM) {
                classificationViewHolder = (ClassificationViewHolder) convertView.getTag();
            } else if (itemType == COCO_ITEM) {
                cocoViewHolder = (COCOViewHolder) convertView.getTag();
            } else if (itemType == EMPTY_ITEM) {
                emptyViewHolder = (EmptyViewHolder) convertView.getTag();
            } else if (itemType == ON_OFF_ITEM) {
                onOffHolder = (OnOffViewHolder) convertView.getTag();
            } else if (itemType == ON_OFF_STOP_ITEM) {
                onOffStopViewHolder = (OnOffStopViewHolder) convertView.getTag();
            } else if (itemType == ALLONE_ITEM) {
                alloneViewHolder = (AlloneViewHolder) convertView.getTag();
            } else if (itemType == ALLONE_AC_ITEM) {
                alloneAcViewHolder = (AlloneAcViewHolder) convertView.getTag();
            } else {
                arrowViewHolder = (ArrowViewHolder) convertView.getTag();
            }
        }
        if (itemType == CLASSIFICATION_ITEM) {
            if (classificationViewHolder.gridLayout != null)
                classificationViewHolder.gridLayout.removeAllViews();
            int i = 0;
            int size = classificationDevices.size();
            for (List<Device> devices : classificationDevices) {
                setClassificationItem(classificationViewHolder, size, i++, devices);
            }
        } else {
            if (!classificationDevices.isEmpty()) {
                position = position - 1;
            }
            Device device = mDevices.get(position);
            final String uid = device.getUid();
            final String deviceId = device.getDeviceId();
            final String deviceName = device.getDeviceName();
//        final int deviceType = device.getDeviceType();
//        final int deviceIconResId = DeviceTool.getDeviceOnlineIconResId(device
//                .getDeviceType());
            // true在线，false离线
            boolean isOnline = isOnline(device);
            int status = DeviceStatusConstant.OFF;
            final String key = getKey(uid, deviceId);
            synchronized (this) {
                if (mDeviceMiniStatusMap.containsKey(key)) {
                    DeviceMiniStatus deviceMiniStatus = mDeviceMiniStatusMap.get(key);
//                    LogUtil.e(TAG,"device="+device);
//                    LogUtil.e(TAG,"DeviceMiniStatus="+deviceMiniStatus);
                    if (deviceMiniStatus != null) {
                        status = deviceMiniStatus.value1;
                    }
//
//                    if (!isOnline) {
//                        status = DeviceStatusConstant.OFF;
//                    }
                }
            }
            int realStatus = status;//设备当前真实的状态

            //当前控制设备的状态
            if (mControlRecord.hasDeviceAction(deviceId)) {
                status = mControlRecord.getDeviceAction(deviceId);
                LogUtil.d(TAG, "getView()-deviceId:" + deviceId + ",status:" + status);
            }
            Drawable deviceIcon = DeviceTool.getDeviceDrawable(device, isOnline);
            if (itemType == COCO_ITEM) {
                hasCOCOItem = true;
                if (status == DeviceStatusConstant.OFF) {
                    cocoViewHolder.deviceIcon_iv.setImageResource(R.drawable.icon_socket_big_off);
                } else {
                    cocoViewHolder.deviceIcon_iv.setImageResource(R.drawable.icon_socket_big_on);
                }
                cocoViewHolder.deviceIcon_iv.setOnClickListener(mClickListener);
                cocoViewHolder.deviceIcon_iv.setTag(R.id.tag_device, device);
                cocoViewHolder.deviceIcon_iv.setContentDescription(realStatus + "");
                cocoViewHolder.deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
                cocoViewHolder.deviceCustomView.setDeviceInfo(deviceName);
                //modify by wuliquan 2016-07-18
                cocoViewHolder.coco_title_layout.setOnClickListener(mClickListener);
                cocoViewHolder.coco_title_layout.setTag(R.id.tag_device, device);

                cocoViewHolder.model_layout.setOnClickListener(mClickListener);
                cocoViewHolder.model_layout.setTag(R.id.tag_device, device);

                cocoViewHolder.time_layout.setOnClickListener(mClickListener);
                cocoViewHolder.time_layout.setTag(R.id.tag_device, device);

                cocoViewHolder.countDown_layout.setOnClickListener(mClickListener);
                cocoViewHolder.countDown_layout.setTag(R.id.tag_device, device);
            } else if (itemType == ON_OFF_ITEM) {
                onOffHolder.deviceCustomView.setOnClickListener(mClickListener);
                onOffHolder.ctrl_iv.setOnClickListener(mClickListener);

                onOffHolder.deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
                onOffHolder.deviceCustomView.setDeviceInfo(deviceName, (!ProductManage.getInstance().isWifiOnOffDevice(device) && isAllRoom) ? getRoom(uid, deviceId) : null);
                if (device.getAppDeviceId() == AppDeviceId.CURTAIN_CONTROL_BOX || device.getAppDeviceId() == AppDeviceId.CURTAIN_CONTROLLER) {
                    if (status > 1) {
                        onOffHolder.ctrl_iv.setImageResource(R.drawable.device_on);
                    } else {
                        onOffHolder.ctrl_iv.setImageResource(R.drawable.device_off);
                    }
                } else {
                    onOffHolder.ctrl_iv
                            .setImageResource(status == DeviceStatusConstant.ON ? R.drawable.device_on
                                    : R.drawable.device_off);
                }
                onOffHolder.ctrl_iv.setContentDescription(realStatus + "");// 缓存当前设备的状态，控制时直接获取到device状态
                onOffHolder.ctrl_iv.setTag(R.id.tag_device, device);
                onOffHolder.deviceCustomView.setTag(R.id.tag_device, device);
            } else if (itemType == ON_OFF_STOP_ITEM) {
                onOffStopViewHolder.deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
                onOffStopViewHolder.deviceCustomView.setDeviceInfo(deviceName, isAllRoom ? getRoom(uid, deviceId) : null);

                onOffStopViewHolder.curtain_on.setOnClickListener(mClickListener);
                onOffStopViewHolder.curtain_stop_left.setOnClickListener(mClickListener);
                onOffStopViewHolder.curtain_close.setOnClickListener(mClickListener);
                onOffStopViewHolder.curtain_stop_right.setOnClickListener(mClickListener);
                onOffStopViewHolder.deviceCustomView.setOnClickListener(mClickListener);

                synchronized (this) {
                    if (mDeviceMiniStatusMap.containsKey(key)) {
                        DeviceMiniStatus deviceMiniStatus = mDeviceMiniStatusMap.get(key);
                        if (deviceMiniStatus != null) {
                            status = deviceMiniStatus.value1;
                        }
//                        if (!isOnline) {
//                            status = DeviceStatusConstant.OFF;
//                        }
                    } else {
                        DeviceStatus deviceStatus = mDeviceStatusDao.selDeviceStatus(deviceId);
                        if (deviceStatus != null) {
                            status = deviceStatus.getValue1();
                            DeviceMiniStatus deviceMiniStatus = new DeviceMiniStatus(deviceStatus.getUid(), deviceStatus.getDeviceId(), deviceStatus.getValue1(), deviceStatus.getOnline(), deviceStatus.getUpdateTime());
                            mDeviceMiniStatusMap.put(key, deviceMiniStatus);
                        }
                        LogUtil.w(TAG, "getView()-Could not found deviceStatus by " + key + ",deviceStatus:" + deviceStatus);
                    }
                }

                if (mDeviceOnOffStopStatuses.containsKey(key)) {
                    if (mDeviceOnOffStopStatuses.get(key) == CURTAIN_ON) {
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_on.setEnabled(true);
                        } else {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_on.setEnabled(false);
                        }
                        onOffStopViewHolder.curtain_on.setVisibility(View.VISIBLE);
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_close.setEnabled(false);
                        } else {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_close.setEnabled(true);
                        }
                        onOffStopViewHolder.curtain_close.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_stop_left.setVisibility(View.GONE);
                        onOffStopViewHolder.curtain_stop_right.setVisibility(View.GONE);
                    } else if (mDeviceOnOffStopStatuses.get(key) == CURTAIN_OFF) {
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_on.setEnabled(false);
                        } else {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_on.setEnabled(true);
                        }
                        onOffStopViewHolder.curtain_on.setVisibility(View.VISIBLE);
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_close.setEnabled(true);
                        } else {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_close.setEnabled(false);
                        }
                        onOffStopViewHolder.curtain_close.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_stop_left.setVisibility(View.GONE);
                        onOffStopViewHolder.curtain_stop_right.setVisibility(View.GONE);
                    } else if (mDeviceOnOffStopStatuses.get(key) == LEFT_STOP) {
                        onOffStopViewHolder.curtain_on.setVisibility(View.GONE);
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_close.setEnabled(false);
                        } else {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_close.setEnabled(true);
                        }
                        onOffStopViewHolder.curtain_close.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_stop_left.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_stop_right.setVisibility(View.GONE);
                    } else if (mDeviceOnOffStopStatuses.get(key) == RIGHT_STOP) {
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_on.setEnabled(false);
                        } else {
                            onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_on.setEnabled(true);
                        }
                        onOffStopViewHolder.curtain_on.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_close.setVisibility(View.GONE);
                        onOffStopViewHolder.curtain_stop_left.setVisibility(View.GONE);
                        onOffStopViewHolder.curtain_stop_right.setVisibility(View.VISIBLE);
                    } else if (mDeviceOnOffStopStatuses.get(key) == ON_OFF_NORMAL) {
                        onOffStopViewHolder.curtain_on.setBackgroundResource(R.drawable.btn_curtain_normal);
                        onOffStopViewHolder.curtain_on.setTextColor(mContext.getResources().getColor(R.color.green));
                        onOffStopViewHolder.curtain_on.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_on.setEnabled(true);
                        if (device.getDeviceType() == DeviceType.SCREEN) {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_disable);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.gray));
                            onOffStopViewHolder.curtain_close.setEnabled(false);
                        } else {
                            onOffStopViewHolder.curtain_close.setBackgroundResource(R.drawable.btn_curtain_normal);
                            onOffStopViewHolder.curtain_close.setTextColor(mContext.getResources().getColor(R.color.green));
                            onOffStopViewHolder.curtain_close.setEnabled(true);
                        }
                        onOffStopViewHolder.curtain_close.setVisibility(View.VISIBLE);
                        onOffStopViewHolder.curtain_stop_left.setVisibility(View.GONE);
                        onOffStopViewHolder.curtain_stop_right.setVisibility(View.GONE);
                    }
                }

                onOffStopViewHolder.curtain_on.setContentDescription(realStatus + "");
                onOffStopViewHolder.curtain_close.setContentDescription(realStatus + "");
                onOffStopViewHolder.curtain_stop_left.setContentDescription(realStatus + "");
                onOffStopViewHolder.curtain_stop_right.setContentDescription(realStatus + "");
                onOffStopViewHolder.curtain_on.setTag(R.id.tag_device, device);
                onOffStopViewHolder.curtain_stop_left.setTag(R.id.tag_device, device);
                onOffStopViewHolder.curtain_close.setTag(R.id.tag_device, device);
                onOffStopViewHolder.curtain_stop_right.setTag(R.id.tag_device, device);
                onOffStopViewHolder.deviceCustomView.setTag(R.id.tag_device, device);
            } else if (itemType == ALLONE_ITEM) {
                alloneViewHolder.alloneItem.setData(device, mContext.getFragmentManager(),isOnline);
                DeviceCustomView deviceCustomView = alloneViewHolder.alloneItem.getDeviceCustomView();
                deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
                deviceCustomView.setDeviceInfo(deviceName, null);
                deviceCustomView.setTag(R.id.tag_device, device);
                deviceCustomView.setOnClickListener(mClickListener);
            } else if (itemType == ALLONE_AC_ITEM) {
                alloneAcViewHolder.alloneAcItem.setData(device, mContext.getFragmentManager(),isOnline);
                DeviceCustomView deviceCustomView = alloneAcViewHolder.alloneAcItem.getDeviceCustomView();
                deviceCustomView.setIsOnLine(isOnline, deviceIcon, null);
                deviceCustomView.setDeviceInfo(deviceName, null);
                deviceCustomView.setTag(R.id.tag_device, device);
                deviceCustomView.setOnClickListener(mClickListener);
            } else if (itemType == ARROW_ITEM) {
                arrowViewHolder.deviceCustomView.setIsOnLine(isOnline, deviceIcon,
                        !isOnline && DeviceTool.isSleep(device.getDeviceType()) ? parent.getContext().getString(R.string.sleep) : null);
                arrowViewHolder.deviceCustomView.setDeviceInfo(deviceName, !ProductManage.isAllonDevice(device) && isAllRoom ? getRoom(uid, deviceId) : null);
                arrowViewHolder.deviceCustomView.setOnClickListener(mClickListener);
                arrowViewHolder.itemView.setOnClickListener(mClickListener);
                convertView.setTag(R.id.tag_device, device);
                arrowViewHolder.deviceCustomView.setTag(R.id.tag_device, device);
            }

            if (itemType == EMPTY_ITEM) {
//            convertView.setBackgroundResource(R.drawable.tran);
            } else {
//            convertView.setBackgroundResource(R.color.blue);
//            convertView.setBackground(mRes.getDrawable(R.drawable.item_selector));
//            convertView.setBackgroundDrawable(mRes.getDrawable(R.drawable.item_selector));
            }
            convertView.setTag(R.id.tag_device, device);
        }
        return convertView;
    }

    private boolean setClassificationItem(ClassificationViewHolder classificationViewHolder, int size, int i, List<Device> devices) {
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        RelativeLayout item = (RelativeLayout) View.inflate(mContext, R.layout.device_lock_and_sensor_item, null);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        if (i == size - 1 && size % 2 == 1) {
            params.columnSpec = GridLayout.spec(i % 2, 2);
            item.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            params.columnSpec = GridLayout.spec(i % 2, 0.5f);
            params.width = mContext.getResources().getDisplayMetrics().widthPixels / 2 - 1;
            if (i % 2 == 0) {
                params.rightMargin = 1;
            } else {
                params.leftMargin = 1;
            }
            if (i / 2 != (size - 1) / 2) {
                params.bottomMargin = 2;
            }
        }
        params.setGravity(Gravity.FILL);
        item.setLayoutParams(params);
        item.setBackgroundResource(R.color.white);
        classificationViewHolder.gridLayout.addView(item);
        ImageView warning = (ImageView) item.findViewById(R.id.warningImageView);
        warning.setVisibility(View.GONE);
        warning.clearAnimation();
        ImageView deviceIcon_iv = (ImageView) item.findViewById(R.id.deviceIcon_iv);
        TextView deviceName_tv = (TextView) item.findViewById(R.id.deviceName_tv);
        deviceName_tv.setTextColor(mContext.getResources().getColor(R.color.black));
        MyCountdownTextView deviceStatus_tv = (MyCountdownTextView) item.findViewById(R.id.deviceStatus_tv);
        deviceStatus_tv.registerCountdownFinishedListener(this);
        deviceStatus_tv.setTextColor(mContext.getResources().getColor(R.color.gray));
        Device device = devices.get(0);
        int deviceType = device.getDeviceType();
        int deviceIconResId = DeviceTool.getClassificationIconByType(deviceType, DeviceStatusConstant.NOT_ALARM);
        String deviceName;
        if (devices.size() == 1) {
            deviceName = device.getDeviceName();
        } else {
            deviceName = mContext.getString(DeviceTool.getDeviceTypeNameResId(deviceType));
        }
        String record = "";
        if (ProductManage.getInstance().canWarning(device)) {
            boolean hasWarning = false;
            long countDownTime = 0;
            for (Device d : devices) {
                DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(d.getDeviceId());
                if (deviceStatus != null) {
                    long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), d.getDeviceId());
                  //  updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                    long sysTime = System.currentTimeMillis();
                    countDownTime = 3 * 60 * 1000L + updateTime - sysTime;
                    if (countDownTime > 3 * 60 * 1000L) {
                        countDownTime = 3 * 60 * 1000L;
                    }
                    if (deviceStatus.getOnline() == OnlineStatus.ONLINE && deviceStatus.getValue1() == DeviceStatusConstant.ALARM && (deviceType != DeviceType.SOS_SENSOR || countDownTime > 1000)) {
                        LogUtil.d(TAG, "countDownTime:" + countDownTime);
                        hasWarning = true;
                        device = d;
                        break;
                    }
                }
            }
            if (hasWarning) {
                item.setBackgroundResource(R.color.warning_red);
                warning.setVisibility(View.VISIBLE);
                warning.startAnimation(warningAnimation);
                deviceName_tv.setTextColor(mContext.getResources().getColor(R.color.white));
                deviceStatus_tv.setTextColor(mContext.getResources().getColor(R.color.white));
                deviceIconResId = DeviceTool.getClassificationIconByType(deviceType, DeviceStatusConstant.ALARM);
                deviceName = device.getDeviceName();
                if (deviceType == DeviceType.WATER_SENSOR) {
                    record = mContext.getString(R.string.sensor_leakage);
                } else {
                    record = mContext.getString(R.string.sensor_alarming);
                }
                if (deviceType == DeviceType.SOS_SENSOR && countDownTime > 1000) {
                    deviceStatus_tv.startCountdown(record + " %s", (int) (countDownTime / 1000));
                }
            } else {
                record = mContext.getString(R.string.sensor_normal);
            }
        } else if (hasSmartLock(devices)) {
            DoorLockRecordData doorLockRecordData = DoorLockRecordDao.getInstance().getRecentRecord(devices);
            doorLockRecordDataRecent = getDoorLockRecordAndDeviceName(doorLockRecordData);
            if (!TextUtils.isEmpty(doorLockRecordDataRecent[1])) {
                deviceName = doorLockRecordDataRecent[1];
                record = doorLockRecordDataRecent[0];
            }
        } else {
            DeviceStatus deviceStatus;
            if (devices.size() == 1) {
                deviceStatus = deviceStatusDao.selDeviceStatus(device.getUid(), device.getDeviceId());
            } else {
                deviceStatus = deviceStatusDao.selLatestDeviceStatuse(UserCache.getCurrentMainUid(mContext), deviceType);
            }
            if (deviceStatus != null) {
                device = mDeviceDao.selDevice(deviceStatus.getDeviceId());
                if (device != null) {
                    deviceName = device.getDeviceName();
                    int status = deviceStatus.getValue1();
                    if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
                        if (!CommonCache.isCelsius()) {
                            record = mContext.getString(R.string.sensor_temperature) + MathUtil.geFahrenheitData(MathUtil.getRoundData(deviceStatus.getValue1())) + CommonCache.getTemperatureUnit();
                        } else {
                            record = mContext.getString(R.string.sensor_temperature) + MathUtil.getRoundData(deviceStatus.getValue1()) + CommonCache.getTemperatureUnit();
                        }
                    } else if (deviceType == DeviceType.HUMIDITY_SENSOR) {
                        record = mContext.getString(R.string.sensor_humidity) + MathUtil.getRoundData(deviceStatus.getValue2()) + "%";
                    } else if (deviceType == DeviceType.INFRARED_SENSOR) {
                        if (status == DeviceStatusConstant.OFF) {
                            long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                            updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                            String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
                            record = time + " " + mContext.getString(R.string.infrared_sensor_alarm);
                        }
                    } else if (DeviceUtil.isMagnetic(deviceType)) {
                        long updateTime = DeviceCache.getDeviceStatusReportTime(UserCache.getCurrentMainUid(mContext), device.getDeviceId());
                        updateTime = Math.max(deviceStatus.getUpdateTime(), updateTime);
                        String time = TimeUtil.secondToDateString((int) (updateTime / 1000));
                        if (status == DeviceStatusConstant.OFF) {
                            record = time + " " + mContext.getString(R.string.magnetic_on);
                        } else {
                            record = time + " " + mContext.getString(R.string.magnetic_off);
                        }
                    } else {
                        if (status == DeviceStatusConstant.OFF) {
                            record = mContext.getString(R.string.magnetic_off);
                        } else {
                            record = mContext.getString(R.string.magnetic_on);
                        }
                    }
                }
            }
        }
        deviceIcon_iv.setImageResource(deviceIconResId);
        deviceName_tv.setText(deviceName);
        if (TextUtils.isEmpty(record)) {
            deviceStatus_tv.setVisibility(View.GONE);
        } else {
            deviceStatus_tv.setVisibility(View.VISIBLE);
            deviceStatus_tv.setText(record);
        }
        item.setOnClickListener(mClickListener);
        item.setTag(devices);
        return true;
    }

    private boolean hasSmartLock(List<Device> devices) {
        for (Device device : devices) {
            if (ProductManage.isSmartLock(device)) {
                return true;
            }
        }
        return false;
    }

    private String[] getDoorLockRecordAndDeviceName(DoorLockRecordData doorLockRecordData) {
        String record = "";
        String deviceName = "";
        if (doorLockRecordData != null) {
            Device device = mDeviceDao.selDevice(doorLockRecordData.getDeviceId());
            if (device != null) {
                deviceName = device.getDeviceName();
            }
            String time = TimeUtil.secondToDateString((int) (doorLockRecordData.getCreateTime() / 1000));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(time).append(" ");
            DoorUserData doorUserData = doorLockRecordData.getDoorUser();
            //门锁开锁显示钥匙开门
            if (doorLockRecordData.getType() == 5) {
                stringBuilder.append(LockUtil.getLockType(doorLockRecordData.getType())).append(" ").append(mContext.getString(R.string.action_unlock_door));
            } else if (doorUserData != null)
                stringBuilder.append(TextUtils.isEmpty(doorUserData.getName()) ? mContext.getString(R.string.user) + " " + doorUserData.getAuthorizedId() : doorUserData.getName()).append(" ").append(mContext.getString(R.string.action_unlock_door));
            else if (doorLockRecordData.getType() >= 6)
                stringBuilder.append(LockUtil.getLockType(doorLockRecordData.getType()));
            else
                stringBuilder.append(mContext.getString(R.string.lock_type_expired)).append(" ").append(mContext.getString(R.string.action_unlock_door));
            record = stringBuilder.toString();
        }
        return new String[]{record, deviceName};
    }

    /**
     * 判断设备是否在线
     *
     * @param device
     * @return true设备在线，默认设备在线。
     */
    private boolean isOnline(Device device) {
        boolean isOnline = true;
        final String deviceId = device.getDeviceId();
        boolean isWifiDevice = ProductManage.getInstance().isWifiDevice(device);
        synchronized (this) {
            try {
                if (isWifiDevice) {
                    //新添加背景音乐在线离线处理逻辑
                    if(device.getDeviceType()==DeviceType.BACK_MUSIC){
                        isOnline = GatewayOnlineCache.isOnline(mContext,device.getUid());
                    }else {
                        isOnline = isOnline(deviceId);
                    }
                } else if (ProductManage.getInstance().isVicenter300ByDeviceType(device.getDeviceType())) {
                    String currentMainUid = UserCache.getCurrentMainUid(ViHomeApplication.getAppContext());
                    isOnline = GatewayOnlineCache.isOnline(mContext, currentMainUid);
                } else if (ProductManage.getInstance().isCamera530(device)) {
                    //TODO 大拿摄像头也应该判断
                    isOnline = true;
                } else if (!deviceId.equals(Constant.NULL_DATA)) {
                    isOnline = isOnline(deviceId);
                }
            } catch (NullPointerException e) {
                //e.printStackTrace();
                // LogUtil.e(TAG, "getView()-mDeviceOOs:" + mDeviceOOs);
            }
        }
        return isOnline;
    }

    @Override
    public void onSetLatestType(String deviceId, int tabViewType) {
        latestTypes.put(deviceId, tabViewType);
    }

    @Override
    public void onCountdownFinished() {
        notifyDataSetChanged();
    }

    private class OnOffViewHolder {
        private DeviceCustomView deviceCustomView;
        private ImageView ctrl_iv;
    }

    private class COCOViewHolder {
        private TouchImageView model_layout, time_layout, countDown_layout;
        private LinearLayout coco_title_layout;
        private DeviceCustomView deviceCustomView;
        private ImageView deviceIcon_iv;
    }

    private class OnOffStopViewHolder implements Serializable {
        private TextView curtain_on;
        private TextView curtain_stop_left;
        private TextView curtain_close;
        private TextView curtain_stop_right;
        private DeviceCustomView deviceCustomView;
    }

    private class ClassificationViewHolder {
        private GridLayout gridLayout;
    }

    private class ArrowViewHolder {
        private DeviceCustomView deviceCustomView;
        private View itemView;
    }


    private class EmptyViewHolder {
    }

    static class AlloneViewHolder {
        private AlloneCommonView alloneItem;
    }

    static class AlloneAcViewHolder {
        private AlloneAcView alloneAcItem;
    }

    private void initHandler(Context context) {
        handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case PROPERTY_REPORT_TIMTOUT_WHAT:
                        Bundle bundle = msg.getData();
                        String key = bundle.getString("key");
                        mDeviceOnOffStopStatuses.put(key, ON_OFF_NORMAL);
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initRooms(List<Device> devices) {
        mFloorNameAndRoomNames = mRoomDao.getRoomNameAndFloorNames(devices);
        LogUtil.d(TAG, "initRooms()-mFloorNameAndRoomNames:" + mFloorNameAndRoomNames);
    }

    /**
     * 没有楼层和房间时不需要显示：未设定区域和房间
     *
     * @param uid
     * @param deviceId
     * @return
     */
    private String getRoom(String uid, String deviceId) {
        final String key = uid + "_" + deviceId;
        String name = null;
        List<Floor> mFloors = mFloorDao.selAllFloors(uid);
        List<Room> rooms = mRoomDao.selAllRooms(uid);
        if (!(mFloors.isEmpty() && rooms.isEmpty())) {
            name = ViHomeProApp.getContext().getString(R.string.device_manage_room_not_set);
        }
        if (mFloorNameAndRoomNames.containsKey(key)) {
            name = mFloorNameAndRoomNames.get(key);
        }
        return name;
    }


}
