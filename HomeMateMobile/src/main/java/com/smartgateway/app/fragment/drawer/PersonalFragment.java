package com.smartgateway.app.fragment.drawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.data.model.User.UserUtil;
import com.smartgateway.app.fragment.BaseTopLevelFragment;
import com.smartgateway.app.fragment.SgWalletFragment;
import com.smartgateway.app.fragment.apartment.ApartmentListFragment;
import com.smartgateway.app.fragment.booking.BookingListFragment;
import com.smartgateway.app.fragment.family.FamilyListFragment;
import com.smartgateway.app.fragment.feedback.FeedbackListFragment;
import com.smartgateway.app.fragment.maintenance.MaintenanceListFragment;

import java.util.Arrays;
import java.util.List;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by yanyu on 5/16/2016.
 */
public class PersonalFragment extends BaseTopLevelFragment<PersonalFragment.PersonalItemDescriptor> {

    private TextView userName;
    private TextView userHome;
    private ImageView userPic;

    private  List<PersonalItemDescriptor> items = Arrays.asList(new PersonalItemDescriptor[]{
            new PersonalItemDescriptor(
                    R.drawable.ic_apartment,
                    R.string.nav_item_apartment
            ),
            new PersonalItemDescriptor(
                    R.drawable.ic_profile,
                    R.string.nav_item_profile
            ),
            new PersonalItemDescriptor(
                    R.drawable.ic_family,
                    R.string.nav_item_family
            ),
            new PersonalItemDescriptor(PersonalItemDescriptor.DELIMETER),
            new PersonalItemDescriptor(
                    R.drawable.sg_wallet,
                    R.string.nav_sg_wallet),

            new PersonalItemDescriptor(PersonalItemDescriptor.DELIMETER),

            new PersonalItemDescriptor(
                    R.drawable.ic_bookings,
                    R.string.nav_item_bookings,
                    0
            ),
            new PersonalItemDescriptor(
                    R.drawable.ic_maintenance,
                    R.string.nav_item_maintenance,
                    0
            ),
            new PersonalItemDescriptor(
                    R.drawable.ic_feedback,
                    R.string.nav_item_feedback
            ),
            new PersonalItemDescriptor(PersonalItemDescriptor.DELIMETER),
            // Temporarily comment out Settings option
//            new PersonalItemDescriptor(
//                    R.drawable.ic_settings,
//                    R.string.nav_item_settings
//            ),


    });

    private static SparseArray<Class<? extends BaseAbstractFragment>> mapper = new SparseArray<Class<? extends BaseAbstractFragment>>() {{
        put(R.drawable.ic_apartment, ApartmentListFragment.class);
        put(R.drawable.ic_profile, ProfileFragment.class);
        put(R.drawable.ic_family, FamilyListFragment.class);

        put(R.drawable.sg_wallet, SgWalletFragment.class);
        put(R.drawable.ic_bookings, BookingListFragment.class);
        put(R.drawable.ic_maintenance, MaintenanceListFragment.class);
        put(R.drawable.ic_feedback, FeedbackListFragment.class);
//        put(R.drawable.ic_settings, SettingsFragment.class);
    }};

    @Override
    public void onResume() {
        super.onResume();
        showUserInfo();
//      ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

    }

