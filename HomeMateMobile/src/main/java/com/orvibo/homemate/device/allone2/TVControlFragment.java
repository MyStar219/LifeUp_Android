package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.IrKeyButton;

/**
 * Created by Smagret on 2016/3/31.
 * 电视架遥控器控制面板
 * update by snown STB和TV界面整合到父类中
 */
public class TVControlFragment extends BaseAlloneControlFragment implements View.OnClickListener{

    private static final String TAG = TVControlFragment.class
            .getSimpleName();
    private IrKeyButton irKeyButtonInput;
    private IrKeyButton irKeyButtonHomepage;
    protected IrKeyButton irKeyButtonBack;//返回
    protected IrKeyButton irKeyButtonMute;//静音


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tv_remote_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        irKeyButtonInput = (IrKeyButton) view.findViewById(R.id.irKeyButtonInput);
        irKeyButtonHomepage = (IrKeyButton) view.findViewById(R.id.irKeyButtonHomepage);
        irKeyButtonBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonBack);
        irKeyButtonMute = (IrKeyButton) view.findViewById(R.id.irKeyButtonMute);
        mainIrKeyButtons.add(irKeyButtonInput);
        mainIrKeyButtons.add(irKeyButtonHomepage);
        mainIrKeyButtons.add(irKeyButtonBack);
        mainIrKeyButtons.add(irKeyButtonMute);
    }

}
