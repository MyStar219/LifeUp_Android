package com.orvibo.homemate.device.allone2.epg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.ir.Device;
import com.hzy.tvmao.model.db.bean.ChannelInfo;
import com.hzy.tvmao.model.legacy.api.data.UIProgramData;
import com.kookong.app.data.LineupList;
import com.kookong.app.data.ProgramData;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.AnimUtils;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.ClearEditText;
import com.orvibo.homemate.view.custom.pulltorefresh.ErrorMaskView;
import com.orvibo.homemate.view.custom.pulltorefresh.PullListMaskController;
import com.orvibo.homemate.view.custom.pulltorefresh.PullRefreshView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuwei on 2016/4/11.
 */
public class ProgramChannelsListActivity extends BaseAlloneControlActivity implements AdapterView.OnItemClickListener, ChangeChannelListener, BaseResultListener {

    public static final String LINEUP_ID = "lineupid";
    public static final String IRDATA = "irdata";


    private TextView cancelSearchTextView;
    private ClearEditText searchEditText;
    private RelativeLayout searchBoxCollapsedRelativeLayout;
    private TextView searchTextView;
    private TextView searchTextView2;

    private PullRefreshView ptrf_program_chennal;
    private PullListMaskController mPullListMaskController;
    private ProgramChannelListAdapter mProgramChannelListAdapter;

    //缓存到本地的频道表
    //private Map<ChannelInfo.ChannelKey, ChannelInfo> sLineupCacheMap;
    private List<ChannelInfo> mChannelInfoList;
    private List<UIProgramData.CatItemData> mCatItemDatas;
    private int lineupid;
    private IrData mIrData;

    //匹配出来的节目单结果
    private List<PairProgramHasChannelName> mPairProgramHasChannelNameList;

