package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;

import com.orvibo.homemate.common.appwidget.WidgetDeviceAndSceneAdapter;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenu;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuAdapter;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuCreator;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuListView;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuView;

/**
 * Created by wuliquan on 2016/7/4.
 */
public class SwipeItemMenuListView extends SwipeMenuListView {
    public SwipeItemMenuListView(Context context) {
        super(context);
    }

    public SwipeItemMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SwipeItemMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setBaseAdapter(new SwipeMenuAdapter(getContext(), adapter) {
            @Override
            public void createMenu(int position,SwipeMenu menu) {
                if( !((WidgetDeviceAndSceneAdapter)adapter).isTitle(position)){
                    if (mMenuCreator != null) {
                        mMenuCreator.create(menu);
                    }
                }

            }

            @Override
            public void onItemClick(SwipeMenuView view, SwipeMenu menu,
                                    int index) {
                if (mOnMenuItemClickListener != null) {
                    mOnMenuItemClickListener.onMenuItemClick(
                            view.getPosition(), menu, index);
                }
                if (mTouchView != null) {
                    mTouchView.smoothCloseMenu();
                }
            }
        });
    }

}
