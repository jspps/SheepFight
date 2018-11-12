package com.sf.logic;

import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.StrEx;
import com.bowlong.reflect.JsonHelper;
import com.bowlong.security.Base64;
import com.bowlong.util.MapEx;
import com.bowlong.util.Ref;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjSession;
import com.sf.entity.GObjType;
import com.sf.entity.GObject;
import com.sf.entity.NotifyType;
import com.sf.entity.Player;

/**
 * 游戏逻辑
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午11:41:51
 */
public class LgcGame extends LgcRoom {

	private static final long serialVersionUID = 1L;
	
	static public boolean isMustEncode = false;

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

	static final private GObjSession mySession(Map<String, ?> pars) {
		long sessionId = MapEx.getLong(pars, GObjConfig.K_SesID);
		return (GObjSession) getSession(sessionId);
	}
	
	static final private boolean isEncode(Map<String, ?> pars){
		boolean isEncode = MapEx.getBoolean(pars, "isEncode");
		return isMustEncode || isEncode;
	}
	
	static final public boolean isFilter4NetCount(Map<String, ?> pars,Ref<Integer> refPars){ 
		GObjSession ses = mySession(pars);
		if(ses != null){
			ses.recordNetCount();
			if(refPars != null){
				refPars.val = ses.getNetCount();
			}
			return ses.isNetMore();
		}
		return false;
	}

	/** 心跳 **/
	static public String heart(Map<String, String> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		if (ses == null) {
			pars.clear();
			pars.put("tip", "帐号已过期");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		} else {
			ses.ResetTimeOverdue();
		}

		Map<String, Object> map = heart(ses, null);
		return msg(GObjConfig.S_Success, map, isEncode);
	}

	/** 登录 **/
	static public String login(Map<String, String> pars) {
		boolean isEncode = isEncode(pars);
		String lgid = MapEx.getString(pars, "lgid");
		String lgpwd = MapEx.getString(pars, "lgpwd");
		if (StrEx.isEmpty(lgid)) {
			pars.clear();
			pars.put("tip", "帐号为空了");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		if (StrEx.isEmpty(lgpwd)) {
			pars.clear();
			pars.put("tip", "密码为空了");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		GObjSession ses = (GObjSession) getSession(lgid, lgpwd);
		if (ses == null) {
			long roomid = getRoomId();
			ses = new GObjSession(lgid, lgpwd, roomid);
			addSession(ses);
		}

		GObjSession sesOther = getOther(ses);
		// 自身数据加上敌人
		if (sesOther != null) {
			ses.setEnemy(sesOther.getCurr());
			sesOther.setEnemy(ses.getCurr());

			// 给敌人推送自身数据
			sesOther.addNotify(NotifyType.Enemy_Login);
		}
		return msg(GObjConfig.S_Success, ses.toMap(), isEncode);
	}

	/** 放羊 **/
	static public String downSheep(Map<String, String> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		if (ses == null) {
			pars.clear();
			pars.put("tip", "帐号已过期");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		int numRunway = MapEx.getInt(pars, "numRunway");
		if (numRunway < 0 || numRunway > GObjConfig.NM_Runway) {
			pars.clear();
			pars.put("tip", "超出了跑道[1-5]");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		int sheepIndex = MapEx.getInt(pars, "sheepIndex");
		GObjType sType = GObjType.get(sheepIndex);
		if (sType == null || sheepIndex > 3) {
			pars.clear();
			pars.put("tip", "类型不对(SheepSmall,SheepMiddle,SheepBig)");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		GObjSession sesOther = getOther(ses);
		if (sesOther == null || !sesOther.IsValid()) {
			pars.clear();
			pars.put("tip", "对手已经掉线");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		Player currPlay = ses.getCurr();
		int needForce = sType.getPower();
		int currForce = currPlay.getForage();
		if (currForce < needForce) {
			pars.clear();
			pars.put("tip", "草料不足，不能放" + sType.getName());
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}
		currForce -= needForce;
		currPlay.setForage(currForce);

		GObject gobj = new GObject(sType, numRunway,ses.getSessionID());
		gobj.StartRunning(sesOther.getSessionID());

		currPlay.getlGobjRunning().add(gobj);

		// 自身数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sesid", ses.getSessionID());
		map.put("listRunning", currPlay.listMap());
		String outVal = msg(GObjConfig.S_Success, map, isEncode);

		// 推送给别的数据
		sesOther.addNotify(NotifyType.Enemy_DownSheep);
		return outVal;
	}
}
