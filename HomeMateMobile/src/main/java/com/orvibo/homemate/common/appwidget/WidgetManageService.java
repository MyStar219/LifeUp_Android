package com.orvibo.homemate.common.appwidget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnNewPropertyReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.common.appwidget.db.WidgetDao;
import com.orvibo.homemate.dao.BaseDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.SecurityRequest;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DeviceSort;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 该service为widget服务
 */
public class WidgetManageService extends Service implements OnNewPropertyReportListener {
    private static final int    WHAT_REFRESH        = 1;
    private static final int    WHAT_CLOSE_ALL      = 2;
    private int energyRemindDevicesCount;
    public static String ACTION = "to_service";

    private String TAG = WidgetManageService.class.getSimpleName();

    private DeviceDao deviceDao;
    private WidgetDao widgetDao ;
    private WidgetItem mWidgetItem;
    private WidgetItem security;
    private ControlDevice controlDevice;
    private DeviceStatusDao deviceStatusDao;

    private SecurityRequest securityRequest;
    private SecurityDao mSecurityDao;



    private List<WidgetItem> sceneWidgetList  ;
    private List<WidgetItem> deviceWidgetList ;
    private List<WidgetItem> linkageWidgetList;
    private List<WidgetItem> energyWidgetList;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_REFRESH:
                    HashMap<String ,List<WidgetItem>> hashMap =
                            (HashMap<String ,List<WidgetItem>>) msg.obj;
                        postState(WidgetManageService.this,hashMap);
                    break;
                case  WHAT_CLOSE_ALL:
                    energyRemindDevicesCount--;
                    if(energyWidgetList!=null) {
                        if (energyRemindDevicesCount >= 0 && energyRemindDevicesCount < energyWidgetList.size()) {
                            WidgetItem device = energyWidgetList.get(energyRemindDevicesCount);
                            controlDevice.off(device.getUid(), device.getDeviceId());
                        }
                    }
                    if (energyRemindDevicesCount > 0) {
                        if (mHandler.hasMessages(WHAT_CLOSE_ALL)) {
                            mHandler.removeMessages(WHAT_CLOSE_ALL);
                        }
                        mHandler.sendEmptyMessageDelayed(WHAT_CLOSE_ALL, 100);
                    }
            }
        }
    };

    private void initControlScene() {
        controlDevice = new ControlDevice(WidgetManageService.this) {
            @Override
            public void onControlDeviceResult(String uid, String deviceId, int result) {
                LogUtil.d(TAG, "onControlDeviceResult()-thread:" + Thread.currentThread());
                if (result == ErrorCode.SUCCESS) {
                    if(mWidgetItem.getTyple().equals("device")) {
                        DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
                        if (deviceStatus.getValue1() == 0) {
                            ToastUtil.showToast(mWidgetItem.getWidgetName() + "关闭了");
                        } else {
                            ToastUtil.showToast(mWidgetItem.getWidgetName() + "打开了");
                        }
                    }else if(mWidgetItem.getTyple().equals("scene")){
                        String content = String.format(mContext.getString(R.string.scene_control_success), mWidgetItem.getWidgetName());
                        ToastUtil.showToast(content);//3s后关闭dialog
                    }
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }


    private void initSecurityRequest() {
        securityRequest = new SecurityRequest(WidgetManageService.this) {
            @Override
            public void onSecurityRequestResult(String uid, int result) {
                if (result == ErrorCode.SUCCESS) {
                    security = mWidgetItem;
                    String mainUid  = UserCache.getCurrentMainUid(WidgetManageService.this);
                    int arm = mSecurityDao.selSecurityBySecurityId(mainUid, mWidgetItem.getDeviceId()).getIsArm();
                    if (arm == ArmType.ARMING) {
                        if (mWidgetItem.getDeviceType() == Security.AT_HOME||mWidgetItem.getDeviceType() == Security.LEAVE_HOME) {
                            String content = getResources().getString(R.string.intelligent_scene_securityed);
                            ToastUtil.showToast(content);
                        }

                    } else if (arm == ArmType.DISARMING) {
                        String content = getResources().getString(R.string.intelligent_scene_unsecurity);
                        ToastUtil.showToast(content);
                    }
                    mHandler.postDelayed(mRefreshRunnable,0);

                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        deviceDao = new DeviceDao();
        widgetDao = new WidgetDao(WidgetManageService.this);
        deviceStatusDao = new DeviceStatusDao();
        BaseDao.initDB(WidgetManageService.this);
        mSecurityDao = SecurityDao.getInstance();

        initControlScene();
        initSecurityRequest();

        PropertyReport.getInstance(WidgetManageService.this).registerNewPropertyReport(this);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mHandler.postDelayed(mRefreshRunnable,0);

        return super.onStartCommand(intent, flags, startId);
    }

    public final void onEventMainThread(WidgetUpdateEvent event) {
        LogUtil.d(TAG, "onEventMainThread()-WidgetUpdateEvent:" + event);
        int type =  event.getType();
        Log.e(TAG,type+"");
        if(event!=null){
            switch (type){
                case 0:
                    //用来刷新widget的数据
                    mHandler.postDelayed(mRefreshRunnable,0);
                    break;
                case 1:
                    controlScene(event.getPosition());
                    break;
                case 2:
                    controlCmd(event.getPosition());
                    break;
                case 3:
                    controlSafe(event.getPosition());
                    break;
                case 4:
                    disArm();
                    break;
                case 5:
                    allDampClose();
                    break;
            }

        }


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void postState(Context context, HashMap<String ,List<WidgetItem>> hashMap) {

        if(hashMap!=null){
            Intent intent = new Intent();
            intent.setClass(context, SmartWidgetProvider.class);
            intent.setAction(SmartWidgetProvider.MAIN_UPDATE_UI);
            intent.putExtra("hashmap", hashMap);
            context.sendBroadcast(intent);
        }

    }

    //发送控制设备命令
    private void controlCmd(int position){
        if(deviceWidgetList!=null) {
            WidgetItem widgetItem =deviceWidgetList.get(position);
            if(widgetItem!=null) {
                mWidgetItem = widgetItem;
                if (mWidgetItem.getTyple().equals("device")) {
                    DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(mWidgetItem.getUid(), mWidgetItem.getDeviceId());
                    if (deviceStatus.getValue1() == 0) {
                        controlDevice.off(mWidgetItem.getUid(), mWidgetItem.getDeviceId());
                    } else {
                        controlDevice.on(mWidgetItem.getUid(), mWidgetItem.getDeviceId());
                    }
                }
            }
        }
    }

    //控制情景模式
    private void controlScene(int position){
        if(sceneWidgetList!=null) {
            WidgetItem widgetItem = sceneWidgetList.get(position);
            if(widgetItem!=null) {
                mWidgetItem = widgetItem;
                if (mWidgetItem.getTyple().equals("scene")) {
                    controlDevice.controlScene(mWidgetItem.getUid(), mWidgetItem.getTableId());
                }
            }
        }
    }
    //控制安防
    private void controlSafe(int position){
        //用来布防的
       if(linkageWidgetList!=null){
           WidgetItem widgetItem = linkageWidgetList.get(position);
        if(widgetItem!=null) {
            mWidgetItem = widgetItem;
            String mainUid = UserCache.getCurrentMainUid(WidgetManageService.this);
            String userName = UserCache.getCurrentUserName(WidgetManageService.this);
            if (mWidgetItem.getIsArm() == ArmType.ARMING) {
                return;
            } else if (mWidgetItem.getIsArm() == ArmType.DISARMING) {
                securityRequest.startSecurityRequest(mainUid, userName,
                        mWidgetItem.getDeviceId(), ArmType.ARMING, mWidgetItem.getDeviceType() == Security.AT_HOME ? 0 : 60);
            }
        }
        }
    }

    //撤防
    private void disArm(){
        String mainUid = UserCache.getCurrentMainUid(WidgetManageService.this);
        String userName = UserCache.getCurrentUserName(WidgetManageService.this);
        securityRequest.startSecurityRequest(mainUid, userName,
                security.getDeviceId(), ArmType.DISARMING, 0);
    }

    //全关灯
    private void allDampClose(){
        if(energyWidgetList!=null) {
            //如果需要关闭的灯的数量为0，不操作
            if(energyWidgetList.size()==0)
            {
                return;
            }
            energyRemindDevicesCount = energyWidgetList.size();
            if (mHandler.hasMessages(WHAT_CLOSE_ALL)) {
                mHandler.removeMessages(WHAT_CLOSE_ALL);
            }
            mHandler.sendEmptyMessage(WHAT_CLOSE_ALL);
        }
    }

    //返回未关电器的数量
    private void  refreshDeviceList() {
        if(energyWidgetList==null)
        {
            energyWidgetList = new ArrayList<>();
        }
        energyWidgetList.clear();
        List<Device> devices = new ArrayList<>();
        String mainUid = UserCache.getCurrentMainUid(WidgetManageService.this);
        devices = (ArrayList) deviceDao.selNeedCloseZigbeeLampsDevices(mainUid);
        DeviceSort.sortDevices(devices,null,false,false,false);
        for (Device device : devices) {
            String uid = device.getUid();
            DeviceStatus deviceStatus;
            deviceStatus = deviceStatusDao.selDeviceStatus(uid, device);
            if (deviceStatus != null) {
                if (deviceStatus.getValue1() == DeviceStatusConstant.ON && deviceStatus.getOnline() == OnlineStatus.ONLINE) {
                    WidgetItem widgetItem = new WidgetItem();
                    widgetItem.setTyple("device");
                    widgetItem.setUid(device.getUid());
                    widgetItem.setDeviceType(device.getDeviceType());
                    widgetItem.setDeviceId(device.getDeviceId());
                    energyWidgetList.add(widgetItem);
                }
            }
        }

    }

    /**
     * 刷新数据线程
     */
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            String userName = UserCache.getCurrentUserName(WidgetManageService.this);

            if ( !StringUtil.isEmpty(userName)) {
                loadData();
            }

        }
    };

    @Override
    public void onNewPropertyReport(Device device, DeviceStatus deviceStatus, PayloadData payloadData) {
        //接收到属性报告后刷新界面
        EventBus.getDefault().post(new WidgetUpdateEvent(0));
    }

    private void loadData(){
        String mainUid= UserCache.getCurrentMainUid(WidgetManageService.this);
        String userId = UserCache.getCurrentUserId(WidgetManageService.this);
        HashMap<String ,List<WidgetItem>>  freshHashMap = new HashMap<>();
         deviceWidgetList =  widgetDao.selItemByTyple(userId,"device");
         sceneWidgetList  =  widgetDao.selItemByTyple(userId,"scene");
         linkageWidgetList=  new ArrayList<>();


        if(deviceWidgetList!=null){
            for (WidgetItem widgetItem:deviceWidgetList) {
                if (widgetItem.getTyple().equals("device")) {
                    DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(widgetItem.getUid(), widgetItem.getDeviceId());
                    if (deviceStatus != null) {
                        if (deviceStatus.getValue1() == DeviceStatusConstant.ON) {
                            widgetItem.setStatus("0");
                        } else {
                            widgetItem.setStatus("1");
                        }
                    }
                }
            }
        }

        WidgetItem widgetItem1 = security2Widget(getAtHomeSecurity(mainUid));
        WidgetItem widgetItem2 = security2Widget(getLeaveHomeSecurity(mainUid));
        if(widgetItem1!=null){
            linkageWidgetList.add(widgetItem1);
        }
        if(widgetItem2!=null){
            linkageWidgetList.add(widgetItem2);
        }

        refreshDeviceList();
        freshHashMap.put("device", deviceWidgetList);
        freshHashMap.put("scene",  sceneWidgetList);
        freshHashMap.put("linkage",linkageWidgetList);
        freshHashMap.put("energy",energyWidgetList);


        Message msg = mHandler.obtainMessage(WHAT_REFRESH);
        msg.obj = freshHashMap;
        mHandler.sendMessage(msg);

    }

    private WidgetItem security2Widget(Security security){
        if(security!=null){
            WidgetItem widgetItem = new WidgetItem();
            widgetItem.setWidgetName(security.getSecurityName());
            widgetItem.setDeviceId(security.getSecurityId());
            widgetItem.setIsArm(security.getIsArm());
            widgetItem.setDeviceType(security.getSecType());
            widgetItem.setTyple("security");
            return widgetItem;
        }
        return null;
    }
    private Security getAtHomeSecurity(String mainUid) {
        if(!StringUtil.isEmpty(mainUid)) {
            Security atHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.AT_HOME);
            return atHomeSecurity;
        }
        return null;
    }

    private Security getLeaveHomeSecurity(String mainUid) {
        if(!StringUtil.isEmpty(mainUid)) {
            Security leaveHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.LEAVE_HOME);
            return leaveHomeSecurity;
        }
        return null;
    }
}
