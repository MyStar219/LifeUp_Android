package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;

public class NavigationGreenBar extends LinearLayout {

    private String mText;
    private String mRightText;
    private int mBgColor;
    private Drawable rightDrawable, leftDrawable, leftToRightDrawable;

    private TextView middleTextView;
    private TextView rightTextView;
    private RelativeLayout navigation_green_ll;

    public static String MIDDLE = "middle";
    public static String RIGHT = "right";

    private TextView backTextView;

    private ImageView backView, rightImageView, iv_left_to_right;

    private View line;//白色底线
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public NavigationGreenBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.title_left_arrow_green_bar,
                this, true);
        backTextView = (TextView) findViewById(R.id.cancle_tv);
        backView = (ImageView) findViewById(R.id.back_iv);
        middleTextView = (TextView) findViewById(R.id.back_titlebar_tv);
        rightTextView = (TextView) findViewById(R.id.confirm_tv);
        navigation_green_ll = (RelativeLayout) findViewById(R.id.navigation_green_ll);
        line = findViewById(R.id.line);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_progress);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NavigationGreenBar);
        this.mText = a.getString(R.styleable.NavigationGreenBar_text);
        this.mRightText = a.getString(R.styleable.NavigationGreenBar_right_text);
        mBgColor = a.getColor(R.styleable.NavigationGreenBar_bg_color, getResources().getColor(R.color.green));
        middleTextView.setText(this.mText);
        rightTextView.setText(this.mRightText);
        rightImageView = (ImageView) findViewById(R.id.confirm_image_tv);
        iv_left_to_right = (ImageView) findViewById(R.id.iv_left_to_right);
        rightDrawable = a.getDrawable(R.styleable.NavigationGreenBar_right_img);
        leftDrawable = a.getDrawable(R.styleable.NavigationGreenBar_ng_left_img);
        leftToRightDrawable = a.getDrawable(R.styleable.NavigationGreenBar_left_to_right_img);
        if (rightDrawable != null) {
            rightTextView.setVisibility(View.GONE);
            rightImageView.setImageDrawable(rightDrawable);
            rightImageView.setVisibility(VISIBLE);
        }
        navigation_green_ll.setBackgroundColor(mBgColor);
        if (leftDrawable != null)
            backView.setImageDrawable(leftDrawable);
        if (leftToRightDrawable != null) {
            iv_left_to_right.setVisibility(View.VISIBLE);
            iv_left_to_right.setImageDrawable(leftToRightDrawable);
        } else {
            iv_left_to_right.setVisibility(View.GONE);
        }

        a.recycle();
    }

    public NavigationGreenBar(Context context) {
        super(context);
    }

    public NavigationGreenBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        middleTextView.setText(this.mText);
    }

    public void setMiddleTextColor(int color) {
        middleTextView.setTextColor(color);
    }

    //private ImageView imageView;
    public String getRightText() {
        return mRightText;
    }

    public void setRightText(String mRightText) {
        this.mRightText = mRightText;
        rightTextView.setText(this.mRightText);
    }

    public void setRightImageViewRes(int res) {
        if (rightImageView != null) {
            rightImageView.setImageResource(res);
            rightImageView.setVisibility(VISIBLE);
        }
    }

    public void setLeftToRightViewRes(int res) {
        if (iv_left_to_right != null)
            iv_left_to_right.setImageResource(res);
    }

    public void setLeftToRightViewVisibility(int visibility) {
        if (iv_left_to_right != null)
            iv_left_to_right.setVisibility(visibility);
    }

    public void setRightTextVisibility(int visibility) {
        if (rightTextView != null) {
            rightTextView.setVisibility(visibility);
        }
    }

    public void setRightImageViewVisibility(int visibility) {
        if (rightImageView != null) {
            rightImageView.setVisibility(visibility);
        }
    }

    public void setRightTextColor(int color) {
        rightTextView.setTextColor(color);
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

    public void setBarColor(int color) {
        navigation_green_ll.setBackgroundColor(color);
    }

    public void setBackViewWhite() {
        backView.setImageResource(R.drawable.bg_white_arrow_selector);
    }

    public void setBackViewGreen() {
        backView.setImageResource(R.drawable.bg_green_arrow_selector);
    }

    /**
     * 展示文字取消
     */
    public void showTextBack() {
        backView.setVisibility(GONE);
        backTextView.setVisibility(VISIBLE);
    }
//    /**
//     * 白色风格
//     */
//    public void showWhiteStyle(){
//        navigation_green_ll.setBackgroundColor(getResources().getColor(R.color.white));
//        middleTextView.setTextColor(getResources().getColor(R.color.black));
//        backView.setImageDrawable(getResources().getDrawable(R.drawable.bg_green_arrow_selector));
//    }

    /**
     * 展示图片需求
     */
    public void showImageBack() {
        backView.setVisibility(VISIBLE);
        backTextView.setVisibility(GONE);
    }

    /**
     * 白色背景风格风格，中间的字体黑色，两边字体和图片绿色
     */
    public void showWhiteStyle() {
        line.setVisibility(VISIBLE);
        navigation_green_ll.setBackgroundColor(getResources().getColor(R.color.title_bar_bg));
        middleTextView.setTextColor(getResources().getColor(R.color.black));
        rightTextView.setTextColor(getResources().getColor(R.color.green));
        backTextView.setTextColor(getResources().getColor(R.color.green));
        backView.setImageDrawable(getResources().getDrawable(R.drawable.bg_green_arrow_selector));
        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.more_green_selector));
    }


    public void setRightTextViewEnable(boolean enable){
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
