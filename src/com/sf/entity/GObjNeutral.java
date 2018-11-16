package com.sf.entity;

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

	public GObjNeutral(ETGObj gobjType, int runway, long belongTo) {
		reInit(gobjType, runway, belongTo);
	}

	public boolean isOverMutiny() {
		return numMutiny >= GObjConfig.NMax_Mutiny;
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
	
//	public boolean isCanRu
}
