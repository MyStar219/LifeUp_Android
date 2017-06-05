package com.smartgateway.app.fragment.user;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.TextView;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.activity.LoginActivity;
import com.smartgateway.app.activity.MainActivity;
import com.smartgateway.app.data.model.LaunchInfo;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.BackgroundService;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * LoginFragment
 * Created by yanyu on 5/13/2016.
 */
public class LoginFragment extends BaseAbstractFragment {

    private EditText pass;
    private EditText phone;
    private DialogUtil dialogUtil;

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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initView(view);

        final TextView forgotPassword = (TextView) view.findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseMainActivity activity = (BaseMainActivity) getActivity();
                if (activity != null) {
                    activity.changeFragment(new ForgotPassFragment(), true);
                }
            }
        });
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeLaunch();
            }
        });
        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    ((LoginActivity) activity).changeFragment(new SignupFragment(), true);
                }
            }
        });
        return view;
    }

    private void executeLaunch() {
        dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();

        Log.i(Constants.TAG, "executing launch call");
        RetrofitManager.getUserApiService()
                .config("", 59.913703, 10.750999) // todo fix default coordiantes or send -1,-1
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LaunchInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.i(Constants.TAG, "onCompleted");
                        if (isDetached()) {
                            return;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogUtil.dismissProgressDialog();
                        if (e == null) return;
                        Log.i(Constants.TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(LaunchInfo launchInfo) {
                        if (isDetached()) {
                            return;
                        }

                        UserUtil.saveLaunchInfo(getContext(), launchInfo);
                        executeLogin();
                    }
                });
    }

    private void initView(View view) {
        pass = (EditText) view.findViewById(R.id.pass);
        phone = (EditText) view.findViewById(R.id.phone);
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String prefUser = preferences.getString(Constants.USER_MOBILE, "");
        phone.setText(prefUser);
    }

    private void login(String username, String password) {
        RetrofitManager.getUserApiService()
                .login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (isDetached()) {
                            return;
                        }
                        if (user.getNotification() == null) {
                            Log.d("Nala", "Notification: Null");
                        } else {
                            Log.d("Nala", "Notification:" + user.getNotification().getFamily());
                        }
                        dialogUtil.dismissProgressDialog();
                        UserUtil.saveUser(getContext(), user);
                        Log.i(Constants.TAG, "toke: " + user.getUser().getToken());
                        BackgroundService.getInstance().storeSession(phone.getText().toString());
                        Activity activity = getActivity();
                        new HomeMateHelper().login(getContext());
                        if (activity != null) {
                            startActivity(new Intent(activity, MainActivity.class));
                            activity.finish();
                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        if (error.getDetail() != null) {
                            Log.e("LoginFragment", "error " + error.getDetail() + " code:" + error
                                    .getResponseCode());
                        }
                        if (error.getReason() != null) {
                            Log.e("LoginFragment", "exception " + error.getReason());
                            error.getReason().printStackTrace();
                        }
                        dialogUtil.dismissProgressDialog();
                    }
                });
    }

    private void executeLogin() {
        String username = phone.getText().toString();
        String password = pass.getText().toString();
        Log.i(Constants.TAG, username + " " + password);
        login(username, password);
    }
}
