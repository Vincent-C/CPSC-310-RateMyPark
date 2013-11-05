package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;
import javax.servlet.http.HttpSession;

/**
 * The async counterpart of <code>EditProfileService</code>.
 */
public interface EditProfileServiceAsync {

	void getCurrentProfile(AsyncCallback<LoginInfo> callback);
	
	void editProfile(LoginInfo newProfile, AsyncCallback<LoginInfo> callback)
		throws IllegalArgumentException;
}
