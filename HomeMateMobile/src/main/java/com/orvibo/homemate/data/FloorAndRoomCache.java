package com.orvibo.homemate.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.StringUtil;

public class FloorAndRoomCache {
	private static final String KEY = "floorAndRoom";

	/**
	 * 保存当前选择的楼层和房间
	 * 
	 * @param context
	 * @param uid
	 * @param floorId
	 * @param roomId
	 */
	public static void saveFloorAndRoom(Context context, String uid,
			int floorId, int roomId) {
		if (context == null || StringUtil.isEmpty(uid)) {
			return;
		}
		synchronized (KEY) {
			SharedPreferences sp = context.getSharedPreferences(
					Constant.SPF_NAME, Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putInt(getFloorKey(context, uid), floorId);
			editor.putInt(getRoomKey(context, uid), roomId);
			editor.commit();
		}
	}

	/**
	 * 获取上一次选择的楼层
	 * 
	 * @param context
	 * @param uid
	 * @return
	 */
	public static int getLastFloor(Context context, String uid) {
		if (context == null || StringUtil.isEmpty(uid)) {
			return Constant.INVALID_NUM;
		}
		synchronized (KEY) {
			return context.getSharedPreferences(Constant.SPF_NAME,
					Context.MODE_PRIVATE).getInt(getFloorKey(context, uid),
					Constant.INVALID_NUM);
		}
	}

	/**
	 * 获取上一次选择的房间
	 * 
	 * @param context
	 * @param uid
	 * @return
	 */
	public static int getLastRoom(Context context, String uid) {
		if (context == null || StringUtil.isEmpty(uid)) {
			return Constant.INVALID_NUM;
		}
		synchronized (KEY) {
			return context.getSharedPreferences(Constant.SPF_NAME,
					Context.MODE_PRIVATE).getInt(getRoomKey(context, uid),
					Constant.INVALID_NUM);
		}
	}

	private static String getFloorKey(Context context, String uid) {
		return getKey(context, uid) + "_floor";
	}

	private static String getRoomKey(Context context, String uid) {
		return getKey(context, uid) + "_room";
	}

	private static String getKey(Context context, String uid) {
		return UserCache.getCurrentUserName(context) + "_" + uid;
	}
}
