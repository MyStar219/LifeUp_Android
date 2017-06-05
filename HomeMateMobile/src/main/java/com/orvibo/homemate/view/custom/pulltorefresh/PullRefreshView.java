package com.orvibo.homemate.view.custom.pulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.smartgateway.app.R;

/**
 * Created by yuwei on 2016/4/7.
 */
public class PullRefreshView extends ListView implements AbsListView.OnScrollListener {

    private Context mContext;
    private enum ListState {
        INIT_STATE(0), PULL_TO_REFRESH(1), RELEASE_TO_REFRESH(2), REFRESHING(3), FINISH(4),	CANCEL(5),REFRESH_FAIL(6);
        ListState(int ni) {
            this.nativeInt = ni;
        }
        final int nativeInt;
    }
    private ListState mCurrentState;
    private final float STEP_RATIO = 0.36f;
    private final float SCROLL_DIRECTION_STEP_UP = 200;
    private final float SCROLL_DIRECTION_STEP_DOWN = 10;
    private HeadView mHeadView;

    /**
     * 抢单次数
     */
    private int grabCount;

    /**
     * 刷新状态
     */
    private int refreshState;

    /**
     * 在首条position为0时记录第一个坐标点
     */
    private boolean mFirstPointRecorded = false;

    /**
     * 记录第一个按下点的位置
     */
    private int mFirstYPos;

    /**
     * 下拉控件的高度
     */
    private int mHeadViewHeight;

    /**
     * 刷新后的回调监听
     */
    private OnRefreshListener mOnRefreshListener;

    /**
     * FootView点击监听
     */
    private OnClickFootViewListener mFootViewListener;

    private LoadAndRetryBar mFootView;

    public boolean HAS_HEADER = true;

    public boolean HAS_FOOTER = true;

    /**
     * 是否可以下拉刷新, 多复用布局做适配
     */
    private boolean isCanPullRefresh = true;

    /**
     * 标识该列表是否有更新数据，即是否还需加载更多
     */
    private boolean hasMoreData = true;

    /**
     * 标识是否正在更新数据，即加载更多操作是否完成
     */
    private boolean isCanLoadMore = true;

    private boolean isNeedRetry = false;

    /**
     * 标识是否为自动刷新
     */
    private boolean mIsAutoLoading = true;

    // 监听上滑和下滑事件
    /**
     * 第一个按下的Y位置
     */
    private float mSDFirstY;

    /**
     * 第一个位置是否记录
     */
    private boolean mIsSDFirstPointRecorded = false;

    float x1, y1, x2, y2;// 滑动时，第一个down和up的xy坐标

    // /**
    // * 第一个位置是否已相应
    // */
    // private boolean mIsSDFirstPointResponsed = false;

    private OnScrollDirectionListener mScrollDirectionListener;

    /**实际的padding的距离与界面上偏移距离的比例 **/
    private final static int RATIO = 2;

    // 监听上滑和下滑事件

    // 复写SetOnScrollListener
    private OnScrollListener mOnScrollListener;

    /** 监听向上向下滑动 */
    private boolean mUpDownActionRecorded = false;
    private int mUpDownPositionY = -1;
    private OnUpDownListener mOnUpDownListener;
    /** 上拉开关 */
    private boolean mIsEnablePullUp = true;

    /**下拉的高度**/
    private  int step;

    /**
     * 是否显示下拉文字
     */
    private boolean isShowPullDownText;
    /**
     * 加载完成所有的数据，是否可以继续下拉刷新
     */
    private boolean isPullRefresh = true;

    public int getGrabCount() {
        return grabCount;
    }

    public void setGrabCount(int grabCount) {
        this.grabCount = grabCount;
    }

    public int getRefreshState() {
        return refreshState;
    }

