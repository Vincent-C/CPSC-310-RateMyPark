package com.ratemypark.client;

public class UserNameExistsException extends Exception {

	public UserNameExistsException(){
		super();
	}
	
	public UserNameExistsException(String message){
		super(message);
	}
	
}
