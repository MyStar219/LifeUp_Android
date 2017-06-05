package com.orvibo.homemate.device.allone2.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceControlApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.AlloneControlData;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.epg.AlloneStbCache;
import com.orvibo.homemate.device.allone2.epg.ProgramGuidesActivity;
import com.orvibo.homemate.event.AlloneViewEvent;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.sharedPreferences.AlloneCache;
import com.orvibo.homemate.util.ClickUtil;
import com.orvibo.homemate.util.DisplayUtils;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DeviceCustomView;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by snown on 2016/7/11.
 * 首页小方子设备风扇,电视，机顶盒的view类
 */
public class BaseAlloneItemView extends LinearLayout implements View.OnClickListener, BaseResultListener {
    protected View itemView;
    protected View controlView;
    protected IrKeyButton btnPower;
    protected IrKeyButton btnTwo;
    protected IrKeyButton btnThree;
    protected IrKeyButton btnFour;
    protected DeviceCustomView deviceCustomView;
    protected View arrowView;
    protected ImageView arrowViewImage;
    protected Device device;
    protected String deviceId;
    protected int deviceType;
    protected IrData irData;


    protected ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();


    protected FragmentManager fragmentManager;

    protected HashMap<Integer, IrData.IrKey> keyHashMap;//固定数据列表(不在这个里面的都置灰，并且不可点击)

    protected LayoutParams lp;
    private DeviceDao deviceDao = new DeviceDao();
    private List<String> alloneDeviceIds = new ArrayList<>();

    protected boolean isFirst = true;

    private static final int ITEM_SPACE = 50;//展开时默认间距

    protected Activity context;

    protected boolean isOnline;

    public void setData(Device device, FragmentManager fragmentManager, boolean isOnline) {
        this.device = device;
        this.deviceId = device.getDeviceId();
        this.deviceType = device.getDeviceType();
        this.fragmentManager = fragmentManager;
        this.isOnline = isOnline;
    }

    public BaseAlloneItemView(Context context) {
        super(context);
    }

