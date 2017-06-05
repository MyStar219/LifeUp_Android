package com.orvibo.homemate.device.selectdevice;

import android.content.Context;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.sharedPreferences.UserCache;

import java.util.List;

/**
 * Created by huangqiyao on 2016/7/16 12:14.
 * 选择小方设备
 *
 * @version v1.10
 */
public class SelectAllone2 {
    private Context mContext;
    private OnSelectDeviceListener mOnSelectDeviceListener;
    //    private SelectAllone2DevicesPopup mSelectAllone2DevicesPopup;
    private Device mSelectedDevice;
    private DeviceDao mDeviceDao;

    public SelectAllone2(Context context) {
        mContext = context;
        mDeviceDao = new DeviceDao();
    }


    /**
     * 初始化，设置初始化值
     *
     * @return
     */
    public List<Device> init() {

//        if (mSelectAllone2DevicesPopup == null) {
//            mSelectAllone2DevicesPopup = new SelectAllone2DevicesPopup(mContext) {
//                @Override
//                protected void onSelectedDevice(Device device) {
//                    super.onSelectedDevice(device);
//                    callback(device);
//                }
//            };
//        }
//        ImageView iv = (ImageView) topView.findViewById(R.id.arrow_iv);
//        mSelectAllone2DevicesPopup.setView(topView, iv);
        String userId = UserCache.getCurrentUserId(mContext);
        List<Device> devices = mDeviceDao.selWifiDevices(userId, DeviceType.ALLONE);
        if (devices.isEmpty()) {
            mSelectedDevice = null;
            callbackEmpty();
        } else if (mSelectedDevice == null) {
            //设置默认选择的设备
            mSelectedDevice = devices.get(0);
            callback(mSelectedDevice);
        }
        return devices;
    }

    /**
     * 显示选择小方设备列表
     */
//    public void showSelectDeviceView() {
//        String userId = UserCache.getCurrentUserId(mContext);
//        List<Device> devices = mDeviceDao.selWifiDevices(userId, DeviceType.ALLONE);
//        if (devices.isEmpty()) {
//            mSelectedDevice = null;
//            callbackEmpty();
//        } else if (mSelectedDevice == null) {
//            //设置默认选择的设备
//            mSelectedDevice = devices.get(0);
//            callback(mSelectedDevice);
//        }
//        mSelectAllone2DevicesPopup.show(devices, mSelectedDevice);
//    }

//    public boolean isShowing() {
//        return mSelectAllone2DevicesPopup != null && mSelectAllone2DevicesPopup.isShowing();
//    }
//
//    public void cancelAnim() {
//        if (mSelectAllone2DevicesPopup != null) {
//            mSelectAllone2DevicesPopup.dismissAfterAnim();
//        }
//    }

    public void setOnSelectDeviceListener(OnSelectDeviceListener listener) {
        mOnSelectDeviceListener = listener;
    }

    private void callbackEmpty() {
        if (mOnSelectDeviceListener != null) {
            mOnSelectDeviceListener.onEmptyDevice();
        }
    }

    private void callback(Device device) {
        if (mOnSelectDeviceListener != null) {
            mOnSelectDeviceListener.onSelectDevice(device);
        }
    }

    public interface OnSelectDeviceListener {
        /**
         * 选择的设备
         *
         * @param device
         */
        void onSelectDevice(Device device);

        /**
         * 没有设备
         */
        void onEmptyDevice();
    }
}
