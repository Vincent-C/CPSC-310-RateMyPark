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
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;

public class NewAccountServiceImpl extends RemoteServiceServlet implements NewAccountService {

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	@Override
	public LoginInfo createNewAccount(String username, String password) throws UserNameException,
			IllegalArgumentException, BadPasswordException {
		String userlower = username.toLowerCase();
		checkValidUsername(userlower);
		checkNameExists(userlower);
		checkValidPassword(password);
		PersistenceManager pm = getPersistenceManager();
		String hash = BCrypt.hashpw(password, BCrypt.gensalt());

		Account acc = new Account(userlower, hash);
		try {
			pm.makePersistent(acc);
		} finally {
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

	private void checkValidPassword(String password) throws BadPasswordException {
		if (password.length() == 0) {
			throw new BadPasswordException("Password cannot be blank");
		}
	}

	private void checkValidUsername(String username) throws UserNameException {
		if (!username.matches("^[a-z0-9]+")) {
			throw new UserNameException("Username can only contain letters and numbers");
		}
	}

	private void checkNameExists(String checkuser) throws UserNameException {
		PersistenceManager pm = getPersistenceManager();
		try {
			// Exception should be thrown here
			pm.getObjectById(Account.class, checkuser);
			// Refresh the object, to make sure its up to date
			throw new UserNameException("Username " + checkuser + " already exists");
		} catch (JDOObjectNotFoundException e) {
			return;
		} finally {
			pm.close();
		}
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
