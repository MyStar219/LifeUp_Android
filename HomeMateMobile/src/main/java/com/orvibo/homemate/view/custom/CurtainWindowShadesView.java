package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;

/**
 * 瑞祥百叶窗
 * Create By smagret 2016/04/13
 */
public class CurtainWindowShadesView extends LinearLayout implements View.OnClickListener {
    private OnActionClickListener    onActionClickListener;
    private CurtainRollerView curtainDropdownView;
    private RelativeLayout           curtainViewRelativeLayout;
    private LinearLayout             limitSetLinearLayout;
    /**
     * 控制模式 true：限位设置；false：表示普通控制
     */
    private boolean IS_LIMIT_SET = false;
    private TextView pageUpTextView, pageDownTextView, limitUpTextView, limitDownTextView, action_on, action_stop, action_off;

    public CurtainWindowShadesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(View.inflate(context, R.layout.curtain_window_shades_view, null));
        initView();
        refreshView();
    }

    private void initView() {
        curtainDropdownView = (CurtainRollerView) findViewById(R.id.curtainRollerView);
        curtainDropdownView.setTouchable(false);
        curtainViewRelativeLayout = (RelativeLayout) findViewById(R.id.curtainViewRelativeLayout);
        limitSetLinearLayout = (LinearLayout) findViewById(R.id.limitSetLinearLayout);
        pageUpTextView = (TextView) findViewById(R.id.pageUpTextView);
        pageUpTextView.setOnClickListener(this);
        pageDownTextView = (TextView) findViewById(R.id.pageDownTextView);
        pageDownTextView.setOnClickListener(this);
        limitUpTextView = (TextView) findViewById(R.id.limitUpTextView);
        limitUpTextView.setOnClickListener(this);
        limitDownTextView = (TextView) findViewById(R.id.limitDownTextView);
        limitDownTextView.setOnClickListener(this);
        action_on = (TextView) findViewById(R.id.action_on);
        action_on.setOnClickListener(this);
        action_stop = (TextView) findViewById(R.id.action_stop);
        action_stop.setOnClickListener(this);
        action_off = (TextView) findViewById(R.id.action_off);
        action_off.setOnClickListener(this);
    }


    public void setStopButtonVisible() {
        action_stop.setVisibility(View.VISIBLE);
    }

    public void setStopButtonGone() {
        action_stop.setVisibility(View.GONE);
    }

    public void setOpenButtonEnable() {
        action_on.setEnabled(true);
        action_on.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
    }

    public void setOpenButtonDisable() {
        action_on.setEnabled(false);
        action_on.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open_disabled, 0, 0);
    }

    public void setCloseButtonEnable() {
        action_off.setEnabled(true);
        action_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close, 0, 0);
    }

    public void setCloseButtonDisable() {
        action_off.setEnabled(false);
        action_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_disabled, 0, 0);
    }

    private void refreshView() {
        if (IS_LIMIT_SET) {
            curtainViewRelativeLayout.setVisibility(View.GONE);
            limitSetLinearLayout.setVisibility(View.VISIBLE);
        } else {
            curtainViewRelativeLayout.setVisibility(View.VISIBLE);
            limitSetLinearLayout.setVisibility(View.GONE);
        }
    }

    public void setLimitSet() {
        IS_LIMIT_SET = true;
        refreshView();
    }

    /**
     * 根据进度百分比，绘制窗帘布
     *
     * @param progress 进度百分比0-100
     */
    public void setProgress(int progress) {
        if (progress >= DeviceStatusConstant.CURTAIN_STATUS_ON) {
            curtainDropdownView.setProgress(DeviceStatusConstant.CURTAIN_ON);
        } else if (progress <= DeviceStatusConstant.CURTAIN_STATUS_OFF) {
            curtainDropdownView.setProgress(DeviceStatusConstant.CURTAIN_OFF);
        } else {
            curtainDropdownView.setProgress(progress);
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();

        switch (vId) {
            case R.id.action_on:
                if (IS_LIMIT_SET) {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_COARSE_TUNE_UPPER, 0);
                } else {
                    onActionClickListener.onActionClick(DeviceOrder.OPEN, DeviceStatusConstant.CURTAIN_ON);
                }
                break;
            case R.id.action_stop:
                if (IS_LIMIT_SET) {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_STOP_TUNING, 0);
                } else {
                    onActionClickListener.onActionClick(DeviceOrder.STOP, DeviceStatusConstant.CURTAIN_STOP);
                }
                break;
            case R.id.action_off:
                if (IS_LIMIT_SET) {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_COARSE_TUNE_LOWER, 0);
                } else {
                    onActionClickListener.onActionClick(DeviceOrder.OPEN, DeviceStatusConstant.CURTAIN_OFF);
                }
                break;
            case R.id.pageUpTextView:
                if (IS_LIMIT_SET) {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_FINE_TUNE_UPPER, 0);
                } else {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_PAGE_UP, 0);
                }
                break;
            case R.id.pageDownTextView:
                if (IS_LIMIT_SET) {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_FINE_TUNE_LOWER, 0);
                } else {
                    onActionClickListener.onActionClick(DeviceOrder.CURTAIN_PAGE_DOWN, 0);
                }
                break;
            case R.id.limitUpTextView:
                onActionClickListener.onActionClick(DeviceOrder.CURTAIN_UPPER_POSITION, 0);
                break;
            case R.id.limitDownTextView:
                onActionClickListener.onActionClick(DeviceOrder.CURTAIN_LOWER_POSITION, 0);
                break;
        }
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        onActionClickListener = listener;
    }

    public interface OnActionClickListener {
        void onActionClick(String order, int progress);
    }
}
