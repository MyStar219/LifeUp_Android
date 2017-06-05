package com.orvibo.homemate.messagepush;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnNewPropertyReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.StatusRecord;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.StatusRecordDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.StatusRecordRequest;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.PinnedSectionListView;
import com.orvibo.homemate.view.custom.pulltorefresh.ErrorMaskView;
import com.orvibo.homemate.view.custom.pulltorefresh.PullListMaskController;
import com.orvibo.homemate.view.custom.pulltorefresh.PullRefreshView;
import com.tencent.stat.StatService;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by baoqi on 2016/7/6.
 */

/**
 * 传感器状态记录
 * 每次打开这个界面，请求的是最新的20条数据(读取的数据条数，默认20条，最多50条)
 */
public class SensorStatusRecordActivity extends BaseActivity implements OnNewPropertyReportListener {
    private final static String TAG = SensorStatusRecordActivity.class.getSimpleName();
    private NavigationGreenBar mNavigationGreenBar;
    private PinnedSectionListView mPinnedSectionListView;
    private PullListMaskController mViewController;
    private StatusRecordDao mStatusRecordDao;
    private LinkedHashMap<String, List<StatusRecord>> mStatusRecords;
    private Device mDevice;
    private String deviceId;
    private SensorStatusRecordAdapter mSensorStatusRecordAdapter;
    private View emptyView;
    private StatusRecordRequest mStatusRecordRequest;
    private String uid;
    //是否是下拉刷新
    private boolean isPullUpRefresh = true;
    //是否还有数据
    private boolean hasMoreDate = true;
    private StatusRecord mLatestStatusRecord;
    private boolean deleteAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_status_record);
        initView();
        initDate();
        initEvent();


    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        //listView 的下拉加载最新的数据
        mPinnedSectionListView.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (NetUtil.isNetworkEnable(SensorStatusRecordActivity.this)) {
                    if (mDevice != null) {
                        requestLatesStatusRecord();
                    }

                } else {
                    mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_FAIL);
                }

            }
        });

        /**
         * 上拉加载下一页
         */
        mPinnedSectionListView.setOnClickFootViewListener(new PullRefreshView.OnClickFootViewListener() {
            @Override
            public void onClickFootView() {
                isPullUpRefresh = false;
                if (mDevice != null) {
                    if (hasMoreDate && !deleteAll) {
                        StatusRecord delstatusRecord = mStatusRecordDao.selDelOldestStatusRecordByDeviceId(mDevice.getDeviceId());
                        StatusRecord oldstatusRecord = mStatusRecordDao.selOldestStatusRecordByDeviceId(mDevice.getDeviceId());
                        if (delstatusRecord != null && oldstatusRecord != null && oldstatusRecord.getSequence() > delstatusRecord.getSequence()) {//有删除记录，并且有更多的状态记录
                            LogUtil.d(TAG, "PullRefreshView.OnClickFootViewListener：onClickFootView()" + "：加载更多的数据" + ",form sequence=" + oldstatusRecord.getSequence() + " to sequence=" + delstatusRecord.getSequence());
                            mStatusRecordRequest.startStatusRecordRequest(currentMainUid, userName, mDevice.getDeviceId(), oldstatusRecord.getSequence(), delstatusRecord.getSequence(), 20);
                        } else if (delstatusRecord == null && oldstatusRecord != null) {//没有删除记录
                            LogUtil.d(TAG, "PullRefreshView.OnClickFootViewListener：onClickFootView()" + "：加载更多的数据" + ",form sequence=" + oldstatusRecord.getSequence());
                            mStatusRecordRequest.startStatusRecordRequest(currentMainUid, userName, mDevice.getDeviceId(), oldstatusRecord.getSequence(), -1, 20);
                        } else {
                            mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                        }
                    } else {
                        refresh();
                        mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                        //hideFootView();
                    }
                }
            }
        });
        /**
         * 监听属性报告,实时更新记录
         */
        PropertyReport.getInstance(mAppContext).registerNewPropertyReport(this);

    }

    /**
     * 加载最新的数据
     */
    private void requestLatesStatusRecord() {
        isPullUpRefresh = true;
        //查找数据库中最新的一条数据
        StatusRecord statusRecord = mStatusRecordDao.selLatestStatusRecordByDeviceId(mDevice.getDeviceId());
        if (statusRecord != null) {
            LogUtil.d(TAG, "PullRefreshView.OnRefreshListener：onRefresh()" + "：加载最新的数据" + ",sequence=" + statusRecord.getSequence());
            mStatusRecordRequest.startStatusRecordRequest(currentMainUid, userName, mDevice.getDeviceId(), -1, statusRecord.getSequence(), 20);
        }
    }

    /**
     * 初始化数据
     */
    private void initDate() {

        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            mDevice = (Device) serializable;
            deviceId = mDevice.getDeviceId();
        }


       /* //假数据
        mStatusRecords = new ArrayList<StatusRecord>();
        for (int i = 0; i < 7; i++) {
            StatusRecord statusRecord_n = new StatusRecord();
            statusRecord_n.setStatusRecord(false);
            mStatusRecords.add(statusRecord_n);
            for (int j = 0; j < 14; j++) {
                StatusRecord statusRecord_y = new StatusRecord();
                statusRecord_y.setStatusRecord(true);
                mStatusRecords.add(statusRecord_y);
            }
        }*/
        refresh();
        initStatusRecordRequest();


    }

    /**
     * 发送指令请求数据
     */
    private void initStatusRecordRequest() {
        mLatestStatusRecord = mStatusRecordDao.selLatestStatusRecordByDeviceId(deviceId);
        mStatusRecordRequest = new StatusRecordRequest(mAppContext) {
            @Override
            public void onStatusRecordRequestResult(int result, List<StatusRecord> statusRecords) {
                LogUtil.d(TAG, "StatusRecordRequest--onStatusRecordRequestResult():result=" + result);
                //mPinnedSectionListView.onRefreshComplete();
                stopProgress();
                if (result == ErrorCode.SUCCESS) {
                    if (!isPullUpRefresh && statusRecords != null) {
                        //如果请求回来的数据小于20，说明没有更多的数据了
                        if (statusRecords.size() < 20) {
                            mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                            hasMoreDate = false;
                            //hideFootView();
                        } else {
                            mViewController.showViewStatus(PullListMaskController.ListViewState.PULL_DOWN_LIST_HAS_MORE);
                        }
                    } else {
                        mViewController.showViewStatus(PullListMaskController.ListViewState.PULL_DOWN_LIST_HAS_MORE);
                    }
                } else {
                    mViewController.showViewStatus(PullListMaskController.ListViewState.PULL_DOWN_LIST_NO_MORE);
                }
                refresh();
            }
        };
        if (mDevice != null) {
            if (mLatestStatusRecord == null) {
                showProgress();
                //第一次请求数据
                mStatusRecordRequest.startStatusRecordRequest(currentMainUid, userName, mDevice.getDeviceId(), -1, -1, 20);
            } else {
                //再次进入界面
                LogUtil.d(TAG, "initStatusRecordRequest(),请求最新的状态记录：sequence=" + mLatestStatusRecord.getSequence());
                mStatusRecordRequest.startStatusRecordRequest(currentMainUid, userName, mDevice.getDeviceId(), -1, mLatestStatusRecord.getSequence(), 20);

            }
        }
    }

