package com.orvibo.homemate.room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;


/**
 * @author Smagret
 * @data 2015/03/28
 * @deprecated 旧楼层房间管理列表交互，已不再使用。
 */
public class AllHomeActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = AllHomeActivity.class
            .getSimpleName();

    private ListView listViewHome;
    private Context mContext;
    private String userName;
    private String uid;
    //    private List<String> uids;
//    private HomeAdapter homeAdapter;
    private CheckBox checkBoxSelectHome;
    private final int RENAME_HOME_NAME = 5;
    private GatewayDao gatewayDao;
    private SavaPopup savaPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_home);
        init();
        initView();
        initListener();
        initData();
    }

    private void init() {
        mContext = AllHomeActivity.this;
        gatewayDao = new GatewayDao();
        savaPopup = new SavaPopup();
        initView();
        initListener();
    }

    private void initView() {
        listViewHome = (ListView) findViewById(R.id.listViewHome);
        checkBoxSelectHome = (CheckBox) findViewById(R.id.checkBoxSelectHome);

    }

    private void initListener() {
        listViewHome.setOnItemClickListener(this);
    }

    public void selectHome(View v) {
        String uid = (String) v.getTag(R.id.checkBoxSelectHome);
        selectHome(uid);
        currentMainUid = uid;
//        if (homeAdapter != null) {
//            homeAdapter.refresh(currentMainUid);
//        }
        //TODO 这个版本先屏蔽
//        uid = (String) v.getTag(R.id.checkBoxSelectHome);
//        if (((CheckBox) v).isChecked()) {
//            ((CheckBox) v).setChecked(false);
//        } else {
//            savaPopup.showPopup(AllHomeActivity.this, getResources()
//                    .getString(R.string.home_change_tips), getResources()
//                    .getString(R.string.save), getResources()
//                    .getString(R.string.not_save));
//
//        }
    }

    private void selectHome(String uid) {
        UserCache.setCurrentMainUid(mContext, uid);
        UserCache.saveLastLoginGateway(mContext, uid);
    }

    public void editHomeName(View v) {
        //TODO 这个版本先屏蔽
//        uid = (String) v.getTag(R.id.imageViewEdit);
//        Intent intent = new Intent();
//        intent.setClass(mContext, RenameRoomNameActivity.class);
//        intent.putExtra("uid", uid);
//        startActivityForResult(intent, RENAME_HOME_NAME);
    }

    public void ToNextActivity(View v) {
//        String[] contentDescription = v.getContentDescription().toString()
//                .split("\\|");
//        uid = contentDescription[0];
//        selectHome(uid);
//        currentMainUid = uid;
//        if (homeAdapter != null) {
//            homeAdapter.refresh(currentMainUid);
//        }
//        int floorNo = Integer.valueOf(contentDescription[1]);
//        Intent intent = new Intent();
//        if (floorNo > 0) {
//            intent.setClass(mContext, SetFloorAndRoomActivity.class);
//        } else {
//            intent.setClass(mContext, RoomManagerActivity.class);
//        }
//        intent.putExtra("uid", uid);
//        startActivity(intent);
    }

    private void initData() {
        userName = UserCache.getCurrentUserName(mContext);
//        uids = UserCache.getMainUids(mContext, userName);
//        homeAdapter = new HomeAdapter(mContext, uids, currentMainUid);
//        listViewHome.setAdapter(homeAdapter);

//        if (uids.size() == 1) {
        Intent intent = new Intent();
        int floorNo = new FloorDao().selFloorNo(currentMainUid);
        if (floorNo > 0) {
            intent.setClass(mContext, SetFloorAndRoomActivity.class);
        } else {
            intent.setClass(mContext, SetFloorAndRoomActivity.class);
        }
        intent.putExtra("uid", currentMainUid);
        startActivity(intent);
        finish();
//        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ToNextActivity(view);
    }

    private class SavaPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            checkBoxSelectHome.setChecked(true);
            UserCache.saveMainUid(mContext, userName, uid);
        }
    }

    /**
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * <p/>
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        switch (requestCode) {
            case RENAME_HOME_NAME:
//                if (data != null) {
//                    result = data.getExtras().getString("homeName");
//                    gatewayDao.selGatewayByUid(uid).setHomeName(result);
//                    homeAdapter.notifyDataSetChanged();
//                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
