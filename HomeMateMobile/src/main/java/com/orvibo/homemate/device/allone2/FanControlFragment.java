package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.custom.MyGridView;

/**
 * Created by snown on 2016/6/1.
 * 风扇fragment
 */
public class FanControlFragment extends BaseControlFragment {

    private IrKeyButton irKeyButtonPower;
    private IrKeyButton irKeyButtonFanSpeed;
    private IrKeyButton irKeyButtonSwing;
    private IrKeyButton irKeyButtonSwingMode;

    protected GridView moreKeyGridView;
    protected MorekeyGridViewAdapter morekeyGridViewAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fan_remote_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initGirdData(false);
    }

    private void initView(View view) {
        irKeyButtonPower = (IrKeyButton) view.findViewById(R.id.irKeyButtonPower);
        irKeyButtonFanSpeed = (IrKeyButton) view.findViewById(R.id.irKeyButtonFanSpeed);
        irKeyButtonSwing = (IrKeyButton) view.findViewById(R.id.irKeyButtonSwing);
        irKeyButtonSwingMode = (IrKeyButton) view.findViewById(R.id.irKeyButtonSwingMode);
        moreKeyGridView = (MyGridView) view.findViewById(R.id.moreKeyGridView);
        mainIrKeyButtons.add(irKeyButtonPower);
        mainIrKeyButtons.add(irKeyButtonFanSpeed);
        mainIrKeyButtons.add(irKeyButtonSwing);
        mainIrKeyButtons.add(irKeyButtonSwingMode);
    }

    /**
     * 初始或更新更多中的数据
     */
    private void initGirdData(boolean isUpdate) {
        if (!isUpdate) {
            morekeyGridViewAdapter = new MorekeyGridViewAdapter(getActivity(), irKeys, irData.fre, keyClickListener, true);
            morekeyGridViewAdapter.setAction(isAction);
            morekeyGridViewAdapter.setAction(action);
            moreKeyGridView.setAdapter(morekeyGridViewAdapter);
        } else if (morekeyGridViewAdapter != null) {
            morekeyGridViewAdapter.setAction(isAction);
            morekeyGridViewAdapter.setAction(action);
            morekeyGridViewAdapter.updateData(irKeys, irData.fre);
        }
    }

    @Override
    public void onRefresh(IrData irData) {
        super.onRefresh(irData);
        initListener();
        initGirdData(true);
    }

    @Override
    public void onRefresh(Action action) {
        super.onRefresh(action);
        initGirdData(true);
    }
}
