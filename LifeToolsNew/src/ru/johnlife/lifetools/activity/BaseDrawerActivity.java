package ru.johnlife.lifetools.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.util.BitmapUtil;

/**
 * Created by yanyu on 4/25/2016.
 */
public abstract class BaseDrawerActivity extends BaseMainActivity {
    private Toolbar pendingToolbar = null;
    private ImageView headerImage;

    protected class DrawerDescriptor {
        int widthDimenId = R.dimen.drawer_default_width;
        int menuId;
        int headerLayoutId = R.layout.drawer_default_header;

        public DrawerDescriptor(int menuId) {
            this.menuId = menuId;
        }

        public DrawerDescriptor(int headerLayoutId, int menuId) {
            this.headerLayoutId = headerLayoutId;
            this.menuId = menuId;
        }

        public DrawerDescriptor(int headerLayoutId, int menuId, int widthDimenId) {
            this.headerLayoutId = headerLayoutId;
            this.menuId = menuId;
            this.widthDimenId = widthDimenId;
        }
    }

    private NavigationView drawerMenu;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerListener;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawer = (DrawerLayout) findViewById(getDrawerId());
        if (drawer == null) {
            throw new IllegalArgumentException("Didn't found a drawer layout with id returned from getDrawerId\nIf you don't need drawer functionality, just use BaseActivity instead of BaseDrawerActivity.");
        }
        drawerMenu = new NavigationView(this);
        DrawerDescriptor dd = getDrawerDescriptor();
        DrawerLayout.LayoutParams layout = new DrawerLayout.LayoutParams(
                (int) getResources().getDimension(dd.widthDimenId),
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.START
        );
        drawerMenu.setLayoutParams(layout);
        drawerMenu.inflateMenu(dd.menuId);
        drawerMenu.inflateHeaderView(dd.headerLayoutId);
        drawerMenu.setItemIconTintList(null);
        drawer.addView(drawerMenu, layout);
        drawerMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                closeNavDrawer();
                return onOptionsItemSelected(item);
            }
        });
        if (null != pendingToolbar) {
            setToolbar(pendingToolbar);
            pendingToolbar = null;
        }

        View headerView = drawerMenu.getHeaderView(0);
        headerImage = (ImageView) headerView.findViewById(R.id.drawerImageProfile);
    }

    public Menu getMainMenu() {
        return drawerMenu.getMenu();
    }

    protected View findDrawerViewById(int viewId) {
        return drawer.findViewById(viewId);
    }

    protected abstract DrawerDescriptor getDrawerDescriptor();

    protected int getDrawerId() {
        return R.id.drawer;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerListener.onConfigurationChanged(newConfig);
    }

    public void setToolbar(Toolbar toolbar) {
        if (null == drawer) { //not yet ready, cache
            pendingToolbar = toolbar;
            return;
        }
        super.setToolbar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            drawerListener = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
            drawerListener.setDrawerIndicatorEnabled(true);
            drawer.addDrawerListener(drawerListener);
            drawerListener.syncState();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hackAddFirstBackHandler(new BackHandler() {
            @Override
            public boolean handleBack() {
                if (isNavDrawerOpen()) {
                    closeNavDrawer();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected int getContentId() {
        return R.id.content;
    }

    @Override
    protected int getMainLayoutId() {
        return R.layout.activity_drawer;
    }

    protected boolean isNavDrawerOpen() {
        return drawer != null && drawer.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    public ImageView getHeaderImage() {
        return headerImage;
    }
}
