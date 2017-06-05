package com.orvibo.homemate.common;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evideo.weiju.WeijuManage;
import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.command.unlock.ObtainUnlockListByPageCommand;
import com.evideo.weiju.command.voip.ObtainCallListByPageCommand;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.unlock.UnlockInfo;
import com.evideo.weiju.info.unlock.UnlockInfoList;
import com.evideo.weiju.info.voip.CallInfo;
import com.evideo.weiju.info.voip.CallInfoList;
import com.smartgateway.app.R;
import com.smartgateway.app.WeijuHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.fragment.BaseListFragment;

/**
 * SmartCommunityFragment
 * Created by MDev on 12/14/16.
 */

public class UsersFragment extends BaseListFragment<UsersFragment.HistoryItem> {
    private EndlessRecyclerViewScrollListener scrollListener;
    private String type;

    class HistoryItem extends AbstractData {
        String photo;
        String name;
        String area;
        String time;

        public HistoryItem(String photo, String name) {
            this.photo = photo;
            this.name = name;
        }

        public HistoryItem(String photo, String name, String area, String time) {
            this.photo = photo;
            this.name = name;
            this.area = area;
            this.time = time;
        }

        public String getPhoto() {
            return photo;
        }

        public String getName() {
            return name;
        }

        public String getArea() {
            return area;
        }

        public String getTime() {
            return time;
        }
    }

    public static UsersFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString("type", type);
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollListener = new EndlessRecyclerViewScrollListener(getListLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadData(page);
            }
        };
        getList().addOnScrollListener(scrollListener);
        loadData(0);
    }

    private void loadData(int page) {
        Log.d("History", "start loading data for page " + page);
        if (type.equals(getString(R.string.unlock_records))) {
            final ObtainUnlockListByPageCommand obtainUnlockListByPageCommand = new ObtainUnlockListByPageCommand(getContext(), WeijuHelper.getApartmentInfo().getId(), page, 5);
            obtainUnlockListByPageCommand.setCallback(new InfoCallback<UnlockInfoList>() {
                @Override
                public void onSuccess(UnlockInfoList result) {
                    Log.d("History", "List fetched :" + result.getList().size());
                    List<HistoryItem> history = new ArrayList<>();

                    for (UnlockInfo info : result.getList()) {
                        history.add(new HistoryItem(info.getThumb_url(),
                                info.getUsername(),
                                info.getArea(),
                                getDate(info.getTime())));
                    }

                    getAdapter().addAll(history);
                }

                @Override
                public void onFailure(CommandError error) {
                    Log.e("History", "can't fetch data: " + error.getStatus() + " message:" + error.getMessage());
                }
            });
            WeijuManage.execute(obtainUnlockListByPageCommand);
        } else {
            final ObtainCallListByPageCommand obtainUnlockListByPageCommand = new ObtainCallListByPageCommand(getContext(), WeijuHelper.getApartmentInfo().getId(), page, 5);
            obtainUnlockListByPageCommand.setCallback(new InfoCallback<CallInfoList>() {
                @Override
                public void onSuccess(CallInfoList result) {
                    Log.d("History", "List fetched :" + result.getList().size());
                    List<HistoryItem> history = new ArrayList<>();

                    for (CallInfo info : result.getList()) {
                        history.add(new HistoryItem(info.getThumb_url(),
                                info.getCaller_device_name(),
                                info.getCaller_area(),
                                getDate(info.getBegin_time())));
                    }

                    getAdapter().addAll(history);
                }

                @Override
                public void onFailure(CommandError error) {
                    Log.e("History", "can't fetch data: " + error.getStatus() + " message:" + error.getMessage());
                }
            });
            WeijuManage.execute(obtainUnlockListByPageCommand);
        }
    }

    @Override
    protected BaseAdapter<HistoryItem> instantiateAdapter(final Context context) {
        return new BaseAdapter<HistoryItem>(R.layout.item_history) {
            @Override
            protected ViewHolder<HistoryItem> createViewHolder(final View view) {
                return new ViewHolder<HistoryItem>(view) {
                    private ImageView photo = (ImageView) view.findViewById(R.id.camera_photo);
                    private TextView time = (TextView) view.findViewById(R.id.time);
                    private TextView usermame = (TextView) view.findViewById(R.id.username);
                    private TextView area = (TextView) view.findViewById(R.id.area);
                    @Override
                    protected void hold(final HistoryItem item) {
                        Picasso.with(getContext())
                                .load(item.getPhoto().replace(" ", "%20"))
                                .into(photo);
                        Log.d("History", "url " + item.getPhoto());
                        usermame.setText(item.getName());
                        time.setText(item.getTime());
                        area.setText(item.getArea());
                    }
                };
            }
        };
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("dd MMM yyyy k:mm:ss a", cal).toString();
        return date;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected String getTitle(Resources res) {
        return type != null ? type : "";
    }
}