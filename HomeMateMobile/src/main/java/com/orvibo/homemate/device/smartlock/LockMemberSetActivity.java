package com.orvibo.homemate.device.smartlock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.MessagePush;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AuthUnlockDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.dao.MessagePushDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.MessagePushStatus;
import com.orvibo.homemate.data.MessagePushType;
import com.orvibo.homemate.device.manage.edit.DeviceNameActivity;
import com.orvibo.homemate.device.manage.edit.SensorMessageSettingActivity;
import com.orvibo.homemate.util.LockUtil;
import com.orvibo.homemate.util.MessagePushUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 门锁成员设置
 * Created by snown on 2015/11/27
 */
public class LockMemberSetActivity extends BaseActivity {


    private com.orvibo.homemate.view.custom.NavigationGreenBar nbTitle;
    private TextView name;
    private LinearLayout nameView, idView, timeValidityView;
    private TextView id;
    private TextView userType;
    private TextView modifyType, timeValidity, idName;
    private TextView backHomeTip;
    private LinearLayout backHomeView;
    private DoorUserData doorUserData;
    private Device device;
    private AuthUnlockData authUnlockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_member_set);
        doorUserData = (DoorUserData) getIntent().getSerializableExtra(IntentKey.DOOR_USER_DATA);
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        initView();
        initData();
    }

    private void initView() {
        this.backHomeView = (LinearLayout) findViewById(R.id.backHomeView);
        this.backHomeTip = (TextView) findViewById(R.id.backHomeTip);
        this.modifyType = (TextView) findViewById(R.id.modifyType);
        this.userType = (TextView) findViewById(R.id.userType);
        this.id = (TextView) findViewById(R.id.id);
        this.nameView = (LinearLayout) findViewById(R.id.nameView);
        this.idView = (LinearLayout) findViewById(R.id.idView);
        this.name = (TextView) findViewById(R.id.name);
        this.nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        this.timeValidityView = (LinearLayout) findViewById(R.id.timeValidityView);
        this.timeValidity = (TextView) findViewById(R.id.timeValidity);
        this.idName = (TextView) findViewById(R.id.idName);

    }

    private void initData() {
        String nameStr = doorUserData.getName();
        nbTitle.setText(TextUtils.isEmpty(nameStr) ? getString(R.string.linkage_action_no) : nameStr);
        name.setText(TextUtils.isEmpty(nameStr) ? getString(R.string.linkage_action_no) : nameStr);
        id.setText(String.valueOf(doorUserData.getAuthorizedId()));
        StringBuilder typeString = new StringBuilder();
        for (int type : doorUserData.getTypes()) {
            typeString.append(LockUtil.getLockType(type)).append("/");
        }
        if (doorUserData.getType() == DoorUserDao.TYPE_TMP_USER) {
            authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(device.getDeviceId());
            if (!PhoneUtil.isCN(mContext))
                modifyType.setTextSize(12);
            modifyType.setText(R.string.lock_type_tmp_pass_tip);
            timeValidityView.setVisibility(View.VISIBLE);
            idName.setText(R.string.user_phone_number);
            if (authUnlockData != null) {
                timeValidity.setText(TimeUtil.getStringDate1(authUnlockData.getCreateTime() + (long) authUnlockData.getTime() * 60 * 1000));
                id.setText(authUnlockData.getPhone());
            }
            if (!PhoneUtil.isCN(mContext) || ProductManage.isBLLock(device)) {
                idView.setVisibility(View.GONE);
            }
        } else {
            modifyType.setText(typeString.substring(0, typeString.lastIndexOf("/")));
        }
        userType.setText(LockUtil.getLockUserType(device, doorUserData.getType(), doorUserData.getAuthorizedId()));
        nameView.setOnClickListener(this);
        backHomeView.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            doorUserData = (DoorUserData) data.getSerializableExtra(IntentKey.DOOR_USER_DATA);
            name.setText(doorUserData.getName());
            nbTitle.setText(doorUserData.getName());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nameView:
                Intent intent = new Intent(this, DeviceNameActivity.class);
                intent.putExtra(IntentKey.DOOR_USER_DATA, doorUserData);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivityForResult(intent, 0);
                break;
            case R.id.backHomeView:
                Intent intent1 = new Intent(this, SensorMessageSettingActivity.class);
                intent1.putExtra(IntentKey.DEVICE, device);
                intent1.putExtra(IntentKey.DOOR_USER_DATA, doorUserData);
                startActivityForResult(intent1, 0);
                break;
        }
    }

    @Override
    public void onResume() {
        MessagePushDao messagePushDao = new MessagePushDao();
        MessagePush messagePush = messagePushDao.selLockMessagePush(device.getDeviceId(), doorUserData.getAuthorizedId());
        if (messagePush == null) {
            messagePush = new MessagePush();
            messagePush.setTaskId(device.getDeviceId());
            messagePush.setIsPush(MessagePushStatus.ON);
            messagePush.setType(MessagePushType.SINGLE_SENSOR_TYPE);
            messagePush.setStartTime("00:00:00");
            messagePush.setEndTime("00:00:00");
            messagePush.setWeek(255);
        }
        if (messagePush.getIsPush() == MessagePushStatus.OFF) {
            backHomeTip.setText(mContext.getString(R.string.device_timing_action_shutdown));
        } else if (messagePush.getStartTime().equals(messagePush.getEndTime())) {
            backHomeTip.setText(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MessagePushUtil.getTimeInterval(mContext, messagePush)).append("\n").append(WeekUtil.getWeeks(mContext, messagePush.getWeek()));
            backHomeTip.setText(stringBuilder.toString());
        }
        super.onResume();
    }
}
