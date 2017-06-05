package com.orvibo.homemate.view.custom.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * Created by yuwei on 2016/4/7.
 */
public class LoadAndRetryBar extends RelativeLayout {

    private Context mContext;

    private LinearLayout mLoadingLayout;
    private TextView mRetryText;

    private OnClickListener mRetryClickListener;

    public LoadAndRetryBar(Context context) {
        super(context);
        initView(context);
    }

    public LoadAndRetryBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadAndRetryBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.refresh_footer, this, true);
        mLoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        mRetryText = (TextView) findViewById(R.id.retry_textview);

        mLoadingLayout.setVisibility(View.GONE);
        mRetryText.setVisibility(View.INVISIBLE);
        mRetryText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mRetryClickListener != null) {
                    mRetryClickListener.onClick(v);
                }
            }
        });
    }

    public void showLoadingBar() {
        mLoadingLayout.setVisibility(View.VISIBLE);
        mRetryText.setVisibility(View.INVISIBLE);
    }

    public void showRetryStatus() {
        mLoadingLayout.setVisibility(View.GONE);
        mRetryText.setVisibility(View.VISIBLE);
    }

    public void setOnRetryClickListener(OnClickListener listener) {
        mRetryClickListener = listener;
    }

}