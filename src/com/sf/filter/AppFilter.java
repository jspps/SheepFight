package com.sf.filter;

import java.util.Map;

import javax.servlet.FilterConfig;

import com.bowlong.third.jsp.BasicFilter;
import com.bowlong.util.CalendarEx;
import com.bowlong.util.MapEx;

public class AppFilter extends BasicFilter {
	@Override
	public void onInit(FilterConfig arg0) {
		ms_bef = CalendarEx.TIME_SECOND * 20;
		ms_aft = CalendarEx.TIME_SECOND * 20;
		isPrint = true;
	}

	@Override
	public boolean isFilter(String uri, Map<String, String> pars) {
		// 要排除的(服务器系统时间,支付回調等)
		if(uri.contains("/Svlet/Game")){
			int cmd = MapEx.getInt(pars, "cmd");
			return (cmd > 1000) && (cmd != 1000);
		}
		return false;
	}
}
