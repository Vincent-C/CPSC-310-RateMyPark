package com.ratemypark.server;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.client.NewAccountService;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;

public class NewAccountServiceImpl extends RemoteServiceServlet implements NewAccountService {

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	@Override
	public LoginInfo createNewAccount(String username, String password) throws UserNameException,
			IllegalArgumentException {
		String userlower = username.toLowerCase();
		checkNameExists(userlower);
		PersistenceManager pm = getPersistenceManager();		
		String hash = BCrypt.hashpw(password, BCrypt.gensalt());
		
		Account acc = new Account(userlower,hash);
		try {
			pm.makePersistent(acc);			
		} finally{
			pm.close();
		}
		
		HttpServletRequest request = this.getThreadLocalRequest();
		String sessionID = "";
		
		if (request != null) {
			HttpSession session = request.getSession();
			session.setAttribute("account", acc);
			System.out.println("New account session is: " + session);
			sessionID = session.getId();
		}
		LoginInfo ret = new LoginInfo(acc.getUsername(), sessionID);
		
		return ret;
	}
	

	private void checkNameExists(String checkuser) throws UserNameException {
		PersistenceManager pm = getPersistenceManager();
		try {
			Account acc = pm.getObjectById(Account.class, checkuser);
			// Refresh the object, to make sure its up to date
			pm.refresh(acc);
		} catch (JDOObjectNotFoundException e) {
			throw new UserNameException("Username " + checkuser + " already exists");
		} finally {
			pm.close();
		}
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
