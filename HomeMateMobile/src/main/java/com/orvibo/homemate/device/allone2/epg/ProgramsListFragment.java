package com.orvibo.homemate.device.allone2.epg;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.model.db.bean.ChannelInfo;
import com.hzy.tvmao.model.legacy.api.data.UIProgramData;
import com.kookong.app.data.LineupList;
import com.kookong.app.data.ProgramData;
import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.dao.ChannelCollectionDao;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.pulltorefresh.ErrorMaskView;
import com.orvibo.homemate.view.custom.pulltorefresh.PullListMaskController;
import com.orvibo.homemate.view.custom.pulltorefresh.PullRefreshView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yuwei on 2016/4/7.
 */
public class ProgramsListFragment extends Fragment implements OnRefreshListener {

    public static final String CATEGORY_TITLE = "category_title";
    public static final String SINGLE_LIST_DATA = "singleDataList";
    public static final String VIEWPAGER_INDEX = "viewpager_index";
    public static final String DEVICE = "device";

    private int viewPagerIndex;
    private String mCategoryTitle;

    private PullRefreshView program_listview;
    private PullListMaskController mViewController;
    private List<PairProgramHasChannelName> mSingleDataList;
    private ProgramListAdapter programListAdapter;
    private static ChangeChannelListener mChangeChannelListener;

    //是否高清
    private boolean ishd;
    //是否付款
    private boolean ispay;
    //是否高清优先
    private boolean isFirsthd;

    private Device device;

    private ProgramGuidesFragment guidesFragment;

    public static ProgramsListFragment newInstance(int position, List<PairProgramHasChannelName> singleDataList, String categoryTitle, Device device, ChangeChannelListener changeChannelListener) {
        mChangeChannelListener = changeChannelListener;
        ProgramsListFragment f = new ProgramsListFragment();
        Bundle b = new Bundle();
        b.putInt(VIEWPAGER_INDEX, position);
        b.putSerializable(SINGLE_LIST_DATA, (Serializable) singleDataList);
        b.putString(CATEGORY_TITLE, categoryTitle);
        b.putSerializable(DEVICE, device);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_programs_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
        initListener();
    }


    private void initView(View view) {
        View empty = view.findViewById(R.id.emptyView);
        empty.setVisibility(View.GONE);
        program_listview = (PullRefreshView) view.findViewById(R.id.prfv_program_list);
        ErrorMaskView maskView = (ErrorMaskView) view.findViewById(R.id.maskView);
        mViewController = new PullListMaskController(program_listview, maskView);
        //mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_LOADING);
    }

    private void initData() {
        //得到传输过来的索引和节目列表
        viewPagerIndex = getArguments().getInt(VIEWPAGER_INDEX, -1);
        mSingleDataList = (List<PairProgramHasChannelName>) getArguments().getSerializable(SINGLE_LIST_DATA);
        mCategoryTitle = getArguments().getString(CATEGORY_TITLE);
        device = (Device) getArguments().getSerializable(DEVICE);
        if (mSingleDataList == null)
            mSingleDataList = new ArrayList<>();
        if (mSingleDataList.size() == 0)
            mSingleDataList.add(new PairProgramHasChannelName());
        programListAdapter = new ProgramListAdapter(getActivity(), device, mSingleDataList, mChangeChannelListener);
        program_listview.setAdapter(programListAdapter);
        mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
/*        if (mSingleDataList == null || mSingleDataList.size() <= 0)
            mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_BLANK);
        else
            mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);*/
    }

