package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;

import org.w3c.dom.Text;

/**
 * Created by wuliquan on 2016/7/19.
 * ImageView按下去的效果改变
 */
public class TouchImageView extends TextView {

    private int normalDrawableSrcId;
    private int pressDrawableSrcId;
    public TouchImageView(Context context) {
        this(context,null);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);

    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TouchImageView, defStyleAttr, 0);
        normalDrawableSrcId = a.getResourceId(R.styleable.TouchImageView_normalDrawable,R.drawable.icon_green_setting_normal);
        pressDrawableSrcId  = a.getResourceId(R.styleable.TouchImageView_pressDrawable,R.drawable.icon_green_setting_pressed);

        setImageResource(normalDrawableSrcId,getResources().getColor(R.color.black));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                setImageResource(pressDrawableSrcId,getResources().getColor(R.color.green));
                setBackgroundColor(getResources().getColor(R.color.coco_item_gray));
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setImageResource(normalDrawableSrcId,getResources().getColor(R.color.black));
                setBackgroundColor(getResources().getColor(R.color.white));
                    break;
        }
        return super.onTouchEvent(event);
    }

    private void setImageResource(int drawableId,int color){

        Drawable drawable= getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        setCompoundDrawables(null,drawable,null,null);
        setTextColor(color);
    }
}
