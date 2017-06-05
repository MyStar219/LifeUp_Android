package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.SmartSceneConstant;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.adapter.IntelligentSceneSelectConditionAdapter;
import com.orvibo.homemate.smartscene.manager.selecthumiturepopup.SelectHumitureHelper;
import com.orvibo.homemate.smartscene.manager.selecthumiturepopup.SelectHumiturePopup;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;
import com.orvibo.homemate.view.popup.SelectDevicePopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能场景选择启动条件
 * Created by Smagret on 2015/10/16.
 */
public class IntelligentSceneSelectConditionActivity extends BaseActivity implements AdapterView.OnItemClickListener, NavigationCocoBar.OnLeftClickListener, NavigationCocoBar.OnRightClickListener {
    private static final String TAG = IntelligentSceneSelectConditionActivity.class.getSimpleName();
    private IntelligentSceneSelectConditionAdapter mIntelligentSceneSelectConditionAdapter;
    private SelectDevicePopup selectDevicePopup;
    private SelectHumiturePopup mSelectHumiturePopup;
    private int deviceType;
    private int value = Constant.INVALID_NUM;
    /**
     * 触发联动的设备编号。传感器的deviceId
     */
    private String selectDeviceId = Constant.NULL_DATA;
    /**
     * 当前选择的设备
     */
    private Device selectedDevice;
    /**
     * 智能场景启动条件类型 0：手动点击、 1：门锁被打开、 2：门锁被关闭、 3：门窗传感器被打开、 4：门窗传感器被关闭、5：人体传感器检测有人经过
     * 如果有对应设备才可以将启动类型加入list中
     */
    private List<Integer> mConditions = new ArrayList<Integer>();
    private List<LinkageCondition> linkageConditions;
    private int conditionType = IntelligentSceneConditionType.OTHER;
    private boolean hasLinkageOutput = false;
    private String currentLinkageId = "";
    /**
     * 温湿度条件.key:deviceId,value:LinkageCondition,LinkageType为{@link com.orvibo.homemate.smartscene.SmartSceneConstant.LinkageType#DEVICE_STATUS}的联动条件
     */
//    private ConcurrentHashMap<String, LinkageCondition> mHumitureConditions = new ConcurrentHashMap<>();
//    private ConcurrentHashMap<String, List<LinkageCondition>> mLinkageConditions = new ConcurrentHashMap<>();
    private DeviceDao mDeviceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_condition);
        mDeviceDao = new DeviceDao();
        Serializable serializable = getIntent().getSerializableExtra(Constant.LINKAGE_CONDITIONS);
        conditionType = getIntent().getIntExtra(Constant.CONDITION_TYPE, IntelligentSceneConditionType.OTHER);
        hasLinkageOutput = getIntent().getBooleanExtra(Constant.HAS_LINKAGEOUTPUT, false);
        if ((conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED)
                || (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED)
                || (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED)
                || (conditionType == IntelligentSceneConditionType.LOCK_OPENED)
                || (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR)
                || (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR)
                || (conditionType == IntelligentSceneConditionType.CLICK_ALLONE)
                ) {
            linkageConditions = (List<LinkageCondition>) serializable;
            selectDeviceId = IntelligentSceneTool.getDeviceId(linkageConditions);
            if (linkageConditions != null && !linkageConditions.isEmpty() && linkageConditions.size() > 0) {
                currentLinkageId = linkageConditions.get(0).getLinkageId();
            }
            //缓存记录的温湿度值和条件
            setHumitureCondition();
        }
        LogUtil.d(TAG, "onCreate()-linkageConditions:" + linkageConditions);

        initSelectDevicePopup();
        initSelectHumiturePopup();
        ListView conditionListView = (ListView) findViewById(R.id.conditionListView);
        conditionListView.setOnItemClickListener(this);
        mIntelligentSceneSelectConditionAdapter = new IntelligentSceneSelectConditionAdapter(mContext, getConditions(), conditionType);
        conditionListView.setAdapter(mIntelligentSceneSelectConditionAdapter);
        View emptyView = findViewById(R.id.empty_ll);
        conditionListView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIntelligentSceneSelectConditionAdapter != null) {
            mIntelligentSceneSelectConditionAdapter.setSelectedConditionType(conditionType);
        }
    }

    /**
     * 用于显示在列表的数据
     *
     * @return
     */
    private List<Integer> getConditions() {
        //1.10.2版本屏蔽小方一键场景
//        已经设置了输出设备，情景、小方联动、主机联动不能互相切换
        //如果没有对应的设备则不在条件列表中显示
        int deviceCount = mDeviceDao.selUnBindAlloneDeviceCount(UserCache.getCurrentUserId(mAppContext));
        if (hasLinkageOutput && conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            if (deviceCount > 0) {
                mConditions.add(IntelligentSceneConditionType.CLICK_ALLONE);
            }
        } else {
            //条件列表显示的数据
            boolean hasLock = mDeviceDao.hasDevice(currentMainUid, DeviceType.LOCK);
            boolean hasDoorContact = mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC)
                    || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_WINDOW)
                    || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_DRAWER)
                    || mDeviceDao.hasDevice(currentMainUid, DeviceType.MAGNETIC_OTHER);
            boolean hasInfraredSensor = mDeviceDao.hasDevice(currentMainUid, DeviceType.INFRARED_SENSOR);

            //单击
            if (!hasLinkageOutput) {
                mConditions.add(IntelligentSceneConditionType.CLICKED);
            }
            //小方，已经选择的输出设备，就不能再显示小方
            if (!hasLinkageOutput && deviceCount > 0) {
                mConditions.add(IntelligentSceneConditionType.CLICK_ALLONE);
            }

            if (hasLock) {
                mConditions.add(IntelligentSceneConditionType.LOCK_OPENED);
                //mConditions.add(IntelligentSceneConditionType.LOCK_CLOSED);
            }
            if (hasDoorContact) {
                mConditions.add(IntelligentSceneConditionType.DOOR_SENSOR_OPENED);
                mConditions.add(IntelligentSceneConditionType.DOOR_SENSOR_CLOSED);
            }
            if (hasInfraredSensor) {
                mConditions.add(IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED);
            }

            boolean hasTemperatureSensor = new DeviceDao().hasDevice(currentMainUid, DeviceType.TEMPERATURE_SENSOR);
            if (hasTemperatureSensor) {
                mConditions.add(IntelligentSceneConditionType.TEMPERATURE_SENSOR);
            }

            boolean hasHumiditySensor = new DeviceDao().hasDevice(currentMainUid, DeviceType.HUMIDITY_SENSOR);
            if (hasHumiditySensor) {
                mConditions.add(IntelligentSceneConditionType.HUMIDITY_SENSOR);
            }
        }
        return mConditions;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        conditionType = mConditions.get(position);
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            Intent intent = new Intent();
            intent.putExtra(IntentKey.LINKAGE_CONDITION_ACTION, conditionType);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            deviceType = IntelligentSceneTool.getDeviceType(conditionType);
            if (conditionType != IntelligentSceneConditionType.TEMPERATURE_SENSOR && conditionType != IntelligentSceneConditionType.HUMIDITY_SENSOR) {
                value = IntelligentSceneTool.getValueByConditionType(conditionType);
            }
            selectDevicePopup.showPopup(IntelligentSceneSelectConditionActivity.this, currentMainUid, deviceType, selectDeviceId,currentLinkageId);
        }
    }

    private void setHumitureCondition() {
        //缓存记录的温湿度值和条件
        if (!TextUtils.isEmpty(selectDeviceId) && linkageConditions != null && !linkageConditions.isEmpty()) {
            if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR || conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
                for (LinkageCondition condition : linkageConditions) {
                    if (condition.getLinkageType() == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                        value = condition.getValue();
                        if (value == Constant.INVALID_NUM) {
                            //还没有设置默认值
                            if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
                                value = SmartSceneConstant.TEMPERATURE_DEFAULT_VALUE;
                            } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
                                value = SmartSceneConstant.HUMIDITY_DEFAULT_VALUE;
                            }
                            condition.setValue(value);
                        }
//                        mHumitureConditions.put(selectDeviceId, condition);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onRightClick(View v) {

    }

    private void initSelectDevicePopup() {
        selectDevicePopup = new SelectDevicePopup() {
            @Override
            public void itemClicked(Device device) {
                selectDeviceId = device.getDeviceId();
                selectedDevice = device;
                if (linkageConditions != null && !linkageConditions.isEmpty()) {
                    for (LinkageCondition linkageCondition : linkageConditions) {
                        if (linkageCondition.getLinkageType() == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                            if (!selectDeviceId.equals(linkageCondition.getDeviceId())) {
                                value = IntelligentSceneTool.getValueByConditionType(conditionType);
                                linkageCondition.setValue(value);
                            }
                            break;
                        }
                    }
                }

                final int deviceType = device.getDeviceType();
                if (deviceType == DeviceType.TEMPERATURE_SENSOR
                        || deviceType == DeviceType.HUMIDITY_SENSOR) {
//                    LinkageCondition linkageCondition = mHumitureConditions.get(selectDeviceId);

//                    if (linkageCondition != null) {
//                        value = linkageCondition.getValue();
//                    } else {
//                        value = Constant.INVALID_NUM;
//                    }
//                    List<LinkageCondition> tempLinkageConditions = null;
//                    if (mLinkageConditions.containsKey(selectDeviceId)) {
//                        tempLinkageConditions = mLinkageConditions.get(selectDeviceId);
//                    }
                    linkageConditions = IntelligentSceneTool.getLinkageConditions(device, linkageConditions, selectDeviceId, value, conditionType);
//                    mLinkageConditions.put(selectDeviceId, linkageConditions);
                    LinkageCondition tempLinkageCondition = null;
                    if (linkageConditions != null && !linkageConditions.isEmpty()) {
                        for (LinkageCondition linkageCondition : linkageConditions) {
                            if (linkageCondition.getLinkageType() == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                                tempLinkageCondition = linkageCondition;
                                break;
                            }
                        }
                    }
                    setHumitureCondition();
                    int conditionPos = SelectHumitureHelper.getPosByCondition(tempLinkageCondition);
                    if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
                        int pos = SelectHumitureHelper.getPositionByTemperature(tempLinkageCondition);
                        mSelectHumiturePopup.show(conditionPos, pos, SelectHumitureHelper.getTemperatureCondition(mContext), SelectHumitureHelper.getTemperatureValues(mContext));
                        mSelectHumiturePopup.setTitle(R.string.smart_scene_condition_temp_title);
                    } else if (deviceType == DeviceType.HUMIDITY_SENSOR) {
                        int pos = SelectHumitureHelper.getPositionByHumidity(tempLinkageCondition);
                        mSelectHumiturePopup.show(conditionPos, pos, SelectHumitureHelper.getTemperatureCondition(mContext), SelectHumitureHelper.getHumidityValues(mContext));
                        mSelectHumiturePopup.setTitle(R.string.smart_scene_condition_humiture_title);
                    }
                } else {
                    linkageConditions = IntelligentSceneTool.getLinkageConditions(device, linkageConditions, selectDeviceId, value, conditionType);
                    Intent intent = new Intent();
                    intent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                    intent.putExtra(IntentKey.LINKAGE_CONDITION_ACTION, conditionType);
                    intent.putExtra(IntentKey.UID, device.getUid());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        };
    }

    private void initSelectHumiturePopup() {
        if (mSelectHumiturePopup != null) {
            return;
        }
        mSelectHumiturePopup = new SelectHumiturePopup(mContext) {
            @Override
            public void onSelect(int selectedLeftPos, WheelBo leftBo, int selectedRightPos, WheelBo rightBo) {
                super.onSelect(selectedLeftPos, leftBo, selectedRightPos, rightBo);
                LogUtil.d(TAG, "onSelect()-selectedLeftPos:" + selectedLeftPos + ",leftBo:" + leftBo + ",selectedRightPos:" + selectedRightPos + ",rightBo:" + rightBo);
                dismiss();
                LinkageCondition tempLinkageCondition = null;
                if (linkageConditions != null && !linkageConditions.isEmpty()) {
                    for (LinkageCondition condition : linkageConditions) {
                        if (condition.getLinkageType() == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                            condition.setCondition(Integer.valueOf(leftBo.getId()));
                            if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
                                condition.setValue(SelectHumitureHelper.getTemperatureByPosition(selectedRightPos));
                            } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
                                condition.setValue(SelectHumitureHelper.getHumidityByPosition(selectedRightPos));
                            }
                            tempLinkageCondition = condition;
//                            mHumitureConditions.put(selectDeviceId, condition);
                            break;
                        }
                    }
//                    mLinkageConditions.put(selectDeviceId, linkageConditions);
                }
                if (tempLinkageCondition != null) {
                    Intent intent = new Intent();
                    intent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                    intent.putExtra(IntentKey.LINKAGE_CONDITION_ACTION, conditionType);
                    if (selectedDevice != null) {
                        intent.putExtra(IntentKey.UID, selectedDevice.getUid());
                    }
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        };
    }
}
