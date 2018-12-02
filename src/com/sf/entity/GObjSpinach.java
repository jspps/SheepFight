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

	public GObjSpinach() {
		super(ETGObj.Spinach, 0);
	}
	
	public void readyIn(long beTo) {
		if (now() <= this.nextLiveMs)
			return;
		setBelongTo(beTo); // 通过beto来判断距离(disPos 的距离)
		setRunway(0);
		double _initPos = RndEx.nextDouble(GObjConfig.NMin_PosSpinach, GObjConfig.NMax_PosSpinach);
		ready(_initPos);
	}

	public double diffDistance(GObject gobj) {
		double lens = GObjConfig.LenMax_Runway;
		if (gobj == null)
			return lens;

		double dis = gobj.allDistance();
		double disCur = allDistance();
		boolean isSameDir = gobj.getId() == getBelongTo();
		return isSameDir ? (disCur - dis) : (lens - disCur - dis);
	}

	public void disappear(boolean isReLive) {
		int delay = isReLive ? GObjConfig.LMS_NextLive_Spinach : 0;
		super.disappear(delay);
	}
}
