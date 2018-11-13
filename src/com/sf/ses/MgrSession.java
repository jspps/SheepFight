package com.sf.ses;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 Session 会话对象
 * 
 * @author Canyon
 */
public class MgrSession implements Serializable {
	private static final long serialVersionUID = 1L;

	static final Map<Long, Session> mapSession = new ConcurrentHashMap<Long, Session>();
	static final Map<String, Long> mapLg2Id = new ConcurrentHashMap<String, Long>();
	static final Map<Long, String> mapId2Lg = new ConcurrentHashMap<Long, String>();

	static final public Session getSession(long sesid) {
		if (mapSession.containsKey(sesid)) {
			return mapSession.get(sesid);
		}
		return null;
	}

	static final public Session getSession(String lgid, String lgpwd) {
		String vKey = String.format("%s_%s", lgid, lgpwd);
		long sKey = 0;
		if (mapLg2Id.containsKey(vKey)) {
			sKey = mapLg2Id.get(vKey);
		}
		return getSession(sKey);
	}

	static final public boolean rmSession(long sesid) {
		if (mapId2Lg.containsKey(sesid)) {
			String vKey = mapId2Lg.get(sesid);
			mapLg2Id.remove(vKey);
			mapId2Lg.remove(sesid);
			mapSession.remove(sesid);
			return true;
		}
		return false;
	}

	static final public boolean rmSession(Session objSes) {
		if (objSes == null)
			return false;
		return rmSession(objSes.getSessionID());
	}

	static final public void addSession(String lgid, String lgpwd, Session objSes) {
		String vKey = String.format("%s_%s", lgid, lgpwd);
		long sesid = objSes.getSessionID();
		mapLg2Id.put(vKey, sesid);
		mapId2Lg.put(sesid, vKey);
		mapSession.put(sesid, objSes);
	}
}
