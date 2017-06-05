package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;

/**
 * Created by smagret on 2015/5/25.
 */
public class MainFourTabView extends RelativeLayout implements View.OnClickListener {
    private OnMainTabSelectedListener listener;

    private MainBottomFourTab deviceBottomTab;
    private MainBottomFourTab sceneBottomTab;
    private MainBottomFourTab securityBottomTab;
    private MainBottomFourTab personalBottomTab;

    public static final int DEVICE_POSITION = 0;
    public static final int SCENE_POSITION = 1;
    public static final int SECURITY_POSITION = 2;
    public static final int PERSONAL_POSITION = 3;

    public MainFourTabView(Context context) {
        this(context, null);
    }

    public MainFourTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(View.inflate(context, R.layout.main_four_tab_view, null), layoutParams);
        init();
    }

    public MainFourTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public MainFourTabView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    private void init() {
        deviceBottomTab = (MainBottomFourTab) findViewById(R.id.deviceBottomTab);
        sceneBottomTab = (MainBottomFourTab) findViewById(R.id.sceneBottomTab);
        securityBottomTab = (MainBottomFourTab)findViewById(R.id.securityBottomTab);
        personalBottomTab = (MainBottomFourTab) findViewById(R.id.personalBottomTab);

        deviceBottomTab.setOnClickListener(this);
        sceneBottomTab.setOnClickListener(this);
        securityBottomTab.setOnClickListener(this);
        personalBottomTab.setOnClickListener(this);

      //  deviceBottomTab.setSelected();
    }

    public void showSmartSceneTag(boolean show) {
        if (sceneBottomTab != null) {
            int curVisibility = sceneBottomTab.getVisibility();
            if (show && curVisibility != View.VISIBLE) {
                sceneBottomTab.setVisibility(View.VISIBLE);
            } else if (!show && curVisibility == View.VISIBLE) {
                sceneBottomTab.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceBottomTab:
                setSelectedPosition(DEVICE_POSITION, true);
                break;
            case R.id.sceneBottomTab:
                setSelectedPosition(SCENE_POSITION, true);
                break;
            case R.id.securityBottomTab:
                setSelectedPosition(SECURITY_POSITION, true);
                break;
            case R.id.personalBottomTab:
                setSelectedPosition(PERSONAL_POSITION, true);
                break;
        }
    }

    /**
     * @param position 选择的tab位置
     * @param callback true会回调对应的tab已选择，否则仅仅改变tab状态
     */
    public void setSelectedPosition(int position, boolean callback) {
        //reset();
        switch (position) {
            case DEVICE_POSITION:
                deviceBottomTab.setSelected();
                sceneBottomTab.setUnselected();
                securityBottomTab.setUnselected();
                personalBottomTab.setUnselected();
                if (callback) {
                    onMainTabSelected(DEVICE_POSITION);
                }
                break;
            case SCENE_POSITION:
                sceneBottomTab.setSelected();
                deviceBottomTab.setUnselected();
                securityBottomTab.setUnselected();
                personalBottomTab.setUnselected();
                if (callback) {
                    onMainTabSelected(SCENE_POSITION);
                }
                break;
            case SECURITY_POSITION:
                sceneBottomTab.setUnselected();
                deviceBottomTab.setUnselected();
                securityBottomTab.setSelected();
                personalBottomTab.setUnselected();
                if (callback) {
                    onMainTabSelected(SECURITY_POSITION);
                }
                break;
            case PERSONAL_POSITION:
                personalBottomTab.setSelected();
                deviceBottomTab.setUnselected();
                securityBottomTab.setUnselected();
                sceneBottomTab.setUnselected();
                if (callback) {
                    onMainTabSelected(PERSONAL_POSITION);
                }
                break;
        }
    }

    public void showPersonalIconHint() {
        if (personalBottomTab != null) {
            personalBottomTab.showIconHint();
        }
    }

    public void hidePersonalIconHint() {
        if (personalBottomTab != null) {
            personalBottomTab.hideIconHint();
        }
    }

    private void onMainTabSelected(int position) {
        if (listener != null) {
            listener.onMainFourTabSelected(position);
        }
    }

    public void reset() {
        setSelectedPosition(DEVICE_POSITION, true);
    }

    public void setOnMainTabSelectedListener(OnMainTabSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnMainTabSelectedListener {
        void onMainFourTabSelected(int position);
    }
}
