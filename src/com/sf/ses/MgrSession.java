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

	static final public Object objLock = new Object();

	static final protected Map<Long, Session> mapSession = new ConcurrentHashMap<Long, Session>();
	static final protected Map<String, Long> mapLg2Key = new ConcurrentHashMap<String, Long>();

	static final public Session getSession(long key) {
		if (mapSession.containsKey(key)) {
			return mapSession.get(key);
		}
		return null;
	}

	static final public Session getSession(String lgid, String lgpwd) {
		String vKey = String.format("%s_%s", lgid, lgpwd);
		long sKey = 0;
		if (mapLg2Key.containsKey(vKey)) {
			sKey = mapLg2Key.get(vKey);
		}
		return getSession(sKey);
	}

	static final public Session rmSession(String lgid, String lgpwd) {
		String vKey = String.format("%s_%s", lgid, lgpwd);
		long sKey = mapLg2Key.get(vKey);
		Session _ses = getSession(sKey);
		if (_ses != null) {
			mapLg2Key.remove(vKey);
			mapSession.remove(sKey);
		}
		return _ses;
	}
}
