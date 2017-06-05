package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.TimePicker;

import com.smartgateway.app.R;

/**
 * Created by allen on 2015/10/12.
 */
public abstract class DeviceSetTimePopup extends CommonPopup implements View.OnClickListener {
    private View wheel_ll;
    private TimePicker timePicker;
    private View v1;
    private boolean is24HourFormat;
    public DeviceSetTimePopup(Context context) {
        mCommonPopupContext = context;
        is24HourFormat = DateFormat.is24HourFormat(mCommonPopupContext);
    }
    public DeviceSetTimePopup(Context context,boolean is24HourFormat) {
        mCommonPopupContext = context;
        this.is24HourFormat = is24HourFormat;
    }
    public void show (String title, int hour, int minute) {
        View contentView = View.inflate(mCommonPopupContext, R.layout.popup_set_time, null);
        wheel_ll = contentView.findViewById(R.id.wheel_ll);
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_in));
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);
        TextView title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        title_tv.setText(title);
        timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(is24HourFormat);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        show(mCommonPopupContext, contentView, true);
    }

    /**
     * create by yuwei 实时更新时间格式
     * @param title
     * @param hour
     * @param minute
     * @param is24Hour
     */
    public void show(String title, int hour, int minute,boolean is24Hour){
        is24HourFormat = is24Hour;
        show(title,hour,minute);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_tv: {
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                break;
            }
            case R.id.confirm_tv: {
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                onSetTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                break;
            }
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }

    public abstract void onSetTime(int hour, int minute);
}