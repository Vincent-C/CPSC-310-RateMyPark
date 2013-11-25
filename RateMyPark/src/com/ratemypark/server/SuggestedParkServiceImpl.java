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
import com.ratemypark.client.Review;
import com.ratemypark.client.ReviewService;
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
		int randomParkId = rng.nextInt(245) + 1;
		System.out.println("Random park: " + randomParkId);
		
		PersistenceManager pm = getPersistenceManager();
		try {
			Park park = pm.getObjectById(Park.class, randomParkId);
			pm.refresh(park);
			// Return the Account entity
			return park;
		} catch (JDOObjectNotFoundException e) {
			return getRandomPark();
		} finally {
			pm.close();
		}
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
	
}