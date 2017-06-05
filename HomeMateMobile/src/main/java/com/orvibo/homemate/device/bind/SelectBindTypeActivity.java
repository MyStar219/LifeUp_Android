package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.BindAction;
import com.orvibo.homemate.bo.BindFail;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.RemoteBind;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceCommenDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.dao.RemoteBindDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KeyAction;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.RequestCode;
import com.orvibo.homemate.model.bind.remote.DeleteRemoteBindAction;
import com.orvibo.homemate.model.bind.remote.RemoteBindAction;
import com.orvibo.homemate.util.ActivityTool;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.ActionView;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.List;

/**
 * 遥控器情景面板绑定
 * 选择动作类型(情景或设备动作)
 * 需要传的参数有deviceId、keyNo和keyAction
 *
 * @author huangqiyao
 * @date 2015/5/4
 */
public class SelectBindTypeActivity extends BaseActivity
//        implements CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = SelectBindTypeActivity.class.getSimpleName();
    private static final String SELECT_YES = "yes";
    private static final String SELECT_NO = "no";

    /**
     * 选择的情景
     */
    private List<BindAction> mSceneBindActions;

    /**
     * 选择的设备动作
     */
    private List<BindAction> mDeviceBindActions;

    private RemoteBindAction mRemoteBindAction;
    private RemoteBind mRemoteBind;

    //view
//    private CheckBox mSceneAction_cb;
//    private CheckBox mDeviceAction_cb;

    private TextView tv_sceneAction;
    private TextView tv_deviceAction;

    private TextView mSceneAction_tv;
    private TextView device_tv;
    /**
     * 设备动作
     */
