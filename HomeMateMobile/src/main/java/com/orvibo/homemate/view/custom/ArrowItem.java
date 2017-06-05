package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;


public class ArrowItem extends LinearLayout {

	private String mText;
	private TextView textView;
	private ImageView imageView;

	public String getmText() {
		return mText;
	}

	public void setmText(String mText) {
		this.mText = mText;
	}

	public ArrowItem(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.arrow_item, this, true);
		imageView = (ImageView) findViewById(R.id.icon_iv);
		textView = (TextView) findViewById(R.id.name_tv);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ArrowItem);
		this.mText = a.getString(R.styleable.ArrowItem_hm_arrow_title);
		textView.setText(this.mText);

		imageView.setImageDrawable(a.getDrawable(R.styleable.ArrowItem_hm_arrow_icon));

		a.recycle();
	}

	public ArrowItem(Context context) {
		super(context);
	}

	public ArrowItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
