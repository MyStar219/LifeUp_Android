package com.orvibo.homemate.device.timing;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.dao.CountdownDao;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.view.custom.CountdownTextView;
import com.orvibo.homemate.view.custom.OnOffCheckbox;

import java.util.List;

/**
 * Created by smagret on 2015/12/08
 */
public class DeviceCountdownAdapter extends BaseAdapter implements CountdownTextView.OnCountdownFinishedListener{
    private static final String TAG = DeviceCountdownAdapter.class.getSimpleName();
    private List<Countdown> countdowns;
    private LayoutInflater mInflater;
    private Context mContext;
    private View.OnClickListener mOnClickListener;
    private CountdownDao countdownDao;

    public DeviceCountdownAdapter(Context context, List<Countdown> countdowns, View.OnClickListener onClickListener) {

        mOnClickListener = onClickListener;
        mContext = context;
        this.countdowns = countdowns;
        countdownDao = new CountdownDao();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return countdowns == null ? 0 : countdowns.size();
    }

    @Override
    public Object getItem(int position) {
        return countdowns == null ? null : countdowns.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = mInflater.inflate(R.layout.device_countdown_item, parent, false);
        ViewHolder holder = new ViewHolder(convertView);

        final Countdown countdown = countdowns.get(position);
        String remainTime = DateUtil.getRemainCountdownString(countdown.getStartTime(), countdown.getTime());
        String time = DateUtil.getCountdownString(countdown.getTime());

        holder.tvTime.setText(remainTime);
        if (remainTime.equals("00:00:00")) {
            countdown.setIsPause(0);
            Countdown tempCount = countdownDao.selCountdown(countdown.getCountdownId());
            if (tempCount != null) {
                tempCount.setIsPause(0);
                countdownDao.updCountdown(tempCount);
            }
            holder.tvTime.unRegisterCountdownFinishedListener();
        }

        String tempAction = mContext.getResources().getString(R.string.device_countdown_action_content);
        String actionName = DeviceTool.getActionName(mContext, countdown);
        String name = countdown.getName();
        if (!TextUtils.isEmpty(name)) {
            actionName = name;
        }
        String actionString = String.format(tempAction, actionName);
        Action action = new Action(countdown.getDeviceId(), countdown.getCommand(), countdown.getValue1(),
                countdown.getValue2(), countdown.getValue3(), countdown.getValue4(), actionString);
        action.setName(name);
        action.setFreq(countdown.getFreq());
        action.setPluseNum(countdown.getPluseNum());
        action.setPluseData(countdown.getPluseData());

        holder.tvAction.setText(actionString);
        holder.cbIsPaused.setChecked(countdown.getIsPause() == 1 ? true : false);

        if (countdown.getIsPause() == 1) {
            holder.tvTime.startCountdown(countdown);
        } else {
            holder.tvTime.stopCountdown(time);
        }

//        LogUtil.d(TAG, "getView() - holder.tvTime = " + holder.tvTime);
        holder.cbIsPaused.setTag(countdown);
        convertView.setTag(R.id.tag_countdown, countdown);
        convertView.setTag(R.id.tag_action, action);
        return convertView;
    }

    public void refresh(List<Countdown> countdowns) {
        this.countdowns = countdowns;
        notifyDataSetChanged();
    }

    @Override
    public void onCountdownFinished() {
        notifyDataSetChanged();
    }

    //添加viewHolder
    class ViewHolder {
        CountdownTextView tvTime;
        TextView tvPeriod;
        TextView tvAction;
        OnOffCheckbox cbIsPaused;

        public ViewHolder(View convertView) {
            tvTime = (CountdownTextView) convertView.findViewById(R.id.tvTime);
            tvTime.registerCountdownFinishedListener(DeviceCountdownAdapter.this);
            tvPeriod = (TextView) convertView.findViewById(R.id.tvPeriod);
            tvAction = (TextView) convertView.findViewById(R.id.tvAction);
            cbIsPaused = (OnOffCheckbox) convertView.findViewById(R.id.cbIsPaused);
            cbIsPaused.setOnClickListener(mOnClickListener);
        }
    }
}
