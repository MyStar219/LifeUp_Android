package com.orvibo.homemate.device.allone2.epg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.ChannelCollection;
import com.orvibo.homemate.dao.ChannelCollectionDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.model.CancelCollectChannel;
import com.orvibo.homemate.model.CollectChannel;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.sharedPreferences.UpdateTimeCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.categoryscrollview.CategoryTabStrip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by allen on 2016/7/12.
 */
public class ChannelDetailActivity extends BaseAlloneControlActivity implements ChangeChannelListener, BaseResultListener {
    private NavigationGreenBar navigationGreenBar;
    private CategoryTabStrip mTabLayout;
    private ViewPager programsViewPager;
    private MyPagerAdapter viewPagerAdapter;
    private PairProgramHasChannelName pairProgramHasChannelName;
    private List<Integer> sortWeeks;
    private List<String> sortWeekNames;
    private ChannelCollectionDao channelCollectionDao;
    private ChannelCollection channelCollection;
    private CollectChannel collectChannel;
    private CancelCollectChannel cancelCollectChannel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);
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
        navigationGreenBar.setText(pairProgramHasChannelName.getChannelInfo().name);
        navigationGreenBar.setMiddleTextColor(getResources().getColor(R.color.black));
        mTabLayout = (CategoryTabStrip) findViewById(R.id.cts_programs_guides);
        programsViewPager = (ViewPager) findViewById(R.id.vp_programs_content);
        channelCollectionDao = new ChannelCollectionDao();
        initCollectChannel();
        getProgramListOfWeek();
        refresh();
    }

    private void initCollectChannel() {
        collectChannel = new CollectChannel() {
            @Override
            public void onCollectChannelResult(int result) {
                dismissDialog();
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else{
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
                } else{
                    EventBus.getDefault().post(new CollectDataRefresh(pairProgramHasChannelName));
                    ToastUtil.showToast(R.string.cancel_collect_success);
                }
                refresh();
            }
        };
    }

    private void getProgramListOfWeek() {
        sortWeeks = new ArrayList<>();
        sortWeekNames = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (today == 0) {
            today = 7;
        }
        String[] weeks = getResources().getStringArray(R.array.weeks_new);
        List<Integer> sortThisWeeks = new ArrayList<>();
        List<Integer> sortNextWeeks = new ArrayList<>();
        List<String> sortThisWeekNames = new ArrayList<>();
        List<String> sortNextWeekNames = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            String name = weeks[i-1];
            if (i < today) {
                sortNextWeeks.add(i);
                name = getString(R.string.next_week) + name;
                sortNextWeekNames.add(name);
            } else {
                sortThisWeeks.add(i);
                sortThisWeekNames.add(name);
            }
        }
        sortWeeks.addAll(sortThisWeeks);
        sortWeeks.addAll(sortNextWeeks);
        sortWeekNames.addAll(sortThisWeekNames);
        sortWeekNames.addAll(sortNextWeekNames);
        viewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        programsViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setViewPager(programsViewPager);

    }

    private void refresh() {
        channelCollection = channelCollectionDao.selChannelCollection(device.getUid(), deviceId, pairProgramHasChannelName.getChannelInfo().channelId);
        if (channelCollection == null) {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_green_collection_normal);
        } else {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_green_collection_press);
        }
    }

    @Override
    public void rightTitleClick(View view) {
        if (channelCollection == null) {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_green_collection_press);
            channelCollection = new ChannelCollection();
            channelCollection.setUid(device.getUid());
            channelCollection.setDeviceId(deviceId);
            channelCollection.setChannelId(pairProgramHasChannelName.getChannelInfo().channelId);
            channelCollection.setIsHd(pairProgramHasChannelName.getChannelInfo().isHd);
            channelCollection.setCountryId(pairProgramHasChannelName.getChannelInfo().countryId);
            channelCollection.setUpdateTime(UpdateTimeCache.getUpdateTime(mContext, device.getUid()));
            collectChannel.startCollectChannel(mContext, channelCollection);
        } else {
            navigationGreenBar.setRightImageViewRes(R.drawable.bt_nav_green_collection_normal);
            cancelCollectChannel.startCancelCollectChannel(mContext, channelCollection.getChannelCollectionId());
        }
        showDialog();
    }

    /**
     * 换台
     *
     * @param object
     */
    @Override
    public void changeChannelClick(Object object) {
        IrData irData = AlloneCache.getIrData(deviceId);
        if (irData == null) {
            loadIrData(device.getIrDeviceId(), pairProgramHasChannelName.getChannelInfo().pulse);
        } else {
            control(irData, pairProgramHasChannelName.getChannelInfo().pulse);
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
                        DeviceControlApi.allOneControl(device.getUid(), deviceId, alloneControlData.getFreq(), alloneControlData.getPluseNum(), alloneControlData.getPluseData(), true, ChannelDetailActivity.this);
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

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private Map<Integer, ChannelProgramListFragment> fragments = new HashMap<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sortWeekNames.get(position);
        }

        @Override
        public int getCount() {
            return sortWeekNames.size();
        }

        @Override
        public Fragment getItem(int position) {
            ChannelProgramListFragment mFragment = fragments.get(position);
            if (mFragment == null) {
                mFragment = ChannelProgramListFragment.newInstance(pairProgramHasChannelName, position, sortWeeks.get(position), getPageTitle(position).toString(), device);
                mFragment.setChangeChannelListener(ChannelDetailActivity.this);
                fragments.put(position, mFragment);
            }
            return mFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ChannelProgramListFragment f = (ChannelProgramListFragment) super.instantiateItem(container, position);
            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
