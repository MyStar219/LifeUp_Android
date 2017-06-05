package com.orvibo.homemate.device.manage;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.util.List;

/**
 * 设备设置界面
 * update by yuwei at 2016-5-28 17:40
 */
public class DeviceSettingActivity extends BaseActivity {
    private static final String TAG = DeviceSettingActivity.class.getSimpleName();
    private TextView             socketConfigSuccessNum;
    private TextView tv_deviceName_title;
    private EditTextWithCompound deviceNameEditText;
    private ModifyDevice         modifyDevice;
    private Device               device;
    private int                  typeId;
    private LinearLayout         mLinearLayout;
    private ImageView            mImageView;

    private List<Device> danaleDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_setting_activity);
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(Constant.DEVICE);
        typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);
        if (typeId == R.string.xiao_ou_camera){
            initDanaleDeivce();
        }else {
            init();
        }
        initModifyDevice();
        initSystemBar(this);
    }

    private void initModifyDevice() {
        modifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                LogUtil.d(TAG, "result:" + result);
                dismissDialog();
                if (ProductManage.getInstance().isCOCO(device)) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_AddSuccessfully_Save), null);
                }
                Intent intent = new Intent(DeviceSettingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
    }

    @Override
    public void onBackPressed() {
        save(null);
    }

    private void init() {
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        mLinearLayout = (LinearLayout) findViewById(R.id.device_add_successfully_tips);
        mImageView = (ImageView) findViewById(R.id.socket_config_success);
        socketConfigSuccessNum = (TextView) findViewById(R.id.socketConfigSuccessNum);
        navigationBar.setLeftTextViewVisibility(View.INVISIBLE);
        deviceNameEditText = (EditTextWithCompound) findViewById(R.id.deviceNameEditText);
        String deviceName = device.getDeviceName();
        if (ProductManage.getInstance().isWifiDeviceByModel(device.getModel())) {
            deviceNameEditText.setNeedRestrict(false);
        }
        if (!PhoneUtil.isCN(this)) {
            navigationBar.setCenterText("");
            mLinearLayout.setPadding(getResources().getDimensionPixelSize(R.dimen.padding_x8), getResources().getDimensionPixelSize(R.dimen.padding_x1), 0, getResources().getDimensionPixelSize(R.dimen.padding_x8));
            mLinearLayout.setBackgroundColor(getResources().getColor(R.color.green));
            mImageView.setImageResource(R.drawable.bg_right);
            socketConfigSuccessNum.setText(getString(R.string.add_device_success_haiwai_tip));
        } else {
            socketConfigSuccessNum.setText(getString(R.string.add_device_success_tip) + deviceName);
        }

//        int deviceType = device.getDeviceType();
//        String model = device.getModel();
//        if (deviceType == DeviceType.CAMERA) {
//            socketConfigSuccessNum.setText(getString(R.string.add_ys_device_success));
//        } else if (deviceType == DeviceType.CLOTHE_SHORSE) {
//            socketConfigSuccessNum.setText(R.string.add_cth_device_success);
//        } else if (deviceType == DeviceType.COCO) {
//        } else {
        // socketConfigSuccessNum.setText(getString(R.string.add_device_success_tip) + deviceName);
//        }
        deviceNameEditText.setText(deviceName + "");
//        if (device.getDeviceType() == DeviceType.CAMERA) {
//            socketConfigSuccessNum.setText(R.string.add_ys_device_success);
//            deviceNameEditText.setText(R.string.ys_camerwa);
//        }
        deviceNameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        deviceNameEditText.setSelection(deviceNameEditText.length());
//        switch (typeId) {
//            case R.string.device_add_airer:
//            case R.string.device_add_s20c:
//            case R.string.device_add_yidong:
//                socketConfigSuccessNum.setText(getString(R.string.add_success) + getString(typeId));
//                deviceNameEditText.setText(getString(typeId));
//                break;
//        }
    }

    public void save(View view) {
        // 判断网络是否连接
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(getString(R.string.net_not_connect));
            return;
        }
        if (typeId == R.string.xiao_ou_camera && danaleDeviceList!=null && danaleDeviceList.size()>1){
            //一次绑定成功多台，不能修改名称，点击完成直接跳到设备列表界面
            Intent intent = new Intent(DeviceSettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            String deviceName = deviceNameEditText.getText().toString();
            if (TextUtils.isEmpty(deviceName)) {
                ToastUtil.showToast(getString(R.string.device_setting_name_empty));
                return;
            }
            if (device != null) {
                showDialogNow();
                //updateDanaleDeviceName(device,typeId,deviceName);
                modifyDevice.modify(device.getUid(), UserCache.getCurrentUserName(this), deviceName, device.getDeviceType(), 0 + "", device.getIrDeviceId()==null?0+"":device.getIrDeviceId(), device.getDeviceId());
            }
        }
    }

    /**
     * 手机状态栏沉浸设置
     *
     * @param activity
     */
    public  void initSystemBar(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            setTranslucentStatus(activity, true);

        }

        SystemBarTintManager tintManager = new SystemBarTintManager(activity);

        tintManager.setStatusBarTintEnabled(true);

        // 使用颜色资源
        tintManager.setStatusBarTintResource(R.color.green);

    }

    @TargetApi(19)
    private static void setTranslucentStatus(Activity activity, boolean on) {

        Window win = activity.getWindow();

        WindowManager.LayoutParams winParams = win.getAttributes();
        //透明状态栏
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        if (on) {

            winParams.flags |= bits;

        } else {

            winParams.flags &= ~bits;

        }

        win.setAttributes(winParams);

    }

    private void initDanaleDeivce(){
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        Button saveButton = (Button) findViewById(R.id.saveButton);
        mLinearLayout = (LinearLayout) findViewById(R.id.device_add_successfully_tips);
        mImageView = (ImageView) findViewById(R.id.socket_config_success);
        socketConfigSuccessNum = (TextView) findViewById(R.id.socketConfigSuccessNum);
        deviceNameEditText = (EditTextWithCompound) findViewById(R.id.deviceNameEditText);
        tv_deviceName_title = (TextView) findViewById(R.id.tv_deviceName_title);
    }
}
