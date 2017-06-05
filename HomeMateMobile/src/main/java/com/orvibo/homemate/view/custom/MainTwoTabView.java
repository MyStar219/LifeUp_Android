package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;

/**
 * Created by Orvibo smagret on 2015/5/25.
 */
public class MainTwoTabView extends RelativeLayout implements View.OnClickListener{
    private OnMainTabSelectedListener listener;
    private MainBottomTwoTab deviceBottomTab;
    private MainBottomTwoTab personalBottomTab;

    public static final int DEVICE_POSITION = 0;
    public static final int PERSONAL_POSITION = 1;

    public MainTwoTabView(Context context) {
        this(context , null);
    }

    public MainTwoTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(View.inflate(context, R.layout.main_two_tab_view, null), layoutParams);
        init();
    }

    private void init() {
        deviceBottomTab = (MainBottomTwoTab)findViewById(R.id.deviceBottomTab);
        personalBottomTab = (MainBottomTwoTab)findViewById(R.id.personalBottomTab);
        deviceBottomTab.setOnClickListener(this);
        personalBottomTab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceBottomTab:
                setSelectedPosition(DEVICE_POSITION);
                break;
            case R.id.personalBottomTab:
                setSelectedPosition(PERSONAL_POSITION);
                break;
        }
    }

    public void setSelectedPosition(int position) {
        //reset();
        switch (position) {
            case DEVICE_POSITION:
                deviceBottomTab.setSelected();
                personalBottomTab.setUnselected();
                onMainTabSelected(DEVICE_POSITION);
                break;
            case PERSONAL_POSITION:
                personalBottomTab.setSelected();
                deviceBottomTab.setUnselected();
                onMainTabSelected(PERSONAL_POSITION);
                break;
        }
    }

    private void onMainTabSelected(int position) {
        if (listener != null) {
            listener.onMainTwoTabSelected(position);
        }
    }

    public void reset() {
        setSelectedPosition(DEVICE_POSITION);
    }

    public void setOnMainTabSelectedListener(OnMainTabSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnMainTabSelectedListener {
        void onMainTwoTabSelected(int position);
    }


}
