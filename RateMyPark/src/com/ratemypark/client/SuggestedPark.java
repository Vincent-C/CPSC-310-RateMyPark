package com.ratemypark.client;

import java.io.Serializable;

public class SuggestedPark implements Serializable {
	private Park park;
	private long rating;
	private int numRatings;
	
	public SuggestedPark() {
	}
	
	public SuggestedPark(Park park, long rating, int numRatings) {
		this.park = park;
		this.rating = rating;
		this.numRatings = numRatings;
	}

	public Park getPark() {
		return park;
	}

	public void setPark(Park park) {
		this.park = park;
	}

	public long getRating() {
		return rating;
	}

	public void setRating(long rating) {
		this.rating = rating;
	}

	public int getNumRatings() {
		return numRatings;
	}

	public void setNumRatings(int numRatings) {
		this.numRatings = numRatings;
	}

}
