package com.smartgateway.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.MarshmallowPermissions;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.util.BitmapUtil;

public abstract class PickImageFragment extends BaseAbstractFragment {

    private int REQUEST_PICK_GALLERY_CODE = 509;
    private int REQUEST_PICK_CAMERA_CODE = 510;

    protected void selectImage() {
        DialogUtil.showErrorAlertV(getContext(),
                                   getString(R.string.select_image),
                                   getString(R.string.take_photo),
                                   getString(R.string.choose_from_library),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MarshmallowPermissions.checkPermissionForCamera(getActivity())) {
                            cameraIntent();
                        } else {
                            MarshmallowPermissions.requestPermissionForCamera(getActivity());
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MarshmallowPermissions.checkPermissionForStorage(getActivity())) {
                            galleryIntent();
                        } else {
                            MarshmallowPermissions.requestPermissionForStorage(getActivity());
                        }
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
        startActivityForResult(intent, REQUEST_PICK_CAMERA_CODE);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_GALLERY_CODE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_PICK_CAMERA_CODE) {
                onCaptureImageResult(data);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        Bitmap bitmap = BitmapUtil.getScaledBitmap(getActivity().getContentResolver(), selectedImageUri);
        onImagePicked(bitmap);
    }

    public abstract void onImagePicked(Bitmap bitmap);

    private void onCaptureImageResult(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        onImagePicked(bitmap);
    }
}
