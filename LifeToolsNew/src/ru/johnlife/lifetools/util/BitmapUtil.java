package ru.johnlife.lifetools.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.johnlife.lifetools.tools.Base64Bitmap;

public class BitmapUtil {
    private final static int IMAGE_MAX_SIZE = 1200000; // 1.2MP

    @Nullable
    public static Bitmap getScaledBitmap(ContentResolver mContentResolver, Uri uri) {
        try {
            InputStream in = mContentResolver.openInputStream(uri);
            // Decode image size
            BitmapFactory.Options o = getOptions(in);
            in = mContentResolver.openInputStream(uri);
            return getOptimizedBitmap(in, o);
        } catch (IOException e) {
            Log.e("getting bitmap", e.getMessage(), e);
            return null;
        }
    }

    public static Bitmap getScaledBase64Bitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, 0);

        try {
            InputStream in = new ByteArrayInputStream(decodedBytes);
            // Decode image size
            BitmapFactory.Options o = getOptions(in);
            in = new ByteArrayInputStream(decodedBytes);
            return getOptimizedBitmap(in, o);
        } catch (IOException e) {
            Log.e("getting bitmap", e.getMessage(), e);
            return null;
        }
    }

    public static void safetyLoadImage(ImageView imgView, String base64Image) {
        if (imgView == null) {
            Log.d("BitmapUtil", "imgView is null, can't show image");
            return;
        }

        if (TextUtils.isEmpty(base64Image) ||
            TextUtils.isEmpty(base64Image.trim()) ||
            base64Image.length() <= 5) {
            Log.d("BitmapUtil", "image base64 not correct, can't show image");
            return;
        }

        Bitmap optimizedDecodedBitmap = Base64Bitmap.decodeBase64(base64Image);
        imgView.setImageBitmap(optimizedDecodedBitmap);
    }

    @NonNull
    private static Bitmap getOptimizedBitmap(InputStream in, BitmapFactory.Options o) throws IOException {
        int scale = 1;
        while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                IMAGE_MAX_SIZE) {
            scale++;
        }
        Log.d("getting bitmap", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

        Bitmap b = null;
        if (scale > 1) {
            scale--;
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            b = BitmapFactory.decodeStream(in, null, o);

            // resize to desired dimensions
            int height = b.getHeight();
            int width = b.getWidth();
            Log.d("getting bitmap", "1th scale operation dimenions - width: " + width + ", height: " + height);

            double y = Math.sqrt(IMAGE_MAX_SIZE
                    / (((double) width) / height));
            double x = (y / height) * width;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                    (int) y, true);
            b.recycle();
            b = scaledBitmap;
        } else {
            b = BitmapFactory.decodeStream(in);
        }
        in.close();

        Log.d("getting bitmap", "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
        return b;
    }

    @NonNull
    private static BitmapFactory.Options getOptions(InputStream in) throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, o);
        in.close();
        return o;
    }
}
