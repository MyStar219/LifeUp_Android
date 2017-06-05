package com.smartgateway.app.fragment.user;

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
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.SmsCodeTextWatcher;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import retrofit2.adapter.rxjava.HttpException;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 8/13/2015.
 */
public class ForgotPassCodeFragment extends BaseAbstractFragment {

    private EditText code, newPass, confirmPass;
    private Observer<Void> userVerifyObserver;
    private String registrationCode, phoneCode, username, password;
    DialogUtil dialogUtil;

    @Override
    protected String getTitle(Resources res) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_change_pass, container, false);
        dialogUtil = new DialogUtil(getContext());

        initView(view);
        initObserver();
        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_FORGOT,
                        Context.MODE_PRIVATE);
                registrationCode = String.valueOf(preferences.getInt(Constants.FORGOT_CODE, 1));
                username = preferences.getString(Constants.FORGOT_USERNAME, "");
                phoneCode = SmsCodeTextWatcher.getValue(code);
                Log.e("registrationCode>>>>",registrationCode );
                Log.e("username>>>>",username );
                Log.e("phoneCode>>>>",phoneCode );

                DialogUtil dialog = new DialogUtil(getActivity());
                if (phoneCode.contentEquals("")) {
                    dialog.showDissmissDialog(R.string.code_empty, R.string.error);
                } else if (!newPass.getText().toString().equals(confirmPass.getText().toString())) {
                    dialog.showDissmissDialog(R.string.password_not_match, R.string.error);
                } else {
                    password = newPass.getText().toString();
                    changePassword();
                }
            }
        });
        return view;
    }

    private void initView(View view){
        code = (EditText) view.findViewById(R.id.code_forgot);
        code.addTextChangedListener(new SmsCodeTextWatcher(code));
        newPass = (EditText) view.findViewById(R.id.pass_forgot);
        confirmPass = (EditText) view.findViewById(R.id.passConfirm_forgot);
    }

    private void initObserver(){
        userVerifyObserver = new Observer<Void>() {
            @Override
            public void onCompleted() {
                Log.i(Constants.TAG, ">>>>>>>>>>>>>onCompleted");
                Log.e(Constants.TAG, ">>>>>>>>>>>>>>onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                dialogUtil.dismissProgressDialog();
            }


            @Override
            public void onNext(Void user) {
                dialogUtil.dismissProgressDialog();
                BaseMainActivity activity = (BaseMainActivity) getActivity();
                if (activity != null) {
                    Log.e("On Next Success", ">>>>>>>>>>On Next Success");
                    activity.changeFragment(new LoginFragment());
                }
            }
        };
    }

    private void changePassword(){
        dialogUtil.showProgressDialog();
        RetrofitManager.getUserApiService()
                .forgotPassword(registrationCode, phoneCode, username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userVerifyObserver);
    }
}
