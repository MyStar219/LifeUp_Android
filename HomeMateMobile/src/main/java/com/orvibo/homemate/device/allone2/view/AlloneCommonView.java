package com.orvibo.homemate.device.allone2.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by snown on 2016/7/11.
 * 首页小方子设备风扇,电视，机顶盒的view类
 */
public class AlloneCommonView extends BaseAlloneItemView implements View.OnClickListener, BaseResultListener {
    protected Set<IrKeyButton> mainIrKeyButtons = new HashSet<>();


    public AlloneCommonView(Activity context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlloneCommonView(Context context) {
        super(context);
    }

    public void setData(Device device, FragmentManager fragmentManager,boolean isOnlone) {
        super.setData(device, fragmentManager,isOnlone);
        if (!StringUtil.isEmpty(deviceId) && !deviceId.equalsIgnoreCase(device.getDeviceId()))
            return;
        initData();
        matchData();
        arrowViewStateRemember();
    }

    /**
     * 根据类型区分显示
     */
    private void initData() {
        btnPower.setFid(KKookongFid.fid_1_power);
        switch (device.getDeviceType()) {
            case DeviceType.FAN:
                btnTwo.setBackgroundResource(R.drawable.icon_home_speed);
                btnTwo.setFid(KKookongFid.fid_9367_fan_speed);
                btnThree.setBackgroundResource(R.drawable.icon_home_head);
                btnThree.setFid(KKookongFid.fid_9362_swing);
                btnFour.setBackgroundResource(R.drawable.icon_home_pattern);
                btnFour.setFid(KKookongFid.fid_9372_swing_mode);
                break;
            case DeviceType.SPEAKER_BOX:
                btnTwo.setBackgroundResource(R.drawable.icon_home_play);
                btnTwo.setFid(KKookongFid.fid_146_play);
                btnThree.setBackgroundResource(R.drawable.icon_home_suspend);
                btnThree.setFid(KKookongFid.fid_166_pause);
                btnFour.setBackgroundResource(R.drawable.icon_home_stop);
                btnFour.setFid(KKookongFid.fid_161_stop);
                break;
            case DeviceType.STB:
                btnTwo.setBackgroundResource(R.drawable.icon_home_menu);
                btnTwo.setFid(0);
                btnThree.setBackgroundResource(R.drawable.icon_home_upper);
                btnThree.setFid(KKookongFid.fid_43_channel_up);
                btnFour.setBackgroundResource(R.drawable.icon_home_lower);
                btnFour.setFid(KKookongFid.fid_44_channel_down);
                break;
        }
        mainIrKeyButtons.clear();
        mainIrKeyButtons.add(btnPower);
        mainIrKeyButtons.add(btnTwo);
        mainIrKeyButtons.add(btnThree);
        mainIrKeyButtons.add(btnFour);

    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.arrowView:
                arrowControl();
                break;
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
                else
                    updateView();
            } else {
                updateView();
            }
        }
    }

    /**
     * 红外码数据获得后更新view界面
     */
    protected void updateView() {
        arrowViewShow(true);
        matchData();
    }

    /**
     * icon匹配
     */
    protected void matchData() {
        irData = AlloneCache.getIrData(device.getDeviceId());
        if (irData != null) {
            keyHashMap = AlloneDataUtil.getAlloneData(irData, device.getDeviceType()).getKeyHashMap();
            for (final IrKeyButton irKeyButton : mainIrKeyButtons) {
                int fid = irKeyButton.getFid();
                if (keyHashMap.containsKey(fid)) {
                    irKeyButton.setMatched(true);
                    AlloneControlData data = new AlloneControlData(irData.fre, keyHashMap.get(fid).pulse);
                    irKeyButton.setControlData(data);
                } else {
                    irKeyButton.setMatched(fid == 0 ? true : false);
                }
            }
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
                updateView();
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

}
