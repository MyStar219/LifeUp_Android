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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.BindFail;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.bo.LinkageOutput;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.LinkageDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LinkageActiveType;
import com.orvibo.homemate.data.LinkageOutputType;
import com.orvibo.homemate.data.SmartSceneConstant;
import com.orvibo.homemate.device.bind.BaseSelectDeviceActionsFragment;
import com.orvibo.homemate.model.bind.scene.LinkageBindPersenter;
import com.orvibo.homemate.smartscene.SmartSceneTool;
import com.orvibo.homemate.smartscene.adapter.LinkageBindAdapter;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.WeekUtil;
import com.orvibo.homemate.view.dialog.SceneTipDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加编辑删除联动都在此activity
 * 同一个联动普通设备只能绑定一次，红外设备可以绑定多次，但是同一个按键只能绑定一次
 * <p>
 * 如果是添加的sceneBind，那么其sceneBindId默认为{@link Constant#INVALID_NUM}，所有新的sceneBindId<=-1</p>
 * Created by Smagret on 2015/10/16.
 */
public class LinkageManagerFragment extends BaseSelectDeviceActionsFragment {
    private static final String TAG = LinkageManagerFragment.class.getSimpleName();
    private Linkage linkage;
    private OnLinkageManagerListener onLinkageManagerListener;
    private LinkageBindAdapter mLinkageBindAdapter;

    private LinkageOutput editingLinkageOutput;

    private LinkageBindPersenter mLinkageBindPersenter;
    private ListView mLinkageBindListView;
    private TextView mAddBindTextView;
    private TextView mTimeIntervalTextView;
    private TextView mTimeRepeatTextView;
    private TextView mCustomTextView;
    private TextView mAllMemberTextView;
    private TextView mAddUserTextView;

    private RelativeLayout mAddTimeRelativeLayout;
    private RelativeLayout mAddUserRelativeLayout;
    private Button mAdd_btn;

    private static final int SELECT_CONDITION = 4;
    private static final int SELECT_TIME = 5;
    private static final int SELECT_USER = 7;

    private List<LinkageCondition> linkageConditions;
    private List<LinkageCondition> srcLinkageConditions;

    /**
     * true添加联动，false编辑。创建linkage成功后需要设置为false
     */
    private boolean isAddNewLinkage = false;
    private String curIntelligentSceneName = "";
    // private boolean isCommonLinkage         = false;

    /**
     * 如果为空，则使用主机的uid,否则使用对应的uid。比如小方的uid
     */
    private String mLinkageUid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            linkage = (Linkage) bundle.getSerializable(Constant.LINKAGE);
            linkageConditions = (List<LinkageCondition>) bundle.getSerializable(Constant.LINKAGE_CONDITIONS);
            srcLinkageConditions = (List<LinkageCondition>) bundle.getSerializable(Constant.LINKAGE_CONDITIONS);
            conditionType = bundle.getInt(IntentKey.LINKAGE_CONDITION_TYPE);
        }

        isAddNewLinkage = linkage == null;
        if (linkage == null) {
            linkage = new Linkage();
        } else {
            curIntelligentSceneName = linkage.getLinkageName();
        }
        LogUtil.d(TAG, "onCreate()-linkage:" + linkage);
        mBindActionType = BindActionType.LINKAGE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_linkage_manager,
                container, false);
        mLinkageBindListView = (ListView) view.findViewById(R.id.linkageBindListView);
        mAddBindTextView = (TextView) view.findViewById(R.id.addBindTextView);
        mCustomTextView = (TextView) view.findViewById(R.id.customTextView);
        mAllMemberTextView = (TextView) view.findViewById(R.id.allMemberTextView);
        mAddUserTextView = (TextView) view.findViewById(R.id.addUserTextView);
        mTimeIntervalTextView = (TextView) view.findViewById(R.id.timeIntervalTextView);
        mTimeRepeatTextView = (TextView) view.findViewById(R.id.timeRepeatTextView);
        //安防模式下不需要显示
        mAddTimeRelativeLayout = (RelativeLayout) view.findViewById(R.id.addTimeRelativeLayout);
        mAddUserRelativeLayout = (RelativeLayout) view.findViewById(R.id.addUserRelativeLayout);
        mAdd_btn = (Button) view.findViewById(R.id.btnAddAction);

        if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            //小方只显示图标和名称，不显示时间
            setTimeView(false);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        LogUtil.d(TAG, "onResume()-mLinkageBindPersenter:" + mLinkageBindPersenter);
    }

    private void init() {
        //安防模式下执行的时间段不需要显示
        initConditionTime();
        initUser();
        initListener();
        initLinkageOutputView();
        initLinkageBindPersenter();
    }

    /**
     * 设置执行时间段
     */
    private void initConditionTime() {
        String timeInterval = IntelligentSceneTool.getTimeInterval(getActivity(), linkageConditions);
        String allDay = getResources().getString(R.string.time_interval_all_day);
        if (StringUtil.isEqual(timeInterval, allDay)) {
            mTimeIntervalTextView.setVisibility(View.GONE);
        } else {
            mTimeIntervalTextView.setVisibility(View.VISIBLE);
        }
        mTimeIntervalTextView.setText(timeInterval);
        mTimeRepeatTextView.setText(WeekUtil.getWeeks(getActivity(), IntelligentSceneTool.getWeek(linkageConditions)));
    }

    /**
     * 汇泰龙门锁，霸陵门锁，设置成员
     */
    private void initUser() {

        String deviceId = IntelligentSceneTool.getDeviceId(linkageConditions);
        Device device = mDeviceDao.selDevice(deviceId);
        if (ProductManage.isSmartLock(device)) {
            mAddUserRelativeLayout.setVisibility(View.VISIBLE);
            String user = IntelligentSceneTool.getSelectedLockMumber(context, linkageConditions);
            if (user.equals(context.getResources().getString(R.string.intelligent_scene_all_users))) {
                mAllMemberTextView.setVisibility(View.GONE);
                mAddUserTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_member, 0, R.drawable.device_item_arrow_right, 0);
            } else {
                mAllMemberTextView.setVisibility(View.VISIBLE);
                mAddUserTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_custom, 0, R.drawable.device_item_arrow_right, 0);
            }
            mCustomTextView.setText(user);
        } else {
            mAddUserRelativeLayout.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        mAddBindTextView.setOnClickListener(this);
        mAddTimeRelativeLayout.setOnClickListener(this);
        mAddUserRelativeLayout.setOnClickListener(this);
        mAdd_btn.setOnClickListener(this);
    }

    private void initLinkageOutputView() {
        if (mAddTimeRelativeLayout.getVisibility() == View.VISIBLE || conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            mAddBindTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_add, 0, R.drawable.device_item_arrow_right, 0);
            mAddBindTextView.setClickable(true);
            mAddBindTextView.setTextColor(getResources().getColor(R.color.black));
        } else {
            mAddBindTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_add_disablerd, 0, R.drawable.device_item_arrow_right, 0);
            mAddBindTextView.setClickable(false);
            mAddBindTextView.setTextColor(getResources().getColor(R.color.font_white_gray));
        }
    }

    private void initLinkageBindPersenter() {
        if (mLinkageBindPersenter == null) {
            mLinkageBindPersenter = new LinkageBindPersenter(mAppContext, linkage) {

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

                //
                @Override
                protected void onLinkageConditionsChanged(List<LinkageCondition> linkageConditions) {
                    LogUtil.d(TAG, "onLinkageConditionsChanged()-linkageConditions:" + linkageConditions);
                    if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
                        //小方只显示图标和名称，不显示时间
                        setTimeView(false);
                    } else {
                        setTimeView(linkageConditions != null && !linkageConditions.isEmpty());
                    }
                    initLinkageOutputView();
                }

                @Override
                protected void onLinkageBindResult(int result) {
                    dismissDialog();
                    if (result == ErrorCode.SUCCESS) {
                        ToastUtil.showToast(R.string.operation_success);
                        onLinkageManagerListener.onLinkageFinish();
                    }
//                    else if (result == ErrorCode.NET_DISCONNECT || !NetUtil.isNetworkEnable(mAppContext)) {
//                        ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
//                    }
//                    else if (!NetUtil.isWifi(mAppContext) || result == ErrorCode.REMOTE_ERROR
//                            || result == ErrorCode.OFFLINE_GATEWAY || !SocketModeCache.isLocal(mAppContext, mainUid)) {
//                        ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
//                    }
                    else if (ErrorCode.isCommonError(result)) {
                        ToastUtil.toastError(result);
                    } else {
                        ToastUtil.showToast(R.string.operation_failed);
                    }
                }
            };
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
        if (isAddNewLinkage) {
            if (linkageOutputs == null || linkageOutputs.size() == 0) {
                onLinkageManagerListener.onHasLinkageOutput(false);
            } else {
                onLinkageManagerListener.onHasLinkageOutput(true);
            }
        } else {
            onLinkageManagerListener.onHasLinkageOutput(true);
        }
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
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 10);
        listView.setLayoutParams(params);
    }

    private void doDeleteLinkageOutput(LinkageOutput linkageOutput) {
        mLinkageBindPersenter.remove(linkageOutput);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-requestCode:" + requestCode + ",resultCode:" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_TIME: {
                    List<LinkageCondition> conditions = (List<LinkageCondition>) data.getSerializableExtra(Constant.LINKAGE_CONDITIONS);
                    LogUtil.d(TAG, "onActivityResult()-conditions:" + conditions);
                    mLinkageBindPersenter.setConditions(srcLinkageConditions, conditions);
                    linkageConditions = mLinkageBindPersenter.getCurrentLinkageConditionList();
                    initConditionTime();
                }

                case SELECT_USER: {
                    List<LinkageCondition> conditions = (List<LinkageCondition>) data.getSerializableExtra(Constant.LINKAGE_CONDITIONS);
                    LogUtil.d(TAG, "onActivityResult()-conditions:" + conditions);
                    mLinkageBindPersenter.setConditions(srcLinkageConditions, conditions);
                    linkageConditions = mLinkageBindPersenter.getCurrentLinkageConditionList();
                    initUser();
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
        mLinkageBindPersenter.setDelayTime(delayTime);
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
        mLinkageBindPersenter.setAction(action);
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
            String linkageId = Constant.NULL_DATA;
            if (!isAddNewLinkage) {
                linkageId = linkage.getLinkageId();
            }
            List<LinkageOutput> binds = new ArrayList<LinkageOutput>();
            DeviceStatusDao deviceStatusDao = new DeviceStatusDao();
            for (Device device : devices) {
                String linkageOutputId = mLinkageBindPersenter.removeDeleteLinkageOutput(device);
                if (!TextUtils.isEmpty(linkageOutputId))
                    continue;
                LinkageOutput linkageOutput = new LinkageOutput();
                linkageOutput.setUid(device.getUid());
                linkageOutput.setUserName(userName);
                linkageOutput.setLinkageOutputId(Constant.NULL_DATA);
                linkageOutput.setItemId(LibSceneTool.getItemId());
                linkageOutput.setDeviceId(device.getDeviceId());
                linkageOutput.setDelayTime(0);
                linkageOutput.setDelFlag(DeleteFlag.NORMAL);
                linkageOutput.setLinkageId(linkageId);
                linkageOutput.setOutputType(LinkageOutputType.NORMAL);
                //设置默认动作
                Action defaultActin = BindTool.getDefaultAction(device, deviceStatusDao.selDeviceStatus(device.getDeviceId()), BindActionType.LINKAGE);
//                Action defaultActin = BindTool.getInitAction(device, BindActionType.LINKAGE);

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
            mLinkageBindPersenter.addLinkageOutputs(binds);
        }
    }

    @Override
    protected void onDeleteAction(Object tag) {
        super.onDeleteAction(tag);
        LinkageOutput linkageOutput = (LinkageOutput) tag;
        LogUtil.d(TAG, "onDeleteAction()-linkageOutput:" + linkageOutput);
        doDeleteLinkageOutput(linkageOutput);
    }

    public void save() {
        LogUtil.d(TAG, "save() - linkageConditions = " + mLinkageBindPersenter.getCurrentLinkageConditionList());
        showDialogNow(null);
        if (StringUtil.isEmpty(curIntelligentSceneName)) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.SCENE_NAME_EMPTY_ERROR);
            return;
        }
        List<LinkageCondition> conditions = mLinkageBindPersenter.getCurrentLinkageConditionList();
        if (conditions == null || conditions.isEmpty()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.LINKAGE_NONE_CONDITION);
            return;
        } else if (conditions.size() == 1 && !SmartSceneTool.isOnlyServerOperate(conditionType)) {
            //提示时间未设置
            dismissDialog();
            ToastUtil.toastError(ErrorCode.LINKAGE_NONE_TIME_AND_WEEK);
            return;
        }

        if (mLinkageBindPersenter.getCurrentLinkageOutputList() == null ||
                mLinkageBindPersenter.getCurrentLinkageOutputList().isEmpty()) {
//        if (mLinkageBindPersenter.getCurrentLinkageOutputList() == null ||
//                mLinkageBindPersenter.getCurrentLinkageOutputList().isEmpty() && !mLinkageBindPersenter.isDeleteLinkageOutput()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.SCENE_BIND_NO_DEVICE);
            return;
        }
        if (mLinkageBindPersenter.hasEmptyAction()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.BIND_NONE_ORDER);
            return;
        }

        linkage.setLinkageName(curIntelligentSceneName);

        //判断时间是否一样(一个红外转发器/小方同一时间只能绑定一个动作)
        HashMap<String, List<LinkageOutput>> sceneBinds = mLinkageBindPersenter.getConflictIrDevice();
        if (sceneBinds != null && sceneBinds.size() > 0) {
            List<CharSequence> tips = new ArrayList<CharSequence>();
            for (Map.Entry<String, List<LinkageOutput>> entry : sceneBinds.entrySet()) {
                List<LinkageOutput> sceneBinds1 = entry.getValue();
                String[] data = DeviceTool.getBindItemName(getActivity(), sceneBinds1.get(0).getDeviceId());
                tips.add((data[0] + "" + data[1] + " " + data[2]).trim());
                for (LinkageOutput sceneBind : sceneBinds1) {
                    String action = DeviceTool.getActionName(getActivity(),sceneBind);
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
        } else if (!NetUtil.isWifi(mAppContext) && !SmartSceneTool.isOnlyServerOperate(conditionType)) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.REMOTE_ERROR);
            return;
        }

        if (isAddNewLinkage) {
            //添加新的联动
            linkage.setIsPause(LinkageActiveType.ACTIVE);
            mLinkageBindPersenter.setLinkage(linkage);
            addNewLinkage(curIntelligentSceneName);
        } else {
            if (!isChanged()) {
                ToastUtil.showToast(R.string.SUCCESS);
                dismissDialog();
                onLinkageManagerListener.onLinkageFinish();
            } else {
                mLinkageBindPersenter.saveLinkageOutputs(mLinkageUid);
            }
        }
    }

    private boolean isLinkageOutputChanged() {
        boolean isAddNewLinkageOutput = mLinkageBindPersenter.isAddNewLinkageOutput();
        boolean isEditLinkageOutput = mLinkageBindPersenter.isEditLinkageOutput();
        boolean isDeleteLinkageOutput = mLinkageBindPersenter.isDeleteLinkageOutput();
        return isAddNewLinkageOutput || isEditLinkageOutput || isDeleteLinkageOutput;
    }

    private void addNewLinkage(String linkageName) {
        LogUtil.d(TAG, "addNewLinkage()-linkageName:" + linkageName);
        if (StringUtil.isEmpty(linkageName)) {
            dismissDialog();
            ToastUtil.showToast(R.string.scene_name_empty);
            return;
        }
        mLinkageBindPersenter.saveLinkageOutputs(mLinkageUid);
    }

    /**
     * @param newName
     * @return情景有无改变
     */
    private boolean isLinkageChanged(String newName) {
        if (!isAddNewLinkage && linkage != null) {
            Linkage oldLinkage = new LinkageDao().selLinkageByLinkageId(linkage.getLinkageId());
            if (oldLinkage != null && newName.equals(oldLinkage.getLinkageName())) {
                return false;
            }
        } else if (isAddNewLinkage) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addConditionTextView:
                if (!ClickUtil.isFastDoubleClick()) {
                    mLinkageBindPersenter.selectLinkageConditions(linkageConditions);
                    Intent conditionIntent = new Intent(getActivity(), IntelligentSceneSelectConditionActivity.class);
                    conditionIntent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                    startActivityForResult(conditionIntent, SELECT_CONDITION);
                }
                break;
            case R.id.addTimeRelativeLayout:
                mLinkageBindPersenter.selectLinkageConditions(linkageConditions);
                Intent timeIntent = new Intent(getActivity(), IntelligentSceneSelectTimeActivity.class);
                timeIntent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                startActivityForResult(timeIntent, SELECT_TIME);

                break;
            case R.id.addUserRelativeLayout:
                mLinkageBindPersenter.selectLinkageConditions(linkageConditions);
                Intent lockMemberIntent = new Intent(getActivity(), IntelligentSceneSelectLockMemberActivity.class);
                lockMemberIntent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                startActivityForResult(lockMemberIntent, SELECT_USER);
                break;
            case R.id.tvTime:
                //选择延迟时间
                editingLinkageOutput = (LinkageOutput) v.getTag();
                String deviceId = editingLinkageOutput.getDeviceId();
                Device device = mDeviceDao.selDevice
                        (deviceId);
                mLinkageBindPersenter.selectLinkageOutput(editingLinkageOutput);
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
                device = mDeviceDao.selDevice(deviceId);
                mLinkageBindPersenter.selectLinkageOutput(editingLinkageOutput);
                if (device != null && DeviceUtil.isIrOrWifiAC(device)) {
                    selDeviceActions(device, editingLinkageOutput, mLinkageBindPersenter.getSameDeviceLinkageOutputs(deviceId), mBindActionType);
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
                if (getSelectedLinkageOutputSize() >= SmartSceneConstant.LINKAGE_OUTPUT_MAX_COUNT) {
                    ToastUtil.toastError(ErrorCode.LINKAGE_BIND_MAX_ERROR);
                    return;
                }
                break;
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
        if (mLinkageBindPersenter != null && mLinkageBindPersenter.getCurrentLinkageOutputList() != null) {
            size = mLinkageBindPersenter.getCurrentLinkageOutputList().size();
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
        return mLinkageBindPersenter.getSelectedDeviceIds();
    }

    private void setAddView(boolean isNoneBind) {
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

    /**
     * @return true已经修改了联动
     */
    public boolean isChanged() {
//        LogUtil.d(TAG, "isLinkageChanged=" + isLinkageNameChanged()
//                + "  mLinkageBindPersenter.isLinkageOutputChanged="
//                + mLinkageBindPersenter.isLinkageOutputChanged()
//                + "  mLinkageBindPersenter.isLinkageConditionChanged="
//                + mLinkageBindPersenter.isLinkageConditionChanged()
//                + " isLinkageOutputChanged=" + isLinkageOutputChanged());
        if (isAddNewLinkage) {
            return isLinkageNameChanged() || mLinkageBindPersenter.isLinkageOutputChanged() || isLinkageOutputChanged();
        } else {
            return isLinkageNameChanged() || mLinkageBindPersenter.isLinkageOutputChanged()
                    || mLinkageBindPersenter.isLinkageConditionChanged() || isLinkageOutputChanged();
        }
    }

    private boolean isLinkageNameChanged() {
        return isLinkageChanged(curIntelligentSceneName);
    }

    /**
     * @param uid           小方的uid或者主机的uid
     * @param conditions
     * @param conditionType {@link IntelligentSceneConditionType}
     */
    public void setLinkageCondition(String uid, List<LinkageCondition> conditions, int conditionType) {
        mLinkageUid = uid;
        linkageConditions = conditions;
        this.conditionType = conditionType;
        if (mLinkageBindPersenter != null) {
            mLinkageBindPersenter.setConditions(srcLinkageConditions, linkageConditions);
        } else {
            LogUtil.e(TAG, "setLinkageCondition()-mLinkageBindPersenter is null");
        }
//        LogUtil.d(TAG, "setLinkageCondition() - linkageConditions = " + mLinkageBindPersenter.getCurrentLinkageConditionList());
//        LogUtil.d(TAG, "setLinkageCondition() - mLinkageBindPersenter = " + mLinkageBindPersenter);
    }

    public void setLinkageName(String linkageName) {
        curIntelligentSceneName = linkageName;
        if (linkage != null) {
            linkage.setLinkageName(curIntelligentSceneName);
        }
    }

    public interface OnLinkageManagerListener {
        void onLinkageFinish();

        void onHasLinkageOutput(boolean hasLinkageOutput);
    }

    public void registerFinishListener(OnLinkageManagerListener onLinkageManagerListener) {
        this.onLinkageManagerListener = onLinkageManagerListener;
    }

    private void setTimeView(boolean visible) {
        if (mAddTimeRelativeLayout != null) {
            mAddTimeRelativeLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        initUser();
    }
}
