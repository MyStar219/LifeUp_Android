package com.orvibo.homemate.device.HopeMusic.widget;

/**
 * Created by wuliquan on 2016/5/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;
import com.orvibo.homemate.device.HopeMusic.listener.OnCmdSendListener;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;

public class SelectSourcePopupWindow extends BasePopupWindow implements OnClickListener {
    private View mMenuView;
    private LinearLayout source_ll;
    private DevicePlayState devicePlayState;
    private OnCmdSendListener onCmdSendListener;
    public SelectSourcePopupWindow(Activity context, DevicePlayState devicePlayState, OnCmdSendListener onCmdSendListener) {
        super(context);
        this.devicePlayState= devicePlayState;
        this.onCmdSendListener = onCmdSendListener;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.pop_set_source_bottom, null);
        source_ll = (LinearLayout)mMenuView.findViewById(R.id.source_ll);
        setListener(source_ll);

        if(devicePlayState!=null) {
            setCheckIndex(Integer.parseInt(devicePlayState.getSource()));
        }

        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.take_photo_anim);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

    }
    @Override
    public void onClick(View view) {
        if(onCmdSendListener!=null&&devicePlayState!=null) {
            String tag = (String) view.getTag();
            Song song = new Song();
            song.setDeviceId(devicePlayState.getDeviceId());
            song.setSource(tag);
            onCmdSendListener.sendCmd(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEDSOURCE, false, song);
        }
    }

    @Override
    public void initSource(String source) {
        setCheckIndex(Integer.parseInt(source));
    }

    public void setCheckIndex(int index){
        setImgSelect(index+"");
    }
    private void setListener(LinearLayout viewGroup){
        if(viewGroup!=null){
            int size = viewGroup.getChildCount();
            for(int i=0;i<size;i++){
                View child= viewGroup.getChildAt(i);
                if(child instanceof LinearLayout){
                    View img = ((LinearLayout) child).getChildAt(0);
                    if (img instanceof ThreeStateImageView)
                        child.setOnClickListener(this);
                }
            }
        }
    }
    private void setImgSelect(String index){
        if(index==null){
            return;
        }
        if(source_ll!=null){
            int size = source_ll.getChildCount();
            for(int i=0;i<size;i++){
                   View child= source_ll.getChildAt(i);
                if(child instanceof LinearLayout){
                    View img = ((LinearLayout) child).getChildAt(0);
                    if (img instanceof ThreeStateImageView) {
                        String tag= (String) img.getTag();
                        if (tag!=null) {
                            if (tag.equals(index)) {
                                ((ThreeStateImageView) img).setCheck(true);
                            } else {
                                ((ThreeStateImageView) img).setCheck(false);
                            }
                        }
                    }
                }
            }
        }
    }


}