package com.orvibo.homemate.util;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.SceneType;

public class SceneTool {
    /**
     * 根据sceneType得到对应的img res id。这些图片用于情景列表界面。
     *
     * @param sceneType
     * @return
     */
    public static int getSceneIconResId(int sceneType) {
        int resId = 0;
        if (sceneType == SceneType.ALL_OFF) {
            resId = R.drawable.scene_all_off;
        } else if (sceneType == SceneType.ALL_ON) {
            resId = R.drawable.scene_all_on;
        } else if (sceneType == SceneType.AT_HOME) {
            resId = R.drawable.scene_at_home;
        } else if (sceneType == SceneType.LEAVE) {
            resId = R.drawable.scene_leave;
        } else if (sceneType == SceneType.MOVIE) {
            resId = R.drawable.scene_movie;
        } else if (sceneType == SceneType.REST) {
            resId = R.drawable.scene_rest;
        } else if (sceneType == SceneType.DINNER) {
            resId = R.drawable.scene_dinner;
        } else if (sceneType == SceneType.CUSTOMER) {
            resId = R.drawable.scene_customer;
        } else {
            resId = R.drawable.scene_other;
        }
        return resId;
    }

    /**
     * sceneType
     *
     * @param sceneType
     * @return
     */
    public static int getSceneTypeNameResId(int sceneType) {
        int resId = R.string.app_name;
        if (sceneType == SceneType.ALL_OFF) {
            resId = R.string.scene_all_off;
        } else if (sceneType == SceneType.ALL_ON) {
            resId = R.string.scene_all_on;
        } else if (sceneType == SceneType.AT_HOME) {
            resId = R.string.scene_at_home;
        } else if (sceneType == SceneType.LEAVE) {
            resId = R.string.scene_leave;
        } else if (sceneType == SceneType.MOVIE) {
            resId = R.string.scene_movie;
        } else if (sceneType == SceneType.REST) {
            resId = R.string.scene_rest;
        } else if (sceneType == SceneType.DINNER) {
            resId = R.string.scene_dinner;
        } else if (sceneType == SceneType.CUSTOMER) {
            resId = R.string.scene_customer;
        } else if (sceneType == SceneType.OTHER) {
            resId = R.string.scene_other;
        }
        return resId;
    }
}
