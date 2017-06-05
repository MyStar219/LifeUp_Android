package com.orvibo.homemate.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{
	
	private List<Fragment> fragments;

	public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	@Override  
    public int getCount() {  
        return fragments.size();  
    }  

    //得到每个item  
    @Override  
    public Fragment getItem(int position) {  
        return fragments.get(position);  
    }  

}
