package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.WheelViewActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 情景绑定
 * Created by huangqiyao on 2015/5/4.
 */
public class BaseSelectDeviceActionsFragment extends
        BaseFragment {
    private static final String TAG =
            BaseSelectDeviceActionsFragment.class.getSimpleName();

    public static final int SET_DELAY_TIME = 1;
    public static final int SET_LINKAGE_DELAY_TIME = 6;
    public static final int SET_ACTION = 2;
    public static final int SELECT_DEVICE = 3;
    private static final int SELECT_PIC = 4;
    public static final int SET_ACTIONS = 5;
    public static final int RESULT_OK = -1;

    protected DeviceDao mDeviceDao;
    /**
     * 秒列表填充内容
     */
    protected int[] secondDelays;

    /**
     * 分列表填充内容
     */
    protected int[] minuteDelays;
    private boolean isIrDevice;

    protected int mBindActionType;

    /**
     * {@link IntelligentSceneConditionType}
     */
    protected int conditionType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        secondDelays = getResources().getIntArray
//                (R.array.time_delay);
        //最大时间4min59sec
        minuteDelays = new int[10];
        for (int i = 0; i < minuteDelays.length; i++) {
            minuteDelays[i] = i;
        }
        secondDelays = new int[66];
        for (int i = 0; i < secondDelays.length; i++) {
            secondDelays[i] = i;
        }
        mDeviceDao = new DeviceDao();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int vId = v.getId();
        if (vId == R.id.tvTime) {
            //选择延迟时间，单位100ms
            if (mBindActionType == BindActionType.LINKAGE) {
                int delayTime = getDelayTime() / 10;//单位s
                Intent intent = new Intent(getActivity(), SelectDelayTimeActivity.class);
                intent.putExtra(IntentKey.DELAY_TIME, delayTime);
                startActivityForResult(intent,
                        SET_LINKAGE_DELAY_TIME);
            } else {
                int delayTime = getDelayTime() / 10;//单位s
                int min = delayTime / 60;
                int sec = delayTime % 60;
                Intent intent = new Intent(getActivity(),
                        WheelViewActivity.class);
                if (isIrDevice) {
                    //红外设备秒0 ~ 59
                    secondDelays = new int[60];
                    for (int i = 0; i < secondDelays.length; i++) {
                        secondDelays[i] = i;
                    }
                    intent.putExtra
                            (WheelViewActivity.FIRST_WHEEL_BOS, (Serializable)
                                    getWheelBos(minuteDelays, getString
                                            (R.string.time_minute_mini)));
                    intent.putExtra(IntentKey.WHEELVIEW_SELECTED_FIRST, min);
                    intent.putExtra(IntentKey.WHEELVIEW_SELECTED_SECOND, sec);
                } else {
                    intent.putExtra(IntentKey.WHEELVIEW_SELECTED_SECOND, min * 60 + sec);
                }
                intent.putExtra
                        (WheelViewActivity.SECOND_WHEEL_BOS, (Serializable)
                                getWheelBos(secondDelays, getString
                                        (R.string.time_second_mini)));
                startActivityForResult(intent,
                        SET_DELAY_TIME);
            }
        } else if (vId == R.id.btnAddAction) {
            //添加设备，一个设备动作没有选择时显示的页面
            Intent intent = new Intent(getActivity(),
                    SelectDeviceActivity.class);
            //跳转到选择小方设备页面
            if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
                intent = new Intent(getActivity(),
                        SelectAllone2ChildDeviceActivity.class);
            }
            if (mBindActionType == BindActionType.LINKAGE) {
                intent.putExtra(IntentKey.BIND_ACTION_TYPE, mBindActionType);
                intent.putExtra(IntentKey.BIND_SIZE, getSelectedLinkageOutputSize());
            }
            intent.putExtra(IntentKey.LINKAGE_CONDITION_TYPE, conditionType);
            intent.putStringArrayListExtra
                    (IntentKey.SELECTED_BIND_IDS, getBindDeviceIds());
            startActivityForResult(intent, SELECT_DEVICE);
        } else if (vId == R.id.addBindTextView) {
            //添加动作 选择了至少一个设备后显示在最底添加按钮
            Intent intent = new Intent(getActivity(),
                    SelectDeviceActivity.class);
            //跳转到选择小方设备页面
            if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
                intent = new Intent(getActivity(),
                        SelectAllone2ChildDeviceActivity.class);
            }
            if (mBindActionType == BindActionType.LINKAGE) {
                intent.putExtra(IntentKey.BIND_ACTION_TYPE, mBindActionType);
            }
            intent.putStringArrayListExtra
                    (IntentKey.SELECTED_BIND_IDS, getBindDeviceIds());
            startActivityForResult(intent, SELECT_DEVICE);
        } else if (vId == R.id.ivDelete) {
            //删除绑定动作
            onDeleteAction(v.getTag());
        }
    }

    protected int getDelayTime() {
        return 0;
    }

    /**
     * 得到已经选择的设备动作对应的设备id
     *
     * @return
     */
    protected ArrayList<String> getBindDeviceIds() {
        return null;
    }

    /**
     * 获取当前联动选中的所有动作的个数
     *
     * @return
     */
    protected int getSelectedLinkageOutputSize() {
        return 0;
    }

