/**
 * @author Raghav Sood
 * @version 1
 * @date 26 January, 2013
 */
package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.LogUtil;

/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBar extends IrButton {
    private final static String TAG = CircularSeekBar.class.getSimpleName();
    private Context mContext;
    private AttributeSet mAttr;
    private OnChangeTemperatureListener mListener;//监听器
    private int maxProgress = 14;//最大进度
    private int progress;//当前进度
    private int startProgress;//初始进度
    private float radius;//滑动路径所在圆弧的半径
    private float cx;//中间点X轴坐标
    private float cy;//中间点Y轴坐标
    private int circleWH;
    private Bitmap markPointBitmap;//标记点位图
    private float markPointX;//标记点中点X轴坐标
    private float markPointY;//标记点中点Y轴坐标
    private Bitmap markPointBitmapPressed;//标记点按下位图
    private Bitmap circularBitmap;//圆弧位图
    private float adjustmentFactor;//标记点可触摸范围
    private boolean IS_PRESSED = false;//是否按下
    private boolean CALLED_FROM_ANGLE = false;//标记setProgress()方法是否被setAngle()方法调用
    private boolean IS_OUT = false;//标记触摸点是否在圆弧范围内
    private boolean IS_LONG_CLICK = false;//是否响应长按事件，如果是长按则不回调onResultTemperature
    private int deltaOfProgressToTemperature = 15;
    private final int INTERVAL_TIME = 200;//移动间隔时间
    private long lastTime;
    private long lastTouchTime;
    private VelocityTracker vTracker = null;
    private boolean isLeanMode = false;
    private boolean isACPanel = false;
    private boolean isWifiPanel = false;
    private float density;
    private OnTouchStateListener onTouchStateListener;
    private PaintFlagsDrawFilter paintFlagsDrawFilter;
    private boolean isCloseStatus = false;
    private boolean isLock = false;
    private int mWidthMeasureSpec;
    private int mHeightMeasureSpec;
    private Bitmap bitmap3;
    private Bitmap circularBitmap1;
    private Bitmap circularBitmap_wifi;
    private Bitmap circularBitmap1_wifi;
    private Bitmap mBitmap3_wifi;
    private Bitmap mBitmap4_wifi;
    private int resultColor = 0;
    private int progressColor = 0;

    public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        progressColor = mContext.getResources().getColor(R.color.green);
        mAttr = attrs;
        initDrawable(context, attrs);
    }

    public CircularSeekBar(Context context, AttributeSet attrs) {
        // super(context, attrs);
        this(context, attrs, 0);
        // initDrawable(context, attrs);
    }


    private void initDrawable(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularSeekBar);
        int width = a.getDimensionPixelSize(R.styleable.CircularSeekBar_width, 180);
        int height = a.getDimensionPixelSize(R.styleable.CircularSeekBar_cicular_height, 180);
        a.recycle();
        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        density = getResources().getDisplayMetrics().density;
        int min = Math.min(width, height);

        int thumbWH = min / 8;
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.conditioner_thumb);
        markPointBitmap = Bitmap.createScaledBitmap(bitmap1, thumbWH, thumbWH, true);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.conditioner_thumb);
        markPointBitmapPressed = Bitmap.createScaledBitmap(bitmap2, thumbWH, thumbWH, true);

        circleWH = min - thumbWH;
        //空调面板
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.condition_circle);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.condition_circle_off);
//            bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.ac_controller_scale_circle_on);
//            bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.ac_controller_scale_circle_off);
        //wifi空调
        mBitmap3_wifi = BitmapFactory.decodeResource(getResources(), R.drawable.cw_controller_scale_on);
        mBitmap4_wifi = BitmapFactory.decodeResource(getResources(), R.drawable.cw_controller_scale_off);
        //打开的图标
        circularBitmap = Bitmap.createScaledBitmap(bitmap3, circleWH, circleWH, true); // 参数为true，消除锯齿
        //关闭图标
        circularBitmap1 = Bitmap.createScaledBitmap(bitmap4, circleWH, circleWH, true); // 参数为true，消除锯齿

        //   circularBitmap_wifi = Bitmap.createScaledBitmap(mBitmap3_wifi, circleWH, circleWH, true); // 参数为true，消除锯齿
        //
        //  circularBitmap1_wifi = Bitmap.createScaledBitmap(mBitmap4_wifi, circleWH, circleWH, true); // 参数为true，消除锯齿

        radius = circleWH / 2 - 4;
        adjustmentFactor = min / 4;
        cx = width / 2;
        cy = height / 2;
        setTemperature(deltaOfProgressToTemperature + maxProgress / 2);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidthMeasureSpec = widthMeasureSpec;
        mHeightMeasureSpec = heightMeasureSpec;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(paintFlagsDrawFilter); // canvas 加抗锯齿
        if (isCloseStatus) {
            canvas.drawBitmap(circularBitmap1, cx - circleWH / 2, cy - circleWH / 2, null);
        } else {
            canvas.drawBitmap(circularBitmap, cx - circleWH / 2, cy - circleWH / 2, null);
        }
        float dx;
        float dy;
        //canvas 标志点的位置
        if (IS_PRESSED) {
            dx = markPointX - (markPointBitmapPressed.getWidth() / 2);
            dy = markPointY - (markPointBitmapPressed.getHeight() / 2);
            canvas.drawBitmap(markPointBitmapPressed, dx, dy, null);
        } else {
            dx = markPointX - (markPointBitmap.getWidth() / 2);
            dy = markPointY - (markPointBitmap.getHeight() / 2);
            canvas.drawBitmap(markPointBitmap, dx, dy, null);
        }
        super.onDraw(canvas);
    }

    /**
     * 设置标志点角度
     *
     * @param angle 新的角度
     */
    private void setAngle(int angle) {
        LogUtil.e(TAG, "angle=" + angle);
        float donePercent = (((float) angle) / 360) * 100;
        float progress = (donePercent / 100) * maxProgress;
        if (isWifiPanel || isACPanel) {
            //这里加5是为了扩展边界值
            //解决边界取不到值的问题,以10为一个进度
            progress += 5;

        }

        LogUtil.e(TAG, "progress=" + progress);
        CALLED_FROM_ANGLE = true;
        setProgress(Math.round(progress));

    }


    /**
     * 设置进度
     *
     * @param progress 新的进度
     */
    private void setProgress(int progress) {
        if (this.progress != progress) {
            this.progress = progress;
            if (!CALLED_FROM_ANGLE) {
                int newPercent = (this.progress * 100) / this.maxProgress;
                int newAngle = (newPercent * 360) / 100;
                this.setAngle(newAngle);
            }
            CALLED_FROM_ANGLE = false;
//            long currentTime = System.currentTimeMillis();
//            if (isLeanMode || currentTime > lastTime + INTERVAL_TIME) {//前后移动时间超过规定间隔时间才通知进度
//                lastTime = currentTime;
            if (mListener != null) {
                if ((isWifiPanel && (this.progress + deltaOfProgressToTemperature) < 170) || (isACPanel && (this.progress + deltaOfProgressToTemperature) > 290) || (isACPanel && this.progress + deltaOfProgressToTemperature < 110)) {
                    //color 的值不在改变
                } else {
                    progressColor = circularBitmap.getPixel((int) (markPointX - (cx - circleWH / 2)), (int) (markPointY - (cx - circleWH / 2)));
                }
                mColor = progressColor;
                tmp = this.progress + deltaOfProgressToTemperature;
                LogUtil.e(TAG, "tmp progress=" + tmp);
                mListener.onChangeTemperature(this, this.progress + deltaOfProgressToTemperature, progressColor);
            }
//            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCloseStatus || isLock) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastTouchTime = System.currentTimeMillis();
                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                } else {
                    vTracker.clear();
                }
                vTracker.addMovement(event);
                IS_OUT = false;
                IS_LONG_CLICK = false;
                float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));//触摸点到中心的距离
                if (distance > radius + adjustmentFactor || distance < radius - adjustmentFactor) {//距离大于或小于标记点可触摸范围
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                IS_PRESSED = true;
                calculate(x, y);
                vTracker.addMovement(event);
                vTracker.computeCurrentVelocity(500);
                float xVelocity = vTracker.getXVelocity();
                float yVelocity = vTracker.getYVelocity();
                Log.d(getClass().getName(), "the x velocity is " + vTracker.getXVelocity());
                Log.d(getClass().getName(), "the y velocity is " + vTracker.getYVelocity());
                long now = System.currentTimeMillis();
                if (isLeanMode && now > lastTouchTime + 500) {
                    lastTouchTime = now;
                    if (Math.abs(xVelocity) < 4 * density && Math.abs(yVelocity) < 4 * density) {
                        lastTouchTime = Long.MAX_VALUE - 500;
                        onlongClick();
                        IS_OUT = true;
                        IS_LONG_CLICK = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                IS_PRESSED = false;
                calculate(x, y);
                if (mListener != null && !IS_LONG_CLICK) {

                    if ((isWifiPanel && (this.progress + deltaOfProgressToTemperature) < 170) || (isACPanel && (this.progress + deltaOfProgressToTemperature) > 290) || (isACPanel && this.progress + deltaOfProgressToTemperature < 110)) {
                        //color 的值不在改变
                    } else {
                        resultColor = circularBitmap.getPixel((int) (markPointX - (cx - circleWH / 2)), (int) (markPointY - (cx - circleWH / 2)));
                    }
                    mListener.onResultTemperature(this, this.progress + deltaOfProgressToTemperature, resultColor);
                }
                //vTracker.recycle();
                break;
        }
        if (onTouchStateListener != null) {
            onTouchStateListener.onTouchState(action);
        }

        return true;
    }

    private void calculate(float x, float y) {
        float lastPX = markPointX;
        float lastPY = markPointY;

        //触摸点与圆心延长线相交于圆弧的点
        markPointX = (float) (cx + radius * Math.cos(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
        markPointY = (float) (cy + radius * Math.sin(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));

        float degrees = (float) (((Math.toDegrees(Math.atan2(x - cx, cy - y)) + 540.0)) % 360.0);
        float degreesOfAtomProgress = 360f / maxProgress;
        if (isACPanel || isWifiPanel) {
            degreesOfAtomProgress *= 10;
        }

        if (degrees > 360 - degreesOfAtomProgress || degrees < degreesOfAtomProgress) {//在圆弧外的区域
            LogUtil.d(TAG, "calculate degrees=" + degrees);
            IS_OUT = true;
            //空调面板越界
        }
        //超出边界只能滑回去，并且不能滑过去另外一边的边界
        if (IS_OUT && (degrees < 360 - degreesOfAtomProgress && degrees > 360 - degreesOfAtomProgress * 2 && lastPX > cx || degrees < degreesOfAtomProgress * 2 && degrees > degreesOfAtomProgress && lastPX < cx)) {
            IS_OUT = false;
        }


        //不能超出边界
        if (IS_OUT) {
            markPointX = lastPX;
            markPointY = lastPY;
        } else {
            setAngle(Math.round(degrees));
            invalidate();
        }
    }

    public void setAsACPanel() {
        maxProgress = 220;
        deltaOfProgressToTemperature = 90;
        isACPanel = true;
    }

    public void setAsWifiACPanel() {
        maxProgress = 170;
        deltaOfProgressToTemperature = 150;
        isWifiPanel = true;
        // invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setIsLeanMode(boolean isLeanMode) {
        this.isLeanMode = isLeanMode;
    }

    /**
     * 每一个温度值对应唯一的一个位置（唯一的一个角度）
     *
     * @param temperature
     */
    public void setTemperature(int temperature) {
        startProgress = temperature - deltaOfProgressToTemperature;//deltaOfProgressToTemperature是对应的下端点位置对应的进度的值
        if (isACPanel || isWifiPanel) {
            startProgress = temperature - deltaOfProgressToTemperature;
        }
        if (startProgress < 1) {
            startProgress = 1;
        } else if (startProgress > maxProgress) {
            startProgress = maxProgress;
        }
//        if (markPointX != 0) {
        Log.d(getClass().getName(), "setTemperature:" + startProgress);
        int alpha = 360 * startProgress / maxProgress;
        //计算标志点的位置
        markPointX = (float) (cx + radius * Math.cos(2 * Math.PI * (alpha + 90) / 360));
        markPointY = (float) (cy + radius * Math.sin(2 * Math.PI * (alpha + 90) / 360));
        setAngle(alpha);
        invalidate();
//            setProgress(startProgress);
//        }
    }

    public void setOnChangeTemperatureListener(OnChangeTemperatureListener listener) {
        mListener = listener;
    }

    public OnChangeTemperatureListener getOnChangeTemperatureListener() {
        return mListener;
    }

    public interface OnChangeTemperatureListener {

        void onChangeTemperature(CircularSeekBar view, int temperature, int color);

        void onResultTemperature(CircularSeekBar view, int temperature, int color);
    }

    public void setOnTouchStateListener(OnTouchStateListener onTouchStateListener) {
        this.onTouchStateListener = onTouchStateListener;
    }

    public interface OnTouchStateListener {
        /**
         * 触摸状态
         *
         * @param motionEvent set {@link MotionEvent}
         */
        void onTouchState(int motionEvent);
    }

    /**
     * 置灰圆盘
     */
    public void setInActivateStatus(boolean isInactivate) {
        isCloseStatus = isInactivate;
        setEnabled(!isInactivate);
        invalidate();


    }

    /**
     * acpanel 锁定
     *
     * @param isLock
     */
    public void setLockStatus(boolean isLock) {
        setEnabled(!isLock);
        this.isLock = isLock;
    }


}
