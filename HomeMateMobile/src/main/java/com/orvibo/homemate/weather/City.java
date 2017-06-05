package com.orvibo.homemate.weather;

import java.io.Serializable;

public class City implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String province;
	private String name;
	private String number;
	private String pinyin;
	private String py;
	private String provincepinyin;
	private String provincepy;

	public City() {
	}

	public City(String province, String name, String number, String pinyin, String py, String provincepinyin, String provincepy) {
		this.province = province;
		this.name = name;
		this.number = number;
		this.pinyin = pinyin;
		this.py = py;
		this.provincepinyin = provincepinyin;
		this.provincepy = provincepy;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getPy() {
		return py;
	}

	public void setPy(String py) {
		this.py = py;
	}

	public String getProvincepinyin() {
		return provincepinyin;
	}

	public void setProvincepinyin(String provincepinyin) {
		this.provincepinyin = provincepinyin;
	}

	public String getProvincepy() {
		return provincepy;
	}

	public void setProvincepy(String provincepy) {
		this.provincepy = provincepy;
	}

	@Override
	public String toString() {
		return "City{" +
				"province='" + province + '\'' +
				", name='" + name + '\'' +
				", number='" + number + '\'' +
				", pinyin='" + pinyin + '\'' +
				", py='" + py + '\'' +
				", provincepinyin='" + provincepinyin + '\'' +
				", provincepy='" + provincepy + '\'' +
				'}';
	}
}
