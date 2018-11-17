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
	static private long defMs1Sec = 1000; // 1秒
	static private long defMs1Min = 60 * defMs1Sec; // 1分钟
	static private long defMs = defMs1Sec * 15;

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
	
	public void ReLmtOver1Min() {
		ReLmtOver(defMs1Min);
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