package com.ratemypark.client;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.DatabaseException;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("suggested-park")
public interface SuggestedParkService extends RemoteService {
	Park getRandomPark() throws DatabaseException;
}