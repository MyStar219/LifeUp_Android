package com.orvibo.homemate.device.smartlock;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.DoorLockRecordData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.dao.DoorLockRecordDao;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.LockUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 门锁记录
 * Created by snown on 2015/11/27
 */
public class LockRecordAdapter extends BaseAdapter {

    private LinkedHashMap<String, List<DoorLockRecordData>> doorLockRecordDatas;
    private BaseControlActivity activity;
    private String deviceId;
    private List<DoorLockRecordData> datas = new ArrayList<>();

    private static final int ITEM_TIME = 0;//日期view
    private static final int ITEM_DATA = 1;//数据view
    private Context context = ViHomeProApp.getContext();

    public LockRecordAdapter(LinkedHashMap<String, List<DoorLockRecordData>> doorLockRecordDatas, BaseControlActivity activity, String deviceId) {
        this.doorLockRecordDatas = doorLockRecordDatas;
        this.activity = activity;
        this.deviceId = deviceId;
        updateData(doorLockRecordDatas);
    }

    public void updateData(LinkedHashMap<String, List<DoorLockRecordData>> doorLockRecordDatas) {
        this.doorLockRecordDatas = doorLockRecordDatas;
        datas.clear();
        for (Map.Entry<String, List<DoorLockRecordData>> entry : doorLockRecordDatas.entrySet()) {
            String key = entry.getKey();
            List<DoorLockRecordData> lockRecordDatas = entry.getValue();
            DoorLockRecordData doorLockRecordData = new DoorLockRecordData();
            doorLockRecordData.setTimestamp(key);
            datas.add(doorLockRecordData);
            datas.addAll(lockRecordDatas);
        }
        notifyDataSetChanged();
    }

