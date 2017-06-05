package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.RemoteBind;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.RemoteBindDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KeyAction;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.util.ColorUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.FrequentlyModeUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 遥控器和情景面板选择绑定按键的基类
 * Created by huangqiyao on 2015/5/7.
 *
 * @intent {@link IntentKey#DEVICE}情景面板对象
 */
public class BaseSelectKeyNoActivity extends BaseActivity {
    private static final int REQUEST_CODE = 1;
    private static final int DRAWABLEID = R.drawable.tv_number_pressed;
    private RemoteBindDao mRemoteBindDao;
    private DeviceStatusDao deviceStatusDao;
    private SceneDao mSceneDao;
    private DeviceDao mDeviceDao;
    private Device mDevice;
    private String mUid;
    protected Map<Integer, View> mAllKeyViews;
    protected Map<Integer, View> mAllKeyLayoutViews;
    protected Map<Integer, View> mAllKeyColorViews;
    protected Map<Integer, TextView> mAllKeyTextViews1;
    protected Map<Integer, TextView> mAllKeyTextViews2;
    private ToastPopup toastPopup;

    private boolean flag = true;//响应点击事件标记

    private synchronized void setFlag(boolean flag) {
        this.flag = flag;
    }

    /**
     * 已经被绑定的keyNo
     */
    private Set<Integer> mBoundKeyNos;
    private volatile int mCurrentKeyNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        mUid = mDevice.getUid();
        mRemoteBindDao = new RemoteBindDao();
        mSceneDao = new SceneDao();
        mDeviceDao = new DeviceDao();
        deviceStatusDao = new DeviceStatusDao();
        toastPopup = new ToastPopup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBound();
    }

    public void selectKeyNo(View v) {
        //添加手机网络连接的判断
        if (!NetUtil.isNetworkEnable(this)){
            ToastUtil.showToast(R.string.NET_DISCONNECT);
            return;
        }
        if (flag) {
            setFlag(false);

            mCurrentKeyNo = Constant.INVALID_NUM;
            String remoteKeyName = null;
            for (Map.Entry<Integer, View> entry : mAllKeyViews.entrySet()) {
                if (v.getId() == entry.getValue().getId()) {
                    mCurrentKeyNo = entry.getKey();
                    if (entry.getValue() instanceof TextView) {
                        TextView textView = (TextView) entry.getValue();
                        remoteKeyName = textView.getText().toString();
                    }
                    break;
                }
            }
            if (isForbbienSet(mDevice.getDeviceType(), mCurrentKeyNo)) {
                ToastUtil.showToast(R.string.REMOTE_KEY_FORBIDDEN);
                setFlag(true);
                return;
            }

            //遥控器绑定要长按遥控器设置键使遥控器进入唤醒模式
            int onlineStatus = deviceStatusDao.selDeviceStatus(currentMainUid, mDevice.getDeviceId()).getOnline();
            if (mDevice.getDeviceType() == DeviceType.REMOTE && onlineStatus == OnlineStatus.OFFLINE) {
                showRemoteBindErrorPopup();
                setFlag(true);
                return;
            }

//        int keyNo = Integer.parseInt(tv.getContentDescription() + "");
            String keyName = mCurrentKeyNo + "";
            if (mDevice.getDeviceType() == DeviceType.REMOTE && !StringUtil.isEmpty(mDevice.getModel()) && mDevice.getModel().equals(ModelID.JIYUE_SUIYITIE)) {
                int order = 1;
                if (mCurrentKeyNo == 3) {
                    order = 1;
                } else if (mCurrentKeyNo == 11) {
                    order = 2;
                } else if (mCurrentKeyNo == 7) {
                    order = 3;
                } else if (mCurrentKeyNo == 15) {
                    order = 4;
                }
                keyName = order + "";
            } else if (mDevice.getDeviceType() == DeviceType.REMOTE && !StringUtil.isEmpty(remoteKeyName)) {
                keyName = remoteKeyName;
            }
            RemoteBind remoteBind = mRemoteBindDao.selRemoteBind(mUid, mDevice.getDeviceId(), mCurrentKeyNo, KeyAction.SINGLE_CLICK);
            Intent intent = new Intent(mContext, SelectBindTypeActivity.class);
            intent.putExtra(IntentKey.REMOTE_KEY_NAME, keyName);
            intent.putExtra(IntentKey.DEVICE, mDevice);
            intent.putExtra(IntentKey.REMOTE_KEY_NO, mCurrentKeyNo);
            intent.putExtra(IntentKey.REMOTE_KEY_ACTION, KeyAction.SINGLE_CLICK);
            intent.putExtra("remoteBind", remoteBind);
            startActivityForResult(intent, REQUEST_CODE);

            new TimeThread().start();
        }
    }

    /**
     * 计时线程（防止在一定时间段内重复点击按钮）
     */
    private class TimeThread extends Thread {
        public void run() {
            try {
                Thread.sleep(1000);
                setFlag(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param deviceType
     * @param keyNo
     * @return true系统禁用这些按钮，不能进行设置。
     */
    private boolean isForbbienSet(int deviceType, int keyNo) {
        if (deviceType == DeviceType.REMOTE) {
            //遥控器的1+ 2+ 锁 + - 设置按钮都不能配置
            if (keyNo == 13 || keyNo == 16 || keyNo == 17 || keyNo == 18 || keyNo == 19 || keyNo == 20) {
                return true;
            }
        }
        return false;
    }

    protected void initBound() {
        List<Integer> keyNos = mRemoteBindDao.selRemoteBindKeyNos(mUid, mDevice.getDeviceId());
        mBoundKeyNos = new HashSet<Integer>(keyNos);

//        Iterator<Integer> iterator = mBoundKeyNos.iterator();
//        while (iterator.hasNext()) {
//            int keyNo = iterator.next();
//            View view = mAllKeyViews.get(keyNo);
//            if (mDevice.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
//                setFiveKeyDrawable(view, keyNo, true);
//            } else if (mDevice.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
//                setSevenKeyDrawable(view, keyNo, true);
//            } else if (mDevice.getDeviceType() == DeviceType.SCENE_KEYPAD) {
//                setThreeKeyDrawable(view, keyNo, true);
//            } else {
//                view.setBackgroundResource(DRAWABLEID);
//            }
//        }

        for (Map.Entry<Integer, View> entry : mAllKeyViews.entrySet()) {
            int keyNo = entry.getKey();
            View view = entry.getValue();
            boolean isBind = mBoundKeyNos.contains(keyNo);
            if (mDevice.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
                setFiveKeyDrawable(view, keyNo, isBind);
            } else if (mDevice.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
                setSevenKeyDrawable(view, keyNo, isBind);
            } else if (mDevice.getDeviceType() == DeviceType.SCENE_KEYPAD) {
                setThreeKeyDrawable(view, keyNo, isBind);
            } else {
                if (!StringUtil.isEmpty(mDevice.getModel()) && ProductManage.isStickers(mDevice.getModel())) {
                    setJiyueRemoteKeyDrawable(view, keyNo, isBind);
                } else {
                    view.setBackgroundResource(isBind ? DRAWABLEID : R.drawable.remote_btn);
                }
            }
        }
    }

    private void setFiveKeyDrawable(View view, int order, boolean bind) {
        if (bind) {
            if (order == 1) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_one_bind);
            } else if (order == 2) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_two_bind);
            } else if (order == 3) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_three_bind);
            } else if (order == 4) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_four_bind);
            } else {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_five_bind);
            }
            setBindInfo(order);
        } else {
            if (order == 1) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_one_unbind);
            } else if (order == 2) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_two_unbind);
            } else if (order == 3) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_three_unbind);
            } else if (order == 4) {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_four_unbind);
            } else {
                view.setBackgroundResource(R.drawable.five_key_scene_btn_five_unbind);
            }
            mAllKeyLayoutViews.get(order).setVisibility(View.GONE);
            mAllKeyTextViews1.get(order).setText(order + "");
        }
    }

    private void setThreeKeyDrawable(View view, int order, boolean bind) {
        if (bind) {
            view.setBackgroundResource(R.drawable.seven_key_bind_small);
            setBindInfo(order);
        } else {
            view.setBackgroundResource(R.drawable.seven_key_unbind_small);
            mAllKeyLayoutViews.get(order).setVisibility(View.GONE);
            mAllKeyTextViews1.get(order).setText(order + "");
        }
    }

    private void setSevenKeyDrawable(View view, int order, boolean bind) {
        if (bind) {
            view.setBackgroundResource(R.drawable.seven_key_bind_small);
            setBindInfo(order);
        } else {
            view.setBackgroundResource(R.drawable.seven_key_unbind_small);
            mAllKeyLayoutViews.get(order).setVisibility(View.GONE);
            mAllKeyTextViews1.get(order).setText(order + "");
        }
    }

    private void setJiyueRemoteKeyDrawable(View view, int order, boolean bind) {
        if (bind) {
            view.setBackgroundResource(R.drawable.jiyue_remote_bind);
            setBindInfo(order);
        } else {
            view.setBackgroundResource(R.drawable.jiyue_remote_unbind);
            mAllKeyLayoutViews.get(order).setVisibility(View.GONE);
            int num = order;
            if (order == 3) {
                num = 1;
            } else if (order == 11) {
                num = 2;
            } else if (order == 7) {
                num = 3;
            } else if (order == 15) {
                num = 4;
            }
            mAllKeyTextViews1.get(order).setText(num + "");
        }
    }

    private void setBindInfo(int order) {
        RemoteBind remoteBind = mRemoteBindDao.selRemoteBind(mUid, mDevice.getDeviceId(), order, KeyAction.SINGLE_CLICK);
        String sceneName = order + "";
        if (remoteBind != null) {
            if (DeviceOrder.SCENE_CONTROL.equals(remoteBind.getCommand())) {
                mAllKeyLayoutViews.get(order).setVisibility(View.GONE);
                Scene scene = mSceneDao.selSceneBySceneId(mUid, remoteBind.getValue1());
                if (scene != null) {
                    sceneName = scene.getSceneName();
                }
                mAllKeyTextViews1.get(order).setText(sceneName);
            } else {
                mAllKeyLayoutViews.get(order).setVisibility(View.VISIBLE);
                String bindedDeviceId = remoteBind.getBindedDeviceId();
                Device device = mDeviceDao.selDevice(mUid, bindedDeviceId);
                if (device != null) {
                    sceneName = device.getDeviceName();
                }
                mAllKeyTextViews1.get(order).setText(sceneName);
                String action = DeviceTool.getActionName(mContext, remoteBind.getCommand(), remoteBind.getValue1(),
                        remoteBind.getValue2(), remoteBind.getValue3(), remoteBind.getValue4(), remoteBind.getBindedDeviceId());

                if (DeviceOrder.COLOR_CONTROL.equals(remoteBind.getCommand())) {
                    int[] rgb = ColorUtil.hsl2DeviceRgb(remoteBind.getValue4(), remoteBind.getValue3(), remoteBind.getValue2());
                    mAllKeyColorViews.get(order).setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
                    mAllKeyColorViews.get(order).setVisibility(View.VISIBLE);
//                    mAllKeyTextViews2.get(order).setText(mContext.getString(R.string.action_color_text));
                    mAllKeyTextViews2.get(order).setText("");
                } else if (DeviceOrder.COLOR_TEMPERATURE.equals(remoteBind.getCommand())) {
                    int colorTemperture = ColorUtil.colorToColorTemperture(remoteBind.getValue3());
                    double[] rgb = ColorUtil.colorTemperatureToRGB(colorTemperture);
                    int red = (int) rgb[0];
                    int green = (int) rgb[1];
                    int blue = (int) rgb[2];
                    mAllKeyColorViews.get(order).setBackgroundColor(Color.rgb(red, green, blue));
                    mAllKeyColorViews.get(order).setVisibility(View.VISIBLE);
//                    mAllKeyTextViews2.get(order).setText(mContext.getString(R.string.action_color_text));
                    mAllKeyTextViews2.get(order).setText("");
                } else {
                    mAllKeyColorViews.get(order).setVisibility(View.GONE);
                    if (device != null && device.getDeviceType() == DeviceType.CURTAIN_PERCENT && !DeviceOrder.STOP.equals(remoteBind.getCommand()) && remoteBind.getValue1() != 0 && remoteBind.getValue1() != 100) {
                        String frequentlyModeString = FrequentlyModeUtil.getFrequentlyModeString(device.getUid(), device.getDeviceId(), remoteBind.getValue1());
                        if (!StringUtil.isEmpty(frequentlyModeString)) {
                            mAllKeyTextViews2.get(order).setText(frequentlyModeString);
                        } else {
                            mAllKeyTextViews2.get(order).setText(action);
                        }
                    } else {
                        mAllKeyTextViews2.get(order).setText(action);
                    }
                }
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CODE) {
//                if (data.hasExtra(IntentKey.ADD_BIND_ID)) {
//                    View view = mAllKeyViews.get(mCurrentKeyNo);
//                    if (view != null) {
//                        if (mDevice.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
//                            setFiveKeyDrawable(view, mCurrentKeyNo, true);
//                        } else if (mDevice.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
//                            setSevenKeyDrawable(view, mCurrentKeyNo, true);
//                        } else if (mDevice.getDeviceType() == DeviceType.SCENE_KEYPAD) {
//                            setThreeKeyDrawable(view, mCurrentKeyNo, true);
//                        } else {
//                            view.setBackgroundResource(DRAWABLEID);
//                        }
//                    }
//                } else if (data.hasExtra(IntentKey.DELETE_BIND_ID)) {
//                    View view = mAllKeyViews.get(mCurrentKeyNo);
//                    if (view != null) {
//                        if (mDevice.getDeviceType() == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
//                            setFiveKeyDrawable(view, mCurrentKeyNo, false);
//                        } else if (mDevice.getDeviceType() == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
//                            setSevenKeyDrawable(view, mCurrentKeyNo, false);
//                        } else if (mDevice.getDeviceType() == DeviceType.SCENE_KEYPAD) {
//                            setThreeKeyDrawable(view, mCurrentKeyNo, false);
//                        } else {
//                            view.setBackgroundResource(R.drawable.remote_btn);
//                        }
//                    }
//                }
//            }
//        } super.onActivityResult(requestCode, resultCode, data);
//    }

    private void showRemoteBindErrorPopup() {
        if (!StringUtil.isEmpty(mDevice.getModel()) && ProductManage.isStickers(mDevice.getModel())) {
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

    @Override
    protected void onDestroy() {

        if (toastPopup != null && toastPopup.isShowing()) {
            toastPopup.dismiss();
        }
        super.onDestroy();
    }
}
