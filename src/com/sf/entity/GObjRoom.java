package com.sf.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private long winSesid = 0;

	// 随机动物
	GObject gobjWolf = new GObject(ETGObj.Wolf);
	GObject gobjNeutral1 = new GObject(ETGObj.SheepNeutral);
	GObject gobjNeutral2 = new GObject(ETGObj.SheepNeutral);
	private CompGObjEnd comObj = new CompGObjEnd();
	private List<GObject> listEnd = new ArrayList<GObject>();

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

	public boolean isHas(long sesid) {
		return psid1 == sesid || psid2 == sesid;
	}

	void stopSF() {
		if (objSF != null) {
			objSF.cancel(true);
			objSF = null;
		}
	}

	public void clear() {
		synchronized (this) {
			stopSF();
			listEnd.clear();
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
			gobjWolf.stop();
			gobjNeutral1.stop();
			gobjNeutral2.stop();
		}
	}

	public void matching(GObjSession ses) {
		synchronized (this) {
			stopSF();
			long id1 = ses.getId();
			long id2 = getOther(id1);
			GObjSession sesOther = LgcGame.targetSession(id2);
			if (sesOther != null) {
				starting(ses, false);
			} else {
				ses.ready(0);
				state = ETState.Matching;
				objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_RoomMatching);
			}
		}
	}

	public void starting(GObjSession ses, boolean isNdSelf) {
		synchronized (this) {
			long id1 = ses.getId();
			if (!isHas(id1)) {
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
			objSF = Toolkit.scheduled8FixedRate(_ses, this, GObjConfig.LMS_RoomUpFirst, GObjConfig.LMS_RoomUpDelay);

			long runTo = 0;
			if (ses.isRobot()) {
				runTo = id2;
			} else if (sesOther.isRobot()) {
				runTo = id1;
			} else {
				runTo = RndEx.nextBoolean() ? id1 : id2;
			}
			rndStartWolf(runTo);
		}
	}

	public void remove(long sesid) {
		synchronized (this) {
			changeOne(sesid, false);
			if (isEmpty()) {
				clear();
			}
		}
	}

	private void rndStartWolf(long runTo) {
		double speed = GObjConfig.NMin_SpeedWolf;
		if (GObjConfig.NMax_SpeedWolf > speed) {
			double diff = GObjConfig.NMax_SpeedWolf - GObjConfig.NMin_SpeedWolf;
			int nx = (int) (diff * 100);
			speed = GObjConfig.NMin_SpeedWolf + RndEx.nextInt(nx) * 0.01;
		}
		gobjWolf.getGobjType().setSpeed(speed);
		gobjWolf.setRunway(0);
		gobjWolf.startRunning(runTo);
	}

	private void handlerRobotDownSheep(GObjSession ses1, GObjSession ses2) {

	}

	private void handlerWolf(GObjSession ses1, GObjSession ses2) {
		int way = gobjWolf.getRunway();
		GObject tmp1 = null;
		if (gobjWolf.isEnd()) {
			long runTo = getOther(gobjWolf.getRunTo());
			rndStartWolf(runTo);
		} else {
			ses1 = gobjWolf.getRunTo() == psid1 ? ses1 : ses2;
			tmp1 = ses1.getCurr().getFirst4Way(way);
			if (tmp1.isColliding(gobjWolf)) {
				tmp1.runBack();
			}
		}
	}

	private void handlerColliding(GObjSession ses1, GObjSession ses2) {
		int powerState = 0;
		GObject tmp1 = null;
		GObject tmp2 = null;
		for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
			tmp1 = ses1.getCurr().getFirst4Way(i);
			tmp2 = ses2.getCurr().getFirst4Way(i);
			if (tmp1 != null && tmp2 != null) {
				if (tmp1.isColliding(tmp2)) {
					powerState = tmp1.comPower(tmp2);
					switch (powerState) {
					case 1:
						tmp2.runBack();
						break;
					case -1:
						tmp1.runBack();
						break;
					default:
						break;
					}
				}
			}
		}
	}

	private boolean handlerEnd(GObjSession ses1, GObjSession ses2) {
		listEnd.clear();
		int lens = 0;
		boolean isEnd = false;
		GObject tmp1 = null;
		GObjSession tmp = null;
		listEnd.addAll(ses1.getCurr().jugdeEnd());
		listEnd.addAll(ses2.getCurr().jugdeEnd());
		Collections.sort(listEnd, comObj);
		lens = listEnd.size();
		for (int i = 0; i < lens; i++) {
			tmp1 = listEnd.get(i);
			if (tmp1.isMvEnemy()) {
				tmp = tmp1.getBelongTo() == ses1.getId() ? ses2 : ses1;
				tmp.reduceForage(tmp1.getGobjType().getPower());
			}
			if (tmp.isFails()) {
				isEnd = true;
				winSesid = tmp1.getBelongTo();
				break;
			}
			tmp = tmp1.getBelongTo() == ses1.getId() ? ses1 : ses2;
			tmp.getCurr().arriveEnd(tmp1);
		}
		listEnd.clear();
		return isEnd;
	}

	public void upRunning() {
		GObjSession ses1 = LgcGame.targetSession(psid1);
		GObjSession ses2 = LgcGame.targetSession(psid2);
		// 处理机器人 放羊
		handlerRobotDownSheep(ses1, ses2);
		// 处理狼数据
		handlerWolf(ses1, ses2);
		// 处理叛变羊
		// 处理相碰
		handlerColliding(ses1, ses2);
		// 处理终点
		boolean isEnd = handlerEnd(ses1, ses2);
		if (isEnd) {
			stopSF();
			if (winSesid == ses1.getId()) {
				ses1.setState(ETState.Win);
				ses2.setState(ETState.Fail);
			} else {
				ses1.setState(ETState.Fail);
				ses2.setState(ETState.Win);
			}
			this.state = ETState.End;
			objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_RoomEnd);
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			switch (state) {
			case None:
			case End:
				clear();
				break;
			case Matching:
				stopSF();
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
				upRunning();
				break;
			default:
				break;
			}
		}
	}
}
