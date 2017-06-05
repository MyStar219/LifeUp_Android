package com.orvibo.homemate.weather;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class CityDB {
	public static final String CITY_DB_NAME = "city.db";
	private static final String CITY_TABLE_NAME = "city";
	private SQLiteDatabase db;

	public CityDB(Context context, String path) {
		db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
	}

	public boolean isOpen() {
		return db != null && db.isOpen();
	}

	public void close() {
		if (db != null && db.isOpen())
			db.close();
	}

	public List<City> getAllCity() {
		List<City> list = new ArrayList<City>();
		Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME +" order by province", null);
		while (c.moveToNext()) {
			String province = c.getString(c.getColumnIndex("province"));
			String city = c.getString(c.getColumnIndex("name"));
			String number = c.getString(c.getColumnIndex("number"));
			String pinyin = c.getString(c.getColumnIndex("pinyin"));
			String py = c.getString(c.getColumnIndex("py"));
			String provincepinyin = c.getString(c.getColumnIndex("provincepinyin"));
			String provincepy = c.getString(c.getColumnIndex("provincepy"));
			City item = new City(province, city, number, pinyin, py,provincepinyin,provincepy);
			list.add(item);
		}
		return list;
	}

	public City getCity(String city) {
		if (TextUtils.isEmpty(city))
			return null;
		City item = getCityInfo(city);//先全部搜索
		if (item == null) {
			item = getCityInfo(parseName(city));//处理一下之后再搜索
		}
		return item;
	}

	/**
	 * 根据城市拼音获取城市名称
	 * @param city
	 * @return
	 */
	public City getCityInfoByPinyin(String city) {
		if (TextUtils.isEmpty(city))
			return null;
		City item = getCityByPinyinInfo(city);//先全部搜索
		if (item == null) {
			item = getCityByPinyinInfo(parseName(city));//处理一下之后再搜索
		}
		return item;
	}

	/**
	 * 去掉市或县搜索
	 * 
	 * @param city
	 * @return
	 */
	private String parseName(String city) {
		city = city.replaceAll("市$", "").replaceAll("县$", "")
				.replaceAll("区$", "");
		return city;
	}

	private City getCityInfo(String city) {
		City item = null;
		Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME
				+ " where name=?", new String[] { city });
		if (c.moveToFirst()) {
			String province = c.getString(c.getColumnIndex("province"));
			String name = c.getString(c.getColumnIndex("name"));
			String number = c.getString(c.getColumnIndex("number"));
			String pinyin = c.getString(c.getColumnIndex("pinyin"));
			String py = c.getString(c.getColumnIndex("py"));
			String provincepinyin = c.getString(c.getColumnIndex("provincepinyin"));
			String provincepy = c.getString(c.getColumnIndex("provincepy"));
			item = new City(province, name, number, pinyin, py,provincepinyin,provincepy);
		}
		return item;
	}

	private City getCityByPinyinInfo(String city) {
		City item = null;
		Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME
				+ " where pinyin=?", new String[] { city });
		if (c.moveToFirst()) {
			String province = c.getString(c.getColumnIndex("province"));
			String name = c.getString(c.getColumnIndex("name"));
			String number = c.getString(c.getColumnIndex("number"));
			String pinyin = c.getString(c.getColumnIndex("pinyin"));
			String py = c.getString(c.getColumnIndex("py"));
			String provincepinyin = c.getString(c.getColumnIndex("provincepinyin"));
			String provincepy = c.getString(c.getColumnIndex("provincepy"));
			item = new City(province, name, number, pinyin, py,provincepinyin,provincepy);
		}
		return item;
	}

	/**
	 * 查询本地数据库是否有这个城市
	 * @param city
	 * @return
	 */
	public boolean hasCity(String city) {
		Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME
				+ " where name=?", new String[] { city });
		if (c.moveToFirst()) {
			return true;
		}
		return false;
	}
}
