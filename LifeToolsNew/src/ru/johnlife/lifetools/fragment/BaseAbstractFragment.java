package ru.johnlife.lifetools.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.johnlife.lifetools.Constants;
import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.activity.BaseActivity;
import ru.johnlife.lifetools.service.BaseBackgroundService;

public abstract class BaseAbstractFragment extends Fragment implements Constants {
	private static final String TAG = "BaseFragment";

	private static final Finder toolbarFinder = new Finder() {
		@Override
		public boolean isFound(View suspect) {
			return suspect instanceof Toolbar;
		}
	};

	private BaseActivity baseActivity;
	private LayoutInflater inflater;
	private CoordinatorLayout coordinator;
	private View mainView;
	private Toolbar toolbar;

	public void addParam(String name, String value) {
		getParams().putString(name, value);
	}

	public void addParam(String name, int value) {
		getParams().putInt(name, value);
	}

	public void addParam(String name, long value) {
		getParams().putLong(name, value);
	}

	public void addParam(String name, boolean value) {
		getParams().putBoolean(name, value);
	}

	public void addParam(String name, Parcelable value) {
		getParams().putParcelable(name, value);
	}

	@NonNull
	private Bundle getParams() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			arguments = new Bundle();
			setArguments(arguments);
		}
		return arguments;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof BaseActivity) {
			baseActivity = (BaseActivity)activity;
		} else {
			throw new IllegalArgumentException(
					String.format(
							"Fragment %s can be attached only to superclass of BaseActivity, but trying to attach to %s",
							getClass().getSimpleName(),
							activity.getClass().getSimpleName()
					)
			);
		}
	}

	protected void setTitle(String title) {
		if (null != toolbar) {
			toolbar.setTitle(title);
		}
		BaseActivity baseActivity = getBaseActivity();
		ActionBar actionBar = baseActivity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(title);
		}
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		coordinator = createCoordinator(inflater, container);
		AppBarLayout appbar = getToolbar(inflater, coordinator);
		if (null != appbar) {
			toolbar = findToolBar(appbar);
			BaseActivity baseActivity = getBaseActivity();
			baseActivity.setToolbar(toolbar);
			ActionBar actionBar = baseActivity.getSupportActionBar();
			if (actionBar != null) {
				String title = getTitle(inflater.getContext().getResources());
				if (null != title) {
					toolbar.setTitle(title);
					actionBar.setTitle(title);
				}
				actionBar.setDisplayHomeAsUpEnabled(isUpAsHomeEnabled());
			}
			coordinator.addView(appbar, 0);
		}
		int idx = coordinator.getChildCount();
		mainView = createView(inflater, coordinator, savedInstanceState);
		if (mainView == coordinator) { //was added to coordinator already
			mainView = coordinator.getChildAt(idx);
		}
		CoordinatorLayout.LayoutParams layout = null;
		try {
			if (mainView != null) {
				layout = (CoordinatorLayout.LayoutParams) mainView.getLayoutParams();
			}
		} catch (ClassCastException e) {
			layout = new CoordinatorLayout.LayoutParams(mainView.getLayoutParams());
		}
		if (layout != null && null == layout.getBehavior()) {
			layout.setBehavior(new AppBarLayout.ScrollingViewBehavior());
			mainView.setLayoutParams(layout);
		}
		if (mainView != null && null == mainView.getParent()) {
			coordinator.addView(mainView);
		}
		return coordinator;

	}


	protected CoordinatorLayout createCoordinator(LayoutInflater inflater, ViewGroup container) {
		return (CoordinatorLayout) inflater.inflate(R.layout.coordinator, container, false);
	}

	/**
	 * Will not be called if getToolbar returned null
     */
	protected boolean isUpAsHomeEnabled() {
		return true;
	}
	/**
	 * Will not be called if getToolbar returned null
	 */
	protected abstract String getTitle(Resources res);

	protected abstract AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container);

	protected AppBarLayout defaultToolbar() {
		return createToolbarFrom(R.layout.toolbar_default);
	}

	protected AppBarLayout createToolbarFrom(int layoutId) {
		return (AppBarLayout) inflater.inflate(layoutId, coordinator, false);
	}

	private interface Finder {
		boolean isFound(View suspect);
	}

	private Toolbar findToolBar(View view) {
		return (Toolbar) findChild(view, toolbarFinder);
	}
	private View findChild(View view, Finder finder) {
		if (null == view) return null;
		if (finder.isFound(view)) return view;
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i=0; i<group.getChildCount(); i++) {
				View value = findChild(group.getChildAt(i), finder);
				if (null != value) return value;
			}
		}
		return null;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		baseActivity = null;
	}

	protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public void addMessage(String message) {
		addMessage(message, null, null);
	}

	public void addMessage(String message, String actionCaption, View.OnClickListener actionClickListener) {
		Snackbar snack = Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG);
		if (null != actionClickListener) {
			snack.setAction(actionCaption, actionClickListener);
		}
		snack.show();
	}

	protected void requestService(BaseBackgroundService.Requester<? extends BaseBackgroundService> requester) {
		Log.d(getClass().getSimpleName(), "Trying to request "+requester);
		if (baseActivity != null) {
			baseActivity.requestService(requester);
		} else {
			Log.e(TAG, "Cannot request activity "+baseActivity);
		}
	}

	public BaseActivity getBaseActivity() {
		return baseActivity;
	}

	public LayoutInflater getLayoutInflater() {
		return inflater;
	}
}