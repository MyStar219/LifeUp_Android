package com.orvibo.homemate.device.manage.add;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.device.searchdevice.SearchWifiDevice;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

import java.util.ArrayList;

public class DeviceAddActivity extends BaseActivity implements DialogFragmentTwoButton.OnTwoButtonClickListener,
        NavigationCocoBar.OnLeftClickListener {
    private static final String TAG = DeviceAddActivity.class.getSimpleName();
    private TextView unbindDeviceTextView;
    private QueryUnbinds queryUnbinds;
    private ArrayList<DeviceQueryUnbind> mDeviceQueryUnbinds = new ArrayList<DeviceQueryUnbind>();
    private SearchWifiDevice mSearchWifiDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_add_activity);
        init();
        initDate();
    }

    private void initDate() {
        Intent data = getIntent();
        if (data != null) {
            boolean isError = data.getBooleanExtra(IntentKey.BACKGROUND_MUSIC_SCAN_ERROR, false);
            if (isError) {
                showErrorDialog(getString(R.string.qr_scanning_failed), getString(R.string.qr_scanning_failed_backgroundmusic_tip));
            }
        }
    }

    private void init() {
        NavigationCocoBar navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        findViewById(R.id.deviceScanningAdd).setOnClickListener(this);
        findViewById(R.id.imageQr).setOnClickListener(this);
        findViewById(R.id.btnDeviceList).setOnClickListener(this);
        unbindDeviceTextView = (TextView) findViewById(R.id.unbindDeviceTextView);
        //  initQueryUnbinds();
        mSearchWifiDevice = new SearchWifiDevice(this, unbindDeviceTextView);

    }



    @Override
    protected void onResume() {
        super.onResume();
        mDeviceQueryUnbinds.clear();
        unbindDeviceTextView.setVisibility(View.GONE);
        int logoutStatus = UserCache.getLoginStatus(mAppContext, userName);
        if (logoutStatus == LoginStatus.SUCCESS) {
            // queryUnbinds.queryAllWifiDevices(mAppContext, false);
           // queryUnbinds.queryAllWifiDevices(mAppContext, false);
            mSearchWifiDevice.searchWifiDevice(false);
        } else {
           // queryUnbinds.queryAllWifiDevices(mAppContext, true);
            mSearchWifiDevice.searchWifiDevice(true);
        }
    }


    public void showLoginDialog() {
        DialogFragmentTwoButton dialogFragment = new DialogFragmentTwoButton();
        String title = getString(R.string.login_now_title);
        dialogFragment.setTitle(title);
        dialogFragment.setLeftButtonText(getString(R.string.cancel));
        dialogFragment.setRightButtonText(getString(R.string.login));
        dialogFragment.setOnTwoButtonClickListener(DeviceAddActivity.this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        dialogFragment.show(transaction, getClass().getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageQr:
            case R.id.deviceScanningAdd:
                if (!PhoneUtil.isCN(DeviceAddActivity.this)) {
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.btnDeviceList:
                ActivityJumpUtil.jumpAct(this, DeviceAddListActivity.class);
                break;
        }
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

    /**
     * @param title
     * @param content
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
}
