package com.sf.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bowlong.lang.RndEx;
import com.bowlong.util.CalendarEx;
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
	Player curr;
	Player enemy;
	String lgid;
	String lgpwd;

	List<NotifyType> lNotify = new ArrayList<NotifyType>();

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

	public Player getEnemy() {
		return enemy;
	}

	public void setEnemy(Player enemy) {
		this.enemy = enemy;
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

	public List<NotifyType> getListNotify() {
		return lNotify;
	}

	public GObjSession() {
		super();
	}

	public GObjSession(String lgid, String lgpwd, long roomid) {
		InitSesID(GObjConfig.SW_ID.nextId());
		this.roomid = roomid;
		this.lgid = lgid;
		this.lgpwd = lgpwd;
		String rndVal = RndEx.nextString09(9);
		this.curr = new Player(rndVal, rndVal, this.roomid);
	}

	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put(GObjConfig.K_SesID, sessionID);
		map.put("time_ms", CalendarEx.now());
		map.put("player", curr.toMap());
		if (enemy != null) {
			map.put("enemy", enemy.toMap());
		}
		return map;
	}

	public Map<String, Object> toMap() {
		return toMap(null);
	}

	public void addNotify(NotifyType notifyType) {
		synchronized (this) {
			if (lNotify.contains(notifyType)) {
				return;
			}
			lNotify.add(notifyType);
		}
	}
	
	public void rmNotify(NotifyType notifyType) {
		synchronized (this) {
			lNotify.remove(notifyType);
		}
	}
}
