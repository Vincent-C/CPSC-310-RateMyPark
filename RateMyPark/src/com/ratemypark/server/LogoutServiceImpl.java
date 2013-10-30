package com.ratemypark.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LogoutService;

public class LogoutServiceImpl extends RemoteServiceServlet implements
		LogoutService {

	@Override
	public String logout(String s) {
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();

		// Clear the saved account from the session
		session.setAttribute("account", null); // This is probably not needed
		session.invalidate();
		
		System.out.println("Logout session is: " + session);
		
		return null;
	}

}