package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoadParksServiceAsync {

	void loadXMLParks(AsyncCallback<Void> callback);

	void loadParks(List<Park> parks, AsyncCallback<Void> callback);
	
	void getParks(AsyncCallback<List<Park>> callback);

	void getParkNames(AsyncCallback<String[]> callback);

	void getPark(Long parkID, AsyncCallback<Park> callback);

	void getParks(List<Long> pids, AsyncCallback<List<Park>> callback);

}
