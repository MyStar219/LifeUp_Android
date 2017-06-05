//package com.orvibo.homemate.device.bind;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ListView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.device.bind.adapter.BindSelectedDeviceActionsAdapter;
//import com.orvibo.homemate.bo.Action;
//import com.orvibo.homemate.bo.BindAction;
//import com.orvibo.homemate.bo.BindFail;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.bo.RemoteBind;
//import com.orvibo.homemate.dao.DeviceCommenDao;
//import com.orvibo.homemate.dao.RemoteBindDao;
//import com.orvibo.homemate.data.Constant;
//import com.orvibo.homemate.data.DeleteFlag;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.model.bind.remote.DeleteRemoteBindAction;
//import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
//import com.orvibo.homemate.util.DeviceTool;
//import com.orvibo.homemate.util.LibSceneTool;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.ToastUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by huangqiyao on 2015/5/4.
// *
// * @deprecated
// */
//public class SelectedDeviceActionsActivity extends BaseSelectDeviceActionsActivity {
//    private static final String TAG = SelectedDeviceActionsActivity.class.getSimpleName();
//    private ConfirmAndCancelPopup mDeletePopup;
//
//    private BindSelectedDeviceActionsAdapter mAdapter;
//    /**
//     * 新添加
//     */
//    private List<BindAction> mAddBindActions = new ArrayList<BindAction>();
//
//    /**
//     * 编辑的绑定动作
//     */
//    private List<BindAction> mModifyBindActions = new ArrayList<BindAction>();
//    private List<BindAction> mOldBindActions = new ArrayList<BindAction>();
//
//    /**
//     * 保存已绑定、待绑定、待修改的动作
//     */
//    private List<BindAction> mAllBindActions = new ArrayList<BindAction>();
//
//    private RemoteBindDao mRemoteBindDao;
//    private BindAction mCurrentBindAction;
//    private String mDeviceId;
//    private int mKeyNo;
//    private int mKeyAction;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_device_bind_selected_device_actions);
//        initData();
//        mOldBindActions = DeviceCommenDao.selBindActions(userName, currentMainUid, mDeviceId, mKeyNo, mKeyAction);
//        mAdapter = new BindSelectedDeviceActionsAdapter(mContext, mOldBindActions, this);
//        ListView listView = (ListView) findViewById(R.id.actions_lv);
//        listView.setAdapter(mAdapter);
//
//        mAllBindActions.addAll(mOldBindActions);
//        mRemoteBindDao = new RemoteBindDao();
//    }
//
//    private void initData() {
//        final int defaultInt = Constant.INVALID_NUM;
//        Intent intent = getIntent();
//        mDeviceId = intent.getStringExtra("deviceId");
//        mKeyNo = intent.getIntExtra("keyNo", defaultInt);
//        mKeyAction = intent.getIntExtra("keyAction", defaultInt);
//    }
//
//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//        final int vId = v.getId();
//        switch (vId) {
//            case R.id.tvAction:
//                mCurrentBindAction = (BindAction) v.getTag();
//                Device device = mDeviceDao.selDevice
//                        (currentMainUid, mCurrentBindAction.getDeviceId());
//                selDeviceAction(device, mCurrentBindAction);
//                break;
//            case R.id.tvTime:
//                mCurrentBindAction = (BindAction) v.getTag();
//                break;
//        }
//    }
//
////    @Override
////    protected ArrayList<String> getBindDeviceIds() {
////        Set<String> ids = new HashSet<String>();
////        for (BindAction bindAction : mAllBindActions) {
////            ids.add(bindAction.getDeviceId());
////        }
////        ArrayList<String> deviceIds = new ArrayList<String>(ids);
////        return deviceIds;
////    }
//
//    @Override
//    protected void onDeleteAction(Object tag) {
//        final BindAction bindAction = (BindAction) tag;
//        if (mDeletePopup == null) {
//            mDeletePopup = new ConfirmAndCancelPopup() {
//                @Override
//                public void confirm() {
//                    // 新添加的bindAction
//                    if (isNewBindAction(bindAction)) {
//                        mAllBindActions.remove(bindAction);
//                        mAddBindActions.remove(bindAction);
//                        mAdapter.refresh(mAllBindActions);
//                        dismiss();
//                    } else {
//                        showDialog();
//                        doDeleteBindAction(bindAction);
//                    }
//                }
//            };
//        }
//        mDeletePopup.showPopup(mContext, String.format(getString(R.string.scene_set_delete_content), bindAction.getItemName()), getString(R.string.device_set_delete_btn), getString(R.string.cancel));
//    }
//
//    /**
//     * 删除绑定动作
//     *
//     * @param bindAction
//     */
//    private void doDeleteBindAction(final BindAction bindAction) {
//        new DeleteRemoteBindAction(mAppContext) {
//            @Override
//            public void onDeleteBindAction(String uid, int result, List<String> bindIds, List<BindFail> failBindList) {
//                if (mDeletePopup != null) {
//                    mDeletePopup.dismiss();
//                }
//                dismissDialog();
//                if (result != ErrorCode.SUCCESS) {
//                    ToastUtil.toastError(result);
//                    return;
//                }
//
//                if (bindIds != null && !bindIds.isEmpty()) {
//                    String bindId = bindIds.get(0);
//                    if (bindId.equals(bindAction.getBindId())) {
//                        mAllBindActions.remove(bindAction);
//                        mModifyBindActions.remove(bindAction);
//                        mAdapter.refresh(mAllBindActions);
//                        return;
//                    }
//                }
//
//                if (failBindList != null && !failBindList.isEmpty()) {
//                    BindFail bindFail = failBindList.get(0);
//                    if (bindFail.getBindId().equals(bindAction.getBindId())) {
//                        ToastUtil.toastError(bindFail.getResult());
//                        return;
//                    }
//                }
//            }
//        }.delete(bindAction.getUid(), bindAction.getBindId());
//    }
//
//    /**
//     * 判断是否为需要添加的绑定动作是bindId<=0||itemId>0
//     *
//     * @param bindAction
//     * @return
//     */
//    private boolean isNewBindAction(BindAction bindAction) {
//        return bindAction.getBindId().isEmpty() || bindAction.getItemId() > 0;
//    }
//
//    @Override
//    protected void onSelectDelayTime(int delayTime) {
//        super.onSelectDelayTime(delayTime);
//        if (isNewBindAction(mCurrentBindAction)) {
//            //新添加
//            final int itemId = mCurrentBindAction.getItemId();
//            for (BindAction bindAction : mAddBindActions) {
//                if (bindAction.getItemId() == itemId) {
//                    bindAction.setDelayTime(delayTime);
//                    break;
//                }
//            }
//        } else {
//            //edit
//            if (mModifyBindActions.contains(mCurrentBindAction)) {
//                final String bindId = mCurrentBindAction.getBindId();
//                for (BindAction bindAction : mModifyBindActions) {
//                    if (bindAction.getBindId().equals(bindId) && bindAction.getDelayTime() != delayTime) {
//                        bindAction.setDelayTime(delayTime);
//                        break;
//                    }
//                }
//            } else {
//                mCurrentBindAction.setDelayTime(delayTime);
//                mModifyBindActions.add(mCurrentBindAction);
//            }
//            removeBindActionIfNoneChange();
//        }
//        mAdapter.refresh(mAllBindActions);
//    }
//
//    @Override
//    protected void onSelectAction(Action action) {
//        super.onSelectAction(action);
//        if (isNewBindAction(mCurrentBindAction)) {
//            //新添加的
//            final int itemId = mCurrentBindAction.getItemId();
//            for (BindAction bindAction : mAddBindActions) {
//                if (bindAction.getItemId() == itemId) {
//                    Action.setData(bindAction, action);
//                    break;
//                }
//            }
//        } else {
//            //edit
//            if (mModifyBindActions.contains(mCurrentBindAction)) {
//                final String bindId = mCurrentBindAction.getBindId();
//                for (BindAction bindAction : mModifyBindActions) {
//                    if (bindAction.getBindId().equals(bindId)) {
//                        Action.setData(mCurrentBindAction, action);
//                        break;
//                    }
//                }
//            } else {
//                Action.setData(mCurrentBindAction, action);
//                mModifyBindActions.add(mCurrentBindAction);
//            }
//
//            removeBindActionIfNoneChange();
//        }
//        mAdapter.refresh(mAllBindActions);
//    }
//
//    /**
//     * 如果没有修改绑定动作，就不需要修改
//     */
//    private void removeBindActionIfNoneChange() {
//        //是否需要edit
//        RemoteBind remoteBind = mRemoteBindDao.selRemoteBind(mCurrentBindAction.getUid(), mCurrentBindAction.getBindId());
//        if (remoteBind != null && Action.isActionEqual(remoteBind, mCurrentBindAction)) {
//            mModifyBindActions.remove(mCurrentBindAction);
//            mAllBindActions.remove(mCurrentBindAction);
//        }
//    }
//
//    @Override
//    protected void onSelectDevices(List<Device> devices) {
//        super.onSelectDevices(devices);
//        if (devices != null && !devices.isEmpty()) {
////            final String defaultInt = Constant.INVALID_NUM;
//            final String uid = currentMainUid;
//            List<BindAction> bindActions = new ArrayList<BindAction>();
//            for (Device device : devices) {
//                //true表示是选择了重复的非红外设备动作
//                boolean isRepeatDevice = false;
//                final String deviceId = device.getDeviceId();
//                for (BindAction bindAction : mAllBindActions) {
//                    if (bindAction.getDeviceId().equals(deviceId)) {
//                        if (!DeviceTool.isIrDevice(uid, deviceId)) {
//                            isRepeatDevice = true;
//                            LogUtil.e(TAG, "onSelectDevices()-重复选择了" + device);
//                            break;
//                        }
//                    }
//                }
//                if (isRepeatDevice) {
//                    continue;
//                }
//                BindAction bindAction = new BindAction();
//                bindAction.setUid(uid);
//                bindAction.setUserName(userName);
////                bindAction.setSceneBindId(defaultInt);
//                bindAction.setItemId(LibSceneTool.getItemId());
//                bindAction.setDeviceId(deviceId);
//                bindAction.setDelayTime(0);
//                bindAction.setDelFlag(DeleteFlag.NORMAL);
////                bindAction.setSceneNo(defaultInt);
//
//                String[] data = DeviceTool.getBindItemName(mAppContext, uid, deviceId);
//                String itemName = data[0] + "" + data[1] + " " + data[2];
//                bindAction.setItemName(itemName);
//                bindActions.add(bindAction);
//            }
//            mAllBindActions.addAll(0, bindActions);
//            mAddBindActions.addAll(0, bindActions);
//            //refresh list
//            mAdapter.refresh(mAllBindActions);
//        }
//    }
//}
