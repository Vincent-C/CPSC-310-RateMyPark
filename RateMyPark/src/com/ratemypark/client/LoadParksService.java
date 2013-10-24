package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.server.Park;

@RemoteServiceRelativePath("load-parks")
public interface LoadParksService extends RemoteService{
	List<Park> loadParks();
}
