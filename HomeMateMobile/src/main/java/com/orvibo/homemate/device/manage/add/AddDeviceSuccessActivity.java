package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnNewDeviceListener;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.device.manage.PercentCurtainSetDeviceActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.device.manage.adapter.AddDeviceSuccessAdapter;
import com.orvibo.homemate.device.manage.edit.DeviceEditActivity;
import com.orvibo.homemate.device.manage.edit.SceneDeviceEditActivity;
import com.orvibo.homemate.device.manage.edit.SensorDeviceEditActivity;
import com.orvibo.homemate.model.NewDevice;
import com.orvibo.homemate.sharedPreferences.GatewayCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加设备成功后跳转到此activity
 *
 * @author huangqiyao
 */
public class AddDeviceSuccessActivity extends BaseActivity implements OnNewDeviceListener {
    private static final String TAG = AddDeviceSuccessActivity.class.getSimpleName();
    public static final int REQUESTCODE = 0;
    private List<Device> mDevices;
    private AddDeviceSuccessAdapter mAdapter;
    private String bindUid;

    private NewDevice newDevice;

    private FrequentlyModeDao frequentlyModeDao;
    private List<FrequentlyMode> frequentlyModes = new ArrayList<FrequentlyMode>();
    private TextView mTip_tv;
    private FloorDao mFloorDao;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_success);
        Intent intent = getIntent();
        bindUid = intent.getStringExtra("vicenter");
        mDevices = (List<Device>) getIntent().getSerializableExtra(
                "addedDevices");
        LogUtil.d(TAG, "onCreate(0)-bindUid:" + bindUid + ",mDevices:" + mDevices);
        frequentlyModeDao = new FrequentlyModeDao();
        if (mDevices != null && !StringUtil.isEmpty(bindUid)) {
            Device device = new Device();
            device.setUid(bindUid);
            device.setExtAddr("");
            device.setDeviceName(getString(R.string.vicenter_default_name));
            String model = GatewayCache.getGatewayModel(bindUid);
            GatewayDao mGatewayDao = new GatewayDao();
            Gateway gateway = mGatewayDao.selGatewayByUid(bindUid);
            if (gateway != null) {
                device.setModel(gateway.getModel());
                device.setDeviceId(gateway.getUid());
                ProductManage pm = ProductManage.getInstance();
                if (pm.isVicenter300(bindUid, model)) {
                    if (pm.isHub(bindUid, model)) {
                        device.setDeviceType(DeviceType.MINIHUB);
                    } else {
                        device.setDeviceType(DeviceType.VICENTER);
                    }
                }
            } else {
                device.setDeviceType(DeviceType.VICENTER);
                LogUtil.e(TAG, "onCreate()-Couldn't found " + bindUid + "'s gateway data.");
            }
            // }
            device.setDeviceId(Constant.INVALID_NUM + "");
            mDevices.add(0, device);
        }
        mAdapter = new AddDeviceSuccessAdapter(mContext, mDevices);
        LogUtil.d(TAG, "onCreate()-bindUid:" + bindUid + ",mDevices:" + mDevices);
        ListView listView = (ListView) findViewById(R.id.newDevice_lv);
        listView.setAdapter(mAdapter);
        mTip_tv = (TextView) findViewById(R.id.addDeviceTip);
        mFloorDao = new FloorDao();
        setTip(mDevices);
        newDevice = new NewDevice();
        newDevice.setOnNewDeviceListener(this);


    }


    private boolean isHasFloor() {
        boolean isHasFloorAndRoom = false;
        List<Floor> allFloors = mFloorDao.selAllFloors(currentMainUid);
        if (!allFloors.isEmpty()) {
            isHasFloorAndRoom = true;
        }
        return isHasFloorAndRoom;
    }

    private void setTip(List<Device> devices) {
        boolean isMulDevice = false;
        if (devices != null && devices.size() > 1) {
            isMulDevice = true;
        }
        //没有楼层和存在楼层两种情况对应两种翻译。
        if (isHasFloor()) {
            if (isMulDevice) {
                mTip_tv.setText(getString(R.string.add_devices_tip));
            } else {
                mTip_tv.setText(getString(R.string.add_device_tip));
            }
        } else {
            if (isMulDevice) {
                mTip_tv.setText(getString(R.string.add_devices_tip_nofloor));
            } else {
                mTip_tv.setText(getString(R.string.add_device_tip_nofloor));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        newDevice.acceptNewDevice(mContext);
    }

    @Override
    protected void onStop() {
        super.onStop();
        newDevice.stopAcceptNewDevice();
    }

    @Override
    public void onNewDevice(Device device) {
        if (device != null) {
            String deviceId = device.getDeviceId();
            if (!StringUtil.isEmpty(deviceId)) {
                boolean exist = false;
                for (int i = 0; i < mDevices.size(); i++) {
                    Device oldDevice = mDevices.get(i);
                    if (deviceId.equals(oldDevice.getDeviceId())) {
                        exist = true;
                        mDevices.set(i, device);
                        break;
                    }
                }
                if (!exist) {
                    mDevices.add(device);
                }
                mAdapter.refresh(mDevices);
                setTip(mDevices);
            }
        }
    }

    @Override
    public void onNewCamera(CameraInfo cameraInfo) {

    }

    public void deviceSet(View v) {
        Device device = (Device) v.getTag(R.id.tag_device);
        Intent intent;
        if (device != null) {
            if (device.getDeviceType() == DeviceType.VICENTER || device.getDeviceType() == DeviceType.MINIHUB) {
                intent = new Intent(mContext, DeviceEditActivity.class);
            } else if (device.getDeviceType() == DeviceType.INFRARED_SENSOR ||
                    device.getDeviceType() == DeviceType.MAGNETIC ||
                    device.getDeviceType() == DeviceType.MAGNETIC_WINDOW ||
                    device.getDeviceType() == DeviceType.MAGNETIC_DRAWER ||
                    device.getDeviceType() == DeviceType.MAGNETIC_OTHER ||
                    device.getDeviceType() == DeviceType.SMOKE_SENSOR) {
                intent = new Intent(mContext, SensorDeviceEditActivity.class);
            } else if (device.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD ||
                    device.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD ||
                    device.getDeviceType() == DeviceType.SCENE_KEYPAD) {
                intent = new Intent(mContext, SceneDeviceEditActivity.class);
            } else if (device.getDeviceType() == DeviceType.CURTAIN_PERCENT || device.getDeviceType() == DeviceType.ROLLER_SHADES_PERCENT) {
                frequentlyModes = frequentlyModeDao.selFrequentlyModes(device.getDeviceId());
                if (frequentlyModes.size() != 0) {
                    intent = new Intent(this, PercentCurtainSetDeviceActivity.class);
                } else {
                    intent = new Intent(mContext, SetDeviceActivity.class);
                }
            } else {
                intent = new Intent(mContext, SetDeviceActivity.class);
            }
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.FIRST_EDIT_DEVICE, true);
            startActivityForResult(intent, REQUESTCODE);
        } else {
            if (mAdapter != null) {
                List<Device> devices = new ArrayList<Device>();
                if (mDevices != null && !mDevices.isEmpty()) {
                    for (Device d : mDevices) {
                        if (d != null) {
                            devices.add(d);
                        }
                    }
                }
                mAdapter.refresh(devices);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == REQUESTCODE) {
            Serializable serializable = data.getSerializableExtra(IntentKey.DEVICE);
            if (serializable != null && serializable instanceof Device) {
                Device setDevice = (Device) serializable;
                String uid = setDevice.getUid();
                String deviceId = setDevice.getDeviceId();
//            String uid = data.getStringExtra("uid");
//            int deviceId = data.getIntExtra("deviceId", -1);
                final String extAddr = setDevice.getExtAddr();
                List<Device> devices = new ArrayList<Device>();
                List<Device> deleteDevices = new ArrayList<Device>();
                int deviceType = setDevice.getDeviceType();
                if (deviceType != DeviceType.TV && deviceType != DeviceType.AC && deviceType != DeviceType.STB && deviceType != DeviceType.SELF_DEFINE_IR) {
                    for (Device device : mDevices) {
                        devices.add(device);
                        if (resultCode == SetDeviceActivity.RESULT_DELETE) {
                            if (device.getUid().equals(uid)
                                    && device.getExtAddr().equals(extAddr)) {
                                deleteDevices.add(device);
                            }
                        } else {
                            if (device.getUid().equals(uid)
                                    && device.getExtAddr().equals(extAddr) && device.getDeviceId().equals(setDevice.getDeviceId())) {
                                deleteDevices.add(device);
                            }
                        }
                    }
                } else {
                    deleteDevices.add(setDevice);
                }
                for (Device device : deleteDevices) {
                    devices.remove(device);
                }
                deleteDevices.clear();
                deleteDevices = null;
                mDevices = devices;
                mAdapter.refresh(mDevices);
                //把vicenter去掉
                if (!StringUtil.isEmpty(bindUid) && mDevices.size() == 1) {
                    Device device = mDevices.get(0);
                    if ((Constant.INVALID_NUM + "").equals(device.getDeviceId())) {
                        mDevices.remove(0);
                    }
                }
                if (mDevices.isEmpty()) {
                    toIndex(null);
                } else {
                    setTip(mDevices);
                }
            }
        }
    }

    private boolean isToIndex = false;

    public void toIndex(View v) {
        //回到首页
        isToIndex = true;
        if (canBack()) {
//            setResult(ResultCode.FINISH);
            Intent toMainIntent = new Intent(mContext, MainActivity.class);
            startActivity(toMainIntent);
            finish();
        } else {
            showPopup(R.string.add_device_index_popup_yes);
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
//        isToIndex = false;
//        if (canBack()) {
//            setResult(ResultCode.FINISH);
//            super.onBackPressed();
//        } else {
//            showExistDeviceNotSetPopup();
//        }
    }

    @Override
    public void onBackPressed() {
        isToIndex = false;
        if (canBack()) {
            setResult(ResultCode.FINISH);
            try {
                super.onBackPressed();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showExistDeviceNotSetPopup();
        }
    }

    private boolean canBack() {
        return mDevices == null || mDevices.isEmpty();
    }

    private void showExistDeviceNotSetPopup() {
        showPopup(R.string.back);
    }

    private void showPopup(int yes) {
        new ConfirmAndCancelPopup() {
            @Override
            public void confirm() {
                super.confirm();
                dismiss();
//                if (isToIndex) {
//                    setResult(AddVicenterTipActivity.CODE_EXIT_ADD_DEVICE);
                //跳转到主界面
                Intent toMainIntent = new Intent(mContext, MainActivity.class);
                startActivity(toMainIntent);
//                } else {
//                    setResult(ResultCode.FINISH);
//                }
                finish();
            }
        }.showPopup(mContext, R.string.add_device_popup_content, yes, R.string.cancel);
    }

    @Override
    protected void onDestroy() {
        if (newDevice != null) {
            newDevice.setOnNewDeviceListener(null);
        }
        super.onDestroy();
    }
}
