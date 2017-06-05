package com.smartgateway.app.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by yanyu on 5/13/2016.
 */
public class SplashFragment extends BaseAbstractFragment {

    @Override
    protected String getTitle(Resources res) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}
