package com.ratemypark.test.server;

import static org.junit.Assert.*;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.server.Account;
import com.ratemypark.server.LoginServiceImpl;
import com.ratemypark.server.NewAccountServiceImpl;
import com.ratemypark.shared.BCrypt;

public class LoginServiceImplTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private LoginServiceImpl service;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		service = new LoginServiceImpl();
		// Add an account that we can try to login to
		PersistenceManager pm = getPersistenceManager();
		String password = "password1";
		String hash = BCrypt.hashpw(password, BCrypt.gensalt(4));
		Account acc = new Account("dendi", hash);
		try {
			pm.makePersistent(acc);
		} finally {
			pm.close();
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testLoginByUsernameAndPassword() {
		String testUsername = "dendi";
		String testPassword = "password1";
		
		try {
			LoginInfo loginResults = service.doLogin(testUsername, testPassword);
		} catch (IllegalArgumentException e) {
			fail("Illegal Argument sent to doLogin");
			e.printStackTrace();
		} catch (UserNameException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (BadPasswordException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
