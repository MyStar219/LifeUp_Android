package com.orvibo.homemate.device.allone2.irlearn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.IrKeyButton;

/**
 * Created by snow on 2016/4/11.
 * 电视红外学习
 */
public class TVLearnFragment extends AlloneBaseLearnFragment implements View.OnClickListener {

    private static final String TAG = TVLearnFragment.class
            .getSimpleName();
    private IrKeyButton irKeyButtonInput;
    private IrKeyButton irKeyButtonHomepage;


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
        mainIrKeyButtons.add(irKeyButtonInput);
        mainIrKeyButtons.add(irKeyButtonHomepage);
    }

}
