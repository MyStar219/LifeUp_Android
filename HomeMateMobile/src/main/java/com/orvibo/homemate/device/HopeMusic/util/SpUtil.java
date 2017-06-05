package com.orvibo.homemate.device.HopeMusic.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreference工具类
 * 
 * @author
 * 
 */
public class SpUtil {
	private SharedPreferences sp;

	public SpUtil(SharedPreferences sp) {
		super();
		this.sp = sp;
	}

	public boolean set(String key, Object value) {
		Editor e = sp.edit();
		// String signKey = TWUtils.encryptStr(key);
		// String result = TWUtils.encryptStr(value.toString());
		String result = value.toString();
		e.putString(key, result);
		return e.commit();
	}

	public Object get(String key, Object defValue) {
		Object value = null;
		// String signKey = TWUtils.encryptStr(key);
		String result = sp.getString(key, defValue.toString());
		if (defValue.toString().equals(result)) {
			return defValue;
		}
		// String signresult = TWUtils.decodeStr(result.toString());
		String signResult = result.trim();

		if (defValue instanceof Boolean) {
			value = Boolean.parseBoolean(signResult);
		} else if (defValue instanceof Float) {
			value = Float.parseFloat(signResult);
		} else if (defValue instanceof Long) {
			value = Long.parseLong(signResult);
		} else if (defValue instanceof Integer) {
			value = Integer.parseInt(signResult);
		} else if (defValue instanceof String) {
			value = signResult;
		}

		return value;

	}

	public void clear() {
		sp.edit().clear().apply();
	}

}
