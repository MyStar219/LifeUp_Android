package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.BindFail;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.bo.LinkageOutput;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SecurityWarning;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.dao.SecurityWarningDao;
import com.orvibo.homemate.data.ArmType;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LinkageOutputType;
import com.orvibo.homemate.data.SecurityWarningType;
import com.orvibo.homemate.device.bind.BaseSelectDeviceActionsFragment;
import com.orvibo.homemate.model.bind.scene.SecurityBindPersenter;
import com.orvibo.homemate.sharedPreferences.SocketModeCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.smartscene.adapter.LinkageBindAdapter;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.dialog.SceneTipDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加编辑删除安防联动都在此activity
 * 同一个联动普通设备只能绑定一次，红外设备可以绑定多次，但是同一个按键只能绑定一次
 * <p>
 * 如果是添加的sceneBind，那么其sceneBindId默认为{@link Constant#INVALID_NUM}，所有新的sceneBindId<=-1</p>
 * Created by Smagret on 2015/10/16.
 */
public class SecurityManagerFragment extends BaseSelectDeviceActionsFragment {
    private static final String TAG = SecurityManagerFragment.class.getSimpleName();
    private Security security;
    private OnLinkageManagerListener onLinkageManagerListener;
    private LinkageBindAdapter mLinkageBindAdapter;
    private LinkageOutput editingLinkageOutput;
    private SecurityBindPersenter mSecurityBindPersenter;
    private ListView mLinkageBindListView;
    private TextView mAddBindTextView;
    private TextView mAddConditionTextView;
    private View notificationView;
    private TextView mNotificationTextView;
    private Button mAdd_btn;

    private LinkageConditionDao linkageConditionDao;

    private static final int SELECT_CONDITION = 4;

    private List<LinkageCondition> linkageConditions;
    private List<LinkageCondition> srclinkageConditions;

    protected int mArmType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            security = (Security) bundle.getSerializable(Constant.SECURITY);
            mArmType = bundle.getInt(IntentKey.ARM_TYPE, ArmType.ARMING);
        }

        LogUtil.d(TAG, "onCreate()-security:" + security);
        mBindActionType = BindActionType.LINKAGE;
        linkageConditionDao = new LinkageConditionDao();
        linkageConditions = linkageConditionDao.selLinkageConditionsByLinkageId(security.getSecurityId());
        srclinkageConditions = linkageConditions;
        LoadParam loadParam = LoadParam.getLoadServerParam(mAppContext);
        LoadUtil.noticeLoadServerData(loadParam);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_security_manager,
                container, false);
        mLinkageBindListView = (ListView) view.findViewById(R.id.linkageBindListView);
        mAddBindTextView = (TextView) view.findViewById(R.id.addBindTextView);
        mAddConditionTextView = (TextView) view.findViewById(R.id.addConditionTextView);
        mNotificationTextView = (TextView) view.findViewById(R.id.notificationTextView);
        notificationView = view.findViewById(R.id.notificationView);
        notificationView.setOnClickListener(this);
        mAdd_btn = (Button) view.findViewById(R.id.btnAddAction);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        refresh();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            refreshLinkage();
        }
        super.onHiddenChanged(hidden);
    }

    private void init() {
        initLinkageCondition();
        initListener();
        initLinkageBindPersenter();
        refresh();
    }

    private void refresh() {
        if (security != null) {
            SecurityWarning securityWarning = new SecurityWarningDao().selSecurityWarning(UserCache.getCurrentUserId(mAppContext), security.getSecurityId());
            if (securityWarning == null || securityWarning.getWarningType()== SecurityWarningType.APP) {
                mNotificationTextView.setText(R.string.security_warning_setting_app);
            } else {
                mNotificationTextView.setText(R.string.security_warning_setting_app_phone);
            }
        }
    }

    private void initLinkageCondition() {
        if (mArmType == ArmType.ARMING) {
            mAddConditionTextView.setText(getResources().getString(R.string.intelligent_scene_arming_condition));
            mAddConditionTextView.setClickable(true);
            mAddConditionTextView.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.icon_select_sensor), null, getResources().getDrawable(R.drawable.device_item_arrow_right), null);
        } else if (mArmType == ArmType.DISARMING) {
            mAddConditionTextView.setText(getResources().getString(R.string.intelligent_scene_disarming_condition));
            mAddConditionTextView.setClickable(false);
            mAddConditionTextView.setFocusable(false);
            mAddConditionTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_manual_click), null, null, null);
        }
    }

    private void initListener() {
        mAddBindTextView.setOnClickListener(this);
        mAdd_btn.setOnClickListener(this);
        if (mArmType == ArmType.ARMING) {
            mAddConditionTextView.setOnClickListener(this);
        }
    }

    private void setAddView(boolean isNoneBind) {
        if (mArmType == ArmType.ARMING) {
            mLinkageBindListView.setVisibility(View.VISIBLE);
            mAdd_btn.setVisibility(View.VISIBLE);
            mAddBindTextView.setVisibility(View.GONE);
            notificationView.setVisibility(View.VISIBLE);
        } else if (mArmType == ArmType.DISARMING) {
            mAddBindTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_add, 0, R.drawable.device_item_arrow_right, 0);
            mAddBindTextView.setClickable(true);
            mAddBindTextView.setTextColor(getResources().getColor(R.color.black));
            notificationView.setVisibility(View.GONE);

            if (isNoneBind) {
                mLinkageBindListView.setVisibility(View.GONE);
                mAdd_btn.setVisibility(View.GONE);
                mAddBindTextView.setVisibility(View.VISIBLE);
            } else {
                mLinkageBindListView.setVisibility(View.VISIBLE);
                mAdd_btn.setVisibility(View.VISIBLE);
                mAddBindTextView.setVisibility(View.GONE);
            }
        }
    }


    private void initLinkageBindPersenter() {
        if (mSecurityBindPersenter == null) {
            mSecurityBindPersenter = new SecurityBindPersenter(mAppContext, security, mArmType) {

                @Override
                public void onLinkageOutputFinish(String uid, boolean isAddLinkageOutputs, int result, List<LinkageOutput> successBindList, List<BindFail> failBindList) {

                }

                @Override
                public void onDeleteLinkageOutputFinish(String uid, int result) {

                }

                @Override
                protected void onLinkageOutputsChanged(List<LinkageOutput> linkageOutputs) {
                    LogUtil.d(TAG, "onLinkageOutputsChanged()-linkageOutputs:" + linkageOutputs);
                    refreshLinkageOutputListView(linkageOutputs);
                    setAddView(mLinkageBindAdapter == null || mLinkageBindAdapter.getCount() == 0);
                }

                @Override
                protected void onLinkageConditionsChanged(List<LinkageCondition> linkageConditions) {
                    LogUtil.d(TAG, "onLinkageConditionsChanged()-linkageConditions:" + linkageConditions);
                }

                @Override
                protected void onLinkageBindResult(int result) {
                    dismissDialog();
                    if (result == ErrorCode.SUCCESS) {
                        reset();
                        refreshLinkage();
                        ToastUtil.showToast(R.string.operation_success);
                        onLinkageManagerListener.onLinkageFinish();
                    } else if (result == ErrorCode.NET_DISCONNECT || !NetUtil.isNetworkEnable(mAppContext)) {
                        ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                    } else if (!NetUtil.isWifi(mAppContext) || result == ErrorCode.REMOTE_ERROR || result == ErrorCode.OFFLINE_GATEWAY || !SocketModeCache.isLocal(mAppContext, mainUid)) {
                        ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
                    } else {
                        ToastUtil.showToast(R.string.operation_failed);
                    }
                }
            };
            mSecurityBindPersenter.setConditions(srclinkageConditions, linkageConditions);
        }
    }

    private void refreshLinkage() {
        if (mSecurityBindPersenter != null) {
            mSecurityBindPersenter.refreshExistLinkageBind(security);
            if (mArmType == ArmType.ARMING) {
                linkageConditions = linkageConditionDao.selLinkageConditionsByLinkageId(security.getSecurityId());
                if (linkageConditions != null) {
                    srclinkageConditions = linkageConditions;
                }
                mSecurityBindPersenter.setConditions(srclinkageConditions, linkageConditions);
            }
        }
    }

    private void refreshLinkageOutputListView(List<LinkageOutput> linkageOutputs) {
        if (mLinkageBindAdapter == null) {
            mLinkageBindAdapter = new LinkageBindAdapter(getActivity(), linkageOutputs, this);
            mLinkageBindListView.setAdapter(mLinkageBindAdapter);
        } else {
            mLinkageBindAdapter.refreshList(linkageOutputs);
        }
        setListViewHeightBasedOnChildren(mLinkageBindListView);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (mLinkageBindAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < mLinkageBindAdapter.getCount(); i++) {
            View listItem = mLinkageBindAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mLinkageBindAdapter.getCount() - 1));
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 30, 0, 10);
        listView.setLayoutParams(params);
    }

    private void doDeleteLinkageOutput(LinkageOutput linkageOutput) {
        mSecurityBindPersenter.remove(linkageOutput);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-requestCode:" + requestCode + ",resultCode:" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_CONDITION: {
                    if (data != null) {
                        List<LinkageCondition> conditions = (List<LinkageCondition>) data.getSerializableExtra(Constant.LINKAGE_CONDITIONS);
                        LogUtil.d(TAG, "onActivityResult()-conditions:" + conditions);
                        mSecurityBindPersenter.setConditions(srclinkageConditions, conditions);
                        linkageConditions = mSecurityBindPersenter.getCurrentLinkageConditionList();
                    }
                }
                break;
            }
        }
    }

    /**
     * 选择了延迟时间
     *
     * @param delayTime 延迟时间，单位100ms
     */
    @Override
    protected void onSelectDelayTime(int delayTime) {
        super.onSelectDelayTime(delayTime);
        LogUtil.d(TAG, "onSelectDelayTime()-delayTime:" + delayTime);
        mSecurityBindPersenter.setDelayTime(delayTime);
    }

    /**
     * 选择设备动作
     *
     * @param action
     */
    @Override
    protected void onSelectAction(Action action) {
        super.onSelectAction(action);
        LogUtil.d(TAG, "onSelectAction()-action:" + action);
        mSecurityBindPersenter.setAction(action);
    }

    /**
     * 选择设备
     *
     * @param devices
     */
    @Override
    protected void onSelectDevices(List<Device> devices) {
        super.onSelectDevices(devices);
        if (devices != null && !devices.isEmpty()) {
            String linkageId = security.getSecurityId();
            List<LinkageOutput> binds = new ArrayList<LinkageOutput>();
            DeviceStatusDao deviceStatusDao = new DeviceStatusDao();
            for (Device device : devices) {
                String linkageOutputId = mSecurityBindPersenter.removeDeleteLinkageOutput(device);
                if (!TextUtils.isEmpty(linkageOutputId))
                    continue;
                LinkageOutput linkageOutput = new LinkageOutput();
                linkageOutput.setUid(mainUid);
                linkageOutput.setUserName(userName);
                linkageOutput.setLinkageOutputId(Constant.NULL_DATA);
                linkageOutput.setItemId(LibSceneTool.getItemId());
                linkageOutput.setDeviceId(device.getDeviceId());
                linkageOutput.setDelayTime(0);
                linkageOutput.setDelFlag(DeleteFlag.NORMAL);
                linkageOutput.setLinkageId(linkageId);
                if (mArmType == ArmType.ARMING) {
                    linkageOutput.setOutputType(LinkageOutputType.ARMING);
                } else if (mArmType == ArmType.DISARMING) {
                    linkageOutput.setOutputType(LinkageOutputType.DISARMING);
                }
                //设置默认动作
                Action defaultActin = BindTool.getDefaultAction(device, deviceStatusDao.selDeviceStatus(device.getDeviceId()), BindActionType.LINKAGE);
//                Action defaultActin = BindTool.getDefaultAction(device.getDeviceId(), device.getDeviceType());
                if (defaultActin != null) {
                    if (DeviceOrder.STOP.equals(defaultActin.getCommand()) && defaultActin.getValue1() == 50) {
                        defaultActin.setCommand(DeviceOrder.OPEN);
                        defaultActin.setValue1(100);
                    }
                    Action.setData(linkageOutput, defaultActin);
                }
                binds.add(linkageOutput);
            }
            mSecurityBindPersenter.addLinkageOutputs(binds);
        }
    }

    @Override
    protected void onDeleteAction(Object tag) {
        super.onDeleteAction(tag);
        LinkageOutput linkageOutput = (LinkageOutput) tag;
        LogUtil.d(TAG, "onDeleteAction()-linkageOutput:" + linkageOutput);
        doDeleteLinkageOutput(linkageOutput);
    }

    public void reset() {
        mSecurityBindPersenter.reset();
    }

    public void save() {
        LogUtil.d(TAG, "save() - linkageConditions = " + mSecurityBindPersenter.getCurrentLinkageConditionList());
        showDialogNow(null);

        if (mSecurityBindPersenter.hasEmptyAction()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.BIND_NONE_ORDER);
            return;
        }

        HashMap<String, List<LinkageOutput>> sceneBinds = mSecurityBindPersenter.getConflictIrDevice();
        if (sceneBinds != null && sceneBinds.size() > 0) {
            List<CharSequence> tips = new ArrayList<CharSequence>();
            for (Map.Entry<String, List<LinkageOutput>> entry : sceneBinds.entrySet()) {
                List<LinkageOutput> sceneBinds1 = entry.getValue();
                String[] data = DeviceTool.getBindItemName(getActivity(), sceneBinds1.get(0).getUid(), sceneBinds1.get(0).getDeviceId());
                tips.add((data[0] + "" + data[1] + " " + data[2]).trim());
                for (LinkageOutput sceneBind : sceneBinds1) {
                    String action = DeviceTool.getActionName(getActivity(), sceneBind.getCommand(), sceneBind.getValue1(),
                            sceneBind.getValue2(), sceneBind.getValue3(), sceneBind.getValue4(), sceneBind.getDeviceId());
                    String time = String.format(getString(R.string.scene_action_time), (sceneBind.getDelayTime() / 10));
                    CharSequence str = Html.fromHtml(String.format("<font color=\"#0000FF\">%s  %s</font>", time, action));
                    tips.add(str);
                }
            }
            dismissDialog();
            SceneTipDialog.newInstance(getString(R.string.scene_time_same_tip), tips).show(getFragmentManager(), null);
            return;
        }

        if (!NetUtil.isNetworkEnable(mAppContext)) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            return;
        } else if (!NetUtil.isWifi(mAppContext)) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
            return;
        }

        if (!isArmChanged() && !isDisArmChanged()) {
//            ToastUtil.showToast(R.string.SUCCESS);
            dismissDialog();
            onLinkageManagerListener.onLinkageFinish();
            getActivity().finish();
        } else {
            mSecurityBindPersenter.saveLinkageOutputs();
        }
    }

    private boolean isLinkageOutputChanged() {
        boolean isAddNewLinkageOutput = mSecurityBindPersenter.isAddNewLinkageOutput();
        boolean isEditLinkageOutput = mSecurityBindPersenter.isEditLinkageOutput();
        boolean isDeleteLinkageOutput = mSecurityBindPersenter.isDeleteLinkageOutput();
        return isAddNewLinkageOutput || isEditLinkageOutput || isDeleteLinkageOutput;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addConditionTextView:
                if (!ClickUtil.isFastDoubleClick()) {
                    Intent conditionIntent = new Intent(getActivity(), SecuritySelectConditionActivity.class);
                    conditionIntent.putExtra(Constant.SECURITY_CONDITIONS, (Serializable) linkageConditions);
                    conditionIntent.putExtra(Constant.SECURITY, security);
                    startActivityForResult(conditionIntent, SELECT_CONDITION);
                }

                break;
            case R.id.tvTime:
                //选择延迟时间
                editingLinkageOutput = (LinkageOutput) v.getTag();
                String deviceId = editingLinkageOutput.getDeviceId();
                Device device = mDeviceDao.selDevice
                        (mainUid, deviceId);
                mSecurityBindPersenter.selectLinkageOutput(editingLinkageOutput);
                if (device != null && DeviceUtil.isIrDevice(device)) {
                    setIrDeviceFlag(true);
                } else {
                    setIrDeviceFlag(false);
                }
                break;
            case R.id.linearAction:
                //选择设备动作
                editingLinkageOutput = (LinkageOutput) v.getTag();
                deviceId = editingLinkageOutput.getDeviceId();
                device = mDeviceDao.selDevice
                        (mainUid, deviceId);
                mSecurityBindPersenter.selectLinkageOutput(editingLinkageOutput);
                if (device != null && DeviceUtil.isIrOrWifiAC(device)) {
                    selDeviceActions(device, editingLinkageOutput, mSecurityBindPersenter.getSameDeviceLinkageOutputs(deviceId), mBindActionType);
                } else {
                    selDeviceAction(device, editingLinkageOutput, mBindActionType);
                }
                break;
            case R.id.save_tv:
                //保存
                showDialogNow(null);
                save();
                break;
            case R.id.btnAddAction:
                //联动执行的场景，最多只能添加16条执行任务，超过16条时，toast提示“最多只能添加16个执行任务哦~”
                if (getSelectedLinkageOutputSize() >= 16) {
                    ToastUtil.toastError(ErrorCode.LINKAGE_BIND_MAX_ERROR);
                    return;
                }
                break;
            case R.id.notificationView: {
                Intent intent = new Intent(mAppContext, SecurityWarningActivity.class);
                intent.putExtra(IntentKey.SECURITY, security);
                startActivity(intent);
                break;
            }

        }
        super.onClick(v);
    }

    @Override
    protected int getDelayTime() {
        if (editingLinkageOutput != null) {
            return editingLinkageOutput.getDelayTime();
        }
        return super.getDelayTime();
    }

    /**
     * 获取当前联动选中的所有动作的个数
     *
     * @return
     */
    protected int getSelectedLinkageOutputSize() {
        int size = 0;
        if (mSecurityBindPersenter != null && mSecurityBindPersenter.getCurrentLinkageOutputList() != null) {
            size = mSecurityBindPersenter.getCurrentLinkageOutputList().size();
        }
        return size;
    }

    /**
     * 得到所有已选择的deviceId
     *
     * @return
     */
    @Override
    protected ArrayList<String> getBindDeviceIds() {
        return mSecurityBindPersenter.getSelectedDeviceIds();
    }

    /**
     * @return true 布防联动被修改
     */
    public boolean isArmChanged() {
        return mSecurityBindPersenter.isLinkageOutputChanged()
                || mSecurityBindPersenter.isLinkageConditionChanged() || isLinkageOutputChanged();
    }

    /**
     * @return true 撤防联动被修改
     */
    public boolean isDisArmChanged() {
        return mSecurityBindPersenter.isLinkageOutputChanged() || isLinkageOutputChanged();
    }

    public interface OnLinkageManagerListener {
        void onLinkageFinish();
    }

    public void registerFinishListener(OnLinkageManagerListener onLinkageManagerListener) {
        this.onLinkageManagerListener = onLinkageManagerListener;
    }
}
