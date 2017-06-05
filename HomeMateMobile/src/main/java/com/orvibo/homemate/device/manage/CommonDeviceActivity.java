package com.orvibo.homemate.device.manage;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.CommonFlag;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.BaseSelectDeviceActionsActivity;
import com.orvibo.homemate.device.bind.SelectDeviceActivity;
import com.orvibo.homemate.device.manage.adapter.CommonDeviceAdapter;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenu;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuCreator;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuItem;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuListView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonDeviceActivity extends BaseSelectDeviceActionsActivity {
    private static final String TAG = CommonDeviceActivity.class.getSimpleName();
    private SwipeMenuListView lvCommonDevice;
    private TextView empty_add_common_device;
    private TextView add_common_device;
    private SwipeMenuCreator creator;
    private ConfirmAndCancelPopup mDeletePopup;

    private DeviceDao deviceDao;
    private ArrayList<Device> devices = new ArrayList<>();
    private CommonDeviceAdapter commonDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        deviceDao = new DeviceDao();
        findViews();
        init();
    }

    private void findViews() {
        lvCommonDevice = (SwipeMenuListView) findViewById(R.id.lvCommonDevice);
        LinearLayout emptyView = (LinearLayout) findViewById(R.id.empty_ll);
        lvCommonDevice.setEmptyView(emptyView);

        empty_add_common_device = (TextView) findViewById(R.id.empty_add_common_device);
        empty_add_common_device.setOnClickListener(this);
        add_common_device = (TextView) findViewById(R.id.add_common_device);
        add_common_device.setOnClickListener(this);
    }

    private void init() {
        creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red)));
                deleteItem.setWidth(getResources().getDimensionPixelSize(R.dimen.swipe_button_width));
                deleteItem.setTitle(getString(R.string.delete));
                deleteItem.setTitleSize((int) (getResources().getDimensionPixelSize(R.dimen.text_normal) / getResources().getDisplayMetrics().scaledDensity));
                deleteItem.setTitleColor(getResources().getColor(R.color.font_black));
                menu.addMenuItem(deleteItem);
            }
        };

        lvCommonDevice.setMenuCreator(creator);
        lvCommonDevice.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//                showDeleteCommonDevicePopup(position);
                Device device = devices.get(position);
                final List<Device> devices = new ArrayList<>();
                devices.add(device);
                deviceDao.updateCommonDevices(mContext,devices,CommonFlag.NORMAL);
                refreshList();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
//        devices = deviceDao.selDevicesByCommonFlag(userId, CommonFlag.COMMON);
        devices = (ArrayList) DeviceTool.getCommonDevices(mContext);
        if (commonDeviceAdapter == null) {
            commonDeviceAdapter = new CommonDeviceAdapter(this,devices);
            lvCommonDevice.setAdapter(commonDeviceAdapter);
        } else {
            commonDeviceAdapter.refreshDevices(devices);
        }
        if (devices.size() == 0) {
            add_common_device.setVisibility(View.GONE);
        } else {
            add_common_device.setVisibility(View.VISIBLE);
        }
    }

//    private void showDeleteCommonDevicePopup(int position) {
//        Device device = devices.get(position);
//        final List<Device> devices = new ArrayList<>();
//        devices.add(device);
//        mDeletePopup = new ConfirmAndCancelPopup() {
//            @Override
//            public void confirm() {
//                deviceDao.updateCommonDevices(mContext,devices,CommonFlag.NORMAL);
//                refreshList();
//                mDeletePopup.dismiss();
//            }
//        };
//        mDeletePopup.showPopup(mContext, String.format(getString(R.string.scene_delete_content), device.getDeviceName()), getString(R.string.delete), getString(R.string.cancel));
//    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
//        if (v.getId() == R.id.addCommonDevices) {
            Intent intent = new Intent(this,
                    SelectDeviceActivity.class);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.COMMON);
            intent.putExtra(IntentKey.DEVICES, (Serializable) devices);
            startActivityForResult(intent, SELECT_DEVICE);
//        }
    }

    @Override
    protected void onSelectDevices(List<Device> devices) {
        deviceDao.updateCommonDevices(mContext,devices,CommonFlag.COMMON);
        List<Device> normalDevices = new ArrayList<>();
        List<String> newDeviceIds = new ArrayList<>();
        if (devices != null) {
            for (Device device : devices) {
                newDeviceIds.add(device.getDeviceId());
            }
        }
        for (Device device : this.devices) {
            if (newDeviceIds.size() == 0) {
                normalDevices.add(device);
            } else if (!newDeviceIds.contains(device.getDeviceId())) {
                normalDevices.add(device);
            }
        }
        deviceDao.updateCommonDevices(mContext,normalDevices,CommonFlag.NORMAL);
        refreshList();
    }

//    @Override
//    protected ArrayList<String> getCommonDeviceIds() {
//        ArrayList<String> deviceIds = new ArrayList<String>();
//        for (Device device : devices) {
//            deviceIds.add(device.getDeviceId());
//        }
//        return  deviceIds;
//    }


//    @Override
//    protected ArrayList<Device> getCommonDevices() {
//        return devices;
//    }

}
