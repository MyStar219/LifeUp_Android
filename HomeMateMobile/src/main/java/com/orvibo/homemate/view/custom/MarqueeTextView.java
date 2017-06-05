package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 跑马灯TextView，特别用于一个界面有多个跑马灯
 * Created by zhaoxiaowei on 2016/2/19.
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
//        自定义设置让focusable为true
//        这个方法相当于在layout中
//        android:focusable="true"
//        android:focusableInTouchMode="true"
    }
}

