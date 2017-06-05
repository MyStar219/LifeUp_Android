package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * 主界面底部tab的控件
 * Created by huangqiyao on 2015/4/8.
 */
public class MainBottomFourTab extends RelativeLayout {
    private int colorNormal;
    private int colorChecked;
    private RelativeLayout tab_rl;
    private TextView mMessageCount_tv;
    TextView textView;
    private ImageView imageView;
    private ImageView icon_hint;

    /**
     * 正常显示图片
     */
    private Drawable mNormalIcon;

    /**
     * 选中图片
     */
    private Drawable mSelectedIcon;

    public MainBottomFourTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MainBottomFourTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MainBottomFourTab(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.main_bottom_tab, this, true);

        tab_rl = (RelativeLayout) findViewById(R.id.tab_rl);
        imageView = (ImageView) findViewById(R.id.icon);
        icon_hint = (ImageView) findViewById(R.id.icon_hint);
        textView = (TextView) findViewById(R.id.text);
        mMessageCount_tv = (TextView) findViewById(R.id.messageCount_tv);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.bottomFourTab);
        String name = a.getString(R.styleable.bottomFourTab_tabName);
        mNormalIcon = a.getDrawable(R.styleable.bottomFourTab_tabNormalIcon);
        mSelectedIcon = a.getDrawable(R.styleable.bottomFourTab_tabSelectedIcon);
        textView.setText(name);
        colorNormal = context.getResources().getColor(R.color.font_learned_white);
        colorChecked = context.getResources().getColor(R.color.green);
        a.recycle();

        setUnselected();
    }

    /**
     * 选中
     */
    public void setSelected() {
        textView.setTextColor(colorChecked);
        imageView.setImageDrawable(mSelectedIcon);
        tab_rl.setBackgroundResource(R.drawable.bg_box_checked);
    }

    /**
     * 不选中
     */
    public void setUnselected() {
        textView.setTextColor(colorNormal);
        imageView.setImageDrawable(mNormalIcon);
        tab_rl.setBackgroundResource(R.drawable.main_bottom_tab_selector);
    }

    /**
     * 隐藏右上角的消息数量
     */
    public void hideMessageCount() {
        mMessageCount_tv.setVisibility(View.GONE);
    }

    /**
     * 显示右上角的消息数量
     *
     * @param count
     */
    public void showMessageCount(int count) {
        mMessageCount_tv.setText(count + "");
        mMessageCount_tv.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏右上角的消息提示图标
     */
    public void hideIconHint() {
        icon_hint.setVisibility(View.GONE);
    }

    /**
     * 显示右上角的消息提示图标
     */
    public void showIconHint() {
        icon_hint.setVisibility(View.VISIBLE);
    }

}
