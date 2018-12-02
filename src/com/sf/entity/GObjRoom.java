package com.sf.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.bowlong.Toolkit;
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
	private int notifyCount = 0; // 结束后需要通知的人数
	private long ms_over = 0; // 房间结束时间点
	private long ms_start = 0; // 开始时间
	private boolean isMatchRobot = false;
	// 随机动物
	private CompGObjEnd comObj = new CompGObjEnd();
	private GObjRobot robot = new GObjRobot(); // 机器人
	private GObjWolf wolf = new GObjWolf();
	private GObjNeutral neutral1 = new GObjNeutral(2);
	private GObjNeutral neutral2 = new GObjNeutral(4);
	private GObjSpinach spinach = new GObjSpinach();
	private List<GObject> listEnd = new ArrayList<GObject>();

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
		return ms_over;
	}

	public boolean isMatchRobot() {
		return isMatchRobot;
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
		return robot;
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
			notifyCount = 0;
			ms_over = 0;
			ms_start = 0;
			listEnd.clear();
			GObjSession ses1 = LgcGame.targetSession(sesid1);
			if (ses1 != null) {
				ses1.clear();
			}
			ses1 = LgcGame.targetSession(sesid2);
			if (ses1 != null) {
				ses1.clear();
			}
			sesid1 = 0;
			sesid2 = 0;
			isMatchRobot = false;
			robot.clear();
			wolf.disappear(false);
			neutral1.disappear(false);
			neutral2.disappear(false);
			spinach.disappear(false);
		}
	}

	public List<GObject> listScene(List<GObject> list) {
		if (wolf.isReadyRunning()) {
			list.add(wolf);
		}
		if (neutral1.isReadyRunning()) {
			list.add(neutral1);
		}
		if (neutral2.isReadyRunning()) {
			list.add(neutral2);
		}
		if (spinach.isReadyRunning()) {
			list.add(spinach);
		}
		return list;
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

	public void starting(GObjSession ses, boolean isNoticeSelf) {
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

			wolf.ready();
			neutral1.ready();
			neutral2.ready();
			spinach.readyIn(0);
			ms_start = now();
			ms_over = ms_start + GObjConfig.NMax_RoomTime;

			state = ETState.Running;
			if (!sesOther.isRobot()) {
				sesOther.addNotify(ETNotify.MatchedEnemy);
			}
			if (isNoticeSelf && !ses.isRobot()) {
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

	void handlerWaitSheep(GObjSession ses1, GObjSession ses2) {
		boolean isOkey1 = ses1.jugdeRndSheep();
		boolean isOkey2 = ses2.jugdeRndSheep();
		if (isOkey1) {
			ses1.addNotify(ETNotify.WaitSelf);
			ses2.addNotify(ETNotify.WaitEnemy);
		}

		if (isOkey2) {
			ses1.addNotify(ETNotify.WaitEnemy);
			ses2.addNotify(ETNotify.WaitSelf);
		}
	}

	void handlerRobotDownSheep(GObjSession ses1, GObjSession ses2) {
		if (robot.isNoCanDown()) {
			return;
		}

		ses1 = robot == ses1 ? ses2 : ses1;
		long runTo = ses1.getId();
		long sheepId = robot.getMaxPowerInWait().getId();
		int runway = 0;
		int power1 = 1, power2 = 0;
		switch (robot.getAiDown()) {
		case 1:
			for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
				power1 = robot.getAllPower4Way(i);
				power2 = ses1.getAllPower4Way(i);
				if (power2 >= power1 + 2) {
					runway = i;
					break;
				}
			}
			if (runway == 0) {
				for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
					power1 = robot.getAllPower4Way(i);
					power2 = ses1.getAllPower4Way(i);
					if (power1 > 0 && power2 <= power1) {
						runway = i;
						break;
					}
				}
			}
			break;
		}
		robot.downSheep(sheepId, runway, runTo);
	}

	void handlerWolf(GObjSession ses1, GObjSession ses2) {
		if (wolf.isEnd()) {
			wolf.disappear(true);
		} else if (wolf.isCanRelive()) {
			// long runTo = getOther(wolf.getPreRunto());
			// wolf.readyGo(runTo);
			wolf.ready();
		} else if (wolf.isReadyRunning()) {
			_wolf(ses1);
			_wolf(ses2);
			if (wolf.isRunning()) {
				GObjNeutral neutral = null;
				int way = wolf.getRunway();
				switch (way) {
				case 2:
					if (neutral1.isRunning()) {
						neutral = neutral1;
					}
					break;
				case 4:
					if (neutral2.isRunning()) {
						neutral = neutral2;
					}
					break;
				default:
					break;
				}
				if (wolf.isColliding(neutral)) {
					neutral.disappear(true);
				}
			}
		}
	}

	private long _wolf(GObjSession ses) {
		int way = wolf.getRunway();
		long runTo = wolf.getRunTo();
		boolean isRunZero = runTo <= 0;
		List<GObject> list = ses.getList4Way(way);
		int lens = list.size();
		GObject tmp1 = null;
		for (int i = 0; i < lens; i++) {
			tmp1 = list.get(i);
			if (wolf.isColliding(tmp1)) {
				ses.onArrive(tmp1);
				runTo = ses.getId();
			}
		}
		if (isRunZero && runTo > 0) {
			wolf.startRunning(runTo);
		}
		return runTo;
	}

	void handlerSpinach(GObjSession ses1, GObjSession ses2) {
		if (spinach.isCanRelive()) {
			spinach.readyIn(0);
			return;
		}

		if (!spinach.isReadyRunning()) {
			return;
		}
		int way = spinach.getRunway();
		GObject tmp1 = ses1.getFirst4Way(way);
		GObject tmp2 = ses2.getFirst4Way(way);
		spinach.beEating(tmp1);
		spinach.beEating(tmp2);
	}

	void handlerNeutral(GObjSession ses1, GObjSession ses2) {
		GObjNeutral[] arrs = { neutral1, neutral2 };
		GObjNeutral tmp1 = null;
		GObjSession tmpSes = null;
		long beTo = 0;
		long runTo = 0;
		int way = 0;
		for (int i = 0; i < arrs.length; i++) {
			tmp1 = arrs[i];
			beTo = tmp1.getBelongTo();
			runTo = tmp1.getRunTo();
			way = tmp1.getRunway();
			if (tmp1.isEnd()) {
				tmp1.disappear(true);
			} else if (tmp1.isCanRelive()) {
				tmp1.ready();
			} else if (tmp1.isReadyRunning()) {
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
				_neutral(tmpSes, tmp1, way, beTo);
				if (runTo <= 0) {
					tmpSes = ses1;
					beTo = ses2.getId();
					_neutral(tmpSes, tmp1, way, beTo);
				}
			}
		}
	}

	private void _neutral(GObjSession srcSes, GObjNeutral neutral, int way, long beTo) {
		GObject gobj = srcSes.getFirst4Way(way);
		if (neutral.isColliding(gobj)) {
			if (neutral.isOverMutiny()) {
				neutral.disappear(true);
				return;
			}
			int power1 = neutral.getGobjType().getPower();
			int power2 = gobj.getGobjType().getPower();
			if (power1 < power2) {
				neutral.doMutiny(srcSes.getId(), beTo, power2);
			} else if (power1 > power2) {
				gobj.runBack();
			}
		}
	}

	void handlerColliding(GObjSession ses1, GObjSession ses2) {
		List<GObject> list1 = null, list2 = null;
		int lens1 = 0, lens2 = 0;
		for (int i = 1; i <= GObjConfig.NMax_Runway; i++) {
			list1 = ses1.getList4Way(i);
			list2 = ses2.getList4Way(i);
			lens1 = list1.size();
			lens2 = list2.size();
			for (int j = 0; j < lens1; j++) {
				_colliding(list1.get(j), lens2 > j ? list2.get(j) : null);
			}
		}
	}

	private void _colliding(GObject gobj1, GObject gobj2) {
		if (gobj1 != null && gobj2 != null) {
			int powerState = 0;
			if (gobj1.isColliding(gobj2)) {
				powerState = gobj1.comPower(gobj2);
				switch (powerState) {
				case 1:
					gobj2.runBack();
					break;
				case -1:
					gobj1.runBack();
					break;
				default:
					break;
				}
			}
		}
	}

	boolean handlerEnd(GObjSession ses1, GObjSession ses2) {
		listEnd.clear();
		int lens = 0;
		boolean isEnd = false;
		GObject tmp1 = null;
		GObjSession tmp = null;
		listEnd.addAll(ses1.jugdeArrive());
		listEnd.addAll(ses2.jugdeArrive());
		Collections.sort(listEnd, comObj);
		lens = listEnd.size();
		for (int i = 0; i < lens; i++) {
			tmp1 = listEnd.get(i);
			if (tmp1.isToEnemy()) {
				tmp = tmp1.getBelongTo() == ses1.getId() ? ses2 : ses1;
				tmp.reduceForage(tmp1.getGobjType().getPower());
				if (tmp.isFails()) {
					isEnd = true;
					winSesid = tmp1.getBelongTo();
					break;
				} else {
					tmp.addNotify(ETNotify.ForageSelf);
					tmp = (tmp == ses1) ? ses2 : ses1;
					tmp.addNotify(ETNotify.ForageEnemy);
				}
			}
			tmp = tmp1.getBelongTo() == ses1.getId() ? ses1 : ses2;
			tmp.onArrive(tmp1);
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
		boolean isHasRobot = ses1.isRobot() || ses2.isRobot();
		objSF = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_RoomWaitEnd);
		notifyCount = isHasRobot ? 1 : 2;
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
		if (isMatchRobot) {
			ses1 = ses1 == null ? robot : ses1;
			ses2 = ses2 == null ? robot : ses2;
		}

		if (ses1 == ses2) {
			this.state = ETState.End;
			return;
		}

		long _now = now();
		if (ses1 == null || ses2 == null) {
			this.state = ETState.End;
			ses1 = ses1 == null ? ses2 : ses1;
			long min = ms_start + GObjConfig.NMin_RoomTime;
			if (min <= _now) {
				ses1.setState(ETState.Win);
			}
			ses1.addNotify(ETNotify.FightEnd);
			return;
		}

		if (ms_over < _now) {
			if (ses1.getForage() >= ses2.getForage()) {
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
		// 菠菜罐头
		handlerSpinach(ses1, ses2);
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
						isMatchRobot = true;
						robot.reRobot(roomid);
						changeOne(robot.getId(), true);
						starting(robot, true);
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

	/** 检测有效性 */
	public void checkRoom() {
		synchronized (this) {
			GObjSession ses = LgcGame.targetSession(sesid1);
			if (ses != null && !ses.IsValid()) {
				LgcGame.remove4Room(ses);
			}
			ses = LgcGame.targetSession(sesid2);
			if (ses != null && !ses.IsValid()) {
				LgcGame.remove4Room(ses);
			}
		}
	}
}
