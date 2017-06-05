package ru.johnlife.lifetools.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * BaseMainActivity
 * Created by yanyu on 5/4/2016.
 */
public abstract class BaseMainActivity extends BaseActivity {
    public interface BackHandler {
        /**
         * @return <code>true</code> if back was handled and no processing should follow, <code>false</code> otherwise
         */
        boolean handleBack();
    }

    private final BackHandler currentFragmentBackHandler = new BackHandler() {
        @Override
        public boolean handleBack() {
            if (currentFragment != null && (currentFragment instanceof BackHandler)) {
                return ((BackHandler) currentFragment).handleBack();
            }
            return false;
        }
    };

    private final BackHandler fragmentBackStackHandler = new BackHandler() {
        @Override
        public boolean handleBack() {
            final FragmentManager manager = fragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStack();
                findViewById(contentId).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentFragment = (BaseAbstractFragment) manager.findFragmentById(contentId);
                    }
                },20);
                return true;
            }
            return false;
        }
    };

    private List<BackHandler> backHandlers;
    private BaseAbstractFragment currentFragment = null;
    private int contentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getMainLayoutId();
        if (-1 != layoutId) {
            setContentView(layoutId);
        }
        contentId = getContentId();
        backHandlers = initBackHandlers();
        Collections.reverse(backHandlers);
        currentFragment = (BaseAbstractFragment) getSupportFragmentManager().findFragmentById(contentId);
        if (null == currentFragment) {
            changeFragment(createInitialFragment());
        }
    }

    /** Hack for navDrawer, hate myself for it */
    /*package*/ void hackAddFirstBackHandler(BackHandler handler) {
        backHandlers.add(0, handler);
    }

    /** Last added called first */
    protected List<BackHandler> initBackHandlers() {
        List<BackHandler> value = new ArrayList<>();
        value.add(fragmentBackStackHandler);
        value.add(currentFragmentBackHandler);
        return value;
    }

    protected int getContentId() {
        return android.R.id.content;
    }

    @NonNull
    protected abstract BaseAbstractFragment createInitialFragment();

    protected int getMainLayoutId() {
        return -1;
    }

    public void changeFragment(BaseAbstractFragment fragment) {
        changeFragment(fragment, false);
    }

    public void changeFragment(BaseAbstractFragment fragment, boolean addToBack) {
        if (fragment == null || currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) return;
        if (!addToBack) {
            while (fragmentManager().getBackStackEntryCount() > 0) {
                fragmentManager().popBackStackImmediate();
            }
        }
        currentFragment = fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(contentId, fragment);
        if (addToBack) ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        for (BackHandler handler : backHandlers) {
            if (handler.handleBack()) return;
        }
        super.onBackPressed();
    }

    public BaseAbstractFragment getCurrentFragment() {
        return currentFragment;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
