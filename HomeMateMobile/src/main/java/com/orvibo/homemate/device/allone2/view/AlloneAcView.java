package com.orvibo.homemate.device.allone2.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.hzy.tvmao.KKACManagerV2;
import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.ir.ac.ACConstants;
import com.hzy.tvmao.ir.ac.ACStateV2;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.device.allone2.RemoteControlActivity;
import com.orvibo.homemate.event.AlloneViewEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.AlloneACUtil;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by snown on 2016/7/11.
 * 首页小方子设备空调的view类
 */
public class AlloneAcView extends BaseAlloneItemView implements View.OnClickListener, BaseResultListener {


    private KKACManagerV2 mKKACManager = new KKACManagerV2();
    private boolean isPowerOff;

    protected View tempView;
    protected TextView temp;
    protected RulerWheel rulerView;
    protected Set<IrKeyButton> mainIrKeyButtons = new HashSet<>();


    public AlloneAcView(Context context) {
        super(context);
    }

    public AlloneAcView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        temp = (TextView) findViewById(R.id.temp);
        rulerView = (RulerWheel) findViewById(R.id.rulerView);
        tempView = findViewById(R.id.tempView);
    }


    public void setData(Device device, FragmentManager fragmentManager,boolean isOnline) {
        super.setData(device, fragmentManager,isOnline);
        if (!StringUtil.isEmpty(deviceId) && !deviceId.equalsIgnoreCase(device.getDeviceId()))
            return;
        arrowViewStateRemember();
        initData();
    }

    private void initData() {
        irData = AlloneCache.getIrData(deviceId);
        if (irData != null) {
            mKKACManager.initIRData(irData.rid, irData.exts, null);
            String acState = AlloneCache.getAcState(deviceId);
            mKKACManager.setACStateV2FromString(acState);//初始化空调状态
            mainIrKeyButtons.clear();
            mainIrKeyButtons.add(btnPower);
            mainIrKeyButtons.add(btnTwo);
            mainIrKeyButtons.add(btnThree);
            mainIrKeyButtons.add(btnFour);
            updateACDisplay();
        }
        initListener();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.arrowView:
                arrowControl();
                break;
            case R.id.btnPower://电源
                mKKACManager.changePowerState();
                updateACDisplay();
                break;
            case R.id.btnTwo://模式
                mKKACManager.changeACModel();
                updateACDisplay();
                break;
            case R.id.btnThree://风量
                mKKACManager.changeWindSpeed();
                updateWindSpeed();
                break;
            case R.id.btnFour://扫风
                mKKACManager.changeUDWindDirect(ACStateV2.UDWindDirectKey.UDDIRECT_KEY_SWING);
                updateWindDirectType();
                break;
        }
        if (v.getId() != R.id.arrowView) {
            IrKeyButton irKeyButton = (IrKeyButton) v;
            AlloneControlData controlData = new AlloneControlData(irData.fre, mKKACManager.getACIRPattern());
            irKeyButton.setControlData(controlData);
            super.controlIrBtn(v);
            String acState = mKKACManager.getACStateV2InString();
            AlloneCache.saveAcState(device.getDeviceId(), acState);
        }

    }


    /**
     * 更新面板和按键显示
     */
    private void updateACDisplay() {
        isPowerOff = mKKACManager.getPowerState() == ACConstants.AC_POWER_OFF;
        for (IrKeyButton irKeyButton : mainIrKeyButtons) {
            if (irKeyButton.getFid() != KKookongFid.fid_1_power)
                irKeyButton.setEnabled(!isPowerOff);
        }
        temp.setTextColor(isPowerOff ? getResources().getColor(R.color.gray) : getResources().getColor(R.color.green));
        tempView.setVisibility(isPowerOff ? GONE : VISIBLE);
        rulerView.setEnabled(!isPowerOff);
        updateModes();
        updateWindSpeed();
        updateTemptures();
        updateWindDirectType();
        updateIrButtonState();
    }

    private void initListener() {
        for (final IrKeyButton irKeyButton : mainIrKeyButtons) {
            irKeyButton.setOnClickListener(this);
        }
    }

    /**
     * 箭头的控制
     */
    protected void arrowControl() {
        isFirst = false;
        if (controlView.isShown()) {
            arrowViewShow(false);
        } else {
            if (irData == null) {
                irData = AlloneCache.getIrData(deviceId);
                if (irData == null)
                    loadIrData(device.getIrDeviceId());
                else {
                    arrowViewShow(true);
                    initData();
                }
            } else {
                arrowViewShow(true);
                updateACDisplay();
            }
        }
    }

    /**
     * 更新空调按钮状态
     */
    private void updateIrButtonState() {
        btnPower.setMatched(true);
        btnTwo.setMatched(true);

        switch (mKKACManager.getCurUDDirectType()) {
            case UDDIRECT_ONLY_SWING://没有风向，风向不能使用
            case UDDIRECT_ONLY_FIX://只有固定风向可用，扫风不能使用
                btnFour.setMatched(false);
                break;
            default:
                btnFour.setMatched(true);
                break;
        }
        btnThree.setMatched(mKKACManager.isWindSpeedCanControl());//风量
    }


    /**
     * 模式操作
     */
    private void updateModes() {
        int modeState = mKKACManager.getCurModelType();
        switch (modeState) {
            case ACConstants.AC_MODE_AUTO://自动
                btnTwo.setBackgroundResource(R.drawable.icon_home_auto);
                break;
            case ACConstants.AC_MODE_COOL://制冷
                btnTwo.setBackgroundResource(R.drawable.icon_home_cooling);
                break;
            case ACConstants.AC_MODE_HEAT://制热
                btnTwo.setBackgroundResource(R.drawable.icon_home_heating);
                break;
            case ACConstants.AC_MODE_FAN://送风
                btnTwo.setBackgroundResource(R.drawable.icon_exhaust_5);
                break;
            case ACConstants.AC_MODE_DRY://除湿
                btnTwo.setBackgroundResource(R.drawable.icon_home_dehumidification);
                break;
        }
    }

    /**
     * 温度显示与操作
     */
    private void updateTemptures() {
        if (mKKACManager.isTempCanControl()) {
            tempView.setVisibility(VISIBLE);
            int minTem = mKKACManager.getMinTemp();
            int maxTem = mKKACManager.getMaxTemp();
            initTempNum(minTem, maxTem);
        } else {
            tempView.setVisibility(GONE);
        }
    }

    private void initTempNum(int minTem, int maxTem) {
        final List<String> list = new ArrayList<>();
        for (int i = minTem; i < maxTem; i += 1) {
            list.add(i + "");
            for (int j = 1; j < 10; j++) {
                list.add(i + "." + j);
            }
        }
        list.add(maxTem + "");
        rulerView.setData(list);
        rulerView.setSelectedValue(mKKACManager.getCurTemp() + "");
        temp.setText(mKKACManager.getCurTemp() + "°");
        rulerView.setScrollingListener(new RulerWheel.OnWheelScrollListener<String>() {
            @Override
            public void onChanged(RulerWheel wheel, String oldValue, String newValue) {
            }

            @Override
            public void onScrollingStarted(RulerWheel wheel) {

            }

            @Override
            public void onScrollingFinished(RulerWheel wheel) {
                int index = wheel.getValue();
                int newValue = MathUtil.getRoundData(list.get(index));
                rulerView.setSelectedValue(newValue + "");
                temp.setText(newValue + "°");
                mKKACManager.setTargetTemp(newValue);
                AlloneControlData controlData = new AlloneControlData(irData.fre, mKKACManager.getACIRPattern());
                controlIrBtn(controlData);
                String acState = mKKACManager.getACStateV2InString();
                AlloneCache.saveAcState(device.getDeviceId(), acState);
            }
        });
    }

    /**
     * 风量显示处理
     */
    private void updateWindSpeed() {
        if (mKKACManager.isWindSpeedCanControl()) {
            btnThree.setMatched(true);
            int speed = mKKACManager.getCurWindSpeed();
            switch (speed) {
                case ACConstants.AC_WIND_SPEED_AUTO://自动风量
                    btnThree.setBackgroundResource(R.drawable.icon_exhaust_auto);
                    break;
                case ACConstants.AC_WIND_SPEED_HIGH://高
                    btnThree.setBackgroundResource(R.drawable.icon_exhaust_5);
                    break;
                case ACConstants.AC_WIND_SPEED_LOW://低
                    btnThree.setBackgroundResource(R.drawable.icon_exhaust_3);
                    break;
                case ACConstants.AC_WIND_SPEED_MEDIUM://中
                    btnThree.setBackgroundResource(R.drawable.icon_exhaust_4);
                    break;
            }
        } else {
            btnThree.setMatched(false);
        }
    }


    /**
     * 扫风控制
     */
    private void updateWindDirectType() {
        // 空调风向处理
        ACStateV2.UDWindDirectType directType = mKKACManager.getCurUDDirectType();

        int windDirect_auto = mKKACManager.getCurUDDirect();

        switch (directType) {
            case UDDIRECT_ONLY_SWING://没有风向
                btnFour.setBackgroundResource(R.drawable.icon_home_wind_off);
                break;
            case UDDIRECT_ONLY_FIX://只有固定风向或不区分扫风和固定风
                btnFour.setBackgroundResource(R.drawable.icon_home_wind_off);
                break;
            case UDDIRECT_FULL://同时具备固定风和扫风
                if (windDirect_auto == ACStateV2.UDWINDDIRECT_AUTO) {//扫风
                    btnFour.setBackgroundResource(R.drawable.icon_home_wind);
                } else {//手动风向
                    btnFour.setBackgroundResource(R.drawable.icon_home_wind_off);
                }
                break;
        }
    }

    /**
     * 根据rid获取对应的红外码
     *
     * @param rid
     */
    public void loadIrData(String rid) {
        progressDialogFragment.show(fragmentManager, "");
        KookongSDK.getIRDataById(rid, new IRequestResult<IrDataList>() {

            @Override
            public void onSuccess(String msg, IrDataList result) {
                progressDialogFragment.dismiss();
                List<IrData> irDatas = result.getIrDataList();
                irData = irDatas.get(0);
                AlloneCache.saveIrData(irData, deviceId);
                if (irData.type == AlloneACUtil.AC_TYPE_SIMPLE) {
                    AlloneCache.setSingleAc(deviceId, true);
                    Intent intent = new Intent(context, RemoteControlActivity.class);
                    intent.putExtra(IntentKey.DEVICE, device);
                    intent.putExtra(IntentKey.IS_HOME_CLICK, true);
                    context.startActivity(intent);
                    EventBus.getDefault().post(new AlloneViewEvent());
                    return;
                } else {
                    arrowViewShow(true);
                    initData();
                }
            }

            @Override
            public void onFail(String msg) {
                progressDialogFragment.dismiss();
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    /**
     * 更换遥控器或者空调点击状态有变化时，更新对应的view
     *
     * @param event
     */
    public void onEventMainThread(ArrowViewUpdateEvent event) {
        if (deviceId.equalsIgnoreCase(event.getDeviceId())) {
                arrowViewShow(event.getDeviceId());
        }
    }

    public void onEventMainThread(AlloneViewEvent event) {
        if (deviceId.equalsIgnoreCase(event.getDeviceId()) && event.isControl()) {
            initData();//空调界面点击后要把首页空调的数据重新同步一遍
        }
    }

}
