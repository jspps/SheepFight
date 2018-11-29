package com.sf.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bowlong.lang.RndEx;
import com.sf.logic.LgcGame;

/**
 * 游戏对象Session
 * 
 * @author Canyon
 * @version createtime：2018-11-11下午12:00:38
 */
public class GObjSession extends GObjSPlayer {
	private static final long serialVersionUID = 1L;
	static private CompGObjWay sortWay = new CompGObjWay();
	private long roomid;
	private String lgid;
	private String lgpwd;
	private long enemySesId;
	private boolean isRobot;

	private ETState state = ETState.None;

	private long lmtLastTime = 0;// 上一次计数时间
	private int netCount = 0;// 5秒内总限定次数
	private List<ETNotify> lNotify = new ArrayList<ETNotify>();
	private List<GObject> lstScene = new ArrayList<GObject>();
	private List<Map<String, Object>> lMap = new ArrayList<Map<String, Object>>();

	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public String getLgid() {
		return lgid;
	}

	public void setLgid(String lgid) {
		this.lgid = lgid;
	}

	public String getLgpwd() {
		return lgpwd;
	}

	public void setLgpwd(String lgpwd) {
		this.lgpwd = lgpwd;
	}

	public long getEnemySesId() {
		return enemySesId;
	}

	public void setEnemySesId(long enemySesId) {
		this.enemySesId = enemySesId;
	}

	public List<ETNotify> getListNotify() {
		return lNotify;
	}

	public int getNetCount() {
		return netCount;
	}

	public ETState getState() {
		return state;
	}

	public void setState(ETState state) {
		this.state = state;
	}

	public boolean isRobot() {
		return isRobot;
	}

	public GObjSession() {
		super();
	}

	public GObjSession(String lgid, String lgpwd) {
		initSes(lgid, lgpwd);
	}

	public GObjSession initSes(String lgid, String lgpwd) {
		this.lgid = lgid;
		this.lgpwd = lgpwd;
		this.isRobot = lgid.contains("robot_");
		InitSesID(GObjConfig.SW_SID.nextId());
		if (this.isRobot) {
			ReLmtOver(0);
		}

		String rndVal = RndEx.nextString09(9);
		initPlayer(rndVal, rndVal);
		return this;
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		map = toMapMust(map);
		map.put("lens_way", GObjConfig.LenMax_Runway);
		map.put("player", toPlayMap(null));
		map.put("listWaitSelf", lmWait());
		GObjSession enemy = LgcGame.enemySession(getId());
		if (enemy != null) {
			map.put("enemy", enemy.toPlayMap(null));
			map.put("listWaitEnemy", enemy.lmWait());
		}
		map.put("listRunning", toLMRunning());
		return map;
	}

	public Map<String, Object> toMapMust(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put(GObjConfig.K_SesID, id);
		map.put("time_ms", now());
		map.put("roomid", roomid);
		return map;
	}

	public List<Map<String, Object>> toLMRunning() {
		lMap.clear();
		lstScene.clear();
		lstScene.addAll(getListRunning());
		GObjSession enemy = LgcGame.enemySession(getId());
		if (enemy != null) {
			lstScene.addAll(enemy.getListRunning());
			// room
			GObjRoom room = LgcGame.getRoom(roomid);
			lstScene = room.listScene(lstScene);
		}
		if (lstScene.size() > 1) {
			Collections.sort(lstScene, sortWay);
		}
		return toListMap(lMap, lstScene);
	}

	public void addNotify(ETNotify notifyType) {
		synchronized (this) {
			if (lNotify.contains(notifyType)) {
				return;
			}
			lNotify.add(notifyType);
		}
	}

	public void rmNotify(ETNotify notifyType) {
		synchronized (this) {
			lNotify.remove(notifyType);
		}
	}

	public void recordNetCount() {
		long _now = now();
		long diff = _now - lmtLastTime;
		if (diff > GObjConfig.LMS_Net) {
			lmtLastTime = _now;
			netCount = 1;
		} else {
			netCount++;
		}
	}

	public boolean isNetMore() {
		return netCount >= GObjConfig.LmtN_Net;
	}

	public void ready(long enemyID) {
		synchronized (this) {
			lNotify.clear();
			enemySesId = enemyID;
			state = ETState.Matching;
		}
	}

	public void start(long enemyID) {
		synchronized (this) {
			lNotify.clear();
			enemySesId = enemyID;
			state = ETState.Running;
		}
	}

	public void clear() {
		synchronized (this) {
			roomid = 0;
			lmtLastTime = 0;
			netCount = 0;
			state = ETState.None;
			enemySesId = 0;
			lNotify.clear();
			lstScene.clear();
			lMap.clear();
			super.clear();
		}
	}

	public boolean isFails() {
		return this.state == ETState.Fail;
	}

	public boolean isWin() {
		return this.state == ETState.Win;
	}

	public boolean isStart() {
		return this.state == ETState.Running;
	}
}
