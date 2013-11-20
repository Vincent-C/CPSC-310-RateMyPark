package com.ratemypark.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.UserNameException;

import javax.servlet.http.HttpSession;

/**
 * The async counterpart of <code>EditProfileService</code>.
 */
public interface ReviewServiceAsync {
	
	void newReview(String username, Long pid, String reviewText, AsyncCallback<Review> callback);
	void getReviews(Long pid, AsyncCallback<List<Review>> callback);
}
