package com.ratemypark.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ratemypark.client.LoadParksService;

public class LoadParksServiceImpl extends RemoteServiceServlet implements LoadParksService {

	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	@Override
	public List<Park> loadParks() {
		DomXMLParser parser = new DomXMLParser();
		return parser.parse();
	}

	private PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}
}
