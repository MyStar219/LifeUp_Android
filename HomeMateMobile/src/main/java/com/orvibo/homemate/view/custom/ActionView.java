package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.RGBData;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.ACPanelModelAndWindConstant;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.ByteUtil;
import com.orvibo.homemate.util.ColorUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.FrequentlyModeUtil;
import com.orvibo.homemate.util.MyLogger;
import com.orvibo.homemate.util.StringUtil;

/**
 * Created by huangqiyao on 2016/8/15 11:30.
 * 显示绑定动作的view。支持文字、颜色展示
 *
 * @version v1.10.2
 */
public class ActionView extends LinearLayout {
    private Context mContext;
    private DeviceDao mDeviceDao;

    private Device mDevice;
    private Action mAction;

    private LinearLayout ll_color;
    private ImageView iv_color;
    private TextView tv_action;

    public ActionView(Context context) {
        super(context);
        init(context);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.bind_action_view, this, true);
        ll_color = (LinearLayout) findViewById(R.id.ll_color);
        iv_color = (ImageView) findViewById(R.id.iv_color);
        tv_action = (TextView) findViewById(R.id.tv_action);
        mDeviceDao = new DeviceDao();
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

    public void setAction(Action action) {
        MyLogger.kLog().d(action);
        mAction = action;
        setActionView();
    }

    public void setDeviceAction(Device device, Action action) {
        setDevice(device);
        setAction(action);
    }

    private void setActionView() {
        if (mDevice == null) {
            if (mAction != null) {
                mDevice = mDeviceDao.selDevice(mAction.getDeviceId());//这个是被绑定设备id,不是情景面板orzigbee智能遥控器的deviceId
            }
        }
        if (mAction != null && !TextUtils.isEmpty(mAction.getCommand())) {
            String action = DeviceTool.getActionName(mContext, mAction);
            String order = mAction.getCommand();
            if (DeviceOrder.COLOR_CONTROL.equals(order)) {
                RGBData mRGBData = new RGBData();
                int[] rgb = ColorUtil.hsl2DeviceRgb(mAction.getValue4(), mAction.getValue3(), mAction.getValue2());
                int[] hsl = new int[4];
                hsl[0] = mAction.getValue4();
                hsl[1] = mAction.getValue3();
                hsl[2] = mAction.getValue2();
                hsl[3] = mAction.getValue1();
                mRGBData.setHsl(hsl);
                iv_color.setBackgroundColor(Color.rgb(mRGBData.getRgb()[0]
                        , mRGBData.getRgb()[1], mRGBData.getRgb()[2]));
                ll_color.setVisibility(View.VISIBLE);
                setValue2ActionView(mRGBData.getBrightness());
            } else if (DeviceOrder.COLOR_TEMPERATURE.equals(order)) {
                int colorTemperture = ColorUtil.colorToColorTemperture(mAction.getValue3());
                double[] rgb = ColorUtil.colorTemperatureToRGB(colorTemperture);
                int red = (int) rgb[0];
                int green = (int) rgb[1];
                int blue = (int) rgb[2];
                iv_color.setBackgroundColor(Color.rgb(red, green, blue));
                ll_color.setVisibility(View.VISIBLE);
                setValue2ActionView(mAction.getValue2());
            } else {
                ll_color.setVisibility(View.GONE);//隐藏颜色页面
                //  Device device = mDeviceDao.selDevice(uid, deviceId);
                if (mDevice != null) {
                    //百分比窗帘，如果有对应的模式则显示模式名称
                    if (mDevice != null && mDevice.getDeviceType() == DeviceType.CURTAIN_PERCENT && !DeviceOrder.STOP.equals(order) && mAction.getValue1() != DeviceStatusConstant.CURTAIN_OFF && mAction.getValue1() != DeviceStatusConstant.CURTAIN_ON) {
                        String frequentlyModeString = FrequentlyModeUtil.getFrequentlyModeString(mAction.getUid(), mAction.getDeviceId(), mAction.getValue1());
                        if (!StringUtil.isEmpty(frequentlyModeString)) {
                            tv_action.setText(frequentlyModeString);
                        } else {
                            tv_action.setText(action);
                        }
                    } else {
                        //wifi空调 空调面板 zigbee空调on/off
                        if (!DeviceOrder.IR_CONTROL.equals(order) && (mDevice.getDeviceType() == DeviceType.AC_PANEL || mDevice.getDeviceType() == DeviceType.AC)) {
                            //wifi空调和空调面板的开关是value1的第四个bit值来判断的
                            int onOffStatus = ByteUtil.fromValue1getOnOffStatus(mAction.getValue1());
                            if (onOffStatus == ACPanelModelAndWindConstant.AC_CLOSE) {
                                tv_action.setText(mContext.getResources().getString(R.string.action_off));
                            } else {
                               // tv_action.setText(mContext.getResources().getString(R.string.action_on));
                                String actionName = DeviceTool.getActionName(mContext, mAction);
                                tv_action.setText(actionName);
                            }
                            //如果是zigBee空调on/off，设置如下
                            if (mDevice.getDeviceType() == DeviceType.AC && mDevice.getAppDeviceId() != AppDeviceId.AC_WIIF) {
                                tv_action.setText(action);
                            }
                        } else {
                            //其他设备的动作设置
                            tv_action.setText(action);
                        }
                    }
                } else {
                    tv_action.setText(action);
                }
            }
        } else {
            tv_action.setText(R.string.device_set_bind_type_action_right);
        }
    }

    private void setValue2ActionView(int value2) {
        try {
            String temp = mContext.getString(R.string.action_level);
            String action = String.format(temp, DeviceTool.getPrecent(value2) + "%");
            tv_action.setText("" + action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置动作文字颜色
     *
     * @param color
     * @param selected
     */
    public void setActionTextColor(int color, boolean selected) {
        tv_action.setTextColor(color);
        //没有选择状态会置灰色
        if (selected) {
            ll_color.setAlpha(1f);
            iv_color.setAlpha(1f);
        } else {
            ll_color.setAlpha(0.3f);
            iv_color.setAlpha(0.3f);
        }
    }

    /**
     * 设置定时列表页面字体大小
     */
    public void setTimingActionTextSize() {
        tv_action.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    }

    public void setEmptyView(int actionTextColor) {
        tv_action.setText(R.string.device_set_bind_type_action_right);
        if (actionTextColor > 0) {
            tv_action.setTextColor(actionTextColor);
        }
        ll_color.setVisibility(View.GONE);
    }

}
