//package com.orvibo.homemate.device.timing;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Timing;
//import com.orvibo.homemate.common.BaseActivity;
//import com.orvibo.homemate.data.DeviceOrder;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.data.TimingConstant;
//import com.orvibo.homemate.model.ActivateTimer;
//import com.orvibo.homemate.sharedPreferences.UserCache;
//import com.orvibo.homemate.util.TimeUtil;
//import com.orvibo.homemate.util.ToastUtil;
//import com.orvibo.homemate.util.WeekUtil;
//import com.tencent.stat.StatService;
//
//import java.util.List;
//
//public class TimingAdapter extends BaseAdapter{
//    private static final String TAG = TimingAdapter.class.getSimpleName();
//	private List<Timing> timings;
//	private Context mContext;
//
//	private LayoutInflater inflater;
//    private boolean is24HourFormat;
//
//
//	public TimingAdapter(Context context,final List<Timing> timings, boolean is24HourFormat) {
//		mContext = context;
//		this.timings = timings;
//		inflater = LayoutInflater.from(context);
//        this.is24HourFormat = is24HourFormat;
//
//	}
//
//	public void setData(List<Timing> timings) {
//		this.timings = timings;
//		this.notifyDataSetChanged();
//	}
//
//	@Override
//	public int getCount() {
//		return timings.size();
//	}
//
//	@Override
//	public Object getItem(int position) {
//		return timings.get(position);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return 0;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder holder = null;
//		if (convertView == null) {
//			holder = new ViewHolder();
//			convertView = inflater.inflate(R.layout.timing_list_item, parent, false);
//			holder.time_tv = (TextView) convertView.findViewById(R.id.timeTextView);
//            holder.action_tv = (TextView) convertView.findViewById(R.id.actionTextView);
//			holder.week_tv = (TextView) convertView.findViewById(R.id.repeatTextView);
//			holder.timingOnOffImageView = (ImageView) convertView.findViewById(R.id.timingOnOffImageView);
//			convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
//
//		Timing timing = timings.get(position);
//		holder.time_tv.setText(TimeUtil.getTime(mContext, timing.getHour(), timing.getMinute()));
//		holder.week_tv.setText(WeekUtil.getWeeks(mContext, timing.getWeek()));
//        String state = timing.getCommand();
//        if (state.equals(DeviceOrder.ON)) {
//            holder.action_tv.setText(R.string.timing_action_on);
//        } else {
//            holder.action_tv.setText(R.string.timing_action_off);
//        }
//		if (timing.getIsPause() == TimingConstant.TIMEING_PAUSE) {
//			holder.timingOnOffImageView.setImageResource(R.drawable.device_off);
//		} else {
//			holder.timingOnOffImageView.setImageResource(R.drawable.device_on);
//		}
//		holder.timingOnOffImageView.setTag(position);
//		convertView.setTag(R.id.timing, timing);
//		return convertView;
//	}
//
//	private class ViewHolder {
//		private TextView time_tv;
//		private TextView week_tv;
//        private TextView action_tv;
//		private ImageView timingOnOffImageView;
//	}
//}
