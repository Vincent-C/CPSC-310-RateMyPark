package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.DatabaseException;

@RemoteServiceRelativePath("load-parks")
public interface LoadParksService extends RemoteService{
	
	// might want to return void
	public void loadXMLParks();
	void loadParks(List<Park> parks);
	public List<Park> getParks();
	public String[] getParkNames();
	public Park getPark(Long parkID) throws DatabaseException;
	List<Park> getParks(List<Long> pids);
}
