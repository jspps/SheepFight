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
	private boolean isRunning = false;
	protected double endDistance = 0; // 目的距离
	private double base_speed = -1; // 基础速度
	private long startStayMs = 0;// 开始停留时间
	private int stayMs = 0; // 停留时间毫秒 ms
	private boolean isRunBack = false; // 是否返了(用于距离减少)

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
		reInit(gobjType, rndWay(), 0);
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
		map.put("start_ms", startMs);
		double dic = calcDistance();
		map.put("distance",this.isRunBack ? this.endDistance - dic : dic);
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
		if (this.base_speed <= 0) {
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

	public void stop() {
		this.runway = 0;
		this.runTo = 0;
		this.startMs = 0;
		this.stayMs = 0;
		this.startStayMs = 0;
		this.isRunning = false;
	}

	private void reRunning(long runTo, int runway, double endDis) {
		this.runway = runway;
		this.runTo = runTo;
		this.endDistance = endDis;
		this.startMs = now();
		this.stayMs = 0;
		this.startStayMs = 0;
		this.isRunning = true;
		this.isRunBack = (this.endDistance != GObjConfig.LenMax_Runway);
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
		reRunning(runTo, currWay(isRndWay), calcDistance());
	}

	public void runBack(double multiples) {
		if (multiples > 0) {
			this.gobjType.setSpeed(this.base_speed * multiples);
		}
		runBack(this.belongTo, false);
	}

	public double calcDistance() {
		if (isRunning) {
			long diffMs = (now() - this.startMs - this.stayMs);
			double val = diffMs * gobjType.getSpeed() * 0.001;
			return round(val, 3);
		}
		return 0;
	}

	// 是否移动到终点
	private boolean isEnd(double otherDis) {
		double mvDis = calcDistance() + otherDis;
		double diff = this.endDistance - mvDis;
		return diff <= GObjConfig.NMax_CollidDistance;
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
