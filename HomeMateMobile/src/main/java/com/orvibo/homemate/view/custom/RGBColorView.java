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
import com.orvibo.homemate.util.ColorUtil;
import com.orvibo.homemate.util.LogUtil;

public class RGBColorView extends View implements OnTouchListener {
    private static final String TAG = RGBColorView.class.getSimpleName();
    private OnColorChangedListener onColorChangedListener;
    private Bitmap                 aimBitmap;//取色瞄准器
    private int                    aimBitmapWH;//取色瞄准器宽高
    private float                  aimX;//取色瞄准器X轴位置
    private float                  aimY;//取色瞄准器Y轴位置
    private Bitmap                 paletteBitmap;//色板
    private int                    paletteBitmapWH;//色板宽高
    private int                    colorViewWH;//this宽高
    private float                  radius;//色板半径
    private float                  cx;//中间点X轴坐标
    private float                  cy;//中间点Y轴坐标
    private final int INTERVAL_TIME = 300;//移动间隔时间
    private long lastTime;

    public RGBColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);

        int aimWH = getResources().getDimensionPixelSize(R.dimen.rgb_aim);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_color_pickup);
        aimBitmap = Bitmap.createScaledBitmap(bitmap2, aimWH, aimWH, true);
        aimBitmapWH = aimBitmap.getWidth();

        int paletteWH = getResources().getDimensionPixelSize(R.dimen.rgb_palette);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.rgb_new_palette);
        paletteBitmap = Bitmap.createScaledBitmap(bitmap1, paletteWH, paletteWH, true);
        paletteBitmapWH = paletteBitmap.getWidth();

        radius = ColorUtil.round((float) (paletteBitmapWH / 2.0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (colorViewWH == 0) {
            colorViewWH = getMeasuredWidth();
            cx = (float)(colorViewWH / 2.0);
            cy = (float)(colorViewWH / 2.0);
            aimX = cx;
            aimY = cy;
        }
        canvas.drawBitmap(paletteBitmap, (colorViewWH - paletteBitmapWH) / 2, (colorViewWH - paletteBitmapWH) / 2, null);
        canvas.drawBitmap(aimBitmap, aimX - (float)(aimBitmapWH / 2.0), aimY - (float)(aimBitmapWH / 2.0), null);

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
//                int color = getColor(x, y);
                float xy[] = pointAdsorb(x, y);
                x = xy[0];
                y = xy[1];
                int color = pointXYToRGB(x, y);
                color = pointAdsorb(color);
                long currentTime = System.currentTimeMillis();
                if (onColorChangedListener!=null) {
                    onColorChangedListener.onColorShow(color);
                }
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
//                int color = getColor(x, y);
                float xy[] = pointAdsorb(x, y);
                x = xy[0];
                y = xy[1];
                int color = pointXYToRGB(x, y);
                color = pointAdsorb(color);

                if (onColorChangedListener != null) {
                    onColorChangedListener.onColorAim(color);
                }
                break;
        }
        return true;
    }

    /**
     * 六个点自动吸附
     */
    private int pointAdsorb(int color) {
        int currentR = Color.red(color);
        int currentG = Color.green(color);
        int currentB = Color.blue(color);
        if (currentR == 255 && currentG == 0) {
            if (currentB < 20) {
                currentB = 0;
            } else if (currentB > 235) {
                currentB = 255;
            }
        } else if (currentR == 255 && currentB == 0) {
            if (currentG < 20) {
                currentG = 0;
            } else if (currentG > 235) {
                currentG = 255;
            }
        } else if (currentG == 255 && currentB == 0) {
            if (currentR < 20) {
                currentR = 0;
            } else if (currentR > 235) {
                currentR = 255;
            }
        } else if (currentG == 255 && currentR == 0) {
            if (currentB < 20) {
                currentB = 0;
            } else if (currentB > 235) {
                currentB = 255;
            }
        } else if (currentB == 255 && currentG == 0) {
            if (currentR < 20) {
                currentR = 0;
            } else if (currentR > 235) {
                currentR = 255;
            }
        } else if (currentB == 255 && currentR == 0) {
            if (currentG < 20) {
                currentG = 0;
            } else if (currentG > 235) {
                currentG = 255;
            }
        }
        int[] rgb = new int[3];
        rgb[0] = currentR;
        rgb[1] = currentG;
        rgb[2] = currentB;
        locationAim(rgb);
        return Color.rgb(currentR, currentG, currentB);
    }

    /**
     * 三条线自动吸附
     */
    private float[] pointAdsorb(float currentX, float currentY) {
        float distance = (float) Math.sqrt(Math.pow((currentX - cx), 2) + Math.pow((currentY - cy), 2));
        if (distance > radius) {//距离超出半径，则设置取色器中心位置在触点、圆心所在直线与圆弧相交的点上
            aimX = (float) (cx + radius * Math.cos(Math.atan2(currentX - cx, cy - currentY) - (Math.PI / 2)));
            aimY = (float) (cy + radius * Math.sin(Math.atan2(currentX - cx, cy - currentY) - (Math.PI / 2)));
        } else {
            aimX = currentX;
            aimY = currentY;
        }
        LogUtil.d("zhaoxiaowei", "onTouch - aimX = " + aimX + " aimY = " + aimY + " pX = " + currentX + "pY = " + currentY);
        invalidate();
        float x = aimX - cx;
        float y = -(aimY - cy);

        float d1 = Math.abs((float) (Math.sqrt(3.0) * x - y) / 2);
        float d2 = Math.abs((float) (Math.sqrt(3.0) * x + y) / 2);
        float d3 = Math.abs(y);
        float d4 = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        if (d1 < 10) {
            if (y / x < Math.sqrt(3.0)) {
                if (x > 0) {
                    y = y + d1 / 2;
                    x = (float) (x - d1 * Math.sqrt(3.0) / 2);
                } else {
                    y = y - d1 / 2;
                    x = (float) (x + d1 * Math.sqrt(3.0) / 2);
                }
            } else {
                if (x > 0) {
                    y = y - d1 / 2;
                    x = (float) (x + d1 * Math.sqrt(3.0) / 2);
                } else {
                    y = y + d1 / 2;
                    x = (float) (x - d1 * Math.sqrt(3.0) / 2);
                }
            }
        }

        if (d2 < 10) {
            if (y / x > -Math.sqrt(3.0)) {
                if (x < 0) {
                    y = y + d2 / 2;
                    x = (float) (x + d2 * Math.sqrt(3.0) / 2);
                } else {
                    y = y - d2 / 2;
                    x = (float) (x - d2 * Math.sqrt(3.0) / 2);
                }
            } else {
                if (x < 0) {
                    y = y - d2 / 2;
                    x = (float) (x - d2 * Math.sqrt(3.0) / 2);
                } else {
                    y = y + d2 / 2;
                    x = (float) (x + d2 * Math.sqrt(3.0) / 2);
                }
            }
        }

        if (d3 < 10) {
            y = 0;
        }

        if (d4 < 20) {
            x = y = 0;
        }

        float[] xy = new float[2];
        xy[0] = x + cx;
        xy[1] = -y + cy;
        return xy;
    }