    public BaseAlloneItemView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LayoutInflater.from(context).inflate(R.layout.view_allone_common_item, this, true);
        deviceCustomView = (DeviceCustomView)
                findViewById(R.id.deviceCustomView);
        arrowView = findViewById(R.id.arrowView);
        itemView = findViewById(R.id.itemView);
        controlView = findViewById(R.id.controlView);
        arrowViewImage = (ImageView) findViewById(R.id.arrowViewImage);
        btnPower = (IrKeyButton) findViewById(R.id.btnPower);
        btnTwo = (IrKeyButton) findViewById(R.id.btnTwo);
        btnThree = (IrKeyButton) findViewById(R.id.btnThree);
        btnFour = (IrKeyButton) findViewById(R.id.btnFour);
        btnPower.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        findViewById(R.id.arrowView).setOnClickListener(this);
        lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTwo:
                if (deviceType == DeviceType.STB) {
                    Intent intent = new Intent(context, ProgramGuidesActivity.class);
                    AlloneStbCache.setEpgDisplay(deviceId, true);
                    intent.putExtra(IntentKey.DEVICE, device);
                    intent.putExtra(IntentKey.IS_HOME_CLICK, true);
                    context.startActivity(intent);
                } else {
                    controlIrBtn(v);
                }
                break;
            case R.id.btnPower:
            case R.id.btnThree:
            case R.id.btnFour:
                controlIrBtn(v);
                break;
        }
    }


    /**
     * 控制按键
     *
     * @param v
     */
    protected void controlIrBtn(View v) {
        IrKeyButton irKeyButton = (IrKeyButton) v;
        if (irKeyButton.isMatched()) {
            AlloneControlData controlData = irKeyButton.getControlData();
            DeviceControlApi.allOneControl(device.getUid(), deviceId, controlData.getFreq(), controlData.getPluseNum(), controlData.getPluseData(), false, this);
        }
    }

    /**
     * 控制按键
     */
    protected void controlIrBtn(AlloneControlData controlData) {
        DeviceControlApi.allOneControl(device.getUid(), deviceId, controlData.getFreq(), controlData.getPluseNum(), controlData.getPluseData(), false, this);
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        //为快速点击时不处理
        if (baseEvent.getResult() == ErrorCode.CLICK_FAST) {
            return;
        }
        ClickUtil.clearTime();
        if (baseEvent.getResult() == ErrorCode.SUCCESS) {
            //控制成功，设备不在线时更新设备状态
            if (!isOnline)
                EventBus.getDefault().post(new AlloneViewEvent(true));
        } else {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }

    public DeviceCustomView getDeviceCustomView() {
        return deviceCustomView;
    }

    public void setDeviceCustomView(DeviceCustomView deviceCustomView) {
        this.deviceCustomView = deviceCustomView;
    }

    /**
     * 通过箭头控制view展示
     *
     * @param isShow
     */
    protected void arrowViewShow(boolean isShow) {
        arrowViewImage.setImageResource(isShow ? R.drawable.icon_home_up_arrow_normal : R.drawable.icon_home_down_arrow_normal);
        controlView.setVisibility(isShow ? VISIBLE : GONE);
        int top = DisplayUtils.dipToPx(context, 16);
        int bottom = top;
        alloneDeviceIds = deviceDao.selAlloneDeviceId(device);
        if (alloneDeviceIds.size() == 0)
            return;
        int index = alloneDeviceIds.indexOf(deviceId);
        int size = alloneDeviceIds.size();
        int indexTop = AlloneCache.getViewTop(deviceId);
        int indexBottom = AlloneCache.getViewBottom(deviceId);
        int previous = index - 1 < 0 ? 0 : index - 1;
        String previousId = alloneDeviceIds.get(previous);
        int previousTop = AlloneCache.getViewTop(previousId);
        int previousBottom = AlloneCache.getViewBottom(previousId);
        int next = index + 1 >= size ? index : index + 1;
        String nextId = alloneDeviceIds.get(next);
        int nextTop = AlloneCache.getViewTop(nextId);
        int nextBottom = AlloneCache.getViewBottom(nextId);
        if (isFirst) {
            top = indexTop;
            bottom = indexBottom;
        } else {
            if (isShow) {//展开
                if ((AlloneCache.isHomeShow(previousId) && previousBottom != 0) || index == 0) {//上一个的底部有展开
                    top = 0;
                }
                if (AlloneCache.isHomeShow(nextId) && nextTop != 0 && index != size - 1) {//下一个的top有展开
                    bottom = 0;
                }
            } else {//关闭
                if (AlloneCache.isHomeShow(nextId) && nextTop == 0) {//下一个有展开，且上部为0
                    AlloneCache.setHomeShow(nextId, true, ITEM_SPACE, nextBottom);
                    EventBus.getDefault().post(new ArrowViewUpdateEvent(nextId));
                }
                if (AlloneCache.isHomeShow(previousId) && previousBottom == 0) {//上一个有展开，且底部为0
                    AlloneCache.setHomeShow(previousId, true, previousTop, ITEM_SPACE);
                    EventBus.getDefault().post(new ArrowViewUpdateEvent(previousId));
                }
                bottom = top = 0;
            }
        }
        lp.setMargins(0, top, 0, bottom);
        itemView.setLayoutParams(lp);
        AlloneCache.setHomeShow(deviceId, isShow, top, bottom);
    }

    /**
     * 直接设置，不进行判断
     */
    protected void arrowViewShow(String deviceId) {
        int indexTop = AlloneCache.getViewTop(deviceId);
        int indexBottom = AlloneCache.getViewBottom(deviceId);
        lp.setMargins(0, indexTop, 0, indexBottom);
        itemView.setLayoutParams(lp);
    }

    /**
     * 本地记忆箭头控制和打开
     */
    protected boolean arrowViewStateRemember() {
        if (AlloneCache.isHomeShow(deviceId) && !controlView.isShown()) {
            irData = AlloneCache.getIrData(deviceId);
            arrowViewShow(true);
            return true;
        } else {
            arrowViewShow(false);
        }
        return false;
    }


}
