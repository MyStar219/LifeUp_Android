package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.orvibo.homemate.util.LogUtil;

/**
 * 点击计数自定义控件
 * Created by Smagret on 2016/4/13.
 */
public class ClickCountImageView extends ImageView {

    private static final String     TAG      = ClickCountImageView.class.getSimpleName();
    private              long    exitTime = 0;
    private              int     num      = 0;// 点击次数
    /**
     * 点击次数，触发特定动作
     */
    private              int     maxCount = 0;
    private OnSpecificClickListener mOnSpecificClickListener;


    public ClickCountImageView(Context context) {
        super(context);
    }

    public ClickCountImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickCountImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClickCountImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    public void setClickCount(int count) {
        maxCount = count;
    }

    public void onClick(){
        if ((System.currentTimeMillis() - exitTime) > 300) {
            num = 1;
        } else {
            num++;
            if (num == maxCount) {
                mOnSpecificClickListener.onSpecificClicked();
            }
        }
        LogUtil.d(TAG,"onClick() - 点击次数为：" + num);
        exitTime = System. currentTimeMillis();
    }

    public void setOnSpecificClickListener(OnSpecificClickListener onSpecificClickListener) {
        mOnSpecificClickListener = onSpecificClickListener;
    }

    public interface OnSpecificClickListener {
        void onSpecificClicked();
    }
}
