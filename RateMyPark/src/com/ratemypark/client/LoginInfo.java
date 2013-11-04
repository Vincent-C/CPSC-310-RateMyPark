package com.ratemypark.client;

import java.io.Serializable;

public class LoginInfo implements Serializable {
	private String username;
	private String sessionID;
	private String firstName;
	private String lastName;
	
	public LoginInfo() {
		
	}
	
	public LoginInfo(String username, String session) {
		this.username = username;
		this.sessionID = session;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getSessionID() {
		return this.sessionID;
	}
	
	public String getName() {
		String fullName = this.firstName + this.lastName;
		return fullName;
	}
	
	public String getFirstName() {
		return this.firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
