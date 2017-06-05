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
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.popup.SelectAlloneTVPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snown on 2016/7/8.
 * 电视盒子fragment
 */
public class TvBoxControlFragment extends BaseAlloneControlFragment implements View.OnClickListener {

    private static final String TAG = TvBoxControlFragment.class
            .getSimpleName();

    protected IrKeyButton irKeyButtonTV;
    private IrKeyButton irKeyButtonHomepage;
    protected IrKeyButton irKeyButtonBack;//返回
    protected boolean hasAlloneTV = false;

    protected List<Device> tvDevices = new ArrayList<>();
    protected KKIr kkIr;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tv_box_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initSelectAlloneTVPopup();
        initData();
    }

    private void initView(View view) {
        irKeyButtonTV = (IrKeyButton) view.findViewById(R.id.irKeyButtonTV);
        irKeyButtonHomepage = (IrKeyButton) view.findViewById(R.id.irKeyButtonHomepage);
        irKeyButtonBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonBack);
        mainIrKeyButtons.add(irKeyButtonHomepage);
        mainIrKeyButtons.add(irKeyButtonBack);
    }

    private void initData() {
        tvDevices = new DeviceDao().selAlloneTvDevices(device.getUid(), DeviceType.TV_BOX, ModelID.Allone2);
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
                    selectAlloneTVPopup.showPopup(getActivity(), device.getUid(), DeviceType.TV, DeviceType.TV_BOX);
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

}
