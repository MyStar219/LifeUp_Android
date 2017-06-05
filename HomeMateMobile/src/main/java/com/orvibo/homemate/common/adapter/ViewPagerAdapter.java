package com.orvibo.homemate.common.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

	private List<View> pageViews;

	public ViewPagerAdapter(List<View> pageViews) {
		super();
		this.pageViews = pageViews;
	}

	@Override
	public int getCount() {
		return pageViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(pageViews.get(arg1));
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(pageViews.get(arg1));
//		if (arg1 == 0) {
//			pageViews.get(arg1).setBackgroundResource(R.drawable.guide_pic_bg_1);
//		} else if (arg1 == 1) {
//			pageViews.get(arg1).setBackgroundResource(R.drawable.guide_pic_bg_2);
//		} else if (arg1 == 2) {
//			pageViews.get(arg1).setBackgroundResource(R.drawable.guide_pic_bg_3);
//		} else if (arg1 == 3) {
//			pageViews.get(arg1).setBackgroundResource(R.drawable.guide_pic_bg_4);
//		}
		return pageViews.get(arg1);
	}
}