package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.PointXY;
import com.orvibo.homemate.util.LogUtil;

public class ColorView extends View implements OnTouchListener {
    private static final String TAG = ColorView.class.getSimpleName();
    private OnColorChangedListener onColorChangedListener;
    private Bitmap aimBitmap;//取色瞄准器
    private int aimBitmapWH;//取色瞄准器宽高
    private float aimX;//取色瞄准器X轴位置
    private float aimY;//取色瞄准器Y轴位置
    private Bitmap paletteBitmap;//色板
    private int paletteBitmapWH;//色板宽高
    private int colorViewWH;//this宽高
    private float radius;//色板半径
    private float cx;//中间点X轴坐标
    private float cy;//中间点Y轴坐标
    private final int INTERVAL_TIME = 300;//移动间隔时间
    private long lastTime;
    private int xCount = 20;
    private int yCount = 10;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);

        int aimWH = getResources().getDimensionPixelSize(R.dimen.rgb_aim);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
                R.drawable.rgb_aim);
        aimBitmap = Bitmap.createScaledBitmap(bitmap2, aimWH, aimWH, true);
        aimBitmapWH = aimBitmap.getWidth();

        int paletteWH = getResources().getDimensionPixelSize(R.dimen.rgb_palette);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.rgb_palette);
        paletteBitmap = Bitmap.createScaledBitmap(bitmap1, paletteWH, paletteWH, true);
        paletteBitmapWH = paletteBitmap.getWidth();

        radius = paletteBitmapWH / 2 - 2;//减2是为了消除边缘误差，从而在取色的时候，定位点不会超出色板边缘
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (colorViewWH == 0) {
            colorViewWH = getMeasuredWidth();
            cx = colorViewWH / 2;
            cy = colorViewWH / 2;
            aimX = cx;
            aimY = cy;
        }
        canvas.drawBitmap(paletteBitmap, (colorViewWH - paletteBitmapWH) / 2, (colorViewWH - paletteBitmapWH) / 2, null);
        canvas.drawBitmap(aimBitmap, aimX - aimBitmapWH / 2, aimY - aimBitmapWH / 2, null);