    public void setRefreshState(int refreshState) {
        this.refreshState = refreshState;
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshState(){

    }

    public boolean ismIsEnablePullUp() {
        return mIsEnablePullUp;
    }

    public void setmIsEnablePullUp(boolean mIsEnablePullUp) {
        this.mIsEnablePullUp = mIsEnablePullUp;
    }

    public void setOnUpDownListener(OnUpDownListener listener) {
        mOnUpDownListener = listener;
    }

    /** 监听向上向下滑动 */

    public LoadAndRetryBar getFootView() {
        return mFootView;
    }


    public boolean isShowPullDownText() {
        return isShowPullDownText;
    }

    public void setShowPullDownText(boolean isShowPullDownText) {
        this.isShowPullDownText = isShowPullDownText;
    }

    @Deprecated
    private void setmFootView(LoadAndRetryBar mFootView) {
        this.mFootView = mFootView;
    }

    public boolean isHasMoreData() {
        return hasMoreData;
    }

    public void setHasMoreData(boolean hasMoreData) {
        this.hasMoreData = hasMoreData;
    }

    public boolean isHasHeader() {
        return HAS_HEADER;
    }

    public boolean isHasFooter() {
        return HAS_FOOTER;
    }

    public PullRefreshView(Context context) {
        super(context);
        initView(context);
    }

    public PullRefreshView(Context context, boolean hasHead, boolean hasFoot) {
        this(context);
        HAS_HEADER = hasHead;
        HAS_FOOTER = hasFoot;
    }

    public PullRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化header和footer
        initListViewProperty(context, attrs);
        initView(context);
    }

