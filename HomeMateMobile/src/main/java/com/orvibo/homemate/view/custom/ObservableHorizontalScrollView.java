package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by Smagret on 2015/12/28.
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView {

    private OnScrollViewListener scrollViewListener = null;

    public ObservableHorizontalScrollView(Context context) {
        super(context);
    }

    public ObservableHorizontalScrollView(Context context, AttributeSet attrs,
                                          int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(OnScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }

    public interface OnScrollViewListener {

        void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y, int oldx, int oldy);

    }

}