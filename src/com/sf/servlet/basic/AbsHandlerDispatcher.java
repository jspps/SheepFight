package com.sf.servlet.basic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bowlong.tool.TkitJsp;


public abstract class AbsHandlerDispatcher extends HttpServlet implements IHandlerDispatcher {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatcher(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatcher(req, resp);
	}
	
	protected void dispatcher(HttpServletRequest req, HttpServletResponse resp) {
		String outVal = HandlerDispatcher(req, resp);
		TkitJsp.writeAndClose(resp, outVal, "UTF-8");
	}
}
