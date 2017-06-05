package com.orvibo.homemate.device.energyremind;

import android.content.Context;

import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.SaveReminderFlag;
import com.orvibo.homemate.event.EnergyRemindEvent;
import com.orvibo.homemate.sharedPreferences.EnergyReminderCache;
import com.orvibo.homemate.sharedPreferences.UserCache;

import org.apache.mina.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2015/12/28.
 */
public class EnergyRemindManager {
    private static EnergyRemindManager mEnergyRemindManager;
    private Context mAppContext;
    private Set<OnEnergyRemindListener> onEnergyRemindListeners = new ConcurrentHashSet<OnEnergyRemindListener>();
    private DeviceDao mDeviceDao;
    private DeviceStatusDao mDeviceStatusDao;
    private List<Device> mDevices = new ArrayList<>();
    private List<Device> energyRemindDevices = new ArrayList<>();

    private EnergyRemindManager(Context context) {
        mAppContext = ViHomeApplication.getAppContext();
        mDeviceDao = new DeviceDao();
        mDeviceStatusDao = new DeviceStatusDao();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public static EnergyRemindManager getInstance(Context context) {
        if (mEnergyRemindManager == null) {
            init(context);
        }
        return mEnergyRemindManager;
    }

    private synchronized static void init(Context context) {
        if (mEnergyRemindManager == null) {
            mEnergyRemindManager = new EnergyRemindManager(context);
        }
    }

    public void registerEnergyRemindListener(OnEnergyRemindListener onEnergyRemindListener) {
        if (!onEnergyRemindListeners.contains(onEnergyRemindListener)) {
            onEnergyRemindListeners.add(onEnergyRemindListener);
        }
    }

    public void unregisterEnergyRemindListener(OnEnergyRemindListener onEnergyRemindListener) {
        if (onEnergyRemindListener != null) {
            onEnergyRemindListeners.remove(onEnergyRemindListener);
        }
    }

    public final void onEventMainThread(EnergyRemindEvent event) {
        synchronized (this) {
            Iterator<OnEnergyRemindListener> iterator = onEnergyRemindListeners.iterator();
            while (iterator.hasNext()) {
                OnEnergyRemindListener listener = iterator.next();
                if (listener != null) {
                    getEnergyRemindDevices();
                    listener.onEnergyRemind(energyRemindDevices);
                }
            }
        }
    }

    private List<Device> getEnergyRemindDevices() {
        mDevices.clear();
        energyRemindDevices.clear();
        mDevices = (ArrayList) mDeviceDao.selNeedCloseZigbeeLampsDevices(UserCache.getCurrentMainUid(mAppContext));
        for (Device device : mDevices) {
            String uid = device.getUid();
            String deviceId = device.getDeviceId();
            DeviceStatus deviceStatus;
            deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
            if (deviceStatus != null && EnergyReminderCache.getEnergyReminder(mAppContext,UserCache.getCurrentMainUid(mAppContext)) == SaveReminderFlag.ON) {
                if (deviceStatus.getValue1() == DeviceStatusConstant.ON && deviceStatus.getOnline() == OnlineStatus.ONLINE) {
                        energyRemindDevices.add(device);
                }
            }
        }
        return energyRemindDevices;
    }

    public void unRegister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        clearListener(onEnergyRemindListeners);
    }

    private void clearListener(Set<?> listeners) {
        if (listeners != null) {
            listeners.clear();
        }
    }

    public interface OnEnergyRemindListener {
        void onEnergyRemind(List<Device> energyRemindDevices);
    }

}
