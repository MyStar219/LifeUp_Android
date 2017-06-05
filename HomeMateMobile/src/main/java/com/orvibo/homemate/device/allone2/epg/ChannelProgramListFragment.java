package com.orvibo.homemate.device.allone2.epg;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.ChannelEpg;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.view.custom.pulltorefresh.ErrorMaskView;
import com.orvibo.homemate.view.custom.pulltorefresh.PullListMaskController;
import com.orvibo.homemate.view.custom.pulltorefresh.PullRefreshView;

/**
 * Created by yuwei on 2016/4/7.
 */
public class ChannelProgramListFragment extends Fragment implements View.OnClickListener{

    public static final String PAIR_PROGRAM_HAS_CHANNEL_NAME = "pairProgramHasChannelName";
    public static final String CATEGORY_TITLE = "category_title";
    public static final String WEEK = "week";
    public static final String VIEWPAGER_INDEX = "viewpager_index";
    public static final String DEVICE = "device";

    private int viewPagerIndex;
    private String mCategoryTitle;

    private PullRefreshView program_listview;
    private PullListMaskController mViewController;
    private PairProgramHasChannelName pairProgramHasChannelName;
    private int week;
    private ChannelEpg channelEpg;
    private ChannelProgramListAdapter programListAdapter;
    private ChangeChannelListener mChangeChannelListener;
    private AnimationDrawable drawable;
    private View emptyView;

    private ImageView emptyImageView;

    private Button btnRetry;

    //是否高清
    private boolean ishd;
    //是否付款
    private boolean ispay;
    //是否高清优先
    private boolean isFirsthd;

    private Device device;

    public static ChannelProgramListFragment newInstance(PairProgramHasChannelName pairProgramHasChannelName, int position, int week, String categoryTitle, Device device) {
        ChannelProgramListFragment f = new ChannelProgramListFragment();
        Bundle b = new Bundle();
        b.putSerializable(PAIR_PROGRAM_HAS_CHANNEL_NAME,pairProgramHasChannelName);
        b.putInt(VIEWPAGER_INDEX,position);
        b.putInt(WEEK, week);
        b.putString(CATEGORY_TITLE,categoryTitle);
        b.putSerializable(DEVICE,device);
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
    }

    public void setChangeChannelListener(ChangeChannelListener changeChannelListener) {
        mChangeChannelListener = changeChannelListener;
        if (programListAdapter != null) {
            programListAdapter.setChangeChannelListener(changeChannelListener);
        }
    }

    private void initView(View view){
        program_listview = (PullRefreshView) view.findViewById(R.id.prfv_program_list);
        program_listview.setHeadViewRefresh(false);
        program_listview.setDivider(getResources().getDrawable(R.drawable.line_divide));
        emptyView = view.findViewById(R.id.emptyView);
        emptyImageView = (ImageView) view.findViewById(R.id.emptyImageView);
        drawable = (AnimationDrawable) emptyImageView.getDrawable();
        drawable.start();
        btnRetry = (Button) view.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(this);
        program_listview.setEmptyView(emptyView);
        ErrorMaskView maskView = (ErrorMaskView) view.findViewById(R.id.maskView);
        mViewController = new PullListMaskController(program_listview,maskView);
        //mViewController.showViewStatus(PullListMaskController.ListViewState.EMPTY_LOADING);
    }

    private void initData(){
        //得到传输过来的索引和节目列表
        pairProgramHasChannelName = (PairProgramHasChannelName) getArguments().getSerializable(PAIR_PROGRAM_HAS_CHANNEL_NAME);
        viewPagerIndex = getArguments().getInt(VIEWPAGER_INDEX, -1);
        week = getArguments().getInt(WEEK, -1);
        mCategoryTitle = getArguments().getString(CATEGORY_TITLE);
        device = (Device) getArguments().getSerializable(DEVICE);
        programListAdapter = new ChannelProgramListAdapter(getActivity(), device, pairProgramHasChannelName, week, viewPagerIndex, channelEpg, mChangeChannelListener);
        program_listview.setAdapter(programListAdapter);
        if (channelEpg == null) {
            KookongSDK.getProgramGuide(pairProgramHasChannelName.getChannelInfo().channelId, "", week, new ProgramGideRequestResult());
        }
    }

    @Override
    public void onClick(View v) {
        btnRetry.setVisibility(View.GONE);
        drawable.start();
        KookongSDK.getProgramGuide(pairProgramHasChannelName.getChannelInfo().channelId, "", week, new ProgramGideRequestResult());
    }

    private class ProgramGideRequestResult implements IRequestResult<ChannelEpg> {

        @Override
        public void onSuccess(String s, ChannelEpg channelEpg) {
            ChannelProgramListFragment.this.channelEpg = channelEpg;
            if (programListAdapter != null) {
                programListAdapter.changeData(channelEpg);
                int livingIndex = programListAdapter.getLivingIndex();
                if (livingIndex > 0) {
                    if (livingIndex + 1 > programListAdapter.getCount() - 1) {
                        program_listview.setSelection(livingIndex);
                    } else {
                        program_listview.setSelection(livingIndex + 1);
                    }
                }
            }
        }

        @Override
        public void onFail(String s) {
            btnRetry.setVisibility(View.VISIBLE);
            drawable.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
