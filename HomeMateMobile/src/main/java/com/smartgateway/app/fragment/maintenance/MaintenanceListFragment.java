package com.smartgateway.app.fragment.maintenance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import ru.johnlife.lifetools.util.RxBus;
import com.smartgateway.app.data.MaintenanceData;
import com.smartgateway.app.data.event.MaintenancesEvent;
import com.smartgateway.app.data.model.Maintenance.MaintenanceList;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.drawer.PersonalFragment;
import com.smartgateway.app.fragment.state.MaintenanceStateHelper;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 *
 * Created by yanyu on 5/15/2016.
 */
public class MaintenanceListFragment extends BaseListFragment<MaintenanceData> {

	private static MaintenanceStateHelper states;

	@Override
	protected BaseAdapter<MaintenanceData> instantiateAdapter(Context context) {
		if (null == states) {
			states = new MaintenanceStateHelper(context);
		}
		return new BaseAdapter<MaintenanceData>(R.layout.item_maintenance) {
			@Override
			protected ViewHolder<MaintenanceData> createViewHolder(final View view) {
				return new ViewHolder<MaintenanceData>(view) {
					private TextView date = (TextView) view.findViewById(R.id.date);
					private TextView what = (TextView) view.findViewById(R.id.item);
					private TextView where = (TextView) view.findViewById(R.id.where);
					private TextView state = (TextView) view.findViewById(R.id.state);

					{
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								MaintenanceDetailsFragment fragment = new MaintenanceDetailsFragment();
								fragment.setItem(getItem());
								BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
								activity.changeFragment(fragment, true);
							}
						});
					}

					@Override
					protected void hold(MaintenanceData item) {
						String d = item.getDate();
						date.setText(GFunctions.convertDatetoViewType(d));
						where.setText(item.getWhere());
						what.setText("Subject: " + item.getItem());
						StateHelper.StateData stateData = states.get(item.getState());
						state.setBackgroundColor(stateData.getColor());
						state.setText(stateData.getName());
						state.setCompoundDrawablesWithIntrinsicBounds(stateData.getIcon(), 0, 0, 0);
					}
				};
			}
		};
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
	protected int getLayoutId() {
		return R.layout.fragment_list;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.createView(inflater, container, savedInstanceState);
		getMaintenanceList();
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
			fragment.addParam(Constants.FROM_WHERE, Constants.FROM_MAINTENANCE);
			activity.changeFragment(fragment, true);
		}
		return super.onOptionsItemSelected(item);
	}

	private void getMaintenanceList() {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		RetrofitManager.getMaintenanceApiService()
				.list(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<MaintenanceList>() {
					@Override
					public void call(MaintenanceList maintenanceList) {
						dialogUtil.dismissProgressDialog();
						BaseAdapter<MaintenanceData> adapter = getAdapter();
						for (MaintenanceList.MaintenanceListBean bean : maintenanceList.getMaintenance_list()) {
							int id = bean.getId();
							String date = bean.getDate();
							String item = bean.getItem();
							int state = getState(bean.getStatus());
							String where = bean.getCondo() + "\n" + bean.getBlock() + " Unit " + bean.getUnit();
							MaintenanceData data = new MaintenanceData(id, date, item, where, state);
							adapter.add(data);
						}
						adapter.notifyDataSetChanged();

						RxBus.getDefaultInstance().post(new MaintenancesEvent(maintenanceList.getMaintenance_list().size()));
						SharedPreferences preferences = getContext().getSharedPreferences(Constants.PERSONAL_COUNT,
								Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt(Constants.MAINTENANCES, maintenanceList.getMaintenance_list().size());
						editor.commit();

					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						dialogUtil.dismissProgressDialog();
					}
				});
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
