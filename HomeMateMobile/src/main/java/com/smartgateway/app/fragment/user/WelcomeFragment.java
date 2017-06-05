package com.smartgateway.app.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.activity.LoginActivity;
import com.smartgateway.app.activity.MainActivity;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.BackgroundService;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.tools.Base64Bitmap;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Anna on 7/13/2016.
 */
public class WelcomeFragment extends BaseAbstractFragment {

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.welcome);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        View startButton = view.findViewById(R.id.btnStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity();
            }
        });
        return view;
    }

    private void goToMainActivity() {
        Activity activity = getActivity();
        if (activity != null){
            startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        }
    }
}
