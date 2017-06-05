package com.orvibo.homemate.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceCommenDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.dao.FloorDao;
import com.orvibo.homemate.data.ACPanelModelAndWindConstant;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.CommonFlag;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.device.HopeMusic.MusicActivity;
import com.orvibo.homemate.device.action.ActionCurtain2HalvesActivity;
import com.orvibo.homemate.device.action.ActionCurtain2HalvesOldActivity;
import com.orvibo.homemate.device.action.ActionCurtainOutsideActivity;
import com.orvibo.homemate.device.action.ActionCurtainRollerActivity;
import com.orvibo.homemate.device.action.ActionCurtainRollerOldActivity;
import com.orvibo.homemate.device.action.ActionLightActivity;
import com.orvibo.homemate.device.action.ActionSecurityActivity;
import com.orvibo.homemate.device.action.ActionSwitchActivity;
import com.orvibo.homemate.device.action.infrareddevice.ActionAcPanelActivity;
import com.orvibo.homemate.device.action.infrareddevice.ActionConditionerActivity;
import com.orvibo.homemate.device.action.infrareddevice.ActionSTBControllerActivity;
import com.orvibo.homemate.device.action.infrareddevice.ActionSelfRemoteActivity;
import com.orvibo.homemate.device.action.infrareddevice.ActionTVControllerActivity;
import com.orvibo.homemate.device.allone2.AlloneControlActivity;
import com.orvibo.homemate.device.allone2.RemoteControlActivity;
import com.orvibo.homemate.device.allone2.epg.ProgramGuidesActivity;
import com.orvibo.homemate.device.allone2.irlearn.RemoteLearnActivity;
import com.orvibo.homemate.device.control.AcPanelActivity;
import com.orvibo.homemate.device.control.CameraActivity;
import com.orvibo.homemate.device.control.ClotheShorseActivity;
import com.orvibo.homemate.device.control.Curtain2HalvesActivity;
import com.orvibo.homemate.device.control.CurtainOldActivity;
import com.orvibo.homemate.device.control.CurtainRollerActivity;
import com.orvibo.homemate.device.control.CurtainWindowShadesActivity;
import com.orvibo.homemate.device.control.NewSensorStatusActivity;
import com.orvibo.homemate.device.control.SensorStatusActivity;
import com.orvibo.homemate.device.control.SocketStatusActivity;
import com.orvibo.homemate.device.control.TemperatureAndHumidityActivity;
import com.orvibo.homemate.device.control.infrareddevice.ConditionerActivity;
import com.orvibo.homemate.device.control.infrareddevice.DeviceSetSelfRemoteActivity;
import com.orvibo.homemate.device.control.infrareddevice.STBControllerActivity;
import com.orvibo.homemate.device.control.infrareddevice.TVControllerActivity;
import com.orvibo.homemate.device.irlearn.ConditionerIrLearnActivity;
import com.orvibo.homemate.device.irlearn.DeviceSetSelfRemoteIrLearnActivity;
import com.orvibo.homemate.device.irlearn.STBIrLearnActivity;
import com.orvibo.homemate.device.irlearn.TVIrLearnActivity;
import com.orvibo.homemate.device.light.MultipleLightActivity;
import com.orvibo.homemate.device.light.SingleLightActivity;
import com.orvibo.homemate.device.light.action.MultipleActionLightActivity;
import com.orvibo.homemate.device.light.action.SingleActionLightActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.device.manage.edit.DeviceSetSelfRemoteAddButtonActivity;
import com.orvibo.homemate.device.manage.edit.SceneDeviceEditActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;

import java.util.ArrayList;
import java.util.List;

public class DeviceTool {
    private static Context context = ViHomeProApp.getContext();

    public static String getDefaultDeviceTypeInfoName(Context context, int deviceType) {
        String infoName = "";
        if (context == null || deviceType == Constant.INVALID_NUM) {
            return infoName;
        }
        if (deviceType == DeviceType.VICENTER) {
            infoName = context.getString(R.string.device_type_VICENTER_44);
        } else if (deviceType == DeviceType.MINIHUB) {
            infoName = context.getString(R.string.device_type_MINIHUB_45);
        }
        if (deviceType == DeviceType.CAMERA) {
            infoName = context.getString(R.string.device_type_P2P_CAMERA);
        }
        return infoName;
    }

    /**
     * 根据resId判断是否属于wifi设备
     *
     * @param resId
     * @return
     */
    public static boolean isWifiDevice(int resId) {
        boolean isWifiDevice = false;
        if (resId == R.string.device_add_coco
                || resId == R.string.device_add_s20c
//                || resId == R.string.device_add_socket_us//以下五个针对海外版本
//                || resId == R.string.device_add_socket_eu
//                || resId == R.string.device_add_socket_uk
//                || resId == R.string.device_add_socket_au
//                || resId == R.string.device_add_socket_cn
                || resId == R.string.device_add_socket_s25//针对海外
                || resId == R.string.device_add_socket_us//针对海外
                || resId == R.string.device_add_socket_eu//针对海外
                || resId == R.string.device_add_socket_uk//针对海外
                || resId == R.string.device_add_socket_au//针对海外
                || resId == R.string.device_add_socket_cn//针对海外
                || resId == R.string.device_add_socket_yd//针对海外
                || resId == R.string.device_add_yidong
                || resId == R.string.device_add_socket_s25//针对海外
                || resId == R.string.device_add_socket_yd//针对海外
                || resId == R.string.device_add_s31_socket//针对海外
                || resId == R.string.device_add_liangba
                || resId == R.string.device_add_oujia
                || resId == R.string.device_add_aoke_liangyi
                || resId == R.string.device_add_mairunclothes
                || resId == R.string.device_add_feidiao_lincoln
                || resId == R.string.device_add_feidiao_xiaoe
                || resId == R.string.device_add_xiaofang_tv
                ) {
            isWifiDevice = true;
        }
        return isWifiDevice;
    }

    /**
     * 该红外设备是否已经学习红外码
     *
     * @param device
     * @return true 已经学习红外码
     */
    public static boolean isIrDevicelearned(Device device) {
        if (device == null) {
            return false;
        }
        final String uid = device.getUid();
        final String deviceId = device.getDeviceId();
        List<DeviceIr> deviceIrs = new DeviceIrDao().selDeviceIrs(uid, deviceId);
        // Device device = new DeviceDao().selDevice(uid, deviceId);
        int deviceType = device.getDeviceType();
        if (deviceType == DeviceType.SELF_DEFINE_IR) {
            for (DeviceIr deviceIr : deviceIrs) {
                if (deviceIr.getIr() != null && deviceIr.getIr().length > 0) {
                    return true;
                }
            }
            return false;
        } else {
            return deviceIrs != null && deviceIrs.size() > 0 ? true : false;
        }
    }

    public static boolean isShowLower(String modleId) {
        return modleId.equals("a9241f3104c1422b82c7ad2cbd5d6fe0");
    }

    public static boolean isSleep(int deviceType) {
        boolean sleep = false;
        if (deviceType == DeviceType.REMOTE) {
            sleep = true;
        }
        return sleep;
    }


    /**
     * 获取设备图标的drawable
     *
     * @param device
     * @param isOnline
     * @return
     */
    public static Drawable getDeviceDrawable(Device device, boolean isOnline) {
        Drawable icon;
        if (!isOnline)
            icon = context.getResources().getDrawable(R.drawable.bg_dormancy);
        else
            icon = context.getResources().getDrawable(getDeviceIcon(device));
        return icon;
    }

