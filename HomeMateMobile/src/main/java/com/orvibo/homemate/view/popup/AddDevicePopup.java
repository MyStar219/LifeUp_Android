//package com.orvibo.homemate.view.popup;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.drawable.AnimationDrawable;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.style.ForegroundColorSpan;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.orvibo.homemate.device.manage.add.AddDeviceFailActivity;
//import com.orvibo.homemate.device.manage.add.AddDeviceSuccessActivity;
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.CameraInfo;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.device.manage.add.AddVicenterActivity;
//import com.orvibo.homemate.model.adddevice.vicenter.DeviceJoinin;
//import com.orvibo.homemate.model.adddevice.vicenter.DeviceJoinin.OnDeviceJoininListener;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.sharedPreferences.UserCache;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.util.NetUtil;
//import com.orvibo.homemate.util.ToastUtil;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * @deprecated 已经使用新的添加交互  {@link AddVicenterActivity}
// * MVC(model,view,control)
// * 添加设备(V+C)
// */
//public class AddDevicePopup {
//    private static final String TAG = AddDevicePopup.class.getSimpleName();
//    private Context mContext;
//    private DeviceJoinin mDeviceJoinin;
//    private String mMainUid;
//
//    private PopupWindow mPopup;
//    private TextView lastTime_tv;
//    private TextView mCount_tv;
//    private AnimationDrawable mProgressAnim;
//
//    private volatile int mAddedCount = 0;
//
//    private volatile List<Device> mDevices;
//
//    /**
//     * true跳转到添加设备失败界面
//     */
//    private volatile boolean isToFailView = false;
//
//    public AddDevicePopup(Context context) {
//        this.mContext = context;
//        mDevices = new ArrayList<Device>();
//        mDeviceJoinin = new DeviceJoinin(context);
////        mDeviceJoinin = DeviceJoinin.getInstance(context);
//        mMainUid = UserCache.getCurrentMainUid(mContext);
//    }
//
//    /**
//     * @param isToFailView true添加失败后跳转到失败界面
//     */
//    @SuppressLint("InflateParams")
//    public void show(boolean isToFailView) {
//        this.isToFailView = isToFailView;
//        if (isShowing()) {
//            dismiss();
//        }
//        mDeviceJoinin.setOnDeviceJoininListener(new MyDeviceJoinInListener());
//
//        View contentView = LayoutInflater.from(mContext).inflate(
//                R.layout.add_device, null);
//        mCount_tv = (TextView) contentView.findViewById(R.id.addedCount_tv);
//        lastTime_tv = (TextView) contentView.findViewById(R.id.lastTime_tv);
//        lastTime_tv.setText(R.string.add_device2);
//        //点击完成
//        contentView.findViewById(R.id.onAddFinish).setOnClickListener(
//                new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        dismiss();
//                        stopDeviceJoinin();
//
//                        if (getAddedCount() > 0) {
//                            // 跳转到添加成功界面
//                            toAddDeviceSuccessActivity();
//                        } else {
//                            if (!NetUtil.isNetworkEnable(mContext)) {
//                                ToastUtil.toastError( ErrorCode.NET_DISCONNECT);
//                            }
//                            // 跳转到添加失败界面
//                            //toAddDeviceFailActivity();
//                            onToAddFailView();
//                        }
//                    }
//                });
//        setAddedCount(0);
//        ImageView iv = (ImageView) contentView.findViewById(R.id.progress_iv);
//        mProgressAnim = (AnimationDrawable) iv.getDrawable();
//        mProgressAnim.start();
//        mPopup = new PopupWindow(contentView,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        mPopup.setFocusable(true);
//        mPopup.setTouchable(true);
//        mPopup.showAtLocation(contentView, Gravity.CENTER, 0, 0);
////        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
////            @Override
////            public void onDismiss() {
////                stopDeviceJoinin();
//
//
////            }
////        });
//        mAddedCount = 0;
//        mDeviceJoinin.joinin(mMainUid,true);
//        mDevices.clear();
//    }
//
//    /**
//     * 跳转到添加设备界面
//     */
//    private void toAddDeviceSuccessActivity() {
//        Intent intent = new Intent(mContext,
//                AddDeviceSuccessActivity.class);
//        intent.putExtra("addedDevices", (Serializable) mDevices);
//        mContext.startActivity(intent);
//        onSetDevices();
//    }
//
//    /**
//     * 跳转到添加失败界面
//     */
//    protected void toAddDeviceFailActivity() {
//        Intent intent = new Intent(mContext,
//                AddDeviceFailActivity.class);
//        //intent.putExtra("addedDevices", (Serializable) mDevices);
//        mContext.startActivity(intent);
//    }
//
//    private synchronized void addAddedDevice(Device device) {
//        mDevices.add(device);
//    }
//
//    private synchronized List<Device> getAddedDevices() {
//        return mDevices;
//    }
//
//    private synchronized void addAddedCount() {
//        mAddedCount += 1;
//    }
//
//    private synchronized int getAddedCount() {
//        return mAddedCount;
//    }
//
//    /**
//     * 已经添加的设备数量
//     *
//     * @param count 设备数量
//     */
//    public void setAddedCount(int count) {
//        String s = mContext.getString(R.string.add_device_count);
//        s = String.format(s, count);
//        SpannableString sp = new SpannableString(s);
//        sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 3, 3 + ("" + count).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        mCount_tv.setText(sp);
//    }
//
//    private class MyDeviceJoinInListener implements OnDeviceJoininListener {
//
//        @Override
//        public void onTimeRemaining(int time) {
//            LogUtil.d(TAG, "onTimeRemaining()-time:" + time);
//            String s = mContext.getString(R.string.add_device);
//            s = String.format(s, time);
//            SpannableString sp = new SpannableString(s);
//            sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 6, 6 + ("" + time).length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            lastTime_tv.setText(sp);
//        }
//
//        @Override
//        public void onNewDevice(Device device) {
//            addAddedDevice(device);
//            addAddedCount();
//            setAddedCount(getAddedCount());
//        }
//
//        @Override
//        public void onNewCamera(CameraInfo cameraInfo) {
//            addAddedCount();
//            setAddedCount(getAddedCount());
//        }
//
//        @Override
//        public void onLinkageFinish() {
//            LogUtil.i(TAG, "onLinkageFinish()-Search finish.");
//            dismiss();
//            if (getAddedCount() == 0) {
//                LogUtil.e(TAG, "onLinkageFinish()-Could not search new devices");
//                if (isToFailView) {
//                    onToAddFailView();
//                    mContext.startActivity(new Intent(mContext, AddDeviceFailActivity.class));
//                }
//            } else {
//                toAddDeviceSuccessActivity();
//            }
//        }
//
//        @Override
//        public void onError(int errorCode) {
//            // 跳转到添加设备失败界面
//            LogUtil.e(TAG, "onError()-errorCode:" + errorCode);
//            dismiss();
//            if (isToFailView) {
//                onToAddFailView();
//                mContext.startActivity(new Intent(mContext, AddDeviceFailActivity.class));
//            }
//        }
//    }
//
//    public void dismiss() {
//        if (mDeviceJoinin != null) {
//            mDeviceJoinin.stopCountDownAndAcceptNewDevice();
//        }
//
//        if (isShowing()) {
//            mPopup.dismiss();
//        }
//        if (mProgressAnim != null) {
//            mProgressAnim.stop();
//        }
//    }
//
//    /**
//     * 停止组网
//     */
//    private void stopDeviceJoinin() {
//        if (mDeviceJoinin != null) {
//            mDeviceJoinin.stopJoinin(true);
//        }
//    }
//
//    public boolean isShowing() {
//        return mPopup != null && mPopup.isShowing();
//    }
//
//    /**
//     * 跳转到设置设备界面
//     */
//    protected void onSetDevices() {
//
//    }
//
//    /**
//     * 跳转到添加设备失败界面
//     */
//    protected void onToAddFailView() {
//
//    }
//    // addFinish
//}
