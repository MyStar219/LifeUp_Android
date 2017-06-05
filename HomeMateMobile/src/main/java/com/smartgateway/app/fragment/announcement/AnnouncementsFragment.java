package com.smartgateway.app.fragment.announcement;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.readystatesoftware.viewbadger.BadgeView;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.R;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;

import java.util.Locale;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.fragment.PagerFragment;

/**
 * AnnouncementsFragment
 * Created by yanyu on 5/17/2016.
 */
public class AnnouncementsFragment extends PagerFragment {
    protected static class AnnouncementListFactory implements FragmentFactory {
        int mode;

        public AnnouncementListFactory(int mode) {
            this.mode = mode;
        }

        @Override
        public BaseAbstractFragment createFragment() {
            AnnouncementsListFragment fragment = new AnnouncementsListFragment();
            fragment.addParam(Constants.PARAM_MODE, mode);
            return fragment;
        }
    }

    @Override
    protected TabDescriptor[] getTabDescriptors() {
        String titleCondo = getResources().getString(R.string.condo_bulletin);
        String titleSystem = getResources().getString(R.string.system_announce);
        User.Notification notification = UserUtil.loadFeedback(getActivity());
        int condo = notification.getCondo();
        int system = notification.getSystem();
        if (condo > 0) {
            titleCondo = String.format(Locale.US, "%s(%d)", titleCondo, condo);
        }
        if (system > 0) {
            titleSystem = String.format(Locale.US, "%s(%d)", titleSystem, system);
        }
        return new TabDescriptor[]{
                new TabDescriptor(titleCondo, new AnnouncementListFactory(0)),
                new TabDescriptor(titleSystem, new AnnouncementListFactory(1)),
        };
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.fragment_announcements);
    }
//    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ViewGroup view = (ViewGroup)super.createView(inflater, container, savedInstanceState);
//        User.Notification notification = UserUtil.loadFeedback(getActivity());
//        AppBarLayout toolbar = (AppBarLayout)view.getChildAt(0);
//        TabLayout tab = (TabLayout)toolbar.getChildAt(1);
//        ViewGroup group = (ViewGroup)tab.getChildAt(0);
//        ViewGroup firstTab = (ViewGroup)group.getChildAt(0);
//        ViewGroup secondTab = (ViewGroup)group.getChildAt(1);
//
//        BadgeView badgeView1 = new BadgeView(getActivity(), firstTab.getChildAt(1));
//        badgeView1.setText(notification.getCondo() + "");
//        badgeView1.show(notification.getCondo() > 0);
//        badgeView1.setBadgeMargin(0);
//        badgeView1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//
//        BadgeView badgeView2 = new BadgeView(getActivity(), secondTab.getChildAt(1));
//        badgeView2.setText(notification.getSystem() + "");
//        badgeView2.show(notification.getSystem() > 0);
//        badgeView2.setBadgeMargin(0);
//        badgeView2
//                .setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//
//        return view;
//    }
}
