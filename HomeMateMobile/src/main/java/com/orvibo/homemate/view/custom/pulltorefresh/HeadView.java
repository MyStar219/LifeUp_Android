package com.orvibo.homemate.view.custom.pulltorefresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuwei on 2016/4/7.
 */
public class HeadView extends LinearLayout{
    /**
     * TAG
     */
    private final String TAG = "PullHeadView";

    //取消帧动画
    //private AnimationDrawable animationDrawable;
    /**
     * header view
     */
    private View mHeaderView;
    /**
     * header view image
     */
    private ImageView mHeaderImageView;
    /**
     * header tip text
     */
    private TextView mHeaderTextView;
    /**
     * header refresh time
     */
    private TextView mHeaderUpdateTextView;
    /**
     * header progress bar
     */
    private ImageView mHeaderProgressBar;

    private SimpleDateFormat dateFormat;
    private String lastRefreshTime;


    /**
     * 刷新状态
     */
    public final static int REFRESH_GRAB = 0X0001;

    /**
     * 上下文对象
     */
    private Context mContext;
    /**
     * 是否显示下拉文字
     */
    private boolean isShowPullDownText;

    public HeadView(Context context) {
        super(context);
        initView(context);
    }

    public HeadView(Context context,boolean isShowPullDownText) {
        super(context);
        this.isShowPullDownText = isShowPullDownText;
        initView(context);
    }

    public HeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    private void initView(Context context) {
        // header view
        this.mContext = context;
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.refresh_header, this, true);
        mHeaderImageView = (ImageView) mHeaderView.findViewById(R.id.pull_to_refresh_image);
        mHeaderTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_text);
        if(!isShowPullDownText){
            mHeaderTextView.setVisibility(View.GONE);
        }
        mHeaderUpdateTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_updated_at);
        mHeaderProgressBar = (ImageView) mHeaderView.findViewById(R.id.pull_to_refresh_progress);
        //取消帧动画
        //animationDrawable = (AnimationDrawable) this.mHeaderProgressBar.getBackground();
        dateFormat = new SimpleDateFormat("HH:mm");//时间格式
        lastRefreshTime = mContext.getString(R.string.last_refresh_time,dateFormat.format(new Date()));
        this.mHeaderProgressBar.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        //取消帧动画
                        //animationDrawable.start();
                        return true;
                    }
                });
    }

    /**
     * 初始化的展示
     * @param refreshState :刷新状态
     * @param grabCount    :今日抢单数
     */
    public void showInitState(int refreshState, int grabCount) {
        if(refreshState == REFRESH_GRAB){
            mHeaderUpdateTextView.setText(String.valueOf(grabCount));
        } else {
            mHeaderUpdateTextView.setText(lastRefreshTime);
        }
        mHeaderImageView.setVisibility(View.VISIBLE);
        mHeaderProgressBar.setVisibility(GONE);
    }

    /**
     * 设置刷新结果的状态
     * @param  grab_count   :今日抢单次数
     * @param  refreshState :刷新状态
     */
    public void setResultStatus(String str,final int mHeadViewHeight,int grab_count,int refreshState) {
        mHeaderTextView.setText(str);
        if(refreshState == REFRESH_GRAB)
            mHeaderUpdateTextView.setText(String.valueOf(grab_count));
/*        else
            mHeaderUpdateTextView.setText(dateFormat.format(new Date()));*/
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                setPadding(0, -1 * mHeadViewHeight, 0, 0);
                lastRefreshTime = mContext.getString(R.string.last_refresh_time,dateFormat.format(new Date()));
            }
        }, 1000);
    }

    /**
     * 显示正在刷新的状态
     */
    public void showRefreshingState() {
        mHeaderProgressBar.setVisibility(View.VISIBLE);
        ((AnimationDrawable)mHeaderImageView.getDrawable()).start();
        mHeaderTextView.setText(getString(R.string.pull_down_to_refreshing));
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    /**
     * 显示下拉刷新的状态
     * @param animate
     */
    public void showPullState(boolean animate) {
        mHeaderTextView.setText(getString(R.string.pull_down_to_refresh));
    }

    /**
     * 显示松开刷新的状态
     * @param
     */
    public void showReleaseState() {
        mHeaderTextView.setText(getString(R.string.pull_down_to_refresh_release));
    }

    /**
     * 设置下拉时的进度
     *
     * @param progress
     */
    public void setCircleProgress(int progress) {
    }
}
