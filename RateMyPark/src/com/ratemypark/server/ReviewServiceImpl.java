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

	public Review newReview(String username, Long pid, String parkName, String reviewText) throws DatabaseException {
		// TODO Exception handling
		PersistenceManager pm = getPersistenceManager();
		Review ret = null;
		Review review = new Review(username, pid, parkName, reviewText);
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
			Query q = pm.newQuery(Review.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Review> result = (List<Review>) q.execute(pid);
			for (Review r : result) {
				Review hackedReview = pm.detachCopy(r); // Prevents 'com.google.appengine.datanucleus.query.StreamingQueryResult' errors
				reviews.add(hackedReview);
			}
		} finally {
			pm.close();
		}
		return reviews;
	}
	
	@Override
	public List<Review> getReviewsForUser(String username) throws DatabaseException {
		PersistenceManager pm = getPersistenceManager();
		List<Review> reviews = new ArrayList<Review>();
		try {
			Query q = pm.newQuery(Review.class, "username == param");
			q.declareParameters("String param");
			List<Review> result = (List<Review>) q.execute(username);
			for (Review r : result) {
				Review hackedReview = pm.detachCopy(r); // Prevents 'com.google.appengine.datanucleus.query.StreamingQueryResult' errors
				reviews.add(hackedReview);
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
