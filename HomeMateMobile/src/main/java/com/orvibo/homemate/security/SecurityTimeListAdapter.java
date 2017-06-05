package com.orvibo.homemate.security;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.OnOffCheckbox;

import java.util.List;

/**
 * Created by wuliquan on 2016/7/21.
 */
public class SecurityTimeListAdapter extends BaseAdapter{

    private static final String TAG = SecurityTimeListAdapter.class.getSimpleName();
    private List<Timing> timings;
    private LayoutInflater mInflater;
    private Context mContext;
    private View.OnClickListener mOnClickListener;


    public SecurityTimeListAdapter(Context context, List<Timing> timings, View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        mContext = context;
        this.timings = timings;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return timings==null?0:timings.size();
    }

    @Override
    public Object getItem(int position) {
        return timings==null?null:timings.get(position);
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
            holder.tvAction = (TextView) convertView.findViewById(R.id.tvAction);
            holder.colorView = convertView.findViewById(R.id.colorView);
            holder.tvWeek = (TextView) convertView.findViewById(R.id.tvWeek);
            holder.color_ll = (LinearLayout) convertView.findViewById(R.id.color_ll);
            holder.cbIsPaused = (OnOffCheckbox) convertView.findViewById(R.id.cbIsPaused);
            holder.cbIsPaused.setOnClickListener(mOnClickListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Timing timing = timings.get(position);

        String selectedWeekString = WeekUtil.getWeeks(mContext, timing.getWeek());

        String time = null;
        time = TimeUtil.getTime(mContext, timing.getHour(), timing.getMinute());

        String tempAction = mContext.getResources().getString(R.string.device_timing_action_content);
        String name = timing.getName();
        String actionName = DeviceTool.getActionName(mContext, timing);
        if (!TextUtils.isEmpty(name)) {
            actionName = name;
        }
        String defaultAction = mContext.getResources().getString(R.string.device_timing_action) + "：";
        String actionString = String.format(tempAction, actionName);

        String tempRepeat = mContext.getResources().getString(R.string.device_timing_repeat_content);
        String repeat = String.format(tempRepeat, selectedWeekString);

        holder.tvTime.setText(time);
        holder.tvAction.setText(actionString);
        holder.tvWeek.setText(repeat);
        holder.cbIsPaused.setChecked(timing.getIsPause() == 1 ? true : false);
        holder.cbIsPaused.setTag(timing);
        convertView.setTag(R.id.tag_timing, timing);

        return convertView;
    }

    class ViewHolder {
        TextView tvTime;
        TextView tvAction;
        TextView tvWeek;
        private View colorView;
        OnOffCheckbox cbIsPaused;
        private LinearLayout color_ll;
    }

}
