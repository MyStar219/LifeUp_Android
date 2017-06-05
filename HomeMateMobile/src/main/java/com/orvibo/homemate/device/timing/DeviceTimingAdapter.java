package com.orvibo.homemate.device.timing;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.ActionView;
import com.orvibo.homemate.view.custom.OnOffCheckbox;

import java.util.List;

/**
 * Created by Allen on 2015/4/3.
 * Modified by smagret on 2015/04/20
 */
public class DeviceTimingAdapter extends BaseAdapter {
    private static final String TAG = DeviceTimingAdapter.class.getSimpleName();
    private List<Timing> timings;
    private LayoutInflater mInflater;
    private Context mContext;
    private View.OnClickListener mOnClickListener;
    private Device mDevice;

    public DeviceTimingAdapter(Context context, List<Timing> timings, View.OnClickListener onClickListener, Device device) {
        mOnClickListener = onClickListener;
        mContext = context;
        this.timings = timings;
        mInflater = LayoutInflater.from(context);
        mDevice = device;
    }

    @Override
    public int getCount() {
        return timings == null ? 0 : timings.size();
    }

    @Override
    public Object getItem(int position) {
        return timings == null ? null : timings.get(position);
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
            convertView = mInflater.inflate(R.layout.device_timing_item, parent, false);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvPeriod = (TextView) convertView.findViewById(R.id.tvPeriod);
            holder.tvAction_tip = (TextView) convertView.findViewById(R.id.tvAction_tip);
            holder.colorView = convertView.findViewById(R.id.colorView);
            holder.tvWeek = (TextView) convertView.findViewById(R.id.tvWeek);
            holder.color_ll = (LinearLayout) convertView.findViewById(R.id.color_ll);
            holder.cbIsPaused = (OnOffCheckbox) convertView.findViewById(R.id.cbIsPaused);
            holder.cbIsPaused.setOnClickListener(mOnClickListener);
            holder.actionView = (ActionView) convertView.findViewById(R.id.av_bindaction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Timing timing = timings.get(position);

        String selectedWeekString = WeekUtil.getWeeks(mContext, timing.getWeek());

        String time = TimeUtil.getTime(mContext, timing.getHour(), timing.getMinute());

        String tempAction = mContext.getResources().getString(R.string.device_timing_action_content);
        String name = timing.getName();
        String actionName = DeviceTool.getActionName(mContext, timing);
        if (!TextUtils.isEmpty(name) && !name.equals("(null)")) {
            actionName = name;
        }
        //   String defaultAction = mContext.getResources().getString(R.string.device_timing_action) + "：";
        String actionString = String.format(tempAction, actionName);
        Action action = new Action(timing.getDeviceId(), timing.getCommand(), timing.getValue1(),
                timing.getValue2(), timing.getValue3(), timing.getValue4(), actionString);
        action.setUid(timing.getUid());
        action.setName(timing.getName());
        action.setFreq(timing.getFreq());
        action.setPluseNum(timing.getPluseNum());
        action.setPluseData(timing.getPluseData());

        //动作：
        holder.tvAction_tip.setText(mContext.getResources().getString(R.string.device_timing_action) + "：");
        //具体动作
        holder.actionView.setAction(action);
        holder.actionView.setActionTextColor(mContext.getResources().getColor(R.color.font_white_gray), true);
        holder.actionView.setTimingActionTextSize();

        String tempRepeat = mContext.getResources().getString(R.string.device_timing_repeat_content);
        String repeat = String.format(tempRepeat, selectedWeekString);

        holder.tvTime.setText(time);
//        holder.tvPeriod.setText(time.substring(6, time.length()));
//        holder.tvAction.setText(actionString);
        holder.tvWeek.setText(repeat);
        holder.cbIsPaused.setChecked(timing.getIsPause() == 1 ? true : false);
        holder.cbIsPaused.setTag(timing);
        convertView.setTag(R.id.tag_timing, timing);
        convertView.setTag(R.id.tag_action, action);
        return convertView;
    }

    public void refresh(List<Timing> timings) {
        this.timings = timings;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView tvTime;
        TextView tvPeriod;
        TextView tvAction_tip;
        TextView tvWeek;
        private View colorView;
        OnOffCheckbox cbIsPaused;
        private LinearLayout color_ll;
        private ActionView actionView;
    }
}
