package com.ratemypark.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoadParksService;
import com.ratemypark.client.Park;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;

public class LoadParksServiceImpl extends RemoteServiceServlet implements LoadParksService {

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	@Override
	public List<Park> loadParks() {
		PersistenceManager pm = getPersistenceManager();

		DomXMLParser parser = new DomXMLParser();

		List<Park> parks = parser.parse();
		try {
			for (Park p : parks) {
				pm.makePersistent(p);
				System.out.println("Wrote park to database: " + p.getPname());
			}
		} finally {
			pm.close();
		}
		// Not sure what I should return
		return parks;

	}

	@Override
	public List<Park> getParks() {
		PersistenceManager pm = getPersistenceManager();
		List<Park> parks = new ArrayList<Park>();
		try {
			Query q = pm.newQuery(Park.class);
			// q.setOrdering("pid desc");
			List<Park> result = (List<Park>) q.execute();
			for (Park p : result) {
				parks.add(p);
			}

		} finally {
			pm.close();
		}
		return parks;

	}

	@Override
	public String[] getParkNames() {
		PersistenceManager pm = getPersistenceManager();
		List<String> parkNames = new ArrayList<String>();
		try {
			Query q = pm.newQuery(Park.class);
			q.setOrdering("pid asc");
			List<Park> parks = (List<Park>) q.execute();
			for (Park p : parks) {
				parkNames.add(p.getPname());
			}

		} finally {
			pm.close();
		}
		return (String[]) parkNames.toArray(new String[0]);
	}

	// Get a specific Park
	@Override
	public Park getPark(Long parkID) {
		PersistenceManager pm = getPersistenceManager();
		Park park;
		try {
			Query q = pm.newQuery(Park.class, "pid == parkID");
			q.declareParameters("Long parkID");
			List<Park> parks = (List<Park>) q.execute(parkID);
			park = parks.get(0);
		} finally {
			pm.close();
		}
		return park;
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}
