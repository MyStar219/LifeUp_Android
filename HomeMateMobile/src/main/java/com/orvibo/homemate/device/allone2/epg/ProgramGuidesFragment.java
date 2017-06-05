package com.orvibo.homemate.device.allone2.epg;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.dao.ChannelCollectionDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.categoryscrollview.CategoryTabStrip;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by snow on 2016/7/19.
 * epg界面的fragment
 */
public class ProgramGuidesFragment extends Fragment implements CategoryTabStrip.PagerChangerCallBack, ChangeChannelListener, BaseResultListener, View.OnClickListener, OnRefreshListener {

    public static final String CATITEMDATAS = "CATITEMDATAS";

    private int ANIMATION_DURATION = 500;

    private NavigationGreenBar nbTitle;
    private RelativeLayout ll_program_guides;
    private CategoryTabStrip mTabLayout;
    private ImageView iv_showMorePrograms;
    private ViewPager programsViewPager;
    private MyPagerAdapter viewPagerAdapter;

    //所有节目栏目布局
    private LinearLayout ll_program_guides_gd;
    //所有节目栏目GridView
    private GridView programGuidesGridView;
    //隐藏所有节目栏目布局
    private ImageView iv_hide_program_guides;
    //机顶盒节目类别列表
    private List<CategoryModel> mCategoryModelList;
    //节目类别列表Adapter
    private CategoryAdapter mCategoryAdapter;
    //节目列表
    private List<UIProgramData.CatItemData> mCatItemDatas;

    private int lineupid;

    //0-9的红外码对应的AlloneControlData对象列表
    private List<AlloneControlData> mAlloneControlDatas;

    //缓存到本地的频道表
    //private Map<ChannelInfo.ChannelKey, ChannelInfo> sLineupCacheMap;
    private List<ChannelInfo> mChannelInfoList;
    //匹配过频道名称的节目列表
    private List<CatItemDataHasChannelName> mCatItemDataHasChannelNameList;

    //是否高清
    private boolean ishd;
    //是否付款
    private boolean ispay;
    //是否高清优先
    private boolean isFirsthd;

    public static final int HD_PROGRAM = 1;
    public static final int SD_PROGRAM = 0;
    public static final int FEE_PROGRAM = 0;
    public static final int PAY_PROGRAM = 1;

    private boolean needToRefresh = false;

/*    public Map<ChannelInfo.ChannelKey, ChannelInfo> getsLineupCacheMap() {
        return sLineupCacheMap;
    }*/

    public List<ChannelInfo> getChannelInfoList() {
        return mChannelInfoList;
    }

    private IrData irData;

    private View emptyView;

    private ImageView emptyImageView;

    private Button btnRetry;
    private AnimationDrawable drawable;

    //选择的栏目位置
    private int selectPostion;

    //更新栏目位置消息
    private static final int MSG_UPDATE_CATEGORY = 1;

    private com.orvibo.homemate.bo.Device device;
    private String deviceId;

    private AlloneSaveData alloneSaveData;

    private Context context = ViHomeProApp.getContext();

