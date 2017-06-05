package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * 主界面底部tab的控件
 * Created by smagret on 2015/8/11.
 */
public class MainBottomTwoTab extends RelativeLayout {
    private int colorNormal;
    private int colorChecked;
    private TextView textView;
    private ImageView imageView;
    private LinearLayout linearLayout;

    /**
     * 正常显示图片
     */
    private Drawable mNormalIcon;

    /**
     * 选中图片
     */
    private Drawable mSelectedIcon;

    /**
     * 正常tab背景
     */
    private Drawable mNormalBg;

    /**
     * 选中tab背景
     */
    private Drawable mSelectedBg;

    public MainBottomTwoTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MainBottomTwoTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MainBottomTwoTab(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.main_bottom_coco_tab, this, true);
        imageView = (ImageView) findViewById(R.id.icon);
        textView = (TextView) findViewById(R.id.text);
        linearLayout = (LinearLayout) findViewById(R.id.bg);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.bottomTwoTab);
        String name = a.getString(R.styleable.bottomTwoTab_twoTabName);
        mNormalIcon = a.getDrawable(R.styleable.bottomTwoTab_twoTabNormalIcon);
        mSelectedIcon = a.getDrawable(R.styleable.bottomTwoTab_twoTabSelectedIcon);
        mNormalBg = a.getDrawable(R.styleable.bottomTwoTab_twoTabNormalBg);
        mSelectedBg = a.getDrawable(R.styleable.bottomTwoTab_twoTabSelectedBg);
        textView.setText(name);
        colorNormal = context.getResources().getColor(R.color.font_learned_white);
        colorChecked = context.getResources().getColor(R.color.green);
        a.recycle();
    }

    /**
     * 选中
     */
    public void setSelected() {
        textView.setTextColor(colorChecked);
        imageView.setImageDrawable(mSelectedIcon);
        linearLayout.setBackgroundDrawable(mSelectedBg);
    }

    /**
     * 不选中
     */
    public void setUnselected() {
        textView.setTextColor(colorNormal);
        imageView.setImageDrawable(mNormalIcon);
        linearLayout.setBackgroundDrawable(mNormalBg);
    }
}