    private void initListener() {
        mViewController.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String deviceId = device.getDeviceId();
                ishd = AlloneStbCache.getNeedToGetHDProgram(deviceId);
                ispay = AlloneStbCache.getIsPayProgram(deviceId);
                isFirsthd = AlloneStbCache.getIsProgramHDFirst(deviceId);
                refreshCategroyProgram();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void refreshCategroyProgram() {
        if (mCategoryTitle.equalsIgnoreCase(getString(R.string.collect))) {
//            LoadParam loadParam = new LoadParam();
//            loadParam.notifyRefresh = true;
//            loadParam.requestConfig = RequestConfig.getOnlyRemoteConfig();
//            LoadTarget loadTarget = new LoadTarget();
//            loadTarget.uid = device.getUid();
//            loadTarget.target = device.getUid();
//            loadTarget.tableName = TableName.CHANNEL_COLLECTION;
//            loadParam.loadTarget = loadTarget;
//            LoadUtil.noticeLoadTable(loadParam);
//            LoadUtil.noticeLoadTable(device.getUid(), TableName.CHANNEL_COLLECTION);
            LoadUtil.noticeLoadServerData(LoadParam.getLoadServerParam(ViHomeApplication.getContext()));
        }
        AlloneSaveData alloneSaveData = new AlloneSaveData();
        alloneSaveData.setData(device.getCompany());
        int areadid = alloneSaveData.getAreaId();
        int spiId = alloneSaveData.getSpId();
        if (viewPagerIndex == -1)
            mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_RETRY);
        else
            loadLineUpList(areadid, spiId);
    }


    private void loadLineUpList(int areadid, int spiId) {
        KookongSDK.getLineUpsList(areadid, spiId, new IRequestResult<LineupList>() {
            @Override
            public void onSuccess(String msg, LineupList result) {
                List<LineupList.Lineup> data = result.lineupList;
                int lineupid = data.get(0).lid;
                loadProgramGuides(lineupid, "0");
            }

            @Override
            public void onFail(String s) {
                //mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_RETRY);
                ToastUtil.showToast(R.string.get_program_list_fail);
                mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
            }
        });
    }

    private void loadProgramGuides(int lineupid, String time) {
        KookongSDK.getTVWallData(lineupid, time, new IRequestResult<UIProgramData>() {
            @Override
            public void onSuccess(String s, UIProgramData uiProgramData) {
                List<UIProgramData.CatItemData> mCatItemDatas = uiProgramData.getCatItemDatas();
                if (null != mCatItemDatas)
                    matchChannelName(mCatItemDatas, KookongSDK.getLineupByDeviceId(com.hzy.tvmao.ir.Device.STB));
                mViewController.showViewStatus(PullListMaskController.ListViewState.PULL_DOWN_LIST_NO_MORE);
            }

            @Override
            public void onFail(String s) {
                //mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_RETRY);
                ToastUtil.showToast(R.string.allone_error_data_tip);
                mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
            }
        });
    }

    /**
     * 将电视墙数据和频道表数据进行匹配
     * @param catItemDatas
     * @param lineupCacheMap
     */
/*    private void matchChannelName(List<UIProgramData.CatItemData> catItemDatas,Map<ChannelInfo.ChannelKey, ChannelInfo> lineupCacheMap){
        if(mSingleDataList==null)
            mSingleDataList = new ArrayList<>();
         mSingleDataList.removeAll(mSingleDataList);
        //获取电视墙中对应节目类别的节目列表
        List<ProgramData.PairProgram> programList = new ArrayList<>();
        for (int i=0;i<catItemDatas.size();i++){
            if (catItemDatas.get(i).getCatTitle().equals(mCategoryTitle)){
                programList = catItemDatas.get(i).getSingleDataList();
                break;
            }
        }
        int programListSize = programList.size();
        for(int j=0;j<programListSize;j++){
            PairProgramHasChannelName pairProgramHasChannelName = new PairProgramHasChannelName();
            ProgramData.PairProgram pairProgram = programList.get(j);
            pairProgramHasChannelName.setPairProgram(pairProgram);
            for (ChannelInfo.ChannelKey channelKey : lineupCacheMap.keySet()){
                ChannelInfo channelInfo = lineupCacheMap.get(channelKey);
                if(pairProgram.cid == channelInfo.channelId){
                    //pairProgramHasChannelName.setChannelName(channelInfo.name);
                    pairProgramHasChannelName.setChannelInfo(channelInfo);
                    mSingleDataList.add(pairProgramHasChannelName);
                    break;
                }
            }
        }
        mSingleDataList = filterHDProgramList(mSingleDataList,ishd,isFirsthd);
        mSingleDataList = filterPayProgramList(mSingleDataList,ispay);
        programListAdapter.changeData(mSingleDataList);
    }*/

