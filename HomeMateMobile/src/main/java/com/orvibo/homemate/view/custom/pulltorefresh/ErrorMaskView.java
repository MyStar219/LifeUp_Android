package com.orvibo.homemate.view.custom.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import com.smartgateway.app.R;

import org.apache.commons.lang.StringUtils;

/**
 * Created by yuwei on 2016/4/7.
 */
public class ErrorMaskView extends RelativeLayout implements OnClickListener {
    private static final String TAG = "ErrorMaskView";
    private Context mContext;

    private LinearLayout mTextLayout;
    private ImageView mIconImage;
    private TextView mTitleText;
    private ImageView null_bg;
    private TextView mSubTitleText;
    private TextView mRetryTitleText;

    private LinearLayout mProgressLayout;
    private TextView mProgressText;

    private static final int STATUS_GONE = 0;
    private static final int STATUS_EMPTY = 1;
    private static final int STATUS_LOADING = 2;
    private static final int STATUS_ERROR = 3;

    private int mStatus;

    private OnClickListener mRetryClickListener;

    public ErrorMaskView(Context context) {
        super(context);
        initView(context);
    }

    public ErrorMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ErrorMaskView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.vm_mask_layout, this);
        mTextLayout = (LinearLayout) findViewById(R.id.textLayout);
        mIconImage = (ImageView) findViewById(R.id.icon);
        mTitleText = (TextView) findViewById(R.id.title);
        null_bg = (ImageView) findViewById(R.id.null_bg);
        mSubTitleText = (TextView) findViewById(R.id.subTitle);
        mRetryTitleText = (TextView) findViewById(R.id.retryTitle);
        mProgressLayout = (LinearLayout) findViewById(R.id.progressLayout);
        mProgressText = (TextView) findViewById(R.id.progressTitle);
        hide();
        mRetryTitleText.setOnClickListener(this);
        //mIconImage.setOnClickListener(this);
        //mTitleText.setOnClickListener(this);
    }

    public void setErrorStatus(String errorTitle, String errorSubTitle) {
        show();
        mProgressLayout.setVisibility(View.GONE);
        mTextLayout.setVisibility(View.VISIBLE);
        mIconImage.setImageBitmap(DefaultImageTools.getNoticeErrorBitmap(mContext));
        if (StringUtils.isNotBlank(errorTitle)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(errorTitle);
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        if (StringUtils.isNotBlank(errorSubTitle)) {
            mSubTitleText.setVisibility(View.VISIBLE);
            mSubTitleText.setText(errorSubTitle);
        } else {
            mSubTitleText.setVisibility(View.GONE);
        }
        mRetryTitleText.setVisibility(View.VISIBLE);
        mStatus = STATUS_ERROR;
    }

    public void setErrorStatus() {
        String errorTitle = mContext.getString(R.string.SOCKET_DISCONNECT);
        String errorSubTitle = mContext.getString(R.string.SOCKET_DISCONNECT);
        setErrorStatus(errorTitle, errorSubTitle);
    }

    public void setErrorStatus(int resId) {
        String errorTitle = mContext.getString(resId);
        setErrorStatus(errorTitle);
    }

    public void setErrorStatus(String errorTitle) {
        setErrorStatus(errorTitle, null);
    }

    public void setLoadingStatus(int resId) {
        String loadingText = mContext.getString(resId);
        setLoadingStatus(loadingText);
    }

    public void setLoadingStatus(String loadingText) {
        show();
        mProgressLayout.setVisibility(View.VISIBLE);
        mTextLayout.setVisibility(View.GONE);

        if (StringUtils.isNotBlank(loadingText)) {
            mProgressText.setVisibility(View.VISIBLE);
            mProgressText.setText(loadingText);
        } else {
            mProgressText.setVisibility(View.GONE);
        }
        mStatus = STATUS_LOADING;
    }

    public void setLoadingStatus() {
        setLoadingStatus(mContext.getString(R.string.loading));
    }

    public void setEmptyStatus(int resId) {
        String emptyText = mContext.getString(resId);
        setEmptyStatus(emptyText);
    }

    public void setEmptyStatus(String emptyText) {
        show();
        mProgressLayout.setVisibility(View.GONE);
        mTextLayout.setVisibility(View.VISIBLE);
        mSubTitleText.setVisibility(View.GONE);
        mIconImage.setImageBitmap(DefaultImageTools.getNoticeEmptyBitmap(mContext));
        if (StringUtils.isNotBlank(emptyText)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(emptyText);
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        null_bg.setVisibility(View.VISIBLE);
        mRetryTitleText.setVisibility(View.GONE);
        mStatus = STATUS_EMPTY;
    }

    public void setEmptyKindness(int backgroupColor,String emptyText,int emptyIconRes){
        show();
        this.setBackgroundColor(mContext.getResources().getColor(backgroupColor));
        mProgressLayout.setVisibility(View.GONE);
        mTextLayout.setVisibility(View.VISIBLE);
        mSubTitleText.setVisibility(View.GONE);
        mIconImage.setImageResource(emptyIconRes);
        if (StringUtils.isNotBlank(emptyText)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(emptyText);
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        null_bg.setVisibility(View.INVISIBLE);
        mRetryTitleText.setVisibility(View.GONE);
        mStatus = STATUS_EMPTY;
    }

    public void setEmptyStatus(int topResId, int bottomResId) {
        show();
        String topTxt = mContext.getString(topResId);
        String bottomTxt = mContext.getString(bottomResId);
        mProgressLayout.setVisibility(View.GONE);
        mTextLayout.setVisibility(View.VISIBLE);
        mSubTitleText.setVisibility(View.GONE);
        mIconImage.setImageBitmap(DefaultImageTools.getNoticeEmptyBitmap(mContext));
        if (StringUtils.isNotBlank(topTxt)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(topTxt);
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        if (StringUtils.isNotBlank(bottomTxt)) {
            mSubTitleText.setVisibility(View.VISIBLE);
            mSubTitleText.setText(bottomTxt);
        } else {
            mSubTitleText.setVisibility(View.GONE);
        }
        mRetryTitleText.setVisibility(View.GONE);
        mStatus = STATUS_EMPTY;
    }

    public void setEmptyStatus() {
        setEmptyStatus(mContext.getString(R.string.empty_list));
    }

    public void setVisibleGone() {
        hide();
    }

    private void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
    }

    private void hide() {
        if (getVisibility() != View.GONE) {
            setVisibility(View.GONE);
        }
        mStatus = STATUS_GONE;
    }

    /**
     * 在出现错误时，点击回调
     *
     * @param listener
     */
    public void setOnRetryClickListener(OnClickListener listener) {
        mRetryClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v == null || mStatus != STATUS_ERROR || mRetryClickListener == null) {
            if (mStatus == STATUS_EMPTY) {
                setLoadingStatus();
                if (R.id.icon == v.getId() || R.id.title == v.getId()) {
                    mRetryClickListener.onClick(v);
                }
            }
            return;
        }

        setLoadingStatus();
        if (R.id.retryTitle == v.getId() || R.id.icon == v.getId()) {
            mRetryClickListener.onClick(v);
        }
    }
}
