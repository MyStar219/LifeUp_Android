package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.smartgateway.app.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Desc:
 * User: tiansj
 */
public class PasswordInputView extends EditText {
    
    private int textLength;

    private int passwordColor;
    private float passwordWidth;
    private float passwordRadius;

    private Paint passwordPaint = new Paint(ANTI_ALIAS_FLAG);

    public PasswordInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final Resources res = getResources();

        final int defaultPasswordColor = res.getColor(R.color.font_black);
        final float defaultPasswordWidth = res.getDimension(R.dimen.margin_x2);
        final float defaultPasswordRadius = res.getDimension(R.dimen.margin_x1);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
        try {
            passwordColor = a.getColor(R.styleable.PasswordInputView_passwordColor, defaultPasswordColor);
            passwordWidth = a.getDimension(R.styleable.PasswordInputView_passwordWidth, defaultPasswordWidth);
            passwordRadius = a.getDimension(R.styleable.PasswordInputView_passwordRadius, defaultPasswordRadius);
        } finally {
            a.recycle();
        }

        passwordPaint.setStrokeWidth(passwordWidth);
        passwordPaint.setStyle(Paint.Style.FILL);
        passwordPaint.setColor(passwordColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();


        // 密码
        float cx, cy = height/ 2;
        float half = passwordWidth / 2;
        for(int i = 0; i < textLength; i++) {
            cx = passwordWidth*(i+1) + half;
            canvas.drawCircle(cx, cy, passwordRadius, passwordPaint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.textLength = text.toString().length();
        invalidate();
    }

    public int getPasswordColor() {
        return passwordColor;
    }

    public void setPasswordColor(int passwordColor) {
        this.passwordColor = passwordColor;
        passwordPaint.setColor(passwordColor);
        invalidate();
    }

    public float getPasswordWidth() {
        return passwordWidth;
    }

    public void setPasswordWidth(float passwordWidth) {
        this.passwordWidth = passwordWidth;
        passwordPaint.setStrokeWidth(passwordWidth);
        invalidate();
    }

    public float getPasswordRadius() {
        return passwordRadius;
    }

    public void setPasswordRadius(float passwordRadius) {
        this.passwordRadius = passwordRadius;
        invalidate();
    }
}
