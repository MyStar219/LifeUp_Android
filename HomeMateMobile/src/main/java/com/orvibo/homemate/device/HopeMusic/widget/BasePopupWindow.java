package com.orvibo.homemate.device.HopeMusic.widget;

import android.content.Context;
import android.widget.PopupWindow;

/**
 * Created by wuliquan on 2016/5/28.
 */
public  class BasePopupWindow extends PopupWindow {
    public BasePopupWindow(Context context) {
        super(context);
    }

    //改变音效
    public void initEffect(String effect){}
    //改变音源
     public void initSource(String source){}
    //改变音量
    public void initVoice(String voice){}

}
