package com.orvibo.homemate.smartscene;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SmartScene;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.core.UserManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.LinkageDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.LinkageActiveType;
import com.orvibo.homemate.model.ActivateLinkage;
import com.orvibo.homemate.model.SecurityRequest;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.SmartSceneCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.adapter.IntelligentSceneAdapter;
import com.orvibo.homemate.smartscene.manager.IntelligentSceneManagerActivity;
import com.orvibo.homemate.smartscene.manager.SecurityManagerActivity;
import com.orvibo.homemate.util.GatewayTool;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.SceneAnimDialog;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;
import com.orvibo.homemate.view.dialog.ToastDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能场景
 *
 * @author huangqiyao
 * @date 2015/10/15
 */
public class SmartSceneFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = SmartSceneFragment.class.getSimpleName();
    private static final int WHAT_REFRESH = 1;
    private static final int SHOW_SAMPLE_ALL = 0;
    private static final int SHOW_SAMPLE_SCENE = 1;
    private static final int SHOW_SAMPLE_LINKAGE = 2;

    /**
     * 当主机开关灯光>=3时显示默认的全开和全关情景
     */
    private static final int SHOW_DEFAULT_SCENE_LIGHT_COUNT = 3;

    private IntelligentSceneAdapter mIntelligentSceneAdapter;
    private ControlDevice controlDevice;
    private ActivateLinkage activateLinkage;
    private SecurityRequest securityRequest;
    private Scene mScene;
    private Linkage mLinkage;
    private SecurityDao mSecurityDao;
    private SceneDao sceneDao;
    private DeviceDao mDeviceDao;
    private LinkageDao linkageDao;
    //view
    private View parentView;
    private ListView intelligentSceneListView;
    private Security mSecurity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.intelligent_scene_activity, container, false);
        sceneDao = new SceneDao();
        linkageDao = new LinkageDao();
        mDeviceDao = new DeviceDao();
        mSecurityDao = SecurityDao.getInstance();
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) parentView.findViewById(R.id.srl_progress);
        initProgress(refreshLayout);

        initView();
        initControlScene();
        initActivateLinkage();
        initSecurityRequest();
        return parentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshScenes();
    }

    @Override
    public void onVisible() {
        super.onVisible();
        refreshScenes();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        refreshScenes();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_REFRESH:
                    if (isAdded() && !isDetached()) {
                        List<SmartScene> smartScenes = (List<SmartScene>) msg.obj;
                        LogUtil.d(TAG, "handleMessage()-smartScenes:" + smartScenes);
                        if (mIntelligentSceneAdapter == null) {
                            mIntelligentSceneAdapter = new IntelligentSceneAdapter(getActivity(), smartScenes, SmartSceneFragment.this);
                            mIntelligentSceneAdapter.setMainUid(mainUid);
                            intelligentSceneListView.setAdapter(mIntelligentSceneAdapter);
                        } else {
                            mIntelligentSceneAdapter.refresh(smartScenes);
                        }
                    } else {
                        LogUtil.w(TAG, "handleMessage()-Fragment has been removed.");
                    }
                    break;
            }
        }
    };

    /**
     * 刷新数据线程
     */
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mainUid = UserCache.getCurrentMainUid(context);
            String userName = UserCache.getCurrentUserName(context);
            LogUtil.d(TAG, "run()-userName:" + userName + ",mainUid:" + mainUid);
            SmartScene sampleScene = new SmartScene();
            sampleScene.scene = getSampleScene();
            SmartScene sampleLinkage = new SmartScene();
            sampleLinkage.linkage = getSampleLinkage();
            ArrayList<SmartScene> smartScenes = new ArrayList<>();
            if (!StringUtil.isEmpty(userName)) {
                int lampCount = mDeviceDao.selZigbeeLampCount(mainUid);
                boolean shown = false;
                //判断默认情景的显示状态
                boolean showed = SmartSceneCache.isDefaultSceneShowed(mainUid);
                //默认情景没有显示并且灯光数超过3个时：
                if (!showed && lampCount >= SHOW_DEFAULT_SCENE_LIGHT_COUNT) {
                    shown = true;
                    SmartSceneCache.saveDefaultSceneShow(mainUid, shown);
                } else {
                    shown = showed;
                }
                //包含默认情景的普通情景集
                List<Scene> scenes = sceneDao.selScenes(mainUid, shown);
                //联动情景集
//                List<Linkage> linkages = linkageDao.selAllLinkages(mainUid);
                String userId = UserCache.getCurrentUserId(mAppContext);
                List<Linkage> linkages = linkageDao.selAllLinkagesByUserId(userId);
                //普通情景和联动情景都为空时添加“回家模式”和“路过—灯亮模式”
                if ((scenes == null || scenes.isEmpty()) && (linkages == null || linkages.isEmpty())) {
                    smartScenes.add(sampleScene);
                    smartScenes.add(sampleLinkage);
                    smartScenes.addAll(addSecurtyModel());
                } else {
                    boolean isUserScene = false;
                    //遍历普通模式集:
                    for (Scene scene : scenes) {
                        //判断是否有自定义模式：
                        if (scene.getOnOffFlag() == 2) {
                            isUserScene = true;
                            break;
                        }
                    }
                    //false并且Linkages表为empty：
                    if (!isUserScene && linkages.isEmpty()) {
                        smartScenes.add(sampleScene);
                        smartScenes.add(sampleLinkage);
                    }
                    smartScenes.addAll(addSecurtyModel());
                    //true并且普通模式表!=null:把普通的模式添加进智能情景集
                    if (!scenes.isEmpty()) {
                        for (Scene scene : scenes) {
                            SmartScene smartScene = new SmartScene();
                            smartScene.scene = scene;
                            smartScenes.add(smartScene);
                        }
                    }
                    //true 并且联动表!=null:把联动的模式添加进智能情景集
                    if (!linkages.isEmpty()) {
                        for (Linkage linkage : linkages) {
                            SmartScene smartLinkage = new SmartScene();
                            smartLinkage.linkage = linkage;
                            smartScenes.add(smartLinkage);
                        }
                    }
                }
//                else if (hasInfraredSensor || hasDoorContact || hasSmokeSensor || hasCoSensor || hasFlammableSecsor || hasWaterSensor) {
//                    SmartScene leaveHomeSecurity = new SmartScene();
//                    leaveHomeSecurity.security = getLeaveHomeSecurity();
//                    if (leaveHomeSecurity.security != null) {
//                        smartScenes.add(leaveHomeSecurity);
//                    }
//
//                }
            } else {
                //用户没有登录时也添加无效模式
                smartScenes.add(sampleScene);
                smartScenes.add(sampleLinkage);
            }
            Message msg = mHandler.obtainMessage(WHAT_REFRESH);
            msg.obj = smartScenes;
            mHandler.sendMessage(msg);
        }
    };

    /**
     * @return
     */
    private List<SmartScene> addSecurtyModel() {
        List<SmartScene> smartScenes = new ArrayList<>();
        // 当添加的传感器中有人体红外，出现离家安防模式和在家安防模式
        boolean hasInfraredSensor = mDeviceDao.hasDevice(mainUid, DeviceType.INFRARED_SENSOR);
        // 当添加的传感器中只有门窗传感器，烟雾报警器，一氧化碳报警器，可燃气体报警器或水浸探测器，只有离家安防模式。
        boolean hasDoorContact = mDeviceDao.hasDevice(mainUid, DeviceType.MAGNETIC)
                || mDeviceDao.hasDevice(mainUid, DeviceType.MAGNETIC_WINDOW)
                || mDeviceDao.hasDevice(mainUid, DeviceType.MAGNETIC_DRAWER)
                || mDeviceDao.hasDevice(mainUid, DeviceType.MAGNETIC_OTHER);
        boolean hasSmokeSensor = mDeviceDao.hasDevice(mainUid, DeviceType.SMOKE_SENSOR);
        boolean hasCoSensor = mDeviceDao.hasDevice(mainUid, DeviceType.CO_SENSOR);
        boolean hasFlammableSecsor = mDeviceDao.hasDevice(mainUid, DeviceType.FLAMMABLE_GAS);
        boolean hasWaterSensor = mDeviceDao.hasDevice(mainUid, DeviceType.WATER_SENSOR);

        if (hasInfraredSensor || hasDoorContact || hasSmokeSensor || hasCoSensor || hasFlammableSecsor || hasWaterSensor) {
            SmartScene atHomeSecurity = new SmartScene();
            SmartScene leaveHomeSecurity = new SmartScene();
            atHomeSecurity.security = getAtHomeSecurity();
            leaveHomeSecurity.security = getLeaveHomeSecurity();
//            if (getIsLeaveHomeArming()) {
//                atHomeSecurity.security.setIsArm(Security.UNSECURITY);
//                leaveHomeSecurity.security.setIsArm(Security.SECURITY);
//            }
            if (leaveHomeSecurity.security != null) {
                smartScenes.add(leaveHomeSecurity);
            }
            if (atHomeSecurity.security != null) {
                smartScenes.add(atHomeSecurity);
            }
        }
        return smartScenes;
    }

    private Scene getSampleScene() {
        Scene scene = new Scene();
        scene.setSceneNo(Constant.INVALID_NUM + "");
        scene.setSceneId(Constant.INVALID_NUM);
        if (context != null) {
            scene.setSceneName(context.getString(R.string.intelligent_scene_go_home));
        }
//        List<Scene> scenes = new ArrayList<Scene>();
//        scenes.add(scene);
//        listSet.add(scenes);
//        SmartScene smartScene = new SmartScene();
//        smartScene.setType(SmartScene.TYPE_SCENE);
//        smartScene.setId(Constant.INVALID_NUM + "");
//        smartScene.setName(getString(R.string.intelligent_scene_go_home));
        return scene;
    }

    private Linkage getSampleLinkage() {
        Linkage linkage = new Linkage();
        if (isAdded() && !isDetached()) {
            linkage.setLinkageId(Constant.INVALID_NUM + "");
            linkage.setLinkageName(getString(R.string.intelligent_scene_open_light));
            linkage.setIsPause(LinkageActiveType.PAUSE);
        }
//        List<Linkage> linkages = new ArrayList<Linkage>();
//        linkages.add(linkage);
//        listSet.add(linkages);
        return linkage;
    }

    private Security getAtHomeSecurity() {
        Security atHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.AT_HOME);
        return atHomeSecurity;
    }

    private Security getLeaveHomeSecurity() {
        Security leaveHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.LEAVE_HOME);
        return leaveHomeSecurity;
    }

    private void initView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        parentView.findViewById(R.id.add_iv).setOnClickListener(this);
        View view = inflater.inflate(R.layout.intelligent_list_head_foot_item, null);
        intelligentSceneListView = (ListView) parentView.findViewById(R.id.intelligentSceneListView);
        intelligentSceneListView.setOnItemClickListener(this);
        intelligentSceneListView.addHeaderView(view, null, false);
        intelligentSceneListView.addFooterView(view, null, false);
    }


    private void refreshScenes() {
        LogUtil.d(TAG, "refreshScenes()");
        mHandler.post(mRefreshRunnable);
    }


    private void showSample() {
        if (mIntelligentSceneAdapter != null) {
            mIntelligentSceneAdapter = null;
            intelligentSceneListView.setAdapter(mIntelligentSceneAdapter);
        }
    }

    private void initControlScene() {
        controlDevice = new ControlDevice(mAppContext) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                LogUtil.d(TAG, "onControlDeviceResult()-thread:" + Thread.currentThread());
//                dismissDialog();
                stopProgress();
                if (result == ErrorCode.SUCCESS) {
                    String content = String.format(mAppContext.getString(R.string.scene_control_success), mScene.getSceneName());
                    ToastDialog.show(context, content);//3s后关闭dialog
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initSecurityRequest() {
        securityRequest = new SecurityRequest(mAppContext) {
            @Override
            public void onSecurityRequestResult(String uid, int result) {
                LogUtil.d(TAG, "onSecurityRequestResult():result=" + result);
//                dismissDialog();
                stopProgress();
                if (result == ErrorCode.SUCCESS) {
                    mSecurity=mSecurityDao.selSecurityBySecurityId(mainUid, mSecurity.getSecurityId());
                    int arm = mSecurity.getIsArm();
                    if (arm == ArmType.ARMING) {
                        if (mSecurity.getSecType() == Security.AT_HOME) {
                            String content = getResources().getString(R.string.intelligent_scene_securityed);
                            ToastDialog.show(context, content);
                        }

                    } else if (arm == ArmType.DISARMING) {
                        String content = getResources().getString(R.string.intelligent_scene_unsecurity);
                        ToastDialog.show(context, content);
                    }

                    refreshScenes();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    private void initActivateLinkage() {
        activateLinkage = new ActivateLinkage(mAppContext) {
            @Override
            public void onActivateLinkageResult(String uid, int serial, int result) {
                LogUtil.d(TAG, "onActivateLinkageResult()-thread:" + Thread.currentThread() + ",result:" + result);
//                dismissDialog();
                stopProgress();
                if (result == ErrorCode.SUCCESS) {
                    refreshScenes();
                    int isPause = linkageDao.selLinkage(uid, mLinkage.getLinkageId()).getIsPause();
                    if (isPause == LinkageActiveType.ACTIVE) {
                        String content = getResources().getString(R.string.intelligent_scene_activated);
                        ToastDialog.show(context, content);
                    } else if (isPause == LinkageActiveType.PAUSE) {
                        String content = getResources().getString(R.string.intelligent_scene_deactived);
                        ToastDialog.show(context, content);
                    }
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mIntelligentSceneAdapter != null) {
//            if (!isLocalModel()) {
//                ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
//                return;
//            }

            //加了一个headView，所以需要减一，并且只有联动的情景和用户自定义才有跳转动作
            Object object = mIntelligentSceneAdapter.getItem(position - 1);
            if (object != null && object instanceof SmartScene) {
                SmartScene smartLinkage = (SmartScene) object;
                Linkage linkage = smartLinkage.linkage;
                if (linkage != null && !(Constant.INVALID_NUM + "").equals(linkage.getLinkageId())) {
                    Intent intent = new Intent(getActivity(), IntelligentSceneManagerActivity.class);
                    intent.putExtra(Constant.LINKAGE, linkage);
                    startActivity(intent);
                }
                Scene scene = smartLinkage.scene;
                if (scene != null && !(Constant.INVALID_NUM == scene.getSceneId())) {
                    if (LibSceneTool.isSystemScene(scene.getOnOffFlag())) {
                        ToastUtil.showToast(R.string.scene_modify_system_error);
                    } else {
                        Intent intent = new Intent(getActivity(), IntelligentSceneManagerActivity.class);
                        intent.putExtra(Constant.SCENE, scene);
                        startActivity(intent);
                    }
                }
                Security security = smartLinkage.security;
                if (security != null && !(Constant.INVALID_NUM + "").equals(security.getSecurityId())) {
                    Intent intent = new Intent(getActivity(), SecurityManagerActivity.class);
                    intent.putExtra(Constant.SECURITY, security);
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 判断是否本地操作。1.10不再判断本地模式
     */
    private boolean isLocalOperate() {
//        if (NetUtil.isLocalLan(getActivity(), UserCache.getCurrentMainUid(getActivity()))) {
//            return true;
//        } else {
//            ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
//            return false;
//        }

        return true;
    }

    @Override
    public void onClick(View view) {
        final int vId = view.getId();
        if (vId == R.id.startTextView) {
            //控制情景
            Scene scene = (Scene) view.getTag(R.id.tag_scene);
            if ((Constant.INVALID_NUM + "").equals(scene.getSceneNo())) {
                SceneAnimDialog.newInstance(R.layout.scene_anim_dialog, R.raw.scene_home, (float) 0.4,
                        context.getString(R.string.anim_scene_home_tip), null).show(getFragmentManager(), null);
            } else {
                mScene = scene;
                LogUtil.d(TAG, "onClick()-scene:" + mScene);
//                showDialog();
                showProgress();
                controlDevice.controlScene(mainUid, mScene.getSceneId());
            }
//        } else if (vId == R.id.activateImageView) {
        } else if (vId == R.id.action_tv) {
            //控制联动
            Linkage linkage = (Linkage) view.getTag(R.id.tag_linkage);
            if ((Constant.INVALID_NUM + "").equals(linkage.getLinkageId())) {
                int te = Integer.valueOf(view.getContentDescription() + "");
                boolean active = te == LinkageActiveType.ACTIVE;
                if (mIntelligentSceneAdapter != null) {
                    mIntelligentSceneAdapter.refreshSampleLinkage(!active);
                }
                if (!active) {
                    SceneAnimDialog.newInstance(R.layout.scene_linkage_anim_dialog, R.raw.scene_link_start, (float) 1,
                            context.getString(R.string.anim_scene_open_tip),
                            getString(R.string.anim_scene_open_tip1)).show(getFragmentManager(), null);
                } else {
                    SceneAnimDialog.newInstance(R.layout.scene_linkage_anim_dialog, R.raw.scene_link_stop, (float) 1,
                            context.getString(R.string.anim_scene_close_tip), getString(R.string.anim_scene_close_tip1)).show(getFragmentManager(), null);
                }
            } else {
                mLinkage = linkage;
                LogUtil.d(TAG, "onClick()-linkage:" + mLinkage);
//                if (!isLocalModel()) {
//                    ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
//                    return;
//                }
                showProgress();
//                showDialog();
                if (linkage.getIsPause() == LinkageActiveType.ACTIVE) {
                    activateLinkage.startActivateLinkage(StringUtil.isEmpty(linkage.getUid()) ? mainUid : linkage.getUid(), UserCache.getCurrentUserName(mAppContext), linkage.getLinkageId(), LinkageActiveType.PAUSE);
                } else if (linkage.getIsPause() == LinkageActiveType.PAUSE) {
                    activateLinkage.startActivateLinkage(StringUtil.isEmpty(linkage.getUid()) ? mainUid : linkage.getUid(), UserCache.getCurrentUserName(mAppContext), linkage.getLinkageId(), LinkageActiveType.ACTIVE);
                }

            }
        } else if (vId == R.id.securityTextView) {
            //布撤防控制
            mSecurity = (Security) view.getTag(R.id.tag_security);
            LogUtil.d(TAG, "onClick()-security:" + mSecurity);
//            Security atHomeSecurity;
//            Security leaveHomeSecurity;
//            showDialog();
            showProgress();

            // 同一时间只能有一个安防模式处于布防状态，客户端只发布防指令，
            // 主机判断另一个安防模式是否处于布防状态，如果是则修改布防状态为撤防，同时布防当前安防模式。
            // 客户端接收到布防成功返回后也要做对应的处理
            if (mSecurity.getIsArm() == ArmType.ARMING) {
                securityRequest.startSecurityRequest(mainUid, UserCache.getCurrentUserName(mAppContext),
                        mSecurity.getSecurityId(), ArmType.DISARMING, 0);
                view.setTag(R.id.securityTextView,null);
            } else if (mSecurity.getIsArm() == ArmType.DISARMING) {
                securityRequest.startSecurityRequest(mainUid, UserCache.getCurrentUserName(mAppContext),
                        mSecurity.getSecurityId(), ArmType.ARMING, mSecurity.getSecType() == Security.AT_HOME ? 0 : 60);
                view.setTag(R.id.securityTextView,mSecurity);
            }

        } else if (vId == R.id.add_iv) {

            /*未登录APP:
            点击【添加智能场景】，先提示登录。

            登录APP:
            如果帐号下没有设备，点击【添加智能场景】，可以进入添加页面，点击【启动条件】，提示：您还没有可添加的设备哦~
            如果帐号下只有WiFi设备，点击【添加智能场景】，可以进入添加页面，点击【启动条件】，提示：您的设备暂不支持智能场景，程序猿哥哥还在拼命开发中...
            如果帐号下有主机或者小主机，点击【添加智能场景】，判断是不是本地操作：
            1.如果不是本地，toast提示：仅限本地操作。
            2.如果是本地，允许进入添加页面,点击【启动条件】
            1）有人体传感器，门窗传感器和支持联动的门锁，显示相应条件；
            2）无条件则提示：您还没有可添加的设备哦~
            3)   只有人体传感器,门窗传感器，门锁，添加执行任务时，提示：您还没有可添加的设备哦~
            3)   只有人体传感器,门窗传感器，门锁和WiFi设备时，添加执行任务时，提示：您的设备暂不支持智能场景，程序猿哥哥还在拼命开发中...*/
            if (UserManage.getInstance(mAppContext).isLogined()) {
                //1.7版本的神交互
//                如果帐号下没有设备(包括主机)，点击【添加智能场景】，可以进入添加页面
//                如果帐号下只有WiFi设备，点击【添加智能场景】，可以进入添加页面
//                如果帐号下有主机或者小主机，点击【添加智能场景】，判断是不是本地操作：
//                1.如果不是本地，toast提示：仅限本地操作。
//                2.如果是本地，允许进入添加页面，点击【添加执行任务】，如果没有可被添加的设备，提示：您还没有支持智能场景的设备哦~

//                if (!TextUtils.isEmpty(mainUid) && !isLocalModel()) {
//                    ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
//                    return;
//                }

                //有主机，不再判断是否处于本地模式。小方支持联动
//                if (mainUid != null) {
//                    int result = GatewayTool.localCheck(mainUid);
//                    if (result != ErrorCode.SUCCESS) {
//                        ToastUtil.toastError(result);
//                        return;
//                    }
//                }
                Intent intent = new Intent(getActivity(), IntelligentSceneManagerActivity.class);
                startActivity(intent);
            } else {
                //如果没有登录，先提示登录
                showLoginDialog();
            }
        }
    }

    /**
     * 判断是否为本地模式。联动和场景的修改需要跟主机同一个局域网才能操作。
     *
     * @return
     */
    private boolean isLocalModel() {
        return !TextUtils.isEmpty(mainUid) && GatewayTool.localCheck(mainUid) == ErrorCode.SUCCESS;
//        if (mainUid != null) {
//            int result = GatewayTool.localCheck(mainUid);
//            if (result != ErrorCode.SUCCESS) {
//              //  ToastUtil.toastError(result);
//                return false;
//            } else {
//                return true;
//            }
//        }
//        return false;
    }

    @Override
    public void onRightButtonClick(View view) {
        super.onRightButtonClick(view);
    }

    @Override
    public void onDestroy() {
        if (controlDevice != null) {
            controlDevice.stopControl();
        }
        if (activateLinkage != null) {
            activateLinkage.cancel();
        }
        if (securityRequest != null) {
            securityRequest.cancel();
        }
        super.onDestroy();
    }

}