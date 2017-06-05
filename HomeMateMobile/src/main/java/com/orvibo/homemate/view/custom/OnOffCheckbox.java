package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.smartgateway.app.R;

/**
 * @author smagret
 * 
 *         OnOff  Checkbox
 */
public class OnOffCheckbox extends ImageView {
//	private Drawable rightDrawable;
//	private Drawable leftDrawable;
	private Context mContext;

	public OnOffCheckbox(Context context) {
		super(context);
		mContext = context;
	}

	public OnOffCheckbox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs);
	}

	public OnOffCheckbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs);
	}

	private void init(AttributeSet attrs) {

//		TypedArray a = context.obtainStyledAttributes(attrs,
//				R.styleable.TextViewImg);
//		this.leftDrawable = a.getDrawable(R.styleable.TextViewImg_left_image);
//		this.rightDrawable = a.getDrawable(R.styleable.TextViewImg_right_image);
//		a.recycle();

	}

    public void setChecked(boolean isChecked) {
        if (isChecked) {
            setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.device_on));
        } else {
            setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.device_off));
        }
    }

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
