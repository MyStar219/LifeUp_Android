package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.WheelViewActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.BindActionType;
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
public class BaseSelectDeviceActionsActivity extends
        BaseActivity {
    private static final String TAG =
            BaseSelectDeviceActionsActivity.class.getSimpleName();

    public static final int SET_DELAY_TIME = 1;
    public static final int SET_ACTION = 2;
    public static final int SELECT_DEVICE = 3;
    private static final int SELECT_PIC = 4;
    public static final int SET_ACTIONS = 5;

    protected DeviceDao mDeviceDao;
    protected int[] secondDelays;
    protected int[] minuteDelays;
    private boolean isIrDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        secondDelays = getResources().getIntArray
//                (R.array.time_delay);
        //最大时间4min59sec
        minuteDelays = new int[10];
        for (int i = 0; i < minuteDelays.length; i++) {
            minuteDelays[i] = i;
        }
        secondDelays = new int[60];
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
            int delayTime = getDelayTime() / 10;//单位s
            int min = delayTime / 60;
            int sec = delayTime % 60;
            Intent intent = new Intent(this,
                    WheelViewActivity.class);
            if (isIrDevice) {
                intent.putExtra
                        (WheelViewActivity.FIRST_WHEEL_BOS, (Serializable)
                                getWheelBos(minuteDelays, getString
                                        (R.string.time_minute_mini)));
                intent.putExtra(IntentKey.WHEELVIEW_SELECTED_FIRST, min);
            }
            intent.putExtra
                    (WheelViewActivity.SECOND_WHEEL_BOS, (Serializable)
                            getWheelBos(secondDelays, getString
                                    (R.string.time_second_mini)));
            intent.putExtra(IntentKey.WHEELVIEW_SELECTED_SECOND, sec);
            startActivityForResult(intent,
                    SET_DELAY_TIME);
        } else if (vId == R.id.btnAddAction) {
            //添加动作
            Intent intent = new Intent(this,
                    SelectDeviceActivity.class);
//            intent.putExtra
//                    (SelectDelayTimeActivity.FIRST_WHEEL_BOS, (Serializable) getWheelBos(secondDelays));
            intent.putStringArrayListExtra
                    (IntentKey.SELECTED_BIND_IDS, getBindDeviceIds());
            startActivityForResult(intent, SELECT_DEVICE);
        } else if (vId == R.id.addBindTextView) {
            //添加动作
            Intent intent = new Intent(this,
                    SelectDeviceActivity.class);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);//TODO
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
     */
    protected void selDeviceAction(Device device, Action
            action) {
        if (action != null && StringUtil.isEmpty(action.getCommand())) {
            ActivityTool.toSelectActionActivity(this,
                    SET_ACTION, BindActionType.SCENE, device, null);
        } else {
            ActivityTool.toSelectActionActivity(this,
                    SET_ACTION, BindActionType.SCENE, device, action);
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
    protected void onActivityResult(int requestCode, int
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
