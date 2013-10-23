package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface NewAccountServiceAsync {
	
	void createNewAccount(String username, String password, AsyncCallback<Boolean> callback)
		throws IllegalArgumentException;;

}
