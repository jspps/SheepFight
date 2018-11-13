package com.sf.logic;

import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.StrEx;
import com.bowlong.util.MapEx;
import com.bowlong.util.Ref;
import com.sf.entity.GObjConfig;
import com.sf.entity.GObjRoom;
import com.sf.entity.GObjSession;
import com.sf.entity.ETGObj;
import com.sf.entity.GObject;
import com.sf.entity.ETNotify;
import com.sf.entity.ETState;
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

	/** 心跳 **/
	static public String heart(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		if (ses == null) {
			pars.clear();
			pars.put("tip", "帐号已过期");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		} else {
			ses.ResetTimeOverdue();
		}

		Map<String, Object> map = roomHeart(ses, null);
		return msg(GObjConfig.S_Success, map, isEncode);
	}

	/** 登录 **/
	static public String login(Map<String, Object> pars) {
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
			ses = new GObjSession(lgid, lgpwd);
			alloterRoom(ses);
		}

		GObjSession sesOther = enemySession(ses);
		// 自身数据加上敌人
		if (sesOther != null) {
			ses.setEnemySesId(sesOther.getSessionID());
			sesOther.setEnemySesId(ses.getSessionID());

			// 给敌人推送自身数据
			sesOther.addNotify(ETNotify.Enemy_Login);
		}
		return msg(GObjConfig.S_Success, ses.toMap(), isEncode);
	}

	static public String Start(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		GObjSession sesOther = enemySession(ses);
		pars.clear();
		if (sesOther == null || !sesOther.IsValid()) {
			pars.put("tip", "尚未对手，请等待");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}
		
		GObjRoom room = getRoom(ses.getRoomid());
		if(room == null){
			room = alloterRoom(ses);
		}
		
		if(sesOther.isReady()){
			sesOther.readyOrStart(true);
			ses.readyOrStart(true);
			room.setState(ETState.Running);
		}else{
			ses.readyOrStart(false);
		}
		
		// 自身数据
		String outVal = msg(GObjConfig.S_Success, ses.toMapMust(null), isEncode);
		// 推送给别的数据
		sesOther.addNotify(ETNotify.Enemy_State);
		return outVal;
	}

	/** 放羊 **/
	static public String downSheep(Map<String, Object> pars) {
		boolean isEncode = isEncode(pars);
		GObjSession ses = mySession(pars);
		int numRunway = MapEx.getInt(pars, "numRunway");
		if (numRunway < 0 || numRunway > GObjConfig.NM_Runway) {
			pars.clear();
			pars.put("tip", "超出了跑道[1-5]");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		int sheepIndex = MapEx.getInt(pars, "sheepIndex");
		ETGObj sType = ETGObj.get(sheepIndex);
		if (sType == null || sheepIndex > 3) {
			pars.clear();
			pars.put("tip", "类型不对(SheepSmall,SheepMiddle,SheepBig)");
			return msg(GObjConfig.S_Fails, pars, isEncode);
		}

		GObjSession sesOther = enemySession(ses);
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

		GObject gobj = new GObject(sType, numRunway, ses.getSessionID());
		gobj.StartRunning(sesOther.getSessionID());

		currPlay.getlGobjRunning().add(gobj);

		// 自身数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sesid", ses.getSessionID());
		map.put("listRunning", currPlay.listMap());
		String outVal = msg(GObjConfig.S_Success, map, isEncode);

		// 推送给别的数据
		sesOther.addNotify(ETNotify.Enemy_DownSheep);
		return outVal;
	}
}
