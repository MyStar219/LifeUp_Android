package com.orvibo.homemate.device.HopeMusic.util;

import java.util.ArrayList;

import cn.nbhope.smarthomelib.app.enity.MusicClassify;


/**
 * 歌曲列表
 * 
 * @author qianjiang
 *
 */
public class Constants {
	public static ArrayList<MusicClassify> getData() {
		ArrayList<MusicClassify> newsClassify = new ArrayList<MusicClassify>();
		MusicClassify classify = new MusicClassify();
		classify.setId(0);
		classify.setTitle("歌曲");
		newsClassify.add(classify);
		classify = new MusicClassify();
		classify.setId(1);
		classify.setTitle("艺人");
		newsClassify.add(classify);
		classify = new MusicClassify();
		classify.setId(2);
		classify.setTitle("专辑");
		newsClassify.add(classify);
		return newsClassify;
	}
}