//    public int getColor(float x, float y) {
//        //触点到圆心距离
//        float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
//        if (distance > radius) {//距离超出半径，则设置取色器中心位置在触点、圆心所在直线与圆弧相交的点上
//            aimX = (float) (cx + radius * Math.cos(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
//            aimY = (float) (cy + radius * Math.sin(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
//        } else {
//            aimX = x;
//            aimY = y;
//        }
//
//        int pX = (int) aimX - (colorViewWH - paletteBitmapWH) / 2;
//        int pY = (int) aimY - (colorViewWH - paletteBitmapWH) / 2;
//        int color = paletteBitmap.getPixel(pX >= paletteBitmapWH ? paletteBitmapWH - 1 : pX, pY >= paletteBitmapWH ? paletteBitmapWH - 1 : pY);
//        //LogUtil.d(TAG, "getColor - aimX = " + aimX + " aimY = " + aimY + " pX = " + pX + "pY = " + pY);
//        invalidate();
//        return color;
//    }

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
    public void locationAim(int rgb[], int colorViewWH) {
        this.colorViewWH = colorViewWH;
        cx = colorViewWH / 2;
        cy = colorViewWH / 2;
        locationAim(rgb);
    }

    /**
     * 定位取色瞄准器位置
     *
     * @param rgb
     */
    public void locationAim(int rgb[]) {
        int currentColor = Color.argb(255, rgb[0], rgb[1], rgb[2]);
        //int currentA = Color.alpha(currentColor);
        int currentR = Color.red(currentColor);
        int currentG = Color.green(currentColor);
        int currentB = Color.blue(currentColor);
        boolean angleSign = false;//夹角系数的正负,flase为负，true为正
        boolean xySign = false;//flase为负，true为正
        boolean xORy = false;//根据x还是y来获取坐标的正负，false：根据x正负来获取.true 根据y正负来获取
        int angleG = 60;
        int angleR = 180;
        int angleB = 300;
        int angle = angleG;
        int colorM = 255;
        int colorN = 255;
        LogUtil.d(TAG, "currentRGB - currentR = " + currentR + " currentG = " + currentG + " currentB = " + currentB);

        if (currentG == 255) {//0-120
            angle = angleG;
            xySign = true;
            xORy = true;
            if (currentR < currentB) {//0-60
                angleSign = false;//arctan(y/x)-60PI/180
                colorM = currentR;
                colorN = currentB;

            } else if (currentR >= currentB) {//60-120
                angleSign = true;
                colorM = currentB;
                colorN = currentR;
            }
        } else if (currentR == 255) {
            angle = angleR;
            xySign = false;
            xORy = false;
            if (currentB < currentG) {//120-180
                angleSign = false;
                colorM = currentB;
                colorN = currentG;

            } else if (currentB >= currentG) {//180-240
                angleSign = true;
                colorM = currentG;
                colorN = currentB;
            }
        } else if (currentB == 255) {
            angle = angleB;
            xySign = false;
            xORy = true;
            if (currentG < currentR) {//240-300
                angleSign = false;
                colorM = currentG;
                colorN = currentR;
            } else if (currentG >= currentR) {//300-360
                angleSign = true;
                colorM = currentR;
                colorN = currentG;
            }
        }
        if (currentG == 255 && currentB == 255) {
            //定位在G B色盘交界处
            aimX = (float) (radius * (1 - currentR / 255.0) + cx);
            aimY = cy;

        } else if (currentG == 255 && currentR == 255 && currentB == 0) {
            //定位在G R色盘交界处
            aimX = cx - radius / 2;
            aimY = (float) (cy - radius * Math.sqrt(3.0) / 2);

        } else if (currentB == 255 && currentR == 255 && currentG == 0) {
            //定位在B R色盘交界处
            aimX = cx - radius / 2;
            aimY = (float) (cy + radius * Math.sqrt(3.0) / 2);
        } else if (currentR == 255 && currentG == 255 && currentB == 255) {
            //定位在中心
            aimX = cx;
            aimY = cy;
        } else {
            getXYByColor(colorM, colorN, angle, angleSign, xORy, xySign);
        }
        LogUtil.d(TAG, "locationAim() - aimX = " + aimX + " aimY = " + aimY);
        invalidate();
    }

    private void getXYByColor(int colorM, int colorN, int angle, boolean angleSign, boolean xORy, boolean xySign) {
        float x;
        float y;
        float temp1 = (float) Math.pow((radius - colorM * radius / 255.0), 2);
        float temp2;
        if (angleSign) {
            temp2 = (float) Math.tan(degreesToRadian(60) * (colorN - colorM) / (255 - colorM) + degreesToRadian(angle));
        } else {
            temp2 = (float) Math.tan(degreesToRadian(angle) - degreesToRadian(60) * (colorN - colorM) / (255 - colorM));
        }
        if (xORy) {

            y = (float) Math.sqrt(temp1 / (1 + 1 / Math.pow(temp2, 2)));
            if (!xySign) {
                y = -y;
            }

            x = y / temp2;
        } else {
            x = (float) Math.sqrt(temp1 / (1 + Math.pow(temp2, 2)));
            if (!xySign) {
                x = -x;
            }
            y = x * temp2;
        }
        aimX = x + cx;
        aimY = -y + cy;
    }

    private float degreesToRadian(int degree) {
        return (float) (degree * Math.PI / 180.0);
    }

    private int pointXYToRGB(float currentX, float currentY) {
        float distance = (float) Math.sqrt(Math.pow((currentX - cx), 2) + Math.pow((currentY - cy), 2));
        if (distance > radius) {//距离超出半径，则设置取色器中心位置在触点、圆心所在直线与圆弧相交的点上
            aimX = (float) (cx + radius * Math.cos(Math.atan2(currentX - cx, cy - currentY) - (Math.PI / 2)));
            aimY = (float) (cy + radius * Math.sin(Math.atan2(currentX - cx, cy - currentY) - (Math.PI / 2)));
        } else {
            aimX = currentX;
            aimY = currentY;
        }
        invalidate();

        int R = 255;
        int G = 255;
        int B = 255;
        float x = aimX - cx;
        float y = -(aimY - cy);

        float C;
        if (y > 0) {
            if (x < 0) {
                C = (float) (Math.atan(y / -x) / degreesToRadian(60));
                if (C > 0 && C < 1) {
                    R = 255;
                    B = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    G = ColorUtil.round(B + C * (255 - B));
                } else {
                    G = 255;
                    B = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    C = (float) ((Math.atan(-x / y) + degreesToRadian(30)) / degreesToRadian(60));
                    R = ColorUtil.round(B + C * (255 - B));
                }
            } else if (x > 0) {
                C = (float) (Math.atan(y / x) / degreesToRadian(60));
                if (C > 0 && C < 1) {
                    G = 255;
                    R = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    B = ColorUtil.round(255 - C * (255 - R));
                } else {
                    G = 255;
                    B = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    C = (float) ((degreesToRadian(30) - Math.atan(x / y)) / degreesToRadian(60));
                    R = ColorUtil.round(B + C * (255 - B));
                }
            } else if (x == 0) {

                R = ColorUtil.round((float) (255 - (y / radius) * (255 / 2.0)));
                G = 255;
                B = ColorUtil.round(255 - (y / radius) * 255);
            }
        } else if (y < 0) {
            if (x < 0) {
                C = (float) (Math.atan(y / x) / degreesToRadian(60));
                if (C > 0 && C < 1) {
                    R = 255;
                    G = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    B = ColorUtil.round(G + C * (255 - G));
                } else {
                    B = 255;
                    G = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    C = (float) ((Math.atan(x / y) + degreesToRadian(30)) / degreesToRadian(60));
                    R = ColorUtil.round(G + C * (255 - G));
                }
            } else if (x > 0) {
                C = (float) (Math.atan(-y / x) / degreesToRadian(60));
                if (C > 0 && C < 1) {
                    B = 255;
                    R = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    G = ColorUtil.round(255 - C * (255 - R));
                } else {
                    B = 255;
                    G = ColorUtil.round((float) (radius - Math.sqrt(x * x + y * y)) * 255 / radius);
                    C = (float) ((degreesToRadian(30) - Math.atan(x / -y)) / degreesToRadian(60));
                    R = ColorUtil.round(G + C * (255 - G));
                }
            } else if (x == 0) {

                R = ColorUtil.round((float) (255 - (-y / radius) * (255 / 2.0)));
                B = 255;
                G = ColorUtil.round(255 - (-y / radius) * 255);
            }
        } else {
            if (x < 0) {
                R = 255;
                G = ColorUtil.round(255 - (-x / radius) * 255);
                B = ColorUtil.round(255 - (-x / radius) * 255);
            } else if (x > 0) {
                R = ColorUtil.round(255 - (x / radius) * 255);
                G = 255;
                B = 255;
            } else {
                R = 255;
                G = 255;
                B = 255;
            }
        }
        return Color.rgb(R, G, B);
    }


    public interface OnColorChangedListener {
        void onStartColorChanged();

        void onColorChanged(int color);

        void onColorShow(int color);

        /**
         * 松开手后，颜色回调
         * @param color
         */
        void onColorAim(int color);
    }
}
