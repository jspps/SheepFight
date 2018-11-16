package com.sf.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	private static final long serialVersionUID = 1L;
	private ScheduledExecutorService _ses = null;
	private long roomid = 0;
	private long sesid1 = 0;
	private long sesid2 = 0;
	private ETState state = ETState.None;
	private ScheduledFuture<?> objSF = null;
	private long winSesid = 0;
	// 随机动物
	private GObject gobjWolf = new GObject(ETGObj.Wolf, 1, 0);
	private int NMutiny1 = 0, NMutiny2 = 0; // 叛变次数
	private GObject gobjNeutral1 = new GObject(ETGObj.SheepNeutral, 2, 0);
	private GObject gobjNeutral2 = new GObject(ETGObj.SheepNeutral, 4, 0);
	private CompGObjEnd comObj = new CompGObjEnd();
	private List<GObject> listEnd = new ArrayList<GObject>();
	private long overtime_ms = 0;
	private int robot_ai = -1; // 放羊ai
	private GObjSession gsesRobot = new GObjSession(); // 机器人
	private int notifyCount = 0;

	public long getRoomid() {
		return roomid;
	}

	public void setRoomid(long roomid) {
		this.roomid = roomid;
	}

	public ETState getState() {
		return state;
	}

	public long getOvertime_ms() {
		return overtime_ms;
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
		return psid == sesid1 ? sesid2 : sesid1;
	}

	private boolean setOne(long psid) {
		if (sesid1 == 0) {
			sesid1 = psid;
			return true;
		} else if (sesid2 == 0) {
			sesid2 = psid;
			return true;
		}
		return false;
	}

	private boolean clearOne(long psid) {
		if (sesid1 == psid) {
			sesid1 = 0;
			return true;
		} else if (sesid2 == psid) {
			sesid2 = 0;
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
		return sesid1 <= 0 && sesid2 <= 0;
	}

	public boolean isFree() {
		return (sesid1 <= 0 || sesid2 <= 0) && (state == ETState.None || state == ETState.Matching);
	}

	public GObjSession getRobot() {
		return gsesRobot;
	}

	public boolean isHas(long sesid) {
		return sesid1 == sesid || sesid2 == sesid;
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
			state = ETState.None;
			robot_ai = -1;
			notifyCount = 0;
			overtime_ms = 0;
			listEnd.clear();
			GObjSession ses1 = LgcGame.targetSession(sesid1);
			if (ses1 != null) {
				ses1.clear();
			}
			ses1 = LgcGame.targetSession(sesid2);
			if (ses1 != null) {
				ses1.clear();
			}
			gsesRobot.clear();

			sesid1 = 0;
			sesid2 = 0;
			gobjWolf.stop();
			NMutiny1 = 0;
			NMutiny2 = 0;
			gobjNeutral1.setBelongTo(0);
			gobjNeutral2.setBelongTo(0);
			gobjNeutral1.stop();
			gobjNeutral2.stop();
		}
	}

	public List<Map<String, Object>> listMap(List<Map<String, Object>> lMap) {
		if (gobjWolf.isRunning()) {
			lMap.add(gobjWolf.toMap());
		}
		if (gobjNeutral1.isRunning()) {
			lMap.add(gobjNeutral1.toMap());
		}
		if (gobjNeutral2.isRunning()) {
			lMap.add(gobjNeutral2.toMap());
		}
		return lMap;
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
				objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_RoomMatching);
				state = ETState.Matching;
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

			ses.start(id2);
			sesOther.start(id1);

			long runTo = 0;
			if (ses.isRobot()) {
				runTo = id2;
			} else if (sesOther.isRobot()) {
				runTo = id1;
			} else {
				runTo = RndEx.nextBoolean() ? id1 : id2;
			}
			rndStartWolf(runTo);

			runTo = RndEx.nextBoolean() ? id1 : id2;
			rndStartNeutral(gobjNeutral1, runTo);

			runTo = RndEx.nextBoolean() ? id1 : id2;
			rndStartNeutral(gobjNeutral2, runTo);
			overtime_ms = now() + GObjConfig.NMax_RoomAllTime;

			state = ETState.Running;
			if (!sesOther.isRobot()) {
				sesOther.addNotify(ETNotify.MatchedEnemy);
			}
			if (isNdSelf && !ses.isRobot()) {
				ses.addNotify(ETNotify.MatchedEnemy);
			}
			// objSF = Toolkit.scheduled8FixedRate(_ses, this,200,500);
			objSF = Toolkit.scheduled8FixedRate(_ses, this, GObjConfig.LMS_RoomUpFirst, GObjConfig.LMS_RoomUpDelay);
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

	private double rndSpeed(double min, double max) {
		double speed = min;
		if (max > speed) {
			double diff = max - min;
			int nx = (int) (diff * 100);
			speed = min + RndEx.nextInt(nx) * 0.01;
		}
		return speed > 0 ? speed : 1;
	}

	private void rndStartGobj(GObject gobj, long runTo, double sp_min, double sp_max) {
		double speed = rndSpeed(sp_min, sp_max);
		gobj.getGobjType().setSpeed(speed);
		gobj.startRunning(runTo);
	}

	void rndStartWolf(long runTo) {
		gobjWolf.setRunway(0);
		rndStartGobj(gobjWolf, runTo, GObjConfig.NMin_SpeedWolf, GObjConfig.NMax_SpeedWolf);
	}

	void rndStartNeutral(GObject gobj, long runTo) {
		rndStartGobj(gobj, runTo, GObjConfig.NMin_SpeedNeutral, GObjConfig.NMax_SpeedNeutral);
	}

	private void handlerWaitSheep(GObjSession ses1, GObjSession ses2) {
		boolean isOkey1 = ses1.getCurr().jugdeRndSheep();
		boolean isOkey2 = ses2.getCurr().jugdeRndSheep();
		if (isOkey1) {
			ses1.addNotify(ETNotify.WaitSelf);
			ses2.addNotify(ETNotify.WaitEnemy);
		}

		if (isOkey2) {
			ses1.addNotify(ETNotify.WaitEnemy);
			ses2.addNotify(ETNotify.WaitSelf);
		}
	}

	private void handlerRobotDownSheep(GObjSession ses1, GObjSession ses2) {
		if (!gsesRobot.isStart()) {
			return;
		}

		if (gsesRobot.isEmptyWait()) {
			return;
		}
		ses1 = gsesRobot == ses1 ? ses2 : ses1;
		long runTo = ses1.getId();
		long sheepId = gsesRobot.getCurr().getMaxPowerInWait().getId();
		int runway = 0;
		int power1 = 1, power2 = 0;
		switch (robot_ai) {
		case 1:
			for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
				power1 = gsesRobot.getCurr().getAllPower4Way(i);
				power2 = ses1.getCurr().getAllPower4Way(i);
				if (power2 >= power1 + 2) {
					runway = i;
					break;
				}
			}
			if (runway == 0) {
				for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
					power1 = gsesRobot.getCurr().getAllPower4Way(i);
					power2 = ses1.getCurr().getAllPower4Way(i);
					if (power1 > 0 && power2 <= power1) {
						runway = i;
						break;
					}
				}
			}
			break;
		}
		gsesRobot.getCurr().startRunning(sheepId, runway, runTo);
	}

	private void handlerWolf(GObjSession ses1, GObjSession ses2) {
		int way = gobjWolf.getRunway();
		GObject tmp1 = null;
		if (gobjWolf.isEnd()) {
			long runTo = getOther(gobjWolf.getRunTo());
			rndStartWolf(runTo);
		} else {
			ses1 = gobjWolf.getRunTo() == sesid1 ? ses1 : ses2;
			tmp1 = ses1.getCurr().getFirst4Way(way);
			if (tmp1 != null && tmp1.isColliding(gobjWolf)) {
				tmp1.runBack(2);
			}
		}
	}

	private void handlerNeutral(GObjSession ses1, GObjSession ses2) {
		GObject[] arrs = { gobjNeutral1, gobjNeutral2 };
		GObject tmp1 = null;
		GObject tmp2 = null;
		GObjSession tmpSes = null;
		long beTo = 0;
		long runTo = 0;
		int way = 0;
		int power1 = 0;
		int power2 = 0;
		for (int i = 0; i < arrs.length; i++) {
			tmp1 = arrs[i];
			beTo = tmp1.getBelongTo();
			runTo = tmp1.getRunTo();
			way = tmp1.getRunway();
			if (tmp1.isEnd()) {
				tmp1.setBelongTo(0);
				tmp1.stop();
			} else if (tmp1.isRunning()) {
				if (beTo > 0) {
					tmpSes = beTo == ses1.getId() ? ses2 : ses1;
				} else {
					if (runTo == ses1.getId()) {
						tmpSes = ses1;
						beTo = ses2.getId();
					} else {
						tmpSes = ses2;
						beTo = ses1.getId();
					}

				}
				tmp2 = tmpSes.getCurr().getFirst4Way(way);
				if (tmp2 != null && tmp1.isColliding(tmp2)) {
					if ((i == 0 && NMutiny1 >= GObjConfig.NMax_Mutiny)
							|| (i == 1 && NMutiny2 >= GObjConfig.NMax_Mutiny)) {
						tmp1.setBelongTo(0);
						tmp1.stop();
						continue;
					}
					power1 = tmp1.getGobjType().getPower();
					power2 = tmp2.getGobjType().getPower();
					if (power1 < power2) {
						tmp1.setBelongTo(tmpSes.getId());
						tmp1.runBack(beTo, false);
						if (i == 0)
							NMutiny1++;
						else
							NMutiny2++;
					} else if (power1 > power2) {
						tmp2.runBack(0);
					}
				}
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
						tmp2.runBack(1.5);
						break;
					case -1:
						tmp1.runBack(1.5);
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
		listEnd.addAll(ses1.getCurr().jugdeArrive());
		listEnd.addAll(ses2.getCurr().jugdeArrive());
		Collections.sort(listEnd, comObj);
		lens = listEnd.size();
		for (int i = 0; i < lens; i++) {
			tmp1 = listEnd.get(i);
			if (tmp1.isMvEnemy()) {
				tmp = tmp1.getBelongTo() == ses1.getId() ? ses2 : ses1;
				tmp.reduceForage(tmp1.getGobjType().getPower());
				if (tmp.isFails()) {
					isEnd = true;
					winSesid = tmp1.getBelongTo();
					break;
				}
			}
			tmp = tmp1.getBelongTo() == ses1.getId() ? ses1 : ses2;
			tmp.getCurr().onArrive(tmp1);
		}
		listEnd.clear();
		return isEnd;
	}

	private void _toEnd(GObjSession ses1, GObjSession ses2) {
		stopSF();
		if (winSesid == ses1.getId()) {
			ses1.setState(ETState.Win);
			ses2.setState(ETState.Fail);
		} else {
			ses1.setState(ETState.Fail);
			ses2.setState(ETState.Win);
		}
		objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_RoomWaitEnd);
		notifyCount = robot_ai != -1 ? 1 : 2;
		this.state = ETState.End;
		ses1.addNotify(ETNotify.FightEnd);
		ses2.addNotify(ETNotify.FightEnd);
	}

	public void upRunning() {
		if (isFree()) {
			return;
		}

		GObjSession ses1 = LgcGame.targetSession(sesid1);
		GObjSession ses2 = LgcGame.targetSession(sesid2);
		ses1 = ses1 == null ? gsesRobot : ses1;
		ses2 = ses2 == null ? gsesRobot : ses2;
		if (ses1 == ses2) {
			this.state = ETState.End;
			return;
		}

		if (overtime_ms < now()) {
			if (ses1.getCurr().getForage() >= ses2.getCurr().getForage()) {
				winSesid = ses1.getId();
			} else {
				winSesid = ses2.getId();
			}
			_toEnd(ses1, ses2);
			return;
		}
		// 处理机器人 放羊
		handlerRobotDownSheep(ses1, ses2);
		// 判断产生wait随机羊
		handlerWaitSheep(ses1, ses2);
		// 处理狼数据
		handlerWolf(ses1, ses2);
		// 处理叛变羊
		handlerNeutral(ses1, ses2);
		// 处理相碰
		handlerColliding(ses1, ses2);
		// 处理终点
		boolean isEnd = handlerEnd(ses1, ses2);
		if (isEnd) {
			_toEnd(ses1, ses2);
		} else {
			ses1.addNotify(ETNotify.Update);
			ses2.addNotify(ETNotify.Update);
		}
	}

	public void notifyEnd() {
		synchronized (this) {
			notifyCount--;
			if (notifyCount <= 0) {
				clear();
			}
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			try {
				switch (state) {
				case End:
					clear();
					break;
				case Matching:
					stopSF();
					if (!isEmpty() && isFree()) {
						// 添加一个机器人
						robot_ai = RndEx.nextInt(2);
						String strRobot = String.format("robot_%s", RndEx.nextString09(9));
						gsesRobot.reInit(strRobot, strRobot);
						gsesRobot.setRoomid(roomid);
						changeOne(gsesRobot.getId(), true);
						starting(gsesRobot, true);
					}
					break;
				case Running:
					upRunning();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
