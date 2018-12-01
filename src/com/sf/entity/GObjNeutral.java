package com.sf.entity;

import com.bowlong.lang.RndEx;

/**
 * 叛变羊
 * 
 * @author Canyon
 * @version createtime：2018-11-13下午7:22:20
 */
public class GObjNeutral extends GObject {
	private static final long serialVersionUID = 1L;

	private int numMutiny; // 叛变次数
	private long nextLiveMs; // 下次复活时间

	public GObjNeutral(int runway) {
		super(ETGObj.SheepNeutral, runway,0);
	}

	public boolean isOverMutiny() {
		return numMutiny >= GObjConfig.NMax_NeutralMutiny;
	}

	public void doMutiny(long beTo, long runTo,int power) {
		this.numMutiny++;
		double speed = RndEx.nextDouble(GObjConfig.NMin_SpeedNeutral, GObjConfig.NMax_SpeedNeutral);
		getGobjType().setSpeed(speed);
		getGobjType().setPower(power);
		setBelongTo(beTo);
		runBack(runTo, false);
	}

	public void disappear(boolean isReLive) {
		int way = getRunway();
		setBelongTo(0);
		stop();
		nextLiveMs = 0;
		numMutiny = 0;
		setRunway(way);
		if (isReLive)
			nextLiveMs = now() + GObjConfig.LMS_NextLive_Neutral;
	}

	public boolean isCanRelive() {
		if (this.nextLiveMs > 0)
			return this.nextLiveMs <= now();
		return false;
	}
	
	@Override
	public void ready(double initPos) {
		super.ready(initPos);
		this.nextLiveMs = 0;
	}
	
	public void ready(){
		ready(GObjConfig.NI_PosNeutral);
	}

	@Override
	public void startRunning(long runTo) {
		super.startRunning(runTo);
		this.nextLiveMs = 0;
	}
}
