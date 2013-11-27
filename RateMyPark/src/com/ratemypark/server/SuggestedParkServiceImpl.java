package com.ratemypark.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.client.Park;
import com.ratemypark.client.Rating;
import com.ratemypark.client.Review;
import com.ratemypark.client.ReviewService;
import com.ratemypark.client.SuggestedPark;
import com.ratemypark.client.SuggestedParkService;
import com.ratemypark.exception.DatabaseException;
import com.ratemypark.exception.UserNameException;

public class SuggestedParkServiceImpl extends RemoteServiceServlet implements SuggestedParkService {

	private java.util.Random rng;

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	@Override
	public SuggestedPark getRandomPark() throws DatabaseException {
		rng = new java.util.Random();
		int maxPid = getMaxParkId();
		int randomParkId = rng.nextInt(maxPid) + 1;

		SuggestedPark suggestedPark;
		PersistenceManager pm = getPersistenceManager();
		try {
			Park park = pm.getObjectById(Park.class, randomParkId);
			pm.refresh(park);
			Query q = pm.newQuery(Rating.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Rating> result = (List<Rating>) q.execute(park.getPid());

			int totalRating = 0, numRatings = 0;

			for (Rating r : result) {
				if (r.getPid() == park.getPid()) { // if statement just to ensure we have the right park
					totalRating += r.getRating();
					numRatings++;
				} else {
					System.out.println("SHOULDNT RUN IN ELSE CLAUSE");
				}
			}
			long avgRating = totalRating/numRatings;
			avgRating = (long) (Math.round(avgRating * 100.0) / 100.0);
			suggestedPark = new SuggestedPark(park, avgRating, numRatings);

		} catch (JDOObjectNotFoundException e) {
			suggestedPark = getRandomPark();
		} finally {
			pm.close();
		}

		return suggestedPark;
	}

	@Override
	public SuggestedPark getHighestRated() throws DatabaseException {
		int maxPid = getMaxParkId();
		int[] numRatings = new int[maxPid + 1];
		long[] ratingsTotal = new long[maxPid + 1];

		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Rating.class);
			List<Rating> result = (List<Rating>) q.execute();
			for (Rating r : result) {
				int id = r.getPid().intValue();
				numRatings[id]++;
				ratingsTotal[id] = ratingsTotal[id] + r.getRating();
			}
		} finally {
			pm.close();
		}

		long maxRating = 0;
		int maxNumRatings = 0;
		int maxParkId = 1;
		for (int i = 1; i < numRatings.length; i++) {
			if (numRatings[i] != 0) {
				ratingsTotal[i] = ratingsTotal[i] / numRatings[i];
				if (ratingsTotal[i] >= maxRating) {
					maxRating = ratingsTotal[i];
					maxParkId = i;
					maxNumRatings = numRatings[i];
				}
			}
		}

		Park park;
		SuggestedPark suggestedPark;
		pm = getPersistenceManager();
		try {
			park = pm.getObjectById(Park.class, maxParkId);
			maxRating = (long) (Math.round(maxRating * 100.0) / 100.0);
			suggestedPark = new SuggestedPark(park, maxRating, maxNumRatings);
		} catch (JDOObjectNotFoundException e) {
			suggestedPark = getRandomPark();
		} finally {
			pm.close();
		}
		return suggestedPark;
	}

	@Override
	public SuggestedPark getMostRated() throws DatabaseException {
		int maxPid = getMaxParkId();
		int[] numRatings = new int[maxPid + 1];
		long[] ratingsTotal = new long[maxPid + 1];

		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Rating.class);
			List<Rating> result = (List<Rating>) q.execute();
			for (Rating r : result) {
				int id = r.getPid().intValue();
				numRatings[id]++;
				ratingsTotal[id] = ratingsTotal[id] + r.getRating();
			}
		} finally {
			pm.close();
		}

		long maxRating = 0;
		int maxNumRatings = 0;
		int maxParkId = 1;
		for (int i = 1; i < numRatings.length; i++) {
			if (numRatings[i] != 0) {
				ratingsTotal[i] = ratingsTotal[i] / numRatings[i];
				if (numRatings[i] >= maxNumRatings) {
					maxParkId = i;
					maxRating = ratingsTotal[i];
					maxNumRatings = numRatings[i];
				}
			}
		}

		Park park;
		SuggestedPark suggestedPark;
		pm = getPersistenceManager();
		try {
			park = pm.getObjectById(Park.class, maxParkId);
			maxRating = (long) (Math.round(maxRating * 100.0) / 100.0);
			suggestedPark = new SuggestedPark(park, maxRating, maxNumRatings);
		} catch (JDOObjectNotFoundException e) {
			suggestedPark = getRandomPark();
		} finally {
			pm.close();
		}
		return suggestedPark;
	}

	// Queries the DB for all the parks the user has not yet rated
	@Override
	public List<Park> getNotYetRatedParks(String name){
		PersistenceManager pm = getPersistenceManager();
		List<Park> parks = new ArrayList<Park>();
		try {
			pm.refreshAll();
			Query q1 = pm.newQuery(Rating.class, "username == name");
			q1.declareParameters("String name");
			List<Rating> ratings = (List<Rating>) q1.execute(name);
			List<Long> ratedPIDs = new ArrayList<Long>();
			for (Rating rev : ratings) {
				ratedPIDs.add(rev.getPid());
			}
			Query q2 = pm.newQuery(Park.class);
			q2.setOrdering("pid desc");
			List<Park> result = (List<Park>) q2.execute();
			for (Park p : result) {
				if (!ratedPIDs.contains(p.getPid())){ // might not work, could use for loop instead
					parks.add(p);
				}
			}
		} finally {
			pm.close();
		}
		return parks;
	}
	
	@Override
	public List<SuggestedPark> getRatedParks(String name){
		PersistenceManager pm = getPersistenceManager();
		List<SuggestedPark> parks = new ArrayList<SuggestedPark>();
		try {
			pm.refreshAll();
			Query q1 = pm.newQuery(Rating.class, "username == name");
			q1.declareParameters("String name");
			List<Rating> ratings = (List<Rating>) q1.execute(name);

			Query q2 = pm.newQuery(Park.class);
			q2.setOrdering("pid desc");
			List<Park> result = (List<Park>) q2.execute();
			for (Park p : result) {
				for (Rating r : ratings){
					if (r.getPid().equals(p.getPid())){
						SuggestedPark sp = new SuggestedPark(p,r.getRating(),1);
						parks.add(sp);	
						break;
					}
				}
			}
		} finally {
			pm.close();
		}
		return parks;
	}
	

	private int getMaxParkId() {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Park.class);
			q.setOrdering("pid desc");
			List<Park> result = (List<Park>) q.execute();
			return result.get(0).getPid().intValue();
		} finally {
			pm.close();
		}
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}