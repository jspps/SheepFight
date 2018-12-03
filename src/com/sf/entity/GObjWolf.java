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
	private long preRunto;
	private long ms_next_rndway = 0; // 在一条道上，待久了没羊来，就随机跑道

	public long getPreRunto() {
		return preRunto;
	}

	public GObjWolf() {
		super(ETGObj.Wolf, 0);
	}

	@Override
	public void ready(double initPos) {
		super.ready(initPos);
		double speed = RndEx.nextDouble(GObjConfig.NMin_SpeedWolf, GObjConfig.NMax_SpeedWolf);
		setSpeed(speed);
		setRunway(rndWay());
		this.ms_next_rndway = now() + GObjConfig.LMS_NextRndWay_Wolf;
	}

	public void ready() {
		ready(GObjConfig.NI_PosWolf);
	}

	@Override
	public void startRunning(long runTo) {
		if (this.isRunning())
			return;
		super.startRunning(runTo);
	}

	public void disappear(boolean isReLive) {
		this.preRunto = getRunTo();
		int delay = isReLive ? GObjConfig.LMS_NextLive_Wolf : 0;
		super.disappear(delay);
	}

	public void readyGo(long runTo) {
		this.ready(0);
		startRunning(runTo);
	}
	
	public void nextRndWay(){
		if(this.isRunning())
			return;
		
		if(this.ms_next_rndway > 0){
			if(now() >= this.ms_next_rndway){
				ready();
			}
		}
	}
}
