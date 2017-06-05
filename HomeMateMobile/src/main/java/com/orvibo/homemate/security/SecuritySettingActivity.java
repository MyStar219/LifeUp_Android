package com.orvibo.homemate.security;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.manager.SecurityManagerActivity;

/**
 * Created by wuliquan on 2016/7/20.
 */
public class SecuritySettingActivity extends BaseActivity{
    private DeviceDao mDeviceDao;
    private SecurityDao mSecurityDao;
    private String mainUid;
    private Security atHomeSecurity,leaveHomeSecurity;
    private RelativeLayout out_layout,inhome_Layout,time_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_security_setting);

        out_layout = (RelativeLayout)findViewById(R.id.out_layout);
        inhome_Layout = (RelativeLayout)findViewById(R.id.inhome_layout);
        time_layout = (RelativeLayout)findViewById(R.id.time_layout);

        out_layout.setOnClickListener(this);
        inhome_Layout.setOnClickListener(this);
        time_layout.setOnClickListener(this);


        mDeviceDao = new DeviceDao();
        mSecurityDao = SecurityDao.getInstance();
        mainUid = UserCache.getCurrentMainUid(SecuritySettingActivity.this);


        addSecurtyModel();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.out_layout){
            if (leaveHomeSecurity != null && !(Constant.INVALID_NUM + "").equals(leaveHomeSecurity.getSecurityId())) {
                Intent intent = new Intent(SecuritySettingActivity.this, SecurityManagerActivity.class);
                intent.putExtra(Constant.SECURITY, leaveHomeSecurity);
                startActivity(intent);
            }
        }else if(v.getId() ==R.id.inhome_layout){
            if (atHomeSecurity != null && !(Constant.INVALID_NUM + "").equals(atHomeSecurity.getSecurityId())) {
                Intent intent = new Intent(SecuritySettingActivity.this, SecurityManagerActivity.class);
                intent.putExtra(Constant.SECURITY, atHomeSecurity);
                startActivity(intent);
            }

        }else if(v.getId() == R.id.time_layout){
               Intent intent = new Intent(SecuritySettingActivity.this,SecurityTimeListActivity.class);
               startActivity(intent);
        }

    }

    /**
     * @return
     */
    private void addSecurtyModel() {
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
            atHomeSecurity = getAtHomeSecurity();
            leaveHomeSecurity= getLeaveHomeSecurity();

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

}
