package com.ratemypark.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;

@RemoteServiceRelativePath("logout")
public interface LogoutService extends RemoteService {
	 String logout(String password);

}
