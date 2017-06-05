package com.orvibo.homemate.device.allone2.irlearn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKDevice;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.dao.KKDeviceDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.listener.OnIrKeyLongClickListener;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.device.timing.TimingCountdownActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.VibratorUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.custom.LinearTipView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * Created by snow on 2016/4/11.
 * allone红外学习基类
 */
public class RemoteLearnActivity extends BaseControlActivity implements OnKeyClickListener, BaseResultListener, OnIrKeyLongClickListener {
    private NavigationGreenBar nbTitle;

    private KKDevice kkDevice;

    protected boolean isStartLearn = false;//学习遥控器
    protected boolean isChangeRemote = false;//设置处修改遥控器
    protected boolean isAction = false;//定时倒计时
    protected boolean is = false;
    private BaseLearnFragment fragment;
    private OnIrLearnRefreshListener onRefreshListener;
    protected LinearTipView tipView;//学习tip
    private Action action;
    private VibratorUtil vibratorUtil = new VibratorUtil();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_learn);
        isStartLearn = getIntent().getBooleanExtra(IntentKey.IS_START_LEARN, false);
        isChangeRemote = getIntent().getBooleanExtra(IntentKey.IS_CHANGE_REMOTE, false);
        isAction = getIntent().getBooleanExtra(IntentKey.IS_ACTION, false);
        action = (Action) getIntent().getSerializableExtra(IntentKey.ACTION);
        initView();
        initData();
    }

    private void initView() {
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        tipView = (LinearTipView) findViewById(R.id.tipView);
        nbTitle.setText(deviceName);
        nbTitle.showWhiteStyle();
        if (isAction) {
            nbTitle.setRightImageViewVisibility(View.GONE);
            nbTitle.setRightTextVisibility(View.GONE);
            tipView.setVisibility(View.GONE);
        } else if (isStartLearn) {
            nbTitle.setRightText(getString(R.string.finish));
            tipView.setVisibility(View.VISIBLE);
        } else {
            if (device.getDeviceType() == DeviceType.SELF_DEFINE_IR) {
                nbTitle.setRightImageViewVisibility(View.VISIBLE);
                nbTitle.setRightImageViewRes(R.drawable.more_green_selector);
                nbTitle.setRightTextVisibility(View.GONE);
            } else {
                nbTitle.setRightImageViewVisibility(View.VISIBLE);
                nbTitle.setRightImageViewRes(R.drawable.setting_green_selector);
                nbTitle.setRightText("");
                tipView.setVisibility(View.GONE);
            }
        }
        if (device.getDeviceType() == DeviceType.SELF_DEFINE_IR) {
            tipView.setVisibility(View.GONE);
        }

    }

    private void initData() {
        if (!TextUtils.isEmpty(device.getIrDeviceId()))
            kkDevice = KKDeviceDao.getInstance().getIrData(device.getIrDeviceId(), deviceId);
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.ALL_ONE_DATA, kkDevice);
        bundle.putSerializable(IntentKey.DEVICE, device);
        bundle.putBoolean(IntentKey.IS_START_LEARN, isStartLearn);
        bundle.putBoolean(IntentKey.IS_ACTION, isAction);
        bundle.putSerializable(IntentKey.ACTION, action);
        switch (device.getDeviceType()) {
            case DeviceType.TV:
                fragment = new TVLearnFragment();
                break;
            case DeviceType.STB:
                fragment = new STBLearnFragment();
                break;
            case DeviceType.SELF_DEFINE_IR:
                fragment = new CustomRemoteFragment();
                break;
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }


    /**
     * 根据数据刷新界面
     */
    private void refreshView() {
        if (!TextUtils.isEmpty(device.getIrDeviceId()))
            kkDevice = KKDeviceDao.getInstance().getIrData(device.getIrDeviceId(), deviceId);
        onRefreshListener.onRefresh(kkDevice, isStartLearn);
    }

    @Override
    public void leftTitleClick(View v) {
        showSaveDialog();
    }

    @Override
    public void rightTitleClick(View v) {
        if (isStartLearn && !isChangeRemote) {
            isStartLearn = false;
            initView();
            refreshView();
        } else if (isChangeRemote) {
            finish();
        } else {
            if (device.getDeviceType() == DeviceType.SELF_DEFINE_IR) {
                super.rightTitleClick(v);
            } else {
                Intent intent = new Intent(RemoteLearnActivity.this, SetDeviceActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            nbTitle.setText(device.getDeviceName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareTextView:
                popupWindow.dismiss();
                Intent intent = new Intent(this, TimingCountdownActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.settingsTextView:
                popupWindow.dismiss();
                Intent intent1 = new Intent(this, SetDeviceActivity.class);
                intent1.putExtra(IntentKey.DEVICE, device);
                startActivity(intent1);
                break;
        }
    }

    @Override
    public void OnClick(IrKeyButton irKeyButton) {
        KKIr kkIr = irKeyButton.getKkIr();
        if (isAction) {
            setAction(irKeyButton);
        } else if (isStartLearn) {
            kkIr.setfKey(irKeyButton.getText().toString());
            kkIr.setfName(irKeyButton.getText().toString());
            Intent intent = new Intent(RemoteLearnActivity.this, IrLearnActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.IS_START_LEARN, irKeyButton.isLearned());
            intent.putExtra(IntentKey.ALL_ONE_IR_KEY, irKeyButton.getKkIr());
            startActivity(intent);
        } else if (irKeyButton.isLearned()) {
            int pulseNum = kkIr.getPluse().split(",").length;
            DeviceControlApi.allOneControl(device.getUid(), deviceId, kkIr.getFreq(), pulseNum, kkIr.getPluse(), false, this);
        }
    }

    @Override
    public void onTvSelected(Device device) {

    }

    @Override
    public void onTvClick(KKIr data,IrKeyButton irKeyButton) {

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
        } else if (isStartLearn) {
            final DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.allone_learn_tip2));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.yes));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.no));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    dialogFragmentTwoButton.dismiss();
                }

                @Override
                public void onRightButtonClick(View view) {
                    if (isChangeRemote)
                        finish();
                    else {
                        isStartLearn = false;
                        initView();
                        refreshView();
                    }
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
        } else {
            ActivityJumpUtil.jumpActFinish(RemoteLearnActivity.this, MainActivity.class);
        }
    }


    @Override
    public void onLongClick(IrKeyButton irKeyButton) {
        //长按按键，进行编辑
        KKIr kkIr = irKeyButton.getKkIr();
        if (isStartLearn) {
            kkIr.setfKey(irKeyButton.getText().toString());
            kkIr.setfName(irKeyButton.getText().toString());
            Intent intent = new Intent(RemoteLearnActivity.this, EditIrButtonActivity.class);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.IS_START_LEARN, irKeyButton.isLearned());
            intent.putExtra(IntentKey.ALL_ONE_IR_KEY, irKeyButton.getKkIr());
            intent.putExtra(EditIrButtonActivity.OPERATION_TYPE, EditIrButtonActivity.OPETATION_TYPE_EDIT);
            startActivity(intent);
        }
    }

    /**
     * 学习红外码和删除红外码成功后刷新下界面
     *
     * @param event
     */
    public void onEventMainThread(PartViewEvent event) {
        refreshView();
    }

    /**
     * 按键处理对应的action封装
     *
     * @param irKeyButton
     */
    public void setAction(IrKeyButton irKeyButton) {
        vibratorUtil.setVirbrator(mContext);
        if (irKeyButton.isLearned()) {
            KKIr kkIr = irKeyButton.getKkIr();
            if (action == null)
                action = new Action();
            String name = irKeyButton.getText().toString();
            if (TextUtils.isEmpty(name))
                name = AlloneUtil.getNameByFid(irKeyButton.getFid());
            action.setName(name);
            action.setValue1(kkDevice.getRid());
            action.setValue2(kkIr.getFid());
            action.setFreq(kkDevice.getFreq());
            int pulseNum = kkIr.getPluse().split(",").length;
            action.setPluseNum(pulseNum);
            action.setPluseData(kkIr.getPluse());
            action.setCommand(DeviceOrder.IR_CONTROL);
            onRefreshListener.onRefresh(action);
        } else {
            ToastUtil.showToast(R.string.allone_ation_no_learn_tip);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            onRefreshListener = (OnIrLearnRefreshListener) fragment;
        } catch (Exception e) {
            throw new ClassCastException(this.toString() + " must implement onRefreshListener");
        }
        super.onAttachFragment(fragment);
    }

    /**
     * fragment界面数据刷新回调接口
     */
    public interface OnIrLearnRefreshListener {
        /**
         * @param irData
         * @param isStartLearn 是否为修改遥控器
         */
        void onRefresh(KKDevice irData, boolean isStartLearn);

        /**
         * 定时倒计时按钮点击状态刷新
         *
         * @param action
         */
        void onRefresh(Action action);
    }
}
