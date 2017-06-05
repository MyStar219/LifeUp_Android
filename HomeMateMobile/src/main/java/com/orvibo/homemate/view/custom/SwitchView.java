package com.orvibo.homemate.view.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.util.PhoneUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smagret
 * @date 2016/1/26
 * 开关View
 */
public class SwitchView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = SwitchView.class.getSimpleName();
    private LinearLayout switchLinearLayout;
    private CheckBox     openCheckBox, closeCheckBox;

    private Context                 mContext;
    private OnSwitchCheckedListener onSwitchCheckedListener;
 //   private List<CheckBox> weekCheckBoxes = new ArrayList<CheckBox>();

    public SwitchView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_switch, this, true);
        findViews();
        init();
    }

    public SwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_switch, this, true);
        findViews();
        init();
    }

    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }


    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    private void findViews() {
        switchLinearLayout = (LinearLayout) findViewById(R.id.switchLinearLayout);
        openCheckBox = (CheckBox) findViewById(R.id.openCheckBox);
        closeCheckBox = (CheckBox) findViewById(R.id.closeCheckBox);

    }

    private void init() {
        openCheckBox.setOnCheckedChangeListener(this);
        closeCheckBox.setOnCheckedChangeListener(this);
//        weekCheckBoxes.add(openCheckBox);
//        weekCheckBoxes.add(closeCheckBox);
    }

    public void refresh(int status, boolean checkable) {
        setCheckable(checkable);
        setCheckStatus(status);
    }

    public void setCheckable(boolean checkable) {
        if (checkable) {
            switchLinearLayout.setBackgroundResource(R.color.white);
            openCheckBox.setEnabled(true);
            closeCheckBox.setEnabled(true);
        } else {
            switchLinearLayout.setBackgroundResource(R.color.transparent);
            openCheckBox.setEnabled(false);
            closeCheckBox.setEnabled(false);
        }
    }

    private void setCheckStatus(int status) {
        if (status == DeviceStatusConstant.ON) {
            openCheckBox.setChecked(true);
        } else if (status == DeviceStatusConstant.OFF) {
            closeCheckBox.setChecked(true);
        }
    }

    public void setSwitchParm(Activity activity) {
        int dipPadding = (int) mContext.getResources().getDimension(R.dimen.padding_x4);
        int pxPadding = PhoneUtil.dip2px(mContext, dipPadding);
        int[] size = PhoneUtil.getScreenPixels(activity);
        int width = ((size[0] - 2 * pxPadding + 10) / 7) * 2;
        switchLinearLayout.setLayoutParams(new LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setOpenCheckBoxText(String content) {
        openCheckBox.setText(content);
    }

    public void setCloseCheckBoxText(String content) {
        closeCheckBox.setText(content);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //TODO
//        openCheckBox.setBackgroundResource(R.drawable.time_repeat_bg);
//        closeCheckBox.setBackgroundResource(R.drawable.time_repeat_bg);

        if (!openCheckBox.isChecked() && !closeCheckBox.isChecked()) {
            buttonView.setChecked(true);
        } else {
            String tag = (String) buttonView.getTag();
            String open = mContext.getResources().getString(R.string.action_open);
            String close = mContext.getResources().getString(R.string.action_close);
            if (tag.equals(open) && isChecked) {
//                openCheckBox.setChecked(true);
                if (isChecked) {
                    openCheckBox.setBackgroundColor(getResources().getColor(R.color.green));
                    closeCheckBox.setBackgroundColor(getResources().getColor(R.color.bg_white_gray));
                    openCheckBox.setTextColor(getResources().getColor(R.color.white));
                    closeCheckBox.setTextColor(getResources().getColor(R.color.gray));

                }
//                else {
//                    openCheckBox.setTextColor(getResources().getColor(R.color.gray));
//                    closeCheckBox.setTextColor(getResources().getColor(R.color.white));
//                }
                closeCheckBox.setChecked(false);

            } else if (tag.equals(close) && isChecked) {
//                closeCheckBox.setChecked(true);
                if (isChecked) {
                    openCheckBox.setBackgroundColor(getResources().getColor(R.color.bg_white_gray));
                    closeCheckBox.setBackgroundColor(getResources().getColor(R.color.green));
                    closeCheckBox.setTextColor(getResources().getColor(R.color.white));
                    openCheckBox.setTextColor(getResources().getColor(R.color.gray));
                }
//                else {
//                    closeCheckBox.setTextColor(getResources().getColor(R.color.gray));
//                    openCheckBox.setTextColor(getResources().getColor(R.color.white));
//                }
                openCheckBox.setChecked(false);
            }

            if (openCheckBox.isChecked() && !closeCheckBox.isChecked()) {
                onSwitchCheckedListener.onSwitchOpened();
            } else if (!openCheckBox.isChecked() && closeCheckBox.isChecked()) {
                onSwitchCheckedListener.onSwitchClosed();
            }
        }
    }

    public void setOnSwitchCheckedListener(OnSwitchCheckedListener onSwitchCheckedListener) {
        this.onSwitchCheckedListener = onSwitchCheckedListener;
    }

    public interface OnSwitchCheckedListener {
        void onSwitchOpened();

        void onSwitchClosed();
    }
}