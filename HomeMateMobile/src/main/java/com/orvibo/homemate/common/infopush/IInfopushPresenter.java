package com.orvibo.homemate.common.infopush;

/**
 * Created by huangqiyao on 2016/7/20 22:22.
 *
 * @version v1.10
 */
public interface IInfopushPresenter {
    /**
     * Activity走到onResume()函数
     */
    void onActivityResume();

    void onRegister();

    void onUnRegister();
}