    public PullRefreshView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 初始化header和footer
        initListViewProperty(context, attrs);
        initView(context);
    }

    /**
     * 配置listview的属性
     *
     * @param context
     * @param attrs
     */
    private void initListViewProperty(Context context, AttributeSet attrs) {
        TypedArray arrayType = context.obtainStyledAttributes(attrs, R.styleable.pull_refresh_listview);
        HAS_HEADER = arrayType.getBoolean(R.styleable.pull_refresh_listview_has_header, true);
        HAS_FOOTER = arrayType.getBoolean(R.styleable.pull_refresh_listview_has_footer, true);
        isShowPullDownText = arrayType.getBoolean(R.styleable.pull_refresh_listview_show_text, true);
//        isPullRefresh = arrayType.getBoolean(R.styleable.pull_refresh_listview_has_pull_refresh, true);
        arrayType.recycle();
    }

    private void initView(Context context) {
        mContext = context;
        if (HAS_HEADER) {
            mHeadView = new HeadView(this.mContext,isShowPullDownText);
            addHeaderView(mHeadView, null, false);
            ViewUtils.measureView(mHeadView);
            mHeadViewHeight = mHeadView.getMeasuredHeight();
            /**
             * 隐藏mHeadView
             */
            mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);
            mHeadView.invalidate();
            mCurrentState = ListState.INIT_STATE;
        }
        if (HAS_FOOTER) {
            mFootView = new LoadAndRetryBar(mContext);
            mFootView.setOnRetryClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNeedRetry || !mIsAutoLoading) {
                        mFootView.showLoadingBar();
                        mFootViewListener.onClickFootView();
                        isCanLoadMore = false;
                    }
                }
            });
            addFooterView(mFootView, null, false);
        }
        super.setOnScrollListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Auto-generated method stub
        // TEST先屏蔽上下事件
        // if (mScrollDirectionListener != null) {
        // dealScrollDirectionEvent(ev);
        // }
        dealUpDownEvent(ev);
        if (HAS_HEADER && isCanPullRefresh) {
            dealTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 对向上滑动和向下滑动的监听
     *
     * @param event
     */
    private void dealUpDownEvent(MotionEvent event) {
        if (mOnUpDownListener == null) {
            return;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mUpDownPositionY == -1) {
                    mUpDownPositionY = (int) event.getY();
                }
                int diff = (int) (event.getY() - mUpDownPositionY);
                if (diff > 20) {
                    // move down
                    if (!mUpDownActionRecorded) {
                        if (mOnUpDownListener != null) {
                            mUpDownActionRecorded = true;
                            mOnUpDownListener.onScrollDown();
                        }
                    }
                } else if (diff < -20) {
                    // move up
                    if (!mUpDownActionRecorded) {
                        if (mOnUpDownListener != null) {
                            mUpDownActionRecorded = true;
                            if(mIsEnablePullUp)
                                mOnUpDownListener.onScrollUp();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mUpDownPositionY = (int) event.getY();
                mUpDownActionRecorded = false;
                break;
            case MotionEvent.ACTION_UP:
                mUpDownActionRecorded = false;
                mUpDownPositionY = -1;
                break;
        }
    }

    public void dealTouchEvent(MotionEvent event) {
        if(!isPullRefresh){
            return;
        }
        int y = (int) event.getY();
        step = 0;
        if (mFirstPointRecorded) {
            step = (int) ((y - mFirstYPos) * STEP_RATIO);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getFirstVisiblePosition() == 0 && !mFirstPointRecorded) {
                    mFirstYPos = y;
                    mFirstPointRecorded = true;
                    /**
                     * 取消侧边滑动条的显示
                     */
                    if (isVerticalFadingEdgeEnabled()) {
                        setVerticalScrollBarEnabled(false);
                    }
                    changeHeadState(ListState.INIT_STATE);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int tempY = (int) event.getY();
                /**
                 * 侧边栏的显示和隐藏
                 */
                if (getFirstVisiblePosition() == 0) {
                    if (isVerticalFadingEdgeEnabled()) {
                        setVerticalScrollBarEnabled(false);
                    }
                } else {
                    if (!isVerticalScrollBarEnabled()) {
                        setVerticalScrollBarEnabled(true);
                    }
                }

                if (getFirstVisiblePosition() == 0 && !mFirstPointRecorded) {
                    mFirstYPos = y;
                    mFirstPointRecorded = true;
                    changeHeadState(ListState.INIT_STATE);
                }

                if (mCurrentState != ListState.REFRESHING && mFirstPointRecorded) {

                    if (mCurrentState == ListState.RELEASE_TO_REFRESH) {
                        /**
                         * 保证在往上推的过程中，ListView不会滑动
                         */
                        setSelection(0);

                        if ((step < mHeadViewHeight) && step > 0) {
                            /**
                             * 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                             */
                            changeHeadState(ListState.PULL_TO_REFRESH);
                        } else if (step <= 0) {
                            /**
                             * 一下子推到顶了
                             */
                            changeHeadState(ListState.INIT_STATE);
                        }
                    }
                    /**
                     * 还没有到达显示松开刷新的时候,INIT_STATE或者是PULL_To_REFRESH状态
                     */
                    if (mCurrentState == ListState.PULL_TO_REFRESH) {
                        setSelection(0);

                        /**
                         * 下拉到可以进入RELEASE_TO_REFRESH的状态
                         */
                        if (step >= mHeadViewHeight + 5) {
                            changeHeadState(ListState.RELEASE_TO_REFRESH);
                        } else if (step <= 0) {
                            /**
                             * 上推到顶了
                             */
                            changeHeadState(ListState.INIT_STATE);
                        }
                    }

                    /**
                     * INIT_STATE状态下
                     */
                    if (mCurrentState == ListState.INIT_STATE) {
                        if (step > 0) {
                            changeHeadState(ListState.PULL_TO_REFRESH);
                        }
                    }

                    /**
                     * 更新headView的padding
                     */
                    if (mCurrentState == ListState.PULL_TO_REFRESH || mCurrentState == ListState.RELEASE_TO_REFRESH) {
                        changeHeadPadding(step);
                    }
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mCurrentState != ListState.REFRESHING && mFirstPointRecorded) {
                    if (mCurrentState == ListState.INIT_STATE) {
                        mFirstPointRecorded = false;
                    }

                    if (mCurrentState == ListState.PULL_TO_REFRESH) {
                        changeHeadState(ListState.CANCEL);
                        mFirstPointRecorded = false;
                    }
                    if (mCurrentState == ListState.RELEASE_TO_REFRESH) {
                        changeHeadState(ListState.REFRESHING);
                        if (mOnRefreshListener != null) {
                            mOnRefreshListener.onRefresh();
                        }
                    }
                }

                break;

            default:
                break;
        }
    }

    /**
     * 显示正在刷新的状态，并刷新数据
     */
    public void showRefreshingState() {
        changeHeadState(ListState.REFRESHING);
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }

    public void dealScrollDirectionEvent(MotionEvent event) {
        float y = event.getY();
        int step = 0;
        if (mIsSDFirstPointRecorded) {
            step = (int) ((y - mSDFirstY));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsSDFirstPointRecorded) {
                    mSDFirstY = y;
                    mIsSDFirstPointRecorded = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!mIsSDFirstPointRecorded) {
                    mSDFirstY = y;
                    mIsSDFirstPointRecorded = true;
                } else {
                    if (mScrollDirectionListener != null) {
                        if (step > SCROLL_DIRECTION_STEP_DOWN) {
                            mScrollDirectionListener.onScrollDown();
                        } else if (step < 0 - SCROLL_DIRECTION_STEP_UP) {
                            mScrollDirectionListener.onScrollUp();
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsSDFirstPointRecorded = false;
                break;

            default:
                break;
        }
    }

    private void changeHeadPadding(int step) {
        mHeadView.setPadding(0, step - mHeadViewHeight, 0, 0);
        mHeadView.setCircleProgress((int) (step * 100 / mHeadViewHeight));
    }

    private void changeHeadState(ListState state) {
        if (mCurrentState != ListState.INIT_STATE && mCurrentState == state) {
            return;
        }
        switch (state) {
            // ANDY 未开发完成
            case INIT_STATE:
                mHeadView.showInitState(refreshState ,grabCount);
                break;

            case PULL_TO_REFRESH:
                if (mCurrentState == ListState.RELEASE_TO_REFRESH) {
                    /**
                     *加载时显示反转动画
                     */
                    mHeadView.showPullState(true);
                } else {
                    mHeadView.showPullState(false);
                }
                break;

            case REFRESHING:
                mHeadView.setPadding(0, 0, 0, 0);
                mHeadView.showRefreshingState();
                break;

            case RELEASE_TO_REFRESH:
                mHeadView.showReleaseState();
                break;

            case FINISH:
                mHeadView.setResultStatus(mContext.getString(R.string.refresh_success), mHeadViewHeight, grabCount ,refreshState);
                break;

            case CANCEL:
                mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);
//              mHeadView.setPadding(0, -step, 0, 0);
                break;
            case REFRESH_FAIL:
                mHeadView.setResultStatus(mContext.getString(R.string.refresh_fail), mHeadViewHeight, grabCount ,refreshState);
                break;
        }
        mCurrentState = state;
    }

    /**
     * 在下拉刷新完成后更换状态
     */
    public void onRefreshComplete() {
        mFirstPointRecorded = false;
        changeHeadState(ListState.FINISH);
        invalidateViews();
        setSelection(0);
    }

    /**
     * 在下拉失败更换状态
     */
    public void onRefreshFail() {
        mFirstPointRecorded = false;
        changeHeadState(ListState.REFRESH_FAIL);
        invalidateViews();
        setSelection(0);
    }

    /**
     * 相应上滑和下滑事件
     *
     * @param listener
     */
    public void setOnScrollDirectionListener(OnScrollDirectionListener listener) {
        mScrollDirectionListener = listener;
    }

    /**
     * Register a callback to be invoked when this list should be refreshed.
     *
     * @param onRefreshListener The callback to run.
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void setOnClickFootViewListener(OnClickFootViewListener listener) {
        mFootViewListener = listener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (HAS_FOOTER) {
            int emptyCount = getHeaderViewsCount() + getFooterViewsCount();
            int mRemainItem = totalItemCount - firstVisibleItem - visibleItemCount;
            if ((mRemainItem == 0) && (totalItemCount > emptyCount) && (isCanLoadMore) && (hasMoreData) && (HAS_FOOTER) && mIsAutoLoading) {
                if (mFootViewListener != null) {
                    mFootViewListener.onClickFootView();
                }
                isCanLoadMore = false;
            }
        }
        globalFirstVisibleItem = firstVisibleItem;

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private int globalFirstVisibleItem;
    public int getGlobalFirstVisibleItem(){
        return globalFirstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void onRefresh() {
        if (mOnRefreshListener != null) {
            if (HAS_HEADER) {
                mOnRefreshListener.onRefresh();
            }
        }
    }

    @Deprecated
    private void setFootViewAddMore() {
        isCanLoadMore = true;
        if (this.getFooterViewsCount() > 0) {

        } else {
            this.addFooterView(mFootView, null, false);
        }
        if (!hasMoreData) {
            try {
                this.removeFooterView(mFootView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置下拉刷新的开关
     *
     * @param isPullRefresh true:可以下拉刷新  false:不能下拉刷新
     */
    public void setHeadViewRefresh(boolean isPullRefresh) {
        this.isPullRefresh = isPullRefresh;
    }
    /**
     * 设置加载更多条的状态
     *
     * @param isAutoLoading true-自动加载更多，false-手动加载
     * @param hasMoreData true-有更多数据，false-无更多数据
     * @param isNeedRetry true-需要点击刷新（用于网络访问未成功时的提示）
     */
    public void setFootViewAddMore(boolean isAutoLoading, boolean hasMoreData, boolean isNeedRetry) {
        if (HAS_FOOTER) {
            isCanLoadMore = true;
            this.hasMoreData = hasMoreData;
            this.isNeedRetry = isNeedRetry;
            if (this.getFooterViewsCount() > 0) {
            } else {
                this.addFooterView(mFootView, null, false);
            }
            if (isNeedRetry) {
                mFootView.showRetryStatus();
                isCanLoadMore = false;
            } else {
                if (!hasMoreData) {
                    try {
                        this.removeFooterView(mFootView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!isAutoLoading) {
                        mFootView.showRetryStatus();
                    } else {
                        mFootView.showLoadingBar();
                    }
                    if (this.getFooterViewsCount() > 0) {
                    } else {
                        this.addFooterView(mFootView, null, false);
                    }
                }
            }
        }
    }

    public void setFootViewLoading() {
        mFootView.showLoadingBar();
    }

    /**
     * Interface definition for a callback to be invoked when list should be
     * refreshed.
     */
    public interface OnRefreshListener {

        /**
         * Called when the list should be refreshed.
         * <p>
         * A call to { PullRefreshViewcopy #onRefreshComplete()} is expected to
         * indicate that the refresh has completed.
         */
        public void onRefresh();
    }

    /**
     * 在一个完整手势中，相应一次方向判断
     *
     * @author boyang
     */
    public interface OnScrollDirectionListener {
        public void onScrollUp();

        public void onScrollDown();
    }

    public interface OnClickFootViewListener {

        public void onClickFootView();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (IndexOutOfBoundsException e) {
            // e.printStackTrace();
        }

    }


    public boolean isAutoLoading() {
        return mIsAutoLoading;
    }

    /**
     * 设置是否自动加载更多
     *
     * @param isAutoLoading
     */
    public void setAutoLoading(boolean isAutoLoading) {
        this.mIsAutoLoading = isAutoLoading;
        if (isAutoLoading) {
            mFootView.showLoadingBar();
        } else {
            mFootView.showRetryStatus();
        }
    }

    public void showListLoading() {
        changeHeadState(ListState.REFRESHING);
    }

    public void setListAdapter(BaseAdapter adapter) {
        setAdapter(adapter);
    }

    @Override
    public void setAdapter(ListAdapter adpter) {
        super.setAdapter(adpter);
    }

    public void showLoadingMore() {
        mFootView.showLoadingBar();
    }

    public boolean isCanPullRefresh() {
        return isCanPullRefresh;
    }

    public void setCanPullRefresh(boolean isCanPullRefresh) {
        this.isCanPullRefresh = isCanPullRefresh;
    }
}