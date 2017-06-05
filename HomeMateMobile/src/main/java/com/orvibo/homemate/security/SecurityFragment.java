package com.orvibo.homemate.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.core.InfoPushManager;
import com.orvibo.homemate.core.NetChangeHelper;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CameraInfoDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.CameraType;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.model.SecurityRequest;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.NetworkUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DragOrClickItemView;
import com.orvibo.homemate.view.dialog.ToastDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wuliquan on 2016/7/14.
 * 该类主要用来设置安防
 */
public class SecurityFragment extends BaseFragment implements InfoPushManager.OnWarningListener,DragOrClickItemView.SecurityStatusListener,
         NetChangeHelper.OnNetChangedListener, DragOrClickItemView.SecurityArmListener {

    private final static String SHAREFERENCE_NAME = "SECURITY_STATUS";
    //以下两个为主机软件版本号，比该版本高级的才支持安放表报警字段
    private final static String HUB_COMPAR_VERSION = "1.3.8.6";
    private final static String VICENTR_COMPAR_VERSION = "1.6.4.6";


    private DragOrClickItemView dragorclickitemview;
    private TextView security_status_title,security_status_msg;
    private ImageView setting_right_img;
    private View parentView;
    private RelativeLayout title_layout;
    private Security mSecurity;
    private DeviceDao mDeviceDao;
    private SecurityDao mSecurityDao;
    private Security atHomeSecurity;
    private Security leaveHomeSecurity;
    private SecurityRequest securityRequest;
    private int countTime = 60;
    private int MSG = 0x001;
    private boolean isReadArm;
    private boolean isOldVersion;

    private boolean isStop;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG){
                countTime--;
                if(countTime<0){
                    isReadArm = false;
                    if(mSecurity!=null){
                    mSecurity.setIsArm(ArmType.ARMING);}
                    if(isFragmentVisible()) {
                        mHandler.removeCallbacksAndMessages(MSG);
                        String content = getActivity().getResources().getString(R.string.intelligent_scene_securityed);
                        ToastUtil.showToast(content);
                        loadSecurtyModel();

                    }
                }else{
                    isReadArm = true;
                    dragorclickitemview.setProgress(true);
                    security_status_msg.setVisibility(View.VISIBLE);
                    if(isFragmentVisible()) {
                        security_status_title.setText(getResources().getString(R.string.out_home_ing));
                        if(PhoneUtil.isCN()){
                        security_status_msg.setText(countTime + getResources().getString(R.string.security_delay_tip));}
                        else{
                            security_status_msg.setText(getResources().getString(R.string.security_delay_tip)+" "+countTime+" sec");
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(MSG,1000);
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InfoPushManager.getInstance(mAppContext).registerWarningListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainUid = UserCache.getCurrentMainUid(getActivity());

        parentView = inflater.inflate(R.layout.fragment_security, container, false);
        title_layout = (RelativeLayout) parentView.findViewById(R.id.title_layout);
        dragorclickitemview = (DragOrClickItemView) parentView.findViewById(R.id.dragorclickitemview);
        setting_right_img = (ImageView) parentView.findViewById(R.id.setting_right_img);
        security_status_title = (TextView) parentView.findViewById(R.id.security_status_title);
        security_status_msg = (TextView) parentView.findViewById(R.id.security_status_msg);
        dragorclickitemview.setStop();

        setting_right_img.setOnClickListener(this);
        dragorclickitemview.setStatusListener(this);
        dragorclickitemview.setArmListener(this);

        mDeviceDao = new DeviceDao();
        mSecurityDao = SecurityDao.getInstance();
        initSecurityRequest();
        return parentView;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.setting_right_img){
            if(!isCanDefence()){
                showTipDialog();
                return;
            }
            Intent intent = new Intent(context, SecuritySettingActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isStop = false;
        if(this.isVisible()){
            dragorclickitemview.reStart();
        }
        if(!isReadArm){
            loadSecurtyModel();
            getStatus();}

    }
    protected boolean isFragmentVisible() {
        return  isVisible();
    }

    /**
     *
     */
    @Override
    public void onVisible() {
        isStop = false;
        super.onVisible();
        loadSecurtyModel();
        dragorclickitemview.reStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            dragorclickitemview.setStop();
        }
    }

    @Override
    public void onRefresh() {
        if(!isReadArm){
        loadSecurtyModel();}
        if(isVisible()&&!isStop) {
            dragorclickitemview.reStart();
        }

    }

    @Override
    public void onStop() {
        isStop = true;
        super.onStop();
        if(isReadArm){
        saveStatus(countTime);}
        mHandler.removeCallbacksAndMessages(MSG);
        dragorclickitemview.setStop();

    }

    /**
     * 判断是否是旧版主机
     * @return
     */
    private boolean judgeOldHost(){

        if(!StringUtil.isEmpty(mainUid)){
            Gateway gateway = new GatewayDao().selGatewayByUid(mainUid);
            if(gateway!=null) {
                String model = gateway.getModel();
                if (model != null) {
                    //软件版本号
                    String softVersion = gateway.getSoftwareVersion();
                    //如果是大主机
                    if (ProductManage.getInstance().isVicenter300Hub(mainUid, model)) {
                        return isOldVersion(softVersion, VICENTR_COMPAR_VERSION);
                    }
                    if (ProductManage.getInstance().isHub(mainUid, model)) {
                        return isOldVersion(softVersion, HUB_COMPAR_VERSION);
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param version
     * @param comparVersion
     * @return
     */
    private boolean isOldVersion(String version,String comparVersion){
        boolean isOld =true;
        String [] ver = version.split("\\.");
        String [] comparVer = comparVersion.split("\\.");

        if(ver.length>=comparVer.length){
            for(int i=0;i<comparVer.length;i++){
                if(Integer.parseInt(ver[i])>Integer.parseInt(comparVer[i])){
                    isOld = false;
                    break;
                }
                else if(Integer.parseInt(ver[i])<Integer.parseInt(comparVer[i])){
                    isOld = true;
                    break;
                }

            }
        }
        else {
            for(int i=0;i<ver.length;i++){
                if(Integer.parseInt(ver[i])>Integer.parseInt(comparVer[i])){
                    isOld = false;
                    break;
                }
                else if(Integer.parseInt(ver[i])<Integer.parseInt(comparVer[i])){
                    isOld = true;
                    break;
                }

            }
        }

     return isOld;

    }

    /**
     * 用来保存安防设置状态（当一分钟倒计时时退出app，重新登陆进来继续按原先的倒计时开始计时）
     */
    private void saveStatus(int countTime){
        SharedPreferences mySharedPreferences= getActivity().getSharedPreferences(SHAREFERENCE_NAME,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt("countTime", countTime);
        editor.putLong("currentTime",System.currentTimeMillis());
        editor.commit();
    }

    /**
     * 获取保存下来的倒计时数
     * @return
     */
    private void getStatus(){
        //如果现在是离家安防的话，才需要倒计时
        if(leaveHomeSecurity!=null&&leaveHomeSecurity.getIsArm() ==ArmType.ARMING) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHAREFERENCE_NAME,
                    Activity.MODE_PRIVATE);
            int count = sharedPreferences.getInt("countTime", 0);
            long oldTime = sharedPreferences.getLong("currentTime", 0L);
            //如果倒计时还没有结束，接着显示倒计时
            long time = System.currentTimeMillis() - oldTime;
            if (time < count*1000L) {
                countTime = count - (int) (time / (1000L));
                mHandler.removeMessages(MSG);
                mHandler.sendEmptyMessage(MSG);
                dragorclickitemview.setAnim(countTime*1000L);
                dragorclickitemview.setStartReadTime(oldTime-(60-count)*1000L);
            }
        }
    }

    /**
     * 初始安防请求
     */
    private void initSecurityRequest() {
        securityRequest = new SecurityRequest(mAppContext) {
            @Override
            public void onSecurityRequestResult(String uid, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    int arm = mSecurityDao.selSecurityBySecurityId(mainUid, mSecurity.getSecurityId()).getIsArm();
                    if (arm == ArmType.ARMING) {
                        if (mSecurity.getSecType() == Security.AT_HOME) {
                            mHandler.removeMessages(MSG);
                            isReadArm =false;
                            countTime =0;
                            mSecurity.setIsArm(ArmType.ARMING);
                            dragorclickitemview.setArm(false);
                            dragorclickitemview.setAnim(30*1000L);
                            dragorclickitemview.setStatus(0,false);
                            security_status_title.setText(getResources().getString(R.string.intelligent_scene_at_home));
                            security_status_msg.setText(getResources().getString(R.string.safe));
                            String content = getResources().getString(R.string.intelligent_scene_securityed);
                            ToastUtil.showToast(content);
                        }
                        else if (mSecurity.getSecType() == Security.LEAVE_HOME) {
                            countTime = 60;
                            mHandler.removeCallbacksAndMessages(MSG);
                            mHandler.sendEmptyMessage(MSG);

                            dragorclickitemview.setArm(false);
                            dragorclickitemview.setStatus(1,true);
                            dragorclickitemview.setAnim(60*1000L);
                            dragorclickitemview.setStartReadTime(System.currentTimeMillis());

                        }

                    } else if (arm == ArmType.DISARMING) {
                        mHandler.removeMessages(MSG);
                        mSecurity = new Security();
                        mSecurity.setSecType(Security.OTHER);
                        mSecurity.setIsArm(ArmType.DISARMING);
                        isReadArm =false;

                        security_status_title.setText(getResources().getString(R.string.security_disarm));
                        security_status_msg.setText(getResources().getString(R.string.safe));

                        dragorclickitemview.setArm(false);
                        dragorclickitemview.setStatus(2,false);
                        dragorclickitemview.stopAnimator();

                        String content = getResources().getString(R.string.intelligent_scene_unsecurity);
                        ToastUtil.showToast(content);
                    }

                } else {
                    dragorclickitemview.recover();
                    ToastUtil.toastError(result);
                }
            }
        };
    }
    /**
     * 点击“撤防” “离家布防” “在家布防”切换状态
     */
    @Override
    public void securityStatus(int status) {

        if(NetworkUtil.isNetworkAvailable(getActivity())){
        // 同一时间只能有一个安防模式处于布防状态，客户端只发布防指令，
        // 主机判断另一个安防模式是否处于布防状态，如果是则修改布防状态为撤防，同时布防当前安防模式。
        // 客户端接收到布防成功返回后也要做对应的处理

        //没有安防设备
        if(!isCanDefence()){
            dragorclickitemview.recover();
            showTipDialog();
            return;
        }
       if(mSecurity==null){
           dragorclickitemview.recover();
       }
       //在家
       if(status ==0){

           if (mSecurity!=null&&mSecurity.getSecType() != Security.AT_HOME) {
               showDialog();
               mSecurity = atHomeSecurity;
               securityRequest.startSecurityRequest(mainUid, UserCache.getCurrentUserName(mAppContext),
                       mSecurity.getSecurityId(), ArmType.ARMING, mSecurity.getSecType() == Security.AT_HOME ? 0 : 60);
           }
           else{
               dragorclickitemview.recover();
           }
       }//离家
       else  if(status ==1){

          if (mSecurity!=null&&mSecurity.getSecType() != Security.LEAVE_HOME) {
              showDialog();
              mSecurity = leaveHomeSecurity;
              securityRequest.startSecurityRequest(mainUid, UserCache.getCurrentUserName(mAppContext),
                      mSecurity.getSecurityId(), ArmType.ARMING, mSecurity.getSecType() == Security.LEAVE_HOME ? 60 : 0);
          }
          else{
              dragorclickitemview.recover();
          }

      }//撤防
      else if(status ==2) {
          if ((mSecurity != null && mSecurity.getIsArm() == ArmType.ARMING) || isReadArm) {
              mSecurity.setIsArm(ArmType.DISARMING);
              showDialog();
              securityRequest.startSecurityRequest(mainUid, UserCache.getCurrentUserName(mAppContext),
                      mSecurity.getSecurityId(), ArmType.DISARMING, 0);
          }else{
              dragorclickitemview.recover();
          }
        }
      }else{
            dragorclickitemview.recover();
            ToastUtil.showToast(getString(R.string.net_not_connect_content));
        }
    }

    /**
     * @return
     */
    private void loadSecurtyModel() {

        //如果主机是旧版本
        isOldVersion = judgeOldHost();
        if(isOldVersion){
            dragorclickitemview.setNeedArm(false);
            security_status_msg.setVisibility(View.GONE);
        }else{
            dragorclickitemview.setNeedArm(true);
            security_status_msg.setVisibility(View.VISIBLE);
        }
            //是否支持布防
            if (isCanDefence()) {


                atHomeSecurity = getAtHomeSecurity();
                leaveHomeSecurity= getLeaveHomeSecurity();

                if(atHomeSecurity!=null&&leaveHomeSecurity!=null) {
                    if (atHomeSecurity.getIsArm() == ArmType.DISARMING && leaveHomeSecurity.getIsArm() == ArmType.DISARMING) {

                        mSecurity = new Security();
                        mSecurity.setSecType(Security.OTHER);
                        mSecurity.setIsArm(ArmType.DISARMING);
                        dragorclickitemview.setStatus(2, false);
                        security_status_title.setText(getResources().getString(R.string.security_disarm));
                        security_status_msg.setText(getResources().getString(R.string.safe));
                        dragorclickitemview.setArm(false);

                    } else if (atHomeSecurity.getIsArm() == ArmType.DISARMING && leaveHomeSecurity.getIsArm() == ArmType.ARMING) {
                        dragorclickitemview.setStatus(1, false);
                        mSecurity = leaveHomeSecurity;
                        security_status_title.setText(getResources().getString(R.string.intelligent_scene_leave_home));
                        if (mSecurity.getIsOccurred() != 0) {
                            dragorclickitemview.setArm(true);
                            security_status_msg.setText(getResources().getString(R.string.arming));
                        } else {
                            dragorclickitemview.setArm(false);
                            security_status_msg.setText(getResources().getString(R.string.safe));
                        }

                    } else if (atHomeSecurity.getIsArm() == ArmType.ARMING && leaveHomeSecurity.getIsArm() == ArmType.DISARMING) {
                        dragorclickitemview.setStatus(0, false);
                        mSecurity = atHomeSecurity;
                        security_status_title.setText(getResources().getString(R.string.intelligent_scene_at_home));
                        if (mSecurity.getIsOccurred() != 0) {
                            dragorclickitemview.setArm(true);
                            security_status_msg.setText(getResources().getString(R.string.arming));
                        } else {
                            dragorclickitemview.setArm(false);
                            security_status_msg.setText(getResources().getString(R.string.safe));
                        }
                    }

                }
            }else{
                security_status_title.setText(R.string.cancel_security);
                security_status_msg.setText(getResources().getString(R.string.safe));
                dragorclickitemview.setStatus(2,false);
            }

    }

    private Security getAtHomeSecurity() {
        Security atHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.AT_HOME);
        return atHomeSecurity;
    }

    private Security getLeaveHomeSecurity() {
        Security leaveHomeSecurity = mSecurityDao.selSecurity(mainUid, Security.LEAVE_HOME);
        return leaveHomeSecurity;
    }



    /**
     * 是否可以布防
     * @return
     */
    private boolean isCanDefence() {
        mainUid = UserCache.getCurrentMainUid(getActivity());
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
            return true;
        }else{
            return false;
        }
    }


    public void showTipDialog() {
        final Dialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = -100;
        dialog.show();
            window.setContentView(R.layout.dialog_no_device_one_button);
        window.findViewById(R.id.gotItButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        InfoPushManager.getInstance(mAppContext).unregisterWarningListener(this);
        mHandler.removeCallbacksAndMessages(MSG);
        dragorclickitemview.setStop();
    }

    @Override
    public void onNetChanged() {

    }

    @Override
    public void securityArm(boolean isArm) {

    }

    @Override
    public void onWarning(InfoPushMsg infoPushMsg) {
        //如果不是旧版本，才支持安防页面报警
//        if(!isOldVersion&&!isReadArm) {
//            if (mSecurity != null && mSecurity.getSecType() == Security.AT_HOME || mSecurity.getSecType() == Security.LEAVE_HOME) {
//                dragorclickitemview.setArm(true);
//                security_status_msg.setText(getResources().getString(R.string.arming));
//            }
//        }
    }


}
