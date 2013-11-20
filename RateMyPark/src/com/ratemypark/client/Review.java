package com.ratemypark.client;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

@PersistenceCapable
public class Review implements Serializable {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private Long pid;
	@Persistent
	private String username;
	@Persistent
	private String reviewText; // This should be type Text, otherwise we are limited to 500 character reviews. Too bad Text causes bugs..
	@Persistent
	private Date dateCreated;
	
	public Review() {
		this.dateCreated = new Date();
	}
	
	public Review(String username, Long pid, String reviewText) {
		this();
		this.username = username;
		this.pid  = pid;
		this.reviewText = reviewText;
	}

	public String getReviewText() {
		return this.reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public Long getPid() {
		return this.pid;
	}

	public String getUsername() {
		return this.username;
	}

	public Date getDateCreated() {
	    return this.dateCreated;
	}
	
}
	
