package com.orvibo.homemate.common.launch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orvibo.homemate.common.MainActivity;
import com.smartgateway.app.R;

import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.IntentKey;

/**
 * Created by Allen on 2015/4/21.
 */
public class Launcher4Fragment extends Fragment implements LauncherGuideActivity.OnFragmentVisibilityListener , View.OnClickListener{
    private ImageView imageView1;
    private TextView textView1, textView2;
    private Button button1;
    private boolean isAnimation;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getName(),"onCreateView");
        View view = inflater.inflate(R.layout.fragment_launcher4,null);
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        button1 = (Button) view.findViewById(R.id.button1);
        imageView1 = (ImageView) view.findViewById(R.id.imageView1);
        button1.setOnClickListener(this);
        init();
        return view;
    }

    private void init() {
        isAnimation = false;
        textView1.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);
        imageView1.setVisibility(View.INVISIBLE);
    }

    private void startAnimations() {
        isAnimation = true;
        Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        animation1.setDuration(500);
        textView1.setVisibility(View.VISIBLE);
        textView1.startAnimation(animation1);
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

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(IntentKey.BOTTOME_TAB_TYPE, BottomTabType.TWO_BOTTOM_TAB);
        startActivity(intent);
        getActivity().finish();
    }

    private class Animation1Listener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isAnimation) {
                Animation animation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
                animation2.setDuration(500);
                textView2.setVisibility(View.VISIBLE);
                textView2.startAnimation(animation2);
                animation2.setAnimationListener(new Animation2Listener());
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
                animation3.setDuration(300);
                imageView1.setVisibility(View.VISIBLE);
                imageView1.startAnimation(animation3);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
