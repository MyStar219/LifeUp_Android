package com.orvibo.homemate.device;

import android.app.Activity;
import android.widget.BaseAdapter;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Conf;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangqiyao on 2015/11/30.
 */
public abstract class BaseDevicesAdapter extends BaseAdapter {
    private final String TAG = "BaseDevicesAdapter";
    private static final String LOCK = "DeviceLock";
    protected Activity mContext;
    protected List<Device> mDevices;
    /**
     * 设备在线和离线状态。key:deviceId or uid,value:true online,false offline
     */
    private Map<String, Boolean> mDeviceOOs = new ConcurrentHashMap<String, Boolean>();
    protected DeviceDao mDeviceDao;
    protected DeviceStatusDao mDeviceStatusDao;

    public BaseDevicesAdapter(Activity context, List<Device> devices, List<DeviceStatus> deviceStatuses) {
        mContext = context;
        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        setDeviceOOs(devices, deviceStatuses);
    }

    /**
     * @param devices        已经排序了的device
     * @param deviceStatuses
     */
    protected void setData(List<Device> devices, List<DeviceStatus> deviceStatuses) {
        setDeviceOOs(devices, deviceStatuses);
    }

    private void setDeviceOOs(List<Device> devices, List<DeviceStatus> deviceStatuses) {
        synchronized (LOCK) {
            mDevices = devices;
            mDeviceOOs = DeviceUtil.getDeviceOOs(devices, deviceStatuses);
        }
    }

    /**
     * 用于刷新设备集中设备的在线状态
     *
     * @param uid
     * @param deviceId
     * @param online   Please see {@link OnlineStatus}
     */
    public void refreshOnline(String uid, String deviceId, int online) {
        if (deviceId == null) {
            return;
        }
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "refreshOnline()-deviceId:" + deviceId + ",online:" + online + ", start to refresh status");
        }
        Device device1 = mDeviceDao.selDevice(deviceId);
        synchronized (LOCK) {
            final boolean isOnline = online == OnlineStatus.ONLINE;
            //1.如果设备Id不为空，首先记录该设备的在线状态
            mDeviceOOs.put(deviceId, isOnline);
            if (mDevices != null && !mDevices.isEmpty()) {
                //设备的MAC地址
                String extAddr = null;
                //获得deviceId对应设备的Mac地址
                for (Device device : mDevices) {
                    if (deviceId != null && deviceId.equals(device.getDeviceId())) {
                        extAddr = device.getExtAddr();
                        break;
                    }
                }
                //非红外设备:
                if (!StringUtil.isEmpty(extAddr)) {
                    //相同extAddr设备设置离线/在线
                    for (Device device : mDevices) {
                        if (extAddr.equals(device.getExtAddr())) {
                            mDeviceOOs.put(device.getDeviceId(), isOnline);
                        }
                    }
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "refreshOnline()-2");
                    }
                    // allone子设备
                } else if (device1 != null && device1.getDeviceType() == DeviceType.ALLONE) {
                    List<Device> devices = mDeviceDao.selAlloneChildDeviceByUid(device1.getUid());
                    if (devices != null && !devices.isEmpty()) {
                        for (Device device : devices) {
                            mDeviceOOs.put(device.getDeviceId(), isOnline);
                        }
                    }
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "refreshOnline()-3");
                    }
                } else {
                    //我的设备不显示红外转发器，当有离线在线上报时在mDevices里面是查找不到该设备，即extAddr为null
                    //如果是红外转发器离线/在线，则对应的虚拟红外设备也设置为离线/在线
                    List<Device> devices = mDeviceDao.selIRDeviceByDeviceId(uid, deviceId);
                    if (devices != null && !devices.isEmpty()) {
                        for (Device device : devices) {
                            mDeviceOOs.put(device.getDeviceId(), isOnline);
                        }
                    }
                    if (Conf.DEBUG_MAIN) {
                        LogUtil.d(Conf.TAG_MAIN, "refreshOnline()-3");
                    }
                }
            }
        }
        if (Conf.DEBUG_MAIN) {
            LogUtil.d(Conf.TAG_MAIN, "refreshOnline()-deviceId:" + deviceId + ",online:" + online + ", finish to refresh status");
        }
        //LogUtil.d(TAG, "refreshOnline()-uid:" + uid + ",deviceId:" + deviceId + ",online:" + online);
        //LogUtil.d(TAG, "refreshOnline()-mDeviceOOs:" + mDeviceOOs);
    }

    /**
     * 判断设备是否在线。
     *
     * @param deviceId
     * @return true 设备在线。默认设置为在线状态
     */
    public boolean isOnline(String deviceId) {
        boolean onLine = true;
        if (!StringUtil.isEmpty(deviceId)) {
            synchronized (LOCK) {
                Boolean temp = mDeviceOOs.get(deviceId);
                if (temp != null) {
                    onLine = temp;
                }
            }
        }
//        LogUtil.d(TAG, "isOnline()-" + deviceId + " isOnline:" + onLine);
        return onLine;
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices == null ? null : mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
