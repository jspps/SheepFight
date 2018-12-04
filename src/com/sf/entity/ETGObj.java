package com.sf.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 枚举 - 对象类型<br/>
 * ET = Enum_Type
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午9:13:20
 */
public enum ETGObj {
	SheepSmall(1, "小羊", 1,0.6), SheepMiddle(2, "中羊", 2,0.78), SheepBig(3, "大羊", 4, 1.05), 
	SheepNeutral(4,"叛变羊", 1), Wolf(5, "狼", 1.32), Spinach(6, "菠菜罐头", 7000, 0.6),;
	// 取得对象的方法
	static public ETGObj get(int index) {
		for (ETGObj got : ETGObj.values()) {
			if (got.getIndex() == index) {
				return got;
			}
		}
		return null;
	}

	static public ETGObj get(String enum_name) {
		for (ETGObj got : ETGObj.values()) {
			if (got.name().equalsIgnoreCase(enum_name)) {
				return got;
			}
		}
		return null;
	}

	// 成员变量
	private String name; // 自定义名字
	private int index; // 唯一标识int值
	private int power;// 力量or总体时长(秒)
	private double speed;// 速度单位Unit/秒 - 这个单位：可能是米，有可能是一个单元格
	private double volume;// 体积单位与速度一个单位Unit

	// get set 方法
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	// 构造方法
	private ETGObj(int index, String name, int power, double speed, double volume) {
		this.name = name;
		this.index = index;
		this.power = power;
		this.speed = speed;
		this.volume = volume;
	}

	private ETGObj(int index, String name, int power, double volume) {
		this(index, name, power, 1, volume);
	}

	private ETGObj(int index, String name, double volume) {
		this(index, name, 0,volume);
	}

	private ETGObj(int index, String name) {
		this(index, name, 0.5d);
	}

	public String toStr() {
		return String.format("%s_%s_%s_%s_%s_%s_%s", name(), ordinal(), index, name, power, speed, volume);
	}

	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put("index", index);
		map.put("name", name);
		return map;
	}

	public Map<String, Object> toMap() {
		return toMap(null);
	}
}