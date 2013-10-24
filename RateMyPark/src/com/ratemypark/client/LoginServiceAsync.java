package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface LoginServiceAsync {
	
	void verifyLogin(String username, String password, AsyncCallback<Boolean> callback)
		throws IllegalArgumentException;

}
