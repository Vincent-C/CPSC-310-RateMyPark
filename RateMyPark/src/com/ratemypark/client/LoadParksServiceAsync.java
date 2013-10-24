package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ratemypark.server.Park;

public interface LoadParksServiceAsync {

	void loadParks(AsyncCallback<List<Park>> callback);

}
