package com.smartgateway.app.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.HardcodedTextWatcher;
import com.smartgateway.app.Utils.SmsCodeTextWatcher;
import com.smartgateway.app.activity.LoginActivity;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.BackgroundService;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import retrofit2.adapter.rxjava.HttpException;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/13/2016.
 */
public class VerifyFragment extends BaseAbstractFragment {
    private TextView phone;
    private EditText verifyCodeInput;
    private String verifyCode;
    private String phoneNo;
    private String registerCode;

    private Observer<User> observer;
    DialogUtil dialogUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phoneNo = getArguments().getString(Constants.USER_MOBILE);
        registerCode = getArguments().getString(Constants.USER_VERIFY);
    }

    public static VerifyFragment newInstance(String phoneNo, String registerCode) {
        VerifyFragment fragment = new VerifyFragment();
        fragment.addParam(Constants.USER_MOBILE, phoneNo);
        fragment.addParam(Constants.USER_VERIFY, registerCode);
        return fragment;
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_verification);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify, container, false);
        dialogUtil = new DialogUtil(getContext());

        initView(view);
        initObserver();

        phone.setText("(+65) " + phoneNo);
        view.findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifyCode = SmsCodeTextWatcher.getValue(verifyCodeInput);
                if (verifyCode.equals("")){
                    DialogUtil dialog = new DialogUtil(getActivity());
                    dialog.showDissmissDialog(R.string.code_empty, R.string.error);
                } else {
                    verify();
                }

//                BackgroundService.getInstance().storeSession(phoneNo);

            }
        });
        return view;
    }

    private void initView(View view){
        phone = (TextView) view.findViewById(R.id.phone);
        verifyCodeInput = (EditText) view.findViewById(R.id.code_verify);
        verifyCodeInput.addTextChangedListener(new SmsCodeTextWatcher(verifyCodeInput));
    }

    private void initObserver(){
        observer = new Observer<User>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                dialogUtil.dismissProgressDialog();
                if (e == null) return;
                Log.i(Constants.TAG, e.getMessage());
            }

            @Override
            public void onNext(User user) {
                dialogUtil.dismissProgressDialog();
                UserUtil.saveUser(getContext(), user);
                BackgroundService.getInstance().storeSession(phone.getText().toString());

                LifeUpProvidersHelper helper = new LifeUpProvidersHelper();
                helper.signOut(getContext());
                helper.login(getContext());

                goToWelcome();
            }
        };
    }

    private void goToWelcome() {
        if (getActivity() != null) {
            Activity activity = getActivity();
            if (activity != null) {
                ((LoginActivity) getActivity()).changeFragment(new WelcomeFragment(), true);
            }
        }
    }

    private void verify(){
        dialogUtil.showProgressDialog();
        RetrofitManager.getUserApiService()
                .verifyRegister(registerCode, verifyCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
