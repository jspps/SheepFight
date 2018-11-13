package com.sf.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bowlong.third.jsp.AbsDispatcherServlet;
import com.bowlong.tool.TkitJsp;
import com.bowlong.util.DateEx;
import com.bowlong.util.MapEx;
import com.sf.logic.LgcGame;

/**
 * 统一入口
 * 
 * @author Canyon
 * 
 */
public class GameServlet extends AbsDispatcherServlet {
	private static final long serialVersionUID = 1L;
	static final Object objLock = new Object();

	@Override
	public String dispatcher(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, String> pars = TkitJsp.getMapAllParams(req, true);
		int cmd = MapEx.getInt(pars, "cmd");
		String outVal = "";
		switch (cmd) {
		case 100: {
			outVal = sysNowTime(pars);
			break;
		}
		case 101: {
			outVal = gameEncode(pars);
			break;
		}
		default:
			outVal = disp(cmd, pars);
			break;
		}
		return outVal;
	}

	String sysNowTime(Map<String, String> pars) {
		return String.valueOf(DateEx.now());
	}

	String gameEncode(Map<String, String> pars) {
		boolean isEncode = MapEx.getBoolean(pars, "isEncode");
		LgcGame.isMustEncode = isEncode;
		return String.valueOf(isEncode);
	}

	String disp(int cmd, Map<String, String> pars) {
		synchronized (objLock) {
			String outVal = "";
			switch (cmd) {
			case 1000: {
				outVal = LgcGame.login(pars);
				break;
			}
			default:
				if(LgcGame.isVerifySession(pars, LgcGame.objMsgValid)){
					outVal = dispGame(cmd,pars);
				}else{
					outVal = LgcGame.objMsgValid.val;
				}
				break;
			}
			return outVal;
		}
	}

	String dispGame(int cmd, Map<String, String> pars) {
		String outVal = "";
		switch (cmd) {
		case 1001: {
			outVal = LgcGame.heart(pars);
			break;
		}
		case 1002: {
			outVal = LgcGame.Start(pars);
			break;
		}
		case 1003: {
			outVal = LgcGame.downSheep(pars);
			break;
		}
		default:
			break;
		}
		return outVal;
	}
}
