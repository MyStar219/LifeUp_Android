package com.smartgateway.app.fragment.feedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.GFunctions;
import com.smartgateway.app.data.FeedbackData;
import com.smartgateway.app.data.model.FeedBack.FeedbackList;
import com.smartgateway.app.fragment.drawer.PersonalFragment;
import com.smartgateway.app.fragment.maintenance.AddMaintenanceFragment;
import com.smartgateway.app.fragment.state.FeedbackStateHelper;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import retrofit2.adapter.rxjava.HttpException;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * FeedbackListFragment
 * Created by yanyu on 5/15/2016.
 */
public class FeedbackListFragment extends BaseListFragment<FeedbackData> {
	private static FeedbackStateHelper states;
	private Observer<FeedbackList> observer;
	DialogUtil dialogUtil;

	@Override
	protected BaseAdapter<FeedbackData> instantiateAdapter(Context context) {
		if (null == states) {
			states = new FeedbackStateHelper(context);
		}
		return new BaseAdapter<FeedbackData>(R.layout.item_feedback) {
			@Override
			protected ViewHolder<FeedbackData> createViewHolder(final View view) {
				return new ViewHolder<FeedbackData>(view) {
					private TextView date = (TextView) view.findViewById(R.id.date);
					private TextView what = (TextView) view.findViewById(R.id.item);
					private TextView where = (TextView) view.findViewById(R.id.where);
					private TextView state = (TextView) view.findViewById(R.id.state);
					private TextView description = (TextView) view.findViewById(R.id.description);

					{
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								FeedbackDetailsFragment fragment = new FeedbackDetailsFragment();
								fragment.setItem(getItem());
								BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
								activity.changeFragment(fragment, true);
							}
						});
					}

					@Override
					protected void hold(FeedbackData item) {
						String d = item.getDate();
						date.setText(GFunctions.convertDatetoViewType(d));
						where.setText(item.getWhere());
						what.setText("Subject : " + item.getItem());
						StateHelper.StateData stateData = states.get(item.getState());
						state.setBackgroundColor(stateData.getColor());
						state.setText(stateData.getName());
//                        description.setText(item.getDescription());
						state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0, 0, 0);
					}
				};
			}
		};
	}

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_feedback);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list;
	}

	@Override
	protected int getListId() {
		return R.id.list;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.createView(inflater, container, savedInstanceState);
		dialogUtil = new DialogUtil(getContext());

		initObserver();
		getFeedbackList();
		return view;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.menu_add, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.nav_add) {
			BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
			AddMaintenanceFragment fragment = new AddMaintenanceFragment();
			fragment.addParam(Constants.FROM_WHERE, Constants.FROM_FEEDBACK);
			activity.changeFragment(fragment, true);
		}
		return super.onOptionsItemSelected(item);
	}

	private void initObserver() {
		observer = new Observer<FeedbackList>() {
			@Override
			public void onCompleted() {

				Log.i(Constants.TAG, "onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
			}

			@Override
			public void onNext(FeedbackList feedbackList) {
				dialogUtil.dismissProgressDialog();
				BaseAdapter<FeedbackData> adapter = getAdapter();
				for (FeedbackList.FeedbackListBean bean : feedbackList.getFeedback_list()) {
					String date = bean.getDate();
					String item = bean.getItem();
					String where = bean.getCondo() + "\n" + bean.getBuilding() + " " + bean.getUnit();
					int id = bean.getId();
					int state = getState(bean.getStatus());
					FeedbackData data = new FeedbackData(id, date, item, where, state);
					adapter.add(data);
				}
				adapter.notifyDataSetChanged();
			}
		};
	}

	private void getFeedbackList() {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		dialogUtil.showProgressDialog();
		RetrofitManager.getFeedbackApiService()
				.list(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer);
	}

	private int getState(String status) {
		switch (status) {
			case Constants.MaintenanceStatus.SENT:
				return Constants.MaintenanceState.SENT;
			case Constants.MaintenanceStatus.RECEIVED:
				return Constants.MaintenanceState.RECEIVED;
			case Constants.MaintenanceStatus.PENDING:
				return Constants.MaintenanceState.PENDING;
			case Constants.MaintenanceStatus.COMPLETED:
				return Constants.MaintenanceState.COMPLETED;
			case Constants.MaintenanceStatus.RATED:
				return Constants.MaintenanceState.RATED;
			case Constants.MaintenanceStatus.DONE:
				return Constants.MaintenanceState.DONE;
			default:
				return Constants.MaintenanceState.DONE;
		}
	}
}
