package com.orvibo.homemate.view.custom.pulltorefresh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.smartgateway.app.R;

/**
 * Created by yuwei on 2016/4/7.
 */
public class DefaultImageTools {
    /**
     * TAG
     */
    private static final String TAG = DefaultImageTools.class.getSimpleName();

    private static Bitmap mNoticeErrorBitmap;
    private static Bitmap mNoticeEmptyBitmap;

    public static Bitmap getNoticeErrorBitmap(Context context) {
        if (mNoticeErrorBitmap == null) {
            mNoticeErrorBitmap = getBitmapFromRes(context, R.drawable.pic_stand);
        }
        return mNoticeErrorBitmap;
    }

    public static Bitmap getNoticeEmptyBitmap(Context context) {
        if (mNoticeEmptyBitmap == null) {
            mNoticeEmptyBitmap = getBitmapFromRes(context, R.drawable.bg_no_device);
        }
        return mNoticeEmptyBitmap;
    }

    public static Bitmap getBitmapFromRes(Context context, int resId) {
        Bitmap bm = null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            bm = BitmapFactory.decodeResource(context.getResources(), resId, opt);
        } catch (OutOfMemoryError e) {
            //LogUtils.e(TAG, "", e);
            e.printStackTrace();
        }
        return bm;
    }
}
