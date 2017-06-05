package com.smartgateway.app.fragment.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.HardcodedTextWatcher;
import com.smartgateway.app.Utils.PhoneTextWatcher;
import com.smartgateway.app.activity.LoginActivity;
import com.smartgateway.app.data.model.User.RegistrationCode;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * SignupFragment
 * Created by Administrator on 8/13/2015.
 */
public class SignupFragment extends BaseAbstractFragment {

	private EditText phone_register;
	private EditText pass_register;
	private EditText passConfirm_register;
	private EditText name_register;
	private CheckBox checkBox;

	private boolean termsChecked;
	private Observer<RegistrationCode> observer;
	DialogUtil dialogUtil;

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_signup);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_signup, container, false);
		dialogUtil = new DialogUtil(getContext());

		initView(view);
		initDataObserver();

		view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeRegister();
			}
		});
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				termsChecked = true;
			}
		});
		return view;
	}

	private void initView(View view) {
		phone_register = (EditText) view.findViewById(R.id.phone_register);
		phone_register.addTextChangedListener(new PhoneTextWatcher(phone_register));

		pass_register = (EditText) view.findViewById(R.id.pass_register);
		passConfirm_register = (EditText) view.findViewById(R.id.passConfirm_register);
		name_register = (EditText) view.findViewById(R.id.name_register);
		checkBox = (CheckBox) view.findViewById(R.id.checkBox);
		checkBox.setChecked(false);
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.URLS,
				Context.MODE_PRIVATE);
		String url = preferences.getString(Constants.TERMS_URL, "https://api.lifeup.com.sg/v1/termsandconditions/");
		String terms_url = String.format("I agree to the <a href=\"%s\">Terms &amp; Conditions</a>", url);
		checkBox.setText(Html.fromHtml(terms_url));
		checkBox.setMovementMethod(LinkMovementMethod.getInstance());
		termsChecked = false;
	}

	private void initDataObserver() {
		observer = new Observer<RegistrationCode>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
			}

			@Override
			public void onNext(final RegistrationCode code) {
				dialogUtil.dismissProgressDialog();

				final Activity activity = getActivity();
				if (activity != null) {
					DialogUtil.showErrorAlert(getContext(), code.getDetail(), new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							VerifyFragment fragment = VerifyFragment.newInstance(PhoneTextWatcher.getValue(phone_register).trim(),
									                                             String.valueOf(code.getRegistration_code()));
							((LoginActivity) activity).changeFragment(fragment, true);
						}
					});
				}
			}
		};
	}

	private void register(String name, String mobile, String password) {
		dialogUtil.showProgressDialog();
		RetrofitManager.getUserApiService()
				.register(name, mobile, password)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer);
	}

	private void executeRegister() {
		String name = name_register.getText().toString();
		String mobile = PhoneTextWatcher.getValue(phone_register);
		String password = pass_register.getText().toString();
		String confirmPassword = passConfirm_register.getText().toString();

		DialogUtil dialog = new DialogUtil(getActivity());

		if ((password.equals(confirmPassword)
					 && (termsChecked == true)
					 && (!PhoneTextWatcher.isPhoneNotValid(phone_register)))) {
			register(name, mobile, password);
		} else if (!password.equals(confirmPassword)) {
			dialog.showDissmissDialog(R.string.password_not_match, R.string.error);
		} else if (!termsChecked) {
			dialog.showDissmissDialog(R.string.check_terms, R.string.error);
		} else if (PhoneTextWatcher.isPhoneNotValid(phone_register)) {
			dialog.showDissmissDialog(R.string.invalid_mobile, R.string.error);
		}
	}
}
