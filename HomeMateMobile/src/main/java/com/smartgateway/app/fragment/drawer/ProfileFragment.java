package com.smartgateway.app.fragment.drawer;

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
import android.support.design.widget.AppBarLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartgateway.app.R;

import ru.johnlife.lifetools.event.DetailEvent;
import ru.johnlife.lifetools.util.BitmapUtil;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.MarshmallowPermissions;
import com.smartgateway.app.Utils.PhoneTextWatcher;
import com.smartgateway.app.data.event.ProfileEvent;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.fragment.PickImageFragment;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.tools.Base64Bitmap;
import ru.johnlife.lifetools.util.RxBus;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/17/2016.
 */
public class ProfileFragment extends PickImageFragment {

	private String fullName, mobileNo, emailAdress, imageString;
	private String token;

	private EditText full, mobile, email;
	private ImageView imgPhoto;

	private Observer<Detail> observer;


	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.nav_item_profile);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container, false);
		initView(view);

		imgPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectImage();
			}
		});

		view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getValue();
				updateProfile();

			}
		});
		return view;
	}

	private void initView(View view) {
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		token = preferences.getString(Constants.USER_TOKEN, "");
		String mobileText = preferences.getString(Constants.USER_MOBILE, "");
		String nameText = preferences.getString(Constants.USER_NAME, "");
		String emailText = preferences.getString(Constants.USER_EMAIL, "");
		imageString = preferences.getString(Constants.USER_PIC_IMAGE, "");

		full = (EditText) view.findViewById(R.id.fullName);
		full.setText(nameText);
		mobile = (EditText) view.findViewById(R.id.mobile);
		mobile.addTextChangedListener(new PhoneTextWatcher(mobile));
		mobile.setText(mobile.getText() + mobileText);
		mobile.setInputType(InputType.TYPE_NULL);
		email = (EditText) view.findViewById(R.id.email);
		email.setText(emailText);

		imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
		UserUtil.loadUserPic(imgPhoto, getContext());
	}

	private void getValue() {
		fullName = full.getText().toString();
		emailAdress = email.getText().toString();
	}

	private void updateProfile() {
		final DialogUtil dialogUtil = new DialogUtil(getContext());
		dialogUtil.showProgressDialog();
		RetrofitManager.getUserApiService()
				.updateProfile(token, fullName, emailAdress, imageString)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<User>() {
					@Override
					public void call(User user) {
						Log.d("Profile", "profile updated");
						dialogUtil.dismissProgressDialog();
						if (getActivity() != null && !getActivity().isFinishing()) {
							UserUtil.saveUser(getContext(), user, false);
							String imageUrl = user.getUser().getImage_url();
							if (imageUrl != null) {
								Picasso.with(getActivity()).invalidate(imageUrl);
								Log.d("Profile", "cache invalidated: " + imageUrl);
							}
							RxBus.getDefaultInstance().post(new ProfileEvent(true));
						}
						if (DialogUtil.isActivityAlive(getActivity())) {
							getActivity().onBackPressed();
						}
					}
				}, new ErrorAction() {
					@Override
					public void call(ResponseError error) {
						Log.d("Profile", "error while updating profile");
						// hotfix for empty response
						dialogUtil.dismissProgressDialog();
						if (error.getReason() != null &&
							error.getReason() instanceof EOFException){
							if (DialogUtil.isActivityAlive(getActivity())) {
								getActivity().onBackPressed();
							}
						}
					}
				})
		;
	}

	@Override
	public void onImagePicked(Bitmap bitmap) {
		if (bitmap != null) {
			imageString = Base64Bitmap.encodeToBase64(bitmap);
			BitmapUtil.safetyLoadImage(imgPhoto, imageString);
		}
	}
}
