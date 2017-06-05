package com.orvibo.homemate.device.timing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnNewPropertyReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.bo.TimingGroup;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.core.load.LoadTarget;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.dao.TimingGroupDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TableName;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.device.DeviceFragment;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.ActivateTimingGroup;
import com.orvibo.homemate.model.PropertyReport;
import com.orvibo.homemate.model.base.RequestConfig;
import com.orvibo.homemate.model.device.DeviceDeletedReport;
import com.orvibo.homemate.api.listener.OnDeviceDeletedListener;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.OnOffCheckbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allen on 2016/7/9.
 */
public class ModeTimingListActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        OnNewPropertyReportListener,
        OnDeviceDeletedListener {
    private NavigationGreenBar nbTitle;
    private ListView lvTiming;
    private View emptyView;
    private TextView textViewAction, tvAddTimer;
    private ModeTimingAdapter modeTimingAdapter;
    private Device device;
    private List<TimingGroup> timingGroups;
    private Map<TimingGroup, List<Timing>> timingsMap;
    private TimingGroupDao timingGroupDao;
    private TimingDao timingDao;
    private ActivateTimingGroup activateTimingGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_list);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            init();
            PropertyReport.getInstance(mContext).registerNewPropertyReport(this);
            mIntentSource = intent.getStringExtra(IntentKey.INTENT_SOURCE);
            DeviceDeletedReport.getInstance().addDeviceDeletedListener(this);
        } else {
            finish();
        }
    }

    private void init() {
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        nbTitle.setText(getString(R.string.mode_title));
        nbTitle.setRightImageViewRes(R.drawable.white_add_selector);
        lvTiming = (ListView) findViewById(R.id.lvTiming);
        lvTiming.setOnItemClickListener(this);
        emptyView = findViewById(R.id.layout_empty);
        lvTiming.setEmptyView(emptyView);
        textViewAction = (TextView) emptyView.findViewById(R.id.textViewAction);
        textViewAction.setText(R.string.mode_list_empty);
        tvAddTimer = (TextView) emptyView.findViewById(R.id.tvAddTimer);
        tvAddTimer.setText(R.string.mode_add_title);
        tvAddTimer.setOnClickListener(this);
        timingsMap = new HashMap<>();
        timingGroupDao = new TimingGroupDao();
        timingDao = new TimingDao();
        initActivateTimingGroup();
    }

    private void initActivateTimingGroup() {
        activateTimingGroup = new ActivateTimingGroup(mContext) {
            @Override
            public void onActivateTimingGroupResult(int serial, int result, String timingGroupId) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    for (TimingGroup timingGroup : timingGroups) {
                        if (!timingGroup.getTimingGroupId().equals(timingGroupId)) {
                            timingGroup.setIsPause(TimingConstant.TIMEING_PAUSE);
                            timingGroupDao.updTimingGroup(timingGroup);
                        }
                    }
                    refresh();
                    modeTimingAdapter.notifyDataSetChanged();

                    LoadParam loadParam = new LoadParam();
                    loadParam.notifyRefresh = true;
                    loadParam.requestConfig = RequestConfig.getOnlyRemoteConfig();
                    LoadTarget loadTarget = new LoadTarget();
                    loadTarget.uid = device.getUid();
                    loadTarget.target = device.getUid();
                    loadTarget.tableName = TableName.TIMING_GROUP;
                    loadParam.loadTarget = loadTarget;
                    LoadUtil.noticeLoadTable(loadParam);
//                    LoadUtil.noticeLoadTable(device.getUid(), TableName.TIMING_GROUP);
                } else if (result == ErrorCode.TIMING_EXIST) {
                    DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                    dialogFragmentOneButton.setTitle(getString(R.string.TIMING_EXIST));
                    dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                    dialogFragmentOneButton.show(getFragmentManager(), "");
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        PropertyReport.getInstance(mContext).unregisterNewPropertyReport(this);
        DeviceDeletedReport.getInstance().removeDeviceDeletedListener(this);
        super.onDestroy();
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        refresh();
        modeTimingAdapter.notifyDataSetChanged();
    }

    private void refresh() {
        timingsMap.clear();
        timingGroups = timingGroupDao.selTimingGroupsByDeviceId(device.getUid(), device.getDeviceId());
        List<TimingGroup> sortTimingGroups = new ArrayList<>();
        for (int i = 0; i < timingGroups.size(); ) {
            TimingGroup timingGroup = timingGroups.get(i);
            List<Timing> timings = timingDao.selTimingsByTimingGroupId(timingGroup.getUid(), timingGroup.getTimingGroupId());
            // 一个模式包含两个定时，如果没有两个定时，不会添加进来，同时也在timingGroups中移除，
            // 使用的时候就无需写那么多判断条件，从而减少代码量，增加可读性，提高运行效率...总之，某些限定规则越早处理越好。
            if (timings.size() >= 2) {
                sortTimingGroup(sortTimingGroups, timingGroup, timings);
                timingsMap.put(timingGroup, timings);
                i++;
            } else {
                timingGroups.remove(i);
            }
        }
        timingGroups = sortTimingGroups;
        if (timingGroups.isEmpty()) {
            nbTitle.setRightImageViewVisibility(View.GONE);
        } else {
            nbTitle.setRightImageViewVisibility(View.VISIBLE);
        }
    }

    /**
     * 先比较开始时间，开始时间早的在前面，如果开始时间相同再比较结束时间，结束时间早的在前面
     *
     * @param sortTimingGroups
     * @param timingGroup
     * @param timings
     */
    private void sortTimingGroup(List<TimingGroup> sortTimingGroups, TimingGroup timingGroup, List<Timing> timings) {
        if (sortTimingGroups.isEmpty()) {//没有东西比较，直接添加
            sortTimingGroups.add(timingGroup);
            return;
        }
        for (int i = 0; i < sortTimingGroups.size(); i++) {
            TimingGroup comparedTimingGroup = sortTimingGroups.get(i);
            List<Timing> comparedTimings = timingsMap.get(comparedTimingGroup);
            int comparedHour = comparedTimings.get(0).getHour();
            int comparedMinute = comparedTimings.get(0).getMinute();
            int hour = timings.get(0).getHour();
            int minute = timings.get(0).getMinute();
            if (comparedHour == hour && comparedMinute == minute) {//开始时间相同再比较结束时间，结束时间早的在前面
                comparedHour = comparedTimings.get(1).getHour();
                comparedMinute = comparedTimings.get(1).getMinute();
                hour = timings.get(1).getHour();
                minute = timings.get(1).getMinute();
            }
            Calendar comparedCalendar = Calendar.getInstance();
            comparedCalendar.set(Calendar.HOUR_OF_DAY, comparedHour);
            comparedCalendar.set(Calendar.MINUTE, comparedMinute);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            if (calendar.before(comparedCalendar)) {
                sortTimingGroups.add(i, timingGroup);
                break;
            } else if (i == sortTimingGroups.size() - 1) {
                sortTimingGroups.add(timingGroup);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        if (modeTimingAdapter == null) {
            modeTimingAdapter = new ModeTimingAdapter();
            lvTiming.setAdapter(modeTimingAdapter);
        } else {
            modeTimingAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cbIsPaused: {
                showDialog();
                TimingGroup timingGroup = (TimingGroup) v.getTag();
                activateTimingGroup.startActivateTimingGroup(timingGroup.getTimingGroupId(), timingGroup.getIsPause() == TimingConstant.TIMEING_EFFECT ? TimingConstant.TIMEING_PAUSE : TimingConstant.TIMEING_EFFECT);
                break;
            }
            case R.id.tvAddTimer:
                Intent intent = new Intent(mContext, ModeTimingCreateActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void rightTitleClick(View view) {
        Intent intent = new Intent(mContext, ModeTimingCreateActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, ModeTimingCreateActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        intent.putExtra(IntentKey.TIMING_GROUP, timingGroups.get(position));
        startActivity(intent);
    }

    @Override
    public void onNewPropertyReport(Device device, DeviceStatus deviceStatus, PayloadData payloadData) {
        //检测到属性报告上来，如果是同一个设备且设置的动作与返回的属性状态一样，进行读表
        if (device.getDeviceId().equals(this.device.getDeviceId())
                && timingGroups != null
                && !timingGroups.isEmpty()) {
            LoadParam loadParam = new LoadParam();
            loadParam.notifyRefresh = true;
            loadParam.requestConfig = RequestConfig.getOnlyRemoteConfig();
            LoadTarget loadTarget = new LoadTarget();
            loadTarget.uid = device.getUid();
            loadTarget.target = device.getUid();
            loadTarget.tableName = TableName.TIMING_GROUP;
            loadParam.loadTarget = loadTarget;
            LoadUtil.noticeLoadTable(loadParam);
//            LoadUtil.noticeLoadTable(device.getUid(), TableName.TIMING_GROUP);
        }
    }

    @Override
    public void onDeviceDeleted(String uid, String deviceId) {
        //从DeviceFragment直接进入此页面，当监听到此设备被删除时直接调回首页
        if (!TextUtils.isEmpty(uid) && device != null
                && uid.equals(device.getUid())
                && !TextUtils.isEmpty(mIntentSource)
                && mIntentSource.equals(DeviceFragment.class.getSimpleName())) {
            if (TextUtils.isEmpty(deviceId) || deviceId.equals(device.getDeviceId())) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    public class ModeTimingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return timingGroups == null ? 0 : timingGroups.size();
        }

        @Override
        public Object getItem(int position) {
            return timingGroups == null ? null : timingGroups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (null == convertView) {
                holder = new ViewHolder();
                //借用zigBee设备的定时布局，注意：定时跟模式显示样式不相同，视图的名称跟id会对不上
                convertView = View.inflate(mContext, R.layout.item_mode_timing, null);
                holder.tvModeName = (TextView) convertView.findViewById(R.id.tvTime);
                holder.tvPeriod = (TextView) convertView.findViewById(R.id.tvAction);
                holder.tvWeek = (TextView) convertView.findViewById(R.id.tvWeek);
                holder.cbIsPaused = (OnOffCheckbox) convertView.findViewById(R.id.cbIsPaused);
                holder.cbIsPaused.setOnClickListener(ModeTimingListActivity.this);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 显示模式名称
            TimingGroup timingGroup = timingGroups.get(position);
            holder.tvModeName.setText(timingGroup.getName());

            // 显示时间
            List<Timing> timings = timingsMap.get(timingGroup);
            // 查询数据库的时候根据showIndex排序，第一个是开始时间，第二个是结束时间
            // 不必担心数字越界问题，在timingMap.put的时候会限定timings数量必须大于2
            Timing beginTiming = timings.get(0);
            Timing endTiming = timings.get(1);
            String beginTime = TimeUtil.getTime(mContext, beginTiming.getHour(), beginTiming.getMinute());
            String endTime = TimeUtil.getTime(mContext, endTiming.getHour(), endTiming.getMinute());
            holder.tvPeriod.setText(String.format(getString(R.string.mode_period), beginTime, endTime));

            //显示重复周期
            String tempRepeat = mContext.getResources().getString(R.string.device_timing_repeat_content);
            String selectedWeekString = WeekUtil.getWeeks(mContext, beginTiming.getWeek());//beginTiming与endTiming的week是一样的
            String repeat = String.format(tempRepeat, selectedWeekString);
            holder.tvWeek.setText(repeat);

            //显示激活状态
            holder.cbIsPaused.setChecked(timingGroup.getIsPause() == TimingConstant.TIMEING_EFFECT);
            holder.cbIsPaused.setTag(timingGroup);

            convertView.setTag(R.id.tag_timing, timingGroup);
            return convertView;
        }

        class ViewHolder {
            TextView tvModeName;
            TextView tvPeriod;
            TextView tvWeek;
            OnOffCheckbox cbIsPaused;
        }
    }
}
