package com.sf.entity;

import com.bowlong.lang.RndEx;


/**
 * 狼
 * 
 * @author Canyon
 * @version createtime：2018-11-17下午7:16:09
 */
public class GObjWolf extends GObject {
	private static final long serialVersionUID = 1L;

	private long nextLiveMs; // 下次复活时间
	
	public GObjWolf() {
		reInit(ETGObj.Wolf,0,0);
	}
	
	@Override
	public void ready(double initPos) {
		super.ready(initPos);
		this.nextLiveMs = 0;
		double speed = RndEx.nextDouble(GObjConfig.NMin_SpeedWolf, GObjConfig.NMax_SpeedWolf);
		getGobjType().setSpeed(speed);
		setRunway(rndWay());
	}
	
	public void ready(){
		ready(GObjConfig.NI_PosWolf);
	}
	
	@Override
	public void startRunning(long runTo) {
		if(this.isRunning())
			return;
		super.startRunning(runTo);
	}
	
	public void disappear(boolean isReLive) {
		setBelongTo(0);
		stop();
		nextLiveMs = 0;
		if (isReLive)
			nextLiveMs = now() + GObjConfig.LMS_NextLive_Wolf;
	}
	
	public boolean isCanRelive() {
		if (this.nextLiveMs > 0)
			return this.nextLiveMs <= now();
		return false;
	}
}