    /**
     * 将电视墙数据和频道表数据进行匹配
     *
     * @param catItemDatas
     * @param channelInfoList
     */
    private void matchChannelName(List<UIProgramData.CatItemData> catItemDatas, List<ChannelInfo> channelInfoList) {
        if (mSingleDataList == null)
            mSingleDataList = new ArrayList<>();
        //如果第一条是空数据，先单独删除，因为直接删除整个列表会有异常
        if (mSingleDataList.get(0).getChannelInfo() == null)
            mSingleDataList.remove(0);
        //清除整个列表，从新添加
        mSingleDataList.removeAll(mSingleDataList);
        //获取电视墙中对应节目类别的节目列表
        List<ProgramData.PairProgram> programList = new ArrayList<>();
        //刷新时收藏数据要重新获取下数据进行匹配
        if (mCategoryTitle.equalsIgnoreCase(getString(R.string.collect))) {
            List<ChannelInfo> channelInfos = new ChannelCollectionDao().selChannelInfo(device.getUid(), device.getDeviceId());
            HashMap<Integer, ChannelInfo> hashMap = new HashMap<>();
            HashMap<Integer, PairProgramHasChannelName> collectPairs = new HashMap<>();
            for (ChannelInfo channelInfo : channelInfos) {
                hashMap.put(channelInfo.channelId, channelInfo);
            }
            for (UIProgramData.CatItemData catItemData : catItemDatas) {
                List<ProgramData.PairProgram> pairPrograms = catItemData.getSingleDataList();
                for (ProgramData.PairProgram pairProgram : pairPrograms) {
                    if (hashMap.containsKey(pairProgram.cid)) {
                        PairProgramHasChannelName pairProgramHasChannelName = new PairProgramHasChannelName();
                        pairProgramHasChannelName.setPairProgram(pairProgram);
                        pairProgramHasChannelName.setChannelInfo(hashMap.get(pairProgram.cid));
                        collectPairs.put(pairProgram.cid, pairProgramHasChannelName);
                    }
                }
            }
            for (ChannelInfo channelInfo : channelInfos) {
                mSingleDataList.add(collectPairs.get(channelInfo.channelId));
            }
        } else {
            for (int i = 0; i < catItemDatas.size(); i++) {
                if (catItemDatas.get(i).getCatTitle().equals(mCategoryTitle)) {
                    programList = catItemDatas.get(i).getSingleDataList();
                    break;
                }
            }
            int programListSize = programList.size();
            for (int j = 0; j < programListSize; j++) {
                PairProgramHasChannelName pairProgramHasChannelName = new PairProgramHasChannelName();
                ProgramData.PairProgram pairProgram = programList.get(j);
                pairProgramHasChannelName.setPairProgram(pairProgram);
                for (ChannelInfo channel : channelInfoList) {
                    if (pairProgram.cid == channel.channelId) {
                        //pairProgramHasChannelName.setChannelName(channelInfo.name);
                        pairProgramHasChannelName.setChannelInfo(channel);
                        mSingleDataList.add(pairProgramHasChannelName);
                        break;
                    }
                }
            }
        }
        mSingleDataList = filterHDProgramList(mSingleDataList, ishd, isFirsthd);
        mSingleDataList = filterPayProgramList(mSingleDataList, ispay);
        //如果刷新出来的数据为空，又要加会一条空数据进列表
        if (mSingleDataList.size() == 0)
            mSingleDataList.add(new PairProgramHasChannelName());
        programListAdapter.changeData(mSingleDataList);
    }


