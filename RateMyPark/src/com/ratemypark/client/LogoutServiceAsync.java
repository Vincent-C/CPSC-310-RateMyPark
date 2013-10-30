package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface LogoutServiceAsync {
	
	void logout(String username, AsyncCallback<String> callback);

}
