package com.smartgateway.app.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by yanyu on 5/15/2016.
 */
public class ChildActivity extends BaseMainActivity {

    @NonNull
    @Override
    protected BaseAbstractFragment createInitialFragment() {
        String fragmentClassName = getIntent().getStringExtra(Constants.EXTRA_FRAGMENT);
        if (fragmentClassName == null) {
            throw new IllegalArgumentException("Intent should contain scting extra Constants.EXTRA_FRAGMENT");
        }
        return (BaseAbstractFragment) Fragment.instantiate(this, fragmentClassName);
    }

    @Override
    protected boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    @Override
    protected void onDetailReturned(String detail, int responseCode) {
        DialogUtil.showDetailErrorAlert(this, detail, responseCode);
    }

    @Override
    protected void onLogoutEvent() {
        new LifeUpProvidersHelper().signOut(this);
    }
}
