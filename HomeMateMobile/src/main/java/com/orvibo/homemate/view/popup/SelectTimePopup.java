package com.orvibo.homemate.view.popup;

import android.content.Context;
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
 * Created by yuwei on 2016/8/25.
 * 自定义时间选择器，精确到秒
 */
public class SelectTimePopup extends CommonPopup implements View.OnClickListener,TosGallery.OnEndFlingListener{

    private View wheel_ll;
    private WheelView hourView, minView, secView;
    private TextView title_tv;

    private List<Integer> hourList = new ArrayList<>();
    private List<Integer> minList = new ArrayList<>();
    private List<Integer> secList = new ArrayList<>();

    private OnTimeSelectListener mOnTimeSelectListener;

    public SelectTimePopup(Context context,OnTimeSelectListener onTimeSelectListener) {
        mCommonPopupContext = context;
        mOnTimeSelectListener = onTimeSelectListener;
        initData();
    }

    public void show(String title, int hour, int minute,int sec){
        View contentView = View.inflate(mCommonPopupContext, R.layout.popup_my_timepicker, null);
        title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        title_tv.setText(title);
        wheel_ll = contentView.findViewById(R.id.wheel_ll);
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mCommonPopupContext, R.anim.bottom_to_top_in));
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);
        hourView = (WheelView) contentView.findViewById(R.id.hour);
        minView = (WheelView) contentView.findViewById(R.id.min);
        secView = (WheelView) contentView.findViewById(R.id.sec);

        //由于时间单位另外用TextView来显示，所以单位传""
        hourView.setAdapter(new WheelLockAdapter(mCommonPopupContext, hourList, ""));
        hourView.setOnEndFlingListener(this);
        hourView.setSelection(hourList.indexOf(hour));

        minView.setAdapter(new WheelLockAdapter(mCommonPopupContext, minList,""));
        minView.setOnEndFlingListener(this);
        minView.setSelection(minList.indexOf(minute));

        secView.setAdapter(new WheelLockAdapter(mCommonPopupContext,secList,""));
        secView.setOnEndFlingListener(this);
        secView.setSelection(secList.get(sec));

        show(mCommonPopupContext, contentView, true);
    }

    /**
     * 初始化时间选择器的时分秒
     */
    private void initData(){
        for (int timevalue=0;timevalue<60;timevalue++){
            if (timevalue<24){
                hourList.add(timevalue);
            }
            minList.add(timevalue);
            secList.add(timevalue);
        }
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
                int selectHour = hourList.get(hourView.getSelectedItemPosition());
                int seletMin = minList.get(minView.getSelectedItemPosition());
                int selectSec = secList.get(secView.getSelectedItemPosition());
                mOnTimeSelectListener.selectTime(selectHour,seletMin,selectSec);
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

    /**
     * 时间选择的回调接口
     */
    public interface OnTimeSelectListener{
        public void selectTime(int hour,int min,int sec);
    }
}