//    private TextView action_tv;
    private LinearLayout action_ll;
    //    private LinearLayout color_ll;
    private ImageView colorView;
    private Button btnSave;
    private ActionView mActionView;

    private ToastPopup toastPopup;

    private DeviceDao mDeviceDao;
    private DeviceStatusDao deviceStatusDao;
    private Action mSelectedAction;

    private String mSelectedDeviceId;
    //智能遥控器
    private Device mRemoteDevice;
    private int mKeyNo;
    private int mKeyAction;

    private Scene mScene;
    private int mOldSceneId;
    private boolean isAddBind = false;

    private volatile int mItemId;

    /**
     * true添加绑定或修改绑定成功
     */
    private volatile boolean isSuccess = false;

    /**
     * 绑定成功的id
     */
    private volatile String mAddSuccessBindId;

    /**
     * 情景动作缓存
     */
    private RemoteBind mSceneActionCache;

    /**
     * 设备动作缓存
     */
    private RemoteBind mDeviceActionCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_scene_button);
        mDeviceDao = new DeviceDao();
        deviceStatusDao = new DeviceStatusDao();

        mSceneActionCache = new RemoteBind();
        mSceneActionCache.setBindedDeviceId("");
        mSceneActionCache.setCommand(DeviceOrder.SCENE_CONTROL);

        mDeviceActionCache = new RemoteBind();

        findViews();
        initData();
        // 不能兼容主从机
        setDevice(currentMainUid, mSelectedDeviceId);
        setAction(mSelectedAction);


        if (isAddBind) {
            mSceneAction_tv.setClickable(false);
            mSceneAction_tv.setTextColor(getResources().getColor(R.color.font_white_gray));
            device_tv.setClickable(false);
            device_tv.setTextColor(getResources().getColor(R.color.font_white_gray));
            action_ll.setClickable(false);
            mActionView.setActionTextColor(getResources().getColor(R.color.font_white_gray), false);
            // action_tv.setTextColor(getResources().getColor(R.color.font_white_gray));
        } else {
            if (mSelectedAction != null) {
                boolean isScene = mSelectedAction.getCommand().equals(DeviceOrder.SCENE_CONTROL);
                mSceneAction_tv.setClickable(isScene);
                device_tv.setClickable(!isScene);
                action_ll.setClickable(!isScene);
                if (isScene) {
//                    mSceneAction_cb.setChecked(true);
                    setSceneView(true);
                    setDeviceView(false);
                    Action.setData(mSceneActionCache, mSelectedAction);
                    mSceneActionCache.setBindedDeviceId("");
                } else {
                    // mDeviceAction_cb.setChecked(true);
                    setSceneView(false);
                    setDeviceView(true);
                    Action.setData(mDeviceActionCache, mSelectedAction);
                    mSceneActionCache.setBindedDeviceId(mSelectedDeviceId);
                }
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        mRemoteDevice = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        mKeyNo = intent.getIntExtra("keyNo", Constant.INVALID_NUM);
        mKeyAction = intent.getIntExtra("keyAction", KeyAction.SINGLE_CLICK);
        if (mRemoteDevice == null || mKeyNo == Constant.INVALID_NUM) {
            throw new RuntimeException("device or keyNo not set.");
        }
        mRemoteBind = (RemoteBind) intent.getSerializableExtra("remoteBind");
        if (mRemoteBind != null) {
            isAddBind = false;
            mSelectedDeviceId = mRemoteBind.getBindedDeviceId();
            mSelectedAction = RemoteBind.getAction(mRemoteBind);
            mSelectedAction.setDeviceId(mSelectedDeviceId);
            mSelectedAction.setUid(mRemoteBind.getUid());

            String command = mRemoteBind.getCommand();
            if (DeviceOrder.SCENE_CONTROL.equals(command)) {
                mSceneActionCache = mRemoteBind.clone();

                mDeviceActionCache.setItemId(mRemoteBind.getItemId());
                mDeviceActionCache.setKeyAction(mRemoteBind.getKeyAction());
                mDeviceActionCache.setKeyNo(mRemoteBind.getKeyNo());
                mDeviceActionCache.setUid(mRemoteBind.getUid());
                mDeviceActionCache.setAlarmType(mRemoteBind.getAlarmType());
                mDeviceActionCache.setKeyName(mRemoteBind.getKeyName());
                mDeviceActionCache.setDelayTime(mRemoteBind.getDelayTime());
            } else {
                mDeviceActionCache = mRemoteBind.clone();

                mSceneActionCache.setItemId(mRemoteBind.getItemId());
                mSceneActionCache.setKeyAction(mRemoteBind.getKeyAction());
                mSceneActionCache.setKeyNo(mRemoteBind.getKeyNo());
                mSceneActionCache.setUid(mRemoteBind.getUid());
                mSceneActionCache.setAlarmType(mRemoteBind.getAlarmType());
                mSceneActionCache.setKeyName(mRemoteBind.getKeyName());
                mSceneActionCache.setDelayTime(mRemoteBind.getDelayTime());
            }
        } else {
            isAddBind = true;
        }

        LogUtil.d(TAG, "initData()-mRemoteDevice:" + mRemoteDevice + ",mRemoteBind:" + mRemoteBind);
    }

    private void findViews() {
        String name = getIntent().getStringExtra(IntentKey.REMOTE_KEY_NAME);
        String title = String.format(getString(R.string.device_set_title_key), name);
        setTitle(title);

//        mSceneAction_cb = (CheckBox) findViewById(R.id.sceneAction_cb);
//        mDeviceAction_cb = (CheckBox) findViewById(R.id.deviceAction_cb);
//        mSceneAction_cb.setOnCheckedChangeListener(this);
//        mDeviceAction_cb.setOnCheckedChangeListener(this);

        tv_sceneAction = (TextView) findViewById(R.id.tv_sceneAction);
        tv_deviceAction = (TextView) findViewById(R.id.tv_deviceAction);
        tv_sceneAction.setOnClickListener(this);
        tv_deviceAction.setOnClickListener(this);

        mSceneAction_tv = (TextView) findViewById(R.id.sceneAction_tv);
        device_tv = (TextView) findViewById(R.id.device_tv);
        // action_tv = (TextView) findViewById(R.id.action_tv);
        action_ll = (LinearLayout) findViewById(R.id.action_ll);
        colorView = (ImageView) findViewById(R.id.colorView);
        //color_ll = (LinearLayout) findViewById(R.id.color_ll);

        btnSave = (Button) findViewById(R.id.btnSave);

        mSceneAction_tv.setOnClickListener(this);
        device_tv.setOnClickListener(this);
//        action_tv.setOnClickListener(this);
        action_ll.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        toastPopup = new ToastPopup();

        mActionView = (ActionView) findViewById(R.id.av_bindaction);
    }

    private void setDevice(String uid, String deviceId) {
        boolean isScene = false;
        if (mSelectedAction != null && !StringUtil.isEmpty(mSelectedAction.getCommand())) {
            isScene = mSelectedAction.getCommand().equals(DeviceOrder.SCENE_CONTROL);
        }
        if (uid == null || StringUtil.isEmpty(deviceId) || isScene) {
            device_tv.setText(R.string.device_set_bind_type_device_right);
        } else {
            String[] names = DeviceCommenDao.selFloorNameAndRoomNameAndDeviceName(uid, deviceId);
            if (names == null || names.length == 0) {
                device_tv.setText(R.string.device_set_bind_type_device_right);
            } else {
                if (names[1] == null || StringUtil.isEmpty(names[1])) {
                    names[0] = mContext.getString(R.string.not_set_floor_room);
                    names[1] = "";
                }
                //添加处理房间楼层是否显示的判断
                String name = null;
                if (isShowRoom()) {
                    name = names[2] + "\n" + names[0] + " " + names[1];
                } else {
                    name = names[2];
                }
                device_tv.setText(name);
            }
        }
    }

    private void setAction(Action action) {
        MyLogger.kLog().d(action);
        if (action == null || StringUtil.isEmpty(action.getCommand())) {
            // action_tv.setText(R.string.device_set_bind_type_action_right);
            mActionView.setEmptyView(Constant.INVALID_NUM);
            //  color_ll.setVisibility(View.GONE);
        } else {
            String order = action.getCommand();
            if (DeviceOrder.SCENE_CONTROL.equals(order)) {
                selectSceneAction(true);
                mScene = new SceneDao().selSceneBySceneId(currentMainUid, action.getValue1());
                if (mScene != null) {
                    mSceneAction_tv.setText(mScene.getSceneName());
                }
                mOldSceneId = action.getValue1();
                //    color_ll.setVisibility(View.GONE);
            } else {
                mActionView.setAction(action);
                selectDeviceAction(true, false);
            }
//                selectDeviceAction(true, false);
//                String actioName = DeviceTool.getActionName(mAppContext, action);
//                if (DeviceOrder.COLOR_CONTROL.equals(order)) {
//                    int[] rgb = ColorUtil.hsl2DeviceRgb(action.getValue4(), action.getValue3(), action.getValue2());
//                    colorView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
//                    color_ll.setVisibility(View.VISIBLE);
////                    action_tv.setText(getResources().getString(R.string.action_color_text));
////                    action_tv.setText(" ");
//                } else if (DeviceOrder.COLOR_TEMPERATURE.equals(order)) {
//                    int value2 = action.getValue2();
//                    if (value2 == 0) {
//                        //value2为0，即亮度为0，认为设备关闭动作
//                        color_ll.setVisibility(View.GONE);
//                        action_tv.setText(R.string.scene_action_off);
//                    } else {
//                        int colorTemperture = ColorUtil.colorToColorTemperture(action.getValue3());
//                        double[] rgb = ColorUtil.colorTemperatureToRGB(colorTemperture);
//                        int red = (int) rgb[0];
//                        int green = (int) rgb[1];
//                        int blue = (int) rgb[2];
//                        colorView.setBackgroundColor(Color.rgb(red, green, blue));
//                        color_ll.setVisibility(View.VISIBLE);
////                holder.tvAction.setText(mContext.getString(R.string.action_color_text));
//                        action_tv.setText(" ");
//                    }
//                } else {
//                    color_ll.setVisibility(View.GONE);
//                    Device device = null;
//                    if (!StringUtil.isEmpty(mSelectedDeviceId)) {
//                        device = mDeviceDao.selDevice
//                                (currentMainUid, mSelectedDeviceId);
//                    }
//                    if (device != null && device.getDeviceType() == DeviceType.CURTAIN_PERCENT && !DeviceOrder.STOP.equals(action.getCommand()) && action.getValue1() != 0 && action.getValue1() != 100) {
//                        String frequentlyModeString = FrequentlyModeUtil.getFrequentlyModeString(currentMainUid, mSelectedDeviceId, action.getValue1());
//                        if (!StringUtil.isEmpty(frequentlyModeString)) {
//                            action_tv.setText(frequentlyModeString);
//                        } else {
//                            action_tv.setText(actioName);
//                        }
//                    } else {
//                        action_tv.setText(actioName);
//                    }
//                }
//            }
        }
    }

    /**
     * 选择情景
     *
     * @param selected
     */
    private void selectSceneAction(boolean selected) {
        selectActionType(selected, true, false);
    }

    /**
     * true选择设备
     *
     * @param selected
     */
    private void selectDeviceAction(boolean selected, boolean isCommandNotSet) {
        selectActionType(selected, false, isCommandNotSet);
    }

    /**
     * 设置字体颜色
     *
     * @param selected
     * @param isSceneAction
     * @param isCommandNotSet true没有设置order
     */
    private void selectActionType(boolean selected, boolean isSceneAction, boolean isCommandNotSet) {
        LogUtil.d(TAG, "selectActionType()-selected:" + selected + ",isSceneAction:" + isSceneAction + ",isCommandNotSet:" + isCommandNotSet);
        setActionView(selected, isSceneAction, isCommandNotSet);
        if (selected) {
            if (isSceneAction) {
                //选择情景，把设备ui设置灰色和取消选中状态
                setSceneView(true);
                setDeviceView(false);
//                tv_sceneAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_checked), null, null, null);
//                tv_sceneAction.setTag(R.id.tag_select, SELECT_YES);
//                tv_deviceAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
//                tv_deviceAction.setTag(R.id.tag_select, SELECT_NO);
                //  mDeviceAction_cb.setChecked(false);
            } else {
                setSceneView(false);
                setDeviceView(true);
//                tv_deviceAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_checked), null, null, null);
//                tv_deviceAction.setTag(R.id.tag_select, SELECT_YES);
//                tv_sceneAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
//                tv_sceneAction.setTag(R.id.tag_select, SELECT_NO);
                //  mSceneAction_cb.setChecked(isSceneAction);
            }
        } else {
            setSceneView(false);
            setDeviceView(false);
//            tv_sceneAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
//            tv_sceneAction.setTag(R.id.tag_select, SELECT_NO);
//            tv_deviceAction.setCompoundDrawablesWithIntrinsicBounds (getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
//            tv_deviceAction.setTag(R.id.tag_select, SELECT_NO);
//            if (isSceneAction) {
//                mSceneAction_cb.setChecked(false);
//            } else {
//                mDeviceAction_cb.setChecked(false);
//            }
        }
    }

    /**
     * 选择情景
     *
     * @param selected true选中
     */
    private void setSceneView(boolean selected) {
        if (selected) {
            tv_sceneAction.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.check_box_checked), null, null, null);
            tv_sceneAction.setTag(R.id.tag_select, SELECT_YES);
        } else {
            tv_sceneAction.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
            tv_sceneAction.setTag(R.id.tag_select, SELECT_NO);
        }
    }

    /**
     * 选择设备
     *
     * @param selected true选中
     */
    private void setDeviceView(boolean selected) {
        if (selected) {
            tv_deviceAction.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.check_box_checked), null, null, null);
            tv_deviceAction.setTag(R.id.tag_select, SELECT_YES);
        } else {
            tv_deviceAction.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.check_box_normal), null, null, null);
            tv_deviceAction.setTag(R.id.tag_select, SELECT_NO);
        }
    }

    /**
     * 设置动作界面字体颜色、是否可点击
     *
     * @param selected        true选中状态
     * @param isSceneAction   true选择情景
     * @param isCommandNotSet true没有设置order
     */
    private void setActionView(boolean selected, boolean isSceneAction, boolean isCommandNotSet) {
        final int uncheckedColor = getResources().getColor(R.color.font_white_gray);
        final int checkedColor = getResources().getColor(R.color.font_black);
        if (selected) {
            mSceneAction_tv.setClickable(isSceneAction);
            mSceneAction_tv.setTextColor(isSceneAction ? checkedColor : uncheckedColor);

            device_tv.setClickable(!isSceneAction);
            action_ll.setClickable(!isSceneAction);

            device_tv.setTextColor(isSceneAction ? uncheckedColor : checkedColor);
            // action_tv.setTextColor(isSceneAction || isCommandNotSet ? uncheckedColor : checkedColor);
            mActionView.setActionTextColor(isSceneAction || isCommandNotSet ? uncheckedColor : checkedColor, !isSceneAction && !isCommandNotSet);
        } else {
            if (isSceneAction) {
                mSceneAction_tv.setClickable(false);
                mSceneAction_tv.setTextColor(uncheckedColor);
            } else {
                device_tv.setClickable(false);
                action_ll.setClickable(false);
                device_tv.setTextColor(uncheckedColor);
//                action_tv.setTextColor(uncheckedColor);
                mActionView.setActionTextColor(uncheckedColor, false);
            }
        }
    }

    //
