package com.smartgateway.app.fragment.facility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.data.BookingDayData;
import com.smartgateway.app.data.BookingHourData;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FacilityVariantData;
import com.smartgateway.app.data.model.Facility.Session;
import com.smartgateway.app.fragment.state.StateHelper;
import com.smartgateway.app.fragment.state.StateHelper.StateData;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import ru.johnlife.lifetools.tools.DateUtil;
import ru.johnlife.lifetools.ui.CoordinatedAutosizeTextView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/13/2016.
 */
public class CalendarFragment extends BaseListFragment<BookingDayData> {

    private int offsetPosition = 0;

    private static StateHelper states;
    private FacilityVariantData variant;
    private FacilityData facility;
    private List<BookingHourData> list = new ArrayList<>();

    public CalendarFragment setFacility(FacilityData facility) {
        this.facility = facility;
        return this;
    }

    public CalendarFragment setVariant(FacilityVariantData variant) {
        this.variant = variant;
        return this;
    }

    @Override
    protected BaseAdapter<BookingDayData> instantiateAdapter(final Context context) {
        return new BaseAdapter<BookingDayData>(R.layout.item_day) {
            private final long now = DateUtil.getBeginOfTheDay(System.currentTimeMillis());
            @Override
            protected ViewHolder<BookingDayData> createViewHolder(final View view) {
                return new ViewHolder<BookingDayData>(view) {
                    private CoordinatedAutosizeTextView number = (CoordinatedAutosizeTextView) view.findViewById(R.id.number);
                    private View colorTag = view.findViewById(R.id.colorTag);
                    private ItemizedClickListener clickListener = new ItemizedClickListener() {
                        @Override
                        public void onClick(View v) {

                            SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                                    Context.MODE_PRIVATE);
                            String token = preferences.getString(Constants.USER_TOKEN, "");
                            if ((getAdapterPosition() - offsetPosition) < 0) {
                                Toast.makeText(getActivity(), "Booking for this date is not available.", Toast.LENGTH_SHORT).show();
                            } else {
                                int posit = getAdapterPosition();
                                String date = variant.getDates().get(getAdapterPosition() - offsetPosition);
                                final DialogUtil dialogUtil = new DialogUtil(getContext());
                                dialogUtil.showProgressDialog();
                                RetrofitManager.getFacilityApiService()
                                        .session(variant.getFid(), date, token)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<Session>() {
                                            @Override
                                            public void onCompleted() {
                                                BaseMainActivity activity = (BaseMainActivity) getBaseActivity();
                                                TimeSlotListFragment fragment = new TimeSlotListFragment().setFacility(facility).setVariant(variant).setDay(getItem());
                                                fragment.setHour(list);
                                                activity.changeFragment(fragment, true);
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                dialogUtil.dismissProgressDialog();
                                                if (e == null) return;
                                                Log.i(Constants.TAG, e.getMessage());
                                            }

                                            @Override
                                            public void onNext(Session session) {
                                                dialogUtil.dismissProgressDialog();
                                                for (int i = 0; i < session.getFacility().getTime().size(); i++) {
                                                    String startTime = session.getFacility().getTime().get(i).getStart();
                                                    String endTime = session.getFacility().getTime().get(i).getEnd();
                                                    boolean peak = session.getFacility().getTime().get(i).isPeak();
                                                    String status = session.getFacility().getTime().get(i).getStatus();
                                                    int session_id = session.getFacility().getTime().get(i).getSession_id();
                                                    BookingHourData data = new BookingHourData(startTime, endTime, peak, status, session_id);
                                                    list.add(data);
                                                }
                                            }
                                        });
                            }
                        }
                    };

                    {
                        number.setCoordinator(getList());
                        view.setOnClickListener(clickListener);
                    }

                    @Override
                    protected void hold(BookingDayData item) {
                        int tag = item.getColorTag(now);
                        if (tag == 0){
                            offsetPosition++;
                        }
                        number.setText(item.getNumber());
                        number.setAlpha(tag == 0 ? 0.6f : 1f);
                        colorTag.setBackgroundColor(states.get(tag).getColor());
                        clickListener.setItem(item);
                    }
                };
            }
        };
    }

    @NonNull
    @Override
    protected LinearLayoutManager getListLayoutManager() {
        return new GridLayoutManager(getList().getContext(), 7);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_calendar;
    }

    @Override
    protected int getListId() {
        return R.id.list;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        //lazy static initialization of states
        Context context = view.getContext();
        if (states == null) {
            states = new StateHelper(context, R.array.state_days); // day booking status
        }
        // data for main adapter & header
        //TODO: replace dummy with real data | don't kill header
        Calendar tmp = Calendar.getInstance();
        ViewGroup header = (ViewGroup) view.findViewById(R.id.header);
        DateFormat weekDayFormat = new SimpleDateFormat("ccc");
        DateFormat monthFormat = new SimpleDateFormat("MMMM");
        List<String> months = new ArrayList<>();
        BaseAdapter<BookingDayData> adapter = getAdapter();
        tmp.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int i = 0;
        for (BookingDayData day : variant.getDays()) {
            if (i++ < 7) { //fill header
                String headerString = weekDayFormat.format(tmp.getTime());
                TextView headerField = (TextView) inflater.inflate(R.layout.item_day_header, header, false);
                headerField.setText(headerString);
                header.addView(headerField);
            }
            adapter.add(day);
            tmp.add(Calendar.DAY_OF_YEAR, 1);
            //month
            String monthName = monthFormat.format(tmp.getTime());
            if (!months.contains(monthName)) {
                months.add(monthName);
            }
        }
        //months header
        TextView monthView = (TextView) view.findViewById(R.id.month);
        StringBuilder b = new StringBuilder(months.get(0));
        for (i=1; i<months.size(); i++) {
            b.append(" & ").append(months.get(i));
        }
        monthView.setText(b.toString());
        //legend
        RecyclerView legend = (RecyclerView) view.findViewById(R.id.legend);
        legend.setHasFixedSize(true);
        legend.setLayoutManager(new GridLayoutManager(context, 3));
        BaseAdapter<StateData> legendAdapter = new BaseAdapter<StateData>(R.layout.item_day_state){
            @Override
            protected ViewHolder<StateData> createViewHolder(final View view) {
                return new ViewHolder<StateData>(view) {
                    private View colorTag = view.findViewById(R.id.colorTag);
                    private TextView name = (TextView) view.findViewById(R.id.name);
                    @Override
                    protected void hold(StateData item) {
                        colorTag.setBackgroundColor(item.getColor());
                        name.setText(item.getName());
                    }
                };
            }
        };
        for (i=1; i<states.size(); i++) {
            legendAdapter.add(states.get(i));
        }
        legend.setAdapter(legendAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected String getTitle(Resources res) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

}
