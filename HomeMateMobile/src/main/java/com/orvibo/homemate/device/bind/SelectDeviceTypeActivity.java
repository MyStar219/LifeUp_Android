package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.ImageName;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.device.bind.adapter.SelectDeviceTypeAdapter;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择设备类型
 */
public class SelectDeviceTypeActivity extends BaseActivity implements AdapterView.OnItemClickListener, NavigationCocoBar.OnLeftClickListener {
    private SelectDeviceTypeAdapter mDeviceTypeAdapter;
    private List<ImageName> imageNameLists = new ArrayList<ImageName>();
    private NavigationCocoBar navigationCocoBar;
    private Device device;
    private ModifyDevice modifyDevice;

    int mSelectedDeviceType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_type);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationCocoBar);
        navigationCocoBar.setOnLeftClickListener(this);
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        initImageNameLists();
        mSelectedDeviceType = device.getDeviceType();
        mDeviceTypeAdapter = new SelectDeviceTypeAdapter(mContext, imageNameLists, device.getDeviceType());
//        int pW = PhoneUtil.getScreenPixels(this)[0];//屏幕宽度，px
//        int s = pW * 27 / 750;
        GridView gridView = (GridView) findViewById(R.id.type_gv);
//        gridView.setHorizontalSpacing(s);
//        gridView.setVerticalSpacing(s);
        gridView.setAdapter(mDeviceTypeAdapter);
        gridView.setOnItemClickListener(this);
    }

    private void initImageNameLists() {
        ImageName imageName3 = new ImageName(DeviceType.SCREEN, R.drawable.scene_pic_curtain, R.string.device_type_SCREEN_3);//3：幕布
        //ImageName imageName4 = new ImageName(DeviceType.WINDOW_SHADES, R.drawable.scene_pic_rolling_gate, R.string.device_type_WINDOW_SHADES_4);//3：幕布
        ImageName imageName8 = new ImageName(DeviceType.CURTAIN, R.drawable.scene_pic_open_window, R.string.device_type_CURTAIN_8);//8：对开窗帘
        ImageName imageName39 = new ImageName(DeviceType.ROLLING_GATE, R.drawable.scene_pic_rolling_gate, R.string.device_type_ROLLING_GATE_39);//39：卷闸门
        ImageName imageName37 = new ImageName(DeviceType.PUSH_WINDOW, R.drawable.scene_pic_pushwindow, R.string.device_type_CURTAIN_37);//41：推窗器
        ImageName imageName42 = new ImageName(DeviceType.ROLLER_SHUTTERS, R.drawable.scene_pic_shutter, R.string.device_type_ROLLER_SHUTTERS_42);//42：卷帘
        ImageName imageNameFake = new ImageName(Constant.INVALID_NUM, Constant.INVALID_NUM, Constant.INVALID_NUM);//假数据
        imageNameLists.add(imageName8);
        imageNameLists.add(imageName42);
        imageNameLists.add(imageName3);
        imageNameLists.add(imageName39);
        imageNameLists.add(imageName37);
        //imageNameLists.add(imageName4);
        imageNameLists.add(imageNameFake);
        initModifyDevice();
    }

    private void initModifyDevice() {
        modifyDevice = new ModifyDevice(mContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                super.onModifyDeviceResult(uid, serial, result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    Intent intent = new Intent();
                    intent.putExtra(Constant.DEVICE, device);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (result == ErrorCode.TIMEOUT_MD) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
                } else {
                    if (!ToastUtil.toastCommonError(result)) {
                        ToastUtil.toastError(result);
                    }
                }
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 5) {
            mSelectedDeviceType = (int) mDeviceTypeAdapter.getItemId(position);
            mDeviceTypeAdapter.select(mSelectedDeviceType);
            device.setDeviceType(mSelectedDeviceType);
            showDialog();
            modifyDevice.modify(device.getUid(), device.getUserName(), device.getDeviceName(), mSelectedDeviceType, device.getRoomId(), device.getIrDeviceId(), device.getDeviceId());
        }
//        Intent intent = new Intent();
//        intent.putExtra("deviceType", mSelectedDeviceType);
//        setResult(RESULT_OK, intent);
//        finish();
    }

    @Override
    public void onLeftClick(View v) {
        finish();
//        showDialog();
//        modifyDevice.modify(device.getUid(), device.getUserName(), device.getDeviceName(), mSelectedDeviceType, device.getRoomId(), device.getIrDeviceId(), device.getDeviceId());
    }

}
