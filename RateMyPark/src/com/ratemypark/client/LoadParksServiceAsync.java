package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoadParksServiceAsync {

	void loadParks(AsyncCallback<List<Park>> callback);

	void getParks(AsyncCallback<List<Park>> callback);

	void getParkNames(AsyncCallback<String[]> callback);

	void getPark(Long parkID, AsyncCallback<Park> callback);

}
