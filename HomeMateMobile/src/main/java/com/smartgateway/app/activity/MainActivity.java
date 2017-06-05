package com.smartgateway.app.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.onesignal.OneSignal;
import com.orvibo.homemate.common.ViHomeProApp;
import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.LifeUpProvidersHelper;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.MarshmallowPermissions;
import com.smartgateway.app.WeijuHelper;
import com.smartgateway.app.data.event.ProfileEvent;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.fragment.HomeMateFragment;
import com.smartgateway.app.fragment.PersonalCenterFragment;
import com.smartgateway.app.fragment.announcement.AnnouncementsFragment;
import com.smartgateway.app.fragment.drawer.AboutFragment;
import com.smartgateway.app.fragment.drawer.AgreementFragment;
import com.smartgateway.app.fragment.drawer.DashBoardFragment;
import com.smartgateway.app.fragment.drawer.SettingsFragment;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseDrawerActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.util.RxBus;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * MainActivity
 * Created by yanyu on 5/12/2016.
 */
public class MainActivity extends BaseDrawerActivity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private GoogleApiClient googleApiClient;

	private double lat = 234234.234, lng = 23423.23423;
	private Menu menu;
	private boolean show = true;
	private CompositeSubscription subscription = new CompositeSubscription();
	private boolean weijuloggedIn = false;

	public boolean getWeiju() {
		return weijuloggedIn;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (googleApiClient == null) {
			googleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		googleApiClient.connect();

		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_PHONE_STATE}, 1);
		} else {
			((ViHomeProApp) ViHomeProApp.getContext()).initServices();
			(new HomeMateHelper()).login(MainActivity.this);
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		googleApiClient.disconnect();
		super.onStop();

	}

	@Override
	protected BaseAbstractFragment createInitialFragment() {
		DashBoardFragment dasboardFragment = new DashBoardFragment();
		Bundle bundle = new Bundle();
		bundle.putDouble("lat", lat);
		bundle.putDouble("lng", lng);
		dasboardFragment.setArguments(bundle);
		return dasboardFragment;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					((ViHomeProApp) ViHomeProApp.getContext()).initServices();
					(new HomeMateHelper()).login(MainActivity.this);
					Log.v("OS6-Permission", "Permission Has result");
				}
				return;
			}
			case MarshmallowPermissions.STORAGE_PERMISSION_REQUEST_CODE: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					galleryIntent();
				}
				return;
			}
		}
	}

	@Override
	protected DrawerDescriptor getDrawerDescriptor() {
		return new DrawerDescriptor(R.menu.drawer);
	}

	@Override
	protected boolean shouldBeLoggedIn() {
		return true;
	}

	@Override
	protected ClassConstantsProvider getClassConstants() {
		return Constants.CLASS_CONSTANTS;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		show = false;
		if (item.getItemId() == R.id.nav_personal_center) {
			changeFragment(new PersonalCenterFragment(), true);
		} else if (item.getItemId() == R.id.nav_dashboard) {
			changeFragment(new DashBoardFragment(), true);
			show = true;
		} else if (item.getItemId() == R.id.nav_announcements) {
			changeFragment(new AnnouncementsFragment(), true);
		} else if (item.getItemId() == R.id.nav_item_settings) {
			changeFragment(new SettingsFragment(), true);
		} else if (item.getItemId() == R.id.nav_item_about) {
			changeFragment(new AboutFragment(), true);
		} else if (item.getItemId() == R.id.nav_item_agreement) {
			changeFragment(new AgreementFragment(), true);
		} else if (item.getItemId() == R.id.nav_item_logout) {
			DialogUtil.showErrorAlert(this, getString(R.string.logout_confirm), getString(android.R.string.ok), getString(android.R.string.cancel), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					logout();
				}
			}, null);
		} else if (item.getItemId() == R.id.nav_smart_life) {
			Credentials credentials = UserUtil.getCredentials(this);
			if (credentials == null) {
				DialogUtil.showErrorAlert(this, "There was an error connecting. Try again shortly", null);
			} else if (credentials.getMessage() != null) {
				DialogUtil.showErrorAlert(this, credentials.getMessage(), null);
			}
			else {
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
					Log.v("OS6-Permission", "Not Granted");

					ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
				} else {
					if (WeijuHelper.isWeijuConnected()) {
						changeFragment(new HomeMateFragment(), true);
					} else {
						DialogUtil.showErrorAlert(this, "Still Connecting to service. Try again shortly", null);
					}
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		showMenu(show);
		return true;
	}

	public void showMenu(boolean show) {
		MenuItem item = menu.findItem(R.id.nav_announcements);
		item.setVisible(show);
	}

	private void logout() {
		new LifeUpProvidersHelper().signOut(this);

		SharedPreferences preferences = getSharedPreferences(Constants.USER_DATA,
				Context.MODE_PRIVATE);
		String token = preferences.getString(Constants.USER_TOKEN, "");
		RetrofitManager.getUserApiService()
				.logout(token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Detail>() {
					@Override
					public void onCompleted() {
//                        Log.i(Constants.TAG, "onCompleted");
//                        requestService(new BaseBackgroundService.Requester() {
//                            @Override
//                            public void requestService(BaseBackgroundService service) {
//                                service.logout();
//                            }
//                        });
					}

					@Override
					public void onError(Throwable e) {
						if (e == null) return;
						Log.i(Constants.TAG, e.getMessage());
					}

					@Override
					public void onNext(Detail detail) {
					}
				});

		requestService(new BaseBackgroundService.Requester() {
			@Override
			public void requestService(BaseBackgroundService service) {
				service.logout();
			}
		});

		OneSignal.deleteTag("user");
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
		} else {
			if (LocationServices.FusedLocationApi.getLastLocation(googleApiClient) != null) {
				lat = LocationServices.FusedLocationApi.getLastLocation(googleApiClient).getLatitude();
				lng = LocationServices.FusedLocationApi.getLastLocation(googleApiClient).getLongitude();
			} else {
				lat = 0.00;
				lng = 0.00;
				lat = 59.913703;
				lng = 10.750999;
			}
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		loadUserPic();

		subscription.add(RxBus.getDefaultInstance().toObserverable(ProfileEvent.class)
				.subscribe(new Action1<ProfileEvent>() {
					@Override
					public void call(ProfileEvent profileEvent) {
						if (profileEvent.isProfilechange()) {
							loadUserPic();
						}
					}
				}));

	}

	@Override
	protected void onDetailReturned(String detail, int responseCode) {
		DialogUtil.showDetailErrorAlert(this, detail, responseCode);
	}

	public void loadUserPic() {
		ImageView headerImage = getHeaderImage();
		UserUtil.loadUserPic(headerImage, this);
	}

	@Override
	protected void onLogoutEvent() {
		new LifeUpProvidersHelper().signOut(this);
	}
}

