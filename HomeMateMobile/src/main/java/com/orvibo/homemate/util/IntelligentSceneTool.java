package com.orvibo.homemate.util;

import android.content.Context;
import android.text.TextUtils;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.Linkage;
import com.orvibo.homemate.bo.LinkageCondition;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.dao.LinkageConditionDao;
import com.orvibo.homemate.data.AuthorizedIdType;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.data.KeyAction;
import com.orvibo.homemate.data.KeyNo;
import com.orvibo.homemate.data.SmartSceneConstant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Smagret
 * @date 2015/10/16
 */
public class IntelligentSceneTool {
    private static final String TAG = "IntelligentSceneTool";

    /**
     * 根据IntelligentSceneConditionType得到对应的img res id。这些图片用于智能场景条件选择界面。
     *
     * @param conditionType
     * @param isAble        是否可用
     * @return
     */
    public static int getConditionIconResId(int conditionType, boolean isAble) {
        int resId = 0;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            if (isAble) {
                resId = R.drawable.icon_manual_click;
            } else {
                resId = R.drawable.icon_manual_click_disabled;
            }
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.drawable.icon_lock_on;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.drawable.icon_lock_off;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.drawable.icon_door_sensor_on;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.drawable.icon_door_sensor_off;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.drawable.icon_human_body_sensor;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.drawable.icon_temperature;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.drawable.icon_water;
        } else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            resId = R.drawable.icon_allone2;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            if (isAble) {
                resId = R.drawable.icon_add;
            } else {
                resId = R.drawable.icon_add_disablerd;
            }
        }
        return resId;
    }

    /**
     * 根据IntelligentSceneConditionType得到对应的img res id。这些图片用于智能场景条件选择界面。
     * 智能场景列表大图标里面的小图标
     *
     * @param conditionType
     * @param isAble        是否可用
     * @return
     */
    public static int getConditionIconResIdInList(int conditionType, boolean isAble) {
        int resId = 0;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            if (isAble) {
                resId = R.drawable.icon_linkage_click;
            } else {
                resId = R.drawable.icon_linkage_click;
            }
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.drawable.icon_linkage_unlock;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.drawable.icon_linkage_lock;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.drawable.icon_linkage_magnetic_unlock;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.drawable.icon_linkage_magnetic_lock;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.drawable.icon_linkage_human;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.drawable.icon_scene_temperature_white;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.drawable.icon_scene_water_white;
        } else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            resId = R.drawable.icon_linkage_click_allone_w;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            if (isAble) {
                resId = R.drawable.white_add_n;
            } else {
                resId = R.drawable.white_add_n;
            }
        }
        return resId;
    }

    /**
     * 根据IntelligentSceneConditionType得到对应的img res id。这些图片用于智能场景条件选择界面。
     *
     * @param conditionType
     * @param isAble        是否可用
     * @return
     */
    public static int getConditionIconResIdInList2stop(int conditionType, boolean isAble) {
        int resId = 0;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            if (isAble) {
                resId = R.drawable.icon_linkage_click;
            } else {
                resId = R.drawable.icon_linkage_click;
            }
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.drawable.icon_scene_linkage_unlock_gray;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.drawable.icon_linkage_lock;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.drawable.icon_scene_linkage_magnetic_unlock_gray;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.drawable.icon_scene_linkage_magnetic_lock_gray;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.drawable.icon_scene_linkage_human_gray;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.drawable.icon_scene_temperature_gray;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.drawable.icon_scene_water_gray;
        } else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            resId = R.drawable.icon_linkage_click_allone_gray;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            if (isAble) {
                resId = R.drawable.white_add_n;
            } else {
                resId = R.drawable.white_add_n;
            }
        }
        return resId;
    }

    /**
     * 根据IntelligentSceneConditionType得到对应的img res id。这些图片用于智能场景条件选择界面。
     * 智能场景列表中场景名称下面的小图标(激活状态)
     *
     * @param conditionType
     * @param isAble        是否可用
     * @return
     */
    public static int getConditionIconResIdInList2start(int conditionType, boolean isAble) {
        int resId = 0;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            if (isAble) {
                resId = R.drawable.icon_linkage_click;
            } else {
                resId = R.drawable.icon_linkage_click;
            }
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.drawable.icon_scene_linkage_unlock_y;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.drawable.icon_linkage_lock;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.drawable.icon_scene_linkage_magnetic_unlock_y;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.drawable.icon_scene_linkage_magnetic_lock_y;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.drawable.icon_scene_linkage_human_y;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.drawable.icon_scene_temperature_y;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.drawable.icon_scene_water_y;
        }else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            resId = R.drawable.icon_linkage_click_allone_y;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            if (isAble) {
                resId = R.drawable.white_add_n;
            } else {
                resId = R.drawable.white_add_n;
            }
        }
        return resId;
    }

    /**
     * sceneType
     *
     * @param conditionType
     * @return
     */
    public static int getConditionTypeNameResId(int conditionType) {
        int resId = R.string.app_name;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            resId = R.string.intelligent_scene_clicked;
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.string.intelligent_scene_lock_opened;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.string.intelligent_scene_lock_closed;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.string.intelligent_scene_door_sensor_opened;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.string.intelligent_scene_door_sensor_closed;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.string.intelligent_scene_human_body_sensor_triggered;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.string.intelligent_scene_temp_condition;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.string.intelligent_scene_humidity_condition;
        } else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            //轻按小方
            resId = R.string.intelligent_scene_allone;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            resId = R.string.intelligent_scene_add_condition;
        }
        return resId;
    }

    /**
     * deviceType
     *
     * @param deviceType
     * @return
     */
    public static int getSecurityConditionTypeNameResId(int deviceType) {
        int resId = R.string.app_name;
        if (deviceType == DeviceType.MAGNETIC || deviceType == DeviceType.MAGNETIC_WINDOW || deviceType == DeviceType.MAGNETIC_DRAWER || deviceType == DeviceType.MAGNETIC_OTHER) {
            resId = R.string.intelligent_scene_door_sensor_opened2;
        } else if (deviceType == DeviceType.INFRARED_SENSOR) {
            resId = R.string.intelligent_scene_human_body_sensor_triggered;
        } else if (deviceType == DeviceType.SMOKE_SENSOR) {
            resId = R.string.intelligent_scene_smoke;
        } else if (deviceType == DeviceType.CO_SENSOR) {
            resId = R.string.intelligent_scene_co;
        } else if (deviceType == DeviceType.FLAMMABLE_GAS) {
            resId = R.string.intelligent_scene_flammable;
        } else if (deviceType == DeviceType.WATER_SENSOR) {
            resId = R.string.intelligent_scene_water;
        } else if (deviceType == DeviceType.SOS_SENSOR) {
            resId = R.string.intelligent_scene_sos;
        }
        return resId;
    }

    /**
     * deviceType
     *
     * @param deviceType
     * @return
     */
    public static int getSecurityConditionIconResId(int deviceType) {
        int resId = 0;
        if (deviceType == DeviceType.MAGNETIC || deviceType == DeviceType.MAGNETIC_WINDOW || deviceType == DeviceType.MAGNETIC_DRAWER || deviceType == DeviceType.MAGNETIC_OTHER) {
            resId = R.drawable.icon_door_sensor_on;
        } else if (deviceType == DeviceType.INFRARED_SENSOR) {
            resId = R.drawable.icon_human_body_sensor;
        } else if (deviceType == DeviceType.SMOKE_SENSOR) {
            resId = R.drawable.icon_smoke;
        } else if (deviceType == DeviceType.CO_SENSOR) {
            resId = R.drawable.icon_co;
        } else if (deviceType == DeviceType.FLAMMABLE_GAS) {
            resId = R.drawable.icon_combustible;
        } else if (deviceType == DeviceType.WATER_SENSOR) {
            resId = R.drawable.icon_water;
        } else if (deviceType == DeviceType.SOS_SENSOR) {
            resId = R.drawable.icon_sos;
        }
        return resId;
    }

    public static int getMiniConditionTypeNameResId(int conditionType) {
        int resId = R.string.app_name;
        if (conditionType == IntelligentSceneConditionType.CLICKED) {
            resId = R.string.intelligent_scene_condition_mini_clicked;
        } else if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            resId = R.string.intelligent_scene_condition_mini_lock_opened;
        }
