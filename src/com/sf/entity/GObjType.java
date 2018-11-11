package com.sf.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象类型
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午9:13:20
 */
public enum GObjType {
	SheepSmall("小羊", 1, 1), SheepMiddle("中羊", 2, 2), SheepBig("大羊", 3, 4), SheepNeutral(
			"叛变羊", 4), Wolf("狼", 5), Spinach("菠菜罐头", 6, 7), ;
	// 取得对象的方法
	static public GObjType get(int index) {
		for (GObjType got : GObjType.values()) {
			if (got.getIndex() == index) {
				return got;
			}
		}
		return null;
	}

	static public GObjType get(String enum_name) {
		for (GObjType got : GObjType.values()) {
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
	private int speed;// 速度单位Unit/秒 - 这个单位：可能是米，有可能是一个单元格

	// 构造方法
	private GObjType(String name, int index, int power, int speed) {
		this.name = name;
		this.index = index;
		this.power = power;
		this.speed = speed;
	}

	private GObjType(String name, int index, int power) {
		this(name, index, power, 1);
	}

	private GObjType(String name, int index) {
		this(name, index, 0);
	}

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

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String toStr() {
		return String.format("%s_%s_%s_%s_%s_%s", name(), ordinal(), index,
				name, power, speed);
	}

	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put("index", index);
		map.put("name", name);
		map.put("power", power);
		map.put("speed", speed);
		return map;
	}

	public Map<String, Object> toMap() {
		return toMap(null);
	}
}