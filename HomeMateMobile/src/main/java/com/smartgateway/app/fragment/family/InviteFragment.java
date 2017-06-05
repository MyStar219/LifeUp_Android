package com.smartgateway.app.fragment.family;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.PhoneTextWatcher;
import com.smartgateway.app.Utils.SmsCodeTextWatcher;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 *
 * Created by yanyu on 5/17/2016.
 */
public class InviteFragment extends BaseAbstractFragment {
	public static final int titles[] = {R.string.send_code, R.string.activate_code};
	public static final int messages[] = {R.string.message_send_code, R.string.message_activate_code};

	private TextView title;
	private TextView message;
	private EditText field;
	private MaterialBetterSpinner spinnerRelationship;
	private Button submit;
	private TextView steps[];

	private Observer<Detail> observer1, observer2;
	private int send_mode = 0;
	private String relationship = "";

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_invite);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_invite, container, false);
		final TextView step1 = (TextView) view.findViewById(R.id.step1);
		final TextView step2 = (TextView) view.findViewById(R.id.step2);
		step1.setOnClickListener(new UISwitcher(0));
		step2.setOnClickListener(new UISwitcher(1));
		steps = new TextView[]{step1, step2};
		title = (TextView) view.findViewById(R.id.title);
		message = (TextView) view.findViewById(R.id.message);
		field = (EditText) view.findViewById(R.id.field);

		spinnerRelationship = (MaterialBetterSpinner) view.findViewById(R.id.spinnerRelationship);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, Constants.FAMILY_ITEMS);
		spinnerRelationship.setAdapter(adapter);
		spinnerRelationship.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				relationship = Constants.FAMILY_ITEMS[position];
			}
		});

		submit = (Button) view.findViewById(R.id.submit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (send_mode == 0 && PhoneTextWatcher.isPhoneNotValid(field)) {
					DialogUtil dialog = new DialogUtil(getActivity());
					dialog.showDissmissDialog(R.string.invalid_mobile, R.string.error);
				}
				else {
					activate(send_mode == 0 ? Integer.parseInt(PhoneTextWatcher.getValue(field)) :
					                          Integer.parseInt(SmsCodeTextWatcher.getValue(field)));
				}
			}
		});
		switchUI(0);
		return view;
	}

	private TextWatcher activeTextWatcher;

	private void switchUI(int mode) {
		title.setText(titles[mode]);
		message.setText(messages[mode]);
		submit.setText(titles[mode]);
		for (TextView step : steps) {
			step.setTextAppearance(title.getContext(), R.style.StepNormal);
		}
		steps[mode].setTextAppearance(title.getContext(), R.style.StepActive);
		if (mode == 0) {
			spinnerRelationship.setVisibility(View.VISIBLE);
			field.removeTextChangedListener(activeTextWatcher);
			activeTextWatcher = new PhoneTextWatcher(field);
			field.addTextChangedListener(activeTextWatcher);
		} else {
			spinnerRelationship.setVisibility(View.GONE);
			field.removeTextChangedListener(activeTextWatcher);
			activeTextWatcher = new SmsCodeTextWatcher(field);
			field.addTextChangedListener(activeTextWatcher);
		}
	}


	private class UISwitcher implements View.OnClickListener {
		int mode;

		public UISwitcher(int mode) {
			this.mode = mode;
		}

		@Override
		public void onClick(View v) {
			send_mode = mode;
			switchUI(send_mode);
		}
	}

	private void activate(int phone_code) {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		if (send_mode == 0) {
			final DialogUtil dialogUtil = new DialogUtil(getContext());
			dialogUtil.showProgressDialog();
			RetrofitManager.getFamilyApiService()
					.register(token, String.valueOf(phone_code), relationship)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Action1<Detail>() {
						@Override
						public void call(Detail detail) {
							dialogUtil.dismissProgressDialog();
							DialogUtil.showErrorAlert(getContext(), detail.getDetail(), null);
						}
					}, new ErrorAction() {
						@Override
						public void call(ResponseError error) {
							dialogUtil.dismissProgressDialog();
						}
					});
		} else if (send_mode == 1) {
			final DialogUtil dialogUtil = new DialogUtil(getContext());
			dialogUtil.showProgressDialog();
			RetrofitManager.getFamilyApiService()
					.activate(token, phone_code)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Action1<Detail>() {
						@Override
						public void call(Detail detail) {
							dialogUtil.dismissProgressDialog();
							DialogUtil.showErrorAlert(getContext(), detail.getDetail(), new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
									if (activity != null) {
										activity.changeFragment(new FamilyListFragment());
									}
								}
							});
						}
					}, new ErrorAction() {
						@Override
						public void call(ResponseError error) {
							dialogUtil.dismissProgressDialog();
						}
					});
		}
	}
}
