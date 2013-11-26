package com.ratemypark.client;


import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.DatabaseException;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("suggested-park")
public interface SuggestedParkService extends RemoteService {
	SuggestedPark getRandomPark() throws DatabaseException;
	SuggestedPark getHighestRated() throws DatabaseException;
	SuggestedPark getMostRated() throws DatabaseException;
	List<Park> getNotYetRatedParks(String name);
	List<SuggestedPark> getRatedParks(String name);
}