package com.orvibo.homemate.device.allone2.irlearn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.KKDevice;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.control.BaseControlActivity;

/**
 * Created by snown on 2016/6/3.
 *
 * @描述: 自定义遥控按键
 */
public class CustomRemoteFragment extends BaseLearnFragment {

    protected GridView moreKeyGridView;
    protected MoreGridViewAdapter customRemoteAdapter;

    private View emptyView;

    private Button btnAddKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_remote, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initGridData();
    }

    private void initView(View view) {
        moreKeyGridView = (GridView) view.findViewById(R.id.moreKeyGridView);
        emptyView = view.findViewById(R.id.emptyView);
        btnAddKey = (Button) view.findViewById(R.id.btnAddKey);
        btnAddKey.setOnClickListener(this);
        changeView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddKey:
                addKey();
                break;
        }
    }

    /**
     * 根据有无数据切换界面效果
     */
    private void changeView() {
        if (irKeys != null && irKeys.size() > 0) {
            emptyView.setVisibility(View.GONE);
            moreKeyGridView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            moreKeyGridView.setVisibility(View.GONE);
            if (isAction)
                btnAddKey.setVisibility(View.INVISIBLE);
        }
    }

    private void initGridData() {
        if (customRemoteAdapter == null) {
            customRemoteAdapter = new MoreGridViewAdapter(getActivity(), irKeys, this.isStartLearn, keyClickListener, mOnIrKeyLongClickListener,device);
            customRemoteAdapter.setAction(action);
            customRemoteAdapter.setAction(isAction);
            moreKeyGridView.setAdapter(customRemoteAdapter);
        } else {
            customRemoteAdapter.setAction(action);
            customRemoteAdapter.setAction(isAction);
            customRemoteAdapter.updateData(irKeys, isStartLearn);
        }
    }


    /**
     * 添加按键跳转
     */
    private void addKey() {
        Intent addKeyIntent = new Intent(getActivity(), EditIrButtonActivity.class);
        addKeyIntent.putExtra(IntentKey.DEVICE, ((BaseControlActivity) getActivity()).getDevice());
        addKeyIntent.putExtra(IntentKey.ALLONE_LEARN_RID, Integer.parseInt(device.getIrDeviceId()));
        addKeyIntent.putExtra(EditIrButtonActivity.OPERATION_TYPE, EditIrButtonActivity.OPERATION_TYPE_ADD);
        startActivity(addKeyIntent);
    }

    @Override
    public void onRefresh(KKDevice irData, boolean isStartLearn) {
        super.onRefresh(irData, isStartLearn);
        changeView();
        initGridData();
    }

    @Override
    public void onRefresh(Action action) {
        super.onRefresh(action);
        initGridData();
    }
}
