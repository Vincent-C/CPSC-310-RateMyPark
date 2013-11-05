package com.ratemypark.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("edit-profile")
public interface EditProfileService extends RemoteService {
  LoginInfo editProfile(LoginInfo newProfile) throws NotLoggedInException, UserNameException;
  
}