package com.sf.ses;

import com.bowlong.tool.SnowflakeIdWorker;
import com.sf.entity.BeanOrigin;

/**
 * Session 会话对象
 * 
 * @author Canyon
 */
public class Session extends BeanOrigin {
	private static final long serialVersionUID = 1L;

	protected long sessionID;
	private long timeOverdue;
	private long creattime;

	protected long overLimitMs = 0;
	static protected long defMs = 600000; // 10分钟

	public long getSessionID() {
		return sessionID;
	}

	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
	}

	public long getTimeOverdue() {
		return timeOverdue;
	}

	public long getCreattime() {
		return creattime;
	}

	public Session() {
		this(defMs);
	}

	public Session(long sesID, long limitMs) {
		InitAll(sesID, limitMs);
	}

	public Session(long limitMs) {
		InitMs(limitMs);
	}

	public void InitAll(long sesID, long limitMs) {
		this.sessionID = sesID;
		this.creattime = System.currentTimeMillis();
		this.overLimitMs = limitMs;
		ResetTimeOverdue();
	}

	public void InitMs(long limitMs) {
		long sesID = SnowflakeIdWorker.defInstance().nextId();
		InitAll(sesID, limitMs);
	}

	public void InitSesID(long sesID) {
		InitAll(sesID, defMs);
	}

	public void ResetTimeOverdue() {
		this.timeOverdue = System.currentTimeMillis() + this.overLimitMs;
	}

	public boolean IsValid() {
		return System.currentTimeMillis() < this.timeOverdue;
	}
}