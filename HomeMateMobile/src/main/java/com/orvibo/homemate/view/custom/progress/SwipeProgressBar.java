//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.orvibo.homemate.view.custom.progress;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

final class SwipeProgressBar {
    private static final int COLOR1 = -1291845632;
    private static final int COLOR2 = -2147483648;
    private static final int COLOR3 = 1291845632;
    private static final int COLOR4 = 436207616;
    private static final int ANIMATION_DURATION_MS = 2000;
    private static final int FINISH_ANIMATION_DURATION_MS = 1000;
    private static final Interpolator INTERPOLATOR = BakedBezierInterpolator.getInstance();
    private final Paint mPaint = new Paint();
    private final RectF mClipRect = new RectF();
    private float mTriggerPercentage;
    private long mStartTime;
    private long mFinishTime;
    private boolean mRunning;
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    private View mParent;
    private Rect mBounds = new Rect();

    public SwipeProgressBar(View parent) {
        this.mParent = parent;
        this.mColor1 = -1291845632;
        this.mColor2 = -2147483648;
        this.mColor3 = 1291845632;
        this.mColor4 = 436207616;
    }

    void setColorScheme(int color1, int color2, int color3, int color4) {
        this.mColor1 = color1;
        this.mColor2 = color2;
        this.mColor3 = color3;
        this.mColor4 = color4;
    }

    void setTriggerPercentage(float triggerPercentage) {
        this.mTriggerPercentage = triggerPercentage;
        this.mStartTime = 0L;
        ViewCompat.postInvalidateOnAnimation(this.mParent);
    }

    void start() {
        if(!this.mRunning) {
            this.mTriggerPercentage = 0.0F;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = true;
            this.mParent.postInvalidate();
        }

    }

    void stop() {
        if(this.mRunning) {
            this.mTriggerPercentage = 0.0F;
            this.mFinishTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = false;
            this.mParent.postInvalidate();
        }

    }

    boolean isRunning() {
        return this.mRunning || this.mFinishTime > 0L;
    }

    void draw(Canvas canvas) {
        int width = this.mBounds.width();
        int height = this.mBounds.height();
        int cx = width / 2;
        int cy = height / 2;
        boolean drawTriggerWhileFinishing = false;
        int restoreCount = canvas.save();
        canvas.clipRect(this.mBounds);
        if(!this.mRunning && this.mFinishTime <= 0L) {
            if(this.mTriggerPercentage > 0.0F && (double)this.mTriggerPercentage <= 1.0D) {
                this.drawTrigger(canvas, cx, cy);
            }
        } else {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long elapsed = (now - this.mStartTime) % 2000L;
            long iterations = (now - this.mStartTime) / 2000L;
            float rawProgress = (float)elapsed / 20.0F;
            if(!this.mRunning) {
                if(now - this.mFinishTime >= 1000L) {
                    this.mFinishTime = 0L;
                    return;
                }

                long pct = (now - this.mFinishTime) % 1000L;
                float finishProgress = (float)pct / 10.0F;
                float pct1 = finishProgress / 100.0F;
                float clearRadius = (float)(width / 2) * INTERPOLATOR.getInterpolation(pct1);
                this.mClipRect.set((float)cx - clearRadius, 0.0F, (float)cx + clearRadius, (float)height);
                canvas.saveLayerAlpha(this.mClipRect, 0, 0);
                drawTriggerWhileFinishing = true;
            }

            if(iterations == 0L) {
                canvas.drawColor(this.mColor1);
            } else if(rawProgress >= 0.0F && rawProgress < 25.0F) {
                canvas.drawColor(this.mColor4);
            } else if(rawProgress >= 25.0F && rawProgress < 50.0F) {
                canvas.drawColor(this.mColor1);
            } else if(rawProgress >= 50.0F && rawProgress < 75.0F) {
                canvas.drawColor(this.mColor2);
            } else {
                canvas.drawColor(this.mColor3);
            }

            float pct2;
            if(rawProgress >= 0.0F && rawProgress <= 25.0F) {
                pct2 = (rawProgress + 25.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor1, pct2);
            }

            if(rawProgress >= 0.0F && rawProgress <= 50.0F) {
                pct2 = rawProgress * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor2, pct2);
            }

            if(rawProgress >= 25.0F && rawProgress <= 75.0F) {
                pct2 = (rawProgress - 25.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor3, pct2);
            }

            if(rawProgress >= 50.0F && rawProgress <= 100.0F) {
                pct2 = (rawProgress - 50.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor4, pct2);
            }

            if(rawProgress >= 75.0F && rawProgress <= 100.0F) {
                pct2 = (rawProgress - 75.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor1, pct2);
            }

            if(this.mTriggerPercentage > 0.0F && drawTriggerWhileFinishing) {
                canvas.restoreToCount(restoreCount);
                restoreCount = canvas.save();
                canvas.clipRect(this.mBounds);
                this.drawTrigger(canvas, cx, cy);
            }

            ViewCompat.postInvalidateOnAnimation(this.mParent);
        }

        canvas.restoreToCount(restoreCount);
    }

    private void drawTrigger(Canvas canvas, int cx, int cy) {
        this.mPaint.setColor(this.mColor1);
        canvas.drawCircle((float)cx, (float)cy, (float)cx * this.mTriggerPercentage, this.mPaint);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, int color, float pct) {
        this.mPaint.setColor(color);
        canvas.save();
        canvas.translate(cx, cy);
        float radiusScale = INTERPOLATOR.getInterpolation(pct);
        canvas.scale(radiusScale, radiusScale);
        canvas.drawCircle(0.0F, 0.0F, cx, this.mPaint);
        canvas.restore();
    }

    void setBounds(int left, int top, int right, int bottom) {
        this.mBounds.left = left;
        this.mBounds.top = top;
        this.mBounds.right = right;
        this.mBounds.bottom = bottom;
    }
}
