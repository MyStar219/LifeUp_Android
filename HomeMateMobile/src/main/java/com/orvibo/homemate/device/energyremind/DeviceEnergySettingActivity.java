package com.orvibo.homemate.device.energyremind;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.SaveReminderFlag;
import com.orvibo.homemate.sharedPreferences.EnergyReminderCache;

import java.util.ArrayList;

public class DeviceEnergySettingActivity extends BaseActivity {

    private ListView devicesEnergySetListView;
    private DeviceEnergySettingAdapter deviceEnergyAdapter;
    private DeviceDao deviceDao;
    private ArrayList<Device> devices;
    private ArrayList<Device> tempDevices =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_setting);
        
        init();
        refresh();
    }

    private void init() {
        deviceDao = new DeviceDao();
        devicesEnergySetListView = (ListView) findViewById(R.id.devicesEnergySetListView);
    }

    private void refresh() {
        devices = (ArrayList) deviceDao.selZigbeeLampsDevices(currentMainUid);
        tempDevices.addAll(devices);
        if (EnergyReminderCache.getEnergyReminder(mAppContext,currentMainUid) == SaveReminderFlag.OFF) {
            devices.clear();
        }
        deviceEnergyAdapter = new DeviceEnergySettingAdapter(mAppContext,this,devices);
        devicesEnergySetListView.setAdapter(deviceEnergyAdapter);
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag == null) {
            if (EnergyReminderCache.getEnergyReminder(mAppContext,currentMainUid) == SaveReminderFlag.OFF) {
                devices.clear();
                devices.addAll(tempDevices);
                EnergyReminderCache.setEnergyReminder(mAppContext,currentMainUid,SaveReminderFlag.ON);
            } else {
                devices.clear();
                EnergyReminderCache.setEnergyReminder(mAppContext,currentMainUid, SaveReminderFlag.OFF);
            }
        } else {
            Device mDevice = (Device) tag;
            int i = -1;
            for (Device device : devices) {
                i++;
                if (device.getDeviceId().equals(mDevice.getDeviceId())) {
                    break;
                }
            }
            devices.remove(i);
            if (mDevice.getSaveReminderFalg() == SaveReminderFlag.OFF) {
                mDevice.setSaveReminderFalg(SaveReminderFlag.ON);
            } else {
                mDevice.setSaveReminderFalg(SaveReminderFlag.OFF);
            }
            devices.add(i, mDevice);
            tempDevices.clear();
            tempDevices.addAll(devices);
        }
        deviceEnergyAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deviceDao.updateDevices(mAppContext, devices);
    }

}
