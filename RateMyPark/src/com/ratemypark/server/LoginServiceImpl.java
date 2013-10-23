package com.ratemypark.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginService;
import com.ratemypark.shared.BCrypt;

public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	@Override
	public Boolean verifyLogin(String username, String password) {

		String hash = BCrypt.hashpw(password, BCrypt.gensalt());	
		
		// TODO
		// Need to get password hash of the user and check with this
		Boolean valid = BCrypt.checkpw(password, hash);
		return valid;
	}

}
