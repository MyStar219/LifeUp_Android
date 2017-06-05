package com.smartgateway.app.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.creativityapps.badgedimageviews.BadgedSquareImageView;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;
import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.activity.ChildActivity;
import com.smartgateway.app.activity.Data;
import com.smartgateway.app.activity.WebActivity;
import com.smartgateway.app.data.model.User.User;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.fragment.apartment.ApartmentListFragment;
import com.smartgateway.app.fragment.booking.BookingListFragment;
import com.smartgateway.app.fragment.drawer.AboutFragment;
import com.smartgateway.app.fragment.drawer.AgreementFragment;
import com.smartgateway.app.fragment.drawer.ProfileFragment;
import com.smartgateway.app.fragment.drawer.SettingsFragment;
import com.smartgateway.app.fragment.family.FamilyListFragment;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.fragment.maintenance.MaintenanceListFragment;

import java.util.ArrayList;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

public class PersonalCenterFragment extends BaseAbstractFragment {

    private TextView userName;
    private TextView userHome;
    private ImageView userPic;

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.nav_personal_center);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pesonal_center, container, false);
        userName = (TextView) view.findViewById(R.id.who);
        userHome = (TextView) view.findViewById(R.id.where);
        userPic = (ImageView) view.findViewById(R.id.img_user_avatar);

        User.Notification notification = UserUtil.loadFeedback(getActivity());

        Renderer<MenuElement> upMenuRenderer = new ElementRenderer(R.layout.personal_center_up_element);
        RendererBuilder<MenuElement> upRendererBuilder = new RendererBuilder<MenuElement>(upMenuRenderer);
        Renderer<MenuElement> bottomMenuRenderer = new ElementRenderer(R.layout.personal_center_bottom_element);
        RendererBuilder<MenuElement> bottomRendererBuilder = new RendererBuilder<MenuElement>(bottomMenuRenderer);

        RecyclerView upMenu = (RecyclerView) view.findViewById(R.id.up_menu);
        RecyclerView bottomMenu = (RecyclerView) view.findViewById(R.id.bottom_menu);

        ArrayList<MenuElement> menuElements = new ArrayList<>();
        menuElements.add(new MenuElement("{fa-building}", "My apartment", 0, R.color.menu_red));
        menuElements.add(new MenuElement("{fa-user}", "My Profile", 0, R.color.menu_red));
        menuElements.add(new MenuElement("{fa-heart}", "My Family", notification.getFamily() ? 1 : 0, R.color.menu_red));
        menuElements.add(new MenuElement("{fa-credit-card-alt}", "SG Wallet", 0, R.color.menu_red));

        menuElements.add(new MenuElement("{fa-book}", "My Bookings", notification.getBooking(), R.color.menu_blue));
        menuElements.add(new MenuElement("{fa-wrench}", "My Maintenance", notification.getMaintenance(), R.color.menu_blue));
        menuElements.add(new MenuElement("{fa-comments}", "My Feedbacks", notification.getFeedback(), R.color.menu_blue));
        menuElements.add(new MenuElement("{fa-gear}", "Settings", 0, R.color.menu_blue));

        RVRendererAdapter<MenuElement> upMenuAdapter = new RVRendererAdapter<>(upRendererBuilder, new ListAdapteeCollection<>(menuElements));
        upMenu.setLayoutManager(new GridLayoutManager(getContext(), 4));
        upMenu.setAdapter(upMenuAdapter);

        ArrayList<MenuElement> buttomMenuElements = new ArrayList<>();
        buttomMenuElements.add(new MenuElement("{fa-question-circle}", "Need help", 0, R.color.menu_green));
        buttomMenuElements.add(new MenuElement("{fa-info-circle}", "About", 0, R.color.menu_green));
        buttomMenuElements.add(new MenuElement("{fa-university}", "Agreement", 0, R.color.menu_green));

        RVRendererAdapter<MenuElement> bottomMenuAdapter = new RVRendererAdapter<>(bottomRendererBuilder,
                new ListAdapteeCollection<>(buttomMenuElements));
        bottomMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        bottomMenu.setAdapter(bottomMenuAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showUserInfo();
    }

    private void showUserInfo() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String name = preferences.getString(Constants.USER_NAME, "");
        String home = preferences.getString(Constants.USER_HOME, "");
        userName.setText(name);
        userHome.setText(home);
        UserUtil.loadUserPic(userPic, getActivity());
    }

    public class MenuElement {
        private String code;
        private String desciprtion;
        private int color;
        private int badgeCount;

        public MenuElement(String code, String desciprtion, int badgeCount, int color) {
            this.code = code;
            this.desciprtion = desciprtion;
            this.color = color;
            this.badgeCount = badgeCount;
        }

        public String getCode() {
            return code;
        }

        public String getDesciprtion() {
            return desciprtion;
        }

        public int getColor() {
            return color;
        }
    }


    public class ElementRenderer extends Renderer<MenuElement> {

        TextView icon;
        TextView description;
        BadgedSquareImageView badge;
        View board;

        int layout;

        public ElementRenderer(int layout) {
            this.layout = layout;
        }

        @Override
        protected View inflate(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(layout, parent, false);
            return view;
        }

        void onElementClicked() {
            MenuElement element = getContent();

            if (element.getCode().equals("{fa-building}")) {
                goToFragment(ApartmentListFragment.class);
            } else if (element.getCode().equals("{fa-user}")) {
                goToFragment(ProfileFragment.class);
            } else if (element.getCode().equals("{fa-heart}")) {
                goToFragment(FamilyListFragment.class);
            } else if (element.getCode().equals("{fa-credit-card-alt}")) {
                goToFragment(SgWalletFragment.class);
            } else if (element.getCode().equals("{fa-book}")) {
                goToFragment(BookingListFragment.class);
            } else if (element.getCode().equals("{fa-wrench}")) {
                goToFragment(MaintenanceListFragment.class);
            } else if (element.getCode().equals("{fa-comments}")) {
                goToFragment(FeedbackListFragment.class);
            } else if (element.getCode().equals("{fa-gear}")) {
                goToFragment(SettingsFragment.class);
            } else if (element.getCode().equals("{fa-info-circle}")) {
                goToFragment(AboutFragment.class);
            } else if (element.getCode().equals("{fa-university}")) {
                goToFragment(AgreementFragment.class);
            } else if (element.getCode().equals("{fa-question-circle}")) {
                goToWebActivity(R.string.need_help, getUrl(Constants.NEEDHELP_URL));
            }
        }

        @Override
        public void render() {
            MenuElement element = getContent();
            icon.setText(element.getCode());
            icon.setTextColor(ContextCompat.getColor(icon.getContext(), element.getColor()));
            description.setText(element.getDesciprtion());
            board.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onElementClicked();
                }
            });
            badge.showBadge(element.badgeCount > 0);
//            if (element.badgeCount > 0) {
                badge.setBadgeText(element.badgeCount + "");
//            } else {
//                badge.setBadgeText("");
//            }
        }

        @Override
        protected void setUpView(View rootView) {
            icon = (TextView) rootView.findViewById(R.id.icon);
            description = (TextView) rootView.findViewById(R.id.menu_description);
            board = rootView.findViewById(R.id.board);
            badge = (BadgedSquareImageView)rootView.findViewById(R.id.badge_bg);
        }

        /**
         * Insert external listeners in some widgets.
         */
        @Override
        protected void hookListeners(View rootView) {
        }
    }

    private void goToWebActivity(int titleRes, String url) {
        Log.d("Personal Center", "go to web activity :" + url);
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
}
