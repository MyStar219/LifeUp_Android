package com.orvibo.homemate.device.searchdevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.core.product.WifiFlag;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.add.AddUnbindDeviceActivity;
import com.orvibo.homemate.device.manage.add.AddUnbindHubActivity;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.model.gateway.OnSearchNewHubListener;
import com.orvibo.homemate.model.gateway.SearchNewHub;
import com.orvibo.homemate.model.gateway.SearchNewHubResult;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangqiyao on 2016/7/9 16:13.
 * 搜索设备。包括wifi设备和主机
 *
 * @version v1.9
 */
public class SearchDevice implements OnSearchNewHubListener {
    private static final String TAG = "SearchDevice";
    private static final int WHAT_SEARCH_HUB = 1;
    private static final int WHAT_SEARCH_WIFI_DEVICE = 2;
    private Context mContext;
    private BaseActivity mActivity;
    private Resources mResources;

    private SearchNewHub mSearchNewHub;
    private QueryUnbinds queryUnbinds;
    private boolean isSearchWifiDevice;

    private DialogFragmentTwoButton mHubDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SEARCH_HUB:
                    mSearchNewHub.search();
                    break;
                case WHAT_SEARCH_WIFI_DEVICE:
                    queryUnbinds.queryAllWifiDevices(mContext, false);
                    break;
            }
        }
    };

    public SearchDevice(BaseActivity activity) {
        mActivity = activity;
        mResources = activity.getResources();
        mContext = activity.getApplicationContext();
    }

    /**
     * 搜索主机
     *
     * @param searchWifiDevice true搜索主机结束后如果没有发现主机则搜索wifi设备
     */
    public void searchHub(boolean searchWifiDevice) {
        LogUtil.d(TAG, "searchHub()-searchWifiDevice:" + searchWifiDevice);
        isSearchWifiDevice = searchWifiDevice;
        if (mSearchNewHub == null) {
            mSearchNewHub = new SearchNewHub(mContext);
        }
        mSearchNewHub.setOnSearchNewHubListener(this);
        mHandler.sendEmptyMessage(WHAT_SEARCH_HUB);
    }

    @Override
    public final void onSearchNewHubs(List<SearchNewHubResult.HubResult> results) {
        //搜索绑定主机结果
        //无论搜索到多少主机，都显示一个主机。因为一个账号只能绑定一个主机
        //  int size = results.size();
        int size = 1;
        SearchNewHubResult.HubResult hubResult = results.get(0);
        int wifiFlag = ProductManage.getInstance().getWifiFlagByModel(hubResult.model);
        String content = null;
        if (wifiFlag == WifiFlag.HUB) {
            content = String.format(mResources.getString(R.string.unbind_device_found_hub_content), size);
            if (size > 1) {
                content = String.format(mResources.getString(R.string.unbind_device_found_hubs_content), size);
            }
        } else {
            content = String.format(mResources.getString(R.string.unbind_device_found_mini_hub_content), size);
        }
        mHubDialog = showDialogFragment(content, mResources.getString(R.string.cancel), mResources.getString(R.string.add), getToHubIntent(hubResult));
    }

    @Override
    public final void onSearchNewHub(SearchNewHubResult.HubResult result) {

    }

    @Override
    public final void onSearchNewHubFail(int result) {
        if (result == ErrorCode.HUBS_NOT_FOUND && isSearchWifiDevice) {
            //没有搜索到可以绑定的主机，接着搜索可绑定的wifi设备
            searchWifiDevice();
        }
    }

    @Override
    public final void onLoginSuccess(String uid) {
        //登录主机成功，读取主机数据
        LoadUtil.noticeLoadHubData(uid, Constant.INVALID_NUM);
    }

    /**
     * 搜索wifi设备
     */
    public void searchWifiDevice() {
        LogUtil.d(TAG, "searchWifiDevice()");
        initQueryUnbinds();
        mHandler.sendEmptyMessage(WHAT_SEARCH_WIFI_DEVICE);
    }

    @SuppressLint("StringFormatMatches")
    private void initQueryUnbinds() {
        if (queryUnbinds == null) {
            queryUnbinds = new QueryUnbinds(mContext) {
                @Override
                public void onQueryResult(final ArrayList<DeviceQueryUnbind> deviceQueryUnbinds, int serial, int result) {
                    super.onQueryResult(deviceQueryUnbinds, serial, result);
                    //筛选wifi设备
                    final ArrayList<DeviceQueryUnbind> wifiDeviceQueryUnbinds = new ArrayList<>();
                    if (deviceQueryUnbinds != null && !deviceQueryUnbinds.isEmpty()) {
                        for (DeviceQueryUnbind deviceQueryUnbind : deviceQueryUnbinds) {
                            if (ProductManage.getInstance().isWifiDevice(deviceQueryUnbind)) {
                                wifiDeviceQueryUnbinds.add(deviceQueryUnbind);
                            } else {
                                continue;
                            }
                        }
                    }
                    if (mActivity != null && mActivity.isVisible() && result == ErrorCode.SUCCESS && deviceQueryUnbinds != null && wifiDeviceQueryUnbinds.size() != 0) {
                        //没有加上单复数
                        if (wifiDeviceQueryUnbinds.size() == 1) {
                            //一个wifi设备
                            String wifiDeviceName = DeviceTool.getWifiDeviceName(wifiDeviceQueryUnbinds.get(0));
                            String content = String.format(mContext.getString(R.string.unbind_device_found_content), wifiDeviceName);
                            //  String content = String.format(mResources.getString(R.string.unbind_device_found_content), wifiDeviceQueryUnbinds.size());
                            showDialogFragment(content, mResources.getString(R.string.cancel), mResources.getString(R.string.add), getToWifiDeviceIntent(wifiDeviceQueryUnbinds));

                        } else {
                            //多个wifi设备

                            String content = String.format(mResources.getString(R.string.unbind_devices_found_content), wifiDeviceQueryUnbinds.size());
                            showDialogFragment(content, mResources.getString(R.string.cancel), mResources.getString(R.string.unbind_device_found_look), getToWifiDeviceIntent(wifiDeviceQueryUnbinds));

                        }
                    }
                }
            };
        }
    }

    private DialogFragmentTwoButton showDialogFragment(String content, String left, String right, final Intent intent) {
        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(mResources.getString(R.string.unbind_device_found));
        dialogFragmentTwoButton.setLeftButtonText(left);
        dialogFragmentTwoButton.setRightButtonText(right);
        dialogFragmentTwoButton.setContent(content);
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {

            }

            @Override
            public void onRightButtonClick(View view) {
                mActivity.startActivity(intent);
            }
        });
        if (!dialogFragmentTwoButton.isHidden()) {
            try {
                if (mActivity != null) {
                    dialogFragmentTwoButton.show(mActivity.getFragmentManager(), "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dialogFragmentTwoButton;
    }

    /**
     * 关闭通知添加设备弹框
     */
    public void dismissDialog() {
        if (mHubDialog != null && mActivity != null && !mActivity.isFinishingOrDestroyed()) {
            try {
                mHubDialog.dismissAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 跳转到添加coco设备页面
     *
     * @param deviceQueryUnbinds
     * @return
     */
    private Intent getToWifiDeviceIntent(ArrayList<DeviceQueryUnbind> deviceQueryUnbinds) {
        Intent intent = new Intent(mContext, AddUnbindDeviceActivity.class);
        intent.putExtra(IntentKey.DEVICE, deviceQueryUnbinds);
        return intent;
    }

    /**
     * 跳转到添加绑定主机页面
     *
     * @param hubResult
     * @return
     */
    private Intent getToHubIntent(SearchNewHubResult.HubResult hubResult) {
        Intent intent = new Intent(mActivity, AddUnbindHubActivity.class);
        intent.putExtra(IntentKey.HUB_RESULT, hubResult);
        return intent;
    }
}
