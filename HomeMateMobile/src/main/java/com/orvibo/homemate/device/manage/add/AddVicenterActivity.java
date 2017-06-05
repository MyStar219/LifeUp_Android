package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.GatewayBindInfo;
import com.orvibo.homemate.bo.GatewayServer;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.OrviboThreadPool;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NetType;
import com.orvibo.homemate.data.ResultCode;
import com.orvibo.homemate.model.ClientLogin;
import com.orvibo.homemate.model.DeviceSearch;
import com.orvibo.homemate.model.adddevice.vicenter.BindVicenter;
import com.orvibo.homemate.model.adddevice.vicenter.DeviceJoinin;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加主机和zigbee设备界面。
 * Created by Allen on 2015/8/14.
 */
public class AddVicenterActivity extends BaseActivity implements BindVicenter.OnBindVicenterListener {
    private static final String TAG = AddVicenterActivity.class.getSimpleName();
    public static final int ACTION_TYPE_BIND = 1;
    public static final int ACTION_TYPE_SEARCH_DEVICE = 2;

    private static final int TAG_FINISH = 1;
    private static final int TAG_CANCEL = 2;

    private static final int MESSAGT_WHAT_ROTATION = 0;
    private static final int MESSAGT_WHAT_ROTATION_TIME = 5000;

    private String mUid;

    //view
    private Button button;
    private TextView tip_tv;
    //倒计时
    private TextView time_tv;
    private ImageView deviceSearchImageView;

    private BindVicenter bindVicenter;
    private DeviceJoinin deviceJoinin;
    private ClientLogin clientLogin;
    private int curActionType = ACTION_TYPE_BIND;
    //    private ConcurrentHashMap<String, Integer> mAdminLoginGateways = new ConcurrentHashMap<String, Integer>();
    private List<GatewayBindInfo> mGatewayBindInfos;
    private volatile List<Device> mDevices = new ArrayList<Device>();
    //    private DialogFragmentTwoButton dialogFragment;
    private ConfirmAndCancelPopup mConfirmAndCancelPopup;
    private SearchDeviceListener searchDeviceListener;
    private int sourceActionType = ACTION_TYPE_BIND;

    private ConfirmAndCancelPopup mContinueFinishPopup;

    private MyHandler mHandler;
    private int rotation = 0;//用于提示语轮换
    private int position;//第几个入口
    /**
     * true网关正在组网，退出此界面时发生退出组网请求
     */
    private volatile boolean isGatewayNetworing = false;

    public static final int RESULT_RETRY = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vicenter_bind_activity);
        button = (Button) findViewById(R.id.cancelButton);
        deviceSearchImageView = (ImageView) findViewById(R.id.deviceSearchImageView);
        button.setOnClickListener(this);
        tip_tv = (TextView) findViewById(R.id.tipsTextView);
        time_tv = (TextView) findViewById(R.id.countDownTextView);
        time_tv.setVisibility(View.VISIBLE);

        setButton(R.string.cancel);
        button.setTag(TAG_CANCEL);
        curActionType = getIntent().getIntExtra(IntentKey.VICENTER_ADD_ACTION_TYPE, ACTION_TYPE_BIND);
        position = getIntent().getIntExtra(IntentKey.VICENTER_ADD_ACTION_POSITION, 0);
        sourceActionType = curActionType;
        if (curActionType == ACTION_TYPE_BIND) {
            setTip(R.string.vicenter_add_doing);
        } else {
            setTip(R.string.vicenter_add_searching_device);
        }
        bindVicenter = new BindVicenter(ViHomeProApp.getContext());
        bindVicenter.setOnBindListener(this);
        deviceJoinin = new DeviceJoinin(mAppContext);
