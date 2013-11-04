package com.ratemypark.server;

import java.util.List;

import javax.jdo.JDOHelper;
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

public class NewAccountServiceImpl extends RemoteServiceServlet implements
		NewAccountService {
	
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	@Override
	public LoginInfo createNewAccount(String username, String password) throws UserNameException,IllegalArgumentException {
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
		HttpSession session = request.getSession();
		session.setAttribute("account", acc);
		
		System.out.println("New account session is: " + session);
		
		LoginInfo ret = new LoginInfo(acc.getUsername(), session.getId());
		
		return ret;
	}
	
	private void checkNameExists(String checkuser) throws UserNameException {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Account.class);
			q.setFilter("username == userParam");
			q.declareParameters("String userParam");
			List<Account> results = (List<Account>) q.execute(checkuser);
			if (!results.isEmpty()) {
				throw new UserNameException("Username " + checkuser + " already exists");
			}
		} finally {
			pm.close();
		}
		
	}

	private PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}

}
