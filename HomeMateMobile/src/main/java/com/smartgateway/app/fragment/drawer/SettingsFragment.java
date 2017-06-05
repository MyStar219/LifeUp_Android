package com.smartgateway.app.fragment.drawer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.activity.Data;
import com.smartgateway.app.activity.WebActivity;
import com.smartgateway.app.fragment.user.ChangePassFragment;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by Administrator on 8/13/2015.
 */
public class SettingsFragment extends BaseAbstractFragment {

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_settings);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        final CheckBox check = (CheckBox) view.findViewById(R.id.check);
        final TextView notification = (TextView) view.findViewById(R.id.notification);
        final TextView privacy = (TextView) view.findViewById(R.id.privacy);
        final TextView terms = (TextView) view.findViewById(R.id.terms);
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check.performClick();
            }
        });
        view.findViewById(R.id.changePas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseMainActivity activity = (BaseMainActivity) getActivity();
                if (activity != null) {
                    activity.changeFragment(new ChangePassFragment(), true);
                }

            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToWebActivity("Privacy Policy", getUrl(Constants.PRIVACY_URL));
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToWebActivity("Terms of Use", getUrl(Constants.TERMS_URL));
            }
        });
        return view;
    }

    private void goToWebActivity(String titleRes, String url) {
        Log.d("Dashboard", "go to web activity :" + url);
        Intent intent = new Intent(getActivity().getApplicationContext(), WebActivity.class);
        Data data = new Data(titleRes, url);
        intent.putExtra("data", data);
        startActivity(intent);
    }

    protected String getUrl(String key) {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.URLS,
                Context.MODE_PRIVATE);
        return preferences.getString(key, "http://smartgateway.com.sg/");
    }

}
