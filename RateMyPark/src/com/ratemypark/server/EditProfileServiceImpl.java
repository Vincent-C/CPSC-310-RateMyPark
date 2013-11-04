package com.ratemypark.server;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.EditProfileService;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;

public class EditProfileServiceImpl extends RemoteServiceServlet implements EditProfileService {
	
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	@Override
	public LoginInfo editProfile(LoginInfo newProfile) throws NotLoggedInException {
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession existingSession = request.getSession();
		Account oldProfile = (Account) existingSession.getAttribute("account");
		
		System.out.println("inside edit profile service");
		
		if (newProfile.getSessionID().equals(existingSession.getId())) {
			System.out.println("sessions equal");
		} else {
			throw new NotLoggedInException();
		}
		
		return newProfile;
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

}
