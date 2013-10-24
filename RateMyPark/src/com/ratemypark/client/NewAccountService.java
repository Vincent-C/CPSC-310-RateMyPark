package com.ratemypark.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("new-account")
public interface NewAccountService extends RemoteService {
  void createNewAccount(String username, String password) throws UserNameExistsException, IllegalArgumentException;
}
