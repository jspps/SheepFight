package com.sf.entity;

import java.util.ArrayList;
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
	static ScheduledExecutorService _ses = Toolkit.newScheduledThreadPool("GObjRoom", 2);
	private static final long serialVersionUID = 1L;
	private long roomid = 0;
	private long psid1 = 0;
	private long psid2 = 0;
	private ETState state = ETState.None;
	
	//随机动物
	private List<GObject> lstRnd = new ArrayList<GObject>();

	private ScheduledFuture<?> sfMatching = null;

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

	public void matching(GObjSession ses) {
		synchronized (this) {
			long id1 = ses.getId();
			long id2 = getOther(id1);
			GObjSession sesOther = LgcGame.targetSession(id2);
			if (sesOther != null) {
				if (sfMatching != null) {
					sfMatching.cancel(true);
					sfMatching = null;
				}
				starting(ses,false);
			} else {
				ses.ready(0);
				state = ETState.Matching;
				sfMatching = Toolkit.scheduleMS(_ses, this, GObjConfig.LMS_Matching);
			}
		}
	}

	public void starting(GObjSession ses,boolean isNdSelf) {
		synchronized (this) {
			state = ETState.Running;
			long id1 = ses.getId();
			long id2 = getOther(id1);
			GObjSession sesOther = LgcGame.targetSession(id2);
			ses.start(id2);
			sesOther.start(id1);
			if(!sesOther.isRobot()){
				sesOther.addNotify(ETNotify.Enemy_Matched);
			}
			if(isNdSelf && !ses.isRobot()){
				ses.addNotify(ETNotify.Enemy_Matched);
			}
		}
	}
	
	public void remove(long sesid){
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
			case Matching:
				sfMatching = null;
				if (!isEmpty() && isFree()) {
					long sid = psid1 > 0 ? psid1 : psid2;
					GObjSession objSes = LgcGame.targetSession(sid);
					if(objSes.isRobot()){
						LgcGame.remove4Room(objSes);
						return;
					}
					
					// 添加一个机器人
					String strRobot = String.format("robot_%s", RndEx.nextString09(9));
					GObjSession robot = new GObjSession(strRobot, strRobot);
					robot.ReLmtOver(0);
					changeOne(robot.getId(), true);
					starting(objSes,true);
				}
				break;
			default:
				break;
			}
		}
	}
}
