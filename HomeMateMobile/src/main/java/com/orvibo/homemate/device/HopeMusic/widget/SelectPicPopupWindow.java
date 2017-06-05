package com.orvibo.homemate.device.HopeMusic.widget;

/**
 * Created by yu on 2016/5/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.MusicConstant;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;

public class SelectPicPopupWindow extends BasePopupWindow implements OnClickListener{
    private Context mContext;
    private View mMenuView;
    private ImageButton mute_ibtn;
    private SeekBar voice_seekbar;
    private LinearLayout set_style_ll;
    private LinearLayout source_ll;
    private OnClickListener itemsOnClick;


    public SelectPicPopupWindow(Activity context, DevicePlayState devicePlayState, OnClickListener itemsOnClick, SeekBar.OnSeekBarChangeListener listener) {
        super(context);
        this.mContext = context;
        this.itemsOnClick=itemsOnClick;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.pop_set_bottom, null);
        set_style_ll=(LinearLayout) mMenuView.findViewById(R.id.set_style_ll);
        set_style_ll.setOnClickListener(this);
        source_ll = (LinearLayout) mMenuView.findViewById(R.id.source_ll);
        mute_ibtn = (ImageButton)mMenuView.findViewById(R.id.mute_ibtn);
        voice_seekbar = (SeekBar)mMenuView.findViewById(R.id.voice_seekbar);
        source_ll.setOnClickListener(this);

        if(devicePlayState!=null) {
            initEffect(devicePlayState.getEffect());
            initSource(devicePlayState.getSource());
            //这里乘以10是为了滑动顺溜
            voice_seekbar.setMax(Integer.parseInt(devicePlayState.getMaxVol())*10);
            voice_seekbar.setProgress(Integer.parseInt(devicePlayState.getCurrentVol())*10);
        }



        voice_seekbar.setOnSeekBarChangeListener(listener);
        mute_ibtn.setOnClickListener(this);
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
    public void initVoice(String voice){
        voice_seekbar.setProgress(Integer.parseInt(voice)*10);
    }
    @Override
    public void initEffect(String effect){
        switch (effect){
            case MusicConstant.EFFECT_0:
                setImageViewAndText(set_style_ll, R.drawable.bg_classical,mContext.getString(R.string.effect_classical));
                break;
            case MusicConstant.EFFECT_1:
                setImageViewAndText(set_style_ll, R.drawable.bg_modern,mContext.getString(R.string.effect_modern));
                break;
            case MusicConstant.EFFECT_2:
                setImageViewAndText(set_style_ll, R.drawable.bg_acid_rock,mContext.getString(R.string.effect_rock));
                break;
            case MusicConstant.EFFECT_3:
                setImageViewAndText(set_style_ll, R.drawable.bg_popular,mContext.getString(R.string.effect_pop));
                break;
            case MusicConstant.EFFECT_4:
                setImageViewAndText(set_style_ll, R.drawable.bg_dance,mContext.getString(R.string.effect_dance));
                break;
            case MusicConstant.EFFECT_5:
                setImageViewAndText(set_style_ll, R.drawable.bg_original_song,mContext.getString(R.string.effect_original));
                break;
        }
    }

    @Override
    public void initSource(String source){
        switch (source){
            case MusicConstant.SOURCE_1:
                setImageViewAndText(source_ll, R.drawable.icon_local_checked,mContext.getString(R.string.source_local));
                break;
            case MusicConstant.SOURCE_2:
                setImageViewAndText(source_ll, R.drawable.icon_circumscribed_checked,mContext.getString(R.string.source_other));
                break;
            case MusicConstant.SOURCE_3:
                setImageViewAndText(source_ll, R.drawable.icon_bluetooth_checked,mContext.getString(R.string.source_bluetooth));
                break;

        }
    }
    private void setImageViewAndText(ViewGroup viewGroup, int id, String text){
        View imageView = (ImageView) viewGroup.getChildAt(0);
        View textView = (TextView) viewGroup.getChildAt(1);
        if(imageView instanceof ImageView){
            ((ImageView) imageView).setImageResource(id);
        }
        if(textView instanceof TextView){
            ((TextView) textView).setText(text);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.set_style_ll:
            case R.id.source_ll:
                this.dismiss();
                if(itemsOnClick!=null)
                    itemsOnClick.onClick(view);
                break;
            case R.id.mute_ibtn:
                if(voice_seekbar!=null){
                    if(itemsOnClick!=null) {
                        itemsOnClick.onClick(view);
                        voice_seekbar.setProgress(0);
                    }
                }
                break;
        }
    }
}