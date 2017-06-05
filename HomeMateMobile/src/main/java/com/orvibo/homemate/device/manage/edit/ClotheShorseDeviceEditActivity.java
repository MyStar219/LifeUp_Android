package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.api.listener.OnClotheShorseTimeReportListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.clotheshorse.ClotheShorseCountdown;
import com.orvibo.homemate.bo.clotheshorse.ClotheShorseStatus;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.ClotheShorseCountdownDao;
import com.orvibo.homemate.dao.ClotheShorseStatusDao;
import com.orvibo.homemate.dao.GatewayDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.model.clotheshorse.ClotheShorseTimeReport;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.popup.RacksTimeSetPopup;

/**
 * 晾衣架编辑界面
 * Created by snown on 2015/11/18.
 */
public class ClotheShorseDeviceEditActivity extends BaseActivity implements View.OnClickListener,
        RacksTimeSetPopup.OnTimeResultListener,
        BaseResultListener,
        OnClotheShorseTimeReportListener {
    private final String TAG = ClotheShorseDeviceEditActivity.class.getSimpleName();
    private NavigationGreenBar    nbTitle;
    private TextView              deviceNameTextView;
    private LinearLayout          deviceName;
    private TextView              itemWindDryingText;
    private LinearLayout          itemWindDryingTime;
    private TextView              itemHeatDryingText;
    private LinearLayout          itemHeatDryingTime;
    private TextView              itemSterilizingText;
    private LinearLayout          itemSterilizingTime;
    private TextView              itemLightingText;
    private LinearLayout          itemLightingTime;
    private TextView              deviceInfoTextView;
    private Button                deleteButton;
    private RacksTimeSetPopup     racksTimeSetPopup;
    private Device                device;
    private ClotheShorseCountdown oldTime, newTime;

    private ClotheShorseStatusDao    clotheShorseStatusDao;
    private ClotheShorseCountdownDao clotheShorseCountdownDao;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothe_shorse_device_edit);
        ClotheShorseTimeReport.getInstance(mAppContext).registerClotheShorseTimeReport(this);
        initView();

        clotheShorseStatusDao = new ClotheShorseStatusDao();
        clotheShorseCountdownDao = new ClotheShorseCountdownDao();
    }

    private void initView() {
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        oldTime = (ClotheShorseCountdown) getIntent().getSerializableExtra(IntentKey.CLOTHE_SHORSE_COUNTDOWN);
        newTime = oldTime.clone();
        nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        //去掉抬头显示晾衣架改为“设置”
//        nbTitle.setText(device.getDeviceName());

        deviceName = (LinearLayout) findViewById(R.id.deviceName);
        deviceName.setOnClickListener(this);
        deviceNameTextView = (TextView) findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(device.getDeviceName());

        //照明
        itemLightingTime = (LinearLayout) findViewById(R.id.itemLightingTime);
        itemLightingTime.setOnClickListener(this);
        itemLightingText = (TextView) findViewById(R.id.itemLightingText);
//        itemLightingText.setText(oldTime.getLightingTime()==0?getString(R.string.lighting_daily):getTime(TimeUtil.getTimeByMin(oldTime.getLightingTime())));
        //消毒
        itemSterilizingTime = (LinearLayout) findViewById(R.id.itemSterilizingTime);
        itemSterilizingTime.setOnClickListener(this);
        itemSterilizingText = (TextView) findViewById(R.id.itemSterilizingText);
        // itemSterilizingText.setText(getTime(TimeUtil.getTimeByMin(oldTime.getSterilizingTime())));
        //烘干
        itemHeatDryingTime = (LinearLayout) findViewById(R.id.itemHeatDryingTime);
        itemHeatDryingTime.setOnClickListener(this);
        itemHeatDryingText = (TextView) findViewById(R.id.itemHeatDryingText);
        //itemHeatDryingText.setText(getTime(TimeUtil.getTimeByMin(oldTime.getHeatDryingTime())));
        //风干
        itemWindDryingTime = (LinearLayout) findViewById(R.id.itemWindDryingTime);
        itemWindDryingTime.setOnClickListener(this);
        itemWindDryingText = (TextView) findViewById(R.id.itemWindDryingText);
        //itemWindDryingText.setText(getTime(TimeUtil.getTimeByMin(oldTime.getWindDryingTime())));

        racksTimeSetPopup = new RacksTimeSetPopup(this);
        racksTimeSetPopup.setOnTimeResultListener(this);

        deviceInfoTextView = (TextView) findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setOnClickListener(this);

        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        initStatus(oldTime);

        imageView = (ImageView) findViewById(R.id.imageView);
        if (device != null && !StringUtil.isEmpty(device.getModel())
                && (device.getModel().equals(ModelID.CLH001) || device.getModel().equals(ModelID.SH40))) {
            itemLightingTime.setVisibility(View.GONE);
            itemSterilizingTime.setVisibility(View.GONE);
            itemHeatDryingTime.setVisibility(View.GONE);
            itemWindDryingTime.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }
    }

    private void initStatus(ClotheShorseCountdown cd) {
        if (cd != null) {
            //照明
            itemLightingText.setText(cd.getLightingTime() == 0 ? getString(R.string.lighting_daily) : TimeUtil.getTime(TimeUtil.getTimeByMin(cd.getLightingTime())));
            //消毒
            itemSterilizingText.setText(TimeUtil.getTime(TimeUtil.getTimeByMin(cd.getSterilizingTime())));
            //烘干
            itemHeatDryingText.setText(TimeUtil.getTime(TimeUtil.getTimeByMin(cd.getHeatDryingTime())));
            //风干
            itemWindDryingText.setText(TimeUtil.getTime(TimeUtil.getTimeByMin(cd.getWindDryingTime())));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.itemWindDryingTime:
                if (!isWorking(R.id.itemWindDryingTime))
                    racksTimeSetPopup.show(v, getString(R.string.select) + getString(R.string.item_wind_drying_time), TimeUtil.getTimeByMin(newTime.getWindDryingTime()));
                else
                    ToastUtil.showToast(getString(R.string.cth_time_change_tip));
                break;
            case R.id.itemHeatDryingTime:
                if (!isWorking(R.id.itemHeatDryingTime))
                    racksTimeSetPopup.show(v, getString(R.string.select) + getString(R.string.item_heat_drying_time), TimeUtil.getTimeByMin(newTime.getHeatDryingTime()));
                else
                    ToastUtil.showToast(getString(R.string.cth_time_change_tip));
                break;
            case R.id.itemSterilizingTime:
                if (!isWorking(R.id.itemSterilizingTime))
                    racksTimeSetPopup.show(v, getString(R.string.select) + getString(R.string.item_sterilizing_time), TimeUtil.getTimeByMin(newTime.getSterilizingTime()));
                else
                    ToastUtil.showToast(getString(R.string.cth_time_change_tip));
                break;
            case R.id.itemLightingTime:
                if (!isWorking(R.id.itemLightingTime))
                    racksTimeSetPopup.show(v,    getString(R.string.item_lighting_time), TimeUtil.getTimeByMin(newTime.getLightingTime()));
//                    racksTimeSetPopup.show(v, getString(R.string.select) + getString(R.string.item_lighting_time), TimeUtil.getTimeByMin(newTime.getLightingTime()));
                else
                    ToastUtil.showToast(getString(R.string.cth_time_change_tip));
                break;
            case R.id.deviceInfoTextView:
                Intent intent = new Intent(this, DeviceInfoActivity.class);
                if (device != null) {
                    intent.putExtra(Constant.GATEWAY, new GatewayDao().selGatewayByUid(device.getUid()));
                }
                intent.putExtra(Constant.DEVICE, device);
                startActivity(intent);
                break;
            case R.id.deviceName:
                Intent intent1 = new Intent(this, DeviceNameActivity.class);
                intent1.putExtra(Constant.DEVICE, device);
                startActivityForResult(intent1, 0);
                break;
            case R.id.deleteButton:
//                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_SettingsCOCO_Delete), null);
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.device_set_delete_content));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.delete));
                dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.red));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(this);
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            device = (Device) data.getSerializableExtra(Constant.DEVICE);
            deviceNameTextView.setText(device.getDeviceName());
            nbTitle.setText(device.getDeviceName());
        }
    }

    /**
     * 点击控件时间返回
     *
     * @param v
     * @param hour
     * @param minute
     */
    @Override
    public void timeResult(View v, int hour, int minute) {
        int time = hour * 60 + minute * 10;
        switch (v.getId()) {
            case R.id.itemWindDryingTime:
//                newTime.setWindDryingTime(time);
                if (time != oldTime.getWindDryingTime()) {
                    showDialog();
                    DeviceApi.clotheShorseTimeSet(UserCache.getCurrentUserName(mAppContext), device.getUid(), device.getDeviceId(), Constant.INVALID_NUM, Constant.INVALID_NUM, Constant.INVALID_NUM, time, this);
                }
                break;
            case R.id.itemHeatDryingTime:
//                newTime.setHeatDryingTime(time);
                if (time != oldTime.getHeatDryingTime()) {
                    showDialog();
                    DeviceApi.clotheShorseTimeSet(UserCache.getCurrentUserName(mAppContext), device.getUid(), device.getDeviceId(), Constant.INVALID_NUM, Constant.INVALID_NUM, time, Constant.INVALID_NUM, this);
                }
                break;
            case R.id.itemSterilizingTime:
//                newTime.setSterilizingTime(time);
                if (time != oldTime.getSterilizingTime()) {
                    showDialog();
                    DeviceApi.clotheShorseTimeSet(UserCache.getCurrentUserName(mAppContext), device.getUid(), device.getDeviceId(), Constant.INVALID_NUM, time, Constant.INVALID_NUM, Constant.INVALID_NUM, this);
                }
                break;
            case R.id.itemLightingTime:
//                newTime.setLightingTime(time);
                if (time != oldTime.getLightingTime()) {
                    showDialog();
                    DeviceApi.clotheShorseTimeSet(UserCache.getCurrentUserName(mAppContext), device.getUid(), device.getDeviceId(), time, Constant.INVALID_NUM, Constant.INVALID_NUM, Constant.INVALID_NUM, this);
                }
                break;
        }
//        if (!ObjectUtil.objectDataEquals(oldTime, newTime))
//            saveTime();
    }