//      deviceJoinin = DeviceJoinin.getInstance(ViHomeProApp.getContext());
        searchDeviceListener = new SearchDeviceListener();
        LogUtil.d(TAG, "onCreate()-setOnDeviceJoininListener:" + searchDeviceListener);
        deviceJoinin.setOnDeviceJoininListener(searchDeviceListener);
        initClentLogin();
        mHandler = new MyHandler();
        mHandler.postDelayed(doAddRunnable, 1200);
        startAnim();
        mGatewayBindInfos = null;

        /**
         * 时间处理，先设置默认时间，因为有延时，所以采用先初始一个数据，同时在回调接口那数据保持同步
         */
        if (curActionType == ACTION_TYPE_BIND) {
            time_tv.setText(String.format(getString(R.string.vicenter_add_time), BindVicenter.mTotalCountTime));
        } else {
            curActionType = ACTION_TYPE_SEARCH_DEVICE;
            time_tv.setText(String.format(getString(R.string.vicenter_add_time), DeviceSearch.TIME_JOININ));
        }
    }

    private Runnable doAddRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishingOrDestroyed()) {
                if (curActionType == ACTION_TYPE_BIND) {
                    setTip(R.string.vicenter_add_doing);
                    bindVicenter.bind(true);
                } else {
                    curActionType = ACTION_TYPE_SEARCH_DEVICE;
                    LogUtil.d(TAG, "onCreate()-currentMainUid:" + currentMainUid);
                    if (position == 5 || position == 6) {
                        mHandler.sendEmptyMessage(MESSAGT_WHAT_ROTATION);
                    } else {
                        showProgress(R.string.vicenter_add_searching_device, DeviceSearch.TIME_JOININ);
                    }
                    mUid = currentMainUid;
                    deviceJoinin(mUid, true);
                }
            }
        }
    };

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setRotationTip();
            mHandler.sendEmptyMessageDelayed(MESSAGT_WHAT_ROTATION, MESSAGT_WHAT_ROTATION_TIME);
        }
    }

    private void stop(final boolean stopSearchZigbeeDevice) {
        LogUtil.w(TAG, "stop()-stopSearchZigbeeDevice:" + stopSearchZigbeeDevice);
        OrviboThreadPool.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                if (deviceJoinin != null) {
                    deviceJoinin.stopJoinin(stopSearchZigbeeDevice);
                    deviceJoinin.removeListener(searchDeviceListener);
                }
                if (bindVicenter != null) {
                    bindVicenter.cancel();
                }
                isGatewayNetworing = false;
            }
        });
        if (mHandler != null) {
            mHandler.removeMessages(MESSAGT_WHAT_ROTATION);
        }
        stopAnim();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
