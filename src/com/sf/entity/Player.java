package com.sf.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

	long belongTo = 0;
	long nextRndWait = 0; // 下一次随机产生的羊
	private Map<Long, GObject> mapWait = new ConcurrentHashMap<Long, GObject>();
	private Map<Long, GObject> mapRunning = new ConcurrentHashMap<Long, GObject>();
	private List<GObject> listRunning = new ArrayList<GObject>();
	private Queue<GObject> queEnd = new ConcurrentLinkedQueue<GObject>();
	private List<GObject> listWay = new ArrayList<GObject>();
	private List<GObject> listEnd = new ArrayList<GObject>();

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

	public List<GObject> getListRunning() {
		return listRunning;
	}

	public Player reInit(String name, String icon, int forage) {
		this.name = name;
		this.icon = icon;
		this.forage = forage;

		mapWait.clear();
		mapRunning.clear();
		listRunning.clear();
		queEnd.clear();
		listWay.clear();
		listEnd.clear();
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
		GObject obj = null;
		ETGObj tmp = ETGObj.get(1 + RndEx.nextInt(3));
		obj = queEnd.poll();
		if (obj == null) {
			obj = new GObject(tmp, beTo);
		}
		obj.reInit(tmp, beTo);
		mapWait.put(obj.getId(), obj);
		return obj;
	}

	public void rndWaitFirst(long beTo) {
		this.belongTo = beTo;
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
		return listMap(listRunning);
	}

	GObject getInWait(long sheepId) {
		return mapWait.get(sheepId);
	}

	public boolean isInWait(long sheepId) {
		return getInWait(sheepId) != null;
	}

	public void jugdeRndSheep() {
		if (this.mapWait.size() >= GObjConfig.NMax_Sheep_Wait)
			return;
		long now = now();
		if (nextRndWait <= 0) {
			this.nextRndWait = now + GObjConfig.LMS_NextNewSheep;
		} else if (nextRndWait <= now) {
			this.nextRndWait = now + GObjConfig.LMS_NextNewSheep;
			rndWaitOne(belongTo);
		}
	}

	public boolean startRunning(long sheepId, int way, long runTo) {
		GObject gobj = getInWait(sheepId);
		if (gobj == null)
			return false;
		gobj.startRunning(runTo, way);
		mapWait.remove(sheepId);
		mapRunning.put(sheepId, gobj);
		listRunning.add(gobj);
		jugdeRndSheep();
		return true;
	}

	private List<GObject> getList(int way, List<GObject> reList) {
		reList.clear();
		int lens = listRunning.size();
		GObject item = null;
		for (int i = 0; i < lens; i++) {
			item = listRunning.get(i);
			if (item.getBelongTo() != item.getRunTo()) {
				if ((way <= 0) || (way > 0 && item.getRunway() == way))
					reList.add(item);
			}
		}
		return reList;
	}

	public GObject getFirst4Way(int way) {
		listWay = getList(way, listWay);
		if (listWay.size() > 0)
			return listWay.get(0);
		return null;
	}

	public List<GObject> jugdeEnd() {
		listEnd.clear();
		int lens = listRunning.size();
		GObject item = null;
		for (int i = 0; i < lens; i++) {
			item = listRunning.get(i);
			if (item.isEnd()) {
				listEnd.add(item);
			}
		}
		return listEnd;
	}

	public void arriveEnd(GObject gobj) {
		gobj.stop();
		mapRunning.remove(gobj.getId());
		listRunning.remove(gobj);
		queEnd.add(gobj);
	}
	
	public void clear(){
		GObject item = null;
		int lens = 0;
		listEnd.clear();
		listEnd.addAll(listRunning);
		listEnd.addAll(mapWait.values());
		lens = listEnd.size();
		for (int i = 0; i < lens; i++) {
			item = listEnd.get(i);
			arriveEnd(item);
		}
	}
}
