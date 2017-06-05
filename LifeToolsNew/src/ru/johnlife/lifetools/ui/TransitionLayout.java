package ru.johnlife.lifetools.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class TransitionLayout extends ScrollView {
	
	
    public TransitionLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFillViewport(true);
	}

	public TransitionLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFillViewport(true);
	}

	public TransitionLayout(Context context) {
		super(context);
		setFillViewport(true);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public float getXFraction() {
    	if (getWidth() == 0) return 0;
        return getTranslationX() / getWidth();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setXFraction(float xFraction) {
    	int width = getWidth();
        setTranslationX((width > 0) ? (xFraction * width) : -9999);
    }
	

}
