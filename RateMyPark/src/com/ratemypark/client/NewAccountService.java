package com.ratemypark.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("new-account")
public interface NewAccountService extends RemoteService {
  LoginInfo createNewAccount(String username, String password) throws UserNameException, IllegalArgumentException, BadPasswordException;
}