//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        boolean isScene = buttonView != mDeviceAction_cb;
//        LogUtil.d(TAG, "onCheckedChanged()-isScene:" + isScene);
//        if (isScene) {
//            //选择情景动作
//            selectSceneAction(isChecked);
////            mSelectedDeviceId = "";
//            if (mSelectedAction != null) {
//                Action.setData(mSelectedAction, mSceneActionCache);
//            } else if (mSceneActionCache != null && !TextUtils.isEmpty(mSceneActionCache.getCommand())) {
//                //从设备切换到情景
//                mSelectedAction = Action.getAction(mSceneActionCache);
//            }
//        } else {
//            if (mSelectedAction != null) {
//                Action.setData(mSelectedAction, mDeviceActionCache);
//                mSelectedDeviceId = mDeviceActionCache.getBindedDeviceId();
//                mSelectedAction.setDeviceId(mSelectedDeviceId);
//                if (TextUtils.isEmpty(mSelectedDeviceId)) {
//                    mSelectedAction = null;
//                }
//            }
//            if (mSelectedAction == null) {
//                if (mDeviceActionCache != null && !TextUtils.isEmpty(mDeviceActionCache.getCommand())) {
//                    mSelectedAction = Action.getAction(mDeviceActionCache);
//                    mSelectedDeviceId = mDeviceActionCache.getBindedDeviceId();
//                }
//            }
//            //判断设备动作是否没有设置
//            boolean isCommandNotSet = mSelectedAction == null || TextUtils.isEmpty(mSelectedAction.getCommand());
//            selectDeviceAction(isChecked, isCommandNotSet);
//        }
//        //修复取消选中字体颜色还是黑色问题
//        //setActionView(true, isScene);
//
////            if (isScene) {
////                mDeviceAction_cb.setChecked(false);
////            } else {
////                mSceneAction_cb.setChecked(false);
////            }
////            mSceneAction_tv.setClickable(isScene);
////            device_tv.setClickable(!isScene);
////            action_tv.setClickable(!isScene);
//    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == action_ll) {
            //选择设备动作，没有选择设备时不能选择动作
            if (!StringUtil.isEmpty(mSelectedDeviceId)) {
                Device device = mDeviceDao.selDevice
                        (currentMainUid, mSelectedDeviceId);
                ActivityTool.toSelectActionActivity(this,
                        BaseSelectDeviceActionsActivity.SET_ACTION, BindActionType.REMOTE, device, mSelectedAction);
            } else {
                new ConfirmAndCancelPopup() {
                    @Override
                    public void confirm() {
                        super.confirm();
                        dismiss();
                    }
                }.showPopup(mContext, R.string.device_bind_action_not_select_device, R.string.ok, Constant.INVALID_NUM);
            }
        } else if (v == device_tv) {
            //选择设备
            Device device = null;
            boolean isScene = false;
            if (mSelectedAction != null && !StringUtil.isEmpty(mSelectedAction.getCommand())) {
                isScene = mSelectedAction.getCommand().equals(DeviceOrder.SCENE_CONTROL);
            }
            if (!StringUtil.isEmpty(mSelectedDeviceId) && !isScene) {
                device = mDeviceDao.selDevice
                        (currentMainUid, mSelectedDeviceId);
            }
            Intent intent = new Intent(mContext, SelectSingleDeviceActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            startActivityForResult(intent, BaseSelectDeviceActionsActivity.SELECT_DEVICE);
        } else if (v == mSceneAction_tv) {
            //选择情景
            Intent intent = new Intent(mContext, SelectSceneActivity.class);
            intent.putExtra(IntentKey.SCENE, mScene);
//            intent.putExtra(IntentKey.SCENE_ID, mRemoteBind == null ? Constant.INVALID_NUM : mRemoteBind.getValue1());
            startActivityForResult(intent, RequestCode.SELECT_SCENE);
        } else if (v == btnSave) {
            //保存
            save();
        } else if (v == tv_sceneAction) {
            //选择情景动作
            String select = (String) v.getTag(R.id.tag_select);
            boolean lastSelect = SELECT_YES.equals(select);//点击前的状态
            v.setTag(R.id.tag_select, lastSelect ? SELECT_NO : SELECT_YES);//设置点击后的状态
            boolean isChecked = !lastSelect;//当前状态 true选中
            selectSceneAction(isChecked);
            mSelectedDeviceId = "";//绑定情景时bindDeviceId需要设置为空值
            if (mSelectedAction != null) {
                if (mSceneActionCache != null) {
                    Action.setData(mSelectedAction, mSceneActionCache);
                }
            } else if (mSceneActionCache != null && !TextUtils.isEmpty(mSceneActionCache.getCommand())) {
                //从设备切换到情景
                mSelectedAction = Action.getAction(mSceneActionCache);
            }
        } else if (v == tv_deviceAction) {
            //选择设备动作
            String select = (String) v.getTag(R.id.tag_select);
            boolean lastSelect = SELECT_YES.equals(select);//点击前的状态
            v.setTag(R.id.tag_select, lastSelect ? SELECT_NO : SELECT_YES);//设置点击后的状态
            boolean isChecked = !lastSelect;//当前状态 true选中

            if (mSelectedAction != null) {
                if (mDeviceActionCache != null) {
                    Action.setData(mSelectedAction, mDeviceActionCache);
                    mSelectedDeviceId = mDeviceActionCache.getBindedDeviceId();
                }
                mSelectedAction.setDeviceId(mSelectedDeviceId);
                if (TextUtils.isEmpty(mSelectedDeviceId)) {
                    mSelectedAction = null;
                }
            }
            if (mSelectedAction == null) {
                if (mDeviceActionCache != null && !TextUtils.isEmpty(mDeviceActionCache.getCommand())) {
                    mSelectedAction = Action.getAction(mDeviceActionCache);
                    mSelectedDeviceId = mDeviceActionCache.getBindedDeviceId();
                }
            }
            //判断设备动作是否没有设置
            boolean isCommandNotSet = mSelectedAction == null || TextUtils.isEmpty(mSelectedAction.getCommand());
            selectDeviceAction(isChecked, isCommandNotSet);
        }
    }

    /**
     * 判断是否选中情景
     *
     * @return
     */
    private boolean isSceneSelected() {
        String select = (String) tv_sceneAction.getTag(R.id.tag_select);
        return SELECT_YES.equals(select);
    }

    /**
     * 判断是否选中设备
     *
     * @return
     */
    private boolean isDeviceSelected() {
        String select = (String) tv_deviceAction.getTag(R.id.tag_select);
        return SELECT_YES.equals(select);
    }

    private void save() {
        //遥控器绑定要长按遥控器设置键使遥控器进入唤醒模式
        if (mRemoteDevice.getDeviceType() == DeviceType.REMOTE) {
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(currentMainUid, mRemoteDevice.getDeviceId());
            if (deviceStatus != null && deviceStatus.getOnline() == OnlineStatus.OFFLINE) {
                showRemoteBindErrorPopup(mRemoteDevice);
                return;
            }
        }
        if (isSceneSelected()) {
            if (mScene == null || mSelectedAction == null) {
                if (isAddBind) {
                    ToastUtil.showToast(R.string.device_set_bind_type_scene_hint);
                    return;
                } else {
                    deleteBind(R.string.device_bind_scene_not_select);
                }
            } else if (mScene.getSceneId() == mOldSceneId) {
                leftTitleClick(null);
            } else {
                //save scene action
                bind();
            }
        } else if (isDeviceSelected()) {
            if (mSelectedAction == null || TextUtils.isEmpty(mSelectedAction.getCommand())) {
                if (isAddBind) {
                    ToastUtil.showToast(R.string.device_set_bind_type_device_hint);
                    return;
                } else if (StringUtil.isEmpty(mSelectedDeviceId)) {
                    deleteBind(R.string.device_bind_device_action_not_select);
                } else {
                    deleteBind(R.string.device_bind_action_not_select);
                }
            } else {
                if (TextUtils.isEmpty(mSelectedDeviceId)) {
                    //没有选择设备,toast提示
                    ToastUtil.showToast(R.string.device_bind_device_action_not_select);
                } else {
                    bind();
                }
            }
        } else if (!isSceneSelected() && !isDeviceSelected()) {
            if (isAddBind) {
                leftTitleClick(null);
            } else {
                deleteBind(R.string.device_bind_scene_device_action_not_select);
            }
        }
    }

    private void deleteBind(int content) {
        new ConfirmAndCancelPopup() {
            @Override
            public void confirm() {
                super.confirm();
                dismiss();
                showDialog();
                new DeleteRemoteBindAction(mAppContext) {
                    @Override
                    public void onDeleteBindAction(String uid, int result, List<String> bindIds, List<BindFail> failBindList) {
                        dismissDialog();
                        if (result != ErrorCode.SUCCESS) {
                            ToastUtil.toastError(result);
                        } else {
                            if (bindIds != null && !bindIds.isEmpty()) {
                                Intent intent = new Intent();
                                intent.putExtra(IntentKey.DELETE_BIND_ID, bindIds.get(0));
                                setResult(RESULT_OK, intent);
                                leftTitleClick(null);
                                return;
                            } else if (failBindList != null && !failBindList.isEmpty()) {
                                BindFail fail = failBindList.get(0);
                                ToastUtil.toastError(fail.getResult());
                            }
                        }
                    }
                }.delete(currentMainUid, mRemoteBind.getRemoteBindId());
            }
        }.showPopup(mContext, content, R.string.save, R.string.cancel);
    }

    private void bind() {
        RemoteBind addBind = null;
        RemoteBind modifyBind = null;
        if (isAddBind) {
            addBind = new RemoteBind();
            Action.setData(addBind, mSelectedAction);
            addBind.setUid(mRemoteDevice.getUid());
            addBind.setDeviceId(mRemoteDevice.getDeviceId());
            addBind.setDelayTime(0);
            addBind.setItemId(getItemId());
            addBind.setKeyNo(mKeyNo);
            addBind.setKeyAction(mKeyAction);
            // addBind.setDeviceId(mRemoteDevice.getDeviceId());
            if (DeviceOrder.SCENE_CONTROL.equals(mSelectedAction.getCommand())) {
                addBind.setBindedDeviceId("");
            } else {
                addBind.setBindedDeviceId(mSelectedDeviceId);
            }
        } else {
            //与旧动作对比
            //   RemoteBind remoteBind = null;
            RemoteBind remoteBind = new RemoteBindDao().selRemoteBindByRemoteBindId(mRemoteBind.getRemoteBindId());
//            if (DeviceOrder.SCENE_CONTROL.equals(mSelectedAction.getCommand())) {
//                remoteBind = new RemoteBindDao().selRemoteBindScene(currentMainUid, mRemoteBind.getDeviceId(), mKeyNo, mKeyAction, mRemoteBind.getValue1());
//            } else {
//                remoteBind = new RemoteBindDao().selRemoteBind(currentMainUid, mRemoteBind.getDeviceId(), mKeyNo, mKeyAction);
//            }
            LogUtil.d(TAG, "bind()-remoteBind:" + remoteBind + ",mSelectedAction:" + mSelectedAction + ",mKeyNo:" + mKeyNo + ",mKeyAction:" + mKeyAction + ",mRemoteBind:" + mRemoteBind);
            Action action1 = null;
            if (remoteBind != null) {
                action1 = RemoteBind.getAction(remoteBind);
                //不能使用deviceId，因为deviceId表示情景面板或者智能遥控器的deviceId
                action1.setDeviceId(remoteBind.getBindedDeviceId());
                action1.setUid(remoteBind.getUid());
            }

            if (Action.isActionEqual(action1, mSelectedAction)) {
                //没有修改动作
                leftTitleClick(null);
                return;
            } else {
                Action.setData(remoteBind, mSelectedAction);
//                remoteBind.setBindedDeviceId("");//绑定情景，bindDeviceId为空
                remoteBind.setBindedDeviceId(mSelectedDeviceId);
                remoteBind.setDeviceId(mRemoteDevice.getDeviceId());
                modifyBind = remoteBind;
            }
        }
        mAddSuccessBindId = null;
        isSuccess = false;
        showDialog();
        if (mRemoteBindAction == null) {
            mRemoteBindAction = new RemoteBindAction(mAppContext) {
                @Override
                public void onAddBind(String uid, int result, List<RemoteBind> successBindList, List<BindFail> failBindList) {
                    LogUtil.i(TAG, "onAddBind()-" + uid + ",result:" + result + ",successBindList:" + successBindList + ",failBindList:" + failBindList);
                    if (result != ErrorCode.SUCCESS) {
                        ToastUtil.toastError(result);
                    } else {
                        if (failBindList != null && !failBindList.isEmpty()) {
                            if (mRemoteDevice.getAppDeviceId() == AppDeviceId.REMOTE || mRemoteDevice.getDeviceType() == DeviceType.REMOTE) {
                                DeviceStatus status = new DeviceStatusDao().selDeviceStatus(uid, mRemoteDevice.getDeviceId());
                                if (status != null) {
                                    if (status.getOnline() == OnlineStatus.OFFLINE) {
//                                        DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                                        dialogFragmentOneButton.setTitle(getString(R.string.remote_sleep));
//                                        dialogFragmentOneButton.show(getFragmentManager(), "");
                                        showRemoteBindErrorPopup(mRemoteDevice);
                                    } else {
                                        BindFail bindFail = failBindList.get(0);
                                        ToastUtil.toastError(bindFail.getResult());
                                    }
                                } else {
                                    BindFail bindFail = failBindList.get(0);
                                    ToastUtil.toastError(bindFail.getResult());
                                }
                            } else {
                                BindFail bindFail = failBindList.get(0);
                                ToastUtil.toastError(bindFail.getResult());
                            }
                        } else {
                            if (successBindList != null && !successBindList.isEmpty()) {
                                RemoteBind remoteBind = successBindList.get(0);
                                if (isAddBind) {
                                    mAddSuccessBindId = remoteBind.getRemoteBindId();
                                } else {

                                }
                                isSuccess = true;
                                ToastUtil.showToast(R.string.binging_success);
                            }
                        }
                    }
                }

                @Override
                public void onModifyBind(String uid, int result, List<RemoteBind> successBindList, List<BindFail> failBindList) {
                    LogUtil.i(TAG, "onModifyBind()-" + uid + ",result:" + result + ",successBindList:" + successBindList + ",failBindList:" + failBindList);
                    if (result != ErrorCode.SUCCESS) {
                        ToastUtil.toastError(result);
                    } else {
                        if (failBindList != null && !failBindList.isEmpty()) {
                            if (mRemoteDevice.getAppDeviceId() == AppDeviceId.REMOTE || mRemoteDevice.getDeviceType() == DeviceType.REMOTE) {
//                                DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
//                                if (!StringUtil.isEmpty(mRemoteDevice.getModel())
//                                        && (mRemoteDevice.getModel().equals(ModelID.JIYUE_SUIYITIE) || mRemoteDevice.getModel().equals(ModelID.NEUTRAL_SUIYITIE))) {
//                                    dialogFragmentOneButton.setTitle(getString(R.string.suiyitie_sleep));
//                                } else {
//                                    dialogFragmentOneButton.setTitle(getString(R.string.remote_sleep));
//                                }
//                                dialogFragmentOneButton.show(getFragmentManager(), "");
                                showRemoteBindErrorPopup(mRemoteDevice);
                            } else {
                                BindFail bindFail = failBindList.get(0);
                                ToastUtil.toastError(bindFail.getResult());
                            }
                        } else if (successBindList != null && !successBindList.isEmpty()) {
                            isSuccess = true;
                            ToastUtil.showToast(R.string.binging_success);
                        }
                    }
                }

                @Override
                protected void onFinish() {
                    super.onFinish();
                    stop();
                    LogUtil.i(TAG, "onLinkageFinish()");
                    dismissDialog();
                    if (isSuccess) {
                        if (isAddBind) {
                            Intent intent = new Intent();
                            intent.putExtra(IntentKey.ADD_BIND_ID, mAddSuccessBindId);
                            setResult(RESULT_OK, intent);
                        }
                        leftTitleClick(null);
                    }
                }
            };
        }
        mRemoteBindAction.bind(currentMainUid, addBind, modifyBind);
    }

    private int getItemId() {
        synchronized (this) {
            mItemId += 1;
        }
        return mItemId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()-requestCode:" + requestCode + ",resultCode:" + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == BaseSelectDeviceActionsActivity.SELECT_DEVICE) {
                //选择了设备，可能没有
                if (data.hasExtra(IntentKey.DEVICE)) {
                    Device device = (Device) data.getSerializableExtra(IntentKey.DEVICE);
                    LogUtil.d(TAG, "onActivityResult()-device:" + device);
                    if (device == null) {
                        mSelectedDeviceId = null;
                        mSelectedAction = null;
                        mDeviceActionCache = null;
                        setDevice(null, mSelectedDeviceId);
                        setAction(null);
                        //设置动作未灰色不可点击
                        selectDeviceAction(true, true);
                    } else {
                        String deviceId = device.getDeviceId();
                        if (!deviceId.equals(mSelectedDeviceId)) {
                            mSelectedDeviceId = device.getDeviceId();
                            DeviceStatus deviceStatus = new DeviceStatusDao().selDeviceStatus(mSelectedDeviceId);
                            mSelectedAction = BindTool.getDefaultAction(device, deviceStatus, BindActionType.REMOTE);
//                        mSelectedAction = BindTool.getDefaultAction(deviceId, device.getDeviceType());
                            setDevice(device.getUid(), mSelectedDeviceId);
                            setAction(mSelectedAction);
//                            if (mDeviceActionCache == null) {
                            mDeviceActionCache = new RemoteBind();
                            if (mRemoteBind != null) {
                                mDeviceActionCache.setItemId(mRemoteBind.getItemId());
                                mDeviceActionCache.setKeyAction(mRemoteBind.getKeyAction());
                                mDeviceActionCache.setKeyNo(mRemoteBind.getKeyNo());
                                mDeviceActionCache.setUid(mRemoteBind.getUid());
                                mDeviceActionCache.setAlarmType(mRemoteBind.getAlarmType());
                                mDeviceActionCache.setKeyName(mRemoteBind.getKeyName());
                                mDeviceActionCache.setDelayTime(mRemoteBind.getDelayTime());
                            } else {
                                if (mRemoteDevice != null) {
                                    mDeviceActionCache.setDeviceId(mRemoteDevice.getDeviceId());
                                }
                                mDeviceActionCache.setKeyAction(mKeyAction);
                                mDeviceActionCache.setKeyNo(mKeyNo);
                            }
//                            }
                            if (mSelectedAction != null) {
                                Action.setData(mDeviceActionCache, mSelectedAction);
                            }
                            mDeviceActionCache.setBindedDeviceId(mSelectedDeviceId);
                        }
                    }
                } else {
                    mSelectedDeviceId = null;
                    mSelectedAction = null;
                    mDeviceActionCache = null;
                    setDevice(null, mSelectedDeviceId);
                    setAction(null);
                    //设置动作未灰色不可点击
                    selectDeviceAction(true, true);
                }
            } else if (requestCode == BaseSelectDeviceActionsActivity.SET_ACTION) {
                //选择了动作
                if (data.hasExtra(IntentKey.ACTION)) {
                    mSelectedAction = (Action) data.getSerializableExtra(IntentKey.ACTION);
                    LogUtil.d(TAG, "onActivityResult()-mSelectedAction:" + mSelectedAction);
                    setAction(mSelectedAction);
                    if (mSelectedAction != null && mSelectedAction.getDeviceId() == null) {
                        mSelectedAction.setDeviceId(mSelectedDeviceId);
                    }
                    Action.setData(mDeviceActionCache, mSelectedAction);
                }
            } else if (requestCode == RequestCode.SELECT_SCENE) {
                //选择了scene
                if (data.hasExtra(IntentKey.SCENE)) {
                    mScene = (Scene) data.getSerializableExtra(IntentKey.SCENE);
                    LogUtil.d(TAG, "onActivityResult()-mScene:" + mScene);
                    if (mScene != null) {
                        mSceneAction_tv.setTextColor(getResources().getColor(R.color.font_black));
                        mSceneAction_tv.setText(mScene.getSceneName());
                        if (mSceneActionCache == null) {
                            mSceneActionCache = new RemoteBind();
                        }
                        mSceneActionCache.setBindedDeviceId("");
                        mSceneActionCache.setCommand(DeviceOrder.SCENE_CONTROL);
                        mSceneActionCache.setValue1(mScene.getSceneId());
                        mSceneActionCache.setDeviceId(mRemoteDevice.getDeviceId());

                        if (mSelectedAction == null
                                || !DeviceOrder.SCENE_CONTROL.equals(mSelectedAction.getCommand())
                                || mSelectedAction.getValue1() != mScene.getSceneId()) {
                            if (mSelectedAction == null) {
                                mSelectedAction = new Action(mRemoteDevice.getDeviceId(), DeviceOrder.SCENE_CONTROL, mScene.getSceneId(), 0, 0, 0, null, 0);
                            } else {
                                mSelectedAction.setCommand(DeviceOrder.SCENE_CONTROL);
                                mSelectedAction.setValue1(mScene.getSceneId());
                                // mSelectedAction.setDeviceId(0);
                            }
                        }
                    } else {
                        mSceneAction_tv.setTextColor(getResources().getColor(R.color.font_white_gray));
                        mSceneAction_tv.setText(R.string.device_set_bind_type_scene_hint);
                        mSelectedAction = null;
                        mSceneActionCache = null;
                    }
                } else {
                    mSceneAction_tv.setTextColor(getResources().getColor(R.color.font_white_gray));
                    mSceneAction_tv.setText(R.string.device_set_bind_type_scene_hint);
                    mSelectedAction = null;
                    mSceneActionCache = null;
                }
            }
            LogUtil.i(TAG, "onActivityResult()-mSelectedAction:" + mSelectedAction);
        }
    }

    private void showRemoteBindErrorPopup(Device device) {
        if (!StringUtil.isEmpty(device.getModel()) && ProductManage.isStickers(device.getModel())) {
            toastPopup.showPopupWithImage(mContext, getResources().getString(R.string.warm_tips), null, getResources().getString(R.string.know), null);
        } else {
            toastPopup.showPopup(mContext, getResources().getString(R.string.device_set_remote_bind_tips), getResources().getString(R.string.know), null);
        }
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    /**
     * 设置房间是否显示
     *
     * @return
     */
    private boolean isShowRoom() {
        //查询主机中设备个数，如果小于10个设备就不显示楼层房间
        int deviceCount = new DeviceDao().selVicenterDevicesCount(currentMainUid);
        int floorCount = new FloorDao().selFloorNo(currentMainUid);
        if (ProductManage.getInstance().isWifiDevice(mRemoteDevice))
            return false;
        if (floorCount <= 0) {
            if (deviceCount <= 10) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (toastPopup != null && toastPopup.isShowing()) {
            toastPopup.dismiss();
        }
        if (mRemoteBindAction != null) {
            mRemoteBindAction.stop();
        }
        super.onDestroy();
    }
}
