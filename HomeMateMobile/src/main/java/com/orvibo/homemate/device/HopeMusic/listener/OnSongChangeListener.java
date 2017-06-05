package com.orvibo.homemate.device.HopeMusic.listener;


import com.orvibo.homemate.device.HopeMusic.Bean.Song;

/**
 * Created by yu on 2016/5/16.
 */
public interface OnSongChangeListener {
    void change(String cmd, boolean isSelect, Song song);
}
