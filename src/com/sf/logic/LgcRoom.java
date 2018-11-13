package com.sf.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bowlong.reflect.JsonHelper;
import com.bowlong.security.Base64;
import com.bowlong.util.MapEx;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjRoom;
import com.sf.entity.GObjSession;
import com.sf.entity.ETNotify;
import com.sf.ses.MgrSession;

/**
 * 管理 Session 会话对象
 * 
 * @author Canyon
 */
class LgcRoom extends MgrSession {
	private static final long serialVersionUID = 1L;
	static public boolean isMustEncode = false;
	static private List<Long> list = new ArrayList<Long>();
	static Map<Long, GObjRoom> mapRoom = new ConcurrentHashMap<Long, GObjRoom>();

	static final public GObjRoom getRoom(long roomid) {
		if (mapRoom.containsKey(roomid)) {
			return mapRoom.get(roomid);
		}
		return null;
	}
	
	static final protected GObjSession mySession(Map<String, ?> pars) {
		long sessionId = MapEx.getLong(pars, GObjConfig.K_SesID);
		return (GObjSession) getSession(sessionId);
	}
	
	static final protected GObjSession otherSession(GObjSession ses) {
		GObjRoom room = getRoom(ses.getRoomid());
		long sesid = room.getOther(ses.getSessionID());
		return (GObjSession) getSession(sesid);
	}
	
	static final protected GObjSession otherSession(Map<String, ?> pars) {
		return otherSession(mySession(pars));
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

	static final protected String msg(String state, Map<String, ?> srcMap,
			boolean encode) {
		String msg = toJson(srcMap, encode);
		if (encode) {
			return String.format(GObjConfig.Fmt_JsonMsgStr, state, msg);
		}
		return String.format(GObjConfig.Fmt_JsonMsg, state, msg);
	}
	
	static final public String msg(String state, Map<String, ?> srcMap){
		boolean encode = isEncode(srcMap);
		return msg(state, srcMap, encode);
	}

	static final protected boolean isEncode(Map<String, ?> pars){
		boolean isEncode = MapEx.getBoolean(pars, "isEncode");
		return isMustEncode || isEncode;
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
		mapLg2Key.put(vKey, ses.getSessionID());
		mapSession.put(ses.getSessionID(), ses);
		chgRoom(ses.getRoomid(), ses.getSessionID(), true);
	}
	
	static final public void Start(Map<String, Object> pars){
		
	}

	static final public Map<String, Object> heart(GObjSession ses,
			Map<String, Object> pars) {
		pars = ses.toMapMust(pars);

		List<ETNotify> _list = new ArrayList<ETNotify>();
		_list.addAll(ses.getListNotify());
		int lens = _list.size();
		if (lens > 0) {
			ETNotify notifyType = ETNotify.None;
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
