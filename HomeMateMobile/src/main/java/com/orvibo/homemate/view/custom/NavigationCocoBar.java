package com.orvibo.homemate.view.custom;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.progress.SwipeRefreshLayout;

/**
 * 所有navigationBar都使用此bar
 */
public class NavigationCocoBar extends LinearLayout implements View.OnClickListener {

    private Context activity;
    private LinearLayout barRelativeLayout;


    /**
     * the click listener of left view, if it is null, will call Activity.finish() when click it.
     */
    private OnLeftClickListener onLeftClickListener;
    private OnRightClickListener onRightClickListener;

    private String leftText;
    private String centerText;
    private String rightText;

    /**
     * the left drawable of left view, default drawable is R.drawable.back on resource.
     */
    private Drawable leftDrawableLeft;
    private Drawable leftDrawableRight;
    private Drawable rightDrawableLeft;
    private Drawable rightDrawableRight;

    private TextView leftTextView;
    private TextView centerTextView;
    private TextView rightTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public NavigationCocoBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        activity = context;

        LayoutInflater.from(context).inflate(R.layout.navigation_bar, this, true);
        barRelativeLayout = (LinearLayout) findViewById(R.id.barRelativeLayout);
        leftTextView = (TextView) findViewById(R.id.leftTextView);
        leftTextView.setOnClickListener(this);
        centerTextView = (TextView) findViewById(R.id.centerTextView);
        rightTextView = (TextView) findViewById(R.id.rightTextView);
        rightTextView.setOnClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_progress);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationCocoBar);
        leftText = typedArray.getString(R.styleable.NavigationCocoBar_leftText);
        centerText = typedArray.getString(R.styleable.NavigationCocoBar_centerText);
        rightText = typedArray.getString(R.styleable.NavigationCocoBar_rightText);
        leftTextView.setText(leftText);
        centerTextView.setText(centerText);
        rightTextView.setText(rightText);

        leftDrawableLeft = typedArray.getDrawable(R.styleable.NavigationCocoBar_leftDrawableLeft);
        if (leftDrawableLeft == null) {
            leftDrawableLeft = getResources().getDrawable(R.drawable.back);
        }
        leftDrawableRight = typedArray.getDrawable(R.styleable.NavigationCocoBar_leftDrawableRight);
        setLeftTextViewCompound();
        rightDrawableLeft = typedArray.getDrawable(R.styleable.NavigationCocoBar_rightDrawableLeft);
        rightDrawableRight = typedArray.getDrawable(R.styleable.NavigationCocoBar_rightDrawableRight);
        setRightTextViewCompound();
        typedArray.recycle();

    }

    private void setLeftTextViewCompound() {
        if (!TextUtils.isEmpty(leftText)) {
            leftTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            leftTextView.setCompoundDrawablesWithIntrinsicBounds(leftDrawableLeft, null, leftDrawableRight, null);
        }
    }

    private void setRightTextViewCompound() {
        if (!TextUtils.isEmpty(rightText)) {
            rightTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            rightTextView.setCompoundDrawablesWithIntrinsicBounds(rightDrawableLeft, null, rightDrawableRight, null);
        }
    }

    public NavigationCocoBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface OnLeftClickListener {
        void onLeftClick(View v);
    }

    public interface OnRightClickListener {
        void onRightClick(View v);
    }

    public void setOnLeftClickListener(OnLeftClickListener listener) {
        onLeftClickListener = listener;
    }

    public void setOnRightClickListener(OnRightClickListener listener) {
        onRightClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftTextView:
                if (onLeftClickListener != null) {
                    onLeftClickListener.onLeftClick(v);
                } else if (activity instanceof Activity) {
                    ((Activity) activity).onBackPressed();
                }
                break;
            case R.id.rightTextView:
                if (onRightClickListener != null) {
                    onRightClickListener.onRightClick(v);
                }
                break;
        }
    }

    public void setBarColor(int color) {
        barRelativeLayout.setBackgroundColor(color);
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String text) {
        leftText = text;
        leftTextView.setText(leftText);
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String text) {
        centerText = text;
        centerTextView.setText(centerText);
    }

    public void setCenterTextColor(int color) {
        centerTextView.setTextColor(color);
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String text) {
        rightText = text;
        rightTextView.setText(rightText);
    }

    public void setRightTextColor(int color) {
        rightTextView.setTextColor(color);
    }

    public Drawable getLeftDrawableLeft() {
        return leftDrawableLeft;
    }

    public void setLeftDrawableLeft(Drawable leftDrawableLeft) {
        this.leftDrawableLeft = leftDrawableLeft;
        setLeftTextViewCompound();
    }

    public Drawable getLeftDrawableRight() {
        return leftDrawableRight;
    }

    public void setLeftDrawableRight(Drawable leftDrawableRight) {
        this.leftDrawableRight = leftDrawableRight;
        setLeftTextViewCompound();
    }

    public Drawable getRightDrawableLeft() {
        return rightDrawableLeft;
    }

    public void setRightDrawableLeft(Drawable rightDrawableLeft) {
        this.rightDrawableLeft = rightDrawableLeft;
        setRightTextViewCompound();
    }

    public Drawable getRightDrawableRight() {
        return rightDrawableRight;
    }

    public void setRightDrawableRight(Drawable rightDrawableRight) {
        this.rightDrawableRight = rightDrawableRight;
        setRightTextViewCompound();
    }

    public void setLeftTextViewVisibility(int visibility) {
        leftTextView.setVisibility(visibility);
    }

    public void setRightTextViewVisibility(int visibility) {
        rightTextView.setVisibility(visibility);
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
