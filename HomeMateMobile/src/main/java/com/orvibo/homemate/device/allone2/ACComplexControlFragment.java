package com.orvibo.homemate.device.allone2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzy.tvmao.KKACManagerV2;
import com.hzy.tvmao.ir.ac.ACConstants;
import com.hzy.tvmao.ir.ac.ACStateV2;
import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.event.AlloneViewEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.AlloneACUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by yuwei on 2016/3/29.
 * 空调遥控器控制面板（复杂）
 */
public class ACComplexControlFragment extends Fragment implements View.OnClickListener, OnRefreshListener {

    private KKACManagerV2 mKKACManager = new KKACManagerV2();
    private int direction;

    private IrData irData;
    protected boolean isAction = false;//定时倒计时
    protected Action action;


    /**
     * 控制面板按钮
     */
    private IrKeyButton tv_air_conditioner_model,//模式
            tv_air_conditioner_power,//开关
            tv_air_conditioner_swept_wind,//扫风
            tv_air_conditioner_hearting,//制热
            tv_air_conditioner_wind_direction,//风向
            tv_air_conditioner_refrigeration,//制冷
            tv_air_conditioner_air_volume,//风量
            iv_wether_up, //温度+
            iv_wether_down;//温度-

    /**
     * 空调控制面板的所有key
     */
    protected List<IrKeyButton> mainIrKeyButtons = new ArrayList<IrKeyButton>();


    //private AlloneData alloneData;

    /**
     * 显示屏部分
     */
    private ImageView iv_air_conditioner_work_model;
    private TextView tv_air_conditioner_temperature;
    private View dash_divide_line;
    private ImageView iv_swept_wind_state;
    private ImageView iv_wind_direction_state;
    private ImageView iv_wind_volume;

    private OnKeyClickListener keyClickListener;

    private boolean isHomeClick = false;

