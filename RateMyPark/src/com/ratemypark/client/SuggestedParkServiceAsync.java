package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>SuggestedParkService</code>.
 */
public interface SuggestedParkServiceAsync {
	void getRandomPark(AsyncCallback<SuggestedPark> callback);
	void getHighestRated(AsyncCallback<SuggestedPark> callback);
	void getMostRated(AsyncCallback<SuggestedPark> callback);
	void getNotYetRatedParks(String name, AsyncCallback<List<Park>> callback);
	void getRatedParks(String name, AsyncCallback<List<SuggestedPark>> callback);
}
