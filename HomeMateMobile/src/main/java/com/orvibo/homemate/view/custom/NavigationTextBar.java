package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;

public class NavigationTextBar extends LinearLayout {

    private String middleText;
    private String leftText;
    private String rightText;

    private TextView leftTextView;
    private TextView middleTextView;
    private TextView rightTextView;

    private LinearLayout background_layout;
    public static String LEFT = "left";
    public static String MIDDLE = "middle";
    public static String RIGHT = "right";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public NavigationTextBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.title_both_text_bar,
                this, true);
        leftTextView = (TextView) view.findViewById(R.id.cancle_tv);
        middleTextView = (TextView) view.findViewById(R.id.titlebar_tv);
        rightTextView = (TextView) view.findViewById(R.id.confirm_tv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_progress);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        background_layout = (LinearLayout) view.findViewById(R.id.background_layout);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NavigationTextBar);
        this.leftText = a.getString(R.styleable.NavigationTextBar_left);
        this.middleText = a.getString(R.styleable.NavigationTextBar_middle);
        this.rightText = a.getString(R.styleable.NavigationTextBar_right);
        leftTextView.setText(this.leftText);
        leftTextView.setTextColor(getResources().getColor(R.color.white));
        leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                .getDimension(R.dimen.text_normal));
        middleTextView.setText(this.middleText);
        middleTextView.setTextColor(getResources().getColor(
                R.color.white));
        middleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                .getDimension(R.dimen.text_big));
        rightTextView.setText(this.rightText);
        rightTextView.setTextColor(getResources().getColor(R.color.white));
        rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                .getDimension(R.dimen.text_normal));
        a.recycle();
    }

    public NavigationTextBar(Context context) {
        super(context);
    }

    public NavigationTextBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void hide(String location) {
        if (!StringUtil.isEmpty(location) && location.equals(LEFT)) {
//            leftTextView.setText("");
            leftTextView.setVisibility(View.GONE);
        } else if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
//            middleTextView.setText("");
            middleTextView.setVisibility(View.GONE);
        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
//            rightTextView.setText("");
            rightTextView.setVisibility(View.GONE);
        }
    }

    public void show(String location) {
        if (!StringUtil.isEmpty(location) && location.equals(LEFT)) {
            leftTextView.setText(this.leftText);
        } else if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
            middleTextView.setText(this.middleText);
        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
            rightTextView.setText(this.rightText);
        }
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
        leftTextView.setText(this.leftText);
    }

    public String getMiddleText() {
        return middleText;
    }

    public void setMiddleText(String mText) {
        this.middleText = mText;
        middleTextView.setText(this.middleText);
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
        rightTextView.setText(this.rightText);
    }

    //添加一个方法强制重绘背景色
    public void setTitleBackgroundColor(int color) {
        if (background_layout != null) {
            background_layout.setBackgroundColor(color);
        }
    }

    public void setRightTextViewVisiblity(int visiblity) {
        rightTextView.setVisibility(visiblity);
    }

    /**
     * 把动态加载的进度放到 NavigationTextBar
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