    public void resetFragmentData(List<PairProgramHasChannelName> singleDataList) {
        if (mSingleDataList != null) {
            mSingleDataList.removeAll(mSingleDataList);
            mSingleDataList.addAll(singleDataList);
            programListAdapter.changeData(mSingleDataList);
        }
    }

    /**
     * 根据是否高清要求，过滤节目
     *
     * @param ishd
     * @param isFirsthd
     * @return
     */
    private List<PairProgramHasChannelName> filterHDProgramList(List<PairProgramHasChannelName> PairProgramHasChannelNameLis, boolean ishd, boolean isFirsthd) {
        List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
        filterPairProgramHasChannelName.addAll(PairProgramHasChannelNameLis);
        int programListSize = PairProgramHasChannelNameLis.size();
        for (int j = 0; j < programListSize; j++) {
            PairProgramHasChannelName pairProgramHasChannelName = PairProgramHasChannelNameLis.get(j);
            if (!ishd) {
                if (pairProgramHasChannelName.getPairProgram().ishd == ProgramGuidesFragment.HD_PROGRAM)
                    //关闭高清频道,清除高清的节目
                    filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
            } else {
                filterPairProgramHasChannelName.remove(getFilterProgram(pairProgramHasChannelName, PairProgramHasChannelNameLis, isFirsthd, j));
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
     * @return
     */
    private PairProgramHasChannelName getFilterProgram(PairProgramHasChannelName pairProgramHasChannelName, List<PairProgramHasChannelName> pairProgramHasChannelNameList, boolean isFirsthd, int index) {
        if (pairProgramHasChannelNameList != null) {
            int length = pairProgramHasChannelNameList.size();
            for (int i = 0; i < length; i++) {
                if (index != i) {
                    PairProgramHasChannelName localPairProgramHasChannelName = pairProgramHasChannelNameList.get(i);
                    if (localPairProgramHasChannelName.getPairProgram().sn.equals(pairProgramHasChannelName.getPairProgram().sn) && localPairProgramHasChannelName.getPairProgram().cid == pairProgramHasChannelName.getPairProgram().cid) {
                        if (isFirsthd)//勾选高清频道，同时打开高清优先，如果一个节目既有标清又有高清，那只显示高清，去除标清
                            return localPairProgramHasChannelName.getPairProgram().ishd == ProgramGuidesFragment.HD_PROGRAM ? pairProgramHasChannelName : localPairProgramHasChannelName;
                        else//勾选高清频道，但没有打开高清优先，如果一个节目既有标清又有高清，那只显示标清，去除高清
                            return localPairProgramHasChannelName.getPairProgram().ishd == ProgramGuidesFragment.HD_PROGRAM ? localPairProgramHasChannelName : pairProgramHasChannelName;
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
    private List<PairProgramHasChannelName> filterPayProgramList(List<PairProgramHasChannelName> PairProgramHasChannelNameList, boolean ispay) {
        List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
        filterPairProgramHasChannelName.addAll(PairProgramHasChannelNameList);
        int programListSize = PairProgramHasChannelNameList.size();
        for (int j = 0; j < programListSize; j++) {
            PairProgramHasChannelName pairProgramHasChannelName = PairProgramHasChannelNameList.get(j);
            if (!ispay && pairProgramHasChannelName.getChannelInfo().fee == ProgramGuidesFragment.PAY_PROGRAM) {
                //关闭付费频道,清除付费的节目
                filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
            }
            //在勾选了付费频道的情况下，所有付费和免费的节目都在显示的列表里，所以不做清除处理
        }
        return filterPairProgramHasChannelName;
    }

    @Override
    public void onRefresh(IrData irData) {

    }

    @Override
    public void onRefresh(Action action) {

    }


}
