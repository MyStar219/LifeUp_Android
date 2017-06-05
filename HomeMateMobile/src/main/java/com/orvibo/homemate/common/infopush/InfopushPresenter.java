package com.orvibo.homemate.common.infopush;

import android.content.Context;
import android.content.Intent;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.bo.infopush.InfoPushHubUpdateMsg;
import com.orvibo.homemate.bo.infopush.OnInfoPushListener;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.InfoPushType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.epg.ProgramInfoPushDialogActivity;
import com.orvibo.homemate.device.hub.HubEvent;
import com.orvibo.homemate.device.hub.HubUpdateActivity;
import com.orvibo.homemate.device.manage.SensorDialogActivity;
import com.orvibo.homemate.event.gateway.HubUpgradeEvent;
import com.orvibo.homemate.message.MessageCache;
import com.orvibo.homemate.model.gateway.HubConstant;
import com.orvibo.homemate.user.family.FamilyInviteDialogActivity;
import com.orvibo.homemate.user.family.FamilyInviteResponseDialogActivity;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by huangqiyao on 2016/7/20 22:13.
 * 将首页处理消息推送相关功能放到此类。
 *
 * @version v1.10
 */
public class InfopushPresenter implements
        OnInfoPushListener,
        InfoPushManager.OnPushInviteFamilyListener,
        InfoPushManager.OnPushInviteFamilyResultListener,
        InfoPushManager.OnWarningListener,
        InfoPushManager.OnProgramMessageListener, IInfopushPresenter {
    private List<InfoPushMsg> infoPushMsgsWarning = new ArrayList<InfoPushMsg>();
    private List<InfoPushMsg> infoPushMsgsInvite = new ArrayList<InfoPushMsg>();
    private List<InfoPushMsg> infoPushMsgsInviteResult = new ArrayList<InfoPushMsg>();
//    private List<InfoPushMsg> infoPushMsgsSensor = new ArrayList<InfoPushMsg>();
    private List<InfoPushMsg> infoPushProgramSubscribe = new ArrayList<InfoPushMsg>();

    /**
     * 可以知道activity的生命周期
     */
    private BaseActivity mActivity;
    private Context mContext;

    public InfopushPresenter(BaseActivity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onRegister() {
        initPushListener();
        InfoPushManager.getInstance(mContext).setLogined(true);
        processMessages();
    }

    /**
     * 处理
     */
    private void processMessages() {
        ConcurrentHashMap<String, InfoPushMsg> infoPushMsgs = MessageCache.getInstance().getAllMessages();
        MyLogger.kLog().d("infoPushMsgs:" + infoPushMsgs);
        if (infoPushMsgs != null && !infoPushMsgs.isEmpty()) {
            for (Map.Entry<String, InfoPushMsg> entry : infoPushMsgs.entrySet()) {
                InfoPushMsg msg = entry.getValue();
                if (msg != null) {
                    processMessage(msg);
                }
            }
            MessageCache.getInstance().clearAllMessages();
        }
    }

    private void processMessage(InfoPushMsg msg) {
        switch (msg.getInfoType()) {
            case InfoPushType.PROPERTYREPORT_PUSH_TYPE:
                //属性报告
                String deviceId = msg.getPushPropertyReportInfo().getDeviceId();
                Device device = new DeviceDao().selDevice(deviceId);
                if (device != null) {
                    if ((DeviceUtil.isSensorDevice(device.getDeviceType()) || DeviceUtil.isNewFiveSensorDevice(device.getDeviceType()))) {
                        onWarning(msg);
                    }
                }
                break;
            case InfoPushType.INVITE_FAMILY_TYPE:
                //家庭成功邀请
                onPushInviteFamily(msg);
                break;
            case InfoPushType.INVITE_FAMILY_RESULT_TYPE:
                //家庭成功邀请结果
                onPushInviteFamilyResult(msg);
                break;
            case InfoPushType.ALLONE_SUBSCRIBE:
                //小方预约执行前5分钟推送
                onProgramMessage(msg);
                break;
        }
    }

    @Override
    public void onActivityResume() {
        //正在升级主机，不显示其他弹框
        if (!ActivityTool.canShowActivity(mActivity)) {
            return;
        }
        for (InfoPushMsg infoPushMsg : infoPushMsgsWarning) {
            Intent intent = new Intent(mActivity, SensorDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        }
        infoPushMsgsWarning.clear();
        for (InfoPushMsg infoPushMsg : infoPushMsgsInvite) {
            Intent intent = new Intent(mActivity, FamilyInviteDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        }
        infoPushMsgsInvite.clear();
        for (InfoPushMsg infoPushMsg : infoPushMsgsInviteResult) {
            Intent intent = new Intent(mActivity, FamilyInviteResponseDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        }
        infoPushMsgsInviteResult.clear();
//        for (InfoPushMsg infoPushMsg : infoPushMsgsSensor) {
//            Intent intent = new Intent(mActivity, SensorDialogActivity.class);
//            intent.putExtra("infoPushMsg", infoPushMsg);
//            mActivity.startActivity(intent);
//        }
//        infoPushMsgsSensor.clear();
        for (InfoPushMsg infoPushMsg : infoPushProgramSubscribe) {
            Intent intent = new Intent(mActivity, ProgramInfoPushDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        }
        infoPushProgramSubscribe.clear();
    }

    @Override
    public void onUnRegister() {
        exitApp();
    }

    private void initPushListener() {
        InfoPushManager.getInstance(mContext).registerWarningListener(this);
        InfoPushManager.getInstance(mContext).registerPushInviteFamilyListener(this);
        InfoPushManager.getInstance(mContext).registerPushInviteFamilyResultListener(this);
        InfoPushManager.getInstance(mContext).registerProgramMessageListener(this);
    }

    @Override
    public void onInfoPush(InfoPushMsg infoPushMsg) {
        MyLogger.kLog().d(infoPushMsg);
        if (infoPushMsg != null) {
            final int infoPushType = infoPushMsg.getInfoType();
            switch (infoPushType) {
                case InfoPushType.HUB_UPDATE:
                    //主机升级推送
                    InfoPushHubUpdateMsg infoPushHubUpdateMsg = infoPushMsg.getInfoPushHubUpdateMsg();
                    MyLogger.kLog().d(infoPushHubUpdateMsg);
                    if (infoPushHubUpdateMsg.getUpgradeStatus() == HubConstant.Upgrade.UPGRADING) {
                        //开始升级主机固件
                        HubUpgradeEvent checkHubUpgradeEvent = new HubUpgradeEvent(infoPushHubUpdateMsg.getUid(), Constant.INVALID_NUM, ErrorCode.SUCCESS, infoPushHubUpdateMsg.getUpgradeStatus());
                        Intent hubUpgradeIntent = new Intent(mActivity, HubUpdateActivity.class);
                        hubUpgradeIntent.putExtra(IntentKey.HUB_UPGRADE_EVENT, checkHubUpgradeEvent);
                        mActivity.startActivity(hubUpgradeIntent);
//                        mActivity.overridePendingTransition(R.anim.top_to_bottom_in_slow, R.anim.top_to_bottom_out_slow);
                    } else {
                        EventBus.getDefault().post(new HubEvent(null, infoPushType, HubConstant.Upgrade.FINISH));
                    }
                    break;
            }
        }
    }

    private void processInfopushMsg(InfoPushMsg infoPushMsg, int infoPushType) {
        switch (infoPushType) {
            case InfoPushType.HUB_UPDATE:
                //主机升级
                break;
        }
    }

    @Override
    public void onWarning(InfoPushMsg infoPushMsg) {
        MyLogger.kLog().d(infoPushMsg);
        if (ActivityTool.canShowActivity(mActivity) && UserManage.getInstance(mContext).isLogined()) {
            Intent intent = new Intent(mActivity, SensorDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        } else {
            infoPushMsgsWarning.add(infoPushMsg);
        }
    }

    @Override
    public void onPushInviteFamily(InfoPushMsg infoPushMsg) {
        MyLogger.kLog().d(infoPushMsg);
        if (ActivityTool.canShowActivity(mActivity) && UserManage.getInstance(mContext).isLogined()) {
            Intent intent = new Intent(mActivity, FamilyInviteDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        } else {
            infoPushMsgsInvite.add(infoPushMsg);
        }
    }

    @Override
    public void onPushInviteFamilyResult(InfoPushMsg infoPushMsg) {
        MyLogger.kLog().d(infoPushMsg);
        if (UserManage.getInstance(mContext).isLogined() && ActivityTool.canShowActivity(mActivity)) {
            Intent intent = new Intent(mActivity, FamilyInviteResponseDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        } else {
            infoPushMsgsInviteResult.add(infoPushMsg);
        }
    }

    @Override
    public void onProgramMessage(InfoPushMsg infoPushMsg) {
        MyLogger.kLog().d(infoPushMsg);
        if (UserManage.getInstance(mContext).isLogined()
                && ActivityTool.canShowActivity(mActivity)) {
            Intent intent = new Intent(mActivity, ProgramInfoPushDialogActivity.class);
            intent.putExtra("infoPushMsg", infoPushMsg);
            mActivity.startActivity(intent);
        } else {
            infoPushProgramSubscribe.add(infoPushMsg);
        }
    }

    private void exitApp() {
        infoPushMsgsWarning.clear();
        infoPushMsgsInvite.clear();
        infoPushMsgsInviteResult.clear();
//        infoPushMsgsSensor.clear();

        InfoPushManager.getInstance(mContext).unregisters();
        InfoPushManager.getInstance(mContext).unregisterWarningListener(this);
    }
}
