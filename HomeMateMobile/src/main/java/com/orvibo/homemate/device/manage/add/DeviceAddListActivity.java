package com.orvibo.homemate.device.manage.add;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.client.android.RobotActivity;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginIntent;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.device.HopeMusic.AddHopeMusicActivity;
import com.orvibo.homemate.device.manage.adapter.DeviceAddAdapter;
import com.orvibo.homemate.device.searchdevice.SearchWifiDevice;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DeviceAddListActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener,
        AdapterView.OnItemClickListener,
        NavigationCocoBar.OnLeftClickListener {
    private static final String TAG = DeviceAddListActivity.class.getSimpleName();
    private String loginEntryString;
    private int loginIntent = LoginIntent.ALL;
    private boolean isShowDialog;
    private DeviceAddAdapter deviceAddAdapter;

    private LinkedHashMap<Integer, Integer> iconWithName = new LinkedHashMap<>();
    private TextView mUnbindDeviceTextView;
    private QueryUnbinds mQueryUnbinds;
    private ArrayList<DeviceQueryUnbind> mDeviceQueryUnbinds = new ArrayList<DeviceQueryUnbind>();
    private SearchWifiDevice mSearchWifiDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add_list);
        init();
        isShowDialog = getIntent().getBooleanExtra("isShowDialog", false);
        if (isShowDialog) {
            showErrorDialog(getString(R.string.qr_scanning_failed), getString(R.string.qr_scanning_failed_tip));
        }
        initView();
    }

    private void initView() {
        mUnbindDeviceTextView = (TextView) findViewById(R.id.unbindDeviceTextView);
        // initQueryUnbinds();
        mSearchWifiDevice = new SearchWifiDevice(this, mUnbindDeviceTextView);
    }



    @Override
    protected void onResume() {
        super.onResume();
        mUnbindDeviceTextView.setVisibility(View.GONE);
        // mQueryUnbinds.queryAllWifiDevices(this, false);
        mSearchWifiDevice.searchWifiDevice(false);
    }

    private void init() {
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        if (!("zh".equals(language))) {
            navigationBar.setCenterText(getResources().getString(R.string.device_add_by_type));
        }
        ListView deviceListView = (ListView) findViewById(R.id.deviceListView);
        iconWithName.put(R.string.device_add_host, R.drawable.device_add_host);
        iconWithName.put(R.string.device_add_socket, R.drawable.device_add_socket);
        iconWithName.put(R.string.device_add_switch, R.drawable.device_add_switch);
        iconWithName.put(R.string.device_add_remote_control, R.drawable.device_add_remote_control);
        iconWithName.put(R.string.device_add_light, R.drawable.device_add_icon_intelligentlighting);
        iconWithName.put(R.string.device_add_iintelligent_door_lock, R.drawable.device_add_iintelligent_door_lock);
        iconWithName.put(R.string.device_add_sensor, R.drawable.device_add_sensor);
        iconWithName.put(R.string.device_add_camera, R.drawable.device_add_camera);
        iconWithName.put(R.string.device_add_smart_sound, R.drawable.device_add_intelligent_sound);
        iconWithName.put(R.string.device_add_curtain_motor, R.drawable.device_add_curtain_motor);
        iconWithName.put(R.string.device_add_airer, R.drawable.device_add_clothes_hanger);
        iconWithName.put(R.string.device_add_control_box, R.drawable.device_add_control_box);
        if (PhoneUtil.isCN(mContext)) {
            iconWithName.put(R.string.device_add_robot, R.drawable.device_add_robot);
        }
        deviceAddAdapter = new DeviceAddAdapter(iconWithName);
        deviceListView.setAdapter(deviceAddAdapter);
        deviceListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String userName = UserCache.getCurrentUserName(mAppContext);
        int logoutStatus = UserCache.getLoginStatus(mAppContext, userName);
        int brandNameId = deviceAddAdapter.getItem(position);
        if (brandNameId == R.string.device_add_socket
                || brandNameId == R.string.device_add_airer
                || brandNameId == R.string.device_add_camera
                || brandNameId == R.string.device_add_sensor
                || brandNameId == R.string.device_add_remote_control
                || brandNameId == R.string.device_add_smart_sound) {
            Intent intent;
            if (brandNameId == R.string.device_add_smart_sound) {
                intent = new Intent(DeviceAddListActivity.this, AddHopeMusicActivity.class);
                //TODO 跟IntentKey.DEVICE_ADD_TYPE一致
                intent.putExtra(Constant.CONFIG_TITLE, brandNameId);
            } else {
                intent = new Intent(DeviceAddListActivity.this, DeviceAddTwoPageActivity.class);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, brandNameId);
                if (mSearchWifiDevice != null && mSearchWifiDevice.getDeviceQueryUnbinds() != null) {
                    intent.putExtra(IntentKey.UNBIND_WIFI_DEVICE, (Serializable) mSearchWifiDevice.getDeviceQueryUnbinds());
                }
            }
            startActivity(intent);
        } else if (logoutStatus != LoginStatus.SUCCESS && logoutStatus != LoginStatus.FAIL) {
            loginIntent = LoginIntent.ALL;
            loginEntryString = Constant.ViHome;
            showLoginDialog();
//        } else if (brandNames[position] == R.string.device_add_camera) {
//            Intent intent = new Intent(this, YsAdd1Activity.class);
//            startActivity(intent);
        } else {
            if (brandNameId == R.string.device_add_host) {
                Intent intent;
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddDevice_ViHomeProSmartHomeSystem), null);
                intent = new Intent(DeviceAddListActivity.this, AddVicenterTipActivity.class);
                intent.putExtra(Constant.CONFIG_TITLE, brandNameId);
                startActivityForResult(intent, 1);
            } else if (brandNameId == R.string.device_add_robot) {
                Account account = new AccountDao().selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
                if (account != null) {
                    if (TextUtils.isEmpty(account.getEmail()) && TextUtils.isEmpty(account.getPhone())) {
                        showErrorDialog(getString(R.string.warm_tips), getString(R.string.need_bind_phone_or_email));
                    } else {
                        Intent intent = new Intent(DeviceAddListActivity.this, RobotActivity.class);
                        startActivity(intent);
                    }
                }
            } else {
                //version 1.9  Smart Switch二级页面增加美标开关系列四个产品
                if ((!PhoneUtil.isCN(DeviceAddListActivity.this)) && brandNameId == R.string.device_add_switch) {
                    toDeviceAddTwoPageActivity(brandNameId);
                } else {
                    Intent intent = new Intent(DeviceAddListActivity.this, ZigBeeDeviceAddActivity.class);
                    intent.putExtra(Constant.CONFIG_TITLE, brandNameId);
                    startActivity(intent);
                }
            }
        }
    }

    private void toDeviceAddTwoPageActivity(int brandName) {
        Intent intent = new Intent(DeviceAddListActivity.this, DeviceAddTwoPageActivity.class);
        intent.putExtra(IntentKey.DEVICE_ADD_TYPE, brandName);
        intent.putExtra(IntentKey.UNBIND_WIFI_DEVICE, (Serializable) mDeviceQueryUnbinds);
        startActivity(intent);
    }

    public void showLoginDialog() {
        DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
        String title = getString(R.string.login_now_title);
        dialogFragment.setTitle(title);
        dialogFragment.setLeftButtonText(getString(R.string.cancel));
        dialogFragment.setRightButtonText(getString(R.string.login));
        dialogFragment.setOnTwoButtonClickListener(DeviceAddListActivity.this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }


    /**
     * 二维码扫描无匹配数据弹框；公子小白添加没绑定邮箱和手机
     */
    private void showErrorDialog(String title, String content) {
        DialogFragmentOneButton dialogFragment = new DialogFragmentOneButton();
        dialogFragment.setTitle(title);
        dialogFragment.setContent(content);
        dialogFragment.setButtonText(getString(R.string.confirm));
        dialogFragment.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismissDialog();
            }
        });
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }

    @Override
    public void onLeftButtonClick(View view) {
    }

    @Override
    public void onRightButtonClick(View view) {
        Intent intent = new Intent(DeviceAddListActivity.this, LoginActivity.class);
        intent.putExtra(Constant.LOGIN_ENTRY, loginEntryString);
        intent.putExtra(IntentKey.LOGIN_INTENT, loginIntent);
        //  intent.putExtra(IntentKey.ACTIVITY_CODE, ActivityCode.DEVICE_ADD_ACTIVITY);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddDeviceBack), null);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-resultCode:" + resultCode);
        if (resultCode == ResultCode.FINISH) {
//            finish();
        }
    }
}
