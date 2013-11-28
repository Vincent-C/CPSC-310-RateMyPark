package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.RatingOutOfRangeException;
import com.ratemypark.exception.UserNameException;

@RemoteServiceRelativePath("rating")
public interface RatingService extends RemoteService {
	 void createRating(Long pid, String username, int rating) throws RatingOutOfRangeException;
	 Integer getRating(Long pid, String username);
	 Float averageRating(Long pid);
	 List<Rating> getRatings(Long pid);
	 Integer totalNumRatings(Long pid);
}
