package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 *
 * @ClassName: CircleWaveView
 * @Description: 水纹扩散效果
 * @author YangMingGuang
 * @date 2015-7-12 上午11:08:54
 *
 */
public class CircleWaveView extends View implements Runnable {

	private float mWidth;
	private float mHeight;
    private Paint mLinePaint;
    private Paint mSolidPaint;
    private float centerX; //圆心X
    private float centerY; //圆心Y
    private float floatRadius; //变化的半径
    private float maxRadius = -1; //圆半径
    private volatile boolean started = false;
	private int waveInterval = 300; //圆环的宽度
    private float bottomMargin = 0;//底部margin
    private boolean centerAlign = true;//居中
    private boolean fillCircle = false;//是否填充成实心圆环

    public CircleWaveView(Context context) {
        this(context, null, 0);
    }

    private int waveColor = Color.argb(128, 110, 219, 2); //颜色

	public CircleWaveView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleWaveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		mLinePaint = new Paint();
		mSolidPaint = new Paint();
	}

	public void init() {
		mWidth = getWidth();
		mHeight = getHeight();

		mLinePaint.setAntiAlias(true);
		mLinePaint.setStrokeWidth(1.0F);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setColor(waveColor);

		if (fillCircle) {
			mSolidPaint.setStyle(Paint.Style.STROKE);
			mSolidPaint.setStrokeWidth(waveInterval);
			mSolidPaint.setColor(waveColor);
		}

		centerX = mWidth / 2.0F;
		if (centerAlign) {
			centerY = (mHeight / 2.0F);
		} else {
			centerY = mHeight - bottomMargin;
		}

		if (mWidth >= mHeight) {
			maxRadius = mHeight / 2.0F;
		} else {
			maxRadius = mWidth / 2.0F;
		}

		floatRadius = (maxRadius % waveInterval);

		start();
	}

	public void start() {
		if (!started) {
			started = true;
			new Thread(this).start();
		}
	}

	public void stop() {
		started = false;
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stop();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (maxRadius <= 0.0F) {
			return;
		}
		float radius = floatRadius % waveInterval;
		while (true) {
			int alpha = (int) (255.0F * (1.0F - radius / maxRadius));
			if (alpha <= 0) {
				break;
			}

			if (fillCircle) {
				mSolidPaint.setAlpha(alpha >> 2);
				canvas.drawCircle(centerX, centerY, radius - waveInterval / 2, mSolidPaint);
			}
			mLinePaint.setAlpha(alpha);
			canvas.drawCircle(centerX, centerY, radius, mLinePaint);
			radius += waveInterval;
		}
	}

	/**
	 * 窗口获取焦点，初始化、停止
	 */
	/*	public void onWindowFocusChanged(boolean hasWindowFocus) {
			super.onWindowFocusChanged(hasWindowFocus);
			if (hasWindowFocus) {
				init();
			} else {
				stop();
			}
		}*/

	public void run() {
		while (started) {
			floatRadius = 4.0F + floatRadius;
			if (floatRadius > maxRadius) {
				floatRadius = (maxRadius % waveInterval);
			}
			postInvalidate();
			try {
				Thread.sleep(50L);
			} catch (InterruptedException localInterruptedException) {
				localInterruptedException.printStackTrace();
			}
		}
	}

	public void setMaxRadius(float maxRadius) {
		this.maxRadius = maxRadius;
	}

	public void setWaveColor(int waveColor) {
		this.waveColor = waveColor;
	}

	public void setWaveInterval(int waveInterval) {
		this.waveInterval = waveInterval;
	}

	public void setCenterAlign(boolean centerAlign) {
		this.centerAlign = centerAlign;
	}
}
