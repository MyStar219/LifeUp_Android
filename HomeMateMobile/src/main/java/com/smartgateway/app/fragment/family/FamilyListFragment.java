package com.smartgateway.app.fragment.family;

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
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.FamilyData;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.Family.FamilyList;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.fragment.drawer.PersonalFragment;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.FamilyApiService;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.io.EOFException;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/17/2016.
 */
public class FamilyListFragment extends BaseListFragment<FamilyData> {

	private String token;

	@Override
	protected BaseAdapter<FamilyData> instantiateAdapter(Context context) {
		return new BaseAdapter<FamilyData>(R.layout.item_family) {
			@Override
			protected ViewHolder<FamilyData> createViewHolder(final View view) {
				return new ViewHolder<FamilyData>(view) {
					private TextView name = (TextView) view.findViewById(R.id.name);
					private TextView relation = (TextView) view.findViewById(R.id.relation);
					private TextView state = (TextView) view.findViewById(R.id.state);

					@Override
					protected void hold(final FamilyData item) {
						name.setText(item.getName());
						relation.setText(item.getRelation());
						state.setVisibility(item.isPending() ? View.VISIBLE : View.GONE);

						{
							view.setOnLongClickListener(new View.OnLongClickListener() {
								@Override
								public boolean onLongClick(View v) {
									DialogUtil.showErrorAlert(getContext(), getString(R.string.remove_family_member_confirm), getString(android.R.string.ok), getString(android.R.string.cancel),
											new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													removeFamily(item);
												}
											}, new View.OnClickListener() {
												@Override
												public void onClick(View v) {

												}
											});
									return true;
								}
							});
						}
					}
				};
			}
		};
	}

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.my_family);
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
		getFamilyList();
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
			InviteFragment fragment = new InviteFragment();
			activity.changeFragment(fragment, true);
		}
		return super.onOptionsItemSelected(item);
	}

	private void getFamilyList() {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		token = preferences.getString(Constants.USER_TOKEN, "");
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		RetrofitManager.getFamilyApiService()
				.familyList(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<FamilyList>() {
					@Override
					public void call(FamilyList familyList) {
						dialogUtil.dismissProgressDialog();
						getAdapter().clear();

						for (FamilyList.FamilyBean family : familyList.getFamily()) {
							getAdapter().add(new FamilyData(family.getUser_id(),
																   family.getName(), family.getRelationship(), false));
						}
						for (FamilyList.PendingFamilyBean pending : familyList.getPending_Family()) {
							getAdapter().add(new FamilyData(pending.getUser_id(),
																   pending.getName(), pending.getRelationship(), true));
						}
					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						dialogUtil.dismissProgressDialog();
					}
				});
	}

	private void removeFamily(final FamilyData item) {
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		FamilyApiService familyApiService = RetrofitManager.getFamilyApiService();
		Observable<Detail> observable = item.isPending() ? familyApiService.removePending(token, item.getId()) :
				                                           familyApiService.remove(token, item.getId());

		observable.subscribeOn(Schedulers.io())
				  .observeOn(AndroidSchedulers.mainThread())
				  .subscribe(new Action1<Detail>() {
						@Override
						public void call(Detail detail) {
							dialogUtil.dismissProgressDialog();
							getFamilyList();
						}
				   }, new ErrorAction() {
						@Override
						public void call(ResponseError error) {
							dialogUtil.dismissProgressDialog();
							if (error.getReason() != null &&
								error.getReason() instanceof EOFException) {
								getFamilyList();
							}

						}
					});
	}
}
