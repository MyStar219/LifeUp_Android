package com.orvibo.homemate.smartscene.manager;

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
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.bo.SceneBind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.SceneType;
import com.orvibo.homemate.device.bind.BaseSelectDeviceActionsFragment;
import com.orvibo.homemate.model.AddScene;
import com.orvibo.homemate.model.ModifyScene;
import com.orvibo.homemate.model.bind.scene.SceneBindPersenter;
import com.orvibo.homemate.smartscene.adapter.SceneBindAdapter;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.dialog.SceneTipDialog;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.popup.SceneBindActionFailPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加情景和编辑情景都在此activity
 * 同一个情景普通设备只能绑定一次，红外设备可以绑定多次，但是同一个按键只能绑定一次
 * <p>
 * 如果是添加的sceneBind，那么其sceneBindId默认为{@link Constant#INVALID_NUM}，所有新的sceneBindId<=-1</p>
 * Created by Allen on 2015/4/16.
 * Modified by smagret on 2015/04/23.
 */
public class SceneManagerFragment extends BaseSelectDeviceActionsFragment {
    private static final String TAG = SceneManagerFragment.class.getSimpleName();
    private Scene scene;
    private int mPic = SceneType.OTHER;//默认为其他模式
    private SceneBindAdapter mSceneBindAdapter;
    private OnSceneManagerListener onSceneManagerListener;

    private SceneBindPersenter mSceneBindPersenter;

    private ListView mSceneBind_lv;
    private Button mAdd_btn;
    private TextView mAddBindTextView;
    private SceneBind editingSceneBind;
    /**
     * 返回提示是否保存界面
     */
    private ConfirmAndCancelPopup mConfirmSavePopup;
    private SceneBindActionFailPopup mSceneBindActionFailPopup;

    /**
     * true添加情景，false编辑。创建scene成功后需要设置为false
     */
    private boolean isAddNewScene = false;
    private String mCurSceneName;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            scene = (Scene) bundle.getSerializable(Constant.SCENE);
        }
        LogUtil.d(TAG, "onCreate()-scene:" + scene);
        mBindActionType = BindActionType.SCENE;
        isAddNewScene = scene == null;
        if (!isAddNewScene) {
            mPic = scene.getPic();
            mCurSceneName = scene.getSceneName();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_scene_manager,
                container, false);
        mAddBindTextView = (TextView) view.findViewById(R.id.addBindTextView);
        mSceneBind_lv = (ListView) view.findViewById(R.id.sceneBind_lv);
        mAdd_btn = (Button) view.findViewById(R.id.btnAddAction);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        initListener();
        initBindFailPopup();
        initSceneBindPersenter();
    }

    private void initListener() {
        mAddBindTextView.setOnClickListener(this);
        mAdd_btn.setOnClickListener(this);
    }

    private void initSceneBindPersenter() {
        if (mSceneBindPersenter == null) {
            mSceneBindPersenter = new SceneBindPersenter(mAppContext, scene) {

                @Override
                protected void onSceneBindsChanged(List<SceneBind> sceneBinds) {
                    LogUtil.d(TAG, "onSceneBindsChanged(" + sceneBinds.size() + ")-sceneBinds:" + sceneBinds);
                    refreshSceneBindListView(sceneBinds);
                    setAddView(mSceneBindAdapter == null || mSceneBindAdapter.getCount() == 0);
                }

                @Override
                protected void onBindResult(int result, List<SceneBind> successSceneBinds, List<SceneBind> failSceneBinds) {
                    try {
                        LogUtil.d(TAG, "onBindResult(" + successSceneBinds.size() + ")-successSceneBinds:" + successSceneBinds);
                        LogUtil.d(TAG, "onBindResult(" + failSceneBinds.size() + ")-failSceneBinds:" + failSceneBinds);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dismissDialog();
                    if (failSceneBinds.size() > 0) {
                        mSceneBindActionFailPopup.setFailSceneBinds(failSceneBinds, mSceneBindPersenter.getTotalCount());
                    } else {
                        ToastUtil.showToast(R.string.operation_success);
                        if (mSceneBindActionFailPopup.isShowing()) {
                            mSceneBindActionFailPopup.dismiss();
                        }
                        onSceneManagerListener.onSceneFinish();
                    }
                }
            };
        }
    }

    private void refreshSceneBindListView(List<SceneBind> sceneBinds) {
        if (mSceneBindAdapter == null) {
            mSceneBindAdapter = new SceneBindAdapter(getActivity(), sceneBinds, this);
            mSceneBind_lv.setAdapter(mSceneBindAdapter);
            setListViewHeightBasedOnChildren(mSceneBind_lv);
        } else {
            mSceneBindAdapter.refreshList(sceneBinds);
            setListViewHeightBasedOnChildren(mSceneBind_lv);
        }

        if (isAddNewScene) {
            if (sceneBinds == null || sceneBinds.size() == 0) {
                onSceneManagerListener.onHasSceneBind(false);
            } else {
                onSceneManagerListener.onHasSceneBind(true);
            }
        } else {
            onSceneManagerListener.onHasSceneBind(true);
        }

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (mSceneBindAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < mSceneBindAdapter.getCount(); i++) {
            View listItem = mSceneBindAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mSceneBindAdapter.getCount() - 1));
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 10);
        listView.setLayoutParams(params);
    }

    private void initBindFailPopup() {
        mSceneBindActionFailPopup = new SceneBindActionFailPopup((BaseActivity) getActivity()) {
            @Override
            protected void onRetry() {
                showDialogNow(null);
                save();
            }

            @Override
            protected void onCancel() {
                mSceneBindPersenter.cancel();
                mSceneBindPersenter.refreshExistSceneBinds();
            }
        };
    }


    private void doDeleteSceneBind(SceneBind sceneBind) {
        // delete scene bind
        mSceneBindPersenter.remove(sceneBind);
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
        mSceneBindPersenter.setDelayTime(delayTime);
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
        mSceneBindPersenter.setAction(action);
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
            String sceneNo = Constant.NULL_DATA;
            if (!isAddNewScene) {
                sceneNo = scene.getSceneNo();
            }
            DeviceStatusDao deviceStatusDao = new DeviceStatusDao();
            List<SceneBind> binds = new ArrayList<SceneBind>();
            for (Device device : devices) {
                SceneBind sceneBind = new SceneBind();

                //设置默认动作
                Action defaultActin = BindTool.getDefaultAction(device, deviceStatusDao.selDeviceStatus(device.getDeviceId()), BindActionType.SCENE);
                if (defaultActin != null) {
                    Action.setData(sceneBind, defaultActin);
                }
                SceneBind oldSceneBind = mSceneBindPersenter.removeDeleteSceneBind(device, defaultActin);
                if (oldSceneBind != null && !TextUtils.isEmpty(oldSceneBind.getSceneBindId())) {
//                    if (defaultActin != null && !Action.isActionEqualExceptUid(sceneBind, defaultActin)) {
//                        mSceneBindPersenter.setAction(defaultActin);
//                    }
                    continue;
                }

                sceneBind.setUid(device.getUid());
                sceneBind.setUserName(userName);
                sceneBind.setSceneBindId(Constant.NULL_DATA);
                sceneBind.setItemId(LibSceneTool.getItemId());
                sceneBind.setDeviceId(device.getDeviceId());
                sceneBind.setDelayTime(0);

                sceneBind.setDelFlag(DeleteFlag.NORMAL);
                sceneBind.setSceneNo(sceneNo);


//                //设置默认动作
//                Action defaultActin = BindTool.getDefaultAction(device, deviceStatusDao.selDeviceStatus(device.getDeviceId()), BindActionType.SCENE);
//                if (defaultActin != null) {
//                    Action.setData(sceneBind, defaultActin);
//                }
                binds.add(sceneBind);
            }
            mSceneBindPersenter.addSceneBinds(binds);
        }
    }

    @Override
    protected void onDeleteAction(Object tag) {
        super.onDeleteAction(tag);
        SceneBind sceneBind = (SceneBind) tag;
        LogUtil.d(TAG, "onDeleteAction()-sceneBind:" + sceneBind);
        doDeleteSceneBind(sceneBind);
    }

    public void setSceneName(String sceneName) {
        mCurSceneName = sceneName;
    }

    public void save() {
        showDialogNow(null);
        //没有设置情景名称
        if (StringUtil.isEmpty(mCurSceneName)) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.SCENE_NAME_EMPTY_ERROR);
            return;
        }

        //不能保存没有添加设备的情景
