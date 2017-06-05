package com.zxing.encoding;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class EncodeHandler {
	private static final String TAG = EncodeHandler.class.getCanonicalName();
	
	private static final int CUT_SIZE = 20;

	// 自由黑点，白色
	public static Bitmap createCode(String content, String charset,
			int qrcode_w, int qrcode_h, Bitmap logo) {
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		// 指定纠错级别(L--7%,M--15%,Q--25%,H--30%),默认级别为L;
		//hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, (charset == null ? "UTF-8" : charset)); 
		
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,
					qrcode_w, qrcode_h, hints);
		} catch (Exception e) {
			Log.e(TAG, "WriterException.");
			e.printStackTrace();
			return null;
		}

		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		int[] pixels = new int[(width - CUT_SIZE) * (height - CUT_SIZE)];
		int halfW = width / 2;
		int halfH = height / 2;
		int logo_halfw = logo != null ? logo.getWidth() / 2 : 0;
		int logo_halfh = logo != null ? logo.getHeight() / 2 : 0;
		for (int y = CUT_SIZE; y < height - CUT_SIZE; y++) {
			int offset = (y - CUT_SIZE) * (width - CUT_SIZE * 2);
			for (int x = CUT_SIZE; x < width - CUT_SIZE; x++) {
				if (logo != null && x > halfW - logo_halfw
						&& x < halfW + logo_halfw && y > halfH - logo_halfh
						&& y < halfH + logo_halfh) {
					pixels[offset + x - CUT_SIZE] = logo.getPixel(x - halfW
							+ logo_halfw, y - halfH + logo_halfh);
					if ((pixels[offset + x - CUT_SIZE] & 0xff000000) == 0) {
						pixels[offset + x - CUT_SIZE] = bitMatrix.get(x, y) ? Color.BLACK
								: Color.WHITE;
					}
				} else {
					pixels[offset + x - CUT_SIZE] = bitMatrix.get(x, y) ? Color.BLACK
								: Color.WHITE;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width - CUT_SIZE * 2, height
				- CUT_SIZE * 2, Bitmap.Config.ARGB_8888);

		bitmap.setPixels(pixels, 0, width - CUT_SIZE * 2, 0, 0, width
				- CUT_SIZE * 2, height - CUT_SIZE * 2);

		Log.i(TAG, "create qrcode success, text:" + content);

		return bitmap;
	}
}
