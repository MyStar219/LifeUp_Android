package ru.johnlife.lifetools.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class CoordinatedAutosizeTextView extends AutosizeTextView {
    private static final LruCache<View, List<CoordinatedAutosizeTextView>> coordinatedLists = new LruCache(30);
    private View coordinator;

    public CoordinatedAutosizeTextView(Context context) {
        super(context);
    }

    public CoordinatedAutosizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoordinatedAutosizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCoordinator(View parent) {
        coordinator = parent;
        getCoordinatedViews(parent).add(this);
        adjustTextSize();
        //TODO: remove from previous list
    }

    @NonNull
    private List<CoordinatedAutosizeTextView> getCoordinatedViews(View parent) {
        List<CoordinatedAutosizeTextView> list = coordinatedLists.get(parent);
        if (null == list) {
            list = new ArrayList<>();
            coordinatedLists.put(parent, list);
        }
        return list;
    }

    @Override
    protected void adjustTextSize() {
        if (null != coordinator) {
            int minSize = Integer.MAX_VALUE;
            List<CoordinatedAutosizeTextView> coordinatedViews = getCoordinatedViews(coordinator);
            for (CoordinatedAutosizeTextView view : coordinatedViews) {
                int size = view.getAutoSize();
                if (size < minSize) {
                    minSize = size;
                }
            }
            for (CoordinatedAutosizeTextView view : coordinatedViews) {
                view.internalSetSize(minSize);
                ViewGroup.LayoutParams layout = view.getLayoutParams();
                layout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(layout);
            }
        }
    }
}
