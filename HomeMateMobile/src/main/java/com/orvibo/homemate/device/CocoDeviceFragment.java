package com.orvibo.homemate.device;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.google.zxing.client.android.CaptureActivity;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnPropertyReportListener;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.add.AddUnbindDeviceActivity;
import com.orvibo.homemate.device.manage.add.DeviceAddActivity;
import com.orvibo.homemate.messagepush.MessageActivity;
import com.orvibo.homemate.model.OOReport;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.InfoPushTopView;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */

public class CocoDeviceFragment extends BaseFragment implements View.OnClickListener,
        OnPropertyReportListener,
        OOReport.OnDeviceOOReportListener,
        NavigationCocoBar.OnRightClickListener {
    private String TAG = CocoDeviceFragment.class.getSimpleName();
    //
    private View emptyView;
    private GridView gridView;
    private NavigationCocoBar navigationCocoBar;
    private InfoPushTopView infoPushCountView;
    private View view;
    //    private List<Device> socketDevices;
    private DeviceAdapter mDeDeviceAdapter;
    public boolean showEmptyView = true;
    private DeviceDao deviceDao;
    private DeviceStatusDao deviceStatusDao;
    private MessageDao messageDao;
    private QueryUnbinds queryUnbinds;
    private boolean hasQueryUnbind = false;
    private DialogFragmentTwoButton dialogFragmentTwoButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.e(TAG, "onCreateView()");
        view = inflater.inflate(R.layout.device_fragment, container, false);
        initView();

        //query();
//        refreshInfoPushCount();
        return view;
    }

    private void initView() {
        navigationCocoBar = (NavigationCocoBar) view.findViewById(R.id.navigationBar);
        gridView = (GridView) view.findViewById(R.id.deviceGridView);
//        socketDevices = new ArrayList<>() ;
        //gridView.setOnItemClickListener(DeviceFragment.this);
        emptyView = view.findViewById(R.id.deviceEmptyLinearLayout);
        emptyView.findViewById(R.id.device_scanning).setOnClickListener(this);
        emptyView.findViewById(R.id.scanning_image).setOnClickListener(this);
        gridView.setEmptyView(emptyView);

        infoPushCountView = (InfoPushTopView) view.findViewById(R.id.infoPushCountView);
        infoPushCountView.setOnClickListener(this);

//        DeviceStatusChanged deviceStatusChanged = DeviceStatusChanged.getInstance(context.getApplicationContext());
//        deviceStatusChanged.setDeviceStatusChangedListener(this);
        deviceDao = new DeviceDao();
        deviceStatusDao = new DeviceStatusDao();
        messageDao = new MessageDao();
        PropertyReport.getInstance(mAppContext).registerPropertyReport(this);
        OOReport.getInstance(mAppContext).registerOOReport(this);
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.unbind_device_found));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.add));
        initQueryUnbinds();
    }

    private void initQueryUnbinds() {
        queryUnbinds = new QueryUnbinds(mAppContext) {
            @Override
            public void onQueryResult(final ArrayList<DeviceQueryUnbind> deviceQueryUnbinds, int serial, int result) {
                super.onQueryResult(deviceQueryUnbinds, serial, result);
                if (!isHidden() && result == ErrorCode.SUCCESS && deviceQueryUnbinds != null && deviceQueryUnbinds.size() != 0) {
                    dialogFragmentTwoButton.setContent(String.format(getString(R.string.unbind_device_found_content), deviceQueryUnbinds.size()));
                    dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                        @Override
                        public void onLeftButtonClick(View view) {

                        }

                        @Override
                        public void onRightButtonClick(View view) {
                            Intent intent = new Intent(mAppContext, AddUnbindDeviceActivity.class);
                            intent.putExtra(IntentKey.DEVICE, deviceQueryUnbinds);
                            startActivity(intent);
                        }
                    });
                    if (!dialogFragmentTwoButton.isHidden()) {
                        dialogFragmentTwoButton.show(getFragmentManager(), "");
                    }
                } else {
                    dialogFragmentTwoButton.dismiss();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        switch (vId) {
            case R.id.device_scanning:
            case R.id.scanning_image:
                StatService.trackCustomKVEvent(context, getString(R.string.MTAClick_MyDeviceBigAddBtn), null);
                Intent intent = new Intent(context, CaptureActivity.class);//xiongwenchao 增加跳转界面
                startActivity(intent);
                break;
            case R.id.infoPushCountView:
                StatService.trackCustomKVEvent(context, getString(R.string.MTAClick_AllMessage), null);
                Intent intent1 = new Intent(getActivity(), MessageActivity.class);
                startActivity(intent1);
                context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                break;
        }

    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        query();
        refreshInfoPushCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    public void query() {
        final String userName = UserCache.getCurrentUserName(context);
        final String userId = UserCache.getCurrentUserId(context);
        int loginStatus = UserCache.getLoginStatus(context, userName);
        LogUtil.d(TAG, "query()-userId:" + userId + ",loginStatus:" + loginStatus);
        if (!StringUtil.isEmpty(userId)) {
            new AsyncTask<Void, Void, List<Device>>() {
                @Override
                protected List<Device> doInBackground(Void... params) {
                    //使用userName的话切换账号时会搜索不到coco
                    return deviceDao.selWifiDevicesByUserId(userId);
                }

                @Override
                protected void onPostExecute(List<Device> devices) {
                    LogUtil.d(TAG, "query()-userId:" + userId + ",devices:" + devices);
                    if (mDeDeviceAdapter == null) {
                        mDeDeviceAdapter = new DeviceAdapter(context, devices);
                        gridView.setAdapter(mDeDeviceAdapter);
                    } else {
                        mDeDeviceAdapter.refresh(devices);
                    }
                    //   LogUtil.d(TAG, "gridView.getCount():" + gridView.getCount());
                    if (gridView.getCount() == 0) {
                        showEmptyView = true;
                        emptyView.setVisibility(View.VISIBLE);
                        if (!hasQueryUnbind && !isHidden()) {
                            queryUnbinds.queryCOCOs(mAppContext, false);
                            hasQueryUnbind = true;
                        }
                    } else {
                        showEmptyView = false;
                        emptyView.setVisibility(View.GONE);
                    }
                    refreshTopVisibility();
                }
            }.execute();
        } else {
            List<Device> devices = new ArrayList<Device>();
            if (mDeDeviceAdapter != null) {
                showEmptyView = true;
                mDeDeviceAdapter.refresh(devices);
            }
            if (!hasQueryUnbind && !isHidden()) {
                queryUnbinds.queryCOCOs(mAppContext, true);
                hasQueryUnbind = true;
            }
        }
        refreshTopVisibility();
    }

    private void refreshTopVisibility() {
        navigationCocoBar.setLeftTextViewVisibility(View.GONE);
        navigationCocoBar.setRightTextViewVisibility(View.VISIBLE);
        navigationCocoBar.setOnRightClickListener(this);
        if (showEmptyView) {
            infoPushCountView.setInfoPushInvisible();
        } else {
            infoPushCountView.setInfoPushVisible(getActivity());
        }
    }

    @Override
    public void onPropertyReport(String uid, String deviceId, int deviceType, int appDeviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        LogUtil.d(TAG, "onPropertyReport()-value1:" + value1);
        if (mDeDeviceAdapter != null) {
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
            mDeDeviceAdapter.refresh(uid, deviceStatus);
        }
    }

    @Override
    public void onDeviceOOReport(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "onDeviceOOReport()-uid:" + uid + ",online:" + online);
        if (mDeDeviceAdapter != null) {
            mDeDeviceAdapter.refreshOnline(uid, deviceId, online);
//            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//            if (null != deviceStatus) {
//                deviceStatus.setOnline(online); // 设置设备离线，在线状态
//                mDeDeviceAdapter.refresh(uid, deviceStatus);
//            }
        }
    }

//    @Override
//    public void onOOReport(String uid, String extAddr, int ooStatus) {
//        LogUtil.d(TAG, "onOOReport()-uid:" + uid + ",ooStatus:" + ooStatus);
//        if (mDeDeviceAdapter != null) {
//            String deviceId = deviceDao.getDeviceIdByUid(uid);
//            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//            if (null != deviceStatus) {
//                deviceStatus.setOnline(ooStatus); // 设置设备离线，在线状态
//                mDeDeviceAdapter.refresh(uid, deviceStatus);
//            }
//        }
//    }

    @Override
    public void onRightClick(View v) {
        StatService.trackCustomKVEvent(context, getString(R.string.MTAClick_MyDeviceSmallAddBtn), null);
        Intent intent = new Intent(context, DeviceAddActivity.class);
        context.startActivity(intent);
    }

    private void refreshInfoPushCount() {
//        int infoPushCount = InfoPushCountCache.getInfoPushCount(context,UserCache.getCurrentUserId(context));
        int infoPushCount = messageDao.selUnreadCount(UserCache.getCurrentUserId(ViHomeApplication.getAppContext()));
        LogUtil.d(TAG, "refreshInfoPushCount() - infoPushCount = " + infoPushCount);
        if (infoPushCount > 0) {
            infoPushCountView.setInfoPushCountVisible();
        } else {
            infoPushCountView.setInfoPushCountInvisible();
        }
        infoPushCountView.setInfoPushcount(infoPushCount);
    }

    @Override
    public void onDestroy() {
        if (context != null) {
            Context appContext = context.getApplicationContext();
            PropertyReport.getInstance(appContext).unregisterPropertyReport(this);
            OOReport.getInstance(appContext).removeOOReport(this);
        }
        super.onDestroy();
    }

}
