package com.orvibo.homemate.messagepush;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.StatusRecord;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.PinnedSectionListView.PinnedSectionListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 传感器的状态记录适配器
 * Created by baoqi on 2016/7/6.
 */
public class SensorStatusRecordAdapter extends BaseAdapter implements PinnedSectionListAdapter {
    private static final String TAG = SensorStatusRecordAdapter.class.getSimpleName();
    private LinkedHashMap<String, List<StatusRecord>> mStatusRecordsMap;
    private List<StatusRecord> mStatusRecords;
    private Context mContext;
    private static final int ITEM_STATUSRECORDS = 0;
    private static final int ITEM_DATESTATUSRECORDS = 1;
    private static final int ITEM_LASTSTATUSRECORDS = 2;
    private LayoutInflater mInflater;
    private Device mDevice;


    public SensorStatusRecordAdapter(Context context, Device device, LinkedHashMap<String, List<StatusRecord>> statusRecordsMap) {
        mContext = context;
        mStatusRecordsMap = statusRecordsMap;
        mDevice = device;
        mInflater = LayoutInflater.from(context);
        mStatusRecords = new ArrayList<StatusRecord>();
    }

    public void setDate(LinkedHashMap<String, List<StatusRecord>> statusRecordsMap) {
        this.mStatusRecordsMap = statusRecordsMap;
        // 对集合的数据处理
        processDate(statusRecordsMap);

    }

    /**
     * 把map集合的数据放到list集合
     *
     * @param statusRecords
     */
    private void processDate(LinkedHashMap<String, List<StatusRecord>> statusRecords) {
        //再次设置数据时把原来的数据清空
        mStatusRecords.clear();
        if (statusRecords != null && statusRecords.size() > 0) {
            for (String monthDay : mStatusRecordsMap.keySet()) {
                StatusRecord monthDayItem = new StatusRecord();//日期的条目
                monthDayItem.setStatusRecordType(ITEM_DATESTATUSRECORDS);
                monthDayItem.setDate(monthDay);
                mStatusRecords.add(monthDayItem);
                List<StatusRecord> statusRecordList = mStatusRecordsMap.get(monthDay);
                for (StatusRecord statusRecord : statusRecordList) {//状态的条目
                    monthDayItem.setCreateTime(statusRecord.getCreateTime());
                    statusRecord.setStatusRecordType(ITEM_STATUSRECORDS);
                    mStatusRecords.add(statusRecord);
                }
            }
            if (mStatusRecords != null && !mStatusRecords.isEmpty() && mStatusRecords.size() > 1) {
                mStatusRecords.get(mStatusRecords.size() - 1).setStatusRecordType(ITEM_LASTSTATUSRECORDS);
            }
        }
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == ITEM_DATESTATUSRECORDS;
    }

