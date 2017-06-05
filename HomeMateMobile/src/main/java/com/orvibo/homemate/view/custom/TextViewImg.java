package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * @author smagret
 * 
 *         带左图标的TextView
 */
public class TextViewImg extends TextView {
	private Drawable rightDrawable;
	private Drawable leftDrawable;
	private Context mContext;

	public TextViewImg(Context context) {
		super(context);
		mContext = context;
	}

	public TextViewImg(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs);
	}

	public TextViewImg(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs);
	}

	private void init(AttributeSet attrs) {

		TypedArray a = mContext.obtainStyledAttributes(attrs,
				R.styleable.TextViewImg);
		this.leftDrawable = a.getDrawable(R.styleable.TextViewImg_left_image);
		this.rightDrawable = a.getDrawable(R.styleable.TextViewImg_right_image);
		a.recycle();

		leftDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_error_normal);
		setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null,
				rightDrawable, null);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public Drawable getLeftDrawable() {
		return leftDrawable;
	}

	public void setLeftDrawable(Drawable leftDrawable) {
		this.leftDrawable = leftDrawable;
	}

	public Drawable getRightDrawable() {
		return rightDrawable;
	}

	public void setRightDrawable(Drawable rightDrawable) {
		this.rightDrawable = rightDrawable;
	}
}
