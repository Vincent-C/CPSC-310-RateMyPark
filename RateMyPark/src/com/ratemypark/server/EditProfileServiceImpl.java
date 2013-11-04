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
import com.ratemypark.client.EditProfileService;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;

public class EditProfileServiceImpl extends RemoteServiceServlet implements EditProfileService {
	
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	@Override
	public LoginInfo editProfile(LoginInfo newProfile) throws NotLoggedInException, UserNameException {
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession existingSession = request.getSession();
		//Account oldProfile = (Account) existingSession.getAttribute("account");
		
		System.out.println("inside edit profile service");
		
		if (newProfile.getSessionID().equals(existingSession.getId())) {
			System.out.println("sessions equal");
			Account newAccount = editAccount(newProfile);
			existingSession.setAttribute("account", newAccount);
		} else {
			throw new NotLoggedInException();
		}
		
		return newProfile;
	}
	
	private PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}
	
	private Account editAccount(LoginInfo newProfile) throws UserNameException {
		PersistenceManager pm = getPersistenceManager();
		try{
				Account acc = pm.getObjectById(Account.class, newProfile.getUsername());
				acc.setFirstName(newProfile.getFirstName());
				acc.setLastName(newProfile.getLastName());
				// Return the Account entity
				pm.makePersistent(acc);
				return acc;
		}catch (JDOObjectNotFoundException e){
			throw new UserNameException("Account for " + newProfile.getUsername() + " not found");
		}
		finally {
			pm.close();
		}
	}
}
