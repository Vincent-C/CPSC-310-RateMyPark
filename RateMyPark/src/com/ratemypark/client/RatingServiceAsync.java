package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RatingServiceAsync {

	void createRating(Long pid, String username, int rate, AsyncCallback<Void> callback);
	void getRating(Long pid, String username, AsyncCallback<Integer> callback);
	void averageRating(Long pid, AsyncCallback<Float> callback);
	void getRatings(Long pid, AsyncCallback<List<Rating>> callback);
	void totalNumRatings(Long pid, AsyncCallback<Integer> callback);

}
