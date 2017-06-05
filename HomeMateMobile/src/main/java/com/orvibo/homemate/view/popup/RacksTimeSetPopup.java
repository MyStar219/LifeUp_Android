package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.ToastUtil;

/**
 * 自定义晾衣架时间选择弹框
 * Created by snown on 2015/11/18.
 */
public class RacksTimeSetPopup extends CommonPopup implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    private View wheel_ll;
    private NumberPicker hourPicker, minutePicker;
    private OnTimeResultListener onTimeResultListener;
    private View clickView;
    String[] minuteData = {"0", "10", "20", "30", "40", "50"};
    private View v1;

    public RacksTimeSetPopup(Context context) {
        mCommonPopupContext = context;
    }

    public void show(View v, String title, int[] time) {
        View contentView = View.inflate(mCommonPopupContext, R.layout.popup_racks_set_time, null);
        wheel_ll = contentView.findViewById(R.id.wheel_ll);
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_in));
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);
        TextView title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        title_tv.setText(title);
        hourPicker = (NumberPicker) contentView.findViewById(R.id.hourPicker);
        hourPicker.setOnValueChangedListener(this);
        hourPicker.setMaxValue(24);
        hourPicker.setMinValue(0);
        hourPicker.setValue(time[0]);
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        minutePicker = (NumberPicker) contentView.findViewById(R.id.minutePicker);
        int postion = 0;
        for (int i = 0; i < minuteData.length; i++) {
            if (minuteData[i].equals(String.valueOf(time[1]))) {
                postion = i;
            }
        }
        minutePicker.setDisplayedValues(minuteData);
        minutePicker.setOnValueChangedListener(this);
        minutePicker.setMaxValue(minuteData.length - 1);
        minutePicker.setMinValue(0);
        if (time[0] == 0 && postion == 0) {
            minutePicker.setValue(1);
        } else {
            minutePicker.setValue(postion);
        }
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        clickView = v;
        switch (clickView.getId()) {
            case R.id.itemWindDryingTime:
                hourPicker.setMaxValue(5);
                break;
            case R.id.itemHeatDryingTime:
                hourPicker.setMaxValue(5);
                break;
            case R.id.itemSterilizingTime:
                hourPicker.setMaxValue(3);
                break;
            case R.id.itemLightingTime:
                contentView.findViewById(R.id.cancel_tv).setVisibility(View.VISIBLE);
                break;
        }
        show(mCommonPopupContext, contentView, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //照明常亮
            case R.id.cancel_tv: {
                if (onTimeResultListener != null)
                    onTimeResultListener.timeResult(clickView, 0, 0);
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                break;
            }
            case R.id.confirm_tv: {
                if (onTimeResultListener != null)
                    onTimeResultListener.timeResult(clickView, hourPicker.getValue(), minutePicker.getValue());
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                break;
            }
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (hourPicker.getValue() == 0 && minutePicker.getValue() == 0) {
            ToastUtil.showToast(mCommonPopupContext.getString(R.string.cth_setting_minute));
            minutePicker.setValue(1);
        }

        if (hourPicker.getValue() == hourPicker.getMaxValue()) {
            minutePicker.setEnabled(false);
            minutePicker.setValue(0);
        } else {
            minutePicker.setEnabled(true);
        }
    }

    public void setOnTimeResultListener(OnTimeResultListener onTimeResultListener) {
        this.onTimeResultListener = onTimeResultListener;
    }

    public interface OnTimeResultListener {
        void timeResult(View v, int hour, int minute);
    }
}