    // 获取数据量
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public DoorLockRecordData getItem(int position) {
        return datas.get(position);
    }


    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        DoorLockRecordData doorLockRecordData = datas.get(position);
        if (!TextUtils.isEmpty(doorLockRecordData.getTimestamp()))
            return ITEM_TIME;
        else
            return ITEM_DATA;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int itemType = getItemViewType(position);
        TimeHolder timeHolder = null;
        DataHolder dataHolder = null;
        DoorLockRecordData doorLockRecordData = datas.get(position);
        if (convertView == null) {
            if (itemType == ITEM_TIME) {
                timeHolder = new TimeHolder();
                convertView = View.inflate(parent.getContext(), R.layout.item_lock_record_time, null);
                timeHolder.day = (TextView) convertView.findViewById(R.id.day);
                convertView.setTag(timeHolder);
            } else {
                dataHolder = new DataHolder();
                convertView = View.inflate(parent.getContext(), R.layout.item_lock_record, null);
                dataHolder.typeImage = (ImageView) convertView.findViewById(R.id.typeImage);
                dataHolder.memberEdit = (ImageView) convertView.findViewById(R.id.memberEdit);
                dataHolder.recordText = (TextView) convertView.findViewById(R.id.recordText);
                dataHolder.time = (TextView) convertView.findViewById(R.id.time);
                dataHolder.line = convertView.findViewById(R.id.line);
                dataHolder.dataView = convertView.findViewById(R.id.dataView);
                convertView.setTag(dataHolder);
            }
        } else {
            if (itemType == ITEM_TIME) {
                timeHolder = (TimeHolder) convertView.getTag();
            } else {
                dataHolder = (DataHolder) convertView.getTag();
            }
        }
        if (itemType == ITEM_TIME) {
            String[] dates = doorLockRecordData.getTimestamp().split("/");
            if (dates != null && dates.length == 2)
                timeHolder.day.setText(TimeUtil.getFormatNumber(Integer.parseInt(dates[0])) + "/" + TimeUtil.getFormatNumber(Integer.parseInt(dates[1])));
            else
                timeHolder.day.setText(null);
        } else {
            final DoorUserData doorUserData = doorLockRecordData.getDoorUser();
            dataHolder.time.setText(TimeUtil.getHm(doorLockRecordData.getCreateTime()));
            dataHolder.time.setTextAppearance(context, R.style.small_gray);
            dataHolder.typeImage.setImageResource(LockUtil.getLockRecordImage(doorLockRecordData.getType()));
            dataHolder.memberEdit.setVisibility(View.INVISIBLE);
            if (doorUserData != null && TextUtils.isEmpty(doorUserData.getName()) && doorUserData.getDelFlag() == DeleteFlag.NORMAL && doorLockRecordData.getType() != 5) {
                dataHolder.memberEdit.setVisibility(View.VISIBLE);
                dataHolder.memberEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                        dialogFragmentTwoButton.setTitle(String.format(context.getString(R.string.lock_member_set_tip), doorUserData.getAuthorizedId()));
                        dialogFragmentTwoButton.setEditText("TT");
                        dialogFragmentTwoButton.setLeftButtonText(context.getString(R.string.cancel));
                        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                            @Override
                            public void onLeftButtonClick(View view) {

                            }

                            @Override
                            public void onRightButtonClick(View view) {
                                if (!TextUtils.isEmpty(dialogFragmentTwoButton.getEditTextWithCompound())) {
                                    activity.showDialog();
                                    DeviceApi.authSetName(activity.userName, activity.currentMainUid, doorUserData.getDoorUserId(), dialogFragmentTwoButton.getEditTextWithCompound(), new BaseResultListener() {
                                        @Override
                                        public void onResultReturn(BaseEvent baseEvent) {
                                            activity.dismissDialog();
                                            if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                                                doorLockRecordDatas = DoorLockRecordDao.getInstance().getShowRecord(deviceId);
                                                updateData(doorLockRecordDatas);
                                            } else if (baseEvent.getResult() == ErrorCode.TIMEOUT_MD) {
                                                ToastUtil.showToast(context.getString(R.string.TIMEOUT));
                                            } else {
                                                ToastUtil.showToast(context.getString(R.string.FAIL));
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        dialogFragmentTwoButton.show(activity.getFragmentManager(), "");
                    }
                });
            }
            if (position + 1 < datas.size() - 1 && !TextUtils.isEmpty(getItem(position + 1).getTimestamp())) {
                dataHolder.line.setVisibility(View.INVISIBLE);
                dataHolder.dataView.setBackgroundResource(R.drawable.bg_day_bottom);
            } else if (position == datas.size() - 1) {
                dataHolder.line.setVisibility(View.INVISIBLE);
                dataHolder.dataView.setBackgroundResource(R.drawable.bg_day_bottom);
            } else {
                dataHolder.line.setVisibility(View.VISIBLE);
                dataHolder.dataView.setBackgroundResource(R.drawable.bg_day_middle);
            }
            dataHolder.recordText.setVisibility(View.VISIBLE);
            dataHolder.recordText.setText(getRecordText(doorUserData, doorLockRecordData));
        }
        return convertView;
    }

    // 时间holder
    static class TimeHolder {
        public TextView day;
    }

    // ViewHolder用于缓存控件
    static class DataHolder {
        public ImageView typeImage;
        public ImageView memberEdit;
        public TextView time;
        public TextView recordText;
        public View line;
        public View dataView;
    }

    /**
     * 内容提取
     *
     * @param doorUserData
     * @param data
     * @return
     */
    private String getRecordText(DoorUserData doorUserData, DoorLockRecordData data) {
        StringBuilder str = new StringBuilder();
        if (data.getType() == 5) {
            str.append(String.format(context.getString(R.string.lock_record_text1), "", LockUtil.getLockType(data.getType())));
        } else if (doorUserData != null) {
            if (!TextUtils.isEmpty(doorUserData.getName())) {
                str.append(String.format(context.getString(R.string.lock_record_text1), doorUserData.getName(), LockUtil.getLockType(data.getType())));
            } else {
                str.append(String.format(context.getString(R.string.lock_record_text), doorUserData.getAuthorizedId(), LockUtil.getLockType(data.getType())));
            }
        } else if (data.getType() >= 6) {
            str.append(LockUtil.getLockType(data.getType()));
        } else
            str.append(String.format(context.getString(R.string.lock_record_text1), context.getString(R.string.lock_type_expired), LockUtil.getLockType(data.getType())));
        return str.toString();
    }
}
