package com.orvibo.homemate.device.HopeMusic.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orvibo.homemate.device.HopeMusic.Bean.DeviceSongBean;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;

import java.util.ArrayList;

import cn.nbhope.smarthomelib.app.enity.Music;

public class MusicUtil {

	private static MusicUtil mInstance = null;

	private ArrayList<Music> mArtist = new ArrayList<Music>();

	public static MusicUtil getInstance() {

		if (mInstance == null) {
			mInstance = new MusicUtil();
		}
		return mInstance;
	}

	private MusicUtil() {
	}

	public int getPosOfObject(String s, ArrayList<Music> musics) {
		int result = -1;
		if (!TextUtils.isEmpty(s) && musics.size() != 0) {
			for (int i = 0; i < musics.size(); i++) {
				if (musics.get(i).getPosition().equals(s)) {
					result = i;
				}
			}
		}

		return result;
	}

	public ArrayList<Music> sortMusicByArtist(ArrayList<Music> musics) {

		if (musics == null && musics.size() == 0) {
			return null;
		}
		mArtist.clear();
		for (Music m : musics) {
			if (mArtist.size() == 0) {
				mArtist.add(m);
			} else {
				boolean contain = false;
				for (Music temp : mArtist) {
					if (temp.getArtist().equalsIgnoreCase(m.getArtist())) {
						contain = true;
					}
				}
				if (!contain) {
					mArtist.add(m);
				}
			}

		}
		return mArtist;
	}

	public static String durationToString(int duration) {
		String reVal = "";
		int i = duration;
		int min = (int) i / 60;
		int sec = i % 60;
		if (min > 9) {
			if (sec > 9) {
				reVal = min + ":" + sec;
			}
			if (sec <= 9) {
				reVal = min + ":0" + sec;
			}
		} else {
			if (sec > 9) {
				reVal = "0" + min + ":" + sec;
			}
			if (sec <= 9) {
				reVal = "0" + min + ":0" + sec;
			}
		}
		return reVal;
	}

	public static DeviceSongBean Jsonobject2DeviceSongBean(JsonObject jsonObject){
		if(jsonObject!=null){
			JsonElement deviceIdElement=jsonObject.get("DeviceId");
			JsonElement totalElement = jsonObject.get("Total");
			JsonElement jsonElement = jsonObject.get("SongList");
			if(deviceIdElement!=null){
				DeviceSongBean bean = new DeviceSongBean();
				bean.setDeviceId(deviceIdElement.getAsString());
				bean.setTotal(totalElement!=null?totalElement.getAsInt():0);
				ArrayList<Song> songList=JsonElement2List(deviceIdElement.getAsString(),jsonElement);
				if(songList!=null)
				bean.setSongList(songList);
				return bean;
			}
		}
		return null;

	}
	public static ArrayList<Song> JsonElement2List(String deviceId, JsonElement jsonElement){
		if (jsonElement!=null){
			ArrayList<Song> songList = new ArrayList<Song>();
			JsonArray asJsonArray = jsonElement.getAsJsonArray();
			int size=asJsonArray.size();
			for(int i=0;i<size;i++){
				JsonElement jsonElement1 = asJsonArray.get(i);
				Song song = JsonElement2Song(deviceId,jsonElement1);
				if (song!=null)
				songList.add(song);
			}
			return  songList;
		}
		Log.e("test","JsonElement2List null");
		return null;
	}
	public static Song JsonElement2Song(String deiceId, JsonElement jsonElement){
		if (jsonElement!=null){
             JsonObject jsonobject = jsonElement.getAsJsonObject();
			 JsonElement idElement = jsonobject.get("id");
			 JsonElement positionElement = jsonobject.get("position");
			 JsonElement durationElement = jsonobject.get("duration");
			 JsonElement titleElement = jsonobject.get("title");
			 JsonElement albumElement = jsonobject.get("album");
			 JsonElement artistElement = jsonobject.get("artist");
			 JsonElement imgElement = jsonobject.get("img");

			if(idElement!=null){
				Song song = new Song();
				song.setDeviceId(deiceId);
				song.setIndex(positionElement.getAsString());
				song.setDuration(durationElement.getAsInt()/1000);
				song.setTitle(titleElement.getAsString());
				song.setAlbum(albumElement.getAsString());
				song.setArtist(artistElement.getAsString());
				song.setImg(imgElement.getAsString());
				return song;
			}
		}
		Log.e("test","JsonElement2Song null");
		return null;
	}
}
