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
	public Park getRandomPark() throws DatabaseException {
		rng = new java.util.Random();
		int maxPid = getMaxParkId();
		int randomParkId = rng.nextInt(maxPid) + 1;
		System.out.println("Random park: " + randomParkId);

		PersistenceManager pm = getPersistenceManager();
		try {
			Park park = pm.getObjectById(Park.class, randomParkId);
			pm.refresh(park);
			return park;
		} catch (JDOObjectNotFoundException e) {
			return getRandomPark();
		} finally {
			pm.close();
		}
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
		} catch (JDOObjectNotFoundException e) {
			throw new DatabaseException();
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
		pm = getPersistenceManager();
		try {
			park = pm.getObjectById(Park.class, maxParkId);
		} catch (JDOObjectNotFoundException e) {
			park = getRandomPark();
			maxRating = 0;
			maxNumRatings = 0;
		} finally {
			pm.close();
		}
		SuggestedPark suggestedPark = new SuggestedPark(park, maxRating, maxNumRatings);
		return suggestedPark;
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
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

}