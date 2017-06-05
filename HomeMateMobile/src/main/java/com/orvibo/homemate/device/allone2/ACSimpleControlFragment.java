package com.orvibo.homemate.device.allone2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hzy.tvmao.KKACManagerV2;
import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;

import java.util.List;

/**
 * Created by yuwei on 2016/3/29.
 * 空调遥控器控制面板（简单）
 */
public class ACSimpleControlFragment extends Fragment implements OnRefreshListener {

    private GridView moreKeyGridView;
    //private List<String> btnNames;
    private MorekeyGridViewAdapter mMorekeyGridViewAdapter;

    private int btnClickCount = 0;

    private IrData irData;
    private KKACManagerV2 mKKACManager = new KKACManagerV2();

    private OnKeyClickListener keyClickListener;
    protected boolean isAction = false;//定时倒计时
    protected Action action;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_allone_gridview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initParams();
    }

    private void initView(View view) {
        moreKeyGridView = (GridView) view.findViewById(R.id.moreKeyGridView);
        moreKeyGridView.setVisibility(View.VISIBLE);
        irData = (IrData) getArguments().getSerializable(IntentKey.ALL_ONE_DATA);
        isAction = getArguments().getBoolean(IntentKey.IS_ACTION, false);
        action = (Action) getArguments().getSerializable(IntentKey.ACTION);
    }

    private void initParams() {
        if (irData != null) {
            List<IrData.IrKey> irKeyList = irData.keys;
            if (irKeyList != null) {
                mMorekeyGridViewAdapter = new MorekeyGridViewAdapter(getActivity(), irKeyList, irData.fre, keyClickListener, false);
                mMorekeyGridViewAdapter.setAction(action);
                mMorekeyGridViewAdapter.setAction(isAction);
                //ArrayAdapter<String> gvAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_ac_gv_item, R.id.simple_ac_btn_name, btnNames);
                moreKeyGridView.setAdapter(mMorekeyGridViewAdapter);
                mKKACManager.initIRData(irData.rid, irData.exts, irData.keys);//初始化红外模块
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            keyClickListener = (OnKeyClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onRefresh(IrData irData) {

    }

    @Override
    public void onRefresh(Action action) {
        this.action = action;
        initParams();
    }

    public void setIrData(IrData irData) {
        this.irData = irData;
        initParams();
    }
}