    private Device device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ac_complex_remotecontrol, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initParams();
    }

    private void initView(View view) {
        tv_air_conditioner_model = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_model);
        tv_air_conditioner_power = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_power);
        tv_air_conditioner_swept_wind = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_swept_wind);
        tv_air_conditioner_hearting = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_hearting);
        tv_air_conditioner_wind_direction = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_wind_direction);
        tv_air_conditioner_refrigeration = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_refrigeration);
        tv_air_conditioner_air_volume = (IrKeyButton) view.findViewById(R.id.tv_air_conditioner_air_volume);
        iv_wether_up = (IrKeyButton) view.findViewById(R.id.iv_wether_up);
        iv_wether_down = (IrKeyButton) view.findViewById(R.id.iv_wether_down);

        mainIrKeyButtons.add(tv_air_conditioner_model);
        mainIrKeyButtons.add(tv_air_conditioner_power);
        mainIrKeyButtons.add(tv_air_conditioner_swept_wind);
        mainIrKeyButtons.add(tv_air_conditioner_hearting);
        mainIrKeyButtons.add(tv_air_conditioner_wind_direction);
        mainIrKeyButtons.add(tv_air_conditioner_refrigeration);
        mainIrKeyButtons.add(tv_air_conditioner_air_volume);
        mainIrKeyButtons.add(iv_wether_up);
        mainIrKeyButtons.add(iv_wether_down);

        iv_air_conditioner_work_model = (ImageView) view.findViewById(R.id.iv_air_conditioner_work_model);
        tv_air_conditioner_temperature = (TextView) view.findViewById(R.id.tv_air_conditioner_temperature);
        dash_divide_line = view.findViewById(R.id.dash_divide_line);
        iv_swept_wind_state = (ImageView) view.findViewById(R.id.iv_swept_wind_state);
        iv_wind_direction_state = (ImageView) view.findViewById(R.id.iv_wind_direction_state);
        iv_wind_volume = (ImageView) view.findViewById(R.id.iv_wind_volume);

        irData = (IrData) getArguments().getSerializable(IntentKey.ALL_ONE_DATA);
        device = (Device) getArguments().getSerializable(IntentKey.DEVICE);
        isHomeClick = getArguments().getBoolean(IntentKey.IS_HOME_CLICK);
        isAction = getArguments().getBoolean(IntentKey.IS_ACTION, false);
        action = (Action) getArguments().getSerializable(IntentKey.ACTION);
    }

    private void initParams() {
        mKKACManager.initIRData(irData.rid, irData.exts, null);
        if (isHomeClick && device != null) {
            String acState = AlloneCache.getAcState(device.getDeviceId());
            mKKACManager.setACStateV2FromString(acState);//初始化空调状态
        } else {
            mKKACManager.setACStateV2FromString("");//初始化空调状态
        }
        initActionState();
        updateACDisplay();
    }


    /**
     * 初始化定时倒计时时的空调状态
     */
    private void initActionState() {
        if (isAction && action != null) {
            int[] data = AlloneACUtil.getAcValue(action.getValue2());
            int powerSatae = AlloneACUtil.getKKPower(data[0]);
            //设置开关
            if (mKKACManager.getPowerState() != powerSatae) {
                mKKACManager.changePowerState();
            }
            if (mKKACManager.getPowerState() != ACConstants.AC_POWER_OFF) {
                int modelType = AlloneACUtil.getKKMode(data[1]);
                //设置模式
                if (mKKACManager.isContainsTargetModel(modelType)) {
                    mKKACManager.changeACTargetModel(modelType);
                }
                //设置风量
                mKKACManager.setTargetWindSpeed(data[2]);
                //风向
                direction = data[3];
//                mKKACManager.setTargetUDWindDirect(direction);
                //设置温度
                mKKACManager.setTargetTemp(data[4]);
            }
        }
    }

    /**
     * 更新面板和按键显示
     */
    private void updateACDisplay() {
        if (mKKACManager.getPowerState() == ACConstants.AC_POWER_OFF) {
            //关闭电源
            iv_air_conditioner_work_model.setVisibility(View.INVISIBLE);
            tv_air_conditioner_temperature.setVisibility(View.INVISIBLE);
            dash_divide_line.setVisibility(View.INVISIBLE);
            iv_swept_wind_state.setVisibility(View.INVISIBLE);
            iv_wind_direction_state.setVisibility(View.INVISIBLE);
            iv_wind_volume.setVisibility(View.INVISIBLE);
        } else {
            iv_air_conditioner_work_model.setVisibility(View.VISIBLE);
            tv_air_conditioner_temperature.setVisibility(View.VISIBLE);
            dash_divide_line.setVisibility(View.VISIBLE);
            iv_swept_wind_state.setVisibility(View.VISIBLE);
            iv_wind_direction_state.setVisibility(View.VISIBLE);
            iv_wind_volume.setVisibility(View.VISIBLE);
            updateModes();
            updateWindSpeed();
            updateTemptures();
            updateWindDirectType();
        }
        updateIrButtonState();
        initListener();
    }

    private void initListener() {
        for (final IrKeyButton irKeyButton : mainIrKeyButtons) {
            irKeyButton.setOnClickListener(this);
        }
    }

    /**
     * 更新空调按钮状态
     */
    private void updateIrButtonState() {
        tv_air_conditioner_model.setMatched(true);
        tv_air_conditioner_power.setMatched(true);
        tv_air_conditioner_swept_wind.setMatched(true);
        tv_air_conditioner_wind_direction.setMatched(true);
        tv_air_conditioner_hearting.setMatched(mKKACManager.isContainsTargetModel(ACConstants.AC_MODE_HEAT));//是否支持制热
        tv_air_conditioner_refrigeration.setMatched(mKKACManager.isContainsTargetModel(ACConstants.AC_MODE_COOL));//是否支持制冷
        iv_wether_up.setMatched(mKKACManager.isTempCanControl());//温度+
        iv_wether_down.setMatched(mKKACManager.isTempCanControl());//温度-

        switch (mKKACManager.getCurUDDirectType()) {
            case UDDIRECT_ONLY_SWING://没有风向，风向不能使用
                tv_air_conditioner_wind_direction.setMatched(false);
                tv_air_conditioner_swept_wind.setMatched(false);
                break;
            case UDDIRECT_ONLY_FIX://只有固定风向可用，扫风不能使用
                tv_air_conditioner_swept_wind.setMatched(false);
                break;
            default:
                tv_air_conditioner_wind_direction.setMatched(true);
                tv_air_conditioner_swept_wind.setMatched(true);
                break;
        }
        tv_air_conditioner_air_volume.setMatched(mKKACManager.isWindSpeedCanControl());//风量
    }


    /**
     * 模式操作
     */
    private void updateModes() {
        int modeState = mKKACManager.getCurModelType();
        switch (modeState) {
            case ACConstants.AC_MODE_AUTO://自动
                iv_air_conditioner_work_model.setImageResource(R.drawable.pic_state_auto);
                break;
            case ACConstants.AC_MODE_COOL://制冷
                iv_air_conditioner_work_model.setImageResource(R.drawable.pic_state_cool);
                break;
            case ACConstants.AC_MODE_HEAT://制热
                iv_air_conditioner_work_model.setImageResource(R.drawable.pic_state_hot);
                break;
            case ACConstants.AC_MODE_FAN://送风
                iv_air_conditioner_work_model.setImageResource(R.drawable.pic_state_wind);
                break;
            case ACConstants.AC_MODE_DRY://除湿
                iv_air_conditioner_work_model.setImageResource(R.drawable.pic_state_water);
                break;
        }
    }

    /**
     * 温度显示与操作
     */
    private void updateTemptures() {
        if (mKKACManager.isTempCanControl()) {
            tv_air_conditioner_temperature.setText(mKKACManager.getCurTemp() + "°");
        } else {
            tv_air_conditioner_temperature.setText("");
        }
    }

    /**
     * 风量显示处理
     */
    private void updateWindSpeed() {
        if (mKKACManager.isWindSpeedCanControl()) {
            iv_wind_volume.setVisibility(View.VISIBLE);
            int speed = mKKACManager.getCurWindSpeed();
            switch (speed) {
                case ACConstants.AC_WIND_SPEED_AUTO://自动风量
                    iv_wind_volume.setImageResource(R.drawable.icon_wind_auto);
                    break;
                case ACConstants.AC_WIND_SPEED_HIGH://高
                    iv_wind_volume.setImageResource(R.drawable.icon_wind_strong);
                    break;
                case ACConstants.AC_WIND_SPEED_LOW://低
                    iv_wind_volume.setImageResource(R.drawable.icon_wind_weak);
                    break;
                case ACConstants.AC_WIND_SPEED_MEDIUM://中
                    iv_wind_volume.setImageResource(R.drawable.icon_wind_medium);
                    break;
            }
        } else {
            iv_wind_volume.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 风向显示处理
     */
    private void updateWindDirect() {
        if (direction == 3) {
            direction = 0;
        }
        iv_wind_direction_state.setVisibility(View.VISIBLE);
        switch (direction) {
            case 0://上
                iv_wind_direction_state.setImageResource(R.drawable.icon_wind_direction_updown);
                break;
            case 1://中
                iv_wind_direction_state.setImageResource(R.drawable.icon_wind_direction_horizontal);
                break;
            case 2://下
                iv_wind_direction_state.setImageResource(R.drawable.icon_wind_direction_down);
                break;
        }
    }

    /**
     * 扫风控制
     */
    private void updateWindDirectType() {
        if (mKKACManager.getPowerState() == ACConstants.AC_POWER_OFF) {
            iv_swept_wind_state.setVisibility(View.INVISIBLE);
        } else {
            // 空调风向处理
            ACStateV2.UDWindDirectType directType = mKKACManager.getCurUDDirectType();

            int windDirect_auto = mKKACManager.getCurUDDirect();

            switch (directType) {
                case UDDIRECT_ONLY_SWING://没有风向
                    iv_swept_wind_state.setImageResource(R.drawable.icon_swept_wind_disable);
                    iv_wind_direction_state.setVisibility(View.INVISIBLE);
                    break;
                case UDDIRECT_ONLY_FIX://只有固定风向或不区分扫风和固定风
                    iv_swept_wind_state.setImageResource(R.drawable.icon_swept_wind_disable);
                    updateWindDirect(); //更新一下 上 中 下 的屏幕
                    break;
                case UDDIRECT_FULL://同时具备固定风和扫风
                    if (windDirect_auto == ACStateV2.UDWINDDIRECT_AUTO) {//扫风
                        iv_swept_wind_state.setImageResource(R.drawable.icon_swept_wind_normal);
                        iv_wind_direction_state.setVisibility(View.INVISIBLE);
                    } else {//手动风向
                        iv_swept_wind_state.setImageResource(R.drawable.icon_swept_wind_disable);
                        updateWindDirect();
                    }
                    break;
            }
        }
    }


    /**
     * 设置工作模式
     *
     * @param modelType
     */
    private void setModel(int modelType) {
        //result:-1代表不具备该模式，1模式切换成功
        if (mKKACManager.isContainsTargetModel(modelType)) {
            mKKACManager.changeACTargetModel(modelType);
            updateACDisplay();
        }
    }

    @Override
    public void onClick(View v) {
        IrKeyButton irKeyButton = (IrKeyButton) v;
        if (irKeyButton.isMatched()) {
            // 按了其他按键,把空调的开关状态置为true
            if (v.getId() != R.id.tv_air_conditioner_power && mKKACManager.getPowerState() == ACConstants.AC_POWER_OFF) {
                mKKACManager.changePowerState();
                updateACDisplay();
            }
            switch (v.getId()) {
                case R.id.tv_air_conditioner_model://模式
                    mKKACManager.changeACModel();
                    updateACDisplay();
                    break;
                case R.id.tv_air_conditioner_power://电源
                    mKKACManager.changePowerState();
                    updateACDisplay();
                    break;
                case R.id.tv_air_conditioner_swept_wind://扫风
                    mKKACManager.changeUDWindDirect(ACStateV2.UDWindDirectKey.UDDIRECT_KEY_SWING);
                    updateWindDirectType();
                    break;
                case R.id.tv_air_conditioner_hearting://制热
                    setModel(ACConstants.AC_MODE_HEAT);
                    break;
                case R.id.tv_air_conditioner_wind_direction://风向
                    mKKACManager.changeUDWindDirect(ACStateV2.UDWindDirectKey.UDDIRECT_KEY_FIX);
                    direction++;
                    updateWindDirectType();
                    break;
                case R.id.tv_air_conditioner_refrigeration://制冷
                    setModel(ACConstants.AC_MODE_COOL);
                    break;
                case R.id.tv_air_conditioner_air_volume://风量
                    mKKACManager.changeWindSpeed();
                    updateWindSpeed();
                    break;
                case R.id.iv_wether_up://温度加
                    mKKACManager.increaseTmp();
                    updateTemptures();
                    break;
                case R.id.iv_wether_down://温度减
                    mKKACManager.decreaseTmp();
                    updateTemptures();
                    break;
            }
            AlloneControlData controlData = new AlloneControlData(irData.fre, mKKACManager.getACIRPattern());
            irKeyButton.setControlData(controlData);
            if (isAction && irKeyButton.isMatched()) {
                StringBuilder data = new StringBuilder();
                //组合码拼接，不能有小于0的出现，因为是int型
                data.append(AlloneACUtil.getHomematPower(mKKACManager.getPowerState())).append(AlloneACUtil.getHomemateMode(mKKACManager.getCurModelType())).append(mKKACManager.getCurWindSpeed() < 0 ? 0 : mKKACManager.getCurWindSpeed()).append(mKKACManager.getCurUDDirect() < 0 ? 0 : mKKACManager.getCurUDDirect()).append(mKKACManager.getCurTemp() < 0 ? 0 : mKKACManager.getCurTemp());
                irKeyButton.setFid(Integer.parseInt(data.toString()));
            }
            keyClickListener.OnClick(irKeyButton);
        }
    }

    @Override
    public void onResume() {
        mKKACManager.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mKKACManager.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (!isAction && isHomeClick && device != null) {
            //获取当前AC状态的字符串
            String acState = mKKACManager.getACStateV2InString();
            AlloneCache.saveAcState(device.getDeviceId(), acState);
            EventBus.getDefault().post(new AlloneViewEvent(true,device.getDeviceId()));
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            keyClickListener = (OnKeyClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onRefresh(IrData irData) {

    }

    @Override
    public void onRefresh(Action action) {

    }

    public void setIrData(IrData irData) {
        this.irData = irData;
        initParams();
    }

    /**
     * 添加完空调后把参数重新传递
     *
     * @param homeClick
     * @param device
     */
    public void setInitData(boolean homeClick, Device device) {
        isHomeClick = homeClick;
        this.device = device;
    }
}
