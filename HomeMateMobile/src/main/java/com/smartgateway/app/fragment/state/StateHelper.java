package com.smartgateway.app.fragment.state;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.DrawableRes;

import com.smartgateway.app.R;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/14/2016.
 */
public class StateHelper {

    public static class StateData extends AbstractData {
        private int color;
        private String name;
        private @DrawableRes int icon;

        public StateData(int color, String name, int icon) {
            this.color = color;
            this.name = name;
            this.icon = icon;
        }

        public int getColor() {
            return color;
        }

        public String getName() {
            return name;
        }

        public int getIcon() {
            return icon;
        }
    }

    private StateData[] states = null;

    public StateHelper(Context context, int nameArrayId) {
        String[] stateNames = context.getResources().getStringArray(nameArrayId);
        String[] stateColors = context.getResources().getStringArray(getColorArray());
        TypedArray imgs = context.getResources().obtainTypedArray(getIconArray());
        int size = stateNames.length;
        if (size != stateColors.length) {
            throw new IllegalArgumentException("Arrays of state names and state colors are different length");
        }
        states = new StateData[size];
        for (int i=0; i<size; i++) {
            states[i] = new StateData(Color.parseColor("#"+stateColors[i]), stateNames[i], imgs.getResourceId(i, 0));
        }
    }

    protected int getColorArray() {
        return R.array.state_colors;
}

    protected int getIconArray() {
        return R.array.state_booking_icons;
    }

    public StateData get(int tag) {
        return states[tag];
    }

    public int size() {
        return states.length;
    }
}
