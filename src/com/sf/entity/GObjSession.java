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
	private long roomid;
	private String lgid;
	private String lgpwd;
	private Player curr;
	private long enemySesId;
	private boolean isRobot;

	private ETState state = ETState.None;

	private long lmtLastTime = 0;// 上一次计数时间
	private int netCount = 0;// 5秒内总限定次数
	private List<ETNotify> lNotify = new ArrayList<ETNotify>();
	private List<Map<String, Object>> lMap = new ArrayList<Map<String, Object>>();

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
	
	public boolean isRobot() {
		return isRobot;
	}

	public GObjSession() {
		super();
	}

	public GObjSession(String lgid, String lgpwd) {
		reInit(lgid, lgpwd);
	}

	public GObjSession reInit(String lgid, String lgpwd) {
		InitSesID(GObjConfig.SW_SID.nextId());
		this.lgid = lgid;
		this.lgpwd = lgpwd;
		isRobot = lgid.contains("robot_");
		String rndVal = RndEx.nextString09(9);
		curr = new Player(rndVal, rndVal);
		curr.rndWaitFirst(id);
		return this;
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		map = toMapMust(map);
		map.put("lens_way", GObjConfig.LM_Runway);
		map.put("player", curr.toMap(null));
		lMap.clear();
		lMap.addAll(curr.listMap());

		GObjSession enemy = LgcGame.targetSession(enemySesId);
		if (enemy != null) {
			Player _pl = enemy.getCurr();
			map.put("enemy", _pl.toMap(null));
//			map.put("enemy_state", enemy.getState().ordinal());
			lMap.addAll(_pl.listMap());
		}
		map.put("listRunning", lMap);
		return map;
	}

	public Map<String, Object> toMapMust(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put(GObjConfig.K_SesID, id);
		map.put("time_ms", CalendarEx.now());
		map.put("roomid", roomid);
//		map.put("state", state.ordinal());
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
		if (diff > GObjConfig.LMS_Net) {
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
			resetRoomId(0);
			lmtLastTime = 0;
			netCount = 0;
			state = ETState.None;
			enemySesId = 0;
			lNotify.clear();
		}
	}

	public boolean isReady() {
		return state == ETState.Matching;
	}
}
