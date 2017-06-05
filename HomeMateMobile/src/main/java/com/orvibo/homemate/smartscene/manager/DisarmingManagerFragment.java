package com.orvibo.homemate.smartscene.manager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orvibo.homemate.data.ArmType;

/**
 * Created by zhaoxiaowei on 2016/3/7.
 */
public class DisarmingManagerFragment extends SecurityManagerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onVisible() {
        mArmType = ArmType.DISARMING;
        super.onVisible();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setListViewHeightBasedOnChildren(ListView listView) {
        super.setListViewHeightBasedOnChildren(listView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
