package com.sf.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bowlong.util.CalendarEx;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjRoom;
import com.sf.entity.GObjSession;
import com.sf.entity.NotifyType;
import com.sf.ses.MgrSession;

/**
 * 管理 Session 会话对象
 * 
 * @author Canyon
 */
class LgcRoom extends MgrSession {
	private static final long serialVersionUID = 1L;
	static private List<Long> list = new ArrayList<Long>();
	static Map<Long, GObjRoom> mapRoom = new ConcurrentHashMap<Long, GObjRoom>();

	static public GObjRoom getRoom(long roomid) {
		if (mapRoom.containsKey(roomid)) {
			return mapRoom.get(roomid);
		}
		return null;
	}

	static final public GObjSession getOther(GObjSession ses) {
		GObjRoom room = getRoom(ses.getRoomid());
		long sesid = room.getOther(ses.getSessionID());
		return (GObjSession) getSession(sesid);
	}

	static final public long getRoomId() {
		synchronized (objLock) {
			long ret = 0;
			list.clear();
			list.addAll(mapRoom.keySet());
			int lens = list.size();
			GObjRoom room = null;
			if (lens <= 0) {
				ret = GObjConfig.SW_RoomID.nextId();
				room = new GObjRoom(ret);
				mapRoom.put(ret, room);
			} else {
				for (int i = 0; i < lens; i++) {
					room = getRoom(list.get(i));
					if (!room.isFull()) {
						ret = room.getRoomid();
						break;
					}
				}
			}
			return ret;
		}
	}

	static final public boolean chgRoom(long roomid, long sesid, boolean isAdd) {
		synchronized (objLock) {
			if (mapRoom.containsKey(roomid)) {
				GObjRoom room = getRoom(roomid);
				return room.changeOne(sesid, isAdd);
			}
			return false;
		}
	}

	static final public void addSession(GObjSession ses) {
		String vKey = String.format("%s_%s", ses.getLgid(), ses.getLgpwd());
		mapLg2Key.put(vKey, ses.sessionID);
		mapSession.put(ses.sessionID, ses);
		chgRoom(ses.getRoomid(), ses.sessionID, true);
	}

	static final public Map<String, Object> heart(GObjSession ses, Map<String, Object> pars) {
		if (pars == null)
			pars = new HashMap<String, Object>();
		pars.put("time_ms", CalendarEx.now());
		pars.put(GObjConfig.K_SesID, ses.getSessionID());

		List<NotifyType> _list = new ArrayList<NotifyType>();
		_list.addAll(ses.getListNotify());
		int lens = _list.size();
		if (lens > 0) {
			NotifyType notifyType = NotifyType.None;
			for (int i = 0; i < lens; i++) {
				notifyType = _list.get(i);
				ses.rmNotify(notifyType);
				switch (notifyType) {
				case Enemy_Login:
					pars.put("enemy", ses.getEnemy().toMap());
					break;
				case Enemy_DownSheep:
					pars.put("listRunning", ses.getEnemy().listMap());
					break;
				default:
					break;
				}
			}
		}
		return pars;
	}
}
