package com.ratemypark.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
  String doLogin(String username, String password) throws UserNameException,BadPasswordException;
  String doLogin(String session) throws NotLoggedInException;
}