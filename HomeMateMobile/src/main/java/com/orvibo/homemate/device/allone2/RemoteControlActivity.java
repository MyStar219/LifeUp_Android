package com.orvibo.homemate.device.allone2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.IrApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.device.allone2.add.DeviceBrandListActivity;
import com.orvibo.homemate.device.allone2.epg.ProgramGuidesActivity;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.device.timing.TimingCountdownActivity;
import com.orvibo.homemate.event.AlloneViewEvent;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.AlloneACUtil;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.VibratorUtil;
import com.orvibo.homemate.view.custom.Allone2TipsDialog;
import com.orvibo.homemate.view.custom.AlloneNotFitTipsDialog;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.RemoteControlBottomView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by yuwei on 2016/3/28.
 * update by snown 遥控匹配控制类
 */
public class RemoteControlActivity extends BaseAlloneControlActivity implements RemoteControlBottomView.RemoteControlBottomBtnClickListener
        , Allone2TipsDialog.Allone2DialogCliclListener, OnKeyClickListener, BaseResultListener {
    private NavigationGreenBar nbTitle;
    private RelativeLayout rl_remote_control_tips_layout;
    private TextView tv_remote_control_tips;
    private ImageView iv_remote_control_tips_close;

    private RemoteControlBottomView remote_control_bottom_view;

    private List<IrData> irDatas;


    private int btnClickCount = 0;

    private int clickFid = -1;//点击3次不同按钮才弹框问是否匹配

    //private BrandList.Brand acBrand;

    private Allone2TipsDialog allone2TipsDialog;
    private boolean isTipsDialogAlreadyShow = false;
    private int deviceType;

    protected boolean isHomeClick = false;
    protected boolean isChangeRemote = false;
    protected boolean isAction = false;//定时倒计时
    private AlloneControlData data;
    private Fragment fragment;
    private Fragment acFragment;//空调另外一种fragment
    FragmentManager fm = getSupportFragmentManager();
    private OnRefreshListener onRefreshListener;
    private List<Integer> remoteids;
    private Action action;
    private VibratorUtil vibratorUtil = new VibratorUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        irDatas = (List<IrData>) getIntent().getSerializableExtra(IntentKey.ALL_ONE_DATA);
        remoteids = getIntent().getIntegerArrayListExtra(IntentKey.ALL_ONE_REMOTE_IDS);
        saveData = (AlloneSaveData) getIntent().getSerializableExtra(IntentKey.ALL_ONE_SAVE_DATA);
        isHomeClick = getIntent().getBooleanExtra(IntentKey.IS_HOME_CLICK, false);
        isChangeRemote = getIntent().getBooleanExtra(IntentKey.IS_CHANGE_REMOTE, false);
        isAction = getIntent().getBooleanExtra(IntentKey.IS_ACTION, false);
        action = (Action) getIntent().getSerializableExtra(IntentKey.ACTION);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        nbTitle.showWhiteStyle();
        rl_remote_control_tips_layout = (RelativeLayout) findViewById(R.id.rl_remote_control_tips_layout);
        tv_remote_control_tips = (TextView) findViewById(R.id.tv_remote_control_tips);
        iv_remote_control_tips_close = (ImageView) findViewById(R.id.iv_remote_control_tips_close);
        remote_control_bottom_view = (RemoteControlBottomView) findViewById(R.id.remote_control_bottom_view);
        if (isHomeClick) {
            nbTitle.showImageBack();
            deviceType = device.getDeviceType();
            isTipsDialogAlreadyShow = true;
            rl_remote_control_tips_layout.setVisibility(View.GONE);
            nbTitle.setText(deviceName);
            remote_control_bottom_view.setVisibility(View.GONE);
            if (AlloneUtil.hasTiming(deviceType)) {
                nbTitle.setRightImageViewVisibility(View.VISIBLE);
                nbTitle.setRightImageViewRes(R.drawable.more_green_selector);
                nbTitle.setRightTextVisibility(View.GONE);
            } else {
                nbTitle.setRightImageViewRes(R.drawable.setting_green_selector);
                nbTitle.setRightImageViewVisibility(View.VISIBLE);
                nbTitle.setRightText("");
            }
            if (isAction) {
                nbTitle.setRightImageViewVisibility(View.GONE);
                nbTitle.setRightTextVisibility(View.GONE);
            }
        } else if (isChangeRemote) {
            deviceType = device.getDeviceType();
            nbTitle.setRightText(getString(R.string.use_tutorial));
        } else {
            deviceType = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.AC);
            nbTitle.setRightText(getString(R.string.use_tutorial));
            nbTitle.showTextBack();
            rl_remote_control_tips_layout.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        if (isHomeClick) {
            IrData irData = AlloneCache.getIrData(deviceId);
            if (irData == null) {
                loadIrData(device.getIrDeviceId());
            } else {
                updateView(irData);
            }
        } else if (isChangeRemote) {
            nbTitle.setText(deviceName);
            setFragmentAndBottomLayout();
            bottomTextViewClick();
        } else {
            nbTitle.setText(getDeviceName());
            tv_remote_control_tips.setText(getString(R.string.remote_control_tips));
            setFragmentAndBottomLayout();
        }
    }

    private void initListener() {
        iv_remote_control_tips_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_remote_control_tips_close://关闭温馨提示
                rl_remote_control_tips_layout.setVisibility(View.GONE);
                break;
            case R.id.shareTextView:
                popupWindow.dismiss();
                Intent intent = new Intent(this, TimingCountdownActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.settingsTextView:
                popupWindow.dismiss();
                Intent intent1 = new Intent(RemoteControlActivity.this, SetDeviceActivity.class);
                intent1.putExtra(IntentKey.DEVICE, device);
                startActivity(intent1);
                break;
        }
    }

    /**
     * 点击下一个获取一次数据
     */

    private void showNext() {
        final int index = remote_control_bottom_view.getIndex();
        if (index + 1 == irDatas.size() && index + 1 < remoteids.size()) {
            if (ClickUtil.isFastDoubleClick(1000))
                return;
            final int newIndex = index + 1;
            showDialog();
            KookongSDK.getIRDataById(AlloneDataUtil.getLoadIrData(remoteids, irDatas.size()), new IRequestResult<IrDataList>() {

                @Override
                public void onSuccess(String msg, IrDataList result) {
                    dismissDialog();
                    MyLogger.jLog().d("load and index=" + newIndex);
                    List<IrData> resultIrDataList = result.getIrDataList();
                    irDatas.addAll(resultIrDataList);
                    if (!remote_control_bottom_view.isShown())
                        remote_control_bottom_view.setVisibility(View.VISIBLE);
                    if (newIndex == remote_control_bottom_view.getIndex() + 1) {
                        remote_control_bottom_view.showNext();
                    }
                    updateACFragment(newIndex, false);
                    onRefreshListener.onRefresh(irDatas.get(newIndex));
                }

                @Override
                public void onFail(String msg) {
                    dismissDialog();
                    ToastUtil.showToast(R.string.allone_error_data_tip);
                }
            });
        } else if (index + 1 < irDatas.size()) {
            remote_control_bottom_view.showNext();
            updateACFragment(index + 1, false);
            onRefreshListener.onRefresh(irDatas.get(index + 1));
        } else if (index + 1 >= remoteids.size()) {
            allConditionerNotApply();
        }
    }

    /**
     * 初始化界面
     */
    private void setFragmentAndBottomLayout() {
        if (irDatas.size() > 0) {
            allone2TipsDialog = new Allone2TipsDialog(this, getString(R.string.the_remote_control_right, AlloneUtil.getDeviceName(deviceType)), this);
            allone2TipsDialog.setCancelable(false);
            initFragmentView(0);
        } else {
            //没有可配置的遥控器
            rl_remote_control_tips_layout.setVisibility(View.GONE);
        }
    }


    /**
     * 更新fragment
     *
     * @param index
     */
    private void initFragmentView(int index) {
        if (deviceType == DeviceType.AC) {
            if (1 == irDatas.get(index).type)
                acFragment = new ACSimpleControlFragment();
            else
                fragment = new ACComplexControlFragment();
        } else {
            fragment = AlloneUtil.getRemoteFragment(deviceType, fragment);
        }
        if (fragment != null) {
            fragment.setArguments(getFragmentBundle(index));
            fm.beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
        if (acFragment != null) {
            acFragment.setArguments(getFragmentBundle(index));
            fm.beginTransaction().add(R.id.fragment_container, acFragment).commitAllowingStateLoss();
        }
    }

    /**
     * 设置fragment bundle
     *
     * @param index
     * @return
     */
    private Bundle getFragmentBundle(int index) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.ALL_ONE_DATA, irDatas.get(index));
        device.setDeviceType(deviceType);
        bundle.putSerializable(IntentKey.DEVICE, device);
        bundle.putBoolean(IntentKey.IS_HOME_CLICK, isHomeClick);
        bundle.putBoolean(IntentKey.IS_ACTION, isAction);
        bundle.putSerializable(IntentKey.ACTION, action);
        if (deviceBrand != null) {
            bundle.putSerializable(DeviceBrandListActivity.BRAND_KEY, deviceBrand);
        }
        return bundle;
    }


    @Override
    public void leftTitleClick(View v) {
        showSaveDialog();
    }

    @Override
    public void rightTitleClick(View v) {
        if (isHomeClick) {
            if (AlloneUtil.hasTiming(deviceType)) {
                super.rightTitleClick(v);
            } else {
                Intent intent = new Intent(RemoteControlActivity.this, SetDeviceActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
            }
        } else {
            ActivityJumpUtil.jumpAct(RemoteControlActivity.this, AlloneUserGuideActivity.class);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null && isHomeClick) {
            nbTitle.setText(device.getDeviceName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /**
         * 释放对象和清空数据
         */
        if (irDatas != null && !irDatas.isEmpty()) {
            irDatas.clear();
            irDatas = null;
        }
        super.onDestroy();
    }

    @Override
    public void preView() {
        int index = remote_control_bottom_view.getIndex();
        updateACFragment(index, false);
        onRefreshListener.onRefresh(irDatas.get(index));
    }

    @Override
    public void middleButtonClick() {
        saveDevice();
    }

    @Override
    public void nextView() {
        showNext();
    }

    public void allConditionerNotApply() {
        AlloneNotFitTipsDialog dialog = new AlloneNotFitTipsDialog(this, device, deviceType);
        dialog.show();
    }

    /**
     * 更新空调的fragment
     *
     * @param index          空调数据下标
     * @param isSetHomeClick 是否传递首页点击参数
     */
    private void updateACFragment(int index, boolean isSetHomeClick) {
        if (deviceType == DeviceType.AC) {
            if (AlloneACUtil.AC_TYPE_SIMPLE == irDatas.get(index).type) {
                if (acFragment == null) {
                    acFragment = new ACSimpleControlFragment();
                    acFragment.setArguments(getFragmentBundle(index));
                } else {
                    ((ACSimpleControlFragment) (acFragment)).setIrData(irDatas.get(index));
                }
                if (!acFragment.isAdded()) {
                    fm.beginTransaction().hide(fragment).add(R.id.fragment_container, acFragment).commitAllowingStateLoss();
                } else if (acFragment.isHidden())
                    fm.beginTransaction().hide(fragment).show(acFragment).commitAllowingStateLoss();
            } else {
                if (fragment == null) {
                    fragment = new ACComplexControlFragment();
                    fragment.setArguments(getFragmentBundle(index));
                } else {
                    //切换遥控器时点保存，要把首页点击参数重新传递
                    if (isSetHomeClick) {
                        ((ACComplexControlFragment) (fragment)).setInitData(isHomeClick, device);
                    }
                    ((ACComplexControlFragment) (fragment)).setIrData(irDatas.get(index));
                }
                if (!fragment.isAdded()) {
                    fm.beginTransaction().hide(acFragment).add(R.id.fragment_container, fragment).commitAllowingStateLoss();
                } else if (fragment.isHidden())
                    fm.beginTransaction().hide(acFragment).show(fragment).commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void topTextViewClick() {
        saveDevice();
    }

    @Override
    public void bottomTextViewClick() {
        remote_control_bottom_view.setVisibility(View.VISIBLE);
        remote_control_bottom_view.initData(remoteids == null ? 0 : remoteids.size(), this);
        if (irDatas.size() == 1) {
            ToastUtil.showToast(R.string.no_more_remote_control);
        } else if (irDatas.size() > 1) {
            if (isChangeRemote) {
                remote_control_bottom_view.preViewClick();
            } else {
                updateACFragment(1, false);
                onRefreshListener.onRefresh(irDatas.get(1));
            }
        }
    }


    @Override
    public void OnClick(IrKeyButton irKeyButton) {
        if (isAction) {
            setAction(irKeyButton);
        } else if (irKeyButton.isMatched()) {
            AlloneControlData controlData = irKeyButton.getControlData();
            if (isHomeClick)
                DeviceControlApi.allOneControl(device.getUid(), deviceId, controlData.getFreq(), controlData.getPluseNum(), controlData.getPluseData(), false, this);
            else
                DeviceControlApi.allOneControl(device.getUid(), "1", controlData.getFreq(), controlData.getPluseNum(), controlData.getPluseData(), false, this);
            if (clickFid != irKeyButton.getFid()) {
                clickFid = irKeyButton.getFid();
                //判断按钮点击次数，在第三次的时候弹出对话框，询问用户，该遥控器是否可用
                btnClickCount = btnClickCount + 1;
            }
            if (btnClickCount == 3 && !isTipsDialogAlreadyShow && !isChangeRemote) {
                allone2TipsDialog.show();
                isTipsDialogAlreadyShow = true;
            }
        }
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        dismissDialog();
        //为快速点击时不处理
        if (baseEvent.getResult() == ErrorCode.CLICK_FAST) {
            return;
        }
        ClickUtil.clearTime();
        if (baseEvent.getResult() == ErrorCode.SUCCESS) {

        } else {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }

    @Override
    public void onBackPressed() {
        showSaveDialog();
    }

    /**
     * 保存设备
     */
    protected void saveDevice() {
        int index = 0;
        if (remote_control_bottom_view.isShown())
            index = remote_control_bottom_view.getIndex();
        if (saveData != null && irDatas != null && irDatas.size() > 0) {
            saveDevice(getDeviceName(), deviceType, irDatas.get(index), saveData.toString());
        }

    }

    /**
     * 获取遥控器名称
     *
     * @return
     */
    private String getDeviceName() {
        int count = new DeviceDao().getAlloneSonCount(deviceType, userName) + 1;
        String deviceName = StringUtil.strApendStr(AlloneUtil.getDeviceName(deviceType), getString(R.string.device_type_REMOTE_16).toLowerCase());
        if (count > 1)
            deviceName = deviceName + count;
        return deviceName;
    }


    /**
     * 返回按键弹框保存设备
     */
    private void showSaveDialog() {
        if (isAction) {
            if (action != null) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                intent.putExtra(IntentKey.DEVICE, device);
                bundle.putSerializable(IntentKey.ACTION, action);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
            }
            finish();
        } else if (!isHomeClick) {
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.dialog_remote_control_save_tip));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.save));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    finish();
                }

                @Override
                public void onRightButtonClick(View view) {
                    saveDevice();
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        } else {
            ActivityJumpUtil.jumpActFinish(RemoteControlActivity.this, MainActivity.class);
        }
    }

    /**
     * 保存设备
     *
     * @param deviceName1
     * @param deviceType
     * @param irData
     * @param brandId
     */
    public void saveDevice(String deviceName1, final int deviceType, final IrData irData, String brandId) {
        showDialog();
        if (isChangeRemote) {
            DeviceApi.modifyDevice(userName, uid, deviceId, nbTitle.getText(), deviceType, device.getRoomId(), String.valueOf(irData.rid), new BaseResultListener() {
                @Override
                public void onResultReturn(BaseEvent baseEvent) {
                    dismissDialog();
                    if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                        AlloneCache.saveIrData(irData, deviceId);
                        saveACState(irData);
                        EventBus.getDefault().post(new PartViewEvent(irData));
                        ToastUtil.showToast(getString(R.string.remote_control_change) + getString(R.string.SUCCESS));
                        finish();
                    } else {
                        ToastUtil.toastError(baseEvent.getResult());
                    }
                }
            });
        } else {
            DeviceApi.createDevice(userName, uid, deviceType, deviceName1, null, String.valueOf(irData.rid), deviceId, brandId, new BaseResultListener.DataListener() {
                @Override
                public void onResultReturn(BaseEvent baseEvent, Object data) {
                    Device device1 = (Device) data;
                    dismissDialog();
                    if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                        //重载界面逻辑
                        AlloneCache.saveIrData(irData, device1.getDeviceId());
                        isHomeClick = true;
                        device = device1;
                        deviceId = device1.getDeviceId();
                        deviceName = device1.getDeviceName();
                        ToastUtil.showToast(getString(R.string.allone_device_add_success));
                        //添加成功后发送结束添加activity事件
                        EventBus.getDefault().post(new ActivityFinishEvent());
                        if (device.getDeviceType() == DeviceType.STB) {
                            Intent intent = new Intent(RemoteControlActivity.this, ProgramGuidesActivity.class);
                            intent.putExtra(IntentKey.DEVICE, device);
                            startActivity(intent);
                            finish();
                        } else {
                            if (device.getDeviceType() == DeviceType.TV_BOX) {//如果是电视盒子，添加完后要更新电视开关按钮
                                ((BaseControlFragment) fragment).setDeviceCreatedData(true, device);
                            }
                            //创建成功遥控器后重新刷新下数据
                            irDatas.clear();
                            irDatas.add(0, AlloneCache.getIrData(deviceId));
                            initView();
                            updateACFragment(0, true);
                            onRefreshListener.onRefresh(irDatas.get(0));
                            saveACState(irDatas.get(0));
                        }
                    } else {
                        ToastUtil.toastError(baseEvent.getResult());
                    }
                }
            });
        }
    }

    /**
     * 机顶盒添加电视机按钮
     *
     * @param mDevice
     */
    @Override
    public void onTvSelected(final Device mDevice) {
        //如果是复制的遥控器，需要去查表
        if (ProductManage.isAlloneLearnDevice(mDevice)) {
            KKIr kkIr = KKIrDao.getInstance().getPower(mDevice.getIrDeviceId(), mDevice.getDeviceId());
            if (kkIr == null)
                ToastUtil.showToast(R.string.allone_error_tip1);
            else
                createIrKey(mDevice.getDeviceId(), Integer.parseInt(mDevice.getIrDeviceId()), kkIr.getFid());
        } else {
            IrData irData = AlloneCache.getIrData(mDevice.getDeviceId());
            if (irData == null) {
                showDialog();
                KookongSDK.getIRDataById(mDevice.getIrDeviceId(), new IRequestResult<IrDataList>() {

                    @Override
                    public void onSuccess(String msg, IrDataList result) {
                        dismissDialog();
                        List<IrData> irDatas = result.getIrDataList();
                        IrData irData = irDatas.get(0);
                        AlloneCache.saveIrData(irData, mDevice.getDeviceId());
                        IrData.IrKey irKey = AlloneDataUtil.getTvPower(irData, KKookongFid.fid_1_power);
                        if (irKey != null)
                            createIrKey(mDevice.getDeviceId(), Integer.parseInt(mDevice.getIrDeviceId()), irKey.fid);
                        else
                            ToastUtil.showToast(R.string.allone_error_tip1);
                    }

                    @Override
                    public void onFail(String msg) {
                        dismissDialog();
                    }
                });
            } else {
                IrData.IrKey irKey = AlloneDataUtil.getTvPower(irData, KKookongFid.fid_1_power);
                if (irKey != null)
                    createIrKey(mDevice.getDeviceId(), Integer.parseInt(mDevice.getIrDeviceId()), irKey.fid);
                else
                    ToastUtil.showToast(R.string.allone_error_tip1);
            }
        }
    }

    /**
     * 电视电源按钮控制
     *
     * @param kkIr
     */
    @Override
    public void onTvClick(final KKIr kkIr, final IrKeyButton irKeyButton) {
        if (kkIr == null && isAction) {
            setAction(irKeyButton);
        } else {
            final Device bindDevice = new DeviceDao().selDevice(kkIr.getBindDeviceId());
            if (bindDevice != null) {
                if (ProductManage.isAlloneLearnDevice(bindDevice)) {
                    KKIr controlIr = KKIrDao.getInstance().getPower(bindDevice.getDeviceId(), false);
                    if (controlIr != null) {
                        data = new AlloneControlData(controlIr.getFreq(), controlIr.getPluse());
                    }
                } else {
                    IrData irData = AlloneCache.getIrData(bindDevice.getDeviceId());
                    if (irData == null) {
                        showDialog();
                        KookongSDK.getIRDataById(bindDevice.getIrDeviceId(), new IRequestResult<IrDataList>() {

                            @Override
                            public void onSuccess(String msg, IrDataList result) {
                                dismissDialog();
                                List<IrData> irDatas = result.getIrDataList();
                                IrData irData = irDatas.get(0);
                                AlloneCache.saveIrData(irData, bindDevice.getDeviceId());
                                IrData.IrKey irKey = AlloneDataUtil.getTvPower(irData, kkIr.getFid());
                                if (irKey != null) {
                                    data = new AlloneControlData(irData.fre, irKey.pulse);
                                }
                                onTvClickControl(irKeyButton);
                            }

                            @Override
                            public void onFail(String msg) {
                                dismissDialog();
                                ToastUtil.showToast(R.string.allone_error_data_tip);
                            }
                        });
                    } else {
                        IrData.IrKey irKey = AlloneDataUtil.getTvPower(irData, kkIr.getFid());
                        if (irKey != null) {
                            data = new AlloneControlData(irData.fre, irKey.pulse);
                        }
                    }
                }
            }
            onTvClickControl(irKeyButton);
        }
    }

    /**
     * 点击电源绑定按钮时的action
     */
    private void onTvClickControl(IrKeyButton irKeyButton) {
        if (data != null) {
            if (isAction) {
                irKeyButton.setControlData(data);
                setAction(irKeyButton);
            } else {
                DeviceControlApi.allOneControl(device.getUid(), deviceId, data.getFreq(), data.getPluseNum(), data.getPluseData(), false, RemoteControlActivity.this);
            }
        }
    }

    /**
     * 添加机顶盒电源键绑定
     *
     * @param bindDeviceId
     * @param rid
     * @param fid
     */
    private void createIrKey(String bindDeviceId, int rid, int fid) {
        showDialog();
        IrApi.createIrKey(userName, uid, deviceId, null, 1, bindDeviceId, rid, fid, new BaseResultListener.DataListListener() {

            @Override
            public void onResultReturn(BaseEvent baseEvent, Object[] datas) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    onRefreshListener.onRefresh(irDatas.get(0));
                } else {
                    ToastUtil.toastError(baseEvent.getResult());
                }
            }
        });
    }


    @Override
    public void leftToRightImgOnClick(View v) {
        super.leftToRightImgOnClick(v);
    }


    /**
     * 根据rid获取对应的红外码
     *
     * @param rid
     */
    public void loadIrData(String rid) {
        showDialog();
        KookongSDK.getIRDataById(rid, new IRequestResult<IrDataList>() {

            @Override
            public void onSuccess(String msg, IrDataList result) {
                dismissDialog();
                List<IrData> irDatas = result.getIrDataList();
                IrData irData = irDatas.get(0);
                AlloneCache.saveIrData(irData, deviceId);
                updateView(irData);
                saveACState(irData);
            }

            @Override
            public void onFail(String msg) {
                dismissDialog();
                ToastUtil.showToast(R.string.allone_error_data_tip);
            }
        });
    }

    /**
     * 重新获取红外码或本地读数据成功后刷新界面
     *
     * @param irData
     */
    private void updateView(IrData irData) {
        irDatas = new ArrayList<>();
        irDatas.add(irData);
        setFragmentAndBottomLayout();
    }

    /**
     * 当解除电源绑定时或更换遥控器时，界面刷新
     *
     * @param event
     */
    public void onEventMainThread(PartViewEvent event) {
        //不是更换遥控器界面的话不处理数据
        if (!isFinishingOrDestroyed() && !isChangeRemote) {
            if (event.getIrData() != null) {
                irDatas.clear();
                irDatas.add(0, event.getIrData());
                updateACFragment(0, false);
            }
            onRefreshListener.onRefresh(irDatas.get(0));
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            onRefreshListener = (OnRefreshListener) fragment;
        } catch (Exception e) {
            throw new ClassCastException(this.toString() + " must implement onRefreshListener");
        }
        super.onAttachFragment(fragment);
    }

    /**
     * 按键处理对应的action封装
     *
     * @param irKeyButton
     */
    public void setAction(IrKeyButton irKeyButton) {
        vibratorUtil.setVirbrator(mContext);
        if (irKeyButton.isMatched()) {
            if (action == null)
                action = new Action();
            int fid = irKeyButton.getFid();
            AlloneControlData controlData = irKeyButton.getControlData();
            String name = irKeyButton.getText().toString();
            if (TextUtils.isEmpty(name))
                name = AlloneUtil.getNameByFid(fid);
            action.setValue2(fid);
            //空调类型为组合码时，内容去拼接
            action.setName(deviceType == DeviceType.AC && irDatas.get(0).type != 1 ? AlloneACUtil.getAcActionName(action.getValue2()) : name);
            action.setValue1(Integer.parseInt(device.getIrDeviceId()));
            action.setFreq(controlData.getFreq());
            action.setPluseNum(controlData.getPluseNum());
            action.setPluseData(controlData.getPluseData());
            action.setCommand(DeviceOrder.IR_CONTROL);
            onRefreshListener.onRefresh(action);
        } else if (deviceType != DeviceType.AC) {
            ToastUtil.showToast(R.string.allone_ation_no_learn_tip);
        }
    }

    /**
     * 更换空调遥控器后要更新空调存储的状态
     */
    private void saveACState(IrData irData) {
        if (deviceType == DeviceType.AC) {
            if (irData.type != AlloneACUtil.AC_TYPE_SIMPLE) {
                if (isChangeRemote) {
                    AlloneCache.saveAcState(deviceId);
                }
                AlloneCache.setSingleAc(deviceId, false);
            } else
                AlloneCache.setSingleAc(deviceId, true);
            EventBus.getDefault().post(new AlloneViewEvent());
        }
    }
}