//        else if (conditionType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            resId = R.string.intelligent_scene_condition_mini_lock_closed;
//        }
        else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            resId = R.string.intelligent_scene_condition_mini_door_sensor_opened;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            resId = R.string.intelligent_scene_condition_mini_door_sensor_closed;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            resId = R.string.intelligent_scene_condition_mini_human_body_sensor_triggered;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            resId = R.string.intelligent_scene_condition_temp;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            resId = R.string.intelligent_scene_condition_humi;
        } else if (conditionType == IntelligentSceneConditionType.OTHER) {
            resId = R.string.intelligent_scene_condition_mini_add_condition;
        }
        return resId;
    }

    /**
     * 获取智能场景启动条件类型
     *
     * @param linkage
     * @return
     */
    public static int getConditionType(Linkage linkage) {
        if (linkage != null) {
            if ((Constant.INVALID_NUM + "").equals(linkage.getLinkageId())) {
                return IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED;
            }
            LinkageCondition linkageCondition;
            Device device;
            int linkageType;
            int statusType;
            int value;
            int deviceType;
            int conditionType = IntelligentSceneConditionType.OTHER;
            LinkageConditionDao linkageConditionDao = new LinkageConditionDao();
            List<LinkageCondition> linkageConditions =
                    linkageConditionDao.selLinkageConditionsByLinkageId(linkage.getLinkageId());
            final DeviceDao deviceDao = new DeviceDao();
            if (linkageConditions != null && linkageConditions.size() > 0) {
                Iterator<LinkageCondition> iterator = linkageConditions.iterator();
                while (iterator.hasNext()) {
                    linkageCondition = iterator.next();
                    linkageType = linkageCondition.getLinkageType();
                    statusType = linkageCondition.getStatusType();
                    value = linkageCondition.getValue();
                    if (linkageType == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                        //0：按照设备状态来触发
                        device = deviceDao.selDevice(linkageCondition.getDeviceId());
                        if (device != null) {
                            deviceType = device.getDeviceType();
                            if (deviceType == DeviceType.LOCK) {
                                if (value == 0) {
                                    conditionType = IntelligentSceneConditionType.LOCK_OPENED;
                                }
//                                    else {
//                                        conditionType = IntelligentSceneConditionType.LOCK_CLOSED;
//                                    }
                            } else if (deviceType == DeviceType.MAGNETIC
                                    || deviceType == DeviceType.MAGNETIC_WINDOW
                                    || deviceType == DeviceType.MAGNETIC_DRAWER
                                    || deviceType == DeviceType.MAGNETIC_OTHER) {
                                if (value == 0) {
                                    conditionType = IntelligentSceneConditionType.DOOR_SENSOR_CLOSED;
                                } else {
                                    conditionType = IntelligentSceneConditionType.DOOR_SENSOR_OPENED;
                                }

                            } else if (deviceType == DeviceType.INFRARED_SENSOR) {
                                conditionType = IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED;
                            } else if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
                                conditionType = IntelligentSceneConditionType.TEMPERATURE_SENSOR;
                            } else if (deviceType == DeviceType.HUMIDITY_SENSOR) {
                                conditionType = IntelligentSceneConditionType.HUMIDITY_SENSOR;
                            }
                        } else {
                            //TODO 触发联动的设备不存在
                        }
                        break;
                    } else if (linkageType == SmartSceneConstant.LinkageType.KEY_ACTION) {
//                        3：按照按键来触发。目前只有轻按小方支持场景 2016/7/16
                        conditionType = IntelligentSceneConditionType.CLICK_ALLONE;
                    }
                }
                return conditionType;
            } else {
                return IntelligentSceneConditionType.OTHER;
            }

        } else {
            return IntelligentSceneConditionType.OTHER;
        }
    }

    /**
     * 获取设备类型类型
     *
     * @param conditionType
     * @return
     */
    public static int getDeviceType(int conditionType) {
        int deviceType = Constant.INVALID_NUM;
        if (conditionType == IntelligentSceneConditionType.LOCK_OPENED) {
            deviceType = DeviceType.LOCK;
        } else if (conditionType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED
                || conditionType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            deviceType = DeviceType.FLAMMABLE_GAS;
        } else if (conditionType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            deviceType = DeviceType.INFRARED_SENSOR;
        } else if (conditionType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            deviceType = DeviceType.TEMPERATURE_SENSOR;
        } else if (conditionType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            deviceType = DeviceType.HUMIDITY_SENSOR;
        } else if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            deviceType = DeviceType.ALLONE;
        }
        return deviceType;
    }

    /**
     * @param linkageConditions
     */
    public static String getTimeInterval(Context context, List<LinkageCondition> linkageConditions) {
        String timeInterval = "";
        String startTime = "";
        String endTime = "";

        LinkageCondition linkageCondition;
        int linkageType;
        int condition;
        int value;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                condition = linkageCondition.getCondition();
                value = linkageCondition.getValue();
                if (linkageType == 1) {
                    if (condition == 3) {
                        startTime = TimeUtil.getTime24(context, value / 3600, (value % 3600) / 60);
                    } else if (condition == 4) {
                        endTime = TimeUtil.getTime24(context, value / 3600, (value % 3600) / 60);
                    }
                }
            }
        }

        if (TextUtils.isEmpty(startTime)) {
            startTime = "00:00";
        }
        if (TextUtils.isEmpty(endTime)) {
            endTime = "00:00";
        }
        String[] startTimeSplit = startTime.split(":");
        String[] endTimeSplit = endTime.split(":");
        if (startTimeSplit.length > 1 && endTimeSplit.length > 1) {
            int startHour = Integer.valueOf(startTimeSplit[0]);
            int startMinute = Integer.valueOf(startTimeSplit[1]);
            int endHour = Integer.valueOf(endTimeSplit[0]);
            int endMinute = Integer.valueOf(endTimeSplit[1]);
            if (startHour > endHour || startHour == endHour && startMinute > endMinute) {
                timeInterval = TimeUtil.getTime(context, startHour, startMinute) + context.getString(R.string.time_interval_to) + context.getString(R.string.time_interval_tomorrow) + TimeUtil.getTime(context, endHour, endMinute);
            } else if (endHour == startHour && endMinute == startMinute) {
                timeInterval = context.getString(R.string.time_interval_all_day);
            } else {
                timeInterval = TimeUtil.getTime(context, startHour, startMinute) + context.getString(R.string.time_interval_to) + TimeUtil.getTime(context, endHour, endMinute);
            }
        }

        return timeInterval;
    }

    /**
     * @param linkageConditions
     */
    public static int getWeek(List<LinkageCondition> linkageConditions) {
        int week = 255;
        LinkageCondition linkageCondition;
        int linkageType;
        int condition;
        int value;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                condition = linkageCondition.getCondition();
                value = linkageCondition.getValue();
                if (linkageType == 2) {
                    if (condition == 0) {
                        week = value;
                    }
                }
            }
        }
        return week;
    }

    /**
     * @param linkageConditions
     */
    public static String getDeviceId(List<LinkageCondition> linkageConditions) {
        String deviceId = Constant.NULL_DATA;
        LinkageCondition linkageCondition;
        int linkageType;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                if (linkageType == SmartSceneConstant.LinkageType.DEVICE_STATUS || linkageType == SmartSceneConstant.LinkageType.KEY_ACTION) {
                    deviceId = linkageCondition.getDeviceId();
                }
            }
        }
        return deviceId;
    }


    /**
     * @param conditonType
     * @return
     */
    public static int getValueByConditionType(int conditonType) {
        int value = Constant.INVALID_NUM;
        if (conditonType == IntelligentSceneConditionType.LOCK_OPENED) {
            //对于门锁，value1里面填写on/off属性值，0表示状态为开，填1表示状态为关
            value = 0;
        }
//        else if (conditonType == IntelligentSceneConditionType.LOCK_CLOSED) {
//            value = 1;
//        }
        else if (conditonType == IntelligentSceneConditionType.DOOR_SENSOR_OPENED) {
            //对于门磁、窗磁：value1填写0表示关闭，不需要报警，填写1表示打开，需要报警
            value = 1;
        } else if (conditonType == IntelligentSceneConditionType.DOOR_SENSOR_CLOSED) {
            value = 0;
        } else if (conditonType == IntelligentSceneConditionType.HUMAN_BODY_SENSOR_TRIGGERED) {
            //填写1表示检测到入侵
            value = 1;
        } else if (conditonType == IntelligentSceneConditionType.TEMPERATURE_SENSOR) {
            value = SmartSceneConstant.TEMPERATURE_DEFAULT_VALUE;
        } else if (conditonType == IntelligentSceneConditionType.HUMIDITY_SENSOR) {
            value = SmartSceneConstant.HUMIDITY_DEFAULT_VALUE;
        }
        return value;
    }

    public static LinkageCondition getLinkageCondition(Device selectDevice, String linkageId) {
        LinkageCondition linkageCondition = new LinkageCondition();

        int value1 = 1;
        int deviceType = selectDevice.getDeviceType();
        if (deviceType == DeviceType.MAGNETIC || deviceType == DeviceType.MAGNETIC_WINDOW
                || deviceType == DeviceType.MAGNETIC_DRAWER || deviceType == DeviceType.MAGNETIC_OTHER) {
            value1 = 1;
        } else if (deviceType == DeviceType.INFRARED_SENSOR) {
            value1 = 1;
        } else if (deviceType == DeviceType.SMOKE_SENSOR) {
            value1 = 1;
        } else if (deviceType == DeviceType.CO_SENSOR) {
            value1 = 1;
        } else if (deviceType == DeviceType.FLAMMABLE_GAS) {
            value1 = 1;
        } else if (deviceType == DeviceType.WATER_SENSOR) {
            value1 = 1;
        } else if (deviceType == DeviceType.SOS_SENSOR) {
            value1 = 1;
        }
        linkageCondition.setDeviceId(selectDevice.getDeviceId());
        linkageCondition.setValue(value1);
        linkageCondition.setLinkageId(linkageId);
        //根据设备状态来联动
        linkageCondition.setStatusType(SmartSceneConstant.StatusType.VALUE1);
        return linkageCondition;
    }

    /**
     * @param device
     * @param linkageConditions
     * @param selectDeviceId
     * @param value
     * @param conditionType     联动条件类型{@link IntelligentSceneConditionType}
     */
    public static List<LinkageCondition> getLinkageConditions(Device device, List<LinkageCondition> linkageConditions, String selectDeviceId, int value, int conditionType) {
        LinkageCondition linkageCondition;
        int linkageType;
        int condition;
        boolean hasLinkageStatus = false;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                condition = linkageCondition.getCondition();
                if (linkageType == SmartSceneConstant.LinkageType.DEVICE_STATUS) {
                    hasLinkageStatus = true;
                    String deviceId = linkageCondition.getDeviceId();
                    if (TextUtils.isEmpty(deviceId) || !deviceId.equals(selectDeviceId)) {
                        if (device != null) {
                            final int deviceType = device.getDeviceType();
                            if (deviceType == DeviceType.TEMPERATURE_SENSOR || deviceType == DeviceType.HUMIDITY_SENSOR) {
                                linkageCondition.setCondition(SmartSceneConstant.Condition.GREATER_THAN_EQUAL);
                                if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
                                    linkageCondition.setStatusType(SmartSceneConstant.StatusType.VALUE1);
                                } else {
                                    linkageCondition.setStatusType(SmartSceneConstant.StatusType.VALUE2);
                                }
                            } else {
                                linkageCondition.setCondition(SmartSceneConstant.Condition.EQUAL);
                                linkageCondition.setStatusType(SmartSceneConstant.StatusType.VALUE1);
                            }
                        }
                    }
                    linkageCondition.setDeviceId(selectDeviceId);
                    linkageCondition.setValue(value);
                } else if (linkageType == SmartSceneConstant.LinkageType.KEY_ACTION) {
                    //设置小方的uid和deviceId，如果切换小方的话uid和deviceId需要改变。
                    hasLinkageStatus = true;
                    linkageCondition.setUid(device.getUid());
                    linkageCondition.setDeviceId(device.getDeviceId());
                    linkageCondition.setStatusType(SmartSceneConstant.StatusType.NOT_SET);
                }
            }

            if (!hasLinkageStatus) {
                LinkageCondition linkageCondition1 = getDefaultLinkageCondition1(device, value);
                linkageConditions.add(linkageCondition1);
            }
        } else {
            linkageConditions = new ArrayList<LinkageCondition>();
            LinkageCondition linkageCondition1 = getDefaultLinkageCondition1(device, value);
            linkageConditions.add(linkageCondition1);
            if (conditionType != IntelligentSceneConditionType.CLICK_ALLONE) {
                LinkageCondition linkageCondition2 = new LinkageCondition();
                linkageCondition2.setLinkageType(1);
                linkageCondition2.setCondition(3);
                linkageCondition2.setDeviceId("");
                linkageCondition2.setStatusType(0);
                linkageCondition2.setValue(0);
                linkageCondition2.setAuthorizedId("");
                linkageConditions.add(linkageCondition2);

                LinkageCondition linkageCondition3 = new LinkageCondition();
                linkageCondition3.setLinkageType(1);
                linkageCondition3.setCondition(4);
                linkageCondition3.setDeviceId("");
                linkageCondition3.setStatusType(0);
                linkageCondition3.setValue(0);
                linkageCondition3.setAuthorizedId("");
                linkageConditions.add(linkageCondition3);

                LinkageCondition linkageCondition4 = new LinkageCondition();
                linkageCondition4.setLinkageType(2);
                linkageCondition4.setCondition(0);
                linkageCondition4.setDeviceId("");
                linkageCondition4.setStatusType(0);
                linkageCondition4.setValue(255);
                linkageCondition4.setAuthorizedId("");
                linkageConditions.add(linkageCondition4);
            }
        }
        return linkageConditions;
    }

    public static LinkageCondition getDefaultLinkageCondition1(Device device, int value) {
        LinkageCondition linkageCondition1 = new LinkageCondition();
        linkageCondition1.setLinkageType(SmartSceneConstant.LinkageType.DEVICE_STATUS);

        int conditionInt = SmartSceneConstant.Condition.EQUAL;
        int statusType = 1;
        if (device != null) {
            final int deviceType = device.getDeviceType();
            if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
                conditionInt = SmartSceneConstant.Condition.GREATER_THAN_EQUAL;
                if (value == Constant.INVALID_NUM) {
                    value = SmartSceneConstant.TEMPERATURE_DEFAULT_VALUE;
                }
            } else if (deviceType == DeviceType.HUMIDITY_SENSOR) {
                conditionInt = SmartSceneConstant.Condition.GREATER_THAN_EQUAL;
                statusType = 2;
                if (value == Constant.INVALID_NUM) {
                    value = SmartSceneConstant.HUMIDITY_DEFAULT_VALUE;
                }
            } else if (deviceType == DeviceType.ALLONE) {
                //小方
                linkageCondition1.setKeyNo(KeyNo.ALLONE);
                linkageCondition1.setUid(device.getUid());
                linkageCondition1.setKeyAction(KeyAction.SINGLE_CLICK);
                statusType = SmartSceneConstant.StatusType.NOT_SET;
                linkageCondition1.setLinkageType(SmartSceneConstant.LinkageType.KEY_ACTION);
                linkageCondition1.setAuthorizedId("");
                value = 0;
            }

            String authorizedId = AuthorizedIdType.ALL_MEMBER + "," + AuthorizedIdType.KEY_UNLOCK;
            linkageCondition1.setCondition(conditionInt);
            linkageCondition1.setDeviceId(device.getDeviceId());
            linkageCondition1.setStatusType(statusType);
            linkageCondition1.setValue(value);
            if (deviceType != DeviceType.ALLONE) {
                linkageCondition1.setAuthorizedId(authorizedId);
            }
        } else {
            LogUtil.e(TAG, "getDefaultLinkageCondition1()-device is null.");
        }
        return linkageCondition1;
    }

    /**
     * @param linkageConditions
     * @return
     */
    public static List<LinkageCondition> getTimeAndWeekConditions(List<LinkageCondition> linkageConditions,
                                                                  String startTime, String endTime, int week) {
        int linkageType;
        int condition;
        int startHour = 0;
        int startMinute = 0;
        int endHour = 0;
        int endMinute = 0;

        if (TextUtils.isEmpty(startTime)) {
            startTime = "00:00";
        }
        if (TextUtils.isEmpty(endTime)) {
            endTime = "00:00";
        }
        String[] startTimeSplit = startTime.split(":");
        String[] endTimeSplit = endTime.split(":");
        if (startTimeSplit.length > 1 && endTimeSplit.length > 1) {
            startHour = Integer.valueOf(startTimeSplit[0]);
            startMinute = Integer.valueOf(startTimeSplit[1]);
            endHour = Integer.valueOf(endTimeSplit[0]);
            endMinute = Integer.valueOf(endTimeSplit[1]);
        }

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                LinkageCondition linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                condition = linkageCondition.getCondition();
                if (linkageType == 1) {
                    if (condition == 3) {
                        linkageCondition.setValue(startHour * 3600 + startMinute * 60);
                    } else if (condition == 4) {
                        linkageCondition.setValue(endHour * 3600 + endMinute * 60);
                    }
                } else if (linkageType == 2) {
                    if (condition == 0) {
                        linkageCondition.setValue(week);
                    }
                }
            }

        }
        return linkageConditions;
    }

    /**
     * 获取授权门锁的用户id列表
     *
     * @param linkageConditions
     */
    public static List<Integer> getSelectedLockMumber(List<LinkageCondition> linkageConditions) {
        List<Integer> authorizedIds = new ArrayList<>();
        LinkageCondition linkageCondition;
        int linkageType;
        String authorizedId;
        String[] authorizedIdStrings;

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                authorizedId = linkageCondition.getAuthorizedId();
                if (linkageType == 0) {
                    if (!StringUtil.isEmpty(authorizedId)) {
                        authorizedIdStrings = authorizedId.split(",");
                        for (int i = 0; i < authorizedIdStrings.length; i++) {
                            authorizedIds.add(Integer.valueOf(authorizedIdStrings[i]));
                        }
                    } else {
                        authorizedIds.add(AuthorizedIdType.ALL_MEMBER);
                    }
                }
            }
        }
        return authorizedIds;
    }

    /**
     * 获取授权门锁的用户id列表
     *
     * @param linkageConditions
     */
    public static String getSelectedLockMumber(Context context, List<LinkageCondition> linkageConditions) {
        List<Integer> authorizedIds = new ArrayList<>();
        LinkageCondition linkageCondition;
        int linkageType;
        String authorizedId;
        String[] authorizedIdStrings;
        String lockMember = "";

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                authorizedId = linkageCondition.getAuthorizedId();
                if (linkageType == 0) {
                    if (!StringUtil.isEmpty(authorizedId)) {
                        authorizedIdStrings = authorizedId.split(",");
                        for (int i = 0; i < authorizedIdStrings.length; i++) {
                            authorizedIds.add(Integer.valueOf(authorizedIdStrings[i]));
                        }
                    } else {
                        authorizedIds.add(AuthorizedIdType.ALL_MEMBER);
                    }
                }
            }
        }

        if (authorizedIds.contains(Integer.valueOf(AuthorizedIdType.ALL_MEMBER))) {
            lockMember = context.getResources().getString(R.string.intelligent_scene_all_users);
        } else {
            String deviceId = IntelligentSceneTool.getDeviceId(linkageConditions);
            Device device = new DeviceDao().selDevice(deviceId);
            List<DoorUserData> doorUserDatas = DoorUserDao.getInstance().getDoorUserList(device.getDeviceId());

            Iterator<DoorUserData> iterator = doorUserDatas.iterator();
            DoorUserData doorUserData;
            while (iterator.hasNext()) {
                doorUserData = iterator.next();
                if (authorizedIds.contains(Integer.valueOf(doorUserData.getAuthorizedId()))) {
                    if (TextUtils.isEmpty(doorUserData.getName())) {
                        lockMember += context.getString(R.string.linkage_action_no)
                                + "(" + context.getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ") ";
                    } else if (doorUserData.getType() == DoorUserDao.TYPE_TMP_USER) {
                        lockMember += doorUserData.getName() + " ";
                    } else {
                        lockMember += doorUserData.getName()
                                + "(" + context.getString(R.string.lock_number) + doorUserData.getAuthorizedId() + ") ";
                    }
                }

            }
            lockMember = lockMember.substring(0, lockMember.length() - 1 > 0 ? lockMember.length() - 1 : 0);
        }
        return lockMember;
    }

    /**
     * @param linkageConditions
     * @param authorizedIds
     * @return
     */
    public static List<LinkageCondition> setSelectedLockMumber(List<LinkageCondition> linkageConditions, List<Integer> authorizedIds) {
        LinkageCondition linkageCondition;
        int linkageType;
        String authorizedIdString = "";

        if (authorizedIds != null && authorizedIds.size() > 0) {
            for (Integer authorizedId : authorizedIds) {
                authorizedIdString += authorizedId + ",";
            }
            authorizedIdString = authorizedIdString.substring(0, authorizedIdString.length() - 1);
        }

        if (linkageConditions != null && linkageConditions.size() > 0) {
            Iterator<LinkageCondition> iterator = linkageConditions.iterator();
            while (iterator.hasNext()) {
                linkageCondition = iterator.next();
                linkageType = linkageCondition.getLinkageType();
                if (linkageType == 0) {
                    linkageCondition.setAuthorizedId(authorizedIdString);
                }
            }
        }
        return linkageConditions;
    }
}
