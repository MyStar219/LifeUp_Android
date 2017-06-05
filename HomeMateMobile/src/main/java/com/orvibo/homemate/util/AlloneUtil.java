package com.orvibo.homemate.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.hzy.tvmao.ir.Device;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.KKookongFid;
import com.orvibo.homemate.device.allone2.FanControlFragment;
import com.orvibo.homemate.device.allone2.ProjectorControlFragment;
import com.orvibo.homemate.device.allone2.STBControlFragment;
import com.orvibo.homemate.device.allone2.SpeakerBoxControlFragment;
import com.orvibo.homemate.device.allone2.TVControlFragment;
import com.orvibo.homemate.device.allone2.TvBoxControlFragment;
import com.orvibo.homemate.device.allone2.add.DeviceBrandListActivity;
import com.orvibo.homemate.device.allone2.add.RemoteMatchOrCopyActivity;
import com.orvibo.homemate.device.allone2.add.RemoteNameAddActivity;

/**
 * Created by snown on 2016/7/8.
 *
 * @描述: allone不同设备类型获取数据的辅助类，添加新的设备时主要在此修改即可
 */
public class AlloneUtil {
    private static Context context = ViHomeProApp.getContext();

    /**
     * 根据设备类型获取设备名称
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceName(int deviceType) {
        String deviceName = null;
        switch (deviceType) {
            case DeviceType.AC:
                deviceName = context.getString(R.string.air_conditioner);
                break;
            case DeviceType.TV:
                deviceName = context.getString(R.string.tv_zh);
                break;
            case DeviceType.STB:
                deviceName = context.getString(R.string.device_set_ir_repeater_tv_box);
                break;
            case DeviceType.FAN:
                deviceName = context.getString(R.string.device_fan);
                break;
            case DeviceType.TV_BOX:
                deviceName = context.getString(R.string.device_tv_box);
                break;
            case DeviceType.SPEAKER_BOX:
                deviceName = context.getString(R.string.device_speaker_box);
                break;
            case DeviceType.PROJECTOR:
                deviceName = context.getString(R.string.device_projector);
                break;
        }
        return deviceName;
    }

    /**
     * 将本地设备类型转换为酷控的设备类型
     *
     * @param deviceType
     * @return
     */
    public static int getKKDeviceType(int deviceType) {
        int realType = Device.AC;
        switch (deviceType) {
            case DeviceType.AC:
                realType = Device.AC;
                break;
            case DeviceType.STB:
                realType = Device.STB;
                break;
            case DeviceType.TV:
                realType = Device.TV;
                break;
            case DeviceType.FAN:
                realType = Device.FAN;
                break;
            case DeviceType.TV_BOX:
                realType = Device.BOX;
                break;
            case DeviceType.SPEAKER_BOX:
                realType = Device.AV;
                break;
            case DeviceType.PROJECTOR:
                realType = Device.PROJECTOR;
                break;
        }
        return realType;
    }

    /**
     * 将酷控的设备类型转换为本地设备类型
     *
     * @param deviceType
     * @return
     */
    public static int getLocalDeviceType(int deviceType) {
        int realType = Device.AC;
        switch (deviceType) {
            case Device.AC:
                realType = DeviceType.AC;
                break;
            case Device.STB:
                realType = DeviceType.STB;
                break;
            case Device.TV:
                realType = DeviceType.TV;
                break;
            case Device.FAN:
                realType = DeviceType.FAN;
                break;
            case Device.BOX:
                realType = DeviceType.TV_BOX;
                break;
            case Device.AV:
                realType = DeviceType.SPEAKER_BOX;
                break;
            case Device.PROJECTOR:
                realType = DeviceType.PROJECTOR;
                break;
        }
        return realType;
    }

