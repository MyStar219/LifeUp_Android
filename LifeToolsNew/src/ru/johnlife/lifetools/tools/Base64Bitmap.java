package ru.johnlife.lifetools.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.LruCache;

import java.io.ByteArrayOutputStream;

import ru.johnlife.lifetools.util.BitmapUtil;

/**
 * Created by yanyu on 4/22/2016.
 */
public class Base64Bitmap {

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @Nullable
    public static Bitmap decodeBase64(String input) {
        try {
            if (input == null ||
                input.trim().length() == 0) {
                return null;
            }
            return BitmapUtil.getScaledBase64Bitmap(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