    /**
     * 获取推送消息设备图标的drawable
     *
     * @param device
     * @return
     */
    public static Drawable getPushDeviceDrawable(Device device) {
        Drawable icon = context.getResources().getDrawable(getWhiteDeviceIcon(device));
        Drawable bg = context.getResources().getDrawable(R.drawable.bg_smallcircle);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{bg, icon});
        layerDrawable.setLayerInset(0, 0, 0, 0, 0);
        layerDrawable.setLayerInset(1, 20, 20, 20, 20);
        return layerDrawable;
    }

    /**
     * 获取常用设备图标的drawable
     * 是否离线状态
     *
     * @param device
     * @return
     */
    public static Drawable getCommonDeviceDrawable(Device device, boolean isOnline) {
        Drawable bg = context.getResources().getDrawable(getCommonDeviceIconBg(device, isOnline));
        if (!isOnline) {
            return bg;
        } else {
            Drawable icon = context.getResources().getDrawable(getWhiteDeviceIcon(device));
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{bg, icon});
            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
            layerDrawable.setLayerInset(1, 60, 60, 60, 60);
            return layerDrawable;
        }
    }

    /**
     * 获取常用设备关闭状态图标的drawable
     *
     * @param device
     * @return
     */
    public static Drawable getCloseCommonDeviceDrawable(Device device) {
        Drawable bg = context.getResources().getDrawable(getCommonDeviceIconBg(device, false));
        Drawable icon = context.getResources().getDrawable(getWhiteDeviceIcon(device));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{bg, icon});
        layerDrawable.setLayerInset(0, 0, 0, 0, 0);
        layerDrawable.setLayerInset(1, 60, 60, 60, 60);
        return layerDrawable;
    }

    /**
     * 获取设备图标
     *
     * @param
     * @param
     * @return
     */
    public static int getDeviceIcon(Device device) {
        int resId = R.drawable.icon_otherequipment;
        int deviceType = Constant.INVALID_NUM;
        if (device != null) {
            deviceType = device.getDeviceType();
        }
        switch (deviceType) {
            case DeviceType.LAMP:
            case DeviceType.DIMMER:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
            case DeviceType.RGB:
                resId = R.drawable.icon_equipment_lamp;
                break;
            case DeviceType.S20:
            case DeviceType.OUTLET:
                // 插座
                resId = R.drawable.icon_socket;
                break;
            case DeviceType.COCO:
                resId = R.drawable.icon_platooninsert;
                break;
            case DeviceType.VICENTER:
                // vicenter300主机
                resId = R.drawable.icon_largehost;
                break;
            case DeviceType.MINIHUB:
                // miniHub
                resId = R.drawable.icon_smallhost;
                break;
            case DeviceType.IR_REPEATER:
                //红外转发器
                resId = R.drawable.icon_infraredtransponder;
                break;
            case DeviceType.TV:
                resId = R.drawable.icon_tv;
                //TV
                break;
            case DeviceType.STB:
                //机顶盒
                resId = R.drawable.icon_settopboxes;
                break;
            case DeviceType.AC:
                //空调
                resId = R.drawable.icon_airconditioning;
                break;
            case DeviceType.LOCK:
                resId = R.drawable.icon_door;
                //门锁
                break;
            case DeviceType.CURTAIN:
            case DeviceType.CURTAIN_PERCENT:
                //对开窗帘
                resId = R.drawable.icon_curtain2;
                break;
            case DeviceType.ROLLER_SHUTTERS:
            case DeviceType.WINDOW_SHADES:
            case DeviceType.ROLLER_SHADES_PERCENT:
                //卷帘
                resId = R.drawable.icon_blind;
                break;
            case DeviceType.PUSH_WINDOW:
                //推拉窗
                resId = R.drawable.icon_awning;
                break;
            case DeviceType.SCREEN:
                //幕布
                resId = R.drawable.icon_curtain;
                break;
            case DeviceType.ROLLING_GATE:
                // 卷闸门
                resId = R.drawable.icon_rollinggate;
                break;
            case DeviceType.SWITCH_RELAY:
                //继电器
                resId = R.drawable.icon_relay;
                break;
            case DeviceType.INFRARED_SENSOR:
                //人体红外
                resId = R.drawable.icon_humanbodyinfrared;
                break;
            case DeviceType.FLAMMABLE_GAS:
                resId = R.drawable.icon_combustible;
                break;
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
                // 门窗磁
                resId = R.drawable.icon_magneticwindowanddoor;
                break;
            case DeviceType.TEMPERATURE_SENSOR:
                resId = R.drawable.icon_device_temperature;
                break;
            case DeviceType.HUMIDITY_SENSOR:
                resId = R.drawable.icon_device_water;
                break;
            case DeviceType.SMOKE_SENSOR:
                resId = R.drawable.icon_smoke;
                break;
            case DeviceType.CO_SENSOR:
                resId = R.drawable.icon_co;
                break;
            case DeviceType.WATER_SENSOR:
                resId = R.drawable.icon_water;
                break;
            case DeviceType.SOS_SENSOR:
                resId = R.drawable.icon_sos;
                break;
            case DeviceType.CAMERA:
                //摄像头
                resId = R.drawable.icon_camera;
                break;
            case DeviceType.CLOTHE_SHORSE:
                // 晾衣架
                resId = R.drawable.icon_clotheshanger;
                break;
            case DeviceType.REMOTE:
                //智能遥控器
                if (!StringUtil.isEmpty(device.getModel()) && ProductManage.isStickers(device.getModel())) {
                    resId = R.drawable.icon_jiyue_suiyitie;
                } else {
                    resId = R.drawable.icon_smartremote;
                }
                break;
            case DeviceType.SCENE_KEYPAD:
                //情景面板
                resId = R.drawable.icon_scenepanel_3;
                break;

            case DeviceType.FIVE_KEY_SCENE_KEYPAD:
                // 情景面板（5键）
                resId = R.drawable.icon_scenepanel_5;
                break;
            case DeviceType.SEVEN_KEY_SCENE_KEYPAD:
                // 情景面板（7键）
                resId = R.drawable.icon_scenepanel_7;
                break;
            case DeviceType.AC_PANEL:
                //空调面板，暂时用空调图标替换
                resId = R.drawable.icon_airconditioning;
                break;
            case DeviceType.ALLONE:
                resId = R.drawable.icon_allone2;
                break;
            case DeviceType.FAN:
                resId = R.drawable.icon_fans;
                break;
            case DeviceType.SELF_DEFINE_IR:
                resId = R.drawable.icon_remote_control;
                break;
            case DeviceType.BACK_MUSIC:
                resId = R.drawable.icon_home_intelligent_sound;
                break;
            case DeviceType.SPEAKER_BOX:
                resId = R.drawable.icon_sound;
                break;
            case DeviceType.PROJECTOR:
                resId = R.drawable.icon_projector;
                break;
            case DeviceType.TV_BOX:
                resId = R.drawable.icon_equipment_tv_box;
                break;
        }
        return resId;
    }

    /**
     * 获取设备图标，白色
     *
     * @param
     * @param
     * @return
     */
    public static int getWhiteDeviceIcon(Device device) {
        int resId = R.drawable.icon_otherequipment_white;
        int deviceType = Constant.INVALID_NUM;
        if (device != null) {
            deviceType = device.getDeviceType();
        }
        switch (deviceType) {
            case DeviceType.LAMP:
            case DeviceType.DIMMER:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
            case DeviceType.RGB:
                resId = R.drawable.icon_equipment_lamp_white;
                break;
            case DeviceType.S20:
            case DeviceType.OUTLET:
                // 插座
                resId = R.drawable.icon_socket_white;
                break;
            case DeviceType.COCO:
                resId = R.drawable.icon_platooninsert_white;
                break;
            case DeviceType.VICENTER:
                // vicenter300主机
                resId = R.drawable.icon_largehost_white;
                break;
            case DeviceType.MINIHUB:
                // miniHub
                resId = R.drawable.icon_smallhost_white;
                break;
            case DeviceType.IR_REPEATER:
                //红外转发器
                resId = R.drawable.icon_infraredtransponder_white;
                break;
            case DeviceType.TV:
                resId = R.drawable.icon_tv_white;
                //TV
                break;
            case DeviceType.STB:
                //机顶盒
                resId = R.drawable.icon_settopboxes_white;
                break;
            case DeviceType.AC:
                //空调
                resId = R.drawable.icon_airconditioning_white;
                break;
            case DeviceType.LOCK:
                resId = R.drawable.icon_door_white;
                //门锁
                break;
            case DeviceType.CURTAIN:
            case DeviceType.CURTAIN_PERCENT:
                //对开窗帘
                resId = R.drawable.icon_curtain2_white;
                break;
            case DeviceType.ROLLER_SHUTTERS:
            case DeviceType.WINDOW_SHADES:
            case DeviceType.ROLLER_SHADES_PERCENT:
                //卷帘
                resId = R.drawable.icon_blind_white;
                break;
            case DeviceType.PUSH_WINDOW:
                //推拉窗
                resId = R.drawable.icon_awning_white;
                break;
            case DeviceType.SCREEN:
                //幕布
                resId = R.drawable.icon_curtain_white;
                break;
            case DeviceType.ROLLING_GATE:
                // 卷闸门
                resId = R.drawable.icon_rollinggate_white;
                break;
            case DeviceType.SWITCH_RELAY:
                //继电器
                resId = R.drawable.icon_relay_white;
                break;
            case DeviceType.INFRARED_SENSOR:
                //人体红外
                resId = R.drawable.icon_humanbodyinfrared_white;
                break;
            case DeviceType.FLAMMABLE_GAS:
                resId = R.drawable.icon_combustible_white;
                break;
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
                // 门窗磁
                resId = R.drawable.icon_magneticwindowanddoor_white;
                break;
            case DeviceType.TEMPERATURE_SENSOR:
                resId = R.drawable.icon_device_temperature_white;
                break;
            case DeviceType.HUMIDITY_SENSOR:
                resId = R.drawable.icon_device_water_white;
                break;
            case DeviceType.SMOKE_SENSOR:
                resId = R.drawable.icon_smoke_white;
                break;
            case DeviceType.CO_SENSOR:
                resId = R.drawable.icon_co_white;
                break;
            case DeviceType.WATER_SENSOR:
                resId = R.drawable.icon_water_white;
                break;
            case DeviceType.SOS_SENSOR:
                resId = R.drawable.icon_sos_white;
                break;
            case DeviceType.CAMERA:
                //摄像头
                resId = R.drawable.icon_camera_white;
                break;
            case DeviceType.CLOTHE_SHORSE:
                // 晾衣架
                resId = R.drawable.icon_clotheshanger_white;
                break;
            case DeviceType.REMOTE:
                //智能遥控器
                if (!StringUtil.isEmpty(device.getModel()) && ProductManage.isStickers(device.getModel())) {
                    resId = R.drawable.icon_jiyue_suiyitie_white;
                } else {
                    resId = R.drawable.icon_smartremote_white;
                }
                break;
            case DeviceType.SCENE_KEYPAD:
                //情景面板
                resId = R.drawable.icon_scenepanel_3_white;
                break;

            case DeviceType.FIVE_KEY_SCENE_KEYPAD:
                // 情景面板（5键）
                resId = R.drawable.icon_scenepanel_5_white;
                break;
            case DeviceType.SEVEN_KEY_SCENE_KEYPAD:
                // 情景面板（7键）
                resId = R.drawable.icon_scenepanel_7_white;
                break;
            case DeviceType.AC_PANEL:
                //空调面板，暂时用空调图标替换
                resId = R.drawable.icon_airconditioning_white;
                break;
            case DeviceType.ALLONE:
                resId = R.drawable.icon_allone2_white;
                break;
            case DeviceType.FAN:
                resId = R.drawable.icon_fans_white;
                break;
            case DeviceType.SELF_DEFINE_IR:
                resId = R.drawable.icon_remote_control_white;
                break;
            case DeviceType.SPEAKER_BOX:
                resId = R.drawable.icon_sound_white;
                break;
            case DeviceType.PROJECTOR:
                resId = R.drawable.icon_projector_white1;
                break;
            case DeviceType.TV_BOX:
                resId = R.drawable.icon_equipment_tv_box_white;
                break;
            case DeviceType.BACK_MUSIC:
                resId = R.drawable.icon_home_intelligent_sound_white;
        }
        return resId;
    }

    /**
     * 获取设备背景
     *
     * @param device 设备
     * @param
     * @return
     */
    public static int getCommonDeviceIconBg(Device device, boolean isOnline) {
        int resId = R.drawable.bg_bigcircle_yellow_normal;
        if (!isOnline) {
            resId = R.drawable.bg_bigcircle;
            return resId;
        }
        int deviceType = Constant.INVALID_NUM;
        if (device != null) {
            deviceType = device.getDeviceType();
        }
        switch (deviceType) {
            case DeviceType.LAMP:
            case DeviceType.DIMMER:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
            case DeviceType.RGB:
         //   case DeviceType.TV:
            case DeviceType.LOCK:
            case DeviceType.REMOTE:
            case DeviceType.SCENE_KEYPAD:
            case DeviceType.FIVE_KEY_SCENE_KEYPAD:
            case DeviceType.SEVEN_KEY_SCENE_KEYPAD:
                resId = R.drawable.bg_bigcircle_yellow_normal;
                break;

            case DeviceType.OUTLET:
            case DeviceType.COCO:
            case DeviceType.S20:
            case DeviceType.IR_REPEATER:
            case DeviceType.STB:
            case DeviceType.TV:
            case DeviceType.SWITCH_RELAY:
            case DeviceType.INFRARED_SENSOR:
            case DeviceType.CAMERA:
                resId = R.drawable.bg_bigcircle_red_normal;
                break;

            case DeviceType.VICENTER:
            case DeviceType.MINIHUB:
            case DeviceType.AC:
            case DeviceType.CURTAIN:
            case DeviceType.CURTAIN_PERCENT:
            case DeviceType.ROLLER_SHUTTERS:
            case DeviceType.WINDOW_SHADES:
            case DeviceType.ROLLER_SHADES_PERCENT:
            case DeviceType.PUSH_WINDOW:
            case DeviceType.SCREEN:
            case DeviceType.ROLLING_GATE:
            case DeviceType.FLAMMABLE_GAS:
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
            case DeviceType.CLOTHE_SHORSE:
                resId = R.drawable.bg_bigcircle_blue_normal;
                break;
        }
        return resId;
    }

    public static final int getClassificationIconByType(int deviceType, int warning) {
        switch (deviceType) {
            case DeviceType.LOCK:
                return R.drawable.icon_door;
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_OTHER:
                return R.drawable.icon_magneticwindowanddoor;
            case DeviceType.INFRARED_SENSOR:
                return R.drawable.icon_sensor;
            case DeviceType.CO_SENSOR:
                return warning == DeviceStatusConstant.ALARM ? R.drawable.icon_co_white : R.drawable.icon_co;
            case DeviceType.FLAMMABLE_GAS:
                return warning == DeviceStatusConstant.ALARM ? R.drawable.icon_combustible_white : R.drawable.icon_combustible;
            case DeviceType.SOS_SENSOR:
                return warning == DeviceStatusConstant.ALARM ? R.drawable.icon_sos_white : R.drawable.icon_sos;
            case DeviceType.SMOKE_SENSOR:
                return warning == DeviceStatusConstant.ALARM ? R.drawable.icon_smoke_white : R.drawable.icon_smoke;
            case DeviceType.WATER_SENSOR:
                return warning == DeviceStatusConstant.ALARM ? R.drawable.icon_water_white : R.drawable.icon_water;
            case DeviceType.HUMIDITY_SENSOR:
                return R.drawable.icon_device_water;
            case DeviceType.TEMPERATURE_SENSOR:
                return R.drawable.icon_device_temperature;
        }
        return 0;
    }

    /**
     * @param deviceType 设备类型
     * @return 获取智能场景设备图标
     */
    public static int getSensorDeviceIconResIdByDeviceType2Security(int deviceType) {
        int resId = 0;
        switch (deviceType) {
            case DeviceType.INFRARED_SENSOR:
                resId = R.drawable.icon_scene_linkage_human_r;
                break;
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:

                resId = R.drawable.icon_scene_linkage_magnetic_unlock_r;
                break;
            case DeviceType.SMOKE_SENSOR:
                resId = R.drawable.icon_scene_linkage_somke_r;
                break;
            case DeviceType.CO_SENSOR:
                resId = R.drawable.icon_scene_linkage_co_r;
                break;
            case DeviceType.WATER_SENSOR:
                resId = R.drawable.icon_scene_linkage_water_r;
                break;
            case DeviceType.FLAMMABLE_GAS:
                resId = R.drawable.icon_scene_linkage_combustible_r;
                break;
            case DeviceType.SOS_SENSOR:
                resId = R.drawable.icon_scene_sos_r;
                break;
        }
        return resId;
    }

    /**
     * @param deviceType 设备类型
     * @return 获取智能场景设备图标
     */
    public static int getSensorDeviceIconResIdByDeviceType2Unsecurity(int deviceType) {
        int resId = 0;
        switch (deviceType) {
            case DeviceType.INFRARED_SENSOR:
                resId = R.drawable.icon_scene_linkage_human_gray;
                break;
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
                resId = R.drawable.icon_scene_linkage_magnetic_unlock_gray;
                break;
            case DeviceType.SMOKE_SENSOR:
                resId = R.drawable.icon_scene_linkage_somke_gray;
                break;
            case DeviceType.CO_SENSOR:
                resId = R.drawable.icon_scene_linkage_co_gray;
                break;
            case DeviceType.WATER_SENSOR:
                resId = R.drawable.icon_scene_linkage_water_gray;
                break;
            case DeviceType.FLAMMABLE_GAS:
                resId = R.drawable.icon_scene_linkage_combustible_gray;
                break;
            case DeviceType.SOS_SENSOR:
                resId = R.drawable.icon_scene_sos_gray;
                break;
        }
        return resId;
    }

    /**
     * @param deviceType 设备类型
     * @return
     * @author Smagret
     * 获取智能场景设备图标
     */
    public static int getDeviceIconResIdByDeviceType(int deviceType) {
        int resId = 0;
        if (deviceType == DeviceType.AC) {
            resId = R.drawable.icon_scene_aircondition5;
        } else if (deviceType == DeviceType.DIMMER) {
            // 调光灯
            resId = R.drawable.icon_scene_dimming_light0;
        } else if (deviceType == DeviceType.LAMP) {
            // 灯光
            resId = R.drawable.icon_scene_light1;
        } else if (deviceType == DeviceType.OUTLET) {
            // 插座
            resId = R.drawable.icon_scene_socket2;
        } else if (deviceType == DeviceType.SCREEN) {
            resId = R.drawable.icon_scene_curtain3;
        } else if (deviceType == DeviceType.CURTAIN || deviceType == DeviceType.CURTAIN_PERCENT) {
            resId = R.drawable.icon_scene_opposite_window8_34;
        } else if (deviceType == DeviceType.REMOTE) {
            resId = R.drawable.icon_scene_remote_control16;
        } else if (deviceType == DeviceType.STB) {
            resId = R.drawable.icon_scene_set_top_bo_gray;
        } else if (deviceType == DeviceType.TV) {
            resId = R.drawable.icon_scene_tv6;
        } else if (deviceType == DeviceType.RGB) {
            resId = R.drawable.icon_scene_rgb_light19;
        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
            //自定义红外
            resId = R.drawable.icon_scene_infrared33;
        } else if (deviceType == DeviceType.SCENE_KEYPAD) {
            resId = R.drawable.icon_scene_panel15;
        } else if (deviceType == DeviceType.CAMERA) {
            resId = R.drawable.icon_scene_camera14;
        } else if (deviceType == DeviceType.CONTACT_RELAY) {
            resId = R.drawable.icon_scene_electric_relay9_10;
        } else if (deviceType == DeviceType.SWITCH_RELAY) {
            resId = R.drawable.icon_scene_electric_relay9_10;
        } else if (deviceType == DeviceType.PUSH_WINDOW) {
            resId = R.drawable.icon_scene_push_window37_41;
        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
            // 色温灯
            resId = R.drawable.icon_scene_colortemperature_light38;
        } else if (deviceType == DeviceType.ROLLING_GATE) {
            // 卷闸门
            resId = R.drawable.icon_scene_rolling_gate39;
        } else if (deviceType == DeviceType.ROLLER_SHUTTERS) {
            // 卷帘（无百分比）
            resId = R.drawable.icon_scene_shutter35;
        } else if (deviceType == DeviceType.WINDOW_SHADES) {
            // 瑞祥百叶窗
            resId = R.drawable.icon_scene_shutter35;
        } else if (deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
            // 卷帘（百分比）
            resId = R.drawable.icon_scene_shutter35;
        } else if (deviceType == DeviceType.LOCK) {
            resId = R.drawable.icon_scene_lock21;
        } else if (deviceType == DeviceType.COCO) {
            // 单控排插
            resId = R.drawable.icon_scene_coco;
        } else if (deviceType == DeviceType.ALLONE) {
            // Allone
            resId = R.drawable.icon_scene_allone;
        } else {
            resId = R.drawable.icon_scene_light1;
        }
        return resId;
    }

    /**
     * @param deviceType 设备类型
     * @return
     * @author Smagret
     * 获取智能场景设备图标
     */
    public static int getDeviceIconResIdByDeviceType2scene(int deviceType) {
        int resId = 0;
        if (deviceType == DeviceType.AC || deviceType == DeviceType.AC_PANEL) {
            resId = R.drawable.icon_scene_aircondition_g;
        } else if (deviceType == DeviceType.DIMMER) {
            // 调光灯
            resId = R.drawable.icon_scene_dimming_light_g;
        } else if (deviceType == DeviceType.LAMP) {
            // 灯光
            resId = R.drawable.icon_scene_light_g;
        } else if (deviceType == DeviceType.OUTLET) {
            // 插座
            resId = R.drawable.icon_scene_socket_g;
        } else if (deviceType == DeviceType.SCREEN) {
            resId = R.drawable.icon_scene_curtain3_g;
        } else if (deviceType == DeviceType.CURTAIN || deviceType == DeviceType.CURTAIN_PERCENT) {
            resId = R.drawable.icon_scene_opposite_window_g;
        } else if (deviceType == DeviceType.REMOTE) {
            resId = R.drawable.icon_scene_remote_control_g;
        } else if (deviceType == DeviceType.STB) {
            resId = R.drawable.icon_scene_set_top_bo_g;
        } else if (deviceType == DeviceType.TV) {
            resId = R.drawable.icon_scene_tv_g;
        } else if (deviceType == DeviceType.RGB) {
            resId = R.drawable.icon_scene_rgb_light_g;
        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
            //自定义红外
            resId = R.drawable.icon_scene_general_g;
        } else if (deviceType == DeviceType.SCENE_KEYPAD) {
            resId = R.drawable.icon_scene_panel_g;
        } else if (deviceType == DeviceType.CAMERA) {
            resId = R.drawable.icon_scene_camera_g;
        } else if (deviceType == DeviceType.CONTACT_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_g;
        } else if (deviceType == DeviceType.SWITCH_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_g;
        } else if (deviceType == DeviceType.PUSH_WINDOW) {
            resId = R.drawable.icon_scene_push_window_g;
        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
            // 色温灯
            resId = R.drawable.icon_scene_lightmodulationlamp_g;
        } else if (deviceType == DeviceType.ROLLING_GATE) {
            // 卷闸门
            resId = R.drawable.icon_scene_rolling_gate_g;
        } else if (deviceType == DeviceType.ROLLER_SHUTTERS) {
            // 卷帘（无百分比）
            resId = R.drawable.icon_scene_shutter_g;
        } else if (deviceType == DeviceType.WINDOW_SHADES) {
            // 瑞祥百叶窗
            resId = R.drawable.icon_scene_shutter_g;
        } else if (deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
            // 卷帘（百分比）
            resId = R.drawable.icon_scene_shutter_g;
        } else if (deviceType == DeviceType.LOCK) {
            resId = R.drawable.icon_scene_lock21_g;
        } else if (deviceType == DeviceType.COCO) {
            // 单控排插
            resId = R.drawable.icon_scene_platooninsert_g;
        } else if (deviceType == DeviceType.ALLONE) {
            // Allone
            resId = R.drawable.icon_scene_allone;
        } else {
            resId = R.drawable.icon_scene_light_g;
        }
        return resId;
    }

    public static int getDeviceIconResIdByDeviceType2linkage(int deviceType) {
        int resId = 0;
        //暂时空调面板与空调使用同一个图标
        if (deviceType == DeviceType.AC || deviceType == DeviceType.AC_PANEL) {
            resId = R.drawable.icon_scene_aircondition_y;
        } else if (deviceType == DeviceType.DIMMER) {
            // 调光灯
            resId = R.drawable.icon_scene_dimming_light_y;
        } else if (deviceType == DeviceType.LAMP) {
            // 灯光
            resId = R.drawable.icon_scene_light_y;
        } else if (deviceType == DeviceType.OUTLET) {
            // 插座
            resId = R.drawable.icon_scene_socket_y;
        } else if (deviceType == DeviceType.SCREEN) {
            resId = R.drawable.icon_scene_curtain3_y;
        } else if (deviceType == DeviceType.CURTAIN || deviceType == DeviceType.CURTAIN_PERCENT) {
            resId = R.drawable.icon_scene_opposite_window_y;
        } else if (deviceType == DeviceType.REMOTE) {
            resId = R.drawable.icon_scene_remote_control_y;
        } else if (deviceType == DeviceType.STB) {
            resId = R.drawable.icon_scene_set_top_bo_y;

        } else if (deviceType == DeviceType.TV) {
            resId = R.drawable.icon_scene_tv_y;
        } else if (deviceType == DeviceType.RGB) {
            resId = R.drawable.icon_scene_rgb_light_y;
        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
            //自定义红外
            resId = R.drawable.icon_scene_general_y;
        } else if (deviceType == DeviceType.SCENE_KEYPAD) {
            resId = R.drawable.icon_scene_panel_y;
        } else if (deviceType == DeviceType.CAMERA) {
            resId = R.drawable.icon_scene_camera_y;
        } else if (deviceType == DeviceType.CONTACT_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_y;
        } else if (deviceType == DeviceType.SWITCH_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_y;
        } else if (deviceType == DeviceType.PUSH_WINDOW) {
            resId = R.drawable.icon_scene_push_window_y;
        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
            // 色温灯
            resId = R.drawable.icon_scene_lightmodulationlamp_y;
        } else if (deviceType == DeviceType.ROLLING_GATE) {
            // 卷闸门
            resId = R.drawable.icon_scene_rolling_gate_y;
        } else if (deviceType == DeviceType.ROLLER_SHUTTERS) {
            // 卷帘（无百分比）
            resId = R.drawable.icon_scene_shutter_y;
        } else if (deviceType == DeviceType.WINDOW_SHADES) {
            // 瑞祥百叶窗
            resId = R.drawable.icon_scene_shutter_y;
        } else if (deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
            // 卷帘（百分比）
            resId = R.drawable.icon_scene_shutter_y;
        } else if (deviceType == DeviceType.LOCK) {
            resId = R.drawable.icon_scene_lock_y;
        } else if (deviceType == DeviceType.COCO) {
            // 单控排插
            resId = R.drawable.icon_scene_platooninsert_y;
        } else if (deviceType == DeviceType.ALLONE) {
            // Allone
            resId = R.drawable.icon_scene_allone;
        } else if (deviceType == DeviceType.FAN) {
            // 电风扇
            resId = R.drawable.icon_scene_fan_y;
        } else if (deviceType == DeviceType.TV_BOX) {
            // 电视盒子
            resId = R.drawable.icon_scene_tv_box_y;
        } else if (deviceType == DeviceType.PROJECTOR) {
            // 投影仪
            resId = R.drawable.icon_scene_projector_y;
        } else if (deviceType == DeviceType.SPEAKER_BOX) {
            // 音箱
            resId = R.drawable.icon_scene_speaker_box_y;
        } else {
            resId = R.drawable.icon_scene_light_y;
        }
        return resId;
    }

    public static int getDeviceIconResIdByDeviceType2stop(int deviceType) {
        int resId = 0;
        if (deviceType == DeviceType.AC || deviceType == DeviceType.AC_PANEL) {
            resId = R.drawable.icon_scene_aircondition_gray;
        } else if (deviceType == DeviceType.DIMMER) {
            // 调光灯
            resId = R.drawable.icon_scene_dimming_light_gray;
        } else if (deviceType == DeviceType.LAMP) {
            // 灯光
            resId = R.drawable.icon_scene_light_gray;
        } else if (deviceType == DeviceType.OUTLET) {
            // 插座
            resId = R.drawable.icon_scene_socket_gray;
        } else if (deviceType == DeviceType.SCREEN) {
            resId = R.drawable.icon_scene_curtain3_gray;
        } else if (deviceType == DeviceType.CURTAIN || deviceType == DeviceType.CURTAIN_PERCENT) {
            resId = R.drawable.icon_scene_opposite_window_gray;
        } else if (deviceType == DeviceType.REMOTE) {
            resId = R.drawable.icon_scene_remote_control_gray;
        } else if (deviceType == DeviceType.STB) {
            resId = R.drawable.icon_scene_set_top_bo_gray;
        } else if (deviceType == DeviceType.TV) {
            resId = R.drawable.icon_scene_tv_gray;
        } else if (deviceType == DeviceType.RGB) {
            resId = R.drawable.icon_scene_rgb_light_gray;
        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
            //自定义红外
            resId = R.drawable.icon_scene_general_gray;
        } else if (deviceType == DeviceType.SCENE_KEYPAD) {
            resId = R.drawable.icon_scene_panel_gray;
        } else if (deviceType == DeviceType.CAMERA) {
            resId = R.drawable.icon_scene_camera_gray;
        } else if (deviceType == DeviceType.CONTACT_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_gray;
        } else if (deviceType == DeviceType.SWITCH_RELAY) {
            resId = R.drawable.icon_scene_electric_relay_gray;
        } else if (deviceType == DeviceType.PUSH_WINDOW) {
            resId = R.drawable.icon_scene_push_window_gray;
        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
            // 色温灯
            resId = R.drawable.icon_scene_lightmodulationlamp_gray;
        } else if (deviceType == DeviceType.ROLLING_GATE) {
            // 卷闸门
            resId = R.drawable.icon_scene_rolling_gate_gray;
        } else if (deviceType == DeviceType.ROLLER_SHUTTERS) {
            // 卷帘（无百分比）
            resId = R.drawable.icon_scene_shutter_gray;
        } else if (deviceType == DeviceType.WINDOW_SHADES) {
            // 瑞祥百叶窗
            resId = R.drawable.icon_scene_shutter_gray;
        } else if (deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
            // 卷帘（百分比）
            resId = R.drawable.icon_scene_shutter_gray;
        } else if (deviceType == DeviceType.LOCK) {
            resId = R.drawable.icon_scene_lock_gray;
        } else if (deviceType == DeviceType.COCO) {
            // 单控排插
            resId = R.drawable.icon_scene_platooninsert_gray;
        } else if (deviceType == DeviceType.ALLONE) {
            // Allone
            resId = R.drawable.icon_scene_allone;
        } else if (deviceType == DeviceType.FAN) {
            // 电风扇
            resId = R.drawable.icon_scene_fan_gray;
        } else if (deviceType == DeviceType.TV_BOX) {
            // 电视盒子
            resId = R.drawable.icon_scene_tv_box_gray;
        } else if (deviceType == DeviceType.PROJECTOR) {
            // 投影仪
            resId = R.drawable.icon_scene_projector_gray;
        } else if (deviceType == DeviceType.SPEAKER_BOX) {
            // 音箱
            resId = R.drawable.icon_scene_speaker_box_gray;
        } else {
            resId = R.drawable.icon_scene_light_gray;
        }
        return resId;
    }

    public static int getIntelligentSceneItemBackgroundResource(int position) {
        if (position % 3 == 0) {
            return R.drawable.smartscene_item_bt_green;
//            return R.drawable.bg_green;
        } else if (position % 3 == 1) {
            return R.drawable.smartscene_item_bt_yellow;
//            return R.drawable.bg_yellow;
        } else if (position % 3 == 2) {
            return R.drawable.smartscene_item_bt_red;
//            return R.drawable.bg_red;
        }
        return R.drawable.smartscene_item_bt_green;
    }

    /**
     * 通过设备类型deviceType得到对应的类型名称
     *
     * @param deviceType
     * @return
     */
    public static int getDeviceTypeNameResId(int deviceType) {
        int resId = 0;
        if (deviceType == DeviceType.DIMMER) {
            resId = R.string.device_type_DIMMER_0;
        } else if (deviceType == DeviceType.LAMP) {
            resId = R.string.device_type_LAMP_1;
        } else if (deviceType == DeviceType.OUTLET) {
            resId = R.string.device_type_OUTLET_2;
        } else if (deviceType == DeviceType.SCREEN) {
            resId = R.string.device_type_SCREEN_3;
        } else if (deviceType == DeviceType.WINDOW_SHADES) {
            resId = R.string.device_type_WINDOW_SHADES_4;
        } else if (deviceType == DeviceType.AC) {
            resId = R.string.device_type_AC_5;
        } else if (deviceType == DeviceType.TV) {
            resId = R.string.device_type_TV_6;
        } else if (deviceType == DeviceType.SPEAKER_BOX) {
            resId = R.string.device_type_SPEAKER_BOX_7;
        } else if (deviceType == DeviceType.CURTAIN) {
            resId = R.string.device_type_CURTAIN_8;
        } else if (deviceType == DeviceType.CONTACT_RELAY) {
            resId = R.string.device_type_CONTACT_RELAY_9;
        } else if (deviceType == DeviceType.SWITCH_RELAY) {
            resId = R.string.device_type_SWITCH_RELAY_10;
        } else if (deviceType == DeviceType.IR_REPEATER) {
            resId = R.string.device_type_IR_REPEATER_11;
        } else if (deviceType == DeviceType.WIRELESS) {
            resId = R.string.device_type_WIRELESS_12;
        } else if (deviceType == DeviceType.SCENE_MODE) {
            resId = R.string.device_type_SCENE_MODE_13;
        } else if (deviceType == DeviceType.CAMERA) {
            resId = R.string.device_type_CAMERA_14;
        } else if (deviceType == DeviceType.SCENE_KEYPAD) {
            resId = R.string.device_type_SCENE_KEYPAD_15;
        } else if (deviceType == DeviceType.REMOTE) {
            resId = R.string.device_type_REMOTE_16;
        } else if (deviceType == DeviceType.REPEATER) {
            resId = R.string.device_type_REPEATER_17;
        } else if (deviceType == DeviceType.LUMINANCE_SENSOR) {
            resId = R.string.device_type_LUMINANCE_SENSOR_18;
        } else if (deviceType == DeviceType.RGB) {
            resId = R.string.device_type_RGB_19;
        } else if (deviceType == DeviceType.VIDEO_INTERCOM) {
            resId = R.string.device_type_VIDEO_INTERCOM_20;
        } else if (deviceType == DeviceType.LOCK) {
            resId = R.string.device_type_LOCK_21;
        } else if (deviceType == DeviceType.TEMPERATURE_SENSOR) {
            resId = R.string.device_type_TEMPERATURE_SENSOR_22;
        } else if (deviceType == DeviceType.HUMIDITY_SENSOR) {
            resId = R.string.device_type_HUMIDITY_SENSOR_23;
        } else if (deviceType == DeviceType.AIR_PURITY_SENSOR) {
            resId = R.string.device_type_AIR_PURITY_SENSOR_24;
        } else if (deviceType == DeviceType.FLAMMABLE_GAS) {
            resId = R.string.device_type_FLAMMABLE_GAS_25;
        } else if (deviceType == DeviceType.INFRARED_SENSOR) {
            resId = R.string.device_type_INFRARED_SENSOR_26;
        } else if (deviceType == DeviceType.SMOKE_SENSOR) {
            resId = R.string.device_type_SMOKE_SENSOR_27;
        } else if (deviceType == DeviceType.PANALARM) {
            resId = R.string.device_type_PANALARM_28;
        } else if (deviceType == DeviceType.S20) {
            resId = R.string.device_type_S20_29;
        } else if (deviceType == DeviceType.ALLONE) {
            resId = R.string.device_type_ALLONE_30;
        } else if (deviceType == DeviceType.KEPLER) {
            resId = R.string.device_type_KEPLER_31;
        } else if (deviceType == DeviceType.STB) {
            resId = R.string.device_type_STB_32;
        } else if (deviceType == DeviceType.SELF_DEFINE_IR) {
            resId = R.string.device_type_SELF_DEFINE_IR_33;
        } else if (deviceType == DeviceType.CURTAIN_PERCENT) {
            resId = R.string.device_type_CURTAIN_8;
        } else if (deviceType == DeviceType.ROLLER_SHADES_PERCENT) {
            resId = R.string.device_type_CURTAIN_35;
        }
//        else if (deviceType == DeviceType.WINDOW_SHADES_PERCENT) {
//            resId = R.string.device_type_CURTAIN_36;
//        }
        else if (deviceType == DeviceType.PUSH_WINDOW) {
            resId = R.string.device_type_CURTAIN_37;
        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
            resId = R.string.COLOR_TEMPERATURE_LAMP_38;
        } else if (deviceType == DeviceType.ROLLING_GATE) {
            resId = R.string.device_type_ROLLING_GATE_39;
        } else if (deviceType == DeviceType.ROLLER_SHUTTERS) {
            resId = R.string.device_type_ROLLER_SHUTTERS_42;
        } else if (deviceType == DeviceType.COCO) {
            resId = R.string.device_type_COCO_43;
        } else if (deviceType == DeviceType.VICENTER) {
            resId = R.string.device_type_VICENTER_44;
        } else if (deviceType == DeviceType.MINIHUB) {
            resId = R.string.device_type_MINIHUB_45;
        } else if (deviceType == DeviceType.MAGNETIC) {
            resId = R.string.device_type_MAGNETIC_46;
        } else if (deviceType == DeviceType.MAGNETIC_WINDOW) {
            resId = R.string.device_type_MAGNETIC_WINDOW_47;
        } else if (deviceType == DeviceType.MAGNETIC_DRAWER) {
            resId = R.string.device_type_MAGNETIC_DRAWER_48;
        } else if (deviceType == DeviceType.MAGNETIC_OTHER) {
            resId = R.string.device_type_MAGNETIC_OTHER_49;
        } else if (deviceType == DeviceType.CO_SENSOR) {
            resId = R.string.device_type_CO_SENSOR_55;
        } else if (deviceType == DeviceType.WATER_SENSOR) {
            resId = R.string.device_type_WATER_SENSOR_54;
        } else if (deviceType == DeviceType.SOS_SENSOR) {
            resId = R.string.device_type_SOS_56;
        } else if (deviceType == DeviceType.FIVE_KEY_SCENE_KEYPAD) {
            resId = R.string.device_type_FIVE_KEY_SCENE_KEYPAD_50;
        } else if (deviceType == DeviceType.SEVEN_KEY_SCENE_KEYPAD) {
            resId = R.string.device_type_SEVEN_KEY_SCENE_KEYPAD_51;
        } else {
            resId = R.string.app_name;
        }
        return resId;
    }

