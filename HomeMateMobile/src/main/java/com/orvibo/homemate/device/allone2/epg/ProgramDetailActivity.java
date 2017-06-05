package com.orvibo.homemate.device.allone2.epg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.model.legacy.api.data.EPGProgramData;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.ChannelCollection;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.dao.ChannelCollectionDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.TimingType;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.AddTimer;
import com.orvibo.homemate.model.CancelCollectChannel;
import com.orvibo.homemate.model.CollectChannel;
import com.orvibo.homemate.model.DeleteTimer;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.sharedPreferences.SubscribeTipsCache;
import com.orvibo.homemate.sharedPreferences.UpdateTimeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.ProgramSubscribeDialogFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by allen on 2016/7/15.
 */
public class ProgramDetailActivity extends BaseAlloneControlActivity implements BaseResultListener {
    private NavigationGreenBar navigationGreenBar;
    private ImageView ivProgram;
    private ImageView ivCircleProgram;
    private TextView tvChannel, tvProgram, tvChangeChannel;
    private ListView lvLivingTime;
    private ProgramLivingTimingAdapter programLivingTimingAdapter;
    private PairProgramHasChannelName pairProgramHasChannelName;
    private EPGProgramData epgProgramData;
    private TimingDao timingDao;
    private ChannelCollectionDao channelCollectionDao;
    private ChannelCollection channelCollection;
    private CollectChannel collectChannel;
    private CancelCollectChannel cancelCollectChannel;
    private AddTimer addTimer;
    private DeleteTimer deleteTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_detail);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.PAIR_PROGRAM_HAS_CHANNEL_NAME);
        if (serializable != null && serializable instanceof PairProgramHasChannelName) {
            pairProgramHasChannelName = (PairProgramHasChannelName) serializable;
            init();
        } else {
            finish();
        }
    }

    private void init() {
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        tvChannel = (TextView) findViewById(R.id.tvChannel);
        tvChannel.setText(pairProgramHasChannelName.getChannelInfo().name);
        tvProgram = (TextView) findViewById(R.id.tvProgram);
        tvProgram.setText(pairProgramHasChannelName.getPairProgram().sn);
        ivProgram = (ImageView) findViewById(R.id.ivProgram);
        ImageLoader.getInstance().displayImage(pairProgramHasChannelName.getPairProgram().thumb, ivProgram, ViHomeApplication.getImageOptions());
        ivCircleProgram = (ImageView) findViewById(R.id.ivCircleProgram);
        ImageLoader.getInstance().displayImage(pairProgramHasChannelName.getPairProgram().thumb, ivCircleProgram, ViHomeApplication.getCircleImageOptions(0));
        tvChangeChannel = (TextView) findViewById(R.id.tvChangeChannel);
        tvChangeChannel.setOnClickListener(this);
        lvLivingTime = (ListView) findViewById(R.id.lvLivingTime);
        programLivingTimingAdapter = new ProgramLivingTimingAdapter();
        lvLivingTime.setAdapter(programLivingTimingAdapter);
        timingDao = new TimingDao();
        channelCollectionDao = new ChannelCollectionDao();
        initTimer();
        initCollectChannel();
//        loadIrData(device.getIrDeviceId(), pairProgramHasChannelName.getChannelInfo().pulse);
        getLivingTimes();
        refresh();
    }

    private void initTimer() {
        addTimer = new AddTimer(mContext) {
            @Override
            public void onAddTimerResult(String uid, int serial, int result, String timingId) {
                dismissDialog();
                if(result == ErrorCode.TIMING_EXIST){
                    ToastUtil.showToast(R.string.subscribe_exist);
                } else if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else if (!SubscribeTipsCache.getSubcribeTips(mAppContext)) {
                    ProgramSubscribeDialogFragment programSubscribeDialogFragment = new ProgramSubscribeDialogFragment();
                    programSubscribeDialogFragment.init(mContext, device);
                    programSubscribeDialogFragment.show(getFragmentManager(), "");
                }
                programLivingTimingAdapter.refresh(epgProgramData);
            }
        };
        deleteTimer = new DeleteTimer(mContext) {
            @Override
            public void onDeleteTimerResult(String uid, int serial, int result) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                }
                programLivingTimingAdapter.refresh(epgProgramData);
            }
        };
    }

    private void initCollectChannel() {
        collectChannel = new CollectChannel() {
            @Override
            public void onCollectChannelResult(int result) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    EventBus.getDefault().post(new CollectDataRefresh(pairProgramHasChannelName));
                    ToastUtil.showToast(R.string.collect_success);
                }
                refresh();
            }
        };

        cancelCollectChannel = new CancelCollectChannel() {
            @Override
            public void onCancelCollectChannelResult(int result) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else {
                    EventBus.getDefault().post(new CollectDataRefresh(pairProgramHasChannelName));
                    ToastUtil.showToast(R.string.cancel_collect_success);
                }
                refresh();
            }
        };
    }

    private void getLivingTimes() {
        KookongSDK.searchProgram(pairProgramHasChannelName.getPairProgram().resId, pairProgramHasChannelName.getPairProgram().typeId, new IRequestResult<EPGProgramData>() {
            @Override
            public void onSuccess(String s, EPGProgramData epgProgramData) {
                ProgramDetailActivity.this.epgProgramData = epgProgramData;
                programLivingTimingAdapter.refresh(epgProgramData);
            }

            @Override
            public void onFail(String s) {
                ToastUtil.showToast(s);
            }
        });
    }

    private void refresh() {
        channelCollection = channelCollectionDao.selChannelCollection(device.getUid(), deviceId, pairProgramHasChannelName.getChannelInfo().channelId);
        if (channelCollection == null) {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_collection_normal);
        } else {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_collection_press);
        }
    }

    @Override
    public void rightTitleClick(View view) {
        if (channelCollection == null) {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_collection_press);
            channelCollection = new ChannelCollection();
            channelCollection.setUid(device.getUid());
            channelCollection.setDeviceId(deviceId);
            channelCollection.setChannelId(pairProgramHasChannelName.getChannelInfo().channelId);
            channelCollection.setIsHd(pairProgramHasChannelName.getChannelInfo().isHd);
            channelCollection.setCountryId(pairProgramHasChannelName.getChannelInfo().countryId);
            channelCollection.setUpdateTime(UpdateTimeCache.getUpdateTime(mContext, device.getUid()));
            collectChannel.startCollectChannel(mContext, channelCollection);
        } else {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_collection_normal);
            cancelCollectChannel.startCancelCollectChannel(mContext, channelCollection.getChannelCollectionId());
        }
        showDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (epgProgramData != null) {
            programLivingTimingAdapter.refresh(epgProgramData);
        }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        super.onRefresh(event);
        MyLogger.kLog().d(event);
        if (epgProgramData != null) {
            programLivingTimingAdapter.refresh(epgProgramData);
        }
    }

    private class ProgramLivingTimingAdapter extends BaseAdapter implements View.OnClickListener{
        private List<String> epgDates = new ArrayList<>();
        private Map<String, List<EPGProgramData.EPGData>> epgDataMap = new HashMap<>();
        private Map<EPGProgramData.EPGData, Timing> subscribedTimings = new HashMap<>();

        public void refresh(EPGProgramData epgProgramData) {
            epgDates.clear();
            epgDataMap.clear();
            subscribedTimings.clear();
            for (EPGProgramData.EPGData epgData: epgProgramData.getEPGList()) {
                List<EPGProgramData.EPGData> epgDataList;
                if(epgDates.contains(epgData.d)) {
                    epgDataList = epgDataMap.get(epgData.d);
                } else {
                    epgDataList = new ArrayList<>();
                    epgDataMap.put(epgData.d, epgDataList);
                    epgDates.add(epgData.d);
                }
                epgDataList.add(epgData);
                int week = TimeUtil.getWeekInt(epgData.d, "yy-MM-dd");
                String[] times = epgData.t.split(":");
                Timing timing = timingDao.selSubscribeTiming(device.getUid(), device.getDeviceId(), epgData.rid, epgData.tid, epgData.isHd, week, Integer.valueOf(times[0]), Integer.valueOf(times[1]));
                if (timing != null) {
                    subscribedTimings.put(epgData, timing);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return epgDates.size();
        }

        @Override
        public Object getItem(int position) {
            return epgDates.get(position);
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
            String date = epgDates.get(position);
            List<EPGProgramData.EPGData> epgDataList = epgDataMap.get(date);
            viewHolder.tvDate.setText(TimeUtil.getDateFormatString(mContext, date, "yy-MM-dd"));
            for (int i = 0; i < viewHolder.epgItems.getChildCount(); i++) {
                View epgItem = viewHolder.epgItems.getChildAt(i);
                epgItem.setVisibility(View.GONE);
            }
            Date now = new Date();
            for (int i = 0; i < epgDataList.size(); i++) {
                EPGProgramData.EPGData epgData = epgDataList.get(i);
                View epgItem = viewHolder.epgItems.getChildAt(i);
                if (epgItem == null) {
                    epgItem = View.inflate(mContext, R.layout.activity_program_detail_epg_item, null);
                    viewHolder.epgItems.addView(epgItem);
                } else {
                    epgItem.setVisibility(View.VISIBLE);
                }
                TextView tvIsHd = (TextView) epgItem.findViewById(R.id.tvIsHd);
                if (epgData.isHd == 0) {
                    tvIsHd.setText(R.string.program_normal);
                } else {
                    tvIsHd.setText(R.string.program_hd);
                }
                TextView tvTime = (TextView) epgItem.findViewById(R.id.tvTime);
                TextView tvSubscribe = (TextView) epgItem.findViewById(R.id.tvSubscribe);
                tvSubscribe.setVisibility(View.VISIBLE);
                if(epgData.endTime.before(now)) {
                    tvTime.setText(epgData.t);
                    tvSubscribe.setVisibility(View.GONE);
                } else if (epgData.startTime.before(now) && epgData.endTime.after(now)) {
                    tvTime.setText(R.string.program_living);
                    tvSubscribe.setVisibility(View.GONE);
                } else {
                    tvTime.setText(epgData.t);
                    if (subscribedTimings.get(epgData) == null) {
                        tvSubscribe.setBackgroundResource(R.drawable.bt_order_ellipse_normal);
                        tvSubscribe.setTextColor(getResources().getColor(R.color.green));
                        tvSubscribe.setText(R.string.program_subscribe);
                    } else {
                        tvSubscribe.setBackgroundResource(R.drawable.bt_order_ellipse_press);
                        tvSubscribe.setTextColor(getResources().getColor(R.color.white));
                        tvSubscribe.setText(R.string.program_subscribed);
                    }
                }
                tvSubscribe.setOnClickListener(this);
                tvSubscribe.setTag(epgData);
            }
            return convertView;
        }

        @Override
        public void onClick(View v) {
            EPGProgramData.EPGData epgData = (EPGProgramData.EPGData) v.getTag();
            showDialog();
            Timing timing = subscribedTimings.get(epgData);
            if (timing == null) {
                timing = new Timing();
                timing.setName(epgData.n);
                timing.setDeviceId(device.getDeviceId());
                String[] times = epgData.t.split(":");
                timing.setHour(Integer.valueOf(times[0]));
                timing.setMinute(Integer.valueOf(times[1]));
                int week = TimeUtil.getWeekInt(epgData.d, "yy-MM-dd");
                timing.setWeek(week);
                timing.setCommand(DeviceOrder.IR_CONTROL);
                timing.setTimingType(TimingType.SUBSCRIBE);
                timing.setResourceId(epgData.rid);
                timing.setTypeId(epgData.tid);
                timing.setIsHD(epgData.isHd);
                IrData irData = AlloneCache.getIrData(deviceId);
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

        class ViewHolder {
            TextView tvDate;
            LinearLayout epgItems;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tvChangeChannel:
                IrData irData = AlloneCache.getIrData(deviceId);
                if (irData == null) {
                    loadIrData(device.getIrDeviceId(), pairProgramHasChannelName.getChannelInfo().pulse);
                } else {
                    control(irData, pairProgramHasChannelName.getChannelInfo().pulse);
                }
                break;
        }
    }

    /**
     * 根据rid获取对应的红外码
     *
     * @param rid
     */
    public void loadIrData(String rid, final String pulse) {
        showDialog();
        KookongSDK.getIRDataById(rid, new IRequestResult<IrDataList>() {

            @Override
            public void onSuccess(String msg, IrDataList result) {
                dismissDialog();
                List<IrData> irDatas = result.getIrDataList();
                IrData irData = irDatas.get(0);
                AlloneCache.saveIrData(irData, deviceId);
                control(irData, pulse);
            }

            @Override
            public void onFail(String msg) {
                dismissDialog();
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    private void control(IrData irData, String pulse) {
        if (ClickUtil.isFastDoubleClick(5000))
            return;
        String[] numberStr = pulse.split("\\^");
        //台号位数
        int delayCount = 0;
        for (int i = 0; i < numberStr.length; i++) {
            String pulseStr = numberStr[i];
            final AlloneControlData alloneControlData = new AlloneControlData(irData.fre, pulseStr);
            //char num = numberStr.charAt(i);
            //使用Handler发送延时指令，模拟用户手动按下台号
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        DeviceControlApi.allOneControl(device.getUid(), deviceId, alloneControlData.getFreq(), alloneControlData.getPluseNum(), alloneControlData.getPluseData(), true, ProgramDetailActivity.this);
                    }
                }
            }, 1000 * delayCount);
            delayCount++;
        }
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (baseEvent.getResult() == ErrorCode.SUCCESS) {
            System.out.println("换台成功！");
        } else {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }
}
