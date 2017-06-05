package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.dao.MessageDao;
import com.orvibo.homemate.sharedPreferences.UserCache;

/**
 * Created by smagret on 2015/8/24.
 */
public class InfoPushTopView extends RelativeLayout {

    private String mText;
    private TextView infoTextView;
    private ImageView infoImageView;
    private int infoPushcount;
    /**
     * 0 绿色；1白色
     */
    private int mTypeBg;
    private AnimationDrawable animationDrawable;

    public InfoPushTopView(Context context) {
        super(context);
    }

    public InfoPushTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public InfoPushTopView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }


    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.info_push_top_view, this, true);
        infoImageView = (ImageView) findViewById(R.id.iconImageView);
        infoTextView = (TextView) findViewById(R.id.productNameTextView);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.InfoPushTopView);
        this.mTypeBg = a.getInteger(R.styleable.InfoPushTopView_typeBg, 0);

//        if (mTypeBg == 0) {
//            infoImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.bg_info_push));
//        } else if (mTypeBg == 1) {
//            infoImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.bg_info_push_white_selector));
//        }
        a.recycle();
    }

    /**
     * 设置消息提醒个数，如果大于99则显示99+
     * @param infoPushcount
     */
    public void setInfoPushcount(int infoPushcount) {
        this.infoPushcount = infoPushcount;
        setText(infoPushcount + "");
        if (infoTextView != null) {
            if (infoPushcount > 99) {
                infoTextView.setText(getResources().getString(R.string.max_message_count));
            } else {
                infoTextView.setText(infoPushcount + "");
            }

        }

    }

    public void startInfoPushAnimation() {
        if (infoImageView != null) {
            infoImageView.setImageResource(R.drawable.info_push_animation);
            animationDrawable = (AnimationDrawable) infoImageView.getDrawable();
            animationDrawable.stop();
            animationDrawable.start();
        }
    }

    /**
     * 显示消息推送个数
     */
    public void setInfoPushCountVisible() {
        if (infoImageView.getVisibility() == View.VISIBLE) {
            infoTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏消息推送个数
     */
    public void setInfoPushCountInvisible() {
        infoTextView.setVisibility(View.GONE);
    }

    /**
     * 显示消息推送图标，如果消息数量大于0，则显示消息个数
     * @param context
     */
    public void setInfoPushVisible(Context context) {
//        infoPushcount = InfoPushCountCache.getInfoPushCount(context, UserCache.getCurrentUserId(context));
        MessageDao messageDao = new MessageDao();
        infoPushcount = messageDao.selUnreadCount(UserCache.getCurrentUserId(ViHomeApplication.getAppContext()));
        infoImageView.setVisibility(View.VISIBLE);
        if (infoPushcount > 0) {
            infoTextView.setVisibility(View.VISIBLE);
        } else {
            infoTextView.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏消息推送图标和数字
     */
    public void setInfoPushInvisible() {
        infoTextView.setVisibility(View.GONE);
        infoImageView.setVisibility(View.GONE);
    }
}