//        Paint p = new Paint();
//        p.setStyle(Paint.Style.FILL);
//        p.setStrokeWidth(10);
//        p.setRgb(Color.BLACK);// 设置红色
//        PointXY[] samplingPointXYs = getPointXY();
//        for (int i = 0; i < xCount * yCount; i++) {
//            canvas.drawPoint((int) samplingPointXYs[i].getPx(), (int) samplingPointXYs[i].getPy(), p);//画一个点
//        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                //触点到圆心距离
                float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
                if (distance > radius + aimBitmapWH / 2) {//距离超出半径，则设置取色器中心位置在触点、圆心所在直线与圆弧相交的点上
                    return false;
                }
                if (onColorChangedListener != null) {
                    onColorChangedListener.onStartColorChanged();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int color = getColor(x, y);
                long currentTime = System.currentTimeMillis();
                if (currentTime > lastTime + INTERVAL_TIME) {//前后移动时间超过规定间隔时间才通知进度
                    lastTime = currentTime;
                    if (onColorChangedListener != null) {
                        onColorChangedListener.onColorChanged(color);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int color = getColor(x, y);
                if (onColorChangedListener != null) {
                    onColorChangedListener.onColorAim(color);
                }
                break;
        }
        return true;
    }

    public int getColor(float x, float y) {
        //触点到圆心距离
        float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
        if (distance > radius) {//距离超出半径，则设置取色器中心位置在触点、圆心所在直线与圆弧相交的点上
            aimX = (float) (cx + radius * Math.cos(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
            aimY = (float) (cy + radius * Math.sin(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
        } else {
            aimX = x;
            aimY = y;
        }

        int pX = (int) aimX - (colorViewWH - paletteBitmapWH) / 2;
        int pY = (int) aimY - (colorViewWH - paletteBitmapWH) / 2;
        int color = paletteBitmap.getPixel(pX, pY);
        //LogUtil.d(TAG, "getColor - aimX = " + aimX + " aimY = " + aimY + " pX = " + pX + "pY = " + pY);
        invalidate();
        return color;
    }

    public void release() {
        try {
            if (aimBitmap != null) {
                if (!aimBitmap.isRecycled()) {
                    aimBitmap.recycle();
                }
                aimBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (paletteBitmap != null) {
                if (!paletteBitmap.isRecycled()) {
                    paletteBitmap.recycle();
                }
                paletteBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        onColorChangedListener = listener;
    }

    /**
     * 定位取色瞄准器位置
     */
    public void locationAim(int rgb[],int colorViewWH) {
        this.colorViewWH = colorViewWH;
        cx = colorViewWH / 2;
        cy = colorViewWH / 2;
        locationAim(rgb);
    }

    /**
     * 定位取色瞄准器位置
     */
    public void locationAim(int rgb[]) {
        int currentColor = Color.argb(255, rgb[0], rgb[1], rgb[2]);
        int currentA = Color.alpha(currentColor);
        int currentR = Color.red(currentColor);
        int currentG = Color.green(currentColor);
        int currentB = Color.blue(currentColor);
        LogUtil.d(TAG, "currentRGB - currentR = " + currentR + " currentG = " + currentG + " currentB = " + currentB + "currentA = " + currentA);
        int min = 0;
        double minDistance = Double.MAX_VALUE;
        double currentDistance = 0;

        PointXY[] samplingPointXYs = getPointXY();
        if (samplingPointXYs == null || samplingPointXYs.length == 0) {
            return;
        }
        for (int i = 0; i < xCount * yCount; i++) {
            try {
                int color = samplingPointXYs[i].getColor();
                int a = Color.alpha(color);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                currentDistance = Math.sqrt((currentR - r) * (currentR - r))
                        + Math.sqrt((currentG - g) * (currentG - g))
                        + Math.sqrt((currentB - b) * (currentB - b));

                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                    min = i;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        PointXY samplingPointXY = samplingPointXYs[min];
        aimX = (int) samplingPointXY.getPx();
        aimY = (int) samplingPointXY.getPy();
        LogUtil.d(TAG, "locationAim() - aimX = " + aimX + " aimY = " + aimY);
        invalidate();
    }

    /**
     * @return
     * @author Smagret
     * <p/>
     * 取100个样点
     */
    private PointXY[] getPointXY() {
        int t = 0;
        PointXY[] pointXYs = new PointXY[xCount * yCount];
        LogUtil.d(TAG, "getPointXY() - cx = " + cx + " cy = " + cy + " colorViewWH = " + colorViewWH + " paletteBitmapWH = " + paletteBitmapWH);
        for (int i = 1; i <= xCount; i++) {
            for (int n = 1; n <= yCount; n++) {
                if (paletteBitmap == null) {
                    return null;
                }
                double pX = (cx + (radius * (0.1 * n) * Math.cos((360.0 / xCount) * i))) - (colorViewWH - paletteBitmapWH) / 2;
                double pY = (cy + (radius * (0.1 * n) * Math.sin((360.0 / xCount) * i))) - (colorViewWH - paletteBitmapWH) / 2;
                int color = paletteBitmap.getPixel((int) (pX > 0 ? pX : 0), (int) (pY > 0 ? pY : 0));
                pX = pX + (colorViewWH - paletteBitmapWH) / 2;
                pY = pY + (colorViewWH - paletteBitmapWH) / 2;
                PointXY pointXY = new PointXY(color, pX, pY);
                pointXYs[t] = pointXY;
                t++;
            }
        }


//        double minX = Double.MAX_VALUE;
//        double maxX = Double.MIN_VALUE;
//        double minY = Double.MAX_VALUE;
//        double maxY = Double.MIN_VALUE;
//
//        for (int i = 0; i < xCount * yCount; i++) {
//            //LogUtil.d(TAG, "getPointXY() - pointXYs = " + pointXYs[i]);
//            if (pointXYs[i].getPx() < minX) {
//                minX = pointXYs[i].getPx();
//            }
//            if (pointXYs[i].getPy() < minY) {
//                minY = pointXYs[i].getPy();
//            }
//            if (pointXYs[i].getPx() > maxX) {
//                maxX = pointXYs[i].getPx();
//            }
//            if (pointXYs[i].getPy() > maxY) {
//                maxY = pointXYs[i].getPy();
//            }
//
//        }
//        LogUtil.d(TAG, "getPointXY() - minX = " + minX + " maxX = " + maxX + " minY = " + minY + " maxY = " + maxY);
        return pointXYs;
    }


    public interface OnColorChangedListener {
        void onStartColorChanged();

        void onColorChanged(int color);

        void onColorAim(int color);
    }
}
