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
import com.ratemypark.exception.DatabaseException;
import com.ratemypark.server.DomXMLParser;
import com.ratemypark.server.LoadParksServiceImpl;

public class LoadParksServiceImplTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private LoadParksServiceImpl service;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		service = new LoadParksServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testLoadParksWithOnePark() {
		Long pid = new Long(1);
		Park park = new Park(pid);
		List<Park> parks = new ArrayList<Park>();
		parks.add(park);
		PersistenceManager pm = getPersistenceManager();

		try {
			pm.getObjectById(Park.class, pid);
			fail("Park found in db, but should not exist");
		} catch (JDOObjectNotFoundException e) {
			assertTrue(true);
		} finally {
			pm.close();
		}

		service.loadParks(parks);

		// Initialize failing park
		Park dbPark = new Park(new Long(0));
		pm = getPersistenceManager();
		// now park should exist in db
		try {
			// Set dbPark to the one in the database
			dbPark = pm.getObjectById(Park.class, pid);
		} catch (JDOObjectNotFoundException e) {
			fail("Park not found in db, but should exist");
		} finally {
			pm.close();
		}

		// Should pass, if dbPark found the right Park, else fail
		assertEquals(dbPark.getPid(), pid);

	}

	@Test
	public void testLoadParkswithTwentyParks() {
		List<Park> parks = new ArrayList<Park>();
		for (int i = 1; i <= 20; i++) {
			Long pid = new Long(i);
			Park park = new Park(pid);
			parks.add(park);
		}
		PersistenceManager pm = getPersistenceManager();

		service.loadParks(parks);

		// Initialize failing park
		Park dbPark = new Park(new Long(0));
		pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Park.class);
			List<Park> dbParks = (List<Park>) q.execute();
			assertEquals(20, dbParks.size());
			assertEquals(new Long(1), dbParks.get(0).getPid());
			assertEquals(new Long(20), dbParks.get(19).getPid());
		} finally {
			pm.close();
		}
	}

	@Test
	public void testGetAllParksFromOnePark() {
		Long pid = new Long(1);
		String pname1 = "pname1";
		addOneParkWithPidAndPname(pid, pname1);

		List<Park> gettedParks = service.getParks();

		assertEquals(1, gettedParks.size());
		assertEquals(gettedParks.get(0).getPid(), pid);
	}

	@Test
	public void testGetOneParkFromTwoParks() {
		Long pid1 = new Long(1);
		String pname1 = "pname1";
		Long pid2 = new Long(2);
		String pname2 = "pname2";

		addOneParkWithPidAndPname(pid1, pname1);
		addOneParkWithPidAndPname(pid2, pname2);

		try {
			Park park1 = service.getPark(pid1);
			assertEquals(pid1, park1.getPid());
			assertEquals(pname1, park1.getPname());
			Park park2 = service.getPark(pid2);
			assertEquals(pid2, park2.getPid());
			assertEquals(pname2, park2.getPname());

		} catch (DatabaseException e) {
			fail("Should not throw exception");
		}

	}

	@Test
	public void testGetNonExistingPark() {
		Long pid1 = new Long(1);
		String pname1 = "pname1";

		addOneParkWithPidAndPname(pid1, pname1);

		try {
			Long noPid = new Long(10);
			service.getPark(noPid);
			fail("Should have thrown exception");
		} catch (DatabaseException e) {
			assertTrue(true); // caught the exception
		} catch (Exception e) {
			fail("Wrong exception thrown");
		}

	}

	@Test
	public void testGetMultipleParksUsingPidList() {
		Long pid1 = new Long(1);
		String pname1 = "pname1";
		Long pid2 = new Long(2);
		String pname2 = "pname2";
		Long pid3 = new Long(3);
		String pname3 = "pname3";
		Long pid4 = new Long(4);
		String pname4 = "pname4";

		addOneParkWithPidAndPname(pid1, pname1);
		addOneParkWithPidAndPname(pid2, pname2);
		addOneParkWithPidAndPname(pid3, pname3);
		addOneParkWithPidAndPname(pid4, pname4);

		List<Long> pidList = new ArrayList<Long>();
		pidList.add(pid1);
		pidList.add(pid3);

		try {
			List<Park> parks = service.getParks(pidList);
			assertEquals(pname3, parks.get(1).getPname());
			assertEquals(pname1, parks.get(0).getPname());

			List<Long> parksPids = new ArrayList<Long>();
			for (Park p : parks) {
				parksPids.add(p.getPid());
			}
			assertTrue(parksPids.contains(pid1));
			assertTrue(parksPids.contains(pid3));
			assertFalse(parksPids.contains(pid2));
			assertFalse(parksPids.contains(pid4));

		} catch (Exception e) {
			fail(e.getMessage()); // caught the exception
		}

	}

	@Test
	public void testGetAllParkNamesFromOnePark() {
		Long pid = new Long(1);
		String pname1 = "pname1";
		addOneParkWithPidAndPname(pid, pname1);

		String[] parkNames = service.getParkNames();

		assertEquals(1, parkNames.length);
		assertEquals(pname1, parkNames[0]);
	}
	
	@Test
	public void testGetAllParkNamesFromTwoParks() {
		Long pid1 = new Long(1);
		String pname1 = "pname1";
		addOneParkWithPidAndPname(pid1, pname1);
		Long pid2 = new Long(2);
		String pname2 = "pname2";
		addOneParkWithPidAndPname(pid2, pname2);

		String[] parkNames = service.getParkNames();

		assertEquals(2, parkNames.length);
		assertEquals(pname1, parkNames[0]);
		assertEquals(pname2, parkNames[1]);
	}

	@Test
	public void testGetAllParkNamesFromSixParks() {
		Long pid1 = new Long(1);
		String pname1 = "pname1";
		Long pid2 = new Long(2);
		String pname2 = "pname2";
		Long pid3 = new Long(3);
		String pname3 = "pname3";
		Long pid4 = new Long(4);
		String pname4 = "pname4";
		Long pid5 = new Long(5);
		String pname5 = "pname5";

		addOneParkWithPidAndPname(pid1, pname1);
		addOneParkWithPidAndPname(pid2, pname2);
		addOneParkWithPidAndPname(pid3, pname3);
		addOneParkWithPidAndPname(pid4, pname4);
		addOneParkWithPidAndPname(pid5, pname5);

		String[] parkNames = service.getParkNames();

		assertEquals(5, parkNames.length);
		
		assertEquals(pname1, parkNames[0]);
		assertEquals(pname2, parkNames[1]);
		assertEquals(pname3, parkNames[2]);
		assertEquals(pname4, parkNames[3]);
		assertEquals(pname5, parkNames[4]);
	}

	private Park addOneParkWithPidAndPname(Long pid, String pname) {
		Park park = new Park(pid, pname, true, new Integer(1010), "ABC Street", "East Street", "North Street", 100.00,
				200.00, 10.00, "Neighbourhood", null);
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.makePersistent(park);
		} finally {
			pm.close();
		}
		return park;
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
