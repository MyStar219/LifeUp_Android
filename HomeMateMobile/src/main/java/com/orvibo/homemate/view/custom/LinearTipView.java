package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * Created by snown on 2016/4/12.
 *
 * @描述: 提示黄条
 */
public class LinearTipView extends LinearLayout {

    private android.widget.TextView tipText;
    private android.widget.RelativeLayout closeIcon;
    private LinearLayout tipView;
    private String text;

    public LinearTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.view_linear_tip, this, true);
        this.tipView = (LinearLayout) view.findViewById(R.id.tipView);
        this.closeIcon = (RelativeLayout) view.findViewById(R.id.closeIcon);
        this.tipText = (TextView) view.findViewById(R.id.tipText);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LinearTipView);
        text = a.getString(R.styleable.LinearTipView_tipText);
        tipText.setText(text);

        closeIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tipView.setVisibility(GONE);
            }
        });
    }

    public void setText(String text) {
        this.text = text;
        if (tipText != null)
            tipText.setText(text);
    }

    /**
     * 是否展示删除按钮
     *
     * @param isShow
     */
    public void isShowDeleteBtn(boolean isShow) {
        if (!isShow)
            this.closeIcon.setVisibility(GONE);
    }

}
