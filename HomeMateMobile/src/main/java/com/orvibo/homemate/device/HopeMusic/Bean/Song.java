package com.orvibo.homemate.device.HopeMusic.Bean;

import java.io.Serializable;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;

/**
 * Created by wuliquan on 2016/5/16.
 */
public class Song extends DevicePlayState implements Serializable {
    private String Id;
    private boolean isSelect;
    private boolean isPlaying;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

}
