package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;

public class NavigationWhiteBar extends LinearLayout {

    private String mText;
    private String mRightText;
    private int mBgColor;

    private TextView middleTextView;
    private TextView rightTextView;
    private LinearLayout navigation_ll;

    public static String MIDDLE = "middle";
    public static String RIGHT = "right";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public NavigationWhiteBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.title_left_arrow_white_bar,
                this, true);
        //imageView = (ImageView) findViewById(R.id.back_iv);
        middleTextView = (TextView) findViewById(R.id.back_titlebar_tv);
        rightTextView = (TextView) findViewById(R.id.confirm_tv);
        navigation_ll = (LinearLayout) findViewById(R.id.navigation_ll);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_progress);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NavigationGreenBar);
        this.mText = a.getString(R.styleable.NavigationGreenBar_text);
        this.mRightText = a.getString(R.styleable.NavigationGreenBar_right_text);
        mBgColor = a.getColor(R.styleable.NavigationGreenBar_bg_color, getResources().getColor(R.color.white));
        middleTextView.setText(this.mText);
        middleTextView.setTextColor(a.getColor(R.styleable.NavigationGreenBar_middle_text_color, getResources().getColor(R.color.green)));
        rightTextView.setText(this.mRightText);
        navigation_ll.setBackgroundColor(mBgColor);
        a.recycle();
    }

    public NavigationWhiteBar(Context context) {
        super(context);
    }

    public NavigationWhiteBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        middleTextView.setText(this.mText);
    }

    //private ImageView imageView;
    public String getRightText() {
        return mRightText;
    }

    public void setRightText(String mRightText) {
        this.mRightText = mRightText;
        rightTextView.setText(this.mRightText);
    }

    public void hide(String location) {
        if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
            middleTextView.setVisibility(View.GONE);
        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
            rightTextView.setVisibility(View.GONE);
        }
    }

    public void show(String location) {
        if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
            middleTextView.setVisibility(View.VISIBLE);
        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
            rightTextView.setVisibility(View.VISIBLE);
        }
    }

    public void rightTitleClick(View view) {

    }

    public void setRightTextColor(int colorRes) {
        rightTextView.setTextColor(colorRes);
    }

    public void setRightTextViewEnable(boolean enable) {
        rightTextView.setEnabled(enable);
    }
    /**
     * 把动态加载的进度放到 NavigationTextBar
     *
     * @return
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            return mSwipeRefreshLayout;
        }
        return null;
    }
}
