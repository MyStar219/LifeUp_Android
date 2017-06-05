package ru.johnlife.lifetools.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import ru.johnlife.lifetools.Constants;
import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

public abstract class AbstractTwoPaneActivity extends BaseActivity implements Constants {

	private static int MASTER_CONTAINER = -1;
	private static int DETAILS_CONTAINER = -1;

	private BaseAbstractFragment masterFragment;
	private BaseAbstractFragment detailFragment;
	private Context context = AbstractTwoPaneActivity.this;
	FrameLayout fragmentMaster;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (-1 == MASTER_CONTAINER) {
			MASTER_CONTAINER = getMasterContainerId();
		}
		if (-1 == DETAILS_CONTAINER) {
			DETAILS_CONTAINER = getDetailContainerId();
		}
		setContentView(getLayoutId());
		fragmentMaster = (FrameLayout) findViewById(MASTER_CONTAINER);
		Intent intent = getIntent();
		FragmentManager manager = getSupportFragmentManager();
		if (savedInstanceState == null) {
			if (null != intent.getExtras() && intent.getExtras().containsKey(EXTRA_MASTER_NAME)) {
				// master
				String masterFragmentClassName = intent.getStringExtra(EXTRA_MASTER_NAME);
				Bundle masterFragmentArgs = intent.getBundleExtra(EXTRA_MASTER_ARGS);
				Fragment.SavedState masterFragmentState = intent.getParcelableExtra(EXTRA_MASTER_STATE);
				masterFragment = (BaseAbstractFragment) Fragment.instantiate(context, masterFragmentClassName, masterFragmentArgs);
				if (masterFragmentState != null) {
					masterFragment.setInitialSavedState(masterFragmentState);
				}
				manager.beginTransaction().replace(MASTER_CONTAINER, masterFragment).commit();
				// detail
				String detailFragmentClassName = intent.getStringExtra(EXTRA_DETAIL_NAME);
				if (null != detailFragmentClassName) {
					Bundle detailFragmentArgs = intent.getBundleExtra(EXTRA_DETAIL_ARGS);
					detailFragment = (BaseAbstractFragment) Fragment.instantiate(context, detailFragmentClassName, detailFragmentArgs);
					manager.beginTransaction().replace(DETAILS_CONTAINER, detailFragment).commit();
				}
			} else {
				// first launch
				masterFragment = handleFirstLaunch(manager);

			}
		} else {
			masterFragment = (BaseAbstractFragment) manager.findFragmentById(MASTER_CONTAINER);
			detailFragment = (BaseAbstractFragment) manager.findFragmentById(DETAILS_CONTAINER);
		}
	}

	protected abstract BaseAbstractFragment handleFirstLaunch(FragmentManager manager);
	protected abstract int getLayoutId();
	protected abstract int getMasterContainerId();
	protected abstract int getDetailContainerId();
	protected abstract Class<? extends AbstractTwoPaneActivity> getGenericActivityClass();

	protected boolean isMasterVisible() {
		LinearLayout.LayoutParams masterLayout = (LinearLayout.LayoutParams) findViewById(MASTER_CONTAINER).getLayoutParams();
		if (masterLayout.weight == 0 && masterLayout.width == 0) {
			return false;
		}
		return true;
	}

	protected boolean isDetailVisible() {
		LinearLayout.LayoutParams detailsLayout = (LinearLayout.LayoutParams) findViewById(DETAILS_CONTAINER).getLayoutParams();
		if (detailsLayout.weight == 0) {
			return false;
		}
		return detailFragment != null;
	}

	protected boolean hasDetails() {
		if (null == detailFragment) {
			FragmentManager manager = getSupportFragmentManager();
			detailFragment = (BaseAbstractFragment) manager.findFragmentById(DETAILS_CONTAINER);
		}
		return null != detailFragment;
	}

	protected void replaceMaster(BaseAbstractFragment fragment) {
		masterFragment = fragment;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(MASTER_CONTAINER, masterFragment);
		detailFragment = (BaseAbstractFragment) getSupportFragmentManager().findFragmentById(DETAILS_CONTAINER);
		ft.addToBackStack(null);
		if (hasDetails()) {
			ft.remove(detailFragment);
			detailFragment = null;
		}
		ft.commit();
	}

	protected void expandMaster() {
		if (hasDetails()) {
			FragmentManager manager = getSupportFragmentManager();
			Fragment fragment = manager.findFragmentById(DETAILS_CONTAINER);
			manager.beginTransaction().remove(fragment).commit();
			manager.executePendingTransactions();
			detailFragment = null;
		}
		animateMaster(true);
	}

	protected void collapseMaster() {
		animateMaster(false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Bundle args = data.getBundleExtra(EXTRA_ARGS);
			refreshFragment(false, args);
		}
	}

	protected void refreshFragment(boolean master, Bundle args) {
		if (args != null) {
			Fragment fragment = master ? masterFragment : detailFragment;
			FragmentManager manager = getSupportFragmentManager();
			Bundle existingArgs = fragment.getArguments();
			manager.beginTransaction().remove(fragment).commit();
			manager.executePendingTransactions();
			if (null == existingArgs) {
				existingArgs = new Bundle();
			}
			existingArgs.putAll(args);
			fragment.setArguments(existingArgs);
			manager.beginTransaction().replace(master ? MASTER_CONTAINER : DETAILS_CONTAINER, fragment).commit();
			manager.executePendingTransactions();
		}
	}

	private void animateMaster(final boolean flag) {
		final View masterView = findViewById(MASTER_CONTAINER);
		final View detailView = findViewById(DETAILS_CONTAINER);
		LinearLayout.LayoutParams masterLayout = (LinearLayout.LayoutParams) masterView.getLayoutParams();
		LinearLayout.LayoutParams detailLayout = (LinearLayout.LayoutParams) detailView.getLayoutParams();

		LayoutParams visibleLayout = flag ? masterLayout : detailLayout;
		LayoutParams invisibleLayout = flag ? detailLayout : masterLayout;

		if (visibleLayout.weight != 0)
			return;
		visibleLayout.weight = 1;
		invisibleLayout.weight = 0;

		masterView.setLayoutParams(masterLayout);
		detailView.setLayoutParams(detailLayout);

		masterView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onPreDraw() {
				masterView.getViewTreeObserver().removeOnPreDrawListener(this);
				View animatable = flag ? masterView : detailView;
				long translationX = (flag ? -1 : 1) * animatable.getWidth();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					animatable.setTranslationX(translationX);
					animatable.animate().translationX(0).setDuration(400).setStartDelay(0).start();
				} else {
					TranslateAnimation anim = new TranslateAnimation(translationX, -translationX, 0, 0);
					anim.setFillAfter(true);
					anim.setDuration(400);
					animatable.startAnimation(anim);
				}
				return true;
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

	private void replaceFragment(String newFragmentName, Bundle newFragmentArguments) {
		FragmentManager manager = getSupportFragmentManager();
//		View detailView = findViewById(DETAILS_CONTAINER);
//		if (detailView.getVisibility() == View.GONE) {
//			masterFragment = (BaseFragment) Fragment.instantiate(context, newFragmentName, newFragmentArguments);
//			replaceMaster(masterFragment);
//		} else {
		boolean shouldExpandDetail = !isDetailVisible();
		detailFragment = (BaseAbstractFragment) Fragment.instantiate(context, newFragmentName, newFragmentArguments);
		manager.beginTransaction().replace(DETAILS_CONTAINER, detailFragment).commit();
		if (shouldExpandDetail) collapseMaster();
	}

	public void showFragment(Fragment currentFragment, Class<? extends BaseAbstractFragment> newFragmentClass, Bundle newFragmentArguments) {
		String newFragmentName = newFragmentClass.getName();
		Log.d("log", "MainActivity:: onShowFragment (" + newFragmentName + ")");
		if (isFinishing())
			return;
		FragmentManager manager = getSupportFragmentManager();
		if (null == masterFragment) {
			masterFragment = (BaseAbstractFragment) manager.findFragmentById(MASTER_CONTAINER);
		}
		if (null == detailFragment) {
			detailFragment = (BaseAbstractFragment) manager.findFragmentById(DETAILS_CONTAINER);
		}
		if (masterFragment.getClass().equals(currentFragment.getClass()) && masterFragment.toString().equals(currentFragment.toString())) {
			Log.d("log", "masterFragment == currentFragment");
			replaceFragment(newFragmentName, newFragmentArguments);
			
		} else { // if click on item of detailFragment
			Bundle args = (Bundle) currentFragment.getArguments().clone();
			Fragment.SavedState state = manager.saveFragmentInstanceState(currentFragment);
			Intent intent = new Intent(this, getGenericActivityClass());
			intent.putExtra(EXTRA_MASTER_NAME, currentFragment.getClass().getName());
			intent.putExtra(EXTRA_MASTER_ARGS, args);
			intent.putExtra(EXTRA_MASTER_STATE, state);
			intent.putExtra(EXTRA_DETAIL_NAME, newFragmentName);
			intent.putExtra(EXTRA_DETAIL_ARGS, newFragmentArguments);
			startActivityForResult(intent, 1);
			overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
		}
	}

	public void goBack(Bundle parentNewArguments) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_ARGS, parentNewArguments);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

}