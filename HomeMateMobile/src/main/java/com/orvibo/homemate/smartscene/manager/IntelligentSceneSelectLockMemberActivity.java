package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.data.AuthorizedIdType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.view.popup.SelectLockMemberPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 联动选择门锁的授权ID
 * Created by Smagret on 2016/4/27.
 */
public class IntelligentSceneSelectLockMemberActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = IntelligentSceneSelectLockMemberActivity.class.getSimpleName();

    private RelativeLayout         allUsersRelativeLayout;
    private RelativeLayout         customUserRelativeLayout;
    private RelativeLayout         keyUnlockRelativeLayout;
    private List<LinkageCondition> linkageConditions;
    private RadioButton            allMemberRadioButton;
    private RadioButton            customMemberRadioButton;
    private ImageView              keyUnlockImageView;
    private TextView               customTextView;
    private SelectLockMemberPopup  mSelectLockMemberPopup;
    private List<DoorUserData>     doorUserDatas;
    private DeviceDao                 mDeviceDao;
    private List<Integer> authorizedIds = new ArrayList<>();
    /**
     * 钥匙开锁执行场景 true:表示钥匙开锁后执行联动场景；false 表示不执行
     */
    private boolean       keyUnlock     = true;

    /**
     * 钥匙开锁执行场景 true:选择所有人；false 表示自定义
     */
    private boolean allMember = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intelligent_scene_select_userid_activity);
        Serializable serializable = getIntent().getSerializableExtra(Constant.LINKAGE_CONDITIONS);
        linkageConditions = (List<LinkageCondition>) serializable;
        initView();
        initListener();
        initSelectedLockMumber();
        initData();
        initSelectLockMemberPopup();
        initUser();

    }

    private void initView() {
        allUsersRelativeLayout = (RelativeLayout) findViewById(R.id.allUsersRelativeLayout);
        customUserRelativeLayout = (RelativeLayout) findViewById(R.id.customUserRelativeLayout);
        keyUnlockRelativeLayout = (RelativeLayout) findViewById(R.id.keyUnlockRelativeLayout);
        allMemberRadioButton = (RadioButton) findViewById(R.id.allMemberRadioButton);
        customMemberRadioButton = (RadioButton) findViewById(R.id.customMemberRadioButton);
        keyUnlockImageView = (ImageView) findViewById(R.id.keyUnlockImageView);
        customTextView = (TextView) findViewById(R.id.customTextView);

    }

    private void initListener() {
        allUsersRelativeLayout.setOnClickListener(this);
        customUserRelativeLayout.setOnClickListener(this);
        keyUnlockRelativeLayout.setOnClickListener(this);
    }

    private void initData() {
        mDeviceDao = new DeviceDao();
        String deviceId = IntelligentSceneTool.getDeviceId(linkageConditions);
        Device device = new DeviceDao().selDevice(deviceId);
        doorUserDatas = DoorUserDao.getInstance().getDoorUserList(device.getDeviceId());
        if (ProductManage.isHTLLock(device)) {
            keyUnlockRelativeLayout.setVisibility(View.GONE);
        } else if (ProductManage.isBLLock(device)) {
            keyUnlockRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initSelectedLockMumber() {
        authorizedIds = IntelligentSceneTool.getSelectedLockMumber(linkageConditions);

        if (authorizedIds.contains(AuthorizedIdType.KEY_UNLOCK)) {
            keyUnlock = true;
        } else {
            keyUnlock = false;
        }

        if (authorizedIds.contains(AuthorizedIdType.ALL_MEMBER)) {
            allMember = true;
        } else {
            allMember = false;
        }

        if (keyUnlock) {
            keyUnlockImageView.setBackgroundResource(R.drawable.device_on);
        } else {
            keyUnlockImageView.setBackgroundResource(R.drawable.device_off);
        }

        if (allMember) {
            allMemberRadioButton.setChecked(true);
            customMemberRadioButton.setChecked(false);
        } else {
            allMemberRadioButton.setChecked(false);
            customMemberRadioButton.setChecked(true);
        }
    }

    private void initSelectLockMemberPopup() {
        mSelectLockMemberPopup = new SelectLockMemberPopup() {
            @Override
            public void confirmClicked(List<Integer> checkedAuthorizedIds) {
                if (checkedAuthorizedIds == null ||
                        checkedAuthorizedIds != null && checkedAuthorizedIds.size() == 0
                        || checkedAuthorizedIds != null && checkedAuthorizedIds.size() == 1
                        && checkedAuthorizedIds.contains(AuthorizedIdType.KEY_UNLOCK)) {
                    resetAuthorizedIds(authorizedIds);
                    authorizedIds.add(AuthorizedIdType.ALL_MEMBER);
                    allMemberRadioButton.setChecked(true);
                    customMemberRadioButton.setChecked(false);
                } else {
                    authorizedIds.clear();
                    authorizedIds.addAll(checkedAuthorizedIds);

                    allMemberRadioButton.setChecked(false);
                    customMemberRadioButton.setChecked(true);
                }
                initUser();
            }

            @Override
            public void cancelClicked() {

            }
        };
    }

    /**
     * 汇泰龙门锁，设置成员
     */
    private void initUser() {
        Collections.sort(authorizedIds, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return new Integer(integer).compareTo(new Integer(t1));
            }
        });
        linkageConditions = IntelligentSceneTool.setSelectedLockMumber(linkageConditions, authorizedIds);

        String user = IntelligentSceneTool.getSelectedLockMumber(mContext, linkageConditions);
        if (user.equals(mContext.getResources().getString(R.string.intelligent_scene_all_users))) {
            customTextView.setVisibility(View.GONE);
        } else {
            customTextView.setVisibility(View.VISIBLE);
        }
        customTextView.setText(user);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.allUsersRelativeLayout:
                allMemberRadioButton.setChecked(true);
                customMemberRadioButton.setChecked(false);
                resetAuthorizedIds(authorizedIds);
                authorizedIds.add(AuthorizedIdType.ALL_MEMBER);
                break;
            case R.id.customUserRelativeLayout:
                if (authorizedIds.contains(AuthorizedIdType.ALL_MEMBER)) {
                    authorizedIds.remove(Integer.valueOf(AuthorizedIdType.ALL_MEMBER));
                }
                mSelectLockMemberPopup.showPopup(IntelligentSceneSelectLockMemberActivity.this, doorUserDatas, authorizedIds);
                break;
            case R.id.keyUnlockRelativeLayout:
                if (authorizedIds.contains(AuthorizedIdType.KEY_UNLOCK)) {
                    keyUnlockImageView.setBackgroundResource(R.drawable.device_off);
                    authorizedIds.remove(Integer.valueOf(AuthorizedIdType.KEY_UNLOCK));
                } else {
                    keyUnlockImageView.setBackgroundResource(R.drawable.device_on);
                    authorizedIds.add(AuthorizedIdType.KEY_UNLOCK);
                }
                break;
        }
    }

    /**
     * 重置自定义授权ID
     */
    private void resetAuthorizedIds(List<Integer> authorizedIds) {
        Iterator<Integer> iterator = authorizedIds.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= AuthorizedIdType.ALL_MEMBER) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Collections.sort(authorizedIds, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return new Integer(integer).compareTo(new Integer(t1));
            }
        });
        linkageConditions = IntelligentSceneTool.setSelectedLockMumber(linkageConditions, authorizedIds);

        Intent intent = new Intent();
        intent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
