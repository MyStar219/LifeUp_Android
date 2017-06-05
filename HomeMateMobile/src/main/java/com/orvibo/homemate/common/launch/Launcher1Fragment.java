package com.orvibo.homemate.common.launch;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.launch.LauncherGuideActivity;

/**
 * Created by Allen on 2015/4/21.
 */
public class Launcher1Fragment extends Fragment implements LauncherGuideActivity.OnFragmentVisibilityListener {
    private ImageView imageView6, imageView5, imageView4, imageView3, imageView2, imageView1;
    private boolean isAnimation;
    private boolean firstPlay = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_launcher1,null);
        imageView6 = (ImageView) view.findViewById(R.id.imageView6);
        imageView5 = (ImageView) view.findViewById(R.id.imageView5);
        imageView4 = (ImageView) view.findViewById(R.id.imageView4);
        imageView3 = (ImageView) view.findViewById(R.id.imageView3);
        imageView2 = (ImageView) view.findViewById(R.id.imageView2);
        imageView1 = (ImageView) view.findViewById(R.id.imageView1);
        init();
        if (firstPlay){
            startAnimations();
            firstPlay = false;
        }
        return view;
    }

    private void init() {
        isAnimation = false;
        imageView6.setVisibility(View.INVISIBLE);
        imageView5.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView1.setVisibility(View.INVISIBLE);
    }

    private void startAnimations() {
        isAnimation = true;
        Activity activity = getActivity();
        Animation animation1 = AnimationUtils.loadAnimation(activity, R.anim.bottom_to_top_in);
        animation1.setDuration(200);
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
                Animation animation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
                animation2.setDuration(200);
                animation2.setAnimationListener(new Animation2Listener());
                imageView2.setVisibility(View.VISIBLE);
                imageView2.startAnimation(animation2);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class Animation2Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                Animation animation3 = AnimationUtils.loadAnimation(getActivity(), R.anim.popup_in);
                animation3.setInterpolator(getActivity(), android.R.interpolator.overshoot);
//            animation3.setDuration(200);
                animation3.setAnimationListener(new Animation3Listener());
                imageView3.setVisibility(View.VISIBLE);
                imageView3.startAnimation(animation3);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class Animation3Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                Animation animation4 = AnimationUtils.loadAnimation(getActivity(), R.anim.popup_in);
                animation4.setInterpolator(getActivity(), android.R.interpolator.overshoot);
                animation4.setAnimationListener(new Animation4Listener());
//            curtainAnim.setDuration(200);
                imageView4.setVisibility(View.VISIBLE);
                imageView4.startAnimation(animation4);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class Animation4Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                Animation animation5 = AnimationUtils.loadAnimation(getActivity(), R.anim.popup_in);
                animation5.setInterpolator(getActivity(), android.R.interpolator.overshoot);
                animation5.setAnimationListener(new Animation5Listener());
//            animation5.setDuration(200);
                imageView5.setVisibility(View.VISIBLE);
                imageView5.startAnimation(animation5);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class Animation5Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                Animation animation6 = AnimationUtils.loadAnimation(getActivity(), R.anim.popup_in);
                animation6.setInterpolator(getActivity(), android.R.interpolator.overshoot);
//            animation6.setDuration(200);
                imageView6.setVisibility(View.VISIBLE);
                imageView6.startAnimation(animation6);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
