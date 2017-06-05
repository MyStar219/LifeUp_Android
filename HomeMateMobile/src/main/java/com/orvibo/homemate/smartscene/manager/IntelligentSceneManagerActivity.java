package com.orvibo.homemate.smartscene.manager;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.dao.LinkageDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.IntelligentSceneType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.SmartSceneConstant;
import com.orvibo.homemate.model.DeleteLinkage;
import com.orvibo.homemate.model.DeleteScene;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.IntelligentSceneTool;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.TimingCountdownTabView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.io.Serializable;
import java.util.List;

/**
 * 添加编辑删除联动都在此activity
 *
 * <p>
 * 如果是添加的sceneBind，那么其sceneBindId默认为{@link Constant#INVALID_NUM}，所有新的sceneBindId<=-1</p>
 * Created by Smagret on 2015/10/16.
 */

public class IntelligentSceneManagerActivity extends BaseActivity
        implements LinkageManagerFragment.OnLinkageManagerListener, SceneManagerFragment.OnSceneManagerListener {
    private static final String TAG = IntelligentSceneManagerActivity.class.getSimpleName();
    private Linkage linkage;
    private Scene scene;

    /**
     * {@link IntelligentSceneConditionType}
     */
    private int conditionType = IntelligentSceneConditionType.OTHER;//默认为其他模式

    private EditTextWithCompound mIntelligentSceneNameEditText;
    private TextView mAddConditionTextView;
    private TextView mTitleTextView;
    private TextView mSaveTextView;
    private TextView deleteTextView;
    private static final int SELECT_CONDITION = 4;

    private List<LinkageCondition> linkageConditions;
    private List<LinkageCondition> srcLinkageConditions;
    private LinkageManagerFragment mLinkageManagerFragment;
    private SceneManagerFragment mSceneManagerFragment;

    private ConfirmAndCancelPopup mDeletePopup;

//    private OnLinkageChangedListener onLinkageChangedListener;
//    private OnSceneChangedListener onSceneChangedListener;

    /**
     * 返回提示是否保存界面
     */
    private ConfirmAndCancelPopup mConfirmSavePopup;

    /**
     * true添加联动，false编辑。创建linkage成功后需要设置为false
     */
    private boolean isAddNewLinkage = false;

    /**
     * true添加情景，false编辑。创建scene成功后需要设置为false
     */
    private boolean isAddNewScene = false;

    private String intelligentName;

    private int intelligentSceneType = IntelligentSceneType.LINKAGE;

    private boolean hasSceneBind = false;
    private boolean hasLinkageOutput = false;
    private TextView mScenename;
    private TimingCountdownTabView mTimingCountdownTabView;
    public static final int SECURITY_STATUS = 0;
    public static final int UNSECURITY_STATUS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_intelligent_scene);
        linkage = (Linkage) getIntent().getSerializableExtra(Constant.LINKAGE);
        scene = (Scene) getIntent().getSerializableExtra(Constant.SCENE);
        LogUtil.d(TAG, "onCreate()-linkage:" + linkage);
        findViews();
        init();
    }

    private void findViews() {

        mIntelligentSceneNameEditText = (EditTextWithCompound) findViewById(R.id.intelligentSceneNameEditText);
        mIntelligentSceneNameEditText.setRightful(getResources().getDrawable(R.drawable.item_selector));
        mIntelligentSceneNameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        mAddConditionTextView = (TextView) findViewById(R.id.addConditionTextView);
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mSaveTextView = (TextView) findViewById(R.id.saveTextView);
        deleteTextView = (TextView) findViewById(R.id.deleteTextView);
       /* //布防与撤防的设置
        mScenename = (TextView) findViewById(R.id.scenename);
        mTimingCountdownTabView = (TimingCountdownTabView) findViewById(R.id.topSecurityId);
        mTimingCountdownTabView.countdownTopTab.setName(getString(R.string.intelligent_security_stop));
        mTimingCountdownTabView.timingTopTab.setName(getString(R.string.intelligent_security_start));
        mTimingCountdownTabView.setSelectedPosition(SECURITY_STATUS);
        mTimingCountdownTabView.setOnTabSelectedListener(this);
        if (!isCommonLinkage) {
            mScenename.setVisibility(View.GONE);
            mIntelligentSceneNameEditText.setVisibility(View.GONE);
            mTimingCountdownTabView.setVisibility(View.VISIBLE);
            mTitleTextView.setVisibility(View.GONE);
        }*/


    }

    private void init() {
        isAddNewLinkage = linkage == null;
        isAddNewScene = scene == null;

        setDeleteViewVisible(!isAddNewLinkage || !isAddNewScene);

        initIntelligentName();
        if (!isAddNewLinkage) {
            intelligentSceneType = IntelligentSceneType.LINKAGE;

        } else if (!isAddNewScene) {
            intelligentSceneType = IntelligentSceneType.SCENE;
            mAddConditionTextView.setTextColor(getResources().getColor(R.color.gray_white));
        }
        initCondition(linkageConditions, conditionType);
        initListener();
        if (mLinkageManagerFragment != null) {
            mLinkageManagerFragment.setLinkageName(intelligentName);
        }
        if (mSceneManagerFragment != null) {
            mSceneManagerFragment.setSceneName(intelligentName);
        }
    }

    /**
     * 设置智能场景名称
     */
    private void initIntelligentName() {

        if (linkage != null) {
            if (!isAddNewLinkage) {
                conditionType = IntelligentSceneTool.getConditionType(linkage);
            }
            intelligentName = linkage.getLinkageName();
            linkageConditions = new LinkageConditionDao().selLinkageConditionsByLinkageId(linkage.getLinkageId());
            srcLinkageConditions = new LinkageConditionDao().selLinkageConditionsByLinkageId(linkage.getLinkageId());
            mTitleTextView.setText(getResources().getString(R.string.intelligent_scene_edit));
        } else if (scene != null) {
            if (!isAddNewScene) {
                conditionType = IntelligentSceneConditionType.CLICKED;
            }
            intelligentName = scene.getSceneName();
            mTitleTextView.setText(getResources().getString(R.string.intelligent_scene_edit));
        } else {
            //设置默认名称，智能场景1，智能场景2...
            final String intelligentSceneNameFormat = getString(R.string.intelligent_scene);
            List<String> intelligentSceneNames = new LinkageDao().selAllLinkageNames(UserCache.getCurrentUserId(mAppContext));
            List<String> sceneNames = new SceneDao().selAllSceneNames(currentMainUid);
            intelligentSceneNames.addAll(sceneNames);
            intelligentName = StringUtil.getDefaultName(intelligentSceneNameFormat, intelligentSceneNames);
        }
        mIntelligentSceneNameEditText.setText(intelligentName);
        if (!StringUtil.isEmpty(intelligentName)) {
            mIntelligentSceneNameEditText.setSelection(mIntelligentSceneNameEditText.getText().toString().length());
        }
    }

    /**
     * 设置智能场景启动条件
     *
     * @param conditionType
     */
    private void initCondition(List<LinkageCondition> conditions, int conditionType) {
        Drawable drawable = getResources().getDrawable(IntelligentSceneTool.getConditionIconResId(conditionType, true));
        if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            //小方图标
            Device device = new Device();
            device.setDeviceType(DeviceType.ALLONE);
            drawable = DeviceTool.getDeviceDrawable(device, true);
        }
        mAddConditionTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, getResources().getDrawable(R.drawable.device_item_arrow_right), null);
        if ((conditions == null || conditions.isEmpty()) && conditionType == IntelligentSceneConditionType.OTHER) {
            mAddConditionTextView.setText(R.string.intelligent_scene_add_condition);
        } else {
            String deviceId = IntelligentSceneTool.getDeviceId(conditions);
            if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
                //小方只需显示icon和名称
                Device device = new DeviceDao().selDevice(deviceId);
                if (device != null) {
                    //格式：轻按 设备名称
                    mAddConditionTextView.setText(getString(R.string.intelligent_scene_preess) + device.getDeviceName());
                } else {
                    LogUtil.e(TAG, "initCondition()-Could not found the device by deviceId " + deviceId);
                }
            } else {
                //楼层，房间，设备
                String[] data = DeviceTool.getBindItemName(mAppContext, currentMainUid, IntelligentSceneTool.getDeviceId(conditions));
                int resId = IntelligentSceneTool.getMiniConditionTypeNameResId(conditionType);
                String name = "";
                if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR || conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
                    int conditionInt = SmartSceneConstant.Condition.GREATER_THAN_EQUAL;
                    int value = 0;
                    LinkageCondition linkageCondition = null;
                    if (conditions != null && !conditions.isEmpty()) {
                        for (LinkageCondition condition : conditions) {
                            if (condition.getLinkageType() == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                                conditionInt = condition.getCondition();
                                value = condition.getValue();
                                linkageCondition = condition;
                                break;
                            }
                        }
                    }
                    String t1 = conditionInt == SmartSceneConstant.Condition.GREATER_THAN_EQUAL ? getString(R.string.smart_scene_condition_exceed) : getString(R.string.smart_scene_condition_under);
                    String t2 = value + "";
                    String t3 = "%";
                    String t4 = data != null && data.length > 2 ? data[2] : "";
                    if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
//                    温度%1$s%2$s%3$s（%4$s）
                        t3 = CommonCache.getTemperatureUnit();
                        if (!CommonCache.isCelsius()) {
                            t2 = MathUtil.geFahrenheitData(value) + "";
                        }
                        name = String.format(getString(resId), t1, t2, t3, t4);
                    } else {
                        name = String.format(getString(resId), t1, t2, t3, t4);
                    }
                } else {
                    name = String.format(getString(resId), data != null && data.length > 2 ? data[2] : "");
                }
                CharSequence str = Html.fromHtml(String.format("<font color=\"#000000\">%s</font><br><font color=\"#868686\" ><small>%s</small></font>", name, data[0] + data[1]));
                if (conditionType == IntelligentSceneConditionType.CLICKED) {
                    mAddConditionTextView.setText(name);
                } else {
                    mAddConditionTextView.setText(str);
                }
            }
        }
        onConditionSelected(conditionType);
    }

    private void initListener() {
        mAddConditionTextView.setOnClickListener(this);
        mSaveTextView.setOnClickListener(this);
        deleteTextView.setOnClickListener(this);
        mIntelligentSceneNameEditText.addTextChangedListener(watcher);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        //门窗磁传感器5、6
        @Override
        public void afterTextChanged(Editable s) {
            if (conditionType == IntelligentSceneConditionType.CLICKED) {
                mSceneManagerFragment.setSceneName(s.toString());
            } else
//            if ((conditionType == IntelligentSceneConditionType.LOCK_OPENED)
//                    || (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED)
//                    || (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED)
//                    || (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED))
//            {
                mLinkageManagerFragment.setLinkageName(s.toString());
        }
//        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onConditionSelected(int conditionType) {
        LogUtil.d(TAG, "onConditionSelected()-conditionType:" + conditionType);
        if (isFinishingOrDestroyed()) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // hideAllFragment(transaction);
        switch (conditionType) {
            case IntelligentSceneConditionType.CLICKED:
                intelligentSceneType = IntelligentSceneType.SCENE;
                showSceneManagerFragment(transaction);
                break;
            case IntelligentSceneConditionType.LOCK_OPENED:
            case IntelligentSceneConditionType.DOOR_SENSOR_OPENED:
            case IntelligentSceneConditionType.DOOR_SENSOR_CLOSED:
            case IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED:
            case IntelligentSceneConditionType.TEMPERATURE_SENSOR:
            case IntelligentSceneConditionType.HUMIDITY_SENSOR:
            case IntelligentSceneConditionType.CLICK_ALLONE:
                intelligentSceneType = IntelligentSceneType.LINKAGE;
                showLinkageManagerFragment(transaction);
                break;
            default:
                showLinkageManagerFragment(transaction);
                break;

        }
        transaction.commitAllowingStateLoss();
    }

    private void showSceneManagerFragment(FragmentTransaction transaction) {
        hideFragment(transaction, mLinkageManagerFragment);
        if (mSceneManagerFragment == null) {
            mSceneManagerFragment = new SceneManagerFragment();
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(Constant.SCENE, scene);
            //通过setArguments给fragment传递数据
            mSceneManagerFragment.setArguments(mBundle);
            mSceneManagerFragment.registerFinishListener(this);
        }
        showFragment(transaction, mSceneManagerFragment);
    }

    private void showLinkageManagerFragment(FragmentTransaction transaction) {
        hideFragment(transaction, mSceneManagerFragment);
        if (mLinkageManagerFragment == null) {
            mLinkageManagerFragment = new LinkageManagerFragment();
            Bundle mBundle = new Bundle();
            mBundle.putInt(IntentKey.LINKAGE_CONDITION_TYPE, conditionType);
            mBundle.putSerializable(Constant.LINKAGE, linkage);
            mBundle.putSerializable(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
            //通过setArguments给fragment传递数据
            mLinkageManagerFragment.setArguments(mBundle);
            mLinkageManagerFragment.registerFinishListener(this);
        }
        showFragment(transaction, mLinkageManagerFragment);
    }

    private void hideFragment(FragmentTransaction transaction, BaseFragment fragment) {
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private synchronized void showFragment(FragmentTransaction ft, BaseFragment baseFragment) {
        boolean isAdded = baseFragment.isAdded();
        LogUtil.d(TAG, "showFragment()-baseFragment:" + baseFragment + ",isAdded:" + isAdded);
        if (!isAdded) {
            ft.add(R.id.container, baseFragment);
        } else {
            ft.show(baseFragment);
        }
        baseFragment.onVisible();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-requestCode:" + requestCode + ",resultCode:" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_CONDITION: {
                    //选择条件
                    linkageConditions = (List<LinkageCondition>) data.getSerializableExtra(Constant.LINKAGE_CONDITIONS);
                    conditionType = data.getIntExtra(IntentKey.LINKAGE_CONDITION_ACTION, IntelligentSceneConditionType.OTHER);
                    LogUtil.d(TAG, "onActivityResult()-conditionType:" + conditionType + ",linkageConditions:" + linkageConditions);
                    initCondition(linkageConditions, conditionType);
                    if (conditionType == IntelligentSceneConditionType.CLICKED) {
                        mSceneManagerFragment.setSceneName(mIntelligentSceneNameEditText.getText().toString());
                    } else if ((conditionType == IntelligentSceneConditionType.LOCK_OPENED)
                            || (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED)
                            || (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED)
                            || (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED)
                            || (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR)
                            || (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR)
                            || (conditionType == IntelligentSceneConditionType.CLICK_ALLONE)
                            ) {
                        String uid = UserCache.getCurrentMainUid(mAppContext);
                        if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
                            uid = data.getStringExtra(IntentKey.UID);
                        }
                        if (TextUtils.isEmpty(uid)) {
                            uid = UserCache.getCurrentMainUid(mAppContext);
                        }
                        mLinkageManagerFragment.setLinkageCondition(uid, linkageConditions, conditionType);
                        mLinkageManagerFragment.setLinkageName(mIntelligentSceneNameEditText.getText().toString());
                    }
                    break;
                }
            }
        }
    }

    private void save() {
        //优先判断网络
        if (!NetUtil.isNetworkEnable(mAppContext)) {
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            return;
        }

        if (intelligentSceneType == IntelligentSceneType.SCENE) {
            if (mSceneManagerFragment != null) {
                mSceneManagerFragment.save();
            }
        } else if (intelligentSceneType == IntelligentSceneType.LINKAGE) {
            if (mLinkageManagerFragment != null) {
                mLinkageManagerFragment.save();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addConditionTextView:
                //添加条件
                if (!ClickUtil.isFastDoubleClick()) {
                    if (intelligentSceneType == IntelligentSceneType.SCENE) {
                        if (hasSceneBind) {
                            if (isAddNewScene) {
                                ToastUtil.showToast(R.string.intelligent_scene_select_condition_error);
                            }
                        } else {
                            Intent conditionIntent = new Intent(this, IntelligentSceneSelectConditionActivity.class);
                            conditionIntent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                            conditionIntent.putExtra(Constant.CONDITION_TYPE, conditionType);
                            startActivityForResult(conditionIntent, SELECT_CONDITION);
                        }
                    } else if (intelligentSceneType == IntelligentSceneType.LINKAGE) {
                        Intent conditionIntent = new Intent(this, IntelligentSceneSelectConditionActivity.class);
                        conditionIntent.putExtra(Constant.LINKAGE_CONDITIONS, (Serializable) linkageConditions);
                        conditionIntent.putExtra(Constant.CONDITION_TYPE, conditionType);
                        conditionIntent.putExtra(Constant.HAS_LINKAGEOUTPUT, hasLinkageOutput);
                        startActivityForResult(conditionIntent, SELECT_CONDITION);
                    }
                }
                break;
            case R.id.saveTextView:
                //保存
                save();
                break;
            case R.id.deleteTextView:
                //优先判断网络
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
                    return;
                }
                if (!isAddNewLinkage) {
                    showDeleteLinkageView();
                } else if (!isAddNewScene) {
                    showDeleteScenePopup(scene);
                }
                break;
        }
        super.onClick(v);
    }

    /**
     * @param visible true显示删除界面
     */
    private void setDeleteViewVisible(boolean visible) {
        deleteTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showDeleteLinkageView() {
        if (linkage != null) {
            DialogFragmentTwoButton deleteLinkageDialog = new DialogFragmentTwoButton();
            String title = String.format(getString(R.string.intelligent_scene_delete_content), linkage.getLinkageName() + "");
            deleteLinkageDialog.setTitle(title);
            deleteLinkageDialog.setLeftButtonText(getString(R.string.cancel));
            deleteLinkageDialog.setRightButtonText(getString(R.string.delete));
            deleteLinkageDialog.setRightTextColor(getResources().getColor(R.color.red));
            deleteLinkageDialog.hideContent();
            deleteLinkageDialog.show(getFragmentManager(), "");
            deleteLinkageDialog.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {

                }

                @Override
                public void onRightButtonClick(View view) {
                    //删除联动
                    doDeleteLinkage();
                }
            });
        }
    }

    /**
     * 删除联动
     */
    private void doDeleteLinkage() {
        showDialogNow();
        DeleteLinkage deleteLinkage = new DeleteLinkage(mAppContext) {
            @Override
            public void onDeleteLinkageResult(String uid, String linkageId, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.intelligent_scene_delete_success);
                    finish();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
        deleteLinkage.deleteLinkage(linkage.getUid(), userName, linkage.getLinkageId());
    }

    /**
     * 显示删除绑定动作界面
     *
     * @param scene
     */
    private void showDeleteScenePopup(final Scene scene) {
        if (scene != null) {
            mDeletePopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    doDeleteScene(scene);
                }
            };
            mDeletePopup.showPopup(mContext, String.format(getString(R.string.scene_delete_content), scene.getSceneName()),
                    getString(R.string.delete), getString(R.string.cancel));
        }
    }

    private void doDeleteScene(Scene scene) {
        //系统自带情景不能删除
        if (LibSceneTool.isSystemScene(scene.getOnOffFlag())) {
            ToastUtil.showToast(R.string.scene_delete_system_error);
        } else {
            showDialog();
            new DeleteScene(mAppContext) {
                @Override
                public void onDeleteSceneResult(String uid, String sceneId, int result) {
                    LogUtil.d(TAG, "onDeleteSceneResult()-uid:" + uid + ",sceneId:" + sceneId + ",result:" + result);
                    if (mDeletePopup != null && mDeletePopup.isShowing()) {
                        mDeletePopup.dismiss();
                    }
                    dismissDialog();
                    if (result == ErrorCode.SUCCESS) {
                        ToastUtil.showToast(R.string.delete_success);
                        finish();
                    } else {
                        ToastUtil.toastError(result);
                    }
                }
            }.delete(currentMainUid, userName, scene.getSceneNo());
        }
    }

