package com.orvibo.homemate.device.allone2.epg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.DeleteTimer;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allen on 2016/7/21.
 */
public class ProgramSubscribeActivity extends BaseActivity {
    private NavigationGreenBar navigationGreenBar;
    private ListView lvSubscribeTiming;
    private View emptyView;
    private ProgramTimingAdapter programTimingAdapter;
    private Device device;
    private TimingDao timingDao;
    private DeleteTimer deleteTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_subscribe);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null) {
            device = (Device) serializable;
            init();
        } else {
            finish();
        }
    }

    private void init() {
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        navigationGreenBar.setMiddleTextColor(getResources().getColor(R.color.black));
        lvSubscribeTiming = (ListView) findViewById(R.id.lvSubscribeTiming);
        timingDao = new TimingDao();
        List<Timing> subscribeTimings = timingDao.selSubscribeTimings(device.getUid(), device.getDeviceId());
        programTimingAdapter = new ProgramTimingAdapter();
        programTimingAdapter.refresh(subscribeTimings);
        lvSubscribeTiming.setAdapter(programTimingAdapter);
        emptyView = findViewById(R.id.emptyView);
        lvSubscribeTiming.setEmptyView(emptyView);
        initTimer();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        List<Timing> subscribeTimings = timingDao.selSubscribeTimings(device.getUid(), device.getDeviceId());
        programTimingAdapter.refresh(subscribeTimings);
    }

    private void initTimer() {
        deleteTimer = new DeleteTimer(mContext) {
            @Override
            public void onDeleteTimerResult(String uid, int serial, int result) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    List<Timing> subscribeTimings = timingDao.selSubscribeTimings(device.getUid(), device.getDeviceId());
                    programTimingAdapter.refresh(subscribeTimings);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ProgramTimingAdapter extends BaseAdapter implements View.OnClickListener{
        private List<String> dates = new ArrayList<>();
        private Map<String, List<Timing>> timingMap = new HashMap<>();
        private Map<String, Integer> dayOfWeeks = new HashMap<>();

        private void refresh(List<Timing> subscribeTimings) {
            dates.clear();
            timingMap.clear();
            for (Timing timing: subscribeTimings) {
                int dayOfWeek = WeekUtil.getDayOfWeek(timing.getWeek());//week转化为dayOfWeek，1-7对应周一到周日
                String date = TimeUtil.getDateByDayOfWeek(dayOfWeek);//dayOfWeek转化为日期
                List<Timing> timingList;
                if(dates.contains(date)) {
                    timingList = timingMap.get(date);
                } else {
                    dates.add(date);
                    timingList = new ArrayList<>();
                    timingMap.put(date, timingList);
                    dayOfWeeks.put(date, dayOfWeek);
                }
                timingList.add(timing);
            }
            if (dates.size() > 1) {//日期排序
                Calendar calendar = Calendar.getInstance();
                int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;
                if (nowDayOfWeek == 0) {
                    nowDayOfWeek = 7;
                }
                List<String> sortDates = new ArrayList<>();
                sortDates.add(dates.get(0));
                for (int i = 1; i < dates.size(); i++) {
                    String comparedDate = dates.get(i);
                    int comparedDayOfWeek = dayOfWeeks.get(comparedDate);
                    if (comparedDayOfWeek < nowDayOfWeek) {//如果是下周则+7
                        comparedDayOfWeek = comparedDayOfWeek + 7;
                    }
                    for (int j = 0; j < sortDates.size(); j++) {
                        String date = sortDates.get(j);
                        int dayOfWeek = dayOfWeeks.get(date);
                        if (dayOfWeek < nowDayOfWeek) {//如果是下周则+7
                            dayOfWeek = dayOfWeek + 7;
                        }
                        if (dayOfWeek > comparedDayOfWeek) {
                            sortDates.add(j, comparedDate);
                            break;
                        } else if (j == sortDates.size()-1) {
                            sortDates.add(comparedDate);
                            break;
                        }
                    }
                }
                dates = sortDates;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public Object getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.activity_program_detail_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
                viewHolder.epgItems = (LinearLayout) convertView.findViewById(R.id.epgItems);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String date = dates.get(position);
            List<Timing> timingList = timingMap.get(date);
            viewHolder.tvDate.setText(date);
            for (int i = 0; i < viewHolder.epgItems.getChildCount(); i++) {
                View epgItem = viewHolder.epgItems.getChildAt(i);
                epgItem.setVisibility(View.GONE);
            }
            for (int i = 0; i < timingList.size(); i++) {
                Timing timing = timingList.get(i);
                View epgItem = viewHolder.epgItems.getChildAt(i);
                if (epgItem == null) {
                    epgItem = View.inflate(mContext, R.layout.activity_subscribe_program_item, null);
                    viewHolder.epgItems.addView(epgItem);
                } else {
                    epgItem.setVisibility(View.VISIBLE);
                }
                TextView tvIsHd = (TextView) epgItem.findViewById(R.id.tvIsHd);
                tvIsHd.setText(timing.getName());
                TextView tvTime = (TextView) epgItem.findViewById(R.id.tvTime);
                tvTime.setText(TimeUtil.getTime24(mContext,timing.getHour(), timing.getMinute()));
                TextView tvSubscribe = (TextView) epgItem.findViewById(R.id.tvSubscribe);
                tvSubscribe.setOnClickListener(this);
                tvSubscribe.setTag(timing);
            }
            return convertView;
        }

        @Override
        public void onClick(final View v) {
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.warm_tips));
            dialogFragmentTwoButton.setContent(getString(R.string.subscribe_cancel_confirm));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.not_cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    Timing timing = (Timing) v.getTag();
                    showDialog();
                    deleteTimer.startDeleteTimer(device.getUid(), UserCache.getCurrentUserName(mContext), timing.getTimingId());
                }

                @Override
                public void onRightButtonClick(View view) {

                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        }

        class ViewHolder {
            TextView tvDate;
            LinearLayout epgItems;
        }
    }
}
