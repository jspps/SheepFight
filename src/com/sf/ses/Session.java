package com.sf.ses;

import com.sf.entity.BeanOrigin;

/**
 * Session 会话对象
 * 
 * @author Canyon
 */
public class Session extends BeanOrigin {
	private static final long serialVersionUID = 1L;
	static long minLmt = 100;
	static protected long defMs = 600000; // 10分钟

	protected long lmsOver = 0;
	protected long id;
	private long nextOverdue;
	private long creattime;

	public long getId() {
		return id;
	}

	public void setId(long sesid) {
		this.id = sesid;
	}

	public long getCreattime() {
		return creattime;
	}

	public Session() {
	}

	public Session(long id) {
		InitSesID(id);
	}

	public Session(long id, long limitMs) {
		InitAll(id, limitMs);
	}

	long now() {
		return System.currentTimeMillis();
	}

	public void InitAll(long id, long lmtMs) {
		this.creattime = now();
		this.id = id;
		ReLmtOver(lmtMs);
	}

	public void InitSesID(long id) {
		long lmtMs = lmsOver > minLmt ? lmsOver : defMs;
		InitAll(id, lmtMs);
	}

	public void ReLmtOver(long lmtMs) {
		lmsOver = lmtMs;
		ResetTimeOverdue();
	}

	public void ResetTimeOverdue() {
		if(lmsOver > minLmt){
			nextOverdue = now() + lmsOver;
		}
	}

	public boolean IsValid() {
		if(lmsOver > minLmt){
			return now() < nextOverdue;
		}
		return true;
	}
}