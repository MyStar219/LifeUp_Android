package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;

/**
 * 倒计时 安防 顶部tab的自定义控件
 * Created by Orvibo smagret on 2015/12/08.
 */
public class TimingCountdownTabView extends RelativeLayout implements View.OnClickListener {
    private OnTabSelectedListener listener;
    public TimingCountdownTab timingTopTab;
    public TimingCountdownTab countdownTopTab;

    public static final int TIMING_POSITION = 0;
    public static final int COUNTDOWN_POSITION = 1;
    /**
     * 安防界面点击的时候，tab不是立马显示为选中状态
     */
    private boolean isArmView = false;
    private boolean isRemoteView = false;
    private Context mContext;

    public TimingCountdownTabView(Context context) {
        this(context, null);
    }

    public TimingCountdownTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(View.inflate(context, R.layout.timing_countdown_tab_view, null), layoutParams);
        init();
    }

    private void init() {
        timingTopTab = (TimingCountdownTab) findViewById(R.id.timingTopTab);
        countdownTopTab = (TimingCountdownTab) findViewById(R.id.countdownTopTab);
        timingTopTab.setOnClickListener(this);
        countdownTopTab.setOnClickListener(this);
    }


    public void initName(String name1, String name2) {
        timingTopTab.setName(name1);
        countdownTopTab.setName(name2);
    }

    public void setArmView(boolean isArmView) {
        this.isArmView = isArmView;
    }

    public void setRemoteView(boolean isRemoteView) {
        this.isRemoteView = isRemoteView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timingTopTab:
                setSelectedPosition(TIMING_POSITION);
                break;
            case R.id.countdownTopTab:
                setSelectedPosition(COUNTDOWN_POSITION);
                break;
        }
    }

    public void setSelectedPosition(int position) {
        //reset();
        switch (position) {
            case TIMING_POSITION:
                if (!isArmView) {
                    timingTopTab.setSelected();
                    countdownTopTab.setUnselected();
                }
                if (isRemoteView) {
                    timingTopTab.setTextColorAndBackDrawable(mContext.getResources().getColor(R.color.white), mContext.getResources().getDrawable(R.drawable.bg_btn_checked));
                    countdownTopTab.setTextColorAndBackDrawable(mContext.getResources().getColor(R.color.white), mContext.getResources().getDrawable(R.drawable.bg_btn_r_normal));
                }
                onTimingTabSelected(TIMING_POSITION);
                break;
            case COUNTDOWN_POSITION:
                if (!isArmView) {
                    countdownTopTab.setSelected();
                    timingTopTab.setUnselected();
                }
                if (isRemoteView) {
                    timingTopTab.setTextColorAndBackDrawable(mContext.getResources().getColor(R.color.white), mContext.getResources().getDrawable(R.drawable.bg_btn_normal));
                    countdownTopTab.setTextColorAndBackDrawable(mContext.getResources().getColor(R.color.white), mContext.getResources().getDrawable(R.drawable.bg_btn_r_checked));
                }
                onTimingTabSelected(COUNTDOWN_POSITION);
                break;
        }
    }

    public void setTabSelectedView(int position) {
        switch (position) {
            case TIMING_POSITION:
                timingTopTab.setSelected();
                countdownTopTab.setUnselected();
                break;
            case COUNTDOWN_POSITION:
                countdownTopTab.setSelected();
                timingTopTab.setUnselected();
                break;
        }
    }

    private void onTimingTabSelected(int position) {
        if (listener != null) {
            listener.onTabSelected(position);
        }
    }

    public void reset() {
        setSelectedPosition(TIMING_POSITION);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }
}
