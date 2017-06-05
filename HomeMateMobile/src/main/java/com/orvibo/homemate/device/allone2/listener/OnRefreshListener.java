package com.orvibo.homemate.device.allone2.listener;

import com.kookong.app.data.api.IrData;
import com.orvibo.homemate.bo.Action;

/**
 * Created by snown on 2016/4/5.
 *
 * @描述: allone匹配遥控器界面刷新回调接口
 */
public interface OnRefreshListener {
    void onRefresh(IrData irData);

    void onRefresh(Action action);

}
