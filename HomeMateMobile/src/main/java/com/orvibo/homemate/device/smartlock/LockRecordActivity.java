package com.orvibo.homemate.device.smartlock;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.DoorLockRecordData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AuthUnlockDao;
import com.orvibo.homemate.dao.DoorLockRecordDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.DisplayUtils;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权开锁记录
 * Created by snown on 2015/11/27.
 */
public class LockRecordActivity extends BaseControlActivity {

    private NavigationGreenBar navigationGreenBar;
    private ListView listView;
    private View emptyView;
    private LinkedHashMap<String, List<DoorLockRecordData>> datas = new LinkedHashMap<>();
    private AuthUnlockData authUnlockData;
    private TextView btnText, btnText1, noRecordTip;
    private LockRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_record);
        this.btnText = (TextView) findViewById(R.id.btnText);
        this.btnText1 = (TextView) findViewById(R.id.btnText1);
        this.noRecordTip = (TextView) findViewById(R.id.noRecordTip);
        emptyView = findViewById(R.id.emptyView);
        this.listView = (ListView) findViewById(R.id.listView);

        this.navigationGreenBar = (NavigationGreenBar) findViewById(R.id.navigationGreenBar);

        if (ProductManage.isBLLock(device)) {
            Drawable topDrawable = getResources().getDrawable(R.drawable.pic_nomessage_baling_lock);
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            noRecordTip.setCompoundDrawables(null, topDrawable, null, null);
            if (ProductManage.isNeutralLock(device)) {
                findViewById(R.id.unlock).setVisibility(View.VISIBLE);
            }
        } else if (ProductManage.isYDLock(device)) {
            findViewById(R.id.unlock).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (payloadData != null && deviceId.equalsIgnoreCase(this.deviceId)) {
            if (authUnlockData != null && payloadData.getAuthorizedId() == authUnlockData.getAuthorizedId() && payloadData.getType() == DoorUserDao.TYPE_TMP_USER) {
                authUnlockData.setDelFlag(DeleteFlag.DELETED);
                AuthUnlockDao.getInstance().insertAuthUnlock(authUnlockData);
            }
            authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(deviceId);
            setData();
        }
    }

    public void rightTitleClick(View v) {
        Intent intent = new Intent(this, SetDeviceActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(deviceId);
        if (device != null) {
            navigationGreenBar.setText(device.getDeviceName());
        }
        setData();
    }

    private void setData() {
        datas = DoorLockRecordDao.getInstance().getShowRecord(deviceId);
        if (datas != null && datas.size() > 0) {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            navigationGreenBar.setRightTextVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new LockRecordAdapter(datas, this, deviceId);
                View view = getLayoutInflater().inflate(R.layout.intelligent_list_head_foot_item, null, false);
                view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DisplayUtils.dipToPx(mContext, 16)));
                listView.addFooterView(view, null, false);
                listView.setAdapter(adapter);
            } else {
                adapter.updateData(datas);
            }
        } else {
            navigationGreenBar.setRightTextColor(R.color.gray_white);
            navigationGreenBar.setRightTextVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        setBtnText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addTmpPass:
                ActivityTool.lockJump(LockRecordActivity.this, AuthUnlockDao.getInstance().getAvailableAuth(deviceId), device);
                break;
            case R.id.unlock:
                Intent intent = new Intent(LockRecordActivity.this, UnlockActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(deviceId);
        setBtnText();
    }

    /**
     * 设置底部按钮显示内容
     */
    private void setBtnText() {
        if (AuthUnlockDao.getInstance().isAvailableData(authUnlockData)) {
            btnText.setVisibility(View.GONE);
            btnText1.setVisibility(View.VISIBLE);
            btnText1.setText(String.format(getString(R.string.smart_lock_result_tip1), getTextInfo()));
        } else {
            btnText1.setVisibility(View.GONE);
            btnText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取展示内容
     *
     * @return
     */
    private String getTextInfo() {
        StringBuilder phoneInfo = new StringBuilder();
        String phoneName = null;
        String phone = authUnlockData.getPhone();
        DoorUserData doorUserData = DoorUserDao.getInstance().getDoorUser(authUnlockData.getDeviceId(), authUnlockData.getAuthorizedId());
        if (doorUserData != null) {
            phoneName = TextUtils.isEmpty(doorUserData.getName()) ? "" : doorUserData.getName();
        }
        if (TextUtils.isEmpty(phoneName)) {
            Map<String, String> hashPhone = SmartLockCache.getInstance().getPhoneInfo(UserCache.getCurrentMainUid(getApplicationContext()));
            phoneName = hashPhone.get(phone);
            if (TextUtils.isEmpty(phoneName)) {
                phoneName = PhoneUtil.getContactName(phone);
            }
        }
        phoneName = StringUtil.splitString(phoneName);
        phoneInfo.append(phoneName);
        return phoneInfo.toString();
    }
}
