package com.ratemypark.server;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.apache.commons.lang.UnhandledException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginService;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;

public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	@Override
	public Boolean verifyLogin(String username, String password) throws IllegalArgumentException, UserNameException, BadPasswordException {

		// Check if username exists in database, and return
		String userlower = username.toLowerCase();	
		Account acc = getAccount(userlower);
		
		// Need to get password hash of the user and check with this
		String hash = acc.getPasswordHash();
		System.out.println(hash);
		checkPassword(password, hash);
		return true;
	}
	
	private PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}
	
	private Account getAccount(String accountName) throws UserNameException{
		PersistenceManager pm = getPersistenceManager();
		try{
			Query q = pm.newQuery(Account.class);
			q.setFilter("username == userParam");
			q.declareParameters("String userParam");
			List<Account> results = (List<Account>) q.execute(accountName);
			if(results.isEmpty()){
				throw new UserNameException("Username " + accountName + " does not exist");
			}if(results.size()>1){
				// Should never run
				System.out.println("Multiple entities for " + accountName + "exist in database");
			}else{
				// Return the Account entity
				return results.get(0);
			}
		}finally{
			pm.close();
		}
		return null;
	}
	private void checkPassword(String password, String hash) throws BadPasswordException{
		Boolean valid = BCrypt.checkpw(password, hash);
		if(!valid){
			throw new BadPasswordException("Wrong password");
		}
	}
}
