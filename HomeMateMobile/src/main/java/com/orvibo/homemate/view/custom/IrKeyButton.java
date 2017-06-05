package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.Constant;

/**
 * @author Smagret
 * @date 2016/03/22
 * Allone2 按键自定义控件
 */

public class IrKeyButton extends TextView {
    private static final String TAG = IrKeyButton.class.getSimpleName();

    private Context mContext;


    private AlloneControlData controlData;

    private boolean matched = false;
    /**
     * 该按键是否被学习
     */
    private boolean isLearned = false;

    /**
     * 进入学习状态图片
     */
    private Drawable learnBg;//学习中的背景
    private Drawable learnTopBg;//学习中的textview上top背景
    private Drawable learnedBg;//学习后背景
    private Drawable learnedTopBg;//学习后textview上top背景

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    private int fid;
    private KKIr kkIr;

    public IrKeyButton(Context context) {
        super(context);
        mContext = context;
    }


    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public IrKeyButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs);
    }

    public IrKeyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs,
                R.styleable.IrKeyButton);
        //update by yuwei(根据属性singleLine参数来设置是否是只有一行)
        boolean isSingleLine = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "singleLine", true);
        if (isSingleLine) {
            setSingleLine();
            setEllipsize(TextUtils.TruncateAt.END);
        }
        fid = a.getInteger(R.styleable.IrKeyButton_fid, Constant.INVALID_NUM);
        learnBg = a.getDrawable(R.styleable.IrKeyButton_learnBg);
        learnTopBg = a.getDrawable(R.styleable.IrKeyButton_learnTopBg);
        learnedBg = a.getDrawable(R.styleable.IrKeyButton_learnedBg1);
        learnedTopBg = a.getDrawable(R.styleable.IrKeyButton_learnedTopBg);
        //update by yuwei(根据属性clickable来设置是否可点击)
        boolean isClickable = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "clickable", true);
        if (isClickable)
            setClickable(true);
        a.recycle();
    }


    /**
     * 设置按钮是否有匹配的红外码
     *
     * @param matched
     */
    public void setMatched(boolean matched) {
        Drawable bg = getBackground();
        Drawable topDrawable = getCompoundDrawables()[1];
        this.matched = matched;
        if (learnedBg != null) {
            setBackgroundDrawable(learnedBg);
        } else if (learnedTopBg != null) {
            setCompoundDrawablesWithIntrinsicBounds(null, learnedTopBg, null, null);
        }
        if (matched) {
            if (learnedBg != null) {
                learnedBg.setAlpha(255);
                setTextColor(getResources().getColor(R.color.green));
            }
            if (learnedTopBg != null) {
                learnedTopBg.setAlpha(255);
            }
            if (bg != null) {
                bg.setAlpha(255);
            }
            if (topDrawable != null) {
                topDrawable.setAlpha(255);
            }
        } else {
            if (learnedBg != null) {
                learnedBg.setAlpha(76);
                setTextColor(getResources().getColor(R.color.font_white_gray));
            }
            if (learnedTopBg != null) {
                learnedTopBg.setAlpha(76);
            }
            if (bg != null) {
                bg.setAlpha(76);
            }
            if (topDrawable != null) {
                topDrawable.setAlpha(76);
            }
        }
    }

    /**
     * 设置按钮是否有匹配的红外码,不更新界面试图，适于用固定按钮的空调界面
     *
     * @param matched
     */
    public void setOnlyMatched(boolean matched) {
        this.matched = matched;
    }

    /**
     * 设置按钮是否有匹配的红外码
     */
    public void setStartLearn(boolean isStartLearn) {
        if (isStartLearn) {
            if (!isLearned) {
                if (learnBg != null)
                    setBackgroundDrawable(learnBg);
                else {
                    setCompoundDrawablesWithIntrinsicBounds(null, learnTopBg, null, null);
                }
            } else {
                if (learnedBg != null) {
                    setBackgroundDrawable(learnedBg);
                    learnedBg.setAlpha(255);
                } else {
                    setCompoundDrawablesWithIntrinsicBounds(null, learnedTopBg, null, null);
                    learnedTopBg.setAlpha(255);
                }
                setClickable(true);
            }
        } else {
            if (learnedBg != null) {
                setBackgroundDrawable(learnedBg);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, learnedTopBg, null, null);
            }
            if (!isLearned) {
                if (learnedBg != null) {
                    learnedBg.setAlpha(76);
                    setTextColor(getResources().getColor(R.color.font_white_gray));
                } else {
                    learnedTopBg.setAlpha(76);
                }
                setClickable(false);
            } else {
                if (learnedBg != null) {
                    learnedBg.setAlpha(255);
                    setTextColor(getResources().getColor(R.color.green));
                } else {
                    learnedTopBg.setAlpha(255);
                }
                setClickable(true);
            }
        }

    }


    public boolean isMatched() {
        return matched;
    }

    public KKIr getKkIr() {
        return kkIr;
    }

    public void setKkIr(KKIr kkIr) {
        this.kkIr = kkIr;
    }

    public AlloneControlData getControlData() {
        return controlData;
    }

    public void setControlData(AlloneControlData controlData) {
        this.controlData = controlData;
    }
}

