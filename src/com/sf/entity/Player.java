package com.sf.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bowlong.lang.RndEx;

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
	
	Map<Long, GObject> mapWait = new ConcurrentHashMap<Long, GObject>();
	Map<Long, GObject> mapRunning = new ConcurrentHashMap<Long, GObject>();
	List<GObject> lstEnd = new ArrayList<GObject>();

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

	public Player reInit(String name, String icon, int forage) {
		this.name = name;
		this.icon = icon;
		this.forage = forage;

		mapWait.clear();
		mapRunning.clear();
		lstEnd.clear();
		return this;
	}

	public Player() {
		super();
	}

	public Player(String name, String icon, int forage) {
		super();
		reInit(name, icon, forage);
	}

	public Player(String name, String icon) {
		this(name, icon, GObjConfig.NI_Forage);
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("icon", icon);
		map.put("forage", forage);
		map.put("listWait", listMap(mapWait.values()));
		return map;
	}

	public GObject rndWaitOne(long beTo) {
		ETGObj tmp = ETGObj.get(1 + RndEx.nextInt(3));
		GObject obj = new GObject(tmp, beTo);
		mapWait.put(obj.getId(), obj);
		return obj;
	}

	public void rndWaitFirst(long beTo) {
		for (int i = 0; i < GObjConfig.NI_Sheep_Wait; i++) {
			rndWaitOne(beTo);
		}
	}

	List<Map<String, Object>> listMap(Collection<GObject> list) {
		List<Map<String, Object>> lMap = new ArrayList<Map<String, Object>>();
		for (GObject item : list) {
			lMap.add(item.toMap(null));
		}
		return lMap;
	}

	public List<Map<String, Object>> listMap() {
		return listMap(mapRunning.values());
	}

	GObject getInWait(long sheepId) {
		return mapWait.get(sheepId);
	}

	public boolean isInWait(long sheepId) {
		return getInWait(sheepId) != null;
	}

	public boolean startRunning(long sheepId, int way, long runTo) {
		GObject gobj = getInWait(sheepId);
		if (gobj == null)
			return false;
		gobj.startRunning(runTo, way);
		mapWait.remove(sheepId);
		mapRunning.put(sheepId, gobj);
		return true;
	}
}
