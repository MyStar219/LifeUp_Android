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
public class ProjectorControlFragment extends BaseAlloneControlFragment implements View.OnClickListener {

    private static final String TAG = ProjectorControlFragment.class
            .getSimpleName();
    private IrKeyButton irKeyButtonZoomin;//放大
    private IrKeyButton irKeyButtonZoomout;//缩小
    protected IrKeyButton irKeyButtonBack;//返回


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projector_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        irKeyButtonBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonBack);
        irKeyButtonZoomin = (IrKeyButton) view.findViewById(R.id.irKeyButtonZoomin);
        irKeyButtonZoomout = (IrKeyButton) view.findViewById(R.id.irKeyButtonZoomout);
        mainIrKeyButtons.add(irKeyButtonZoomin);
        mainIrKeyButtons.add(irKeyButtonZoomout);
        mainIrKeyButtons.add(irKeyButtonBack);
    }

}
