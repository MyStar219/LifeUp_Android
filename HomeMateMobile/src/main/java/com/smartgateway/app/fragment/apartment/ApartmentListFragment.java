package com.smartgateway.app.fragment.apartment;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.ApartmentData;
import com.smartgateway.app.data.model.Apartment.Apartments;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.CredentialsBean;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * ApartmentListFragment
 * Created by yanyu on 5/20/2016.
 */

public class ApartmentListFragment extends BaseListFragment<ApartmentData> {

	private Observer<Apartments> observer;
	private Observer<Detail> detailObserver;
	private List<ApartmentData> list = new ArrayList<>();
	private TextView txtMessage;
	private String token;
	private DialogUtil dialogUtil;
	private BaseAdapter<ApartmentData> adapter = null;
	@Override
	protected BaseAdapter<ApartmentData> instantiateAdapter(Context context) {
		final String unitString = context.getString(R.string.unit);
		adapter = new BaseAdapter<ApartmentData>(R.layout.item_apartment) {
			@Override
			protected ViewHolder<ApartmentData> createViewHolder(final View view) {

				return new ViewHolder<ApartmentData>(view) {
					private TextView name = (TextView) view.findViewById(R.id.name);
					private TextView block = (TextView) view.findViewById(R.id.block);
					private TextView state = (TextView) view.findViewById(R.id.state);
					private ImageView tick = (ImageView) view.findViewById(R.id.tick);
					private StringBuilder b = new StringBuilder();

					{
						view.setOnLongClickListener(new View.OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								DialogUtil.showErrorAlert(getContext(), "Delete " + getItem().getName(), getString(R.string.confirm), getString(R.string.cancel),
										new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												String apartmentId = String.valueOf(getItem().getId());
												removeApartment(apartmentId);
												getAdapter().remove(getItem());
											}
										}, null);
								return true;
							}
						});

						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
//								if (getItem().isPending()) return;
								DialogUtil.showErrorAlert(getContext(), getString(R.string.switch_apartment_confirm), getString(android.R.string.ok), getString(android.R.string.cancel),
										new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
														Context.MODE_PRIVATE);
												SharedPreferences.Editor editor = preferences.edit();
												editor.putString(Constants.USER_HOME, getItem().getName());
												editor.apply();
												switchApartment(getItem().getId());
												notifyDataSetChanged();
											}
										}, null);
							}
						});
					}

					@Override
					protected void hold(final ApartmentData item) {
						name.setText(item.getName());
						name.setTextColor(item.isPending() ?
												  getResources().getColor(R.color.black) : getResources().getColor(R.color.colorPrimaryDark));
						b.setLength(0);
						b.append(item.getBlock()).append(", ")
								.append(unitString).append(' ').append(item.getUnit());
						block.setText(b.toString());
						state.setVisibility(item.isPending() ? View.VISIBLE : View.GONE);
						if (item.isSelected()) {
							tick.setVisibility(View.VISIBLE);
						} else {
							tick.setVisibility(View.INVISIBLE);
						}
					}
				};
			}
		};
		return  adapter;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.createView(inflater, container, savedInstanceState);
		dialogUtil = new DialogUtil(getContext());
		txtMessage = (TextView) view.findViewById(R.id.txtMessage);
		list.clear();
		initObserver();
		getApartments();

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
			activity.changeFragment(new AddApartmentFragment(), true);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list;
	}

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_my_apartment);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	private void initObserver() {
		observer = new Observer<Apartments>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, "onCompleted");
				if (list.size() > 0) {
					txtMessage.setVisibility(View.GONE);
				}
				else {
					txtMessage.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
			}

			@Override
			public void onNext(final Apartments apartments) {
				dialogUtil.dismissProgressDialog();
				if (apartments.getDetail() == null) {
					for (int i = 0; i < apartments.getApartments().size(); i++) {
						ApartmentData data = new ApartmentData(
																	  apartments.getApartments().get(i).getApartment_id(),
																	  apartments.getApartments().get(i).getCondo().getName(),
																	  apartments.getApartments().get(i).getCondo().getBuilding().getBlock(),
																	  apartments.getApartments().get(i).getCondo().getBuilding().getLevel().getUnit().getUnit_no(),
																	  (apartments.getApartments().get(i).getStatus().equals("pending")),
																	  (apartments.getApartments().get(i).isDefaultX())
						);
						list.add(data);
					}
					getAdapter().clear();
					getAdapter().addAll(list);
					getAdapter().notifyDataSetChanged();
				}
			}
		};

		detailObserver = new Observer<Detail>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, "onCompleted");
				getApartments();

			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				getApartments();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(Detail detail) {
				dialogUtil.dismissProgressDialog();
			}
		};
	}

	private void getApartments() {
		list.clear();
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		token = preferences.getString(Constants.USER_TOKEN, "");
		dialogUtil.showProgressDialog();
		RetrofitManager.getApartmentApiService()
				.list(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer);
	}

	private void switchApartment(int apartmentId) {
		dialogUtil.showProgressDialog();
		RetrofitManager.getApartmentApiService()
				.apartmentSwitch(apartmentId, token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<CredentialsBean>() {
					@Override
					public void call(CredentialsBean credentialsBean) {
						Credentials credentials = credentialsBean.getCredentials();

						if (dialogUtil != null) {
							dialogUtil.dismissProgressDialog();
						}

						if (DialogUtil.isActivityAlive(getContext())) {
							UserUtil.saveCredentials(getContext(), credentials);
							LifeUpProvidersHelper helper = new LifeUpProvidersHelper();
							helper.signOut(getContext());
							helper.login(getContext());
							getApartments();
						}
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						if (dialogUtil != null) {
							dialogUtil.dismissProgressDialog();
						}
					}
				});
	}


	private void removeApartment(String apartmentId) {
		dialogUtil.showProgressDialog();
		RetrofitManager.getApartmentApiService()
				.remove(apartmentId, token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(detailObserver);
	}
}
