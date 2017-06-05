/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxing.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.zxing.camera.CameraManager;
import com.zxinglib.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
			128, 64 };
	private static final long ANIMATION_DELAY = 80L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;
	private static final int MIDDLE_LINE_WIDTH = 3;
	private static final int MIDDLE_LINE_PADDING = 5;
	private static final int SPEEN_DISTANCE = 8;
    private static final int TEXT_SIZE = 16;  
    private static final int TEXT_PADDING_TOP = 30;  
    
    private int CORNER_WIDTH = 50;

	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private float density;
	private int ScreenRate;
	private int slideTop;
	private boolean isFirst;
	private final int maskColor;
	private final int resultColor;
	private final int frameColor;
	private final int scanLineColor;
	private final int resultPointColor;
	private int scannerAlpha;
	private List<ResultPoint> possibleResultPoints;
	private List<ResultPoint> lastPossibleResultPoints;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
		density = resources.getDisplayMetrics().density;
		CORNER_WIDTH = (int) (16 * density);
		ScreenRate = (int) (4 * resources.getDisplayMetrics().density);
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		scanLineColor = resources.getColor(R.color.viewfinder_scan_line);;
		resultPointColor = resources.getColor(R.color.possible_result_points);
		scannerAlpha = 0;
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		lastPossibleResultPoints = null;
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
		Rect previewFrame = cameraManager.getFramingRectInPreview();
		if (frame == null || previewFrame == null) {
			return;
		}
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
		}

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			// Draw a red "laser scanner" line through the middle to show
			// decoding is active
			paint.setColor(frameColor);
			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
			// int middle = frame.height() / 2 + frame.top;
			// canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
			// middle + 2, paint);

			float scaleX = frame.width() / (float) previewFrame.width();
			float scaleY = frame.height() / (float) previewFrame.height();

			List<ResultPoint> currentPossible = possibleResultPoints;
			List<ResultPoint> currentLast = lastPossibleResultPoints;
			int frameLeft = frame.left;
			int frameTop = frame.top;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new ArrayList<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(CURRENT_POINT_OPACITY);
				paint.setColor(resultPointColor);
				synchronized (currentPossible) {
					for (ResultPoint point : currentPossible) {
						canvas.drawCircle(frameLeft
								+ (int) (point.getX() * scaleX), frameTop
								+ (int) (point.getY() * scaleY), POINT_SIZE,
								paint);
					}
				}
			}
			// draw QR code frame
			paint.setColor(frameColor);
			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
					+ ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
					+ CORNER_WIDTH, frame.bottom, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.bottom
					- CORNER_WIDTH, frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom
					- ScreenRate, frame.right, frame.bottom, paint);
			
			//draw tips
			paint.setColor(Color.WHITE);    
			int textSize = getResources().getDimensionPixelSize(R.dimen.capture_btn_text_size);
			paint.setTextSize(textSize);    
			paint.setAlpha(0x40);    
			paint.setTypeface(Typeface.DEFAULT_BOLD);   
			String text = getResources().getString(R.string.scan_text);  
			float textWidth = paint.measureText(text);  
			canvas.drawText(text, (width - textWidth)/2
					, (float) (frame.bottom + (float)TEXT_PADDING_TOP *density), paint) ; 
            
			
			slideTop += SPEEN_DISTANCE;
			if (slideTop >= (frame.bottom - 8)) {
				slideTop = frame.top;
			}
			// draw QR code scan line
			/*paint.setColor(scanLineColor);
			RectF scanLine = new RectF(frame.left + MIDDLE_LINE_PADDING, slideTop
					- MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING,
					slideTop + MIDDLE_LINE_WIDTH / 2);
			canvas.drawRect(scanLine , paint);*/
			Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + 8;
            canvas.drawBitmap(((BitmapDrawable)(getResources()
            		.getDrawable(R.drawable.qrcode_scan_line))).getBitmap()
            		, null, lineRect, paint);
            
			if (currentLast != null) {
				paint.setAlpha(CURRENT_POINT_OPACITY / 2);
				paint.setColor(resultPointColor);
				synchronized (currentLast) {
					float radius = POINT_SIZE / 2.0f;
					for (ResultPoint point : currentLast) {
						canvas.drawCircle(frameLeft
								+ (int) (point.getX() * scaleX), frameTop
								+ (int) (point.getY() * scaleY), radius, paint);
					}
				}
			}

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
					frame.top - POINT_SIZE, frame.right + POINT_SIZE,
					frame.bottom + POINT_SIZE);
		}
	}

	public void drawViewfinder() {
		//Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		/*if (resultBitmap != null) {
			resultBitmap.recycle();
		}*/
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

}
