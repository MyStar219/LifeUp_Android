package com.smartgateway.app.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by MDev on 10/14/16.
 *
 */

public class AvenirRomanTextView extends TextView {
    public AvenirRomanTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AvenirRomanTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvenirRomanTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/avenir-roman.ttf");
        setTypeface(tf);
    }
}
