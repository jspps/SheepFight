package com.sf.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.bowlong.lang.RndEx;
import com.sf.ses.Session;

/**
 * 玩家
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午9:05:32
 */
public class GObjSPlayer extends Session {
	private static final long serialVersionUID = 1L;
	String name; // 名字
	String icon; // 头像
	int forage; // 草料

	long nextRndWait = 0; // 下一次随机产生的羊
	private Queue<GObject> queEnd = new ConcurrentLinkedQueue<GObject>();
	private Map<Long, GObject> mapWait = new ConcurrentHashMap<Long, GObject>();
	private List<GObject> listWait = new ArrayList<GObject>();
	private Map<Long, GObject> mapRunning = new ConcurrentHashMap<Long, GObject>();
	private List<GObject> listRunning = new ArrayList<GObject>();
	private List<GObject> listWay = new ArrayList<GObject>();
	private List<GObject> listEnd = new ArrayList<GObject>();
	List<Map<String, Object>> lmWait = new ArrayList<Map<String, Object>>();
	CompGObjPower objPower = new CompGObjPower();

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

	public GObjSPlayer() {
		super();
	}

	private GObjSPlayer initPlayer(String name, String icon, int forage) {
		clear();
		rndWaitFirst();
		this.name = name;
		this.icon = icon;
		this.forage = forage;
		return this;
	}

	public GObjSPlayer initPlayer(String name, String icon) {
		return initPlayer(name, icon, GObjConfig.NI_Forage);
	}

	public Map<String, Object> toPlayMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("icon", icon);
		map.put("forage", forage);
//		map.put("listWait", lmWait());
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
		listWait.add(obj);
		return obj;
	}

	private void rndWaitFirst() {
		for (int i = 0; i < GObjConfig.NI_Sheep_Wait; i++) {
			rndWaitOne(id);
		}
	}

	protected List<Map<String, Object>> toListMap(List<Map<String, Object>> lMap, Collection<GObject> list) {
		if (lMap == null)
			lMap = new ArrayList<Map<String, Object>>();
		for (GObject item : list) {
			lMap.add(item.toMap());
		}
		return lMap;
	}
	
	public List<Map<String, Object>> lmWait() {
		lmWait.clear();
		return toListMap(lmWait, listWait);
	}

	GObject getInWait(long sheepId) {
		return mapWait.get(sheepId);
	}

	public boolean isInWait(long sheepId) {
		return getInWait(sheepId) != null;
	}

	public boolean isEmptyWait() {
		return listWait.size() <= 0;
	}

	public GObject getMaxPowerInWait() {
		int size = listWait.size();
		if (size > 1) {
			Collections.sort(listWait, objPower);
		}
		if (size > 0)
			return listWait.get(0);
		return null;
	}

	public GObject getInRunning(long sheepId) {
		return mapRunning.get(sheepId);
	}

	public boolean jugdeRndSheep() {
		if (this.listWait.size() >= GObjConfig.NMax_Sheep_Wait){
			this.nextRndWait = 0;
			return false;
		}
		
		long now = now();
		if (nextRndWait <= now) {
			this.nextRndWait = now + GObjConfig.LMS_NextNewSheep;
			rndWaitOne(id);
			return true;
		}
		return false;
	}
	
	private void reNextTime4Wait() {
		if (this.listWait.size() >= GObjConfig.NMax_Sheep_Wait){
			this.nextRndWait = 0;
			return;
		}
		
		long now = now();
		if (nextRndWait <= 0) {
			this.nextRndWait = now + GObjConfig.LMS_NextNewSheep;
		}
	}

	private boolean startRunning(long sheepId, int way, long runTo) {
		GObject gobj = getInWait(sheepId);
		if (gobj == null)
			return false;
		gobj.startRunning(runTo, way);
		mapWait.remove(sheepId);
		listWait.remove(gobj);
		mapRunning.put(sheepId, gobj);
		listRunning.add(gobj);
		reNextTime4Wait();
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
	
	public List<GObject> getList4Way(int way) {
		return getList(way, listWay);
	}

	public GObject getFirst4Way(int way) {
		GObject gobj = null;
		listWay = getList(way, listWay);
		if (listWay.size() > 0)
			gobj = listWay.get(0);
		listWay.clear();
		return gobj;
	}

	public int getAllPower4Way(int way) {
		listWay = getList(way, listWay);
		int lens = listWay.size();
		int sum = 0;
		for (int i = 0; i < lens; i++) {
			sum += listWay.get(i).getGobjType().getPower();
		}
		listWay.clear();
		return sum;
	}

	public List<GObject> jugdeArrive() {
		listEnd.clear();
		int lens = listRunning.size();
		GObject item = null;
		for (int i = 0; i < lens; i++) {
			item = listRunning.get(i);
			if (item.isEnd()) {
				item.getGobjType().setSpeed(1);
				listEnd.add(item);
			}
		}
		return listEnd;
	}

	public void onArrive(GObject gobj) {
		gobj.stop();
		mapRunning.remove(gobj.getId());
		listRunning.remove(gobj);
		queEnd.add(gobj);
	}

	public void clear() {
		GObject item = null;
		int lens = 0;
		listEnd.clear();
		listEnd.addAll(listRunning);
		listEnd.addAll(listWait);
		lens = listEnd.size();
		for (int i = 0; i < lens; i++) {
			item = listEnd.get(i);
			onArrive(item);
		}
		nextRndWait = 0;
		
		mapWait.clear();
		listWait.clear();
		mapRunning.clear();
		listRunning.clear();
		listWay.clear();
		listEnd.clear();
		lmWait.clear();
		
		forage = GObjConfig.NI_Forage;
	}
	
	public void reduceForage(int reduce) {
		int curr = getForage();
		int nCurr = curr - reduce;
		nCurr = nCurr <= 0 ? 0 : nCurr;
		setForage(nCurr);
	}
	
	public boolean downSheep(long sheepId, int runway, long runTo) {
		return startRunning(sheepId, runway, runTo);
	}
}
