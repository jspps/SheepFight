package com.sf.logic;

import java.util.Map;

import com.bowlong.lang.StrEx;
import com.bowlong.util.MapEx;
import com.bowlong.util.Ref;
import com.sf.entity.ETGObj;
import com.sf.entity.ETNotify;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjRoom;
import com.sf.entity.GObjSession;
import com.sf.entity.GObject;
import com.sf.entity.Player;

/**
 * 游戏逻辑
 * 
 * @author Canyon
 * @version createtime：2018-11-11上午11:41:51
 */
public class LgcGame extends LgcRoom {

	private static final long serialVersionUID = 1L;

	static final public boolean isFilter4NetCount(Map<String, ?> pars, Ref<Integer> refPars) {
		GObjSession ses = mySession(pars);
		if (ses != null) {
			ses.recordNetCount();
			if (refPars != null) {
				refPars.val = ses.getNetCount();
			}
			return ses.isNetMore();
		}
		return false;
	}

	static final Map<String, Object> matching(GObjSession ses, Map<String, Object> pars) {
		pars.clear();
		GObjRoom room = alloterRoom(ses);
		room.matching(ses);
		return ses.toMap(pars);
	}

	/** 心跳 **/
	static public String heart(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		pars.clear();
		if (ses == null) {
			pars.put("tip", "帐号已过期");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		} else {
			ses.ResetTimeOverdue();
		}
		pars = roomHeart(ses, pars);
		return msg(GObjConfig.S_Success, pars, isEncode);
	}

	/** 登录 **/
	static public String login(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		String lgid = MapEx.getString(pars, "lgid");
		String lgpwd = MapEx.getString(pars, "lgpwd");
		pars.clear();
		if (StrEx.isEmpty(lgid)) {
			pars.put("tip", "帐号为空了");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		if (StrEx.isEmpty(lgpwd)) {
			pars.put("tip", "密码为空了");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		GObjSession ses = (GObjSession) getSession(lgid, lgpwd);
		if (ses == null) {
			ses = new GObjSession(lgid, lgpwd);
		}
		
		pars = matching(ses, pars);
		return msg(GObjConfig.S_Success, pars, isEncode);
	}

	/** 放羊 **/
	static public String downSheep(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		int numRunway = MapEx.getInt(pars, "numRunway");
		int sheepIndex = MapEx.getInt(pars, "sheepIndex");
		pars.clear();
		if (numRunway < 0 || numRunway > GObjConfig.NM_Runway) {
			pars.put("tip", "超出了跑道[1-5]");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		ETGObj sType = ETGObj.get(sheepIndex);
		if (sType == null || sheepIndex > 3) {
			pars.put("tip", "类型不对(SheepSmall,SheepMiddle,SheepBig)");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		GObjSession sesOther = enemySession(ses);
		if (sesOther == null || !sesOther.IsValid()) {
			pars.put("tip", "对手已经掉线");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		Player currPlay = ses.getCurr();
		int needForce = sType.getPower();
		int currForce = currPlay.getForage();
		if (currForce < needForce) {
			pars.put("tip", "草料不足，不能放" + sType.getName());
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}
		currForce -= needForce;
		currPlay.setForage(currForce);

		GObject gobj = new GObject(sType, numRunway, ses.getId());
		gobj.StartRunning(sesOther.getId());

		currPlay.getlGobjRunning().add(gobj);

		// 自身数据
		pars.put("sesid", ses.getId());
		pars.put("listRunning", currPlay.listMap());
		String outVal = msg(GObjConfig.S_Success, pars, isEncode);

		// 推送给别的数据
		sesOther.addNotify(ETNotify.Enemy_DownSheep);
		return outVal;
	}
}
