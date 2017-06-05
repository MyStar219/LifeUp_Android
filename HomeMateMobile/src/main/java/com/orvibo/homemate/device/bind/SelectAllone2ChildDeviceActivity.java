package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.adapter.SelectAlloneChildDevicesAdapter;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangqiyao on 2016/7/16 13:49.
 * 选择小方红外设备
 *
 * @version v1.10
 */
public class SelectAllone2ChildDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        OOReport.OnDeviceOOReportListener {
    private static final String TAG = SelectAllone2ChildDeviceActivity.class.getSimpleName();
    private SelectAlloneChildDevicesAdapter mSelectAlloneChildDevicesAdapter;

//    private SelectAlloneChildDeviceAdapter mSelectAlloneChildDeviceAdapter;

    private ListView mDevices_lv;

    private DeviceDao mDeviceDao;
    /**
     * 已经选择的设备
     */
    private ArrayList<Device> checkedDevices = new ArrayList<>();

//    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_allone_child_devices);
        checkedDevices = (ArrayList<Device>) getIntent().getSerializableExtra(IntentKey.DEVICES);
        if (checkedDevices == null) {
            checkedDevices = new ArrayList<>();
        }
        mDeviceDao = new DeviceDao();
        mDevices_lv = (ListView) findViewById(R.id.devices_lv);
        mDevices_lv.setOnItemClickListener(this);
        LinearLayout deviceEmptyLinearLayout = (LinearLayout) findViewById(R.id.deviceEmptyLinearLayout);
        mDevices_lv.setEmptyView(deviceEmptyLinearLayout);

//        mRecyclerView = (RecyclerView) findViewById(R.id.rv_alloneChildDevice);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        // 设置布局管理器
//        mRecyclerView.setLayoutManager(layoutManager);
        refresh();

    }

    private void refresh() {
        String userId = UserCache.getCurrentUserId(mAppContext);
//        String uid = selectAlloneDevice.getUid();
//        List<Device> alloneDevices = mDeviceDao.selWifiDevices(userId, DeviceType.ALLONE);
        List<Device> alloneDevices = mDeviceDao.selWifiDevicesWithOutShared(userId, DeviceType.ALLONE);
        List<Device> devices = new ArrayList<>();
        if (!alloneDevices.isEmpty()) {
            for (Device device : alloneDevices) {
                List<Device> tempDevices = mDeviceDao.selAlloneDevice(device);
                if (tempDevices.size() > 0) {
                    devices.add(device);
                    devices.addAll(tempDevices);
                }
            }
        }

//        if (mSelectAlloneChildDeviceAdapter == null) {
//            mSelectAlloneChildDeviceAdapter = new SelectAlloneChildDeviceAdapter(this, devices, checkedDevices);
//            mRecyclerView.setAdapter(mSelectAlloneChildDeviceAdapter);
//        } else {
//            mSelectAlloneChildDeviceAdapter.refresh(devices, checkedDevices);
//        }
        if (mSelectAlloneChildDevicesAdapter == null) {
            mSelectAlloneChildDevicesAdapter = new SelectAlloneChildDevicesAdapter(SelectAllone2ChildDeviceActivity.this, devices, checkedDevices);
            mDevices_lv.setAdapter(mSelectAlloneChildDevicesAdapter);
        } else {
            mSelectAlloneChildDevicesAdapter.refresh(devices, checkedDevices);
        }
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        mSelectAlloneChildDevicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = (Device) view.getTag(R.id.tag_device);
        if (device != null) {
            if (device.getDeviceType() != DeviceType.ALLONE) {
                if (isContainDevice(device)) {
                    removeSelectedDevice(device);
                } else {
                    boolean isLearned;//如果是小方学习设备，未学习不能选择
                    if (ProductManage.isAlloneLearnDevice(device)) {
                        if (KKIrDao.getInstance().sunDeviceIsLearned(device.getDeviceId())) {
                            isLearned = true;
                        } else {
                            isLearned = false;
                        }
                    } else {
                        isLearned = true;
                    }
                    if (isLearned) {
                        checkedDevices.add(device);
                    }
                }
                mSelectAlloneChildDevicesAdapter.selectDevices(checkedDevices);
            }
        }
    }

    /**
     * 将设备从选中列表移除
     *
     * @param removeDevice
     */
    private void removeSelectedDevice(Device removeDevice) {
        if (checkedDevices != null && !checkedDevices.isEmpty() && removeDevice != null) {
            String deviceId = removeDevice.getDeviceId();
            if (!TextUtils.isEmpty(deviceId)) {
                final int count = checkedDevices.size();
                for (int i = 0; i < count; i++) {
                    Device device = checkedDevices.get(i);
                    if (deviceId.equals(device.getDeviceId())) {
                        checkedDevices.remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 判断是否包含设备
     *
     * @param selectedDevice
     * @return true已经包含此设备
     */
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
            LogUtil.d(TAG, "returnResult() - checkedDevices" + checkedDevices);
            setResult(RESULT_OK, data);
        }
//        else if (commonDevice == CommonDeviceActivity.TYPE_COMMON_DEVICE) {
//            Intent data = new Intent();
//            data.putExtra("checkedDevices", checkedDevices);
//            LogUtil.d(TAG, "returnResult() - checkedDevices" + checkedDevices);
//            setResult(RESULT_OK, data);
//        }
    }
}
