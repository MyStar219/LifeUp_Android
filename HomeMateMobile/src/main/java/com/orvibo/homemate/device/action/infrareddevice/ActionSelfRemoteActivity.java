package com.orvibo.homemate.device.action.infrareddevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.device.action.BaseActionActivity;
import com.orvibo.homemate.device.control.infrareddevice.SelfRemoteButtonAdapter;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.List;

/**
 * Created by smagret on 2015/04/13
 */

public class ActionSelfRemoteActivity extends BaseActionActivity implements SelfRemoteButtonAdapter.OnActionSelectResultListener {

    private static final String TAG = ActionSelfRemoteActivity.class
            .getSimpleName();
    private NavigationGreenBar navigationBar;
    private List<DeviceIr> deviceIrs;
    private SelfRemoteButtonAdapter selfRemoteButtonAdapter;
    private GridView gridView;
    private View emptyView;
    private DeviceIrDao deviceIrDao;

    /**
     * 该按键是否被学习
     */
    private boolean IS_LEARNED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_self_remote);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        deviceIrDao = new DeviceIrDao();
        deviceIrs = deviceIrDao.selDeviceIrs(uid, deviceId);
        selfRemoteButtonAdapter = new SelfRemoteButtonAdapter(this, deviceIrs, SelfRemoteButtonAdapter.TYPE_ACTION);
        selfRemoteButtonAdapter.setOnActionSelectResultListener(this);
        gridView.setAdapter(selfRemoteButtonAdapter);
        selfRemoteButtonAdapter.refreshSelectBg(command);
        gridView.setEmptyView(emptyView);
    }

    private void initView() {
        navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        gridView = (GridView) findViewById(R.id.gridView);
        emptyView = findViewById(R.id.emptyView);
    }

    private void initData() {
        navigationBar.setRightText(getResources().getString(R.string.confirm));
        navigationBar.setRightTextVisibility(View.GONE);
    }

    public void rightTitleClick(View view) {
        if (IS_LEARNED) {
            Intent intent = new Intent();
            action = new Action(deviceId, command, 0, 0, 0, 0, keyName);
            Bundle bundle = new Bundle();
            bundle.putSerializable("action", action);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            this.finish();
            super.rightTitleClick(view);
        } else {
            String message = getResources().getString(R.string.select_learned_key_tips);
            ToastUtil.showToast(
                    message,
                    ToastType.ERROR, ToastType.SHORT);
        }
    }

    @Override
    public void onActionSelectResult(String keyName, String order, boolean is_learned) {
        IS_LEARNED = is_learned;
        if (is_learned) {
            this.command = order;
            this.keyName = keyName;
        } else {
            this.command = "";
            this.keyName = "";
        }
    }
}
