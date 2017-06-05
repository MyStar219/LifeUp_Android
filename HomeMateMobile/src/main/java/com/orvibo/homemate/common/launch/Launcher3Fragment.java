package com.orvibo.homemate.common.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.launch.LauncherGuideActivity;

/**
 * Created by Allen on 2015/4/21.
 */
public class Launcher3Fragment extends Fragment implements LauncherGuideActivity.OnFragmentVisibilityListener {
    private ImageView imageView7,imageView6, imageView5, imageView4, imageView3, imageView2, imageView1;
    private boolean isAnimation;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getName(),"onCreateView");
        View view = inflater.inflate(R.layout.fragment_launcher3,null);
        imageView7 = (ImageView) view.findViewById(R.id.imageView7);
        imageView6 = (ImageView) view.findViewById(R.id.imageView6);
        imageView5 = (ImageView) view.findViewById(R.id.imageView5);
        imageView4 = (ImageView) view.findViewById(R.id.imageView4);
        imageView3 = (ImageView) view.findViewById(R.id.imageView3);
        imageView2 = (ImageView) view.findViewById(R.id.imageView2);
        imageView1 = (ImageView) view.findViewById(R.id.imageView1);
        init();
        return view;
    }

    private void init() {
        isAnimation = false;
        imageView7.setVisibility(View.INVISIBLE);
        imageView6.setVisibility(View.INVISIBLE);
        imageView5.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView1.setVisibility(View.INVISIBLE);
    }

    private void startAnimations() {
        isAnimation = true;
        Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_to_top_in);
        animation1.setDuration(500);
        imageView1.setVisibility(View.VISIBLE);
        imageView1.startAnimation(animation1);
        animation1.setAnimationListener(new Animation1Listener());
    }

    @Override
    public void onVisible() {
        startAnimations();
    }

    @Override
    public void onInvisible() {
        init();
    }


    private class Animation1Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                int[] location7 = new int[2];
                imageView7.getLocationOnScreen(location7);
                startTranslateAnimation(imageView6, location7);
                startTranslateAnimation(imageView5, location7);
                startTranslateAnimation(imageView4, location7);
                startTranslateAnimation(imageView3, location7);
                startTranslateAnimation(imageView2, location7);

                Animation animation7 = AnimationUtils.loadAnimation(getActivity(), R.anim.popup_in);
                animation7.setInterpolator(getActivity(), android.R.interpolator.overshoot);
                animation7.setStartOffset(500);
                imageView7.setVisibility(View.VISIBLE);
                imageView7.startAnimation(animation7);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void startTranslateAnimation(ImageView imageView, int[] from) {
        int[] to = new int[2];
        imageView.getLocationOnScreen(to);
        TranslateAnimation animation = new TranslateAnimation(from[0]-to[0], 0, from[1]-to[1], 0);
        animation.setInterpolator(getActivity(), android.R.interpolator.overshoot);
        animation.setDuration(500);
        imageView.setVisibility(View.VISIBLE);
        imageView.startAnimation(animation);
    }

}
