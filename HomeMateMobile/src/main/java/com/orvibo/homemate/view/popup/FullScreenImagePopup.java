package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartgateway.app.R;
import com.orvibo.homemate.util.PopupWindowUtil;

/**
 * Created by yuwei on 2016/7/26.
 * 图片全屏显示popup
 */
public class FullScreenImagePopup implements View.OnClickListener{

    private PopupWindow mPopupWindow;

    public FullScreenImagePopup(){

    }

    public void show(Activity context, String path){
        PopupWindowUtil.disPopup(mPopupWindow);
        View view = LayoutInflater.from(context).inflate(R.layout.popup_fullscreen_image, null);
        view.setOnClickListener(this);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_full_screen_image);
        ImageLoader.getInstance().displayImage("file://"+path,imageView);
        imageView.setOnClickListener(this);
        mPopupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        PopupWindowUtil.initPopup(mPopupWindow,context.getResources().getDrawable(R.color.popup_bg), 1);
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        PopupWindowUtil.disPopup(mPopupWindow);
    }
}
