package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.smartgateway.app.R;

public class AutoFitScreenLayout extends FrameLayout {
	
	public final int RELATIVEWIDTH = 0;
	public final int RELATIVEHEIGHT = 1;
	
	public float mulr=0.0f;
	public int mRelative = RELATIVEWIDTH;

	public AutoFitScreenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 得到自定义属性的集合
		TypedArray attributes = context.obtainStyledAttributes(attrs,
				R.styleable.AutoFitScreenLayout);
		mulr = attributes.getFloat(R.styleable.AutoFitScreenLayout_mulriple, 0);
		mRelative = attributes.getInt(R.styleable.AutoFitScreenLayout_relative,
				mRelative);
		attributes.recycle();
	}

	public AutoFitScreenLayout(Context context) {
		this(context, null);
		//Canvas canvas=new Canvas();
		//canvas.d
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		int parentHeightMode = MeasureSpec.getMode(heightMeasureSpec);
		// *******************width="match_parent"*************************
		if (parentWidthMode == MeasureSpec.EXACTLY && mRelative == RELATIVEWIDTH) {
			// 得到父容器的高宽
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = (int) (parentWidth / mulr+0.5f);
			// 设置父容器的高宽
			setMeasuredDimension(parentWidth, parentHeight);
			// 得到子容器的高宽
			int childenWidth = (int) (parentWidth - (getPaddingLeft() + getPaddingRight()));
			int childenHeight = parentHeight
					- (getPaddingTop() + getPaddingBottom());
			int childenWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
					childenWidth, MeasureSpec.EXACTLY);
			int childenHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					childenHeight, MeasureSpec.EXACTLY);
			// 设置子容器的高宽
			measureChildren(childenWidthMeasureSpec, childenHeightMeasureSpec);
			// *******************height="match_parent"*************************
		} else if (parentHeightMode == MeasureSpec.EXACTLY
				&& mRelative == RELATIVEHEIGHT) {
			// 得到父容器得高宽
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			int parentWidth = (int) (parentHeight * mulr+0.5f);
			// 设置父容器的高宽
			setMeasuredDimension(parentWidth, parentHeight);
			// 得到子容器的高宽
			int childenWidth = parentWidth
					- (getPaddingLeft() + getPaddingRight());
			int childenHeight = parentHeight
					- (getPaddingBottom() + getPaddingTop());
			int childenWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
					childenWidth, MeasureSpec.EXACTLY);
			int childenHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					childenHeight, MeasureSpec.EXACTLY);
			measureChildren(childenWidthMeasureSpec, childenHeightMeasureSpec);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
