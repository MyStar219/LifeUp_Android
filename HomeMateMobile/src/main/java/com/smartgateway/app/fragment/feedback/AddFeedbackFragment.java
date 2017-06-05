package com.smartgateway.app.fragment.feedback;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.MarshmallowPermissions;
import com.squareup.picasso.Picasso;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.tools.Base64Bitmap;

public class AddFeedbackFragment extends BaseAbstractFragment {
	private static final int PICK_IMAGE = 0x13;
	private static final int CAPTURE_IMAGE = 0x14;

	private ImageView image;
	TextView apartment;

	private String bitmapString;

	private final View.OnClickListener addPhotoClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			selectImage();
		}
	};
	private View addPhoto;

	@Override
	protected String getTitle(Resources res) {
		return res.getString(R.string.fragment_add_feedback);
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_maintenance, container, false);
		image = (ImageView) view.findViewById(R.id.image);
		addPhoto = view.findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(addPhotoClickListener);
		apartment = (TextView) view.findViewById(R.id.txtApartment);
		final TextView item = (TextView) view.findViewById(R.id.item);
		final TextView description = (TextView) view.findViewById(R.id.description);
		final MaterialBetterSpinner type = (MaterialBetterSpinner) view.findViewById(R.id.type);

		SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String where = preferences.getString(Constants.USER_HOME, "");
		apartment.setText(where);
//        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BackgroundService.getInstance().getFeedbackList().add(
//                        new FeedbackData(
//                                System.currentTimeMillis(),
//                                item.getText().toString(),
//                                apartment.getText().toString(),
//                                description.getText().toString()
////                                String.valueOf(type.item() == null? "" : type.getSelectedItem())
//                        )
//                );
//                BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
//                FeedbackListFragment fragment = new FeedbackListFragment();
//                activity.changeFragment(fragment, false);
//            }
//        });
		return view;
	}

	@Override
	protected boolean isUpAsHomeEnabled() {
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
				image.setVisibility(View.VISIBLE);
				Picasso.with(getBaseActivity()).load(data.getData()).into(image);
				addPhoto.setVisibility(View.GONE);
				bitmapString = Base64Bitmap.encodeToBase64(getBitmapFromUri(data.getData()));
			} else if (requestCode == CAPTURE_IMAGE && data != null) {
				image.setVisibility(View.VISIBLE);
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
				bitmapString = Base64Bitmap.encodeToBase64(thumbnail);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
				File destination = new File(Environment.getExternalStorageDirectory(),
												   System.currentTimeMillis() + ".jpg");
				FileOutputStream fo;
				try {
					destination.createNewFile();
					fo = new FileOutputStream(destination);
					fo.write(bytes.toByteArray());
					fo.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Picasso.with(getBaseActivity()).load(destination).into(image);
				addPhoto.setVisibility(View.GONE);
			}
		}
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

	private void selectImage() {
		DialogUtil.showErrorAlertV(getContext(), "Select your profile image", "Take Photo", "Choose from Library",
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (MarshmallowPermissions.checkPermissionForCamera(getActivity()))
							cameraIntent();
						else MarshmallowPermissions.requestPermissionForCamera(getActivity());
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (MarshmallowPermissions.checkPermissionForStorage(getActivity()))
							galleryIntent();
						else MarshmallowPermissions.requestPermissionForStorage(getActivity());
					}
				});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MarshmallowPermissions.CAMERA_PERMISSION_REQUEST_CODE: {
				if (grantResults.length > 0
							&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					cameraIntent();
				}
				return;
			}
			case MarshmallowPermissions.STORAGE_PERMISSION_REQUEST_CODE: {
				if (grantResults.length > 0
							&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					galleryIntent();
				}
				return;
			}
		}
	}

	private void cameraIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAPTURE_IMAGE);
	}

	private void galleryIntent() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);//
		startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE);
	}
}
