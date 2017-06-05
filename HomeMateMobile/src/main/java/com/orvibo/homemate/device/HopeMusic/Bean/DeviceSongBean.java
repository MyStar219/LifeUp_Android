package com.orvibo.homemate.device.HopeMusic.Bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wuliquan on 2016/5/26.
 */
public class DeviceSongBean implements Serializable {
    /**
         "DeviceId": 815, 设备的id
         "Total":    11,  该设备下歌曲的总数
         "SongList"       该设备下歌曲的列表
     */
    private String deviceId;
    private int total;
    private ArrayList<Song> songList;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }



    @Override
    public String toString() {
        return "DeviceSongBean{" +
                "deviceId=" + deviceId +
                ", total=" + total +
                ", songList=" + songList.toString() +
                '}';
    }
}
