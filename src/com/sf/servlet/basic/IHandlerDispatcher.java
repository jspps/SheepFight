package com.sf.servlet.basic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IHandlerDispatcher {
	String HandlerDispatcher(HttpServletRequest req, HttpServletResponse resp);
}
