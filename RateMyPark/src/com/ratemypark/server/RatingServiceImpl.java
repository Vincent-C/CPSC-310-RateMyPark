package com.ratemypark.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.Rating;
import com.ratemypark.client.RatingService;
import com.ratemypark.client.Review;
import com.ratemypark.exception.RatingOutOfRangeException;

public class RatingServiceImpl extends RemoteServiceServlet implements RatingService {

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	@Override
	public void createRating(Long pid, String username, int rating) throws RatingOutOfRangeException {
		PersistenceManager pm = getPersistenceManager();
		try {
			// Check if it exists in db already
			Query q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			List<Rating> result = (List<Rating>) q.execute(pid, username);
			Rating r;
			if (!result.isEmpty()) {
				r = result.get(0);
			} else {
				r = new Rating(pid, username);
			}
			if (rating > Rating.MAX_VALUE || rating < Rating.MIN_VALUE) {
				throw new RatingOutOfRangeException("Rating was trying to be set out of range");
			}
			r.setRating(rating);
			pm.makePersistent(r);
		} finally {
			pm.close();
		}
	}

	@Override
	public Rating getRating(Long pid, String username) {
		PersistenceManager pm = getPersistenceManager();
		Rating ret = null;
		try {
			Query q = pm.newQuery(Rating.class, "pid == parkID && username == name");
			q.declareParameters("Long parkID, String name");
			List<Rating> result = (List<Rating>) q.execute(pid, username);
			if (!result.isEmpty()) {
				ret = result.get(0);
				pm.refresh(ret);
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public List<Rating> getRatings(Long pid) {
		PersistenceManager pm = getPersistenceManager();
		List<Rating> ret = new ArrayList<Rating>();
		try {
			Query q = pm.newQuery(Rating.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Rating> result = (List<Rating>) q.execute(pid);
			for (Rating r : result) {
				pm.refresh(r);
				ret.add(r);
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public Float averageRating(Long pid) {
		PersistenceManager pm = getPersistenceManager();
		Float ret = null;
		try {
			Query q = pm.newQuery(Rating.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Rating> result = (List<Rating>) q.execute(pid);
			if (!result.isEmpty()) {
				ret = new Float(0);
				for (Rating r : result) {
					pm.refresh(r);
					ret += r.getRating();
				}
				ret = ret / result.size();
				ret = (float) Math.round(ret * 100);
				ret = ret/100;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			pm.close();
		}
		return ret;
	}


	@Override
	public Integer totalNumRatings(Long pid) {
		PersistenceManager pm = getPersistenceManager();
		Integer ret;
		try {
			Query q = pm.newQuery(Rating.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Rating> result = (List<Rating>) q.execute(pid);
			ret = result.size();
		} finally {
			pm.close();
		}
		return ret;
	}
	
	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

}
