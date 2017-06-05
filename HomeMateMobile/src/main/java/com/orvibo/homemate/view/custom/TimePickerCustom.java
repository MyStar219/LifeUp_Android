package com.orvibo.homemate.view.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TimePicker;

/**
 * Created by snown on 2015/11/19.
 */
public class TimePickerCustom extends TimePicker {
    public TimePickerCustom(Context context) {
        super(context);
    }

    public TimePickerCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePickerCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TimePickerCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
