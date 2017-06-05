package com.smartgateway.app.fragment.announcement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;
import com.smartgateway.app.Utils.GFunctions;
import com.smartgateway.app.activity.Data;
import com.smartgateway.app.activity.WebActivity;
import com.smartgateway.app.data.AnnouncementData;
import com.smartgateway.app.data.model.Announcement.AnnouncementList;
import com.smartgateway.app.service.NetworkService.ErrorAction;
import com.smartgateway.app.data.model.User.ResponseError;
import com.smartgateway.app.service.NetworkService.RetrofitManager;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanyu on 5/17/2016.
 */
public class AnnouncementsListFragment extends BaseListFragment<AnnouncementData> {
    public static final int modes[] = {R.string.condo, R.string.system};
    private int mode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(Constants.PARAM_MODE);
    }

    @Override
    protected BaseAdapter<AnnouncementData> instantiateAdapter(final Context context) {
        return new BaseAdapter<AnnouncementData>(R.layout.item_announcement) {
            @Override
            protected ViewHolder<AnnouncementData> createViewHolder(final View view) {
                return new ViewHolder<AnnouncementData>(view) {
                    private TextView title = (TextView) view.findViewById(R.id.title);
                    private TextView date = (TextView) view.findViewById(R.id.date);
                    private TextView content = (TextView) view.findViewById(R.id.content);
//                    private Date tmp = new Date();

                    @Override
                    protected void hold(final AnnouncementData item) {
                        title.setText(item.getTitle());
                        String d = item.getDate();
                        date.setText(GFunctions.convertDatetoViewType(d));
                        content.setText(item.getContent());
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goToAnnoucementWebView(item);
                            }
                        });
                    }
                };
            }
        };
    }

    private void goToAnnoucementWebView(AnnouncementData item) {
        Log.d("Announcement", "on announcements clicked");
        Intent intent = new Intent(getActivity().getApplicationContext(), WebActivity.class);
        String announcementUrl = getAnnouncementUrl(item);
        Log.d("Announcement", "go to " + announcementUrl);
        Data data = new Data("Announcement", announcementUrl);
        intent.putExtra("data", data);
        startActivity(intent);
    }

    private String getAnnouncementUrl(AnnouncementData item) {
        return RetrofitManager.BASE_URL + "announcement/detail/?" +
                (mode == 0 ? "condo_bid" : "system_bid") + "=" + item.getId();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        getAnnouncement();
        TextView scope = (TextView) view.findViewById(R.id.scope);
        scope.setText(modes[mode]);
        return view;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_annnouncement_list;
    }

    @Override
    protected String getTitle(Resources res) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    private void getAnnouncement(){
        final DialogUtil dialogUtil = new DialogUtil(getContext());
        dialogUtil.showProgressDialog();
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String token = preferences.getString(Constants.USER_TOKEN, "");
        RetrofitManager.getAnnouncementApiService()
                .list(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AnnouncementList>() {
                    @Override
                    public void call(AnnouncementList announcementList) {
                        dialogUtil.dismissProgressDialog();
                        switch (mode) {
                            case 0:
                                for (AnnouncementList.AnnouncementBean.SystemBean bean
                                        : announcementList.getAnnouncement().getCondo()) {
                                    String date = bean.getCreate_date();
                                    String title = bean.getSubject();
                                    String content = bean.getContent();
                                    AnnouncementData data = new AnnouncementData(title, content, date, String.valueOf(bean.getId()));
                                    getAdapter().add(data);
                                }
                                break;
                            case 1:
                                for (AnnouncementList.AnnouncementBean.SystemBean bean
                                        : announcementList.getAnnouncement().getSystem()) {
                                    String date = bean.getCreate_date();
                                    String title = bean.getSubject();
                                    String content = bean.getContent();
                                    AnnouncementData data = new AnnouncementData(title, content, date, String.valueOf(bean.getId()));
                                    getAdapter().add(data);
                                }
                                break;
                        }
                        getAdapter().notifyDataSetChanged();
                    }
                }, new ErrorAction() {
                    @Override
                    public void call(ResponseError error) {
                        dialogUtil.dismissProgressDialog();
                    }
                });
    }
}
