package com.orvibo.homemate.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

/**
 * @author Smagret
 */
public class AnimationHelper {

    private static final int HALF_A_SECOND = 300;
    private static final float VISIBLE = 1.0f;
    private static final float INVISIBLE = 0.0f;

    /**
     * @return A fade out animation from 100% - 0% taking half a second
     */
    public static Animation createFadeoutAnimation() {
        Animation fadeout = new AlphaAnimation(VISIBLE, INVISIBLE);
        fadeout.setDuration(HALF_A_SECOND);
        return fadeout;
    }

    /**
     * @return A fade in animation from 0% - 100% taking half a second
     */
    public static Animation createFadeInAnimation() {
        Animation animation = new AlphaAnimation(INVISIBLE, VISIBLE);
        animation.setDuration(HALF_A_SECOND);
        return animation;
    }

    /**
     * @return
     */
    public static Animation createTranslateInAnimation(ListView listView) {
        Animation animation = new TranslateAnimation(listView.getLeft(),
                listView.getLeft(), listView.getTop(), listView.getBottom());
        animation.setDuration(HALF_A_SECOND);
        return animation;
    }

    /**
     * @return
     */
    public static Animation createShowRightTranslateInAnimation(final View view) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f);
        animation.setDuration(500);
        animation.setFillAfter(true);
        return animation;
    }

    /**
     * @return
     */
    public static Animation createHideRightTranslateInAnimation(final View view) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f);
        animation.setDuration(500);
        animation.setFillAfter(true);
        return animation;
    }
}
