package com.sf.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午9:05:32
 */
public class Player extends BeanOrigin {
	private static final long serialVersionUID = 1L;
	String name; // 名字
	String icon; // 头像
	int forage; // 草料
	long roomid;// 房间号

	List<GObject> lGobjRunning = new ArrayList<GObject>();
	List<Map<String, Object>> lMap = new ArrayList<Map<String, Object>>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getForage() {
		return forage;
	}

	public void setForage(int forage) {
		this.forage = forage;
	}

	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public List<GObject> getlGobjRunning() {
		return lGobjRunning;
	}

	public Player() {
		super();
		this.forage = GObjConfig.N_Init_Forage;
	}

	public Player(String name, String icon, int forage, long roomid) {
		super();
		this.name = name;
		this.icon = icon;
		this.forage = forage;
		this.roomid = roomid;
	}

	public Player(String name, String icon, long roomid) {
		this(name, icon, GObjConfig.N_Init_Forage, roomid);
	}

	public List<Map<String, Object>> listMap() {
		lMap.clear();
		for (GObject item : lGobjRunning) {
			lMap.add(item.toMap());
		}
		return lMap;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("icon", icon);
		map.put("forage", forage);
		map.put("roomid", roomid);
		map.put("small", GObjType.SheepSmall.toMap());
		map.put("middle", GObjType.SheepMiddle.toMap());
		map.put("big", GObjType.SheepBig.toMap());
		map.put("listRunning", listMap());
		return map;
	}
}
