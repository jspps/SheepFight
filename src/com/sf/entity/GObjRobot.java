package com.sf.entity;

import com.bowlong.lang.RndEx;

/**
 * 机器人
 * 
 * @author Canyon
 * @version createtime：2018-11-13下午7:43:04
 */
public class GObjRobot extends GObjSession {
	private static final long serialVersionUID = 1L;

	private int aiDown;
	private long msNextDown;

	public int getAiDown() {
		return aiDown;
	}

	public void setAiDown(int aiDown) {
		this.aiDown = aiDown;
	}

	public long getMsNextDown() {
		return msNextDown;
	}

	public void setMsNextDown(long msNextDown) {
		this.msNextDown = msNextDown;
	}

	public GObjRobot() {
		super();
	}

	public void reRobot(long roomid) {
		this.aiDown = RndEx.nextInt(2);
		this.msNextDown = now() + GObjConfig.LMS_FirstRobotDownSheep;
		String strRobot = String.format("robot_%s", RndEx.nextString09(9));
		initSes(strRobot, strRobot);
		setRoomid(roomid);
	}

	public boolean isNoCanDown() {
		return !isStart() || isEmptyWait() || msNextDown > now();
	}

	@Override
	public void downSheep(long sheepId, int runway, long runTo) {
		super.downSheep(sheepId, runway, runTo);
		this.msNextDown = now() + GObjConfig.LMS_NextRobotDownSheep;
	}

	@Override
	public void clear() {
		this.msNextDown = 0;
		super.clear();
	}

	@Override
	public boolean jugdeRndSheep() {
		boolean isEmpty = isEmptyWait();
		boolean isOkey = super.jugdeRndSheep();
		if(isOkey && isEmpty){
			this.msNextDown = now() + GObjConfig.LMS_NextRobotDownSheep;
		}
		return isOkey;
	}

	
}
