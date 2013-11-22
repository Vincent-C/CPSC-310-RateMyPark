package com.ratemypark.client;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

@PersistenceCapable
public class Rating implements IsSerializable{
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long ratingID;
	
	@Persistent
	@Nonnull
	private String username;
	
	@Persistent
	@Nonnull
	private Long pid;
	
	@Persistent
	private int ratingnumber;
	
	@Persistent
	private Date dateReviewed;
	
	public static final int MIN_VALUE = 0;

	public static final int MAX_VALUE = 5;
	
	public Rating() {
		this.dateReviewed = new Date();
	}
	
	public Rating(Long pid, String username){
		this();
//		this.key = new KeyFactory.Builder("Park",pid).addChild("Account",username).getKey();
		this.pid = pid;
		this.username = username;		
	}
	
	// Sets the rating value, and returns true if the number was valid, false otherwise
	public boolean setRating(int num) {
		if (MIN_VALUE <= num && num <= MAX_VALUE){
			// Check if rating is in range of 0-5
			this.ratingnumber = num;
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