//    protected ArrayList<String> getCommonDeviceIds() {
//        return null;
//    }

    protected ArrayList<Device> getCommonDevices() {
        return null;
    }

    /**
     * @author Smagret
     * @data 2015/10/23
     * 设置是否是虚拟红外设备
     */
    protected void setIrDeviceFlag(boolean isIrDevice) {
        this.isIrDevice = isIrDevice;
    }

    /**
     * 选择设备动作
     *
     * @param device
     * @param action
     * @param bindActionType {@link BindActionType}
     */
    protected void selDeviceAction(Device device, Action
            action, int bindActionType) {
        if (action != null && StringUtil.isEmpty(action.getCommand())) {
            ActivityTool.toSelectActionActivity(getActivity(), BaseSelectDeviceActionsFragment.this,
                    SET_ACTION, bindActionType, device, null);
        } else {
            ActivityTool.toSelectActionActivity(getActivity(), BaseSelectDeviceActionsFragment.this,
                    SET_ACTION, bindActionType, device, action);
        }
    }

    /**
     * 一个设备可以选择多个动作，但是延时不能相同
     *
     * @param device
     * @param curAction      当前要设置的动作
     * @param actions        已选中的动作
     * @param bindActionType {@link BindActionType}
     */
    protected void selDeviceActions(Device device, Action curAction, List<Action> actions, int bindActionType) {
        boolean isNoneCommand = true;
        if (curAction != null && !StringUtil.isEmpty(curAction.getCommand())) {
            isNoneCommand = false;
        } else if (actions != null && !actions.isEmpty()) {
            for (Action action : actions) {
                if (!StringUtil.isEmpty(action.getCommand())) {
                    isNoneCommand = false;
                    break;
                }
            }
        }
        if (isNoneCommand) {
            ActivityTool.toSelectActionsActivity(getActivity(), BaseSelectDeviceActionsFragment.this,
                    SET_ACTION, bindActionType, device, curAction, null);
        } else {
            ActivityTool.toSelectActionsActivity(getActivity(), BaseSelectDeviceActionsFragment.this,
                    SET_ACTION, bindActionType, device, curAction, actions);
        }
    }

    protected List<WheelBo> getWheelBos(int[] timeDelays, String unit) {
        List<WheelBo> wheelBos = new ArrayList<WheelBo>();
        for (int timeDelay : timeDelays) {
            WheelBo wheelBo = new WheelBo();
            wheelBo.setName(timeDelay + unit);
            wheelBos.add(wheelBo);
        }
        LogUtil.d(TAG, "getWheelBos()-wheelBos:" + wheelBos);
        return wheelBos;
    }

    /**
     * 删除动作
     *
     * @param tag
     */
    protected void onDeleteAction(Object tag) {

    }

    @Override
    public void onActivityResult(int requestCode, int
            resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,
                data);
        LogUtil.d(TAG, "onActivityResult()-requestCode:" +
                requestCode + ",resultCode:" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SET_DELAY_TIME:
                    int firstIndex = data.getIntExtra
                            (IntentKey.WHEELVIEW_SELECTED_FIRST, 0);
                    int secondIndex = data.getIntExtra
                            (IntentKey.WHEELVIEW_SELECTED_SECOND, 0);
                    int min = 0;
                    if (firstIndex > 0 && firstIndex < minuteDelays.length) {
                        min = minuteDelays[firstIndex];
                    }
                    int sec = 0;
                    if (secondIndex > 0 && secondIndex < secondDelays.length) {
                        sec = secondDelays[secondIndex];
                    }
                    onSelectDelayTime((min * 60 + sec) * 10);
                    break;
                case SET_LINKAGE_DELAY_TIME:
                    int delayTime = data.getIntExtra
                            (IntentKey.DELAY_TIME, 0);

                    onSelectDelayTime(delayTime * 10);
                    break;
                case SELECT_DEVICE:
                    //选择设备
                    ArrayList<Device> devices = null;
                    Serializable serializable1 =
                            data.getSerializableExtra("checkedDevices");
                    if (serializable1 != null) {
                        devices = (ArrayList<Device>)
                                serializable1;
                        LogUtil.d(TAG, "onActivityResult() - devices" + devices);
                    }
                    onSelectDevices(devices);
                    break;
                case SET_ACTION:
                    //选择设备动作
                    Action action = (Action)
                            data.getSerializableExtra("action");
                    if (action != null) {
                        onSelectAction(action);
                    }
                    break;
                case SET_ACTIONS:
                    break;
            }
        }
    }

    /**
     * @param delayTime 延迟时间，单位100ms
     */
    protected void onSelectDelayTime(int delayTime) {

    }


    protected void onSelectAction(Action action) {

    }

    protected void onSelectDevices(List<Device> devices) {

    }
}
