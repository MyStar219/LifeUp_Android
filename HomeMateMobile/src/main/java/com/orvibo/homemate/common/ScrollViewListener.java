package com.orvibo.homemate.common;


import com.orvibo.homemate.view.custom.ObservableScrollView;

/**
 * @author zhaoxiaowei
 * @date 2016/01/07
 * ScrollView 滑动监听
 */
public interface ScrollViewListener {

    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);

}
