package com.orvibo.homemate.device.allone2.epg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kookong.app.data.ChannelEpg;
import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.TimingType;
import com.orvibo.homemate.model.AddTimer;
import com.orvibo.homemate.model.DeleteTimer;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuwei on 2016/4/12.
 */
public class ChannelProgramListAdapter extends BaseAdapter implements View.OnClickListener{

    private Context mContext;
    private ChannelEpg channelEpg;
    private LayoutInflater inflater;
    private NumberFormat numberFormat;
    private ChangeChannelListener mChangeChannelListener;//换台回调接口
    private Device device;
    private int week;
    private int pageIndex;
    private int livingIndex = -1;
    private Map<ChannelEpg.PG, Timing> subscribedTimings = new HashMap<>();
    private TimingDao timingDao;
    private AddTimer addTimer;
    private DeleteTimer deleteTimer;
    private PairProgramHasChannelName pairProgramHasChannelName;

    public ChannelProgramListAdapter(Context context,Device device, PairProgramHasChannelName pairProgramHasChannelName, int week, int pageIndex, ChannelEpg channelEpg, ChangeChannelListener changeChannelListener) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        // 创建一个数值格式化对象
        numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        this.device = device;
        this.pairProgramHasChannelName = pairProgramHasChannelName;
        this.week = week;
        this.pageIndex = pageIndex;
        this.mChangeChannelListener = changeChannelListener;
        this.channelEpg = channelEpg;
        timingDao = new TimingDao();
        initLivingIndex();
        initSubscribeTimings();
        initTimer();
    }

    public void changeData(ChannelEpg channelEpg) {
        this.channelEpg = channelEpg;
        initLivingIndex();
        initSubscribeTimings();
        notifyDataSetChanged();
    }

    public void setChangeChannelListener(ChangeChannelListener changeChannelListener) {
        this.mChangeChannelListener = changeChannelListener;
    }

    /**
     * 正在播的节目
     */
    private void initLivingIndex() {
        if (channelEpg == null || pageIndex != 0) {
            return;
        }
        Calendar nowCalendar = Calendar.getInstance();
        for (int i = 0; i < channelEpg.epgList.size(); i++) {
            ChannelEpg.PG pg = channelEpg.epgList.get(i);
            String[] startHourMinute = pg.time.split(":");
            String[] endHourMinute = pg.etime.split(":");
            if (startHourMinute.length >= 2 && endHourMinute.length >= 2) {
                int startHour = Integer.valueOf(startHourMinute[0]);
                int startMinute = Integer.valueOf(startHourMinute[1]);
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
                startCalendar.set(Calendar.MINUTE, startMinute);
                int endHour = Integer.valueOf(endHourMinute[0]);
                int endMinute = Integer.valueOf(endHourMinute[1]);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
                endCalendar.set(Calendar.MINUTE, endMinute);
                if (nowCalendar.after(startCalendar) && nowCalendar.before(endCalendar)) {
                    livingIndex = i;
                    break;
                }
            }
        }
    }

    private void initSubscribeTimings() {
        if (channelEpg == null) {
            return;
        }
        subscribedTimings.clear();
        ArrayList<Integer> weeks = new ArrayList<>();
        weeks.add(week);
        int week = WeekUtil.getSingleSelectedWeekInt(weeks);
        for (ChannelEpg.PG pg: channelEpg.epgList) {
            String[] times = pg.time.split(":");
            Timing timing = timingDao.selSubscribeTiming(device.getUid(),device.getDeviceId(), pg.resId, pg.typeId, 0, week, Integer.valueOf(times[0]), Integer.valueOf(times[1]));
            if (timing != null) {
                subscribedTimings.put(pg, timing);
            }
        }
    }


    private void initTimer() {
        addTimer = new AddTimer(mContext) {
            @Override
            public void onAddTimerResult(String uid, int serial, int result, String timingId) {
                if(result == ErrorCode.TIMING_EXIST){
                    ToastUtil.showToast(R.string.subscribe_exist);
                } else if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                }
                initSubscribeTimings();
                notifyDataSetChanged();
            }
        };
        deleteTimer = new DeleteTimer(mContext) {
            @Override
            public void onDeleteTimerResult(String uid, int serial, int result) {
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                }
                initSubscribeTimings();
                notifyDataSetChanged();
            }
        };
    }

    public int getLivingIndex(){
        return livingIndex;
    }

    @Override
    public int getCount() {
        if (channelEpg == null) {
            return 0;
        }
        return channelEpg.epgList.size();
    }

    @Override
    public Object getItem(int position) {
        if (channelEpg == null) {
            return null;
        }
        return channelEpg.epgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.epg_channel_programs_list_item, null);
            holder.change_channel = (TextView) convertView.findViewById(R.id.change_channel);
            holder.tv_program_name = (TextView) convertView.findViewById(R.id.tv_program_name);
            holder.tv_program_time = (TextView) convertView.findViewById(R.id.tv_program_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ChannelEpg.PG pg = channelEpg.epgList.get(position);
        holder.change_channel.setVisibility(View.GONE);
        holder.change_channel.setText(R.string.change_channel);
        holder.tv_program_name.setTextColor(mContext.getResources().getColor(R.color.black));
        holder.tv_program_time.setTextColor(mContext.getResources().getColor(R.color.black));
        convertView.setBackgroundResource(R.color.transparent);
        if (livingIndex > position) {
            holder.tv_program_name.setTextColor(mContext.getResources().getColor(R.color.gray));
            holder.tv_program_time.setTextColor(mContext.getResources().getColor(R.color.gray));
            convertView.setBackgroundResource(R.color.title_bar_bg);
        } else if (livingIndex == position) {
            holder.tv_program_name.setTextColor(mContext.getResources().getColor(R.color.green));
            holder.tv_program_time.setTextColor(mContext.getResources().getColor(R.color.green));
            holder.change_channel.setVisibility(View.VISIBLE);
        } else {
            holder.change_channel.setVisibility(View.VISIBLE);
            if (subscribedTimings.get(pg) == null) {
                holder.change_channel.setText(R.string.program_subscribe);
            } else {
                holder.change_channel.setText(R.string.program_subscribed);
            }
        }
        holder.tv_program_name.setText(pg.name);
        holder.tv_program_time.setText(pg.time);
        holder.change_channel.setOnClickListener(this);
        holder.change_channel.setTag(pg);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_channel: {
                TextView textView = (TextView) v;
                if (textView.getText().toString().equals(mContext.getString(R.string.change_channel))) {
                    if (mChangeChannelListener != null) {
                        mChangeChannelListener.changeChannelClick(null);
                    }
                } else {
                    ChannelEpg.PG pg = (ChannelEpg.PG) v.getTag();
                    Timing timing = subscribedTimings.get(pg);
                    if (timing == null) {
                        timing = new Timing();
                        timing.setName(pg.name);
                        timing.setDeviceId(device.getDeviceId());
                        String[] times = pg.time.split(":");
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTimeInMillis(System.currentTimeMillis()+2*60*1000);
//                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                        int minute = calendar.get(Calendar.MINUTE);
//                        timing.setHour(hour);
//                        timing.setMinute(minute);
                        timing.setHour(Integer.valueOf(times[0]));
                        timing.setMinute(Integer.valueOf(times[1]));
                        ArrayList<Integer> weeks = new ArrayList<>();
                        weeks.add(week);
                        int week = WeekUtil.getSingleSelectedWeekInt(weeks);
                        timing.setWeek(week);
                        timing.setCommand(DeviceOrder.IR_CONTROL);
                        timing.setTimingType(TimingType.SUBSCRIBE);
                        timing.setResourceId(pg.resId);
                        timing.setTypeId(pg.typeId);
                        timing.setIsHD(0);
                        IrData irData = AlloneCache.getIrData(device.getDeviceId());
                        if (irData != null) {
                            timing.setFreq(irData.fre);
                            timing.setPluseNum(0);
                        }
                        timing.setPluseData(pairProgramHasChannelName.getChannelInfo().pulse);
                        addTimer.startAddTiming(device.getUid(), UserCache.getCurrentUserName(mContext), timing);
                        ((TextView) v).setText(R.string.program_subscribed);
                    } else {
                        deleteTimer.startDeleteTimer(device.getUid(), UserCache.getCurrentUserName(mContext), timing.getTimingId());
                        ((TextView) v).setText(R.string.program_subscribe);
                    }
                }
                break;
            }
        }
    }

    class ViewHolder {
        TextView change_channel;
        TextView tv_program_name;
        TextView tv_program_time;
    }
}
