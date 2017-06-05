package com.smartgateway.app.fragment.drawer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evideo.weiju.info.CommandError;
import com.orvibo.homemate.bo.Device;
import com.smartgateway.app.HomeMateHelper;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.WeijuHelper;
import com.smartgateway.app.activity.ChildActivity;
import com.smartgateway.app.activity.Data;
import com.smartgateway.app.activity.WebActivity;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.LaunchInfo;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.fragment.BaseTopLevelFragmentSync;
import com.smartgateway.app.fragment.SgWalletFragment;
import com.smartgateway.app.fragment.WaveInvitationFragment;
import com.smartgateway.app.fragment.apartment.AddApartmentFragment;
import com.smartgateway.app.fragment.apartment.ApartmentListFragment;
import com.smartgateway.app.fragment.facility.FacilityListFragment;
import com.smartgateway.app.fragment.family.InviteFragment;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.fragment.maintenance.MaintenanceListFragment;
import com.smartgateway.app.service.NetworkService.RetrofitManager;
import com.smartgateway.app.weiju.WaveUnlockingHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * DashBoardFragment
 * Created by yanyu on 5/13/2016.
 */
public class DashBoardFragment extends BaseTopLevelFragmentSync<DashBoardFragment.DashItemDescriptor> {
    private boolean hasProvider = false;
    private final List<DashItemDescriptor> items1 = Arrays.asList(
            new DashItemDescriptor[]{
                    new DashItemDescriptor(R.drawable.dashboard_faciliti, R.string.dash_facility, "#ffd200"),
                    new DashItemDescriptor(R.drawable.dashboard_maintainance, R.string.dash_maintainance, "#207b76"),
                    new DashItemDescriptor(R.drawable.dashboard_feedback, R.string.dash_feedback, "#f98c81"),
                    new DashItemDescriptor(R.drawable.dashboard_sg_wallet, R.string.dash_wallet, "#00c6ff"),
//                    new DashItemDescriptor(R.drawable.dashboard_forest, R.string.dash_property, "#39b54a"),
                    new DashItemDescriptor(R.drawable.dashboard_invite, R.string.dash_invite, "#605ca8"),
                    new DashItemDescriptor(R.drawable.dashboard_car_plate, R.string.dash_car_plate, "#e64646"),
                    new DashItemDescriptor(R.drawable.dashboard_invite_guest, R.string.dash_invite_guest, "#228481"),
                    new DashItemDescriptor(R.drawable.dashboard_wave_unlocking, R.string.dash_wave_unlocking, "#83036d"),
                    new DashItemDescriptor(R.drawable.dashboard_one_time_password, R.string.dash_one_time_password, "#686868"),
//                    new DashItemDescriptor(R.drawable.dashboard_find_car, R.string.dash_find_car, "#4159a7"),
            });
    private final List<DashItemDescriptor> items2 = Arrays.asList(
            new DashItemDescriptor[]{
                    new DashItemDescriptor(R.drawable.dashboard_faciliti, R.string.dash_facility, "#ffd200"),
                    new DashItemDescriptor(R.drawable.dashboard_maintainance, R.string.dash_maintainance, "#207b76"),
                    new DashItemDescriptor(R.drawable.dashboard_feedback, R.string.dash_feedback, "#f98c81"),
                    new DashItemDescriptor(R.drawable.dashboard_sg_wallet, R.string.dash_wallet, "#00c6ff"),
                    new DashItemDescriptor(R.drawable.dashboard_invite, R.string.dash_invite, "#605ca8"),
                    new DashItemDescriptor(R.drawable.dashboard_forest, R.string.dash_property, "#8dc63f"),
            });

