package com.orvibo.homemate.common.launch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.adapter.MyFragmentPagerAdapter;
import com.orvibo.homemate.sharedPreferences.AppCache;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装后首次打开app显示的引导界面
 * Created by Allen on 2015/4/21.
 */
public class LauncherGuideActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private List<OnFragmentVisibilityListener> onFragmentVisibilityListeners;
    private MyFragmentPagerAdapter adapter;
    private int lastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        fragments = new ArrayList<Fragment>();
        onFragmentVisibilityListeners = new ArrayList<OnFragmentVisibilityListener>();
        Launcher1Fragment launcher1Fragment = new Launcher1Fragment();
        Launcher2Fragment launcher2Fragment = new Launcher2Fragment();
        Launcher3Fragment launcher3Fragment = new Launcher3Fragment();
        Launcher4Fragment launcher4Fragment = new Launcher4Fragment();
        fragments.add(launcher1Fragment);
        fragments.add(launcher2Fragment);
        fragments.add(launcher3Fragment);
        fragments.add(launcher4Fragment);
        onFragmentVisibilityListeners.add(launcher1Fragment);
        onFragmentVisibilityListeners.add(launcher2Fragment);
        onFragmentVisibilityListeners.add(launcher3Fragment);
        onFragmentVisibilityListeners.add(launcher4Fragment);
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        onFragmentVisibilityListeners.get(position).onVisible();
        onFragmentVisibilityListeners.get(lastPosition).onInvisible();
        lastPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.saveAppVersion(this);
    }

    public interface OnFragmentVisibilityListener {
        void onVisible();

        void onInvisible();
    }
}
