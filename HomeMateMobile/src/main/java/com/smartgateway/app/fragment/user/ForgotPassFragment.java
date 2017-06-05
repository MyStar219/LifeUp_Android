package com.smartgateway.app.fragment.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.PhoneTextWatcher;
import com.smartgateway.app.data.model.User.RegistrationCode;
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
public class ForgotPassFragment extends BaseAbstractFragment {

	private EditText phoneNumber;
	private String phone;
	private int registerCode;
	private String alert;
	private Observer<RegistrationCode> registrationCodeObserver;
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
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_email_pass, container, false);
		dialogUtil = new DialogUtil(getContext());

		phoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
		phoneNumber.addTextChangedListener(new PhoneTextWatcher(phoneNumber));
		initObserver();
		view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				phone = PhoneTextWatcher.getValue(phoneNumber);
				if (PhoneTextWatcher.isPhoneNotValid(phoneNumber)) {
					DialogUtil dialog = new DialogUtil(getActivity());
					dialog.showDissmissDialog(R.string.invalid_mobile, R.string.error);
				} else {
					forgotPassword();
				}
			}
		});
		return view;
	}

	private void initObserver() {
		registrationCodeObserver = new Observer<RegistrationCode>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, "onCompleted");
				DialogUtil.showErrorAlert(getContext(), alert, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						BaseMainActivity activity = (BaseMainActivity) getActivity();
						if (activity != null) {
							ForgotPassCodeFragment fragment = new ForgotPassCodeFragment();
							activity.changeFragment(fragment, true);
						}
					}
				});
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
			}

			@Override
			public void onNext(RegistrationCode registrationCode) {
				dialogUtil.dismissProgressDialog();
				SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_FORGOT,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();

				editor.putString(Constants.FORGOT_USERNAME, phone);
				editor.putInt(Constants.FORGOT_CODE, registrationCode.getRegistration_code());
				editor.commit();
				registerCode = registrationCode.getRegistration_code();
				alert = registrationCode.getDetail();
			}
		};
	}

	private void forgotPassword() {
		dialogUtil.showProgressDialog();
		RetrofitManager.getUserApiService()
				.forgot(phone)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(registrationCodeObserver);
	}
}