//    private void saveTime() {
//        showDialogNow();
//        DeviceApi.clotheShorseTimeSet(UserCache.getCurrentUserName(mAppContext), device.getUid(), device.getDeviceId(), newTime.getLightingTime(), newTime.getSterilizingTime(), newTime.getHeatDryingTime(), newTime.getWindDryingTime(), new BaseResultListener() {
//            @Override
//            public void onResultReturn(BaseEvent baseEvent) {
//                dismissDialog();
//                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
////                    oldTime = newTime.clone();
////                    initStatus(newTime);
//                } else {
//                    ToastUtil.toastError(baseEvent.getResult());
//                }
//            }
//        });
//    }


    @Override
    public void onLeftButtonClick(View view) {
// 判断网络是否连接
        if (!NetUtil.isNetworkEnable(this)) {
            ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
            return;
        }
        showDialog();
        mDeleteDevice.deleteWifiDeviceOrGateway(device.getUid(), UserCache.getCurrentUserName(this));
        //mDeleteDevice.startDeleteDevice(device.getUid(), UserCache.getCurrentUserName(this), null, null, device.getDeviceType());
    }

    @Override
    public void onRightButtonClick(View view) {

    }

    DeleteDevice mDeleteDevice = new DeleteDevice(ClotheShorseDeviceEditActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                //删除状态表和倒计时表
                clotheShorseCountdownDao.delClotheShorseCountdown(device.getDeviceId());
                clotheShorseStatusDao.delClotheShorseStatus(device.getDeviceId());
                setResult(1);
                finish();
            } else {
                ToastUtil.toastError(result);
            }
        }
    };

    @Override
    public void onClotheShorseTimeReport(ClotheShorseCountdown clotheShorseTime) {
        LogUtil.d(TAG, "onClotheShorseTimeReport()-clotheShorseTime:" + clotheShorseTime);
        if (!isFinishingOrDestroyed() && (device != null && !StringUtil.isEmpty(device.getDeviceId()) && clotheShorseTime != null && device.getDeviceId().equals(clotheShorseTime.getDeviceId()))) {
            oldTime = clotheShorseTime;
            newTime = oldTime.clone();
            initStatus(clotheShorseTime);
        }
    }

    @Override
    protected void onDestroy() {
        ClotheShorseTimeReport.getInstance(mAppContext).unregisterClotheShorseTimeReport(this);
        super.onDestroy();
    }

    /**
     * 判断是否在工作状态
     *
     * @param viewId
     * @return
     */
    public boolean isWorking(int viewId) {
        ClotheShorseStatus clotheShorseStatus = new ClotheShorseStatusDao().selClotheShorseStatus(device.getDeviceId());
        String onTag = "on";
        switch (viewId) {
            case R.id.itemWindDryingTime:
                if (clotheShorseStatus.getWindDryingState().equals(onTag))
                    return true;
                break;
            case R.id.itemHeatDryingTime:
                if (clotheShorseStatus.getHeatDryingState().equals(onTag))
                    return true;
                break;
            case R.id.itemSterilizingTime:
                if (clotheShorseStatus.getSterilizingState().equals(onTag))
                    return true;
                break;
            case R.id.itemLightingTime:
                if (clotheShorseStatus.getLightingState().equals(onTag))
                    return true;
                break;
        }
        return false;
    }


    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (isFinishingOrDestroyed()) {
            return;
        }
        dismissDialog();
        if (baseEvent.getResult() != ErrorCode.SUCCESS) {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }

}
