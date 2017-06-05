package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.util.List;

/**
 * Created by allen on 2016/3/11.
 */
public class TipsLayout extends RelativeLayout implements View.OnClickListener{
    private View tipsContent;
    private ImageView clickMe;
    private TextView tipsText;
    private List<String> tipsList;
    private int currentIndex = -1;//-1表示睡眠状态

    public TipsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = View.inflate(context, R.layout.tips_layout, this);
        tipsContent = view.findViewById(R.id.tipsContent);
        clickMe = (ImageView) view.findViewById(R.id.clickMe);
        clickMe.setOnClickListener(this);
        tipsText = (TextView) view.findViewById(R.id.tipsText);
        tipsText.setOnClickListener(this);
    }

    /**
     * 设置待显示的tips列表
     * @param tipsList 待显示的tips列表
     */
    public void setTipsList(List<String> tipsList) {
        if (tipsList == null || tipsList.isEmpty()) {
            tipsContent.setVisibility(GONE);
            return;
        }
        this.tipsList = tipsList;
        tipsContent.setVisibility(VISIBLE);
    }

    /**
     * 显示tips
     */
    public void showTips() {
        if (currentIndex == -1) {
            currentIndex = 0;
        }
        showTips(currentIndex, currentIndex);
    }

    /**
     * 显示某条tips
     * @param index
     */
    public void showTips(int index) {
        showTips(index, index);
    }

    /**
     * 显示多条tips
     * @param start 开始序号的tips
     * @param end 结束序号的tips
     */
    public void showTips(int start, int end) {
        if (tipsList == null || tipsList.isEmpty()) {
            return;
        }
        int size = tipsList.size();
        if (start <= end && size > end) {
            String tips = "";
            for (int i = start; i < end+1; i++) {
                tips += tipsList.get(i) + "\n";
            }
            if (tips.length() > 0) {
                tips = tips.substring(0, tips.length() - 1);
            }
            currentIndex = end;
            tipsText.setText(tips);
            tipsText.setVisibility(VISIBLE);
            clickMe.setImageResource(R.drawable.pic_stand);
        }
    }

    /**
     * 进入睡眠状态
     */
    public void sleep() {
        if (tipsList == null || tipsList.isEmpty()) {
            return;
        }
        if (currentIndex == tipsList.size()-1) {
            currentIndex = -1;
        }
        tipsText.setVisibility(GONE);
        clickMe.setImageResource(R.drawable.pic_guide);
    }

    @Override
    public void onClick(View v) {
        if (tipsList == null || tipsList.isEmpty()) {
            return;
        }
        if(currentIndex == tipsList.size()-1) {
            currentIndex = -1;
            sleep();
        } else {
            ++currentIndex;
            showTips();
        }
    }
}
