package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.WheelLockAdapter;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;
import com.orvibo.homemate.view.custom.wheelview.WheelView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yuwei on 2016/6/30.
 */
public class MyDatePickerPopup extends CommonPopup implements View.OnClickListener,TosGallery.OnEndFlingListener{

    private long initDateTime;
    private Context mContext;
    private LinearLayout wheel_ll;
    private TextView cancel_tv;
    private WheelView yearView, monthView, dayView;
    private List<Integer> yearList = new ArrayList<>();
    private List<Integer> monthList = new ArrayList<>();
    private List<Integer> dayList = new ArrayList<>();

    private SelectDayListener mSelectDayListener;
    private WheelLockAdapter dayAdapter;

    public MyDatePickerPopup(Context context, long initDateTime) {
        this.mContext = context;
        this.initDateTime = initDateTime;
    }

    private void initDatas() {
        long ms = System.currentTimeMillis();
        //初始年
        int year1 = Integer.parseInt(TimeUtil.getYear(ms));
        if (initDateTime != 0)
            ms = initDateTime;
        //选择后年
        int year2 = Integer.parseInt(TimeUtil.getYear(ms));
        int month1 = Integer.parseInt(TimeUtil.getMonth(ms));
        int day1 = Integer.parseInt(TimeUtil.getDay(ms));


        for (int pyear = 1970; pyear <= year1; pyear++) {
            yearList.add(pyear);
        }

        for (int i=1;i<=20;i++){
            yearList.add(year1+i);
        }

        for (int i = 1; i <= 12; i++) {
            monthList.add(i);
        }
        yearView.setAdapter(new WheelLockAdapter(mContext, yearList, mContext.getString(R.string.year)));
        yearView.setOnEndFlingListener(this);
        yearView.setSelection(yearList.indexOf(year2));

        monthView.setAdapter(new WheelLockAdapter(mContext, monthList, mContext.getString(R.string.month)));
        monthView.setOnEndFlingListener(this);
        monthView.setSelection(monthList.indexOf(month1));

        int days = getDayNum(yearList.get(0), month1);
        for (int i = 1; i <= days; i++) {
            dayList.add(i);
        }
        dayAdapter = new WheelLockAdapter(mContext, dayList, mContext.getString(R.string.day));
        dayView.setAdapter(dayAdapter);
        dayView.setOnEndFlingListener(this);
        dayView.setSelection(dayList.indexOf(day1));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismiss();
        }
    };


    public void dismissPopupDelay() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    public void show() {
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_my_datepicker, null);
        wheel_ll = (LinearLayout) contentView.findViewById(R.id.wheel_ll);
        yearView = (WheelView) contentView.findViewById(R.id.year);
        monthView = (WheelView) contentView.findViewById(R.id.month);
        dayView = (WheelView) contentView.findViewById(R.id.day);

        initDatas();
        inAnim();

        cancel_tv = (TextView) contentView.findViewById(R.id.cancel_tv);
        cancel_tv.setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);

        show(mContext, contentView, true);
    }

    private void inAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_in));
    }

    public void outAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_out));
    }

    public int getDayNum(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();    //在使用set方法之前，必须先clear一下，否则很多信息会继承自系统当前时间
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);  //Calendar对象默认一月为0
        int endday = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);//获得本月的天数
        return endday;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_tv:
                if (mSelectDayListener != null)
                    mSelectDayListener.selectDay(getSelectTime());
                outAnim();
                dismissPopupDelay();
                break;
            case R.id.cancel_tv:
                outAnim();
                dismissPopupDelay();
                break;
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }

    @Override
    public void onEndFling(TosGallery v) {
        if (v == yearView || v == monthView) {
            int days = getDayNum(yearList.get(yearView.getSelectedItemPosition()), monthList.get(monthView.getSelectedItemPosition()));
            dayList.clear();
            for (int i = 1; i <= days; i++) {
                dayList.add(i);
            }
            dayAdapter.updateData(dayList, mContext.getString(R.string.day));
        } else {
            v.setSelection(v.getSelectedItemPosition());
        }
    }

    public interface SelectDayListener{
        public void selectDay(int[] yearMonthDay);
    }

    public void setSelectDayListener(SelectDayListener selectDayListener) {
        mSelectDayListener = selectDayListener;
    }

    /**
     * 根据选择的控件获取时间戳
     *
     * @return
     */
    public int[] getSelectTime() {
        int[] yearMonthDay = new int[3];
        yearMonthDay[0] = yearList.get(yearView.getSelectedItemPosition());
        yearMonthDay[1] = monthList.get(monthView.getSelectedItemPosition());
        yearMonthDay[2] = dayList.get(dayView.getSelectedItemPosition());
        return yearMonthDay;
    }
}
