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
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 8/13/2015.
 */
public class ChangePassFragment extends BaseAbstractFragment {

	private String currentPassword, password, token;
	private EditText oldPass, newPass, confirmNewPass;

	private Observer<Detail> detailObserver;

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_change_pass);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_change_pass, container, false);

		init(view);
		view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getValue();
			}
		});
		return view;
	}

	private void init(View view) {
		oldPass = (EditText) view.findViewById(R.id.oldPass);
		newPass = (EditText) view.findViewById(R.id.newPass);
		confirmNewPass = (EditText) view.findViewById(R.id.confirmNewPass);
	}

	private void getValue() {
		currentPassword = oldPass.getText().toString();
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		token = preferences.getString(Constants.USER_TOKEN, "");
		Log.i(Constants.TAG, "current toke is: " + token);
		if (newPass.getText().toString().equals(confirmNewPass.getText().toString())) {
			password = newPass.getText().toString();
			changePassword();
		} else {
			DialogUtil dialog = new DialogUtil(getActivity());
			dialog.showDissmissDialog(R.string.password_not_match, R.string.error);
		}
	}

	private void changePassword() {
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		RetrofitManager.getUserApiService()
				.changePassword(token, currentPassword, password)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Detail>() {
					@Override
					public void call(Detail detail) {
						dialogUtil.dismissProgressDialog();
					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						dialogUtil.dismissProgressDialog();
					}
				});
	}
}