//    public interface OnLinkageChangedListener{
//        void onLinkageConditionChanged(List<LinkageCondition> conditions);
//        void onLinkageNameChanged(String linkageName);
//    }
//
//    public void registerLinkageChangedListener(OnLinkageChangedListener onLinkageChangedListener){
//        this.onLinkageChangedListener = onLinkageChangedListener;
//    }
//
//    public interface OnSceneChangedListener{
//        void onSceneNameChanged(String linkageName);
//    }
//
//    public void registerSceneChangedListener(OnSceneChangedListener onSceneChangedListener){
//        this.onSceneChangedListener = onSceneChangedListener;
//    }

    /**
     * 提示是否保存修改界面
     */
    private void showConfirmSavePopup() {
        if (mConfirmSavePopup == null) {
            mConfirmSavePopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    save();
                }

                @Override
                public void cancel() {
                    super.cancel();
                    finish();
                }
            };
        }
        mConfirmSavePopup.showPopup(mContext, R.string.save_content, R.string.save, R.string.unsave);
    }


    @Override
    public void leftTitleClick(View v) {
        if (isChanged()) {
            showConfirmSavePopup();
        } else {
            super.leftTitleClick(v);
        }
    }

    /**
     * @return true已经修改了联动
     */
    private boolean isChanged() {
        boolean isLinkageChanged = false;
        boolean isSceneChanged = false;
        if (mLinkageManagerFragment != null) {
            isLinkageChanged = mLinkageManagerFragment.isChanged();
        }
        if (mSceneManagerFragment != null) {
            isSceneChanged = mSceneManagerFragment.isChanged();
        }

        return isLinkageChanged || isSceneChanged;
    }

    @Override
    public void onBackPressed() {
        if (!isDialogShowing()
                && isChanged()
                && (mConfirmSavePopup == null || (mConfirmSavePopup != null && !mConfirmSavePopup.isShowing()))) {
            showConfirmSavePopup();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLinkageFinish() {
        finish();
    }

    @Override
    public void onHasLinkageOutput(boolean hasLinkageOutput) {
        this.hasLinkageOutput = hasLinkageOutput;
    }

    @Override
    public void onSceneFinish() {
        finish();
    }

    @Override
    public void onHasSceneBind(boolean hasSceneBind) {
        this.hasSceneBind = hasSceneBind;
    }


}