    @Override
    public int getCount() {
        if (mStatusRecords != null) {
            return mStatusRecords.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mStatusRecords != null) {
            return mStatusRecords.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StatusRecord statusRecord = mStatusRecords.get(position);
        LogUtil.d(TAG, "position=" + position + "...itemType=" + getItemViewType(position) + "...statusRecord=" + statusRecord );
        if (getItemViewType(position) == ITEM_STATUSRECORDS) {
            //状态记录的条目
            StatusRecordViewHolder statusRecordHolder;
            if (convertView == null) {
                statusRecordHolder = new StatusRecordViewHolder();
                convertView = mInflater.inflate(R.layout.statusrecord_item, parent, false);
                statusRecordHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeText);
                statusRecordHolder.statusRecordTextView = (TextView) convertView.findViewById(R.id.statusRecordText);
                statusRecordHolder.imageViewLine = (View) convertView.findViewById(R.id.image_line);
                convertView.setTag(statusRecordHolder);
            } else {
                statusRecordHolder = (StatusRecordViewHolder) convertView.getTag();
            }
            //设置数据
            if (statusRecord != null) {

                if (PhoneUtil.isCN(mContext)) {
                    statusRecordHolder.timeTextView.setText(TimeUtil.millisecondToForMatTime(statusRecord.getCreateTime()));
                } else {
                    statusRecordHolder.timeTextView.setText(TimeUtil.millisecondToForMatTimeOffSet(statusRecord.getCreateTime()));
                }
                 statusRecordHolder.statusRecordTextView.setText(statusRecord.getText());

            }


        } else if (getItemViewType(position) == ITEM_DATESTATUSRECORDS) {
            //星期的条目
            DateViewHolder dateViewHolder;
            if (convertView == null) {
                dateViewHolder = new DateViewHolder();
                convertView = mInflater.inflate(R.layout.unstatusrecord_item, parent, false);
                dateViewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                convertView.setTag(dateViewHolder);
            } else {
                dateViewHolder = (DateViewHolder) convertView.getTag();
            }
            // 设置日期
            if (statusRecord != null) {
                dateViewHolder.dateTextView.setText(statusRecord.getDate() + " " + TimeUtil.getWeekDay(statusRecord.getCreateTime()));
            }
        } else if (getItemViewType(position) == ITEM_LASTSTATUSRECORDS) {
            //状态记录的条目
            LastStatusRecordViewHolder lastStatusRecordViewHolder;
            if (convertView == null) {
                lastStatusRecordViewHolder = new LastStatusRecordViewHolder();
                convertView = mInflater.inflate(R.layout.statusrecord_item, parent, false);
                lastStatusRecordViewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeText);
                lastStatusRecordViewHolder.statusRecordTextView = (TextView) convertView.findViewById(R.id.statusRecordText);
                lastStatusRecordViewHolder.imageViewLine = (View) convertView.findViewById(R.id.image_line);
                convertView.setTag(lastStatusRecordViewHolder);
            } else {
                lastStatusRecordViewHolder = (LastStatusRecordViewHolder) convertView.getTag();
            }
            //设置数据
            if (statusRecord != null) {
//                if (android.text.format.DateFormat.is24HourFormat(mContext)) {
//                    statusRecordHolder.timeTextView.setText(TimeUtil.getDayofHMS(statusRecord.getCreateTime()));
//                } else {
//                    statusRecordHolder.timeTextView.setText(TimeUtil.millisecondToHour(statusRecord.getCreateTime()));
//                }
                if (PhoneUtil.isCN(mContext)) {
                    lastStatusRecordViewHolder.timeTextView.setText(TimeUtil.millisecondToForMatTime(statusRecord.getCreateTime()));
                } else {
                    lastStatusRecordViewHolder.timeTextView.setText(TimeUtil.millisecondToForMatTimeOffSet(statusRecord.getCreateTime()));
                }
                lastStatusRecordViewHolder.statusRecordTextView.setText(statusRecord.getText());
                lastStatusRecordViewHolder.imageViewLine.setVisibility(View.GONE);

            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mStatusRecords != null) {
            if (mStatusRecords.get(position).getStatusRecordType() == ITEM_STATUSRECORDS) {
                return ITEM_STATUSRECORDS;
            } else if (mStatusRecords.get(position).getStatusRecordType() == ITEM_DATESTATUSRECORDS) {
                return ITEM_DATESTATUSRECORDS;
            } else if (mStatusRecords.get(position).getStatusRecordType() == ITEM_LASTSTATUSRECORDS) {
                return ITEM_LASTSTATUSRECORDS;
            }
        }
        return ITEM_STATUSRECORDS;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    /**
     * 最后一条状态记录的缓存条目
     */
    private class LastStatusRecordViewHolder {
        public TextView timeTextView;
        public TextView statusRecordTextView;
        public View imageViewLine;

    }

    /**
     * 状态记录的缓存条目
     */
    private class StatusRecordViewHolder {
        public TextView timeTextView;
        public TextView statusRecordTextView;
        public View imageViewLine;

    }

    /**
     * 日期的缓存条目
     */
    private class DateViewHolder {
        public TextView dateTextView;

    }

}