/*    private void hideFootView() {
        mPinnedSectionListView.setFootViewAddMore(false, false, false);
    }*/

    /**
     * 初始化控件对象
     */
    private void initView() {
        mNavigationGreenBar = (NavigationGreenBar) findViewById(R.id.statusRecordNavigationGreenBar);
        mNavigationGreenBar.setText(getString(R.string.message_sensor_record));
        mPinnedSectionListView = (PinnedSectionListView) findViewById(R.id.statusRecordListView);
        mPinnedSectionListView.setPadding(0, 0, 0, 100);
        ErrorMaskView maskView = (ErrorMaskView) findViewById(R.id.maskView);
        mViewController = new PullListMaskController(mPinnedSectionListView, maskView);
        //防止数据没有满屏的时候显示加载更多
        mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
        //让底部的进度显示出来
        // mPinnedSectionListView.setFootViewLoading();
        mStatusRecordDao = StatusRecordDao.getInstance();
        emptyView = LayoutInflater.from(mContext).inflate(
                R.layout.empty_message_view, null);
    }

    private void refresh() {
        if (mDevice != null) {
            mStatusRecords = mStatusRecordDao.getShowRecord(deviceId);

            if (mSensorStatusRecordAdapter == null) {
                mSensorStatusRecordAdapter = new SensorStatusRecordAdapter(mContext, mDevice, mStatusRecords);
                mPinnedSectionListView.setAdapter(mSensorStatusRecordAdapter);
            } else {
                mSensorStatusRecordAdapter.setDate(mStatusRecords);
                mSensorStatusRecordAdapter.notifyDataSetChanged();

            }

            if (mStatusRecords == null || mStatusRecords.size() == 0) {
                ((ViewGroup) mPinnedSectionListView.getParent()).removeView(emptyView);
                ((ViewGroup) mPinnedSectionListView.getParent()).addView(emptyView);
                mPinnedSectionListView.setEmptyView(emptyView);
                mNavigationGreenBar.setRightTextVisibility(View.GONE);
            } else {
                mNavigationGreenBar.setRightTextVisibility(View.VISIBLE);
                if (emptyView != null) {
                    ((ViewGroup) mPinnedSectionListView.getParent()).removeView(emptyView);
                }
            }
        }
    }

    public void rightTitleClick(View v) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(getString(R.string.message_clear_confirm));
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.message_clear));
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
        dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    @Override
    public void onLeftButtonClick(View view) {
        deleteAll = true;
        mStatusRecordDao.delStatusRecordByDeviceId(deviceId);
        refresh();
    }

    @Override
    public void onRightButtonClick(View view) {

    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AllMessage_Back), null);
        super.onBackPressed();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStatusRecordRequest != null) {
            mStatusRecordRequest.stopQueryRecord();
        }
        PropertyReport.getInstance(mAppContext).unregisterNewPropertyReport(this);
    }

    @Override
    public void onNewPropertyReport(Device device, DeviceStatus deviceStatus, PayloadData payloadData) {
        if (mDevice != null && device != null) {
            if (device.getDeviceId().equals(mDevice.getDeviceId())) {
                requestLatesStatusRecord();
            }
        }
    }
}