//        if (mSceneBindPersenter.getCurrentSceneBindList() == null || mSceneBindPersenter.getCurrentSceneBindList().isEmpty() && !mSceneBindPersenter.isDeleteSceneBind()) {
        if (mSceneBindPersenter.getCurrentSceneBindList() == null || mSceneBindPersenter.getCurrentSceneBindList().isEmpty()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.SCENE_BIND_NO_DEVICE);
            return;
        }

        //有绑定没有添加动作，提示失败
        if (mSceneBindPersenter.hasEmptyAction()) {
            dismissDialog();
            ToastUtil.toastError(ErrorCode.BIND_NONE_ORDER);
            return;
        }

        HashMap<Integer, List<SceneBind>> sceneBinds = mSceneBindPersenter.getConflictIrDevice();
        if (sceneBinds != null && sceneBinds.size() > 0) {
            List<CharSequence> tips = new ArrayList<CharSequence>();
            for (Map.Entry<Integer, List<SceneBind>> entry : sceneBinds.entrySet()) {
                List<SceneBind> sceneBinds1 = entry.getValue();
                String[] data = DeviceTool.getBindItemName(mAppContext, sceneBinds1.get(0).getUid(), sceneBinds1.get(0).getDeviceId());
                String itemName = data[0] + "" + data[1] + " " + data[2];
                tips.add(itemName);
                for (SceneBind sceneBind : sceneBinds1) {
                    String action = DeviceTool.getActionName(mAppContext, sceneBind.getCommand(), sceneBind.getValue1(),
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

        if (isAddNewScene) {
            //添加情景和情景绑定
            addNewScene(mCurSceneName);
        } else {
            //编辑情景
            if (isSceneChanged(mCurSceneName, mPic)) {
                modifyScene(mCurSceneName);
            } else {
                MyLogger.jLog().d();
                saveSceneBinds();
            }
        }
    }

    private void saveSceneBinds() {
        if (!isSceneBindChanged()) {
            ToastUtil.showToast(R.string.operation_success);
            dismissDialog();
            onSceneManagerListener.onSceneFinish();
        } else {
            mSceneBindPersenter.saveSceneBinds();
        }
    }

    private boolean isSceneBindChanged() {
        boolean isAddNewSceneBind = mSceneBindPersenter.isAddNewSceneBind();
        boolean isEditSceneBind = mSceneBindPersenter.isEditSceneBind();
        boolean isDeleteSceneBind = mSceneBindPersenter.isDeleteSceneBind();
        return isAddNewSceneBind || isEditSceneBind || isDeleteSceneBind;
    }

    private void addNewScene(String sceneName) {
        LogUtil.d(TAG, "addNewScene()-sceneName:" + sceneName + ",mPic:" + mPic);
        if (StringUtil.isEmpty(sceneName)) {
            dismissDialog();
            ToastUtil.showToast(R.string.scene_name_empty);
            return;
        }
        new AddScene(mAppContext) {
            @Override
            public void onAddSceneResult(String uid, Scene scene, int result) {
                unregisterEvent(this);
                LogUtil.d(TAG, "onAddSceneResult()-uid:" + uid + ",scene:" + scene + ",result:" + result);
                if (result == ErrorCode.SUCCESS) {
                    SceneManagerFragment.this.scene = scene;
                    isAddNewScene = false;
                    //添加sceneBind
                    if (mSceneBindPersenter.isAddNewSceneBind()) {
//                        addNewSceneBind();
                        mSceneBindPersenter.setScene(scene);
                        mSceneBindPersenter.saveSceneBinds();
                    } else {
                        ToastUtil.showToast(R.string.operation_success);
                        dismissDialog();
                        onSceneManagerListener.onSceneFinish();
                    }
                } else {
                    dismissDialog();
                    ToastUtil.toastError(result);
                }
            }
        }.addScene(mainUid, userName, sceneName, mPic);
    }

    private void modifyScene(final String sceneName) {
        new ModifyScene(mAppContext) {
            @Override
            public void onModifySceneResult(String uid, int result) {
                unregisterEvent(this);
                LogUtil.d(TAG, "onModifySceneResult()-uid:" + uid + ",result:" + result);
                scene.setSceneName(sceneName);
                scene.setPic(mPic);

                if (result == ErrorCode.SUCCESS) {
                    saveSceneBinds();
                } else {
                    dismissDialog();
                    ToastUtil.toastError(result);
                }
            }
        }.modifyScene(mainUid, userName, scene.getSceneNo(), sceneName, mPic);
    }


    /**
     * @param newName
     * @param newPic
     * @return情景有无改变
     */
    private boolean isSceneChanged(String newName, int newPic) {
        if (!isAddNewScene && scene != null) {
            Scene oldScene = new SceneDao().selScene(mainUid, scene.getSceneNo());
            if (oldScene != null && newName.equals(oldScene.getSceneName()) && newPic == oldScene.getPic()) {
                return false;
            }
        } else if (isAddNewScene) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvTime:
                //选择延迟时间
                editingSceneBind = (SceneBind) v.getTag();
                mSceneBindPersenter.selectSceneBind(editingSceneBind);
                Device irDevice = mDeviceDao.selDevice
                        (mainUid, editingSceneBind.getDeviceId());
                if (irDevice != null && DeviceUtil.isIrDevice(irDevice)) {
                    setIrDeviceFlag(true);
                } else {
                    setIrDeviceFlag(false);
                }
                break;
            case R.id.linearAction:
//            case R.id.tvAction:
                //选择设备动作
                editingSceneBind = (SceneBind) v.getTag();
                mSceneBindPersenter.selectSceneBind(editingSceneBind);
                String deviceId = editingSceneBind.getDeviceId();
                Device device = mDeviceDao.selDevice
                        (mainUid, deviceId);
                if (device != null && DeviceUtil.isIrOrWifiAC(device)) {
                    selDeviceActions(device, editingSceneBind, mSceneBindPersenter.getSameDeviceSceneBinds(deviceId), mBindActionType);
                } else {
                    selDeviceAction(device, editingSceneBind, mBindActionType);
                }
                break;
        }
        super.onClick(v);
    }

    @Override
    protected int getDelayTime() {
        if (editingSceneBind != null) {
            return editingSceneBind.getDelayTime();
        }
        return super.getDelayTime();
    }

    /**
     * 得到所有已选择的deviceId
     *
     * @return
     */
    @Override
    protected ArrayList<String> getBindDeviceIds() {
        return mSceneBindPersenter.getSelectedDeviceIds();
    }

    private void setAddView(boolean isNoneBind) {
        if (isNoneBind) {
            mSceneBind_lv.setVisibility(View.GONE);
            mAdd_btn.setVisibility(View.GONE);
            mAddBindTextView.setVisibility(View.VISIBLE);
        } else {
            mSceneBind_lv.setVisibility(View.VISIBLE);
            mAdd_btn.setVisibility(View.VISIBLE);
            mAddBindTextView.setVisibility(View.GONE);
        }
    }

    /**
     * 提示是否保存修改界面
     */
    private void showConfirmSavePopup() {
        if (mConfirmSavePopup == null) {
            mConfirmSavePopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    showDialogNow(null);
                    save();
                }

                @Override
                public void cancel() {
                    super.cancel();
                    onSceneManagerListener.onSceneFinish();
                }
            };
        }
        mConfirmSavePopup.showPopup(getActivity(), R.string.save_content, R.string.save, R.string.unsave);
    }

    /**
     * @return true已经修改了情景
     */
    public boolean isChanged() {
        MyLogger.jLog().d("isSceneChanged=" + isSceneChanged() + "  mSceneBindPersenter.isSceneBindChanged=" + mSceneBindPersenter.isSceneBindChanged() + " isSceneBindChanged=" + isSceneBindChanged());
        return isSceneChanged() || mSceneBindPersenter.isSceneBindChanged() || isSceneBindChanged();
    }

    private boolean isSceneChanged() {
        return isSceneChanged(mCurSceneName, mPic);
    }

    public interface OnSceneManagerListener {
        void onSceneFinish();

        /**
         * @param hasSceneBind true 情景有绑定设备；false 情景没有绑定设备
         */
        void onHasSceneBind(boolean hasSceneBind);
    }

    public void registerFinishListener(OnSceneManagerListener onSceneManagerListener) {
        this.onSceneManagerListener = onSceneManagerListener;
    }

    @Override
    public void onDestroy() {
        if (mSceneBindPersenter != null) {
            mSceneBindPersenter.cancel();
        }
        super.onDestroy();
    }
}
