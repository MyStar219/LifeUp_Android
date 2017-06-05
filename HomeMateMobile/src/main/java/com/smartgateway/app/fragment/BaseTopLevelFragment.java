package com.smartgateway.app.fragment;

import android.content.Intent;
import android.util.SparseArray;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.activity.ChildActivity;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.fragment.BaseListFragment;

/**
 * Created by yanyu on 5/16/2016.
 */
public abstract class BaseTopLevelFragment<T extends AbstractData> extends BaseListFragment<T> {
    protected void goTo(int id) {
        Class<? extends BaseAbstractFragment> mapping = getMapper().get(id);
        if (mapping == null) return;
        BaseAbstractFragment fragment = (BaseAbstractFragment) BaseAbstractFragment.instantiate(getContext(), mapping.getName());
        goTo(fragment);
    }

    protected abstract SparseArray<Class<? extends BaseAbstractFragment>> getMapper();

    private void goTo(BaseAbstractFragment fragment) {
        BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
        if (null != activity) {
            Intent i = new Intent(activity, ChildActivity.class);
            i.putExtra(Constants.EXTRA_FRAGMENT, fragment.getClass().getName());
            startActivity(i);
        }
    }
}
