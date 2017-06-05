package com.smartgateway.app.fragment.state;

import android.content.Context;

import com.smartgateway.app.R;

/**
 * Created by yanyu on 5/15/2016.
 */
public class FeedbackStateHelper extends StateHelper {
    public FeedbackStateHelper(Context context) {
        super(context, R.array.state_maintenance_names);
    }

    @Override
    public StateData get(int tag) {
        return super.get(tag-200);
    }

    @Override
    protected int getColorArray() {
        return R.array.state_maintenance_colors;
    }

    @Override
    protected int getIconArray() {
        return R.array.state_maintenance_icons;
    }
}
