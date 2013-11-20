package com.ratemypark.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoginInfo;
import com.ratemypark.client.Park;
import com.ratemypark.client.Review;
import com.ratemypark.client.ReviewService;
import com.ratemypark.exception.DatabaseException;

public class ReviewServiceImpl extends RemoteServiceServlet implements ReviewService {

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	public Review newReview(String username, Long pid, String reviewText) throws DatabaseException {
		// TODO Exception handling
		PersistenceManager pm = getPersistenceManager();
		Review ret = null;
		Review review = new Review(username, pid, reviewText);
		try {
			pm.makePersistent(review);
			ret = pm.detachCopy(review); // Client.rpc being stupid with dates...
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public List<Review> getReviews(Long pid) throws DatabaseException {
		PersistenceManager pm = getPersistenceManager();
		List<Review> reviews = new ArrayList<Review>();
		try {
			Query q = pm.newQuery(Review.class);
			List<Review> result = (List<Review>) q.execute();
			// Loop hack due to com.google.appengine.datanucleus.query.StreamingQueryResult serialization errors
			for (Review r : result) {
				if (r.getPid().equals(pid)) {
					Review hackedReview = pm.detachCopy(r);
					reviews.add(hackedReview); // Client.rpc being even more stupid with dates..
				}
			}
		} finally {
			pm.close();
		}
		return reviews;
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}
