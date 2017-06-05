package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * Create By Allen 2015/05/13
 */
public class CurtainControlOldView extends LinearLayout implements View.OnClickListener  {
    private OnStatusChangedListener onStatusChangedListener;
    private TextView tvOn, tvStop, tvOff;
    private boolean mVisible = true;

    public CurtainControlOldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(View.inflate(context, R.layout.curtain_control_old_view, null));
        initView();
        init();
    }

    private void initView() {
        tvOn = (TextView) findViewById(R.id.tvOn);
        tvStop = (TextView) findViewById(R.id.tvStop);
        tvOff = (TextView) findViewById(R.id.tvOff);
    }

    private void init() {
        tvOn.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvOff.setOnClickListener(this);
    }

    public void setStopButtonVisible(boolean visible) {
        mVisible = visible;
        if (visible) {
            tvStop.setVisibility(VISIBLE);
        } else {
            tvStop.setVisibility(GONE);
        }
    }

    public void setOnStatusChangedListener (OnStatusChangedListener onStatusChangedListener) {
        this.onStatusChangedListener = onStatusChangedListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOn: {
                if (!mVisible) {
                    open();
                }
                if (onStatusChangedListener != null) {
                    onStatusChangedListener.onCurtainOpen();
                }
                break;
            }
            case R.id.tvStop: {
                if (!mVisible) {
                    stopUnCheck();
                }
                if (onStatusChangedListener!=null){
                    onStatusChangedListener.onCurtainStop();
                }
                break;
            }
            case R.id.tvOff: {
                if (!mVisible) {
                    close();
                }
                if (onStatusChangedListener!=null){
                    onStatusChangedListener.onCurtainClose();
                }
                break;
            }
        }
    }

    public void open() {
        tvOn.setEnabled(false);
        tvStop.setEnabled(true);
        tvOff.setEnabled(true);
        tvOn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open_checked, 0, 0);
        tvStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop, 0, 0);
        tvOff.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close, 0, 0);
    }

    public void close() {
        tvOn.setEnabled(true);
        tvStop.setEnabled(true);
        tvOff.setEnabled(false);
        tvOn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
        tvStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop, 0, 0);
        tvOff.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close_checked, 0, 0);
    }

    public void stop() {
        tvOn.setEnabled(true);
        tvStop.setEnabled(false);
        tvOff.setEnabled(true);
        tvOn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
        tvStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop_checked, 0, 0);
        tvOff.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close,0,0);
    }

    public void stopUnCheck() {
        tvOn.setEnabled(true);
        tvStop.setEnabled(true);
        tvOff.setEnabled(true);
        tvOn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
        tvStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop, 0, 0);
        tvOff.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close,0,0);
    }

    public void initStatus(){
        tvOn.setEnabled(true);
        tvStop.setEnabled(true);
        tvOff.setEnabled(true);
        tvOn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_open, 0, 0);
        tvStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_stop, 0, 0);
        tvOff.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.curtain_two_half_close,0,0);
    }
    public interface OnStatusChangedListener {
        void onCurtainOpen();
        void onCurtainClose();
        void onCurtainStop();
    }
}