    private CatItemDataHasChannelName collectType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_program_fragment, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initListener();
    }

    private void initView(View view) {
        ll_program_guides = (RelativeLayout) view.findViewById(R.id.ll_program_guides);
        mTabLayout = (CategoryTabStrip) view.findViewById(R.id.cts_programs_guides);
        iv_showMorePrograms = (ImageView) view.findViewById(R.id.iv_more_programs);
        programsViewPager = (ViewPager) view.findViewById(R.id.vp_programs_content);
        ll_program_guides_gd = (LinearLayout) view.findViewById(R.id.ll_program_guides_gd);
        programGuidesGridView = (GridView) view.findViewById(R.id.gv_program_guides);
        iv_hide_program_guides = (ImageView) view.findViewById(R.id.iv_hide_program_guides);
        emptyView = view.findViewById(R.id.emptyView);
        emptyImageView = (ImageView) view.findViewById(R.id.emptyImageView);
        drawable = (AnimationDrawable) emptyImageView.getDrawable();
        drawable.start();
        btnRetry = (Button) view.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(this);
    }

    private void initData() {
        device = (com.orvibo.homemate.bo.Device) getArguments().getSerializable(IntentKey.DEVICE);
        irData = (IrData) getArguments().getSerializable(IntentKey.ALL_ONE_DATA);
        deviceId = device.getDeviceId();
        //获取节目赛选条件
        ishd = AlloneStbCache.getNeedToGetHDProgram(deviceId);
        ispay = AlloneStbCache.getIsPayProgram(deviceId);
        isFirsthd = AlloneStbCache.getIsProgramHDFirst(deviceId);
        alloneSaveData = new AlloneSaveData();
        alloneSaveData.setData(device.getCompany());
        mCategoryModelList = new ArrayList<>();
        mCatItemDataHasChannelNameList = new ArrayList<>();
        //获取红外码库
        irData = AlloneCache.getIrData(deviceId);
        loadLineUpList(alloneSaveData.getAreaId(), alloneSaveData.getSpId(), ishd, ispay, isFirsthd);
    }

    private void initViewPagerAndNavigationBar(List<CatItemDataHasChannelName> catItemDataHasChannelNameList, boolean ishd, boolean ispay, boolean isFirsthd) {
        List<CatItemDataHasChannelName> filterData = filterHDProgramList(catItemDataHasChannelNameList, ishd, isFirsthd);
        filterData = filterPayProgramList(filterData, ispay);
        ll_program_guides.setVisibility(View.VISIBLE);
        viewPagerAdapter = new MyPagerAdapter(getFragmentManager());
        viewPagerAdapter.setCatItemDatas(filterData);
        programsViewPager.setAdapter(viewPagerAdapter);

        mCategoryAdapter = new CategoryAdapter(context, mCategoryModelList);
        programGuidesGridView.setAdapter(mCategoryAdapter);
        mTabLayout.setViewPager(programsViewPager);
        mTabLayout.setCateGoryChangeSelectCallBack(this);
    }

    private void refreshData(List<CatItemDataHasChannelName> catItemDataHasChannelNameList, boolean ishd, boolean ispay, boolean isFirsthd) {
        List<CatItemDataHasChannelName> filterData = filterHDProgramList(catItemDataHasChannelNameList, ishd, isFirsthd);
        filterData = filterPayProgramList(filterData, ispay);
        viewPagerAdapter.setCatItemDatas(filterData);
        viewPagerAdapter.notifyDataSetChanged();
        needToRefresh = false;
    }


    private void initListener() {
        iv_showMorePrograms.setOnClickListener(this);
        iv_hide_program_guides.setOnClickListener(this);
        programGuidesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                selectPostion = position;
                mCategoryAdapter.changeData(mCategoryModelList, position);
                hideAllProramGuidesLayout();
                mHandler.sendEmptyMessage(MSG_UPDATE_CATEGORY);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean curretISHD = AlloneStbCache.getNeedToGetHDProgram(deviceId);
        boolean currentISPAY = AlloneStbCache.getIsPayProgram(deviceId);
        boolean currentHDFirst = AlloneStbCache.getIsProgramHDFirst(deviceId);
        if (curretISHD == ishd && currentISPAY == currentISPAY && currentHDFirst == isFirsthd) {
            //EPG的偏好设置没有改变，不做任何处理
        } else {
            needToRefresh = true;
            ishd = curretISHD;
            ispay = currentISPAY;
            isFirsthd = currentHDFirst;
            loadLineUpList(alloneSaveData.getAreaId(), alloneSaveData.getSpId(), ishd, ispay, isFirsthd);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_more_programs:
                showAllProgramGuidesLayout();
                break;
            case R.id.iv_hide_program_guides:
                hideAllProramGuidesLayout();
                break;
            case R.id.btnRetry:
                btnRetry.setVisibility(View.GONE);
                drawable.start();
                initData();
                break;
        }
    }

    private void showAllProgramGuidesLayout() {
        ll_program_guides_gd.setVisibility(View.VISIBLE);
    }

    private void hideAllProramGuidesLayout() {
        ll_program_guides_gd.setVisibility(View.GONE);
    }

    @Override
    public void cateGoryChangeselected(int position) {
        mCategoryAdapter.changeData(mCategoryModelList, position);
    }

    /**
     * 换台
     *
     * @param object
     */
    @Override
    public void changeChannelClick(Object object) {
        PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) object;
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
        KookongSDK.getIRDataById(rid, new IRequestResult<IrDataList>() {

            @Override
            public void onSuccess(String msg, IrDataList result) {
                List<IrData> irDatas = result.getIrDataList();
                IrData irData = irDatas.get(0);
                AlloneCache.saveIrData(irData, deviceId);
                control(irData, pulse);
            }

            @Override
            public void onFail(String msg) {
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }


    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (baseEvent.getResult() == ErrorCode.SUCCESS) {
            System.out.println("换台成功！");
        } else {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }

    @Override
    public void onRefresh(IrData irData) {

    }

    @Override
    public void onRefresh(Action action) {

    }

    /**
     * 跳转到频道列表
     */
    public void jumpChannel() {
        Intent programChannelListIntent = new Intent();
        programChannelListIntent.putExtra(ProgramChannelsListActivity.LINEUP_ID, lineupid);
        programChannelListIntent.putExtra(CATITEMDATAS, (Serializable) mCatItemDatas);
        programChannelListIntent.putExtra(IntentKey.DEVICE, device);
        programChannelListIntent.putExtra(ProgramChannelsListActivity.IRDATA, irData);
        ActivityJumpUtil.jumpAct(getActivity(), ProgramChannelsListActivity.class, programChannelListIntent);
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<CatItemDataHasChannelName> mCatItemDatas;
        private WeakReference<ProgramsListFragment> mFragment;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setCatItemDatas(List<CatItemDataHasChannelName> catItemDatas) {
            if (catItemDatas == null)
                this.mCatItemDatas = new ArrayList<>();
            else
                mCatItemDatas = catItemDatas;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCatItemDatas.get(position).getCatTitle();
        }

        @Override
        public int getCount() {
            return mCatItemDatas.size();
        }

        @Override
        public Fragment getItem(int position) {
            mFragment = new WeakReference<ProgramsListFragment>(ProgramsListFragment.newInstance(position, mCatItemDatas.get(position).getSingleDataList(), mCatItemDatas.get(position).getCatTitle(), device, ProgramGuidesFragment.this));
            return mFragment.get();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ProgramsListFragment f = (ProgramsListFragment) super.instantiateItem(container, position);
            if (needToRefresh && mCatItemDatas != null && mCatItemDatas.size() > 0 && position >= 0 && position < mCatItemDatas.size()) {
                CatItemDataHasChannelName catItemDataHasChannelName = mCatItemDatas.get(position);
                if (catItemDataHasChannelName != null) {
                    f.resetFragmentData(catItemDataHasChannelName.getSingleDataList());
                }
            }
            System.gc();
            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
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
                if (data != null && data.size() > 0) {
                    lineupid = data.get(0).lid;
                    getLineupAndSave(Device.STB, lineupid, ishd, ispay, isFirsthd);
                }
            }

            @Override
            public void onFail(String s) {
                changeView(false);
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
        KookongSDK.getLineupDataAndSave(localDeviceid, lineupid, irData.rid, new IRequestResult<String>() {
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
                changeView(false);
            }
        });
    }

    /**
     * 获取电视墙数据
     *
     * @param lineupid
     * @param time
     * @param lineupCacheMap
     */
/*    private void loadProgramGuides(int lineupid, String time, final Map<ChannelInfo.ChannelKey, ChannelInfo> lineupCacheMap, final boolean ishd, final boolean ispay, final boolean isFirsthd) {
        KookongSDK.getTVWallData(lineupid, time, new IRequestResult<UIProgramData>() {
            @Override
            public void onSuccess(String s, UIProgramData uiProgramData) {
                //返回的电视墙数据
                List<UIProgramData.CatItemData> localCatItemDatas = uiProgramData.getCatItemDatas();
                //赋值给全局的电视墙列表
                mCatItemDatas = localCatItemDatas;
                matchChannelName(localCatItemDatas, lineupCacheMap, ishd, ispay, isFirsthd);
            }

            @Override
            public void onFail(String s) {
                dismissDialog();
                ToastUtil.showToast(s);
            }
        });
    }*/

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
                matchChannelName(localCatItemDatas, ishd, ispay, isFirsthd);
            }

            @Override
            public void onFail(String s) {
                changeView(false);
            }
        });
    }


    /**
     * 将电视墙数据和频道表数据进行匹配，得到Fragment需要显示的数据列表（得到自定义的电视节目列表数据List<CatItemDataHasChannelName>）(新SDK)
     *
     * @param catItemDatas
     * @param ishd
     * @param ispay
     * @param isFirsthd
     */
    private void matchChannelName(List<UIProgramData.CatItemData> catItemDatas, boolean ishd, boolean ispay, boolean isFirsthd) {
        mCatItemDataHasChannelNameList.removeAll(mCatItemDataHasChannelNameList);
        int catItemListSize = catItemDatas.size();
        //收藏的频道数据
        List<ChannelInfo> channelInfos = new ChannelCollectionDao().selChannelInfo(device.getUid(), deviceId);
        collectType = new CatItemDataHasChannelName();
        collectType.setCategoryid(EPGCategory.MY_PROGRAM_COLLECT);
        collectType.setCatTitle(getString(R.string.collect));
        HashMap<Integer, PairProgramHasChannelName> collectPairs = new HashMap<>();
        List<PairProgramHasChannelName> listcollectPairs = new ArrayList<>();
        for (int i = 0; i < catItemListSize; i++) {
            CatItemDataHasChannelName catItemDataHasChannelName = new CatItemDataHasChannelName();
            catItemDataHasChannelName.setCatTitle(catItemDatas.get(i).getCatTitle());
            List<ProgramData.PairProgram> programList = catItemDatas.get(i).getSingleDataList();
            List<PairProgramHasChannelName> pairProgramHasChannelNameList = new ArrayList<>();
            int programListSize = programList.size();
            for (int j = 0; j < programListSize; j++) {
                PairProgramHasChannelName pairProgramHasChannelName = new PairProgramHasChannelName();
                ProgramData.PairProgram pairProgram = programList.get(j);
                pairProgramHasChannelName.setPairProgram(pairProgram);
                int channelInfoListSize = mChannelInfoList.size();
                for (int k = 0; k < channelInfoListSize; k++) {
                    ChannelInfo channelInfo = mChannelInfoList.get(k);
                    if (pairProgram.cid == channelInfo.channelId) {
                        //pairProgramHasChannelName.setChannelName(channelInfo.name);
                        pairProgramHasChannelName.setChannelInfo(channelInfo);
                        pairProgramHasChannelNameList.add(pairProgramHasChannelName);
                        break;
                    }
                }
                for (ChannelInfo channelInfo : channelInfos) {
                    if (pairProgram.cid == channelInfo.channelId) {
                        PairProgramHasChannelName pairProgramHasChannelName1 = new PairProgramHasChannelName();
                        ProgramData.PairProgram pairProgram1 = programList.get(j);
                        pairProgramHasChannelName1.setPairProgram(pairProgram1);
                        //pairProgramHasChannelName.setChannelName(channelInfo.name);
                        pairProgramHasChannelName1.setChannelInfo(channelInfo);
                        collectPairs.put(pairProgram.cid, pairProgramHasChannelName1);
                        break;
                    }
                }
            }
            if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_DRAMA)))//电视剧
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_DRAMA);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_TVCOLUMN)))//综艺
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_TVCOLUMN);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_SPORTS)))//体育
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_SPORTS);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_MOVIE)))//电影
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_MOVIE);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_KIDS)))//少儿
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_KIDS);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_LIFE)))//生活
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_LIFE);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_FINANCE)))//财经
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_FINANCE);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_SCIEDU)))//科教
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_SCIEDU);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_NEWS)))//新闻
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_NEWS);
            else if (catItemDatas.get(i).getCatTitle().equals(getString(R.string.kk_program_OTHER)))//其他
                catItemDataHasChannelName.setCategoryid(EPGCategory.MY_PROGRAM_OTHER);
            catItemDataHasChannelName.setSingleDataList(pairProgramHasChannelNameList);
            mCatItemDataHasChannelNameList.add(catItemDataHasChannelName);
            Collections.sort(mCatItemDataHasChannelNameList, new Comparator<CatItemDataHasChannelName>() {
                @Override
                public int compare(CatItemDataHasChannelName lhs, CatItemDataHasChannelName rhs) {
                    return Integer.valueOf(lhs.getCategoryid()).compareTo(Integer.valueOf(rhs.getCategoryid()));
                }
            });
        }
        for (ChannelInfo channelInfo : channelInfos) {
            listcollectPairs.add(collectPairs.get(channelInfo.channelId));
        }
        collectType.setSingleDataList(listcollectPairs);
        //把收藏数据放入list中
        mCatItemDataHasChannelNameList.add(0, collectType);
        //处理电视墙里的电视类别
        mCategoryModelList.removeAll(mCategoryModelList);
        CategoryModel categoryModel;
        for (int i = 0; i < mCatItemDataHasChannelNameList.size(); i++) {
            categoryModel = new CategoryModel();
            categoryModel.setCategory(mCatItemDataHasChannelNameList.get(i).getCatTitle());
            mCategoryModelList.add(categoryModel);
        }
        if (!needToRefresh)
            initViewPagerAndNavigationBar(mCatItemDataHasChannelNameList, ishd, ispay, isFirsthd);
        else
            refreshData(mCatItemDataHasChannelNameList, ishd, ispay, isFirsthd);
        changeView(true);
    }

    /**
     * 根据是否高清要求，过滤节目
     *
     * @param programList
     * @param ishd
     * @param isFirsthd
     * @return
     */
    private List<CatItemDataHasChannelName> filterHDProgramList(List<CatItemDataHasChannelName> programList, boolean ishd, boolean isFirsthd) {
        List<CatItemDataHasChannelName> filterProgramList = new ArrayList<>();
        if (programList != null) {
            int length = programList.size();
            for (int i = 0; i < length; i++) {
                CatItemDataHasChannelName filterCatItemDataHasChannelName = new CatItemDataHasChannelName();
                CatItemDataHasChannelName catItemDataHasChannelName = programList.get(i);
                filterCatItemDataHasChannelName.setCatTitle(catItemDataHasChannelName.getCatTitle());
                List<PairProgramHasChannelName> pairProgramHasChannelNameList = catItemDataHasChannelName.getSingleDataList();
                List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
                filterPairProgramHasChannelName.addAll(pairProgramHasChannelNameList);
                int programListSize = pairProgramHasChannelNameList.size();
                for (int j = 0; j < programListSize; j++) {
                    PairProgramHasChannelName pairProgramHasChannelName = pairProgramHasChannelNameList.get(j);
                    if (!ishd) {
                        if (pairProgramHasChannelName.getPairProgram().ishd == HD_PROGRAM)
                            //关闭高清频道,清除高清的节目
                            filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
                    } else {
                        filterPairProgramHasChannelName.remove(getFilterProgram(pairProgramHasChannelName, pairProgramHasChannelNameList, isFirsthd, j));
                    }
/*                    if (!ishd && pairProgramHasChannelName.getPairProgram().ishd==HD_PROGRAM){
                        //关闭高清频道,清除高清的节目
                        filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
                    }else{
                        filterPairProgramHasChannelName.remove(getFilterProgram(pairProgramHasChannelName,pairProgramHasChannelNameList,isFirsthd));
                    }*/
                }
                filterCatItemDataHasChannelName.setSingleDataList(filterPairProgramHasChannelName);
                filterProgramList.add(filterCatItemDataHasChannelName);
            }
        }
        return filterProgramList;
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
                            return localPairProgramHasChannelName.getPairProgram().ishd == HD_PROGRAM ? pairProgramHasChannelName : localPairProgramHasChannelName;
                        else//勾选高清频道，但没有打开高清优先，如果一个节目既有标清又有高清，那只显示标清，去除高清
                            return localPairProgramHasChannelName.getPairProgram().ishd == HD_PROGRAM ? localPairProgramHasChannelName : pairProgramHasChannelName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据付费条件，过滤节目
     *
     * @param programList
     * @param ispay
     * @return
     */
    private List<CatItemDataHasChannelName> filterPayProgramList(List<CatItemDataHasChannelName> programList, boolean ispay) {
        List<CatItemDataHasChannelName> filterProgramList = new ArrayList<>();
        if (programList != null) {
            int length = programList.size();
            for (int i = 0; i < length; i++) {
                CatItemDataHasChannelName filterCatItemDataHasChannelName = new CatItemDataHasChannelName();
                CatItemDataHasChannelName catItemDataHasChannelName = programList.get(i);
                filterCatItemDataHasChannelName.setCatTitle(catItemDataHasChannelName.getCatTitle());
                List<PairProgramHasChannelName> pairProgramHasChannelNameList = catItemDataHasChannelName.getSingleDataList();
                List<PairProgramHasChannelName> filterPairProgramHasChannelName = new ArrayList<>();
                filterPairProgramHasChannelName.addAll(pairProgramHasChannelNameList);
                int programListSize = pairProgramHasChannelNameList.size();
                for (int j = 0; j < programListSize; j++) {
                    PairProgramHasChannelName pairProgramHasChannelName = pairProgramHasChannelNameList.get(j);
                    if (!ispay && pairProgramHasChannelName.getChannelInfo().fee == PAY_PROGRAM) {
                        //关闭付费频道,清除付费的节目
                        filterPairProgramHasChannelName.remove(pairProgramHasChannelName);
                    }
                    //在勾选了付费频道的情况下，所有付费和免费的节目都在显示的列表里，所以不做清除处理
                }
                filterCatItemDataHasChannelName.setSingleDataList(filterPairProgramHasChannelName);
                filterProgramList.add(filterCatItemDataHasChannelName);
            }
        }
        return filterProgramList;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_CATEGORY:
                    if (programsViewPager != null)
                        programsViewPager.setCurrentItem(selectPostion);
                    break;
            }
        }
    };

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
                    DeviceControlApi.allOneControl(device.getUid(), deviceId, alloneControlData.getFreq(), alloneControlData.getPluseNum(), alloneControlData.getPluseData(), true, ProgramGuidesFragment.this);
                }
            }, 1000 * delayCount);
            delayCount++;
        }
    }

    /**
     * 数据下载view更新
     *
     * @param isSuccess 数据下载成功
     */
    private void changeView(boolean isSuccess) {
        drawable.stop();
        if (!isSuccess) {
            emptyView.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
        }
    }

    /**
     * 收藏更新
     *
     * @param event
     */
    public void onEventMainThread(CollectDataRefresh event) {
        if (collectType != null) {
            PairProgramHasChannelName pairProgramHasChannelName = event.getPairProgramHasChannelName();
            boolean isAdd = true;
            for (int i = 0; i < collectType.getSingleDataList().size(); i++) {
                PairProgramHasChannelName p = collectType.getSingleDataList().get(i);
                if (p.getChannelInfo().channelId == pairProgramHasChannelName.getChannelInfo().channelId) {
                    isAdd = false;
                    collectType.getSingleDataList().remove(i);
                    break;
                }
            }
            if (isAdd) {
                collectType.getSingleDataList().add(0, pairProgramHasChannelName);
            }
            refreshData(mCatItemDataHasChannelNameList, ishd, ispay, isFirsthd);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
