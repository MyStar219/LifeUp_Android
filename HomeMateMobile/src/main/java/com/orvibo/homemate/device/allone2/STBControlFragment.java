package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.popup.SelectAlloneTVPopup;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Smagret on 2016/3/31.
 * 电视架遥控器控制面板
 * update by snown STB和TV界面整合到父类中
 */
public class STBControlFragment extends BaseAlloneControlFragment implements View.OnClickListener {

    protected IrKeyButton irKeyButtonLookBack;
    protected IrKeyButton irKeyButtonChannelAdd;
    protected IrKeyButton irKeyButtonChannelMinus;
    protected IrKeyButton irKeyButtonTV;
    protected IrKeyButton irKeyButtonBack;//返回
    protected IrKeyButton irKeyButtonMute;//静音
    protected List<String> allones = new ArrayList<>();
    protected boolean hasAlloneTV = false;

    protected List<Device> tvDevices = new ArrayList<>();
    protected KKIr kkIr;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stb_remote_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initSelectAlloneTVPopup();
        initData();
    }

    private void initView(View view) {
        irKeyButtonChannelAdd = (IrKeyButton) view.findViewById(R.id.irKeyButtonChannelAdd);
        irKeyButtonChannelMinus = (IrKeyButton) view.findViewById(R.id.irKeyButtonChannelMinus);
        irKeyButtonLookBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonLookBack);
        irKeyButtonTV = (IrKeyButton) view.findViewById(R.id.irKeyButtonTV);
        irKeyButtonBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonBack);
        irKeyButtonMute = (IrKeyButton) view.findViewById(R.id.irKeyButtonMute);
        mainIrKeyButtons.add(irKeyButtonChannelAdd);
        mainIrKeyButtons.add(irKeyButtonChannelMinus);
        mainIrKeyButtons.add(irKeyButtonLookBack);
        mainIrKeyButtons.add(irKeyButtonBack);
        mainIrKeyButtons.add(irKeyButtonMute);
    }

    private void initData() {
        tvDevices = new DeviceDao().selAlloneTvDevices(device.getUid(), DeviceType.TV, ModelID.Allone2);
        if (!tvDevices.isEmpty())
            hasAlloneTV = true;
        kkIr = KKIrDao.getInstance().getPower(device.getDeviceId(), true);
        if (isHomeClick) {
            irKeyButtonTV.setVisibility(View.VISIBLE);
            if (hasAlloneTV && kkIr != null) {
                irKeyButtonTV.setMatched(true);
            } else
                irKeyButtonTV.setMatched(false);
            //单独处理下绑定的电源按键
            if (isAction) {
                if (action != null && action.getValue2() == KKookongFid.fid_own_power) {
                    irKeyButtonTV.setSelected(true);
                } else {
                    irKeyButtonTV.setSelected(false);
                }
            }
        } else {
            irKeyButtonTV.setVisibility(View.GONE);
        }
        irKeyButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kkIr == null && !isAction) {
                    selectAlloneTVPopup.showPopup(getActivity(), device.getUid(), DeviceType.TV,DeviceType.STB);
                } else
                    keyClickListener.onTvClick(kkIr, irKeyButtonTV);
            }
        });
    }


    private void initSelectAlloneTVPopup() {
        selectAlloneTVPopup = new SelectAlloneTVPopup() {
            @Override
            public void bindTV(Device device) {
                keyClickListener.onTvSelected(device);
            }
        };
    }

    @Override
    public void onRefresh(IrData irData) {
        initData();
        super.onRefresh(irData);
    }

    /**
     * 当解除电源绑定时或更换遥控器时，界面刷新
     *
     * @param event
     */
    public void onEventMainThread(PartViewEvent event) {
        initData();
    }

}
