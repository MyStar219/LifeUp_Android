package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.WheelLockAdapter;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;
import com.orvibo.homemate.view.custom.wheelview.WheelView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snown on 2016/07/4.
 * 时间选择控件
 */
public class DeviceSelectTimePopup extends CommonPopup implements View.OnClickListener, TosGallery.OnEndFlingListener {
    private static final String TAG = DeviceSelectTimePopup.class.getSimpleName();
    private View wheel_ll;

    private WheelView timeView;
    private TextView titleView;

    private WheelLockAdapter timeAdapter;

    private List<Integer> timeList = new ArrayList<>();

    private boolean isSecond;//区分是分钟还是秒

    private ITimeListener iTimeListener;

    private String suffix;//后缀


    public DeviceSelectTimePopup(Activity activity, ITimeListener iTimeListener) {
        mCommonPopupContext = activity;
        this.iTimeListener = iTimeListener;
    }


    public void showView(String title, int time, int count, boolean isSecond) {
        timeList.clear();
        this.isSecond = isSecond;
        View contentView = LayoutInflater.from(mCommonPopupContext).inflate(
                R.layout.popup_select_time, null);
        wheel_ll = contentView.findViewById(R.id.wheel_ll);
        timeView = (WheelView) contentView.findViewById(R.id.time);
        titleView = (TextView) contentView.findViewById(R.id.title);
        titleView.setText(title);

        for (int i = 0; i < count; i++) {
            timeList.add(i + 1);
        }
        suffix = isSecond ? mCommonPopupContext.getString(R.string.time_second) : mCommonPopupContext.getString(R.string.time_minutes);
        timeAdapter = new WheelLockAdapter(mCommonPopupContext, timeList, suffix);
        timeView.setAdapter(timeAdapter);
        if(!timeList.isEmpty())
            timeView.setSelection(timeList.indexOf(time) != -1 ? timeList.indexOf(time) : 0);
        timeView.setOnEndFlingListener(this);

        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        show(mCommonPopupContext, contentView, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_tv: {
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                break;
            }
            case R.id.confirm_tv: {
                wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_out));
                dismissPopupDelay();
                iTimeListener.onTimeReturn(timeList.get(timeView.getSelectedItemPosition()), isSecond);
                break;
            }
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }

    @Override
    public void onEndFling(TosGallery v) {
        v.setSelection(v.getSelectedItemPosition());
    }

    public interface ITimeListener {
        void onTimeReturn(int time, boolean isSecond);
    }
}