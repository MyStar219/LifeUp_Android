package com.orvibo.homemate.device.manage.add;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginIntent;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.device.ap.ApConfig1Activity;
import com.orvibo.homemate.device.manage.adapter.DeviceAddAdapter;
import com.orvibo.homemate.device.searchdevice.SearchWifiDevice;
import com.orvibo.homemate.device.ys.YsAdd1Activity;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 添加设备二级列表
 * Created by snown on 2015/11/18
 */
public class DeviceAddTwoPageActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener,
        AdapterView.OnItemClickListener,
        NavigationCocoBar.OnLeftClickListener {
    private static final String TAG = DeviceAddTwoPageActivity.class.getSimpleName();
    private String loginEntryString;
    private int loginIntent = LoginIntent.ALL;
    int typeId;
    private LinkedHashMap<Integer, Integer> iconWithName = new LinkedHashMap<>();
    private DeviceAddAdapter deviceAddAdapter;
    private boolean isCn;
    private ArrayList<DeviceQueryUnbind> mDeviceQueryUnbinds;
    private TextView mUnbindDeviceTextView;
    private QueryUnbinds mQueryUnbinds;
    private SearchWifiDevice mSearchWifiDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add_two_page);
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        typeId = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, R.string.device_add_coco);
        isCn = PhoneUtil.isCN(mContext);
        switch (typeId) {
            case R.string.device_add_socket:
                iconWithName.put(R.string.device_add_coco, R.drawable.icon_bg_coco);
                // 海外
                if (!isCn) {
                    iconWithName.put(R.string.device_add_socket_eu, R.drawable.device_500_s20_eu2);
                    iconWithName.put(R.string.device_add_socket_au, R.drawable.device_500_s20_au);
                    iconWithName.put(R.string.device_add_socket_cn, R.drawable.device_500_s20_cn);
                    iconWithName.put(R.string.device_add_socket_uk, R.drawable.device_500_s20_uk);
                    iconWithName.put(R.string.device_add_socket_us, R.drawable.device_500_s20_us);
                    iconWithName.put(R.string.device_add_s31_socket, R.drawable.device_120_s31_no);
                } else {
                    iconWithName.put(R.string.device_add_s20c, R.drawable.icon_bg_lianrong_s20);
                    iconWithName.put(R.string.device_add_yidong, R.drawable.icon_bg_yidong_s);
                    iconWithName.put(R.string.device_add_feidiao_lincoln, R.drawable.icon_bg_feidiao_lincoln);
                    iconWithName.put(R.string.device_add_feidiao_xiaoe, R.drawable.icon_bg_feidiao_xiaoe);
                    iconWithName.put(R.string.device_add_other_socket, R.drawable.icon_bg_smart_socket);
                }
                break;
            case R.string.device_add_airer:
                iconWithName.put(R.string.device_add_liangba, R.drawable.icon_bg_liangba);
                iconWithName.put(R.string.device_add_oujia, R.drawable.icon_bg_zicheng);
                iconWithName.put(R.string.device_add_aoke_liangyi, R.drawable.icon_bg_aoke_liangyi);
                if (isCn) {
                    iconWithName.put(R.string.device_add_mairunclothes, R.drawable.device_500_mairun);
                }
                break;
            case R.string.device_add_camera:
                iconWithName.put(R.string.xiao_ou_camera, R.drawable.device_120_xiaoou);
                iconWithName.put(R.string.device_add_yingshi_camera, R.drawable.icon_bg_yingshi_c2c);
                iconWithName.put(R.string.device_add_p2p_camera, R.drawable.icon_bg_camera);
                break;
            case R.string.device_add_sensor:
                iconWithName.put(R.string.device_add_magnetometer, R.drawable.device_500_magnetic_window_and_door);
                iconWithName.put(R.string.device_add_human_body_sensor, R.drawable.device_500_human_body_infrared);
                iconWithName.put(R.string.device_add_smoke_sensor, R.drawable.device_500_hmsmoke);
                iconWithName.put(R.string.device_add_co_sensor, R.drawable.device_500_hmco);
                iconWithName.put(R.string.device_add_flammable_gas_sensor, R.drawable.device_500_hmburn);
                iconWithName.put(R.string.device_add_flooding_detector, R.drawable.device_500_hmwater);
                iconWithName.put(R.string.device_add_temperature_and_humidity_probe, R.drawable.device_500_hmtemp);
                iconWithName.put(R.string.device_add_emergency_button, R.drawable.device_500_hmbutton);
                break;
            case R.string.device_add_remote_control:
                iconWithName.put(R.string.device_add_xiaofang_tv, R.drawable.device_500_allone2);
                iconWithName.put(R.string.device_add_zigbee, R.drawable.device_500_allone);
                if (isCn)
                    iconWithName.put(R.string.device_add_zigbee_remote_control, R.drawable.device_500_intelligent_remote_controller);
                break;

            case R.string.device_add_switch:
                if (!isCn) {
                    iconWithName.put(R.string.device_add_zigbee_smart_switch,
                            R.drawable.device_120_us_switch);
                    iconWithName.put(R.string.device_add_zigbee_smart_outlet,
                            R.drawable.device_120_us_socket);
                    iconWithName.put(R.string.device_add_zigbee_scene_switch,
                            R.drawable.device_120_us_qingjing);
                    iconWithName.put(R.string.device_add_zigbee_dimmer_switch,
                            R.drawable.device_120_us_tiaoguang);
                }
                break;
        }
        if (!PhoneUtil.isCN(mContext)) {
            navigationBar.setCenterText(getString(typeId));
        } else {
            navigationBar.setCenterText(getString(R.string.add) + getString(typeId));
        }

        if (iconWithName.size() > 0)
            init();
    }

    private void init() {
        ListView deviceListView = (ListView) findViewById(R.id.deviceListView);
        deviceAddAdapter = new DeviceAddAdapter(iconWithName, R.string.device_add_socket);
        deviceListView.setAdapter(deviceAddAdapter);
        deviceListView.setOnItemClickListener(this);
        //  initQueryUnbinds();
        mUnbindDeviceTextView = (TextView) findViewById(R.id.unbindDeviceTextView);
        mUnbindDeviceTextView.setVisibility(View.GONE);
        mSearchWifiDevice = new SearchWifiDevice(this, mUnbindDeviceTextView);
        initUnBindTextView();
    }

    private void initUnBindTextView() {

        mDeviceQueryUnbinds = (ArrayList<DeviceQueryUnbind>) getIntent().getSerializableExtra(IntentKey.UNBIND_WIFI_DEVICE);
        if (mDeviceQueryUnbinds == null && mDeviceQueryUnbinds.isEmpty()) {
            mUnbindDeviceTextView.setVisibility(View.GONE);
            // mQueryUnbinds.queryAllWifiDevices(this, false);
            mSearchWifiDevice.searchWifiDevice(false);
        } else {
            mSearchWifiDevice.toBind(mDeviceQueryUnbinds);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String userName = UserCache.getCurrentUserName(mAppContext);
        int logoutStatus = UserCache.getLoginStatus(mAppContext, userName);
        int positionNameId = deviceAddAdapter.getItem(position);
        if (logoutStatus != LoginStatus.SUCCESS && logoutStatus != LoginStatus.FAIL) {
            if (positionNameId == R.string.device_add_coco
                    || positionNameId == R.string.device_add_s20c
                    || positionNameId == R.string.device_add_yidong
                    || positionNameId == R.string.device_add_feidiao_lincoln
                    || positionNameId == R.string.device_add_feidiao_xiaoe
                    || positionNameId == R.string.device_add_oujia
                    || positionNameId == R.string.device_add_liangba
                    || positionNameId == R.string.device_add_xiaofang_tv
                    || positionNameId == R.string.device_add_aoke_liangyi
                    || positionNameId == R.string.device_add_mairunclothes) {
                loginIntent = LoginIntent.SERVER;
                loginEntryString = Constant.COCO;
            } else {
                loginIntent = LoginIntent.ALL;
                loginEntryString = Constant.ViHome;
            }
            showLoginDialog();
        } else {
            if (DeviceTool.isWifiDevice(positionNameId)) {//COCO
                Intent intent;
                if (positionNameId == R.string.device_add_coco) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddDevice_COCOSmartSocket), null);
                }
                intent = new Intent(DeviceAddTwoPageActivity.this, ApConfig1Activity.class);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, positionNameId);
                startActivityForResult(intent, 1);
            } else if (positionNameId == R.string.device_add_yingshi_camera) {
                //萤石摄像机
                Intent intent = new Intent(DeviceAddTwoPageActivity.this, YsAdd1Activity.class);
                intent.putExtra(Constant.CONFIG_TITLE, positionNameId);
                startActivity(intent);
            } else if (positionNameId == R.string.xiao_ou_camera) {
                //小欧智能摄像机添加入口
//                Intent intent = new Intent(DeviceAddTwoPageActivity.this, DanaleAddFirstLevelPageActivity.class);
//                intent.putExtra(Constant.CONFIG_TITLE, positionNameId);
//                startActivity(intent);
            } else if (positionNameId == R.string.device_add_smoke_sensor || positionNameId == R.string.device_add_co_sensor
                    || positionNameId == R.string.device_add_flooding_detector || positionNameId == R.string.device_add_temperature_and_humidity_probe || positionNameId == R.string.device_add_emergency_button) {
                Intent intent = new Intent(DeviceAddTwoPageActivity.this, ZigBeeSensorDeviceAddActivity.class);
                intent.putExtra(Constant.CONFIG_TITLE, positionNameId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(DeviceAddTwoPageActivity.this, ZigBeeDeviceAddActivity.class);
                intent.putExtra(Constant.CONFIG_TITLE, positionNameId);
                startActivity(intent);
            }
        }
    }

    public void showLoginDialog() {
        DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
        String title = getString(R.string.login_now_title);
        dialogFragment.setTitle(title);
        dialogFragment.setLeftButtonText(getString(R.string.cancel));
        dialogFragment.setRightButtonText(getString(R.string.login));
        dialogFragment.setOnTwoButtonClickListener(DeviceAddTwoPageActivity.this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }


    @Override
    public void onLeftButtonClick(View view) {
    }

    @Override
    public void onRightButtonClick(View view) {
        Intent intent = new Intent(DeviceAddTwoPageActivity.this, LoginActivity.class);
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
}
