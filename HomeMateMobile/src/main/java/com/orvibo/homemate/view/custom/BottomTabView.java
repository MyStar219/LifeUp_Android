package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * BottomTabView
 * Created by MDev on 12/17/16.
 */

public class BottomTabView extends LinearLayout {
    TextView txtIcon, txtDescription;

    public BottomTabView(Context context) {
        this(context, null);
    }

    public BottomTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.bottom_tab, null);
        view.findViewById(R.id.txtIcon);
        txtIcon = (TextView) view.findViewById(R.id.txtIcon);
        txtDescription = (TextView) view.findViewById(R.id.txtDescription);
        this.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void setActive(boolean enabled) {
        txtIcon.setTextColor(ContextCompat.getColor(getContext(), enabled ? R.color.colorPrimary : R.color.black));
        txtDescription.setTextColor(ContextCompat.getColor(getContext(), enabled ? R.color.colorPrimary : R.color.black));
    }

    public void setText(int icon, int description) {
        txtIcon.setText(icon);
        txtDescription.setText(description);
    }
}
