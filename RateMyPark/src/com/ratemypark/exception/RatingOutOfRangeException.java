package com.ratemypark.exception;

public class RatingOutOfRangeException extends Exception{

	public RatingOutOfRangeException(){
		super();
	}
	
	public RatingOutOfRangeException(String message){
		super(message);
	}
}
