package com.orvibo.homemate.user;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.bo.GatewayServer;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.SetActivity;
import com.orvibo.homemate.common.appwidget.WidgetSettingActivity;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.dao.GatewayServerDao;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.CommonDeviceActivity;
import com.orvibo.homemate.device.manage.edit.DeviceInfoActivity;
import com.orvibo.homemate.messagepush.MessageActivity;
import com.orvibo.homemate.messagepush.MessageSettingActivity;
import com.orvibo.homemate.model.user.GetAccountIcon;
import com.orvibo.homemate.sharedPreferences.CommonDeviceCache;
import com.orvibo.homemate.sharedPreferences.FindNewVersion;
import com.orvibo.homemate.sharedPreferences.PicCache;
import com.orvibo.homemate.sharedPreferences.SessionIdCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.family.FamilyActivity;
import com.orvibo.homemate.user.family.FamilyInviteActivity;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.InfoPushTopView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViHomePersonalFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ViHomePersonalFragment.class.getSimpleName();
    private View view;
    private InfoPushTopView infoPushCountView;
    private RelativeLayout rl_userInfo;
    private ImageView userIcon;
    private TextView userNameTextView;
    private RelativeLayout userLoginLayout;
    private ImageView findNewVersionImageView;
    private ImageView messageSettingLineImageView;
    private RelativeLayout allSet;
    private RelativeLayout familyMember;
    private RelativeLayout commonDevice;
    private RelativeLayout messageSetting;
    private RelativeLayout myHost;
    private RelativeLayout about;
    private Account account;

    private AccountDao mAccountDao;
    private MessageDao mMessageDao;
    private GatewayDao mGatewayDao;
    private GatewayServerDao mGatewayServerDao;
    private Device device = new Device();
    private RelativeLayout mUserFeedBackView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, "======= onCreateView ======");
        view = inflater.inflate(R.layout.vihome_personal_fragment, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "======= onResume ======");
        refreshInfoPushCount();
        refresh();
        setCommonDeviceView();
        setHubView();
    }

    /**
     * version1.8.5 常用设备一旦显示就不消失
     */
    private void setCommonDeviceView() {
        List<Device> devices = DeviceTool.getAllCommonDevices(context);
        String currentUserId = UserCache.getCurrentUserId(ViHomeApplication.getAppContext());
        boolean showCommonDeviceView = CommonDeviceCache.getBoolean(ViHomeApplication.getAppContext(), currentUserId);
        if (devices.size() >= 10 || showCommonDeviceView) {
            CommonDeviceCache.putBoolean(ViHomeApplication.getAppContext(), currentUserId, true);
            commonDevice.setVisibility(View.VISIBLE);
        } else {
            commonDevice.setVisibility(View.GONE);
        }
    }

    /**
     * 设置有主机或者没有主机时的界面
     */
    private void setHubView() {
        if (StringUtil.isEmpty(mainUid)) {
            myHost.setVisibility(View.GONE);
            allSet.setVisibility(View.GONE);
            messageSettingLineImageView.setVisibility(View.GONE);
        } else {
            myHost.setVisibility(View.VISIBLE);
            messageSettingLineImageView.setVisibility(View.VISIBLE);
            allSet.setVisibility(View.GONE);
        }
    }

    /**
     * 存在主机时设置主机信息，用于传递给主机信息界面
     */
    private void setHubInfo() {
        ProductManage pm = ProductManage.getInstance();
        String model = pm.getModel(mainUid);
        Gateway gateway = mGatewayDao.selGatewayByUid(mainUid);
        if (TextUtils.isEmpty(model) && gateway != null) {
            model = gateway.getModel();
        }
        if (StringUtil.isEmpty(model)) {
            LogUtil.w(TAG, "refreshAllRoomDevices()-" + mainUid + "'s model is empty or null.");
        } else {
            if (pm.isVicenter300(mainUid, model)) {
                if (pm.isHub(mainUid, model)) {
                    device.setDeviceType(DeviceType.MINIHUB);
                } else {
                    device.setDeviceType(DeviceType.VICENTER);
                }
            } else {
                device.setDeviceType(DeviceType.VICENTER);
            }
        }
        device.setUid(mainUid);
        if (gateway != null) {
            // device.setDeviceId(gateway.getUid());
            String deviceName = gateway.getHomeName();
            device.setDeviceName(deviceName);
            device.setUid(gateway.getUid());
            String tempModel = gateway.getModel();
            device.setModel(tempModel);
        } else {
            GatewayServer gatewayServer = mGatewayServerDao.selGatewayServer(mainUid);
            if (gatewayServer != null) {
                device.setDeviceId(gatewayServer.getUid());
                device.setDeviceName(getString(R.string.vicenter_default_name));
                device.setUid(gatewayServer.getUid());
                if (StringUtil.isEmpty(model)) {
                    device.setModel(gatewayServer.getModel());
                } else {
                    device.setModel(model);
                }
            }
        }
    }

    private void initView(View view) {
        mAccountDao = new AccountDao();
        mMessageDao = new MessageDao();
        mGatewayDao = new GatewayDao();
        mGatewayServerDao = new GatewayServerDao();
        userIcon = (ImageView) view.findViewById(R.id.userIcon);
        infoPushCountView = (InfoPushTopView) view.findViewById(R.id.infoPushCountView);
        infoPushCountView.setOnClickListener(this);
        rl_userInfo = (RelativeLayout) view.findViewById(R.id.rl_userInfo);
        rl_userInfo.setOnClickListener(this);
        userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
        userLoginLayout = (RelativeLayout) view.findViewById(R.id.userLoginLayout);
        userLoginLayout.setOnClickListener(this);
        allSet = (RelativeLayout) view.findViewById(R.id.allSet);
        allSet.setOnClickListener(this);
        familyMember = (RelativeLayout) view.findViewById(R.id.familyMember);
        familyMember.setOnClickListener(this);
        commonDevice = (RelativeLayout) view.findViewById(R.id.commonDevice);
        commonDevice.setOnClickListener(this);
        messageSettingLineImageView = (ImageView) view.findViewById(R.id.messageSettingLineImageView);
        messageSetting = (RelativeLayout) view.findViewById(R.id.messageSetting);
        messageSetting.setOnClickListener(this);
        myHost = (RelativeLayout) view.findViewById(R.id.myHost);
        myHost.setOnClickListener(this);
        findNewVersionImageView = (ImageView) view.findViewById(R.id.findNewVersion);

        if (judgeIsHope()) {
            ((RelativeLayout) view.findViewById(R.id.widgetSetting)).setOnClickListener(this);
            ((RelativeLayout) view.findViewById(R.id.widgetSetting)).setVisibility(View.VISIBLE);
        } else {
            ((RelativeLayout) view.findViewById(R.id.widgetSetting)).setVisibility(View.GONE);
        }


        about = (RelativeLayout) view.findViewById(R.id.about);
        about.setOnClickListener(this);
        mUserFeedBackView = (RelativeLayout) view.findViewById(R.id.user_feedback);
        mUserFeedBackView.setOnClickListener(this);

        about.setVisibility(View.GONE);
        mUserFeedBackView.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        LogUtil.d(TAG, "======= onRefresh ======");
        super.onRefresh();
        refreshInfoPushCount();
        refresh();
    }

    public void refresh() {
        String userId = UserCache.getCurrentUserId(context);
        String appUserName = UserCache.getCurrentUserName(context);
        LogUtil.d(TAG, "refresh()-appUserName:" + appUserName + ",userId:" + userId);
        if (UserManage.getInstance(context).isLogined()) {
            account = mAccountDao.selCurrentAccount(userId);
            LogUtil.d(TAG, "refresh(0)-account:" + account);
            if (account == null) {
                LogUtil.d(TAG, "refresh()-Could not found account info by " + userId);
                if (!StringUtil.isEmpty(appUserName)) {
                    account = mAccountDao.selMainAccountdByUserName(appUserName);
                    LogUtil.d(TAG, "refresh(1)-account:" + account);
                }
            }
            rl_userInfo.setVisibility(View.VISIBLE);
            userLoginLayout.setVisibility(View.GONE);
            if (account != null) {
                String userName = account.getUserName();
                if (!TextUtils.isEmpty(userName)) {
                    userNameTextView.setText(userName);
                } else {
                    userNameTextView.setText(R.string.user_nickname_no);
                }
            } else {
                userNameTextView.setText(R.string.user_nickname_no);
            }
            setPic(); //设置头像
        } else {
            rl_userInfo.setVisibility(View.GONE);
            userLoginLayout.setVisibility(View.VISIBLE);
        }

        // LifeUp We will not show the user login layout or the user info
        rl_userInfo.setVisibility(View.GONE);
        userLoginLayout.setVisibility(View.GONE);

        mainUid = UserCache.getCurrentMainUid(context);
        setHubView();
        refreshUpdateInfo();
    }

    @Override
    public void onVisible() {
        mainUid = UserCache.getCurrentMainUid(mAppContext);
        LogUtil.d(TAG, "======= onVisible =======" + mainUid);
        refreshInfoPushCount();
        setHubView();
        setCommonDeviceView();
        refreshUpdateInfo();
    }

    private void refreshInfoPushCount() {
        String userId = UserCache.getCurrentUserId(mAppContext);
        int infoPushCount = mMessageDao.selUnreadCount(userId);
        LogUtil.d(TAG, "refreshInfoPushCount() - infoPushCount = " + infoPushCount + ",userId:" + userId);
        if (infoPushCountView != null) {
            if (infoPushCount > 0) {
                infoPushCountView.setInfoPushCountVisible();
                infoPushCountView.startInfoPushAnimation();
            } else {
                infoPushCountView.setInfoPushCountInvisible();
            }
            infoPushCountView.setInfoPushcount(infoPushCount);
        }
    }

    /**
     * 刷新是否显示app升级红点提示
     */
    private void refreshUpdateInfo() {
        if (FindNewVersion.getIsNewVersion(context)) {
            findNewVersionImageView.setVisibility(View.VISIBLE);
        } else {
            findNewVersionImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.widgetSetting:
                Intent widgetSetIntent = new Intent(context, WidgetSettingActivity.class);
                startActivity(widgetSetIntent);
                break;
            case R.id.rl_userInfo: {
                account = mAccountDao.selCurrentAccount(UserCache.getCurrentUserId(context));
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra(Constant.ACCOUNT, account);
                startActivity(intent);
                break;
            }
            case R.id.userLoginLayout: {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra(Constant.LOGIN_ENTRY, Constant.CocoPersonalFragment);
                startActivity(intent);
                break;
            }
            case R.id.allSet: {
                //所有设置
                if (!NetUtil.isWifi(context)) {
                    ToastUtil.toastError(ErrorCode.WIFI_DISCONNECT);
                    return;
                }
                String userName = UserCache.getCurrentUserName(context);
                String md5Password = UserCache.getMd5Password(context, userName);
                if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(md5Password)) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.login_tip);
                    return;
                }
                Intent intent = new Intent(context, SetActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.familyMember: {
                if (!UserManage.getInstance(context).isLogined()) {
                    showLoginDialog();
                } else {
                    List<Account> accounts = new AccountDao().selFamily(UserCache.getCurrentUserId(getActivity()));
                    if (accounts.isEmpty()) {
                        Intent intent = new Intent(getActivity(), FamilyInviteActivity.class);
                        getActivity().startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), FamilyActivity.class);
                        getActivity().startActivity(intent);
                    }
                }
                break;
            }
            case R.id.commonDevice: {
                Intent intent = new Intent(context, CommonDeviceActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.messageSetting: {
                if (!UserManage.getInstance(context).isLogined()) {
                    showLoginDialog();
                } else {
                    Intent intent = new Intent(context, MessageSettingActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.myHost: {
                //我的主机
                mainUid = UserCache.getCurrentMainUid(mAppContext);
                setHubInfo();
                LogUtil.d(TAG, "onClick()-mainUid:" + mainUid + "," + device);
                Intent intent = new Intent(context, DeviceInfoActivity.class);
                if (device != null) {
                    intent.putExtra(Constant.GATEWAY, new GatewayDao().selGatewayByUid(device.getUid()));
                }
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            }
            case R.id.about: {
                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.infoPushCountView: {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                startActivity(intent);
                context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                break;
            }
            case R.id.user_feedback: {
                Intent intent = new Intent(getActivity(), UserFeedBackActivity.class);
                startActivity(intent);
            }
        }
    }

    /**
     * 设置头像
     */
    private void setPic() {
        final String share_picUrl = PicCache.getPicUrl(getActivity(), userName);
        ImageLoader.getInstance().displayImage(share_picUrl, userIcon, ViHomeApplication.getCircleImageOptions(R.drawable.bg_head_portrait_normal));

        //向服务器请求查询头像的url
        GetAccountIcon getAccountIcon = new GetAccountIcon() {
            @Override
            public void onGetAccountIconResult(String picUrl, int errorCode, String errorMessage) {
                switch (errorCode) {
                    case ErrorCode.SUCCESS: {
                        if (!picUrl.equalsIgnoreCase(share_picUrl)) {
                            PicCache.savePic(getActivity(), userName, picUrl);
                            DiskCacheUtils.removeFromCache(picUrl, ImageLoader.getInstance().getDiskCache());
                            MemoryCacheUtils.removeFromCache(picUrl, ImageLoader.getInstance().getMemoryCache());
                            ImageLoader.getInstance().displayImage(picUrl, userIcon, ViHomeApplication.getCircleImageOptions(R.drawable.bg_head_portrait_normal));
                        }
                        break;
                    }
                }
            }
        };
        getAccountIcon.startGetAccountIcon(Constant.SOURCE, userName, SessionIdCache.getServerSessionId(getActivity()));
    }

    /**
     * 判断是否是向往公司的平板
     *
     * @return
     */
    private boolean judgeIsHope() {
        String manufacture = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (manufacture.equals("HOPE") & model.contains("HOPE")) {
            return true;
        } else {
            return false;
        }
    }
}
