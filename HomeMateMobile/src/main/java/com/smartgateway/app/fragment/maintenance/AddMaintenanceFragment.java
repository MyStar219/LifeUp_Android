package com.smartgateway.app.fragment.maintenance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.MarshmallowPermissions;
import com.smartgateway.app.data.model.Apartment.Condo;
import com.smartgateway.app.data.model.Apartment.Condos;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.FeedBack.FeedbackCategory;
import com.smartgateway.app.data.model.Maintenance.Category;
import com.smartgateway.app.fragment.PickImageFragment;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.squareup.picasso.Picasso;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.tools.Base64Bitmap;
import ru.johnlife.lifetools.util.BitmapUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddMaintenanceFragment extends PickImageFragment {

	// For submit
	private String bitmapString;
	private String itemName;
	private String categoryId = "1";
	private String desc;
	private String token;
	private String condoName;
	private String fromWhere;

    /*
	Data observer
     */

	//common
	private Observer<Condos> condosObserver;
	private Observer<Condo> condoObserver;
	private Observer<Detail> observer;
	//maintenance
	private Observer<Category> maintenanceCategoryObserver;
	//feedback
	private Observer<FeedbackCategory> feedbackCategoryObserver;

	DialogUtil dialogUtil;


	//Views
	private TextView item;
	private TextView apartment;
	private TextView description;
	private MaterialBetterSpinner type;
	private MaterialBetterSpinner building;
	private ImageView image;
	private View addPhoto;

	//Adapter data
	private ArrayAdapter<String> categoryAdapter;
	private ArrayAdapter<String> nameAdapter;
	private ArrayAdapter<String> buildingAdapter;
	private List<String> categoryList;
	private List<Integer> categoryIdList;
	private List<Condos.CondosBean> condosBeanList;
	private HashMap<String, Integer> condoMap;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fromWhere = getArguments().getString(Constants.FROM_WHERE);
	}

	@Override
	protected String getTitle(Resources res) {
		switch (fromWhere) {
			case Constants.FROM_MAINTENANCE:
				return res.getString(R.string.fragment_add_maintenance);
			case Constants.FROM_FEEDBACK:
				return res.getString(R.string.fragment_add_feedback);
			default:
				return res.getString(R.string.fragment_add_maintenance);
		}
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_maintenance, container, false);
		dialogUtil = new DialogUtil(getContext());

		init(view);
		initCondoObserver();
		goAsMaintenanceOrFeedback();

		view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				itemName = item.getText().toString();
				desc = description.getText().toString();
				register();
			}
		});
		return view;
	}

	private void goAsMaintenanceOrFeedback() {
		switch (fromWhere) {
			case Constants.FROM_MAINTENANCE:
				initMaintenanceObserver();
				getMaintenanceCategory();
				return;
			case Constants.FROM_FEEDBACK:
				initFeedbackObserver();
				getFeedbackCategory();
				return;
		}
	}

	private void register() {
		switch (fromWhere) {
			case Constants.FROM_MAINTENANCE:
				maintenanceRegister();
				return;
			case Constants.FROM_FEEDBACK:
				feedbackRegister();
				return;
		}
	}

	private void goBack() {
		BaseMainActivity activity = (BaseMainActivity) getBaseActivity();

		switch (fromWhere) {
			case Constants.FROM_MAINTENANCE:
				if (activity != null) {
					Log.d("Add", "go back to maintainance list");
					activity.changeFragment(new MaintenanceListFragment(), false);
				}
				return;
			case Constants.FROM_FEEDBACK:
				if (activity != null) {
					Log.d("Add", "go back to feedback list");
					activity.changeFragment(new FeedbackListFragment());
				}
				return;
		}
	}

	@Override
	protected boolean isUpAsHomeEnabled() {
		return true;
	}

	@Override
	public void onImagePicked(Bitmap bitmap) {
		if (bitmap != null) {
			image.setVisibility(View.VISIBLE);
			bitmapString = Base64Bitmap.encodeToBase64(bitmap);
			BitmapUtil.safetyLoadImage(image, bitmapString);
			addPhoto.setVisibility(View.GONE);
		}
	}


	/*
	Feedback request
	 */
	private void initFeedbackObserver() {
		feedbackCategoryObserver = new Observer<FeedbackCategory>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
				categoryAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(FeedbackCategory feedbackCategory) {
				dialogUtil.dismissProgressDialog();
				categoryAdapter.clear();
				for (int i = 0; i < feedbackCategory.getFeedback_category().size(); i++) {
					categoryIdList.add(feedbackCategory.getFeedback_category().get(i).getId());
					categoryAdapter.add(feedbackCategory.getFeedback_category().get(i).getName());
				}
			}
		};
	}

	private void getFeedbackCategory() {
		dialogUtil.showProgressDialog();
		RetrofitManager.getFeedbackApiService()
				.category(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(feedbackCategoryObserver);
	}

	private void feedbackRegister() {
		dialogUtil.showProgressDialog();
		RetrofitManager.getFeedbackApiService()
				.register(token, itemName, categoryId, desc, bitmapString)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer);
	}

	/*
	 Maintenance request
	 */
	private void initMaintenanceObserver() {

		maintenanceCategoryObserver = new Observer<Category>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
				categoryAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(Category category) {
				dialogUtil.dismissProgressDialog();
				categoryAdapter.clear();
				for (int i = 0; i < category.getMaintenance_category().size(); i++) {
					categoryIdList.add(category.getMaintenance_category().get(i).getId());
					categoryAdapter.add(category.getMaintenance_category().get(i).getName());
				}
			}
		};
	}

	private void maintenanceRegister() {
		dialogUtil.showProgressDialog();
		RetrofitManager.getMaintenanceApiService()
				.register(token, itemName, categoryId, desc, bitmapString)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer);
	}

	private void getMaintenanceCategory() {
		dialogUtil.showProgressDialog();
		RetrofitManager.getMaintenanceApiService()
				.category(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(maintenanceCategoryObserver);
	}

    /*
	 Common use for adding maintenance & feedback
     */

	private void initCondoObserver() {
		// Condo name & id
		condosObserver = new Observer<Condos>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
				nameAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(Condos condos) {
				dialogUtil.dismissProgressDialog();
				nameAdapter.clear();
				for (Condos.CondosBean condo : condos.getCondos()) {
					String name = condo.getName();
					int id = condo.getCondo_id();
					nameAdapter.add(name);
					condoMap.put(name, id);
				}
			}
		};

		condoObserver = new Observer<Condo>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
				buildingAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(Condo condo) {
				dialogUtil.dismissProgressDialog();
				for (Condo.CondoBean.BuildingsBean bean : condo.getCondo().getBuildings()) {
					buildingAdapter.add(String.valueOf(bean.getBuilding_id()));
				}
			}
		};

		observer = new Observer<Detail>() {
			@Override
			public void onCompleted() {
				Log.i(Constants.TAG, " onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				dialogUtil.dismissProgressDialog();
				if (e == null) return;
				Log.i(Constants.TAG, e.getMessage());
			}

			@Override
			public void onNext(Detail detail) {
				dialogUtil.dismissProgressDialog();
				if (detail != null) {
					DialogUtil.showErrorAlert(getContext(), detail.getDetail(), new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							goBack();
						}
					});
				}
			}
		};
	}

	private void getCondos(String name) {
		dialogUtil.showProgressDialog();
		RetrofitManager.getApartmentApiService()
				.condos(name, token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(condosObserver);
	}

	private void getCondoDetail(int id) {
		dialogUtil.showProgressDialog();
		RetrofitManager.getApartmentApiService()
				.getCondo(id, token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(condoObserver);
	}

	private Bitmap getBitmapFromUri(Uri uri) {
		try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void init(View view) {
		categoryIdList = new ArrayList<>();
		condosBeanList = new ArrayList<>();
		condoMap = new HashMap<>();

		categoryAdapter = new ArrayAdapter<>(getActivity(),
													android.R.layout.simple_dropdown_item_1line);
		nameAdapter = new ArrayAdapter<>(getActivity(),
												android.R.layout.simple_dropdown_item_1line);
		buildingAdapter = new ArrayAdapter<>(getActivity(),
													android.R.layout.simple_dropdown_item_1line);

		item = (TextView) view.findViewById(R.id.item);
		description = (TextView) view.findViewById(R.id.description);
		apartment = (TextView) view.findViewById(R.id.txtApartment);
		type = (MaterialBetterSpinner) view.findViewById(R.id.type);
		building = (MaterialBetterSpinner) view.findViewById(R.id.building);
		image = (ImageView) view.findViewById(R.id.image);
		addPhoto = view.findViewById(R.id.addPhoto);

		type.setAdapter(categoryAdapter);
		addPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});

		type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				categoryId = String.valueOf(categoryIdList.get(position));
			}
		});

		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String where = preferences.getString(Constants.USER_HOME, "");
		apartment.setText(where);
		token = preferences.getString(Constants.USER_TOKEN, "");
	}
}
