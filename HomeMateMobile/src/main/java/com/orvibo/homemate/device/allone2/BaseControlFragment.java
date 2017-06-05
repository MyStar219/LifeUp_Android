package com.orvibo.homemate.device.allone2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.kookong.app.data.api.IrData;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.AlloneData;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by snow on 2016/4/11.
 * 红外匹配基类,只负责处理数据
 */
public class BaseControlFragment extends Fragment implements View.OnClickListener, OnRefreshListener {


    protected boolean isHomeClick = false;

    /**
     * 主界面所有key
     */
    protected List<IrKeyButton> mainIrKeyButtons = new ArrayList<IrKeyButton>();

    protected Device device;

    protected IrData irData;

    protected AlloneData alloneData;
    protected List<IrData.IrKey> irKeys;//下拉数据列表
    protected HashMap<Integer, IrData.IrKey> keyHashMap;//固定数据列表(不在这个里面的都置灰，并且不可点击)
    protected OnKeyClickListener keyClickListener;
    protected boolean isAction = false;//定时倒计时
    protected Action action;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        irData = (IrData) getArguments().getSerializable(IntentKey.ALL_ONE_DATA);
        device = (Device) getArguments().getSerializable(IntentKey.DEVICE);
        isHomeClick = getArguments().getBoolean(IntentKey.IS_HOME_CLICK);
        isAction = getArguments().getBoolean(IntentKey.IS_ACTION, false);
        action = (Action) getArguments().getSerializable(IntentKey.ACTION);
        initMathData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
    }


    public void initListener() {
        for (final IrKeyButton irKeyButton : mainIrKeyButtons) {
            final int fid = irKeyButton.getFid();
            if (keyHashMap.containsKey(fid)) {
                irKeyButton.setMatched(true);
                if (isAction && action != null && action.getValue2() == fid) {
                    irKeyButton.setSelected(true);
                } else {
                    irKeyButton.setSelected(false);
                }
                AlloneControlData data = new AlloneControlData(irData.fre, keyHashMap.get(fid).pulse);
                irKeyButton.setControlData(data);
            } else {
                irKeyButton.setMatched(false);
            }

            irKeyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    keyClickListener.OnClick(irKeyButton);
                }
            });
        }
    }


    /**
     * 匹配界面过滤显示数据
     */
    private void initMathData() {
        if (irData != null) {
            alloneData = AlloneDataUtil.getAlloneData(irData, device.getDeviceType());
            irKeys = alloneData.getIrKeys();
            keyHashMap = alloneData.getKeyHashMap();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
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
        this.irData = irData;
        initMathData();
    }

    @Override
    public void onRefresh(Action action) {
        this.action = action;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void setDeviceCreatedData(boolean homeClick,Device device) {
        isHomeClick = homeClick;
        this.device=device;
    }
}
