package com.orvibo.homemate.device.HopeMusic.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/***
 * GsonUtils 工具包
 * 
 * @author qianjiang (123567549@qq.com)
 *
 */
public final class GsonUtils {
	public static <T> T parseJson(String jsonObj, Class<T> clazz) {
		Gson gson = new Gson();
		T t = gson.fromJson(jsonObj, clazz);
		return t;
	}

	/**
	 *
	 * @param jsonArr
	 * @param type
	 *            例如：// Type type = new TypeToken&lt;ArrayList&lt;NewCourse>>()
	 *            {}.getType();
	 * @param <T>
	 * @return
	 */
	public static <T> T parseJson(String jsonArr, Type type) {
		Gson gson = new Gson();
		T t = gson.fromJson(jsonArr, type);
		return t;
	}

	private GsonUtils() {
	}
}
