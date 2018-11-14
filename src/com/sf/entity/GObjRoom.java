package com.sf.entity;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.bowlong.Toolkit;
import com.bowlong.lang.RndEx;
import com.sf.logic.LgcGame;

/**
 * 房间数据
 * 
 * @author Canyon
 * @version createtime：2018-11-11下午2:48:18
 */
public class GObjRoom extends BeanOrigin implements Runnable {
	private ScheduledExecutorService _ses = null;
	private static final long serialVersionUID = 1L;
	private long roomid = 0;
	private long psid1 = 0;
	private long psid2 = 0;
	private ETState state = ETState.None;
	private ScheduledFuture<?> objSF = null;

	// 随机动物
	GObject gobjWolf = new GObject(ETGObj.Wolf);
	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public long getPsid1() {
		return psid1;
	}

	public void setPsid1(long psid1) {
		this.psid1 = psid1;
	}

	public long getPsid2() {
		return psid2;
	}

	public void setPsid2(long psid2) {
		this.psid2 = psid2;
	}

	public ETState getState() {
		return state;
	}

	public void setState(ETState state) {
		this.state = state;
	}

	public GObjRoom() {
		super();
	}

	public GObjRoom(long roomid) {
		super();
		this.roomid = roomid;
		_ses = Toolkit.newScheduledThreadPool("GRoom_" + roomid, 2);
	}

	public long getOther(long psid) {
		return psid == psid1 ? psid2 : psid1;
	}

	private boolean setOne(long psid) {
		if (psid1 == 0) {
			psid1 = psid;
			return true;
		} else if (psid2 == 0) {
			psid2 = psid;
			return true;
		}
		return false;
	}

	private boolean clearOne(long psid) {
		if (psid1 == psid) {
			psid1 = 0;
			return true;
		} else if (psid2 == psid) {
			psid2 = 0;
			return true;
		}
		return false;
	}

	public boolean changeOne(long psid, boolean isAdd) {
		if (isAdd) {
			return setOne(psid);
		} else {
			return clearOne(psid);
		}
	}

	public boolean isEmpty() {
		return psid1 <= 0 && psid2 <= 0;
	}

	public boolean isFree() {
		return psid1 <= 0 || psid2 <= 0;
	}
	
	public boolean isHas(long sesid){
		return psid1 == sesid || psid2 == sesid; 
	}

	public void clear() {
		synchronized (this) {
			if (objSF != null) {
				objSF.cancel(true);
				objSF = null;
			}

			GObjSession ses1 = LgcGame.targetSession(psid1);
			if (ses1 != null) {
				ses1.clear();
			}
			ses1 = LgcGame.targetSession(psid2);
			if (ses1 != null) {
				ses1.clear();
			}

			psid1 = 0;
			psid2 = 0;
			state = ETState.None;
		}
	}

	public void matching(GObjSession ses) {
		synchronized (this) {
			if (objSF != null) {
				objSF.cancel(true);
				objSF = null;
			}
			long id1 = ses.getId();
			long id2 = getOther(id1);
			GObjSession sesOther = LgcGame.targetSession(id2);
			if (sesOther != null) {
				starting(ses, false);
			} else {
				ses.ready(0);
				state = ETState.Matching;
				objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_Matching);
			}
		}
	}

	public void starting(GObjSession ses, boolean isNdSelf) {
		synchronized (this) {
			long id1 = ses.getId();
			if(!isHas(id1)){
				return;
			}
			long id2 = getOther(id1);
			GObjSession sesOther = LgcGame.targetSession(id2);
			if (ses.isRobot() && sesOther.isRobot()) {
				clear();
				return;
			}

			state = ETState.Running;
			ses.start(id2);
			sesOther.start(id1);
			if (!sesOther.isRobot()) {
				sesOther.addNotify(ETNotify.Enemy_Matched);
			}
			if (isNdSelf && !ses.isRobot()) {
				ses.addNotify(ETNotify.Enemy_Matched);
			}
			objSF = Toolkit.scheduled8FixedRate(_ses, this, 0, 100);
			
			long runTo = 0;
			if (ses.isRobot()) {
				runTo = id2;
			} else if (sesOther.isRobot()) {
				runTo = id1;
			} else {
				runTo = RndEx.nextBoolean() ? id1 : id2;
			}
			gobjWolf.startRunning(runTo);
		}
	}

	public void remove(long sesid) {
		synchronized (this) {
			changeOne(sesid, false);
			if (isEmpty()) {
				state = ETState.None;
			}
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			switch (state) {
			case None:
				clear();
				break;
			case Matching:
				objSF = null;
				if (!isEmpty() && isFree()) {
					long sid = psid1 > 0 ? psid1 : psid2;
					GObjSession objSes = LgcGame.targetSession(sid);
					if (objSes.isRobot()) {
						LgcGame.remove4Room(objSes);
						return;
					}

					// 添加一个机器人
					String strRobot = String.format("robot_%s", RndEx.nextString09(9));
					GObjSession robot = new GObjSession(strRobot, strRobot);
					robot.ReLmtOver(0);
					changeOne(robot.getId(), true);
					starting(objSes, true);
				}
				break;
			case Running:
				break;
			default:
				break;
			}
		}
	}
}
