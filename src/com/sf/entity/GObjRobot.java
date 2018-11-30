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
		int rndVal = RndEx.nextInt(GObjConfig.NMax_RobotAI_Basic);
		this.aiDown = (rndVal <= GObjConfig.NL_RobotAI_Middle) ? 1 : 0;
		this.msNextDown = now() + GObjConfig.LMS_FirstRobotDownSheep;
		String strRobot = String.format("robot_%s", RndEx.nextString09(9));
		initSes(strRobot, strRobot);
		setRoomid(roomid);
	}

	public boolean isNoCanDown() {
		return !isStart() || isEmptyWait() || msNextDown > now();
	}

	@Override
	public boolean downSheep(long sheepId, int runway, long runTo) {
		boolean isOkey = super.downSheep(sheepId, runway, runTo);
		if (isOkey) {
			this.msNextDown = now() + GObjConfig.LMS_NextRobotDownSheep;
		}
		return isOkey;
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
		if (isOkey && isEmpty) {
			this.msNextDown = now() + GObjConfig.LMS_NextRobotDownSheep;
		}
		return isOkey;
	}

}
