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

	public GObjSpinach(int runway, long belongTo) {
		reInit(ETGObj.Spinach, runway, belongTo);
	}

	@Override
	public GObject reInit(ETGObj gobjType, int runway, long belongTo) {
		super.reInit(gobjType, runway, belongTo);
		this.disPos = RndEx.nextDouble(GObjConfig.NMin_PosSpinach, GObjConfig.NMax_PosSpinach);
		return this;
	}

	@Override
	public double calcDistance() {
		return disPos;
	}

}
