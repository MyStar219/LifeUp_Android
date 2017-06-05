package ru.johnlife.lifetools.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class LazyLoadListView extends ListView {
    public interface OverScrollListener{
        void onOverScroll(int scrollX, int scrollY,boolean clampedX, boolean clampedY );
    }

    public interface NextRequestedListener{
    	/** ALLWAYS call requestSucceded() once it is */
        void nextRequested();
    }

    
    private OverScrollListener overscrollListener;
    private NextRequestedListener nextListener;
    
	private ProgressBar refreshSpinner;
	private boolean requesting = false;
	private boolean canRequestNextPage = true;
	
    public LazyLoadListView(Context context) {
            super(context);
//            setOverScrollMode(OVER_SCROLL_ALWAYS);
            init(context);
    }

    public LazyLoadListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
    }

    private void init(Context context) {
            setOverScrollMode(OVER_SCROLL_ALWAYS);
    		ViewGroup footer = new FrameLayout(context);
    		refreshSpinner = new ProgressBar(context);
    		refreshSpinner.setIndeterminate(true);
    		footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    		refreshSpinner.setVisibility(View.GONE);
    		addFooterView(footer);
    }
    
    

    @Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter.isEmpty()) {
			refreshSpinner.setVisibility(View.VISIBLE);
		}
	}

	@Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                    int scrollY, int scrollRangeX, int scrollRangeY,
                    int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
            return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0, 100, isTouchEvent);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
            super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
            if (overscrollListener != null) {
            	overscrollListener.onOverScroll(scrollX, scrollY, clampedX, clampedY);
            } else if (
            		nextListener != null &&
            		getAdapter() != null &&
            		clampedY && 
            		(scrollY > 20) && 
            		(!requesting) && 
            		canRequestNextPage
            	) {
					requesting = true;
					refreshSpinner.setVisibility(View.VISIBLE);
					nextListener.nextRequested();
					setSelection(getAdapter().getCount());
			}
    }
    
    public void requestSucceded(boolean canRequestMore) {
    	canRequestNextPage = canRequestMore;
		refreshSpinner.setVisibility(View.GONE);
		requesting = false;
		if (!canRequestNextPage) {
			setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
		}
    }
    
    public void startRequestProgress() {
		requesting = true;
		refreshSpinner.setVisibility(View.VISIBLE);
    }

    public void setOnOverScrollListener(OverScrollListener listener) {
    	this.overscrollListener = listener;
    }

	public void setOnNextRequestedListener(NextRequestedListener nextListener) {
		this.nextListener = nextListener;
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