//        stop();
        //如果是搜索到vicenter，点击完成时直接返回到首页
        LogUtil.d(TAG, "onClick()-sourceActionType = " + sourceActionType);
        if (sourceActionType == ACTION_TYPE_BIND) {
            if (bindVicenter.isFoundBindedVicenter()) {
                LogUtil.d(TAG, "isFoundBindedVicenter");
                stop(isGatewayNetworing);
                if (mDevices.isEmpty()) {
                    Intent toMainIntent = new Intent(mContext, MainActivity.class);
                    startActivity(toMainIntent);
                    finish();
                } else {
                    toSetDevice();
                    return;
                }
            } else if (bindVicenter.isBindSuccess()) {
                LogUtil.d(TAG, "isBindSuccess");
                if (mDevices.isEmpty()) {
                    showContinueFinishPopup();
//                    finish();
                    return;
                } else {
                    stop(isGatewayNetworing);
                    toSetDevice();
                    return;
                }
            } else {
                stop(isGatewayNetworing);
                finish();
            }
        } else {
            stop(isGatewayNetworing);
            if (!mDevices.isEmpty()) {
                toSetDevice();
                return;
            }
            setResult(ResultCode.FINISH);
            finish();
        }
    }

    private void showContinueFinishPopup() {
        if (mContinueFinishPopup == null) {
            mContinueFinishPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                }

                @Override
                public void cancel() {
                    dismiss();
                    stop(isGatewayNetworing);
                    Intent toMainIntent = new Intent(mContext, MainActivity.class);
                    startActivity(toMainIntent);
                    finish();
                }
            };
        }
        mContinueFinishPopup.showPopup(mContext, R.string.vicenter_add_popup_content, R.string.vicenter_add_popup_wait, R.string.vicenter_add_popup_finish);
    }

    private void dismissContinuePopup() {
        if (mContinueFinishPopup != null && mContinueFinishPopup.isShowing()) {
            mContinueFinishPopup.dismiss();
        }
    }

    private void toSetDevice() {
        Intent intent = new Intent(mContext,
                AddDeviceSuccessActivity.class);
        if (sourceActionType == ACTION_TYPE_BIND) {
            intent.putExtra("vicenter", mUid);
        }
        intent.putExtra("addedDevices", (Serializable) mDevices);
        startActivityForResult(intent, 1);
//        mContext.startActivity(intent);
    }

    private void initClentLogin() {
        clientLogin = new ClientLogin(mAppContext) {
            @Override
            public void onCientLoginResult(List<GatewayServer> gatewayServers, int result) {

            }

            @Override
            public void onCientLoginResult(String uid, int result) {
//                mAdminLoginGateways.put(uid, result);
//                int adminLoginResult = isAdminLoginFinish();
                LogUtil.d(TAG, "onClientLoginResult()-uid:" + uid + ",result:" + result);
                //6431 【添加主机】提示添加成功，又弹框提示添加失败
                if (result == ErrorCode.SUCCESS) {
                    //success,do request device join in
                    curActionType = ACTION_TYPE_SEARCH_DEVICE;
                    showProgress(R.string.vicenter_add_search_after_addsuccess, DeviceSearch.TIME_JOININ);
                    deviceJoinin(uid, true);
                    //startShowSearchDeviceView(true);
                } else if (result == ErrorCode.MULTI_ADMIN_LOGIN_ERROR) {
                    //fail
                    showAddVicenterFailDialog(null, R.string.ADMIN_LOGIN_FAIL, null);
                } else {
                    //fail
                    showAddVicenterFailDialog(null, R.string.vicenter_add_fail_content, null);
                    ToastUtil.toastError(result);
                }
//                if (adminLoginResult < 0) {
//                    //fail
//                    showAddVicenterFailDialog(null, R.string.vicenter_add_fail_content, null);
//                    ToastUtil.toastError(result);
//                } else if (adminLoginResult == ErrorCode.MULTI_ADMIN_LOGIN_ERROR) {
//                    //fail
//                    showAddVicenterFailDialog(null, R.string.ADMIN_LOGIN_FAIL, null);
//                } else if (adminLoginResult >= 0) {
//                    //success,do request device join in
//                    curActionType = ACTION_TYPE_SEARCH_DEVICE;
//                    showProgress(R.string.vicenter_add_search_after_addsuccess, DeviceSearch.TIME_JOININ);
//                    deviceJoinin(uid, true);
//                    //startShowSearchDeviceView(true);
//                }
            }
        };
    }

    private void showProgress(int textResId, int sec) {
        LogUtil.d(TAG, "showProgress()-sec:" + sec);
        if (sec < 0) {
            tip_tv.setText(textResId);
        } else {
            tip_tv.setText(String.format(getString(textResId), sec));
        }
    }

    @Override
    public void onBindVicenterResult(List<GatewayBindInfo> gatewayBindInfos, List<String> uids, int result, int sec) {
        LogUtil.d(TAG, "onBindVicenterResult()-gatewayBindInfos:" + gatewayBindInfos + ",uids:" + uids + ",result:" + result);
        if (isFinishingOrDestroyed()) {
            return;
        }
        if (uids != null && !uids.isEmpty() && result == ErrorCode.SUCCESS) {
            if (bindVicenter != null) {
                bindVicenter.cancel();
            }
            curActionType = ACTION_TYPE_SEARCH_DEVICE;
            showProgress(R.string.vicenter_add_search_after_addsuccess, DeviceSearch.TIME_JOININ);
            setButton(R.string.finish);
            button.setTag(TAG_FINISH);
            //管理员登录
            // admin login
            String uid = uids.get(0);
            mUid = uid;
            deviceJoinin(uid, true);
        } else {
            //显示的内容
            int resId = R.string.vicenter_add_fail_content;
            int netStatus = NetUtil.judgeNetConnect(mContext);
            if (result == ErrorCode.WIFI_DISCONNECT || netStatus == NetType.GPRS) {
                cancelBind();
                resId = R.string.WIFI_DISCONNECT;
                showTip(null, resId, null);
            } else if (result == ErrorCode.NET_DISCONNECT || netStatus == NetType.NET_ERROR) {
                cancelBind();
                resId = R.string.NET_DISCONNECT;
                showTip(null, resId, null);
            } else if (result == ErrorCode.MULTI_ADMIN_LOGIN_ERROR) {
                cancelBind();
                //fail
                showAddVicenterFailDialog(null, R.string.ADMIN_LOGIN_FAIL, null);
            } else {
                if (mGatewayBindInfos == null) {
                    mGatewayBindInfos = gatewayBindInfos;
                } else if (gatewayBindInfos != null && !gatewayBindInfos.isEmpty()) {
                    if (mGatewayBindInfos.isEmpty()) {
                        mGatewayBindInfos.addAll(gatewayBindInfos);
                    } else {
                        for (GatewayBindInfo gatewayBindInfo : gatewayBindInfos) {
                            String uid = gatewayBindInfo.uid;
                            boolean exist = false;
                            for (GatewayBindInfo existGatewayBindInfo : mGatewayBindInfos) {
                                if (uid.equals(existGatewayBindInfo.uid)) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist) {
                                mGatewayBindInfos.add(gatewayBindInfo);
                            }
                        }
                    }
                }
                String title = null;
                if (sec > 0) {
                    bindVicenter.reBind(sec);
                } else {
                    cancelBind();
                    String confirmTxt = null;
                    if (mGatewayBindInfos != null && !mGatewayBindInfos.isEmpty()) {
                        boolean containNotBoundVicenter = false;
                        for (GatewayBindInfo gatewayBindInfo : mGatewayBindInfos) {
                            if (gatewayBindInfo.state == ErrorCode.USER_NOT_BINDED) {
                                containNotBoundVicenter = true;
                                break;
                            }
                        }
                        if (containNotBoundVicenter) {
                            confirmTxt = getString(R.string.vicenter_add_fail_operation);
                            title = getString(R.string.vicenter_add_fail_title_vicenter_found);
                            resId = R.string.vicenter_add_found_vicenter_content;
                        }
                    } else {
                        title = getString(R.string.vicenter_add_fail_title_not_found_vicenter);
                    }
                    LogUtil.d(TAG, "onBindVicenterResult()-mGatewayBindInfos:" + mGatewayBindInfos);
                    showTip(title, resId, confirmTxt);
                }
            }
        }
    }

    /**
     * 取消绑定
     */
    private void cancelBind() {
        if (bindVicenter != null) {
            bindVicenter.cancel();
        }
    }

    private void showTip(String title, int resId, String confirmTxt) {
        stopAnim();
        showAddVicenterFailDialog(title, resId, confirmTxt);
    }

    @Override
    public void onCountdown(int sec) {
        LogUtil.d(TAG, "onCountdown:" + sec);
        if (isFinishingOrDestroyed()) {
            return;
        }
        setTime(sec);
    }

    private void deviceJoinin(String uid, boolean isFirst) {
        mDevices.clear();
        deviceJoinin.setOnDeviceJoininListener(searchDeviceListener);
        deviceJoinin.joinin(uid, isFirst);
    }

    private void showFailDialog(String title, String content, String confirmTxt) {
        if (!isFinishingOrDestroyed()) {
            if (mConfirmAndCancelPopup != null && mConfirmAndCancelPopup.isShowing()) {
                mConfirmAndCancelPopup.dismiss();
            }
            mConfirmAndCancelPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    super.confirm();
                    dismiss();
                    LogUtil.d(TAG, "confirm()");
                    int type = getFoundVicenterType();
                    if (curActionType == ACTION_TYPE_BIND && (type == DeviceType.MINIHUB || type == DeviceType.VICENTER)) {
                        //点击如何操作?按钮，将显示重置主机popup
                        String content = "";
                        if (type == DeviceType.MINIHUB) {
                            content = getString(R.string.vicenter_add_reset_hub_tip1) + "\n\n" + getString(R.string.vicenter_add_reset_hub);
                        } else {
                            content = getString(R.string.vicenter_add_reset_vih_tip1) + "\n\n" + getString(R.string.vicenter_add_reset_vih);
                        }
                        showResetPopup(content, type);
                    } else {
                        //再来一次
                        retry();
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                    stop(isGatewayNetworing);
                    finish();
                }
            };
            if (StringUtil.isEmpty(confirmTxt)) {
                confirmTxt = getString(R.string.retry_once);
            }
            mConfirmAndCancelPopup.showPopup(mContext, title, content, confirmTxt, getString(R.string.cancel));
            // mConfirmAndCancelPopup.setCancelTextColor(getResources().getColor(R.color.font_white_gray));
            mConfirmAndCancelPopup.setCancelTextColor(getResources().getColor(R.color.green));
        }
    }

    private void retry() {
        setButton(R.string.cancel);
        button.setTag(TAG_CANCEL);
        startAnim();
        if (curActionType == ACTION_TYPE_BIND) {
            mGatewayBindInfos = null;
            bindVicenter.bind(true);
        } else {
            deviceJoinin.setOnDeviceJoininListener(searchDeviceListener);
            deviceJoinin.joinin(mUid, true);
            if (position == 5 || position == 6) {
                rotation = 0;
                mHandler.sendEmptyMessage(MESSAGT_WHAT_ROTATION);
            }
        }
    }

    /**
     * 如果返回{@link Constant#INVALID_NUM}表示没有找到主机
     *
     * @return See {@link DeviceType}
     */
    private int getFoundVicenterType() {
        int type = Constant.INVALID_NUM;
        if (mGatewayBindInfos != null && !mGatewayBindInfos.isEmpty()) {
            boolean containBoundVicenter = false;
            boolean containHub = false;
            ProductManage pm = ProductManage.getInstance();
            for (GatewayBindInfo gatewayBindInfo : mGatewayBindInfos) {
                if (gatewayBindInfo.state == ErrorCode.USER_NOT_BINDED || gatewayBindInfo.state == ErrorCode.GATEWAY_BINDED) {
                    containBoundVicenter = true;
                    if (pm.isHub(gatewayBindInfo.uid, gatewayBindInfo.model)) {
                        containHub = true;
                        break;
                    }
                }
            }

            if (containBoundVicenter) {
                if (containHub) {
                    type = DeviceType.MINIHUB;
                } else {
                    type = DeviceType.VICENTER;
                }
            }
        }
        return type;
    }

    private void showAddVicenterFailDialog(String title, int contentResId, String confirmTxt) {
        LogUtil.d(TAG, "showAddVicenterFailDialog()");
        showAddVicenterFailDialog(title, getString(contentResId), confirmTxt);
    }

    /**
     * 提示重置popup
     *
     * @param content
     */
    private void showResetPopup(String content, int deviceType) {
        if (!isFinishingOrDestroyed()) {
            stopAnim();

            if (mConfirmAndCancelPopup != null && mConfirmAndCancelPopup.isShowing()) {
                mConfirmAndCancelPopup.dismiss();
            }
            mConfirmAndCancelPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    super.confirm();
                    dismiss();
                    if (mGatewayBindInfos != null && !mGatewayBindInfos.isEmpty()) {
                        mGatewayBindInfos.clear();
                    }
                    mGatewayBindInfos = null;

                    //重新添加
                    setButton(R.string.cancel);
                    button.setTag(TAG_CANCEL);
                    startAnim();
                    if (curActionType == ACTION_TYPE_BIND) {
                        bindVicenter.bind(false);
                    } else {
                        deviceJoinin.setOnDeviceJoininListener(searchDeviceListener);
                        deviceJoinin.joinin(mUid, false);
                        if (position == 5 || position == 6) {
                            rotation = 0;
                            mHandler.sendEmptyMessage(MESSAGT_WHAT_ROTATION);
                        }
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                    finish();
                }
            };
            mConfirmAndCancelPopup.showVicenterResetPopup(mContext, content, deviceType);
        }
    }

    private void showAddVicenterFailDialog(String title, String content, String confirmTxt) {
        if (StringUtil.isEmpty(title)) {
            title = getString(R.string.vicenter_add_fail_title);
        }
        showFailDialog(title, content, confirmTxt);
    }

    private void showSearchDevicerFailDialog(int contentResId) {
        showFailDialog(getString(R.string.vicenter_add_device_fail_title), getString(contentResId), null);
    }

    private void showFinishView() {
        button.setText(R.string.finish);
        button.setTag(TAG_FINISH);
    }

    private class SearchDeviceListener implements DeviceJoinin.OnDeviceJoininListener {
        @Override
        public void onTimeRemaining(int time) {
            LogUtil.d(TAG, "onTimeRemaining()-time:" + time);
            if (isFinishingOrDestroyed()) {
                return;
            }
            setTime(time);
            if (time <= 0) {
                mHandler.removeMessages(MESSAGT_WHAT_ROTATION);
            } else {
                isGatewayNetworing = true;
            }
        }

        @Override
        public void onFinish() {
            LogUtil.d(TAG, "onFinish()");
            if (isFinishingOrDestroyed()) {
                return;
            }
            //250s入网时间结束
            stop(isGatewayNetworing);
            isGatewayNetworing = false;//取消了组网，设置当前为组网已关闭状态
//            stopAnim();
            if (sourceActionType == ACTION_TYPE_BIND && bindVicenter.isFoundBindedVicenter()) {
                //绑定网关，其他app已经绑定，当倒计时结束后直接跳转到首页
//                stop();
                if (mDevices.isEmpty()) {
                    setResult(AddVicenterTipActivity.CODE_EXIT_ADD_DEVICE);
                    Intent toMainIntent = new Intent(mContext, MainActivity.class);
                    startActivity(toMainIntent);
                } else {
                    toSetDevice();
                }
                finish();
            } else {
                if (curActionType == ACTION_TYPE_SEARCH_DEVICE) {
                    if (mDevices.isEmpty()) {
                        if (position == 7) {
                            toDeviceAddGasFailActivity();
                        } else {
                            showSearchDevicerFailDialog(R.string.vicenter_add_device_fail_content);
                        }
                    } else {
//                        stop();
                        toSetDevice();
                        finish();
                    }
                } else {
                    showAddVicenterFailDialog(null, R.string.vicenter_add_fail_content, null);
                }
            }
        }

        private void toDeviceAddGasFailActivity() {
            Intent intent = new Intent(mContext, DeviceAddGasFailActivity.class);
            startActivityForResult(intent, 0);
        }

        @Override
        public void onNewDevice(Device device) {
            LogUtil.d(TAG, "onNewDevice()-device:" + device);
            if (isFinishingOrDestroyed()) {
                return;
            }
            dismissContinuePopup();
//            if (!isContainSameDevice(device)) {
//                mDevices.add(device);
//                mHandler.removeMessages(MESSAGT_WHAT_ROTATION);
//                setTip(String.format(getString(R.string.vicenter_add_search_device_success), mDevices.size()));
//            } else {
//                LogUtil.w(TAG, "onNewDevice()-已经接收到此设备入网请求:" + device);
//            }
            if (device != null) {
                String deviceId = device.getDeviceId();
                if (!StringUtil.isEmpty(deviceId)) {
                    boolean exist = false;
                    int index = -1;
                    for (int i = 0; i < mDevices.size(); i++) {
                        Device oldDevice = mDevices.get(i);
                        if (deviceId.equals(oldDevice.getDeviceId())) {
                            exist = true;
                            mDevices.set(i, device);
                            break;
                        } else if (DeviceTool.isSameDevice(device, oldDevice)) {
                            index = i;
                            break;
                        }
                    }
                    //一个设备入网后又把它重新入网，虽然deviceId变了，但实际是同一个设备。
                    if (index >= 0) {
                        try {
                            mDevices.remove(index);
//                            mDevices.add(device);Q
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!exist) {
                        mDevices.add(device);
                        mHandler.removeMessages(MESSAGT_WHAT_ROTATION);
                        if (mDevices.size() == 1) {
                            setTip(String.format(getString(R.string.vicenter_add_search_device_success), mDevices.size()));
                        } else if (mDevices.size() > 1) {
                            setTip(String.format(getString(R.string.vicenter_add_search_devices_success), mDevices.size()));
                        }
                    } else {
                        LogUtil.w(TAG, "onNewDevice()-已经接收到此设备入网请求:" + device);
                    }
                }
            }
            showFinishView();
        }

        @Override
        public void onNewCamera(CameraInfo cameraInfo) {

        }

        @Override
        public void onError(int errorCode) {
            LogUtil.e(TAG, "onError()-errorCode:" + errorCode + ",thread:" + Thread.currentThread());
            if (isFinishingOrDestroyed()) {
                return;
            }
            stopAnim();
            mHandler.removeMessages(MESSAGT_WHAT_ROTATION);
            isGatewayNetworing = false;
            int resId = R.string.vicenter_add_fail_content;
//            int resId = R.string.vicenter_add_device_fail_content;
            int netStatus = NetUtil.judgeNetConnect(mContext);
            if (errorCode == ErrorCode.REMOTE_ERROR) {
                resId = R.string.vicenter_add_fail_content;
            } else if (errorCode == ErrorCode.NET_DISCONNECT || netStatus == NetType.NET_ERROR) {
                resId = R.string.NET_DISCONNECT;
            } else if (errorCode == ErrorCode.WIFI_DISCONNECT || netStatus == NetType.GPRS) {
                resId = R.string.WIFI_DISCONNECT;
            } else if (errorCode == ErrorCode.SPECIAL_VICENTER_NETWORKING) {
                resId = R.string.vicenter_add_device_fail_44_content;
            } else if (errorCode == ErrorCode.NO_ADMIN_PERMISSIONS) {
                //没有管理员权限
                resId = R.string.NO_ADMIN_PERMISSIONS;
                new ConfirmAndCancelPopup() {
                    @Override
                    public void confirm() {
                        super.confirm();
                        dismiss();
                        stop(isGatewayNetworing);
                        finish();
                    }
                }.showPopup(mContext, getString(R.string.warm_tips), getString(resId), getString(R.string.know), null);
                return;
            } else if (errorCode == ErrorCode.GATEWAY_NOT_BINDED || errorCode == ErrorCode.USER_NOT_BINDED) {
                //组网时如果主机被其他app解绑或者绑定，将走添加主机流程
                LogUtil.e(TAG, "onError()-Hub has been unbind or binded by other app.Ready to bind hub.");
                curActionType = ACTION_TYPE_BIND;
                sourceActionType = curActionType;
                setTip(R.string.vicenter_add_doing);
                startAnim();
                bindVicenter.bind(true);
                return;
            } else if (position == 7) {
                toDeviceAddGasFailActivity();
                return;
            }
            showSearchDevicerFailDialog(resId);
        }

        @Override
        public void onOpened() {
            isGatewayNetworing = true;
        }

        @Override
        public void onClosed() {
            isGatewayNetworing = false;
        }

        private boolean isContainSameDevice(Device device) {
            for (Device d : mDevices) {
                if (d.getDeviceId().equals(device.getDeviceId())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 开始执行扫描动画
     */
    private void startAnim() {
        if (deviceSearchImageView != null) {
            Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
            deviceSearchImageView.startAnimation(scanAnim);
        }
    }

    /**
     * 清除扫描动画
     */
    private void stopAnim() {
        if (deviceSearchImageView != null) {
            deviceSearchImageView.clearAnimation();
        }
    }

    private void setButton(int textResId) {
        button.setText(textResId);
    }

    private void setTip(String tip) {
        tip_tv.setText(tip);
    }

    private void setTip(int tip) {
        tip_tv.setText(tip);
    }

    private void setRotationTip() {
        if (rotation == 0) {
            rotation = 1;
            tip_tv.setText(R.string.vicenter_add_searching_device);
        } else {
            rotation = 0;
            if (position == 5) {
                tip_tv.setText(R.string.vicenter_add_searching_device_rotation);
            } else {
                tip_tv.setText(R.string.vicenter_add_searching_device_rotation2);
            }
        }
    }

    private void setTime(int sec) {
        time_tv.setText(String.format(getString(R.string.vicenter_add_time), sec));
    }

    @Override
    public void onBackPressed() {
        stop(isGatewayNetworing);
        setResult(ResultCode.FINISH);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-resultCode:" + resultCode);
        if (resultCode == ResultCode.FINISH) {
            setResult(ResultCode.FINISH);
            finish();
        } else if (resultCode == AddVicenterTipActivity.CODE_EXIT_ADD_DEVICE) {//resultCode常量需要在同一个地方定义，或者在不同地方定义不同数量级别的常量，避免冲突 by Allen
            setResult(AddVicenterTipActivity.CODE_EXIT_ADD_DEVICE);
            finish();
        } else if (resultCode == RESULT_RETRY) {
            retry();
        }
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(doAddRunnable);
        }
        if (clientLogin != null) {
            clientLogin.cancel();
        }

        if (bindVicenter != null) {
            bindVicenter.setOnBindListener(null);
            //修复了这里取消读表，但如果关闭组网需要读表的话就会出现读完表没有回调问题
            if (!isGatewayNetworing) {
                bindVicenter.cancel();
            }
        }
//        stop();
        currentMainUid = UserCache.getCurrentMainUid(mAppContext);
        // EventBus.getDefault().post(new MainEvent(StringUtil.isEmpty(currentMainUid) ? BottomTabType.TWO_BOTTOM_TAB : BottomTabType.FOUR_BOTTOM_TAB, true));
        super.onDestroy();
        if (!isGatewayNetworing) {
            if (deviceJoinin != null) {
                deviceJoinin.cancel();
            }
        }
    }
}
