package com.sf.entity;

import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.RndEx;

/**
 * 游戏对象
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午9:10:50
 */
public class GObject extends BeanOrigin {
	private static final long serialVersionUID = 1L;
	private long id;
	private ETGObj gobjType = ETGObj.SheepSmall; // 类型
	private int runway = 0; // 跑道编号
	private long createtime = 0; // 创建时间
	private long startMs = 0;// 开始跑的时间
	private long belongTo = 0; // 拥有者
	private long runTo = 0; // 跑向的人
	protected int nState = 0; // 运行状态[1:ready,2:Running]
	protected double endDistance = 0; // 目的距离
	private double base_speed = -1; // 基础速度
	private long startStayMs = 0;// 开始停留时间
	private int stayMs = 0; // 停留时间毫秒 ms
	protected boolean isRunBack = false; // 是否返了(用于距离减少) - 目前没用
	private double initPos = 0; // 初始位置
	private double volume = 0.2d; // 体积大小
	protected long nextLiveMs; // 下次复活时间

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ETGObj getGobjType() {
		return gobjType;
	}

	public void setGobjType(ETGObj gobjType) {
		this.gobjType = gobjType;
	}

	public int getRunway() {
		return runway;
	}

	public void setRunway(int runway) {
		this.runway = runway;
	}

	public long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	public long getStartMs() {
		return startMs;
	}

	public void setStartMs(long startMs) {
		this.startMs = startMs;
	}

	public long getBelongTo() {
		return belongTo;
	}

	public void setBelongTo(long belongTo) {
		this.belongTo = belongTo;
	}

	public long getRunTo() {
		return runTo;
	}

	public void setRunTo(long runTo) {
		this.runTo = runTo;
	}

	public boolean isRunning() {
		return nState == 2;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public GObject() {
		super();
	}

	public GObject(ETGObj gobjType, int runway, long belongTo) {
		reInit(gobjType, runway, belongTo);
	}

	public GObject(ETGObj gobjType, long belongTo) {
		reInit(gobjType, 0, belongTo);
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map = gobjType.toMap(map);
		map.put("id", String.valueOf(id));
		map.put("runway", runway);
		map.put("belongTo", String.valueOf(belongTo));
		map.put("runTo", String.valueOf(runTo));
		map.put("endDistance", this.endDistance);
		map.put("distance", calcDistance());
		map.put("isRunning", isRunning());
		// map.put("start_ms", startMs);
		return map;
	}

	public GObject reInit(ETGObj gobjType, int runway, long belongTo) {
		this.stop();
		this.id = GObjConfig.SW_GID.nextId();
		this.gobjType = gobjType;
		this.runway = runway;
		this.belongTo = belongTo;
		this.createtime = now();
		if (this.base_speed == -1) {
			this.base_speed = this.gobjType.getSpeed();
		}
		return this;
	}

	public GObject reInit(ETGObj gobjType, long belongTo) {
		return reInit(gobjType, 0, belongTo);
	}

	public GObject reInit(ETGObj gobjType) {
		return reInit(gobjType, currWay(this.runway <= 0), 0);
	}

	protected int rndWay() {
		return 1 + RndEx.nextInt(GObjConfig.NMax_Runway);
	}

	public int currWay(boolean isRnd) {
		if (isRnd) {
			this.runway = rndWay();
		}
		return this.runway;
	}

	protected void stop() {
		// this.runway = 0;
		this.startMs = 0;
		this.belongTo = 0;
		this.runTo = 0;
		this.nState = 0;
		this.startStayMs = 0;
		this.stayMs = 0;
		this.isRunBack = false;
		this.initPos = 0;
		this.nextLiveMs = 0;
		this.getGobjType().setSpeed(this.base_speed);
	}

	public void disappear(boolean isReLive) {
		stop();
	}

	public void disappear(long delayReLiveMs) {
		stop();
		if (delayReLiveMs > 0)
			this.nextLiveMs = now() + delayReLiveMs;
	}

	public void ready(double initPos) {
		if (this.runway <= 0) {
			this.runway = currWay(true);
		}
		reRunning(this.runTo, this.runway, GObjConfig.LenMax_Runway);
		this.nState = 1;
		this.initPos = initPos;
	}

	public boolean isReadyRunning() {
		return this.nState == 1 || this.nState == 2;
	}

	private void reRunning(long runTo, int runway, double endDis) {
		this.runway = runway;
		this.runTo = runTo;
		this.endDistance = endDis;
		this.startMs = now() + 20;
		this.stayMs = 0;
		this.startStayMs = 0;
		this.nextLiveMs = 0;
		this.nState = 2;
		this.isRunBack = (this.runTo == this.belongTo);
	}

	public void startRunning(long runTo, int runway) {
		if (runway <= 0) {
			runway = currWay(true);
		}
		reRunning(runTo, runway, GObjConfig.LenMax_Runway);
	}

	public void startRunning(long runTo) {
		startRunning(runTo, this.runway);
	}

	public void runBack(long runTo, boolean isRndWay) {
		double dis = GObjConfig.LenMax_Runway;
		this.initPos = dis - calcDistance();
		reRunning(runTo, currWay(isRndWay), dis);
	}

	public void runBack(double multiples) {
		runBack(this.belongTo, false);
		if (multiples > 0 && multiples != 1) {
			this.gobjType.setSpeed(this.base_speed * multiples);
		}
	}

	public double calcDistance() {
		double val = initPos;
		if (isRunning()) {
			long diffMs = (now() - this.startMs - this.stayMs);
			val += (diffMs * gobjType.getSpeed()) / 1000;
		}
		return val;
	}

	// 是否移动到终点
	private boolean isEnd(double otherDis, boolean isAbs) {
		double mvDis = calcDistance() + otherDis + this.volume / 2;
		double diff = this.endDistance - mvDis;
		if (isAbs) {
			diff = Math.abs(diff);
		}
		return diff <= GObjConfig.NMax_CollidDistance;
	}

	// 是否移动到终点
	public boolean isEnd() {
		return isEnd(0, false);
	}

	public boolean isColliding(GObject gobj) {
		if (gobj == null)
			return false;
		double val = gobj.calcDistance() + gobj.getVolume() / 2;
		return isEnd(val, true);
	}

	public int comPower(GObject gobj) {
		if (this.gobjType.getPower() > gobj.getGobjType().getPower()) {
			return 1;
		}

		if (this.gobjType.getPower() < gobj.getGobjType().getPower()) {
			return -1;
		}
		return 0;
	}

	public boolean isToEnemy() {
		return this.belongTo != this.runTo;
	}

	public boolean isCanRelive() {
		if (this.nextLiveMs > 0)
			return this.nextLiveMs <= now();
		return false;
	}

	// 停留会儿吧
	public void stayOrRun(boolean isStay) {
		if (isStay) {
			this.startStayMs = now();
		} else {
			if (this.startStayMs > 0) {
				this.startMs += now() - this.startStayMs;
			}
		}
	}
}
