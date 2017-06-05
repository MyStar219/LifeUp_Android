//package com.orvibo.homemate.view;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.util.StringUtil;
//
//public class NavigationBar extends LinearLayout {
//
//	private String mText;
//	private String mRightText;
//
//	private TextView middleTextView;
//    private TextView rightTextView;
//
//    public static String MIDDLE = "middle";
//    public static String RIGHT = "right";
//
//	public NavigationBar(Context context, AttributeSet attrs) {
//		super(context, attrs);
//
//		LayoutInflater.from(context).inflate(R.layout.title_left_arrow_bar,
//				this, true);
//		//imageView = (ImageView) findViewById(R.id.back_iv);
//		middleTextView = (TextView) findViewById(R.id.back_titlebar_tv);
//        rightTextView = (TextView) findViewById(R.id.confirm_tv);
//
//		TypedArray a = context.obtainStyledAttributes(attrs,
//				R.styleable.NavigationBar);
//		this.mText = a.getString(R.styleable.NavigationBar_text);
//        this.mRightText = a.getString(R.styleable.NavigationBar_right_text);
//        middleTextView.setText(this.mText);
//        rightTextView.setText(this.mRightText);
//		a.recycle();
//	}
//
//	public NavigationBar(Context context) {
//		super(context);
//	}
//
//	public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//	}
//
//    public String getText() {
//        return mText;
//    }
//
//    public void setText(String mText) {
//        this.mText = mText;
//        middleTextView.setText(this.mText);
//    }
//
//    //private ImageView imageView;
//    public String getRightText() {
//        return mRightText;
//    }
//
//    public void setRightText(String mRightText) {
//        this.mRightText = mRightText;
//        rightTextView.setText(this.mRightText);
//    }
//
//    public void hide(String location) {
//        if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
//            middleTextView.setVisibility(View.GONE);
//        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
//            rightTextView.setVisibility(View.GONE);
//        }
//    }
//
//    public void showPopup(String location) {
//        if (!StringUtil.isEmpty(location) && location.equals(MIDDLE)) {
//            middleTextView.setVisibility(View.VISIBLE);
//        } else if (!StringUtil.isEmpty(location) && location.equals(RIGHT)) {
//            rightTextView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    public void confirm(View view) {
//
//    }
//
//}
