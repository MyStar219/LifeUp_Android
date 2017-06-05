package ru.johnlife.lifetools.ui;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import ru.johnlife.lifetools.ui.listener.AnimationFinishedListener;

/**
 * Created by yanyu on 4/12/2016.
 */
public abstract class AnimationUtils  {
    public static void hideDown(View view, AnimationFinishedListener listener) {
        int dy = ((View)view.getParent()).getHeight() - view.getTop();
        view.animate().translationY(dy).setInterpolator(new AccelerateInterpolator()).setListener(listener).start();
    }

    public static void hideUp(View view, AnimationFinishedListener listener) {
        int dy = - view.getHeight() - view.getTop();
        view.animate().translationY(dy).setInterpolator(new AccelerateInterpolator()).setListener(listener).start();
    }

    public static void showUp(View view, AnimationFinishedListener listener) {
        view.setVisibility(View.VISIBLE);
        int dy = ((View)view.getParent()).getHeight() - view.getTop();
        view.setTranslationY(dy);
        view.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).setListener(listener).start();
    }
}