    private static SparseArray<Class<? extends BaseAbstractFragment>> mapper1 = new SparseArray<Class<? extends BaseAbstractFragment>>() {{
        put(R.drawable.dashboard_faciliti, FacilityListFragment.class);
        put(R.drawable.dashboard_maintainance, MaintenanceListFragment.class);
        put(R.drawable.dashboard_feedback, FeedbackListFragment.class);
        put(R.drawable.dashboard_sg_wallet, SgWalletFragment.class);
        put(R.drawable.dashboard_invite, InviteFragment.class);
        put(R.drawable.ic_apartment, ApartmentListFragment.class);
        put(R.drawable.ic_add, AddApartmentFragment.class);
        put(R.drawable.ic_about, WebFragment.class);
    }};
    private static SparseArray<Class<? extends BaseAbstractFragment>> mapper2 = new SparseArray<Class<? extends BaseAbstractFragment>>() {{
        put(R.drawable.dashboard_faciliti, FacilityListFragment.class);
        put(R.drawable.dashboard_maintainance, MaintenanceListFragment.class);
        put(R.drawable.dashboard_feedback, FeedbackListFragment.class);
        put(R.drawable.dashboard_sg_wallet, SgWalletFragment.class);
        put(R.drawable.dashboard_invite, InviteFragment.class);
        put(R.drawable.dash_property, ApartmentListFragment.class);
    }};

    private boolean hasApartment;

    TextView txtTitle;

    private ViewPager pagerBanner;
    private Button btnWebView;
    private TabLayout indicator;

    private double lat;
    private double lng;

    private List<String> imageArray;
    private int banner_count = 0;

    private DialogUtil dialogUtil;

    @Override
    protected String getTitle(Resources res) {
        return "";
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_dashboard);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lat = getArguments() != null ? getArguments().getDouble("lat") : 59.913703;
        lng = getArguments() != null ? getArguments().getDouble("lng") : 10.750999;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.createView(inflater, container, savedInstanceState);
        dialogUtil = new DialogUtil(getContext());

        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        setTitle("");

