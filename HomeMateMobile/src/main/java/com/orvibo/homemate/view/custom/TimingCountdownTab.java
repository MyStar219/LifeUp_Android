package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * 定时倒计时顶部tab的控件
 * Created by smagret on 2015/12/08.
 */
public class TimingCountdownTab extends RelativeLayout {
    private int colorNormal;
    private int colorChecked;
    private TextView textView;
    private LinearLayout linearLayout;

    /**
     * 正常tab背景
     */
    private Drawable mNormalBg;

    /**
     * 选中tab背景
     */
    private Drawable mSelectedBg;

    public TimingCountdownTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimingCountdownTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TimingCountdownTab(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.timing_countdown_tab, this, true);
        textView = (TextView) findViewById(R.id.text);
        linearLayout = (LinearLayout) findViewById(R.id.bg);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.timingCountdownTab);
        String name = a.getString(R.styleable.timingCountdownTab_timingTabName);
        mNormalBg = a.getDrawable(R.styleable.timingCountdownTab_timingTabNormalBg);
        mSelectedBg = a.getDrawable(R.styleable.timingCountdownTab_timingTabSelectedBg);
        textView.setText(name);
        colorNormal = context.getResources().getColor(R.color.white);
        colorChecked = context.getResources().getColor(R.color.green);
        a.recycle();
    }

    /**
     * 选中
     */
    public void setSelected() {
        textView.setTextColor(colorChecked);
        linearLayout.setBackgroundDrawable(mSelectedBg);
    }

    /**
     * 不选中
     */
    public void setUnselected() {
        textView.setTextColor(colorNormal);
        linearLayout.setBackgroundDrawable(mNormalBg);
    }

    /** add version 1.8
     *提供一个可以接受外部传递资源的接口
     * @param color
     * @param drawable
     */
    public void setTextColorAndBackDrawable(int color, Drawable drawable) {
        textView.setTextColor(color);
        linearLayout.setBackgroundDrawable(drawable);
    }

    /**
     * @param name 设置tab的名字
     */
    public void setName(String name) {
        textView.setText(name);
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
    }


}
