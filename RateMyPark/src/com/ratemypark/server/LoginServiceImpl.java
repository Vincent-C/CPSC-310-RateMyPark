package com.ratemypark.server;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.UnhandledException;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.client.LoginService;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;

public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	@Override
	public LoginInfo doLogin(String username, String password) throws IllegalArgumentException, UserNameException, BadPasswordException {

		// Check if username exists in database, and return
		String userlower = username.toLowerCase();	
		Account acc = getAccount(userlower);
		
		// Need to get password hash of the user and check with this
		String hash = acc.getPasswordHash();
		checkPassword(password, hash);

		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();
		session.setAttribute("account", acc);
		
		System.out.println("Login session is: " + session);
		
		LoginInfo ret = new LoginInfo(acc.getUsername(), session.getId());
		
		return ret;
	}
	
	@Override
	public LoginInfo doLogin(String session) throws NotLoggedInException {

		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession existingSession = request.getSession();
		
		Account gettedAccount;
		if (existingSession.getId().equals(session)) {
			gettedAccount = (Account) existingSession.getAttribute("account");
			System.out.println("Already logged in to: " + gettedAccount.getUsername());

		} else {
			throw new NotLoggedInException();
		}
		
		LoginInfo ret = new LoginInfo(gettedAccount.getUsername(), existingSession.getId());

		return ret;
	}
	
	
	private PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}
	
	private Account getAccount(String accountName) throws UserNameException {
		PersistenceManager pm = getPersistenceManager();
		try{
			Query q = pm.newQuery(Account.class);
			q.setFilter("username == userParam");
			q.declareParameters("String userParam");
			List<Account> results = (List<Account>) q.execute(accountName);
			if (results.isEmpty()) {
				throw new UserNameException("Username " + accountName + " does not exist");
			} if (results.size() > 1) {
				// Should never run
				System.out.println("Multiple entities for " + accountName + "exist in database");
			} else {
				// Return the Account entity
				return results.get(0);
			}
		} finally {
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
