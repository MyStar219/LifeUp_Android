package com.smartgateway.app.fragment.state;

import android.content.Context;

import com.smartgateway.app.R;

/**
 * Created by yanyu on 5/15/2016.
 */
public class BookingStateHelper extends StateHelper {
    public BookingStateHelper(Context context) {
        super(context, R.array.state_booking_names);
    }

    @Override
    public StateData get(int tag) {
        return super.get(tag-100);
    }

    @Override
    protected int getColorArray() {
        return R.array.state_booking_colors;
    }

    @Override
    protected int getIconArray() {
        return R.array.state_booking_icons;
    }
}