    private boolean ishd;
    private boolean ispay;
    private boolean isFirstHd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_channel_list);

        lineupid = getIntent().getIntExtra(LINEUP_ID, -1);
        mCatItemDatas = (List<UIProgramData.CatItemData>) getIntent().getSerializableExtra(ProgramGuidesFragment.CATITEMDATAS);
        mIrData = (IrData) getIntent().getSerializableExtra(IRDATA);
        initView();
        if (mCatItemDatas == null) {
            AlloneSaveData alloneSaveData = new AlloneSaveData();
            alloneSaveData.setData(device.getCompany());
            //获取节目赛选条件
            ishd = AlloneStbCache.getNeedToGetHDProgram(deviceId);
            ispay = AlloneStbCache.getIsPayProgram(deviceId);
            isFirstHd = AlloneStbCache.getIsProgramHDFirst(deviceId);
            loadLineUpList(alloneSaveData.getAreaId(), alloneSaveData.getSpId(), ishd, ispay, isFirstHd);
        } else {
            initData();
            initListener();
        }
    }

    private void initView() {
        cancelSearchTextView = (TextView) findViewById(R.id.cancelSearchTextView);
        searchEditText = (ClearEditText) findViewById(R.id.searchEditText);
        searchBoxCollapsedRelativeLayout = (RelativeLayout) findViewById(R.id.searchBoxCollapsedRelativeLayout);
        searchTextView = (TextView) findViewById(R.id.searchTextView);
        searchTextView2 = (TextView) findViewById(R.id.searchTextView2);
        ptrf_program_chennal = (PullRefreshView) findViewById(R.id.ptrf_program_chennal);
        ErrorMaskView maskView = (ErrorMaskView) findViewById(R.id.maskView);
        mPullListMaskController = new PullListMaskController(ptrf_program_chennal, maskView);
        //mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.EMPTY_LOADING);
    }


    private void initData() {
        searchEditText.setHint(getString(R.string.please_input_channel_or_tv_station));
        searchTextView2.setText(getString(R.string.please_input_channel_or_tv_station));
        mPairProgramHasChannelNameList = new ArrayList<>();
        mProgramChannelListAdapter = new ProgramChannelListAdapter(this, mPairProgramHasChannelNameList, this);
        ptrf_program_chennal.setAdapter(mProgramChannelListAdapter);
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work);
            return;
        }
        ptrf_program_chennal.setOnItemClickListener(this);
        showDialog();
        //获取红外码库
        mIrData = AlloneCache.getIrData(deviceId);
        if (mIrData == null) {
            KookongSDK.getIRDataById(device.getIrDeviceId(), new IRequestResult<IrDataList>() {
                @Override
                public void onSuccess(String s, IrDataList irDataList) {
                    List<IrData> irDatas = irDataList.getIrDataList();
                    IrData irData = irDatas.get(0);
                    AlloneCache.saveIrData(irData, deviceId);
                    mIrData = irData;
                    getLineupAndSave(Device.STB, lineupid, PullListMaskController.ListViewState.LIST_NO_MORE);
                }

                @Override
                public void onFail(String s) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.allone_error_data_tip);
                }
            });
        } else
            getLineupAndSave(Device.STB, lineupid, PullListMaskController.ListViewState.LIST_NO_MORE);
    }


    /**
     * 获取lineupid
     *
     * @param areadid
     * @param spiId
     */
    private void loadLineUpList(int areadid, int spiId, final boolean ishd, final boolean ispay, final boolean isFirsthd) {
        KookongSDK.getLineUpsList(areadid, spiId, new IRequestResult<LineupList>() {
            @Override
            public void onSuccess(String msg, LineupList result) {
                List<LineupList.Lineup> data = result.lineupList;
                lineupid = data.get(0).lid;
                getLineupAndSave(Device.STB, lineupid, ishd, ispay, isFirsthd);
            }

            @Override
            public void onFail(String s) {
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    /**
     * 通过lineupid，获取频道表保存到本地
     *
     * @param localDeviceid
     * @param lineupid
     */
    private void getLineupAndSave(final int localDeviceid, final int lineupid, final boolean ishd, final boolean ispay, final boolean isFirsthd) {
        KookongSDK.getLineupDataAndSave(localDeviceid, lineupid, mIrData.rid, new IRequestResult<String>() {
            @Override
            public void onSuccess(String msg, String result) {
                //获取成功后，去本地读取频道表数据（返回Map数据）
                //旧的SDK
                //LineupControl.i().loadLineupByDeviceId(localDeviceid);
                //新的SDK获取加载频道表
                KookongSDK.loadLineupByDeviceId(localDeviceid);
                //sLineupCacheMap = KookongSDK.getLineupByDeviceId(localDeviceid);
                mChannelInfoList = KookongSDK.getLineupByDeviceId(localDeviceid);
                //通过lineupid获取电视墙数据
                loadProgramGuides(lineupid, "0", ishd, ispay, isFirsthd);
            }

            @Override
            public void onFail(String s) {
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    /**
     * 获取电视墙数据(新SDK)
     *
     * @param lineupid
     * @param time
     * @param ishd
     * @param ispay
     * @param isFirsthd
     */
    private void loadProgramGuides(int lineupid, String time, final boolean ishd, final boolean ispay, final boolean isFirsthd) {
        KookongSDK.getTVWallData(lineupid, time, new IRequestResult<UIProgramData>() {
            @Override
            public void onSuccess(String s, UIProgramData uiProgramData) {

                //返回的电视墙数据
                List<UIProgramData.CatItemData> localCatItemDatas = uiProgramData.getCatItemDatas();
                //赋值给全局的电视墙列表
                mCatItemDatas = localCatItemDatas;
                initData();
                initListener();
            }

            @Override
            public void onFail(String s) {
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    private void initListener() {
        searchTextView.setOnClickListener(this);
        cancelSearchTextView.setOnClickListener(this);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    AnimUtils.showInputMethod(v);
                else
                    AnimUtils.hideInputMethod(v);
            }
        });
        //根据输入框输入值的改变来过滤搜索
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ptrf_program_chennal.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLineupAndSave(Device.STB, lineupid, PullListMaskController.ListViewState.LIST_REFRESH_NO_MORE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ishd = AlloneStbCache.getNeedToGetHDProgram(deviceId);
        ispay = AlloneStbCache.getIsPayProgram(deviceId);
        isFirstHd = AlloneStbCache.getIsProgramHDFirst(deviceId);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchTextView:
                changeState();
                break;
        }
    }

    /**
     * 改变输入框状态
     */
    private void changeState() {
        if (searchEditText.isFocused()) {
            searchBoxCollapsedRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            searchEditText.requestFocus();
            searchBoxCollapsedRelativeLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 首先获取频道表保存到本地
     *
     * @param localDeviceid
     * @param lineupid
     */
    private void getLineupAndSave(final int localDeviceid, int lineupid, final PullListMaskController.ListViewState loadType) {
        KookongSDK.getLineupDataAndSave(localDeviceid, lineupid, mIrData.rid, new IRequestResult<String>() {
            @Override
            public void onSuccess(String msg, String result) {
                //获取成功后，去本地读取频道表数据（返回Map数据）
                KookongSDK.loadLineupByDeviceId(localDeviceid);
                //LineupControl.i().loadLineupByDeviceId(localDeviceid);
                //sLineupCacheMap = KookongSDK.getLineupByDeviceId(localDeviceid);
                mChannelInfoList = KookongSDK.getLineupByDeviceId(localDeviceid);
                //匹配电视墙数据
                //matchingProgramGuides(sLineupCacheMap,loadType);
                matchProgramChannel(mChannelInfoList, loadType);
            }

            @Override
            public void onFail(String s) {
                dismissDialog();
                if (loadType == PullListMaskController.ListViewState.LIST_REFRESH_NO_MORE) {
                    ToastUtil.showToast(R.string.get_program_list_fail);
                    mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
                } else {
                    ToastUtil.showToast(R.string.allone_error_data_tip);
                }

            }
        });
    }


    private void matchProgramChannel(List<ChannelInfo> channelInfoList, PullListMaskController.ListViewState loadType) {
        if (channelInfoList == null || channelInfoList.size() <= 0 || mCatItemDatas == null || mCatItemDatas.size() <= 0) {
            //STB设备没有节目，所以节目单为空
            switch (loadType) {
                case LIST_NO_MORE:
                    mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                    break;
                case LIST_REFRESH_NO_MORE:
                    mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
                    break;
            }
            ToastUtil.showToast(R.string.no_program_channel_list);
            dismissDialog();
            return;
        }
        mPairProgramHasChannelNameList.removeAll(mPairProgramHasChannelNameList);
        //一个个频道对象从频道列表取出来
        for (ChannelInfo localChannel : mChannelInfoList) {
            //开始匹配
            int count = 0;
            //得到节目列表总数
            int mCatItemDatasLength = mCatItemDatas.size();
            for (int i = 0; i < mCatItemDatasLength; i++) {
                //获取对应节目类别下的在播节目列表
                List<ProgramData.PairProgram> singleDataList = mCatItemDatas.get(i).getSingleDataList();
                //获取对应节目类别下的在播节目列表总数
                int singleDataListLength = singleDataList.size();
                for (int j = 0; j < singleDataListLength; j++) {
                    if ((singleDataList.get(j).cid == localChannel.channelId) && singleDataList.get(j).ctry.equals(localChannel.countryId) && singleDataList.get(j).ishd == localChannel.isHd) {
                        PairProgramHasChannelName pairProgramHasChannelName = new PairProgramHasChannelName();
                        pairProgramHasChannelName.setChannelInfo(localChannel);
                        pairProgramHasChannelName.setPairProgram(singleDataList.get(j));
                        if (!mPairProgramHasChannelNameList.contains(pairProgramHasChannelName))
                            mPairProgramHasChannelNameList.add(pairProgramHasChannelName);
                    }
                }
            }
        }
        //排序
        Collections.sort(mPairProgramHasChannelNameList, new Comparator<PairProgramHasChannelName>() {
            @Override
            public int compare(PairProgramHasChannelName lhs, PairProgramHasChannelName rhs) {
                return Integer.valueOf(lhs.getChannelInfo().num).compareTo(Integer.valueOf(rhs.getChannelInfo().num));
            }
        });
        //匹配完成后，展示到节目单界面
        mPairProgramHasChannelNameList = filterHDChannelList(mPairProgramHasChannelNameList, ishd, isFirstHd);
        mPairProgramHasChannelNameList = filterPayChannelList(mPairProgramHasChannelNameList, ispay);
        mProgramChannelListAdapter.changeData(mPairProgramHasChannelNameList);
        switch (loadType) {
            case LIST_NO_MORE:
                mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                break;
            case LIST_REFRESH_NO_MORE:
                mPullListMaskController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
                break;
        }
        dismissDialog();
    }

    /**
     * 根据是否高清要求，过滤节目
     *
     * @param ishd
     * @param isFirsthd
     * @return
     */
    private List<PairProgramHasChannelName> filterHDChannelList(List<PairProgramHasChannelName> PairProgramHasChannelNameList, boolean ishd, boolean isFirsthd) {
        List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
        filterPairProgramHasChannelName.addAll(PairProgramHasChannelNameList);
        int programListSize = PairProgramHasChannelNameList.size();
        for (int j = 0; j < programListSize; j++) {
            PairProgramHasChannelName pairProgramHasChannelName = PairProgramHasChannelNameList.get(j);
            if (!ishd) {
                if (pairProgramHasChannelName.getChannelInfo().isHd == ProgramGuidesFragment.HD_PROGRAM)
                    //关闭高清频道,清除高清的节目
                    filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
            } else {
                filterPairProgramHasChannelName.remove(getFilterChannel(pairProgramHasChannelName, PairProgramHasChannelNameList, isFirsthd, j));
            }
/*                    if (!ishd && pairProgramHasChannelName.getPairProgram().ishd==HD_PROGRAM){
                        //关闭高清频道,清除高清的节目
                        filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
                    }else{
                        filterPairProgramHasChannelName.remove(getFilterProgram(pairProgramHasChannelName,pairProgramHasChannelNameList,isFirsthd));
                    }*/
        }
        return filterPairProgramHasChannelName;
    }

    /**
     * 根据是否高清优先，返回需要过滤的节目对象
     *
     * @param pairProgramHasChannelName
     * @param pairProgramHasChannelNameList
     * @param isFirsthd
     * @param index
     * @return
     */
    private PairProgramHasChannelName getFilterChannel(PairProgramHasChannelName pairProgramHasChannelName, List<PairProgramHasChannelName> pairProgramHasChannelNameList, boolean isFirsthd, int index) {
        if (pairProgramHasChannelNameList != null) {
            int length = pairProgramHasChannelNameList.size();
            for (int i = 0; i < length; i++) {
                if (index != i) {
                    PairProgramHasChannelName localPairProgramHasChannelName = pairProgramHasChannelNameList.get(i);
                    if (localPairProgramHasChannelName.getChannelInfo().channelId == pairProgramHasChannelName.getChannelInfo().channelId) {
                        if (isFirsthd)
                            return localPairProgramHasChannelName.getChannelInfo().isHd == ProgramGuidesFragment.HD_PROGRAM ? pairProgramHasChannelName : localPairProgramHasChannelName;
                        else
                            return localPairProgramHasChannelName.getChannelInfo().isHd == ProgramGuidesFragment.HD_PROGRAM ? localPairProgramHasChannelName : pairProgramHasChannelName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据付费条件，过滤节目
     *
     * @param ispay
     * @return
     */
    private List<PairProgramHasChannelName> filterPayChannelList(List<PairProgramHasChannelName> pairProgramHasChannelNameList, boolean ispay) {
        List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
        filterPairProgramHasChannelName.addAll(pairProgramHasChannelNameList);
        int programListSize = pairProgramHasChannelNameList.size();
        for (int j = 0; j < programListSize; j++) {
            PairProgramHasChannelName pairProgramHasChannelName = pairProgramHasChannelNameList.get(j);
            if (!ispay && pairProgramHasChannelName.getChannelInfo().fee == ProgramGuidesFragment.PAY_PROGRAM) {
                //关闭付费频道,清除付费的节目
                filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
            }
            //在勾选了付费频道的情况下，所有付费和免费的节目都在显示的列表里，所以不做清除处理
        }
        return filterPairProgramHasChannelName;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<PairProgramHasChannelName> filterDataList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {
            filterDataList = mPairProgramHasChannelNameList;
        } else {
            filterDataList.clear();
            for (PairProgramHasChannelName pairProgramHasChannelName : mPairProgramHasChannelNameList) {
                if (pairProgramHasChannelName.getChannelInfo().name.toLowerCase().indexOf(filterStr.toString().toLowerCase()) != -1
                        ) {
                    filterDataList.add(pairProgramHasChannelName);
                }
            }
        }
        mProgramChannelListAdapter.changeData(filterDataList);
    }


    @Override
    public void changeChannelClick(Object object) {
        PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) object;
        if (mIrData == null) {
            loadIrData(device.getIrDeviceId(), pairProgramHasChannelName.getChannelInfo().pulse);
        } else {
            control(mIrData, pairProgramHasChannelName.getChannelInfo().pulse);
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

    private Handler mHandler = new Handler();

    /**
     * 分割頻道号，操作机顶盒换台
     *
     * @param irData
     * @param pulse
     */
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
                        DeviceControlApi.allOneControl(device.getUid(), deviceId, alloneControlData.getFreq(), alloneControlData.getPluseNum(), alloneControlData.getPluseData(), true, ProgramChannelsListActivity.this);
                    }
                }
            }, 1000 * delayCount);
            delayCount++;
        }
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (baseEvent.getResult() == ErrorCode.SUCCESS) {

        } else {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PairProgramHasChannelName pairProgramHasChannelName = mPairProgramHasChannelNameList.get(position - 1);
        Intent intent = new Intent(mContext, ChannelDetailActivity.class);
        intent.putExtra(IntentKey.PAIR_PROGRAM_HAS_CHANNEL_NAME, pairProgramHasChannelName);
        intent.putExtra(IntentKey.DEVICE, device);
        mContext.startActivity(intent);
    }
}
