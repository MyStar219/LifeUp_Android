package com.orvibo.homemate.device.allone2.epg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.IrApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.core.load.LoadParam;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.KKIrDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.allone2.STBControlFragment;
import com.orvibo.homemate.device.allone2.listener.OnKeyClickListener;
import com.orvibo.homemate.device.allone2.listener.OnRefreshListener;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.LoadUtil;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.FragmentSaveStateTabHost;
import com.orvibo.homemate.view.custom.IrKeyButton;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by snow on 2016/7/19.
 * epg界面包含epg和stb
 */
public class ProgramGuidesActivity extends BaseControlActivity implements OnKeyClickListener, BaseResultListener, TabHost.OnTabChangeListener {
    private FragmentSaveStateTabHost mTabHost = null;
    private IrData irData;

    private final static String TAG_EPG = "epg";
    private final static String TAG_STB = "stb";


    private AlloneControlData data;

    private OnRefreshListener onRefreshListener;

    protected PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_guides);
        initView();
        initData();
    }

    protected void initView() {
        mTabHost = (FragmentSaveStateTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        initPopup();
    }


    protected void initData() {
//        LoadParam loadParam = new LoadParam();
//        loadParam.notifyRefresh = true;
//        loadParam.requestConfig = RequestConfig.getOnlyRemoteConfig();
//        LoadTarget loadTarget = new LoadTarget();
//        loadTarget.uid = device.getUid();
//        loadTarget.target = device.getUid();
//        loadTarget.tableName = TableName.CHANNEL_COLLECTION;
//        loadParam.loadTarget = loadTarget;
        LoadUtil.noticeLoadServerData(LoadParam.getLoadServerParam(mAppContext));
//        LoadUtil.noticeLoadTable(device.getUid(), TableName.CHANNEL_COLLECTION);
        irData = AlloneCache.getIrData(deviceId);
        if (irData == null)
            finish();
        mTabHost.addTab(mTabHost.newTabSpec(TAG_EPG).setIndicator(getTabItemView(TAG_EPG)), ProgramGuidesFragment.class, getBundle());
        mTabHost.addTab(mTabHost.newTabSpec(TAG_STB).setIndicator(getTabItemView(TAG_STB)), STBControlFragment.class, getBundle());
        mTabHost.setOnTabChangedListener(this);
        if (AlloneStbCache.isEpgDisplay(deviceId))
            mTabHost.setCurrentTab(0);
        else
            mTabHost.setCurrentTab(1);
    }

    private Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.DEVICE, device);
        bundle.putSerializable(IntentKey.ALL_ONE_DATA, irData);
        bundle.putBoolean(IntentKey.IS_HOME_CLICK, true);
        return bundle;
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(String tag) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view, null);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        switch (tag) {
            case TAG_EPG:
                textView.setText(getString(R.string.tv_program));
                textView.setBackgroundResource(R.drawable.btn_tab_left_selector);
                break;
            case TAG_STB:
                textView.setText(getString(R.string.stb_tab_name));
                textView.setBackgroundResource(R.drawable.btn_tab_right_selector);
                break;
        }

        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        popupWindow.dismiss();
        switch (v.getId()) {
            case R.id.programSet:
                //节目单,如果在epg可直接跳转
                if (isEpgFragment()) {
                    ((ProgramGuidesFragment) mTabHost.getCurrentFragment()).jumpChannel();
                } else {
                    //如果是在机顶盒界面要把部分数据下载下来
                    Intent programChannelListIntent = new Intent();
                    programChannelListIntent.putExtra(IntentKey.DEVICE, device);
                    programChannelListIntent.putExtra(ProgramChannelsListActivity.IRDATA, irData);
                    ActivityJumpUtil.jumpAct(ProgramGuidesActivity.this, ProgramChannelsListActivity.class, programChannelListIntent);
                }
                break;
            case R.id.reserveSet:
                Intent intent = new Intent(mContext, ProgramSubscribeActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.channelSet:
                //更换频道表
                Intent changeChannelTableIntent = new Intent();
                changeChannelTableIntent.putExtra(IntentKey.DEVICE, device);
                ActivityJumpUtil.jumpAct(this, ChangeChennalTableActivity.class, changeChannelTableIntent);
                break;
        }
    }


    @Override
    public void OnClick(IrKeyButton irKeyButton) {
        if (irKeyButton.isMatched()) {
            AlloneControlData controlData = irKeyButton.getControlData();
            DeviceControlApi.allOneControl(device.getUid(), deviceId, controlData.getFreq(), controlData.getPluseNum(), controlData.getPluseData(), false, this);
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
    public void onTvClick(final KKIr kkIr, IrKeyButton irKeyButton) {
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
                            if (data != null) {
                                showDialog();
                                DeviceControlApi.allOneControl(device.getUid(), deviceId, data.getFreq(), data.getPluseNum(), data.getPluseData(), false, ProgramGuidesActivity.this);
                            }
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
        if (data != null) {
            DeviceControlApi.allOneControl(device.getUid(), deviceId, data.getFreq(), data.getPluseNum(), data.getPluseData(), false, this);
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
                    EventBus.getDefault().post(new PartViewEvent());
                    onRefreshListener.onRefresh(irData);
                } else {
                    ToastUtil.toastError(baseEvent.getResult());
                }
            }
        });
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

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
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
    public void rightTitleClick(View view) {
        popupWindow.showAtLocation(view, Gravity.RIGHT | Gravity.TOP, 0, getResources().getDimensionPixelSize(R.dimen.coco_popup_margin));
    }

    private void initPopup() {
        View view = View.inflate(this, R.layout.popup_allone_epg_setting, null);
        TextView programSet = (TextView) view.findViewById(R.id.programSet);
        TextView reserveSet = (TextView) view.findViewById(R.id.reserveSet);
        TextView channelSet = (TextView) view.findViewById(R.id.channelSet);
        TextView settingsTextView = (TextView) view.findViewById(R.id.settingsTextView);
        programSet.setOnClickListener(this);
        reserveSet.setOnClickListener(this);
        channelSet.setOnClickListener(this);
        settingsTextView.setOnClickListener(this);

        popupWindow = new PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        PopupWindowUtil.initPopup(popupWindow, getResources().getDrawable(R.color.transparent), 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 是否为epg的fragment
     *
     * @return
     */
    private boolean isEpgFragment() {
        return AlloneStbCache.isEpgDisplay(deviceId);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (tabId.equalsIgnoreCase(TAG_EPG))
            AlloneStbCache.setEpgDisplay(deviceId, true);
        else
            AlloneStbCache.setEpgDisplay(deviceId, false);
    }
}
