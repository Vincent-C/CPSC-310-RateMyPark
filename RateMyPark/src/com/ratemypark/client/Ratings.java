package com.ratemypark.client;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

@PersistenceCapable
public class Ratings implements IsSerializable{
	
	@PrimaryKey
	@Persistent
	private String username;
	
	@PrimaryKey
	@Persistent
	private Long pid;
	
	@Persistent
	private int ratingnumber;
	
	@Persistent
	private Date dateReviewed;
	
	private final int MIN_VALUE = 0;
	private final int MAX_VALUE = 5;
	
	public Ratings() {
		this.dateReviewed= new Date();
	}
	
	public Ratings(Long pid, String username){
		this();
		this.pid = pid;
		this.username = username;		
	}
	
	// Sets the rating value, and returns true if the number was valid, false otherwise
	public boolean setRating(int num) {
		if (MIN_VALUE <= num && num <= MAX_VALUE){
			// Check if rating is in range of 0-5
			ratingnumber = num;
			return true;
		} else {
			// throw exception that rating we're trying to set it out of range
//			throw new RatingOutOfRangeException("Rating is out of the range 0-5");
			return false;
		}
	}

	public int getRating() {
		return this.ratingnumber;
	}

	public String getUsername() {
		return this.username;
	}

	public Long getPid() {
		return this.pid;
	}
}

