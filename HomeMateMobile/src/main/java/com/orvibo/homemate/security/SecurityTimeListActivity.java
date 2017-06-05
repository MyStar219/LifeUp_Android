package com.orvibo.homemate.security;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.SecurityDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.model.ActivateTimer;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 安防定时
 * Created by wuliquan on 2016/7/21.
 */
public class SecurityTimeListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private String TAG = SecurityTimeListActivity.class.getSimpleName();
    private LinearLayout emptyView = null;
    private TextView tvAddTimer;
    private ListView lvTiming;

    private String mainUid;
    private TimingDao timingDao;
    private SecurityDao mSecurityDao;
    private NavigationGreenBar nbTitle;
    private SecurityTimeListAdapter mAdapter;
    private List<Timing> timingList = new ArrayList<>();

    private ActivateTimerControl activateTimerControl;
    private int WHAT_LOAD_TIMER = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_LOAD_TIMER) {
                if (NetUtil.isNetworkEnable(mAppContext)) {
                    loadTimer(mainUid);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_timing_list);

        timingDao = new TimingDao();
        mSecurityDao = SecurityDao.getInstance();
        mainUid = UserCache.getCurrentMainUid(SecurityTimeListActivity.this);

        emptyView = (LinearLayout) findViewById(R.id.layout_empty);
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        lvTiming = (ListView) findViewById(R.id.lvTiming);
        lvTiming.setEmptyView(emptyView);

        tvAddTimer = (TextView) emptyView.findViewById(R.id.tvAddTimer);
        tvAddTimer.setOnClickListener(this);
        nbTitle.setRightImageViewRes(R.drawable.white_add_selector);
        mAdapter = new SecurityTimeListAdapter(SecurityTimeListActivity.this,timingList,clickListener);
        lvTiming.setAdapter(mAdapter);
        lvTiming.setOnItemClickListener(this);

        activateTimerControl = new ActivateTimerControl(mAppContext);



    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSecurity();
        mHandler.removeCallbacksAndMessages(WHAT_LOAD_TIMER);
        mHandler.sendEmptyMessage(WHAT_LOAD_TIMER);

    }

    @Override
    public void rightTitleClick(View v){
        Intent intent = new Intent(SecurityTimeListActivity.this, SecurityTimeCreateActivity.class);
        startActivity(intent);
    }
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.tvAddTimer:
               Intent intent = new Intent(SecurityTimeListActivity.this, SecurityTimeCreateActivity.class);
               startActivity(intent);
               break;
       }
    }

    @Override
    protected void onRefresh(ViewEvent event) {
        loadSecurity();
    }
    private void loadTimer(String mainUid) {
        if (NetUtil.isNetworkEnable(mAppContext)) {
//            LoadUtil.noticeLoadHubData(mainUid, Constant.INVALID_NUM);
            LoadUtil.noticeLoadData(mAppContext, mainUid, Constant.INVALID_NUM);
        }
    }

    private void loadSecurity(){

        List<Timing> timeList = timingDao.selTimingByUid(mainUid);

        if(timeList!=null) {
            timingList.clear();
            timingList.addAll(timeList);
            mAdapter.notifyDataSetInvalidated();
        }

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Timing timing = (Timing) view.getTag();
            int isPause = timing.getIsPause();
            String timingId = timing.getTimingId();
            if (isPause == TimingConstant.TIMEING_EFFECT) {
                showDialog();
                activateTimerControl.startActivateTimer(timing.getUid(), UserCache.getCurrentUserName(mAppContext), timingId, TimingConstant.TIMEING_PAUSE);
            } else if (isPause == TimingConstant.TIMEING_PAUSE) {
                showDialog();
                activateTimerControl.startActivateTimer(timing.getUid(), UserCache.getCurrentUserName(mAppContext), timingId, TimingConstant.TIMEING_EFFECT);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Timing timing = timingList.get(position);
        Intent intent = new Intent(SecurityTimeListActivity.this, SecurityTimeCreateActivity.class);
        intent.putExtra("timing", timing);
        startActivity(intent);
    }



    private class ActivateTimerControl extends ActivateTimer {

        public ActivateTimerControl(Context context) {
            super(context);
        }

        @Override
        public void onActivateTimerResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                loadSecurity();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, result),
                        ToastType.NULL, ToastType.SHORT);
            }
        }
    }



}
