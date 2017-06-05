package com.orvibo.homemate.device.selectdevice;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.ObservableHorizontalScrollView;
import com.orvibo.homemate.view.popup.CommonPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangqiyao on 2016/7/16
 * 选择设备。智能场景选择小方页面。
 */
public class SelectAllone2DevicesPopup extends CommonPopup implements View.OnClickListener, View.OnKeyListener {
    private static final String TAG = SelectAllone2DevicesPopup.class.getSimpleName();
    private SelectAllone2DeviceAdapter mSelectAllone2DeviceAdapter;
    private View mContentView;
    private ObservableHorizontalScrollView mObservableHorizontalScrollView;
    private TextView bottomView;
    private GridView roomGridView;
    private LinearLayout mContent_ll;

    private View mTopView;
    private ImageView mArrow_iv;
    private String mUid;
    private Activity activity;

    /**
     * true记录选择了的房间，下次进来时就显示该房间。
     */
    private volatile boolean recordRoom = false;

    /**
     * true 显示箭头；false 不显示箭头
     */
    private volatile boolean mShowArrow = true;

    private List<Device> mDevices = new ArrayList<>();
    private Device mSelectedDevice;
    private Context mContext;

    public SelectAllone2DevicesPopup(Context context) {
        mContext = context;
        mCommonPopupContext = context;
    }

    @Override
    protected final void onPopupDismiss() {
        super.onPopupDismiss();
        if (mArrow_iv != null) {
            mArrow_iv.setAnimation(null);
            mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);
        }
    }

    public void setView(View topView, ImageView arrow_iv) {
        mTopView = topView;
        mArrow_iv = arrow_iv;
    }

    /**
     * 显示设备列表
     *
     * @param devices
     * @param selectedDevice 当前被选中的设备
     */
    public void show(List<Device> devices, Device selectedDevice) {
        mDevices = devices;
        mSelectedDevice = selectedDevice;
        if (devices == null || devices.isEmpty()) {
            mArrow_iv.setVisibility(View.GONE);
        } else {
            if (mShowArrow) {
                mArrow_iv.setVisibility(View.VISIBLE);
            } else {
                mArrow_iv.setVisibility(View.GONE);
            }
            mArrow_iv.setImageResource(R.drawable.select_room_arrow_selector);
            startArrowInAnim();

            mContentView = LayoutInflater.from(mCommonPopupContext).inflate(R.layout.popup_select_floor_and_room, null);
            mObservableHorizontalScrollView = (ObservableHorizontalScrollView) mContentView.findViewById(R.id.floorHorizontalScrollView);
            mObservableHorizontalScrollView.setVisibility(View.GONE);
            mContent_ll = (LinearLayout) mContentView.findViewById(R.id.content_ll);
            mContentView.findViewById(R.id.top_view).setOnClickListener(this);
            bottomView = (TextView) mContentView.findViewById(R.id.bottom_view);
            bottomView.setOnClickListener(this);

            roomGridView = (GridView) mContentView.findViewById(R.id.roomGridView);
            roomGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dismissAfterAnim();
                    mSelectedDevice = (Device) view.getTag(R.id.tag_device);
                    selectDevice(mSelectedDevice);
                }
            });

            roomGridView.setOnKeyListener(this);
            LogUtil.d(TAG, "showPopup()-devices:" + devices + ",selectedDevice:" + selectedDevice);
            String selectedDeviceId = null;
            if (selectedDevice != null) {
                selectedDeviceId = selectedDevice.getDeviceId();
            }
            mSelectAllone2DeviceAdapter = new SelectAllone2DeviceAdapter(mContext, devices, null, selectedDeviceId);
            roomGridView.setAdapter(mSelectAllone2DeviceAdapter);
            //当房间很多时也能显示当前选中的房间
            show(mCommonPopupContext, mContentView, mTopView, true);
            startInAnim();
        }
    }


    private void selectDevice(Device device) {
        LogUtil.d(TAG, "selectDevice()-device:" + device);
        mSelectAllone2DeviceAdapter.selectDevice(device.getDeviceId());
        onSelectedDevice(device);
    }

    /**
     * 显示选择房间界面动画
     */
    public void startInAnim() {
//        startAnim(R.anim.top_2_bottom_in);
    }

    private void startAnim(int anim) {
        mContent_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, anim));
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.top_view || vId == R.id.bottom_view) {
            dismissAfterAnim();
        } else if (vId == R.id.roomLinearLayout) {
            //选择设备item
            dismissAfterAnim();
            mSelectedDevice = (Device) v.getTag(R.id.tag_device);
            selectDevice(mSelectedDevice);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0 && isShowing()) {
            LogUtil.d(TAG, "onKey()");
            dismissAfterAnim();
            return true;
        }
        return false;
    }

    private void startArrowInAnim() {
        final Animation animation = AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.roate_0_to_180);
        animation.setFillAfter(true);
        animation.setFillBefore(false);
        animation.setFillEnabled(true);
        mArrow_iv.startAnimation(animation);
    }

    private void startArrowOutAnim() {
        Animation animation = AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.roate_180_to_0);
        animation.setFillAfter(true);
        animation.setFillBefore(false);
        animation.setFillEnabled(true);
        mArrow_iv.startAnimation(animation);
    }

    /**
     * 动画界面后再关闭界面
     */
    public void dismissAfterAnim() {
        mArrow_iv.clearAnimation();
        mArrow_iv.setImageResource(R.drawable.select_room_arrow_up_selector);
        bottomView.setVisibility(View.GONE);
        startArrowOutAnim();
        Animation animation = AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.top_2_bottom_out);
        mContent_ll.startAnimation(animation);
        mContentView.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.to_tran));
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //注意：4.1版本不能直接dismiss()。
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    protected void onSelectedDevice(Device device) {

    }
}
