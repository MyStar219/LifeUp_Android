package com.smartgateway.app.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orvibo.homemate.common.ChildActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.common.SmartCommunityFragment;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.sg.DeviceFragment2;
import com.orvibo.homemate.user.LoginActivity;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.User.UserUtil;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

public class HomeMateFragment extends BaseAbstractFragment implements View.OnClickListener {

    @Override
    protected String getTitle(Resources res) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mate, container, false);
        initView(view);
        addDevicesFragment();
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.btnSmartCommunity).setOnClickListener(this);
        view.findViewById(R.id.smart_home).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSmartCommunity:
                goToFragment(SmartCommunityFragment.class);
                break;
            case R.id.smart_home:
                Credentials credentials = UserUtil.getCredentials(getContext());
                final DialogUtil dialogUtil = new DialogUtil(getContext());
                if (credentials == null) {
                    dialogUtil.showDissmissDialog(R.string.error, R.string.error);
                } else if (credentials.getMessage() != null) {
                    dialogUtil.showDissmissDialog(credentials.getMessage(), R.string.error);
                } else {
                    Intent toMainIntent = new Intent(getActivity(), MainActivity.class);
                    toMainIntent.putExtra(IntentKey.EVENT_LOAD_IMPORTANT_DATA_RESULT, "uid");
                    toMainIntent.putExtra(IntentKey.TO_MAIN_SOURCE, LoginActivity.class.getSimpleName());
                    startActivity(toMainIntent);
                }
                break;
        }
    }

    protected void goToFragment(Class klass) {
        BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
        if (null != activity) {
            Intent i = new Intent(activity, ChildActivity.class);
            i.putExtra(Constants.EXTRA_FRAGMENT, klass.getName());
            startActivity(i);
        }
    }

    private void addDevicesFragment() {
        FragmentManager fm = getChildFragmentManager();
        Fragment devicesFragment = fm.findFragmentByTag("devices_fragment");
        if (devicesFragment == null) {
            devicesFragment = new DeviceFragment2();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.home_devices_fragment, devicesFragment, "devices_fragment");
            ft.commit();
            fm.executePendingTransactions();
        }
    }
}
