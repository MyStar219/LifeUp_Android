package com.smartgateway.app.fragment.drawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by Terry on 8/7/16.
 */
public class AboutFragment extends BaseAbstractFragment {
    private WebView webView;
    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_about);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        webView = (WebView) view.findViewById(R.id.aboutWebview);
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.URLS,
                Context.MODE_PRIVATE);
        String url = preferences.getString(Constants.ABOUT_URL, "http://smartgateway.com.sg/");
        webView.loadUrl(url);
        return view;
    }
}
