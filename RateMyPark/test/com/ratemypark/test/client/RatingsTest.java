package com.ratemypark.test.client;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ratemypark.client.Rating;

public class RatingsTest {

	Rating ratings;

	@Before
	public void setup(){
		Long pid = new Long(1);
		String accname = "account1";
		ratings = new Rating(pid,accname);
		
	}
	
	@Test
	public void testRatingsLongString() {
		assertEquals(new Long(1),ratings.getPid());
		assertEquals("account1",ratings.getUsername());
	}
	
	@Test
	public void testSetValidRating(){
		assertTrue(ratings.setRating(5));
		assertTrue(ratings.setRating(4));
		assertTrue(ratings.setRating(3));
		assertTrue(ratings.setRating(2));
		assertTrue(ratings.setRating(1));
		assertTrue(ratings.setRating(0));
	}
	
	@Test 
	public void testSetInvalidRating(){
		assertFalse(ratings.setRating(100));
		assertFalse(ratings.setRating(-1));
		assertFalse(ratings.setRating(6));
		assertFalse(ratings.setRating(10));
		assertFalse(ratings.setRating(-100));
	}
	
	@Test
	public void testGetValidRating(){
		ratings.setRating(5);
		assertEquals(5,ratings.getRating());
	}
	
	@Test
	public void testGetValidAfterInvalidRating(){
		ratings.setRating(5);
		assertFalse(ratings.setRating(100));
		assertEquals(5,ratings.getRating());
	}


}