        return view;
    }

    private void initRequest() {
        hasProvider = false;
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA, Context.MODE_PRIVATE);
        String token = preferences.getString(Constants.USER_TOKEN, "");
        RetrofitManager.getUserApiService()
                .config(token, lat, lng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LaunchInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.i(Constants.TAG, "onCompleted");
                        if (isDetached()) {
                            return;
                        }
                        initViewPager(getView());
                    }
                    @Override
                    public void onError(Throwable e) {
                        if (e == null) return;
                        Log.i(Constants.TAG, e.getMessage());
                    }
                    @Override
                    public void onNext(LaunchInfo launchInfo) {
                        if (isDetached()) {
                            init();
                            return;
                        }
                        UserUtil.saveLaunchInfo(getContext(), launchInfo);
                        if (launchInfo != null &&
                                launchInfo.getCredentials() != null &&
                                TextUtils.isEmpty(launchInfo.getCredentials().getMessage())) {
                            new WeijuHelper().login(getContext());
                        }
                        if (launchInfo != null && launchInfo.getCredentials() != null && launchInfo.getCredentials().getProviderCount() > 0) {
                            hasProvider = true;
                        }
                        banner_count = launchInfo.getBanners().size();
                        imageArray = new ArrayList<>();
                        for (int i = 0; i < banner_count; i++) {
                            imageArray.add(launchInfo.getBanners().get(i).getUrl());
                        }
                        init();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        showDefaultCondo();
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasApartment) {
                    goTo(R.drawable.ic_apartment);
                } else {
                    goTo(R.drawable.ic_apartment);
                }
            }
        });

        initRequest();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected BaseAdapter<DashItemDescriptor> instantiateAdapter(Context context) {
        List<DashItemDescriptor> items;
        if (hasProvider) {
            items = items1;
        } else {
            items = items2;
        }
        return new BaseAdapter<DashItemDescriptor>(R.layout.item_dash, items) {
            @Override
            protected ViewHolder<DashItemDescriptor> createViewHolder(final View view) {
                return new ViewHolder<DashItemDescriptor>(view) {
                    private View tile = view;

                    {
                        tile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goTo(getItem().getIcon());
                            }
                        });
                    }

                    @Override
                    protected void hold(DashItemDescriptor item) {
                        ImageView img = (ImageView) view.findViewById(R.id.dash_item);
                        img.setImageResource(item.getIcon());
                        if (hasProvider) {
                            img.setScaleX(1);
                            img.setScaleY(1);
                        } else {
                            img.setScaleX(1.2f);
                            img.setScaleY(1.2f);
                        }
                        TextView text = (TextView) view.findViewById(R.id.dash_text);
                        text.setText(item.getText());
                        text.setTextColor(Color.parseColor(item.getColor()));
                    }
                };
            }
        };
    }

    @NonNull
    @Override
    protected LinearLayoutManager getListLayoutManager() {
        int countRow = 3;
        if (!hasProvider) {
            countRow = 2;
        }
        return new GridLayoutManager(null, countRow);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dashboard;
    }

    @Override
    protected SparseArray<Class<? extends BaseAbstractFragment>> getMapper() {
        SparseArray<Class<? extends BaseAbstractFragment>> mapper;
        if (hasProvider) {
            mapper = mapper1;
        } else {
            mapper = mapper2;
        }
        return mapper;
    }

    protected void goTo(int id) {
        Credentials credentials = UserUtil.getCredentials(getContext());
        switch (id) {
            case R.drawable.dashboard_faciliti:
                goToFragment(FacilityListFragment.class);
                break;
            case R.drawable.dashboard_maintainance:
                goToFragment(MaintenanceListFragment.class);
                break;
            case R.drawable.dashboard_feedback:
                goToFragment(FeedbackListFragment.class);
                break;
            case R.drawable.dashboard_sg_wallet:
                goToFragment(SgWalletFragment.class);
                break;
            case R.drawable.dashboard_invite:
                goToFragment(InviteFragment.class);
                break;
            case R.drawable.ic_apartment:
                goToFragment(ApartmentListFragment.class);
                break;
            case R.drawable.ic_add:
                goToFragment(AddApartmentFragment.class);
                break;
            case R.drawable.ic_about:
                goToFragment(WebFragment.class);
                break;
            case R.drawable.dashboard_find_car:
                goToWebActivity(R.string.dash_find_car, getUrl(Constants.FINDCAR_URL));
                break;
            case R.drawable.dashboard_car_plate:
                if (credentials == null) {
                    DialogUtil.showErrorAlert(getContext(), "There was an error connecting. Try again shortly", null);
                } else if (credentials.getMessage() != null) {
                    DialogUtil.showErrorAlert(getContext(), credentials.getMessage(), null);
                } else {
                    goToWebActivity(R.string.dash_car_plate, getUrl(Constants.CARPLATE_URL));
                }
                break;
            case R.drawable.dashboard_invite_guest:
                if (credentials == null) {
                    DialogUtil.showErrorAlert(getContext(), "There was an error connecting. Try again shortly", null);
                } else if (credentials.getMessage() != null) {
                    DialogUtil.showErrorAlert(getContext(), credentials.getMessage(), null);
                } else {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                    } else {
                        if (WeijuHelper.isWeijuConnected()) {
                            goToFragment(WaveInvitationFragment.class);
                        } else {
                            DialogUtil.showErrorAlert(getContext(), "Still Connecting to service. Try again shortly", null);
                        }
                    }
                }
                break;
            case R.drawable.dashboard_one_time_password:
                if (credentials == null) {
                    DialogUtil.showErrorAlert(getContext(), "There was an error connecting. Try again shortly", null);
                } else if (credentials.getMessage() != null) {
                    DialogUtil.showErrorAlert(getContext(), credentials.getMessage(), null);
                } else {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                    } else {
                        HomeMateHelper homeMateHelper = new HomeMateHelper();
                        Device doorLock = homeMateHelper.getDoorLock(getActivity());
                        if (doorLock != null) {
                            homeMateHelper.openLockRecord(getActivity(), doorLock);
                        } else {
                            new DialogUtil(getActivity()).showDissmissDialog(getString(R.string.no_door_lock_found),
                                    R.string.door_lock);
                        }
                    }
                }
                break;
            case R.drawable.dashboard_wave_unlocking:
                if (credentials == null) {
                    DialogUtil.showErrorAlert(getContext(), "There was an error connecting. Try again shortly", null);
                } else if (credentials.getMessage() != null) {
                    DialogUtil.showErrorAlert(getContext(), credentials.getMessage(), null);
                } else {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                    } else {
                        if (WeijuHelper.isWeijuConnected()) {
                            playWeijuUnlockWave();
                        } else {
                            DialogUtil.showErrorAlert(getContext(), "Still Connecting to service. Try again shortly", null);
                        }
                    }
                }
                break;
        }
    }

    private void playWeijuUnlockWave() {
        dialogUtil.showProgressDialog();

        WaveUnlockingHelper waveUnlockingHelper = new WaveUnlockingHelper();
        waveUnlockingHelper.fetchUnlockingWave(getContext().getApplicationContext(),
                new WaveUnlockingHelper.WaveUnlockingCallback() {
                    @Override
                    public void onUrlFetched(String url) {
                        if (TextUtils.isEmpty(url)) {
                            Log.e("Dashboard", "wave url is empty or null");
                            onFailure(null);
                        }

                        try {
                            Log.d("Dashboard", "start download and play wave url " + url);
                            final MediaPlayer player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(url);
                            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    if (dialogUtil != null) {
                                        dialogUtil.dismissProgressDialog();
                                    }
                                    player.start();
                                }
                            });
                            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Log.e("Dashboard", "media player error : " + what + " extra: " + extra);
                                    onFailure(null);
                                    return true;
                                }
                            });

                            player.prepareAsync();
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(null);
                        }
                    }

                    @Override
                    public void onFailure(CommandError error) {
                        if (dialogUtil != null) {
                            dialogUtil.dismissProgressDialog();
                        }

                        if (error == null) {
                            DialogUtil.showErrorAlert(getContext(), "Sorry, can't play unlock wave", null);
                        } else {
                            DialogUtil.showErrorAlert(getContext(), "Sorry, can't play unlock wave " +
                                            error.getStatus() +
                                            " message:" +
                                            error.getMessage(),
                                    null);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        if (dialogUtil != null) {
            dialogUtil.dismissProgressDialog();
        }

        super.onDestroyView();
    }

    private void goToWebActivity(int titleRes, String url) {
        Log.d("Dashboard", "go to web activity :" + url);
        Intent intent = new Intent(getActivity().getApplicationContext(), WebActivity.class);
        Data data = new Data(getString(titleRes), url);
        intent.putExtra("data", data);
        startActivity(intent);
    }

    protected String getUrl(String key) {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.URLS,
                Context.MODE_PRIVATE);
        return preferences.getString(key, "http://smartgateway.com.sg/");
    }

    protected void goToFragment(Class klass) {
        BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
        if (null != activity) {
            Intent i = new Intent(activity, ChildActivity.class);
            i.putExtra(Constants.EXTRA_FRAGMENT, klass.getName());
            startActivity(i);
        }
    }

    protected static class DashItemDescriptor extends AbstractData {
        private int icon;
        private
        @StringRes
        int text;
        private String color;

        public DashItemDescriptor(int icon, @StringRes int text, String color) {
            this.icon = icon;
            this.text = text;
            this.color = color;
        }

        public int getText() {
            return text;
        }

        public String getColor() {
            return color;
        }

        public int getIcon() {
            return icon;
        }
    }

    public void showDefaultCondo() {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String where = preferences.getString(Constants.USER_HOME, "");
        if (where.contentEquals("") || where.contentEquals("false")) {
            hasApartment = false;
            txtTitle.setText(getResources().getString(R.string.fragment_dashboard));
        } else {
            hasApartment = true;
            txtTitle.setText(where);
        }
    }

    public void initViewPager(View view) {
        pagerBanner = (ViewPager) view.findViewById(R.id.pagerBanner);
        pagerBanner.setVisibility(View.VISIBLE);
        btnWebView = (Button) view.findViewById(R.id.btnWebView);
        FragmentStatePagerAdapter adapterBanner = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return OnBannerShow.newInstance(imageArray.get(position));
            }

            @Override
            public int getCount() {
                return banner_count;
            }
        };

        pagerBanner.setAdapter(adapterBanner);
        btnWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(R.drawable.ic_about);
            }
        });
        indicator = (TabLayout) view.findViewById(R.id.indicator);
        indicator.setupWithViewPager(pagerBanner);
    }

    public static class OnBannerShow extends Fragment {

        private String bannerUrl;

        static OnBannerShow newInstance(String s) {
            OnBannerShow onBannerShow = new OnBannerShow();
            Bundle args = new Bundle();
            args.putString("image", s);
            onBannerShow.setArguments(args);
            return onBannerShow;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            bannerUrl = getArguments() != null ? getArguments().getString("image") : null;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_banner, container, false);

            final ImageView imgBanner = (ImageView) view.findViewById(R.id.imgBanner);
            if (!TextUtils.isEmpty(bannerUrl)) {
                loadBannerImage(imgBanner);
            }

            return view;
        }

        private void loadBannerImage(final ImageView imgBanner) {
            Picasso.with(getActivity()).load(bannerUrl).into(imgBanner);
        }
    }
}