//    /**
//     * 按照协议排序设备
//     *
//     * @param devices
//     * @return
//     */
//    public static List<Device> orderDevices(List<Device> devices) {
//        List<Device> orderDevices = new ArrayList<Device>();
//        int appDeviceId = 0x0302;
//        if (devices != null && !devices.isEmpty()) {
//            final int count = devices.size();
//            for (int i = 0; i < count; i++) {
//                final Device device = devices.get(i);
//                final int tempAppDeviceId = device.getAppDeviceId();
//                if (appDeviceId == tempAppDeviceId) {
//                    orderDevices.add(device);
//                }
//
//                if (i == count - 1) {
//                    i = 0;
//                    if (appDeviceId == 0x0302) {
//                        appDeviceId = 0x03FD;
//                    } else if (appDeviceId == 0x03FD) {
//                        appDeviceId = 0x0000;
//                    } else if (appDeviceId == 0x0000) {
//                        appDeviceId = 0x0001;
//                    } else if (appDeviceId == 0x0001) {
//                        appDeviceId = 0x0102;
//                    } else if (appDeviceId == 0x0102) {
//                        appDeviceId = 0x0105;
//                    } else if (appDeviceId == 0x0105) {
//                        appDeviceId = 0x0002;
//                    } else if (appDeviceId == 0x0002) {
//                        appDeviceId = 0x000a;
//                    } else if (appDeviceId == 0x000a) {
//                        appDeviceId = 0x0203;
//                    } else if (appDeviceId == 0x0203) {
//                        appDeviceId = 0x000b;
//                    } else if (appDeviceId == 0x000b) {
//                        appDeviceId = 0x0008;
//                    } else if (appDeviceId == 0x0008) {
//                        appDeviceId = 0x000d;
//                    } else if (appDeviceId == 0x000d) {
//                        appDeviceId = 0x00FE;
//                    } else if (appDeviceId == 0x00FE) {
//                        appDeviceId = 0x0402;
//                    } else if (appDeviceId == 0x0402) {
//                        appDeviceId = 0x0006;
//                    } else if (appDeviceId == 0x0006) {
//                        appDeviceId = 0x000c;
//                    } else if (appDeviceId == 0x000c) {
//                        break;
//                    }
//                }
//            }
//        }
//        return orderDevices;
//    }

    public static String getControlActivityNameByDeviceType(Device device) {
        int deviceType = device.getDeviceType();
        final int appDeviceId = device.getAppDeviceId();
        String activityName = SetDeviceActivity.class.getName();
        switch (deviceType) {
            case DeviceType.LAMP:
            case DeviceType.OUTLET:
            case DeviceType.SWITCH_RELAY:
            case DeviceType.COCO:
            case DeviceType.S20:
                activityName = SocketStatusActivity.class.getName();
//                activityName = SwitchActivity.class.getName();
                break;
            case DeviceType.DIMMER:
            case DeviceType.RGB:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
//                activityName = RgbLightActivity.class.getName();
                if (ProductManage.isSkyRGBW(device)) {
                    activityName = MultipleLightActivity.class.getName();
                } else
                    activityName = SingleLightActivity.class.getName();
                break;
            case DeviceType.AC:
                if (appDeviceId == AppDeviceId.AC_WIIF) {
                    if (ProductManage.isAlloneSunDevice(device)) {
                        activityName = RemoteControlActivity.class.getName();
                    } else
                        activityName = AcPanelActivity.class.getName();
                } else {
                    activityName = ConditionerActivity.class.getName();
                }
                break;
            case DeviceType.STB:
                if (ProductManage.isAlloneLearnDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else if (ProductManage.isAlloneSunDevice(device)) {
                    //activityName = RemoteControlActivity.class.getName();
                    activityName = ProgramGuidesActivity.class.getName();
                } else
                    activityName = STBControllerActivity.class.getName();
                break;
            case DeviceType.TV:
                if (ProductManage.isAlloneLearnDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else if (ProductManage.isAlloneSunDevice(device)) {
                    activityName = RemoteControlActivity.class.getName();
                } else
                    activityName = TVControllerActivity.class.getName();
                break;
            case DeviceType.FAN:
                activityName = RemoteControlActivity.class.getName();
                break;
//            case DeviceType.PUSH_WINDOW:
//            activityName = CurtainOutsideActivity.class.getName();
//            break;
            case DeviceType.ROLLER_SHADES_PERCENT:
                activityName = CurtainRollerActivity.class.getName();
                break;
            case DeviceType.WINDOW_SHADES:
                activityName = CurtainWindowShadesActivity.class.getName();
                break;
            case DeviceType.CURTAIN_PERCENT:
                activityName = Curtain2HalvesActivity.class.getName();
                break;
            case DeviceType.PUSH_WINDOW:
                activityName = CurtainOldActivity.class.getName();
                break;
            case DeviceType.SELF_DEFINE_IR:
                if (ProductManage.isAlloneSunDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else
                    activityName = DeviceSetSelfRemoteActivity.class.getName();
                break;
            //TODO 暂时将卷闸门、花洒、卷帘放在此处
            case DeviceType.ROLLING_GATE:
                //case DeviceType.SPRINKLER:
            case DeviceType.ROLLER_SHUTTERS:
            case DeviceType.SCREEN:
                activityName = CurtainOldActivity.class.getName();
                break;
            case DeviceType.CURTAIN:
                activityName = CurtainOldActivity.class.getName();
                break;
            case DeviceType.INFRARED_SENSOR:
            case DeviceType.MAGNETIC:
            case DeviceType.MAGNETIC_WINDOW:
            case DeviceType.MAGNETIC_DRAWER:
            case DeviceType.MAGNETIC_OTHER:
                activityName = SensorStatusActivity.class.getName();
                break;
//            case DeviceType.FIVE_KEY_SCENE_KEYPAD:
//            case DeviceType.SEVEN_KEY_SCENE_KEYPAD:
//                activityName = SceneDeviceEditActivity.class.getName();
//                break;
            case DeviceType.CAMERA:
                activityName = CameraActivity.class.getName();
                break;
            case DeviceType.CLOTHE_SHORSE:
                activityName = ClotheShorseActivity.class.getName();
                break;
            case DeviceType.FIVE_KEY_SCENE_KEYPAD:
            case DeviceType.SEVEN_KEY_SCENE_KEYPAD:
            case DeviceType.SCENE_KEYPAD:
                activityName = SceneDeviceEditActivity.class.getName();
                break;
            case DeviceType.FLAMMABLE_GAS:
            case DeviceType.SMOKE_SENSOR:
            case DeviceType.WATER_SENSOR:
            case DeviceType.CO_SENSOR:
            case DeviceType.SOS_SENSOR:
                activityName = NewSensorStatusActivity.class.getName();
                break;
            case DeviceType.TEMPERATURE_SENSOR:
            case DeviceType.HUMIDITY_SENSOR:
                activityName = TemperatureAndHumidityActivity.class.getName();
                break;
            //增加空调面板设备
            case DeviceType.AC_PANEL:
                activityName = AcPanelActivity.class.getName();
                break;
            case DeviceType.ALLONE:
                activityName = AlloneControlActivity.class.getName();
                break;
            case DeviceType.BACK_MUSIC:
                activityName = MusicActivity.class.getName();
                break;
            case DeviceType.PROJECTOR:
            case DeviceType.TV_BOX:
            case DeviceType.SPEAKER_BOX:
                activityName = RemoteControlActivity.class.getName();
                break;
            default:
                break;
        }
        return activityName;
    }

    /**
     * @param device
     * @return true为红外设备
     * @author smagret
     * 0：调光灯、 1：普通灯光、 2：插座、 3：幕布、 4：百叶窗、5：空调、 6：电视； 7：音箱； 8：对开窗帘 
     * 9：点触型继电器； 10：开关型继电器；11：红外转发器； 12：无线； 13：情景模式； 14：摄像头； 
     * 15：情景面板；16：遥控器；17：中继器；18：亮度传感器; 19：RGB灯；20:可视对讲模块;21:门锁;
     * 22:温度传感器；23：湿度传感器;24:空气质量传感器;25:可燃气体传感器;26:红外人体传感器;27:烟雾传感器;
     * 28:报警设备；29：S20；30：Allone；31：kepler；32：机顶盒；33：自定义红外；
     * 34：对开窗帘（支持按照百分比控制）；35：卷帘（支持按照百分比控制）；36：空调面板；37：推窗器；
     * 38：色温灯；39：卷闸门；40：花洒；41：推窗器；42：卷帘（无百分比）；43：单控排插；
     * 44：vicenter300主机；45：miniHub；46：门磁；47：窗磁；48：抽屉磁；49：其他类型的门窗磁；
     * 50：情景面板（5键）；51：情景面板（7键）；52：晾衣架;53：华顶夜灯插座；54：水浸传探测器；
     * 55：一氧化碳报警器；56：紧急按钮 57：背景音乐；58：电风扇；59：电视盒子；60：投影仪；
     * 61：情景面板（1键）；62：情景面板（2键）；63：情景面板（4键）
     */
    public static String getActionActivityNameByDeviceType(Device device) {
        int deviceType = device.getDeviceType();
        String modelId = device.getModel();
        String activityName = null;
        switch (deviceType) {
            case DeviceType.DIMMER:
            case DeviceType.COLOR_TEMPERATURE_LAMP:
            case DeviceType.RGB:
                if (ProductManage.isSkyRGBW(device))
                    activityName = MultipleActionLightActivity.class.getName();
                else
                    activityName = SingleActionLightActivity.class.getName();
                break;
            case DeviceType.OUTLET:
                activityName = ActionSwitchActivity.class.getName();
                break;
            case DeviceType.LAMP:
            case DeviceType.COCO:
            case DeviceType.SWITCH_RELAY:
            case DeviceType.S20:
                activityName = ActionLightActivity.class.getName();
                break;
            case DeviceType.AC:
                //区分zigbee红外伴侣创建的空调、Allone2创建的空调、创维WiFi空调
                if (!StringUtil.isEmpty(modelId)
                        && modelId.equals(ModelID.Allone2)) {
                    activityName = RemoteControlActivity.class.getName();
                } else if (device.getAppDeviceId() == AppDeviceId.AC_WIIF) {
                    if (!StringUtil.isEmpty(modelId)
                            && modelId.equals(ModelID.MODEL_WIFI_AC)) {
                        //创维WiFi空调
                        activityName = ActionAcPanelActivity.class.getName();
                    } else {
                        activityName = RemoteControlActivity.class.getName();
                    }
                } else {
                    //zigbee红外伴侣创建的空调
                    activityName = ActionConditionerActivity.class.getName();
                }
                break;
            case DeviceType.TV:
                if (ProductManage.isAlloneLearnDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else if (ProductManage.isAlloneSunDevice(device)) {
                    activityName = RemoteControlActivity.class.getName();
                } else {
                    activityName = ActionTVControllerActivity.class.getName();
                }
                break;
            case DeviceType.STB:

                if (ProductManage.isAlloneLearnDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else if (ProductManage.isAlloneSunDevice(device)) {
                    activityName = RemoteControlActivity.class.getName();
                } else {
                    activityName = ActionSTBControllerActivity.class.getName();
                }
                break;
            case DeviceType.ROLLER_SHADES_PERCENT:
                activityName = ActionCurtainRollerActivity.class.getName();
                break;
            case DeviceType.CURTAIN_PERCENT:
                activityName = ActionCurtain2HalvesActivity.class.getName();
                break;
            case DeviceType.PUSH_WINDOW:
                activityName = ActionCurtainOutsideActivity.class.getName();
                break;
            //TODO 暂时将卷闸门、卷帘放在此处
            case DeviceType.ROLLING_GATE:
                //case DeviceType.SPRINKLER:
            case DeviceType.ROLLER_SHUTTERS:
            case DeviceType.SCREEN:
                activityName = ActionCurtainRollerOldActivity.class.getName();
                break;
            case DeviceType.CURTAIN:
                activityName = ActionCurtain2HalvesOldActivity.class.getName();
                break;
            case DeviceType.WINDOW_SHADES:
                activityName = ActionCurtainRollerOldActivity.class.getName();
                break;
            case DeviceType.SELF_DEFINE_IR:
                if (ProductManage.isAlloneLearnDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else if (ProductManage.isAlloneSunDevice(device)) {
                    activityName = RemoteLearnActivity.class.getName();
                } else
                    activityName = ActionSelfRemoteActivity.class.getName();
                break;

            case DeviceType.TEMPERATURE_SENSOR:
            case DeviceType.HUMIDITY_SENSOR:
            case DeviceType.AIR_PURITY_SENSOR:
            case DeviceType.FLAMMABLE_GAS:
            case DeviceType.INFRARED_SENSOR:
            case DeviceType.SMOKE_SENSOR:
            case DeviceType.PANALARM:
                //布撤防
                activityName = ActionSecurityActivity.class.getName();
                break;
            case DeviceType.AC_PANEL:
                activityName = ActionAcPanelActivity.class.getName();
                break;
            case DeviceType.FAN:
            case DeviceType.SPEAKER_BOX:
            case DeviceType.TV_BOX:
            case DeviceType.PROJECTOR:
                activityName = RemoteControlActivity.class.getName();
                break;
        }
        return activityName;
    }


    /**
     * @param deviceType
     * @return true为红外设备
     * @author smagret
     */
    public static String getLearnIrActivityNameByDeviceType(int deviceType, boolean hasKey) {
        String activityName = null;
        switch (deviceType) {

            case DeviceType.TV:
                activityName = TVIrLearnActivity.class.getName();
                break;
            case DeviceType.STB:
                activityName = STBIrLearnActivity.class.getName();
                break;
            case DeviceType.AC:
                activityName = ConditionerIrLearnActivity.class.getName();
                break;
            case DeviceType.SELF_DEFINE_IR:
                if (hasKey) {
                    activityName = DeviceSetSelfRemoteIrLearnActivity.class.getName();
                } else {
                    activityName = DeviceSetSelfRemoteAddButtonActivity.class.getName();
                }
                break;
        }
        return activityName;
    }

    public static String[] getBindItemName(Context context, String uid, String deviceId) {
        String[] names = DeviceCommenDao.selFloorNameAndRoomNameAndDeviceName(uid, deviceId);
        if (names != null && names.length == 3) {
            String floorName = names[0];
            if (StringUtil.isEmpty(floorName)) {
                if (new FloorDao().selFloorNo(UserCache.getCurrentMainUid(context)) > 0) {
                    floorName = context.getString(R.string.not_set_floor_room);
                } else {
                    floorName = "";
                }
            }
            names[0] = floorName;
        }
        return names;
    }

    public static String[] getBindItemName(Context context, String deviceId) {
        String[] names = DeviceCommenDao.selFloorNameAndRoomNameAndDeviceName(deviceId);
        if (names != null && names.length == 3) {
            String floorName = names[0];
            if (StringUtil.isEmpty(floorName)) {
                if (new FloorDao().selFloorNo(UserCache.getCurrentMainUid(context)) > 0) {
                    floorName = context.getString(R.string.not_set_floor_room);
                } else {
                    floorName = "";
                }
            }
            names[0] = floorName;
        }
        return names;
    }

    public static String getActionName(Context context, Timing timing) {
        String actionName = null;
        if (context == null || timing == null) {
            return actionName;
        }
        return getActionName(context, timing.getCommand(), timing.getValue1(), timing.getValue2(),
                timing.getValue3(), timing.getValue4(), timing.getDeviceId());
    }

    public static String getActionName(Context context, Countdown countdown) {
        String actionName = null;
        if (context == null || countdown == null) {
            return actionName;
        }
        return getActionName(context, countdown.getCommand(), countdown.getValue1(), countdown.getValue2(),
                countdown.getValue3(), countdown.getValue4(), countdown.getDeviceId());
    }

    public static String getActionName(Context context, Action action) {
        String actionName = getActionName(context, action.getCommand(), action.getValue1(), action.getValue2(), action.getValue3(), action.getValue4(), action.getDeviceId());

        //allone的actionName
        if (TextUtils.isEmpty(actionName)) {
            actionName = action.getName();
        }
        return actionName;
    }

    public static String getActionName(Context context, String order, int value1, int value2, int value3, int value4, String deviceId) {
        Resources res = context.getResources();
        String actionName = null;
        if (res == null) {
            return actionName;
        }
        if (StringUtil.isEmpty(order)) {
            return res.getString(R.string.action_not_set);
        }

        if (order.equals(DeviceOrder.ON)) {
            actionName = res.getString(R.string.action_on);
        } else if (order.equals(DeviceOrder.OPEN)) {
            int deviceType = DeviceType.CURTAIN;
            Device device = new DeviceDao().selDevice(deviceId);
            if (device != null) {
                deviceType = device.getDeviceType();
            }
            if (value1 == DeviceStatusConstant.CURTAIN_ON) {
                //幕布的显示跟其他非百分比窗帘的状态相反。比如command为open，幕布显示的动作未关闭，其他非百分比窗帘为打开。
                if (deviceType == DeviceType.SCREEN) {
                    actionName = res.getString(R.string.action_close);
                } else {
                    actionName = res.getString(R.string.action_open);
                }
            } else if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
                //幕布的显示跟其他非百分比窗帘的状态相反。比如command为open，幕布显示的动作未关闭，其他非百分比窗帘为打开。
                if (deviceType == DeviceType.SCREEN) {
                    actionName = res.getString(R.string.action_open);
                } else {
                    actionName = res.getString(R.string.action_close);
                }
            } else if (value1 == 255) {//奥科电机。初始化的时候value1 = 255
                actionName = res.getString(R.string.action_close);
            } else {
                String percentString = value1 + "%";
                String temp = res.getString(R.string.action_on_percent);
                actionName = String.format(temp, percentString);
            }
        } else if (order.equals(DeviceOrder.OFF)) {
            actionName = res.getString(R.string.action_off);
            Device device = new DeviceDao().selDevice(deviceId);
            //RGBW灯的关闭显示特殊处理
            if (ProductManage.isSkyRGBW(device)) {
                if (device.getDeviceType() == DeviceType.RGB) {
                    actionName = res.getString(R.string.action_close) + res.getString(R.string.rgb);
                } else {
                    actionName = res.getString(R.string.action_close) + res.getString(R.string.white_light);
                }
            }
        } else if (order.equals(DeviceOrder.CLOSE)) {
            int deviceType = DeviceType.CURTAIN;
            Device device = new DeviceDao().selDevice(deviceId);
            if (device != null) {
                deviceType = device.getDeviceType();
            }
            if (deviceType == DeviceType.SCREEN) {
                actionName = res.getString(R.string.action_open);
            } else {
                actionName = res.getString(R.string.action_close);
            }
        } else if (order.equals(DeviceOrder.TOGGLE)) {
            actionName = res.getString(R.string.action_toggle);
            Device device = new DeviceDao().selDevice(deviceId);
            //RGBW灯的显示特殊处理
            if (ProductManage.isSkyRGBW(device)) {
                if (device.getDeviceType() == DeviceType.RGB) {
                    actionName = res.getString(R.string.action_toggle) + res.getString(R.string.rgb);
                } else {
                    actionName = res.getString(R.string.action_toggle) + res.getString(R.string.white_light);
                }
            }
        } else if (order.equals(DeviceOrder.MOVE_TO_LEVEL)) {
            int percent = getPrecent(value2);
            String percentString = percent + "%";
            String temp = res.getString(R.string.action_level);//亮度 %1$s
            actionName = String.format(temp, percentString);
            Device device = new DeviceDao().selDevice(deviceId);
            //RGBW灯的显示特殊处理
            if (ProductManage.isSkyRGBW(device)) {
                actionName = context.getString(R.string.white_light) + actionName;//白光亮度 %1$s
            }
        } else if (order.equals(DeviceOrder.COLOR_TEMPERATURE)) {
            int percent = getPrecent(value2);
            String percentString = percent + "%";
            String temp = res.getString(R.string.action_color_temp);
            actionName = String.format(temp, percentString, value3);
        } else if (order.equals(DeviceOrder.COLOR_CONTROL)) {
            int level = value2;
            int saturation = value3;
            int hue = value4;
            int[] rgb = ColorUtil.hsl2Rgb(hue, saturation, level);
            String temp = res.getString(R.string.action_color);
            actionName = String.format(temp, rgb[0], rgb[1], rgb[2]);
        } else if (order.equals(DeviceOrder.STOP)) {
            actionName = res.getString(R.string.action_stop);
        } else if (order.equals(DeviceOrder.ALARM)) {
            actionName = res.getString(R.string.action_alarm);
        } else if (order.equals(DeviceOrder.DISALARM)) {
            actionName = res.getString(R.string.action_disalarm);
        } else if (order.equals(DeviceOrder.AC_CTRL)) {
            //获取温度和模式
            actionName = getWifiAcBindName(order, value1, value2, value3, value4);
        } else if (order.equals(DeviceOrder.CURTAIN_PAGE_UP)) {
            actionName = res.getString(R.string.curtain_page_up);
        } else if (order.equals(DeviceOrder.CURTAIN_PAGE_DOWN)) {
            actionName = res.getString(R.string.curtain_page_down);
        } else if (order.equals(DeviceOrder.SECURITY_DISARM)) {
            actionName = res.getString(R.string.cancel_security);
        } else if (order.equals(DeviceOrder.SECURITY_INHOME)) {
            actionName = res.getString(R.string.inhome_security);
        } else if (order.equals(DeviceOrder.SECURITY_OUTSIDE)) {
            actionName = res.getString(R.string.outhome_security);
        } else {
            actionName = getIrCommandName(context, order, deviceId);
        }
        switch (order) {
            case DeviceOrder.ON://on的指令要根据设备的类型再次判断一下
            case DeviceOrder.OFF:
            case DeviceOrder.AC_LOCK_SETTING:
            case DeviceOrder.AC_TEMPERATURE_SETTING:
            case DeviceOrder.AC_WIND_SETTING:
            case DeviceOrder.AC_MODE_SETTING:
                String bindName = getAcPanelBindName(order, value1, value2, value3, value4, deviceId);
                if (bindName != null && !bindName.isEmpty()) {
                    actionName = bindName;
                }
                break;
        }
        return actionName;
    }

    /**
     * 获取调光灯、RGB灯、色温灯亮度的百分比。
     *
     * @param value2 亮度
     * @return 百分值，比如：50
     */
    public static int getPrecent(int value2) {
        int percent = (int) (value2 * 1f * 100 / Constant.DIMMER_MAX);
        if (percent == 0 && value2 > 0) {
            percent = 1;
        }
        return percent;
    }

    /**
     * wifiac
     *
     * @param value1
     * @param value2
     * @param value3
     * @param value4
     * @return
     */
    private static String getWifiAcBindName(String order, int value1, int value2, int value3, int value4) {
        String actionName = "";
        Resources res = context.getResources();
        Acpanel mAcpanel = Acpanel.getInstance();
        mAcpanel.setValue1(value1);
        mAcpanel.setValue2(value2);
        mAcpanel.setValue3(value3);
        mAcpanel.setValue4(value4);
        int current_model = mAcpanel.getModel();
        int current_lock = mAcpanel.getLock();
        String current_order = order;
        //TODO 目前按照摄氏温度来做，后期根据需求，需要在摄氏温度和华氏温度之前切换显示
        int current_temperature = mAcpanel.getSetTemperature();
        int current_onOFf = mAcpanel.getOnoff();
        if (current_onOFf == ACPanelModelAndWindConstant.AC_OPEN) {
            if (current_model == ACPanelModelAndWindConstant.MODEL_AUTO) {
                actionName = res.getString(R.string.conditioner_auto);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_EXHAUST) {
                actionName = res.getString(R.string.conditioner_dehumidifier);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_SEND_WIND) {
                actionName = res.getString(R.string.conditioner_wind);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_COOL) {
                actionName = res.getString(R.string.conditioner_cold) + ": " + current_temperature + res.getString(R.string.conditioner_temperature_unit);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_HOT) {
                actionName = res.getString(R.string.conditioner_hot) + ": " + current_temperature + res.getString(R.string.conditioner_temperature_unit);
            }
            //lock的优先级比模式高
            if (DeviceOrder.AC_LOCK_SETTING.equals(current_order)) {
                if (current_lock == ACPanelModelAndWindConstant.AC_LOCK) {
                    actionName = res.getString(R.string.conditioner_lockunlock_lock);
                } else {
                    actionName = res.getString(R.string.conditioner_lockunlock_unlock);
                }
            }
        } else {
            actionName = res.getString(R.string.action_off);
        }
        return actionName;
    }

    /**
     * acpanel 需要注意的是acpanel的电源的开、关（on、off）的指令会和其他的设备使用同一个
     *
     * @param order
     * @param value1
     * @param value2
     * @param value3
     * @param value4
     * @param deviceId
     * @return
     */
    private static String getAcPanelBindName(String order, int value1, int value2, int value3, int value4, String deviceId) {
        String actionName = "";
        Device device = new DeviceDao().selDevice(deviceId);
        //主机删除设备没有把remoteBind删除，导致查找不到device
        if (device == null) {
            return actionName;
        }
        if (!(device.getDeviceType() == DeviceType.AC_PANEL)) {
            return actionName;
        }
        Resources res = context.getResources();
        Acpanel mAcpanel = Acpanel.getInstance();
        mAcpanel.setValue1(value1);
        mAcpanel.setValue2(value2);
        mAcpanel.setValue3(value3);
        mAcpanel.setValue4(value4);
        int current_model = mAcpanel.getModel();
        int current_lock = mAcpanel.getLock();
        String current_order = order;
        //TODO 目前按照摄氏温度来做，后期根据需求，需要在摄氏温度和华氏温度之前切换显示
        int current_temperature = mAcpanel.getSetTemperature(device.getDeviceType());
        int current_onOFf = mAcpanel.getOnoff();
        if (current_onOFf == ACPanelModelAndWindConstant.AC_OPEN) {
            if (current_model == ACPanelModelAndWindConstant.MODEL_AUTO) {
                actionName = res.getString(R.string.conditioner_auto);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_EXHAUST) {
                actionName = res.getString(R.string.conditioner_dehumidifier);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_SEND_WIND) {
                actionName = res.getString(R.string.conditioner_wind);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_COOL) {
                actionName = res.getString(R.string.conditioner_cold) + ": " + current_temperature + res.getString(R.string.conditioner_temperature_unit);
            } else if (current_model == ACPanelModelAndWindConstant.MODEL_HOT) {
                actionName = res.getString(R.string.conditioner_hot) + ": " + current_temperature + res.getString(R.string.conditioner_temperature_unit);
            }
            //lock的优先级比模式高
            if (DeviceOrder.AC_LOCK_SETTING.equals(current_order)) {
                if (current_lock == ACPanelModelAndWindConstant.AC_LOCK) {
                    actionName = res.getString(R.string.conditioner_lockunlock_lock);
                } else {
                    actionName = res.getString(R.string.conditioner_lockunlock_unlock);
                }
            }
        } else {
            actionName = res.getString(R.string.action_off);
        }
        return actionName;
    }

    private static String getIrCommandName(Context context, String order, String deviceId) {
        Resources res = context.getResources();
        String actionName = null;
        if (res == null || StringUtil.isEmpty(order)) {
            return actionName;
        }
        if (order.contains("3101")) {
            if (order.equals("310100")) {
                actionName = res.getString(R.string.tv_program_0);
            } else if (order.equals("310101")) {
                actionName = res.getString(R.string.tv_program_1);
            } else if (order.equals("310102")) {
                actionName = res.getString(R.string.tv_program_2);
            } else if (order.equals("310103")) {
                actionName = res.getString(R.string.tv_program_3);
            } else if (order.equals("310104")) {
                actionName = res.getString(R.string.tv_program_4);
            } else if (order.equals("310105")) {
                actionName = res.getString(R.string.tv_program_5);
            } else if (order.equals("310106")) {
                actionName = res.getString(R.string.tv_program_6);
            } else if (order.equals("310107")) {
                actionName = res.getString(R.string.tv_program_7);
            } else if (order.equals("310108")) {
                actionName = res.getString(R.string.tv_program_8);
            } else if (order.equals("310109")) {
                actionName = res.getString(R.string.tv_program_9);
            } else if (order.equals("310110")) {
                actionName = res.getString(R.string.tv_power);
            } else if (order.equals("310111")) {
                actionName = res.getString(R.string.tv_program_close);
            } else if (order.equals("310112")) {
                actionName = res.getString(R.string.tv_silence);
            } else if (order.equals("310113")) {
                actionName = res.getString(R.string.tv_volume_add);
            } else if (order.equals("310114")) {
                actionName = res.getString(R.string.tv_volume_minus);
            } else if (order.equals("310115")) {
                actionName = res.getString(R.string.tv_program_add);
            } else if (order.equals("310116")) {
                actionName = res.getString(R.string.tv_program_minus);
            } else if (order.equals("310119")) {
                actionName = res.getString(R.string.tv_menu);
            } else if (order.equals("310120")) {
                actionName = res.getString(R.string.tv_up);
            } else if (order.equals("310121")) {
                actionName = res.getString(R.string.tv_down);
            } else if (order.equals("310122")) {
                actionName = res.getString(R.string.tv_left);
            } else if (order.equals("310123")) {
                actionName = res.getString(R.string.tv_right);
            } else if (order.equals("310124")) {
                actionName = res.getString(R.string.tv_confirm);
            } else if (order.equals("310125")) {
                actionName = res.getString(R.string.tv_back);
            } else if (order.equals("310126")) {
                actionName = res.getString(R.string.tv_tv_av);
            } else if (order.equals("310118")) {
                actionName = res.getString(R.string.tv_menu);
            }

        } else if (order.contains("311")) {
            if (order.equals("311000")) {
                actionName = res.getString(R.string.conditioner_cold);
            } else if (order.equals("311001")) {
                actionName = res.getString(R.string.conditioner_dehumidifier);
            } else if (order.equals("311003")) {
                actionName = res.getString(R.string.conditioner_hot);
            } else if (order.equals("311004")) {
                actionName = res.getString(R.string.conditioner_close);
            } else if (order.equals("311005")) {
                actionName = res.getString(R.string.conditioner_low);
            } else if (order.equals("311006")) {
                actionName = res.getString(R.string.conditioner_middle);
            } else if (order.equals("311007")) {
                actionName = res.getString(R.string.conditioner_high);
            } else if (order.equals("311009")) {
                actionName = res.getString(R.string.conditioner_sweep);
            } else if (order.equals("311010")) {
                actionName = res.getString(R.string.conditioner_stop_sweep);
            } else if (order.equals("311011")) {
                actionName = res.getString(R.string.conditioner_open);
            } else if (order.equals("311016")) {
                actionName = 16 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311017")) {
                actionName = 17 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311018")) {
                actionName = 18 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311019")) {
                actionName = 19 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311020")) {
                actionName = 20 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311021")) {
                actionName = 21 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311022")) {
                actionName = 22 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311023")) {
                actionName = 23 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311024")) {
                actionName = 24 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311025")) {
                actionName = 25 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311026")) {
                actionName = 26 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311027")) {
                actionName = 27 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311028")) {
                actionName = 28 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311116")) {
                actionName = 16 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311117")) {
                actionName = 17 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311118")) {
                actionName = 18 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311119")) {
                actionName = 19 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311120")) {
                actionName = 20 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311121")) {
                actionName = 21 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311122")) {
                actionName = 22 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311123")) {
                actionName = 23 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311124")) {
                actionName = 24 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311125")) {
                actionName = 25 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311126")) {
                actionName = 26 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311127")) {
                actionName = 27 + res.getString(R.string.conditioner_temperature_unit);
            } else if (order.equals("311128")) {
                actionName = 28 + res.getString(R.string.conditioner_temperature_unit);
            }
        } else if (order.contains("3102")) {
            if (order.equals("310200")) {
                actionName = res.getString(R.string.tv_program_0);
            } else if (order.equals("310201")) {
                actionName = res.getString(R.string.tv_program_1);
            } else if (order.equals("310202")) {
                actionName = res.getString(R.string.tv_program_2);
            } else if (order.equals("310203")) {
                actionName = res.getString(R.string.tv_program_3);
            } else if (order.equals("310204")) {
                actionName = res.getString(R.string.tv_program_4);
            } else if (order.equals("310205")) {
                actionName = res.getString(R.string.tv_program_5);
            } else if (order.equals("310206")) {
                actionName = res.getString(R.string.tv_program_6);
            } else if (order.equals("310207")) {
                actionName = res.getString(R.string.tv_program_7);
            } else if (order.equals("310208")) {
                actionName = res.getString(R.string.tv_program_8);
            } else if (order.equals("310209")) {
                actionName = res.getString(R.string.tv_program_9);
            } else if (order.equals("310210")) {
                actionName = res.getString(R.string.tv_power);
            } else if (order.equals("310211")) {
                actionName = res.getString(R.string.tv_program_close);
            } else if (order.equals("310212")) {
                actionName = res.getString(R.string.tv_silence);
            } else if (order.equals("310213")) {
                actionName = res.getString(R.string.tv_volume_add);
            } else if (order.equals("310214")) {
                actionName = res.getString(R.string.tv_volume_minus);
            } else if (order.equals("310215")) {
                actionName = res.getString(R.string.tv_program_add);
            } else if (order.equals("310216")) {
                actionName = res.getString(R.string.tv_program_minus);
            } else if (order.equals("310219")) {
                actionName = res.getString(R.string.tv_menu);
            } else if (order.equals("310220")) {
                actionName = res.getString(R.string.tv_up);
            } else if (order.equals("310221")) {
                actionName = res.getString(R.string.tv_down);
            } else if (order.equals("310222")) {
                actionName = res.getString(R.string.tv_left);
            } else if (order.equals("310223")) {
                actionName = res.getString(R.string.tv_right);
            } else if (order.equals("310224")) {
                actionName = res.getString(R.string.tv_confirm);
            } else if (order.equals("310225")) {
                actionName = res.getString(R.string.tv_back);
            } else if (order.equals("310217")) {
                actionName = res.getString(R.string.tv_back);
            } else if (order.equals("310218")) {
                actionName = res.getString(R.string.tv_menu);
            } else if (order.equals("310226")) {
                actionName = res.getString(R.string.tv_change);
            } else if (order.equals("310241")) {
                actionName = res.getString(R.string.tv_home);
            }
        } else if (order.contains("37")) {
            String currentMainUid = UserCache.getCurrentMainUid(context);
            actionName = new DeviceIrDao().selKeyNameByCommand(currentMainUid, order, deviceId);
        }
        return actionName;
    }

    /**
     * 按协议排序设备
     *
     * @param devices          ORDER BY extAddr,endpoint ASC
     * @param emptySplitDevice 不同类型间添加空设备标志
     * @return
     */
    public static List<Device> sortDevices(List<Device> devices, boolean emptySplitDevice) {
        List<Device> linphoneDevices = new ArrayList<Device>();//可视对讲
        List<Device> backgroundMusicDevices = new ArrayList<Device>();//背景音乐
        List<Device> lightDevices = new ArrayList<Device>();//灯(灯光、调光灯、RGB灯排序)
        List<Device> socketDevices = new ArrayList<Device>();//插座
        List<Device> irDevices = new ArrayList<Device>();//红外(电视、机顶盒、空调、空气净化器排序)
        List<Device> infraredDevices = new ArrayList<Device>();//红外转发器
        List<Device> lockDevices = new ArrayList<Device>();//门锁
        List<Device> curtainDevices = new ArrayList<Device>();//窗帘
        List<Device> relayDevices = new ArrayList<Device>();//继电器
        List<Device> sensorDevices = new ArrayList<Device>();//安防传感器(人体红外、门磁、窗磁、烟雾报警器排序)
        List<Device> p2pCameraDevices = new ArrayList<Device>();//p2p摄像头
        List<Device> remoteDevices = new ArrayList<Device>();//遥控器
        List<Device> scenePanelDevices = new ArrayList<Device>();//情景面板
        List<Device> otherDevices = new ArrayList<Device>();//兼容

        for (Device device : devices) {
            final int deviceType = device.getDeviceType();
            final int appDeviceId = device.getAppDeviceId();
            final boolean isRoomNotSet = device.getRoomId().isEmpty();//true还没有设置房间
            if (deviceType == DeviceType.VIDEO_INTERCOM || appDeviceId == AppDeviceId.VIDEO_INTERCOM) {
                linphoneDevices.add(device);
            }
            //背景音乐
//            else  if (deviceType == DeviceType.VIDEO_INTERCOM || appDeviceId==AppDeviceId.VIDEO_INTERCOM) {
//                backgroundMusicDevices.add(device);
//            }
            else if (deviceType == DeviceType.DIMMER || deviceType == DeviceType.LAMP || deviceType == DeviceType.RGB) {
                lightDevices.add(device);
            } else if (deviceType == DeviceType.OUTLET) {
                socketDevices.add(device);
            } else if (DeviceUtil.isIrDevice(device)) {
                irDevices.add(device);
            } else if (deviceType == DeviceType.IR_REPEATER || appDeviceId == AppDeviceId.IR_REPEATER) {
                infraredDevices.add(device);
            } else if (deviceType == DeviceType.LOCK) {
                lockDevices.add(device);
            } else if (DeviceUtil.isCurtain(deviceType)) {
                curtainDevices.add(device);
            } else if (deviceType == DeviceType.CONTACT_RELAY || deviceType == DeviceType.SWITCH_RELAY || deviceType == DeviceType.REPEATER) {
                relayDevices.add(device);
            } else if (DeviceUtil.isSensor(deviceType)) {
                sensorDevices.add(device);
            } else if (deviceType == DeviceType.CAMERA) {
                p2pCameraDevices.add(device);
            } else if (deviceType == DeviceType.REMOTE || appDeviceId == AppDeviceId.REMOTE) {
                remoteDevices.add(device);
            } else if (deviceType == DeviceType.SCENE_KEYPAD || appDeviceId == AppDeviceId.SCENE_KEYPAD) {
                scenePanelDevices.add(device);
            } else {
                otherDevices.add(device);
            }
        }
        List<Device> sortDevices = new ArrayList<Device>();
        return sortDevices;
    }

    public static boolean isSecurityDevice(int deviceTyoe) {
        return deviceTyoe == DeviceType.FLAMMABLE_GAS || deviceTyoe == DeviceType.INFRARED_SENSOR || deviceTyoe == DeviceType.SMOKE_SENSOR;
    }


    /**
     * 获取所有可设置为常用设备的列表
     */
    public static List<Device> getAllCommonDevices(Context context) {
        List<Device> tempDevices = new ArrayList<Device>();
        String mainUid = UserCache.getCurrentMainUid(context);
        String userId = UserCache.getCurrentUserId(context);
        DeviceDao mDeviceDao = new DeviceDao();
        List<Device> wifiDevices = mDeviceDao.selWifiDevicesByUserId(userId);
        if (StringUtil.isEmpty(mainUid) && (wifiDevices == null || wifiDevices.isEmpty())) {
            return tempDevices;
        } else {
            tempDevices = mDeviceDao.selAllRoomControlDevicesByTypes(mainUid, DeviceUtil.getTypeSQL(5));
            List<Device> aDevices = new ArrayList<>();
            for (Device wifiDevice : wifiDevices) {
                boolean contains = false;
                String wifiDeviceId = wifiDevice.getDeviceId();
/*                if (StringUtil.isEmpty(wifiDeviceId) || wifiDevice.getDeviceType() == DeviceType.ALLONE) {
                    continue;
                }*/
                //为了在常用设备里面显示allone的子设备，去掉过滤
                if (StringUtil.isEmpty(wifiDeviceId)) {
                    continue;
                }
                for (Device tempDevice : tempDevices) {
                    if (wifiDeviceId.equals(tempDevice.getDeviceId())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    aDevices.add(wifiDevice);
                }
            }
            if (!aDevices.isEmpty()) {
                tempDevices.addAll(aDevices);
            }
            return tempDevices;
        }
    }

    /**
     * 筛选出房间里的灯和插座
     *
     * @param context
     * @return
     */
    public static List<Device> getLampAndSocket(Context context) {
        List<Device> tempDevices = new ArrayList<Device>();
        String mainUid = UserCache.getCurrentMainUid(context);
        DeviceDao mDeviceDao = new DeviceDao();
        tempDevices = mDeviceDao.selLampAndSocketByRoom(mainUid, DeviceUtil.getTypeSQL(8));
        return tempDevices;
    }

    /**
     * 获取某个房间下可设为常用设备的列表
     *
     * @return
     */
    public static List<Device> getCommonDevicesByRoom(Context context, String mCurrentRoomId) {
        List<Device> tempDevices = new ArrayList<Device>();
        String mainUid = UserCache.getCurrentMainUid(context);
        DeviceDao mDeviceDao = new DeviceDao();
        tempDevices = mDeviceDao.selCommonDevicesByTypesAndRoomId(mainUid, mCurrentRoomId, DeviceUtil.getTypeSQL(5));
        return tempDevices;
    }

    public static List<DeviceStatus> getDeviceStatusesByDevices(List<Device> tempDevices) {
        List<DeviceStatus> statuses = new ArrayList<DeviceStatus>();
        DeviceStatusDao mDeviceStatusDao = new DeviceStatusDao();
        for (Device device : tempDevices) {
            String uid = device.getUid();
            String deviceId = device.getDeviceId();
            DeviceStatus deviceStatus;
            if (DeviceUtil.isIrDevice(uid, deviceId)) {
                deviceStatus = mDeviceStatusDao.selIrDeviceStatus(uid, device.getExtAddr());
            } else {
                deviceStatus = mDeviceStatusDao.selDeviceStatus(uid, device);
            }
            if (deviceStatus != null) {
                statuses.add(deviceStatus);
            }
        }
        return statuses;
    }

    /**
     * 获取已设为常用设备列表。已过滤RGBW的W设备
     *
     * @return
     */
    public static List<Device> getCommonDevices(Context context) {
        List<Device> commonDevices = new ArrayList();
        List<Device> devices = getAllCommonDevices(context);
        for (Device device : devices) {
            if (device.getCommonFlag() == CommonFlag.COMMON) {
                //如果是创维rgbw灯，且类型不为rgb灯时，把另外一个灯屏蔽掉
                if (ProductManage.isSkyRGBW(device) && device.getDeviceType() != DeviceType.RGB) {
                    continue;
                }
                commonDevices.add(device);
            }
        }
        return commonDevices;
    }

    public static List<Device> getCommonDevices(List<Device> devices) {
        List<Device> commonDevices = new ArrayList();
        if (devices == null) {
            return commonDevices;
        }
        for (Device device : devices) {
            if (device.getCommonFlag() == CommonFlag.COMMON) {
                //如果是创维rgbw灯，且类型不为rgb灯时，把另外一个灯屏蔽掉
                if (ProductManage.isSkyRGBW(device) && device.getDeviceType() != DeviceType.RGB) {
                    continue;
                }
                commonDevices.add(device);
            }
        }
        return commonDevices;
    }

    /**
     * 判断2个设备对象是否为同一个真实设备
     *
     * @param device1
     * @param device2
     * @return true同一个设备
     */
    public static boolean isSameDevice(Device device1, Device device2) {
        if (device1 == null || device2 == null) {
            return false;
        }
        String extAddr1 = device1.getExtAddr();
        String extAddr2 = device2.getExtAddr();
        if (TextUtils.isEmpty(extAddr1) || TextUtils.isEmpty(extAddr2)) {
            return false;
        }
        return extAddr1.equals(extAddr2) && device1.getEndpoint() == device2.getEndpoint();
    }

    /**
     * 获取设备相反的动作。比如现在为开，那么返回的是关闭状态。
     *
     * @param device
     * @param ctrlValue1 当前的设备状态
     * @return 获取设备相反的动作
     */
    public static int getOppositeValue1(Device device, int ctrlValue1) {
        final int appDeviceId = device.getAppDeviceId();
        final int deviceType = device.getDeviceType();
        int oppositeValue1 = DeviceStatusConstant.ON;
        if (appDeviceId == AppDeviceId.SWITCH
                || appDeviceId == AppDeviceId.DIMMER_SWITCH
                || appDeviceId == AppDeviceId.OUTLET
                || appDeviceId == AppDeviceId.LAMP
                || appDeviceId == AppDeviceId.RGB
                || appDeviceId == AppDeviceId.RGB_CONTROLLER
                || appDeviceId == AppDeviceId.DIMMER
                || appDeviceId == AppDeviceId.SWITCH_LAMP
                || appDeviceId == AppDeviceId.COCO
                || deviceType == DeviceType.DIMMER
                || deviceType == DeviceType.LAMP
                || deviceType == DeviceType.RGB
                || deviceType == DeviceType.OUTLET
                || deviceType == DeviceType.SWITCH_RELAY
                || deviceType == DeviceType.COLOR_TEMPERATURE_LAMP
                || deviceType == DeviceType.COCO
                || ProductManage.getInstance().isWifiOnOffDevice(device)
                ) {
            if (ctrlValue1 == DeviceStatusConstant.ON) {
                oppositeValue1 = DeviceStatusConstant.OFF;
            } else {
                oppositeValue1 = DeviceStatusConstant.ON;
            }
        }
        return oppositeValue1;
    }

    /**
     * 根据未绑定的设备获取wifi设备名称
     *
     * @param deviceQueryUnbind
     * @return
     */
    public static String getWifiDeviceName(DeviceQueryUnbind deviceQueryUnbind) {

        String deviceName = "";
        if (deviceQueryUnbind != null) {
            String model = deviceQueryUnbind.getModel();

            if (ProductManage.getInstance().isOrviboCOCO(model)) {
                deviceName = context.getString(R.string.device_type_COCO_43);
            } else if (ProductManage.getInstance().isS20orS25(model)) {
                deviceName = context.getString(R.string.device_add_s20c);
            } else if (ProductManage.getInstance().isAllone2(model)) {
                deviceName = context.getString(R.string.device_add_xiaofang_tv);
            } else if (ProductManage.getInstance().isLiangBa(model)) {
                deviceName = context.getString(R.string.device_add_liangba);
            } else if (ProductManage.getInstance().isAoKe(model)) {
                deviceName = context.getString(R.string.device_add_aoke_liangyi);
            } else if (ProductManage.getInstance().isOuJia(model)) {
                deviceName = context.getString(R.string.device_add_oujia);
            } else if (ProductManage.getInstance().isMaiRun(model)) {
                deviceName = context.getString(R.string.device_add_mairunclothes);
            } else if (ProductManage.getInstance().isMethanal(model)) {

            } else if (ProductManage.getInstance().isCo(model)) {

            } else if (ProductManage.getInstance().isXiaoOuCamera(model)) {

                deviceName = context.getString(R.string.xiao_ou_camera);
            }

            String language = context.getResources().getConfiguration().locale.getLanguage();
            DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(model);
            if (deviceDesc != null) {
                DeviceLanguage deviceLanguage = new DeviceLanguageDao().selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
                if (deviceLanguage != null) {
                    deviceName = deviceLanguage.getProductName();
                    return deviceName;
                }
            }

        }
        return deviceName;
    }

}
