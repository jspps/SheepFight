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
	private int power;// 力量or总体时长(秒)
	private double speed;// 速度单位Unit/秒 - 这个单位：可能是米，有可能是一个单元格
	private int runway = 0; // 跑道编号
	private long createtime = 0; // 创建时间
	private long startMs = 0;// 开始跑的时间
	private long belongTo = 0; // 拥有者
	private long runTo = 0; // 跑向的人
	protected int nState = 0; // 运行状态[1:ready,2:Running]
	protected double endDistance = 0; // 目的距离
	private double speed_base = -1; // 基础速度
	private double speed_multiples = 1; // 速度倍率
	private long startStayMs = 0;// 开始停留时间
	private long currStayMs = 0; // 停留时间
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
		this.speed_base = this.gobjType.getSpeed();
		setSpeed(this.speed_base * this.speed_multiples);
		setPower(this.gobjType.getPower());
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
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
		if (power > 0)
			map.put("power", power);
		if (speed > 0)
			map.put("speed", speed);
		// map.put("start_ms", startMs);
		return map;
	}

	public GObject reInit(ETGObj gobjType, int runway, long belongTo) {
		this.stop();
		this.id = GObjConfig.SW_GID.nextId();
		setGobjType(gobjType);
		this.runway = runway;
		this.belongTo = belongTo;
		this.createtime = now();
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
		this.currStayMs = 0;
		this.isRunBack = false;
		this.initPos = 0;
		this.nextLiveMs = 0;
		this.speed_multiples = 1;
		setSpeed(this.gobjType.getSpeed());
		setPower(this.gobjType.getPower());
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
		this.nextLiveMs = 0;
		this.nState = 2;
		this.isRunBack = (this.runTo == this.belongTo);
		this.isStay(false);
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
			this.speed_multiples = multiples;
			setSpeed(this.speed_base * this.speed_multiples);
		}
	}

	public void runBack() {
		runBack(GObjConfig.NL_BackSpeedMultiples);
	}

	private double calcDistance() {
		double val = initPos;
		if (isRunning()) {
			long ms_mv = (now() - this.startMs);
			long ms_stay = this.stayMs();
			long ms_diff = ms_mv - ms_stay;
			val += (ms_diff * getSpeed()) / 1000;
		}
		return val;
	}

	public double allDistance() {
		return calcDistance() + this.volume / 2;
	}

	private boolean isJugdeCollide(double diff, boolean isAbs) {
		if (isAbs) {
			diff = Math.abs(diff);
		}
		return diff <= GObjConfig.NMax_CollidDistance;
	}

	// 是否移动到终点
	private boolean isEnd(double otherDis, boolean isAbs) {
		double mvDis = allDistance() + otherDis;
		double diff = this.endDistance - mvDis;
		return isJugdeCollide(diff, isAbs);
	}

	// 是否移动到终点
	public boolean isEnd() {
		return isEnd(0, false);
	}

	public boolean isColliding(GObject gobj) {
		if (gobj == null)
			return false;
		boolean isSameDirection = gobj.getRunTo() == this.getRunTo();
		double val = gobj.allDistance();
		if (isSameDirection) {
			double curVal = allDistance();
			return isJugdeCollide(val - curVal, true);
		}
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

	private long stayMs() {
		if (this.currStayMs > 0)
			return this.currStayMs;

		if (this.startStayMs > 0) {
			return now() - this.startStayMs;
		}
		return 0;
	}

	// 停留会儿吧
	public void isStay(boolean isStaying) {
		if (isStaying) {
			this.startStayMs = now();
		} else {
			if (!this.isRunBack && this.startStayMs > 0) {
				this.currStayMs = now() - this.startStayMs;
			}
			this.startStayMs = 0;
		}
	}

	public void goOn() {
		isStay(false);
	}
}
