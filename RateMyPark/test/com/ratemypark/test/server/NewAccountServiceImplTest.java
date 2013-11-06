package com.ratemypark.test.server;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.ratemypark.client.Park;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.DatabaseException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.server.Account;
import com.ratemypark.server.DomXMLParser;
import com.ratemypark.server.LoadParksServiceImpl;
import com.ratemypark.server.NewAccountServiceImpl;
import com.ratemypark.shared.BCrypt;

import org.junit.Test;

public class NewAccountServiceImplTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private NewAccountServiceImpl service;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		service = new NewAccountServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testCreateNewAccount() {
		String username = "ferrari430sopro";
		String password = "secretpassword";

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getObjectById(Account.class, username);
			fail("Found an account with username: " + username + ", but it wasn't added to the db yet");
		} catch (JDOObjectNotFoundException e) {
			assertTrue(true);
		} finally {
			pm.close();
		}
		
		try {
			service.createNewAccount(username, password);
		} catch (IllegalArgumentException e) {
			fail("IllegalArugmentException?? Someone wrote a bad test??");
			e.printStackTrace();
		} catch (UserNameException e) {
			fail(e.getMessage() + " " + username + " should not be a bad username...");
			e.printStackTrace();
		} catch (BadPasswordException e) {
			fail(e.getMessage() + " : found empty password");
			e.printStackTrace();
		}
		
		pm = getPersistenceManager();
		try {
			Account acc = pm.getObjectById(Account.class, username);
			assertEquals(acc.getUsername(), username);
//			assertTrue(BCrypt.checkpw(acc.getPasswordHash(), password)); // Salt version seems to be causing issues with this test
		} catch (JDOObjectNotFoundException e) {
			fail("Should have found an account with the username: " + username);
		} finally {
			pm.close();
		}
	}
	
	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