    /**
     * 添加设备时跳转的intent
     *
     * @return
     */
    public static Intent getAddIntent(int viewId, Activity activity) {
        Intent intent = new Intent(activity, DeviceBrandListActivity.class);
        switch (viewId) {
            case R.id.ll_add_device_stb:
                intent.setClass(activity, RemoteMatchOrCopyActivity.class);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.STB);
                break;
            case R.id.ll_add_device_ac:
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.AC);
                break;
            case R.id.ll_add_device_tv:
                intent.setClass(activity, RemoteMatchOrCopyActivity.class);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.TV);
                break;
            case R.id.ll_add_device_fan:
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.FAN);
                break;
            case R.id.ll_add_self_remote:
                intent.setClass(activity, RemoteNameAddActivity.class);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.SELF_DEFINE_IR);
                break;
            case R.id.ll_add_device_tv_box:
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.TV_BOX);
                break;
            case R.id.ll_add_device_projection:
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.PROJECTOR);
                break;
            case R.id.ll_add_device_audio:
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.SPEAKER_BOX);
                break;
        }
        return intent;
    }


    /**
     * 根据设备类型获取对应的fragment
     *
     * @param deviceType
     * @return
     */
    public static Fragment getRemoteFragment(int deviceType, Fragment fragment) {
        switch (deviceType) {
            case DeviceType.STB:
                fragment = new STBControlFragment();
                break;
            case DeviceType.TV:
                fragment = new TVControlFragment();
                break;
            case DeviceType.FAN:
                fragment = new FanControlFragment();
                break;
            case DeviceType.TV_BOX:
                fragment = new TvBoxControlFragment();
                break;
            case DeviceType.PROJECTOR:
                fragment = new ProjectorControlFragment();
                break;
            case DeviceType.SPEAKER_BOX:
                fragment = new SpeakerBoxControlFragment();
                break;
        }
        return fragment;
    }


    /**
     * 小方子设备是否有定时和倒计时
     *
     * @param deviceType
     * @return
     */
    public static boolean hasTiming(int deviceType) {
        return deviceType == DeviceType.FAN || deviceType == DeviceType.AC || deviceType == DeviceType.SPEAKER_BOX || deviceType == DeviceType.SELF_DEFINE_IR;
    }

    /**
     * 通过fid获取按键名称
     *
     * @return
     */
    public static String getNameByFid(int fid) {
        String name = "";
        switch (fid) {
            case KKookongFid.fid_45_menu:
                name = context.getString(R.string.allone_menu);
                break;
            case KKookongFid.fid_46_navigate_up:
                name = context.getString(R.string.tv_up);
                break;
            case KKookongFid.fid_47_navigate_down:
                name = context.getString(R.string.tv_down);
                break;
            case KKookongFid.fid_48_navigate_left:
                name = context.getString(R.string.tv_left);
                break;
            case KKookongFid.fid_49_navigate_right:
                name = context.getString(R.string.tv_right);
                break;
            case KKookongFid.fid_50_volume_up:
                name = context.getString(R.string.tv_volume_add);
                break;
            case KKookongFid.fid_51_volume_down:
                name = context.getString(R.string.tv_volume_minus);
                break;
            case KKookongFid.fid_201_previous:
                name = context.getString(R.string.change_music_previous);
                break;
            case KKookongFid.fid_206_next:
                name = context.getString(R.string.change_music_next);
                break;
            case KKookongFid.fid_106_mute:
                name = context.getString(R.string.tv_silence);
                break;
            case KKookongFid.fid_116_back:
                name = context.getString(R.string.tv_back);
                break;
            case KKookongFid.fid_43_channel_up:
                name = context.getString(R.string.allone_channle_up);
                break;
            case KKookongFid.fid_44_channel_down:
                name = context.getString(R.string.allone_channle_down);
                break;
            case KKookongFid.fid_own_power:
                name = context.getString(R.string.allone_tv);
                break;
            case KKookongFid.fid_2807_zoom_up:
                name = context.getString(R.string.zoom_up);
                break;
            case KKookongFid.fid_2812_zoom_down:
                name = context.getString(R.string.zoom_down);
                break;
            case KKookongFid.fid_136_homepage:
                name = context.getString(R.string.tv_home);
                break;
        }
        return name;
    }
}
