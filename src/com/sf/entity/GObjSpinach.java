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
	private long endEatMs = 0;
	private GObject gobjEating = null;
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

	private double diffDistance(GObject gobj) {
		double lens = GObjConfig.LenMax_Runway;
		if (gobj == null)
			return lens;

		double dis = gobj.allDistance();
		double disCur = allDistance();
		boolean isSameDir = gobj.getId() == getBelongTo();
		return isSameDir ? (disCur - dis) : (lens - disCur - dis);
	}

	public void disappear(boolean isReLive) {
		this.endEatMs = 0;
		this.gobjEating = null;
		int delay = isReLive ? GObjConfig.LMS_NextLive_Spinach : 0;
		super.disappear(delay);
	}
	
	private void changeBig(){
		this.gobjEating.setGobjType(ETGObj.SheepBig);
		this.gobjEating.goOn();
		this.disappear(true);
	}
	
	private void resetEat(GObject gobj){
		this.endEatMs = now() + this.getGobjType().getPower();
		this.gobjEating = gobj;
		this.gobjEating.isStay(true);
	}
	
	public void beEating(GObject gobj){
		if(!this.isReadyRunning() || gobj == null){
			return;
		}
		
		if(this.gobjEating != null){
			if(!this.gobjEating.isReadyRunning()){
				this.endEatMs = 0;
				this.gobjEating = null;
			}
			
			if(gobj == this.gobjEating){
				if(this.endEatMs > 0 && now() >= this.endEatMs){
					this.changeBig();
				}
				return;
			}
		}
		
		double df1 = diffDistance(gobj);
		if (df1 >= 0 && df1 <= GObjConfig.NMax_CollidDistance) {
			if(this.gobjEating == null){
				this.resetEat(gobj);
			}else{
				if(this.gobjEating.getPower() > gobj.getPower()){
					gobj.runBack();
				}else if(this.gobjEating.getPower() < gobj.getPower()){
					this.gobjEating.runBack();
					this.resetEat(gobj);
				}
			}
		}
	}
}
