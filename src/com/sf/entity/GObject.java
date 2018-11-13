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
	ETGObj gobjType = ETGObj.SheepSmall; // 类型
	int numRunway = 0; // 跑道编号
	long createtime = 0; // 创建时间
	long startRunTime = 0;// 开始跑的时间
	long belongTo = 0; // 拥有者
	long runTo = 0; // 跑向的人
	boolean isRunning = false;
	double distance = 0.0;

	public ETGObj getGobjType() {
		return gobjType;
	}

	public void setGobjType(ETGObj gobjType) {
		this.gobjType = gobjType;
	}

	public int getNumRunway() {
		return numRunway;
	}

	public void setNumRunway(int numRunway) {
		this.numRunway = numRunway;
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
		this.createtime = CalendarEx.now();
	}

	public GObject(ETGObj gobjType, int numRunway, long belongTo) {
		super();
		this.gobjType = gobjType;
		this.numRunway = numRunway;
		this.belongTo = belongTo;
		this.createtime = CalendarEx.now();
	}

	public GObject(ETGObj gobjType, int numRunway) {
		this(gobjType, numRunway, 0);
	}

	public GObject(ETGObj gobjType) {
		this(gobjType, 1 + RndEx.nextInt(GObjConfig.NM_Runway));
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		// map.put("type", gobjType.name());
		map.put("numRunway", numRunway);
		map.put("belongTo", belongTo);
		map.put("runTo", runTo);
		map.put("isRunning", isRunning);
		map.put("distance", calcDistance());
		map = gobjType.toMap(map);
		return map;
	}

	public void StartRunning(long runTo) {
		this.runTo = runTo;
		this.startRunTime = CalendarEx.now();
		this.isRunning = true;
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
