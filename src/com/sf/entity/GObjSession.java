package com.sf.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bowlong.lang.RndEx;
import com.bowlong.util.CalendarEx;
import com.sf.logic.LgcGame;
import com.sf.ses.Session;

/**
 * 游戏对象Session
 * 
 * @author Canyon
 * @version createtime：2018-11-11下午12:00:38
 */
public class GObjSession extends Session {
	private static final long serialVersionUID = 1L;
	long roomid;
	String lgid;
	String lgpwd;
	Player curr;
	long enemySesId;

	ETState state = ETState.None;

	long lmtLastTime = 0;// 上一次计数时间
	int netCount = 0;// 5秒内总限定次数
	List<ETNotify> lNotify = new ArrayList<ETNotify>();
	List<Map<String, Object>> lMap = new ArrayList<Map<String, Object>>();

	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public Player getCurr() {
		return curr;
	}

	public void setCurr(Player currPlay) {
		this.curr = currPlay;
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

	public GObjSession() {
		super();
	}

	public GObjSession(String lgid, String lgpwd) {
		reInit(lgid, lgpwd);
	}

	public GObjSession reInit(String lgid, String lgpwd) {
		this.lgid = lgid;
		this.lgpwd = lgpwd;
		String rndVal = RndEx.nextString09(9);
		this.curr = new Player(rndVal, rndVal, 0);
		InitSesID(GObjConfig.SW_ID.nextId());
		return this;
	}

	public Map<String, Object> toMap(Map<String, Object> map) {
		map = toMapMust(map);
		map.put("player", curr.toMap());
		lMap.clear();
		lMap.addAll(curr.listMap());

		GObjSession enemy = LgcGame.targetSession(enemySesId);
		if (enemy != null) {
			Player _pl = enemy.getCurr();
			map.put("enemy", _pl.toMap());
			map.put("enemy_state", enemy.getState().ordinal());
			lMap.addAll(_pl.listMap());
		}
		map.put("listRunning", lMap);
		return map;
	}

	public Map<String, Object> toMap() {
		return toMap(null);
	}

	public Map<String, Object> toMapMust(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put(GObjConfig.K_SesID, sessionID);
		map.put("time_ms", CalendarEx.now());
		map.put("lens_way", GObjConfig.LM_Runway);
		map.put("roomid", roomid);
		map.put("state", state.ordinal());
		return map;
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
		long _now = CalendarEx.now();
		long diff = _now - lmtLastTime;
		if (diff > GObjConfig.LS_Net) {
			lmtLastTime = _now;
			netCount = 1;
		} else {
			netCount++;
		}
	}

	public boolean isNetMore() {
		return netCount >= GObjConfig.LN_Net;
	}

	public void resetRoomId(long roomId) {
		setRoomid(roomId);
	}

	public void readyOrStart(boolean isStart) {
		synchronized (this) {
			lNotify.clear();
			if (isStart)
				state = ETState.Running;
			else
				state = ETState.Ready;
		}
	}

	public void clear() {
		synchronized (this) {
			resetRoomId(0);
			lmtLastTime = 0;
			netCount = 0;
			state = ETState.None;
			enemySesId = 0;
			lNotify.clear();
		}
	}

	public boolean isReady() {
		return state == ETState.Ready;
	}
}
