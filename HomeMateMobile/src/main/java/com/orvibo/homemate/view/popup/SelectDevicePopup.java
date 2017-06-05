package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.device.bind.adapter.SelectDeviceAdapter;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.PopupWindowUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能场景选择安防设备
 *
 * @author smagret
 * @date 2015/10/16
 */
public abstract class SelectDevicePopup implements AdapterView.OnItemClickListener {
    private PopupWindow popup;
    private DeviceDao mDeviceDao;
    private SelectDeviceAdapter selectDeviceAdapter;
    private List<Device> devices;
    private List<Device> tempDevices;

    public SelectDevicePopup() {
        mDeviceDao = new DeviceDao();
    }

    /**
     * @param context
     * @param uid
     * @param deviceType
     */
    public void showPopup(Activity context, String uid, int deviceType, String deviceId, String linkageId) {
        dismiss();
        View view = LayoutInflater.from(context).inflate(R.layout.select_device,
                null);

        if (devices == null) {
            devices = new ArrayList<Device>();
        } else {
            devices.clear();
        }
        if (deviceType == DeviceType.FLAMMABLE_GAS) {
            tempDevices = mDeviceDao.selDevicesByDeviceType(uid, DeviceType.MAGNETIC);
            devices.addAll(tempDevices);
            tempDevices = mDeviceDao.selDevicesByDeviceType(uid, DeviceType.MAGNETIC_WINDOW);
            devices.addAll(tempDevices);
            tempDevices = mDeviceDao.selDevicesByDeviceType(uid, DeviceType.MAGNETIC_DRAWER);
            devices.addAll(tempDevices);
            tempDevices = mDeviceDao.selDevicesByDeviceType(uid, DeviceType.MAGNETIC_OTHER);
            devices.addAll(tempDevices);
        } else if (deviceType == DeviceType.ALLONE) {
            //小方
            //新添加的联动，去掉已经在联动条件中选择的小方
            //编辑联动，去掉已经在联动条件中选择的小方（本联动的还是显示，但是是选中状态）
            devices = mDeviceDao.selUnBindAlloneDeviceCount(UserCache.getCurrentUserId(context), linkageId);
        } else {
            devices = mDeviceDao.selDevicesByDeviceType(uid, deviceType);
        }
        //只有一个设备时默认选中并返回
        if (devices.size() == 1) {
            itemClicked(devices.get(0));
        } else {
            selectDeviceAdapter = new SelectDeviceAdapter(context, devices, deviceId);
            ListView device_lv = (ListView) view.findViewById(R.id.device_lv);
            device_lv.setOnItemClickListener(this);
            device_lv.setAdapter(selectDeviceAdapter);

            popup = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            PopupWindowUtil.initPopup(popup,
                    context.getResources().getDrawable(R.drawable.gray_ligth), 1);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popup.isShowing()) {
                        popup.dismiss();
                    }
                }
            });
        }
    }

    public void dismiss() {
        PopupWindowUtil.disPopup(popup);
    }

    public boolean isShowing() {
        return popup != null && popup.isShowing();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = (Device) view.getTag(R.id.tag_device);
        itemClicked(device);
        dismiss();
    }

    public abstract void itemClicked(Device device);
}
