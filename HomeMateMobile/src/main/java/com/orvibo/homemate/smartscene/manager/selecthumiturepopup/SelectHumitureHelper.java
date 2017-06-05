package com.orvibo.homemate.smartscene.manager.selecthumiturepopup;

import android.content.Context;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.data.SmartSceneConstant;
import com.orvibo.homemate.sharedPreferences.CommonCache;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keeu on 2016/3/10.
 */
public class SelectHumitureHelper {

    /**
     * 通过温度得到对应的position
     *
     * @param condition
     * @return
     */
    public static int getPositionByTemperature(LinkageCondition condition) {
        int pos = 20;
        if(!CommonCache.isCelsius()) {
            pos = 36;
        }
        if (condition != null) {
            if (CommonCache.isCelsius()) {
                pos = 50 - condition.getValue();
            } else {
                pos = 122 - MathUtil.geFahrenheitData(condition.getValue());
            }
        }
        return pos;
    }

    /**
     * 通过position得到对应的温度
     *
     * @param pos
     * @return
     */
    public static int getTemperatureByPosition(int pos) {
        int value;
            if(CommonCache.isCelsius()) {
                if (pos < 0) {
                    value = 50;
                } else if (pos > 60) {
                    value = -10;
                } else {
                    value = 50 - pos;
                }
            } else {
                if (pos < 0) {
                    value = 122;
                } else if (pos > 108) {
                    value = 14;
                } else {
                    value = MathUtil.getCelsiusData(122 - pos);
                }
            }
        return value;
    }

    /**
     * 通过湿度得到对应的position
     *
     * @param condition
     * @return
     */
    public static int getPositionByHumidity(LinkageCondition condition) {
        int pos = 2;
        if (condition != null) {
            pos = 20 - condition.getValue() / 5;
        }
        return pos;
    }

    /**
     * 通过position得到对应的温度
     *
     * @param pos
     * @return
     */
    public static int getHumidityByPosition(int pos) {
        int value;
        if (pos < 0) {
            value = 100;
        } else if (pos > 20) {
            value = 0;
        } else {
            value = 100 - pos * 5;
        }
        return value;
    }

    public static int getPosByCondition(LinkageCondition condition) {
        int pos = 0;
        if (condition != null) {
            int c = condition.getCondition();
            if (c == 3) {
                pos = 0;
            } else {
                pos = 1;
            }
        }
        return pos;
    }

    public static List<WheelBo> getTemperatureCondition(Context context) {
        List<WheelBo> bos = new ArrayList<>();
        WheelBo wheelBo = new WheelBo();
        wheelBo.setName(context.getResources().getString(R.string.smart_scene_condition_exceed));
        wheelBo.setId(SmartSceneConstant.Condition.GREATER_THAN_EQUAL + "");
        bos.add(wheelBo);

        wheelBo = new WheelBo();
        wheelBo.setName(context.getResources().getString(R.string.smart_scene_condition_under));
        wheelBo.setId(SmartSceneConstant.Condition.LESS_THAN_EQUAL + "");
        bos.add(wheelBo);
        return bos;
    }

    /**
     * 获取温度所有值
     *
     * @param context
     * @return
     */
    public static List<WheelBo> getTemperatureValues(Context context) {
        List<WheelBo> bos = new ArrayList<>();
        final String unit = CommonCache.getTemperatureUnit();
        if(CommonCache.isCelsius()) {
            for (int i = 50; i >= -10; i--) {
                WheelBo wheelBo = new WheelBo();
                wheelBo.setName(i + unit);
                wheelBo.setId((50 - i) + "");
                bos.add(wheelBo);
            }
        } else {
            for (int i = 122; i >= 14; i--) {
                WheelBo wheelBo = new WheelBo();
                wheelBo.setName(i + unit);
                wheelBo.setId((122 - i) + "");
                bos.add(wheelBo);
            }
        }
        return bos;
    }

    /**
     * 获取湿度值
     *
     * @param context
     * @return
     */
    public static List<WheelBo> getHumidityValues(Context context) {
        List<WheelBo> bos = new ArrayList<>();
        final String unit = "%";
        for (int i = 100; i >= 0; i--) {
            if (i % 5 == 0) {
                WheelBo wheelBo = new WheelBo();
                wheelBo.setName(i + unit);
                wheelBo.setId((i - 100) + "");
                bos.add(wheelBo);
            }
        }
        return bos;
    }
}
