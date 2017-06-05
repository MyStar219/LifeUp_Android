package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.smartscene.adapter.SecuritySelectConditionAdapter;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 安防场景选择启动条件
 */
public class SecuritySelectConditionActivity extends BaseActivity implements AdapterView.OnItemClickListener, NavigationCocoBar.OnLeftClickListener, NavigationCocoBar.OnRightClickListener {
    private static final String TAG = SecuritySelectConditionActivity.class.getSimpleName();
    private TextView  emptyTextView;
    private ImageView emptyImageView;
    /**
     * 触发联动的设备编号
     */
    private String selectDeviceId = Constant.NULL_DATA;
    /**
     * 智能场景启动条件类型 0：手动点击、 1：门锁被打开、 2：门锁被关闭、 3：门窗传感器被打开、 4：门窗传感器被关闭、5：人体传感器检测有人经过
     * 如果有对应设备才可以将启动类型加入list中
     */
    private List<LinkageCondition> selectedLinkageConditions;
    private List<LinkageCondition> srcSelectedLinkageConditions;
    private boolean hasLinkageOutput = false;
    private boolean hasSceneBind     = false;
    private DeviceDao                      mDeviceDao;
    private SecuritySelectConditionAdapter mSecuritySelectConditionAdapter;
    private NavigationCocoBar              mNavigationCocoBar;
    private List<Device> allSecurityDevices = new ArrayList<>();
    private List<Device> selectedDevices    = new ArrayList<>();
    private Security            mSecurity;
    private LinkageConditionDao mLinkageConditionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_condition);
        //根据传递过来的安防对象判断
        mSecurity = (Security) getIntent().getSerializableExtra(Constant.SECURITY);
        selectedLinkageConditions = (List<LinkageCondition>) getIntent().getSerializableExtra(Constant.SECURITY_CONDITIONS);
        srcSelectedLinkageConditions = (List<LinkageCondition>) getIntent().getSerializableExtra(Constant.SECURITY_CONDITIONS);
        if (selectedLinkageConditions == null) {
            finish();
        }
        mDeviceDao = new DeviceDao();
        //条件转设备
        condition2Device(selectedLinkageConditions);
        mLinkageConditionDao = new LinkageConditionDao();
        emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        emptyImageView = (ImageView) findViewById(R.id.emptyImageView);
        mNavigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationCocoBar);
        mNavigationCocoBar.setCenterText(getString(R.string.intelligent_scene_select_condition));
        ListView conditionListView = (ListView) findViewById(R.id.conditionListView);
        conditionListView.setOnItemClickListener(this);
        mSecuritySelectConditionAdapter = new SecuritySelectConditionAdapter(mContext, getDevices(), selectedDevices);
        mSecuritySelectConditionAdapter.setMainUid(currentMainUid);
        conditionListView.setAdapter(mSecuritySelectConditionAdapter);
        View emptyView = findViewById(R.id.empty_ll);
        if (allSecurityDevices == null) {
            conditionListView.setEmptyView(emptyView);
        }
    }

    private void condition2Device(List<LinkageCondition> selectedlinkageConditions) {
        for (LinkageCondition condition : selectedlinkageConditions) {
            Device device = mDeviceDao.selDevice(currentMainUid, condition.getDeviceId());
            if (device != null) {
                selectedDevices.add(device);
            }
        }
    }


    private List<Device> getDevices() {

        //门磁
        List<Device> magneticSersonDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.MAGNETIC);
        if (magneticSersonDevices != null && magneticSersonDevices.size() > 0) {
            allSecurityDevices.addAll(magneticSersonDevices);
        }
        //窗磁：
        List<Device> magneticWindowSersonDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.MAGNETIC_WINDOW);
        if (magneticWindowSersonDevices != null && magneticWindowSersonDevices.size() > 0) {
            allSecurityDevices.addAll(magneticWindowSersonDevices);
        }
        //抽屉磁：
        List<Device> magneticDrawerSersonDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.MAGNETIC_DRAWER);
        if (magneticDrawerSersonDevices != null && magneticDrawerSersonDevices.size() > 0) {
            allSecurityDevices.addAll(magneticDrawerSersonDevices);
        }
        //其他类型的门窗磁其他类型的门窗磁：
        List<Device> magneticOtherSersonDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.MAGNETIC_OTHER);
        if (magneticOtherSersonDevices != null && magneticOtherSersonDevices.size() > 0) {
            allSecurityDevices.addAll(magneticOtherSersonDevices);
        }
        //人体红外
        List<Device> humanDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.INFRARED_SENSOR);
        if (humanDevices != null && humanDevices.size() > 0) {
            //由于在1.10版本中，在家安防也显示人体红外，但是默认不选中状态
            allSecurityDevices.addAll(humanDevices);
/*            if (!(mSecurity.getSecType() == Security.AT_HOME)) {
                allSecurityDevices.addAll(humanDevices);
            }*/
        }
        //烟雾传感器
        List<Device> smokeDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.SMOKE_SENSOR);
        if (smokeDevices != null && smokeDevices.size() > 0) {
            allSecurityDevices.addAll(smokeDevices);
        }
        //co
        List<Device> CODevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.CO_SENSOR);
        if (CODevices != null && CODevices.size() > 0) {
            allSecurityDevices.addAll(CODevices);
        }
        //可燃气体
        List<Device> flammableDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.FLAMMABLE_GAS);
        if (flammableDevices != null && flammableDevices.size() > 0) {
            allSecurityDevices.addAll(flammableDevices);
        }
        //水浸
        List<Device> waterDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.WATER_SENSOR);
        if (waterDevices != null && waterDevices.size() > 0) {
            allSecurityDevices.addAll(waterDevices);
        }
        //紧急按钮
        List<Device> sosDevices = mDeviceDao.selDevicesByDeviceType(currentMainUid, DeviceType.SOS_SENSOR);
        if (sosDevices != null && sosDevices.size() > 0) {
            allSecurityDevices.addAll(sosDevices);
        }
        //selectedDevices.addAll(allSecurityDevices);
        allSecurityDevices.size();
        return allSecurityDevices;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.securityCheck);
        boolean isChecked = (boolean) checkBox.getTag();
        Device securityDevice = allSecurityDevices.get(position);
        if (isChecked) {
            if (selectedDevices.contains(securityDevice)) {
                selectedDevices.remove(securityDevice);
            }
        } else {
            if (!selectedDevices.contains(securityDevice)) {
                if (securityDevice != null) {
                    selectedDevices.add(securityDevice);
                }
            }
        }
        refresh();
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        if (selectedDevices != null && selectedDevices.isEmpty()) {
//            selectedDevices.addAll(allSecurityDevices);
//        }
        selectedLinkageConditions = device2Condition(selectedDevices);
        Intent intent = new Intent();
        intent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) selectedLinkageConditions);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * 根据选择的设备转换成相应的条件
     * 1.
     *
     * @param selectedDevices
     * @return
     */
    private List<LinkageCondition> device2Condition(List<Device> selectedDevices) {
        selectedLinkageConditions.clear();
        for (Device device : selectedDevices) {
            LinkageCondition linkageCondition;
            linkageCondition = mLinkageConditionDao.selLinkageConditionsByDeviceIdAndLinkageId(currentMainUid, device.getDeviceId(), mSecurity.getSecurityId());
            if (linkageCondition == null) {
                linkageCondition = IntelligentSceneTool.getLinkageCondition(device, mSecurity.getSecurityId());
            }
            selectedLinkageConditions.add(linkageCondition);
        }
        return selectedLinkageConditions;
    }

    @Override
    public void onRightClick(View v) {

    }

    private void refresh() {
        mSecuritySelectConditionAdapter.refresh(allSecurityDevices, selectedDevices);
    }
}
