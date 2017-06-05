package com.smartgateway.app.fragment.maintenance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.hedgehog.ratingbar.RatingBar;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SubmitMaintenanceFragment extends BaseAbstractFragment {

	private RatingBar ratingBar;
	private EditText comment;
	private Button btnSubmit;

	private String star;
	private int id;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_maintenance);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_submit, container, false);
		initView(view);

		return view;
	}

	private void initView(View view) {
		ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
		comment = (EditText) view.findViewById(R.id.comment);
		btnSubmit = (Button) view.findViewById(R.id.btnSubmit);

		ratingBar.setOnRatingChangeListener(new RatingBar.OnRatingChangeListener() {
			@Override
			public void onRatingChange(int RatingCount) {
				star = rateString(RatingCount);
			}
		});

		btnSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rate(id, star, comment.getText().toString());
			}
		});
	}

	public void setId(int id) {
		this.id = id;
	}

	private void rate(int id, String rate, String comment) {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		RetrofitManager.getMaintenanceApiService()
				.rate(token, id, rate, comment)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Detail>() {
					@Override
					public void call(final Detail detail) {
						dialogUtil.dismissProgressDialog();
						if (detail != null) {
							DialogUtil.showErrorAlert(getContext(), detail.getDetail(), new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
									if (activity != null) {
										activity.changeFragment(new MaintenanceListFragment());
									}
								}
							});
						}
					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						dialogUtil.dismissProgressDialog();
					}
				});
	}

	private String rateString(int starCount) {
		switch (starCount) {
			case 1:
				return "poor";
			case 2:
				return "fair";
			case 3:
				return "good";
			case 4:
				return "very_good";
			case 5:
				return "excellent";
			default:
				return "good";
		}
	}
}