    @Override
    public void onPause() {
        super.onPause();
//        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    protected BaseAdapter<PersonalItemDescriptor> instantiateAdapter(final Context context) {
        return new BaseAdapter<PersonalItemDescriptor>(R.layout.item_personal, items) {
            @Override
            protected ViewHolder<PersonalItemDescriptor> createViewHolder(final View view) {
                return new ViewHolder<PersonalItemDescriptor>(view) {
                    private TextView text = (TextView) view.findViewById(R.id.text);
                    private TextView tag = (TextView) view.findViewById(R.id.tag);
                    private View separator = (View) view.findViewById(R.id.seperator);
                    {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goTo(getItem().getIcon());
                            }
                        });
                    }
                    @Override
                    protected void hold(PersonalItemDescriptor item) {
                        boolean delim = item.getIcon() == PersonalItemDescriptor.DELIMETER;
                        text.setVisibility(delim ? View.GONE : View.VISIBLE);
//                        tag.setVisibility(delim ? View.GONE : View.VISIBLE);
                        separator.setVisibility(delim ? View.VISIBLE : View.GONE);
                        if (delim) {
                            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                        } else {
                            text.setText(item.getStringRes());
                            text.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), 0, 0, 0);
                            int number = item.getActiveNumber();

//                            if (getItem().getStringRes() == R.string.nav_item_maintenance) {
//                                SharedPreferences preferences = getContext().getSharedPreferences(Constants.PERSONAL_COUNT,
//                                        Context.MODE_PRIVATE);
//                                int maintenances = preferences.getInt(Constants.MAINTENANCES, 0);
//                                tag.setVisibility(maintenances > 0 ? View.VISIBLE : View.GONE);
//                                tag.setText(String.valueOf(maintenances));
//                                subscription.add(RxBus.getDefaultInstance().toObserverable(MaintenancesEvent.class)
//                                        .subscribe(new Action1<MaintenancesEvent>() {
//                                            @Override
//                                            public void call(MaintenancesEvent event) {
//                                                tag.setVisibility(event.getCount() > 0 ? View.VISIBLE : View.GONE);
//                                                tag.setText(String.valueOf(event.getCount()));
//                                            }
//                                        }));
//
//                            } else if (getItem().getStringRes() == R.string.nav_item_bookings){
//                                SharedPreferences preferences = getContext().getSharedPreferences(Constants.PERSONAL_COUNT,
//                                        Context.MODE_PRIVATE);
//                                int bookings = preferences.getInt(Constants.BOOKINGS, 0);
//                                tag.setVisibility(bookings > 0 ? View.VISIBLE : View.GONE);
//                                tag.setText(String.valueOf(bookings));
//                                subscription.add(RxBus.getDefaultInstance().toObserverable(BookingsEvent.class)
//                                    .subscribe(new Action1<BookingsEvent>() {
//                                        @Override
//                                        public void call(BookingsEvent bookingsEvent) {
//                                            tag.setVisibility(bookingsEvent.getBookings() > 0 ? View.VISIBLE : View.GONE);
//                                            tag.setText(String.valueOf(bookingsEvent.getBookings()));
//                                        }
//                                    }));
//
//                            } else {
//                                tag.setVisibility(number > 0 ? View.VISIBLE : View.GONE);
//                                tag.setText(String.valueOf(number));
//                            }

                        }
                    }
                };
            }
        };
    }

    @Override
    protected String getTitle(Resources res) {
        return res.getString(R.string.nav_personal_center);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        final AppBarLayout toolbar = createToolbarFrom(R.layout.toolbar_personal);
        Log.i(Constants.TAG, "toolbar is created.");
        userName = (TextView) toolbar.findViewById(R.id.who);
        userHome = (TextView) toolbar.findViewById(R.id.where);
        userPic = (ImageView) toolbar.findViewById(R.id.img_user_avatar);
        TextView toolbarTitle = (TextView)toolbar.findViewById(R.id.txtTitle);
        toolbarTitle.setText(getTitle(getResources()));
        return toolbar;
    }

    private void showUserInfo() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String name = preferences.getString(Constants.USER_NAME,"");
        String home = preferences.getString(Constants.USER_HOME, "");
        userName.setText(name);
        userHome.setText(home);
        UserUtil.loadUserPic(userPic, getActivity());
    }

    @Override
    protected SparseArray<Class<? extends BaseAbstractFragment>> getMapper() {
        return mapper;
    }

    //item descriptor class
    protected static class PersonalItemDescriptor extends AbstractData {
        public static final int DELIMETER = -666;
        private int icon;
        private int stringRes;
        private int activeNumber;

        public PersonalItemDescriptor(int icon) {
            this.icon = icon;
        }

        public PersonalItemDescriptor(int icon, int stringRes) {
            this.icon = icon;
            this.stringRes = stringRes;
        }

        public PersonalItemDescriptor(int icon, int stringRes, int activeNumber) {
            this.icon = icon;
            this.stringRes = stringRes;
            this.activeNumber = activeNumber;
        }

        public int getActiveNumber() {
            return activeNumber;
        }

        public int getIcon() {
            return icon;
        }

        public int getStringRes() {
            return stringRes;
        }
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.createView(inflater, container, savedInstanceState);
        setTitle("");

        return view;
    }
}
