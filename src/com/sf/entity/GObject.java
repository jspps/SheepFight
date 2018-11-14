package com.sf.entity;

import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.RndEx;
import com.bowlong.util.CalendarEx;

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
	private double distance = 0.0;

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
		super();
		this.id = GObjConfig.SW_GID.nextId();
		this.gobjType = gobjType;
		this.runway = runway;
		this.belongTo = belongTo;
		this.createtime = CalendarEx.now();
	}

	public GObject(ETGObj gobjType, long belongTo) {
		this(gobjType, 0, belongTo);
	}

	public GObject(ETGObj gobjType) {
		this(gobjType, 1 + RndEx.nextInt(GObjConfig.NM_Runway), 0);
	}

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

	public void startRunning(long runTo, int runway) {
		this.runway = runway;
		this.runTo = runTo;
		this.startRunTime = CalendarEx.now();
		this.isRunning = true;
	}

	public void startRunning(long runTo) {
		startRunning(runTo, this.runway);
	}
	
	public boolean isWolf(){
		return this.gobjType == ETGObj.Wolf;
	}
	
	public boolean isNeutral(){
		return this.gobjType == ETGObj.SheepNeutral;
	}

	public double calcDistance() {
		distance = 0;
		if (isRunning) {
			distance = CalendarEx.now() - this.startRunTime;
			distance *= gobjType.getSpeed() * 0.001;
		}
		return distance;
	}
}
