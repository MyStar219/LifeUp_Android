package com.orvibo.homemate.device.smartlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.util.List;

/**
 * 门锁成员列表
 * Created by snown on 2015/11/27
 */
public class LockMemberListActivity extends BaseActivity {

    private NavigationCocoBar navigationBar;
    private View emptyView;
    private ListView listView;
    private Device device;
    MemberSettingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_member_list);
        emptyView = findViewById(R.id.emptyView);
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        listView = (ListView) findViewById(R.id.listView);
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
    }

    @Override
    public void onResume() {
        List<DoorUserData> doorUserDatas = DoorUserDao.getInstance().getDoorUserList(device.getDeviceId());
        adapter = new MemberSettingAdapter(doorUserDatas);
        if (doorUserDatas != null && doorUserDatas.size() > 0) {
            emptyView.setVisibility(View.GONE);
            listView.setAdapter(adapter);
        } else {
            listView.setVisibility(View.GONE);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DoorUserData doorUserData = adapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(IntentKey.DOOR_USER_DATA, doorUserData);
                intent.putExtra(IntentKey.DEVICE, device);
                ActivityJumpUtil.jumpAct(LockMemberListActivity.this, LockMemberSetActivity.class, intent);
            }
        });
        super.onResume();
    }
}
