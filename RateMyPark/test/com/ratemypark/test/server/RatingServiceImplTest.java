package com.ratemypark.test.server;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.ratemypark.client.Rating;
import com.ratemypark.exception.RatingOutOfRangeException;
import com.ratemypark.server.RatingServiceImpl;

public class RatingServiceImplTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private RatingServiceImpl service;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		service = new RatingServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testNoRating() {
		Long pid = new Long(1);
		String username = "user1";
		PersistenceManager pm = getPersistenceManager();

		try {
			Query q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			List<Rating> result = (List<Rating>) q.execute(pid, username);
			assertTrue(result.size() == 0);
			q = pm.newQuery(Rating.class);
			result = (List<Rating>) q.execute();
			assertTrue(result.size() == 0);
		} finally {
			pm.close();
		}
	}

	@Test
	public void testAddOneRating() {
		Long pid = new Long(1);
		String username = "user1";
		int rating = 5;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid, username, rating);

			Query q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			List<Rating> result = (List<Rating>) q.execute(pid, username);
			assertTrue(result.size() == 1);
			Rating r = result.get(0);
			pm.refresh(r);
			assertEquals(pid, r.getPid());
			assertEquals(username, r.getUsername());
			assertEquals(rating, r.getRating());
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}
	
	@Test
	public void testGetOneRating() {
		Long pid = new Long(1);
		String username = "user1";
		int rating = 5;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid, username, rating);
			Integer r = service.getRating(pid, username);
			assertEquals(rating, r.intValue());
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}

	@Test
	public void testAddAndUpdateRating() {
		Long pid = new Long(2);
		String username = "user2";
		int rating = 1;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid, username, rating);
			int newRating = 2;
			service.createRating(pid, username, newRating);
			Query q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			List<Rating> result = (List<Rating>) q.execute(pid, username);
			assertTrue(result.size() == 1);
			Rating r = result.get(0);
			pm.refresh(r);
			assertEquals(pid, r.getPid());
			assertEquals(username, r.getUsername());
			assertEquals(newRating, r.getRating());

			int newestRating = 1;
			service.createRating(pid, username, newestRating);
			q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			result = (List<Rating>) q.execute(pid, username);
			assertTrue(result.size() == 1);
			r = result.get(0);
			pm.refresh(r);
			assertEquals(pid, r.getPid());
			assertEquals(username, r.getUsername());
			assertEquals(newestRating, r.getRating());
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}

	@Test
	public void testAddTwoRatingsForSamePark() {
		Long pid1 = new Long(1);
		String username1 = "user1";
		int rating1 = 1;
		Long pid2 = new Long(1);
		String username2 = "user2";
		int rating2 = 2;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid1, username1, rating1);
			service.createRating(pid2, username2, rating2);

			// Check there are 2 ratings in db
			Query q = pm.newQuery(Rating.class);
			List<Rating> result = (List<Rating>) q.execute();
			assertTrue(result.size() == 2);

			q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			result = (List<Rating>) q.execute(pid1, username1);
			assertTrue(result.size() == 1);
			Rating r = result.get(0);
			pm.refresh(r);
			assertEquals(pid1, r.getPid());
			assertEquals(username1, r.getUsername());
			assertEquals(rating1, r.getRating());

			q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			result = (List<Rating>) q.execute(pid2, username2);
			assertTrue(result.size() == 1);
			r = result.get(0);
			pm.refresh(r);
			assertEquals(pid2, r.getPid());
			assertEquals(username2, r.getUsername());
			assertEquals(rating2, r.getRating());
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}

	public void testAddTwoRatingsForSameUser() {
		Long pid1 = new Long(1);
		String username1 = "user1";
		int rating1 = 1;
		Long pid2 = new Long(2);
		String username2 = "user1";
		int rating2 = 2;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid1, username1, rating1);
			service.createRating(pid2, username2, rating2);

			int newRating = 3;
			service.createRating(pid2, username2, newRating);

			// Check there are 2 ratings in db
			Query q = pm.newQuery(Rating.class);
			List<Rating> result = (List<Rating>) q.execute();
			assertTrue(result.size() == 2);

			q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			result = (List<Rating>) q.execute(pid1, username1);
			assertTrue(result.size() == 1);
			Rating r = result.get(0);
			pm.refresh(r);
			assertEquals(pid1, r.getPid());
			assertEquals(username1, r.getUsername());
			assertEquals(rating1, r.getRating());

			q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			result = (List<Rating>) q.execute(pid2, username2);
			assertTrue(result.size() == 1);
			r = result.get(0);
			pm.refresh(r);
			assertEquals(pid2, r.getPid());
			assertEquals(username2, r.getUsername());
			assertEquals(newRating, r.getRating());
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}

	@Test
	public void testGetRatingsFromOnePark() {
		Long pid1 = new Long(1);
		String username1 = "user1";
		int rating1 = 1;
		String username2 = "user2";
		int rating2 = 2;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid1, username1, rating1);
			service.createRating(pid1, username2, rating2);

			List<Rating> ratings = service.getRatings(pid1);
			assertTrue(ratings.size()==2);
			
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
	}
	
	@Test
	public void testEmptyAverage() {
		Long pid1 = new Long(1);
		PersistenceManager pm = getPersistenceManager();
		try {
			assertNull(service.averageRating(pid1));
		}finally {
			pm.close();
		}
		
	}
	
	@Test 
	public void testAverageRatingForOnePark(){
		Long pid1 = new Long(1);
		String username1 = "user1";
		int rating1 = 1;
		String username2 = "user2";
		int rating2 = 3;

		// Check that the rating is now in db
		PersistenceManager pm = getPersistenceManager();
		try {
			service.createRating(pid1, username1, rating1);
			service.createRating(pid1, username2, rating2);

			float avg = service.averageRating(pid1);
			assertTrue(avg==2);
			
			service.createRating(pid1, username1, 3);
			service.createRating(pid1, username2, 3);
			avg = service.averageRating(pid1);
			assertTrue(avg==3);
			
			service.createRating(pid1, username1, 5);
			service.createRating(pid1, username2, 4);
			avg = service.averageRating(pid1);
			assertTrue(avg==4.5);
			
			service.createRating(pid1, username1, 1);
			service.createRating(pid1, username2, 2);
			service.createRating(pid1, "username", 3);
			avg = service.averageRating(pid1);
			assertTrue(avg==2);
			
			
		} catch (RatingOutOfRangeException e) {
			fail("Shouldn't throw exception");
		} finally {
			pm.close();
		}
		
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
