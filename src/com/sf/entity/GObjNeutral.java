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

	public int getNumMutiny() {
		return numMutiny;
	}

	public void setNumMutiny(int numMutiny) {
		this.numMutiny = numMutiny;
	}

	public long getNextLiveMs() {
		return nextLiveMs;
	}

	public void setNextLiveMs(long nextLiveMs) {
		this.nextLiveMs = nextLiveMs;
	}

	public GObjNeutral(int runway, long belongTo) {
		reInit(ETGObj.SheepNeutral, runway, belongTo);
		int power = RndEx.nextInt(GObjConfig.NMin_NeutralPower, GObjConfig.NMax_NeutralPower + 1);
		this.getGobjType().setPower(power);
	}

	public boolean isOverMutiny() {
		return numMutiny >= GObjConfig.NMax_NeutralMutiny;
	}

	public void doMutiny(long beTo, long runTo) {
		setBelongTo(beTo);
		runBack(runTo, false);
		this.numMutiny++;
	}

	public void disappear(boolean isReLive) {
		setBelongTo(0);
		stop();
		nextLiveMs = 0;
		numMutiny = 0;
		if (isReLive)
			nextLiveMs = now() + GObjConfig.LMS_Neutral_NextLive;
	}
}
