package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;
import javax.servlet.http.HttpSession;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface LoginServiceAsync {
	
	void doLogin(String username, String password, AsyncCallback<LoginInfo> callback)
		throws IllegalArgumentException;

	void doLogin(String session, AsyncCallback<LoginInfo> callback)
			throws IllegalArgumentException;
}
