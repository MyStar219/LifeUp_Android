package com.orvibo.homemate.device.allone2.irlearn;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKDevice;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.data.AlloneData;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnIrKeyLongClickListener;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by snow on 2016/4/11.
 * 红外学习fragment基类
 */
public class BaseLearnFragment extends Fragment implements View.OnClickListener, RemoteLearnActivity.OnIrLearnRefreshListener {


    /**
     * 主界面所有key
     */
    protected List<IrKeyButton> mainIrKeyButtons = new ArrayList<IrKeyButton>();


    protected KKDevice irData;

    protected AlloneData alloneData;
    protected List<KKIr> irKeys;//下拉数据列表
    protected HashMap<Integer, KKIr> keyHashMap;//固定数据列表(不在这个里面的都置灰，并且不可点击)
    protected OnKeyClickListener keyClickListener;
    protected OnIrKeyLongClickListener mOnIrKeyLongClickListener;
    protected boolean isStartLearn;

    protected Device device;

    protected boolean isAction = false;//定时倒计时
    protected Action action;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        irData = (KKDevice) getArguments().getSerializable(IntentKey.ALL_ONE_DATA);
        device = (Device) getArguments().getSerializable(IntentKey.DEVICE);
        isStartLearn = getArguments().getBoolean(IntentKey.IS_START_LEARN);
        isAction = getArguments().getBoolean(IntentKey.IS_ACTION, false);
        action = (Action) getArguments().getSerializable(IntentKey.ACTION);
        initMathData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
            mOnIrKeyLongClickListener = (OnIrKeyLongClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    /**
     * 匹配界面过滤显示数据
     */
    private void initMathData() {
        if (irData != null) {
            alloneData = AlloneDataUtil.getAlloneData(irData, device.getDeviceType());
            irKeys = alloneData.getIrLearnKeys();
            keyHashMap = alloneData.getKeyLearnHashMap();
        }
    }

    @Override
    public void onRefresh(KKDevice irData, boolean isStartLearn) {
        this.irData = irData;
        this.isStartLearn = isStartLearn;
        initMathData();
    }

    @Override
    public void onRefresh(Action action) {
        this.action=action;
    }

}
