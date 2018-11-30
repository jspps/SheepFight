package com.sf.ses;

import com.sf.entity.BeanOrigin;

/**
 * Session 会话对象
 * 
 * @author Canyon
 */
public class Session extends BeanOrigin {
	private static final long serialVersionUID = 1L;
	static int minLmt = 100;
	static private int defMs1Sec = 1000; // 1秒
	static private int defMs1Min = defMs1Sec * 600; // 10分钟
	static private int defMs = defMs1Sec * 20;

	protected int lmsOver = 0;
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

	public Session(long id, int limitMs) {
		InitAll(id, limitMs);
	}

	public void InitAll(long id, int lmtMs) {
		this.creattime = now();
		this.id = id;
		ReLmtOver(lmtMs);
	}

	public void InitSesID(long id) {
		int lmtMs = lmsOver > minLmt ? lmsOver : defMs;
		InitAll(id, lmtMs);
	}

	public void ReLmtOver(int lmtMs) {
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