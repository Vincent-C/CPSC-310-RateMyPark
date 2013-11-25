package com.ratemypark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>SuggestedParkService</code>.
 */
public interface SuggestedParkServiceAsync {
	void getRandomPark(AsyncCallback<Park> callback);
}
