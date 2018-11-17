package com.sf.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bowlong.reflect.JsonHelper;
import com.bowlong.security.Base64;
import com.bowlong.util.MapEx;
import com.bowlong.util.Ref;
import com.sf.entity.ETNotify;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjRoom;
import com.sf.entity.GObjSession;
import com.sf.ses.MgrSession;

/**
 * 管理 Session 会话对象
 * 
 * @author Canyon
 */
class LgcRoom extends MgrSession {
	private static final long serialVersionUID = 1L;
	static final Map<Long, GObjRoom> mapRoom = new ConcurrentHashMap<Long, GObjRoom>();
	static final List<Long> list = new ArrayList<Long>();
	static final public Ref<String> objMsgValid = new Ref<String>("");
	static public boolean isMustEncode = false;

	static final public GObjRoom getRoom(long roomid) {
		if (mapRoom.containsKey(roomid)) {
			return mapRoom.get(roomid);
		}
		return null;
	}

	static final public GObjSession targetSession(long sesid) {
		return (GObjSession) getSession(sesid);
	}

	static final GObjSession mySession(Map<String, ?> pars) {
		long sessionId = MapEx.getLong(pars, GObjConfig.K_SesID);
		return targetSession(sessionId);
	}

	static final GObjSession enemySession(GObjSession ses) {
		GObjRoom room = null;
		if (ses != null)
			room = getRoom(ses.getRoomid());

		if (room != null) {
			long sesid = room.getOther(ses.getId());
			GObjSession enemy = targetSession(sesid);
			if (enemy == null && room.isMatchRobot()) {
				return room.getRobot();
			}
			return enemy;
		}
		return null;
	}

	static final public GObjSession enemySession(long sesid) {
		GObjSession ses = targetSession(sesid);
		return enemySession(ses);
	}

	static final protected String base64(String src, boolean encode) {
		try {
			if (encode) {
				return Base64.encode(src);
			}
			return Base64.decodeToStr(src);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	static final protected String toJson(Map<String, ?> srcMap, boolean encode) {
		String ret = JsonHelper.toJSON(srcMap).toString();
		if (encode) {
			ret = base64(ret, true);
		}
		return ret;
	}

	static final protected String msg(String state) {
		return String.format(GObjConfig.Fmt_JsonState, state);
	}

	static final protected String msg(String state, Map<String, ?> srcMap, boolean encode) {
		String msg = toJson(srcMap, encode);
		if (encode) {
			return String.format(GObjConfig.Fmt_JsonMsgStr, state, msg);
		}
		return String.format(GObjConfig.Fmt_JsonMsg, state, msg);
	}

	static final public String msg(String state, Map<String, ?> srcMap) {
		boolean encode = isEncode(srcMap);
		return msg(state, srcMap, encode);
	}

	static final protected boolean isEncode(Map<String, ?> pars) {
		boolean isEncode = MapEx.getBoolean(pars, "isEncode");
		return isMustEncode || isEncode;
	}

	static final private GObjRoom getFreeRoom() {
		list.clear();
		list.addAll(mapRoom.keySet());
		int lens = list.size();
		GObjRoom _tmp = null;
		GObjRoom _ret = null;
		for (int i = 0; i < lens; i++) {
			_tmp = getRoom(list.get(i));
			_tmp.checkRoom();
			if (_tmp.isFree()) {
				_ret = _tmp;
				break;
			}
		}
		if (_ret == null) {
			long id = GObjConfig.SW_RID.nextId();
			_ret = new GObjRoom(id);
			mapRoom.put(id, _ret);
		}
		return _ret;
	}

	static protected GObjRoom alloterRoom(GObjSession ses) {
		synchronized (mapRoom) {
			long sesid = ses.getId();
			GObjRoom room = getFreeRoom();
			room.changeOne(sesid, true);

			ses.setRoomid(room.getRoomid());
			addSession(ses.getLgid(), ses.getLgpwd(), ses);
			return room;
		}
	}

	static public void remove4Room(GObjSession ses) {
		synchronized (mapRoom) {
			if (rmSession(ses)) {
				long roomid = ses.getRoomid();
				GObjRoom room = getRoom(roomid);
				if (room != null) {
					room.remove(ses.getId());
				}
			}
		}
	}

	static public boolean isVerifySession(Map<String, Object> pars, Ref<String> refMsg) {
		refMsg.val = "";
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		if (ses == null) {
			pars.clear();
			pars.put("tip", "账号未登录");
			refMsg.val = msg(GObjConfig.S_Fails, pars, isEncode);
			return false;
		}
		if (!ses.IsValid()) {
			pars.clear();
			pars.put("tip", "帐号已过期");
			refMsg.val = msg(GObjConfig.S_Fails, pars, isEncode);
			remove4Room(ses);
			return false;
		}
		return true;
	}

	static public Map<String, Object> roomHeart(GObjSession ses, Map<String, Object> pars) {
		GObjRoom room = getRoom(ses.getRoomid());
		GObjSession enemy = enemySession(ses);
		// pars = ses.toMapMust(pars);
		List<ETNotify> _list = new ArrayList<ETNotify>();
		_list.addAll(ses.getListNotify());
		int lens = _list.size();
		if (lens > 0) {
			ETNotify notifyType = ETNotify.None;
			for (int i = 0; i < lens; i++) {
				notifyType = _list.get(i);
				ses.rmNotify(notifyType);
				switch (notifyType) {
				case MatchedEnemy:
					if (enemy != null) {
						pars.put("enemy", enemy.toPlayMap(null));
						pars.put("roomOverTimeMs", room.getOvertime_ms());
					}
					break;
				case Update:
					pars.put("listRunning", ses.toLMRunning());
					break;
				case FightEnd:
					pars.put("isWin", ses.isWin());
					room.notifyEnd();
					break;
				case WaitSelf:
					pars.put("waitListSelf", ses.lmWait());
					break;
				case WaitEnemy:
					if (enemy != null) {
						pars.put("waitListEnemy", enemy.lmWait());
					}
					break;
				default:
					break;
				}
			}
		}
		return pars;
	}
}
