package com.orvibo.homemate.smartscene;

import com.orvibo.homemate.data.IntelligentSceneConditionType;

/**
 * Created by huangqiyao on 2016/7/20 18:16.
 *
 * @version v1.10
 */
public class SmartSceneTool {

    /**
     * 判断场景跟服务器通信还是主机通信
     *
     * @param conditionType {@link IntelligentSceneConditionType}
     * @return true只跟服务器通信
     */
    public static boolean isOnlyServerOperate(int conditionType) {
        return conditionType == IntelligentSceneConditionType.CLICK_ALLONE;
    }
}
