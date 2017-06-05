package com.orvibo.homemate.device.allone2.listener;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.view.custom.IrKeyButton;

/**
 * Created by snown on 2016/4/5.
 *
 * @描述: 按键点击事件
 */
public interface OnKeyClickListener {
    void OnClick(IrKeyButton irKeyButton);

    void onTvSelected(Device device);

    void onTvClick(KKIr kkIr,IrKeyButton irKeyButton);
}
