package com.sf.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bowlong.lang.RndEx;
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
		if(isRobot){
			ReLmtOver(0);
		}
		
		String rndVal = RndEx.nextString09(9);
		curr = new Player(rndVal, rndVal);
		curr.rndWaitFirst(id);
		return this;
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		map = toMapMust(map);
		map.put("lens_way", GObjConfig.LenMax_Runway);
		map.put("player", curr.toMap());
		GObjSession enemy = LgcGame.enemySession(getId());
		if (enemy != null) {
			map.put("enemy", enemy.getCurr().toMap());
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
		// map.put("state", state.ordinal());
		return map;
	}

	public List<Map<String, Object>> toLMRunning() {
		lMap.clear();
		curr.lmRunning(lMap);
		GObjSession enemy = LgcGame.enemySession(getId());
		if (enemy != null) {
			enemy.getCurr().lmRunning(lMap);
			// room
			GObjRoom room = LgcGame.getRoom(roomid);
			room.listMap(lMap);
		}
		return lMap;
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
			curr.clear();
		}
	}

	public void reduceForage(int reduce) {
		int curr = this.curr.getForage();
		int nCurr = curr - reduce;
		nCurr = nCurr <= 0 ? 0 : nCurr;
		this.curr.setForage(nCurr);
		if (nCurr <= 0)
			this.state = ETState.Fail;
	}

	public boolean isFails() {
		return this.state == ETState.Fail;
	}

	public boolean isWin() {
		return this.state == ETState.Win;
	}
	
	public boolean isStart(){
		return this.state == ETState.Running;
	}
	
	public boolean isEmptyWait(){
		return this.curr.isEmptyWait();
	}
}
