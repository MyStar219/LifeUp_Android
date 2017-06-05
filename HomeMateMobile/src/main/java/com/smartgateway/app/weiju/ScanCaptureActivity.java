package com.smartgateway.app.weiju;

import android.graphics.Bitmap;

import com.google.zxing.Result;
import com.zxing.activity.CaptureActivity;

/**
 * Created by lianhongrang on 2016/10/27.
 */
public class ScanCaptureActivity extends CaptureActivity{
    public final static int BIND_BY_QCODE = 1;
    public final static int UNLOCK_BY_QCODE = 2;
    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
    }
}
