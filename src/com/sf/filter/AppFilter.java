package com.sf.filter;

import java.util.Map;

import javax.servlet.FilterConfig;

import com.bowlong.third.jsp.BasicFilter;
import com.bowlong.util.CalendarEx;
import com.bowlong.util.MapEx;
import com.bowlong.util.Ref;
import com.sf.entity.GObjConfig;
import com.sf.logic.LgcGame;

public class AppFilter extends BasicFilter {
	
	int netCount = 0;
	Ref<Integer> refObj = new Ref<Integer>(0);
	
	@Override
	public void onInit(FilterConfig arg0) {
		ms_bef = CalendarEx.TIME_SECOND * 20;
		ms_aft = CalendarEx.TIME_SECOND * 20;
		isPrint = true;
		isCFDef = false;
	}

	@Override
	public boolean isFilter(String uri, Map<String, String> pars) {
		boolean isFitler = false;
		netCount = 0;
		isVTime = false;
		// 要排除的(服务器系统时间,支付回調等)
		if (uri.contains("/Svlet/Game")) {
			int cmd = MapEx.getInt(pars, "cmd");
			isFitler = (cmd > 1000) && (cmd != 1000);
			isFitler = isFitler && isFilterTime(pars, key_time);
		}
		isFitler = isFitler || LgcGame.isFilter4NetCount(pars,refObj);
		if(isFitler){
			netCount = refObj.val;
			refObj.val = 0;
		}
		return isFitler;
	}

	@Override
	public String cfFilter(int state,String uri, Map<String, String> pars) {
		pars.put("uri", uri);
		if(netCount > 0)
			pars.put("tip", String.format("每%s秒超过了%s条请求,当前已请求%s条!",(GObjConfig.LS_Net / 1000),GObjConfig.LN_Net,netCount));
		else
			pars.put("tip", (state == 1)?"消息过时了!":"消息带有有sql注入");
		return LgcGame.msg(GObjConfig.S_Fails, pars);
	}
}
