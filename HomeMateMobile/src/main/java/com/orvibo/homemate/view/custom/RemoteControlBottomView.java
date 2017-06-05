package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.smartgateway.app.R;

/**
 * Created by yuwei on 2016/3/29.
 * 遥控器底部自定义控件
 */
public class RemoteControlBottomView extends LinearLayout implements View.OnClickListener {

    private Context mContext;

    private Button bt_remote_control_left, bt_remote_control_middle, bt_remote_control_right;

    private int viewpagerSize;//页面总数
    private int index;//当前显示页面的索引

    private RemoteControlBottomBtnClickListener remoteControlBottomBtnClickListener;

    public RemoteControlBottomView(Context context) {
        super(context);
        init(context);
        initListener();
    }

    public RemoteControlBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initListener();
    }

    public RemoteControlBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initListener();
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.remote_control_bottom_view, this, true);
        bt_remote_control_left = (Button) findViewById(R.id.bt_remote_control_left);
        bt_remote_control_middle = (Button) findViewById(R.id.bt_remote_control_middle);
        bt_remote_control_right = (Button) findViewById(R.id.bt_remote_control_right);
    }

    private void initListener() {
        bt_remote_control_left.setOnClickListener(this);
        bt_remote_control_middle.setOnClickListener(this);
        bt_remote_control_right.setOnClickListener(this);
    }

    public void initData(int pageSize, RemoteControlBottomBtnClickListener remoteControlBottomBtnClickListener) {
        this.viewpagerSize = pageSize;
        this.remoteControlBottomBtnClickListener = remoteControlBottomBtnClickListener;
        index = 0;
        if (pageSize <= 0) {
            //pageSize小于等于0的时候隐藏底部控件
            this.setVisibility(View.GONE);
        } else if (pageSize == 1) {
            //pageSize等于1，只有一个遥控器，左侧按钮隐藏，右侧按钮变成“都不合适？”
            this.setVisibility(View.VISIBLE);
            bt_remote_control_left.setVisibility(View.INVISIBLE);
            bt_remote_control_middle.setVisibility(View.VISIBLE);
            bt_remote_control_right.setVisibility(View.VISIBLE);
            bt_remote_control_right.setText(R.string.null_btn_right);
            bt_remote_control_right.setCompoundDrawables(null, null, null, null);
        } else if (viewpagerSize == 2) {
            this.setVisibility(View.VISIBLE);
            bt_remote_control_left.setVisibility(View.VISIBLE);
            bt_remote_control_middle.setVisibility(View.VISIBLE);
            bt_remote_control_right.setVisibility(View.VISIBLE);
            //pageSize等于2，左侧按钮显示第一个，右侧按钮显示“都不合适”，选中的是第二个（也是最后一个）
            bt_remote_control_left.setText(mContext.getString(R.string.remote_control_index, "1"));
            bt_remote_control_right.setText(R.string.null_btn_right);
            bt_remote_control_right.setCompoundDrawables(null, null, null, null);
            index = 1;
        } else {
            this.setVisibility(View.VISIBLE);
            bt_remote_control_left.setVisibility(View.VISIBLE);
            bt_remote_control_middle.setVisibility(View.VISIBLE);
            bt_remote_control_right.setVisibility(View.VISIBLE);
            //pageSize>=3,左侧按钮显示第一个，选中第二个，右侧按钮显示“第三个”
            bt_remote_control_left.setText(mContext.getString(R.string.remote_control_index, "1"));
            bt_remote_control_right.setText(mContext.getString(R.string.remote_control_index, "3"));
            index = 1;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_remote_control_left:
                preViewClick();
                remoteControlBottomBtnClickListener.preView();
                break;
            case R.id.bt_remote_control_middle:
                remoteControlBottomBtnClickListener.middleButtonClick();
                break;
            case R.id.bt_remote_control_right:
                remoteControlBottomBtnClickListener.nextView();
                break;
        }
    }

    public void preViewClick() {
        //当前显示Fragment索引减1
        index--;
        if (index == 0) {
            //viewpager显示了第一个Fragment，左侧按钮隐藏，右侧按钮显示“第二个”
            bt_remote_control_left.setVisibility(View.INVISIBLE);
            bt_remote_control_right.setVisibility(View.VISIBLE);
            bt_remote_control_right.setText(mContext.getString(R.string.remote_control_index, "2"));
        } else if (index >= 1) {
            //viewpager显示上一个Fragment，左侧按钮显示第index-1个，右侧按钮显示“第index+1个”
            bt_remote_control_left.setText(mContext.getString(R.string.remote_control_index, (index) + ""));
            bt_remote_control_right.setText(mContext.getString(R.string.remote_control_index, (index + 2) + ""));
        }
        Drawable leftClickDrawable = mContext.getResources().getDrawable(R.drawable.remote_control_bottom_right_selector);
        bt_remote_control_right.setCompoundDrawablesWithIntrinsicBounds(null, null, leftClickDrawable, null);
    }

    /**
     * 获取当前索引
     *
     * @return
     */
    public int getIndex() {
        return index;
    }

    public interface RemoteControlBottomBtnClickListener {
        //上一个View
        void preView();

        //中间按钮点击接口
        void middleButtonClick();

        //下一个View
        void nextView();

    }



    /**
     * 底部选择框界面显示
     */
    public void showNext() {
            index++;
            bt_remote_control_left.setVisibility(View.VISIBLE);
            bt_remote_control_left.setText(mContext.getString(R.string.remote_control_index, index + ""));
            if (index + 1 < viewpagerSize) {
                bt_remote_control_right.setText(mContext.getString(R.string.remote_control_index, (index + 2) + ""));
                Drawable rightClickDrawable = mContext.getResources().getDrawable(R.drawable.remote_control_bottom_right_selector);
                bt_remote_control_right.setCompoundDrawablesWithIntrinsicBounds(null, null, rightClickDrawable, null);
            } else if (index + 1 == viewpagerSize) {
                bt_remote_control_right.setText(R.string.null_btn_right);
                bt_remote_control_right.setCompoundDrawables(null, null, null, null);
            }
    }

}
