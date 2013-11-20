package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.DatabaseException;
import com.ratemypark.exception.NotLoggedInException;
import com.ratemypark.exception.UserNameException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("review")
public interface ReviewService extends RemoteService {
	Review newReview(String username, Long pid, String reviewText) throws DatabaseException;
	List<Review> getReviews(Long pid) throws DatabaseException;
}