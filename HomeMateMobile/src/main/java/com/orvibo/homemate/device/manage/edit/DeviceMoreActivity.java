package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton.OnButtonClickListener;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import de.greenrobot.event.EventBus;


/**
 * Created by Smagret on 2016/3/17.
 */
public class DeviceMoreActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener, OnButtonClickListener {
    private final String TAG = DeviceInfoActivity.class.getSimpleName();

    private TextView deleteTextView;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_more_activity);
        init();
    }

    private void init() {
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        deleteTextView = (TextView) findViewById(R.id.deleteTextView);
        deleteTextView.setOnClickListener(this);
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.vicenter_delete_title));
        dialogFragmentTwoButton.setContent(getString(R.string.vicenter_delete_content));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    @Override
    public void onRightButtonClick(View view) {

    }

    @Override
    public void onLeftButtonClick(View view) {
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        showDialog();
        mDeleteDevice.deleteWifiDeviceOrGateway(device.getUid(), UserCache.getCurrentUserName(this));
    }

    DeleteDevice mDeleteDevice = new DeleteDevice(DeviceMoreActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                ToastUtil.showToast(R.string.device_delete_success, Toast.LENGTH_SHORT);
                toMainActivity();
            } else if (result == ErrorCode.OFFLINE_GATEWAY) {
                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                dialogFragmentOneButton.setTitle(getString(R.string.delete_success));
                dialogFragmentOneButton.setContent(getString(R.string.vicenter_delete_success_content));
                dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                dialogFragmentOneButton.setButtonTextColor(getResources().getColor(R.color.blue));
                dialogFragmentOneButton.setOnButtonClickListener(DeviceMoreActivity.this);
                dialogFragmentOneButton.show(getFragmentManager(), "");
            } else {
                ToastUtil.showToast(R.string.device_delete_failure, Toast.LENGTH_SHORT);
            }
        }
    };

    private void toMainActivity() {
        EventBus.getDefault().post(new MainEvent(BottomTabType.TWO_BOTTOM_TAB, true));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onButtonClick(View view) {
        MessageDao messageDao = new MessageDao();
        String userId = UserCache.getCurrentUserId(ViHomeApplication.getAppContext());
        messageDao.delSensorMessagesByUserId(userId);
        toMainActivity();
    }
}
