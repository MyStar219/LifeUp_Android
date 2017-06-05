package com.orvibo.homemate.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.orvibo.homemate.common.BaseFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UploadImageUtil {

	private static final String TAG = UploadImageUtil.class.getSimpleName();
	private Activity mAct;
	//图片文件过大，作为中间转换的文件名
	private static final String tempImageFileName = "temp_image.jpg";

	public UploadImageUtil(Activity act) {
		this.mAct = act;
	}
	
	/**
	 * 选择相机
	 * @param fileName 文件名
	 * @param requestCode 请求码
	 */
	
	public void chooseCamera(BaseFragment frg, String fileName, int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(),fileName)));
		frg.startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 获取本地图片
	 * @param type 文件类型
	 * @param requestCode 请求码
	 */
	public void chooseLocalImage(BaseFragment frg, String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, type);
        frg.startActivityForResult(intent, requestCode);
    }
	
    /**
     * 获取本地图片路径
     * @return
     */
	public String getLocalImagePath(Context context, Uri uri) {
		boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;//是否大于等于4.4版本
		String filePath = "";
		if (isKitKat) {
			filePath = getPath(context, uri);
		} else {
			Cursor cursor = null;
			try {
				String[] projection = { MediaStore.Images.Media.DATA };
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				filePath = cursor.getString(column_index);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return filePath;
	}
	
	/** 
	 * <br>功能简述:4.4及以上获取图片的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param uri
	 * @return
	 */

	@TargetApi(19)
	public String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	

	public static String getDataColumn(Context context, Uri uri, String selection,
			String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 *
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 */
	public Bitmap getBitmap(String path) throws FileNotFoundException {
		InputStream is = new FileInputStream(path);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig=Bitmap.Config.RGB_565;//表示16位位图 565代表对应三原色占的位数
		opts.inJustDecodeBounds = true;
		opts.inInputShareable=true;
		opts.inPurgeable=true;//设置图片可以被回收
		BitmapFactory.decodeStream(is, null, opts);
		opts.inSampleSize = 5;
		opts.inJustDecodeBounds = false;
		is = new FileInputStream(path);
		return BitmapFactory.decodeStream(is, null, opts);
	}
}