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
	private long startRunTime = 0;// 开始跑的时间
	private long belongTo = 0; // 拥有者
	private long runTo = 0; // 跑向的人
	private boolean isRunning = false;
	private double endDistance = 0; // 目的距离

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

	public long getStartRunTime() {
		return startRunTime;
	}

	public void setStartRunTime(long startRunTime) {
		this.startRunTime = startRunTime;
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
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
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

	public GObject(ETGObj gobjType) {
		reInit(gobjType, 1 + RndEx.nextInt(GObjConfig.NMax_Runway), 0);
	}

	@Override
	public Map<String, Object> toMap(Map<String, Object> map) {
		if (map == null)
			map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("runway", runway);
		map.put("belongTo", belongTo);
		map.put("runTo", runTo);
		map.put("isRunning", isRunning);
		map.put("start_ms", startRunTime);
		map.put("distance", calcDistance());
		map = gobjType.toMap(map);
		return map;
	}

	public GObject reInit(ETGObj gobjType, int runway, long belongTo) {
		this.stop();
		this.id = GObjConfig.SW_GID.nextId();
		this.gobjType = gobjType;
		this.runway = runway;
		this.belongTo = belongTo;
		this.createtime = now();
		return this;
	}

	public GObject reInit(ETGObj gobjType, long belongTo) {
		return reInit(gobjType, 0, belongTo);
	}

	public GObject reInit(ETGObj gobjType) {
		return reInit(gobjType, currWay(), 0);
	}

	private int rndWay() {
		return 1 + RndEx.nextInt(GObjConfig.NMax_Runway);
	}

	public int currWay(boolean isRnd) {
		if (isRnd) {
			this.runway = rndWay();
		}
		return this.runway;
	}

	public int currWay() {
		return currWay(this.runway <= 0);
	}

	public void stop() {
		this.runway = 0;
		this.runTo = 0;
		this.startRunTime = 0;
		this.isRunning = false;
	}

	private void reRunning(long runTo, int runway, double endDis) {
		this.runway = runway;
		this.runTo = runTo;
		this.endDistance = endDis;
		this.startRunTime = now();
		this.isRunning = true;
	}

	public void startRunning(long runTo, int runway) {
		if (runway <= 0) {
			runway = rndWay();
		}
		reRunning(runTo, runway, GObjConfig.LenMax_Runway);
	}

	public void startRunning(long runTo) {
		startRunning(runTo, currWay());
	}

	public void runBack(long runTo, boolean isRndWay) {
		reRunning(runTo, currWay(isRndWay), calcDistance());
	}

	public void runBack(double multiples) {
		if (multiples > 0) {
			double speed = this.gobjType.getSpeed();
			this.gobjType.setSpeed(speed * multiples);
		}
		runBack(this.belongTo, false);
	}

	public double calcDistance() {
		if (isRunning) {
			double val = (now() - this.startRunTime) * gobjType.getSpeed() * 0.001;
			return round(val, 3);
		}
		return 0;
	}

	// 是否移动到终点
	private boolean isEnd(double otherDis) {
		double mvDis = calcDistance() + otherDis;
		double diff = Math.abs(this.endDistance - mvDis);
		return diff <= 0.1;
	}

	public boolean isColliding(GObject gobj) {
		return isEnd(gobj.calcDistance());
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

	// 是否移动到终点
	public boolean isEnd() {
		return isEnd(0);
	}

	public boolean isMvEnemy() {
		return this.belongTo != this.runTo;
	}
}
