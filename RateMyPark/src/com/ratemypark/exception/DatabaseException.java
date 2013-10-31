package com.ratemypark.exception;

public class DatabaseException extends Exception{

	public DatabaseException(){
		super();
	}
	
	public DatabaseException(String message){
		super(message);
	}
}
