package com.sf.entity;

import com.bowlong.lang.RndEx;

/**
 * 菠菜罐头
 * 
 * @author Canyon
 * @version createtime：2018-11-17下午7:16:09
 */
public class GObjSpinach extends GObject {
	private static final long serialVersionUID = 1L;

	private double disPos; // 位置
	private long nextLiveMs; // 下次复活时间

	public GObjSpinach() {
		super(ETGObj.Spinach, 0);
	}

	public void startRnd(long beTo) {
		if (now() <= this.nextLiveMs)
			return;
		this.nextLiveMs = 0;
		this.disPos = RndEx.nextDouble(GObjConfig.NMin_PosSpinach, GObjConfig.NMax_PosSpinach);
		setBelongTo(beTo); // 通过beto来判断距离(disPos 的距离)
		setRunTo(0);
		setRunway(rndWay());
		this.nState = 1;
		// setRunning(true);
	}

	public double diffDistance(GObject gobj) {
		double lens = GObjConfig.LenMax_Runway;
		if(gobj == null)
			return lens;
		
		double dis = gobj.calcDistance();
		boolean isSameDir = gobj.getId() == getBelongTo();
		return isSameDir ? (this.disPos - dis) : (lens - this.disPos - dis);
	}

	public void disappear(boolean isReLive) {
		stop();
		setBelongTo(0);
		this.disPos = 0;
		if (isReLive)
			this.nextLiveMs = now() + GObjConfig.LMS_NextLive_Spinach;
		else
			this.nextLiveMs = 0;
	}
	
	public boolean isCanRelive() {
		if (this.nextLiveMs > 0)
			return this.nextLiveMs <= now();
		return false;
	}

	@Override
	public double calcDistance() {
		return this.disPos;
	}
	
	
}
