package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("load-parks")
public interface LoadParksService extends RemoteService{
	
	// might want to return void
	public List<Park> loadParks();
	public List<Park> getParks();
	public String[] getParkNames();
	public Park getPark(Long parkID);
}
