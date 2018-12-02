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

	public GObjNeutral(int runway) {
		super(ETGObj.SheepNeutral, runway,0);
	}

	public boolean isOverMutiny() {
		return numMutiny >= GObjConfig.NMax_NeutralMutiny;
	}

	public void doMutiny(long beTo, long runTo,int power) {
		this.numMutiny++;
		double speed = RndEx.nextDouble(GObjConfig.NMin_SpeedNeutral, GObjConfig.NMax_SpeedNeutral);
		setSpeed(speed);
		setPower(power);
		setBelongTo(beTo);
		runBack(runTo, false);
	}

	public void disappear(boolean isReLive) {
		numMutiny = 0;
		setPower(0);
		int delay = isReLive ? GObjConfig.LMS_NextLive_Neutral : 0;
		super.disappear(delay);
	}
		
	public void ready(){
		ready(GObjConfig.NI_PosNeutral);
	}
}
