package com.ratemypark.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.NewAccountService;
import com.ratemypark.shared.BCrypt;

public class NewAccountServiceImpl extends RemoteServiceServlet implements
		NewAccountService {

	@Override
	public Boolean createNewAccount(String username, String password) throws IllegalArgumentException {
		String hash = BCrypt.hashpw(password, BCrypt.gensalt());	
		
		// TODO
		// Need to create new user account with username and password
		Boolean valid = BCrypt.checkpw(password, hash);
		return valid;
	}

}
